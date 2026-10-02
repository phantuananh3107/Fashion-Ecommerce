package controller.web;

import service.ChatBotService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ChatBotController - Xử lý AJAX requests từ chatbot widget
 * Nhận tin nhắn từ client, gửi đến Service, trả về JSON response
 */
@WebServlet(urlPatterns = "/chatbot")
public class ChatBotController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Đặt charset UTF-8 để xử lý tiếng Việt
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        try {
            // 1. Lấy tin nhắn từ request
            String userMessage = request.getParameter("message");

            // Kiểm tra tin nhắn không được rỗng
            if (userMessage == null || userMessage.trim().isEmpty()) {
                sendJsonResponse(response, false, "Vui lòng nhập tin nhắn");
                return;
            }

            // 2. Gọi ChatBotService để lấy response từ OpenAI
            String botResponse = ChatBotService.getChatResponse(userMessage);

            // 3. Trả về JSON response
            sendJsonResponse(response, true, botResponse);

        } catch (Exception e) {
            e.printStackTrace();
            sendJsonResponse(response, false, "❌ Lỗi server: " + e.getMessage());
        }
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

