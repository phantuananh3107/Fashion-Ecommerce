package dao;
import model.AddressObject;
import java.util.List;

public interface AddressDAO {
    List<AddressObject> getAddressesByUserId(int userId);
    boolean insertAddress(AddressObject address);
    boolean updateAddress(AddressObject address);
    void clearDefaultAddress(int userId); // Hàm để xóa trạng thái mặc định của các địa chỉ cũ
}