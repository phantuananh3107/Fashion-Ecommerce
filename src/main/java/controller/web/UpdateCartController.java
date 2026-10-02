package controller.web;

import dao.CartDAO;
import dao.Impl.CartDAOImpl;
import model.CartObject;
import model.UserObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/customer/update-cart")
public class UpdateCartController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        UserObject user = (UserObject) session.getAttribute("user");

        if (user == null) {
            response.setStatus(401);
            return;
        }

        try {
            int productId = Integer.parseInt(request.getParameter("product_id"));
            String size = request.getParameter("size");
            String action = request.getParameter("action");

            CartDAO cartDAO = new CartDAOImpl();
            CartObject currentItem = cartDAO.getCartItem(user.getUserId(), productId, size);

            String result = "success";

            if (currentItem != null) {
                int currentQty = currentItem.getQuantity();

                if ("up".equals(action)) {
                    cartDAO.updateCart(user.getUserId(), productId, currentQty + 1, size);
                } else if ("down".equals(action)) {
                    if (currentQty > 1) {
                        cartDAO.updateCart(user.getUserId(), productId, currentQty - 1, size);
                    } else {
                        // Nếu đang là 1 mà bấm giảm -> Xóa luôn
                        cartDAO.deleteCartItem(user.getUserId(), productId, size);
                        result = "deleted";
                    }
                }
                // ==========================================
                // ĐÃ BỔ SUNG: Xử lý khi khách hàng gõ số trực tiếp
                // ==========================================
                else if ("set".equals(action)) {
                    try {
                        // Lấy số lượng mới từ Javascript truyền xuống
                        int newQty = Integer.parseInt(request.getParameter("quantity"));
                        if (newQty >= 1) {
                            // Cập nhật Database bằng con số mới
                            cartDAO.updateCart(user.getUserId(), productId, newQty, size);
                        }
                    } catch (NumberFormatException ex) {
                        // Bỏ qua nếu dữ liệu không phải là số hợp lệ
                    }
                }
                // ==========================================

                // Cập nhật lại Session để giao diện lấy dữ liệu mới
                List<CartObject> updatedCart = cartDAO.getCartItems(user.getUserId());
                session.setAttribute("cart", updatedCart);

                response.setContentType("text/plain");
                response.getWriter().write(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}