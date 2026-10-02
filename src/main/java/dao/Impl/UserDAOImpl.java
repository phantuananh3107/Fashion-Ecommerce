package dao.Impl;

import dao.UserDAO;
import model.RoleObject;
import model.UserObject;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class UserDAOImpl implements UserDAO {

	private boolean exe(Connection conn, PreparedStatement pre) {
		if (pre != null) {
			try {
				int results = pre.executeUpdate();
				System.out.println("Số dòng ảnh hưởng: " + results); // Debug

				// Lệnh này chỉ chạy được nếu trước đó đã gọi conn.setAutoCommit(false)
				conn.commit();
				return results > 0;

			} catch (SQLException e) {
				System.err.println("Lỗi SQL: " + e.getMessage()); // Debug
				e.printStackTrace();
				try {
					if (conn != null) {
						conn.rollback();
						System.err.println("Đã rollback giao dịch");
					}
				} catch (SQLException ex) {
					System.err.println("Lỗi rollback: " + ex.getMessage());
					ex.printStackTrace();
				}
				return false;
			} finally {
				// THÊM CHỐT AN TOÀN: Luôn trả lại trạng thái auto-commit=true để tránh lỗi cho các hàm khác dùng chung DB
				try {
					if (conn != null) {
						conn.setAutoCommit(true);
					}
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
		}
		return false;
	}

	@Override
	public UserObject getUserByUsernamePassword(String username, String password) {
		// Chỉ cho phép người dùng có user_isactive = 1 (Hoạt động) đăng nhập
		String sql = "SELECT u.*, r.role_name " +
				"FROM users u " +
				"INNER JOIN roles r ON u.role_id = r.role_id " +
				"WHERE user_email = ? AND password = ?";

		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, username);
			ps.setString(2, password);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					// 1. Đọc thông tin User từ DB
					UserObject user = mapRowToUser(rs);

					// 2. TĂNG SỐ LẦN ĐĂNG NHẬP (+1) VÀO DATABASE
					updateLoginCount(user.getUserId());

					// 3. Cập nhật luôn biến loginCount trên RAM để mang đi hiển thị ngay mà không cần truy vấn lại
					user.setLoginCount(user.getLoginCount() + 1);

					return user;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void updateLoginCount(int userId) {
		String updateSql = "UPDATE users SET login_count = login_count + 1 WHERE user_id = ?";
		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement ps = conn.prepareStatement(updateSql)) {
			// Lưu ý: Không cần setAutoCommit(false) ở đây vì chỉ chạy 1 lệnh Update đơn giản
			ps.setInt(1, userId);
			ps.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Lỗi khi tăng login_count: " + e.getMessage());
		}
	}


	private UserObject mapRowToUser(ResultSet rs) throws SQLException {
		UserObject user = new UserObject();
		user.setUserId(rs.getInt("user_id"));
		user.setPassword(rs.getString("password"));
		user.setGender(rs.getString("gender"));
		user.setFullname(rs.getString("user_fullname"));
		user.setPhoneNumber(rs.getString("user_phone_number"));
		user.setEmail(rs.getString("user_email"));
		user.setCreateDate(rs.getTimestamp("user_created_date"));
		user.setModifiedDate(rs.getTimestamp("user_modified_date"));

		user.setActive(rs.getInt("user_isactive"));

		user.setAddress(rs.getString("user_address"));
		user.setLoginCount(rs.getInt("login_count"));

		int roleId = rs.getInt("role_id");
		RoleObject role = new RoleObject();
		role.setRoleId(roleId);
		role.setRoleName(rs.getString("role_name"));
		user.setRole(role);

		return user;
	}

	// quyet anh
	@Override
	public List<UserObject> getAllUsers(String keyword, String sortBy) {
		List<UserObject> list = new ArrayList<>();

		// 1. Khởi tạo câu SQL gốc
		StringBuilder sql = new StringBuilder("SELECT u.*, r.role_name FROM users u INNER JOIN roles r ON u.role_id = r.role_id ");

		boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

		// 2. Nối thêm điều kiện TÌM KIẾM (Nếu có)
		if (hasKeyword) {
			sql.append("WHERE u.user_fullname LIKE ? OR u.user_email LIKE ? ");
		}

		// 3. Nối thêm điều kiện SẮP XẾP (Nếu có)
		if ("name".equals(sortBy)) {
			// Cắt lấy từ cuối cùng trong chuỗi user_fullname để sắp xếp theo Tên (A-Z)
			sql.append("ORDER BY SUBSTRING_INDEX(TRIM(u.user_fullname), ' ', -1) ASC ");
		} else if ("id".equals(sortBy)) {
			sql.append("ORDER BY u.user_id ASC ");
		} else {
			// Mặc định luôn là mới nhất lên đầu
			sql.append("ORDER BY u.user_created_date DESC ");
		}

		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql.toString())) {

			// 4. Đổ dữ liệu chữ vào dấu ? nếu có tìm kiếm
			if (hasKeyword) {
				String searchPattern = "%" + keyword.trim() + "%";
				ps.setString(1, searchPattern);
				ps.setString(2, searchPattern);
			}

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				UserObject p = new UserObject();
				p.setUserId(rs.getInt("user_id"));
				p.setFullname(rs.getString("user_fullname"));
				p.setPassword(rs.getString("password"));
				p.setGender(rs.getString("gender"));
				p.setAddress(rs.getString("user_address"));
				p.setLoginCount(rs.getInt("login_count"));
				p.setActive(rs.getInt("user_isactive"));
				p.setEmail(rs.getString("user_email"));
				p.setPhoneNumber(rs.getString("user_phone_number"));
				p.setCreateDate(rs.getDate("user_created_date"));
				p.setModifiedDate(rs.getDate("user_modified_date"));

				RoleObject r = new RoleObject();
				r.setRoleId(rs.getInt("role_id"));
				r.setRoleName(rs.getString("role_name"));
				p.setRole(r);

				list.add(p);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi getAllUsers: " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public UserObject getUserById(int id) {
		String sql = "SELECT \n" + "  u.*,\n" + "  r.role_name\n" + "FROM users u\n"
				+ "INNER JOIN roles r ON u.role_id = r.role_id\n WHERE  user_id = ?";
		try (Connection conn = DBUtil.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				UserObject p = new UserObject();
				p.setUserId(rs.getInt("user_id"));
				p.setFullname(rs.getString("user_fullname"));
				p.setPassword(rs.getString("password"));
				p.setGender(rs.getString("gender"));
				p.setAddress(rs.getString("user_address"));
				p.setLoginCount(rs.getInt("login_count"));

				p.setActive(rs.getInt("user_isactive"));
				p.setEmail(rs.getString("user_email"));
				p.setPhoneNumber(rs.getString("user_phone_number"));
				p.setCreateDate(rs.getDate("user_created_date"));
				p.setModifiedDate(rs.getDate("user_modified_date"));

				RoleObject r = new RoleObject();
				r.setRoleId(rs.getInt("role_id"));
				r.setRoleName(rs.getString("role_name"));
				p.setRole(r);
				return p;
			}
		} catch (SQLException e) {
			System.err.println("Lỗi getUserById: " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}



	@Override
	public boolean deactivateUser(int user_id) {
		System.out.println("Thực thi deactivateUser cho user_id: " + user_id);
		String sql = "UPDATE users SET user_isactive = 3 WHERE user_id = ?";
		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement pre = conn.prepareStatement(sql)) {

			if (conn == null) {
				System.err.println("Kết nối CSDL là null");
				return false;
			}

			// THÊM DÒNG NÀY: Chìa khóa giải quyết lỗi ảo
			conn.setAutoCommit(false);

			pre.setInt(1, user_id);
			return exe(conn, pre);

		} catch (SQLException e) {
			System.err.println("Lỗi deactivateUser: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public Map<String, Integer> getNewUsersByMonth() {
		Map<String, Integer> result = new LinkedHashMap<>();
		String sql = "SELECT DATE_FORMAT(user_created_date, '%m/%Y') AS month, COUNT(*) AS count " +
				"FROM users " +
				"GROUP BY month " +
				"ORDER BY STR_TO_DATE(month, '%m/%Y')";
		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				result.put(rs.getString("month"), rs.getInt("count"));
			}
		} catch (SQLException e) {
			System.err.println("Lỗi getNewUsersByMonth: " + e.getMessage());
		}
		return result;
	}

	@Override
	public Map<String, Integer> getUserStatusCounts() {
		Map<String, Integer> result = new HashMap<>();
		String sql = "SELECT " +
				"CASE WHEN user_isactive = 1 THEN 'Hoạt động' ELSE 'Không hoạt động' END AS status, " +
				"COUNT(*) AS count " +
				"FROM users " +
				"GROUP BY status";
		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				result.put(rs.getString("status"), rs.getInt("count"));
			}
		} catch (SQLException e) {
			System.err.println("Lỗi getUserStatusCounts: " + e.getMessage());
		}
		return result;
	}

	@Override
	public Map<String, Integer> getUserGenderCounts() {
		Map<String, Integer> result = new HashMap<>();
		String sql = "SELECT gender, COUNT(*) AS count FROM users GROUP BY gender";
		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				String gender = rs.getString("gender");
				if (gender == null || gender.isEmpty()) gender = "Không xác định";
				result.put(gender, rs.getInt("count"));
			}
		} catch (SQLException e) {
			System.err.println("Lỗi getUserGenderCounts: " + e.getMessage());
		}
		return result;
	}

	@Override
	public int getTotalUserCount() {
		String sql = "SELECT COUNT(*) AS total_users FROM users";
		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			if (rs.next()) {
				return rs.getInt("total_users");
			}
		} catch (SQLException e) {
			System.err.println("Lỗi getTotalUserCount: " + e.getMessage());
		}
		return 0;
	}
	@Override
	public Map<String, Integer> getUserRoleCounts() {
		Map<String, Integer> roleCounts = new HashMap<>();
		String sql = "SELECT r.role_name, COUNT(*) as count " +
				"FROM users u JOIN roles r ON u.role_id = r.role_id " +
				"GROUP BY r.role_name";
		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql);
			 ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				roleCounts.put(rs.getString("role_name"), rs.getInt("count"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return roleCounts;
	}

	@Override
	public boolean insertUser(UserObject user) {
		String sql = "INSERT INTO users(password, gender, user_fullname, user_phone_number, user_email, user_created_date, " +
				"user_modified_date, user_isactive, user_address, login_count, role_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement pre = conn.prepareStatement(sql)) {

			conn.setAutoCommit(false);
			pre.setString(1, user.getPassword());
			pre.setString(2, user.getGender());
			pre.setString(3, user.getFullname());
			pre.setString(4, user.getPhoneNumber());
			pre.setString(5, user.getEmail());
			pre.setDate(6, new java.sql.Date(user.getCreateDate().getTime()));
			pre.setDate(7, null); // modified_date = NULL

			// ĐÃ SỬA DÒNG NÀY: Chuyển sang setInt và getActive()
			pre.setInt(8, user.getActive());

			pre.setString(9, user.getAddress());
			pre.setInt(10, user.getLoginCount()); // mặc định là 0 hoặc 1
			pre.setInt(11, user.getRole().getRoleId());

			return this.exe(conn, pre);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean updateUser(UserObject user) {
		// Cập nhật tên, sđt, giới tính, địa chỉ và ngày chỉnh sửa
		String sql = "UPDATE users SET user_fullname = ?, user_phone_number = ?, gender = ?, user_address = ?, user_modified_date = ? WHERE user_id = ?";

		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement pre = conn.prepareStatement(sql)) {

			// Set auto commit false vì hàm exe() của bạn có gọi conn.commit()
			conn.setAutoCommit(false);

			pre.setString(1, user.getFullname());
			pre.setString(2, user.getPhoneNumber());
			pre.setString(3, user.getGender());
			pre.setString(4, user.getAddress());
			// Lấy thời gian hiện tại làm ngày modified_date
			pre.setTimestamp(5, new java.sql.Timestamp(System.currentTimeMillis()));
			pre.setInt(6, user.getUserId());

			// Gọi hàm exe() có sẵn của bạn để thực thi và commit
			return this.exe(conn, pre);

		} catch (SQLException e) {
			System.err.println("Lỗi updateUser: " + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public List<UserObject> getInactiveSoonUsers(int limit) {
		List<UserObject> list = new ArrayList<>();

		// ĐÃ TỐI ƯU HÓA LOGIC SQL:
		// 1. Chỉ tìm những người đang hoạt động (user_isactive = 1)
		// 2. Ưu tiên những người có Số lần đăng nhập thấp nhất (login_count ASC)
		// 3. Nếu modified_date là NULL, dùng tạm create_date để đánh giá độ "cũ" (COALESCE)
		String sql = "SELECT u.*, r.role_name " +
				"FROM users u " +
				"JOIN roles r ON u.role_id = r.role_id " +
				"WHERE u.user_isactive = 1 " +
				"ORDER BY u.login_count ASC, COALESCE(u.user_modified_date, u.user_created_date) ASC " +
				"LIMIT ?";

		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, limit);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(mapRowToUser(rs));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi getInactiveSoonUsers: " + e.getMessage());
		}
		return list;
	}

	// Hàm này sẽ tự động tìm user theo email và đổi trạng thái thành 2 (Tạm khóa)
	@Override
	public void lockUserAccount(String email) {
		String sql = "UPDATE users SET user_isactive = 2 WHERE user_email = ?";
		try (Connection conn = DBUtil.getConnection();
			 PreparedStatement ps = conn.prepareStatement(sql)) {

			// Ở đây chỉ chạy 1 lệnh Update đơn giản nên không cần setAutoCommit(false)
			ps.setString(1, email);
			int result = ps.executeUpdate();

			if(result > 0) {
				System.out.println("Đã tự động khóa tài khoản: " + email + " do đăng nhập sai 5 lần.");
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi khóa tài khoản: " + e.getMessage());
		}
	}

	@Override
	public String checkUserExistsDetail(String fullname, String email, String phone) {
		Connection conn = null;
		try {
			conn = DBUtil.getConnection();

			// 1. Kiểm tra Email trước (vì Email thường là quan trọng nhất)
			String sqlEmail = "SELECT 1 FROM users WHERE user_email = ?";
			try (PreparedStatement ps = conn.prepareStatement(sqlEmail)) {
				ps.setString(1, email);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) return "email_exists";
				}
			}

			// 2. Kiểm tra Số điện thoại
			String sqlPhone = "SELECT 1 FROM users WHERE user_phone_number = ?";
			try (PreparedStatement ps = conn.prepareStatement(sqlPhone)) {
				ps.setString(1, phone);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) return "phone_exists";
				}
			}

			// 3. Kiểm tra Tên đăng nhập (Fullname)
			String sqlName = "SELECT 1 FROM users WHERE user_fullname = ?";
			try (PreparedStatement ps = conn.prepareStatement(sqlName)) {
				ps.setString(1, fullname);
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) return "username_exists";
				}
			}

		} catch (SQLException e) {
			System.err.println("Lỗi checkUserExistsDetail: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try { if (conn != null) conn.close(); } catch (SQLException e) { e.printStackTrace(); }
		}

		return null; // Không trùng cái nào cả, dữ liệu sạch
	}

}
