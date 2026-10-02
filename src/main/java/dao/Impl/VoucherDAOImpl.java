package dao.Impl;

import dao.VoucherDAO;
import model.VoucherObject;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VoucherDAOImpl implements VoucherDAO {

    @Override
    public VoucherObject getVoucherByCode(String code) {
        VoucherObject voucher = null;
        // Chỉ lấy những mã có thật và số lượng phải lớn hơn 0
        String sql = "SELECT * FROM Voucher WHERE voucher_code = ? AND quantity > 0";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    voucher = new VoucherObject();
                    voucher.setVoucherId(rs.getInt("voucher_id"));
                    voucher.setVoucherCode(rs.getString("voucher_code"));
                    voucher.setDiscountAmount(rs.getDouble("discount_amount"));
                    voucher.setQuantity(rs.getInt("quantity"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return voucher; // Nếu không tìm thấy hoặc hết lượt, sẽ trả về null
    }

    @Override
    public boolean decreaseVoucherQuantity(int voucherId) {
        String sql = "UPDATE Voucher SET quantity = quantity - 1 WHERE voucher_id = ? AND quantity > 0";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, voucherId);
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu update thành công

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}