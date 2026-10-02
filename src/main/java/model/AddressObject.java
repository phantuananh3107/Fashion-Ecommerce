package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressObject {
    private int addressId;
    private int userId;
    private String receiverName;
    private String receiverPhone;
    private String addressDetail;
    private boolean isDefault; // Ánh xạ với cột is_default trong bảng useraddress
}