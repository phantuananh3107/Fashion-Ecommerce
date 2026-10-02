package dao.Impl;

import dao.CategoryDAO;
import model.CategoryObject;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryDAOImpl implements CategoryDAO {

    @Override
    public List<CategoryObject> getMenuTree() {
        List<CategoryObject> rootCategories = new ArrayList<>();
        Map<Integer, CategoryObject> categoryMap = new HashMap<>();

        String sql = "SELECT * FROM category";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            // Bước 1: Khởi tạo tất cả Object và đưa vào Map để dễ tìm kiếm
            List<CategoryObject> allCategories = new ArrayList<>();
            while (rs.next()) {
                CategoryObject cat = new CategoryObject();
                cat.setCategoryId(rs.getInt("category_id"));
                cat.setCategoryName(rs.getString("category_name"));

                int parentId = rs.getInt("parent_id");
                // Nếu wasNull() là true thì set parentId = null
                cat.setParentId(rs.wasNull() ? null : parentId);

                categoryMap.put(cat.getCategoryId(), cat);
                allCategories.add(cat);
            }

            // Bước 2: Ghép nối cha - con (Xây dựng cây)
            for (CategoryObject cat : allCategories) {
                if (cat.getParentId() == null) {
                    // Nếu không có cha -> Nó là danh mục Gốc (Nam, Nữ)
                    rootCategories.add(cat);
                } else {
                    // Nếu có cha -> Tìm người cha đó trong Map và nhét nó vào list children của cha
                    CategoryObject parent = categoryMap.get(cat.getParentId());
                    if (parent != null) {
                        parent.getChildren().add(cat);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Lỗi getMenuTree: " + e.getMessage());
        }

        return rootCategories;
    }
}