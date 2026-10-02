<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chỉnh sửa sản phẩm | TA shop</title>
</head>
<body>
<main id="main" class="main">
    <div class="pagetitle">
        <h1>Chỉnh sửa sản phẩm</h1>
    </div>
    <div class="card">
        <div class="card-body">
            <c:if test="${not empty error}">
                <div class="alert alert-danger fade show">${error}</div>
            </c:if>
            <form id="formUpdateProductIvy" action="${pageContext.request.contextPath}/admin/admin-repair-product" method="post" enctype="multipart/form-data">
                <input type="hidden" name="productId" value="${product.productId}">

                <input type="hidden" name="oldImage1" value="${product.productImage1}">
                <input type="hidden" name="oldImage2" value="${product.productImage2}">
                <input type="hidden" name="oldImage3" value="${product.productImage3}">
                <input type="hidden" name="oldImage4" value="${product.productImage4}">

                <div class="row mt-4 mb-4">
                    <div class="col-md-6 d-flex align-items-center position-relative mb-3">
                        <label for="productName" class="form-label" style="width: 150px;">Tên sản phẩm</label>
                        <input type="text" class="form-control" id="productName" name="productName" value="${product.productName}" style="margin-left: 40px;">
                    </div>
                    <div class="col-md-6 d-flex align-items-center position-relative mb-3">
                        <label for="productCode" class="form-label" style="width: 150px;">Mã sản phẩm</label>
                        <input type="text" class="form-control" id="productCode" name="productCode" value="${product.productCode}" style="margin-left: 40px;">
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-md-6 d-flex align-items-center position-relative mb-3">
                        <label for="productPrice" class="form-label" style="width: 150px;">Giá</label>
                        <input type="number" step="0.01" class="form-control" id="productPrice" name="productPrice" value="${product.productPrice}" style="margin-left: 40px;">
                    </div>

                    <div class="col-md-6 d-flex align-items-center position-relative mb-3">
                        <label for="categoryId" class="form-label" style="width: 150px;">Danh mục</label>
                        <select class="form-select" id="categoryId" name="categoryId" style="margin-left: 40px;">
                            <option value="0">Chọn danh mục</option>
                            <c:forEach var="cat" items="${categories}">
                                <option value="${cat.categoryId}" <c:if test="${cat.categoryId == product.categoryId}">selected</c:if>>
                                        ${cat.categoryName}
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-md-6 d-flex align-items-center position-relative mb-3">
                        <label for="productSize" class="form-label" style="width: 150px;">Kích cỡ</label>
                        <div style="flex-grow: 1; margin-left: 40px;">
                            <input type="text" class="form-control" id="productSize" name="productSize"
                                   value="S, M, L, XL, XXL" readonly
                                   style="background-color: #e9ecef; cursor: not-allowed; font-weight: 600; color: #495057;">
                            <div class="text-muted small mt-1">Hệ thống tự động áp dụng 5 size chuẩn.</div>
                        </div>
                    </div>

                    <div class="col-md-6 d-flex align-items-center position-relative mb-3">
                        <label for="productColor" class="form-label" style="width: 150px;">Màu sắc</label>
                        <input type="text" class="form-control" id="productColor" name="productColor" value="${product.productColor}" style="margin-left: 40px;">
                    </div>
                </div>

                <div class="row mb-3">
                    <div class="col-md-6 d-flex align-items-center position-relative mb-3">
                        <label for="productQuantity" class="form-label" style="width: 150px;">Số lượng</label>
                        <input type="number" class="form-control" id="productQuantity" name="productQuantity" value="${product.productQuantity}" style="margin-left: 40px;">
                    </div>

                    <div class="col-md-6 d-flex align-items-center position-relative mb-3">
                        <label for="productImage" class="form-label" style="width: 150px;">Ảnh sản phẩm</label>
                        <div style="flex-grow: 1; margin-left: 40px;">
                            <input class="form-control mb-2" type="file" id="productImage" name="productImage" multiple accept="image/*">
                            <div class="text-muted small mb-2">Để trống nếu không muốn thay đổi ảnh. Nhấn giữ <b>Ctrl</b> để chọn lại 4 ảnh mới.</div>

                            <div class="d-flex flex-wrap gap-2">
                                <c:if test="${not empty product.productImage1}"><img src="${pageContext.request.contextPath}${product.productImage1}" style="height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #ccc;"></c:if>
                                <c:if test="${not empty product.productImage2}"><img src="${pageContext.request.contextPath}${product.productImage2}" style="height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #ccc;"></c:if>
                                <c:if test="${not empty product.productImage3}"><img src="${pageContext.request.contextPath}${product.productImage3}" style="height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #ccc;"></c:if>
                                <c:if test="${not empty product.productImage4}"><img src="${pageContext.request.contextPath}${product.productImage4}" style="height: 60px; object-fit: cover; border-radius: 4px; border: 1px solid #ccc;"></c:if>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="row mb-3 position-relative">
                    <label for="productDescription" class="col-md-4 col-lg-3 col-form-label" style="width: 150px;">Mô tả sản phẩm</label>
                    <div class="col-md-8 col-lg-9" style="width: 1059px;">
                        <textarea class="tinymce-editor" id="productDescription" name="productDescription">${product.productDescription}</textarea>
                    </div>
                </div>

                <div class="d-flex justify-content-between mt-4">
                    <a href="admin-manage-product" class="btn btn-secondary">
                        <i class="bi bi-arrow-left"></i> Quay lại danh sách
                    </a>
                    <button type="submit" class="btn btn-success btn-update-product">Cập nhật sản phẩm</button>
                </div>
            </form>
        </div>
    </div>
</main>
<script>
    document.addEventListener("DOMContentLoaded", function() {
        // setTimeout(function() {
        //     let alerts = document.querySelectorAll('.alert');
        //     alerts.forEach(function(alert) {
        //         alert.classList.remove('show');
        //         setTimeout(() => alert.remove(), 200);
        //     });
        // }, 3000);
    });
</script>
</body>
</html>