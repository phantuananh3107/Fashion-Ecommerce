<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="cart-page-wrapper container">
    <h2 class="cart-page-title">
        Giỏ hàng của bạn <span>${fn:length(sessionScope.cart)} Sản Phẩm</span>
    </h2>

    <div class="row">
        <div class="col-lg-8">
            <div id="miniCartItems" class="cart-large-items">
                <c:choose>
                    <c:when test="${empty sessionScope.cart}">
                        <ul class="mini-cart-list">
                            <li class="mini-cart-empty">Giỏ hàng trống</li>
                        </ul>
                    </c:when>
                    <c:otherwise>
                        <ul class="mini-cart-list">
                            <c:forEach var="item" items="${sessionScope.cart}">
                                <c:if test="${not empty item.productObject}">
                                    <c:set var="p" value="${item.productObject}" />

                                    <li class="cart-item d-flex align-items-center justify-content-between border-bottom py-3">

                                        <div class="d-flex align-items-center">
                                            <div class="item-image-wrapper me-3">
                                                <img src="<c:url value='${p.productImage}'/>" alt="${p.productName}" class="img-fluid item-image" />
                                            </div>

                                            <div class="item-info-left">
                                                <p class="item-name mb-1 fw-bold text-uppercase">${p.productName}</p>
                                                <p class="item-detail text-muted mb-0">
                                                    <span>Màu: ${empty p.productColor ? 'Mặc định' : p.productColor}</span> | <span>Size: ${item.productSize}</span>
                                                </p>
                                            </div>
                                        </div>

                                        <div class="item-action-right d-flex align-items-center">

                                            <div class="item-quantity-control d-flex align-items-center border rounded-pill me-4">
                                                <button class="btn-qty-down border-0 bg-transparent px-2" data-product-id="${p.productId}" data-product-size="${item.productSize}">−</button>
                                                    <%-- Đã thay span thành input --%>
                                                <input type="text" class="qty-input-manual qty-display px-2 border-start border-end" value="${item.quantity}" data-product-id="${p.productId}" data-product-size="${item.productSize}" data-product-max="${p.productQuantity}" style="width: 50px; text-align: center; border: none; font-weight: bold; background: transparent; outline: none;" />
                                                <button class="btn-qty-up border-0 bg-transparent px-2" data-product-id="${p.productId}" data-product-size="${item.productSize}" data-product-max="${p.productQuantity}">+</button>
                                            </div>

                                            <p class="item-price mb-0 fw-bold text-dark fs-5 me-4">
                                                <span class="total-price">
                                                    <c:choose>
                                                        <c:when test="${p.discountPercent > 0}">
                                                            <fmt:formatNumber value="${(p.productPrice * (100 - p.discountPercent) / 100) * item.quantity}" pattern="###,###"/>đ
                                                        </c:when>
                                                        <c:otherwise>
                                                            <fmt:formatNumber value="${p.productPrice * item.quantity}" pattern="###,###"/>đ
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </p>

                                            <button class="btn-remove-item cart-large-del border-0 bg-transparent text-secondary fs-5" data-product-id="${p.productId}" data-product-size="${item.productSize}">
                                                <i class="fa-solid fa-trash-can"></i>
                                            </button>

                                        </div>
                                    </li>

                                </c:if>
                            </c:forEach>
                        </ul>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="cart-summary-box">
                <h3 class="summary-title">Tổng tiền giỏ hàng</h3>

                <c:set var="totalQty" value="0" />
                <c:set var="sum" value="0" />
                <c:forEach var="item" items="${sessionScope.cart}">
                    <c:if test="${not empty item.productObject}">
                        <c:set var="totalQty" value="${totalQty + item.quantity}" />
                        <c:set var="sum" value="${sum + ((item.productObject.productPrice * (100 - item.productObject.discountPercent) / 100) * item.quantity)}" />
                    </c:if>
                </c:forEach>

                <div class="summary-row">
                    <span class="summary-label">Tổng sản phẩm</span>
                    <span class="summary-value">${totalQty}</span>
                </div>

                <div class="summary-row">
                    <span class="summary-label">Tổng tiền hàng</span>
                    <span class="summary-value"><fmt:formatNumber value="${sum}" pattern="###,###"/>đ</span>
                </div>

                <div class="summary-row summary-bold">
                    <span class="summary-label">Thành tiền</span>
                    <span class="summary-value"><fmt:formatNumber value="${sum}" pattern="###,###"/>đ</span>
                </div>

                <div class="summary-row summary-bold">
                    <span class="summary-label">Tạm tính</span>
                    <span class="summary-value"><fmt:formatNumber value="${sum}" pattern="###,###"/>đ</span>
                </div>

                <div class="summary-warning">
                    <i class="fa-solid fa-circle-exclamation"></i> Không thanh toán cho Shipper khi chưa nhận hàng !
                </div>

                <hr class="summary-divider">

                <a href="${pageContext.request.contextPath}/customer/checkout" class="btn-checkout-large">ĐẶT HÀNG</a>
            </div>
        </div>
    </div>
</div>