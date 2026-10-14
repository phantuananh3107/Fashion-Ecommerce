package service;

import java.io.IOException;

/**
 * ChatBotService — Facade cho chatbot local 3 tầng (Rule → JEV → LLM).
 *
 * <p>Trước đây gọi Gemini cloud. Nay toàn bộ logic định tuyến nằm ở
 * FastAPI (chatbot-service/app/main.py). Lớp này giữ nguyên chữ ký public
 * {@link #getChatResponse(String)} để không phá vỡ {@code ChatBotController}
 * hay bất kỳ caller nào khác.
 *
 * <p>Luồng mới:
 * <ol>
 *   <li>Java gửi {@code {message, user_id, session_id}} → {@code POST /api/chat}</li>
 *   <li>FastAPI định tuyến Rule (T1) → JEV (T2) → Qwen7B (T3)</li>
 *   <li>Java nhận {@code {reply, intent, route, tier}} và trả {@code reply} về widget</li>
 * </ol>
 */
public class ChatBotService {

    private static final ChatBotClient CLIENT = new ChatBotClient();

    /**
     * Lấy câu trả lời từ chatbot local.
     *
     * @param userMessage tin nhắn của khách (UTF-8, không null)
     * @return câu trả lời hiển thị cho khách
     * @throws IOException khi FastAPI không phản hồi
     */
    public static String getChatResponse(String userMessage) throws IOException {
        return getChatResponse(userMessage, null, null);
    }

    /**
     * Lấy câu trả lời từ chatbot local, có kèm định danh để FastAPI
     * cá nhân hoá (gợi ý theo hành vi) và ghi log hội thoại.
     *
     * @param userMessage tin nhắn của khách
     * @param userId      id người dùng đã đăng nhập, null nếu khách vãng lai
     * @param sessionId   id phiên chat (dùng cho session-based khi chưa đăng nhập)
     * @return câu trả lời hiển thị cho khách
     * @throws IOException khi FastAPI không phản hồi
     */
    public static String getChatResponse(String userMessage, Integer userId, String sessionId)
            throws IOException {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "Vui lòng nhập tin nhắn";
        }

        try {
            ChatBotClient.ChatResult result = CLIENT.send(userMessage.trim(), userId, sessionId);
            if (result == null) {
                return "Hệ thống tư vấn đang bảo trì. Vui lòng thử lại sau vài phút nhé!";
            }

            // Log route để theo dõi chất lượng định tuyến 3 tầng (đáp ứng yêu cầu thầy:
            // "theo dõi performance để điều chỉnh trọng số")
            System.out.println("[ChatBot] route=" + result.route
                    + " tier=" + result.tier
                    + " intent=" + result.intent
                    + " tool=" + result.toolCalled
                    + " latency=" + result.totalLatencyMs + "ms");

            return result.bestMessage();

        } catch (IOException e) {
            System.err.println("[ChatBot] FastAPI không phản hồi: " + e.getMessage());
            // Fallback offline: trả hướng dẫn tĩnh, widget không bị trắng trang
            return "Hệ thống tư vấn đang bận. Bạn có thể: 1) Xem mục Sản phẩm mới, "
                    + "2) Tra đơn trong mục Đơn hàng, 3) Gọi hotline để được hỗ trợ trực tiếp.";
        }
    }

    /**
     * FastAPI còn sống không — Controller gọi trước khi gửi tin nhắn
     * để trả lỗi sớm thay vì chờ timeout.
     */
    public static boolean isServiceAvailable() {
        try {
            return CLIENT.isAvailable();
        } catch (Exception e) {
            return false;
        }
    }
}