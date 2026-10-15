package util;

/**
 * Hằng số trạng thái đơn hàng và thanh toán dùng chung toàn hệ thống.
 * Không rải chuỗi rời rạc trong code — luôn tham chiếu qua class này.
 */
public final class OrderStatus {

    private OrderStatus() {}

    // ── Order status (orderStatus) ──────────────────────────────────────────
    public static final String ORDER_PENDING    = "Chờ xử lý";
    public static final String ORDER_CONFIRMED  = "Đã xác nhận";
    public static final String ORDER_SUCCESS    = "Thành công";
    public static final String ORDER_CANCELLED  = "Đã hủy";   // Giá trị chuẩn trong dự án

    // ── Payment status (paymentStatus) ──────────────────────────────────────
    /** COD — chưa thu tiền */
    public static final String PAY_UNPAID            = "Chưa thanh toán";
    /** Đang chờ khách chuyển khoản (SePay) hoặc redirect cổng (cũ) */
    public static final String PAY_WAITING_ONLINE    = "Chờ thanh toán online";
    /** SePay webhook xác nhận đã nhận đủ tiền */
    public static final String PAY_PAID_SEPAY        = "Đã thanh toán (SePay)";
    /** Đơn hết hạn, chưa nhận được tiền */
    public static final String PAY_EXPIRED_SEPAY     = "Hết hạn (SePay)";
    /** Tiền về SAU khi đơn đã hết hạn — cần admin xử lý thủ công */
    public static final String PAY_LATE_SEPAY        = "Đã thanh toán sau hạn - cần kiểm tra";

    // ── Payment method ───────────────────────────────────────────────────────
    public static final String METHOD_COD   = "COD";
    public static final String METHOD_SEPAY = "SEPAY";
}
