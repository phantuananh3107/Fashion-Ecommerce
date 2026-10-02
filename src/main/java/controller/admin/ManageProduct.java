package controller.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.CategoryDAO;
import dao.IProductDAO;
import dao.Impl.CategoryDAOImpl;
import dao.Impl.ProductImpl;
import model.CategoryObject;
import model.ProductObject;

@WebServlet("/admin/admin-manage-product")
public class ManageProduct extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private IProductDAO productDAO;
    private CategoryDAO categoryDAO; // ĐÃ THÊM: Khai báo CategoryDAO

    @Override
    public void init() {
        productDAO = new ProductImpl();
        categoryDAO = new CategoryDAOImpl(); // ĐÃ THÊM: Khởi tạo CategoryDAO
    }

    // ĐÃ THÊM: Hàm đệ quy làm phẳng cây Menu
    private List<CategoryObject> getFlatCategoryList() {
        List<CategoryObject> tree = categoryDAO.getMenuTree();
        List<CategoryObject> flatList = new ArrayList<>();
        for (CategoryObject root : tree) {
            flatList.add(root);
            if (root.getChildren() != null) {
                for (CategoryObject child : root.getChildren()) {
                    flatList.add(child);
                    if (child.getChildren() != null) {
                        flatList.addAll(child.getChildren());
                    }
                }
            }
        }
        return flatList;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Lấy danh sách sản phẩm
        List<ProductObject> products = productDAO.getAllProducts();
        List<ProductObject> deletedProducts = productDAO.getDeletedProducts();

        // ĐÃ THÊM: Lấy danh sách danh mục
        List<CategoryObject> categories = getFlatCategoryList();

        // Nạp dữ liệu vào Request
        request.setAttribute("products", products);
        request.setAttribute("deletedProducts", deletedProducts);
        request.setAttribute("categories", categories); // ĐÃ THÊM: Nạp danh mục sang JSP

        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/manage-product.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}