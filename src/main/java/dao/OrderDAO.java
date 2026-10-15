package dao;

import java.util.List;

import model.CartObject;
import model.OrderInfo;
import model.OrderObject;


public interface OrderDAO {
	
    List<OrderInfo> getRecentOrders(int limit);
    
    // lấy tất cả đơn hàng theo phân trang
    List<OrderObject> getAllOrders(int pageNo, int pageSize, String orderStatus,
                             String paymentStatus, String paymentMethod);

    // Đếm số lượng đơn hàng
    int countAllOrders(String orderStatus, String paymentStatus, String paymentMethod);

    // lấy list đơn hàng theo userId
    List<OrderObject> getOrdersByUserId(int userId);

    // lấy chi tiết đơn hàng theo orderId
    OrderObject getOrderDetailByOrderId(int orderId);

    // Xóa đơn hàng
    boolean deleteOrder(int orderId);

    // Cập nhật trạng thái đơn hàng
    boolean updateOrder(int orderId, int userId, String orderStatus, String paymentStatus);

    // Tạo đơn hàng mới
    int insertOrder(OrderObject order, List<CartObject> cartList);

    // ── Phương thức SePay ────────────────────────────────────────────────────

    /**
     * Cập nhật payment_code và expires_at sau khi đã có orderId.
     * Gọi ngay sau insertOrder trong nhánh SEPAY.
     */
    boolean updateSePayFields(int orderId, String paymentCode, java.sql.Timestamp expiresAt);

    /**
     * Tìm đơn hàng theo paymentCode (case-insensitive).
     * Trả null nếu không tìm thấy.
     */
    OrderObject findByPaymentCode(String paymentCode);

    /**
     * Tìm đơn hàng theo orderId và userId (để kiểm tra chủ sở hữu).
     * Trả null nếu không thuộc user này.
     */
    OrderObject findByOrderIdAndUserId(int orderId, int userId);

    /**
     * Chuyển trạng thái từ "Chờ thanh toán online" → paidStatus.
     * UPDATE có điều kiện WHERE payment_status = 'Chờ thanh toán online'.
     * Trả số dòng bị ảnh hưởng (0 hoặc 1).
     */
    int markPaidConditional(int orderId, String paidStatus, java.sql.Timestamp paidAt);

    /**
     * Cập nhật paymentStatus cho đơn đã hết hạn nhưng tiền vẫn về
     * (không trừ kho, không kiểm tra điều kiện chờ).
     */
    boolean markLatePayment(int orderId, String lateStatus);

    /**
     * Hoàn trả tồn kho và đặt order về trạng thái hủy trong một transaction.
     * UPDATE có điều kiện WHERE payment_status = 'Chờ thanh toán online'.
     * Trả số dòng bị ảnh hưởng.
     */
    int cancelAndRestoreStock(int orderId, String cancelledPayStatus, String cancelledOrderStatus);

    /**
     * Ghi bản ghi vào payment_transaction để chống xử lý trùng.
     * Trả false nếu sepayTransactionId đã tồn tại (duplicate).
     */
    boolean insertPaymentTransaction(int orderId, String sepayTransactionId,
                                     double amount, String content, String rawPayload);

    /**
     * Lấy danh sách orderId các đơn SEPAY hết hạn chưa xử lý.
     * Dùng bởi SePayExpirationListener.
     */
    List<Integer> findExpiredSepayOrderIds();
}