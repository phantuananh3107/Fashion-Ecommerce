<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Trang admin | TA shop</title>
</head>
<body>
<main id="main" class="main">
	<div class="pagetitle d-flex align-items-center justify-content-between">
		<h1>Tổng quan</h1>
		<nav>
			<ol class="breadcrumb mb-0">
				<li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/admin-home"><i class="bi bi-house-door"></i></a></li>
				<li class="breadcrumb-item active">Tổng quan</li>
			</ol>
		</nav>
	</div>
	<section class="section dashboard">
		<div class="row">
			<div class="col-lg-8">
				<div class="row">
					<div class="col-xxl-4 col-md-4">
						<div class="card info-card sales-card">
							<div class="filter">
								<a class="icon" href="#" data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></a>
								<ul class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
									<li class="dropdown-header text-start"><h6>Bộ lọc</h6></li>
									<li><a class="dropdown-item" href="admin-home?time=today">Hôm nay</a></li>
									<li><a class="dropdown-item" href="admin-home?time=month">Tháng này</a></li>
									<li><a class="dropdown-item" href="admin-home?time=year">Năm này</a></li>
									<li><a class="dropdown-item" href="admin-home?time=all">Tất cả</a></li>
								</ul>
							</div>
							<div class="card-body">
								<h5 class="card-title">Doanh số <span>| ${timeLabel}</span></h5>
								<div class="d-flex align-items-center">
									<div class="card-icon rounded-circle d-flex align-items-center justify-content-center">
										<i class="bi bi-cart"></i>
									</div>
									<div class="ps-3">
										<h6>${totalOrders}</h6>
										<span class="text-muted small pt-2 ps-1">tăng</span> <span class="text-success small pt-1 fw-bold">12%</span>
									</div>
								</div>
							</div>
						</div>
					</div>

					<div class="col-xxl-4 col-md-4">
						<div class="card info-card revenue-card">
							<div class="filter">
								<a class="icon" href="#" data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></a>
								<ul class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
									<li class="dropdown-header text-start"><h6>Bộ lọc</h6></li>
									<li><a class="dropdown-item" href="admin-home?time=today">Hôm nay</a></li>
									<li><a class="dropdown-item" href="admin-home?time=month">Tháng này</a></li>
									<li><a class="dropdown-item" href="admin-home?time=year">Năm này</a></li>
									<li><a class="dropdown-item" href="admin-home?time=all">Tất cả</a></li>
								</ul>
							</div>
							<div class="card-body">
								<h5 class="card-title">Doanh thu <span>| ${timeLabel}</span></h5>
								<div class="d-flex align-items-center">
									<div class="card-icon rounded-circle d-flex align-items-center justify-content-center">
										<i class="bi bi-currency-dollar"></i>
									</div>
									<div class="ps-3">
										<h6><fmt:formatNumber value="${totalRevenue}" type="number" pattern="#,##0"/>đ</h6>
										<span class="text-muted small pt-2 ps-1">tăng</span> <span class="text-success small pt-1 fw-bold">8%</span>
									</div>
								</div>
							</div>
						</div>
					</div>

					<div class="col-xxl-4 col-md-4">
						<div class="card info-card customers-card">
							<div class="filter">
								<a class="icon" href="#" data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></a>
								<ul class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
									<li class="dropdown-header text-start"><h6>Bộ lọc</h6></li>
									<li><a class="dropdown-item" href="admin-home?time=today">Hôm nay</a></li>
									<li><a class="dropdown-item" href="admin-home?time=month">Tháng này</a></li>
									<li><a class="dropdown-item" href="admin-home?time=year">Năm này</a></li>
									<li><a class="dropdown-item" href="admin-home?time=all">Tất cả</a></li>
								</ul>
							</div>
							<div class="card-body">
								<h5 class="card-title">Khách hàng <span>| ${timeLabel}</span></h5>
								<div class="d-flex align-items-center">
									<div class="card-icon rounded-circle d-flex align-items-center justify-content-center">
										<i class="bi bi-people"></i>
									</div>
									<div class="ps-3">
										<h6>${totalCustomers}</h6>
										<span class="text-muted small pt-2 ps-1">giảm</span> <span class="text-danger small pt-1 fw-bold">12%</span>
									</div>
								</div>
							</div>
						</div>
					</div>

					<div class="col-12">
						<div class="card">
							<div class="card-body">
								<h5 class="card-title">Thống kê <span>| ${timeLabel}</span></h5>
								<div id="reportsChart"></div>
								<script>
									document.addEventListener("DOMContentLoaded", () => {
										const chartLabels = ${empty chartLabels ? '[]' : chartLabels};
										const chartOrders = ${empty chartOrders ? '[]' : chartOrders};
										const chartRevenue = ${empty chartRevenue ? '[]' : chartRevenue};

										new ApexCharts(document.querySelector("#reportsChart"), {
											series: [
												{ name: "Doanh số (Đơn)", data: chartOrders },
												{ name: "Doanh thu (VNĐ)", data: chartRevenue }
											],
											chart: { height: 400, type: "area", toolbar: { show: false } },
											markers: { size: 5 },
											colors: ["#4154f1", "#2eca6a"],
											fill: {
												type: "gradient",
												gradient: { shadeIntensity: 1, opacityFrom: 0.3, opacityTo: 0.4, stops: [0, 90, 100] }
											},
											dataLabels: { enabled: false },
											stroke: { curve: "smooth", width: 2 },
											xaxis: {
												type: "category",
												categories: chartLabels
											},
											tooltip: { x: { format: "dd/MM/yy" } }
										}).render();
									});
								</script>
							</div>
						</div>
					</div>
				</div>
			</div>

			<div class="col-lg-4">
				<div class="card">
					<div class="card-body">
						<h5 class="card-title">Hoạt động gần đây <span>| Cập nhật liên tục</span></h5>
						<div class="activity overflow-auto" style="max-height: 265px">
							<c:forEach var="act" items="${orderList}" end="4">
								<div class="activity-item d-flex">
									<div class="activite-label">${fn:substring(act.orderDate, 11, 16)}</div>
									<c:choose>
										<c:when test="${act.orderStatus == 'Thành công'}">
											<i class="bi bi-circle-fill activity-badge text-success align-self-start"></i>
											<div class="activity-content">Đơn của <strong>${act.customerName}</strong> thành công.</div>
										</c:when>
										<c:when test="${act.orderStatus == 'cancelled'}">
											<i class="bi bi-circle-fill activity-badge text-danger align-self-start"></i>
											<div class="activity-content">Đơn hàng của <strong>${act.customerName}</strong> bị hủy.</div>
										</c:when>
										<c:otherwise>
											<i class="bi bi-circle-fill activity-badge text-primary align-self-start"></i>
											<div class="activity-content"><strong>${act.customerName}</strong> vừa đặt đơn mới.</div>
										</c:otherwise>
									</c:choose>
								</div>
							</c:forEach>
							<c:if test="${empty orderList}">
								<div class="text-center text-muted mt-3">Chưa có hoạt động.</div>
							</c:if>
						</div>
					</div>
				</div>

				<div class="card">
					<div class="card-body pb-0">
						<h5 class="card-title">Sức mạnh danh mục <span>| Thực tế</span></h5>
						<div id="budgetChart" style="min-height: 325px" class="echart"></div>

						<script>
							document.addEventListener("DOMContentLoaded", () => {
								const thucTeData = [
									${empty valSoMi ? 0 : valSoMi},
									${empty valThun ? 0 : valThun},
									${empty valQuan ? 0 : valQuan},
									${empty valChanVay ? 0 : valChanVay},
									${empty valDamCS ? 0 : valDamCS},
									${empty valDamDH ? 0 : valDamDH}
								];

								const mucTieuData = [5000000, 4000000, 7000000, 6000000, 5000000, 8000000];

								let maxDoanhThu = Math.max(...thucTeData, ...mucTieuData);
								let radarMax = maxDoanhThu > 0 ? (maxDoanhThu * 1.15) : 10000000;

								echarts.init(document.querySelector("#budgetChart")).setOption({
									legend: { data: ["Mục tiêu", "Thực tế"] },
									radar: {
										indicator: [
											{ name: "Áo sơ mi", max: radarMax },
											{ name: "Áo thun", max: radarMax },
											{ name: "Quần dài", max: radarMax },
											{ name: "Chân váy", max: radarMax },
											{ name: "Đầm công sở", max: radarMax },
											{ name: "Đầm dạ hội", max: radarMax }
										]
									},
									series: [{
										name: "Phân tích sản phẩm",
										type: "radar",
										data: [
											{ value: mucTieuData, name: "Mục tiêu" },
											{ value: thucTeData, name: "Thực tế" }
										]
									}]
								});
							});
						</script>
					</div>
				</div>
			</div>

			<div class="col-12">
				<div class="card">
					<div class="card-body">
						<h5 class="card-title">Doanh số gần đây</h5>
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
									<td><fmt:formatNumber value="${order.price}" type="number" pattern="#,##0"/> VNĐ</td>
									<td>
										<c:choose>
											<c:when test="${order.orderStatus == 'Thành công'}">
												<span class="badge bg-success">${order.orderStatus}</span>
											</c:when>
											<c:when test="${order.orderStatus == 'Đã xác nhận'}">
												<span class="badge bg-primary">${order.orderStatus}</span>
											</c:when>
											<c:when test="${order.orderStatus == 'Chờ xử lý'}">
												<span class="badge bg-secondary">${order.orderStatus}</span>
											</c:when>
											<c:when test="${order.orderStatus == 'cancelled'}">
												<span class="badge bg-danger">${order.orderStatus}</span>
											</c:when>
											<c:otherwise>
												<span class="badge bg-dark">${order.orderStatus}</span>
											</c:otherwise>
										</c:choose>
									</td>
									<td>${order.orderDate}</td>
								</tr>
							</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
			</div>

			<div class="col-12">
				<div class="card top-selling overflow-auto">
					<div class="filter">
						<a class="icon" href="#" data-bs-toggle="dropdown"><i class="bi bi-three-dots"></i></a>
						<ul class="dropdown-menu dropdown-menu-end dropdown-menu-arrow">
							<li class="dropdown-header text-start"><h6>Bộ lọc</h6></li>
							<li><a class="dropdown-item" href="#">Hôm nay</a></li>
							<li><a class="dropdown-item" href="#">Tháng này</a></li>
							<li><a class="dropdown-item" href="#">Năm này</a></li>
						</ul>
					</div>
					<div class="card-body pb-0" id="banchay">
						<h5 class="card-title">Bán chạy nhất <span>| Toàn thời gian</span></h5>
						<table class="table table-borderless">
							<thead>
							<tr>
								<th scope="col">Hình ảnh</th>
								<th scope="col">Sản phẩm</th>
								<th scope="col">Giá</th>
								<th scope="col">Đã bán</th>
								<th scope="col">Doanh thu</th>
							</tr>
							</thead>
							<tbody>
							<c:forEach var="product" items="${topProductList}">
								<tr>
									<th scope="row">
										<a href="#">
											<img src="<c:url value='${empty product.productImage ? "/templates/admin/img/default-product.jpg" : product.productImage}'/>"
												 alt="${product.productName}"
												 style="width: 65px; border-radius: 5px;">
										</a>
									</th>
									<td><a href="#" class="text-primary fw-bold">${product.productName}</a></td>
									<td><fmt:formatNumber value="${product.price}" type="number" pattern="#,##0"/> VNĐ</td>
									<td class="fw-bold">${product.totalSold}</td>
									<td><fmt:formatNumber value="${product.revenue}" type="number" pattern="#,##0"/> VNĐ</td>
								</tr>
							</c:forEach>
							<c:if test="${empty topProductList}">
								<tr><td colspan="5" class="text-center">Chưa có dữ liệu sản phẩm bán chạy.</td></tr>
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