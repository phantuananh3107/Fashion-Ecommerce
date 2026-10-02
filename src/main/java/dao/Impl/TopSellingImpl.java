package dao.Impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import dao.ITopSelling;
import model.TopSellingProduct;
import util.DBUtil;

public class TopSellingImpl implements ITopSelling {
	@Override
    public List<TopSellingProduct> getTopSellingProducts(int limit, int offset) {
        List<TopSellingProduct> productList = new ArrayList<>();
        String sql = "SELECT p.id, p.product_name, p.product_image, AVG(od.price) as avg_price, " +
                "SUM(od.quantity_sold) as total_sold, SUM(od.quantity_sold * od.price) as revenue " +
                "FROM Product p " +
                "JOIN OrderDetail od ON p.id = od.product_id " +
                "JOIN `Order` o ON od.order_id = o.order_id " +
                "WHERE o.order_status IN ('Thành công', 'Đã xác nhận') " +
                "GROUP BY p.id, p.product_name, p.product_image " +
                "ORDER BY total_sold DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TopSellingProduct product = new TopSellingProduct();
                    product.setProductId(rs.getInt("id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setProductImage(rs.getString("product_image"));
                    product.setPrice(rs.getDouble("avg_price"));
                    product.setTotalSold(rs.getLong("total_sold"));
                    product.setRevenue(rs.getDouble("revenue"));
                    productList.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    @Override
    public int getTotalProductCount() {
        String sql = "SELECT COUNT(DISTINCT p.id) as total FROM Product p";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    @Override
    public List<TopSellingProduct> searchTopSellingProducts(String keyword, int limit, int offset) {
        List<TopSellingProduct> productList = new ArrayList<>();
        String sql = "SELECT p.id, p.product_name, p.product_image, AVG(od.price) as avg_price, " +
                "SUM(od.quantity_sold) as total_sold, SUM(od.quantity_sold * od.price) as revenue " +
                "FROM Product p " +
                "JOIN OrderDetail od ON p.id = od.product_id " +
                "JOIN `Order` o ON od.order_id = o.order_id " +
                "WHERE o.order_status IN ('Thành công', 'Đã xác nhận') " +
                "GROUP BY p.id, p.product_name, p.product_image " +
                "ORDER BY total_sold DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setInt(2, limit);
            stmt.setInt(3, offset);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    TopSellingProduct product = new TopSellingProduct();
                    product.setProductId(rs.getInt("id"));
                    product.setProductName(rs.getString("product_name"));
                    product.setProductImage(rs.getString("product_image"));
                    product.setPrice(rs.getDouble("avg_price"));
                    product.setTotalSold(rs.getLong("total_sold"));
                    product.setRevenue(rs.getDouble("revenue"));
                    productList.add(product);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return productList;
    }

    @Override
    public int getTotalProductCountBySearch(String keyword) {
        String sql = "SELECT COUNT(DISTINCT p.id) as total FROM Product p WHERE p.product_name LIKE ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
