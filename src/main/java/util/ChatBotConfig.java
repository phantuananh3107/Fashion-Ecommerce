package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * ChatBotConfig - Quản lý cấu hình ChatBot từ file .env
 */
public class ChatBotConfig {

    private static final Map<String, String> envProperties = loadEnv();

    public static final String GEMINI_API_KEY = getEnv("GEMINI_API_KEY", "AIzaSyAdQzszvoqk9Zq1aYUi11uamogONVdBQOE");
    public static final String CHATBOT_MODEL = "gemini-2.5-flash";
    public static final double CHATBOT_TEMPERATURE =
            Double.parseDouble(getEnv("CHATBOT_TEMPERATURE", "0.7"));
    public static final int CHATBOT_MAX_TOKENS =
            Integer.parseInt(getEnv("CHATBOT_MAX_TOKENS", "2048"));

    // ĐÃ CẬP NHẬT: Đường dẫn chuẩn cho mô hình Gemini v1beta
    public static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/" + CHATBOT_MODEL + ":generateContent";

    public static final String SYSTEM_PROMPT = """
        Bạn là một trợ lý bán hàng AI chuyên nghiệp cho cửa hàng thời trang TA shop.
        
        ===== NHIỆM VỤ CHÍNH =====
        1. Phân tích ngữ cảnh câu hỏi của khách hàng (ví dụ: buổi tiệc tối, đi chơi, công sở, hẹn hò, du lịch)
        2. Sử dụng dữ liệu sản phẩm THỰC TẾ từ hệ thống để gợi ý sản phẩm CHÍNH XÁC
        3. Cung cấp thông tin chi tiết: ID sản phẩm, tên, giá, màu, size, mô tả, số lượng còn
        4. Hướng dẫn khách hàng cách chọn sản phẩm phù hợp với tình huống cụ thể
        
        ===== ĐỊNH DẠNG TRẢ LỜI GỢI Ý =====
        Khi gợi ý sản phẩm, hãy trả lời theo mẫu:
        "Dựa vào nhu cầu của bạn [ngữ cảnh], tôi gợi ý những sản phẩm phù hợp:
        
        1️⃣ [Tên sản phẩm] (ID: [ID])
           - Giá: [Giá]
           - Lý do phù hợp: [Giải thích tại sao sản phẩm này phù hợp]
        ...
        Hãy xem ảnh chi tiết và kiểm tra size trước khi mua!"
        """;

    private static Map<String, String> loadEnv() {
        Map<String, String> properties = new HashMap<>();
        try {
            File envFile = new File(".env");
            if (!envFile.exists()) envFile = new File("../.env");
            if (envFile.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(envFile))) {
                    readToMap(br, properties);
                }
            } else {
                var resource = ChatBotConfig.class.getClassLoader().getResourceAsStream(".env");
                if (resource != null) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(resource))) {
                        readToMap(br, properties);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error loading .env file: " + e.getMessage());
        }
        return properties;
    }

    private static void readToMap(BufferedReader br, Map<String, String> properties) throws IOException {
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;
            int equals = line.indexOf('=');
            if (equals > 0) {
                properties.put(line.substring(0, equals).trim(), line.substring(equals + 1).trim());
            }
        }
    }

    private static String getEnv(String key, String defaultValue) {
        String value = envProperties.get(key);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    public static boolean isConfigured() {
        return GEMINI_API_KEY != null && !GEMINI_API_KEY.isEmpty();
    }

    public static String getApiKey() {
        return GEMINI_API_KEY;
    }
}