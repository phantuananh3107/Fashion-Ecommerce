package model;

import lombok.Data;

@Data
public class OrderInfo {
    private String customerName;
    private String productName;
    private double price;
    private String orderStatus;
    private String orderDate;
}
