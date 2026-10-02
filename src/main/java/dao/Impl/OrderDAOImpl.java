package dao.Impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import dao.OrderDAO;
import model.*;
import util.DBUtil;


import dao.UserDAO;

import java.util.*;

public class OrderDAOImpl implements OrderDAO {
    @Override
    public List<OrderInfo> getRecentOrders(int limit) {
        List<OrderInfo> orderList = new ArrayList<>();
        String sql = "SELECT u.user_fullname, p.product_name, od.price, o.order_status, o.order_date " +
                "FROM `Order` o " +
                "JOIN `OrderDetail` od ON o.order_id = od.order_id " +
                "JOIN `Product` p ON od.product_id = p.id " +
                "JOIN `users` u ON o.user_id = u.user_id " +
                "ORDER BY o.order_date DESC LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OrderInfo order = new OrderInfo();
                    order.setCustomerName(rs.getString("user_fullname"));
                    order.setProductName(rs.getString("product_name"));
                    order.setPrice(rs.getDouble("price"));
                    order.setOrderStatus(rs.getString("order_status"));
                    order.setOrderDate(rs.getString("order_date"));
                    orderList.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderList;
    }

    // ha
    private UserDAO userDAO = new UserDAOImpl();

    @Override
    public List<OrderObject> getAllOrders(int pageNo, int pageSize, String orderStatus,
                                          String paymentStatus, String paymentMethod) {
        Map<Integer, OrderObject> orderMap = new LinkedHashMap<>();

        StringBuilder sql = new StringBuilder();
        // ĐÃ SỬA: Lấy shipping_name, shipping_phone, shipping_address từ bảng Order
        sql.append("SELECT o.order_id, o.total_amount, o.order_status, o.payment_status, o.payment_method, o.order_date, ");
        sql.append("o.shipping_name, o.shipping_phone, o.shipping_address, ");
        sql.append("od.quantity_sold, od.price, od.product_id, od.product_size, od.product_color, ");
        sql.append("p.product_name, p.product_image ");
        sql.append("FROM `Order` o ");
        sql.append("LEFT JOIN OrderDetail od ON od.order_id = o.order_id ");
        sql.append("LEFT JOIN Product p ON p.id = od.product_id ");

        // Xây dựng điều kiện WHERE động
        List<String> conditions = new ArrayList<>();
        if (orderStatus != null && !orderStatus.isEmpty()) conditions.add("o.order_status = ?");
        if (paymentStatus != null && !paymentStatus.isEmpty()) conditions.add("o.payment_status = ?");
        if (paymentMethod != null && !paymentMethod.isEmpty()) conditions.add("o.payment_method = ?");

        if (!conditions.isEmpty()) {
            sql.append("WHERE ");
            sql.append(String.join(" AND ", conditions));
            sql.append(" ");
        }
        sql.append("ORDER BY o.order_date DESC ");
        sql.append("LIMIT ? OFFSET ?");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int index = 1;
            if (orderStatus != null && !orderStatus.isEmpty()) ps.setString(index++, orderStatus);
            if (paymentStatus != null && !paymentStatus.isEmpty()) ps.setString(index++, paymentStatus);
            if (paymentMethod != null && !paymentMethod.isEmpty()) ps.setString(index++, paymentMethod);

            int offset = (pageNo - 1) * pageSize;
            ps.setInt(index++, pageSize);
            ps.setInt(index, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    OrderObject order = orderMap.get(orderId);
                    if (order == null) {
                        order = new OrderObject();
                        order.setOrderId(rs.getInt("order_id"));
                        order.setOrderDate(rs.getTimestamp("order_date"));
                        order.setTotalAmount(rs.getFloat("total_amount"));
                        order.setOrderStatus(rs.getString("order_status"));
                        order.setPaymentStatus(rs.getString("payment_status"));
                        order.setPaymentMethod(rs.getString("payment_method"));

                        // ĐÃ SỬA: Map thông tin Shipping vào UserObject
                        UserObject user = new UserObject();
                        user.setFullname(rs.getString("shipping_name"));
                        user.setPhoneNumber(rs.getString("shipping_phone"));
                        user.setAddress(rs.getString("shipping_address"));
                        order.setUserObject(user);

                        order.setOrderDetailList(new ArrayList<>());
                        orderMap.put(orderId, order);
                    }

                    if (rs.getInt("product_id") > 0) {
                        ProductObject productObject = new ProductObject();
                        productObject.setProductId(rs.getInt("product_id"));
                        productObject.setProductName(rs.getString("product_name"));
                        productObject.setProductImage(rs.getString("product_image"));

                        OrderDetailObject orderDetailObject = new OrderDetailObject();
                        orderDetailObject.setPrice(rs.getFloat("price"));
                        orderDetailObject.setQuantitySold(rs.getInt("quantity_sold"));
                        orderDetailObject.setProductSize(rs.getString("product_size"));
                        orderDetailObject.setProductColor(rs.getString("product_color"));
                        orderDetailObject.setProductObject(productObject);

                        order.getOrderDetailList().add(orderDetailObject);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(orderMap.values());
    }


    @Override
    public int countAllOrders(String orderStatus, String paymentStatus, String paymentMethod) {
        StringBuilder sql = new StringBuilder();
        sql.append("select count(*) from `Order`");
        List<String> conditions = new ArrayList<>();
        if (orderStatus != null && !orderStatus.isEmpty()) conditions.add("order_status = ?");
        if (paymentStatus != null && !paymentStatus.isEmpty()) conditions.add("payment_status = ?");
        if (paymentMethod != null && !paymentMethod.isEmpty()) conditions.add("payment_method = ?");

        if (!conditions.isEmpty()) {
            sql.append("WHERE ");
            sql.append(String.join(" AND ", conditions));
            sql.append(" ");
        }
        try (
                Connection connection = DBUtil.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql.toString())
        ) {
            int index = 1;
            if (orderStatus != null && !orderStatus.isEmpty()) ps.setString(index++, orderStatus);
            if (paymentStatus != null && !paymentStatus.isEmpty()) ps.setString(index++, paymentStatus);
            if (paymentMethod != null && !paymentMethod.isEmpty()) ps.setString(index, paymentMethod);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<OrderObject> getOrdersByUserId(int userId) {
        // Dùng LinkedHashMap để giữ đúng thứ tự đơn hàng mới nhất lên đầu
        Map<Integer, OrderObject> orderMap = new LinkedHashMap<>();

        String sql = "SELECT o.*, od.quantity_sold, od.price, od.product_size, od.product_color, " +
                "p.product_name, p.product_image " +
                "FROM `Order` o " +
                "LEFT JOIN OrderDetail od ON o.order_id = od.order_id " +
                "LEFT JOIN Product p ON od.product_id = p.id " +
                "WHERE o.user_id = ? " +
                "ORDER BY o.order_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    OrderObject order = orderMap.get(orderId);

                    if (order == null) {
                        order = new OrderObject();
                        order.setOrderId(orderId);
                        order.setOrderDate(rs.getTimestamp("order_date"));
                        order.setTotalAmount(rs.getDouble("total_amount"));
                        order.setOrderStatus(rs.getString("order_status"));
                        order.setPaymentStatus(rs.getString("payment_status"));
                        order.setPaymentMethod(rs.getString("payment_method"));
                        order.setOrderDetailList(new ArrayList<>());
                        orderMap.put(orderId, order);
                    }

                    // Lấy thông tin chi tiết từng sản phẩm
                    if (rs.getInt("quantity_sold") > 0) { // Tránh trường hợp đơn hàng rỗng
                        OrderDetailObject detail = new OrderDetailObject();
                        detail.setQuantitySold(rs.getInt("quantity_sold")); // QUAN TRỌNG: Phải set giá trị này
                        detail.setPrice(rs.getDouble("price"));
                        detail.setProductSize(rs.getString("product_size"));
                        detail.setProductColor(rs.getString("product_color"));

                        ProductObject p = new ProductObject();
                        p.setProductName(rs.getString("product_name"));
                        p.setProductImage(rs.getString("product_image"));

                        detail.setProductObject(p);
                        order.getOrderDetailList().add(detail);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>(orderMap.values());
    }

    @Override
    public OrderObject getOrderDetailByOrderId(int orderId) {
        OrderObject order = new OrderObject();
        StringBuilder sql = new StringBuilder();

        // ĐÃ SỬA: Thêm JOIN với bảng voucher để lấy voucher_code
        sql.append("SELECT o.*, v.voucher_code, ");
        sql.append("od.quantity_sold, od.price, od.product_id, od.product_size, od.product_color, ");
        sql.append("p.product_name, p.product_image ");
        sql.append("FROM `Order` o ");
        sql.append("LEFT JOIN OrderDetail od ON od.order_id = o.order_id ");
        sql.append("LEFT JOIN Product p ON p.id = od.product_id ");
        sql.append("LEFT JOIN voucher v ON o.voucher_id = v.voucher_id "); // Join bảng Voucher
        sql.append("WHERE o.order_id = ?");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (order.getOrderId() == 0) {
                        order.setOrderId(rs.getInt("order_id"));
                        order.setOrderDate(rs.getTimestamp("order_date"));
                        order.setTotalAmount(rs.getDouble("total_amount"));
                        order.setOrderStatus(rs.getString("order_status"));
                        order.setPaymentStatus(rs.getString("payment_status"));
                        order.setPaymentMethod(rs.getString("payment_method"));
                        order.setOrderNote(rs.getString("order_note"));

                        order.setShippingName(rs.getString("shipping_name"));
                        order.setShippingPhone(rs.getString("shipping_phone"));
                        order.setShippingAddress(rs.getString("shipping_address"));

                        // Thông tin Voucher
                        order.setDiscountAmount(rs.getDouble("discount_amount"));
                        order.setVoucherId(rs.getInt("voucher_id"));


                        // ĐÃ SỬA: Đẩy thông tin Shipping vào UserObject để in ra JSP
                        UserObject user = new UserObject();
                        user.setFullname(rs.getString("shipping_name"));
                        user.setPhoneNumber(rs.getString("shipping_phone"));
                        user.setAddress(rs.getString("shipping_address"));
                        order.setUserObject(user);

                        order.setOrderDetailList(new ArrayList<>());
                    }

                    if (rs.getInt("product_id") > 0) {
                        OrderDetailObject detail = new OrderDetailObject();
                        detail.setPrice(rs.getDouble("price"));
                        detail.setQuantitySold(rs.getInt("quantity_sold"));
                        detail.setProductSize(rs.getString("product_size"));
                        detail.setProductColor(rs.getString("product_color"));

                        ProductObject p = new ProductObject();
                        p.setProductName(rs.getString("product_name"));
                        p.setProductImage(rs.getString("product_image"));
                        detail.setProductObject(p);

                        order.getOrderDetailList().add(detail);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return order;
    }

    @Override
    public boolean deleteOrder(int orderId) {

        String deleteOrderItemsSql = "DELETE FROM `OrderDetail` WHERE order_id = ?";
        String deleteOrderSql = "DELETE FROM `Order` WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            // Bắt đầu transaction
            conn.setAutoCommit(false);

            try (
                    PreparedStatement psItems = conn.prepareStatement(deleteOrderItemsSql);
                    PreparedStatement psOrder = conn.prepareStatement(deleteOrderSql)
            ) {
                // Xóa các mục trong đơn hàng
                psItems.setInt(1, orderId);
                psItems.executeUpdate();

                // Xóa đơn hàng
                psOrder.setInt(1, orderId);
                int affectedRows = psOrder.executeUpdate();

                if (affectedRows > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                }
            } catch (SQLException e) {
                conn.rollback(); // rollback nếu có lỗi trong quá trình xóa
                e.printStackTrace();
            } finally {
                conn.setAutoCommit(true); // khôi phục trạng thái ban đầu
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean updateOrder(int orderId, int userId, String orderStatus, String paymentStatus) {
        // ĐÃ SỬA: Xóa AND user_id = ? vì order_id đã là khóa chính duy nhất rồi
        String sql = "UPDATE `Order` SET order_status = ?, payment_status = ? WHERE order_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderStatus);
            ps.setString(2, paymentStatus);
            ps.setInt(3, orderId);
            // ps.setInt(4, userId); // Bỏ dòng này đi
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    // HÀM TẠO ĐƠN HÀNG (Dùng cho cả COD và VNPAY)
    // ==========================================
    // HÀM TẠO ĐƠN HÀNG VÀ TRỪ SỐ LƯỢNG TỒN KHO CÙNG LÚC
    // ==========================================
    @Override
    public int insertOrder(OrderObject order, List<CartObject> cartList) {
        int generatedOrderId = 0;

        // SQL lưu hóa đơn chính
        String sqlOrder = "INSERT INTO `Order` (user_id, total_amount, order_status, payment_status, payment_method, order_note, shipping_name, shipping_phone, shipping_address, shipping_fee, discount_amount, voucher_id, order_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";

        // SQL lưu chi tiết đơn hàng
        String sqlDetail = "INSERT INTO `OrderDetail` (order_id, product_id, product_size, product_color, quantity_sold, price) VALUES (?, ?, ?, ?, ?, ?)";

        // SQL TRỪ TỒN KHO (ĐÃ BỔ SUNG)
        String sqlUpdateStock = "UPDATE `Product` SET product_quantity = product_quantity - ? WHERE id = ? AND product_quantity >= ?";

        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            // Tắt auto-commit để bắt đầu Transaction (Rất quan trọng để tránh lỗi nửa vời)
            conn.setAutoCommit(false);

            try (PreparedStatement psOrder = conn.prepareStatement(sqlOrder, java.sql.Statement.RETURN_GENERATED_KEYS)) {
                if (order.getUserId() != null) {
                    psOrder.setInt(1, order.getUserId());
                } else {
                    psOrder.setNull(1, java.sql.Types.INTEGER);
                }
                psOrder.setDouble(2, order.getTotalAmount());
                psOrder.setString(3, order.getOrderStatus());
                psOrder.setString(4, order.getPaymentStatus());
                psOrder.setString(5, order.getPaymentMethod());
                psOrder.setString(6, order.getOrderNote());
                psOrder.setString(7, order.getShippingName());
                psOrder.setString(8, order.getShippingPhone());
                psOrder.setString(9, order.getShippingAddress());
                psOrder.setDouble(10, 0); // shipping_fee
                psOrder.setDouble(11, order.getDiscountAmount()); // discount_amount

                if (order.getVoucherId() != null) {
                    psOrder.setInt(12, order.getVoucherId()); // voucher_id
                } else {
                    psOrder.setNull(12, java.sql.Types.INTEGER);
                }

                psOrder.executeUpdate();

                try (ResultSet rs = psOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedOrderId = rs.getInt(1);
                    }
                }
            }

            if (generatedOrderId > 0) {
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
                     PreparedStatement psUpdateStock = conn.prepareStatement(sqlUpdateStock)) {

                    for (CartObject item : cartList) {
                        int productId = item.getProductObject().getProductId();
                        int quantitySold = item.getQuantity();

                        // 1. Set tham số thêm vào bảng OrderDetail
                        psDetail.setInt(1, generatedOrderId);
                        psDetail.setInt(2, productId);
                        psDetail.setString(3, item.getProductSize());
                        psDetail.setString(4, ""); // product_color
                        psDetail.setInt(5, quantitySold);
                        psDetail.setDouble(6, item.getProductObject().getProductPrice());
                        psDetail.addBatch();

                        // 2. Set tham số trừ tồn kho trong bảng Product
                        psUpdateStock.setInt(1, quantitySold); // Trừ đi số lượng mua
                        psUpdateStock.setInt(2, productId);    // Của đúng sản phẩm này
                        psUpdateStock.setInt(3, quantitySold); // Check thêm: Đảm bảo kho còn >= số lượng mua mới trừ
                        psUpdateStock.addBatch();
                    }

                    // Thực thi Insert chi tiết đơn
                    psDetail.executeBatch();

                    // Thực thi Update trừ kho
                    int[] stockUpdateResults = psUpdateStock.executeBatch();
                    for (int result : stockUpdateResults) {
                        if (result == 0) {
                            // Nếu result == 0 tức là có sản phẩm không đủ hàng để trừ -> Hủy ngang toàn bộ quá trình
                            conn.rollback();
                            return 0;
                        }
                    }
                }
                // Vượt qua hết mọi cửa ải thì chốt commit lưu vào DB
                conn.commit();
            } else {
                conn.rollback();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return 0;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return generatedOrderId;
    }
}
