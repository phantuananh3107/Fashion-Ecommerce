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

@WebServlet(urlPatterns = {"/public/sale"})
public class SaleController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 24; // Số sản phẩm mỗi trang giống AllProduct
    private IProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        productDAO = new ProductImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // 1. Lấy danh sách màu sắc CHỈ DÀNH CHO ĐỒ SALE và Sắp xếp
        List<String> colorList = productDAO.getColorsForSale(); // Dùng hàm mới mình vừa viết ở bước trước
        if (colorList != null) {
            colorList.sort((c1, c2) -> Integer.compare(getColorPriority(c1), getColorPriority(c2)));
        }
        req.setAttribute("colorList", colorList);

        // 2. Hứng tham số lọc
        String color = req.getParameter("color");
        String minPriceStr = req.getParameter("minPrice");
        String maxPriceStr = req.getParameter("maxPrice");
        String discountType = req.getParameter("discountType");

        double minPrice = (minPriceStr != null && !minPriceStr.isEmpty()) ? Double.parseDouble(minPriceStr) : 0;
        double maxPrice = (maxPriceStr != null && !maxPriceStr.isEmpty()) ? Double.parseDouble(maxPriceStr) : 0;

        if (discountType == null || discountType.isEmpty()) {
            discountType = "all_sale";
        }

        // 3. Lấy danh sách sản phẩm ĐÃ LỌC (Đây là danh sách tổng)
        List<ProductObject> allSaleProducts = productDAO.getProductsFiltered(0, color, minPrice, maxPrice, discountType);

        // 4. LOGIC PHÂN TRANG (Giữ nguyên cấu trúc của bạn bên AllProduct)
        String pageStr = req.getParameter("page");
        int page = 1;
        try {
            if (pageStr != null) {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            }
        } catch (NumberFormatException e) {
            page = 1;
        }

        int totalProducts = allSaleProducts.size();
        int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);
        if (page > totalPages && totalPages > 0) {
            page = totalPages;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, totalProducts);
        List<ProductObject> products = new ArrayList<>();
        if (start < totalProducts) {
            products = allSaleProducts.subList(start, end);
        }

        // 5. Đẩy dữ liệu ra View
        req.setAttribute("products", products); // Danh sách đã cắt theo trang
        req.setAttribute("currentPage", page);
        req.setAttribute("totalPages", totalPages);
        req.setAttribute("categoryName", "SẢN PHẨM KHUYẾN MÃI");

        RequestDispatcher rd = req.getRequestDispatcher("/views/web/all_product.jsp");
        rd.forward(req, resp);
    }

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
        return 100;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}