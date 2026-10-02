package controller.web;

import dao.OrderDAO;
import dao.Impl.OrderDAOImpl;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

// Đăng ký đường link để hứng khách từ VNPAY trả về
@WebServlet("/customer/vnpay-return")
public class VnPayReturnController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Lấy toàn bộ tham số VNPAY trả về trên URL
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> params = request.getParameterNames(); params.hasMoreElements(); ) {
            String fieldName = URLEncoder.encode(params.nextElement(), StandardCharsets.US_ASCII.toString());
            String fieldValue = URLEncoder.encode(request.getParameter(fieldName), StandardCharsets.US_ASCII.toString());
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnp_SecureHash = request.getParameter("vnp_SecureHash");
        if (fields.containsKey("vnp_SecureHashType")) {
            fields.remove("vnp_SecureHashType");
        }
        if (fields.containsKey("vnp_SecureHash")) {
            fields.remove("vnp_SecureHash");
        }

        // 2. Tính toán lại chữ ký để đảm bảo hacker không tự ý đổi URL
        String signValue = Config.hmacSHA512(Config.secretKey, hashAllFields(fields));

        // 3. Xử lý kết quả
        if (signValue.equals(vnp_SecureHash)) {
            // Chữ ký hợp lệ
            String responseCode = request.getParameter("vnp_ResponseCode");
            String txnRef = request.getParameter("vnp_TxnRef"); // Đây chính là orderId

            OrderDAO orderDAO = new OrderDAOImpl();
            HttpSession session = request.getSession();

            if ("00".equals(responseCode)) {
                // THANH TOÁN THÀNH CÔNG!
                int orderId = Integer.parseInt(txnRef);
                int userId = ((model.UserObject) session.getAttribute("user")).getUserId();

                // Cập nhật Database: Đổi trạng thái sang "Đã thanh toán online"
                orderDAO.updateOrder(orderId, userId, "Đã xác nhận", "Đã thanh toán (VNPAY)");

                // Xóa giỏ hàng
                session.removeAttribute("cart");

                //  Xóa giỏ hàng trong Database - BỔ SUNG ĐOẠN NÀY
                dao.CartDAO cartDAO = new dao.Impl.CartDAOImpl();
                cartDAO.clearCartByUserId(userId);

                // Đá về trang success kèm theo mã đơn hàng
                response.sendRedirect(request.getContextPath() + "/views/web/checkout-success.jsp?orderId=" + orderId);
            } else {
                // THANH TOÁN THẤT BẠI (Hoặc khách bấm Hủy)
                response.sendRedirect(request.getContextPath() + "/customer/checkout?error=vnpay_failed");
            }
        } else {
            // Chữ ký KHÔNG hợp lệ (Có dấu hiệu giả mạo)
            response.sendRedirect(request.getContextPath() + "/customer/checkout?error=invalid_signature");
        }
    }

    // Hàm phụ trợ dùng để ghép chuỗi tạo chữ ký băm
    private String hashAllFields(Map<String, String> fields) {
        java.util.List<String> fieldNames = new java.util.ArrayList<>(fields.keySet());
        java.util.Collections.sort(fieldNames);
        StringBuilder sb = new StringBuilder();
        java.util.Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                sb.append(fieldName);
                sb.append("=");
                sb.append(fieldValue);
            }
            if (itr.hasNext()) {
                sb.append("&");
            }
        }
        return sb.toString();
    }
}