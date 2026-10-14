# -*- coding: utf-8 -*-
"""Test tích hợp web app Tomcat (:8080) ↔ FastAPI chatbot (:8000)."""
import json
import urllib.parse
import urllib.request

URL = "http://localhost:8080/chatbot"

CASES = [
    "xin chào",
    "tìm áo thun đen size L",
    "đơn DH1234 đang ở đâu",
    "ship bao lâu vậy",
    "có mã giảm giá không",
    "thời tiết hôm nay thế nào",
    "sao",
    "đổi trả thế nào",
]

for text in CASES:
    data = urllib.parse.urlencode({"message": text}).encode("utf-8")
    req = urllib.request.Request(URL, data=data, headers={"Content-Type": "application/x-www-form-urlencoded"})
    try:
        with urllib.request.urlopen(req, timeout=120) as resp:
            body = resp.read().decode("utf-8")
        r = json.loads(body)
        ok = "OK " if r.get("success") else "ERR"
        msg = (r.get("message") or "")[:110].replace("\n", " ")
        print(f"[{ok}] {text}")
        print(f"       -> {msg}")
    except Exception as e:
        print(f"[FAIL] {text} -> {e}")
    print()
