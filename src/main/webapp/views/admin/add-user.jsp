<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Trang người dùng mới | TA shop</title>
</head>
<body>
<main id="main" class="main">
  <div class="pagetitle">
    <h1>Thêm người dùng mới</h1>
  </div>
  <div class="card">
    <div class="card-body">

      <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger">${errorMessage}</div>
      </c:if>
      <c:if test="${not empty successMessage}">
        <div class="alert alert-success">${successMessage}</div>
      </c:if>
      <form id="addUserForm" action="${pageContext.request.contextPath}/admin/admin-add-user" method="post">
        <div class="row mt-4 mb-4">
          <div class="col-md-6 d-flex align-items-center position-relative mb-3">
            <label for="fullname" class="form-label" style="width: 150px;">Họ và tên</label>
            <input type="text" class="form-control" id="fullname" name="fullname" value="${requestScope.user.fullname}"required placeholder="Nguyễn Văn A" style="margin-left: 40px;">
          </div>
          <div class="col-md-6 d-flex align-items-center position-relative mb-3">
            <label for="password" class="form-label" style="width: 150px;">Password</label>
            <input type="text" class="form-control" id="password" name="password" value="${requestScope.user.password}" required placeholder="" style="margin-left: 40px;">
          </div>
        </div>
        <div class="row mb-3">
          <div class="col-md-6 d-flex align-items-center position-relative mb-3">
            <label for="email" class="form-label" style="width: 150px;">Email</label>
            <input type="text" class="form-control" id="email" name="email" value="${requestScope.user.email}" style="margin-left: 40px;" required>
          </div>
          <div class="col-md-6 d-flex align-items-center position-relative mb-3">
            <label for="gender" class="form-label" style="width: 150px;">Giới tính</label>
            <select class="form-select" id="gender" name="gender" style="margin-left: 40px;">
              <option value="">Chọn giới tính</option>
              <option value="Nam" <c:if test="${requestScope.user.gender == 'Nam'}">selected</c:if>>Nam</option>
              <option value="Nữ" <c:if test="${requestScope.user.gender == 'Nữ'}">selected</c:if>>Nữ</option>
            </select>
          </div>
        </div>
        <div class="row mb-3">
          <div class="col-md-6 d-flex align-items-center position-relative mb-3">
            <label for="phoneNumber" class="form-label" style="width: 150px;">SĐT</label>
            <input type="text" class="form-control" id="phoneNumber" name="phoneNumber" value="${requestScope.user.phoneNumber}" style="margin-left: 40px;">
          </div>
          <div class="col-md-6 d-flex align-items-center position-relative mb-3">
            <label for="address" class="form-label" style="width: 150px;">Địa chỉ</label>
            <input type="text" class="form-control" id="address" name="address" value="${requestScope.user.address}" style="margin-left: 40px;">
          </div>
        </div>
        <div class="row mb-3">

          <div class="col-md-6 d-flex align-items-center position-relative mb-3">
            <label for="roleId" class="form-label" style="width: 150px;">Vai trò</label>
            <select class="form-select" id="roleId" name="roleId" style="margin-left: 40px;">
              <option value="">Chọn vai trò</option>
              <option value="1" <c:if test="${requestScope.user.role.roleId == 1}">selected</c:if>>Quản trị viên</option>
              <option value="2" <c:if test="${requestScope.user.role.roleId == 2}">selected</c:if>>Người dùng</option>
            </select>
          </div>
        </div>
        <div class="row mb-3 position-relative">
          <button type="submit" class="btn btn-primary" style="width: 300px; margin: 0 auto">Thêm người dùng</button>
        </div>
        <div class="d-flex justify-content-between mt-4">
          <a href="admin-manage-account" class="btn btn-secondary">
            <i class="bi bi-arrow-left"></i> Quay lại danh sách
          </a>
        </div>
      </form>
    </div>
  </div>
</main>
</body>
</html>
