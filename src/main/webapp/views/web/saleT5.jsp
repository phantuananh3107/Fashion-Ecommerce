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
	<!-- Content -->
	<div class="container mb-5">
		<!-- Breadcrumb -->
		<div
			class="d-flex align-items-center border-bottom border-secondary border-opacity-10  flex-row"
			style="height: 57px;">
			<p class="m-0" style="opacity: 50%;">Trang chủ -</p>
			<p class="mb-0 ms-1">Sale 50% +++ Toàn bộ SP</p>
		</div>
		<!-- End Breadcrumb -->

		<!-- Main Content -->
		<div class="row mt-4">
			<div class="col-md-3">
				<ul class="list-unstyled">
					<li style="border-bottom: 1px solid #f7f8f9; margin-bottom: 16px;">
						<p class="d-flex justify-content-between size">
							Size<i class="fa-solid fa-plus"></i>
						</p>
						<div class=" list-size mb-3 d-none ">
							<span class=" align-items-center justify-content-center"
								style="display: flex; margin-right: 15px; width: 47px; height: 32px; font-size: 12px; color: #6c6d70; border: 1px solid #ccc; border-radius: 10px 0 10px 0;">S</span>
							<span class=" align-items-center justify-content-center"
								style="display: flex; margin-right: 15px; width: 47px; height: 32px; font-size: 12px; color: #6c6d70; border: 1px solid #ccc; border-radius: 10px 0 10px 0;">M</span>
							<span class=" align-items-center justify-content-center"
								style="display: flex; margin-right: 15px; width: 47px; height: 32px; font-size: 12px; color: #6c6d70; border: 1px solid #ccc; border-radius: 10px 0 10px 0;">L</span>
							<span class=" align-items-center justify-content-center"
								style="display: flex; margin-right: 15px; width: 47px; height: 32px; font-size: 12px; color: #6c6d70; border: 1px solid #ccc; border-radius: 10px 0 10px 0;">XL</span>
							<span class=" align-items-center justify-content-center"
								style="display: flex; margin-right: 15px; width: 47px; height: 32px; font-size: 12px; color: #6c6d70; border: 1px solid #ccc; border-radius: 10px 0 10px 0;">XXL</span>
						</div>
					</li>
					<li style="border-bottom: 1px solid #f7f8f9; margin-bottom: 16px;">
						<p class="d-flex justify-content-between colorq">
							Màu sắc<i class="fa-solid fa-plus"></i>
						</p>
						<div class=" list-color mb-3 d-none flex-wrap">
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/049.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border: 1px solid #ccc; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/001.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/038.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/054.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/015.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/031.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/052.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/064.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/035.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/011.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/033.png'/>);"></span>
							<span
								style="margin-bottom: 20px; margin-right: 16px; width: 18px; height: 18px; border-radius: 50px; background-image: url(<c:url value='/templates/web/images/004.png'/>);"></span>

						</div>
					</li>
					<li style="border-bottom: 1px solid #f7f8f9; margin-bottom: 16px;">
						<p class="d-flex justify-content-between">
							Mức giá<i class="fa-solid fa-plus" style="font-weight: 100;"></i>
						</p>
					</li>
					<li style="border-bottom: 1px solid #f7f8f9; margin-bottom: 16px;">
						<p class="d-flex justify-content-between rate">
							Mức chiết khấu<i class="fa-solid fa-plus"></i>
						</p>
						<div class="discount-rate d-none flex-column">
							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="discount"
									id="discount1"> <label class="form-check-label"
									for="discount1" style="font-size: 14px; padding-left: 5px;">
									Dưới 30% </label>
							</div>

							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="discount"
									id="discount2"> <label class="form-check-label"
									for="discount2" style="font-size: 14px; padding-left: 5px;">
									Từ 30% - 50% </label>
							</div>

							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="discount"
									id="discount3"> <label class="form-check-label"
									for="discount3" style="font-size: 14px; padding-left: 5px;">
									Từ 50% - 70% </label>
							</div>
							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="discount"
									id="discount4"> <label class="form-check-label"
									for="discount4" style="font-size: 14px; padding-left: 5px;">
									Từ 70% </label>
							</div>
							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="discount"
									id="discount5"> <label class="form-check-label"
									for="discount5" style="font-size: 14px; padding-left: 5px;">
									Giá đặc biệt </label>
							</div>
						</div>
					</li>
					<li style="border-bottom: 1px solid #f7f8f9; margin-bottom: 16px;">
						<p class="d-flex justify-content-between lift-high">
							Nâng cao<i class="fa-solid fa-plus"></i>
						</p>
						<div class="lift-high-sub d-none flex-column"
							style="color: #6c6d70;">
							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="filter"
									id="discount1"> <label
									class="form-check-label d-flex justify-content-between"
									for="discount1"
									style="font-size: 14px; padding-left: 5px; margin-top: 3px;">
									Chất liệu<i class="fa-solid fa-plus"></i>
								</label>
							</div>
							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="filter"
									id="discount2"> <label
									class="form-check-label d-flex justify-content-between"
									for="discount2"
									style="font-size: 14px; padding-left: 5px; margin-top: 3px;">
									Kiểu dáng<i class="fa-solid fa-plus"></i>
								</label>
							</div>
							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="filter"
									id="discount3"> <label
									class="form-check-label d-flex justify-content-between"
									for="discount3"
									style="font-size: 14px; padding-left: 5px; margin-top: 3px;">
									Họa tiết<i class="fa-solid fa-plus"></i>
								</label>
							</div>
							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="filter"
									id="discount4"> <label
									class="form-check-label d-flex justify-content-between"
									for="discount4"
									style="font-size: 14px; padding-left: 5px; margin-top: 3px;">
									Cổ áo<i class="fa-solid fa-plus"></i>
								</label>
							</div>
							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="filter"
									id="discount5"> <label
									class="form-check-label d-flex justify-content-between"
									for="discount5"
									style="font-size: 14px; padding-left: 5px; margin-top: 3px;">
									Tay áo<i class="fa-solid fa-plus"></i>
								</label>
							</div>
							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="filter"
									id="discount6"> <label
									class="form-check-label d-flex justify-content-between"
									for="discount6"
									style="font-size: 14px; padding-left: 5px; margin-top: 3px;">
									Cạp quần<i class="fa-solid fa-plus"></i>
								</label>
							</div>
							<div class="form-check" style="margin-bottom: 20px;">
								<input class="form-check-input" type="radio" name="filter"
									id="discount7"> <label
									class="form-check-label d-flex justify-content-between"
									for="discount7"
									style="font-size: 14px; padding-left: 5px; margin-top: 3px;">
									Cạp zuýp<i class="fa-solid fa-plus"></i>
								</label>
							</div>
						</div>
					</li>

				</ul>
				<div class="col-md-12 p-0" style="margin-top: 30px;">
					<div class="row m-0 p-0 ">
						<div class="col-6">
							<button type="button" class="btn border-dark uncollect"
								style="font-size: 13px; padding: 10px 20px; border-radius: 15px 0 15px 0;">BỎ
								LỌC</button>
						</div>
						<div class="col-6">
							<button type="button" class="btn border-dark collect"
								style="font-size: 13px; padding: 10px 20px; border-radius: 15px 0 15px 0; background-color: #000; color: #fff;">LỌC</button>
						</div>
					</div>
				</div>
			</div>
			<div class="col-md-9">
				<!-- section 1 -->
				<div class="row align-items-center">
					<div class="col-8">
						<h1 class="mb-0" style="font-size: 24px;">SIÊU DEAL T5 CHÀO
							HÈ - SALE 50% +++</h1>
					</div>
					<div class="col-4">
						<div
							class="btn  d-flex align-items-center justify-content-between rounded-5 p-2 position-relative arrange"
							style="width: 230px; font-size: 14px; margin-left: 70px; border: 1px solid #ccc;">
							Sắp xếp theo <i class="fa-solid fa-chevron-down ms-2 icon-arrow"></i>
						</div>

						<ul
							class="list-content list-unstyled position-absolute rounded-3 mt-2 list  "
							style="width: 230px; height: 270px; background-color: #fff; padding: 10px; border: 1px solid #ccc; margin-left: 70px; display: none;">
							<li><a href="" class="text-secondary d-block "
								style="font-size: 14px; width: 100%; height: 40px; line-height: 40px;">Mặc
									định</a></li>
							<li><a href="" class="text-secondary d-block "
								style="font-size: 14px; width: 100%; height: 40px; line-height: 40px;">Mới
									nhất</a></li>
							<li><a href="" class="text-secondary d-block "
								style="font-size: 14px; width: 100%; height: 40px; line-height: 40px;">Được
									mua nhiều nhất</a></li>
							<li><a href="" class="text-secondary d-block "
								style="font-size: 14px; width: 100%; height: 40px; line-height: 40px;">Được
									yêu thích nhất</a></li>
							<li><a href="" class="text-secondary d-block "
								style="font-size: 14px; width: 100%; height: 40px; line-height: 40px;">Giá:
									cao đến thấp</a></li>
							<li><a href="" class="text-secondary d-block "
								style="font-size: 14px; width: 100%; height: 40px; line-height: 40px;">Giá:
									thấp đến cao</a></li>
						</ul>
					</div>

				</div>


				<!--  end section 1 -->
				<!-- Slide bar -->
				<div class="row mt-3 mb-3">
					<img src="<c:url value='/templates/web/images/web-50A.jpg'/>"
						alt="" style="width: 100%; border-radius: 80px 0;">
				</div>
				<!-- End Slide bar -->
				<!-- List Product -->
				<div class="row mt-3">
					<c:forEach var="product" items="${products}">
						<div class="col-md-3" style="margin-bottom: 30px;">
							<div class="img-lb-bl position-relative">
								<img class="product-img"
									src="<c:url value='${product.productImage}'/>"
									alt="${product.productName}" style="width: 100%;"> <span
									class="d-flex position-absolute text-center align-items-center justify-content-center text-white"
									style="width: 45px; height: 45px; top: 8px; right: 9px; border-radius: 50px 50px 0px 50px; font-weight: 600; font-size: 14px; background-color: #DC633A;">
									50% </span> <span
									class="d-flex position-absolute text-center align-items-center justify-content-center text-white"
									style="width: 100px; height: 30px; top: 0px; left: 0px; border-radius: 0px 0px 50px 0px; font-weight: 600; font-size: 14px; background-color: #AC2F33;">
									Best Seller </span>
							</div>
							<div class="change-color-img mt-2 d-flex justify-content-between">
								<ul class="list-unstyled mb-0">
									<li class="list-inline-item">
										<div class="box-2 position-relative"
											style="width: 18px; height: 18px; background-color: ${product.productColor}; border-radius: 50%; background-size: 100%;">
											<i class="fa-solid fa-check position-absolute"
												style="font-size: 12px; opacity: 80%; top: 3px; left: 3px; color: #fff;"></i>
										</div>
									</li>
								</ul>
								<div class="position-relative"
									style="width: 18px; height: 18px;">
									<i class="bi bi-heart position-absolute"></i>
								</div>
							</div>
							<h3 class="product-title">
								<a href="/detail.html?id=${product.productId}"
									style="font-size: 14px; color: #57585A; margin-bottom: 10px; font-weight: 400; line-height: 16px;">
									${product.productName} </a>
							</h3>
							<div
								class="product-price d-flex justify-content-between align-items-center">
								<div class="price"
									style="color: #3E3E3F; font-weight: 600; font-size: 16px; line-height: 24px;">
									${product.productPrice * 0.5}đ
									<del style="color: #a8a9ad; font-size: 12px;">
										<span>${product.productPrice}đ</span>
									</del>
								</div>
								<div class="card">
									<i class="bi bi-handbag"
										style="width: 32px; height: 32px; padding: 5px; background: #221F20; color: #fff; text-align: center; border-radius: 8px 0; border: 1px solid transparent;"></i>
								</div>
							</div>
						</div>
					</c:forEach>
				</div>
				<!-- End List Product -->
				<!-- Pagination -->
<nav class="pagination">
    <ul>
        <!-- Trang trước -->
        <li>
            <a href="<c:url value='/saleT5?page=${currentPage > 1 ? currentPage - 1 : 1}'/>">
                <i class="fa-solid fa-angles-left"></i>
            </a>
        </li>
        <!-- Các số trang -->
        <c:forEach begin="1" end="${totalPages}" var="pageNum">
            <li>
                <a href="<c:url value='/saleT5?page=${pageNum}'/>"
                   class="${pageNum == currentPage ? 'active' : ''}">
                    ${pageNum}
                </a>
            </li>
        </c:forEach>
        <!-- Trang sau -->
        <li>
            <a href="<c:url value='/saleT5?page=${currentPage < totalPages ? currentPage + 1 : totalPages}'/>">
                <i class="fa-solid fa-angles-right"></i>
            </a>
        </li>
        <!-- Trang cuối -->
        <li>
            <a href="<c:url value='/saleT5?page=${totalPages}'/>">Trang cuối</a>
        </li>
    </ul>
</nav>
<!-- End Pagination -->

			</div>

		</div>
		<!-- End Main Content -->

	</div>
	<!-- End Content -->
</body>
</html>