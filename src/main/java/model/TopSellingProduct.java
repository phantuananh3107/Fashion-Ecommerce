package model;

import lombok.Data;

@Data
public class TopSellingProduct {
    private int productId;
    private String productName;
    private String productImage;
    private double price;
    private long totalSold;
    private double revenue;
}
