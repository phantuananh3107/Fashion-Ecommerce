package controller.web;

import dao.WishlistDAO;
import dao.Impl.WishlistDAOImpl;
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

@WebServlet(urlPatterns = {"/customer/wishlist", "/api/wishlist/toggle"})
public class WishlistController extends HttpServlet {

    private WishlistDAO wishlistDAO = new WishlistDAOImpl();

    // GET: Mở trang Danh sách yêu thích
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession();
        UserObject userSession = (UserObject) session.getAttribute("user");

        if (userSession == null) {
            resp.sendRedirect(req.getContextPath() + "/public/login");
            return;
        }

        List<ProductObject> wishlist = wishlistDAO.getWishlistByUserId(userSession.getUserId());
        req.setAttribute("wishlistProducts", wishlist);
        req.getRequestDispatcher("/views/web/wishlist.jsp").forward(req, resp);
    }

    // POST: Xử lý khi user bấm nút Tim (AJAX gọi ngầm)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        HttpSession session = req.getSession();
        UserObject userSession = (UserObject) session.getAttribute("user");

        // Chưa đăng nhập thì báo lỗi để JS bắt và chuyển hướng sang trang login
        if (userSession == null) {
            out.print("{\"status\":\"unauthorized\", \"message\":\"Vui lòng đăng nhập!\"}");
            out.flush();
            return;
        }

        try {
            // Lấy mã sản phẩm kiểu String
            String productCode = req.getParameter("productCode");

            boolean isAdded = wishlistDAO.toggleWishlist(userSession.getUserId(), productCode);

            if (isAdded) {
                out.print("{\"status\":\"added\", \"message\":\"Đã thêm sản phẩm vào danh sách yêu thích!\"}");
            } else {
                out.print("{\"status\":\"removed\", \"message\":\"Đã bỏ sản phẩm khỏi danh sách yêu thích!\"}");
            }
        } catch (Exception e) {
            out.print("{\"status\":\"error\", \"message\":\"Có lỗi xảy ra.\"}");
        }
        out.flush();
    }
}