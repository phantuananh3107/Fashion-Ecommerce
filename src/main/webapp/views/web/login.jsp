<%@ page language="java" contentType="text/html; charset=UTF-8"
		 pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<link href="<c:url value='/templates/web/images/logo.png'/>"
		  rel="icon" />
	<link href="<c:url value='/templates/web/images/logo.png'/>"
		  rel="apple-touch-icon" />

	<link href="https://fonts.gstatic.com" rel="preconnect" />
	<link
			href="https://fonts.googleapis.com/css?family=Open+Sans:300,300i,400,400i,600,600i,700,700i|Nunito:300,300i,400,400i,600,600i,700,700i|Poppins:300,300i,400,400i,500,500i,600,600i,700,700i"
			rel="stylesheet" />

	<link
			href="<c:url value='/templates/admin/bootstrap/bootstrap.min.css'/>"
			rel="stylesheet" />
	<link
			href="<c:url value='/templates/admin/bootstrap-icons/bootstrap-icons.css'/>"
			rel="stylesheet" />
	<link
			href="<c:url value='/templates/admin/boxicons/css/boxicons.min.css'/>"
			rel="stylesheet" />
	<link href="<c:url value='/templates/admin/quill/quill.snow.css'/>"
		  rel="stylesheet" />
	<link href="<c:url value='/templates/admin/quill/quill.bubble.css'/>"
		  rel="stylesheet" />
	<link href="<c:url value='/templates/admin/remixicon/remixicon.css'/>"
		  rel="stylesheet" />
	<link
			href="<c:url value='/templates/admin/simple-datatables/style.css'/>"
			rel="stylesheet" />

	<link rel="stylesheet"
		  href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css"
		  integrity="sha512-Evv84Mr4kqVGRNSgIGL/F/aIDqQb7xQ2vcrdIwxfjThSH8CSR7PBEakCr51Ck+w+/U6swU2Im1vVX0SVk9ABhg=="
		  crossorigin="anonymous" referrerpolicy="no-referrer" />
	<link rel="stylesheet"
		  href="<c:url value='/templates/admin/css/ngoquangha-order-list.css'/>">
	<link rel="stylesheet"
		  href="<c:url value='/templates/admin/css/ngoquangha-order-edit.css'/>">
	<link rel="stylesheet"
		  href="<c:url value='/templates/admin/css/manager-user.css'/>">
	<link href="<c:url value='/templates/admin/css/style.css'/>"
		  rel="stylesheet" />
	<style>
		.login-brand {
			display: inline-flex;
			align-items: center;
			max-width: 100%;
		}

		.login-brand img {
			display: block;
			width: 220px !important;
			max-width: 100%;
			height: auto !important;
			max-height: 74px !important;
			object-fit: contain;
			margin-right: 8px;
		}
	</style>
	<title>Đăng nhập | TA shop</title>
</head>
<body
		style="background-image: url(<c:url value='/templates/admin/img/background.png'/>);">
<main>
	<div class="container">
		<section
				class="section register min-vh-100 d-flex flex-column align-items-center justify-content-center py-4">
			<div class="container">
				<div class="row justify-content-center">
					<div
							class="col-lg-4 col-md-6 d-flex flex-column align-items-center justify-content-center">
						<div class="d-flex justify-content-center py-4">
							<a href="${pageContext.request.contextPath}/public/trang-chu"
							   class="logo login-brand d-flex align-items-center w-auto">
								<img src="<c:url value='/templates/web/images/logo.png'/>"
									 alt="TA shop"
									 style="display:block; width:220px !important; max-width:220px !important; height:auto !important; max-height:74px !important; object-fit:contain;" /> <span class="d-none d-lg-block"
													 style="font-size: 26px; font-weight: 700; color: #010101; font-family:'Nunito', sans-serif;">TA
                               Moda</span>
							</a>

						</div>
						<div class="card mb-3"
							 style="display: flex; flex-direction: row; width: 800px; padding: 10px; border-radius: 45px;">
							<div>
								<img src="<c:url value='/templates/admin/img/login2.png'/>"
									 alt="" style="width: 100%;">
							</div>

							<div class="card-body">
								<div class="pt-4 pb-2" style="margin-top: 40px;">
									<h5 class="card-title text-center pb-0 fs-4">Đăng nhập
										tài khoản</h5>
								</div>

								<c:if test="${not empty param.error}">
									<div class="alert alert-danger text-center" style="border-radius: 10px; padding: 10px; margin-bottom: 20px;">
										<c:choose>
											<c:when test="${param.error == 'empty'}">
												<i class="fa-solid fa-circle-exclamation"></i> Vui lòng nhập đầy đủ thông tin!
											</c:when>
											<c:when test="${param.error == 'invalid'}">
												<i class="fa-solid fa-triangle-exclamation"></i> Sai tài khoản hoặc mật khẩu!<br>
												<c:if test="${not empty param.attempts}">
													<small style="font-weight: bold;">(Bạn còn ${param.attempts} lần thử)</small>
												</c:if>
											</c:when>
											<c:when test="${param.error == 'locked'}">
												<i class="fa-solid fa-lock"></i> Tài khoản đã bị tạm khóa do đăng nhập sai quá nhiều lần!
											</c:when>
										</c:choose>
									</div>
								</c:if>
								<form class="row g-3 needs-validation" action="${pageContext.request.contextPath}/public/login" method="post">
									<div class="col-12">
										<label for="yourUsername" class="form-label">Tên
											đăng nhập</label>
										<div class="input-group has-validation">
											<input type="text" name="username" class="form-control"
												   id="yourUsername" required />
											<div class="invalid-feedback">Vui lòng nhập tên của
												bạn!</div>
										</div>
									</div>

									<div class="col-12">
										<label for="yourPassword" class="form-label">Mật
											khẩu</label> <input type="password" name="password"
																class="form-control" id="yourPassword" required />
										<div class="invalid-feedback">Vui lòng nhập mật khẩu
											của bạn!</div>
									</div>

									<div class="col-12">
										<div class="form-check"
											 style="display: flex; justify-content: space-between;">
											<div>
												<input class="form-check-input" type="checkbox"
													   name="remember" value="true" id="rememberMe" />
												<label class="form-check-label" for="rememberMe">Lưu
													mật khẩu</label>
											</div>

											<a href="" style="text-align: right; color: #212529;">Quên
												mật khẩu?</a>
										</div>
									</div>
									<div class="col-12">
										<button class="btn btn-secondary w-100" type="submit">
											Đăng nhập</button>
									</div>
									<div class="col-12">
										<p class="small mb-0">
											Chưa có tài khoản? <a href="${pageContext.request.contextPath}/public/register">Tạo tài khoản</a>
										</p>
									</div>
								</form>
							</div>
						</div>
					</div>
				</div>
			</div>
		</section>
	</div>
</main>
<script
		src="<c:url value='/templates/admin/apexcharts/apexcharts.min.js'/>"></script>
<script
		src="<c:url value='/templates/admin/bootstrap/bootstrap.bundle.min.js'/>"></script>
<script src="<c:url value='/templates/admin/chart.js/chart.umd.js'/>"></script>
<script src="<c:url value='/templates/admin/echarts/echarts.min.js'/>"></script>
<script src="<c:url value='/templates/admin/quill/quill.js'/>"></script>
<script
		src="<c:url value='/templates/admin/simple-datatables/simple-datatables.js'/>"></script>
<script src="<c:url value='/templates/admin/tinymce/tinymce.min.js'/>"></script>
<script
		src="<c:url value='/templates/admin/php-email-form/validate.js'/>"></script>

<script src="<c:url value='/templates/admin/js/main.js'/>"></script>
<script src="<c:url value='/templates/admin/js/products-list.js'/>"></script>
<script src="<c:url value='/templates/admin/js/validate-product.js'/>"></script>
<script src="<c:url value='/templates/admin/js/add-product.js'/>"></script>
<script src="<c:url value='/templates/admin/js/validate-login.js'/>"></script>

</body>
</html>