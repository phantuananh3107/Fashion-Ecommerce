package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartObject {

    private int cartId;
    private int quantity;
    private String productSize;
    private UserObject userObject;
    private ProductObject productObject;
}
