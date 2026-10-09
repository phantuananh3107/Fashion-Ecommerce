package controller.web;

import dao.OrderDAO;
import dao.Impl.OrderDAOImpl;
import dao.VoucherDAO;
import dao.Impl.VoucherDAOImpl;
import model.CartObject;
import model.OrderObject;
import model.ProductObject;
import model.UserObject;
import model.VoucherObject;
import util.Config;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/customer/process-checkout")
public class ProcessCheckoutController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();
        UserObject user = (UserObject) session.getAttribute("user");
        List<CartObject> cartList = (List<CartObject>) session.getAttribute("cart");

        if (user == null || cartList == null || cartList.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/customer/cart");
            return;
        }

        String fullname = request.getParameter("fullname");
        String phone = request.getParameter("phone");
        String province = request.getParameter("province");
        String district = request.getParameter("district");
        String ward = request.getParameter("ward");
        String address = request.getParameter("address");
        String note = request.getParameter("note");
        String paymentMethod = request.getParameter("paymentMethod");
        // Lấy thêm mã voucher từ giao diện
        String voucherCode = request.getParameter("voucher_code");

        String fullAddress = address + ", " + ward + ", " + district + ", " + province;

        // ==========================================
        // ĐÃ SỬA LỖI: TÍNH TỔNG TIỀN THEO GIÁ ĐÃ GIẢM
        // ==========================================
        double totalAmount = 0;
        for (CartObject item : cartList) {
            ProductObject p = item.getProductObject();
            if(p != null) {
                // Tính giá sau khi áp dụng discountPercent của sản phẩm
                double finalPrice = p.getProductPrice() * (100 - p.getDiscountPercent()) / 100.0;
                totalAmount += finalPrice * item.getQuantity();
            }
        }

        // ==========================================
        // XỬ LÝ LOGIC VOUCHER TRƯỚC KHI LƯU DB
        // ==========================================
        double discountAmount = 0;
        Integer appliedVoucherId = null;

        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            VoucherDAO voucherDAO = new VoucherDAOImpl();
            VoucherObject v = voucherDAO.getVoucherByCode(voucherCode.trim());

            if (v != null) {
                discountAmount = v.getDiscountAmount();
                appliedVoucherId = v.getVoucherId();
                // Trừ tiền giảm giá vào tổng bill
                totalAmount -= discountAmount;
                if (totalAmount < 0) totalAmount = 0; // Tránh tình trạng âm tiền
            }
        }

        OrderObject order = new OrderObject();
        order.setUserId(user.getUserId());
        order.setTotalAmount(totalAmount);
        // Lưu thông tin voucher vào hóa đơn
        order.setDiscountAmount(discountAmount);
        order.setVoucherId(appliedVoucherId);

        order.setOrderStatus("Chờ xử lý");

        if ("COD".equals(paymentMethod)) {
            order.setPaymentStatus("Chưa thanh toán");
        } else {
            order.setPaymentStatus("Chờ thanh toán online");
        }

        order.setPaymentMethod(paymentMethod);
        order.setOrderNote(note);
        order.setShippingName(fullname);
        order.setShippingPhone(phone);
        order.setShippingAddress(fullAddress);

        // Gọi DAO lưu vào Database và lấy orderId
        OrderDAO orderDAO = new OrderDAOImpl();
        int orderId = orderDAO.insertOrder(order, cartList);

        if (orderId > 0) {
            // Track PURCHASE behavior for each product in checkout order
            String sessionId = session.getId();
            Integer userId = (user != null) ? user.getUserId() : null;
            if (cartList != null) {
                for (CartObject item : cartList) {
                    if (item != null && item.getProductObject() != null) {
                        service.BehaviorTracker.trackPurchase(userId, sessionId, item.getProductObject().getProductId());
                    }
                }
            }

            // ==========================================
            // CẬP NHẬT LẠI SỐ LƯỢNG VOUCHER
            // ==========================================
            if (appliedVoucherId != null) {
                VoucherDAO voucherDAO = new VoucherDAOImpl();
                voucherDAO.decreaseVoucherQuantity(appliedVoucherId);
            }

            if ("COD".equals(paymentMethod)) {
                // 1. Xóa giỏ hàng trong Session (RAM)
                session.removeAttribute("cart");

                // 2. Xóa giỏ hàng trong Database (Ổ cứng)
                dao.CartDAO cartDAO = new dao.Impl.CartDAOImpl();
                cartDAO.clearCartByUserId(user.getUserId());
                // Đá về trang success kèm theo mã đơn hàng
                response.sendRedirect(request.getContextPath() + "/views/web/checkout-success.jsp?orderId=" + orderId);
            }
            else if ("VNPAY".equals(paymentMethod)) {
                // ==========================================
                // THUẬT TOÁN TẠO LINK THANH TOÁN VNPAY
                // ==========================================
                String vnp_Version = "2.1.0";
                String vnp_Command = "pay";
                String orderType = "other";

                // VNPAY yêu cầu số tiền phải nhân với 100. Lúc này totalAmount đã được tính đúng ở trên.
                long amount = (long) (totalAmount * 100);

                String vnp_TxnRef = String.valueOf(orderId); // Dùng ID đơn hàng làm mã giao dịch
                String vnp_IpAddr = Config.getIpAddress(request);
                String vnp_TmnCode = Config.vnp_TmnCode;

                Map<String, String> vnp_Params = new HashMap<>();
                vnp_Params.put("vnp_Version", vnp_Version);
                vnp_Params.put("vnp_Command", vnp_Command);
                vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
                vnp_Params.put("vnp_Amount", String.valueOf(amount));
                vnp_Params.put("vnp_CurrCode", "VND");
                vnp_Params.put("vnp_BankCode", "");
                vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
                vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + vnp_TxnRef);
                vnp_Params.put("vnp_OrderType", orderType);
                vnp_Params.put("vnp_Locale", "vn");
                vnp_Params.put("vnp_ReturnUrl", Config.vnp_ReturnUrl);
                vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

                Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
                SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
                String vnp_CreateDate = formatter.format(cld.getTime());
                vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

                cld.add(Calendar.MINUTE, 15);
                String vnp_ExpireDate = formatter.format(cld.getTime());
                vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

                List fieldNames = new ArrayList(vnp_Params.keySet());
                Collections.sort(fieldNames);
                StringBuilder hashData = new StringBuilder();
                StringBuilder query = new StringBuilder();
                Iterator itr = fieldNames.iterator();
                while (itr.hasNext()) {
                    String fieldName = (String) itr.next();
                    String fieldValue = (String) vnp_Params.get(fieldName);
                    if ((fieldValue != null) && (fieldValue.length() > 0)) {
                        //Build hash data
                        hashData.append(fieldName);
                        hashData.append('=');
                        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                        //Build query
                        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                        query.append('=');
                        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                        if (itr.hasNext()) {
                            query.append('&');
                            hashData.append('&');
                        }
                    }
                }
                String queryUrl = query.toString();
                String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());
                queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
                String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;

                // Đá văng khách sang trang VNPAY
                response.sendRedirect(paymentUrl);
            }
            else {
                response.getWriter().write("Chức năng MoMo đang được tích hợp!");
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/customer/checkout?error=1");
        }
    }
}