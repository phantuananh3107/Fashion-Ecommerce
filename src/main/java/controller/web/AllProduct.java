package controller.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.IProductDAO;
import dao.Impl.ProductImpl;
import model.ProductObject;

@WebServlet("/public/all-product")
public class AllProduct extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 24;
    private IProductDAO productDAO;

    public AllProduct() {
        super();
        productDAO = new ProductImpl();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ==========================================
        // PHẦN 1: BỘ LỌC ĐỘNG (Lọc dữ liệu từ DB)
        // ==========================================

        // 1. Lấy danh sách màu sắc và SẮP XẾP THEO BẢNG MÀU (Sáng -> Tối)
        List<String> colorList = productDAO.getColorsByCategoryId(0);
        if (colorList != null) {
            colorList.sort((c1, c2) -> Integer.compare(getColorPriority(c1), getColorPriority(c2)));
        }
        request.setAttribute("colorList", colorList);

        // Hứng tham số lọc
        String color = request.getParameter("color");
        String minPriceStr = request.getParameter("minPrice");
        String maxPriceStr = request.getParameter("maxPrice");
        String discountType = request.getParameter("discountType");

        double minPrice = (minPriceStr != null && !minPriceStr.isEmpty()) ? Double.parseDouble(minPriceStr) : 0;
        double maxPrice = (maxPriceStr != null && !maxPriceStr.isEmpty()) ? Double.parseDouble(maxPriceStr) : 0;

        List<ProductObject> allProducts;

        // Lấy danh sách tổng: Có lọc hoặc Không lọc
        if (color != null || minPrice > 0 || maxPrice > 0 || discountType != null) {
            allProducts = productDAO.getProductsFiltered(0, color, minPrice, maxPrice, discountType);
        } else {
            allProducts = productDAO.getAllProducts();
        }

        // ==========================================
        // PHẦN 2: PHÂN TRANG (Giữ nguyên logic của bạn)
        // ==========================================

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

        // Tính tổng số trang dựa trên danh sách đã lọc ở Phần 1
        int totalProducts = allProducts.size();
        int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);
        if (page > totalPages && totalPages > 0) {
            page = totalPages;
        }

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
        request.setAttribute("categoryName", "Tất cả sản phẩm");

        // Forward đến JSP
        RequestDispatcher rd = request.getRequestDispatcher("/views/web/all_product.jsp");
        rd.forward(request, response);
    }

    /**
     * Hàm định nghĩa thứ tự ưu tiên của màu sắc (Càng nhỏ càng đứng trước - Sáng trước Tối sau)
     */
    private int getColorPriority(String color) {
        String c = color.toLowerCase();
        if (c.contains("trắng")) return 1;
        if (c.contains("be") || c.contains("kem")) return 2;
        if (c.contains("vàng")) return 3;
        if (c.contains("cam")) return 4;
        if (c.contains("hồng")) return 5;
        if (c.contains("đỏ")) return 6;
        if (c.contains("bạc hà") || c.contains("xanh bơ")) return 7;
        if (c.contains("xanh bầu trời") || c.contains("xanh dương")) return 8;
        if (c.contains("tím")) return 9;
        if (c.contains("ghi") || c.contains("xám")) return 10;
        if (c.contains("nâu")) return 11;
        if (c.contains("đen")) return 12;
        return 100; // Họa tiết hoặc các màu khác cho xuống cuối
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}