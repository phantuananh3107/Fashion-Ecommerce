package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailObject {

    private int orderDetailId;
    private int quantitySold;
    private double price;
    private String productSize;
    private String productColor;
    private ProductObject productObject;
}
