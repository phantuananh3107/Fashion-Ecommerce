package controller.auth;

import dao.UserDAO;
import dao.Impl.UserDAOImpl;
import model.RoleObject;
import model.UserObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@WebServlet("/public/register")
public class RegisterController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Luôn dùng forward để mở trang register.jsp
        request.getRequestDispatcher("/views/web/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. Thiết lập Tiếng Việt
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // 2. Lấy dữ liệu từ form
        String fullname = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String phone = request.getParameter("phone");
        String gender = request.getParameter("gender");
        String address = request.getParameter("address");

        UserDAO userDAO = new UserDAOImpl();
        String errorCode = null;

        // 3. Kiểm tra rỗng (Validation cơ bản)
        if (fullname == null || fullname.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                phone == null || phone.trim().isEmpty()) {
            errorCode = "empty";
        }
        // 4. Kiểm tra khớp mật khẩu
        else if (!password.equals(confirmPassword)) {
            errorCode = "password_mismatch";
        }
        // 5. Kiểm tra trùng lặp bằng hàm Detail (trả về email_exists, phone_exists...)
        else {
            errorCode = userDAO.checkUserExistsDetail(fullname, email, phone);
        }

        // XỬ LÝ KHI CÓ LỖI: Dùng Forward để giữ lại dữ liệu cũ
        if (errorCode != null) {
            request.setAttribute("error", errorCode);

            // Gửi ngược lại các giá trị đã nhập để hiển thị lại trên Form (Value attribute)
            request.setAttribute("oldFullname", fullname);
            request.setAttribute("oldEmail", email);
            request.setAttribute("oldPhone", phone);
            request.setAttribute("oldAddress", address);
            request.setAttribute("oldGender", gender);

            // Forward về trang JSP
            request.getRequestDispatcher("/views/web/register.jsp").forward(request, response);
            return;
        }

        // 6. Nếu dữ liệu sạch -> Đóng gói và lưu Database
        UserObject newUser = new UserObject();
        newUser.setFullname(fullname);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setPhoneNumber(phone);
        newUser.setGender(gender);
        newUser.setAddress(address);

        // Thiết lập các thông số hệ thống
        newUser.setCreateDate(new Date());
        newUser.setActive(1); // 1: Hoạt động
        newUser.setLoginCount(0);

        // Thiết lập quyền mặc định (RoleID = 2 cho khách hàng)
        RoleObject role = new RoleObject();
        role.setRoleId(2);
        newUser.setRole(role);

        boolean isSuccess = userDAO.insertUser(newUser);

        if (isSuccess) {
            // Thành công dùng Redirect để tránh lặp dữ liệu khi user nhấn F5
            response.sendRedirect(request.getContextPath() + "/public/login?success=register_ok");
        } else {
            // Lỗi hệ thống khi lưu
            request.setAttribute("error", "failed");
            request.getRequestDispatcher("/views/web/register.jsp").forward(request, response);
        }
    }
}