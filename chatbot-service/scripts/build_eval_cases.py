# -*- coding: utf-8 -*-
"""
Sinh data/eval_cases.jsonl — tập câu hỏi mẫu cho JEV (tầng 2).

Nguồn:
  1. 85 case gốc từ tests/test_intents.py (đã đạt 100% với Rule) → split "train"
  2. ~180 paraphrase mới mô phỏng cách viết thực tế → 80% "train", 20% "test"

Paraphrase styles:
  - Không dấu: "tim ao thun den size l"
  - Teencode: "co ao khoac ko", "ship bn lau"
  - Câu dài nhiều điều kiện
  - Lỗi chính tả thiểu, thiếu dấu câu
  - Ngữ nghĩa gần gũi, đời thường
"""
import json
import random
from pathlib import Path

BASE = Path(__file__).resolve().parent.parent
OUT = BASE / "data" / "eval_cases.jsonl"

# ---------------------------------------------------------------------------
# 85 case gốc (trùng với tests/test_intents.py) → train
# ---------------------------------------------------------------------------
ORIGINAL = [
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
    ("áo thun đen size M còn hàng không", "check_availability"),
    ("váy đỏ còn không", "check_availability"),
    ("size L còn không", "check_availability"),
    ("màu trắng còn hàng không", "check_availability"),
    ("con hang khong shop", "check_availability"),
    ("áo khoác này giá bao nhiêu", "price_query"),
    ("váy đỏ bao nhiêu tiền", "price_query"),
    ("giá quần jean nam", "price_query"),
    ("gia bao nhieu shop", "price_query"),
    ("có mã giảm giá không", "promotion_query"),
    ("đang có khuyến mãi gì", "promotion_query"),
    ("freeship không shop", "promotion_query"),
    ("voucher 50k còn không", "promotion_query"),
    ("bảng size áo thun", "size_guide"),
    ("mình 60kg mặc size gì", "size_guide"),
    ("size M bao nhiêu kg", "size_guide"),
    ("tư vấn size quần jean", "size_guide"),
    ("đơn DH1234 đang ở đâu", "track_order"),
    ("kiểm tra đơn hàng 0123456789", "track_order"),
    ("đơn của mình giao chưa", "track_order"),
    ("tra đơn DH999", "track_order"),
    ("đơn hàng của mình đến đâu rồi", "track_order"),
    ("don hang nao ban", "track_order"),
    ("hủy đơn DH5678", "cancel_order"),
    ("muốn hủy đơn hàng", "cancel_order"),
    ("không lấy nữa", "cancel_order"),
    ("ship bao lâu", "check_policy"),
    ("đổi trả thế nào", "check_policy"),
    ("thanh toán cod được không", "check_policy"),
    ("có trả hàng không", "check_policy"),
    ("chính sách bảo hành ra sao", "check_policy"),
    ("áo này có ai đánh giá chưa", "review_query"),
    ("xem review váy đỏ", "review_query"),
    ("sản phẩm này tốt không", "review_query"),
    ("áo bị lỗi", "complaint"),
    ("giao sai hàng", "complaint"),
    ("muốn khiếu nại", "complaint"),
    ("gặp nhân viên", "human_handoff"),
    ("cho gặp người thật", "human_handoff"),
    ("nhân viên tư vấn", "human_handoff"),
    ("xin chào", "greeting"),
    ("hello shop", "greeting"),
    ("chào shop", "greeting"),
    ("hi", "greeting"),
    ("tạm biệt", "goodbye"),
    ("bye", "goodbye"),
    ("hẹn gặp lại", "goodbye"),
    ("cảm ơn", "thanks"),
    ("thanks shop", "thanks"),
    ("cam on ban", "thanks"),
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
    ("???", "unclear"),
    ("sao", "unclear"),
    (".", "unclear"),
    ("cái này", "unclear"),
    ("á", "unclear"),
]

# ---------------------------------------------------------------------------
# Paraphrase mới — mục tiêu: JEV bắt được những câu Rule hay bỏ sót
# ---------------------------------------------------------------------------
PARAPHRASE = {
    "find_product": [
        "mình đi biển cuối tuần mặc gì đẹp",
        "có áo nào mỏng mát cho mùa hè không",
        "tìm quần tây đen cho đi làm",
        "có váy ôm dáng đẹp không",
        "cho tôi xem đồ mùa thu",
        "tìm áo khoác dưới 500k",
        "đồ nữ đi làm công sở nên mặc gì",
        "áo thun trắng basic còn không shop",
        "có quần kaki nữ không",
        "tìm áo bomber nam",
        "muốn mua váy dạ hộp",
        "gợi ý đồ mặc hôm nay",
        "đồ mặc đi dạo phố cuối tuần",
        "tìm áo polo nữ trắng",
        "có đồ vest nam không",
        "áo sweater ấm mùa lạnh",
        "tìm quần lụng nam",
        "shop có bán áo khoác không",
        "cho xem đồ mới nhất",
        "áo dạ hộp nữ đẹp",
        "tìm quần short nữ",
        "đồ trẻ em cute",
        "tôi cần mua áo sơ mi đi làm",
        "có áo phông nữ không",
        "tìm set đồ ở nhà",
        "áo sơ mi kẻ sọc",
        "cho tôi xem túi xách",
        "đồ thời trang nữ cao cấp",
        "mua quần vải cho con",
        # không dấu
        "tim quan ao lot nữ",
        "co ao khoac puach khong",
        "tim ao som mi nam",
        "ao thun trang co khong",
        # teencode
        "co ao croptop ko",
        "tim set do van phong dep",
        "ao mua dot nuoc roi sao re",
        # câu dài
        "tôi cần tìm một chiếc áo thun nữ chất liệu cotton màu trắng cho mùa hè",
        "shop có bán quần jean nữ ống rộng không ạ",
    ],
    "check_availability": [
        "áo này còn bao nhiêu chiếc",
        "size M còn hàng không shop",
        "màu xanh còn không",
        "đơn này còn bao nhiêu hàng",
        "áo hoodie đen còn hàng ko",
        "còn hàng không áo thun trắng",
        "váy đen size S còn không",
        "quần jean còn size 30 không",
        "shop còn item này không",
        "còn size nào của áo khoác",
        # không dấu
        "ao thun den con hang ko",
        "size l con khong",
        "con hang chua shop",
        # teencode
        "con size M ko",
        "hang con ko shop",
        # câu dài
        "cho tôi hỏi chiếc áo khoác denim này còn hàng không ạ",
    ],
    "price_query": [
        "váy này giá bao nhiêu",
        "áo hoodie bao nhiêu tiền",
        "giá quần short nữ",
        "cho tôi hỏi giá chiếc giày này",
        "đồ này giá bao nhiêu vậy",
        "giá set đồ công sở",
        "áo sơ mi bao nhiêu",
        "tính giá giúp tôi cái váy",
        # không dấu
        "gia bao nhieu ao hoodie",
        "quan jean gia bao nhieu",
        # teencode
        "gia ao khoac bn",
        "bao nhieu tien cai day",
        # câu dài
        "cho tôi biết chiếc áo len mùa đông này giá bao nhiêu ạ",
    ],
    "promotion_query": [
        "shop đang có ưu đãi gì",
        "có mã giảm giá nào không",
        "mua 2 cái có được giảm không",
        "đang sale mấy phần",
        "voucher của shop gửi ở đâu",
        "có freeship không",
        "khuyến mãi tháng này là gì",
        "mã ưu đãi dùng ở đâu",
        "shop có giảm giá cho khách mới không",
        # không dấu
        "co ma giam gia khong",
        "shop dang sale gi",
        "voucher con khong",
        # teencode
        "co freeship ko",
        "khuyen mai gi bay gio",
        # câu dài
        "cho tôi hỏi shop có mã giảm giá nào đang chạy không ạ",
    ],
    "size_guide": [
        "mình cao 1m70 nặng 60kg mặc size gì",
        "áo này size M là bao nhiêu kg",
        "tôi mập có mặc được không",
        "bảng size của shop",
        "quần jean này chọn size nào",
        "size XL rộng bao nhiêu",
        "tư vấn giúp tôi chọn size",
        "áo khoác size S có vừa với người gầy không",
        # không dấu
        "bang size ao thun",
        "minh 60kg mac size gi",
        # teencode
        "bang size co khong",
        "tu van size giup minh",
        # câu dài
        "mình cao 1m75 nặng 70kg thì nên chọn size áo thun bao nhiêu ạ",
    ],
    "track_order": [
        "đơn DH2024 của tôi đến đâu rồi",
        "ship của tôi bao giờ tới",
        "tra cứu đơn hàng DH8888",
        "đơn hàng giao chưa shop",
        "tôi mua hôm qua, hàng đang ở đâu",
        "đơn của tôi còn bao lâu nữa tới",
        "kiểm tra đơn 0123456789 giúp tôi",
        "hàng tôi đặt bao giờ ship",
        "đơn DH777 đã giao chưa",
        # không dấu
        "don DH1234 dang o dau",
        "tra don hang cua toi",
        "kiem tra don 0123456789",
        # teencode
        "don cua minh co den chua",
        "ship den dau roi",
        # câu dài
        "cho tôi tra cứu đơn hàng DH5555 tôi đặt hôm qua ạ",
    ],
    "cancel_order": [
        "tôi muốn hủy đơn này",
        "hủy DH3333 giúp tôi",
        "đặt nhầm rồi, hủy đơn giúp tôi",
        "cho tôi hủy đơn hàng với",
        "không muốn nhận nữa",
        "đơn này tôi không lấy",
        "hủy đơn DH666 đi",
        "tôi đổi ý, hủy đơn thôi",
        # không dấu
        "huy don DH1234",
        "khong lay nua",
        # teencode
        "huy don giup minh",
        "toi muon huy don nay",
        # câu dài
        "cho tôi hủy đơn hàng DH888 tôi đặt nhầm mà",
    ],
    "check_policy": [
        "giao hàng mất bao lâu",
        "đổi trả trong bao lâu",
        "thanh toán bằng thẻ được không",
        "có được đổi sản phẩm không",
        "chính sách đổi trả ra sao",
        "hàng lỗi thì xử lý thế nào",
        "shop có bảo hành không",
        "trả hàng cần mang theo gì",
        "phí vận chuyển bao nhiêu",
        "mua online có được không",
        # không dấu
        "ship bao lau",
        "doi tra the nao",
        "thanh toan cod duoc khong",
        # teencode
        "ship bn lau",
        "co doi tra khong",
        # câu dài
        "cho tôi hỏi chính sách đổi trả của shop áp dụng thế nào ạ",
    ],
    "review_query": [
        "áo này có đánh giá tốt không",
        "xem nhận xét của khách về sản phẩm này",
        "mọi người thấy chiếc váy này thế nào",
        "có ai đã mua chưa, đánh giá sao",
        "mình muốn xem review",
        "sản phẩm này chất lượng ra sao",
        "cho tôi xem đánh giá của khách",
        # không dấu
        "co ai danh gia chua",
        "xem review vai do",
        # teencode
        "sp nay danh gia nhu the nao",
        "review co tot khong",
        # câu dài
        "cho tôi xem đánh giá của những khách hàng đã mua chiếc áo khoác này",
    ],
    "complaint": [
        "tôi muốn phản ánh về đơn hàng",
        "hàng tôi nhận bị sai size",
        "sản phẩm không đúng mô tả",
        "giao chậm quá, tôi không hài lòng",
        "tôi cần khiếu nại về chất lượng",
        "vải của áo bị xùa",
        "đơn hàng chưa tới mà đã hủy",
        "nhân viên trả lời không vui",
        # không dấu
        "ao bi loi",
        "giao sai hang",
        # teencode
        "sp loi giup minh",
        "khieu nai don hang",
        # câu dài
        "tôi muốn khiếu nại về chiếc quần tôi mua tuần trước bị lỗi chất liệu",
    ],
    "human_handoff": [
        "cho tôi nói chuyện với nhân viên",
        "tôi cần gặp người tư vấn",
        "có thể kết nối với nhân viên không",
        "tư vấn giúp tôi với người thật",
        "ai đang trực tuyến",
        "tôi muốn nói chuyện với admin",
        "chuyển tôi sang nhân viên",
        # không dấu
        "gap nhan vien",
        "cho gap nguoi that",
        # teencode
        "co ai ko shop",
        "toi muon noi chuyen voi nhan vien",
        # câu dài
        "cho tôi liên hệ trực tiếp với nhân viên tư vấn được không",
    ],
    "greeting": [
        "hello",
        "hi shop",
        "chào bạn",
        "xin chào",
        "hey",
        "chào buổi sáng",
        "chào buổi chiều",
        "alo shop ơi",
        # không dấu
        "chao shop",
        "xin chao",
        # teencode
        "hey shop",
        "oi shop",
    ],
    "goodbye": [
        "bye",
        "tạm biệt",
        "hẹn gặp lại",
        "chào bạn",
        "tôi đi đây",
        "gặp lại nhé",
        # không dấu
        "tam biet",
        "hen gap lai",
        # teencode
        "bye shop",
        "toi di day",
    ],
    "thanks": [
        "cảm ơn shop",
        "thanks",
        "cảm ơn bạn nhiều",
        "cảm ơn nha",
        "đa tạ",
        "cảm ơn vì đã tư vấn",
        # không dấu
        "cam on",
        "cam on shop nhieu",
        # teencode
        "thanks a",
        "cam on nha",
    ],
    "out_of_scope": [
        "hôm nay trời lạnh không",
        "giải giúp tôi bài toán 2x + 3 = 7",
        "tổng thống Mỹ hiện tại là ai",
        "kể tôi nghe chuyện cười",
        "dịch giúp tôi câu này",
        "lập trình python đọc file thế nào",
        "trận bóng tối qua có ai thắng",
        "chứng khoán đang tăng hay giảm",
        "bầu cử president ra sao",
        "hướng dẫn tôi mổ tim",
        "bệnh viêm phổi điều trị thế nào",
        "luật giao dịch dân sự quy định gì",
        # không dấu
        "thoi tiet hom nay",
        "gia chung khoan",
        # teencode
        "giai phuong trinh giup",
        "ke chuyen cuoi",
        # câu dài
        "theo bạn thị trường chứng khoán Việt Nam tuần này sẽ ra sao",
    ],
    "unclear": [
        "à",
        "ừ",
        "hả",
        "gì",
        "kìa",
        "hm",
        "ha",
        "vậy",
        "thế",
        "ơi",
        "ê",
        "chờ đợi",
    ],
}


def main():
    random.seed(42)  # reproducible
    records = []

    # 1. 85 case gốc → train (đã chứng minh Rule đạt 100%)
    for text, intent in ORIGINAL:
        records.append({"text": text, "intent": intent, "split": "train", "source": "original"})

    # 2. Paraphrase → 80% train / 20% test
    for intent, items in PARAPHRASE.items():
        for text in items:
            split = "test" if random.random() < 0.20 else "train"
            records.append({"text": text, "intent": intent, "split": split, "source": "paraphrase"})

    # Ghi JSONL
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with OUT.open("w", encoding="utf-8") as f:
        for r in records:
            f.write(json.dumps(r, ensure_ascii=False) + "\n")

    # Thống kê
    train = [r for r in records if r["split"] == "train"]
    test = [r for r in records if r["split"] == "test"]
    intents = sorted({r["intent"] for r in records})

    print(f"Đã ghi {len(records)} câu → {OUT}")
    print(f"  train: {len(train)} | test: {len(test)}")
    print(f"  số intent: {len(intents)}")
    print()
    print("Phân bố train / test theo intent:")
    for it in intents:
        n_tr = sum(1 for r in train if r["intent"] == it)
        n_te = sum(1 for r in test if r["intent"] == it)
        flag = "  ⚠ thiếu" if n_tr < 10 else ""
        print(f"  {it:22s} train={n_tr:3d}  test={n_te:2d}{flag}")


if __name__ == "__main__":
    main()
