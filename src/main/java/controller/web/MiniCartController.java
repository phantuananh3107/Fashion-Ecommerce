package controller.web;

import dao.CartDAO;
import dao.Impl.CartDAOImpl;
import dao.IProductDAO;
import dao.Impl.ProductImpl;
import model.CartObject;
import model.ProductObject;
import model.UserObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * MiniCartController: Cập nhật giỏ hàng vào Session
 * URL: /customer/mini-cart
 */
@WebServlet("/customer/mini-cart")
public class MiniCartController extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private CartDAO cartDAO;

    @Override
    public void init() throws ServletException {
        cartDAO = new CartDAOImpl();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserObject userObject = (UserObject) session.getAttribute("user");

        // Nếu chưa đăng nhập, trả về error
        if (userObject == null) {
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("error");
            out.flush();
            return;
        }

        try {
            // Lấy danh sách sản phẩm trong giỏ từ Database
            List<CartObject> cartItems = cartDAO.getCartItems(userObject.getUserId());

            // Cập nhật vào Session
            session.setAttribute("cart", cartItems);

            // Trả về "success"
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("success");
            out.flush();

        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.print("error");
            out.flush();
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}

