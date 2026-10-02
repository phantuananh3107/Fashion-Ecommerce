<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Trang quản lý đơn hàng | TA shop</title>
</head>
<body>
<main id="main" class="main">
  <div class="pagetitle d-flex align-items-center justify-content-between">
    <h1>Quản lý đơn hàng</h1>
    <nav>
      <ol class="breadcrumb mb-0">
        <li class="breadcrumb-item">
          <a href="${pageContext.request.contextPath}/admin/admin-home"><i class="bi bi-bag"></i></a>
        </li>
        <li class="breadcrumb-item active">Quản lý đơn hàng</li>
      </ol>
    </nav>
  </div>
  <section class="section dashboard">
    <div class="row">
      <div class="section-4">
        <form action="${pageContext.request.contextPath}/admin/admin-order-list" method="get">
          <div class="inner-wrap">
            <div class="inner-item inner-label"><i class="fa-solid fa-filter"></i> Bộ lọc</div>
            <div class="inner-item">
              <select name="orderStatus">
                <option value="">Trạng thái </option>
                <option ${orderStatus == 'Chờ xử lý' ? 'selected="selected"' : ''} value="Chờ xử lý">Chờ xử lý</option>
                <option ${orderStatus == 'Đã xác nhận' ? 'selected="selected"' : ''} value="Đã xác nhận">Đã xác nhận</option>
                <option ${orderStatus == 'Thành công' ? 'selected="selected"' : ''} value="Thành công">Thành công</option>
                <option ${orderStatus == 'Đã hủy' ? 'selected="selected"' : ''} value="Đã hủy">Đã hủy</option>
              </select>
            </div>
            <div class="inner-item">
              <select name="paymentMethod">
                <option value="">Phương thức thanh toán</option>
                <option ${paymentMethod == 'COD' ? 'selected="selected"' : ''} value="COD">Tiền mặt (COD)</option>
                <option ${paymentMethod == 'VNPAY' ? 'selected="selected"' : ''} value="VNPAY">VNPAY / Thẻ ATM</option>
                <option ${paymentMethod == 'MOMO' ? 'selected="selected"' : ''} value="MOMO">Ví MoMo</option>
              </select>
            </div>
            <div class="inner-item">
              <select name="paymentStatus">
                <option value="">Trạng thái thanh toán</option>
                <option ${paymentStatus == 'Chưa thanh toán' ? 'selected="selected"' : ''} value="Chưa thanh toán">Chưa thanh toán</option>
                <option ${paymentStatus == 'Chờ thanh toán online' ? 'selected="selected"' : ''} value="Chờ thanh toán online">Chờ thanh toán online</option>
                <option ${paymentStatus == 'Đã thanh toán (VNPAY)' ? 'selected="selected"' : ''} value="Đã thanh toán (VNPAY)">Đã thanh toán (VNPAY)</option>
              </select>
            </div>
            <button type="submit" class="inner-item inner-reset">Áp dụng</button>
          </div>
        </form>
      </div>
    </div>
    <div class="row">
      <div class="section-6">
        <div class="table-2">
          <table>
            <thead>
            <tr>
              <th>Mã</th>
              <th>Thông tin khách</th>
              <th>Danh sách đơn hàng</th>
              <th>Thanh toán</th>
              <th>Trạng thái</th>
              <th>Ngày đặt</th>
              <th>Hành động</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="order" items="${orders}">
              <tr>
                <td>
                  <div class="inner-code">DH#${order.orderId}</div>
                </td>
                <td>
                  <div>Họ tên: ${order.userObject.fullname}</div>
                  <div>SĐT: ${order.userObject.phoneNumber}</div>
                  <div>Địa chỉ: ${order.userObject.address}</div>
                </td>
                <td>
                  <div class="inner-list">
                    <c:forEach var="orderItem" items="${order.orderDetailList}">
                      <div class="inner-item">
                        <div class="inner-image"> <img src="<c:url value='${orderItem.productObject.productImage}'/>" /></div>
                        <div class="inner-content">
                          <div class="inner-name">${orderItem.productObject.productName}</div>
                          <div class="inner-quantity">
                            <div>Màu sắc: ${orderItem.productColor}</div>
                            <div>Size: ${orderItem.productSize}</div>
                            <div>Số lượng:
                              <fmt:formatNumber value="${orderItem.quantitySold}"
                                                type="number" groupingUsed="true" />
                            </div>
                            <div>Giá:
                              <fmt:formatNumber value="${orderItem.price}"
                                                type="number" groupingUsed="true" />
                            </div>
                          </div>
                        </div>
                      </div>
                    </c:forEach>
                  </div>
                </td>
                <td>
                  <div>Tổng tiền:
                    <fmt:formatNumber value="${order.totalAmount}"
                                      type="number" groupingUsed="true" />đ
                  </div>

                  <c:if test="${order.discountAmount > 0}">
                    <div style="color: #e74c3c; font-size: 0.9em; margin-bottom: 2px;">
                      Giảm giá: -<fmt:formatNumber value="${order.discountAmount}" type="number" groupingUsed="true" />đ
                    </div>
                  </c:if>

                  <div>PTTT:
                    <c:choose>
                      <c:when test="${order.paymentMethod == 'COD' || order.paymentMethod == 'cod'}">Thanh toán khi nhận hàng</c:when>
                      <c:when test="${order.paymentMethod == 'VNPAY' || order.paymentMethod == 'vnpay'}">VNPAY / Thẻ ATM</c:when>
                      <c:when test="${order.paymentMethod == 'MOMO' || order.paymentMethod == 'momo'}">Ví MoMo</c:when>
                      <c:otherwise>${order.paymentMethod}</c:otherwise>
                    </c:choose>
                  </div>
                  <div>TTTT:
                    <c:choose>
                      <c:when test="${order.paymentStatus == 'Chưa thanh toán'}">Chưa thanh toán</c:when>
                      <c:when test="${order.paymentStatus == 'Chờ thanh toán online'}">Chờ thanh toán online</c:when>
                      <c:when test="${order.paymentStatus == 'Đã thanh toán (VNPAY)'}">Đã thanh toán (VNPAY)</c:when>
                    </c:choose>
                  </div>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${order.orderStatus == 'Chờ xử lý'}">
                      <div class="badge badge-orange">Khởi tạo</div>
                    </c:when>
                    <c:when test="${order.orderStatus == 'Đã xác nhận'}">
                      <div class="badge badge-orange">Đã xác nhận</div>
                    </c:when>
                    <c:when test="${order.orderStatus == 'Thành công'}">
                      <div class="badge badge-green">Thành công</div>
                    </c:when>
                    <c:when test="${order.orderStatus == 'Đã hủy'}">
                      <div class="badge badge-red">Đã hủy</div>
                    </c:when>
                  </c:choose>

                </td>
                <td>
                  <div>${order.orderDate}</div>
                </td>
                <td>
                  <div class="inner-buttons"><a class="inner-edit"
                                                href="${pageContext.request.contextPath}/admin/admin-order-edit?orderId=${order.orderId}">
                    <i class="fa-solid fa-pen-to-square"></i></a>
                    <a class="inner-remove"  href="#" data-order-id="${order.orderId}"><i class="fa-solid fa-trash-can"></i></a></div>
                </td>
              </tr>
            </c:forEach>
            </tbody>
          </table>
        </div>
      </div>
    </div>
    <div class="row">
      <nav aria-label="Page navigation example">
        <ul class="pagination justify-content-end">

          <c:if test="${pageNo gt 1}">
            <li class="page-item">
              <a class="page-link"
                 href="${pageContext.request.contextPath}/admin/admin-order-list?pageNo=${pageNo - 1}&orderStatus=${orderStatus}&paymentStatus=${paymentStatus}&paymentMethod=${paymentMethod}"
                 aria-label="Previous">
                <span aria-hidden="true">&laquo;</span>
              </a>
            </li>
          </c:if>

          <c:if test="${pageNo ne 1}">
            <li class="page-item"><a class="page-link"
                                     href="${pageContext.request.contextPath}/admin/admin-order-list?pageNo=1&orderStatus=${orderStatus}&paymentStatus=${paymentStatus}&paymentMethod=${paymentMethod}">1</a></li>
          </c:if>

          <c:if test="${pageNo gt 2}">
            <li class="page-item disabled"><a class="page-link">...</a></li>
          </c:if>

          <li class="page-item active"><a class="page-link" href="#">${pageNo}</a></li>

          <c:if test="${pageNo lt pageNumber - 1}">
            <li class="page-item disabled"><a class="page-link">...</a></li>
          </c:if>

          <c:if test="${pageNo ne pageNumber}">
            <li class="page-item"><a class="page-link"
                                     href="${pageContext.request.contextPath}/admin/admin-order-list?pageNo=${pageNumber}&orderStatus=${orderStatus}&paymentStatus=${paymentStatus}&paymentMethod=${paymentMethod}">${pageNumber}</a></li>
          </c:if>

          <c:if test="${pageNo lt pageNumber}">
            <li class="page-item">
              <a class="page-link"
                 href="${pageContext.request.contextPath}/admin/admin-order-list?pageNo=${pageNo + 1}&orderStatus=${orderStatus}&paymentStatus=${paymentStatus}&paymentMethod=${paymentMethod}" aria-label="Next">
                <span aria-hidden="true">&raquo;</span>
              </a>
            </li>
          </c:if>

        </ul>
      </nav>

    </div>
  </section>
</main>
<script src="<c:url value='/templates/admin/js/order.js'/>"></script>
</body>
</html>