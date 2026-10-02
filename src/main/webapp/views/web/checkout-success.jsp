<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>


<div class="success-page">
    <div class="success-card">

        <i class="fas fa-check-circle icon-check"></i>

        <h2 class="success-title">Đặt hàng thành công</h2>

        <p class="success-text">Cảm ơn bạn đã tin tưởng và mua sắm tại TA shop.</p>
        <p class="success-text">Mã số đơn hàng của bạn là:
            <span class="order-id">
                ${param.orderId != null ? param.orderId : "Đã được ghi nhận"}
            </span>
        </p>
        <p class="success-text">Chúng tôi sẽ sớm liên hệ với bạn để xác nhận giao hàng.</p>

        <a href="${pageContext.request.contextPath}/public/trang-chu" class="btn-ivy">QUAY LẠI TRANG CHỦ</a>
    </div>
</div>