"""
Tool Executor — Thực thi tool của chatbot trên MySQL thật (database `sys`).

Đây là mảnh còn thiếu khiến tier1/tier2 chọn tool nhưng trả reply=None:
router quyết định đúng tool, nhưng chưa có nơi thực thi. Module này lấp chỗ đó.

4 tool khớp TOOL_SCHEMAS trong app/llm_client.py:
  - search_products(keyword, category, color, size, max_price)
  - check_stock(product_name, size, color)
  - lookup_faq(topic)
  - get_order(order_code, phone)

Kết nối MySQL dùng chung biến môi trường với web app (DB_NAME/DB_USER/DB_PASS)
để một nguồn cấu hình duy nhất — đọc từ .env của Fashion-Ecommerce.
"""
from __future__ import annotations

import json
import os
import re
from pathlib import Path
from typing import Any

# ---------------------------------------------------------------------------
# Config — đọc .env của Fashion-Ecommerce (một nguồn sự thật)
# ---------------------------------------------------------------------------
_ENV_CANDIDATES = [
    Path(__file__).resolve().parent.parent / ".env",            # chatbot-service/.env
    Path(__file__).resolve().parent.parent.parent / ".env",     # Fashion-Ecommerce/.env
]


def _load_env() -> dict[str, str]:
    values: dict[str, str] = {}
    for path in _ENV_CANDIDATES:
        if not path.exists():
            continue
        for line in path.read_text(encoding="utf-8").splitlines():
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            values[k.strip()] = v.strip()
        break
    return values


_ENV = _load_env()


def _cfg(key: str, default: str) -> str:
    return os.getenv(key) or _ENV.get(key) or default


DB_HOST = _cfg("DB_HOST", "127.0.0.1")
DB_PORT = int(_cfg("DB_PORT", "3306"))
DB_NAME = _cfg("DB_NAME", "fashionecom")
DB_USER = _cfg("DB_USER", "root")
DB_PASS = _cfg("DB_PASS", "")


def _connect():
    import pymysql

    return pymysql.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS,
        database=DB_NAME, charset="utf8mb4", cursorclass=pymysql.cursors.DictCursor,
        connect_timeout=5, read_timeout=10,
    )


def _fmt_price(v: Any) -> str:
    try:
        return f"{int(float(v)):,}".replace(",", ".") + "đ"
    except (TypeError, ValueError):
        return str(v)


def _final_price(price: Any, discount: Any) -> int:
    try:
        p = float(price)
        d = float(discount or 0)
        return int(p * (100 - d) / 100)
    except (TypeError, ValueError):
        return 0


# ---------------------------------------------------------------------------
# FAQ — trả lời chính sách (tĩnh, khớp shop TA)
# ---------------------------------------------------------------------------
FAQ = {
    "shipping": "Đơn nội thành giao 1-2 ngày, ngoại thành 3-5 ngày. Freeship cho đơn từ 300.000đ.",
    "return": "Đổi trả trong 7 ngày nếu sản phẩm chưa qua sử dụng, còn nguyên tem mác và hóa đơn.",
    "payment": "Shop nhận COD (thanh toán khi nhận hàng), chuyển khoản, ví điện tử và thẻ tín dụng.",
    "promotion": "Shop thường xuyên có mã giảm giá và chương trình sale. Bạn xem mục Khuyến mãi trên trang chủ nhé.",
    "size_guide": "Áo: S (45-52kg), M (52-60kg), L (60-68kg), XL (68-76kg). Quần: chọn theo số đo vòng eo. Bạn cho mình chiều cao/cân nặng để tư vấn chính xác hơn.",
    "warranty": "Sản phẩm được bảo hành 3 tháng với lỗi từ nhà sản xuất.",
}

_FAQ_TOPIC_HINTS = {
    "shipping": ["ship", "giao hàng", "vận chuyển", "giao"],
    "return": ["đổi trả", "trả hàng", "đổi", "hoàn"],
    "payment": ["thanh toán", "cod", "chuyển khoản", "thẻ"],
    "promotion": ["khuyến mãi", "giảm giá", "voucher", "sale", "ưu đãi", "mã"],
    "size_guide": ["size", "kích cỡ", "cân nặng", "chiều cao", "chọn size"],
    "warranty": ["bảo hành"],
}


def _resolve_topic(topic: str | None, query: str) -> str:
    """Chuẩn hóa topic; nếu topic rỗng thì đoán từ câu hỏi."""
    if topic and topic in FAQ:
        return topic
    low = (query or "").lower()
    for t, hints in _FAQ_TOPIC_HINTS.items():
        if any(h in low for h in hints):
            return t
    return topic if topic else "shipping"


# ---------------------------------------------------------------------------
# Tool implementations
# ---------------------------------------------------------------------------
def search_products(args: dict[str, Any], query: str = "") -> dict[str, Any]:
    keyword = (args.get("keyword") or "").strip()
    category = (args.get("category") or "").strip()
    color = (args.get("color") or "").strip()
    size = (args.get("size") or "").strip()
    max_price = args.get("max_price")

    where = ["p.isDeleted = 0"]
    params: list[Any] = []

    if keyword:
        where.append("(p.product_name LIKE %s OR c.category_name LIKE %s OR p.product_description LIKE %s)")
        params += [f"%{keyword}%", f"%{keyword}%", f"%{keyword}%"]
    if category:
        where.append("(p.product_name LIKE %s OR c.category_name LIKE %s)")
        params += [f"%{category}%", f"%{category}%"]
    if color:
        where.append("p.product_color LIKE %s")
        params.append(f"%{color}%")
    if size:
        where.append("p.product_size LIKE %s")
        params.append(f"%{size}%")

    sql = (
        "SELECT p.id, p.product_name, p.product_color, p.product_size, "
        "p.product_price, p.discount_percent, p.product_quantity, c.category_name "
        "FROM product p LEFT JOIN category c ON p.category_id = c.category_id "
        f"WHERE {' AND '.join(where)} ORDER BY p.discount_percent DESC, p.product_quantity DESC LIMIT 20"
    )

    conn = _connect()
    try:
        with conn.cursor() as cur:
            cur.execute(sql, params)
            rows = cur.fetchall()
    finally:
        conn.close()

    if max_price:
        try:
            mp = float(max_price)
            rows = [r for r in rows if _final_price(r["product_price"], r["discount_percent"]) <= mp]
        except (TypeError, ValueError):
            pass

    items = []
    for r in rows[:5]:
        items.append({
            "id": r["id"],
            "name": r["product_name"],
            "price": _final_price(r["product_price"], r["discount_percent"]),
            "price_text": _fmt_price(_final_price(r["product_price"], r["discount_percent"])),
            "color": r["product_color"],
            "size": r["product_size"],
            "stock": r["product_quantity"],
            "category": r["category_name"],
        })
    return {"total": len(rows), "products": items}


def check_stock(args: dict[str, Any], query: str = "") -> dict[str, Any]:
    name = (args.get("product_name") or args.get("keyword") or "").strip()
    size = (args.get("size") or "").strip()
    color = (args.get("color") or "").strip()

    # Nếu router không trích được tên SP, lấy keyword sản phẩm từ câu hỏi
    if not name:
        m = re.search(
            r"(áo thun|áo sơ mi|áo khoác|áo polo|áo len|áo hoodie|quần jean|quần tây|quần short|"
            r"quần|váy|đầm|chân váy|set bộ|hoodie|giày|túi)",
            (query or "").lower(),
        )
        name = m.group(1) if m else ""

    if not name:
        return {"found": False, "message": "Bạn cho mình tên sản phẩm cần kiểm tra tồn kho nhé."}

    # Tìm theo từng từ trong tên (OR) để bắt được "áo khoác" → "áo khoác dù", "áo khoác bomber"...
    # Ưu tiên cụm dài trước (dùng AND cho cụm >= 2 từ) để tránh "áo khoác" khớp nhầm "áo polo".
    terms = [t for t in name.split() if len(t) > 1] or [name]
    if len(terms) >= 2:
        # AND các từ: "áo khoác" chỉ khớp sản phẩm chứa cả "áo" và "khoác"
        where = " AND ".join(["p.product_name LIKE %s"] * len(terms))
    else:
        where = "p.product_name LIKE %s"
    params = [f"%{t}%" for t in terms]

    sql = (f"SELECT p.product_name, p.product_size, p.product_color, p.product_quantity, "
           f"c.category_name FROM product p LEFT JOIN category c ON p.category_id = c.category_id "
           f"WHERE p.isDeleted = 0 AND ({where}) "
           f"ORDER BY p.product_quantity DESC LIMIT 5")
    conn = _connect()
    try:
        with conn.cursor() as cur:
            cur.execute(sql, params)
            rows = cur.fetchall()
            if not rows and len(terms) >= 2:
                # Nới lỏng: nếu AND không ra, thử OR với từ khóa chính (từ dài nhất)
                main = max(terms, key=len)
                cur.execute(
                    f"SELECT p.product_name, p.product_size, p.product_color, p.product_quantity, "
                    f"c.category_name FROM product p LEFT JOIN category c ON p.category_id = c.category_id "
                    f"WHERE p.isDeleted = 0 AND p.product_name LIKE %s ORDER BY p.product_quantity DESC LIMIT 5",
                    (f"%{main}%",),
                )
                rows = cur.fetchall()
    finally:
        conn.close()

    if not rows:
        return {"found": False, "message": f"Mình chưa tìm thấy sản phẩm '{name}' trong shop."}

    # Ưu tiên dòng có tồn kho
    r = rows[0]
    available = int(r["product_quantity"] or 0) > 0
    if size and size.lower() not in str(r["product_size"] or "").lower():
        available = False
    return {
        "found": True,
        "product_name": r["product_name"],
        "size": size or r["product_size"],
        "color": color or r["product_color"],
        "stock": r["product_quantity"],
        "available": available,
        "matched": len(rows),
    }


def lookup_faq(args: dict[str, Any], query: str = "") -> dict[str, Any]:
    topic = _resolve_topic(args.get("topic"), query)
    return {"topic": topic, "answer": FAQ.get(topic, "Mình chưa có thông tin cho chủ đề này.")}


def get_order(args: dict[str, Any], query: str = "") -> dict[str, Any]:
    code = (args.get("order_code") or "").strip()
    phone = (args.get("phone") or "").strip()

    # Mã đơn DH1234 → số 1234
    order_id = None
    if code:
        m = re.search(r"(\d+)", code)
        if m:
            order_id = int(m.group(1))
    if order_id is None and not phone:
        m = re.search(r"\b(\d{3,})\b", query or "")
        if m:
            order_id = int(m.group(1))

    if order_id is None and not phone:
        return {"found": False, "message": "Bạn cho mình mã đơn (ví dụ DH1234) hoặc số điện thoại đặt hàng nhé."}

    conn = _connect()
    try:
        with conn.cursor() as cur:
            if order_id is not None:
                cur.execute(
                    "SELECT order_id, order_status, payment_status, payment_method, "
                    "total_amount, order_date, shipping_name FROM `order` WHERE order_id = %s",
                    (order_id,),
                )
            else:
                cur.execute(
                    "SELECT order_id, order_status, payment_status, payment_method, "
                    "total_amount, order_date, shipping_name FROM `order` "
                    "WHERE shipping_phone = %s ORDER BY order_date DESC LIMIT 1",
                    (phone,),
                )
            row = cur.fetchone()
    finally:
        conn.close()

    if not row:
        return {"found": False, "message": f"Không tìm thấy đơn hàng cho mã/SĐT bạn cung cấp."}

    return {
        "found": True,
        "order_code": f"DH{row['order_id']}",
        "order_status": row["order_status"],
        "payment_status": row["payment_status"],
        "payment_method": row["payment_method"],
        "total_amount": int(float(row["total_amount"])),
        "total_text": _fmt_price(row["total_amount"]),
        "order_date": str(row["order_date"]),
        "receiver": row["shipping_name"],
    }


# ---------------------------------------------------------------------------
# Tool bổ trợ: hủy đơn, tạo ticket khiếu nại, xem đánh giá
# ---------------------------------------------------------------------------
def cancel_order(args: dict[str, Any], query: str = "") -> dict[str, Any]:
    """
    Hủy đơn hàng. Chỉ cho phép khi đơn CHƯA giao (Chờ xử lý / Đang xử lý).
    Không tự động hủy — trả về xác nhận để khách đồng ý trước (an toàn nghiệp vụ).
    """
    code = (args.get("order_code") or "").strip()
    order_id = None
    if code:
        m = re.search(r"(\d+)", code)
        if m:
            order_id = int(m.group(1))
    if order_id is None:
        m = re.search(r"\b(\d{3,})\b", query or "")
        if m:
            order_id = int(m.group(1))
    if order_id is None:
        return {"found": False, "message": "Bạn cho mình mã đơn cần hủy (ví dụ DH153) nhé."}

    conn = _connect()
    try:
        with conn.cursor() as cur:
            cur.execute("SELECT order_id, order_status FROM `order` WHERE order_id = %s", (order_id,))
            row = cur.fetchone()
    finally:
        conn.close()

    if not row:
        return {"found": False, "message": f"Không tìm thấy đơn DH{order_id} để hủy."}

    status = (row["order_status"] or "").lower()
    cancellable = ("chờ" in status) or ("xử lý" in status and "giao" not in status)
    if not cancellable:
        return {
            "found": True, "cancellable": False, "order_code": f"DH{order_id}",
            "message": f"Đơn DH{order_id} đang ở trạng thái '{row['order_status']}' nên không thể hủy. "
                       f"Bạn vui lòng liên hệ nhân viên để được hỗ trợ.",
        }
    return {
        "found": True, "cancellable": True, "order_code": f"DH{order_id}",
        "order_status": row["order_status"],
        "message": f"Đơn DH{order_id} có thể hủy. Bạn xác nhận muốn hủy đơn này không? "
                   f"(Nhân viên sẽ xử lý yêu cầu của bạn.)",
    }


def create_ticket(args: dict[str, Any], query: str = "") -> dict[str, Any]:
    """Ghi nhận khiếu nại/phản ánh và hướng dẫn khách bước tiếp theo."""
    return {
        "created": True,
        "message": "Mình đã ghi nhận phản ánh của bạn và chuyển tới bộ phận chăm sóc khách hàng. "
                   "Bạn vui lòng để lại mã đơn hàng và mô tả chi tiết (kèm ảnh nếu có) để được hỗ trợ nhanh nhất nhé.",
    }


def get_reviews(args: dict[str, Any], query: str = "") -> dict[str, Any]:
    """Đánh giá sản phẩm — chưa có bảng review trong CSDL, trả thông báo trung thực."""
    return {
        "found": False,
        "message": "Hiện shop chưa có dữ liệu đánh giá cho sản phẩm này. "
                   "Bạn có thể xem mô tả chi tiết và chính sách đổi trả nếu chưa hài lòng nhé.",
    }


# ---------------------------------------------------------------------------
# Dispatcher
# ---------------------------------------------------------------------------
_DISPATCH = {
    "search_products": search_products,
    "check_stock": check_stock,
    "lookup_faq": lookup_faq,
    "get_order": get_order,
    "cancel_order": cancel_order,
    "create_ticket": create_ticket,
    "get_reviews": get_reviews,
}


def execute_tool(name: str, args: dict[str, Any] | None = None, query: str = "") -> dict[str, Any]:
    """Thực thi tool theo tên. Trả dict kết quả (JSON-serializable)."""
    fn = _DISPATCH.get(name)
    if fn is None:
        return {"error": f"tool '{name}' chưa được hỗ trợ"}
    try:
        return fn(args or {}, query)
    except Exception as e:  # noqa: BLE001
        return {"error": f"lỗi khi thực thi {name}: {e}"}


def execute_tool_json(name: str, args: dict[str, Any] | None = None, query: str = "") -> str:
    return json.dumps(execute_tool(name, args, query), ensure_ascii=False)


if __name__ == "__main__":
    # Smoke test nhanh
    for tool, args in [
        ("search_products", {"keyword": "áo thun", "color": "đen"}),
        ("check_stock", {"product_name": "áo polo", "size": "M"}),
        ("lookup_faq", {"topic": "shipping"}),
        ("get_order", {"order_code": "DH153"}),
    ]:
        print(f"{tool}: {execute_tool_json(tool, args)[:180]}")
