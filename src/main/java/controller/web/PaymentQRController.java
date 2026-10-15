package controller.web;

import dao.OrderDAO;
import dao.Impl.OrderDAOImpl;
import model.OrderObject;
import model.UserObject;
import util.OrderStatus;
import util.SepayConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

/**
 * GET  /customer/payment?orderId=...  → Hiển thị trang QR thanh toán
 * POST /customer/payment?action=cancel&orderId=... → Hủy thanh toán
 */
@WebServlet("/customer/payment")
public class PaymentQRController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        UserObject user = (session != null) ? (UserObject) session.getAttribute("user") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/customer/login");
            return;
        }

        String orderIdParam = request.getParameter("orderId");
        if (orderIdParam == null || orderIdParam.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/customer/home");
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(orderIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/customer/home");
            return;
        }

        OrderDAO orderDAO = new OrderDAOImpl();
        // Kiểm tra chủ sở hữu: chỉ chủ đơn mới xem được
        OrderObject order = orderDAO.findByOrderIdAndUserId(orderId, user.getUserId());
        if (order == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền xem đơn hàng này.");
            return;
        }

        // Ghép URL QR — chỉ ghép URL, KHÔNG gọi thử dịch vụ ngoài
        String paymentCode = order.getPaymentCode();
        if (paymentCode == null) paymentCode = "DH" + orderId;

        String qrUrl = "https://qr.sepay.vn/img"
                + "?acc=" + URLEncoder.encode(SepayConfig.ACCOUNT_NUMBER, "UTF-8")
                + "&bank=" + URLEncoder.encode(SepayConfig.BANK_CODE, "UTF-8")
                + "&amount=" + (long) order.getTotalAmount()
                + "&des=" + URLEncoder.encode(paymentCode, "UTF-8");

        request.setAttribute("order", order);
        request.setAttribute("qrUrl", qrUrl);
        request.setAttribute("accountNumber", SepayConfig.ACCOUNT_NUMBER);
        request.setAttribute("accountName", SepayConfig.ACCOUNT_NAME);
        request.setAttribute("bankCode", SepayConfig.BANK_CODE);
        request.setAttribute("paymentCode", paymentCode);

        request.getRequestDispatcher("/views/web/payment-qr.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        UserObject user = (session != null) ? (UserObject) session.getAttribute("user") : null;
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/customer/login");
            return;
        }

        String action        = request.getParameter("action");
        String orderIdParam  = request.getParameter("orderId");
        if (!"cancel".equals(action) || orderIdParam == null) {
            response.sendRedirect(request.getContextPath() + "/customer/home");
            return;
        }

        int orderId;
        try {
            orderId = Integer.parseInt(orderIdParam);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/customer/home");
            return;
        }

        OrderDAO orderDAO = new OrderDAOImpl();
        // Xác nhận chủ sở hữu trước khi hủy
        OrderObject order = orderDAO.findByOrderIdAndUserId(orderId, user.getUserId());
        if (order == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Hủy + hoàn kho với UPDATE có điều kiện (Điều 3 & 6)
        int rows = orderDAO.cancelAndRestoreStock(
                orderId, OrderStatus.PAY_EXPIRED_SEPAY, OrderStatus.ORDER_CANCELLED);

        if (rows == 1) {
            response.sendRedirect(request.getContextPath() + "/customer/order-history?cancelled=1");
        } else {
            // Đơn đã thay đổi trạng thái (đã thanh toán hoặc đã hủy từ nơi khác)
            response.sendRedirect(request.getContextPath() + "/customer/payment?orderId=" + orderId + "&cancelFailed=1");
        }
    }
}
