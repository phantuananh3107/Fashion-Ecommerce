package dao;

import model.ProductObject; // Nhớ import đúng Model Product của bạn
import java.util.List;

public interface WishlistDAO {
    // Trả về true nếu thêm mới, false nếu xóa khỏi danh sách
    boolean toggleWishlist(int userId, String productCode);

    // Lấy danh sách sản phẩm yêu thích của 1 User
    List<ProductObject> getWishlistByUserId(int userId);
}