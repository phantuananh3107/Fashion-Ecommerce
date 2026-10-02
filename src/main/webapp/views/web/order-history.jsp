<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<style>
    .order-card { background: #fff; transition: all 0.3s ease; }
    .order-card:hover { transform: translateY(-2px); box-shadow: 0 4px 12px rgba(0,0,0,0.1) !important; }
    .product-img-wrapper { width: 80px; height: 100px; overflow: hidden; flex-shrink: 0; }
    .product-img-wrapper img { width: 100%; height: 100%; object-fit: cover; }
    .other-products-tag { font-size: 0.85rem; color: #888; font-weight: normal; font-style: italic; }
    .order-meta { font-size: 0.85rem; color: #666; }
</style>

<div class="container py-5">
    <h3 class="text-uppercase mb-4 fw-bold" style="letter-spacing: 1px;">Lịch sử đơn hàng</h3>

    <c:forEach var="order" items="${orderList}">
        <%-- BƯỚC 1: Lấy sản phẩm đầu tiên làm đại diện --%>
        <c:set var="firstDetail" value="${order.orderDetailList[0]}" />

        <%-- BƯỚC 2: Tính tổng số lượng (quantity_sold) của tất cả sản phẩm trong đơn --%>
        <c:set var="totalQuantity" value="0" />
        <c:forEach var="detail" items="${order.orderDetailList}">
            <c:set var="totalQuantity" value="${totalQuantity + detail.quantitySold}" />
        </c:forEach>

        <%-- BƯỚC 3: Đếm xem có bao nhiêu mẫu sản phẩm khác nhau --%>
        <c:set var="distinctItemsCount" value="${fn:length(order.orderDetailList)}" />

        <div class="order-card mb-4 border p-3 bg-white shadow-sm">
                <%-- Header: Mã đơn & Trạng thái --%>
            <div class="d-flex justify-content-between border-bottom pb-2 mb-3">
                <span class="fw-bold text-secondary">Mã đơn: #${order.orderId}</span>
                <span class="badge bg-dark px-3 py-2 text-uppercase" style="font-size: 0.7rem;">${order.orderStatus}</span>
            </div>

                <%-- Body: Ảnh & Thông tin sản phẩm --%>
            <div class="d-flex align-items-center">
                <div class="product-img-wrapper border">
                    <img src="<c:url value='${firstDetail.productObject.productImage}'/>"
                         alt="${firstDetail.productObject.productName}">
                </div>

                <div class="ms-3 flex-grow-1">
                    <h6 class="mb-1 fw-bold">
                            ${firstDetail.productObject.productName}
                            <%-- Nếu đơn có nhiều hơn 1 LOẠI sản phẩm thì hiện "... và X sản phẩm khác" --%>
                        <c:if test="${distinctItemsCount > 1}">
                            <span class="other-products-tag">
                                và ${distinctItemsCount - 1} loại sản phẩm khác
                            </span>
                        </c:if>
                    </h6>

                    <div class="order-meta">
                        <span class="d-block">Ngày đặt: ${order.orderDate}</span>
                        <span class="d-block">Tổng số lượng: <strong>${totalQuantity}</strong> sản phẩm</span>
                        <span class="d-block">Thanh toán: ${order.paymentMethod}</span>
                    </div>
                </div>

                <div class="text-end">
                    <div class="fw-bold text-danger h5 mb-0">
                        <fmt:formatNumber value="${order.totalAmount}" pattern="###,###"/>đ
                    </div>
                    <small class="text-muted d-block mt-1">${order.paymentStatus}</small>
                </div>
            </div>

                <%-- Footer: Nút bấm --%>
            <div class="text-end border-top pt-3 mt-3">
                <a href="<c:url value='/customer/order-detail?id=${order.orderId}'/>"
                   class="btn btn-outline-dark btn-sm rounded-0 px-4">CHI TIẾT</a>


            </div>
        </div>
    </c:forEach>

    <%-- Nếu danh sách trống --%>
    <c:if test="${empty orderList}">
        <div class="text-center py-5">
            <p class="fs-5 text-muted">Bạn chưa có đơn hàng nào.</p>
            <a href="<c:url value='/'/>" class="btn btn-dark rounded-0 px-5">MUA SẮM NGAY</a>
        </div>
    </c:if>
</div>