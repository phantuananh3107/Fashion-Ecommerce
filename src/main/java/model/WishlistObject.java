package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WishlistObject {
    private int wishlistId;
    private int userId;
    private String productCode;
    private Timestamp createdDate;
}