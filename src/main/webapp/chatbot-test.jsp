<!-- TEST PAGE - Để verify ChatBot hoạt động -->
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ChatBot Test Page - TA shop</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            padding: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
        }
        .container {
            max-width: 800px;
            margin: 0 auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 5px 20px rgba(0,0,0,0.2);
        }
        h1 {
            color: #667eea;
            text-align: center;
        }
        .info-box {
            background: #f0f0f0;
            padding: 20px;
            border-radius: 5px;
            margin: 20px 0;
            border-left: 5px solid #667eea;
        }
        .success { color: green; font-weight: bold; }
        .error { color: red; font-weight: bold; }
        .warning { color: orange; font-weight: bold; }
        code {
            background: #f5f5f5;
            padding: 2px 5px;
            border-radius: 3px;
            font-family: monospace;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🤖 TA shop ChatBot - Test Page</h1>

        <div class="info-box">
            <h2>✅ Setup Status</h2>
            <p><strong>Trang này được dùng để verify rằng ChatBot đã được install đúng.</strong></p>

            <h3>Kiểm Tra:</h3>
            <ul>
                <li>✓ File JSP này load được → Server đang chạy</li>
                <li>✓ Nếu thấy nút "TA shop ChatBot" ở góc phải → Chatbot widget loaded</li>
                <li>✓ Nếu click được vào nút → Event listener hoạt động</li>
                <li>✓ Nếu gửi tin nhắn được → AJAX endpoint /chatbot hoạt động</li>
            </ul>
        </div>

        <div class="info-box">
            <h2>🔧 Checklist</h2>
            <p>
                <span class="success">✓ ChatBotConfig.java</span> - Loads .env config<br>
                <span class="success">✓ ChatBotService.java</span> - Calls OpenAI API<br>
                <span class="success">✓ ChatBotController.java</span> - AJAX handler (@WebServlet("/chatbot"))<br>
                <span class="success">✓ chatbot.jsp</span> - UI Widget<br>
                <span class="success">✓ chatbot.js</span> - Event handling<br>
            </p>
        </div>

        <div class="info-box">
            <h2>📋 Test Steps</h2>
            <ol>
                <li>Scroll down to see if <code>TA shop ChatBot</code> button appears at bottom-right</li>
                <li>Click the button → Widget should pop up with animation</li>
                <li>Type a message in Vietnamese: <code>Xin chào</code></li>
                <li>Press Enter or click Send button</li>
                <li>Wait 2-5 seconds for AI response</li>
                <li>If you get a response → ChatBot is working! 🎉</li>
            </ol>
        </div>

        <div class="info-box">
            <h2>🔍 Troubleshooting</h2>
            <p>If ChatBot doesn't appear:</p>
            <ol>
                <li>Press F12 → Open Browser Console</li>
                <li>Check for JavaScript errors (red text)</li>
                <li>Go to Network tab → Filter by /chatbot endpoint</li>
                <li>Try sending a message and watch Network requests</li>
                <li>If you see 404 → Servlet not mapped correctly</li>
                <li>If you see 500 → Check Tomcat logs for Java exceptions</li>
            </ol>
        </div>

        <div class="info-box">
            <h2>📞 Next Steps</h2>
            <p>If everything works:</p>
            <ol>
                <li>Open http://localhost:8080/jsp-servlet/ (your main homepage)</li>
                <li>ChatBot should appear on all pages (except login)</li>
                <li>Test with different questions</li>
                <li>Monitor API costs on OpenAI dashboard</li>
            </ol>
        </div>

        <div class="info-box" style="background: #fff3cd;">
            <h2>⚠️ Important Notes</h2>
            <ul>
                <li>Make sure <code>.env</code> file exists in project root with <code>OPENAI_API_KEY</code></li>
                <li>API calls cost money - set budget alert on OpenAI account</li>
                <li>First response may take 3-5 seconds (API latency)</li>
                <li>Vietnamese text should display correctly (UTF-8)</li>
            </ul>
        </div>
    </div>

    <!-- Include ChatBot Widget -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css" />
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>

    <!-- ChatBot HTML & CSS -->
    <%@ include file="/common/web/chatbot.jsp" %>

    <!-- ChatBot JavaScript -->
    <script src="${pageContext.request.contextPath}/templates/web/js/chatbot.js"></script>
</body>
</html>
