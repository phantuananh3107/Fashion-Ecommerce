package controller.web;

import dao.CartDAO;
import dao.Impl.CartDAOImpl;
import model.CartObject;
import model.UserObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/customer/cart")
public class CartController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public CartController() {
        super();
        // TODO Auto-generated constructor stub
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();
        UserObject userObject = (UserObject) session.getAttribute("user");

        if(userObject == null) {
            response.sendRedirect(request.getContextPath() + "/public/login");
        }else{
            CartDAO cartDAO = new CartDAOImpl();
            List<CartObject> cartItems = cartDAO.getCartItems(userObject.getUserId());

            int totalQuantity = 0;
            double totalPrice = 0.0;
            for(CartObject cartItem : cartItems) {
                totalQuantity += cartItem.getQuantity();
                totalPrice += cartItem.getQuantity() * cartItem.getProductObject().getProductPrice();
            }

            request.setAttribute("cartItems", cartItems);
            request.setAttribute("totalQuantity", totalQuantity);
            request.setAttribute("totalPrice", totalPrice);
            RequestDispatcher rd = request.getRequestDispatcher("/views/web/cart.jsp");
            rd.forward(request, response);
        }

    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // TODO Auto-generated method stub
        doGet(request, response);
    }
}
