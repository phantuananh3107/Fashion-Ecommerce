<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Trang quản lý tài khoản | TA shop</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css" rel="stylesheet">
  <style>
    /* Các class khác giữ nguyên */
    .badge-green {
      background-color: #28a745 !important;
      color: white !important;
      padding: 5px 10px !important;
      border-radius: 12px !important;
    }
    .badge-red {
      background-color: #dc3545 !important;
      color: white !important;
      padding: 5px 10px !important;
      border-radius: 12px !important;
    }
    .badge-orange {
      background-color: #f4a261 !important;
      color: white !important;
      padding: 5px 10px !important;
      border-radius: 12px !important;
    }
    .badge-secondary {
      background-color: #6c757d !important;
      color: white !important;
      padding: 5px 10px !important;
      border-radius: 12px !important;
    }
    .hidden-row {
      display: none !important;
    }
  </style>
</head>
<body>
<main class="main" id="main">
  <section class="section">
    <div class="row">
      <div class="col-12">
        <h1 class="box-title">Quản lý người dùng</h1>

        <div class="section-5">
          <form action="${pageContext.request.contextPath}/admin/admin-manage-account" method="get" style="margin: 0; padding: 0;">
            <div class="inner-wrap">
              <div class="inner-change-status">
                <div class="inner-item">
                  <select id="statusFilter" name="sortBy">
                    <option value="">-- Điều kiện --</option>
                    <option value="name" ${param.sortBy == 'name' ? 'selected' : ''}>Tên</option>
                    <option value="id" ${param.sortBy == 'id' ? 'selected' : ''}>ID</option>
                    <option value="date" ${param.sortBy == 'date' ? 'selected' : ''}>Ngày tạo</option>
                  </select>
                </div>
                <div class="inner-item">
                  <button type="submit" id="filterButton">Sắp xếp</button>
                </div>
              </div>
              <div class="inner-search">
                <i class="fa-solid fa-magnifying-glass"></i>
                <input type="text" name="keyword" value="${param.keyword}" placeholder="Tìm kiếm" />
                <button type="submit" style="display: none;"></button>
              </div>

              <div class="report-user" style="margin: auto; display: flex; align-items: center">
                <a class="btn btn-lg btn-outline-info btn-light" href="admin-report-user">
                  <i class="fa-regular fa-flag"></i><h4>Biểu đồ thống kê</h4>
                </a>
              </div>
              <div class="create-user" style="margin: auto; display: flex; align-items: center">
                <a class="btn btn-lg btn-outline-info btn-light" href="admin-add-user">
                  <i class="fa-solid fa-plus"></i><h4>Add</h4>
                </a>
              </div>
              <div class="create-user" style="margin: auto; display: flex; align-items: center">
                <a class="btn btn-lg btn-outline-info btn-light" href="inactive-users">
                  <i class="fa-solid fa-exclamation"></i><h4>Inactive</h4>
                </a>
              </div>
            </div>
          </form>
        </div>

        <div class="section-6">
          <div class="table-2" style="width: 99%">
            <table>
              <thead>
              <tr>
                <th>ID</th>
                <th>Họ tên</th>
                <th>Giới tính</th>
                <th>Email</th>
                <th>Ngày tạo</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
              </thead>
              <tbody>
              <c:set var="itemsPerPage" value="7" />
              <c:set var="totalItems" value="${fn:length(users)}" />
              <c:set var="totalPages" value="${(totalItems + itemsPerPage - 1) / itemsPerPage}" />
              <c:set var="currentPage" value="${param.page != null ? param.page : 1}" />

              <c:forEach var="user" items="${users}" varStatus="loop">
                <c:if test="${loop.index >= (currentPage - 1) * itemsPerPage and loop.index < currentPage * itemsPerPage}">
                  <tr data-user-id="${user.userId}">
                    <td>${user.userId}</td>
                    <td>${user.fullname}</td>
                    <td>${user.gender}</td>
                    <td>${user.email}</td>
                    <td>${user.createDate}</td>

                    <td class="status-cell">
                      <c:choose>
                        <c:when test="${user.active == 1}">
                          <div class="badge badge-green">Hoạt động</div>
                        </c:when>
                        <c:when test="${user.active == 2}">
                          <div class="badge badge-orange">Tạm khóa</div>
                        </c:when>
                        <c:when test="${user.active == 3}">
                          <div class="badge badge-red">Ngừng HĐ</div>
                        </c:when>
                        <c:when test="${user.active == 0}">
                          <div class="badge" style="background-color: #ffc107 !important; color: #000 !important; padding: 6px 22px !important; border-radius: 4.5px !important; font-weight: 700 !important;font-size: 12px;">Chờ xác thực</div>
                        </c:when>
                        <c:otherwise>
                          <div class="badge badge-secondary">Không xác định</div>
                        </c:otherwise>
                      </c:choose>
                    </td>

                    <td>
                      <div class="d-flex justify-content-start gap-2">
                        <a class="btn btn-sm btn-outline-info" href="${pageContext.request.contextPath}/admin/admin-detail-user?userId=${user.userId}">
                          <i class="fa-solid fa-eye"></i>
                        </a>
                        <c:if test="${not empty user.userId}">
                          <a class="btn btn-sm btn-outline-danger inner-remove" onclick="confirmDelete(event, '${user.userId}'); return false;">
                            <i class="fa-solid fa-trash-can"></i>
                          </a>
                        </c:if>
                      </div>
                    </td>
                  </tr>
                </c:if>
              </c:forEach>
              </tbody>
            </table>
          </div>
        </div>

        <nav>
          <ul class="pagination justify-content-start mt-4" id="pages">
            <c:if test="${currentPage > 1}">
              <li class="page-item">
                <a class="page-link" href="?page=${currentPage - 1}&sortBy=${param.sortBy}&keyword=${param.keyword}"><i class="fa-solid fa-chevron-left"></i></a>
              </li>
            </c:if>
            <c:forEach begin="1" end="${totalPages}" var="i">
              <li class="page-item ${currentPage == i ? 'active' : ''}">
                <a class="page-link" href="?page=${i}&sortBy=${param.sortBy}&keyword=${param.keyword}">${i}</a>
              </li>
            </c:forEach>
            <c:if test="${currentPage < totalPages}">
              <li class="page-item">
                <a class="page-link" href="?page=${currentPage + 1}&sortBy=${param.sortBy}&keyword=${param.keyword}"><i class="fa-solid fa-chevron-right"></i></a>
              </li>
            </c:if>
          </ul>
        </nav>
      </div>
    </div>
  </section>

  <script>
    // Hàm gọi API khóa User giữ nguyên
    async function confirmDelete(event, userId) {
      if (confirm('Bạn có chắc chắn muốn ngừng hoạt động người dùng này?')) {
        try {
          const response = await fetch('admin-delete-user', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `userId=` + userId
          });

          if (response.ok) {
            const row = event.target.closest('tr');
            if (row) {
              const statusCell = row.querySelector('.status-cell');
              if (statusCell) {
                statusCell.innerHTML = '<div class="badge badge-red">Ngừng HĐ</div>';
              }
              alert("Đã khóa tài khoản thành công!");
            }
          } else {
            alert("Lỗi khi cập nhật trạng thái! (Có thể do lỗi Database)");
          }
        } catch (error) {
          console.error("Lỗi:", error);
          alert("Lỗi kết nối đến máy chủ.");
        }
      }
    }
  </script>
</body>
</html>