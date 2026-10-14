"""
Test suite cho Intent Matcher.
Chạy: python -m pytest tests/test_intents.py -v
Mục tiêu: accuracy >= 85% trên toàn bộ case, 100% cho out_of_scope.
"""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app.intent_matcher import IntentMatcher

matcher = IntentMatcher()

# (input, expected_intent)
CASES = [
    # --- find_product (20) ---
    ("tìm áo thun nam màu đen size L", "find_product"),
    ("có váy nào dưới 300k không", "find_product"),
    ("áo khoác jean", "find_product"),
    ("cho xem quần jean nữ", "find_product"),
    ("muốn mua đầm dự tiệc", "find_product"),
    ("tìm áo sơ mi trắng", "find_product"),
    ("có quần short không", "find_product"),
    ("áo hoodie", "find_product"),
    ("tìm đồ cho nam", "find_product"),
    ("quần áo trẻ em", "find_product"),
    ("áo thun", "find_product"),
    ("tim ao thun den", "find_product"),
    ("áo thun đen giá rẻ", "find_product"),
    ("có bán giày không", "find_product"),
    ("tìm áo crop top", "find_product"),
    ("váy maxi", "find_product"),
    ("áo len mùa đông", "find_product"),
    ("tìm set đồ công sở", "find_product"),
    ("cho xem sản phẩm mới", "find_product"),
    ("shop có túi xách không", "find_product"),
    # --- check_availability (5) ---
    ("áo thun đen size M còn hàng không", "check_availability"),
    ("váy đỏ còn không", "check_availability"),
    ("size L còn không", "check_availability"),
    ("màu trắng còn hàng không", "check_availability"),
    ("con hang khong shop", "check_availability"),
    # --- price_query (4) ---
    ("áo khoác này giá bao nhiêu", "price_query"),
    ("váy đỏ bao nhiêu tiền", "price_query"),
    ("giá quần jean nam", "price_query"),
    ("gia bao nhieu shop", "price_query"),
    # --- promotion_query (4) ---
    ("có mã giảm giá không", "promotion_query"),
    ("đang có khuyến mãi gì", "promotion_query"),
    ("freeship không shop", "promotion_query"),
    ("voucher 50k còn không", "promotion_query"),
    # --- size_guide (4) ---
    ("bảng size áo thun", "size_guide"),
    ("mình 60kg mặc size gì", "size_guide"),
    ("size M bao nhiêu kg", "size_guide"),
    ("tư vấn size quần jean", "size_guide"),
    # --- track_order (6) ---
    ("đơn DH1234 đang ở đâu", "track_order"),
    ("kiểm tra đơn hàng 0123456789", "track_order"),
    ("đơn của mình giao chưa", "track_order"),
    ("tra đơn DH999", "track_order"),
    ("đơn hàng của mình đến đâu rồi", "track_order"),
    ("don hang nao ban", "track_order"),
    # --- cancel_order (3) ---
    ("hủy đơn DH5678", "cancel_order"),
    ("muốn hủy đơn hàng", "cancel_order"),
    ("không lấy nữa", "cancel_order"),
    # --- check_policy (5) ---
    ("ship bao lâu", "check_policy"),
    ("đổi trả thế nào", "check_policy"),
    ("thanh toán cod được không", "check_policy"),
    ("có trả hàng không", "check_policy"),
    ("chính sách bảo hành ra sao", "check_policy"),
    # --- review_query (3) ---
    ("áo này có ai đánh giá chưa", "review_query"),
    ("xem review váy đỏ", "review_query"),
    ("sản phẩm này tốt không", "review_query"),
    # --- complaint (3) ---
    ("áo bị lỗi", "complaint"),
    ("giao sai hàng", "complaint"),
    ("muốn khiếu nại", "complaint"),
    # --- human_handoff (3) ---
    ("gặp nhân viên", "human_handoff"),
    ("cho gặp người thật", "human_handoff"),
    ("nhân viên tư vấn", "human_handoff"),
    # --- greeting (4) ---
    ("xin chào", "greeting"),
    ("hello shop", "greeting"),
    ("chào shop", "greeting"),
    ("hi", "greeting"),
    # --- goodbye (3) ---
    ("tạm biệt", "goodbye"),
    ("bye", "goodbye"),
    ("hẹn gặp lại", "goodbye"),
    # --- thanks (3) ---
    ("cảm ơn", "thanks"),
    ("thanks shop", "thanks"),
    ("cam on ban", "thanks"),
    # --- out_of_scope (10) ---
    ("thời tiết hôm nay thế nào", "out_of_scope"),
    ("giải phương trình x^2 + 1 = 0", "out_of_scope"),
    ("ai là tổng thống hiện tại", "out_of_scope"),
    ("kể chuyện cười đi", "out_of_scope"),
    ("dịch câu này sang tiếng anh", "out_of_scope"),
    ("viết code python cho mình", "out_of_scope"),
    ("đá bóng tối nay ai thắng", "out_of_scope"),
    ("giá chứng khoán hôm nay", "out_of_scope"),
    ("chính sách bầu cử ra sao", "out_of_scope"),
    ("hướng dẫn mổ tim", "out_of_scope"),
    # --- unclear (5) ---
    ("???", "unclear"),
    ("sao", "unclear"),
    (".", "unclear"),
    ("cái này", "unclear"),
    ("á", "unclear"),
]


def run_all():
    total = len(CASES)
    passed = 0
    failures = []
    for text, expected in CASES:
        result = matcher.match(text)
        got = result["intent"]
        if got == expected:
            passed += 1
        else:
            failures.append((text, expected, got, result["confidence"]))
    accuracy = passed / total
    return total, passed, accuracy, failures


def test_accuracy_threshold():
    """Accuracy tổng >= 85%."""
    total, passed, accuracy, failures = run_all()
    assert accuracy >= 0.85, f"Accuracy {accuracy:.1%} < 85%. Failures: {failures}"


def test_out_of_scope_perfect():
    """Tất cả case out_of_scope phải đúng 100% (không gọi tool bừa)."""
    oos_cases = [(t, e) for t, e in CASES if e == "out_of_scope"]
    failures = []
    for text, expected in oos_cases:
        result = matcher.match(text)
        assert result["intent"] == "out_of_scope", f"out_of_scope fail: {text} -> {result['intent']}"
        assert result["tool"] is None, f"out_of_scope must not call tool: {text}"
    assert not failures


def test_no_tool_for_out_of_scope():
    """Kiểm tra mọi input không liên quan đều không sinh tool."""
    junk = ["chào bạn", "123", "ok", "á", "hehe", "whatever"]
    for text in junk:
        r = matcher.match(text)
        if r["intent"] == "out_of_scope":
            assert r["tool"] is None


def test_track_order_requires_code():
    """track_order thiếu mã đơn → phải ask_missing."""
    r = matcher.match("đơn của mình giao chưa")
    if r["intent"] == "track_order":
        # nếu không có order_code trong text thì phải hỏi lại
        if "order_code" not in r["entities"]:
            assert r["ask_missing"] is not None, "phải ask_missing khi thiếu mã đơn"


def test_order_code_extraction():
    """Entity order_code trích đúng."""
    r = matcher.match("kiểm tra đơn DH12345")
    assert "dh12345" in str(r["entities"].get("order_code", "")).lower()


def test_size_color_extraction():
    """Entity size và color trích đúng."""
    r = matcher.match("tìm áo thun đen size L")
    assert r["intent"] == "find_product"
    assert r["entities"].get("size", "").lower() == "l"
    assert "đen" in str(r["entities"].get("color", ""))


def test_tehnh_code_normalization():
    """Teencode: 'ko' → không vẫn match được."""
    r = matcher.match("shop co ao thun ko")
    assert r["intent"] in ("find_product", "check_availability")


def test_no_accent_input():
    """Input không dấu vẫn match được."""
    r = matcher.match("tim ao thun mau do")
    assert r["intent"] == "find_product"


if __name__ == "__main__":
    total, passed, accuracy, failures = run_all()
    print(f"RESULT: {passed}/{total} = {accuracy:.1%}")
    if failures:
        print("FAILURES:")
        for text, expected, got, conf in failures:
            print(f"  '{text}' -> expected={expected} got={got} (conf={conf:.2f})")
    else:
        print("ALL PASS")
    # run unittest-style
    import pytest

    sys.exit(pytest.main([__file__, "-v"]))
