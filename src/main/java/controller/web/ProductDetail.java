package controller.web;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.IProductDAO;
import dao.Impl.ProductImpl;
import model.ProductObject;

@WebServlet("/public/product-detail")
public class ProductDetail extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private IProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        // Khởi tạo ProductImpl
        productDAO = new ProductImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy productId từ tham số truy vấn
        String productIdStr = request.getParameter("id");
        ProductObject product = null;

        if (productIdStr != null && !productIdStr.isEmpty()) {
            try {
                int productId = Integer.parseInt(productIdStr);
                // Lấy thông tin sản phẩm từ cơ sở dữ liệu
                product = productDAO.getProductById(productId);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        // Đặt sản phẩm vào request attribute
        request.setAttribute("product", product);

        // Chuyển hướng đến product-detail.jsp
        RequestDispatcher rd = request.getRequestDispatcher("/views/web/product-detail.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}