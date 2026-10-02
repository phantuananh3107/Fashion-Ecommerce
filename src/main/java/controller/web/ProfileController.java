package controller.web; // Sửa lại package cho đúng với file của bạn nếu cần

import dao.UserDAO;
import dao.Impl.UserDAOImpl;
import model.UserObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

// Đường dẫn url để vào trang cá nhân
@WebServlet(urlPatterns = {"/customer/profile"})
public class ProfileController extends HttpServlet {

    private UserDAO userDAO = new UserDAOImpl();

    // doGet: Xử lý khi người dùng VÀO trang (Hiển thị form)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();

        // LẤY USER TỪ SESSION (Lưu ý: Chữ "user" này phải khớp với lúc bạn setAttribute ở hàm Login)
        UserObject userSession = (UserObject) session.getAttribute("user");

        if (userSession == null) {
            // Chưa đăng nhập -> Đá về trang login
            resp.sendRedirect(req.getContextPath() + "/public/login");
            return;
        }

        // Lấy dữ liệu mới nhất từ DB lên để hiển thị cho chắc cú
        UserObject freshUser = userDAO.getUserById(userSession.getUserId());
        req.setAttribute("userInfo", freshUser);

        // Chuyển hướng sang giao diện JSP
        req.getRequestDispatcher("/views/web/profile.jsp").forward(req, resp);
    }

    // doPost: Xử lý khi người dùng ấn nút LƯU THAY ĐỔI
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Cấu hình UTF-8 để nhận tiếng Việt không bị lỗi font
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession();
        UserObject userSession = (UserObject) session.getAttribute("user");

        if (userSession != null) {
            // 1. Lấy dữ liệu từ các ô input mà người dùng vừa nhập
            String fullname = req.getParameter("fullname");
            String phone = req.getParameter("phone");
            String gender = req.getParameter("gender");
            String address = req.getParameter("address");

            // 2. Cập nhật vào đối tượng
            userSession.setFullname(fullname);
            userSession.setPhoneNumber(phone);
            userSession.setGender(gender);
            userSession.setAddress(address);

            // 3. Lưu xuống Database bằng hàm đã viết ở DAO
            boolean isUpdated = userDAO.updateUser(userSession);

            if (isUpdated) {
                // Thành công: Cập nhật lại session & báo xanh
                session.setAttribute("user", userSession);
                req.setAttribute("message", "Cập nhật thông tin thành công!");
            } else {
                // Thất bại: Báo đỏ
                req.setAttribute("error", "Lỗi! Không thể cập nhật thông tin.");
            }

            // Trả dữ liệu về lại trang profile để hiển thị
            req.setAttribute("userInfo", userSession);
            req.getRequestDispatcher("/views/web/profile.jsp").forward(req, resp);

        } else {
            resp.sendRedirect(req.getContextPath() + "/public/login");
        }
    }
}