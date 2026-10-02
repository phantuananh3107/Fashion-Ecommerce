package dao;

import model.VoucherObject;

public interface VoucherDAO {
    // 1. Tìm voucher theo mã code (chỉ lấy voucher còn số lượng > 0)
    VoucherObject getVoucherByCode(String code);

    // 2. Trừ đi 1 số lượng của voucher sau khi khách đặt hàng thành công
    boolean decreaseVoucherQuantity(int voucherId);
}