package controller.web;

import dao.Impl.OrderDAOImpl;
import dao.OrderDAO;
import model.OrderObject;
import model.UserObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/customer/order-history")
public class OrderHistoryController extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        UserObject user = (UserObject) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/public/login");
            return;
        }

        OrderDAO orderDAO = new OrderDAOImpl();
        List<OrderObject> list = orderDAO.getOrdersByUserId(user.getUserId());

        request.setAttribute("orderList", list);
        // Đẩy sang trang JSP (Bạn tạo file này ở bước 3)
        request.getRequestDispatcher("/views/web/order-history.jsp").forward(request, response);
    }
}