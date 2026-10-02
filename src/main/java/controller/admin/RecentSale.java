package controller.admin;

import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.OrderDAO;
import dao.Impl.OrderDAOImpl;
import model.OrderInfo;


@WebServlet("/admin/admin-recent-sale")
public class RecentSale extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public RecentSale() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        OrderDAO orderDAO = new OrderDAOImpl();
        List<OrderInfo> orderList = orderDAO.getRecentOrders(50);
        request.setAttribute("orderList", orderList);
        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/RecentSales.jsp");
        rd.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}