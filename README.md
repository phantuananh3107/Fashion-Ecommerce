# 🛍️ Fashion Ecommerce System (`fashionecom`)



## 📌 1. TỔNG QUAN DỰ ÁN (PROJECT OVERVIEW)

- **Tên dự án:** Website Thương mại Điện tử Thời trang (`fashionecom` / `laptrinhjavaweb`)
- **Mục đích:** Hệ thống bán hàng thời trang trực tuyến full-stack hỗ trợ Khách hàng mua sắm (Web Client), Quản trị viên vận hành hệ thống (Admin Portal), tích hợp **AI Tư vấn Bán hàng (Google Gemini REST API)** và **Thanh toán Trực tuyến VNPay Gateway (Sandbox)**.
- **Kiến trúc:** Monolithic Model-View-Controller (**MVC**) chuẩn Java Web Classic.
  - **Model:** Java Object POJOs + Tầng DAO (Data Access Object) tương tác MySQL với JDBC & Connection Pool C3P0.
  - **View:** Dynamic JSP (JavaServer Pages), JSTL, kết hợp **SiteMesh Decorator Framework 2.4.2** để quản lý giao diện chung (`web.jsp` và `admin.jsp`).
  - **Controller:** Java Servlet API 4.0 (dùng Annotation `@WebServlet`).
  - **Authentication & Security:** Cookie/Session-based auth với `AuthorizationFilter` kiểm soát phân quyền.

---

## 🛠️ 2. CÔNG NGHỆ & THƯ VIỆN SỬ DỤNG (TECH STACK)

| Thành phần | Công nghệ / Thư viện | Phiên bản | Mô tả |
| :--- | :--- | :--- | :--- |
| **Language** | Java | JDK 17 | Ngôn ngữ backend chính |
| **Web Spec** | Servlet API & JSP / JSTL | 4.0.1 / 1.2 | Xử lý request HTTP & render giao diện HTML |
| **Decorator** | SiteMesh | 2.4.2 | Khung bao giao diện (Header, Footer, Sidebar admin) |
| **Database** | MySQL | 8.0.28 | Cơ sở dữ liệu quan hệ |
| **DB Pool** | C3P0 | 0.9.5.5 | Quản lý connection pool JDBC |
| **Boilerplate** | Lombok | 1.18.30 | Auto-generate Getters, Setters, Constructors |
| **File Upload** | Apache Commons FileUpload & IO | 1.5 / 2.16.1 | Xử lý upload ảnh sản phẩm từ Admin |
| **JSON Parser**| Jackson Databind | 2.15.2 | Xử lý dữ liệu JSON |
| **Environment**| DotEnv Java | 3.0.0 | Đọc biến môi trường từ file `.env` |
| **AI Integration**| Google Gemini API (`gemini-2.5-flash`) | REST API | Trợ lý tư vấn thời trang thông minh |
| **Payment** | VNPay Gateway | API v2.1.0 | Cổng thanh toán quét mã / ATM / QR (HMAC SHA-512) |

---

## 📁 3. CẤU TRÚC THƯ MỤC & MÃ NGUỒN CHI TIẾT (DETAILED CODE STRUCTURE)

```
d:\fashion-ecommerce\
├── .env                              # Khai báo API Key Gemini, config Chatbot
├── pom.xml                           # Cấu hình Maven dependencies & compiler Java 17
└── src/main/
    ├── java/
    │   ├── controller/               # Tầng Servlet xử lý điều hướng Request
    │   │   ├── admin/                # Quản trị viên (Phân quyền /admin/*)
    │   │   │   ├── addProduct.java            # Servlet thêm sản phẩm mới + upload 4 ảnh
    │   │   │   ├── addUser.java               # Servlet thêm người dùng mới từ Admin
    │   │   │   ├── DeleteProduct.java         # Xóa mềm sản phẩm (isDeleted = true)
    │   │   │   ├── DeleteUser.java            # Xóa/Vô hiệu hóa người dùng
    │   │   │   ├── DetailProduct.java         # Xem thông tin chi tiết 1 sản phẩm
    │   │   │   ├── DetailUser.java            # Xem thông tin chi tiết 1 người dùng
    │   │   │   ├── HomeController.java        # Trang Dashboard thống kê Admin
    │   │   │   ├── InactiveSoonServlet.java   # Cảnh báo user sắp bị khóa do lâu không dùng
    │   │   │   ├── ManageAccount.java         # Quản lý danh sách tài khoản
    │   │   │   ├── ManageProduct.java         # Quản lý danh sách sản phẩm
    │   │   │   ├── OrderDeleteController.java # Xóa đơn hàng
    │   │   │   ├── OrderEdit.java             # Trang chỉnh sửa đơn hàng
    │   │   │   ├── OrderList.java             # Quản lý danh sách đơn hàng (Filter & Paging)
    │   │   │   ├── OrderUpdateController.java # Cập nhật trạng thái đơn & thanh toán
    │   │   │   ├── PermanentlyDeleteProduct.java # Xóa vĩnh viễn sản phẩm khỏi DB
    │   │   │   ├── RecentSale.java            # Thống kê giao dịch gần đây
    │   │   │   ├── RepairProduct.java         # Chỉnh sửa thông tin sản phẩm
    │   │   │   ├── ReportPrice.java           # Báo cáo doanh thu / tồn kho
    │   │   │   ├── ReportUser.java            # Báo cáo phân tích người dùng
    │   │   │   ├── RestoreProduct.java        # Khôi phục sản phẩm đã xóa mềm
    │   │   │   └── TopSelling.java            # Báo cáo Top sản phẩm bán chạy nhất
    │   │   ├── auth/                 # Xác thực & Phân quyền
    │   │   │   ├── LoginController.java       # Xử lý Đăng nhập, chống Brute-force, Session
    │   │   │   ├── Logout.java                # Đăng xuất, hủy Session
    │   │   │   └── RegisterController.java    # Đăng ký tài khoản khách hàng mới
    │   │   ├── filter/               # Bộ lọc Servlet Filter
    │   │   │   └── AuthorizationFilter.java   # Chặn truy cập trái phép theo Session Role
    │   │   └── web/                  # Khách hàng (Phân quyền /customer/* & /public/*)
    │   │       ├── AddToCartController.java   # Thêm sản phẩm vào giỏ (Session & DB)
    │   │       ├── AddressController.java     # Quản lý sổ địa chỉ nhận hàng
    │   │       ├── AllProduct.java            # Trang hiển thị toàn bộ sản phẩm + Lọc
    │   │       ├── CartController.java        # Trang giỏ hàng đầy đủ
    │   │       ├── CategoryController.java    # Xem sản phẩm theo danh mục
    │   │       ├── ChatBotController.java     # Endpoint nhận tin nhắn AJAX từ Chatbot AI
    │   │       ├── CheckVoucherController.java# AJAX kiểm tra mã giảm giá
    │   │       ├── CheckoutController.java    # Trang điền thông tin giao hàng & thanh toán
    │   │       ├── DeleteCartController.java  # Xóa item khỏi giỏ hàng
    │   │       ├── HomeController.java        # Trang chủ khách hàng
    │   │       ├── MiniCartController.java    # Render popup giỏ hàng nhanh (AJAX)
    │   │       ├── OrderDetailController.java # Xem chi tiết đơn hàng cá nhân
    │   │       ├── OrderHistoryController.java# Lịch sử đơn hàng của người dùng
    │   │       ├── ProcessCheckoutController.java # Xử lý chốt đơn (COD & Redirect VNPay)
    │   │       ├── ProductDetail.java         # Trang chi tiết sản phẩm khách xem
    │   │       ├── ProfileController.java     # Cập nhật thông tin cá nhân khách hàng
    │   │       ├── SaleController.java        # Trang chuyên mục sản phẩm Khuyến mãi (Sale)
    │   │       ├── SearchController.java      # Tìm kiếm sản phẩm theo từ khóa
    │   │       ├── UpdateCartController.java  # Thay đổi số lượng item giỏ hàng
    │   │       ├── VnPayReturnController.java # Endpoint hứng kết quả thanh toán từ VNPay
    │   │       ├── WishlistController.java    # Quản lý danh sách yêu thích
    │   │       └── saleT5.java                # Chuyên trang khuyến mãi tháng 5
    │   ├── dao/                      # Interface Data Access Object
    │   │   ├── AddressDAO.java
    │   │   ├── CartDAO.java
    │   │   ├── CategoryDAO.java
    │   │   ├── DashboardDAO.java
    │   │   ├── IProductDAO.java
    │   │   ├── ITopSelling.java
    │   │   ├── OrderDAO.java
    │   │   ├── UserDAO.java
    │   │   ├── VoucherDAO.java
    │   │   ├── WishlistDAO.java
    │   │   └── Impl/                 # Implementations tầng DAO với JDBC
    │   │       ├── AddressDAOImpl.java
    │   │       ├── CartDAOImpl.java
    │   │       ├── CategoryDAOImpl.java
    │   │       ├── DashboardDAOImpl.java
    │   │       ├── OrderDAOImpl.java
    │   │       ├── ProductImpl.java
    │   │       ├── TopSellingImpl.java
    │   │       ├── UserDAOImpl.java
    │   │       ├── VoucherDAOImpl.java
    │   │       └── WishlistDAOImpl.java
    │   ├── model/                    # Tầng POJO / Model Entities
    │   │   ├── AddressObject.java
    │   │   ├── CartObject.java
    │   │   ├── CategoryObject.java
    │   │   ├── OrderDetailObject.java
    │   │   ├── OrderInfo.java
    │   │   ├── OrderObject.java
    │   │   ├── ProductImageObject.java
    │   │   ├── ProductObject.java
    │   │   ├── RoleObject.java
    │   │   ├── TopSellingProduct.java
    │   │   ├── UserObject.java
    │   │   ├── VoucherObject.java
    │   │   └── WishlistObject.java
    │   ├── service/                  # Business Logic Layer
    │   │   └── ChatBotService.java    # Thuật toán lọc Intent + Giao tiếp với AI Gemini API
    │   └── util/                     # Helpers & Utility Classes
    │       ├── ChatBotConfig.java     # Cấu hình AI Gemini API (đọc .env)
    │       ├── Config.java            # Cấu hình VNPay Gateway (TmnCode, Hash Secret, URL)
    │       ├── DBUtil.java            # C3P0 DataSource Connection Pool Manager
    │       └── ProductSearchHelper.java
    └── webapp/
        ├── WEB-INF/
        │   ├── web.xml                # Cấu hình Servlet & SiteMesh Filter
        │   └── decorators.xml         # Cấu hình định tuyến layout SiteMesh (/admin* vs /*)
        ├── decorators/                # File Layout chung
        │   ├── admin.jsp              # Template khung Admin (Header, Sidebar, Content)
        │   └── web.jsp                # Template khung Web Client (Header, Footer, Chatbot)
        └── views/                     # Các trang JSP con
            ├── admin/                 # Giao diện trang quản trị (15 JSPs)
            └── web/                   # Giao diện cửa hàng khách hàng (16 JSPs)
```

---

## ⚡ 4. DANH SÁCH CHỨC NĂNG CHI TIẾT (FEATURE LIST)

### 🛒 4.1. Phân hệ Khách hàng (Web Storefront)
1. **Trang chủ & Duyệt sản phẩm:**
   - Xem banner, sản phẩm mới nhất, sản phẩm giảm giá, danh mục sản phẩm.
   - Bộ lọc nâng cao trên trang danh mục / trang Sale: Lọc theo danh mục, lọc theo màu sắc (`product_color`), khoảng giá thực tế (`minPrice` - `maxPrice` sau giảm gia), phần trăm discount.
   - Sắp xếp theo giá tăng/giảm, sản phẩm mới nhất.
   - Tìm kiếm sản phẩm theo từ khóa (tên sản phẩm, mã sản phẩm `product me`, tên danh mục).
2. **Chi tiết sản phẩm (`ProductDetail`):**
   - Xem bộ sưu tập 4 hình ảnh sản phẩm (`product_image1` đến `product_image4`).
   - Chọn Size, Màu sắc, Số lượng.
   - Tự động tính toán giá gốc vs giá SALE dựa trên `discount_percent`.
3. **Quản lý Giỏ hàng (`CartController` & `MiniCartController`):**
   - Thêm sản phẩm vào giỏ bằng AJAX mà không cần load lại trang.
   - Giỏ hàng đồng bộ linh hoạt: Khi khách chưa đăng nhập -> lưu vào Session; Khi khách đăng nhập -> tự động lưu và đồng bộ xuống bảng `Cart` trong MySQL.
   - Cập nhật số lượng, xóa item giỏ hàng, tính tổng tiền tự động.
4. **Mã giảm giá (`CheckVoucherController`):**
   - Nhập mã Voucher giảm giá -> Kiểm tra số lượng khả dụng, ngày hết hạn -> Trực tiếp trừ số tiền giảm (`discountAmount`) vào hóa đơn.
5. **Thanh toán & Đặt hàng (`ProcessCheckoutController` & `VnPayReturnController`):**
   - Quản lý sổ địa chỉ giao hàng (`AddressController`) hoặc điền thông tin giao hàng mới.
   - **Hình thức 1: COD (Cash On Delivery)** - Đặt hàng trả tiền mặt.
   - **Hình thức 2: VNPay Online Payment** - Hệ thống sinh URL thanh toán VNPay Sandbox hóa HMAC SHA-512. Khách thanh toán thành công -> VNPay callback về `VnPayReturnController` -> Kiểm tra chữ ký an toàn -> Chuyển trạng thái đơn thành "Đã thanh toán (VNPAY)" -> Tự động trừ kho & dọn giỏ hàng.
6. **Lịch sử đơn hàng & Hồ sơ cá nhân (`OrderHistoryController`, `ProfileController`):**
   - Xem danh sách các đơn hàng đã đặt và trạng thái chi tiết (Chờ xử lý, Đã xác nhận, Đang giao, Thành công, Đã hủy).
   - Quản lý danh sách sản phẩm yêu thích (Wishlist).

---

### 👑 4.2. Phân hệ Quản trị viên (Admin Portal)
1. **Bảng điều khiển Thống kê (`HomeController` / `DashboardDAOImpl`):**
   - Thống kê Tổng doanh thu hệ thống (từ các đơn hàng thành công).
   - Thống kê Tổng giá trị hàng tồn kho (`product_price * (product_quantity - sold)`).
   - Biểu đồ tăng trưởng người dùng mới theo tháng.
   - Thống kê Top sản phẩm bán chạy nhất (`TopSellingImpl`).
   - Danh sách giao dịch mới nhất (`RecentSale`).
2. **Quản lý Sản phẩm (`ManageProduct`, `addProduct`, `RepairProduct`):**
   - Danh sách sản phẩm phân trang.
   - Thêm mới sản phẩm: Nhập mã SP, tên, giá, số lượng, màu, size, mô tả, danh mục và **Upload đồng thời 4 hình ảnh**.
   - Chỉnh sửa sản phẩm: Cập nhật thông tin, thay đổi ảnh.
   - **Xóa mềm (Soft Delete):** Cập nhật `isDeleted = TRUE` để không làm đứt gãy lịch sử hóa đơn.
   - **Thùng rác & Khôi phục:** Xem danh sách SP đã xóa mềm, chọn khôi phục (`RestoreProduct`) hoặc Xóa vĩnh viễn (`PermanentlyDeleteProduct`).
3. **Quản lý Tài khoản & Khách hàng (`ManageAccount`, `addUser`, `DetailUser`):**
   - Xem danh sách tài khoản, tìm kiếm & sắp xếp theo tên (A-Z), ID, ngày tạo.
   - Vô hiệu hóa tài khoản (`user_isactive = 3`).
   - Cảnh báo danh sách người dùng sắp chuyển sang trạng thái ngưng hoạt động (`InactiveSoonServlet`).
4. **Quản lý Đơn hàng (`OrderList`, `OrderEdit`, `OrderUpdateController`):**
   - Bộ lọc đơn hàng theo: Trạng thái đơn (Chờ xử lý, Đã xác nhận...), Trạng thái thanh toán, Phương thức thanh toán.
   - Chỉnh sửa thông tin đơn hàng, cập nhật trạng thái đơn hàng & thanh toán.
   - Xóa đơn hàng (tự động xóa cascade các item trong `OrderDetail`).

---

### 🤖 4.3. Phân hệ Trợ lý AI Tư vấn Bán hàng (`ChatBotService`)
- **Mô hình:** Google Gemini (`gemini-2.5-flash` qua HTTP REST API).
- **Thuật toán Tìm kiếm theo Ý định (Intent-based Filter):**
  1. Nhận tin nhắn từ khách hàng (Ví dụ: *"Tìm cho tôi đầm đi tiệc màu đỏ dưới 1 triệu"*).
  2. Hệ thống phân tích cú pháp Java loại sản phẩm (*đầm*), màu sắc (*đỏ*), khoảng giá (*< 1.000.000 VNĐ*), hàng sale/bán chạy.
  3. Truy vấn trực tiếp Database lấy ra danh sách sản phẩm thực tế khớp nhất.
  4. Ghép ngữ cảnh (Context) chứa dữ liệu 5 sản phẩm khớp nhất vào **System Prompt**.
  5. Gửi request đến Gemini API để AI sinh câu trả lời tư vấn tự nhiên, chuẩn xác với sản phẩm đang bán trong kho.

---

## 🔍 5. PHÂN TÍCH LOGIC CODE: ĐÚNG vs SAI / BUG / TECHNICAL DEBT (CRITICAL FOR AI)

> 💡 **Mục này cực kỳ quan trọng đối với Claude/Gemini khi tiếp cận chỉnh sửa code để tránh lặp lại lỗi cũ hoặc phá vỡ logic đang chạy.**

### ✅ 5.1. Những Logic ĐÚNG & Thiết kế Tốt (Best Practices)
1. **Transaction Management Đảm bảo Tính Nguyên tố (Atomicity):**
   - Trong `OrderDAOImpl.insertOrder()`: Khi người dùng đặt hàng, hệ thống dùng `conn.setAutoCommit(false)`, thêm hóa đơn chính -> thêm các `OrderDetail` -> **Cập nhật trừ số lượng kho (`Product.product_quantity`)** với điều kiện `product_quantity >= quantity_bought`. Nếu bất kỳ sản phẩm nào bị hết hàng giữa chừng, toàn bộ transaction sẽ `rollback()`.
2. **Đồng bộ Giỏ hàng Thông minh (Session + Database):**
   - Khách vãng lai dùng Giỏ hàng bằng Session RAM. Khi Đăng nhập (`LoginController`), hệ thống nạp toàn bộ giỏ hàng lưu dưới DB (`Cart` table) lên Session một lần duy nhất.
3. **Tính toán Giá Khuyến mãi Thống nhất:**
   - Giá bán thực tế luôn được áp dụng công thức: `price = product_price * (100 - discount_percent) / 100.0` thống nhất ở cả DAO Lọc, Cart, Checkout và AI Chatbot.
4. **Chống Brute-force Login:**
   - Trong `LoginController`: Đếm số lần đăng nhập sai `loginAttempts` trong Session. Nếu sai 5 lần liên tiếp -> Tự động gọi `UserDAO.lockUserAccount()` cập nhật `user_isactive = 2` (Tạm khóa).

---

### ❌ 5.2. Những Logic SAI, Lỗi Tiềm ẩn & Nợ Kỹ thuật (Flaws, Bugs & Technical Debt)

#### 🔴 A. BẢO MẬT (SECURITY RISKS) - MỨC ĐỘ NGHÊM TRỌNG
1. **Mật khẩu lưu dạng PLAIN TEXT:**
   - `UserDAOImpl` lưu và so sánh trực tiếp mật khẩu thô (Plain Text) mà không qua thuật toán băm (BCrypt, Argon2, PBKDF2).
2. **Hardcode Gemini API Key Fallback trong Source Code:**
   - Trong `ChatBotConfig.java` (Dòng 18): Có chứa hardcode API Key mặc định `AIzaSyAdQzszvoqk9Zq...` nếu file `.env` bị thiếu. **Tuyệt đối không để lộ API key trong file Java!**
3. **Hardcode Thông tin DB & VNPay:**
   - `DBUtil.java` hardcode `root` / `123456`.
   - `Config.java` hardcode `vnp_TmnCode` và `secretKey`. Cần chuyển tất cả vào file `.env`.
4. **Hạn chế của `AuthorizationFilter`:**
   - Filter chỉ kiểm tra URL prefix `/admin/`, `/customer/`, `/public/`. Nếu người dùng gõ trực tiếp URL file JSP (ví dụ: `http://localhost:8080/views/admin/home.jsp`), họ có thể bypass Filter nếu server container không chặn truy cập trực tiếp `/views/`.

#### 🟡 B. LỖI LOGIC & BẤT ĐỒNG BỘ (LOGIC BUGS)
1. **Bất đồng bộ Tên đăng nhập vs Email (`UserDAOImpl` & `LoginController`):**
   - Trang JSP nhận parameter `username`, nhưng câu SQL lại query: `WHERE user_email = ? AND password = ?`. Do đó người dùng **bắt buộc phải nhập Email** để đăng nhập, gõ username thật sẽ bị báo sai tài khoản!
2. **C3P0 Connection Pool Limit quá nhỏ (`DBUtil.java`):**
   - `DB_MAX_CONNECTIONS = 4`. Giới hạn 4 kết nối đồng thời là quá bé đối với ứng dụng Web. Khi có vài request AJAX chạy song song (Chatbot + MiniCart + Lấy sản phẩm), ứng dụng sẽ bị treo đơ do chờ Connection Pool (`Timeout Exception`).
3. **Lỗi Quản lý Connection & Transaction trong `exe()` Helper Method:**
   - Trong `UserDAOImpl.exe()` và `ProductImpl.exe()`: Code gọi `conn.commit()` vô điều kiện. Nếu connection đang ở chế độ auto-commit mặc định (`autoCommit = true`), lệnh `conn.commit()` có thể gây lỗi hoặc không đúng chuẩn JDBC API. Đồng thời trong khối `finally`, code gọi `conn.setAutoCommit(true)` mà **không hề đóng connection** `conn.close()`, dẫn đến nguy cơ RÒ RỈ KẾT NỐI (Connection Leak) khi dùng C3P0!
4. **Parse Response JSON từ AI Gemini bằng Thuật toán Chuỗi Thủ công (`ChatBotService`):**
   - `ChatBotService.extractMessageFromGeminiJson()` tự viết hàm tìm vị trí chuỗi `jsonResponse.indexOf("\"text\":")` thay vì dùng thư viện Jackson `ObjectMapper`. Nếu câu trả lời của AI chứa dấu ngoặc kép, ký tự xuống dòng `\n` hoặc cấu trúc JSON Gemini thay đổi nhẹ, hàm này sẽ ném ngoại lệ làm lỗi Chatbot.
5. **Escape JSON Thủ công:**
   - `ChatBotService.buildGeminiJsonRequest()` tự replace `"` thành `\"`. Nếu prompt chứa HTML hoặc ký tự UTF-8 đặc biệt, chuỗi JSON gửi lên Gemini dễ bị malformed (Lỗi HTTP 400 Bad Request).
6. **Ép kiểu Dữ liệu Tiền tệ không đồng nhất (`float` vs `double`):**
   - Trong `OrderDAOImpl.java`, một số nơi dùng `rs.getFloat("total_amount")` / `rs.getFloat("price")`, trong khi Model định nghĩa `double`. Khi xử lý số tiền hàng triệu VNĐ, kiểu `float` bị mất độ chính xác phần thập phân.

---

## 🗄️ 6. CƠ SỞ DỮ LIỆU & BẢNG CHÍNH (DATABASE SCHEMA SUMMARY)

Tên CSDL: `fashionecom`

- `users`: Thông tin người dùng (`user_id`, `user_fullname`, `user_email`, `password`, `user_phone_number`, `user_address`, `user_isactive` [1: Active, 2: Locked, 3: Disabled], `login_count`, `role_id`).
- `roles`: Vai trò hệ thống (`role_id`, `role_name` ['ADMIN', 'CUSTOMER']).
- `Product`: Thông tin sản phẩm (`id`, `product_code`, `product_name`, `product_image1..4`, `product_price`, `product_quantity`, `product_color`, `product_size`, `product_description`, `category_id`, `discount_percent`, `isDeleted`, `created_at`, `updated_at`).
- `category`: Danh mục sản phẩm (`category_id`, `category_name`, `description`).
- `Order`: Đơn hàng (`order_id`, `user_id`, `total_amount`, `order_status`, `payment_status`, `payment_method`, `order_note`, `shipping_name`, `shipping_phone`, `shipping_address`, `shipping_fee`, `discount_amount`, `voucher_id`, `order_date`).
- `OrderDetail`: Chi tiết hóa đơn (`order_detail_id`, `order_id`, `product_id`, `product_size`, `product_color`, `quantity_sold`, `price`).
- `cart`: Giỏ hàng lưu vết DB (`cart_id`, `user_id`, `product_id`, `quantity`, `product_size`, `created_at`).
- `voucher`: Mã giảm giá (`voucher_id`, `voucher_code`, `discount_amount`, `quantity`, `expiration_date`).
- `wishlist`: Danh sách yêu thích (`wishlist_id`, `user_id`, `product_id`, `created_at`).
- `addresses`: Sổ địa chỉ khách hàng (`address_id`, `user_id`, `recipient_name`, `phone_number`, `province`, `district`, `ward`, `detailed_address`, `is_default`).

---

## 🚀 7. HƯỚNG DẪN CHẠY & CẤU HÌNH (SETUP & ENVIRONMENT)

### 1. Cấu hình CSDL:
- Tạo cơ sở dữ liệu `fashionecom` trong MySQL (Port 3306).
- Cập nhật tài khoản/mật khẩu DB trong `src/main/java/util/DBUtil.java` (hoặc cấu hình lại file `.env`).

### 2. Cấu hình File `.env`:
Tạo hoặc chỉnh sửa file `.env` ở thư mục gốc dự án:
```env
GEMINI_API_KEY=AIzaSy... (API Key của bạn từ Google AI Studio)
CHATBOT_MODEL=gemini-2.5-flash
CHATBOT_TEMPERATURE=0.7
CHATBOT_MAX_TOKENS=2048
```

### 3. Build & Run Dự án:
- Sử dụng IntelliJ IDEA / Eclipse với Tomcat Web Server (Apache Tomcat 9.0 / 10.0 tương thích Servlet 4.0).
- Build Maven command:
  ```bash
  mvn clean package
  ```
- Deploy file WAR `jsp-servlet-1.0.war` lên Tomcat.

---

## 🤖 8. QUY TẮC DÀNH RIÊNG CHO AI (CLAUDE / GEMINI) KHĨ PHÁT TRIỂN TIẾP

1. **Giữ Nguyên Cấu Trúc Architecture:** Không tự ý chuyển từ Servlet/JSP sang Spring Boot trừ khi người dùng yêu cầu rõ ràng.
2. **Xử lý DB Connection:** Khi sửa mã nguồn tầng DAO, **luôn đảm bảo đóng Connection / PreparedStatement / ResultSet** trong khối `try-with-resources` hoặc `finally` để tránh cạn kiệt C3P0 connection pool.
3. **Khi sửa logic Đăng nhập/Đăng ký:** Lưu ý kiểm tra cả trường `user_email` và `username` để đồng bộ đúng giao diện JSP.
4. **Khi sửa logic Đơn hàng & Kho:** Luôn thực thi trong **Transaction (`conn.setAutoCommit(false)`)** khi tác động đồng thời đến `Order`, `OrderDetail`, `Product.product_quantity` và `cart`.
5. **JSON Processing:** Ưu tiên dùng thư viện Jackson (`ObjectMapper`) cho bất kỳ thao tác xử lý JSON nào thay vì dùng thao tác cắt chuỗi Regex/String manipulation.

---
*README này tự động phản ánh trạng thái chính xác nhất của dự án.* 🚀
