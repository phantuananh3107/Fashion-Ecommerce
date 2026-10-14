# Chatbot Service — Trợ lý tư vấn thời trang (kiến trúc 3 tầng, chạy local)

Dịch vụ chatbot cho website **Fashion-Ecommerce** (TA shop). Chạy hoàn toàn trên máy, **không gọi API cloud**, không tốn chi phí. Viết bằng **FastAPI (Python)** và được
website Java gọi qua REST.

---

## 1. Vì sao có dịch vụ này

Website Java (JSP/Servlet) không có sẵn thư viện NLP/embedding/LLM phù hợp để làm chatbot thông minh. Vì vậy chatbot được tách thành một **service Python riêng**, giao tiếp với website qua JSON — mô hình `Java web → FastAPI → mô hình AI`, đúng kiến trúc đã định trong đề cương đồ án.

---

## 2. Kiến trúc 3 tầng

Câu hỏi của khách đi qua 3 tầng theo thứ tự; tầng nào tự tin thì dừng, không tốn thời gian các tầng sau:

```
Khách hỏi
   │
   ▼
┌───────────────────────────────────────────────────────────┐
│ TẦNG 1 — Rule Matcher          (keyword + regex, ~1ms)    │
│  • Chuẩn hóa: lowercase, bỏ dấu câu, teencode (ko→không)  │
│  • Bỏ dấu: NFKD + thay 'đ'→'d' (Unicode không tách được)  │
│  • Token matching: tránh bắt nhầm "gia"≠"giao", "ao"≠"sao"│
│  • 16 intent, 8 quy tắc phân xử, trích entity             │
│    (order_code DH1234, size, color, price_range, keyword) │
└───────────────┬───────────────────────────────────────────┘
                │ chưa đủ tự tin
                ▼
┌──────────────────────────────────────────────────────────┐
│ TẦNG 2 — JEV Embedding      (e5-small, ~45-89ms)         │
│  • Vector hóa câu hỏi bằng intfloat/multilingual-e5-small│
│  • So cosine với centroid 16 intent (384 chiều)          │
│  • Bắt được câu diễn đạt tự nhiên / không dấu / teencode │
│  • τ = 0.72: đủ chắc thì quyết định luôn                 │
└───────────────┬──────────────────────────────────────────┘
                │ vẫn mơ hồ & Rule không đồng ý
                ▼
┌──────────────────────────────────────────────────────────┐
│ TẦNG 3 — LLM Tool Calling    (Qwen2.5 7B, ~1-4s CPU)     │
│  • Ollama phục vụ qwen2.5:7b-instruct-q4_K_M             │
│  • Ép LLM gọi tool, KHÔNG được bịa giá/tồn kho           │
│  • Tool result trả về LLM để sinh câu trả lời tự nhiên   │
└───────────────┬──────────────────────────────────────────┘
                │
                ▼
        Tool thực thi trên MySQL (database `sys`)
        search_products · check_stock · lookup_faq · get_order
```

**Nguyên tắc thiết kế:** JEV là bộ phân loại **chính**; Rule giữ vai trò *bảo vệ* (chặn câu ngoài phạm vi, trích entity bằng regex) và *short-circuit* những câu không thể mơ hồ (mã đơn `DH1234`, "hủy đơn", chào hỏi). Lý do: đo trên tập test, JEV (80.4%) vượt Rule (71.7%) ở nhóm câu khó.

---

## 3. Cấu trúc thư mục

```
chatbot-service/
├── app/
│   ├── main.py           # FastAPI: POST /api/chat, GET /health, GET /metrics
│   ├── router.py         # HybridRouter — định tuyến 3 tầng + ngưỡng
│   ├── intent_matcher.py # Tầng 1: rule + entity extraction
│   ├── jev_service.py    # Tầng 2: embedding matcher (E5-small)
│   ├── llm_client.py     # Tầng 3: Ollama client + 4 tool schemas
│   └── tools.py          # Thực thi tool trên MySQL thật (database `sys`)
├── data/
│   ├── intents.json      # 16 intent: keyword, pattern, entity, priority
│   ├── centroids.json    # Vector centroid 16 intent (384 chiều) + mẫu câu
│   ├── eval_cases.jsonl  # 322 câu đánh giá (train 276 / test 46)
│   └── thresholds.json   # Ngưỡng đã tinh chỉnh
├── scripts/
│   ├── build_eval_cases.py  # Sinh tập đánh giá từ intents
│   ├── build_centroids.py   # Sinh centroid từ tập train
│   ├── evaluate.py          # So sánh Rule / JEV / Hybrid
│   └── tune_thresholds.py   # Grid search ngưỡng tối ưu
├── tests/
│   ├── test_intents.py          # 85 unit test cho tầng 1 (pytest)
│   ├── e2e_smoke.py             # Smoke test 3 tầng
│   ├── llm_tier3_smoke.py       # Test tầng 3 với Qwen thật
│   ├── api_smoke.py             # Test endpoint FastAPI
│   ├── webapp_integration_test.py # Test tích hợp web app ↔ FastAPI
│   └── diagnose_errors.py       # Phân tích lỗi từng case
├── INTENT_SPEC.md        # Đặc tả 16 intent + mẫu câu
├── requirements.txt
└── README.md             # (file này)
```

---

## 4. Yêu cầu hệ thống

| Thành phần | Yêu cầu |
|---|---|
| Python | 3.10+ (đang dùng 3.10) |
| RAM | 8GB tối thiểu, 16GB thoải mái |
| Ổ đĩa | ~2GB (torch CPU) + ~4.7GB (Qwen 7B) + ~400MB (E5-small) |
| Ollama | Đã cài, model `qwen2.5:7b-instruct-q4_K_M` |
| MySQL | Database chứa dữ liệu shop (bảng `product`, `order`, `users`...) |

Model cache đặt ở **ổ D** để không chiếm ổ C:
- E5-small: `D:\models\huggingface`
- Qwen 7B: `D:\Ollama\models`

---

## 5. Cài đặt

```bash
# 1. Tạo & kích hoạt virtualenv (nếu chưa có)
python -m venv .venv
.venv\Scripts\activate            # Windows

# 2. Cài thư viện
pip install torch --index-url https://download.pytorch.org/whl/cpu
pip install -r requirements.txt

# 3. Tải model embedding E5-small (lần đầu, ~400MB)
python scripts/build_centroids.py
```

Cấu hình database: dịch vụ đọc cùng file `.env` với website Java
(`Fashion-Ecommerce/.env`), gồm `DB_NAME`, `DB_USER`, `DB_PASS`.

---

## 6. Chạy dịch vụ

```bash
# Bước 1: khởi động Ollama (nếu chưa chạy)
ollama serve

# Bước 2: khởi động FastAPI
uvicorn app.main:app --host 0.0.0.0 --port 8000

# Bước 3: kiểm tra
curl http://localhost:8000/health
# → {"ok":true,"rule":true,"jev":true,"llm":true,"model":"qwen2.5:7b-instruct-q4_K_M"}
```

Sau đó khởi động website Java (Tomcat) — chatbot đã sẵn sàng ở widget góc phải màn hình.

---

## 7. API

### `POST /api/chat`

Request:
```json
{
  "message": "tìm áo thun đen size L",
  "user_id": "34",
  "session_id": "D63E4C4D5482CF7DB1184B9A4C6DD347"
}
```

Response:
```json
{
  "reply": "Mình gợi ý 5 sản phẩm phù hợp:\n• Áo thun chữ Y croptop – 716.000đ – màu Đen\n...",
  "intent": "find_product",
  "route": "tier2",
  "tier": 2,
  "tool_called": "search_products",
  "entities": {"keyword": "áo thun", "color": "đen", "size": "l"},
  "ask_missing": null,
  "latency_ms": {"tier1": 1.2, "tier2": 52.3, "total": 53.5}
}
```

### `GET /health`
Trạng thái 3 tầng: `rule`, `jev`, `llm` (true/false) và model đang dùng.

### `GET /metrics`
Thống kê phân bố route (tier1/tier2/tier3) và latency p50/p95 — phục vụ việc
"theo dõi performance để điều chỉnh trọng số".

---

## 8. Bốn tool (truy vấn MySQL thật)

| Tool | Chức năng | Bảng truy vấn |
|---|---|---|
| `search_products` | Tìm sản phẩm theo keyword/danh mục/màu/size/giá | `product`, `category` |
| `check_stock` | Kiểm tra tồn kho theo tên/size/màu | `product` |
| `lookup_faq` | Trả lời chính sách (ship/đổi trả/thanh toán/khuyến mãi/size/bảo hành) | tĩnh |
| `get_order` | Tra trạng thái đơn theo mã `DHxxxx` hoặc SĐT | `order` |
| `cancel_order` | Kiểm tra điều kiện hủy đơn (chỉ đơn chưa giao) | `order` |
| `create_ticket` | Ghi nhận khiếu nại, hướng dẫn bước tiếp theo | tĩnh |
| `get_reviews` | Đánh giá sản phẩm (chưa có bảng review → thông báo trung thực) | — |

---

## 9. Kết quả đo lường

Tập đánh giá: 322 câu (train 276 / **test 46 hold-out**), 16 intent.

| Phương án | Accuracy (test 46) | Accuracy (toàn bộ 322) | Latency |
|---|---|---|---|
| Tầng 1 — Rule | 71.7% | 82.3% | ~1-2ms |
| Tầng 2 — JEV | 80.4% | 87.9% | ~45-89ms |
| **Hybrid 3 tầng** | **91.3%** | **91.6%** | ~58ms (chưa tính tier3) |

Ngưỡng tối ưu (grid search): `RULE_HIGH=0.92`, `RULE_LOW=0.30`, `JEV_TAU=0.72`.

Chạy lại đánh giá:
```bash
python scripts/evaluate.py --split test --pretty
python scripts/tune_thresholds.py --pretty
```

---

## 10. Kiểm thử

```bash
# Unit test tầng 1 (85 case)
python -m pytest tests/test_intents.py -v

# Smoke test 3 tầng
python tests/e2e_smoke.py

# Test tầng 3 với Qwen thật
python tests/llm_tier3_smoke.py

# Test tích hợp web app ↔ FastAPI (cần Tomcat chạy ở :8080)
python tests/webapp_integration_test.py
```

---

## 11. Ghi chú kỹ thuật

- **`đ` không tách dấu được:** Unicode NFKD không phân rã `đ`/`Đ`, nên `remove_accents` phải thay thủ công `đ→d` trước khi normalize.
- **Token matching thay substring:** nếu dùng `in`, `gia` sẽ khớp nhầm `giao`/`giày`, `ao` khớp nhầm `sao`. Hàm `has_phrase` so khớp theo cụm token liên tiếp.
- **`unclear` KHÔNG nằm trong `RULE_TRUSTED_INTENTS`:** câu tự nhiên như "mình đi biển mặc gì đẹp" khiến Rule trả `unclear` (conf=0) nhưng JEV nhận đúng `find_product`; phải để JEV quyết định.
- **Tier1/tier2 cũng thực thi tool:** không chỉ tier3. `_run_tool` gọi `tools.execute_tool` và `_render_tool_reply` sinh câu trả lời tiếng Việt từ kết quả DB.
- **Fallback nhiều lớp:** JEV thiếu → bỏ qua tầng 2; LLM lỗi → vẫn thực thi tool theo router; FastAPI chết → website Java trả câu hướng dẫn tĩnh (không trắng trang).

---

## 12. Liên kết

- Đặc tả intent chi tiết: [`INTENT_SPEC.md`](INTENT_SPEC.md)
- Website Java gọi dịch vụ này qua `service/ChatBotClient.java` và
  `controller/web/ChatBotController.java`
