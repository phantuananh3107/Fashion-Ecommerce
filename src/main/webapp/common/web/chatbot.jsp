<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div id="chatbot-widget" class="chatbot-widget">
    <div class="chatbot-header">
        <div class="chatbot-title">
            <i class="fas fa-robot"></i> TA shop ChatBot
        </div>
        <button id="chatbot-close-btn" class="chatbot-close-btn" title="Đóng">
            <i class="fas fa-times"></i>
        </button>
    </div>

    <div id="chatbot-messages" class="chatbot-messages">
        <div class="chatbot-message bot-message">
            <div class="message-bubble">Xin chào! Tôi là ChatBot của TA shop. Hỏi tôi bất cứ điều gì về thời trang, sản phẩm hoặc lời khuyên phối đồ!</div>
        </div>
    </div>

    <div class="chatbot-input-area">
        <input
            type="text"
            id="chatbot-message-input"
            class="chatbot-input"
            placeholder="Nhập tin nhắn..."
            autocomplete="off"
        />
        <button id="chatbot-send-btn" class="chatbot-send-btn" title="Gửi">
            <i class="fas fa-paper-plane"></i>
        </button>
    </div>
</div>

<button id="chatbot-toggle-btn" class="chatbot-toggle-btn" title="Mở ChatBot">
    <i class="fas fa-comments"></i>
    <span class="chatbot-badge">TA</span>
</button>

<style>
    .chatbot-widget {
        position: fixed;
        bottom: 100px;
        right: 20px;
        width: 400px;
        height: 600px;
        background: #fff;
        border-radius: 15px;
        box-shadow: 0 5px 40px rgba(0, 0, 0, 0.16);
        display: none;  /* ← MẶCĐỊNH ẨN, chỉ hiện khi click */
        flex-direction: column;
        z-index: 10000;
        font-family: 'Arial', sans-serif;
        animation: slideUp 0.3s ease-in-out;
    }

    .chatbot-widget.active {
        display: flex;
    }

    @keyframes slideUp {
        from {
            transform: translateY(20px);
            opacity: 0;
        }
        to {
            transform: translateY(0);
            opacity: 1;
        }
    }

    .chatbot-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px 20px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        border-radius: 15px 15px 0 0;
        box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    }

    .chatbot-title {
        font-weight: bold;
        font-size: 16px;
        display: flex;
        align-items: center;
        gap: 10px;
    }

    .chatbot-close-btn {
        background: none;
        border: none;
        color: white;
        cursor: pointer;
        font-size: 20px;
        padding: 0;
        transition: transform 0.2s;
    }

    .chatbot-close-btn:hover {
        transform: scale(1.2);
    }

    .chatbot-messages {
        flex: 1;
        padding: 20px;
        overflow-y: auto;
        background: #f5f5f5;
        display: flex;
        flex-direction: column;
        gap: 15px;
    }

    .chatbot-message {
        display: flex;
        animation: fadeIn 0.3s ease-in;
    }

    @keyframes fadeIn {
        from {
            opacity: 0;
            transform: translateY(10px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    .message-bubble {
        padding: 12px 15px;
        border-radius: 10px;
        max-width: 85%;
        word-wrap: break-word;
        white-space: pre-wrap;
        line-height: 1.4;
        font-size: 14px;
    }

    .bot-message .message-bubble a {
        color: #667eea;
        text-decoration: underline;
    }

    .user-message {
        justify-content: flex-end;
    }

    .user-message .message-bubble {
        background: #667eea;
        color: white;
    }

    .bot-message {
        justify-content: flex-start;
    }

    .bot-message .message-bubble {
        background: white;
        color: #333;
        border: 1px solid #ddd;
        box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    }

    .message-bubble.loading {
        display: flex;
        align-items: center;
        gap: 5px;
    }

    .loading-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #667eea;
        animation: bounce 1.4s infinite;
    }

    .loading-dot:nth-child(1) { animation-delay: 0s; }
    .loading-dot:nth-child(2) { animation-delay: 0.2s; }
    .loading-dot:nth-child(3) { animation-delay: 0.4s; }

    @keyframes bounce {
        0%, 80%, 100% { transform: translateY(0); opacity: 0.5; }
        40% { transform: translateY(-10px); opacity: 1; }
    }

    .chatbot-input-area {
        display: flex;
        gap: 10px;
        padding: 15px;
        background: white;
        border-radius: 0 0 15px 15px;
        border-top: 1px solid #e0e0e0;
    }

    .chatbot-input {
        flex: 1;
        padding: 10px 15px;
        border: 1px solid #ddd;
        border-radius: 25px;
        font-size: 14px;
        outline: none;
        transition: all 0.3s;
    }

    .chatbot-input:focus {
        border-color: #667eea;
        box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    .chatbot-send-btn {
        padding: 10px 15px;
        background: #667eea;
        color: white;
        border: none;
        border-radius: 50%;
        cursor: pointer;
        font-size: 16px;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        justify-content: center;
        width: 42px;
        height: 42px;
    }

    .chatbot-send-btn:hover {
        background: #764ba2;
        transform: scale(1.05);
    }

    .chatbot-send-btn:active {
        transform: scale(0.95);
    }

    .chatbot-toggle-btn {
        position: fixed;
        bottom: 30px;
        right: 30px;
        width: 60px;
        height: 60px;
        border-radius: 50%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
        border: none;
        cursor: pointer;
        font-size: 24px;
        box-shadow: 0 4px 20px rgba(102, 126, 234, 0.4);
        transition: all 0.3s;
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9999;
    }

    .chatbot-toggle-btn:hover {
        transform: scale(1.1);
        box-shadow: 0 6px 25px rgba(102, 126, 234, 0.6);
    }

    .chatbot-toggle-btn:active {
        transform: scale(0.95);
    }

    .chatbot-badge {
        position: absolute;
        top: -5px;
        right: -5px;
        background: #ff4757;
        color: white;
        border-radius: 50%;
        width: 30px;
        height: 30px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: bold;
    }

    .chatbot-toggle-btn {
        display: flex;
    }

    .chatbot-toggle-btn.hidden {
        display: none;
    }

    @media (max-width: 768px) {
        .chatbot-widget {
            width: calc(100% - 40px);
            height: 70vh;
            bottom: 80px;
            right: 20px;
            left: 20px;
        }
    }
</style>
