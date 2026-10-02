package dao;

import java.util.List;

import model.ProductObject;
import model.ProductImageObject;

public interface IProductDAO {
	List<ProductObject> getAllProducts();
	ProductObject getProductById(int id);
	boolean insertProduct(ProductObject product);
	boolean updateProduct(ProductObject product);
	boolean deleteProduct(int id);
    double getTotalRevenue();
    double getTotalInventoryValue();
    List<ProductObject> getProductsByCategoryId(int categoryId);
    List<ProductObject> getDeletedProducts(); // Thêm phương thức lấy sản phẩm trong thùng rác
    boolean restoreProduct(int id); // Thêm phương thức khôi phục
    boolean permanentlyDeleteProduct(int id); // Thêm phương thức xóa vĩnh viễn
    List<ProductObject> getDiscountedProducts();
    List<String> getColorsByCategoryId(int categoryId);
    List<ProductObject> getProductsFiltered(int categoryId, String color, double minPrice, double maxPrice, String discountType);
    List<String> getColorsForSale();
    List<ProductObject> searchProducts(String keyword);


}
