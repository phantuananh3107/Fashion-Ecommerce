package dao.Impl;

import dao.AddressDAO;
import model.AddressObject;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AddressDAOImpl implements AddressDAO {

    @Override
    public List<AddressObject> getAddressesByUserId(int userId) {
        List<AddressObject> list = new ArrayList<>();
        // Lấy địa chỉ theo user_id, sắp xếp is_default = 1 lên đầu tiên
        String sql = "SELECT * FROM useraddress WHERE user_id = ? ORDER BY is_default DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                AddressObject addr = new AddressObject();
                addr.setAddressId(rs.getInt("address_id"));
                addr.setUserId(rs.getInt("user_id"));
                addr.setReceiverName(rs.getString("receiver_name"));
                addr.setReceiverPhone(rs.getString("receiver_phone"));
                addr.setAddressDetail(rs.getString("address_detail"));
                addr.setDefault(rs.getBoolean("is_default"));
                list.add(addr);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi getAddressesByUserId: " + e.getMessage());
        }
        return list;
    }

    // Hàm này rất quan trọng: Set toàn bộ is_default = 0 cho user này
    @Override
    public void clearDefaultAddress(int userId) {
        String sql = "UPDATE useraddress SET is_default = 0 WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean insertAddress(AddressObject address) {
        // Nếu địa chỉ mới là mặc định, reset các địa chỉ cũ
        if (address.isDefault()) {
            clearDefaultAddress(address.getUserId());
        }

        String sql = "INSERT INTO useraddress(user_id, receiver_name, receiver_phone, address_detail, is_default) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, address.getUserId());
            ps.setString(2, address.getReceiverName());
            ps.setString(3, address.getReceiverPhone());
            ps.setString(4, address.getAddressDetail());
            ps.setBoolean(5, address.isDefault());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateAddress(AddressObject address) {
        // Nếu địa chỉ sửa thành mặc định, reset các địa chỉ cũ
        if (address.isDefault()) {
            clearDefaultAddress(address.getUserId());
        }

        String sql = "UPDATE useraddress SET receiver_name=?, receiver_phone=?, address_detail=?, is_default=? WHERE address_id=? AND user_id=?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, address.getReceiverName());
            ps.setString(2, address.getReceiverPhone());
            ps.setString(3, address.getAddressDetail());
            ps.setBoolean(4, address.isDefault());
            ps.setInt(5, address.getAddressId());
            ps.setInt(6, address.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}