<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Trang chi tiết người dùng | TA shop</title>
</head>
<body>
<main id="main" class="main">
    <div class="pagetitle">
        <h1>Chi tiết ngươi dùng</h1>
    </div>
    <div class="card">
        <div class="card-body">
            <c:if test="${not empty user}">

                <div class="row g-0 mt-3">
                    <div class="col-md-4 mt-4">
                        <div class="col-md-4 mt-4">
                            <img src="https://cdn.sforum.vn/sforum/wp-content/uploads/2023/10/avatar-trang-4.jpg" class="img-fluid rounded-start " style="width: 100%;margin-left: 90px;margin-top: 40px;  border-radius: 10px" alt="Ảnh sản phẩm">
                        </div>
                    </div>
                    <div class="col-md-8">
                        <div class="card-body">
                            <h5 class="card-title d-flex" id="userName">${user.fullname}</h5>
                            <div class="row">
                                <div class="col-md-6 d-flex">
                                    <div class="col-md-6">
                                        <p><strong>Mã người dùng: </strong></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p id="userID" style="margin-left: 30px;">${user.userId}</p>
                                    </div>
                                </div>
                                <div class="col-md-6 d-flex">
                                    <div class="col-md-6">
                                        <p><strong>Password: </strong></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p id="userPassword" style="margin-left: 30px;">${user.password}</p>
                                    </div>
                                </div>
                            </div>

                            <div class="row">
                                <div class="col-md-6 d-flex">
                                    <div class="col-md-6">
                                        <p><strong>Số lần login: </strong></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p id="userLoginCount" style="margin-left: 30px;">${user.loginCount}</p>
                                    </div>
                                </div>
                                <div class="col-md-6 d-flex">
                                    <div class="col-md-6">
                                        <p><strong>Vai trò: </strong></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p id="userRole" style="margin-left: 30px;">${user.role.roleName}</p>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6 d-flex">
                                    <div class="col-md-6">
                                        <p><strong>Trạng thái: </strong></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p id="userIsactive" style="margin-left: 30px;">
                                            <c:choose>
                                            <c:when test="${user.active == 1}">
                                              Hoạt động
                                        </c:when>
                                        <c:otherwise>
                                               Ngừng hoạt động
                                        </c:otherwise>
                                        </c:choose>
                                        </p>
                                    </div>

                                </div>
                                <div class="col-md-6 d-flex">
                                    <div class="col-md-6">
                                        <p><strong>Giới tính: </strong></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p id="userGender" style="margin-left: 30px;">${user.gender}</p>
                                    </div>
                                </div>

                            </div>
                            <div class="row">
                                <div class="col-md-6 d-flex">
                                    <div class="col-md-6">
                                        <p><strong>Số điện thoại: </strong></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p id="userPhoneNumber" style="margin-left: 30px;">${user.phoneNumber}</p>
                                    </div>
                                </div>
                                <div class="col-md-6 d-flex">
                                    <div class="col-md-6">
                                        <p><strong>Ngày tạo: </strong></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p id="userCreated" style="margin-left: 30px;">${user.createDate}</p>
                                    </div>
                                </div>
                            </div>
                            <div class="row">
                                <div class="col-md-6 d-flex">
                                    <div class="col-md-6">
                                        <p><strong>Email: </strong></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p id="userEmail" style="margin-left: 30px;">${user.email}</p>
                                    </div>
                                </div>

                                <div class="col-md-6 d-flex">
                                    <div class="col-md-6">
                                        <p><strong>Địa chỉ: </strong></p>
                                    </div>
                                    <div class="col-md-6">
                                        <p id="userAddress" style="margin-left: 30px;">${user.address}</p>
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>
                </div>
            </c:if>
            <c:if test="${empty user}">
                <p class="text-muted">Không tìm người dùng nào.</p>
            </c:if>
        </div>
    </div>
    <div class="mt-4">
        <a href="admin-manage-account" class="btn btn-secondary">
            <i class="bi bi-arrow-left"></i> Quay lại danh sách
        </a>
    </div>
</main>
</body>
</html>