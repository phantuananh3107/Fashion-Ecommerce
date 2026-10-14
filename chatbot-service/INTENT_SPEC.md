# ĐẶC TẢ INTENT — CHATBOT THỜI TRANG

> Mục tiêu: bao phủ tối đa case thực tế + xử lý input không liên quan. Tầng 1 của kiến trúc Hybrid (Rule-based), làm nền cho tầng 2 LLM tool-calling.

## 1. Tổng quan 15 intent

| # | Intent | Mục đích | Tool / Hành động | Ưu tiên |
|---|--------|----------|------------------|---------|
| 1 | `find_product` | Tìm sản phẩm theo từ khóa/thuộc tính | `search_products` | 1 |
| 2 | `check_availability` | Hỏi còn hàng / còn size/màu không | `check_stock` | 1 |
| 3 | `price_query` | Hỏi giá sản phẩm cụ thể | `search_products` (kèm giá) | 2 |
| 4 | `promotion_query` | Hỏi khuyến mãi, voucher, giảm giá | `lookup_faq(topic=promotion)` | 2 |
| 5 | `size_guide` | Hỏi bảng size, tư vấn chọn size | `lookup_faq(topic=size_guide)` | 2 |
| 6 | `track_order` | Tra cứu trạng thái đơn hàng | `get_order` | 1 |
| 7 | `cancel_order` | Yêu cầu hủy đơn | `cancel_order` (cần xác nhận) | 1 |
| 8 | `check_policy` | Hỏi chính sách ship/đổi trả/thanh toán | `lookup_faq` | 2 |
| 9 | `review_query` | Hỏi đánh giá sản phẩm | `get_reviews` | 2 |
| 10 | `complaint` | Khiếu nại / phản ánh | `create_ticket` + handoff | 1 |
| 11 | `human_handoff` | Yêu cầu gặp nhân viên | handoff | 1 |
| 12 | `greeting` | Chào hỏi | template | 3 |
| 13 | `goodbye` | Tạm biệt | template | 3 |
| 14 | `thanks` | Cảm ơn | template | 3 |
| 15 | `out_of_scope` | Không liên quan (thời tiết, toán, tin tức…) | fallback template + gợi ý | 0 |
| 16 | `unclear` | Câu mơ hồ, không đủ thông tin | hỏi lại làm rõ | 0 |

> Ưu tiên 1 = nghiệp vụ chính, 2 = hỗ trợ, 3 = xã giao, 0 = meta. Khi 1 tin nhắn chứa nhiều intent, chọn ưu tiên cao nhất.

## 2. Chi tiết từng intent

### 2.1 find_product
- **Mô tả:** Khách muốn tìm/xem/gợi ý sản phẩm. Bao phủ cả câu đầy đủ lẫn từ khóa ngắn.
- **Entities:** `keyword` (áo thun, váy, quần jean…), `category`, `color` (đỏ, đen, trắng…), `size` (S/M/L/XL/28/30…), `price_range` (dưới 200k, 300k-500k…), `brand`
- **Mẫu câu (20):**
  - "tìm áo thun nam màu đen size L"
  - "có váy nào dưới 300k không"
  - "áo khoác jean"
  - "cho xem quần jean nữ"
  - "muốn mua đầm dự tiệc"
  - "tìm áo sơ mi trắng"
  - "có quần short không"
  - "áo hoodie"
  - "tìm đồ cho nam"
  - "quần áo trẻ em"
  - "áo thun" (ngắn gọn)
  - "tim ao thun den" (không dấu)
  - "áo thun đen giá rẻ"
  - "có bán giày không"
  - "tìm áo crop top"
  - "váy maxi"
  - "áo len mùa đông"
  - "tìm set đồ công sở"
  - "có áo khoác da không shop"
  - "cho xem sản phẩm mới"

### 2.2 check_availability
- **Mô tả:** Hỏi còn hàng/không, còn size/màu.
- **Entities:** `product_name`, `size`, `color`
- **Mẫu:** "áo thun đen size M còn hàng không", "váy đỏ còn không", "size L còn không", "màu trắng còn hàng không"

### 2.3 price_query
- **Mô tả:** Hỏi giá cụ thể 1 sản phẩm.
- **Entities:** `product_name`
- **Mẫu:** "áo khoác này giá bao nhiêu", "váy đỏ bao nhiêu tiền", "giá quần jean nam"

### 2.4 promotion_query
- **Mô tả:** Hỏi khuyến mãi, voucher, freeship.
- **Mẫu:** "có mã giảm giá không", "đang có khuyến mãi gì", "freeship không shop", "voucher 50k còn không"

### 2.5 size_guide
- **Mô tả:** Hỏi bảng size, tư vấn size.
- **Mẫu:** "bảng size áo thun", "mình 60kg mặc size gì", "size M bao nhiêu kg", "tư vấn size quần jean"

### 2.6 track_order
- **Mô tả:** Tra đơn. Yêu cầu `order_code` dạng DHxxxx hoặc số điện thoại.
- **Entities:** `order_code` (regex `DH\d{4,}` hoặc `\d{10,}`), `phone`
- **Mẫu:** "đơn DH1234 đang ở đâu", "kiểm tra đơn hàng 0123456789", "đơn của mình giao chưa"

### 2.7 cancel_order
- **Mô tả:** Muốn hủy đơn.
- **Mẫu:** "hủy đơn DH1234", "muốn hủy đơn hàng", "không lấy nữa"

### 2.8 check_policy
- **Mô tả:** Hỏi chính sách chung.
- **Entities:** `topic` (shipping, return, payment, warranty)
- **Mẫu:** "ship bao lâu", "đổi trả thế nào", "thanh toán cod được không", "có trả hàng không"

### 2.9 review_query
- **Mô tả:** Hỏi đánh giá.
- **Mẫu:** "áo này có ai đánh giá chưa", "xem review váy đỏ", "sản phẩm này tốt không"

### 2.10 complaint
- **Mô tả:** Khiếu nại.
- **Mẫu:** "áo bị lỗi", "giao sai hàng", "chất lượng kém", "muốn khiếu nại"

### 2.11 human_handoff
- **Mô tả:** Đòi gặp người.
- **Mẫu:** "gặp nhân viên", "cho gặp người thật", "có ai không", "nhân viên tư vấn"

### 2.12 greeting
- **Mẫu:** "xin chào", "hello", "hi shop", "chào shop", "hey"

### 2.13 goodbye
- **Mẫu:** "tạm biệt", "bye", "cảm ơn nhé bye", "hẹn gặp lại"

### 2.14 thanks
- **Mẫu:** "cảm ơn", "thanks", "cảm ơn shop", "ok cảm ơn"

### 2.15 out_of_scope
- **Mô tả:** Input không liên quan mua sắm thời trang. Phải phát hiện để không gọi tool bừa.
- **Mẫu:** "thời tiết hôm nay thế nào", "giải phương trình x^2", "ai là tổng thống Mỹ", "kể chuyện cười", "dịch câu này sang tiếng anh", "viết code python", "đá bóng tối nay ai thắng", "bạn có yêu tôi không" (chitchat quá xa)

### 2.16 unclear
- **Mô tả:** Câu quá ngắn/mơ hồ, không đủ entities.
- **Mẫu:** "áo", "cái này", "sao", "???", "shop", "alo"

## 3. Quy tắc xử lý input không liên quan

1. **Phát hiện out_of_scope:** nếu không khớp bất kỳ keywords nào của 14 intent chính + chứa từ khóa xa (thời tiết, chính trị, toán, code, dịch thuật, y tế, pháp luật không liên quan thời trang) → `out_of_scope`.
2. **Phản hồi:** template lịch sự + gợi ý 3 việc chatbot làm được, không gọi tool.
3. **unclear:** nếu câu <2 ký tự có nghĩa hoặc chỉ 1 từ chung chung → hỏi lại: "Bạn muốn tìm sản phẩm, tra đơn hay hỏi chính sách ạ?"
4. **An toàn:** không bao giờ đoán order_code hay gọi tool khi thiếu entity bắt buộc (track_order thiếu mã → hỏi lại).

## 4. Xử lý biến thể tiếng Việt

- Chuẩn hóa: lowercase, bỏ dấu câu dư, chuẩn hóa khoảng trắng.
- Không dấu: map `ao`→`áo`, `quan`→`quần` qua từ điển không dấu; matcher chạy trên cả bản có dấu và không dấu.
- Teencode: `shop`, `sp`, `ko/k`, `dc/đc`, `vs`, `ad` → chuẩn hóa trước khi match.
- Emoji/icon: bỏ qua.

## 5. Độ ưu tiên khi nhiều intent

`track_order`/`cancel_order`/`complaint`/`human_handoff` > `find_product`/`check_availability` > `price_query`/`promotion_query`/`size_guide`/`check_policy` > `greeting`/`thanks`/`goodbye` > `out_of_scope`/`unclear`.

Ví dụ: "tìm áo thun và cho hỏi đơn DH1234" → chọn `track_order` (ưu tiên 1) và gợi ý thêm find_product.
