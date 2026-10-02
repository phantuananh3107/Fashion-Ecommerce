<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Trang chi tiết sản phẩm | TA shop</title>
</head>
<body>
    <main id="main" class="main">
        <div class="pagetitle">
            <h1>Chi tiết sản phẩm</h1>
        </div>
        <div class="card">
            <div class="card-body">
                <c:if test="${not empty product}">
                    <div class="row g-0 mt-3">
                        <div class="col-md-4 mt-4">
                            <img src="<c:url value='${product.productImage}'/>" class="img-fluid rounded-start" alt="Ảnh sản phẩm">
                        </div>
                        <div class="col-md-8">
                            <div class="card-body">
                                <h5 class="card-title d-flex" id="productName">${product.productName}</h5>
                                <div class="row">
                                    <div class="col-md-6 d-flex">
                                        <div class="col-md-6">
                                            <p><strong>Mã sản phẩm: </strong></p>
                                        </div>
                                        <div class="col-md-6">
                                            <p id="productCode" style="margin-left: 30px;">${product.productCode}</p>
                                        </div>
                                    </div>
                                    <div class="col-md-6 d-flex">
                                        <div class="col-md-6">
                                            <p><strong>Giá: </strong></p>
                                        </div>
                                        <div class="col-md-6">
                                            <p id="productPrice" style="margin-left: 30px;">${product.productPrice}đ</p>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-6 d-flex">
                                        <div class="col-md-6">
                                            <p><strong>Danh mục: </strong></p>
                                        </div>
                                        <div class="col-md-6">
                                            <p id="productCategory" style="margin-left: 30px;">${product.categoryName}</p>
                                        </div>
                                    </div>
                                    <div class="col-md-6 d-flex">
                                        <div class="col-md-6">
                                            <p><strong>Số lượng: </strong></p>
                                        </div>
                                        <div class="col-md-6">
                                            <p id="productQuantity" style="margin-left: 30px;">${product.productQuantity}</p>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <div class="col-md-6 d-flex">
                                        <div class="col-md-6">
                                            <p><strong>Màu sắc: </strong></p>
                                        </div>
                                        <div class="col-md-6">
                                            <p id="productColor" style="margin-left: 30px;">${product.productColor}</p>
                                        </div>
                                    </div>
                                    <div class="col-md-6 d-flex">
                                        <div class="col-md-6">
                                            <p><strong>Kích cỡ: </strong></p>
                                        </div>
                                        <div class="col-md-6">
                                            <p id="productSize" style="margin-left: 30px;">${product.productSize}</p>
                                        </div>
                                    </div>
                                </div>
                                <div class="row">
                                    <p><strong>Mô tả: </strong></p>
                                    <p>${product.productDescription}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>
                <c:if test="${empty product}">
                    <p class="text-muted">Không tìm thấy sản phẩm.</p>
                </c:if>
            </div>
        </div>
        <div class="mt-4">
            <a href="admin-manage-product" class="btn btn-secondary">
                <i class="bi bi-arrow-left"></i> Quay lại danh sách
            </a>
        </div>
    </main>
</body>
</html>