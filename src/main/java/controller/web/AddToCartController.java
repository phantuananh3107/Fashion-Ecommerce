package controller.web;

import dao.CartDAO;
import dao.Impl.CartDAOImpl;
import dao.IProductDAO; // THÊM THƯ VIỆN NÀY
import dao.Impl.ProductImpl; // THÊM THƯ VIỆN NÀY
import model.CartObject;
import model.ProductObject; // THÊM THƯ VIỆN NÀY
import model.UserObject;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/customer/add-to-cart")
public class AddToCartController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        UserObject user = (UserObject) session.getAttribute("user");

        // 1. Kiểm tra đăng nhập
        if (user == null) {
            response.setStatus(401);
            out.print("{\"status\":\"error\", \"message\":\"Vui lòng đăng nhập!\"}");
            out.flush();
            out.close();
            return;
        }

        try {
            String pIdStr = request.getParameter("product_id");
            String qtyStr = request.getParameter("quantity");
            String size = request.getParameter("size");

            if (pIdStr == null || qtyStr == null || size == null || pIdStr.isEmpty()) {
                out.print("{\"status\":\"error\", \"message\":\"Thiếu thông tin sản phẩm!\"}");
                return;
            }

            int productId = Integer.parseInt(pIdStr);
            int quantityToAdd = Integer.parseInt(qtyStr);

            // ==========================================
            // BƯỚC MỚI BỔ SUNG: KIỂM TRA TỒN KHO TRƯỚC KHI THÊM
            // ==========================================
            IProductDAO productDAO = new ProductImpl(); // Sử dụng DAO của em
            ProductObject product = productDAO.getProductById(productId);

            if (product == null || product.isDeleted()) {
                out.print("{\"status\":\"error\", \"message\":\"Sản phẩm không tồn tại hoặc đã bị xóa!\"}");
                return;
            }

            CartDAO cartDAO = new CartDAOImpl();

            // Tính tổng số lượng nếu sản phẩm này đã có sẵn trong giỏ hàng
            List<CartObject> currentCart = cartDAO.getCartItems(user.getUserId());
            int currentQtyInCart = 0;
            for (CartObject item : currentCart) {
                if (item.getProductObject().getProductId() == productId && item.getProductSize().equals(size)) {
                    currentQtyInCart = item.getQuantity();
                    break;
                }
            }

            int totalExpectedQty = currentQtyInCart + quantityToAdd;

            // Nếu Tổng (Đang có trong giỏ + Muốn thêm) > Tồn kho => Chặn lại
            if (totalExpectedQty > product.getProductQuantity()) {
                int maxCanAdd = product.getProductQuantity() - currentQtyInCart;
                if (maxCanAdd > 0) {
                    out.print("{\"status\":\"error\", \"message\":\"Giỏ hàng đang có " + currentQtyInCart + " sp. Bạn chỉ có thể thêm tối đa " + maxCanAdd + " sp nữa!\"}");
                } else {
                    out.print("{\"status\":\"error\", \"message\":\"Giỏ hàng của bạn đã chứa số lượng tối đa có thể mua của sản phẩm này!\"}");
                }
                return; // Dừng lại, không chạy code thêm vào giỏ bên dưới nữa
            }
            // ==========================================

            // 4. Nếu đủ tồn kho -> Thực hiện thêm vào giỏ hàng
            cartDAO.addToCart(user.getUserId(), productId, quantityToAdd, size);

            // 5. Lấy lại list mới nhất từ DB để Session/Sidebar hiển thị đúng
            List<CartObject> updatedCart = cartDAO.getCartItems(user.getUserId());
            session.setAttribute("cart", updatedCart);

            out.print("{\"status\":\"success\", \"message\":\"Thêm vào giỏ hàng thành công!\"}");

        } catch (NumberFormatException e) {
            out.print("{\"status\":\"error\", \"message\":\"Dữ liệu số lượng hoặc ID không hợp lệ!\"}");
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"status\":\"error\", \"message\":\"Lỗi hệ thống: " + e.getMessage() + "\"}");
        } finally {
            out.flush();
            out.close();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }
}