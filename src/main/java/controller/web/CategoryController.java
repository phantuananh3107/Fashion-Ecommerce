package controller.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.IProductDAO;
import dao.Impl.ProductImpl;
import model.CategoryObject;
import model.ProductObject;

@WebServlet(urlPatterns = {"/category"})
public class CategoryController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private IProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        productDAO = new ProductImpl();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");

        if (idStr != null && !idStr.isEmpty()) {
            try {
                int categoryId = Integer.parseInt(idStr);

                // --- BẮT ĐẦU PHẦN THÊM MỚI: BỘ LỌC ---
                // 1. Bơm danh sách màu sắc chỉ dành riêng cho danh mục này
                List<String> colorList = productDAO.getColorsByCategoryId(categoryId);
                req.setAttribute("colorList", colorList);

                // 2. Hứng các tham số lọc từ URL
                String color = req.getParameter("color");
                String minPriceStr = req.getParameter("minPrice");
                String maxPriceStr = req.getParameter("maxPrice");
                String discountType = req.getParameter("discountType");

                double minPrice = (minPriceStr != null && !minPriceStr.isEmpty()) ? Double.parseDouble(minPriceStr) : 0;
                double maxPrice = (maxPriceStr != null && !maxPriceStr.isEmpty()) ? Double.parseDouble(maxPriceStr) : 0;

                // 3. SỬA: Gọi hàm Lọc động thay vì hàm cũ
                List<ProductObject> listProduct = productDAO.getProductsFiltered(categoryId, color, minPrice, maxPrice, discountType);

                // Đẩy data ra View
                req.setAttribute("products", listProduct);
                // --- KẾT THÚC PHẦN THÊM MỚI ---


                // --- PHẦN DƯỚI NÀY LÀ TÌM TÊN DANH MỤC THÔNG MINH ---
                String categoryName = "Tất cả sản phẩm";
                List<CategoryObject> menuCategories = (List<CategoryObject>) getServletContext().getAttribute("menuCategories");

                if (menuCategories != null) {
                    for (CategoryObject root : menuCategories) {
                        if (root.getCategoryId() == categoryId) categoryName = root.getCategoryName();
                        if (root.getChildren() != null) {
                            for (CategoryObject child : root.getChildren()) {
                                if (child.getCategoryId() == categoryId) categoryName = child.getCategoryName();
                                if (child.getChildren() != null) {
                                    for (CategoryObject sub : child.getChildren()) {
                                        if (sub.getCategoryId() == categoryId) categoryName = sub.getCategoryName();
                                    }
                                }
                            }
                        }
                    }
                }

                req.setAttribute("categoryName", categoryName);

            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        RequestDispatcher rd = req.getRequestDispatcher("/views/web/all_product.jsp");
        rd.forward(req, resp);
    }
}