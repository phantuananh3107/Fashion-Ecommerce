package service;

import util.ChatBotConfig;
import dao.Impl.ProductImpl;
import dao.Impl.TopSellingImpl;
import model.TopSellingProduct;
import model.ProductObject;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * ChatBotService - Xử lý giao tiếp với Gemini API
 * Sử dụng Thuật toán: Phân tích ý định người dùng (Intent-based Search) & Lọc đa luồng (Multi-filter)
 */
public class ChatBotService {

    /**
     * 1. HÀM GIAO TIẾP VỚI API GEMINI (Giữ nguyên cấu trúc, thêm xử lý lỗi mượt hơn)
     */
    public static String getChatResponse(String userMessage) throws IOException {
        if (!ChatBotConfig.isConfigured()) {
            return "❌ Chatbot chưa được cấu hình. Vui lòng kiểm tra API Key.";
        }

        try {
            // Lấy dữ liệu sản phẩm từ DB đã qua lưới lọc
            String productContext = getProductContext(userMessage);

            // Gói vào JSON gửi đi
            String jsonBody = buildGeminiJsonRequest(userMessage, productContext);

            String apiUrl = ChatBotConfig.GEMINI_API_URL + "?key=" + ChatBotConfig.getApiKey();
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                if (responseCode == 503) {
                    return "Hệ thống AI đang quá tải cục bộ. Quý khách vui lòng hỏi lại sau vài giây nhé!";
                }
                // THÊM ĐOẠN NÀY DÀNH CHO LỖI 429
                if (responseCode == 429) {
                    return "Hệ thống tư vấn đang nhận quá nhiều câu hỏi cùng lúc. Bạn vui lòng chờ khoảng 1 phút rồi hỏi lại mình nhé!";
                }
                return "❌ Lỗi kết nối AI (" + responseCode + "). Vui lòng thử lại sau.";
            }

            return extractMessageFromGeminiJson(readStream(conn.getInputStream()));

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Đã xảy ra lỗi hệ thống khi xử lý AI, vui lòng thử lại.";
        }
    }

    /**
     * 2. HÀM TẠO NGỮ CẢNH (CONTEXT) CHO AI
     * Chức năng: Đọc Database, lọc sản phẩm và "ép" AI phải nghe theo dữ liệu này
     */
    private static String getProductContext(String userMessage) {
        try {
            ProductImpl productDAO = new ProductImpl();
            List<ProductObject> allProducts = productDAO.getAllProducts();

            if (allProducts == null || allProducts.isEmpty()) return "";

            // Gọi hàm não bộ để lọc
            List<ProductObject> matchedProducts = analyzeIntentAndFilter(userMessage.toLowerCase(), allProducts);

            StringBuilder contextBuilder = new StringBuilder();

            //  ÉP AI CHẤP NHẬN DỮ LIỆU BÁN CHẠY
            if (userMessage.toLowerCase().contains("bán chạy") || userMessage.toLowerCase().contains("hot")) {
                contextBuilder.append("\n[LỆNH HỆ THỐNG]: Danh sách dưới đây ĐÃ LÀ CÁC SẢN PHẨM BÁN CHẠY NHẤT ĐƯỢC LẤY TỪ HỆ THỐNG THỐNG KÊ DOANH THU. BẠN PHẢI DÙNG DỮ LIỆU NÀY ĐỂ TRẢ LỜI, KHÔNG ĐƯỢC NÓI LÀ KHÔNG CÓ THỐNG KÊ.\n");
            }

            contextBuilder.append("\n[DỮ LIỆU SẢN PHẨM KHỚP VỚI YÊU CẦU]:\n");

            if (matchedProducts.isEmpty()) {
                // Rút dự phòng: Lấy 5 sản phẩm mới nhất nếu không tìm ra
                matchedProducts = allProducts;
                matchedProducts.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));
                contextBuilder.append("(Không tìm thấy chính xác, dùng danh sách hàng mới nhất thay thế)\n");
            }

            // Chỉ gửi top 5 để tiết kiệm Token và tránh cắt ngang câu
            for (int i = 0; i < Math.min(5, matchedProducts.size()); i++) {
                contextBuilder.append(formatProductInfo(matchedProducts.get(i)));
            }

            return contextBuilder.toString();

        } catch (Exception e) {
            System.err.println("Error context: " + e.getMessage());
            return "";
        }
    }

    /**
     * 3. HÀM NÃO BỘ LỌC DỮ LIỆU (INTENT ANALYSIS)
     * Chức năng: Chạy qua các lớp lưới lọc dựa trên từ khóa khách gõ
     */
    private static List<ProductObject> analyzeIntentAndFilter(String msg, List<ProductObject> list) {
        List<ProductObject> result = new ArrayList<>(list);

        // 3.1. Lọc theo Loại (Đầm, Váy, Áo...)
        result = filterByCategory(msg, result);

        // 3.2. Lọc theo Giá (Dưới 1 triệu, 800k...)
        result = filterByPrice(msg, result);

        // 3.3. Lọc theo Màu sắc (Đỏ, Đen, Trắng...)
        result = filterByColor(msg, result);

        // 3.4. Lọc Sản phẩm Khuyến mãi (Sale)
        if (msg.contains("sale") || msg.contains("giảm giá") || msg.contains("khuyến mãi")) {
            result = result.stream().filter(p -> p.getDiscountPercent() > 0).collect(Collectors.toList());
        }

        // 3.5. Sắp xếp (Sort) theo Mới/Cũ/Giá/Bán chạy
        result = sortByIntent(msg, result);

        return result;
    }

    /**
     * 4. CÁC HÀM LƯỚI LỌC CHI TIẾT
     */
    private static List<ProductObject> filterByCategory(String msg, List<ProductObject> list) {
        String[] types = {"áo", "quần", "váy", "đầm", "chân váy", "sơ mi", "thun", "dạ hội", "công sở"};
        boolean hasCategoryIntent = false;

        for (String type : types) {
            if (msg.contains(type)) { hasCategoryIntent = true; break; }
        }

        if (!hasCategoryIntent) return list;

        return list.stream().filter(p -> {
            String name = p.getProductName() != null ? p.getProductName().toLowerCase() : "";
            String cat = p.getCategoryName() != null ? p.getCategoryName().toLowerCase() : "";
            String desc = p.getProductDescription() != null ? p.getProductDescription().toLowerCase() : "";

            for (String type : types) {
                if (msg.contains(type) && (name.contains(type) || cat.contains(type) || desc.contains(type))) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
    }

    private static List<ProductObject> filterByPrice(String msg, List<ProductObject> list) {
        double maxPrice = Double.MAX_VALUE;
        double minPrice = 0;

        // Xử lý giá tiền Việt Nam
        if (msg.contains("dưới 500") || msg.contains("tầm 500")) maxPrice = 550000;
        else if (msg.contains("tầm 800") || msg.contains("dưới 800")) { minPrice = 600000; maxPrice = 900000; }
        else if (msg.contains("dưới 1 triệu") || msg.contains("dưới 1 củ")) maxPrice = 1000000;
        else if (msg.contains("dưới 2 triệu")) maxPrice = 2000000;
        else if (msg.contains("trên 1 triệu")) minPrice = 1000000;

        final double min = minPrice;
        final double max = maxPrice;

        if (min == 0 && max == Double.MAX_VALUE) return list;

        return list.stream().filter(p -> {
            double finalPrice = p.getProductPrice() * (100 - p.getDiscountPercent()) / 100;
            return finalPrice >= min && finalPrice <= max;
        }).collect(Collectors.toList());
    }

    private static List<ProductObject> filterByColor(String msg, List<ProductObject> list) {
        String[] colors = {"đen", "trắng", "đỏ", "vàng", "xanh", "hồng", "tím", "cam", "nâu", "be"};
        String requestedColor = null;

        for (String c : colors) {
            if (msg.contains("màu " + c) || msg.contains(c)) { requestedColor = c; break; }
        }
        if (requestedColor == null) return list;

        final String rc = requestedColor;
        return list.stream()
                .filter(p -> p.getProductColor() != null && p.getProductColor().toLowerCase().contains(rc))
                .collect(Collectors.toList());
    }

    /**
     * 5. HÀM SẮP XẾP (SORTING)
     * Chức năng: Đưa sản phẩm phù hợp nhất lên Top 1 để AI thấy
     */
    /**
     * 5. HÀM SẮP XẾP (SORTING) - TÍCH HỢP DỮ LIỆU BÁN CHẠY TỪ ADMIN
     */
    private static List<ProductObject> sortByIntent(String msg, List<ProductObject> list) {
        if (msg.contains("bán chạy") || msg.contains("hot") || msg.contains("nhiều người mua")) {
            try {
                TopSellingImpl topDAO = new TopSellingImpl();
                // Lấy 50 sản phẩm bán chạy nhất từ Database (Bảng OrderDetail)
                List<TopSellingProduct> topSellers = topDAO.getTopSellingProducts(50, 0);

                // Khớp dữ liệu bán chạy với list sản phẩm hiện tại để lấy đủ thông tin
                List<ProductObject> realTopSellersList = new ArrayList<>();
                for (TopSellingProduct top : topSellers) {
                    for (ProductObject p : list) {
                        if (p.getProductId() == top.getProductId()) {
                            realTopSellersList.add(p);
                            break;
                        }
                    }
                }

                // Nếu lấy được danh sách bán chạy thành công thì trả về luôn
                if (!realTopSellersList.isEmpty()) {
                    return realTopSellersList;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else if (msg.contains("mới nhất") || msg.contains("vừa về") || msg.contains("mới")) {
            list.sort((p1, p2) -> {
                if(p1.getCreatedAt() == null || p2.getCreatedAt() == null) return 0;
                return p2.getCreatedAt().compareTo(p1.getCreatedAt());
            });
        }
        else if (msg.contains("cao nhất") || msg.contains("đắt nhất")) {
            list.sort((p1, p2) -> Double.compare(p2.getProductPrice(), p1.getProductPrice()));
        }
        else if (msg.contains("thấp nhất") || msg.contains("rẻ nhất")) {
            list.sort((p1, p2) -> Double.compare(p1.getProductPrice(), p2.getProductPrice()));
        }

        return list;
    }

    /**
     * 6. CÁC HÀM TIỆN ÍCH CHO AI
     */
    private static String formatProductInfo(ProductObject p) {
        StringBuilder info = new StringBuilder();
        info.append("🛍️ [Mã SP: ").append(p.getProductCode()).append("] ").append(p.getProductName());

        double originalPrice = p.getProductPrice();
        if (p.getDiscountPercent() > 0) {
            double salePrice = originalPrice * (100 - p.getDiscountPercent()) / 100;
            info.append(" | Giá SALE: ").append(String.format("%.0f VNĐ", salePrice));
        } else {
            info.append(" | Giá: ").append(String.format("%.0f VNĐ", originalPrice));
        }

        if (p.getProductColor() != null) info.append(" | Màu: ").append(p.getProductColor());
        if (p.getProductDescription() != null) {
            String desc = p.getProductDescription().replaceAll("<[^>]*>", ""); // Xóa HTML tag
            info.append(" | Mô tả: ").append(desc.length() > 100 ? desc.substring(0, 100) + "..." : desc);
        }
        info.append("\n");
        return info.toString();
    }

    private static String buildGeminiJsonRequest(String userMessage, String productContext) {
        String systemPrompt = ChatBotConfig.SYSTEM_PROMPT;

        // Lệnh BẮT BUỘC để AI không nói dài và không bị đứt đoạn
        systemPrompt += "\n[QUAN TRỌNG]: Hãy trả lời NGẮN GỌN, SÚC TÍCH. Dừng lại khi đã giới thiệu xong sản phẩm, KHÔNG viết dài dòng để tránh bị cắt ngang câu.";

        if (productContext != null && !productContext.isEmpty()) {
            systemPrompt += "\n" + productContext;
        }

        String escapedContent = escapeJson(systemPrompt + "\n\nKhách hỏi: " + userMessage);
        return "{"
                + "\"contents\": [{\"parts\": [{\"text\": \"" + escapedContent + "\"}]}],"
                + "\"generationConfig\": {"
                + "  \"temperature\": " + ChatBotConfig.CHATBOT_TEMPERATURE + ","
                + "  \"maxOutputTokens\": 2048" // ÉP CỨNG LIMIT LÊN 2048 Ở ĐÂY ĐỂ TRÁNH LỖI CẮT CHỮ
                + "}"
                + "}";
    }

    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private static String extractMessageFromGeminiJson(String jsonResponse) {
        try {
            int textKey = jsonResponse.indexOf("\"text\":");
            if (textKey == -1) return "❌ Lỗi: Không tìm thấy text trả lời";

            int start = jsonResponse.indexOf("\"", textKey + 7);
            if (start == -1) return "❌ Lỗi: Sai format JSON";

            int end = start + 1;
            while (end < jsonResponse.length()) {
                if (jsonResponse.charAt(end) == '"' && jsonResponse.charAt(end - 1) != '\\') break;
                end++;
            }

            return jsonResponse.substring(start + 1, end).replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\").trim();
        } catch (Exception e) {
            return "❌ Lỗi xử lý response AI";
        }
    }
}