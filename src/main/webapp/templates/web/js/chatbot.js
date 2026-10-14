/**
 * chatbot.js - Logic JavaScript cho ChatBot Widget
 * Xử lý: Toggle show/hide, Gửi tin nhắn, Hiển thị response
 * Lưu lịch sử chat vào sessionStorage (Reset khi đóng trình duyệt)
 */


    // ===== BIẾN TOÀN CỤC =====
    const $chatWidget = $('#chatbot-widget');
    const $toggleBtn = $('#chatbot-toggle-btn');
    const $closeBtn = $('#chatbot-close-btn');
    const $messagesContainer = $('#chatbot-messages');
    const $input = $('#chatbot-message-input');
    const $sendBtn = $('#chatbot-send-btn');

    // Key để lưu chat history
    const CHAT_HISTORY_KEY = 'chatbot_chat_history';

    let isLoading = false;

    // ===== KHỞI ĐỘNG: Load lịch sử chat từ sessionStorage =====
    loadChatHistory();

    // ===== EVENT: Click nút Toggle (Mở/Đóng ChatBot) =====
        $chatWidget.addClass('active');
        $toggleBtn.addClass('hidden');
        $input.focus();
    });

    // ===== EVENT: Click nút Close (Đóng ChatBot) =====
        $chatWidget.removeClass('active');
        $toggleBtn.removeClass('hidden');
    });

    // ===== EVENT: Click nút Send hoặc nhấn Enter =====
    $sendBtn.on('click', sendMessage);
        if (e.which === 13) {
            sendMessage();
        }
    });

    // ===== FUNCTION: Load lịch sử chat =====
    function loadChatHistory() {
        try {
            // ĐÃ SỬA: Dùng sessionStorage thay vì localStorage
            const savedHistory = sessionStorage.getItem(CHAT_HISTORY_KEY);
            if (savedHistory) {
                const messages = JSON.parse(savedHistory);
                $messagesContainer.empty();
                    appendMessage(msg.text, msg.sender, false);
                });
            }
            console.error('Error loading chat history:', e);
        }
    }

    // ===== FUNCTION: Lưu tin nhắn =====
    function saveChatHistory() {
        try {
            const messages = [];
                const $bubble = $(this).find('.message-bubble');
                const text = $bubble.text();
                const sender = $(this).hasClass('user-message') ? 'user' : 'bot';

                if (text.trim() && text !== '...' && !text.includes('●')) {
                    messages.push({ text: text, sender: sender });
                }
            });
            // ĐÃ SỬA: Dùng sessionStorage
            sessionStorage.setItem(CHAT_HISTORY_KEY, JSON.stringify(messages));
            console.error('Error saving chat history:', e);
        }
    }

    // ===== FUNCTION: Xóa lịch sử chat =====
    function clearChatHistory() {
        try {
            // ĐÃ SỬA: Dùng sessionStorage
            sessionStorage.removeItem(CHAT_HISTORY_KEY);
            console.error('Error clearing chat history:', e);
        }
    }

    // ===== PUBLIC FUNCTION: Xóa chat history khi logout =====
        clearChatHistory();
    };

    // ===== FUNCTION: Gửi tin nhắn =====
    function sendMessage() {
        const message = $input.val().trim();

        if (!message || isLoading) {
            return;
        }

        appendMessage(message, 'user');
        saveChatHistory();
        $input.val('');
        showLoading();

        var contextPath = $('meta[name="contextPath"]').attr('content') || '/jsp-servlet';
        var rawContextPath = $('meta[name="contextPath"]').attr('content');
        var contextPath = (typeof rawContextPath !== 'undefined') ? rawContextPath : '/jsp-servlet';

        $.ajax({
            url: contextPath + '/chatbot',
            type: 'POST',
            data: { message: message },
            dataType: 'json',
            timeout: 30000,
                removeLoading();
                if (response.success) {
                    appendMessage(response.message, 'bot');
                } else {
                    appendMessage('❌ ' + response.message, 'bot');
                }
                saveChatHistory();
            },
                removeLoading();
                var errorMsg = '❌ Lỗi kết nối';
                if (status === 'timeout') {
                    errorMsg = '❌ Timeout: Vui lòng kiểm tra kết nối mạng';
                } else if (xhr.status === 404) {
                    errorMsg = '❌ Lỗi 404: Endpoint /chatbot không tìm thấy';
                } else if (xhr.status === 500) {
                    errorMsg = '❌ Lỗi 500: Server error - Kiểm tra logs';
                }
                appendMessage(errorMsg, 'bot');
            }
        });

        isLoading = true;
    }

    // ===== CÁC HÀM TIỆN ÍCH GIỮ NGUYÊN =====
    function appendMessage(text, sender) {
        const messageClass = sender === 'user' ? 'user-message' : 'bot-message';
        const messageHTML = `
            <div class="chatbot-message ${messageClass}">
                <div class="message-bubble">${escapeHtml(text)}</div>
            </div>
        `;
        $messagesContainer.append(messageHTML);
        $messagesContainer.scrollTop($messagesContainer[0].scrollHeight);
    }

    function showLoading() {
        const loadingHTML = `
            <div class="chatbot-message bot-message" id="loading-indicator">
                <div class="message-bubble loading">
                    <div class="loading-dot"></div>
                    <div class="loading-dot"></div>
                    <div class="loading-dot"></div>
                </div>
            </div>
        `;
        $messagesContainer.append(loadingHTML);
        $messagesContainer.scrollTop($messagesContainer[0].scrollHeight);
    }

    function removeLoading() {
        $('#loading-indicator').remove();
        isLoading = false;
    }

    function escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, m => map[m]);
    }
});