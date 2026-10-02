package dao;

import java.util.List;

import model.CartObject;
import model.ProductObject;



public interface CartDAO {
	List<CartObject> getCartItems(int userId);

    CartObject getCartItem(int userId, int productId, String productSize);

    void addToCart(int userId, int productId, int quantity, String productSize);

    boolean updateCart(int userId, int productId, int quantity, String productSize);

    boolean deleteCartItem(int userId, int productId, String size);
    // Xóa toàn bộ giỏ hàng của user sau khi thanh toán thành công
    void clearCartByUserId(int userId);
}