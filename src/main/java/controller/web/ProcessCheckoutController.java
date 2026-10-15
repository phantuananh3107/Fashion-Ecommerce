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
import util.OrderStatus;
import util.SepayConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

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

        String fullname      = request.getParameter("fullname");
        String phone         = request.getParameter("phone");
        String province      = request.getParameter("province");
        String district      = request.getParameter("district");
        String ward          = request.getParameter("ward");
        String address       = request.getParameter("address");
        String note          = request.getParameter("note");
        String paymentMethod = request.getParameter("paymentMethod");
        String voucherCode   = request.getParameter("voucher_code");

        // ── CHỈ CHẤP NHẬN COD VÀ SEPAY ─────────────────────────────────────
        if (!OrderStatus.METHOD_COD.equals(paymentMethod)
                && !OrderStatus.METHOD_SEPAY.equals(paymentMethod)) {
            response.sendRedirect(request.getContextPath() + "/customer/checkout?error=invalid_payment_method");
            return;
        }

        String fullAddress = address + ", " + ward + ", " + district + ", " + province;

        // ── TÍNH TỔNG TIỀN THEO GIÁ ĐÃ GIẢM ────────────────────────────────
        double totalAmount = 0;
        for (CartObject item : cartList) {
            ProductObject p = item.getProductObject();
            if (p != null) {
                double finalPrice = p.getProductPrice() * (100 - p.getDiscountPercent()) / 100.0;
                totalAmount += finalPrice * item.getQuantity();
            }
        }

        // ── VOUCHER ──────────────────────────────────────────────────────────
        double discountAmount   = 0;
        Integer appliedVoucherId = null;

        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            VoucherDAO voucherDAO = new VoucherDAOImpl();
            VoucherObject v = voucherDAO.getVoucherByCode(voucherCode.trim());
            if (v != null) {
                discountAmount    = v.getDiscountAmount();
                appliedVoucherId  = v.getVoucherId();
                totalAmount      -= discountAmount;
                if (totalAmount < 0) totalAmount = 0;
            }
        }

        OrderObject order = new OrderObject();
        order.setUserId(user.getUserId());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setVoucherId(appliedVoucherId);
        order.setOrderStatus(OrderStatus.ORDER_PENDING);
        order.setPaymentMethod(paymentMethod);
        order.setOrderNote(note);
        order.setShippingName(fullname);
        order.setShippingPhone(phone);
        order.setShippingAddress(fullAddress);

        if (OrderStatus.METHOD_COD.equals(paymentMethod)) {
            order.setPaymentStatus(OrderStatus.PAY_UNPAID);
        } else {
            order.setPaymentStatus(OrderStatus.PAY_WAITING_ONLINE);
        }

        OrderDAO orderDAO = new OrderDAOImpl();
        int orderId = orderDAO.insertOrder(order, cartList);

        if (orderId > 0) {
            // Giảm số lượng voucher
            if (appliedVoucherId != null) {
                VoucherDAO voucherDAO = new VoucherDAOImpl();
                voucherDAO.decreaseVoucherQuantity(appliedVoucherId);
            }

            // ── XÓA GIỎ HÀNG CHO CẢ COD VÀ SEPAY (Điều 6) ──────────────────
            session.removeAttribute("cart");
            dao.CartDAO cartDAO = new dao.Impl.CartDAOImpl();
            cartDAO.clearCartByUserId(user.getUserId());

            if (OrderStatus.METHOD_COD.equals(paymentMethod)) {
                // COD: ghi PURCHASE cho hệ gợi ý
                String sessionId = session.getId();
                for (CartObject item : cartList) {
                    if (item != null && item.getProductObject() != null) {
                        service.BehaviorTracker.trackPurchase(
                                user.getUserId(), sessionId, item.getProductObject().getProductId());
                    }
                }
                response.sendRedirect(request.getContextPath() + "/views/web/checkout-success.jsp?orderId=" + orderId);

            } else {
                // SEPAY: sinh payment_code = "DH" + orderId, set expires_at
                // KHÔNG ghi PURCHASE ở đây — chờ webhook xác nhận (Điều 7)
                String paymentCode = "DH" + orderId;
                long expireMs = (long) SepayConfig.PAYMENT_EXPIRE_MINUTES * 60 * 1000L;
                Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + expireMs);
                orderDAO.updateSePayFields(orderId, paymentCode, expiresAt);

                response.sendRedirect(request.getContextPath() + "/customer/payment?orderId=" + orderId);
            }
        } else {
            response.sendRedirect(request.getContextPath() + "/customer/checkout?error=1");
        }
    }
}