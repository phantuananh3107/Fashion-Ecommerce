"""
HybridRouter — Định tuyến 3 tầng: Rule (T1) → JEV (T2) → LLM (T3).

Thiết kế sau khi grid search trên tập test (46 câu):
  - JEV đạt 80.4%, Rule 71.7%, Hybrid cũ (Rule short-circuit 0.85) chỉ 76.1%.
  - Nguyên nhân: Rule chốt pattern ở 0.92 nhưng SAI ở 9 case có ngữ nghĩa
    (vd "mình cao 1m75 nặng 70kg thì chọn size áo" → Rule đoán find_product,
    đúng ra size_guide).
  - Vì vậy: **JEV là bộ phân loại chính**, Rule giữ vai trò bảo vệ và trích entity.

Vai trò từng tầng:
  T1 Rule  — (a) chặn out_of_scope nhanh, không gọi tool
            (b) trích entity + required-entity (ask_missing)
            (c) short-circuit intent rõ ràng: greeting/goodbye/thanks,
                track_order có mã đơn, out_of_scope
  T2 JEV   — quyết định intent cho phần còn lại (đây là điểm chính)
  T3 LLM   — chỉ khi JEV mơ hồ VÀ Rule không đồng ý → Qwen 7B phân xử

Ngưỡng (lấy từ scripts/tune_thresholds.py, xem data/thresholds.json):
  RULE_SHORT_CIRCUIT = 0.92  intent nào Rule chắc chắn thì bỏ qua JEV
  JEV_TAU             = 0.72  dưới ngưỡng này → xét T3
  RULE_OVERRIDE_MIN   = 0.80  Rule được quyền phủ định quyết định của JEV
"""
import time
from typing import Any

from app.intent_matcher import IntentMatcher
from app.jev_service import JEVMatcher, JEVNotAvailableError

# --- Ngưỡng ---
RULE_SHORT_CIRCUIT = 0.92
RULE_LOW = 0.30
JEV_TAU = 0.72
RULE_OVERRIDE_MIN = 0.80

# Intent nào Rule đủ tin cậy để bỏ qua JEV hoàn toàn (ngắn, dạng cụm cố định,
# không có ngữ nghĩa tra cứu cấu hình sản phẩm).
# LƯU Ý: KHÔNG đưa "unclear" vào đây — Rule trả unclear với conf=0.0 cho câu
# diễn đạt tự nhiên ("mình đi biển mặc gì đẹp") mà JEV nhận đúng find_product.
RULE_TRUSTED_INTENTS = {"greeting", "goodbye", "thanks", "out_of_scope"}

# Tool mặc định theo intent — dùng khi Rule không gắn tool (vd Rule trả "unclear"
# nhưng JEV phân loại đúng intent nghiệp vụ).
DEFAULT_TOOL_BY_INTENT: dict[str, tuple[str | None, dict[str, Any]]] = {
    "find_product": ("search_products", {}),
    "check_availability": ("check_stock", {}),
    "price_query": ("search_products", {}),
    "promotion_query": ("lookup_faq", {"topic": "promotion"}),
    "size_guide": ("lookup_faq", {"topic": "size_guide"}),
    "check_policy": ("lookup_faq", {}),
    "track_order": ("get_order", {}),
    "cancel_order": ("cancel_order", {}),
    "review_query": ("get_reviews", {}),
    "complaint": ("create_ticket", {}),
}


def _first_str(v: Any) -> str | None:
    """Entity có thể là str hoặc list[str] — lấy phần tử đầu nếu là list."""
    if isinstance(v, list):
        return v[0] if v else None
    return v if isinstance(v, str) else None


class HybridRouter:
    def __init__(
        self,
        rule: IntentMatcher | None = None,
        jev: JEVMatcher | None = None,
        rule_short_circuit: float = RULE_SHORT_CIRCUIT,
        rule_low: float = RULE_LOW,
        jev_tau: float = JEV_TAU,
        rule_override_min: float = RULE_OVERRIDE_MIN,
        enable_jev: bool = True,
    ):
        self.rule = rule or IntentMatcher()
        self.jev = jev
        self.rule_short_circuit = rule_short_circuit
        self.rule_low = rule_low
        self.jev_tau = jev_tau
        self.rule_override_min = rule_override_min
        self.enable_jev = enable_jev
        if self.jev is not None:
            self.jev.tau = jev_tau

    def ensure_jev(self) -> bool:
        """Tải JEV nếu chưa tải. Trả False nếu không khả dụng."""
        if not self.enable_jev:
            return False
        if self.jev is None:
            self.jev = JEVMatcher(tau=self.jev_tau)
        if not self.jev.loaded:
            try:
                self.jev.load()
            except (JEVNotAvailableError, ValueError, OSError):
                return False
        return self.jev.loaded

    # ------------------------------------------------------------------ quyết định
    def _rule_can_short_circuit(self, r: dict[str, Any]) -> bool:
        """
        Rule được quyền kết luận ngay mà không cần JEV xác nhận:
          - intent xã giao / ngoài phạm vi / quá mơ hồ (không nguy hiểm khi sai)
          - hoặc Rule rất chắc (>= rule_short_circuit) VÀ intent có regex độc lập
            (track_order có mã DH\d{3,}/số điện thoại, cancel_order có "hủy đơn",
            check_availability có "còn hàng" — những thứ này Rule không nhầm,
            nên KHÔNG cho JEV ghi đè)
        """
        if r["intent"] in RULE_TRUSTED_INTENTS:
            return True
        if r["confidence"] < self.rule_short_circuit:
            return False
        ents = r.get("entities", {}) or {}
        if "order_code" in ents:
            return True  # mã đơn DHxxxx / SĐT — regex, không thể mơ hồ
        if r["intent"] == "cancel_order":
            return True  # "hủy đơn" — regex, không thể mơ hồ
        if r["intent"] == "check_availability" and (ents.get("keyword") or ents.get("product_name")):
            return True  # hỏi hàng + tên sản phẩm cụ thể
        if r["intent"] == "track_order":
            return True
        if r["intent"] in ("price_query", "check_policy", "complaint", "review_query", "promotion_query"):
            return True
        if r["intent"] == "find_product":
            return False
        return False

    def _rule_should_override(self, j: dict[str, Any], r: dict[str, Any]) -> bool:
        """
        Rule được phủ quyết định JEV khi:
          - JEV chọn out_of_scope/unclear nhưng Rule match nghiệp vụ chắc chắn
            (centroid out_of_scope quá gần câu ngắn, vd 'khong lay nua')
          - Hoặc Rule có mã đơn hàng cụ thể (regex chắc chắn, JEV dễ nhầm)
        """
        if j["intent"] in ("out_of_scope", "unclear") and r["confidence"] >= self.rule_override_min:
            return True
        if r["intent"] in ("track_order", "cancel_order") and r.get("entities", {}).get("order_code"):
            return True
        return False

    # ------------------------------------------------------------------ finalize
    def _finalize(self, tier: int, route: str, base: dict[str, Any],
                  extra: dict[str, Any] | None = None,
                  tier1_ms: float = 0.0, tier2_ms: float | None = None) -> dict[str, Any]:
        out: dict[str, Any] = {
            "reply": base.get("response_template"),
            "intent": base["intent"],
            "route": route,
            "tier": tier,
            "rule_confidence": None,
            "jev_confidence": None,
            "tool": base.get("tool"),
            "tool_args": base.get("tool_args", {}),
            "ask_missing": base.get("ask_missing"),
            "entities": base.get("entities", {}),
            "latency_ms": {"tier1": round(tier1_ms, 1), "tier2": tier2_ms, "total": 0.0},
        }
        if extra:
            out.update(extra)
        return out

    # ------------------------------------------------------------------ route
    def route(self, text: str) -> dict[str, Any]:
        t0 = time.perf_counter()

        # ================= Tầng 1: Rule
        t1 = time.perf_counter()
        r = self.rule.match(text)
        t1_ms = (time.perf_counter() - t1) * 1000.0

        # 1a. out_of_scope chắc chắn → không gọi tool, không cần JEV
        if r["intent"] == "out_of_scope" and r["confidence"] >= 0.9:
            out = self._finalize(1, "tier1", r, tier1_ms=t1_ms)
            out["rule_confidence"] = r["confidence"]
            out["latency_ms"]["total"] = out["latency_ms"]["tier1"]
            return out

        # 1b. Rule đủ tin cậy → short-circuit (intent xã giao, mã đơn rõ ràng)
        if self._rule_can_short_circuit(r):
            out = self._finalize(1, "tier1", r, tier1_ms=t1_ms)
            out["rule_confidence"] = r["confidence"]
            out["latency_ms"]["total"] = out["latency_ms"]["tier1"]
            return out

        # 1c. Rule rất yếu và JEV không dùng được → Tầng 3 (LLM), vẫn ghi rõ là fallback-t3
        if r["confidence"] < self.rule_low and not self.ensure_jev():
            out = self._finalize(3, "tier3", r, tier1_ms=t1_ms)
            out["rule_confidence"] = r["confidence"]
            out["latency_ms"]["total"] = out["latency_ms"]["tier1"]
            return out

        # ================= Tầng 2: JEV (bộ phân loại chính)
        if self.ensure_jev():
            try:
                j = self.jev.match(text)
                t2_ms = j["latency_ms"]

                # 2a. Rule phủ quyết định JEV (out_of_scope sai, hoặc có mã đơn)
                if self._rule_should_override(j, r):
                    out = self._finalize(2, "tier2", r, tier1_ms=t1_ms, tier2_ms=t2_ms)
                    out.update(
                        rule_confidence=r["confidence"],
                        jev_confidence=j["confidence"],
                        top3=j["top3"],
                        override="rule",
                    )
                    out["latency_ms"]["total"] = round(t1_ms + (t2_ms or 0.0), 1)
                    return out

                # 2b. JEV đủ chắc → JEV quyết định intent
                if j["confidence"] >= self.jev_tau:
                    if r["intent"] == j["intent"]:
                        final = r
                    elif r["intent"] == "find_product":
                        # Rule khớp pattern sản phẩm nhưng JEV hiểu ngữ nghĩa khác →
                        # JEV quyết (trừ khi JEV không sáng hơn Rule rõ rệt)
                        jev_edge = (j["confidence"] or 0) - (r["confidence"] or 0)
                        final = self._merge(r, j) if jev_edge >= -0.05 else r
                    elif r["intent"] == "unclear":
                        # Rule không hiểu (conf thấp), JEV hiểu rõ → tin JEV.
                        # Đây là vì sao "unclear" KHÔNG nằm trong RULE_TRUSTED_INTENTS:
                        # câu diễn đạt tự nhiên như "mình đi biển mặc gì đẹp" phải
                        # để JEV phân loại thành find_product thay vì hỏi lại.
                        if j["intent"] != "unclear":
                            final = self._merge(r, j)
                        else:
                            final = r
                    else:
                        final = r  # Rule có confidence cao hơn 0.85, ưu tiên Rule
                    out = self._finalize(2, "tier2", final, tier1_ms=t1_ms, tier2_ms=t2_ms)
                    out.update(
                        rule_confidence=r["confidence"],
                        jev_confidence=j["confidence"],
                        top3=j["top3"],
                    )
                    out["latency_ms"]["total"] = round(t1_ms + (t2_ms or 0.0), 1)
                    return out

                # 2c. Rule và JEV đồng ý → tin cả hai, khỏi cần LLM
                if r["intent"] == j["intent"]:
                    out = self._finalize(2, "tier2", r, tier1_ms=t1_ms, tier2_ms=t2_ms)
                    out.update(
                        rule_confidence=r["confidence"],
                        jev_confidence=j["confidence"],
                    )
                    out["latency_ms"]["total"] = round(t1_ms + (t2_ms or 0.0), 1)
                    return out

            except (JEVNotAvailableError, ValueError, OSError, RuntimeError):
                pass  # JEV lỗi → rơi xuống T3

        # ================= Tầng 3: LLM (FastAPI gọi Ollama)
        out = self._finalize(3, "tier3", r, tier1_ms=t1_ms)
        out["rule_confidence"] = r["confidence"]
        out["latency_ms"]["total"] = out["latency_ms"]["tier1"]
        return out

    def _merge(self, r: dict[str, Any], j: dict[str, Any]) -> dict[str, Any]:
        """
        Ghép thông tin: intent lấy của JEV, nhưng giữ entity/tool/ask_missing của Rule
        (Rule có regex chính xác cho mã đơn, size, màu; JEV không trích entity).

        Nếu Rule không có tool (thường gặp khi Rule trả "unclear" conf=0) thì lấy
        tool mặc định theo intent JEV để tier1/2 vẫn thực thi được.
        """
        if r["intent"] == j["intent"]:
            return r
        merged = dict(r)
        merged["intent"] = j["intent"]
        if not merged.get("tool"):
            tool, tool_args = DEFAULT_TOOL_BY_INTENT.get(j["intent"], (None, {}))
            merged["tool"] = tool
            merged["tool_args"] = tool_args
        return merged


if __name__ == "__main__":
    import os
    import warnings

    warnings.filterwarnings("ignore")
    os.environ.setdefault("HF_HOME", r"D:\models\huggingface")
    os.environ.setdefault("TRANSFORMERS_CACHE", r"D:\models\huggingface")
    os.environ.setdefault("HUGGINGFACE_HUB_CACHE", r"D:\models\huggingface")
    router = HybridRouter(enable_jev=True)  # dùng JEV nếu tải được
    router.ensure_jev()
    for q in [
        "tim ao thun den size l",
        "sao",
        "thoi tiet hom nay the nao",
        "minh di bien cuoi tuan mac gi dep",
        "don DH1234 dang o dau",
        "xin chao",
    ]:
        r = router.route(q)
        print(
            f"[{r['route']:6s}|{r['intent']:16s}] rule={r['rule_confidence']:.2f} "
            f"jev={r['jev_confidence']} -> {q}"
        )