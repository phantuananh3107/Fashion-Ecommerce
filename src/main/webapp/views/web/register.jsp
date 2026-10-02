<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<link href="<c:url value='/templates/web/images/logo.png'/>" rel="icon" />
	<link href="https://fonts.gstatic.com" rel="preconnect" />
	<link href="https://fonts.googleapis.com/css?family=Nunito:300,400,600,700" rel="stylesheet" />
	<link href="<c:url value='/templates/admin/bootstrap/bootstrap.min.css'/>" rel="stylesheet" />
	<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.7.2/css/all.min.css"/>
	<link href="<c:url value='/templates/admin/css/style.css'/>" rel="stylesheet" />
	<title>Đăng ký | TA shop</title>
</head>
<body style="background-image: url(<c:url value='/templates/admin/img/background.png'/>); background-size: cover;">
<main>
	<div class="container">
		<section class="section register min-vh-100 d-flex flex-column align-items-center justify-content-center py-4">
			<div class="container">
				<div class="row justify-content-center">
					<div class="col-lg-4 col-md-6 d-flex flex-column align-items-center justify-content-center">

						<div class="d-flex justify-content-center py-4">
							<a href="#" class="logo d-flex align-items-center w-auto" style="display:inline-flex; max-width:100%;">
								<img src="<c:url value='/templates/web/images/logo.png'/>" alt="TA shop"
									 style="display:block; width:220px !important; max-width:220px !important; height:auto !important; max-height:74px !important; object-fit:contain;" />
								<span class="d-none d-lg-block" style="font-size: 26px; font-weight: 700; color: #010101; font-family:'Nunito', sans-serif;">TA shop</span>
							</a>
						</div>

						<div class="card mb-3" style="display: flex; flex-direction: row; width: 850px; padding: 10px; border-radius: 45px; box-shadow: 0 0 20px rgba(0,0,0,0.1);">
							<div style="flex: 1; display: flex; align-items: center;">
								<img src="<c:url value='/templates/admin/img/login2.png'/>" alt="" style="width: 100%;">
							</div>

							<div class="card-body" style="flex: 1;">
								<div class="pt-4 pb-2" style="margin-top: 10px;">
									<h5 class="card-title text-center pb-0 fs-4">Đăng ký tài khoản</h5>
								</div>

								<!-- HIỂN THỊ THÔNG BÁO LỖI CHI TIẾT -->
								<c:set var="errorStatus" value="${not empty error ? error : param.error}" />
								<c:if test="${not empty errorStatus}">
									<div class="alert alert-danger text-center" style="border-radius: 10px; padding: 10px; margin-bottom: 20px; font-size: 14px;">
										<c:choose>
											<c:when test="${errorStatus == 'empty'}">
												<i class="fa-solid fa-circle-exclamation"></i> Vui lòng nhập đầy đủ thông tin!
											</c:when>
											<c:when test="${errorStatus == 'email_exists'}">
												<i class="fa-solid fa-envelope"></i> Email này đã được sử dụng!
											</c:when>
											<c:when test="${errorStatus == 'phone_exists'}">
												<i class="fa-solid fa-phone"></i> Số điện thoại này đã tồn tại!
											</c:when>
											<c:when test="${errorStatus == 'username_exists'}">
												<i class="fa-solid fa-user-tag"></i> Tên đăng nhập này đã có người dùng!
											</c:when>
											<c:when test="${errorStatus == 'exists'}">
												<i class="fa-solid fa-triangle-exclamation"></i> Thông tin đã tồn tại trên hệ thống!
											</c:when>
											<c:when test="${errorStatus == 'password_mismatch'}">
												<i class="fa-solid fa-key"></i> Xác nhận mật khẩu không khớp!
											</c:when>
											<c:otherwise>
												<i class="fa-solid fa-circle-exclamation"></i> Có lỗi xảy ra, vui lòng thử lại!
											</c:otherwise>
										</c:choose>
									</div>
								</c:if>

								<form class="row g-3 needs-validation" action="${pageContext.request.contextPath}/public/register" method="post" id="registerForm">

									<div class="col-md-6">
										<label for="yourUsername" class="form-label">Tên đăng nhập</label>
										<input type="text" name="username" class="form-control ${errorStatus == 'username_exists' ? 'is-invalid' : ''}" id="yourUsername" value="${oldFullname}" required autocomplete="off"/>
									</div>

									<div class="col-md-6">
										<label for="email" class="form-label">Email</label>
										<input type="email" name="email" class="form-control ${errorStatus == 'email_exists' ? 'is-invalid' : ''}" id="email" value="${oldEmail}" required placeholder="abc@gmail.com" autocomplete="off"/>
									</div>

									<div class="col-md-6">
										<label for="yourPassword" class="form-label">Mật khẩu</label>
										<input type="password" name="password" class="form-control" id="yourPassword" required autocomplete="new-password"/>
									</div>

									<div class="col-md-6">
										<label for="confirmPassword" class="form-label">Xác nhận mật khẩu</label>
										<input type="password" name="confirmPassword" class="form-control ${errorStatus == 'password_mismatch' ? 'is-invalid' : ''}" id="confirmPassword" required />
									</div>

									<div class="col-md-6">
										<label for="phone" class="form-label">Số điện thoại</label>
										<input type="text" name="phone" class="form-control ${errorStatus == 'phone_exists' ? 'is-invalid' : ''}" id="phone" value="${oldPhone}" required pattern="[0-9]{10,11}" />
									</div>

									<div class="col-md-6">
										<label for="gender" class="form-label">Giới tính</label>
										<select name="gender" id="gender" class="form-select" required>
											<option value="" disabled ${empty oldGender ? 'selected' : ''}>Chọn...</option>
											<option value="Nam" ${oldGender == 'Nam' ? 'selected' : ''}>Nam</option>
											<option value="Nữ" ${oldGender == 'Nữ' ? 'selected' : ''}>Nữ</option>
										</select>
									</div>

									<div class="col-12">
										<label for="address" class="form-label">Địa chỉ</label>
										<input type="text" name="address" class="form-control" id="address" value="${oldAddress}" required placeholder="Số nhà, tên đường..."/>
									</div>

									<div class="col-12 mt-4">
										<button class="btn btn-secondary w-100" type="submit" style="border-radius: 10px;">Tạo tài khoản</button>
									</div>
									<div class="col-12 text-center">
										<p class="small mb-0">
											Đã có tài khoản? <a href="${pageContext.request.contextPath}/public/login">Đăng nhập ngay</a>
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

<script>
	document.getElementById('registerForm').addEventListener('submit', function(event) {
		var password = document.getElementById('yourPassword').value;
		var confirmPassword = document.getElementById('confirmPassword').value;

		if (password !== confirmPassword) {
			alert('Mật khẩu và Xác nhận mật khẩu không khớp!');
			event.preventDefault();
		}
	});
</script>
</body>
</html>