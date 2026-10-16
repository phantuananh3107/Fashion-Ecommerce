package controller.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dao.OrderDAO;
import dao.Impl.OrderDAOImpl;
import model.OrderDetailObject;
import model.OrderObject;
import service.BehaviorTracker;
import util.OrderStatus;
import util.SepayConfig;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * POST /api/sepay-webhook
 * Nhận webhook từ SePay, xác minh Apikey, xử lý thanh toán.
 *
 * Payload SePay (các trường cần dùng):
 *   id              – ID giao dịch SePay (dùng chống trùng)
 *   transferType    – "in" | "out"  (chỉ xử lý "in")
 *   transferAmount  – số tiền nhận
 *   content         – nội dung chuyển khoản
 *   code            – mã đơn nếu SePay parse được (có thể null)
 *   subAccount      – tài khoản ảo (VA)
 */
@WebServlet("/api/sepay-webhook")
public class SePayWebhookServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    // ───────────────────────────────────────────────────────────────────────
    //  POST handler
    // ───────────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        // ── 1. Xác thực Apikey bằng constant-time comparison ──────────────
        String authHeader = request.getHeader("Authorization");
        String expectedHeader = "Apikey " + SepayConfig.API_KEY;
        if (!SepayConfig.isWebhookConfigured()
                || !constantTimeEquals(authHeader, expectedHeader)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, false, "unauthorized");
            return;
        }

        // ── 2. Đọc body JSON ───────────────────────────────────────────────
        String rawBody;
        try {
            rawBody = new String(request.getInputStream().readAllBytes(), "UTF-8");
        } catch (IOException e) {
            log("[SePay] Lỗi đọc body: " + e.getMessage());
            writeSuccess(response);
            return;
        }

        JsonNode payload;
        try {
            payload = MAPPER.readTree(rawBody);
        } catch (Exception e) {
            log("[SePay] Body không phải JSON hợp lệ: " + rawBody);
            writeSuccess(response);
            return;
        }

        String sepayTxId    = textOrNull(payload, "id");
        String transferType = textOrNull(payload, "transferType");
        double transferAmt  = doubleOrZero(payload, "transferAmount");
        String content      = textOrNull(payload, "content");
        String code         = textOrNull(payload, "code");

        log("[SePay] Webhook nhận: txId=" + sepayTxId
                + " type=" + transferType
                + " amount=" + transferAmt
                + " code=" + code
                + " content=" + content);

        // ── 3. Chỉ xử lý giao dịch "in" ───────────────────────────────────
        if (!"in".equalsIgnoreCase(transferType)) {
            log("[SePay] Bỏ qua transferType=" + transferType);
            writeSuccess(response);
            return;
        }

        if (sepayTxId == null || sepayTxId.isEmpty()) {
            log("[SePay] Thiếu id giao dịch, bỏ qua.");
            writeSuccess(response);
            return;
        }

        // ── 4. Xác định mã đơn hàng ────────────────────────────────────────
        String paymentCode = PaymentCodeResolver.resolve(code, content);
        if (paymentCode == null) {
            log("[SePay] Không xác định được mã đơn từ code='" + code
                    + "' content='" + content + "'. Ghi log, bỏ qua.");
            writeSuccess(response);
            return;
        }

        // ── 5. Tìm đơn hàng ────────────────────────────────────────────────
        OrderDAO orderDAO = new OrderDAOImpl();
        OrderObject order = orderDAO.findByPaymentCode(paymentCode);
        if (order == null) {
            log("[SePay] Không tìm thấy đơn với paymentCode=" + paymentCode);
            writeSuccess(response);
            return;
        }

        int orderId = order.getOrderId();

        // ── 6. Kiểm tra số tiền ────────────────────────────────────────────
        if (transferAmt < order.getTotalAmount()) {
            log("[SePay] Thiếu tiền cho đơn " + orderId
                    + ": nhận=" + transferAmt + " cần=" + order.getTotalAmount());
            // Ghi giao dịch nhưng KHÔNG đổi trạng thái
            try {
                orderDAO.insertPaymentTransaction(orderId, sepayTxId, transferAmt, content, rawBody);
            } catch (Exception e) {
                log("[SePay] Lỗi ghi giao dịch thiếu tiền: " + e.getMessage(), e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeJson(response, false, "internal_error");
                return;
            }
            writeSuccess(response);
            return;
        }

        OrderObject fullOrder = orderDAO.getOrderDetailByOrderId(orderId);
        List<Integer> purchaseProductIds = new ArrayList<>();
        if (fullOrder != null && fullOrder.getOrderDetailList() != null) {
            for (OrderDetailObject detail : fullOrder.getOrderDetailList()) {
                if (detail != null && detail.getProductObject() != null) {
                    purchaseProductIds.add(detail.getProductObject().getProductId());
                }
            }
        }

        OrderDAO.PaymentTransactionResult result;
        try {
            result = orderDAO.processPaymentTransaction(order, sepayTxId, transferAmt,
                    content, rawBody, new Timestamp(System.currentTimeMillis()),
                    OrderStatus.PAY_PAID_SEPAY, OrderStatus.PAY_LATE_SEPAY);
        } catch (Exception e) {
            log("[SePay] Lỗi hệ thống khi xử lý txId=" + sepayTxId + ": " + e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, false, "internal_error");
            return;
        }

        if (result == OrderDAO.PaymentTransactionResult.DUPLICATE) {
            log("[SePay] Giao dịch trùng txId=" + sepayTxId + " cho đơn " + orderId + ". Bỏ qua.");
            writeSuccess(response);
            return;
        }
        if (result == OrderDAO.PaymentTransactionResult.PROCESSED_WITH_PURCHASE_LOG) {
            try {
                for (Integer productId : purchaseProductIds) {
                    BehaviorTracker.trackPurchase(order.getUserId(), null, productId);
                }
            } catch (Exception e) {
                log("[SePay] Lỗi ghi PURCHASE log cho đơn " + orderId + ": " + e.getMessage(), e);
            }
        }
        log("[SePay] Đã ghi nhận thanh toán cho đơn " + orderId + ".");
        writeSuccess(response);
    }

    // ───────────────────────────────────────────────────────────────────────
    //  Helpers
    // ───────────────────────────────────────────────────────────────────────

    /**
     * Xác định paymentCode theo ưu tiên:
     *  1. Trường `code` (so sánh bất kể hoa thường, trả về dạng UPPER)
     *  2. Tìm trong `content` regex DH<số> với word-boundary
     *  Nếu content cho nhiều kết quả → log + trả null
     */
    /**
     * So sánh constant-time để chống timing attack.
     */
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] ba = a.getBytes();
        byte[] bb = b.getBytes();
        if (ba.length != bb.length) {
            // vẫn duyệt đủ để tránh timing leak
            int dummy = 0;
            for (byte x : ba) dummy ^= x;
            return false;
        }
        int diff = 0;
        for (int i = 0; i < ba.length; i++) {
            diff |= ba[i] ^ bb[i];
        }
        return diff == 0;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull()) return null;
        String v = n.asText("").trim();
        return v.isEmpty() ? null : v;
    }

    private double doubleOrZero(JsonNode node, String field) {
        JsonNode n = node.get(field);
        if (n == null || n.isNull()) return 0.0;
        return n.asDouble(0.0);
    }

    private void writeSuccess(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        writeJson(response, true, null);
    }

    private void writeJson(HttpServletResponse response, boolean success, String error) throws IOException {
        PrintWriter pw = response.getWriter();
        if (error != null) {
            pw.write("{\"success\":" + success + ",\"error\":\"" + error + "\"}");
        } else {
            pw.write("{\"success\":" + success + "}");
        }
        pw.flush();
    }
}
