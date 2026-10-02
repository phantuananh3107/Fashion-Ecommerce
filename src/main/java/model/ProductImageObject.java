package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductImageObject {
    private int imageId;
    private int productId;
    private String imageUrl;
    private int displayOrder; // Để sắp xếp thứ tự hiển thị
    private boolean isPrimary; // Ảnh chính (sẽ hiển thị đầu tiên)
}

