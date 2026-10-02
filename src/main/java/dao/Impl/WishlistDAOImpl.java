package dao.Impl;

import dao.WishlistDAO;
import model.ProductObject;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAOImpl implements WishlistDAO {

    @Override
    public boolean toggleWishlist(int userId, String productCode) {
        String checkSql = "SELECT wishlist_id FROM wishlist WHERE user_id = ? AND product_code = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement checkPs = conn.prepareStatement(checkSql)) {

            checkPs.setInt(1, userId);
            checkPs.setString(2, productCode);
            ResultSet rs = checkPs.executeQuery();

            if (rs.next()) {
                // Đã có -> Xóa tim
                String deleteSql = "DELETE FROM wishlist WHERE user_id = ? AND product_code = ?";
                try (PreparedStatement delPs = conn.prepareStatement(deleteSql)) {
                    delPs.setInt(1, userId);
                    delPs.setString(2, productCode);
                    delPs.executeUpdate();
                }
                return false;
            } else {
                // Chưa có -> Thêm tim
                String insertSql = "INSERT INTO wishlist (user_id, product_code) VALUES (?, ?)";
                try (PreparedStatement insPs = conn.prepareStatement(insertSql)) {
                    insPs.setInt(1, userId);
                    insPs.setString(2, productCode);
                    insPs.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi toggleWishlist: " + e.getMessage());
        }
        return false;
    }

    @Override
    public List<ProductObject> getWishlistByUserId(int userId) {
        List<ProductObject> list = new ArrayList<>();
        // ĐÃ SỬA: Lấy thêm p.id để làm link chi tiết
        String sql = "SELECT p.* FROM Product p INNER JOIN wishlist w ON p.product_code = w.product_code WHERE w.user_id = ? AND p.isDeleted = FALSE ORDER BY w.created_date DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProductObject p = new ProductObject();
                // Map ID kiểu số để truyền vào link ?id=...
                p.setProductId(rs.getInt("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));
                p.setProductPrice(rs.getDouble("product_price"));
                // Lấy ảnh đầu tiên (product_image1) để hiển thị ở wishlist
                p.setProductImage(rs.getString("product_image1"));
                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getWishlistByUserId: " + e.getMessage());
        }
        return list;
    }
}