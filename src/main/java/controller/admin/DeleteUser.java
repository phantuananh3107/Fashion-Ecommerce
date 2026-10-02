package controller.admin;

import dao.Impl.UserDAOImpl;
import dao.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/admin-delete-user")
public class DeleteUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    public void init() {
        userDAO = new UserDAOImpl();
    }

    // ĐÃ SỬA: Đổi từ doGet sang doPost để bảo mật và khớp với Javascript fetch
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String userIdParam = request.getParameter("userId");

        if (userIdParam != null && !userIdParam.trim().isEmpty()) {
            try {
                int userId = Integer.parseInt(userIdParam);
                boolean success = userDAO.deactivateUser(userId);

                if (success) {
                    // Cập nhật DB thành công
                    response.setStatus(HttpServletResponse.SC_OK); // Trả về mã 200 (response.ok = true)
                    response.getWriter().write("{\"status\": \"success\"}");
                } else {
                    // Gọi DB thất bại
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Lỗi 500
                    response.getWriter().write("{\"status\": \"fail\"}");
                }
            } catch (NumberFormatException e) {
                // Sai định dạng ID
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Lỗi 400
                response.getWriter().write("{\"status\": \"fail\"}");
            }
        } else {
            // Thiếu tham số ID
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Lỗi 400
            response.getWriter().write("{\"status\": \"fail\"}");
        }
    }
}