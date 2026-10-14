# -*- coding: utf-8 -*-
"""
Smoke test end-to-end cho chatbot 3 tầng (Rule → JEV → LLM stubs).

Mỗi case: Router quyết định tier → nếu tier3 thì Qwen 7B chọn tool → stub trả dữ liệu → Qwen sinh trả lời.
Chạy sau khi Ollama đã pull qwen2.5:7b và JEV centroids đã sinh.
"""
import sys
from pathlib import Path

# Model cache dùng ổ D (thống nhất với tài liệu dự án)
import os

os.environ.setdefault("HF_HOME", r"D:\models\huggingface")
os.environ.setdefault("TRANSFORMERS_CACHE", r"D:\models\huggingface")
os.environ.setdefault("HUGGINGFACE_HUB_CACHE", r"D:\models\huggingface")

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app.llm_client import LLMError, OllamaClient, SYSTEM_PROMPT, TOOL_SCHEMAS, stub_tools
from app.router import HybridRouter

OLLAMA_MODEL = "qwen2.5:7b-instruct-q4_K_M"

CASES = [
    ("tìm áo thun đen size L", "Rule→tier1, tìm sản phẩm"),
    ("đơn DH1234 đang ở đâu", "Rule→tier1, tra đơn"),
    ("áo thun trắng còn hàng không", "Rule/JEV→tier1 hoặc tier2, kiểm tồn kho"),
    ("ship bao lâu vậy", "Rule→tier1, policy shipping"),
    ("mình đi biển cuối tuần mặc gì đẹp", "JEV hoặc tier3, paraphrase khó"),
    ("thời tiết hôm nay thế nào", "fallback out_of_scope, KHÔNG gọi tool"),
    ("sao", "tier3 vì unclear, phải hỏi lại"),
]

HEADERS = {"Content-Type": "text/html; charset=utf-8"}

MESSAGES = {"suffix": " (english: hello, how are you)", "separator": ", "}


def check_unclear_strict(r: dict, text: str) -> list[str]:
    """unclear KHÔNG được chạy vào LLM (tier3) — phải hỏi lại ngay.

    Không bắt buộc phải ở tier1: Rule có thể trả unclear ở tier1, hoặc JEV
    xác nhận unclear ở tier2 (cả hai đều đúng, miễn không gọi LLM).
    """
    if r["intent"] == "unclear":
        if r["route"] == "tier3":
            return [f"'{text}': unclear không được gọi LLM (đang ở tier3)"]
        if r["reply"] is None:
            return [f"'{text}': unclear thiếu câu hỏi làm rõ"]
    return []


def center(text: str, width: int = 56, fill: str = "-") -> str:
    pad = (width - len(text) - 2) // 2
    return f"{fill * pad} {text} {fill * (width - len(text) - 2 - pad)}"


def run() -> int:
    router = HybridRouter()
    router.enable_jev = True
    ok = router.ensure_jev()
    print(f"JEV: {'tải OK' if ok else 'KHÔNG KHẢ DỤNG (thiếu model/lib)'}")

    client = OllamaClient(model=OLLAMA_MODEL)
    h = client.health()
    if not h["ok"]:
        print(f"Ollama không chạy: {h['error']}. Bỏ qua T3.")
        llm_ok = False
    elif not h["has_model"]:
        print(f"Model {OLLAMA_MODEL} chưa có trong Ollama: {h['models']}")
        llm_ok = False
    else:
        print(f"Ollama OK: model={OLLAMA_MODEL}")
        llm_ok = True

    failures: list[str] = []
    print("\n" + center("BEGIN SMOKE"))
    for text, note in CASES:
        print("\n" + center(f" {text!r} ", fill="="))
        print(f"  Mô tả: {note}")
        r = router.route(text)
        print(
            f"  Router: route={r['route']:6s} intent={r['intent']:18s} "
            f"rule={r['rule_confidence']:.2f} jev={r['jev_confidence']} "
            f"tier={r['tier']} latency={r['latency_ms']}"
        )
        print(f"          tool={r['tool']}  ask_missing={r['ask_missing']}")

        # Tier3 → gọi Qwen chọn tool, sau đó stub trả dữ liệu
        if r["route"] == "tier3" and llm_ok and r["intent"] not in ("out_of_scope", "unclear"):
            try:
                msgs = [
                    {"role": "system", "content": SYSTEM_PROMPT},
                    {"role": "user", "content": text},
                ]
                raw = client.chat(msgs, tools=TOOL_SCHEMAS)
                content = raw.get("message", {}).get("content", "") or ""
                tool_calls: list[dict] = raw.get("_tool_calls", []) or []
                lat = raw.get("_latency_ms", 0)
                if tool_calls:
                    tc = tool_calls[0]
                    tool_result = stub_tools(tc)
                    print(f"  Qwen chọn tool: {tc['name']}({tc['arguments']}) → {tool_result}")
                    # Đợt 2: đưa tool_result về cho LLM sinh câu trả lời
                    msgs2 = msgs + [
                        {"role": "assistant", "tool_calls": raw.get("message", {}).get("tool_calls", [])},
                        {"role": "tool", "content": tool_result},
                    ]
                    raw2 = client.chat(msgs2)
                    print(f"  Qwen trả lời: {raw2.get('message', {}).get('content', '')!r}")
                    print(f"       latency: {lat}ms + {raw2.get('_latency_ms', 0)}ms")
                elif content:
                    print(f"  Qwen trả lời (không tool): {content!r} ({lat}ms)")
                else:
                    print(f"  Qwen không trả về gì ({lat}ms) — raw={raw}")
            except (LLMError, Exception) as e:  # noqa
                print(f"  Lỗi gọi LLM: {e}")
        elif r["intent"] in ("out_of_scope", "unclear"):
            print(f"  Fallback: {r['reply']!r}")

        # Kiểm tra đơn giản: tier1/tier2 phải có tool hoặc reply, tier3 phải có intent hợp lệ
        if r["route"] == "tier3" and r["intent"] == "unclear" and text.strip() not in ("sao", "???", ".", "cái này", "á"):
            pass  # unclear cho câu khó là hợp lệ
        elif r["intent"] not in (
            "find_product", "check_availability", "price_query", "promotion_query",
            "size_guide", "track_order", "cancel_order", "check_policy",
            "review_query", "complaint", "human_handoff", "greeting", "goodbye",
            "thanks", "out_of_scope", "unclear",
        ):
            failures.append(text)
            print(f"  ⚠ Intent không hợp lệ: {r['intent']}")

        # unclear phải dừng ở tier1, không gọi LLM
        failures.extend(check_unclear_strict(r, text))

    print("\n" + center("KẾT QUẢ"))
    if failures:
        print(f"  FAIL ({len(failures)} câu): {failures}")
        return 1
    print("  PASS — 0 lỗi route")
    return 0


if __name__ == "__main__":
    raise SystemExit(run())
