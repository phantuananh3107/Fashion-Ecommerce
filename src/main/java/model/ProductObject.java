package model;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ProductObject {
	private int productId;
	private String productCode;
	private String productName;
	private String productImage; // Ảnh chính (backward compatibility)
	private String productImage1; // Ảnh 1
	private String productImage2; // Ảnh 2
	private String productImage3; // Ảnh 3
	private String productImage4; // Ảnh 4
	private double productPrice;
	private int productQuantity;
	private String productColor;
	private String productSize;
	private String productDescription;
	private int categoryId;
	private String categoryName;
	private Date createdAt;
	private Date updateAt;
	private boolean isDeleted;
	private double averageRating; // Đánh giá trung bình
	private int totalReviews; // Tổng số review
	private List<ProductImageObject> productImages; // Danh sách ảnh sản phẩm
	private int discountPercent;

}
