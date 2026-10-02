<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="checkout-page-wrapper container py-5">
    <form action="${pageContext.request.contextPath}/customer/process-checkout" method="POST">
        <div class="row">
            <div class="col-lg-7 pe-lg-5">
                <div class="checkout-block">
                    <h3 class="checkout-title">Địa chỉ giao hàng</h3>
                    <div class="row g-3">
                        <div class="col-md-6">
                            <input type="text" class="form-control ivy-input" name="fullname" placeholder="Họ và tên" value="${sessionScope.user.fullname}" required>
                        </div>
                        <div class="col-md-6">
                            <input type="text" class="form-control ivy-input" name="phone" placeholder="Số điện thoại" value="${sessionScope.user.phoneNumber}" required>
                        </div>

                        <div class="col-md-4">
                            <select class="form-select ivy-input" name="province" id="province" required>
                                <option value="" selected disabled>Tỉnh/Thành phố</option>
                                <option value="Hà Nội">Hà Nội</option>
                                <option value="TP HCM">TP. Hồ Chí Minh</option>
                                <option value="Đà Nẵng">Đà Nẵng</option>
                                <option value="Hải Phòng">Hải Phòng</option>
                                <option value="Cần Thơ">Cần Thơ</option>
                                <option value="Bình Dương">Bình Dương</option>
                                <option value="Đồng Nai">Đồng Nai</option>
                                <option value="Quảng Ninh">Quảng Ninh</option>
                                <option value="Thanh Hóa">Thanh Hóa</option>
                                <option value="Bà Rịa - Vũng Tàu">Bà Rịa - Vũng Tàu</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <select class="form-select ivy-input" name="district" id="district" required>
                                <option value="" selected disabled>Quận/Huyện</option>
                            </select>
                        </div>
                        <div class="col-md-4">
                            <select class="form-select ivy-input" name="ward" id="ward" required>
                                <option value="" selected disabled>Phường/Xã</option>
                            </select>
                        </div>

                        <div class="col-12">
                            <input type="text" class="form-control ivy-input" name="address" placeholder="Địa chỉ chi tiết (Số nhà, tên đường...)" required>
                        </div>
                        <div class="col-12">
                            <textarea class="form-control ivy-input" name="note" rows="2" placeholder="Ghi chú đơn hàng (Tùy chọn)"></textarea>
                        </div>
                    </div>
                </div>

                <div class="checkout-block mt-4">
                    <h3 class="checkout-title">Phương thức thanh toán</h3>
                    <div class="payment-methods">
                        <label class="payment-method-item">
                            <input type="radio" name="paymentMethod" value="COD" checked>
                            <span class="payment-name">Thanh toán khi nhận hàng (COD)</span>
                        </label>
                        <label class="payment-method-item">
                            <input type="radio" name="paymentMethod" value="VNPAY">
                            <span class="payment-name">Thanh toán qua VNPAY / Thẻ ATM</span>
                        </label>
                        <label class="payment-method-item">
                            <input type="radio" name="paymentMethod" value="MOMO">
                            <span class="payment-name">Thanh toán qua Ví MoMo</span>
                        </label>
                    </div>
                </div>
            </div>

            <div class="col-lg-5">
                <div class="checkout-summary-box">
                    <h3 class="summary-title">Tóm tắt đơn hàng</h3>

                    <c:set var="sum" value="0" />
                    <c:forEach var="item" items="${sessionScope.cart}">
                        <c:if test="${not empty item.productObject}">
                            <%-- ĐÃ SỬA LỖI: Tính tổng tiền lấy giá sau khi đã giảm --%>
                            <c:set var="sum" value="${sum + ((item.productObject.productPrice * (100 - item.productObject.discountPercent) / 100) * item.quantity)}" />
                        </c:if>
                    </c:forEach>

                    <div class="summary-row">
                        <span class="summary-label">Tổng tiền hàng</span>
                        <span class="summary-value"><fmt:formatNumber value="${sum}" pattern="###,###"/>đ</span>
                    </div>

                    <div class="summary-row">
                        <span class="summary-label">Tạm tính</span>
                        <span class="summary-value"><fmt:formatNumber value="${sum}" pattern="###,###"/>đ</span>
                    </div>

                    <div class="summary-row">
                        <span class="summary-label">Phí vận chuyển</span>
                        <span class="summary-value">0đ</span>
                    </div>

                    <div class="summary-row text-danger" id="discount-row" style="display: none;">
                        <span class="summary-label">Giảm giá</span>
                        <span class="summary-value" id="discount-display">-0đ</span>
                    </div>

                    <hr class="summary-divider">

                    <div class="summary-row summary-total">
                        <span class="summary-label">Tiền thanh toán</span>
                        <%-- Thuộc tính data-original-sum ở đây cũng sẽ nhận giá trị chuẩn để JS tính mã giảm giá --%>
                        <span class="summary-value fw-bold" id="final-total-display" data-original-sum="${sum}">
                            <fmt:formatNumber value="${sum}" pattern="###,###"/>đ
                        </span>
                    </div>

                    <div class="discount-box mt-4">
                        <div class="d-flex justify-content-between align-items-center mb-2">
                            <label class="fw-bold">Mã phiếu giảm giá</label>
                            <a href="#" class="text-muted text-decoration-none">Mã của tôi</a>
                        </div>
                        <div class="d-flex gap-2">
                            <input type="text" class="form-control ivy-input" id="voucher-input" name="voucher_code" placeholder="Mã giảm giá">
                            <button type="button" class="btn btn-outline-dark btn-apply">ÁP DỤNG</button>
                        </div>
                    </div>

                    <button type="submit" class="btn-checkout-large mt-4">HOÀN THÀNH</button>
                </div>
            </div>
        </div>
    </form>
</div>

