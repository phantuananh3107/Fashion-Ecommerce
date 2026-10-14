package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * ChatBotClient — Cầu nối HTTP tới FastAPI chatbot local (kiến trúc 3 tầng).
 *
 * <p>Kiến trúc 3 tầng nằm hẳn ở phía Python, không phải ở đây:
 * <ul>
 *   <li>Tầng 1 — Rule matcher (keyword/regex, ~1ms)</li>
 *   <li>Tầng 2 — JEV embedding (multilingual-e5-small, ~45-89ms)</li>
 *   <li>Tầng 3 — LLM tool-calling (Qwen2.5 7B qua Ollama)</li>
 * </ul>
 * Lớp này chỉ đóng vai trò transport: đóng gói JSON, gửi POST, parse kết quả.
 *
 * <p>FastAPI trả về:
 * <pre>
 * {
 *   "reply": "...",          // câu trả lời cho khách
 *   "intent": "find_product",
 *   "route": "tier1",        // tier1 | tier2 | tier3
 *   "tier": 1,
 *   "tool_called": "search_products",
 *   "entities": {...},
 *   "ask_missing": null,
 *   "latency_ms": {"tier1": 1.2, "tier2": null, "total": 3.4}
 * }
 * </pre>
 */
public class ChatBotClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String apiUrl;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public ChatBotClient() {
        this(util.ChatBotConfig.CHAT_API_URL,
             util.ChatBotConfig.CHATBOT_CONNECT_TIMEOUT_MS,
             util.ChatBotConfig.CHATBOT_READ_TIMEOUT_MS);
    }

    public ChatBotClient(String apiUrl, int connectTimeoutMs, int readTimeoutMs) {
        this.apiUrl = apiUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    /**
     * Gửi câu hỏi của khách tới chatbot 3 tầng.
     *
     * @param userMessage câu hỏi (bắt buộc, đã trim)
     * @param userId      id người dùng đã đăng nhập, null nếu khách vãng lai
     * @param sessionId   id phiên chat, dùng cho gợi ý theo hành vi khi chưa đăng nhập
     * @return kết quả từ FastAPI, hoặc null nếu service không phản hồi
     * @throws IOException khi không kết nối được tới FastAPI
     */
    public ChatResult send(String userMessage, Integer userId, String sessionId) throws IOException {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("message", userMessage);
        if (userId != null && userId > 0) payload.put("user_id", String.valueOf(userId));
        if (sessionId != null && !sessionId.isEmpty()) payload.put("session_id", sessionId);

        byte[] body = MAPPER.writeValueAsBytes(payload);

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(connectTimeoutMs);
        conn.setReadTimeout(readTimeoutMs);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
        }

        int status = conn.getResponseCode();
        if (status != 200) {
            // Đọc lỗi để log, không ném exception chi tiết ra ngoài
            String err = readAll(conn.getErrorStream());
            conn.disconnect();
            throw new IOException("FastAPI trả về HTTP " + status + ": " + err);
        }

        try (InputStream is = conn.getInputStream()) {
            JsonNode node = MAPPER.readTree(is);
            ChatResult result = new ChatResult();
            result.reply = textOrNull(node, "reply");
            result.intent = textOrNull(node, "intent");
            result.route = textOrNull(node, "route");
            result.tier = node.hasNonNull("tier") ? node.get("tier").asInt() : 0;
            result.toolCalled = textOrNull(node, "tool_called");
            result.askMissing = textOrNull(node, "ask_missing");
            if (node.hasNonNull("latency_ms")) {
                JsonNode lat = node.get("latency_ms");
                result.totalLatencyMs = lat.hasNonNull("total") ? lat.get("total").asDouble() : 0;
            }
            return result;
        } finally {
            conn.disconnect();
        }
    }

    /** Kiểm tra FastAPI còn sống không — dùng cho health check / fallback. */
    public boolean isAvailable() {
        HttpURLConnection conn = null;
        try {
            String healthUrl = apiUrl.replaceAll("/api/chat$", "/health");
            conn = (HttpURLConnection) new URL(healthUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            return conn.getResponseCode() == 200;
        } catch (Exception e) {
            return false;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String textOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }

    private static String readAll(InputStream is) {
        if (is == null) return "";
        try (InputStream in = is) {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = in.read(buf)) > 0) bos.write(buf, 0, n);
            return new String(bos.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    /** Kết quả trả về từ chatbot 3 tầng. */
    public static class ChatResult {
        public String reply;
        public String intent;
        public String route;
        public int tier;
        public String toolCalled;
        public String askMissing;
        public double totalLatencyMs;

        /** reply rỗng/null thì dùng askMissing, cuối cùng mới tới câu báo lỗi chung. */
        public String bestMessage() {
            if (reply != null && !reply.trim().isEmpty()) return reply;
            if (askMissing != null && !askMissing.trim().isEmpty()) return askMissing;
            return "Mình chưa hiểu rõ ý bạn. Bạn có thể hỏi về sản phẩm, đơn hàng hoặc chính sách ạ?";
        }
    }
}