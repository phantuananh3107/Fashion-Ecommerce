package controller.web;

import dao.CartDAO;
import dao.Impl.CartDAOImpl;
import model.UserObject;
import model.CartObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

@WebServlet("/customer/cart-delete-item")
public class DeleteCartController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // LƯU Ý: Nhận ở doPost vì Javascript đang dùng hàm $.post()
        int productId = Integer.parseInt(request.getParameter("productId"));
        String size = request.getParameter("size");

        HttpSession session = request.getSession();
        UserObject userObject = (UserObject) session.getAttribute("user");

        if(userObject == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Chưa đăng nhập
            return;
        }

        CartDAO cartDAO = new CartDAOImpl();
        boolean checkDelete = cartDAO.deleteCartItem(userObject.getUserId(), productId, size);

        if (checkDelete) {
            // ==============================================================
            // BƯỚC QUAN TRỌNG: TÌM VÀ XÓA MÓN ĐỒ TRONG SESSION
            // ==============================================================
            List<CartObject> cartList = (List<CartObject>) session.getAttribute("cart");
            if (cartList != null) {
                Iterator<CartObject> iterator = cartList.iterator();
                while (iterator.hasNext()) {
                    CartObject item = iterator.next();
                    // So sánh đúng ID và Size thì xóa
                    if (item.getProductObject().getProductId() == productId && item.getProductSize().equals(size)) {
                        iterator.remove();
                        break;
                    }
                }
                session.setAttribute("cart", cartList); // Lưu danh sách mới lên lại
            }

            // Trả về mã 200 Thành công cho Javascript (Không dùng sendRedirect nữa)
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}
