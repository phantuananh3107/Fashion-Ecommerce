import json
import re
import unicodedata
from pathlib import Path
from typing import Any

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
INTENTS_FILE = DATA_DIR / "intents.json"

THRESHOLD = 0.3


def normalize_text(text: str) -> str:
    """Chuẩn hóa: lowercase, bỏ dấu câu dư, gộp khoảng trắng."""
    text = text.lower().strip()
    text = re.sub(r"[!?,;]+", " ", text)
    text = re.sub(r"\s+", " ", text)
    return text


def remove_accents(text: str) -> str:
    """Bỏ dấu tiếng Việt để match cả không dấu."""
    text = text.replace("đ", "d").replace("Đ", "D")
    nfkd = unicodedata.normalize("NFKD", text)
    return "".join(c for c in nfkd if not unicodedata.combining(c))


def expand_teencode(text: str, teencode_map: dict[str, str]) -> str:
    """Mở rộng teencode: ko→không, dc→được, sp→sản phẩm..."""
    words = text.split()
    expanded = [teencode_map.get(w, w) for w in words]
    return " ".join(expanded)


def has_phrase(haystack_tokens: list[str], needle: str) -> bool:
    """
    Kiểm tra cụm từ có xuất hiện liên tiếp trong chuỗi token hay không.
    Tránh false-positive do substring: 'gia' KHÔNG khớp 'giao', 'ao' KHÔNG khớp 'sao'.
    """
    needle_tokens = needle.split()
    if not needle_tokens:
        return False
    n = len(needle_tokens)
    return any(
        haystack_tokens[i:i + n] == needle_tokens for i in range(len(haystack_tokens) - n + 1)
    )


def norm_variants(keyword: str) -> tuple[str, str]:
    """Trả về (bản có dấu chuẩn hóa, bản không dấu) của keyword để so sánh."""
    norm = normalize_text(keyword)
    return norm, remove_accents(norm)


class IntentMatcher:
    def __init__(self, intents_file: Path = INTENTS_FILE):
        data = json.loads(intents_file.read_text(encoding="utf-8"))
        self.intents = data["intents"]
        self.entity_patterns = data.get("entity_patterns", {})
        self.out_of_scope_keywords = data.get("out_of_scope_keywords", [])
        self.teencode_map = data.get("teencode_map", {})
        # compile patterns
        for intent in self.intents:
            intent["_compiled_patterns"] = [
                re.compile(p, re.IGNORECASE) for p in intent.get("patterns", [])
            ]
        self.shopping_signals = [
            "áo", "ao", "quần", "quan", "váy", "vay", "đầm", "dam", "giày", "giay",
            "túi", "tui", "đơn", "don", "shop", "sản phẩm", "san pham", "size",
            "màu", "mau", "hàng", "hang", "thanh toán", "mua",
        ]

    def _score_intent(self, text: str, text_no_accents: str, intent: dict) -> tuple[float, int]:
        """Trả về (score 0..1, số keyword khớp)."""
        score = 0.0

        for pat in intent["_compiled_patterns"]:
            if pat.search(text) or pat.search(text_no_accents):
                score = 0.92
                break

        keywords = intent.get("keywords", [])
        matched = 0
        for kw in keywords:
            kw_norm, kw_na = norm_variants(kw)
            if has_phrase(text.split(), kw_norm) or has_phrase(text_no_accents.split(), kw_na):
                matched += 1
        if matched:
            kw_score = min(0.75, 0.35 + 0.12 * matched)
            score = max(score, kw_score)

        return score, matched

    def _is_out_of_scope(self, text: str, text_no_accents: str) -> bool:
        toks = text.split()
        toks_na = text_no_accents.split()
        for kw in self.out_of_scope_keywords:
            kw_norm, kw_na = norm_variants(kw)
            if has_phrase(toks, kw_norm) or has_phrase(toks_na, kw_na):
                return True
        return False

    def _has_shopping_signal(self, text: str, text_no_accents: str) -> bool:
        toks = text.split()
        toks_na = text_no_accents.split()
        return any(
            has_phrase(toks, normalize_text(s)) or has_phrase(toks_na, normalize_text(s))
            for s in self.shopping_signals
        )

    def _extract_entities(self, text: str) -> dict[str, Any]:
        entities: dict[str, Any] = {}
        for entity_name, pattern in self.entity_patterns.items():
            matches = re.findall(pattern, text, re.IGNORECASE)
            if matches:
                entities[entity_name] = matches[0] if len(matches) == 1 else matches
        return entities

    def _resolve(self, best: dict, score: float, text: str, text_na: str,
                 entities: dict, matched_kw: dict[str, int]) -> tuple[dict, float]:
        """Quy tắc phân xử khi multiple intent cùng điểm / chồng nhau."""
        name = best["name"]

        def has(*kws: str) -> bool:
            toks = text.split()
            toks_na = text_na.split()
            return any(
                has_phrase(toks, normalize_text(k)) or has_phrase(toks_na, normalize_text(k))
                for k in kws
            )

        if has("hủy đơn", "huy don", "không lấy", "khong lay", "hủy hàng") and \
                name in ("track_order", "find_product", "unclear"):
            return self._by_name("cancel_order"), max(score, 0.9)

        if name == "find_product" and has(
            "còn hàng", "con hang", "còn không", "con khong",
            "còn size", "con size", "còn màu", "con mau",
        ):
            return self._by_name("check_availability"), max(score, 0.85)

        if name in ("find_product", "price_query", "unclear") and has(
            "giá bao nhiêu", "gia bao nhieu", "bao nhiêu tiền", "bao nhieu tien",
            "giá là bao nhiêu", "gia la bao nhieu",
        ):
            return self._by_name("price_query"), max(score, 0.85)

        if name == "find_product" and text.split()[:1] in (["giá"], ["gia"]):
            return self._by_name("price_query"), max(score, 0.85)

        if name in ("find_product", "unclear") and has(
            "đổi trả", "doi tra", "trả hàng", "tra hang", "chính sách", "chinh sach",
            "thanh toán", "thanh toan", "bảo hành", "bao hanh", "ship",
        ):
            return self._by_name("check_policy"), max(score, 0.8)

        if name in ("find_product", "price_query", "unclear") and has(
            "đánh giá", "danh gia", "review", "nhận xét", "nhan xet", "tốt không", "tot khong",
        ):
            return self._by_name("review_query"), max(score, 0.8)

        if name in ("find_product", "unclear") and has(
            "bị lỗi", "bi loi", "giao sai", "khiếu nại", "khieu nai",
            "chất lượng kém", "chan luong kem", "phàn nàn", "phan nan", "hàng lỗi",
        ):
            return self._by_name("complaint"), max(score, 0.85)

        if name in ("check_availability", "find_product", "unclear") and has(
            "voucher", "khuyến mãi", "khuyen mai", "giảm giá", "giam gia",
            "freeship", "mã giảm", "ma giam",
        ):
            return self._by_name("promotion_query"), max(score, 0.8)

        if name == "find_product" and ("order_code" in entities or has("đơn của mình", "don cua minh")):
            return self._by_name("track_order"), max(score, 0.85)

        if name == "find_product" and has(
            "chào", "chao", "hello", "xin chào", "xin chao",
        ) and len(text.split()) <= 3:
            return self._by_name("greeting"), max(score, 0.8)

        return best, score

    def _by_name(self, name: str) -> dict:
        return next(i for i in self.intents if i["name"] == name)

    def match(self, raw_text: str) -> dict[str, Any]:
        """
        Trả về:
        {
            "intent", "confidence", "entities", "tool", "tool_args",
            "ask_missing", "response_template", "is_out_of_scope"
        }
        """
        text = normalize_text(raw_text)
        text_no_accents = remove_accents(text)
        text_expanded = expand_teencode(text, self.teencode_map)

        if not self._has_shopping_signal(text, text_no_accents) and self._is_out_of_scope(
            text, text_no_accents
        ):
            oos = self._by_name("out_of_scope")
            return self._result(oos, 0.95, {}, is_out_of_scope=True)

        scored: list[tuple[dict, float, int]] = []
        matched_kw: dict[str, int] = {}
        for intent in self.intents:
            if intent["name"] in ("out_of_scope", "unclear"):
                continue
            s, kw = self._score_intent(text_expanded, text_no_accents, intent)
            scored.append((intent, s, kw))
            matched_kw[intent["name"]] = kw

        scored.sort(key=lambda x: (-x[1], -x[0].get("priority", 0)))
        best, best_score, _ = scored[0]

        best, best_score = self._resolve(best, best_score, text, text_no_accents,
                                         self._extract_entities(text), matched_kw)

        if best_score < THRESHOLD:
            unclear = self._by_name("unclear")
            return self._result(unclear, 0.0, {})

        entities = self._extract_entities(text)
        required = best.get("required_entities", [])
        missing = [e for e in required if e not in entities]
        ask_missing = best.get("ask_missing") if missing else None

        return self._result(best, best_score, entities, ask_missing=ask_missing)

    def _result(self, intent: dict, confidence: float, entities: dict,
                ask_missing: str | None = None,
                is_out_of_scope: bool = False) -> dict[str, Any]:
        return {
            "intent": intent["name"],
            "confidence": round(confidence, 3),
            "entities": entities,
            "tool": intent.get("tool"),
            "tool_args": intent.get("tool_args", {}),
            "ask_missing": ask_missing,
            "response_template": intent.get("response_template"),
            "is_out_of_scope": is_out_of_scope or intent["name"] == "out_of_scope",
        }


if __name__ == "__main__":
    matcher = IntentMatcher()
    test_cases = [
        "tìm áo thun nam màu đen size L",
        "đơn DH1234 đang ở đâu",
        "có mã giảm giá không",
        "thời tiết hôm nay thế nào",
        "cảm ơn shop",
        "hủy đơn DH5678",
        "bảng size áo thun",
        "gặp nhân viên",
        "áo thun đen còn hàng không",
        "giá váy đỏ bao nhiêu",
        "áo bị lỗi",
        "sao",
        "kể chuyện cười đi",
    ]
    for tc in test_cases:
        result = matcher.match(tc)
        print(f"[{result['intent']:20s}] conf={result['confidence']:.2f} | {tc}")
        if result["entities"]:
            print(f"  entities: {result['entities']}")
        if result["ask_missing"]:
            print(f"  ask: {result['ask_missing']}")
