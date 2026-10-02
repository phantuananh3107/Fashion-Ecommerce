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
import dao.CategoryDAO;           // Thêm import
import dao.Impl.CategoryDAOImpl;  // Thêm import
import model.ProductObject;
import model.CategoryObject;      // Thêm import

    @WebServlet(urlPatterns = {"/public/trang-chu"})
public class HomeController extends HttpServlet {
    private static final long serialVersionUID = 2686801510274002166L;

    private IProductDAO productDAO;
    private CategoryDAO categoryDAO; // Khai báo thêm DAO cho Menu

    @Override
    public void init() throws ServletException {
        productDAO = new ProductImpl();
        categoryDAO = new CategoryDAOImpl(); // Khởi tạo

        // Lấy danh sách Mega Menu (đã ghép cha-con)
        List<CategoryObject> menuCategories = categoryDAO.getMenuTree();

        // Lưu vào Application Scope (Tất cả các trang JSP đều đọc được biến này)
        getServletContext().setAttribute("menuCategories", menuCategories);
    }

        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            // 1. Lấy danh sách hàng Mới (NEW ARRIVAL)
            List<ProductObject> allProducts = productDAO.getAllProducts();
            List<ProductObject> newArrivals = allProducts.size() > 10 ? allProducts.subList(0, 10) : allProducts;

            // 2. Lấy danh sách hàng GIẢM GIÁ (SALE)
            List<ProductObject> saleProducts = productDAO.getDiscountedProducts();

            // Nếu hàng sale nhiều quá, chỉ cắt lấy 10 sản phẩm mới nhất để hiển thị cho trang chủ đỡ dài
            if (saleProducts.size() > 10) {
                saleProducts = saleProducts.subList(0, 10);
            }

            // 3. Đẩy dữ liệu ra ngoài file home.jsp
            req.setAttribute("newArrivals", newArrivals);
            req.setAttribute("saleProducts", saleProducts);

            RequestDispatcher rd = req.getRequestDispatcher("/views/web/home.jsp");
            rd.forward(req, resp);
        }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}