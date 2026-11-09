package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ImageSearchClient — Gửi ảnh query tới FastAPI /api/image-search (CV truyền thống).
 *
 * <p>FastAPI nhận multipart/form-data (field "file" + "top_k") và trả về:
 * <pre>
 * {
 *   "matches": [{"product_id": 1, "score": 0.9812}, ...],
 *   "total": 10,
 *   "top1_score": 0.9812,
 *   "latency_ms": 231.4,
 *   "provider": "cv"
 * }
 * </pre>
 */
public class ImageSearchClient {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String apiUrl;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    public ImageSearchClient() {
        // Dùng lại điểm cấu hình duy nhất của hệ chatbot: CHATBOT_API_URL
        this(util.ChatBotConfig.getChatApiUrl().replaceAll("/api/chat$", "/api/image-search"),
             util.ChatBotConfig.CHATBOT_CONNECT_TIMEOUT_MS,
             30000);
    }

    public ImageSearchClient(String apiUrl, int connectTimeoutMs, int readTimeoutMs) {
        this.apiUrl = apiUrl;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    /** Kết quả 1 match từ image search. */
    public static class Match {
        public int productId;
        public double score;
    }

    /**
     * Tìm top-k sản phẩm tương đồng với ảnh.
     *
     * @param imageBytes  nội dung file ảnh (jpg/png/webp)
     * @param filename    tên file gốc (để gửi trong multipart)
     * @param contentType MIME type của ảnh
     * @param topK        số kết quả mong muốn (FastAPI giới hạn 1..50)
     * @return danh sách match, sắp xếp giảm dần theo score
     * @throws IOException khi FastAPI không phản hồi / trả lỗi HTTP
     */
    public List<Match> search(byte[] imageBytes, String filename, String contentType, int topK)
            throws IOException {
        String boundary = "----ImageSearchBoundary" + UUID.randomUUID().toString().replace("-", "");

        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setConnectTimeout(connectTimeoutMs);
        conn.setReadTimeout(readTimeoutMs);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Accept", "application/json");

        String safeName = (filename == null || filename.isBlank()) ? "query.jpg" : filename;
        String safeType = (contentType == null || !contentType.startsWith("image/")) ? "image/jpeg" : contentType;

        try (OutputStream os = conn.getOutputStream()) {
            // field top_k
            os.write(("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"top_k\"\r\n\r\n"
                    + topK + "\r\n").getBytes(StandardCharsets.UTF_8));
            // field file
            os.write(("--" + boundary + "\r\n"
                    + "Content-Disposition: form-data; name=\"file\"; filename=\"" + safeName + "\"\r\n"
                    + "Content-Type: " + safeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
            os.write(imageBytes);
            os.write(("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            os.flush();
        }

        int status = conn.getResponseCode();
        if (status != 200) {
            String err = readAll(conn.getErrorStream());
            conn.disconnect();
            throw new IOException("FastAPI /api/image-search trả về HTTP " + status + ": " + err);
        }

        try (InputStream is = conn.getInputStream()) {
            JsonNode root = MAPPER.readTree(is);
            List<Match> matches = new ArrayList<>();
            JsonNode arr = root.get("matches");
            if (arr != null && arr.isArray()) {
                for (JsonNode m : arr) {
                    Match match = new Match();
                    match.productId = m.get("product_id").asInt();
                    match.score = m.get("score").asDouble();
                    matches.add(match);
                }
            }
            return matches;
        } finally {
            conn.disconnect();
        }
    }

    /** FastAPI image search còn sống không (dùng cho fallback / báo lỗi sớm). */
    public boolean isAvailable() {
        HttpURLConnection conn = null;
        try {
            String healthUrl = util.ChatBotConfig.getChatApiUrl().replaceAll("/api/chat$", "/health");
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
}
