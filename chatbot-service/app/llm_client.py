import json
import time
import urllib.error
import urllib.request
from typing import Any

DEFAULT_OLLAMA_HOST = "http://localhost:11434"
DEFAULT_MODEL = "qwen2.5:7b-instruct-q4_K_M"

SYSTEM_PROMPT = """Bạn là trợ lý thời trang của một website bán quần áo online.

Quy tắc:
1. Chỉ trả lời bằng tiếng Việt, thân thiện và ngắn gọn (tối đa 3 câu).
2. Với câu hỏi về sản phẩm, đơn hàng, chính sách → PHẢI gọi tool tương ứng.
   KHÔNG BAO GIỜ tự bịa giá, tồn kho hay trạng thái đơn hàng.
3. Nếu thiếu thông tin cần thiết (ví dụ mã đơn hàng) → hỏi lại khách, đừng gọi tool.
4. Nếu câu hỏi ngoài lĩnh vực thời trang, nói lịch sự rằng bạn không hỗ trợ.

Ví dụ:
- "tìm áo thun đen size M" → gọi search_products(keyword="áo thun", color="đen", size="M")
- "đơn DH1234 đến đâu" → gọi get_order(order_code="DH1234")
- "ship bao lâu" → gọi lookup_faq(topic="shipping")
- "có mã giảm giá không" → gọi lookup_faq(topic="promotion")
"""

TOOL_SCHEMAS: list[dict[str, Any]] = [
    {
        "type": "function",
        "function": {
            "name": "search_products",
            "description": "Tìm kiếm sản phẩm theo từ khóa và bộ lọc (danh mục, màu, size, khoảng giá).",
            "parameters": {
                "type": "object",
                "properties": {
                    "keyword": {"type": "string", "description": "Từ khóa, ví dụ 'áo thun', 'quần jean'"},
                    "category": {"type": "string", "description": "Danh mục, ví dụ 'áo', 'quần', 'váy', 'giày'"},
                    "color": {"type": "string", "description": "Màu sắc, ví dụ 'đen', 'trắng', 'xanh'"},
                    "size": {"type": "string", "description": "Kích cỡ, ví dụ 'S', 'M', 'L', 'XL'"},
                    "max_price": {"type": "number", "description": "Giá tối đa (VNĐ)"},
                },
                "required": ["keyword"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "check_stock",
            "description": "Kiểm tra tồn kho của một sản phẩm theo size và màu.",
            "parameters": {
                "type": "object",
                "properties": {
                    "product_name": {"type": "string", "description": "Tên sản phẩm"},
                    "size": {"type": "string", "description": "Kích cỡ cần kiểm tra"},
                    "color": {"type": "string", "description": "Màu cần kiểm tra"},
                },
                "required": ["product_name"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "lookup_faq",
            "description": "Tra cứu chính sách của shop theo chủ đề.",
            "parameters": {
                "type": "object",
                "properties": {
                    "topic": {
                        "type": "string",
                        "description": "Chủ đề: shipping, return, payment, promotion, size_guide, warranty",
                    }
                },
                "required": ["topic"],
            },
        },
    },
    {
        "type": "function",
        "function": {
            "name": "get_order",
            "description": "Tra cứu trạng thái đơn hàng theo mã đơn hoặc số điện thoại.",
            "parameters": {
                "type": "object",
                "properties": {
                    "order_code": {"type": "string", "description": "Mã đơn dạng DH1234"},
                    "phone": {"type": "string", "description": "Số điện thoại đặt hàng"},
                },
                "required": [],
            },
        },
    },
]


class LLMError(RuntimeError):
    """Lỗi khi gọi Ollama (server không chạy, timeout, model không tồn tại)."""


class OllamaClient:
    def __init__(self, host: str = DEFAULT_OLLAMA_HOST, model: str = DEFAULT_MODEL,
                 timeout: int = 120):
        self.host = host.rstrip("/")
        self.model = model
        self.timeout = timeout

    def _post(self, path: str, payload: dict[str, Any]) -> dict[str, Any]:
        url = f"{self.host}{path}"
        data = json.dumps(payload, ensure_ascii=False).encode("utf-8")
        req = urllib.request.Request(url, data=data, method="POST")
        req.add_header("Content-Type", "application/json; charset=utf-8")
        try:
            with urllib.request.urlopen(req, timeout=self.timeout) as resp:
                return json.loads(resp.read().decode("utf-8"))
        except urllib.error.URLError as e:
            raise LLMError(f"Không gọi được Ollama tại {self.host}: {e}") from e
        except TimeoutError as e:
            raise LLMError(f"Ollama timeout sau {self.timeout}s") from e

    def health(self) -> dict[str, Any]:
        """Kiểm tra server + model có sẵn không."""
        try:
            with urllib.request.urlopen(f"{self.host}/api/tags", timeout=5) as r:
                tags = json.loads(r.read().decode("utf-8"))
            names = [m["name"] for m in tags.get("models", [])]
            return {"ok": True, "models": names, "has_model": self.model in names}
        except Exception as e:  # noqa: BLE001
            return {"ok": False, "error": str(e), "models": [], "has_model": False}

    def chat(
        self,
        messages: list[dict[str, str]],
        tools: list[dict[str, Any]] | None = None,
        temperature: float = 0.2,
        top_p: float = 0.9,
        num_predict: int = 512,
    ) -> dict[str, Any]:
        """
        Gọi Ollama /api/chat. Trả về raw response của Ollama.
        Message phải dùng tools=tool_schemas nếu muốn LLM chọn tool.
        """
        payload: dict[str, Any] = {
            "model": self.model,
            "messages": messages,
            "stream": False,
            "options": {
                "temperature": temperature,
                "top_p": top_p,
                "num_predict": num_predict,
            },
        }
        if tools:
            payload["tools"] = tools

        t0 = time.perf_counter()
        result = self._post("/api/chat", payload)
        result["_latency_ms"] = round((time.perf_counter() - t0) * 1000.0, 1)
        result["_tool_calls"] = _extract_tool_calls(result.get("message", {}))
        return result


def _extract_tool_calls(message: dict[str, Any]) -> list[dict[str, Any]]:
    """Chuẩn hoá tool_calls của Ollama về dạng [{name, arguments(dict)}]."""
    calls: list[dict[str, Any]] = []
    for tc in message.get("tool_calls", []) or []:
        fn = tc.get("function", {}) or {}
        args = fn.get("arguments", {}) or {}
        if isinstance(args, str):
            try:
                args = json.loads(args)
            except json.JSONDecodeError:
                args = {"_raw": args}
        calls.append({"name": fn.get("name", ""), "arguments": args})
    return calls

def stub_tools(args: dict[str, Any]) -> str:
    """Tool giả lập — trả dữ liệu mẫu. Thay bằng truy vấn MySQL thật ở tuần 3."""
    name = args.get("name", "")
    a = args.get("arguments", {}) or {}
    if name == "search_products":
        kw = a.get("keyword", "")
        return json.dumps(
            {
                "keyword": kw,
                "total": 2,
                "products": [
                    {"id": 1, "name": f"{kw} basic màu đen", "price": 249000, "stock": 12},
                    {"id": 2, "name": f"{kw} cotton trắng", "price": 299000, "stock": 3},
                ],
            },
            ensure_ascii=False,
        )
    if name == "check_stock":
        return json.dumps(
            {"product_name": a.get("product_name"), "size": a.get("size"),
             "color": a.get("color"), "stock": 8, "available": True},
            ensure_ascii=False,
        )
    if name == "lookup_faq":
        topic = a.get("topic", "")
        faq = {
            "shipping": "Đơn nội thành giao 1-2 ngày, ngoại thành 3-5 ngày. Freeship cho đơn từ 300k.",
            "return": "Đổi trả trong 7 ngày nếu sản phẩm chưa qua sử dụng, giữ tem mác.",
            "payment": "Chấp nhận COD, chuyển khoản, ví điện tử (MoMo, ZaloPay), thẻ tín dụng.",
            "promotion": "Giảm 10% cho đơn đầu tiên, giảm thêm 50k cho đơn từ 500k.",
            "size_guide": "Áo: S 45-55kg, M 55-65kg, L 65-75kg. Quần: 28 (80-88cm), 30 (88-96cm).",
            "warranty": "Sản phẩm được bảo hành 3 tháng về lỗi sản xuất.",
        }
        return json.dumps({"topic": topic, "answer": faq.get(topic, "Chưa có thông tin cho chủ đề này.")},
                          ensure_ascii=False)
    if name == "get_order":
        return json.dumps(
            {"order_code": a.get("order_code"), "status": "Đang giao hàng",
             "carrier": "Giao Hàng Nhanh", "eta": "2026-10-07"},
            ensure_ascii=False,
        )
    return json.dumps({"error": f"tool {name} chưa cài đặt"}, ensure_ascii=False)


__all__ = [
    "OllamaClient",
    "LLMError",
    "TOOL_SCHEMAS",
    "SYSTEM_PROMPT",
    "stub_tools",
    "_extract_tool_calls",
]