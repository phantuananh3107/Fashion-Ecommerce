package controller.auth;

import dao.Impl.UserDAOImpl;
import dao.Impl.CartDAOImpl;
import dao.UserDAO;
import dao.CartDAO;
import model.UserObject;
import model.CartObject;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;        // ĐÃ THÊM: Dùng để tạo sổ tay
import java.util.HashMap;    // ĐÃ THÊM: Dùng để tạo sổ tay

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
@WebServlet("/public/login")
public class LoginController extends HttpServlet {
	private static final long serialVersionUID = 1L;


	public LoginController() {
		super();
		// TODO Auto-generated constructor stub
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestDispatcher rd = request.getRequestDispatcher("/views/web/login.jsp");
		rd.forward(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */

//  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//     String username = request.getParameter("username");
//     String password = request.getParameter("password");
//     String contextPath = request.getContextPath();
//
//     if(username != null && !username.isEmpty() && password != null && !password.isEmpty()){
//        UserDAO userDAO = new UserDAOImpl();
//        UserObject userObject = userDAO.getUserByUsernamePassword(username, password);
//
//        if (userObject == null){
//           // Chuyển hướng về trang login nếu sai tk/mk
//           response.sendRedirect(contextPath + "/public/login?error=invalid");
//        } else {
//           // Lưu thông tin người dùng vào session
//           HttpSession session = request.getSession();
//           session.setAttribute("user", userObject);
//           session.setAttribute("userId", userObject.getUserId());
//           session.setAttribute("fullname", userObject.getFullname());
//           session.setAttribute("email", userObject.getEmail());
//
//           String roleName = userObject.getRole().getRoleName();
//           session.setAttribute("role", roleName);
//
//           // ========== GLOBAL SYNC: Nạp giỏ hàng vào Session ==========
//           // Khi user đăng nhập, tải danh sách giỏ hàng từ DB vào session một lần
//           CartDAO cartDAO = new CartDAOImpl();
//           List<CartObject> cartItems = cartDAO.getCartItems(userObject.getUserId());
//           // Nếu cartItems là null, khởi tạo ArrayList rỗng để tránh NullPointerException
//           if (cartItems == null) {
//              cartItems = new ArrayList<>();
//           }
//           session.setAttribute("cart", cartItems);
//           // ============================================================
//
//           // In ra console để kiểm tra giá trị thực tế từ DB
//           System.out.println("========== LOGIN SUCCESS ==========");
//           System.out.println("User ID: " + userObject.getUserId());
//           System.out.println("Full Name: " + userObject.getFullname());
//           System.out.println("Email: " + userObject.getEmail());
//           System.out.println("Role: " + roleName);
//           System.out.println("Cart Items: " + (cartItems != null ? cartItems.size() : 0));
//           System.out.println("===================================");
//
//           if ("ADMIN".equals(roleName)) {
//              response.sendRedirect(contextPath + "/admin/admin-home");
//           } else {
//              response.sendRedirect(contextPath + "/public/trang-chu");
//           }
//        }
//     } else {
//        // Nếu không có username hoặc password
//        response.sendRedirect(contextPath + "/public/login?error=empty");
//     }
//  }

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String username = request.getParameter("username");
		String password = request.getParameter("password");
		String contextPath = request.getContextPath();
		HttpSession session = request.getSession();

		if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {

			@SuppressWarnings("unchecked")
			Map<String, Integer> loginAttempts = (Map<String, Integer>) session.getAttribute("loginAttempts");
			if (loginAttempts == null) {
				loginAttempts = new HashMap<>();
			}

			int failedAttempts = loginAttempts.getOrDefault(username, 0);

			if (failedAttempts >= 5) {
				response.sendRedirect(contextPath + "/public/login?error=locked");
				return;
			}

			UserDAO userDAO = new UserDAOImpl();
			UserObject userObject = userDAO.getUserByUsernamePassword(username, password);

			if (userObject == null) {
				// NHẬP SAI MẬT KHẨU HOẶC TÀI KHOẢN KHÔNG TỒN TẠI
				failedAttempts++;
				loginAttempts.put(username, failedAttempts);
				session.setAttribute("loginAttempts", loginAttempts);

				if (failedAttempts >= 5) {
					userDAO.lockUserAccount(username);
					response.sendRedirect(contextPath + "/public/login?error=locked");
				} else {
					int attemptsLeft = 5 - failedAttempts;
					response.sendRedirect(contextPath + "/public/login?error=invalid&attempts=" + attemptsLeft);
				}
			} else {
				// ĐÃ NHẬP ĐÚNG TÀI KHOẢN VÀ MẬT KHẨU!
				// --> KIỂM TRA TRẠNG THÁI TRƯỚC KHI CHO VÀO

				int status = userObject.getActive();
				if (status == 2 || status == 3) {
					// Nếu đang bị Tạm khóa (2) hoặc Ngừng hoạt động (3) -> Báo lỗi khóa luôn!
					response.sendRedirect(contextPath + "/public/login?error=locked");
					return;
				}

				// ĐĂNG NHẬP THÀNH CÔNG (Trạng thái = 1)
				loginAttempts.remove(username); // Xóa lịch sử sai
				session.setAttribute("loginAttempts", loginAttempts);

				// GIỮ NGUYÊN CODE SET SESSION CỦA BẠN BÊN DƯỚI NÀY
				session.setAttribute("user", userObject);
				session.setAttribute("userId", userObject.getUserId());
				session.setAttribute("fullname", userObject.getFullname());
				session.setAttribute("email", userObject.getEmail());

				String roleName = userObject.getRole().getRoleName();
				session.setAttribute("role", roleName);

				CartDAO cartDAO = new CartDAOImpl();
				List<CartObject> cartItems = cartDAO.getCartItems(userObject.getUserId());
				if (cartItems == null) {
					cartItems = new ArrayList<>();
				}
				session.setAttribute("cart", cartItems);

				if ("ADMIN".equals(roleName)) {
					response.sendRedirect(contextPath + "/admin/admin-home");
				} else {
					response.sendRedirect(contextPath + "/public/trang-chu");
				}
			}
		} else {
			response.sendRedirect(contextPath + "/public/login?error=empty");
		}
	}
}