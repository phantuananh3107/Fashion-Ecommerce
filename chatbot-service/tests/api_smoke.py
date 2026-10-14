# -*- coding: utf-8 -*-
"""Test endpoint FastAPI /api/chat qua HTTP thật."""
import json
import urllib.request

BASE = "http://127.0.0.1:8000"

CASES = [
    "xin chào",
    "tìm áo thun đen size L",
    "đơn DH1234 đang ở đâu",
    "ship bao lâu vậy",
    "thời tiết hôm nay thế nào",
    "sao",
]

for text in CASES:
    payload = json.dumps({"message": text}, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        f"{BASE}/api/chat", data=payload, headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(req, timeout=180) as resp:
        r = json.loads(resp.read().decode("utf-8"))
    print(f"[{r['route']:6s}|{r['intent']:16s}] tool={r['tool_called']} total={r['latency_ms']['total']}ms")
    print(f"  -> {text}")
    if r.get("reply"):
        print(f"  reply: {r['reply'][:120]}")
    print()