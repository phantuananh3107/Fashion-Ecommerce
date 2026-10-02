<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Trang quản lý sản phẩm | TA shop</title>
</head>
<body>
	<main id="main" class="main">
		<section class="section">
			<div class="row">
				<div class="col-lg-12">
					<div class="card">
						<div class="card-body">
							<h5 class="card-title">Doanh số gần đây</h5>
							<c:if test="${not empty error}">
								<div class="alert alert-danger">${error}</div>
							</c:if>
							<table class="table datatable">
								<thead>
									<tr>
										<th>Khách hàng</th>
										<th>Sản phẩm</th>
										<th>Giá</th>
										<th>Trạng thái</th>
										<th>Ngày đặt hàng</th>
									</tr>
								</thead>
								<tbody>
									<c:forEach var="order" items="${orderList}">
										<tr>
											<td>${order.customerName}</td>
											<td>${order.productName}</td>
											<td><fmt:formatNumber value="${order.price}"
													type="number" pattern="#,##0.00" /> VNĐ</td>
											<td><c:choose>
													<c:when test="${order.orderStatus == 'pending'}">
														<span class="badge bg-warning">${order.orderStatus}</span>
													</c:when>
													<c:when test="${order.orderStatus == 'approved'}">
														<span class="badge bg-success">${order.orderStatus}</span>
													</c:when>
													<c:when test="${order.orderStatus == 'cancelled'}">
														<span class="badge bg-danger">${order.orderStatus}</span>
													</c:when>
													<c:otherwise>
														<span class="badge bg-secondary">${order.orderStatus}</span>
													</c:otherwise>
												</c:choose></td>
											<td>${order.orderDate}</td>
										</tr>
									</c:forEach>
									<c:if test="${empty orderList}">
										<tr>
											<td colspan="5" class="text-center">Không có đơn hàng
												nào.</td>
										</tr>
									</c:if>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</div>
		</section>
	</main>
</body>
</html>