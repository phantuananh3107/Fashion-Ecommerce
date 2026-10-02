package controller.web;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import model.UserObject;

// Đăng ký đường link /customer/checkout để hứng request từ nút bấm
@WebServlet("/customer/checkout")
public class CheckoutController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public CheckoutController() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserObject userObject = (UserObject) session.getAttribute("user");

        // 1. Kiểm tra xem khách đã đăng nhập chưa
        if (userObject == null) {
            // Nếu chưa, đá về trang đăng nhập
            response.sendRedirect(request.getContextPath() + "/public/login");
            return;
        }

        // 2. Kiểm tra xem giỏ hàng có đồ không
        if (session.getAttribute("cart") == null) {
            // Nếu giỏ hàng trống mà cứ cố tình vào trang thanh toán thì đá về lại trang giỏ hàng
            response.sendRedirect(request.getContextPath() + "/customer/cart");
            return;
        }

        // 3. Nếu mọi thứ ok, mở file giao diện checkout.jsp ra cho khách điền form
        RequestDispatcher dispatcher = request.getRequestDispatcher("/views/web/checkout.jsp");
        dispatcher.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}