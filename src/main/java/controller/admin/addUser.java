package controller.admin;

import dao.Impl.UserDAOImpl;
import dao.UserDAO;
import model.RoleObject;
import model.UserObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

@WebServlet("/admin/admin-add-user")
public class addUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    public void init() {
        userDAO = new UserDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.removeAttribute("user");
        request.getSession().removeAttribute("user");

        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/add-user.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            String fullname = request.getParameter("fullname");
            String email = request.getParameter("email");
            String password = request.getParameter("password");
            String phone = request.getParameter("phoneNumber");
            String gender = request.getParameter("gender");
            String address = request.getParameter("address");
            String roleIdStr = request.getParameter("roleId");

            if (fullname == null || email == null || password == null || roleIdStr == null) {
                throw new IllegalArgumentException("Thiếu thông tin bắt buộc.");
            }

            int roleId = Integer.parseInt(roleIdStr);

            // 👇 Tự động lấy ngày hiện tại làm ngày tạo
            Date createDate = Date.valueOf(LocalDate.now());

            UserObject user = new UserObject();
            user.setFullname(fullname);
            user.setEmail(email);
            user.setPassword(password);
            user.setPhoneNumber(phone);
            user.setGender(gender);
            user.setAddress(address);
            user.setCreateDate(createDate);
            user.setModifiedDate(null);
            user.setActive(1);
            user.setLoginCount(0);

            RoleObject role = new RoleObject();
            role.setRoleId(roleId);
            user.setRole(role);

            boolean success = userDAO.insertUser(user);

            if (success) {
                request.setAttribute("successMessage", "Thêm người dùng thành công!");
                request.setAttribute("user", null); // Xóa thông tin nhập lại
            } else {
                request.setAttribute("errorMessage", "Thêm người dùng thất bại!");
                request.setAttribute("user", user);
            }

        } catch (Exception e) {
            UserObject user = new UserObject();
            user.setFullname(request.getParameter("fullname"));
            user.setEmail(request.getParameter("email"));
            user.setPassword(request.getParameter("password"));
            user.setPhoneNumber(request.getParameter("phoneNumber"));
            user.setGender(request.getParameter("gender"));
            user.setAddress(request.getParameter("address"));
            request.setAttribute("errorMessage", "Lỗi: " + e.getMessage());
            request.setAttribute("user", user);
        }

        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/add-user.jsp");
        rd.forward(request, response);
    }
}
