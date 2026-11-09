package controller.web;

import dao.IProductDAO;
import dao.Impl.ProductImpl;
import model.ProductObject;
import service.ImageSearchClient;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ImageSearchController — Tìm kiếm sản phẩm bằng hình ảnh (CV phase 1).
 *
 * <p>Luồng:
 * <ol>
 *   <li>Widget gửi multipart file ảnh → {@code POST /public/image-search}</li>
 *   <li>Servlet forward ảnh sang FastAPI {@code /api/image-search} (ChatbotService)</li>
 *   <li>Nhận top-10 product_id + score, nạp ProductObject từ DB</li>
 *   <li>Lưu kết quả vào session rồi redirect sang {@code GET /public/image-search}
 *       (Post/Redirect/Get — tránh submit lại file khi refresh)</li>
 *   <li>GET render lại {@code all_product.jsp} với tiêu đề "TÌM KIẾM BẰNG HÌNH ẢNH"</li>
 * </ol>
 */
@WebServlet("/public/image-search")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 8L * 1024 * 1024,
        maxRequestSize = 10L * 1024 * 1024
)
public class ImageSearchController extends HttpServlet {

    private static final int TOP_K = 10;
    private static final String SESSION_RESULTS = "imageSearchResults";
    private static final String SESSION_FILE_NAME = "imageSearchFileName";
    private static final String SESSION_ERROR = "imageSearchError";
    private static final String TITLE = "TÌM KIẾM BẰNG HÌNH ẢNH";

    private final IProductDAO productDAO = new ProductImpl();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(true);

        // Xoá kết quả/ lỗi của lần tìm trước
        session.removeAttribute(SESSION_RESULTS);
        session.removeAttribute(SESSION_FILE_NAME);
        session.removeAttribute(SESSION_ERROR);

        try {
            Part part = request.getPart("image");
            if (part == null || part.getSize() == 0) {
                session.setAttribute(SESSION_ERROR, "Bạn chưa chọn ảnh. Vui lòng chọn 1 file hình ảnh.");
                response.sendRedirect(request.getContextPath() + "/public/image-search");
                return;
            }

            String contentType = part.getContentType();
            if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
                session.setAttribute(SESSION_ERROR, "File bạn chọn không phải hình ảnh. Chỉ hỗ trợ JPG, PNG, WEBP.");
                response.sendRedirect(request.getContextPath() + "/public/image-search");
                return;
            }

            byte[] imageBytes = readPart(part);
            if (imageBytes.length == 0) {
                session.setAttribute(SESSION_ERROR, "File ảnh rỗng. Vui lòng chọn ảnh khác.");
                response.sendRedirect(request.getContextPath() + "/public/image-search");
                return;
            }

            ImageSearchClient client = new ImageSearchClient();
            List<ImageSearchClient.Match> matches =
                    client.search(imageBytes, part.getSubmittedFileName(), contentType, TOP_K);

            List<ProductObject> products = new ArrayList<>();
            for (ImageSearchClient.Match m : matches) {
                ProductObject p = productDAO.getProductById(m.productId);
                if (p != null && products.size() < TOP_K) {
                    products.add(p);
                }
            }

            if (products.isEmpty()) {
                session.setAttribute(SESSION_ERROR,
                        "Chưa tìm thấy sản phẩm tương đồng. Bạn thử ảnh rõ hơn (nền sạch, đủ sáng) nhé.");
            } else {
                session.setAttribute(SESSION_RESULTS, products);
                String fileName = part.getSubmittedFileName();
                session.setAttribute(SESSION_FILE_NAME, fileName == null ? "ảnh đã tải lên" : fileName);
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute(SESSION_ERROR,
                    "Hệ thống tìm kiếm bằng hình ảnh đang bận. Vui lòng thử lại sau ít phút.");
        }

        // Post/Redirect/Get
        response.sendRedirect(request.getContextPath() + "/public/image-search");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);

        String error = null;
        List<ProductObject> results = null;
        String fileName = null;
        if (session != null) {
            error = (String) session.getAttribute(SESSION_ERROR);
            results = (List<ProductObject>) session.getAttribute(SESSION_RESULTS);
            fileName = (String) session.getAttribute(SESSION_FILE_NAME);
        }

        if (results == null) {
            // Chưa có phiên tìm kiếm → về trang chủ
            if (error == null) {
                response.sendRedirect(request.getContextPath() + "/public/trang-chu");
                return;
            }
            results = new ArrayList<>();
        }

        // Áp dụng bộ lọc phụ trên trang kết quả (Màu sắc / Mức giá / Mức chiết khấu)
        List<ProductObject> filtered = applyFilters(results, request);

        request.setAttribute("products", filtered);
        request.setAttribute("categoryName", TITLE);
        request.setAttribute("currentPage", 1);
        request.setAttribute("totalPages", 1);
        request.setAttribute("colorList", productDAO.getColorsByCategoryId(0));
        request.setAttribute("imageSearchFileName", fileName);
        request.setAttribute("imageSearchError", error);
        request.setAttribute("imageSearchTotal", filtered.size());

        request.getRequestDispatcher("/views/web/all_product.jsp").forward(request, response);
    }

    private List<ProductObject> applyFilters(List<ProductObject> products, HttpServletRequest request) {
        String color = request.getParameter("color");
        String minStr = request.getParameter("minPrice");
        String maxStr = request.getParameter("maxPrice");
        String discountType = request.getParameter("discountType");

        double minPrice = parseDouble(minStr, -1);
        double maxPrice = parseDouble(maxStr, -1);

        List<ProductObject> out = new ArrayList<>();
        for (ProductObject p : products) {
            if (color != null && !color.trim().isEmpty()) {
                String pc = p.getProductColor() == null ? "" : p.getProductColor();
                if (!pc.toLowerCase().contains(color.trim().toLowerCase())) continue;
            }

            double effective = p.getProductPrice() * (100 - p.getDiscountPercent()) / 100.0;
            if (minPrice >= 0 && effective < minPrice) continue;
            if (maxPrice >= 0 && effective > maxPrice) continue;

            if (discountType != null && !discountType.isEmpty()) {
                int d = p.getDiscountPercent();
                switch (discountType) {
                    case "1" -> { if (!(d > 0 && d < 30)) continue; }
                    case "2" -> { if (!(d >= 30 && d <= 50)) continue; }
                    case "3" -> { if (!(d > 50)) continue; }
                    default -> { }
                }
            }
            out.add(p);
        }
        return out;
    }

    private static double parseDouble(String raw, double fallback) {
        if (raw == null || raw.trim().isEmpty()) return fallback;
        try {
            return Double.parseDouble(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static byte[] readPart(Part part) throws IOException {
        try (InputStream is = part.getInputStream();
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                bos.write(buf, 0, n);
            }
            return bos.toByteArray();
        }
    }
}
