<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<div class="container py-5">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h3 class="text-uppercase fw-bold m-0">Chi tiết đơn hàng #${order.orderId}</h3>
    <a href="${pageContext.request.contextPath}/customer/order-history" class="text-decoration-none text-dark">
      <i class="fa-solid fa-arrow-left"></i> Quay lại lịch sử
    </a>
  </div>

  <div class="row">
    <div class="col-lg-4">
      <div class="border p-3 bg-light mb-4">
        <h6 class="fw-bold text-uppercase border-bottom pb-2 mb-3">Thông tin nhận hàng</h6>
        <p class="mb-1"><strong>Người nhận:</strong> ${order.shippingName}</p>
        <p class="mb-1"><strong>Điện thoại:</strong> ${order.shippingPhone}</p>
        <p class="mb-1"><strong>Địa chỉ:</strong> ${order.shippingAddress}</p>
        <p class="mb-1"><strong>Hình thức:</strong> ${order.paymentMethod}</p>
        <p class="mb-0"><strong>Ghi chú:</strong> ${empty order.orderNote ? 'Không có' : order.orderNote}</p>
      </div>

      <div class="border p-3 bg-white">
        <h6 class="fw-bold text-uppercase border-bottom pb-2 mb-3">Trạng thái đơn hàng</h6>
        <div class="d-flex justify-content-between mb-2">
          <span>Trạng thái:</span>
          <span class="badge bg-dark">${order.orderStatus}</span>
        </div>
        <div class="d-flex justify-content-between">
          <span>Thanh toán:</span>
          <span class="text-muted small">${order.paymentStatus}</span>
        </div>
      </div>
    </div>

    <div class="col-lg-8">
      <div class="border p-3 bg-white shadow-sm">
        <h6 class="fw-bold text-uppercase border-bottom pb-2 mb-3">Sản phẩm đã đặt</h6>

        <c:forEach var="detail" items="${order.orderDetailList}">
          <div class="d-flex align-items-center border-bottom py-3">
            <div style="width: 70px; height: 90px; overflow: hidden;" class="border">
              <img src="<c:url value='${detail.productObject.productImage}'/>"
                   class="img-fluid" style="object-fit: cover; width: 100%; height: 100%;">
            </div>
            <div class="ms-3 flex-grow-1">
              <h6 class="mb-0 fw-bold">${detail.productObject.productName}</h6>
              <small class="text-muted">Size: ${detail.productSize} | SL: ${detail.quantitySold}</small>
            </div>
            <div class="text-end">
              <div class="fw-bold">
                <fmt:formatNumber value="${detail.price}" pattern="###,###"/>đ
              </div>
              <small class="text-muted">
                Tổng: <fmt:formatNumber value="${detail.price * detail.quantitySold}" pattern="###,###"/>đ
              </small>
            </div>
          </div>
        </c:forEach>

        <div class="mt-4 pt-2">
          <div class="d-flex justify-content-between mb-2">
            <span>Tiền hàng:</span>
            <span><fmt:formatNumber value="${order.totalAmount + order.discountAmount}" pattern="###,###"/>đ</span>
          </div>
          <c:if test="${order.discountAmount > 0}">
            <div class="d-flex justify-content-between mb-2 text-danger">
              <span>Giảm giá:</span>
              <span>-<fmt:formatNumber value="${order.discountAmount}" pattern="###,###"/>đ</span>
            </div>
          </c:if>
          <div class="d-flex justify-content-between mb-2">
            <span>Phí vận chuyển:</span>
            <span class="text-success">Miễn phí</span>
          </div>
          <hr>
          <div class="d-flex justify-content-between">
            <h5 class="fw-bold">Tổng thanh toán:</h5>
            <h5 class="fw-bold text-danger">
              <fmt:formatNumber value="${order.totalAmount}" pattern="###,###"/>đ
            </h5>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>