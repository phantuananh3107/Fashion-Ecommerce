# -*- coding: utf-8 -*-
"""
Smoke test Tầng 3 thật — gọi Qwen 7B chọn tool và sinh câu trả lời.

Dùng trực tiếp app.llm_client.OllamaClient (không qua FastAPI) để test nhanh
không cần khởi động server.

Chạy:  python tests/llm_tier3_smoke.py
"""
import os
import sys
import time
from pathlib import Path

os.environ.setdefault("HF_HOME", r"D:\models\huggingface")
os.environ.setdefault("TRANSFORMERS_CACHE", r"D:\models\huggingface")
os.environ.setdefault("HUGGINGFACE_HUB_CACHE", r"D:\models\huggingface")

BASE = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(BASE))

from app.llm_client import (  # noqa: E402
    SYSTEM_PROMPT,
    TOOL_SCHEMAS,
    OllamaClient,
    stub_tools,
)
from app.router import HybridRouter  # noqa: E402

OLLAMA_MODEL = "qwen2.5:7b-instruct-q4_K_M"

CASES = [
    "tôi cần mua một chiếc áo khoác nhẹ cho mùa thu, giá khoảng 500k",
    "đơn hàng của tôi đến đâu rồi, mã là DH1234",
    "shop có chính sách đổi trả thế nào",
    "có size nào phù hợp cho người cao 1m70 nặng 65kg không",
]


def center(text: str, width: int = 62, fill: str = "=") -> str:
    pad = max((width - len(text) - 2) // 2, 0)
    return f"{fill * pad} {text} {fill * max(width - len(text) - 2 - pad, 0)}"


def main() -> int:
    client = OllamaClient(model=OLLAMA_MODEL)
    h = client.health()
    if not h["ok"]:
        print(f"Ollama không chạy: {h['error']}")
        return 1
    if not h["has_model"]:
        print(f"Model {OLLAMA_MODEL} chưa có: {h['models']}")
        return 1
    print(f"Ollama OK — model {OLLAMA_MODEL}")

    router = HybridRouter(enable_jev=False)  # tier3 test: bỏ qua JEV để ép LLM
    failures = 0
    print()
    for text in CASES:
        print(center(f" {text} ", fill="-"))
        r = router.route(text)
        print(f"  Router tier={r['route']} intent={r['intent']} (rule={r['rule_confidence']:.2f})")

        t0 = time.perf_counter()
        try:
            msgs = [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": text},
            ]
            raw = client.chat(msgs, tools=TOOL_SCHEMAS)
            tool_calls = raw.get("_tool_calls", []) or []
            content = raw.get("message", {}).get("content", "") or ""

            if tool_calls:
                tc = tool_calls[0]
                print(f"  ✓ Tool call: {tc['name']}")
                print(f"    args: {tc['arguments']}")
                tool_result = stub_tools(tc)
                msgs2 = msgs + [
                    {"role": "assistant", "tool_calls": raw.get("message", {}).get("tool_calls", [])},
                    {"role": "tool", "content": tool_result},
                ]
                raw2 = client.chat(msgs2)
                final = raw2.get("message", {}).get("content", "") or ""
                print(f"  ✓ Câu trả lời (sau tool): {final[:200]}")
            else:
                print(f"  (không tool call) reply: {content[:200]}")
                if not content:
                    failures += 1
                    print("  ✗ LLM không trả về nội dung")
        except Exception as e:  # noqa: BLE001
            failures += 1
            print(f"  ✗ Lỗi: {e}")
        dt = (time.perf_counter() - t0) * 1000.0
        print(f"  thời gian: {dt:.0f}ms")
        print()

    print(center("KẾT QUẢ"))
    if failures:
        print(f"  FAIL — {failures} lỗi")
        return 1
    print("  PASS — Tầng 3 (Qwen 7B tool calling) hoạt động")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())