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

/**
 * Servlet implementation class RepairProduct
 */
@WebServlet("/admin/admin-repair-product")
public class RepairProduct extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private IProductDAO productDAO;
    private CategoryDAO categoryDAO;

    public void init() {
        productDAO = new ProductImpl();
        categoryDAO = new CategoryDAOImpl();
    }

    // Hàm đệ quy làm phẳng cây Menu để đưa vào thẻ <select>
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
        String id = request.getParameter("id");
        ProductObject product = null;
        List<CategoryObject> categories = null;

        try {
            product = productDAO.getProductById(Integer.parseInt(id));
            categories = getFlatCategoryList(); // ĐÃ SỬA: Lấy danh mục từ CategoryDAO
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Lỗi khi tải dữ liệu sản phẩm.");
        }

        request.setAttribute("product", product);
        request.setAttribute("categories", categories);
        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/repair-product.jsp");
        rd.forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        System.out.println("=== BẮT ĐẦU CHẠY VÀO HÀM CẬP NHẬT SẢN PHẨM ===");

        if (ServletFileUpload.isMultipartContent(request)) {
            ProductObject product = new ProductObject();
            StringBuilder errors = new StringBuilder();
            String[] oldImages = new String[4];
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
                            case "productId":
                                product.setProductId(Integer.parseInt(fieldValue));
                                break;
                            case "productName":
                                product.setProductName(fieldValue);
                                break;
                            case "productCode":
                                product.setProductCode(fieldValue);
                                break;
                            case "productPrice":
                                try { product.setProductPrice(Double.parseDouble(fieldValue)); }
                                catch (Exception e) { product.setProductPrice(-1); }
                                break;
                            case "categoryId":
                                try { product.setCategoryId(Integer.parseInt(fieldValue)); }
                                catch (Exception e) { product.setCategoryId(0); }
                                break;
                            case "productColor":
                                product.setProductColor(fieldValue);
                                break;
                            case "productQuantity":
                                try { product.setProductQuantity(Integer.parseInt(fieldValue)); }
                                catch (Exception e) { product.setProductQuantity(-1); }
                                break;
                            case "productDescription":
                                product.setProductDescription(fieldValue);
                                break;
                            case "oldImage1": oldImages[0] = fieldValue; break;
                            case "oldImage2": oldImages[1] = fieldValue; break;
                            case "oldImage3": oldImages[2] = fieldValue; break;
                            case "oldImage4": oldImages[3] = fieldValue; break;
                        }
                    } else {
                        if (item.getFieldName().equals("productImage") && item.getSize() > 0) {
                            String fileName = new File(item.getName()).getName();
                            String uploadPath = getServletContext().getRealPath("") + File.separator + "templates" + File.separator + "admin" + File.separator + "img";
                            File uploadDir = new File(uploadPath);
                            if (!uploadDir.exists()) uploadDir.mkdirs();

                            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                            String newImagePath = "/templates/admin/img/" + uniqueFileName;
                            item.write(new File(uploadPath + File.separator + uniqueFileName));

                            if (imageCount == 1) product.setProductImage1(newImagePath);
                            else if (imageCount == 2) product.setProductImage2(newImagePath);
                            else if (imageCount == 3) product.setProductImage3(newImagePath);
                            else if (imageCount == 4) product.setProductImage4(newImagePath);

                            imageCount++;
                        }
                    }
                }

                // Gộp ảnh cũ nếu không upload ảnh mới
                if (product.getProductImage1() == null && oldImages[0] != null) product.setProductImage1(oldImages[0]);
                if (product.getProductImage2() == null && oldImages[1] != null) product.setProductImage2(oldImages[1]);
                if (product.getProductImage3() == null && oldImages[2] != null) product.setProductImage3(oldImages[2]);
                if (product.getProductImage4() == null && oldImages[3] != null) product.setProductImage4(oldImages[3]);

                product.setProductSize("S, M, L, XL, XXL");

                // Bắt lỗi
                if (product.getProductName() == null || product.getProductName().trim().isEmpty()) errors.append("Tên sản phẩm trống.\n");
                if (product.getProductPrice() <= 0) errors.append("Giá sản phẩm không hợp lệ.\n");

                System.out.println("Số lượng lỗi validation: " + errors.length());

                if (errors.length() > 0) {
                    request.setAttribute("error", errors.toString());
                    request.setAttribute("product", product);
                    request.setAttribute("categories", getFlatCategoryList());
                    request.getRequestDispatcher("/views/admin/repair-product.jsp").forward(request, response);
                    return;
                }

                System.out.println(">>> CHUẨN BỊ LƯU VÀO DATABASE CHO SẢN PHẨM ID: " + product.getProductId());
                boolean success = productDAO.updateProduct(product);

                if (success) {
                    System.out.println(">>> LƯU THÀNH CÔNG, ĐANG CHUYỂN TRANG...");
                    request.getSession().setAttribute("message", "Cập nhật sản phẩm thành công!");
                    response.sendRedirect(request.getContextPath() + "/admin/admin-manage-product");
                } else {
                    throw new Exception("Lỗi Update DAO trả về false.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(">>> LỖI EXCEPTION: " + e.getMessage());
                request.setAttribute("error", "Lỗi hệ thống: " + e.getMessage());
                request.setAttribute("categories", getFlatCategoryList());
                request.getRequestDispatcher("/views/admin/repair-product.jsp").forward(request, response);
            }
        } else {
            System.out.println(">>> LỖI: Form không phải định dạng Multipart!");
        }
    }
}