$(document).ready(function () {

    const $chatWidget = $('#chatbot-widget');
    const $toggleBtn = $('#chatbot-toggle-btn');
    const $closeBtn = $('#chatbot-close-btn');
    const $messagesContainer = $('#chatbot-messages');
    const $input = $('#chatbot-message-input');
    const $sendBtn = $('#chatbot-send-btn');

    const CHAT_HISTORY_KEY = 'chatbot_chat_history';

    let isLoading = false;

    loadChatHistory();

    $toggleBtn.on('click', function () {
        $chatWidget.addClass('active');
        $toggleBtn.addClass('hidden');
        $input.focus();
    });

    $closeBtn.on('click', function () {
        $chatWidget.removeClass('active');
        $toggleBtn.removeClass('hidden');
    });

    $sendBtn.on('click', sendMessage);
    $input.on('keypress', function (e) {
        if (e.which === 13) {
            sendMessage();
        }
    });

    function loadChatHistory() {
        try {
            const savedHistory = sessionStorage.getItem(CHAT_HISTORY_KEY);
            if (savedHistory) {
                const messages = JSON.parse(savedHistory);
                $messagesContainer.empty();
                messages.forEach(function (msg) {
                    appendMessageHtml(msg.html, msg.sender);
                });
            }
        } catch (e) {
            console.error('Error loading chat history:', e);
        }
    }

    function saveChatHistory() {
        try {
            const messages = [];
            $messagesContainer.find('.chatbot-message').each(function () {
                const $bubble = $(this).find('.message-bubble');
                const html = $bubble.html();
                const sender = $(this).hasClass('user-message') ? 'user' : 'bot';

                if (html && html.trim() && html !== '...' && !html.includes('●')) {
                    messages.push({ html: html, sender: sender });
                }
            });
            sessionStorage.setItem(CHAT_HISTORY_KEY, JSON.stringify(messages));
        } catch (e) {
            console.error('Error saving chat history:', e);
        }
    }

    function clearChatHistory() {
        try {
            sessionStorage.removeItem(CHAT_HISTORY_KEY);
        } catch (e) {
            console.error('Error clearing chat history:', e);
        }
    }

    window.clearChatHistoryOnLogout = function () {
        clearChatHistory();
    };

    function sendMessage() {
        const message = $input.val().trim();

        if (!message || isLoading) {
            return;
        }

        appendMessage(message, 'user');
        saveChatHistory();
        $input.val('');
        showLoading();

        var rawContextPath = $('meta[name="contextPath"]').attr('content');
        var contextPath = (typeof rawContextPath !== 'undefined') ? rawContextPath : '/jsp-servlet';

        $.ajax({
            url: contextPath + '/chatbot',
            type: 'POST',
            data: { message: message },
            dataType: 'json',
            timeout: 30000,
            success: function (response) {
                removeLoading();
                if (response.success) {
                    appendMessage(response.message, 'bot');
                } else {
                    appendMessage('❌ ' + response.message, 'bot');
                }
                saveChatHistory();
            },
            error: function (xhr, status, error) {
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

    function appendMessage(text, sender) {
        const displayText = sender === 'user' ? escapeHtml(text) : text.replace(/\n/g, '<br>');
        appendMessageHtml(displayText, sender);
    }

    function appendMessageHtml(html, sender) {
        const messageClass = sender === 'user' ? 'user-message' : 'bot-message';
        const messageHTML = `
            <div class="chatbot-message ${messageClass}">
                <div class="message-bubble">${html}</div>
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