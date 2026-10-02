<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="container mt-5 mb-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card shadow-sm">
                <div class="card-header bg-dark text-white text-center">
                    <h4 class="mb-0">HỒ SƠ CỦA TÔI</h4>
                </div>
                <div class="card-body p-4">

                    <c:if test="${not empty message}">
                        <div class="alert alert-success fw-bold">${message}</div>
                    </c:if>
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger fw-bold">${error}</div>
                    </c:if>

                    <form action="${pageContext.request.contextPath}/customer/profile" method="POST">

                        <div class="mb-3">
                            <label class="form-label fw-bold">Email đăng nhập</label>
                            <input type="email" class="form-control" value="${userInfo.email}" readonly style="background-color: #e9ecef;">
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Họ và tên</label>
                            <input type="text" class="form-control" name="fullname" value="${userInfo.fullname}" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Số điện thoại</label>
                            <input type="text" class="form-control" name="phone" value="${userInfo.phoneNumber}">
                        </div>

                        <div class="mb-3">
                            <label class="form-label fw-bold">Giới tính</label>
                            <select class="form-select" name="gender">
                                <option value="Nam" ${userInfo.gender == 'Nam' ? 'selected' : ''}>Nam</option>
                                <option value="Nữ" ${userInfo.gender == 'Nữ' ? 'selected' : ''}>Nữ</option>
                                <option value="Khác" ${userInfo.gender == 'Khác' ? 'selected' : ''}>Khác</option>
                            </select>
                        </div>

                        <div class="mb-4">
                            <label class="form-label fw-bold">Địa chỉ</label>
                            <textarea class="form-control" name="address" rows="3">${userInfo.address}</textarea>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-dark btn-lg">LƯU THAY ĐỔI</button>
                        </div>

                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

