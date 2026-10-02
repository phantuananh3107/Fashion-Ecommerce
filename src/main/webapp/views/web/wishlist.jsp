<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<div class="container mt-5 mb-5" style="min-height: 70vh;">
    <div class="row">
        <div class="col-md-3">
            <div class="sidebar-wrapper">
                <div class="sidebar-user-profile d-flex align-items-center mb-3 pb-3 border-bottom">
                    <div class="user-avatar text-secondary me-3">
                        <i class="fa-solid fa-circle-user fa-3x"></i>
                    </div>
                    <div class="user-info">
                        <h6 class="mb-0 fw-bold">${user.fullname}</h6>
                    </div>
                </div>

                <div class="list-group sidebar-menu border-0">
                    <a href="${pageContext.request.contextPath}/customer/profile" class="list-group-item list-group-item-action border-0 mb-1">
                        <i class="fa-regular fa-user me-2"></i> Thông tin tài khoản
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/order-history" class="list-group-item list-group-item-action border-0 mb-1">
                        <i class="fa-solid fa-clock-rotate-left me-2"></i> Quản lý đơn hàng
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/wishlist" class="list-group-item list-group-item-action active-menu border-0 mb-1 fw-bold">
                        <i class="fa-solid fa-heart me-2"></i> Danh sách yêu thích
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/address" class="list-group-item list-group-item-action border-0 mb-1">
                        <i class="fa-solid fa-location-dot me-2"></i> Sổ địa chỉ
                    </a>
                </div>
            </div>
        </div>

        <div class="col-md-9">
            <h3 class="mb-4 fw-bold">Danh sách yêu thích</h3>

            <c:choose>
                <c:when test="${empty wishlistProducts}">
                    <div class="alert alert-info text-center py-5">
                        <i class="fa-regular fa-heart fa-3x mb-3 text-secondary"></i>
                        <h5>Bạn chưa có sản phẩm yêu thích nào.</h5>
                        <a href="${pageContext.request.contextPath}/home" class="btn btn-dark mt-2">Tiếp tục mua sắm</a>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="row">
                        <c:forEach var="item" items="${wishlistProducts}">
                            <div class="col-md-4 mb-4">

                                <div class="product-item" style="position: relative;">
                                    <div class="inner-image">
                                        <a href="<c:url value='/public/product-detail?id=${item.productId}'/>">
                                            <img src="<c:url value='${item.productImage}'/>" alt="${item.productName}">
                                            <img src="<c:url value='${item.productImage}'/>" alt="${item.productName}" class="hover-img">
                                        </a>
                                    </div>
                                    <div class="inner-content">
                                        <div class="inner-meta">
                                            <div class="inner-color">
                                                <div class="inner-box" style="background-color: ${item.productColor};"></div>
                                                <div class="inner-box">
                                                    <i class="bi bi-check2"></i>
                                                </div>
                                            </div>

                                            <div class="inner-favorite" style="cursor: pointer;" onclick="toggleWishlist('${item.productCode}', this)">
                                                <i class="bi bi-heart-fill text-danger fs-5"></i>
                                            </div>
                                        </div>

                                        <h3 class="inner-title">
                                            <a href="<c:url value='/public/product-detail?id=${item.productId}'/>">${item.productName}</a>
                                        </h3>

                                        <div class="price-product">
                                            <div class="inner-price">
                                                <div class="inner-price-new">
                                                    <fmt:formatNumber value="${item.productPrice}" pattern="#,###"/>đ
                                                </div>
                                                <div class="inner-price-old">
                                                    <fmt:formatNumber value="${item.productPrice * 1.3}" pattern="#,###"/>đ
                                                </div>
                                            </div>

                                            <div class="inner-bag dropdown">
                                                <a href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false" data-bs-offset="0, 20">
                                                    <i class="bi bi-bag"></i>
                                                </a>
                                                <ul class="dropdown-menu">
                                                    <li><a class="dropdown-item" href="#" data-product-id="${item.productCode}" data-size="S">S</a></li>
                                                    <li><a class="dropdown-item" href="#" data-product-id="${item.productCode}" data-size="M">M</a></li>
                                                    <li><a class="dropdown-item" href="#" data-product-id="${item.productCode}" data-size="L">L</a></li>
                                                    <li><a class="dropdown-item" href="#" data-product-id="${item.productCode}" data-size="XL">XL</a></li>
                                                </ul>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
    function toggleWishlist(productCode, element) {
        fetch('${pageContext.request.contextPath}/api/wishlist/toggle', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'productCode=' + encodeURIComponent(productCode)
        })
            .then(response => response.json())
            .then(data => {
                if (data.status === 'unauthorized') {
                    Swal.fire({
                        icon: 'warning',
                        title: 'Chưa đăng nhập',
                        text: 'Vui lòng đăng nhập để sử dụng tính năng này!',
                        showCancelButton: true,
                        confirmButtonText: 'Đăng nhập',
                        cancelButtonText: 'Đóng'
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '${pageContext.request.contextPath}/public/login';
                        }
                    });
                } else if (data.status === 'added' || data.status === 'removed') {
                    Swal.fire({
                        position: 'bottom-end',
                        icon: data.status === 'added' ? 'success' : 'info',
                        title: data.message,
                        showConfirmButton: false,
                        timer: 1500,
                        toast: true
                    });

                    // Xóa thẻ card sản phẩm khỏi màn hình một cách mượt mà khi bỏ tim
                    if (data.status === 'removed') {
                        let cardColumn = element.closest('.col-md-4');
                        // Hiệu ứng mờ dần rồi xóa
                        cardColumn.style.transition = "opacity 0.4s ease";
                        cardColumn.style.opacity = 0;
                        setTimeout(() => {
                            cardColumn.remove();
                            // Nếu xóa hết thì reload lại trang để hiện thông báo "Chưa có sản phẩm nào"
                            if (document.querySelectorAll('.col-md-4').length === 0) {
                                window.location.reload();
                            }
                        }, 400);
                    }
                }
            })
            .catch(error => console.error('Error:', error));
    }
</script>