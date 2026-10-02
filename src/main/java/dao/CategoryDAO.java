package dao;

import model.CategoryObject;
import java.util.List;

public interface CategoryDAO {
    // Lấy toàn bộ cây Menu
    List<CategoryObject> getMenuTree();
}