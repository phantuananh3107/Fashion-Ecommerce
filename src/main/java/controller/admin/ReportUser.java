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

@WebServlet("/admin/admin-report-user")
public class ReportUser extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    public ReportUser() {
        super();
        userDAO = new UserDAOImpl(); // Khởi tạo DAO
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy dữ liệu từ DAO
        int totalUsers = userDAO.getTotalUserCount();
        var genderCounts = userDAO.getUserGenderCounts();
        var statusCounts = userDAO.getUserStatusCounts();
        var newUsersByMonth = userDAO.getNewUsersByMonth();
        var roleCounts = userDAO.getUserRoleCounts(); // Thêm dòng này

        // Truyền dữ liệu qua request
        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("genderCounts", genderCounts);
        request.setAttribute("statusCounts", statusCounts);
        request.setAttribute("newUsersByMonth", newUsersByMonth);
        request.setAttribute("roleCounts", roleCounts); // Thêm dòng này

        // Chuyển tiếp đến trang chi tiết sản phẩm
        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/report-user.jsp");
        rd.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}