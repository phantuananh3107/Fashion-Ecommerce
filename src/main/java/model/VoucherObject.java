package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VoucherObject {
    private int voucherId;
    private String voucherCode;
    private double discountAmount;
    private int quantity;
}