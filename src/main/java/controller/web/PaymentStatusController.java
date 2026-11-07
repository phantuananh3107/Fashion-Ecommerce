package controller.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.OrderDAO;
import dao.Impl.OrderDAOImpl;
import model.OrderObject;
import model.UserObject;
import util.OrderStatus;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * GET /customer/payment/status?orderId=...
 * Trả JSON: { "status": "...", "paidAt": "..." }
 * Chỉ chủ đơn gọi được.
 */
@WebServlet("/customer/payment/status")
public class PaymentStatusController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        UserObject user = (session != null) ? (UserObject) session.getAttribute("user") : null;
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"unauthorized\"}");
            return;
        }

        String orderIdParam = request.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"missing orderId\"}");
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(orderIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"invalid orderId\"}");
            return;
        }

        OrderDAO orderDAO = new OrderDAOImpl();
        // Kiểm tra chủ sở hữu
        OrderObject order = orderDAO.findByOrderIdAndUserId(orderId, user.getUserId());
        if (order == null) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\":\"forbidden\"}");
            return;
        }

        String paymentStatus = order.getPaymentStatus();

        // Endpoint poll tự phát hiện hết hạn nếu listener chưa kịp chạy
        if (OrderStatus.PAY_WAITING_ONLINE.equals(paymentStatus)
                && order.getExpiresAt() != null
                && order.getExpiresAt().getTime() < System.currentTimeMillis()) {
            paymentStatus = OrderStatus.PAY_EXPIRED_SEPAY;
        }

        Map<String, String> result = new HashMap<>();
        result.put("status", paymentStatus);
        result.put("paidAt", order.getPaidAt() != null
                ? SDF.format(order.getPaidAt()) : null);

        response.getWriter().write(MAPPER.writeValueAsString(result));
    }
}
