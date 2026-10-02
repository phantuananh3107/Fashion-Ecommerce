package controller.admin;

import dao.Impl.UserDAOImpl;
import dao.UserDAO;
import model.UserObject;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/admin-manage-account")
public class ManageAccount extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private UserDAO userDAO;

	public void init() {
		userDAO = new UserDAOImpl();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Bắt buộc có dòng này để tìm kiếm tiếng Việt không bị lỗi font
		request.setCharacterEncoding("UTF-8");

		// Hứng từ khóa tìm kiếm và cách sắp xếp từ giao diện
		String keyword = request.getParameter("keyword");
		String sortBy = request.getParameter("sortBy");

		// Gọi hàm đã nâng cấp
		List<UserObject> users = userDAO.getAllUsers(keyword, sortBy);

		// Gửi dữ liệu sang JSP
		request.setAttribute("users", users);

		RequestDispatcher rd = request.getRequestDispatcher("/views/admin/manage-account.jsp");
		rd.forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}