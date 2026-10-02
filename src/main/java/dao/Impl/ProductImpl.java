package dao.Impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

import dao.IProductDAO;
import model.ProductObject;
import model.ProductImageObject;
import util.DBUtil;

public class ProductImpl implements IProductDAO {

    private boolean exe(Connection conn, PreparedStatement pre) {
        if (pre != null) {
            try {
                int results = pre.executeUpdate();
                if (results == 0) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public List<ProductObject> getAllProducts() {
        List<ProductObject> list = new ArrayList<>();
        // SỬA TẠI ĐÂY: Thêm LEFT JOIN
        String sql = "SELECT p.*, c.category_name FROM Product p "
                + "LEFT JOIN category c ON p.category_id = c.category_id "
                + "WHERE p.isDeleted = FALSE "
                + "ORDER BY p.id ASC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductObject p = new ProductObject();
                p.setProductId(rs.getInt("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));
                p.setProductImage(rs.getString("product_image")); // Ảnh chính
                p.setProductImage1(rs.getString("product_image1"));
                p.setProductImage2(rs.getString("product_image2"));
                p.setProductImage3(rs.getString("product_image3"));
                p.setProductImage4(rs.getString("product_image4"));
                p.setProductPrice(rs.getDouble("product_price"));
                p.setProductQuantity(rs.getInt("product_quantity"));
                p.setProductColor(rs.getString("product_color"));
                p.setProductSize(rs.getString("product_size"));
                p.setProductDescription(rs.getString("product_description"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category_name"));
                p.setDiscountPercent(rs.getInt("discount_percent"));
                p.setCreatedAt(rs.getDate("created_at"));
                p.setUpdateAt(rs.getDate("updated_at"));
                p.setDeleted(rs.getBoolean("isDeleted"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ProductObject> getProductsByCategoryId(int categoryId) {
        List<ProductObject> list = new ArrayList<>();
        // Câu lệnh SQL lọc theo category_id
        String sql = "SELECT p.*, c.category_name FROM Product p LEFT JOIN category c ON p.category_id = c.category_id WHERE p.category_id = ? AND p.isDeleted = FALSE ORDER BY p.id ASC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, categoryId); // Truyền ID danh mục vào câu query
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProductObject p = new ProductObject();
                p.setProductId(rs.getInt("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));
                p.setProductImage(rs.getString("product_image"));
                p.setProductImage1(rs.getString("product_image1"));
                p.setProductImage2(rs.getString("product_image2"));
                p.setProductImage3(rs.getString("product_image3"));
                p.setProductImage4(rs.getString("product_image4"));
                p.setProductPrice(rs.getDouble("product_price"));
                p.setProductQuantity(rs.getInt("product_quantity"));
                p.setProductColor(rs.getString("product_color"));
                p.setProductSize(rs.getString("product_size"));
                p.setProductDescription(rs.getString("product_description"));
                p.setDiscountPercent(rs.getInt("discount_percent"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category_name"));

                p.setCreatedAt(rs.getDate("created_at"));
                p.setUpdateAt(rs.getDate("updated_at"));
                p.setDeleted(rs.getBoolean("isDeleted"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public List<ProductObject> getDiscountedProducts() {
        List<ProductObject> list = new ArrayList<>();
        String sql = "SELECT p.*, c.category_name FROM Product p LEFT JOIN category c ON p.category_id = c.category_id WHERE p.discount_percent > 0 AND p.isDeleted = FALSE ORDER BY p.id DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProductObject p = new ProductObject();
                p.setProductId(rs.getInt("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));
                p.setProductImage(rs.getString("product_image1"));
                p.setProductImage1(rs.getString("product_image1"));
                p.setProductImage2(rs.getString("product_image2"));
                p.setProductImage3(rs.getString("product_image3"));
                p.setProductImage4(rs.getString("product_image4"));
                p.setProductPrice(rs.getDouble("product_price"));
                // ĐÃ BỔ SUNG LẤY % GIẢM GIÁ
                p.setDiscountPercent(rs.getInt("discount_percent"));
                p.setProductQuantity(rs.getInt("product_quantity"));
                p.setProductColor(rs.getString("product_color"));
                p.setProductSize(rs.getString("product_size"));
                p.setProductDescription(rs.getString("product_description"));

                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category_name"));

                p.setCreatedAt(rs.getDate("created_at"));
                p.setUpdateAt(rs.getDate("updated_at"));
                p.setDeleted(rs.getBoolean("isDeleted"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public ProductObject getProductById(int id) {
        // ĐÃ SỬA: Thêm LEFT JOIN lấy tên danh mục cho trang detail.jsp
        String sql = "SELECT p.*, c.category_name FROM Product p LEFT JOIN category c ON p.category_id = c.category_id WHERE p.id = ? AND p.isDeleted = FALSE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                ProductObject p = new ProductObject();
                p.setProductId(rs.getInt("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));
                p.setProductImage(rs.getString("product_image1"));
                p.setProductImage1(rs.getString("product_image1"));
                p.setProductImage2(rs.getString("product_image2"));
                p.setProductImage3(rs.getString("product_image3"));
                p.setProductImage4(rs.getString("product_image4"));
                p.setProductPrice(rs.getDouble("product_price"));
                p.setDiscountPercent(rs.getInt("discount_percent"));
                p.setProductQuantity(rs.getInt("product_quantity"));
                p.setProductColor(rs.getString("product_color"));
                p.setProductSize(rs.getString("product_size"));
                p.setProductDescription(rs.getString("product_description"));

                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category_name")); // Lấy tên danh mục

                p.setCreatedAt(rs.getDate("created_at"));
                p.setUpdateAt(rs.getDate("updated_at"));
                p.setDeleted(rs.getBoolean("isDeleted"));

                // Tạo danh sách ảnh từ 4 ảnh riêng biệt
                List<ProductImageObject> images = new ArrayList<>();
                if (p.getProductImage1() != null) {
                    ProductImageObject img1 = new ProductImageObject();
                    img1.setImageUrl(p.getProductImage1());
                    img1.setDisplayOrder(0);
                    img1.setPrimary(true);
                    images.add(img1);
                }
                if (p.getProductImage2() != null) {
                    ProductImageObject img2 = new ProductImageObject();
                    img2.setImageUrl(p.getProductImage2());
                    img2.setDisplayOrder(1);
                    img2.setPrimary(false);
                    images.add(img2);
                }
                if (p.getProductImage3() != null) {
                    ProductImageObject img3 = new ProductImageObject();
                    img3.setImageUrl(p.getProductImage3());
                    img3.setDisplayOrder(2);
                    img3.setPrimary(false);
                    images.add(img3);
                }
                if (p.getProductImage4() != null) {
                    ProductImageObject img4 = new ProductImageObject();
                    img4.setImageUrl(p.getProductImage4());
                    img4.setDisplayOrder(3);
                    img4.setPrimary(false);
                    images.add(img4);
                }
                p.setProductImages(images);

                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public boolean insertProduct(ProductObject product) {
        StringBuilder sql = new StringBuilder("INSERT INTO Product(");
        sql.append("product_code,product_name,product_image1,product_image2,product_image3,product_image4,");
        sql.append("product_price,product_quantity,product_color,product_size,");
        sql.append("product_description,category_id,isDeleted,created_at,updated_at");
        sql.append(") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pre = conn.prepareStatement(sql.toString())) {
            conn.setAutoCommit(false);
            pre.setString(1, product.getProductCode());
            pre.setString(2, product.getProductName());
            pre.setString(3, product.getProductImage1());
            pre.setString(4, product.getProductImage2());
            pre.setString(5, product.getProductImage3());
            pre.setString(6, product.getProductImage4());
            pre.setDouble(7, product.getProductPrice());
            pre.setInt(8, product.getProductQuantity());
            pre.setString(9, product.getProductColor());
            pre.setString(10, product.getProductSize());
            pre.setString(11, product.getProductDescription());
            pre.setInt(12, product.getCategoryId());
            pre.setBoolean(13, false); // Mặc định isDeleted = false
            pre.setTimestamp(14, new Timestamp(System.currentTimeMillis())); // created_at
            pre.setTimestamp(15, null); // updated_at
            return this.exe(conn, pre);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateProduct(ProductObject product) {
        StringBuilder sql = new StringBuilder("UPDATE Product SET ");
        sql.append("product_code=?, product_name=?, product_image1=?, product_image2=?, ");
        sql.append("product_image3=?, product_image4=?, product_price=?, product_quantity=?, ");
        sql.append("product_color=?, product_size=?, product_description=?, category_id=?, ");
        sql.append("isDeleted=?, updated_at=? WHERE id=?");
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pre = conn.prepareStatement(sql.toString())) {
            conn.setAutoCommit(false);
            pre.setString(1, product.getProductCode());
            pre.setString(2, product.getProductName());
            pre.setString(3, product.getProductImage1());
            pre.setString(4, product.getProductImage2());
            pre.setString(5, product.getProductImage3());
            pre.setString(6, product.getProductImage4());
            pre.setDouble(7, product.getProductPrice());
            pre.setInt(8, product.getProductQuantity());
            pre.setString(9, product.getProductColor());
            pre.setString(10, product.getProductSize());
            pre.setString(11, product.getProductDescription());
            pre.setInt(12, product.getCategoryId());
            pre.setBoolean(13, product.isDeleted());
            pre.setTimestamp(14, new Timestamp(System.currentTimeMillis())); // updated_at
            pre.setInt(15, product.getProductId());
            return exe(conn, pre);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteProduct(int id) {
        String sql = "UPDATE Product SET isDeleted = TRUE, updated_at = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pre = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            pre.setTimestamp(1, new Timestamp(System.currentTimeMillis())); // updated_at
            pre.setInt(2, id);
            return exe(conn, pre);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ProductObject> getDeletedProducts() {
        List<ProductObject> list = new ArrayList<>();
        // SỬA TẠI ĐÂY: Thêm LEFT JOIN
        String sql = "SELECT p.*, c.category_name FROM Product p "
                + "LEFT JOIN category c ON p.category_id = c.category_id "
                + "WHERE p.isDeleted = TRUE "
                + "ORDER BY p.id DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductObject p = new ProductObject();
                p.setProductId(rs.getInt("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));
                p.setProductImage(rs.getString("product_image"));
                p.setProductImage1(rs.getString("product_image1"));
                p.setProductImage2(rs.getString("product_image2"));
                p.setProductImage3(rs.getString("product_image3"));
                p.setProductImage4(rs.getString("product_image4"));
                p.setProductPrice(rs.getDouble("product_price"));
                p.setProductQuantity(rs.getInt("product_quantity"));
                p.setProductColor(rs.getString("product_color"));
                p.setProductSize(rs.getString("product_size"));
                p.setProductDescription(rs.getString("product_description"));
                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category_name"));
                p.setCreatedAt(rs.getDate("created_at"));
                p.setUpdateAt(rs.getDate("updated_at"));
                p.setDeleted(rs.getBoolean("isDeleted"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean restoreProduct(int id) {
        String sql = "UPDATE Product SET isDeleted = FALSE, updated_at = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pre = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            pre.setTimestamp(1, new Timestamp(System.currentTimeMillis())); // updated_at
            pre.setInt(2, id);
            return exe(conn, pre);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean permanentlyDeleteProduct(int id) {
        String sql = "DELETE FROM Product WHERE id = ? AND isDeleted = TRUE";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pre = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            pre.setInt(1, id);
            return exe(conn, pre);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public double getTotalRevenue() {
        String sql = "SELECT SUM(COALESCE(quantity_sold, 0) * COALESCE(price, 0)) FROM OrderDetail";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public double getTotalInventoryValue() {
        String sql = "SELECT SUM(total_value) FROM ("
                   + "SELECT COALESCE(p.product_price, 0) * (p.product_quantity - COALESCE(SUM(od.quantity_sold), 0)) AS total_value "
                   + "FROM Product p "
                   + "LEFT JOIN OrderDetail od ON p.id = od.product_id "
                   + "WHERE p.isDeleted = FALSE "
                   + "GROUP BY p.id, p.product_quantity, p.product_price"
                   + ") subquery";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public List<String> getColorsByCategoryId(int categoryId) {
        List<String> colors = new ArrayList<>();

        // Nếu categoryId = 0 (tức là ấn Xem tất cả), lấy toàn bộ màu trong hệ thống
        String sql = (categoryId > 0)
                ? "SELECT DISTINCT product_color FROM Product WHERE category_id = ? AND isDeleted = FALSE AND product_color IS NOT NULL"
                : "SELECT DISTINCT product_color FROM Product WHERE isDeleted = FALSE AND product_color IS NOT NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Chỉ truyền tham số nếu đang ở trong 1 danh mục cụ thể
            if (categoryId > 0) {
                ps.setInt(1, categoryId);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                colors.add(rs.getString("product_color"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return colors;
    }

    @Override
    public List<ProductObject> getProductsFiltered(int categoryId, String color, double minPrice, double maxPrice, String discountType) {
        List<ProductObject> list = new ArrayList<>();

        // 1. Dùng nguyên câu lệnh có LEFT JOIN của bạn, thêm bí danh "p." để tránh lỗi mập mờ cột (ambiguous column)
        StringBuilder sql = new StringBuilder("SELECT p.*, c.category_name FROM Product p LEFT JOIN category c ON p.category_id = c.category_id WHERE p.isDeleted = FALSE");

        // 2. Nối chuỗi điều kiện ĐỘNG (Nhớ thêm tiền tố p. vào các cột)
        if (categoryId > 0) {
            sql.append(" AND p.category_id = ?");
        }

        if (color != null && !color.isEmpty()) {
            sql.append(" AND p.product_color = ?");
        }

        if (maxPrice > 0) {
            // Lọc dựa trên giá thực tế
            sql.append(" AND (p.product_price * (100 - p.discount_percent) / 100) BETWEEN ? AND ?");
        }

        if ("1".equals(discountType)) {
            sql.append(" AND p.discount_percent > 0 AND p.discount_percent < 30");
        } else if ("2".equals(discountType)) {
            sql.append(" AND p.discount_percent >= 30 AND p.discount_percent <= 50");
        } else if ("3".equals(discountType)) {
            sql.append(" AND p.discount_percent > 50");
        }else if ("all_sale".equals(discountType)) {
            // Thêm dòng này để trang Sale mặc định lấy hết đồ đang giảm giá
            sql.append(" AND p.discount_percent > 0");
        }

        sql.append(" ORDER BY p.id DESC");

        // 3. Thực thi truy vấn
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (categoryId > 0) {
                ps.setInt(paramIndex++, categoryId);
            }
            if (color != null && !color.isEmpty()) {
                ps.setString(paramIndex++, color);
            }
            if (maxPrice > 0) {
                ps.setDouble(paramIndex++, minPrice);
                ps.setDouble(paramIndex++, maxPrice);
            }

            // 4. Lấy dữ liệu đắp vào Object (Bê nguyên các trường của bạn sang)
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProductObject p = new ProductObject();
                p.setProductId(rs.getInt("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));

                // Các ảnh
                p.setProductImage(rs.getString("product_image"));
                p.setProductImage1(rs.getString("product_image1"));
                p.setProductImage2(rs.getString("product_image2"));
                p.setProductImage3(rs.getString("product_image3"));
                p.setProductImage4(rs.getString("product_image4"));

                // Giá, số lượng và CHIẾT KHẤU
                p.setProductPrice(rs.getDouble("product_price"));
                p.setDiscountPercent(rs.getInt("discount_percent")); // Phải có trường này để ngoài JSP còn tính giá Sale
                p.setProductQuantity(rs.getInt("product_quantity"));

                // Phân loại
                p.setProductColor(rs.getString("product_color"));
                p.setProductSize(rs.getString("product_size")); // Không có cái này JSP sẽ báo lỗi hoặc trống size
                p.setProductDescription(rs.getString("product_description"));

                // Danh mục
                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category_name"));

                // Meta data
                p.setCreatedAt(rs.getDate("created_at"));
                p.setUpdateAt(rs.getDate("updated_at"));
                p.setDeleted(rs.getBoolean("isDeleted"));

                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public List<String> getColorsForSale() {
        List<String> colors = new ArrayList<>();
        // Câu lệnh SQL chỉ lấy màu từ những sản phẩm có discount_percent > 0
        String sql = "SELECT DISTINCT product_color FROM Product WHERE discount_percent > 0 AND isDeleted = FALSE AND product_color IS NOT NULL";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                colors.add(rs.getString("product_color"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return colors;
    }

    @Override
    public List<ProductObject> searchProducts(String keyword) {
        List<ProductObject> list = new ArrayList<>();
        // SQL: LEFT JOIN với bảng category để lấy được category_name hiển thị ra giao diện nếu cần

        String sql = "SELECT p.*, c.category_name FROM Product p "
                + "LEFT JOIN category c ON p.category_id = c.category_id "
                + "WHERE p.isDeleted = FALSE "
                + "AND (p.product_code LIKE ? OR p.product_name LIKE ? OR c.category_name LIKE ?) "
                + "ORDER BY p.id DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // Tham số : Khớp một phần tên sản phẩm (chứa keyword)
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            ps.setString(3, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ProductObject p = new ProductObject();
                p.setProductId(rs.getInt("id"));
                p.setProductCode(rs.getString("product_code"));
                p.setProductName(rs.getString("product_name"));

                // Lấy đúng các trường ảnh như hàm mẫu bạn gửi
                p.setProductImage(rs.getString("product_image1"));
                p.setProductImage1(rs.getString("product_image1"));
                p.setProductImage2(rs.getString("product_image2"));
                p.setProductImage3(rs.getString("product_image3"));
                p.setProductImage4(rs.getString("product_image4"));

                p.setProductPrice(rs.getDouble("product_price"));
                p.setProductQuantity(rs.getInt("product_quantity"));
                p.setProductColor(rs.getString("product_color"));
                p.setProductSize(rs.getString("product_size"));
                p.setProductDescription(rs.getString("product_description"));

                // Các thông tin bổ sung
                p.setCategoryId(rs.getInt("category_id"));
                p.setCategoryName(rs.getString("category_name")); // Lấy từ kết quả JOIN
                p.setDiscountPercent(rs.getInt("discount_percent")); // Cần thiết để tính giá Sale

                p.setCreatedAt(rs.getDate("created_at"));
                p.setUpdateAt(rs.getDate("updated_at"));
                p.setDeleted(rs.getBoolean("isDeleted"));

                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}