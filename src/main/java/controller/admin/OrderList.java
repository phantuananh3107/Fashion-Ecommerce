package controller.admin;

import dao.Impl.OrderDAOImpl;
import dao.OrderDAO;
import model.OrderObject;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class OrderList
 */
@WebServlet("/admin/admin-order-list")
public class OrderList extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public OrderList() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String pageNum = request.getParameter("pageNo");

		int pageNo = pageNum == null ? 1 : Integer.parseInt(pageNum);

		String orderStatus = request.getParameter("orderStatus");
		String paymentStatus = request.getParameter("paymentStatus");
		String paymentMethod = request.getParameter("paymentMethod");

		OrderDAO orderDAO = new OrderDAOImpl();
		List<OrderObject> orders = orderDAO.getAllOrders(pageNo, 3, orderStatus, paymentStatus, paymentMethod);
		int pageNumber = (int) Math.ceil((double) orderDAO.countAllOrders(orderStatus, paymentStatus, paymentMethod) / 3);

		request.setAttribute("orders", orders);
		request.setAttribute("pageNo", pageNo);
		request.setAttribute("pageNumber", pageNumber);
		request.setAttribute("orderStatus", orderStatus);
		request.setAttribute("paymentStatus", paymentStatus);
		request.setAttribute("paymentMethod", paymentMethod);

		RequestDispatcher rd = request.getRequestDispatcher("/views/admin/order-list.jsp");
		rd.forward(request, response);
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
