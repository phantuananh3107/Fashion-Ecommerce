<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Trang chi tiết người dùng | TA shop</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.5/font/bootstrap-icons.css">

</head>
<body>
<style>
    /* CSS để hỗ trợ hiệu ứng hover */
    .user-info {
        transition: transform 0.3s ease, box-shadow 0.3s ease;
    }
    .user-info:hover {
        transform: scale(1.01); /* Phóng to 1% */
        box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
    }
</style>
<main id="main" style="padding: 20px; font-family: Arial, sans-serif; max-width: 1100px; margin: 100px 30px 20px 350px; background-color: #ffffff; border-radius: 20px;">
    <h2 style="text-align: center; margin-bottom: 20px; font-size: 35px; color: #ea2424; border-bottom: 2px solid #3498db; padding-bottom: 10px;">Top 7 người dùng có nguy cơ không hoạt động</h2>

    <c:if test="${empty inactiveUsers}">
        <p style="text-align: center; color: #e74c3c; font-size: 16px;">Không có dữ liệu người dùng.</p>
    </c:if>

    <c:forEach var="user" items="${inactiveUsers}">
        <div class="user-info" style="border: 1px solid #ccc; border-radius: 10px; padding: 15px; margin-bottom: 15px; color: #2c3e50; text-align: center;
                background: linear-gradient(to bottom, #ffebee, #ffcdd2);">
            <h3 style="margin: 0 0 10px; font-size: 20px; color: #e74c3c;">${user.fullname}</h3>
            <p style="margin: 5px 0; font-size: 16px;"><strong style="color: #2980b9;">Email:</strong> ${user.email}</p>
            <p style="margin: 5px 0; font-size: 16px;"><strong style="color: #2980b9;">Số lần đăng nhập:</strong> ${user.loginCount}</p>
            <p style="margin: 5px 0; font-size: 16px;"><strong style="color: #2980b9;">Ngày tạo:</strong> ${user.createDate}</p>

            <button style="margin: 5px 0; padding: 10px 20px; background: #ffffff; border-radius: 10px; border: 2px solid ${user.active == 1 ? '#28a745' : '#dc3545'}; outline: none; font-size: 16px; font-weight: bold; color: ${user.active == 1 ? '#28a745' : '#dc3545'};">
                <c:choose>
                    <c:when test="${user.active == 1}">Đang hoạt động</c:when>
                    <c:when test="${user.active == 2}">Tạm khóa</c:when>
                    <c:when test="${user.active == 3}">Ngừng HĐ</c:when>
                    <c:otherwise>Chờ xác thực</c:otherwise>
                </c:choose>
            </button>
        </div>
    </c:forEach>

    <div style="text-align: center; margin-top: 20px;">
        <a href="admin-manage-account" style="display: inline-block; padding: 10px 20px; background-color: #3498db; color: white; text-decoration: none; border-radius: 5px; font-size: 16px;">
            <i class="bi bi-arrow-left"></i> Quay lại danh sách
        </a>
    </div>
</main>
</body>
</html>