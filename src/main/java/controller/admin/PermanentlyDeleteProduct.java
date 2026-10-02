package controller.admin;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.IProductDAO;
import dao.Impl.ProductImpl;

@WebServlet("/admin/admin-permanently-delete-product")
public class PermanentlyDeleteProduct extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private IProductDAO productDAO;

    @Override
    public void init() {
        productDAO = new ProductImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String productIdStr = request.getParameter("id");
            if (productIdStr == null || productIdStr.isEmpty()) {
                request.getSession().setAttribute("errorMessage", "Thiếu ID sản phẩm.");
                response.sendRedirect("admin-manage-product");
                return;
            }

            int productId = Integer.parseInt(productIdStr);
            boolean success = productDAO.permanentlyDeleteProduct(productId);
            if (success) {
                request.getSession().setAttribute("message", "Xóa vĩnh viễn sản phẩm thành công!");
                response.sendRedirect("admin-manage-product");
            } else {
                request.getSession().setAttribute("errorMessage", "Xóa vĩnh viễn sản phẩm thất bại.");
                response.sendRedirect("admin-manage-product");
            }
        } catch (NumberFormatException e) {
            request.getSession().setAttribute("errorMessage", "ID sản phẩm không hợp lệ.");
            response.sendRedirect("admin-manage-product");
        } catch (Exception e) {
            request.getSession().setAttribute("errorMessage", "Đã xảy ra lỗi khi xóa vĩnh viễn sản phẩm.");
            response.sendRedirect("admin-manage-product");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}