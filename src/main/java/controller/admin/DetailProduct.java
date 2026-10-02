package controller.admin;

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

@WebServlet("/admin/admin-detail-product")
public class DetailProduct extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private IProductDAO productDAO;

    public DetailProduct() {
        super();
        productDAO = new ProductImpl(); // Khởi tạo DAO
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy productId từ tham số URL
        String productId = request.getParameter("id");
        ProductObject product = null;

        try {
            if (productId != null && !productId.isEmpty()) {
                int id = Integer.parseInt(productId);
                // Lấy thông tin sản phẩm từ cơ sở dữ liệu
                product = productDAO.getProductById(id);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        // Đặt sản phẩm vào request để gửi đến JSP
        request.setAttribute("product", product);

        // Chuyển tiếp đến trang chi tiết sản phẩm
        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/details-product.jsp");
        rd.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}