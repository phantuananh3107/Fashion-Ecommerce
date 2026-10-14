package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * ChatBotConfig — Quản lý cấu hình ChatBot (kiến trúc 3 tầng local).
 *
 * Trước đây gọi Gemini cloud. Nay FastAPI local :8000 (chatbot-service/app/main.py)
 * là runtime duy nhất. Không cần API key cloud.
 *
 * Thứ tự đọc cấu hình (ưu tiên cao → thấp):
 *   1. System property -Dchatbot.*  (ví dụ: -Dchatbot.api.url=http://...).
 *   2. File .env ở working directory.
 *   3. Classpath resource ".env".
 *   4. Giá trị mặc định hard-coded dưới đây.
 *
 * Biến môi trường / .env dùng được:
 *   CHATBOT_API_URL   — URL đầy đủ tới /api/chat (ví dụ http://localhost:8000/api/chat)
 *   CHATBOT_TIMEOUT_MS — timeout đọc (ms), mặc định 65000 để tier3 kịp trả lời
 *   CHATBOT_CONNECT_MS — timeout kết nối (ms), mặc định 5000
 *   GEMINI_API_KEY    — giữ lại để tương thích .env cũ (bị bỏ qua, không đọc nữa)
 */
public class ChatBotConfig {

    private static final Map<String, String> envProperties = loadEnv();

    // FastAPI chatbot local — chỉnh trong .env nếu port khác 8000
    public static final String CHAT_API_URL = getConfig("CHATBOT_API_URL", "http://localhost:8000/api/chat");

    public static final int CHATBOT_CONNECT_TIMEOUT_MS = Integer.parseInt(
            getConfig("CHATBOT_CONNECT_MS", "5000"));
    public static final int CHATBOT_READ_TIMEOUT_MS = Integer.parseInt(
            getConfig("CHATBOT_TIMEOUT_MS", "65000"));

    // Gemini cloud đã loại bỏ. Giữ các hằng dưới đây chỉ để file .env cũ không báo lỗi
    // khi ChatBotService cũ chưa được HotSwap (tránh parse Double với key cũ)
    @Deprecated public static final String CHATBOT_MODEL = "qwen2.5:7b-instruct-q4_K_M";
    @Deprecated public static final double CHATBOT_TEMPERATURE = 0.2;
    @Deprecated public static final int CHATBOT_MAX_TOKENS = 512;

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

    /**
     * Đọc cấu hình theo thứ tự: System property → biến môi trường → .env → mặc định.
     * Dùng cho tên key không chấp nhấn dấu gạch dưới, ví dụ CHATBOT_API_URL → chatbot.api.url.
     */
    private static String getConfig(String key, String defaultValue) {
        String prop = System.getProperty(key.toLowerCase().replace('_', '.'));
        if (prop != null && !prop.isEmpty()) return prop.trim();

        String env = System.getenv(key);
        if (env != null && !env.isEmpty()) return env.trim();

        String fromFile = envProperties.get(key);
        if (fromFile != null && !fromFile.isEmpty()) return fromFile.trim();

        return defaultValue;
    }

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

    public static boolean isConfigured() {
        return CHAT_API_URL != null && !CHAT_API_URL.isEmpty();
    }

    /**
     * URL tới FastAPI /api/chat — điểm cấu hình duy nhất của toàn bộ hệ chatbot.
     */
    public static String getChatApiUrl() {
        return CHAT_API_URL;
    }
}