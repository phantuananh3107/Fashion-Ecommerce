<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<div class="qr-payment-page">
    <div class="qr-card">

        <c:choose>
            <%-- Đơn đã thanh toán --%>
            <c:when test="${order.paymentStatus == 'Đã thanh toán (SePay)'}">
                <div class="qr-status qr-paid" id="status-area">
                    <i class="fas fa-check-circle"></i>
                    <h2>Thanh toán thành công!</h2>
                    <p>Đơn hàng <strong>DH#${order.orderId}</strong> đã được xác nhận.</p>
                    <p>Số tiền đã thanh toán: <strong><fmt:formatNumber value="${order.totalAmount}" pattern="###,###"/>đ</strong></p>
                    <c:if test="${not empty order.paidAt}">
                        <p>Thời điểm: <fmt:formatDate value="${order.paidAt}" pattern="HH:mm:ss dd/MM/yyyy"/></p>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/customer/order-history" class="btn-ivy mt-3">XEM ĐƠN HÀNG</a>
                </div>
            </c:when>

            <%-- Đơn hết hạn --%>
            <c:when test="${order.paymentStatus == 'Hết hạn (SePay)'}">
                <div class="qr-status qr-expired" id="status-area">
                    <i class="fas fa-clock"></i>
                    <h2>Mã QR đã hết hạn</h2>
                    <p>Đơn hàng <strong>DH#${order.orderId}</strong> đã bị hủy do quá thời gian thanh toán.</p>
                    <a href="${pageContext.request.contextPath}/customer/cart" class="btn-ivy mt-3">QUAY LẠI GIỎ HÀNG / MUA LẠI</a>
                </div>
            </c:when>

            <%-- Đang chờ thanh toán --%>
            <c:otherwise>
                <h2 class="qr-title">Quét mã QR để thanh toán</h2>

                <c:if test="${not qrConfigured}">
                    <div class="qr-fallback qr-config-error">
                        Thanh toán QR hiện chưa được cấu hình đầy đủ. Vui lòng chọn phương thức khác
                        hoặc liên hệ quản trị viên.
                    </div>
                </c:if>

                <c:if test="${qrConfigured}">
                <div id="qr-payment-content">
                <div class="qr-info-row">
                    <div class="qr-image-wrap">
                        <img src="${qrUrl}" alt="Mã QR thanh toán DH#${order.orderId}"
                             id="qr-img" class="qr-img"
                             onerror="document.getElementById('qr-img').style.display='none'; document.getElementById('qr-fallback').style.display='block';">
                        <div id="qr-fallback" style="display:none; padding:12px; border:1px solid #ccc; border-radius:8px; font-size:0.85em; color:#666;">
                            Không tải được ảnh QR. Vui lòng chuyển khoản thủ công.
                        </div>
                    </div>

                    <div class="qr-details">
                        <div class="qr-detail-item">
                            <span class="qr-label">Ngân hàng</span>
                            <span class="qr-value">${bankCode}</span>
                        </div>
                        <div class="qr-detail-item">
                            <span class="qr-label">Số tài khoản</span>
                            <span class="qr-value" id="acc-number">${accountNumber}</span>
                            <button class="btn-copy" onclick="copyText('acc-number')">
                                <i class="fas fa-copy"></i> Sao chép
                            </button>
                        </div>
                        <div class="qr-detail-item">
                            <span class="qr-label">Tên tài khoản</span>
                            <span class="qr-value">${accountName}</span>
                        </div>
                        <div class="qr-detail-item">
                            <span class="qr-label">Số tiền</span>
                            <span class="qr-value qr-amount">
                                <fmt:formatNumber value="${order.totalAmount}" pattern="###,###"/>đ
                            </span>
                        </div>
                        <div class="qr-detail-item">
                            <span class="qr-label">Nội dung CK</span>
                            <span class="qr-value" id="pay-code">${paymentCode}</span>
                            <button class="btn-copy" onclick="copyText('pay-code')">
                                <i class="fas fa-copy"></i> Sao chép
                            </button>
                        </div>
                    </div>
                </div>

                <%-- Đồng hồ đếm ngược --%>
                <div class="qr-countdown">
                    <i class="fas fa-hourglass-half"></i>
                    Hết hạn sau: <span id="countdown-display">--:--</span>
                </div>

                <div class="qr-notice">
                    <i class="fas fa-info-circle"></i>
                    Sau khi chuyển khoản, hệ thống sẽ tự động xác nhận trong vài giây.
                    <strong>Không cần thao tác thêm.</strong>
                </div>

                </div>

                <%-- Thông báo trạng thái (cập nhật bởi polling) --%>
                <div id="status-area" class="qr-status-polling" style="display:none;"></div>

                <%-- Nút hủy thanh toán (Điều 6) --%>
                <form method="POST" action="${pageContext.request.contextPath}/customer/payment"
                      onsubmit="return confirm('Bạn có chắc muốn hủy thanh toán này?');"
                      class="qr-cancel-form">
                    <input type="hidden" name="action" value="cancel">
                    <input type="hidden" name="orderId" value="${order.orderId}">
                    <button type="submit" class="btn-cancel-payment">
                        <i class="fas fa-times-circle"></i> Hủy thanh toán
                    </button>
                </form>
                </c:if>
            </c:otherwise>
        </c:choose>

    </div>
</div>

<%-- CSS inline cho trang QR --%>
<style>
.qr-payment-page { display:flex; justify-content:center; padding:40px 16px; }
.qr-card {
    background:#fff;
    border-radius:16px;
    box-shadow:0 8px 32px rgba(0,0,0,.1);
    padding:36px 32px;
    max-width:640px;
    width:100%;
    text-align:center;
}
.qr-title { font-size:1.4rem; font-weight:700; margin-bottom:24px; color:#1a1a2e; }
.qr-info-row { display:flex; gap:28px; align-items:flex-start; justify-content:center; flex-wrap:wrap; text-align:left; }
.qr-img { width:200px; height:200px; border-radius:8px; border:2px solid #e0e0e0; }
.qr-details { flex:1; min-width:200px; }
.qr-detail-item { display:flex; align-items:center; gap:8px; margin-bottom:10px; flex-wrap:wrap; }
.qr-label { font-size:.8rem; color:#888; min-width:100px; }
.qr-value { font-weight:600; color:#1a1a2e; }
.qr-amount { font-size:1.1rem; color:#e74c3c; }
.btn-copy { background:none; border:1px solid #ccc; border-radius:6px; padding:2px 8px; cursor:pointer; font-size:.78rem; color:#555; transition:.2s; }
.btn-copy:hover { background:#f0f0f0; }
.qr-countdown { margin:20px 0 8px; font-size:1rem; color:#e67e22; font-weight:600; }
.qr-notice { background:#fffde7; border:1px solid #ffe082; border-radius:8px; padding:10px 14px; font-size:.85rem; color:#555; margin:12px 0 20px; }
.qr-config-error { background:#fff3cd; border:1px solid #ffecb5; border-radius:8px; padding:16px; color:#664d03; }
.qr-cancel-form { margin-top:8px; }
.btn-cancel-payment { background:none; border:2px solid #e74c3c; color:#e74c3c; border-radius:8px; padding:8px 20px; cursor:pointer; font-weight:600; transition:.2s; }
.btn-cancel-payment:hover { background:#e74c3c; color:#fff; }
.qr-status { padding:24px 16px; border-radius:12px; }
.qr-status i { font-size:2.5rem; margin-bottom:12px; }
.qr-paid { background:#e8f5e9; color:#2e7d32; }
.qr-expired { background:#fce4ec; color:#b71c1c; }
.qr-status-polling { margin:16px 0; padding:12px; border-radius:8px; font-weight:600; }
.btn-ivy { display:inline-block; background:#1a1a2e; color:#fff; padding:10px 24px; border-radius:8px; text-decoration:none; }
</style>

<%-- JS: đồng hồ đếm ngược + polling mỗi 3 giây --%>
<script>
(function(){
    const orderId   = '${order.orderId}';
    const paidAmount = Number('${order.totalAmount}');
    const expiresAt = new Date('${order.expiresAt}').getTime();
    const ctxPath   = '${pageContext.request.contextPath}';

    // Đếm ngược
    const countdownEl = document.getElementById('countdown-display');
    let countdownTimer;
    if (countdownEl && expiresAt) {
        countdownTimer = setInterval(function(){
            const diff = expiresAt - Date.now();
            if (diff <= 0) {
                clearInterval(countdownTimer);
                countdownEl.textContent = '00:00';
            } else {
                const m = Math.floor(diff / 60000);
                const s = Math.floor((diff % 60000) / 1000);
                countdownEl.textContent = (m < 10 ? '0' : '') + m + ':' + (s < 10 ? '0' : '') + s;
            }
        }, 1000);
    }

    // Polling mỗi 3 giây
    const statusArea = document.getElementById('status-area');
    const qrPaymentContent = document.getElementById('qr-payment-content');
    const cancelForm = document.querySelector('.qr-cancel-form');
    if (!statusArea || !qrPaymentContent) {
        return;
    }
    let pollTimer;
    pollTimer = setInterval(function(){
        fetch(ctxPath + '/customer/payment/status?orderId=' + orderId)
            .then(r => r.json())
            .then(data => {
                const st = data.status;
                if (st === 'Đã thanh toán (SePay)') {
                    clearInterval(pollTimer);
                    clearInterval(countdownTimer);
                    if (qrPaymentContent) qrPaymentContent.style.display = 'none';
                    if (cancelForm) cancelForm.style.display = 'none';
                    statusArea.style.display = 'block';
                    statusArea.className = 'qr-status qr-paid';
                    statusArea.innerHTML = '<i class="fas fa-check-circle"></i>'
                        + '<h2>Thanh toán thành công!</h2>'
                        + '<p>Đơn hàng <strong>DH#' + orderId + '</strong> đã được xác nhận.</p>'
                        + '<p>Số tiền đã thanh toán: <strong>' + paidAmount.toLocaleString('vi-VN') + 'đ</strong></p>'
                        + (data.paidAt ? '<p>Thời điểm: <strong>' + data.paidAt + '</strong></p>' : '')
                        + '<a href="' + ctxPath + '/customer/order-history" class="btn-ivy mt-3">XEM ĐƠN HÀNG</a>';
                } else if (st === 'Hết hạn (SePay)') {
                    clearInterval(pollTimer);
                    clearInterval(countdownTimer);
                    if (qrPaymentContent) qrPaymentContent.style.display = 'none';
                    if (cancelForm) cancelForm.style.display = 'none';
                    statusArea.style.display = 'block';
                    statusArea.className = 'qr-status qr-expired';
                    statusArea.innerHTML = '<i class="fas fa-clock"></i><br><strong>Mã QR đã hết hạn.</strong>'
                        + '<br><a href="' + ctxPath + '/customer/cart" class="btn-ivy" style="margin-top:12px;">QUAY LẠI GIỎ HÀNG / MUA LẠI</a>';
                }
            })
            .catch(function(){/* bỏ qua lỗi mạng thoáng qua */});
    }, 3000);

    // Hàm copy
    window.copyText = function(id) {
        const el = document.getElementById(id);
        if (!el) return;
        navigator.clipboard.writeText(el.textContent.trim())
            .then(function(){ alert('Đã sao chép!'); })
            .catch(function(){ alert('Trình duyệt không hỗ trợ copy tự động.'); });
    };
})();
</script>
