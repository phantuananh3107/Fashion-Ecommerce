package controller.admin;

import dao.Impl.UserDAOImpl;
import dao.UserDAO;
import model.UserObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/admin/admin-detail-user")
public class DetailUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    public DetailUser() {
        super();
        userDAO = new UserDAOImpl(); // Khởi tạo DAO
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Lấy user từ tham số URL
        String userId = request.getParameter("userId");
        UserObject user = null;

        try {
            if (userId != null && !userId.isEmpty()) {
                int userID = Integer.parseInt(userId);
                // Lấy thông tin sản phẩm từ cơ sở dữ liệu
                user = userDAO.getUserById(userID);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // Đặt user vào request để gửi đến JSP
        request.setAttribute("user", user);

        // Chuyển tiếp đến trang chi tiết sản phẩm
        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/details-user.jsp");
        rd.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}