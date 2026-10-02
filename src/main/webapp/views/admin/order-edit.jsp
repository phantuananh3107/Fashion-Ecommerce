<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Trang chỉnh sửa đơn hàng | TA shop</title>
</head>

<body>
<main id="main" class="main">
  <section class="section dashboard">
    <div class="row">
      <div class="col-12">
        <h1 class="box-title">Đơn hàng: DH#${order.orderId}</h1>
        <c:if test="${param.status == 'success'}">
          <div class="alert alert-success alert-dismissible fade show">Cập nhật đơn hàng thành công!</div>
        </c:if>
      </div>
      <div class="col-12">
        <div class="section-8">
          <form action="${pageContext.request.contextPath}/admin/admin-update-order" method="post" class="form-order-edit">
            <input type="text" name="userId" value="${order.userObject.userId}" hidden class="inner-name"/>
            <input type="text" name="orderId" value="${order.orderId}" hidden class="inner-name"/>

            <div class="inner-group">
              <label class="inner-label" for="name">Tên khách hàng</label>
              <input type="text" id="name" name="name" value="${order.userObject.fullname}" class="inner-name"/>
              <div class="message message-name"></div>
            </div>
            <div class="inner-group">
              <label class="inner-label" for="phone">Số điện thoại</label>
              <input type="text" id="phone" name="phone" value="${order.userObject.phoneNumber}" class="inner-phone"/>
              <div class="message message-phone"></div>
            </div>
            <div class="inner-group inner-two-col">
              <label class="inner-label" for="address">Địa chỉ</label>
              <input type="text" id="address" name="address" value="${order.userObject.address}" class="inner-address"/>
            </div>
            <div class="inner-group">
              <label class="inner-label" for="paymentMethod">Phương thức thanh toán</label>
              <c:choose>
                <c:when test="${order.paymentMethod == 'COD' || order.paymentMethod == 'cod'}">
                  <input type="text" id="paymentMethod" name="paymentMethod" value="Tiền mặt (COD)" readonly/>
                </c:when>
                <c:when test="${order.paymentMethod == 'VNPAY' || order.paymentMethod == 'vnpay'}">
                  <input type="text" id="paymentMethod" name="paymentMethod" value="VNPAY / Thẻ ATM" readonly/>
                </c:when>
                <c:when test="${order.paymentMethod == 'MOMO' || order.paymentMethod == 'momo'}">
                  <input type="text" id="paymentMethod" name="paymentMethod" value="Ví MoMo" readonly/>
                </c:when>
                <c:otherwise>
                  <input type="text" id="paymentMethod" name="paymentMethod" value="${order.paymentMethod}" readonly/>
                </c:otherwise>
              </c:choose>
            </div>
            <div class="inner-group">
              <label class="inner-label" for="statusPay">Trạng thái thanh toán</label>
              <select name="paymentStatus" id="statusPay">
                <option ${order.paymentStatus == 'Đã thanh toán (VNPAY)' ? 'selected="selected"' : ''} value="Đã thanh toán (VNPAY)">Đã thanh toán (VNPAY)</option>
                <option ${order.paymentStatus == 'Chưa thanh toán' ? 'selected="selected"' : ''} value="Chưa thanh toán">Chưa thanh toán</option>
                <option ${order.paymentStatus == 'Chờ thanh toán online' ? 'selected="selected"' : ''} value="Chờ thanh toán online">Chờ thanh toán online</option>
              </select>
            </div>
            <div class="inner-group">
              <label class="inner-label" for="createAt">Ngày đặt</label>
              <input type="datetime-local" id="createAt" name="createAt" value="${order.orderDate}" readonly="readonly" />
            </div>
            <div class="inner-group">
              <label class="inner-label" for="status">Trạng thái</label>
              <select name="orderStatus" id="status">
                <option ${order.orderStatus == 'Chờ xử lý' ? 'selected="selected"' : ''} value="Chờ xử lý">Chờ xử lý</option>
                <option ${order.orderStatus == 'Đã xác nhận' ? 'selected="selected"' : ''} value="Đã xác nhận">Đã xác nhận</option>
                <option ${order.orderStatus == 'Thành công' ? 'selected="selected"' : ''} value="Thành công">Thành công</option>
                <option ${order.orderStatus == 'Đã hủy' ? 'selected="selected"' : ''} value="Đã hủy">Đã hủy</option>
              </select>
            </div>
            <div class="inner-group">
              <label class="inner-label">Danh sách đơn hàng</label>
              <div class="inner-order-list">
                <c:forEach var="orderItem" items="${order.orderDetailList}">
                  <div class="inner-order-item">
                    <div class="inner-order-image"> <img src="<c:url value='${orderItem.productObject.productImage}'/>" /></div>
                    <div class="inner-order-content">
                      <div class="inner-order-name">${orderItem.productObject.productName}</div>
                      <div class="inner-order-text">Màu sắc: ${orderItem.productColor}</div>
                      <div class="inner-order-text">Size: ${orderItem.productSize}</div>
                      <div class="inner-order-text">Số lượng:
                        <fmt:formatNumber value="${orderItem.quantitySold}"
                                          type="number" groupingUsed="true" />
                      </div>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </div>
            <div class="inner-group">
              <label class="inner-label">Thanh toán</label>
              <div class="inner-order-total">
                <div>Tổng tiền:  <fmt:formatNumber value="${order.totalAmount}"
                                                   type="number" groupingUsed="true" />đ
                </div>
                <c:if test="${order.discountAmount > 0}">
                  <div style="color: #e74c3c; margin-top: 5px;">
                    Đã áp dụng Voucher giảm: -<fmt:formatNumber value="${order.discountAmount}" type="number" groupingUsed="true" />đ
                  </div>
                </c:if>
              </div>
            </div>
            <div class="inner-button inner-two-col">
              <button type="submit" class="btn-update">Cập nhật</button>
            </div>
          </form>
          <div class="inner-back"> <a href="${pageContext.request.contextPath}/admin/admin-order-list">Quay lại danh sách</a></div>
        </div>
      </div>
    </div>
  </section>
</main>

<script>
  document.addEventListener("DOMContentLoaded", function() {
    // 1. Logic ẩn thông báo sau 3s
    setTimeout(function() {
      let alerts = document.querySelectorAll('.alert');
      alerts.forEach(function(alert) {
        alert.classList.remove('show');
        setTimeout(() => alert.remove(), 200);
      });
    }, 3000);

    // ==========================================
    // 2. LOGIC KHÓA VAN 1 CHIỀU CHO TRẠNG THÁI ĐƠN HÀNG
    // ==========================================
    const statusSelect = document.getElementById('status');
    const options = statusSelect.options;

    // Định nghĩa cấp bậc của từng trạng thái
    const statusRank = {
      "Chờ xử lý": 1,
      "Đã xác nhận": 2,
      "Thành công": 3,
      "Đã hủy": 4 // Đã hủy là chốt hạ, không thể đổi sang cái khác
    };

    // Lấy trạng thái hiện tại lúc vừa load trang (trạng thái đã lưu trong DB)
    const currentStatus = "${order.orderStatus}";
    const currentRank = statusRank[currentStatus] || 0;

    // Lặp qua tất cả các lựa chọn
    for (let i = 0; i < options.length; i++) {
      let optRank = statusRank[options[i].value] || 0;

      // NẾU TÙY CHỌN CÓ CẤP BẬC NHỎ HƠN HIỆN TẠI -> VÔ HIỆU HÓA (XÁM ĐI)
      if (optRank < currentRank) {
        options[i].disabled = true;
      }

      // Nếu trạng thái hiện tại là "Thành công" hoặc "Đã hủy" -> Khóa chết thẻ Select
      if (currentStatus === "Thành công" || currentStatus === "Đã hủy") {
        statusSelect.style.pointerEvents = "none";
        statusSelect.style.backgroundColor = "#e9ecef"; // Đổi màu nền cho giống ô readonly
        document.querySelector(".btn-update").style.display = "none"; // Ẩn luôn nút Cập nhật
      }
    }
  });
</script>
</body>
</html>