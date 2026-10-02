package controller.admin;

import java.io.IOException;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import dao.ITopSelling;
import dao.Impl.TopSellingImpl;
import model.TopSellingProduct;

@WebServlet("/admin/admin-top-selling")
public class TopSelling extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PRODUCTS_PER_PAGE = 10;

    public TopSelling() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int currentPage = 1;
        String searchKeyword = request.getParameter("search");

        try {
            String pageParam = request.getParameter("page");
            if (pageParam != null) {
                currentPage = Integer.parseInt(pageParam);
            }
        } catch (NumberFormatException e) {
            currentPage = 1;
        }

        ITopSelling topSellingDAO = new TopSellingImpl();
        List<TopSellingProduct> productList;
        int totalProducts;

        // Xử lý tìm kiếm
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            productList = topSellingDAO.searchTopSellingProducts(searchKeyword, PRODUCTS_PER_PAGE, (currentPage - 1) * PRODUCTS_PER_PAGE);
            totalProducts = topSellingDAO.getTotalProductCountBySearch(searchKeyword);
        } else {
            productList = topSellingDAO.getTopSellingProducts(PRODUCTS_PER_PAGE, (currentPage - 1) * PRODUCTS_PER_PAGE);
            totalProducts = topSellingDAO.getTotalProductCount();
        }

        int totalPages = (int) Math.ceil((double) totalProducts / PRODUCTS_PER_PAGE);

        request.setAttribute("productList", productList);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("searchKeyword", searchKeyword); // Lưu từ khóa để hiển thị lại

        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/TopSelling.jsp");
        rd.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}