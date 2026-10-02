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
import java.util.List;

/**
 * Servlet implementation class ManageAccount
 */
@WebServlet("/admin/inactive-users")
public class InactiveSoonServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDAO userDAO;

	public void init() {
		userDAO = new UserDAOImpl();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		List<UserObject> users = userDAO.getInactiveSoonUsers(7); // Lấy top 7
		request.setAttribute("inactiveUsers", users);
		request.getRequestDispatcher("/views/admin/inactive_users.jsp").forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
