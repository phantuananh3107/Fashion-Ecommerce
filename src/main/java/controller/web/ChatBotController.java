package controller.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import service.ChatBotService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * ChatBotController — Nhận tin nhắn từ widget và trả lời.
 *
 * <p>Chấp nhận cả 2 kiểu request (không sửa chatbot.js):
 * <ul>
 *   <li>Form cổ điển: <code>POST message=...&lt;form-urlencoded&gt;</code></li>
 *   <li>JSON: <code>POST {"message":"..."}</code></li>
 * </ul>
 * Controller đọc {@code userId} từ session (đã đăng nhập) và {@code JSESSIONID}
 * rồi truyền cho {@link service.ChatBotService#getChatResponse(String, Integer, String)}
 * để FastAPI ghi đúng {@code user_behavior_log} (khách vãng lai dùng {@code sessionId}).
 *
 * <p>Response giữ nguyên hợp đồng cũ {@code {"success", "message", "timestamp"}}
 * nên {@code chatbot.js} không cần sửa.
 */
@WebServlet(urlPatterns = "/chatbot")
public class ChatBotController extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Đặt charset UTF-8 để xử lý tiếng Việt
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        try {
            // 1. Lấy tin nhắn — hỗ trợ cả form (chatbot.js) lẫn JSON
            String userMessage = request.getParameter("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                userMessage = readJsonMessage(request);
            }

            // Kiểm tra tin nhắn không được rỗng
            if (userMessage == null || userMessage.trim().isEmpty()) {
                sendJsonResponse(response, false, "Vui lòng nhập tin nhắn");
                return;
            }

            // 1.1. Thu thập định danh cho FastAPI (cá nhân hoá + log hội thoại)
            //     Dùng đúng key session mà LoginController đặt: "userId" (Integer).
            //     session.getId() là session id chuẩn, khớp cột user_behavior_log.session_id.
            Integer userId = null;
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object uidAttr = session.getAttribute("userId");
                if (uidAttr instanceof Integer && (Integer) uidAttr > 0) {
                    userId = (Integer) uidAttr;
                }
            }
            String sessionId = session != null ? session.getId() : request.getRequestedSessionId();

            // 2. Gọi chatbot 3 tầng (Rule → JEV → Qwen, qua ChatBotClient → FastAPI)
            String botResponse = ChatBotService.getChatResponse(userMessage, userId, sessionId);

            // 3. Trả về JSON response (hợp đồng cũ, không sửa chatbot.js)
            sendJsonResponse(response, true, botResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "❌ Lỗi server: " + e.getMessage());
        }
    }

    /**
     * Đọc message từ body JSON khi request không phải form. Trả null nếu không phải JSON.
     */
    private static String readJsonMessage(HttpServletRequest request) {
        try {
            String ct = request.getContentType();
            if (ct == null || !ct.contains("json")) return null;
            JsonNode node = MAPPER.readTree(request.getInputStream());
            if (node.hasNonNull("message")) return node.get("message").asText();
            if (node.hasNonNull("text")) return node.get("text").asText();
        } catch (Exception e) {
            // Không phải JSON hợp lệ — bỏ qua, Controller sẽ trả lỗi rỗng
        }
        return null;
    }

    /**
     * Helper method - Gửi JSON response về client
     * Dùng cách thủ công xây dựng JSON string để tránh phụ thuộc quá nhiều vào thư viện
     */
    private void sendJsonResponse(HttpServletResponse response, boolean success, String message)
            throws IOException {
        // Escape JSON string để tránh lỗi
        String escapedMessage = escapeJsonString(message);

        // Xây dựng JSON response theo cách thủ công
        String jsonResponse = String.format(
            "{\"success\":%b,\"message\":\"%s\",\"timestamp\":%d}",
            success,
            escapedMessage,
            System.currentTimeMillis()
        );

        response.getWriter().write(jsonResponse);
    }

    /**
     * Escape JSON string để tránh lỗi khi có ký tự đặc biệt
     */
    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
}

