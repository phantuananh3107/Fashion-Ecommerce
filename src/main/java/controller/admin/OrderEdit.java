package controller.admin;

import dao.Impl.OrderDAOImpl;
import dao.OrderDAO;
import model.OrderObject;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/admin-order-edit")
public class OrderEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
       

    public OrderEdit() {
        super();
        // TODO Auto-generated constructor stub
    }


	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String orderIdParam = request.getParameter("orderId");
		int orderId;
		if(orderIdParam != null) {
			orderId = Integer.parseInt(orderIdParam);
			OrderDAO orderDAO = new OrderDAOImpl();
			OrderObject order = orderDAO.getOrderDetailByOrderId(orderId);

			request.setAttribute("order", order);
			RequestDispatcher rd = request.getRequestDispatcher("/views/admin/order-edit.jsp");
			rd.forward(request, response);
		}
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
