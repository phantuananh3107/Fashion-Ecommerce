package controller.web;

import dao.IProductDAO;
import dao.Impl.ProductImpl;
import model.ProductObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/public/search")
public class SearchController extends HttpServlet {
    private static final int PAGE_SIZE = 24; // Thêm biến chia trang giống AllProduct
    private IProductDAO productDAO = new ProductImpl();

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        if (keyword == null) keyword = "";

        // 1. Gọi hàm search (đã nâng cấp tìm cả danh mục)
        List<ProductObject> allResults = productDAO.searchProducts(keyword.trim());

        // 2. Bơm danh sách màu sắc để cột trái không bị trống (Sửa lỗi ảnh 2)
        List<String> colorList = productDAO.getColorsByCategoryId(0);
        request.setAttribute("colorList", colorList);

        // 3. Phân trang cơ bản để không bị vỡ giao diện all_product.jsp
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

        int totalProducts = allResults.size();
        int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);
        if (page > totalPages && totalPages > 0) {
            page = totalPages;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, totalProducts);
        List<ProductObject> products = new ArrayList<>();
        if (start < totalProducts) {
            products = allResults.subList(start, end);
        }

        // 4. Đẩy dữ liệu ra view
        request.setAttribute("products", products); // Danh sách sản phẩm của 1 trang
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("categoryName", "KẾT QUẢ TÌM KIẾM CHO: " + keyword.toUpperCase());

        // Tận dụng lại giao diện có sẵn
        request.getRequestDispatcher("/views/web/all_product.jsp").forward(request, response);
    }
}