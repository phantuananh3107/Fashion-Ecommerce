package controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import dao.IProductDAO;
import dao.Impl.ProductImpl;
import dao.CategoryDAO;
import dao.Impl.CategoryDAOImpl;
import model.ProductObject;
import model.CategoryObject;

@WebServlet("/admin/admin-add-product")
public class addProduct extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private IProductDAO productDAO;
    private CategoryDAO categoryDAO; // Thêm DAO của Category

    public void init() {
        productDAO = new ProductImpl();
        categoryDAO = new CategoryDAOImpl();
    }

    // Hàm đệ quy nhỏ để làm phẳng cây Menu thành 1 list dài cho thẻ <select> dễ đọc
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Lấy danh sách danh mục cực nhanh từ CategoryDAO
        List<CategoryObject> categories = getFlatCategoryList();

        request.setAttribute("categories", categories);
        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/add-product.jsp");
        rd.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (ServletFileUpload.isMultipartContent(request)) {
            ProductObject product = new ProductObject();
            StringBuilder errors = new StringBuilder();

            // Biến đếm để xử lý mảng nhiều ảnh
            int imageCount = 1;

            try {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);
                List<FileItem> items = upload.parseRequest(request);

                for (FileItem item : items) {
                    if (item.isFormField()) {
                        String fieldName = item.getFieldName();
                        String fieldValue = item.getString("UTF-8");
                        switch (fieldName) {
                            case "productName":
                                product.setProductName(fieldValue);
                                break;
                            case "productCode":
                                product.setProductCode(fieldValue);
                                break;
                            case "productPrice":
                                try {
                                    product.setProductPrice(Double.parseDouble(fieldValue));
                                } catch (NumberFormatException e) {
                                    product.setProductPrice(-1); // Gán âm để tí nữa validation bắt lỗi
                                }
                                break;
                            case "categoryId":
                                try {
                                    product.setCategoryId(Integer.parseInt(fieldValue));
                                } catch (NumberFormatException e) {
                                    product.setCategoryId(0);
                                }
                                break;
                            case "productColor":
                                product.setProductColor(fieldValue);
                                break;
                            case "productQuantity":
                                try {
                                    product.setProductQuantity(Integer.parseInt(fieldValue));
                                } catch (NumberFormatException e) {
                                    product.setProductQuantity(-1);
                                }
                                break;
                            case "productDescription":
                                product.setProductDescription(fieldValue);
                                break;
                            // Bỏ qua lấy "productSize" từ Form vì ta sẽ fix cứng bên dưới
                        }
                    } else {
                        // Xử lý bóc tách 4 ảnh từ input multiple
                        if (item.getFieldName().equals("productImage") && item.getSize() > 0) {
                            String fileName = new File(item.getName()).getName();
                            String uploadPath = getServletContext().getRealPath("") + File.separator + "templates" + File.separator + "admin" + File.separator + "img";
                            File uploadDir = new File(uploadPath);
                            if (!uploadDir.exists()) {
                                uploadDir.mkdirs();
                            }

                            String imagePath = "/templates/admin/img/" + fileName;
                            File storeFile = new File(uploadPath + File.separator + fileName);
                            item.write(storeFile);

                            // Phân bổ lần lượt các file ảnh vào đúng cột trong DB
                            if (imageCount == 1) {
                                product.setProductImage1(imagePath);
                            } else if (imageCount == 2) {
                                product.setProductImage2(imagePath);
                            } else if (imageCount == 3) {
                                product.setProductImage3(imagePath);
                            } else if (imageCount == 4) {
                                product.setProductImage4(imagePath);
                            }
                            imageCount++; // Tăng biến đếm cho ảnh tiếp theo
                        }
                    }
                }

                // [LOGIC QUAN TRỌNG]: Mặc định đủ 5 size cho mọi sản phẩm TA shop
                product.setProductSize("S, M, L, XL, XXL");

                // --- KIỂM TRA LỖI (SERVER-SIDE VALIDATION) ---
                if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
                    errors.append("Tên sản phẩm không được để trống.\n");
                    request.setAttribute("errorProductName", "Tên sản phẩm không được để trống");
                }
                if (product.getProductCode() == null || product.getProductCode().trim().isEmpty()) {
                    errors.append("Mã sản phẩm không được để trống.\n");
                    request.setAttribute("errorProductCode", "Mã sản phẩm không được để trống");
                }
                if (product.getProductPrice() <= 0) {
                    errors.append("Giá sản phẩm phải là số dương.\n");
                    request.setAttribute("errorProductPrice", "Giá sản phẩm phải hợp lệ và lớn hơn 0");
                }
                if (product.getCategoryId() <= 0) {
                    errors.append("Danh mục không được để trống.\n");
                    request.setAttribute("errorProductCategory", "Vui lòng chọn danh mục");
                }
                if (product.getProductColor() == null || product.getProductColor().trim().isEmpty()) {
                    errors.append("Màu sắc không được để trống.\n");
                    request.setAttribute("errorProductColor", "Màu sắc không được để trống");
                }
                if (product.getProductQuantity() < 0) {
                    errors.append("Số lượng phải là số không âm.\n");
                    request.setAttribute("errorProductQuantity", "Số lượng phải là số lớn hơn hoặc bằng 0");
                }
                if (product.getProductDescription() == null || product.getProductDescription().trim().isEmpty()) {
                    errors.append("Mô tả sản phẩm không được để trống.\n");
                    request.setAttribute("errorProductDescription", "Mô tả sản phẩm không được để trống");
                }
                if (product.getProductImage1() == null) {
                    errors.append("Ảnh chính không được để trống.\n");
                    request.setAttribute("errorProductImage", "Vui lòng chọn ít nhất 1 ảnh (tối đa 4 ảnh)");
                }

                // Nếu có bất kỳ lỗi nào, trả lại thông tin đã nhập ra màn hình (để người dùng không phải nhập lại từ đầu)
                if (errors.length() > 0) {
                    request.setAttribute("errorMessage", errors.toString());
                    request.setAttribute("productName", product.getProductName());
                    request.setAttribute("productCode", product.getProductCode());
                    request.setAttribute("productPrice", product.getProductPrice() > 0 ? product.getProductPrice() : "");
                    request.setAttribute("categoryId", product.getCategoryId());
                    request.setAttribute("productSize", product.getProductSize()); // Truyền ra cho ô readonly
                    request.setAttribute("productColor", product.getProductColor());
                    request.setAttribute("productQuantity", product.getProductQuantity() >= 0 ? product.getProductQuantity() : "");
                    request.setAttribute("productDescription", product.getProductDescription());

                    // Nạp lại danh sách Category cho ô Select Option
                    request.setAttribute("categories", getFlatCategoryList());

                    RequestDispatcher rd = request.getRequestDispatcher("/views/admin/add-product.jsp");
                    rd.forward(request, response);
                    return;
                }

                // --- NẾU DỮ LIỆU ĐÚNG CHUẨN -> LƯU VÀO DATABASE ---
                boolean success = productDAO.insertProduct(product);
                if (success) {
                    request.getSession().setAttribute("message", "Thêm sản phẩm mới thành công!");
                    response.sendRedirect(request.getContextPath() + "/admin/admin-manage-product");
                } else {
                    throw new Exception("Lỗi hệ thống khi lưu xuống Database.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                request.setAttribute("errorMessage", "Lỗi khi thêm sản phẩm: " + e.getMessage());
                request.setAttribute("categories", getFlatCategoryList());
                RequestDispatcher rd = request.getRequestDispatcher("/views/admin/add-product.jsp");
                rd.forward(request, response);
            }
        }
    }
}