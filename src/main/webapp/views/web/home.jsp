<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Trang chủ | TA shop</title>
</head>
<body>
	<!-- Section 1 -->
	<div class="section-1">
		<div class="container">
			<div class="swiper swiperSection1">
				<div class="swiper-wrapper">
					<div class="swiper-slide inner-item">
						<a href="#"> <img
							src="<c:url value='/templates/web/images/banner-4.webp'/>" alt="">
						</a>
					</div>
					<div class="swiper-slide inner-item">
						<a href="#"> <img
							src="<c:url value='/templates/web/images/banner-1.webp'/>" alt="">
						</a>
					</div>
					<div class="swiper-slide inner-item">
						<a href="#"> <img
							src="<c:url value='/templates/web/images/banner-2.webp'/>" alt="">
						</a>
					</div>
					<div class="swiper-slide inner-item">
						<a href="#"> <img
							src="<c:url value='/templates/web/images/banner-3.webp'/>" alt="">
						</a>
					</div>
					<div class="swiper-slide inner-item">
						<a href="#"> <img
							src="<c:url value='/templates/web/images/banner-5.webp'/>" alt="">
						</a>
					</div>
				</div>
				<div class="swiper-button-next">
					<i class="bi bi-arrow-right"></i>
				</div>
				<div class="swiper-button-prev">
					<i class="bi bi-arrow-left"></i>
				</div>
				<div class="swiper-pagination"></div>
			</div>
		</div>
	</div>
	<!-- End Section 1 -->

	<!-- Section 2: New Arrival -->
	<div class="section-2">
		<div class="container">
			<h2 class="box-title">NEW ARRIVAL</h2>
			<div class="swiper swiperSection2">
				<div class="swiper-wrapper">
					<c:forEach var="product" items="${newArrivals}">
						<div class="swiper-slide product-item">
							<div class="inner-tag tag-uppercase">New</div>
							<div class="inner-image">
								<a
									href="<c:url value='/public/product-detail?id=${product.productId}'/>">
									<img src="<c:url value='${product.productImage}'/>"
									alt="${product.productName}"> <img
									src="<c:url value='${product.productImage}'/>"
									alt="${product.productName}" class="hover-img">
								</a>
							</div>
							<div class="inner-content">
								<div class="inner-meta">
									<div class="inner-color">
										<div class="inner-box"
											style="background-color: ${product.productColor};"></div>
										<div class="inner-box">
											<i class="bi bi-check2"></i>
										</div>
									</div>
									<div class="inner-favorite">
										<i class="bi bi-heart"></i>
									</div>
								</div>
								<h3 class="inner-title">
									<a
										href="<c:url value='/public/product-detail?id=${product.productId}'/>">${product.productName}</a>
								</h3>
								<div class="price-product">
									<div class="inner-price">
										<div class="inner-price-new">${product.productPrice}đ</div>
<%--										<div class="inner-price-old">${product.productPrice}đ</div>--%>
									</div>
									<div class="inner-bag dropdown">
										<a href="#" role="button" data-bs-toggle="dropdown"
											aria-expanded="false" data-bs-offset="0, 20"> <i
											class="bi bi-bag"></i>
										</a>
										<ul class="dropdown-menu">
											<li><a class="dropdown-item" href="#"
												data-product-id=${product.productId } data-size="S">S</a></li>
											<li><a class="dropdown-item" href="#"
												data-product-id=${product.productId } data-size="M">M</a></li>
											<li><a class="dropdown-item" href="#"
												data-product-id=${product.productId } data-size="L">L</a></li>
											<li><a class="dropdown-item" href="#"
												data-product-id=${product.productId } data-size="XL">XL</a></li>
										</ul>
									</div>
								</div>
							</div>
						</div>
					</c:forEach>
				</div>
				<div class="swiper-button-next swiper-button-next-custom">
					<i class="bi bi-arrow-right"></i>
				</div>
				<div class="swiper-button-prev swiper-button-prev-custom">
					<i class="bi bi-arrow-left"></i>
				</div>
			</div>
			<div class="inner-button">
				<a href="all-product" class="button-outline">Xem tất cả</a>
			</div>
		</div>
	</div>
	<!-- End Section 2 -->

	<!-- Section 2 more: Best Seller -->
	<div class="section-2">
		<div class="container">
			<h2 class="box-title">RẠNG RỠ THÁNG 6 ƯU ĐÃI LỚN - GIẢM tới 60% CÁC
				SẢN PHẨM</h2>
			<div class="swiper swiperSection2">
				<div class="swiper-wrapper">
					<c:forEach var="product" items="${saleProducts}">
						<div class="swiper-slide product-item">
							<div class="inner-tag">Best Seller</div>

							<div class="inner-discount">
								-${product.discountPercent}<span>%</span>
							</div>
							<div class="inner-image">
								<a
									href="<c:url value='/public/product-detail?id=${product.productId}'/>">
									<img src="<c:url value='${product.productImage}'/>"
									alt="${product.productName}"> <img
									src="<c:url value='${product.productImage}'/>"
									alt="${product.productName}" class="hover-img">
								</a>
							</div>
							<div class="inner-content">
								<div class="inner-meta">
									<div class="inner-color">
										<div class="inner-box"
											style="background-color: ${product.productColor};"></div>
										<div class="inner-box">
											<i class="bi bi-check2"></i>
										</div>
									</div>
									<div class="inner-favorite">
										<i class="bi bi-heart"></i>
									</div>
								</div>
								<h3 class="inner-title">
									<a
										href="<c:url value='/public/product-detail?id=${product.productId}'/>">${product.productName}</a>
								</h3>
								<div class="price-product">
									<div class="inner-price">
										<div class="inner-price-new">${product.productPrice - (product.productPrice * product.discountPercent / 100)}đ</div>
										<div class="inner-price-old">${product.productPrice}đ</div>
									</div>
									<div class="inner-bag dropdown">
										<a href="#" role="button" data-bs-toggle="dropdown"
											aria-expanded="false" data-bs-offset="0, 20"> <i
											class="bi bi-bag"></i>
										</a>
										<ul class="dropdown-menu">
											<li><a class="dropdown-item" href="#">S</a></li>
											<li><a class="dropdown-item" href="#">M</a></li>
											<li><a class="dropdown-item" href="#">L</a></li>
											<li><a class="dropdown-item" href="#">XL</a></li>
										</ul>
									</div>
								</div>
							</div>
						</div>
					</c:forEach>
				</div>
				<div class="swiper-button-next swiper-button-next-custom">
					<i class="bi bi-arrow-right"></i>
				</div>
				<div class="swiper-button-prev swiper-button-prev-custom">
					<i class="bi bi-arrow-left"></i>
				</div>
			</div>
			<div class="inner-button">
				<a href="${pageContext.request.contextPath}/public/sale" class="button-outline">Xem tất cả</a>
			</div>
		</div>
	</div>
	<!-- End Section 2 more -->

	<!-- Section: Dành Cho Bạn (Personalized Recommendations) -->
	<c:if test="${not empty personalizedProducts}">
	<div class="section-2 mt-4">
		<div class="container">
			<h2 class="box-title">DÀNH CHO BẠN</h2>
			<div class="swiper swiperSection2">
				<div class="swiper-wrapper">
					<c:forEach var="rec" items="${personalizedProducts}">
						<c:set var="product" value="${rec.product}"/>
						<div class="swiper-slide product-item">
							<div class="inner-tag tag-uppercase" style="background-color: #0d6efd; color: #fff;">${rec.reason}</div>
							<c:if test="${product.discountPercent > 0}">
								<div class="inner-discount">-${product.discountPercent}<span>%</span></div>
							</c:if>
							<div class="inner-image">
								<a href="<c:url value='/public/product-detail?id=${product.productId}&ref=${rec.source}'/>">
									<img src="<c:url value='${product.productImage}'/>" alt="${product.productName}">
									<img src="<c:url value='${product.productImage}'/>" alt="${product.productName}" class="hover-img">
								</a>
							</div>
							<div class="inner-content">
								<div class="inner-meta">
									<div class="inner-color">
										<div class="inner-box" style="background-color: ${product.productColor};"></div>
									</div>
								</div>
								<h3 class="inner-title">
									<a href="<c:url value='/public/product-detail?id=${product.productId}&ref=${rec.source}'/>">${product.productName}</a>
								</h3>
								<div class="price-product">
									<div class="inner-price">
										<c:choose>
											<c:when test="${product.discountPercent > 0}">
												<div class="inner-price-new"><fmt:formatNumber value="${product.productPrice - (product.productPrice * product.discountPercent / 100)}" pattern="###,###"/>đ</div>
												<div class="inner-price-old"><fmt:formatNumber value="${product.productPrice}" pattern="###,###"/>đ</div>
											</c:when>
											<c:otherwise>
												<div class="inner-price-new"><fmt:formatNumber value="${product.productPrice}" pattern="###,###"/>đ</div>
											</c:otherwise>
										</c:choose>
									</div>
								</div>
							</div>
						</div>
					</c:forEach>
				</div>
				<div class="swiper-button-next swiper-button-next-custom"><i class="bi bi-arrow-right"></i></div>
				<div class="swiper-button-prev swiper-button-prev-custom"><i class="bi bi-arrow-left"></i></div>
			</div>
		</div>
	</div>
	</c:if>

	<!-- Section: Đang Được Quan Tâm (Trending Recommendations) -->
	<c:if test="${not empty trendingProducts}">
	<div class="section-2 mt-4">
		<div class="container">
			<h2 class="box-title">ĐANG ĐƯỢC QUAN TÂM</h2>
			<div class="swiper swiperSection2">
				<div class="swiper-wrapper">
					<c:forEach var="rec" items="${trendingProducts}">
						<c:set var="product" value="${rec.product}"/>
						<div class="swiper-slide product-item">
							<div class="inner-tag tag-uppercase" style="background-color: #ffc107; color: #000;">HOT</div>
							<c:if test="${product.discountPercent > 0}">
								<div class="inner-discount">-${product.discountPercent}<span>%</span></div>
							</c:if>
							<div class="inner-image">
								<a href="<c:url value='/public/product-detail?id=${product.productId}&ref=${rec.source}'/>">
									<img src="<c:url value='${product.productImage}'/>" alt="${product.productName}">
									<img src="<c:url value='${product.productImage}'/>" alt="${product.productName}" class="hover-img">
								</a>
							</div>
							<div class="inner-content">
								<h3 class="inner-title">
									<a href="<c:url value='/public/product-detail?id=${product.productId}&ref=${rec.source}'/>">${product.productName}</a>
								</h3>
								<div class="price-product">
									<div class="inner-price">
										<c:choose>
											<c:when test="${product.discountPercent > 0}">
												<div class="inner-price-new">${product.productPrice - (product.productPrice * product.discountPercent / 100)}đ</div>
												<div class="inner-price-old">${product.productPrice}đ</div>
											</c:when>
											<c:otherwise>
												<div class="inner-price-new">${product.productPrice}đ</div>
											</c:otherwise>
										</c:choose>
									</div>
								</div>
							</div>
						</div>
					</c:forEach>
				</div>
				<div class="swiper-button-next swiper-button-next-custom"><i class="bi bi-arrow-right"></i></div>
				<div class="swiper-button-prev swiper-button-prev-custom"><i class="bi bi-arrow-left"></i></div>
			</div>
		</div>
	</div>
	</c:if>

	<!-- Section: Bạn Đã Xem Gần Đây (Recently Viewed) -->
	<c:if test="${not empty recentlyViewedProducts}">
	<div class="section-2 mt-4">
		<div class="container">
			<h2 class="box-title">BẠN ĐÃ XEM GẦN ĐÂY</h2>
			<div class="swiper swiperSection2">
				<div class="swiper-wrapper">
					<c:forEach var="rec" items="${recentlyViewedProducts}">
						<c:set var="product" value="${rec.product}"/>
						<div class="swiper-slide product-item">
							<div class="inner-tag tag-uppercase" style="background-color: #6c757d; color: #fff;">Đã xem</div>
							<div class="inner-image">
								<a href="<c:url value='/public/product-detail?id=${product.productId}&ref=${rec.source}'/>">
									<img src="<c:url value='${product.productImage}'/>" alt="${product.productName}">
								</a>
							</div>
							<div class="inner-content">
								<h3 class="inner-title">
									<a href="<c:url value='/public/product-detail?id=${product.productId}&ref=${rec.source}'/>">${product.productName}</a>
								</h3>
								<div class="price-product">
									<div class="inner-price">
										<div class="inner-price-new">${product.productPrice}đ</div>
									</div>
								</div>
							</div>
						</div>
					</c:forEach>
				</div>
				<div class="swiper-button-next swiper-button-next-custom"><i class="bi bi-arrow-right"></i></div>
				<div class="swiper-button-prev swiper-button-prev-custom"><i class="bi bi-arrow-left"></i></div>
			</div>
		</div>
	</div>
	</c:if>

	<!-- Section 3 -->
	<div class="section-3">
		<div class="container">
			<h2 class="box-title">NEW COLLECTION 2025</h2>
			<div class="inner-wrap">
				<div class="inner-item">
					<a href="#"> <img
						src="<c:url value='/templates/web/images/image-1.webp'/>" alt="">
					</a>
				</div>
				<div class="inner-item">
					<a href="#"> <img
						src="<c:url value='/templates/web/images/image-2.webp'/>" alt="">
					</a>
				</div>
				<div class="inner-item">
					<a href="#"> <img
						src="<c:url value='/templates/web/images/image-3.webp'/>" alt="">
					</a>
				</div>
				<div class="inner-item">
					<a href="#"> <img
						src="<c:url value='/templates/web/images/image-4.webp'/>" alt="">
					</a>
				</div>
			</div>
		</div>
	</div>
	<!-- End Section 3 -->
</body>
</html>