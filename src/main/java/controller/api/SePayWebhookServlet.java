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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    /** Regex khớp cụm DH<số> với word-boundary để DH1 không khớp DH12 */
    private static final Pattern PAYMENT_CODE_PATTERN =
            Pattern.compile("(?<![\\w\\d])(DH\\d+)(?![\\d])", Pattern.CASE_INSENSITIVE);

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
        if (!constantTimeEquals(authHeader, expectedHeader)) {
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
        String paymentCode = resolvePaymentCode(code, content);
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
            orderDAO.insertPaymentTransaction(orderId, sepayTxId, transferAmt, content, rawBody);
            writeSuccess(response);
            return;
        }

        // ── 7. Chống trùng + update trong cùng 1 DB transaction ───────────
        //    insertPaymentTransaction trả false nếu sepay_transaction_id đã tồn tại (UNIQUE)
        boolean inserted = orderDAO.insertPaymentTransaction(orderId, sepayTxId, transferAmt, content, rawBody);
        if (!inserted) {
            log("[SePay] Giao dịch trùng txId=" + sepayTxId + " cho đơn " + orderId + ". Bỏ qua.");
            writeSuccess(response);
            return;
        }

        // ── 8. Cập nhật trạng thái đơn ────────────────────────────────────
        Timestamp paidAt = new Timestamp(System.currentTimeMillis());
        String currentStatus = order.getPaymentStatus();

        if (OrderStatus.PAY_EXPIRED_SEPAY.equals(currentStatus)) {
            // Tiền về SAU khi đơn đã hết hạn → đánh dấu đặc biệt, không trừ kho
            boolean updated = orderDAO.markLatePayment(orderId, OrderStatus.PAY_LATE_SEPAY);
            log("[SePay] Đơn " + orderId + " đã hết hạn nhưng tiền về. markLate=" + updated);

        } else {
            // Điều kiện: WHERE payment_status = 'Chờ thanh toán online'
            int rows = orderDAO.markPaidConditional(orderId, OrderStatus.PAY_PAID_SEPAY, paidAt);
            if (rows == 1) {
                log("[SePay] Đơn " + orderId + " đã thanh toán thành công.");

                // ── 9. Ghi PURCHASE behavior cho từng sản phẩm ────────────
                Integer userId = order.getUserId();
                if (userId != null && userId > 0) {
                    // Reload order detail để có danh sách sản phẩm
                    OrderObject fullOrder = orderDAO.getOrderDetailByOrderId(orderId);
                    List<OrderDetailObject> details = fullOrder != null
                            ? fullOrder.getOrderDetailList() : new ArrayList<>();
                    for (OrderDetailObject d : details) {
                        if (d != null && d.getProductObject() != null) {
                            BehaviorTracker.trackPurchase(userId, null,
                                    d.getProductObject().getProductId());
                        }
                    }
                }
            } else {
                // rows == 0: đơn đã được xử lý bởi luồng khác (expiration listener hoặc webhook trùng race-condition)
                log("[SePay] Đơn " + orderId
                        + " markPaidConditional=0 (đã xử lý trước đó). txId=" + sepayTxId);
            }
        }

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
    private String resolvePaymentCode(String code, String content) {
        // Ưu tiên trường code
        if (code != null && !code.trim().isEmpty()) {
            return code.trim().toUpperCase();
        }

        // Tìm trong content
        if (content == null || content.trim().isEmpty()) {
            return null;
        }

        Matcher m = PAYMENT_CODE_PATTERN.matcher(content);
        List<String> found = new ArrayList<>();
        while (m.find()) {
            String candidate = m.group(1).toUpperCase();
            if (!found.contains(candidate)) {
                found.add(candidate);
            }
        }

        if (found.size() == 1) {
            return found.get(0);
        } else if (found.size() > 1) {
            log("[SePay] Nhiều mã đơn trong content: " + found + " — content='" + content + "'. Không xử lý.");
            return null;
        }
        return null;
    }

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
