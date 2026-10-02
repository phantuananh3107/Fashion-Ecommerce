package controller.web;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.IProductDAO;
import dao.Impl.ProductImpl;
import model.ProductObject;

/**
 * Servlet implementation class saleT5
 */
@WebServlet("/public/saleT5")
public class saleT5 extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 24; // 24 sản phẩm mỗi trang (6 hàng x 4 sản phẩm)
    private IProductDAO productDAO;

    public saleT5() {
        super();
        productDAO = new ProductImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Lấy tham số page từ request, mặc định là trang 1
        String pageStr = request.getParameter("page");
        int page = 1;
        try {
            if (pageStr != null) {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        // Lấy tất cả sản phẩm
        List<ProductObject> allProducts = productDAO.getAllProducts();

        // Tính tổng số trang
        int totalProducts = allProducts.size();
        int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);
        if (page > totalPages && totalPages > 0) {
            page = totalPages; // Nếu page vượt quá totalPages, đặt về trang cuối
        }

        // Lấy danh sách sản phẩm cho trang hiện tại
        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, totalProducts);
        List<ProductObject> products = new ArrayList<>();
        if (start < totalProducts) {
            products = allProducts.subList(start, end);
        }

        // Đặt các thuộc tính vào request
        request.setAttribute("products", products);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);

        // Forward đến JSP
        RequestDispatcher rd = request.getRequestDispatcher("/views/web/saleT5.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}