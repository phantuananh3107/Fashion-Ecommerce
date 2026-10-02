package controller.auth;

import dao.Impl.UserDAOImpl;
import dao.UserDAO;
import model.UserObject;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Login
 */
@WebServlet("/logout")
public class Logout extends HttpServlet {
    private static final long serialVersionUID = 1L;


    public Logout() {
        super();
        // TODO Auto-generated constructor stub
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        // In ra console trước khi hủy session
        String fullname = (String) session.getAttribute("fullname");
        System.out.println("========== LOGOUT ==========");
        System.out.println("User: " + fullname + " đã đăng xuất");
        System.out.println("============================");

        // Hủy session
        session.invalidate();

        // Lấy context path để redirect
        String contextPath = request.getContextPath();

        // Redirect về trang chủ
        response.sendRedirect(contextPath + "/public/trang-chu");
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub


    }

}
