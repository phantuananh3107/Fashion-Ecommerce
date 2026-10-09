<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Hiệu quả Gợi ý sản phẩm | Admin</title>
</head>
<body>
<main id="main" class="main">
    <div class="pagetitle">
        <h1>Báo cáo Hiệu quả Gợi ý Sản phẩm</h1>
        <nav>
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/admin-home">Home</a></li>
                <li class="breadcrumb-item active">Recommendation CTR Report</li>
            </ol>
        </nav>
    </div>

    <section class="section dashboard">
        <div class="row">

            <!-- Card 1: Tổng đơn chứa SP từ gợi ý -->
            <div class="col-xxl-6 col-md-6 mb-4">
                <div class="card info-card sales-card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">Đơn hàng từ Gợi ý <span>| Chuyển đổi</span></h5>
                        <div class="d-flex align-items-center">
                            <div class="card-icon rounded-circle d-flex align-items-center justify-content-center bg-primary text-white p-3 me-3" style="width: 50px; height: 50px;">
                                <i class="bi bi-bag-check fs-4"></i>
                            </div>
                            <div class="ps-3">
                                <h3>${recOrdersCount}</h3>
                                <span class="text-muted small pt-2">đơn hàng có chứa sản phẩm người dùng click từ khối gợi ý</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Card 2: CTR Tổng quan -->
            <div class="col-xxl-6 col-md-6 mb-4">
                <div class="card info-card revenue-card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">Thống kê Khối Gợi ý <span>| CTR (Click-Through Rate)</span></h5>
                        <div class="d-flex align-items-center">
                            <div class="card-icon rounded-circle d-flex align-items-center justify-content-center bg-success text-white p-3 me-3" style="width: 50px; height: 50px;">
                                <i class="bi bi-graph-up-arrow fs-4"></i>
                            </div>
                            <div class="ps-3">
                                <h3>${ctrList.size()} Vị trí</h3>
                                <span class="text-muted small pt-2">vị trí khối gợi ý đang hoạt động trên hệ thống</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

        </div>

        <!-- Bảng Thống kê CTR theo vị trí (Source) -->
        <div class="row mb-4">
            <div class="col-12">
                <div class="card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">CTR (Click-Through Rate) theo Vị trí Gợi ý</h5>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>Vị trí (Source)</th>
                                        <th class="text-center">Số lượt hiển thị (Impressions)</th>
                                        <th class="text-center">Số lượt click (Clicks)</th>
                                        <th class="text-center">Tỷ lệ CTR (%)</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty ctrList}">
                                            <c:forEach var="item" items="${ctrList}">
                                                <tr>
                                                    <td>
                                                        <span class="badge bg-secondary">${item.source}</span>
                                                    </td>
                                                    <td class="text-center">${item.impressions}</td>
                                                    <td class="text-center">${item.clicks}</td>
                                                    <td class="text-center fw-bold text-success">${item.ctr}%</td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="4" class="text-center text-muted py-4">Chưa có dữ liệu sự kiện gợi ý.</td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Bảng Top Sản Phẩm Được Gợi Ý -->
        <div class="row">
            <div class="col-12">
                <div class="card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">Top Sản phẩm Được Gợi ý Nhiều Nhất</h5>
                        <div class="table-responsive">
                            <table class="table table-hover align-middle">
                                <thead class="table-light">
                                    <tr>
                                        <th>Mã SP</th>
                                        <th>Hình ảnh</th>
                                        <th>Tên sản phẩm</th>
                                        <th class="text-center">Hiển thị (Impressions)</th>
                                        <th class="text-center">Click</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${not empty topRecProducts}">
                                            <c:forEach var="p" items="${topRecProducts}">
                                                <tr>
                                                    <td>#${p.productId}</td>
                                                    <td>
                                                        <img src="<c:url value='${p.productImage}'/>" alt="${p.productName}" style="width: 48px; height: 48px; object-fit: cover; border-radius: 4px;">
                                                    </td>
                                                    <td class="fw-bold">${p.productName}</td>
                                                    <td class="text-center">${p.impressions}</td>
                                                    <td class="text-center text-primary fw-bold">${p.clicks}</td>
                                                </tr>
                                            </c:forEach>
                                        </c:when>
                                        <c:otherwise>
                                            <tr>
                                                <td colspan="5" class="text-center text-muted py-4">Chưa có dữ liệu sản phẩm gợi ý.</td>
                                            </tr>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </section>
</main>
</body>
</html>
