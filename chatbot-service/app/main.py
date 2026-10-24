from __future__ import annotations

import json
import os
import time
from collections import Counter
from contextlib import asynccontextmanager
from typing import Any

os.environ.setdefault("HF_HOME", r"D:\models\huggingface")
os.environ.setdefault("TRANSFORMERS_CACHE", r"D:\models\huggingface")
os.environ.setdefault("HUGGINGFACE_HUB_CACHE", r"D:\models\huggingface")

from fastapi import FastAPI, HTTPException  # noqa: E402
from fastapi.middleware.cors import CORSMiddleware  # noqa: E402
from pydantic import BaseModel, Field  # noqa: E402

from app.intent_matcher import IntentMatcher  # noqa: E402
from app.jev_service import JEVMatcher, JEVNotAvailableError  # noqa: E402
from app.llm_client import (  # noqa: E402
    LLMError,
    OllamaClient,
    SYSTEM_PROMPT,
    TOOL_SCHEMAS,
    stub_tools,
)
from app.router import HybridRouter  # noqa: E402
from app.tools import execute_tool  # noqa: E402

OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "qwen2.5:7b-instruct-q4_K_M")
OLLAMA_HOST = os.getenv("OLLAMA_HOST", "http://localhost:11434")
ENABLE_LLM = os.getenv("ENABLE_LLM", "1") == "1"

class AppState:
    def __init__(self) -> None:
        self.rule: IntentMatcher | None = None
        self.jev: JEVMatcher | None = None
        self.router: HybridRouter | None = None
        self.llm: OllamaClient | None = None
        self.llm_ok: bool = False
        self.route_counts: Counter = Counter()
        self.latency_samples: list[float] = []


state = AppState()


@asynccontextmanager
async def lifespan(_: FastAPI):
    # Tải Rule (nhanh)
    state.rule = IntentMatcher()
    print("[startup] Rule matcher: OK")

    try:
        state.jev = JEVMatcher()
        state.jev.load()
        print(f"[startup] JEV: OK ({state.jev.model_name})")
    except (JEVNotAvailableError, ValueError, OSError) as e:
        print(f"[startup] JEV: KHÔNG KHẢ DỤNG ({e}) — router sẽ fallback T3")
        state.jev = None

    state.router = HybridRouter(rule=state.rule, jev=state.jev, enable_jev=state.jev is not None)
    print(f"[startup] Router: JEV={'ON' if state.jev else 'OFF'}")

    if ENABLE_LLM:
        state.llm = OllamaClient(host=OLLAMA_HOST, model=OLLAMA_MODEL)
        h = state.llm.health()
        state.llm_ok = bool(h["ok"] and h["has_model"])
        print(f"[startup] LLM: {'OK' if state.llm_ok else 'OFF'} ({OLLAMA_MODEL})")
    else:
        print("[startup] LLM: DISABLED (ENABLE_LLM=0)")

    yield

    print("[shutdown] done")


app = FastAPI(title="Fashion Chatbot 3-Tier", version="1.0.0", lifespan=lifespan)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Java web gọi cross-origin
    allow_methods=["*"],
    allow_headers=["*"],
)

class ChatRequest(BaseModel):
    message: str = Field(..., min_length=1, max_length=2000, description="Câu hỏi của khách")
    user_id: str | None = Field(None, description="ID người dùng (nếu đã đăng nhập)")
    session_id: str | None = Field(None, description="ID phiên chat")


class ChatResponse(BaseModel):
    reply: str | None
    intent: str
    route: str
    tier: int
    tool_called: str | None = None
    entities: dict[str, Any] = {}
    ask_missing: str | None = None
    latency_ms: dict[str, float | None] = {}


def _render_tool_reply(tool: str | None, result: dict[str, Any], ask_missing: str | None) -> str | None:
    """
    Sinh câu trả lời tiếng Việt từ kết quả tool cho tier1/tier2 (không qua LLM).

    Tier1/tier2 chọn tool trực tiếp nên phải tự render — đây chính là phần
    còn thiếu khiến reply=None trước đây.
    """
    if ask_missing:
        return ask_missing

    if not tool:
        return None

    if "error" in result:
        return "Xin lỗi, mình đang gặp trục trặc khi tra dữ liệu. Bạn thử lại giúp mình nhé."

    if tool == "search_products":
        products = result.get("products", [])
        if not products:
            return "Mình chưa tìm thấy sản phẩm phù hợp. Bạn thử từ khóa khác (ví dụ: áo thun, quần jean, đầm) nhé."
        lines = [f"Mình gợi ý {len(products)} sản phẩm phù hợp:"]
        for p in products:
            link = p.get("product_url", "#")
            name = p.get("name", "Sản phẩm")
            price_text = p.get("price_text", "")
            color = p.get("color", "")
            size = p.get("size", "")
            stock = p.get("stock", 0)
            stock_note = "" if stock > 0 else " (tạm hết hàng)"
            lines.append(f'• <a href="{link}" target="_blank" rel="noopener noreferrer">{name}</a> – {price_text} – màu {color}, size {size}{stock_note}')
        lines.append("Bạn xem chi tiết và chọn size trước khi đặt nhé!")
        return "\n".join(lines)

    if tool == "check_stock":
        if not result.get("found"):
            return result.get("message", "Mình chưa kiểm tra được tồn kho sản phẩm này.")
        if result.get("available"):
            return (f"Sản phẩm \"{result['product_name']}\" còn hàng "
                    f"({result.get('stock')} chiếc, size {result.get('size')}). Bạn đặt ngay nhé!")
        return (f"Rất tiếc, \"{result['product_name']}\" hiện tạm hết hàng "
                f"hoặc chưa có size bạn cần. Bạn thử size/màu khác nhé.")

    if tool == "lookup_faq":
        return result.get("answer")

    if tool == "get_order":
        if not result.get("found"):
            return result.get("message", "Mình chưa tìm thấy đơn hàng này.")
        return (f"Đơn {result['order_code']} của bạn đang ở trạng thái: "
                f"{result['order_status']} (thanh toán: {result['payment_status']}). "
                f"Tổng tiền: {result['total_text']}. Người nhận: {result.get('receiver') or 'N/A'}.")

    if tool == "cancel_order":
        return result.get("message")

    if tool == "create_ticket":
        return result.get("message")

    if tool == "get_reviews":
        return result.get("message")

    return None


def _run_tool(router_result: dict[str, Any], text: str) -> dict[str, Any]:
    """Thực thi tool mà router đã chọn, gắn reply vào kết quả."""
    tool = router_result.get("tool")
    args = router_result.get("tool_args", {}) or {}
    for k, v in (router_result.get("entities", {}) or {}).items():
        args.setdefault(k, v)
    result = execute_tool(tool, args, query=text) if tool else {}
    reply = _render_tool_reply(tool, result, router_result.get("ask_missing"))
    return {**router_result, "reply": reply, "tool_result": result}


def _run_llm_tier3(text: str, router_result: dict[str, Any]) -> dict[str, Any]:
    """Gọi Qwen 7B chọn tool → thực thi tool thật → LLM sinh câu trả lời."""
    if not state.llm_ok or state.llm is None:
        return {**router_result, "reply": router_result.get("reply") or "Xin lỗi, hệ thống đang bận. Bạn thử lại sau nhé."}

    try:
        msgs = [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": text},
        ]
        raw = state.llm.chat(msgs, tools=TOOL_SCHEMAS)
        tool_calls = raw.get("_tool_calls", []) or []

        if not tool_calls:
            content = raw.get("message", {}).get("content", "") or ""
            return {**router_result, "reply": content or router_result.get("reply"), "tool_called": None}

        tc = tool_calls[0]
        tool_result = execute_tool(tc["name"], tc.get("arguments", {}), query=text)

        msgs2 = msgs + [
            {"role": "assistant", "tool_calls": raw.get("message", {}).get("tool_calls", [])},
            {"role": "tool", "content": json.dumps(tool_result, ensure_ascii=False)},
        ]
        raw2 = state.llm.chat(msgs2)
        content = raw2.get("message", {}).get("content", "") or ""
        return {
            **router_result,
            "reply": content or _render_tool_reply(tc["name"], tool_result, None),
            "tool": tc["name"],
            "tool_args": tc.get("arguments", {}),
            "tool_result": tool_result,
        }
    except (LLMError, Exception) as e:  # noqa: BLE001
        print(f"[tier3] LLM error: {e}")
        return _run_tool(router_result, text) if router_result.get("tool") else {
            **router_result, "reply": router_result.get("reply") or "Xin lỗi, hệ thống đang bận."
        }

@app.post("/api/chat", response_model=ChatResponse)
async def chat(req: ChatRequest) -> ChatResponse:
    if state.router is None:
        raise HTTPException(503, "Service chưa sẵn sàng")

    t0 = time.perf_counter()
    result = state.router.route(req.message)
    total_ms = (time.perf_counter() - t0) * 1000.0

    if result["route"] == "tier3" and result["intent"] not in ("out_of_scope", "unclear"):
        result = _run_llm_tier3(req.message, result)
    elif result.get("tool"):
        result = _run_tool(result, req.message)

    state.route_counts[result["route"]] += 1
    state.latency_samples.append(total_ms)
    if len(state.latency_samples) > 1000:
        state.latency_samples = state.latency_samples[-1000:]

    return ChatResponse(
        reply=result.get("reply"),
        intent=result["intent"],
        route=result["route"],
        tier=result["tier"],
        tool_called=result.get("tool"),
        entities=result.get("entities", {}),
        ask_missing=result.get("ask_missing"),
        latency_ms={
            "tier1": result.get("latency_ms", {}).get("tier1", 0.0),
            "tier2": result.get("latency_ms", {}).get("tier2"),
            "total": round(total_ms, 1),
        },
    )


@app.get("/health")
async def health() -> dict[str, Any]:
    return {
        "ok": True,
        "rule": state.rule is not None,
        "jev": state.jev is not None and state.jev.loaded,
        "llm": state.llm_ok,
        "model": OLLAMA_MODEL,
    }


@app.get("/metrics")
async def metrics() -> dict[str, Any]:
    n = sum(state.route_counts.values())
    lat = state.latency_samples
    return {
        "total_requests": n,
        "route_distribution": dict(state.route_counts),
        "latency_ms": {
            "p50": sorted(lat)[len(lat) // 2] if lat else 0,
            "p95": sorted(lat)[int(len(lat) * 0.95)] if lat else 0,
            "avg": sum(lat) / len(lat) if lat else 0,
        },
    }


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=False)
