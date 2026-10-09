<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@page import="model.UserObject"%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Chi tiết sản phẩm | TA shop</title>
</head>
<body>
<div class="product-detail">
    <div class="container">
        <div class="box-breadcumb">
            <nav>
                <ul>
                    <li><a href="<c:url value='/trang-chu'/>">Trang chủ<i class="fa-solid fa-minus"></i></a></li>
                    <li><a href="#">${product.categoryName}<i class="fa-solid fa-minus"></i></a></li>
                    <li><a href="#">${product.productName}</a></li>
                </ul>
            </nav>
        </div>
        <c:if test="${not empty product}">
            <div class="row">
                <div class="col-md-6">
                    <div class="product-detail__gallery">
                        <div class="product-gallery__slide">
                            <div class="product-gallery__slide-big">
                                <div id="carouselExampleIndicators" class="carousel slide">
                                    <div class="carousel-indicators">
                                        <button type="button" data-bs-target="#carouselExampleIndicators" data-bs-slide-to="0" class="active" aria-current="true" aria-label="Slide 1"></button>
                                        <button type="button" data-bs-target="#carouselExampleIndicators" data-bs-slide-to="1" aria-label="Slide 2"></button>
                                        <button type="button" data-bs-target="#carouselExampleIndicators" data-bs-slide-to="2" aria-label="Slide 3"></button>
                                        <button type="button" data-bs-target="#carouselExampleIndicators" data-bs-slide-to="3" aria-label="Slide 4"></button>
                                    </div>
                                    <div class="carousel-inner">
                                        <div class="carousel-item active">
                                            <img src="<c:url value='${product.productImage}'/>" class="d-block w-100" alt="...">
                                        </div>
                                        <div class="carousel-item">
                                            <img src="<c:url value='${product.productImage2}'/>" class="d-block w-100" alt="...">
                                        </div>
                                        <div class="carousel-item">
                                            <img src="<c:url value='${product.productImage3}'/>" class="d-block w-100" alt="...">
                                        </div>
                                        <div class="carousel-item">
                                            <img src="<c:url value='${product.productImage4}'/>" class="d-block w-100" alt="...">
                                        </div>
                                    </div>
                                    <button class="carousel-control-prev" type="button" data-bs-target="#carouselExampleIndicators" data-bs-slide="prev">
                                        <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                                        <span class="visually-hidden">Previous</span>
                                    </button>
                                    <button class="carousel-control-next" type="button" data-bs-target="#carouselExampleIndicators" data-bs-slide="next">
                                        <span class="carousel-control-next-icon" aria-hidden="true"></span>
                                        <span class="visually-hidden">Next</span>
                                    </button>
                                </div>
                            </div>
                            <div class="product-gallery__slide-small">
                                <div class="swiper-wraper">
                                    <div class="swiper-slide">
                                        <img src="<c:url value='${product.productImage}'/>" alt="">
                                    </div>
                                    <div class="swiper-slide">
                                        <img src="<c:url value='${product.productImage2}'/>" alt="">
                                    </div>
                                    <div class="swiper-slide">
                                        <img src="<c:url value='${product.productImage3}'/>" alt="">
                                    </div>
                                    <div class="swiper-slide">
                                        <img src="<c:url value='${product.productImage4}'/>" alt="">
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="product-detail__information">
                        <h1>${product.productName}</h1>
                        <div class="product-detail__sub-infor">
                            <span>${product.productCode}</span>
                            <div class="product-detail__rating">
                                <div class="product-detail__rating-star">
                                    <i class="fa-solid fa-star"></i>
                                    <i class="fa-solid fa-star"></i>
                                    <i class="fa-solid fa-star"></i>
                                    <i class="fa-solid fa-star"></i>
                                    <i class="fa-solid fa-star"></i>
                                </div>
                                <span>(0 đánh giá)</span>
                            </div>
                        </div>
                        <div class="product-detail__price">
                            <c:choose>
                                <%-- TRƯỜNG HỢP CÓ GIẢM GIÁ --%>
                                <c:when test="${product.discountPercent > 0}">
                                    <b style="color: #dc3545; font-size: 24px;">
                                        <fmt:formatNumber value="${product.productPrice - (product.productPrice * product.discountPercent / 100)}" pattern="###,###"/>đ
                                    </b>
                                    <span style="text-decoration: line-through; color: #999; font-size: 16px; margin-left: 10px; font-weight: normal;">
                                        <fmt:formatNumber value="${product.productPrice}" pattern="###,###"/>đ
                                    </span>
                                    <span style="background: #dc3545; color: #fff; padding: 2px 8px; border-radius: 4px; font-size: 14px; margin-left: 10px; vertical-align: middle;">
                                        -${product.discountPercent}%
                                   </span>
                                </c:when>

                                <%-- TRƯỜNG HỢP GIÁ GỐC (KHÔNG GIẢM GIÁ) --%>
                                <c:otherwise>
                                    <b style="font-size: 24px;">
                                        <fmt:formatNumber value="${product.productPrice}" pattern="###,###"/>đ
                                    </b>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="product-detail__color">
                            <p>Màu sắc:${product.productColor}</p>
                            <div class="product-detail__color-check">
                                <button class="check-empty"></button>
                                <button class="check"><i class="fa-solid fa-check"></i></button>
                            </div>
                        </div>
                        <div class="product-detail__size">
                            <div class="product-detail__size-input">
                                <p>S</p>
                                <p>M</p>
                                <p>L</p>
                                <p>XL</p>
                                <p>XXL</p>
                            </div>
                            <p class="check-size" onclick="openSizeModal()" style="cursor: pointer;">
                                <i class="fa-solid fa-ruler"></i> Kiểm tra size của bạn
                            </p>
                        </div>

                            <%-- LOGIC KIỂM SOÁT SỐ LƯỢNG TỒN KHO TRÊN UI --%>
                        <c:choose>
                            <c:when test="${product.productQuantity > 0}">
                                <p class="text-success mb-2 mt-2" style="font-size: 14px;">
                                    <i class="fa-solid fa-check-circle"></i> Sẵn có: ${product.productQuantity} sản phẩm
                                </p>
                                <div class="product-detail__quanlity">
                                    <h3>Số lượng</h3>
                                    <div class="product-detail__num">
                                        <div class="product-detail__num-decre"><i class="fa-solid fa-minus"></i></div>
                                            <%-- ĐÃ ĐỔI SANG ID ĐỘC LẬP ĐỂ TRÁNH XUNG ĐỘT JS --%>
                                        <input type="text" id="detailQtyInput" value="1" style="width: 50px; text-align: center; border: none; font-weight: bold; font-size: 16px; outline: none; background: transparent;">
                                        <div class="product-detail__num-incre"><i class="fa-solid fa-plus"></i></div>
                                    </div>
                                </div>
                                <div class="product-detail__action">
                                    <button class="add">Thêm vào giỏ</button>
                                    <button class="buy">Mua hàng</button>
                                    <button class="heart ${isLiked ? 'active' : ''}">
                                        <i class="bi ${isLiked ? 'bi-heart-fill text-danger' : 'bi-heart'}"></i>
                                    </button>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="product-detail__quanlity mt-3 mb-3">
                                    <div class="alert alert-danger" style="width: 100%; text-align: center; margin-bottom: 0; padding: 10px;">
                                        <i class="fa-solid fa-xmark-circle"></i> Sản phẩm này hiện đã hết hàng!
                                    </div>
                                </div>
                                <div class="product-detail__action">
                                    <button class="add" disabled style="background-color: #ccc; cursor: not-allowed; border: none; color: #666;">Thêm vào giỏ</button>
                                    <button class="buy" disabled style="background-color: #ccc; cursor: not-allowed; border: none; color: #666;">Mua hàng</button>
                                    <button class="heart ${isLiked ? 'active' : ''}">
                                        <i class="bi ${isLiked ? 'bi-heart-fill text-danger' : 'bi-heart'}"></i>
                                    </button>
                                </div>
                            </c:otherwise>
                        </c:choose>
                            <%-- KẾT THÚC LOGIC TỒN KHO TRÊN UI --%>

                        <div class="product-detail__find">
                            <a>Tìm tại cửa hàng</a>
                        </div>

                        <div class="product-detail__tab">
                            <nav class="tab-navs">
                                <ul>
                                    <li><a href="#tab1" class="active">Giới Thiệu</a></li>
                                    <li><a href="#tab2">Chi Tiết Sản Phẩm</a></li>
                                    <li><a href="#tab3">Bảo Quản</a></li>
                                </ul>
                            </nav>
                            <div class="tab-contents">
                                <div id="tab1">
                                    <div class="tab-content-inner">
                                        <p>${product.productDescription}</p>
                                        <h3>Thông tin mẫu:</h3>
                                        <h3>Chiều cao: <span>167cm</span></h3>
                                        <h3>Cân nặng: <span>50kg</span></h3>
                                        <h3>Số đo 3 vòng:  <span>83-67-99 cm</span></h3>
                                        <p>Lưu ý: Màu sắc sản phẩm thực tế sẽ có sự chênh lệch nhỏ so với ảnh do điều kiện ánh sáng khi chụp
                                            và màu sắc hiển thị qua màn hình máy tính/ điện thoại.</p>
                                    </div>
                                    <div class="show-more-btn">
                                        <i class="fa-solid fa-chevron-down"></i>
                                    </div>
                                </div>
                                <div id="tab2">
                                    <div class="tab-content-inner">
                                        <h3>Dòng sản phẩm: <span>you</span></h3>
                                        <h3>Nhóm sản phẩm:  <span>Đầm</span></h3>
                                        <h3>Cổ áo: <span>Cổ tròn</span></h3>
                                        <h3>Tay áo: <span>Không tay</span></h3>
                                        <h3>Kiểu dáng:  <span>Đầm xòe</span></h3>
                                        <h3>Độ dài:  <span>qua gối</span></h3>
                                        <h3>Họa tiết:  <span>Trơn</span></h3>
                                        <h3>Chất liệu: <span>Tuytsi</span></h3>
                                    </div>
                                    <div class="show-more-btn">
                                        <i class="fa-solid fa-chevron-down"></i>
                                    </div>
                                </div>
                                <div id="tab3">
                                    <div class="tab-content-inner">
                                        <h3>
                                            Chi tiết bảo quản:
                                        </h3>
                                        <p>* Vải dệt kim: sau khi giặt sản phẩm phải được phơi ngang tránh bai giãn.</p>
                                        <p>* Vải voan, lụa, chiffon nên giặt bằng tay.</p>
                                        <p>* Vải thô, tuytsi, kaki không có phối hay trang trí đá cườm thì có thể giặt máy.</p>
                                        <p>* Vải thô, tuytsi, kaki có phối màu tương phản hay trang trí voan, lụa, đá cườm thì cần giặt tay.</p>
                                        <p>* Đồ Jeans nên hạn chế giặt bằng máy giặt vì sẽ làm phai màu jeans.
                                            Nếu giặt thì nên lộn trái sản phẩm khi giặt, đóng khuy,
                                            kéo khóa, không nên giặt chung cùng đồ sáng màu, tránh dính màu vào các sản phẩm khác. </p>
                                    </div>
                                    <div class="show-more-btn">
                                        <i class="fa-solid fa-chevron-down"></i>
                                    </div>
                                </div>
                            </div>
                            <div class="product-detail__tab-body">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>

        <!-- Recommendation Block: Khách Mua Sản Phẩm Này Cũng Mua -->
        <c:if test="${not empty alsoBoughtProducts}">
            <div class="product-recommendations mt-5 pt-4 border-top">
                <h3 class="mb-4 font-weight-bold" style="font-size: 20px; text-transform: uppercase;">KHÁCH MUA SẢN PHẨM NÀY CŨNG MUA</h3>
                <div class="row">
                    <c:forEach var="rec" items="${alsoBoughtProducts}">
                        <c:set var="p" value="${rec.product}"/>
                        <div class="col-6 col-md-4 col-lg-2 mb-4">
                            <div class="card h-100 border-0 shadow-sm rounded">
                                <a href="<c:url value='/public/product-detail?id=${p.productId}&ref=${rec.source}'/>" class="position-relative text-decoration-none text-dark">
                                    <c:if test="${p.discountPercent > 0}">
                                        <span class="badge bg-danger position-absolute top-0 start-0 m-2">-${p.discountPercent}%</span>
                                    </c:if>
                                    <img src="<c:url value='${p.productImage}'/>" class="card-img-top" alt="${p.productName}" style="object-fit: cover; height: 180px;">
                                    <div class="card-body p-2">
                                        <div class="text-muted small mb-1">${rec.reason}</div>
                                        <h6 class="card-title text-truncate mb-1" style="font-size: 14px;">${p.productName}</h6>
                                        <div class="fw-bold text-danger" style="font-size: 14px;">
                                            <fmt:formatNumber value="${p.discountPercent > 0 ? p.productPrice * (100 - p.discountPercent) / 100 : p.productPrice}" pattern="###,###"/>đ
                                        </div>
                                    </div>
                                </a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </c:if>

        <!-- Recommendation Block: Sản Phẩm Tương Tự -->
        <c:if test="${not empty similarProducts}">
            <div class="product-recommendations mt-4 pt-3 border-top">
                <h3 class="mb-4 font-weight-bold" style="font-size: 20px; text-transform: uppercase;">SẢN PHẨM TƯƠNG TỰ</h3>
                <div class="row">
                    <c:forEach var="rec" items="${similarProducts}">
                        <c:set var="p" value="${rec.product}"/>
                        <div class="col-6 col-md-4 col-lg-2 mb-4">
                            <div class="card h-100 border-0 shadow-sm rounded">
                                <a href="<c:url value='/public/product-detail?id=${p.productId}&ref=${rec.source}'/>" class="position-relative text-decoration-none text-dark">
                                    <c:if test="${p.discountPercent > 0}">
                                        <span class="badge bg-danger position-absolute top-0 start-0 m-2">-${p.discountPercent}%</span>
                                    </c:if>
                                    <img src="<c:url value='${p.productImage}'/>" class="card-img-top" alt="${p.productName}" style="object-fit: cover; height: 180px;">
                                    <div class="card-body p-2">
                                        <h6 class="card-title text-truncate mb-1" style="font-size: 14px;">${p.productName}</h6>
                                        <div class="fw-bold text-danger" style="font-size: 14px;">
                                            <fmt:formatNumber value="${p.discountPercent > 0 ? p.productPrice * (100 - p.discountPercent) / 100 : p.productPrice}" pattern="###,###"/>đ
                                        </div>
                                    </div>
                                </a>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </div>
        </c:if>

        <c:if test="${empty product}">
            <div class="inner-wrap text-center py-5">
                <i class="fa-solid fa-box-open fa-4x text-secondary mb-3"></i>
                <h3 class="text-secondary">Sản phẩm không tồn tại hoặc đã bị xóa.</h3>
                <a href="${pageContext.request.contextPath}/home" class="btn btn-dark mt-3">Quay lại trang chủ</a>
            </div>
        </c:if>
    </div>
</div>

<%
    UserObject userObj = (UserObject) session.getAttribute("user");
    boolean isLoggedIn = (userObj != null);
%>

<script>
    $(document).ready(function() {
        const isLoggedIn = <%= isLoggedIn %>;
        // Lấy số lượng tồn kho từ Java đẩy xuống JS để chặn bấm dấu +
        const maxQuantity = parseInt('${product.productQuantity > 0 ? product.productQuantity : 0}');

        // 1. Logic chuyển tab
        $(".tab-navs a").click(function (e) {
            e.preventDefault();
            $(".tab-navs a").removeClass("active");
            $(this).addClass("active");

            let currentTab = $(this).attr("href");
            $(".tab-contents > div").hide();
            $(currentTab).show();
        });

        // 2. Logic rèm che (Nút mũi tên)
        $(".show-more-btn").click(function() {
            $(this).prev(".tab-content-inner").toggleClass("expanded");
            $(this).toggleClass("active");
        });

        // 3. Logic chọn size sản phẩm
        $(".product-detail__size-input p").click(function() {
            $(".product-detail__size-input p").removeClass("active-size");
            $(this).addClass("active-size");
        });

        // 4. Helper: Kiểm tra size đã chọn
        function isSelectedSize() {
            return $(".product-detail__size-input p.active-size").length > 0;
        }

        // ==========================================
        // 5. TĂNG/GIẢM SỐ LƯỢNG (SỬA LẠI THEO ID MỚI)
        // ==========================================
        $(".product-detail__num-incre, .product-detail__num-decre").off("click").on("click", function(e) {
            e.preventDefault();
            if (!isSelectedSize()) {
                showToast("Vui lòng chọn size sản phẩm!");
                return false;
            }

            const $quantityInput = $("#detailQtyInput"); // Lấy giá trị từ ID mới
            let currentQuantity = parseInt($quantityInput.val());

            if (isNaN(currentQuantity)) currentQuantity = 1;

            if ($(this).hasClass("product-detail__num-incre")) {
                if (currentQuantity < maxQuantity) {
                    currentQuantity++;
                } else {
                    showToast("Xin lỗi, kho chỉ còn tối đa " + maxQuantity + " sản phẩm!");
                }
            } else if ($(this).hasClass("product-detail__num-decre")) {
                if (currentQuantity > 1) {
                    currentQuantity--;
                }
            }

            $quantityInput.val(currentQuantity);
        });

        // Bắt sự kiện khi khách hàng tự gõ phím vào ô input
        $("#detailQtyInput").on("input change", function() {
            let val = parseInt($(this).val());

            // Xử lý nếu nhập số âm, số 0 hoặc chữ bậy bạ
            if (isNaN(val) || val < 1) {
                $(this).val(1);
            }
            // Xử lý nếu gõ số vượt quá tồn kho
            else if (val > maxQuantity) {
                showToast("Xin lỗi, kho chỉ còn tối đa " + maxQuantity + " sản phẩm!");
                $(this).val(maxQuantity);
            }
            // Hợp lệ
            else {
                $(this).val(val);
            }
        });

        // ==========================================
        // 6. THÊM VÀO GIỎ HÀNG (ĐÃ NGĂN CHẶN TRÙNG LẶP REQUEST)
        // ==========================================
        $(".add").off("click").on("click", function(e) {
            e.preventDefault();
            e.stopImmediatePropagation(); // CẤM MỌI FILE JS KHÁC CHẠY

            if (!isLoggedIn) {
                showToast("Vui lòng đăng nhập để thực hiện chức năng này!");
                return false;
            }

            if (!isSelectedSize()) {
                showToast("Vui lòng chọn size sản phẩm!");
                return false;
            }

            const productId = '${product.productId}';
            const selectedSize = $(".product-detail__size-input p.active-size").text().trim();
            const quantity = parseInt($("#detailQtyInput").val()); // Đọc từ ID mới

            $.ajax({
                url: '${pageContext.request.contextPath}/customer/add-to-cart',
                type: 'POST',
                dataType: 'json',
                data: {
                    product_id: productId,
                    size: selectedSize,
                    quantity: quantity
                },
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                },
                success: function(response) {
                    if (response.status === 'success') {
                        showToast('Thêm sản phẩm vào giỏ hàng thành công!');
                        if (typeof refreshCartUI === 'function') {
                            refreshCartUI(false);
                        }
                    } else {
                        showToast(response.message);
                    }
                },
                error: function() {
                    showToast('Lỗi khi thêm sản phẩm vào giỏ!');
                }
            });
        });

        // ==========================================
        // 7. MUA HÀNG NGAY (ĐÃ NGĂN CHẶN TRÙNG LẶP REQUEST)
        // ==========================================
        $(".buy").off("click").on("click", function(e) {
            e.preventDefault();
            e.stopImmediatePropagation(); // CẤM MỌI FILE JS KHÁC CHẠY

            if (!isLoggedIn) {
                showToast("Vui lòng đăng nhập để thực hiện chức năng này!");
                return false;
            }

            if (!isSelectedSize()) {
                showToast("Vui lòng chọn size sản phẩm trước khi mua hàng!");
                return false;
            }

            const productId = '${product.productId}';
            const selectedSize = $(".product-detail__size-input p.active-size").text().trim();
            const quantity = parseInt($("#detailQtyInput").val()); // Đọc từ ID mới

            $.ajax({
                url: '${pageContext.request.contextPath}/customer/add-to-cart',
                type: 'POST',
                dataType: 'json',
                data: {
                    product_id: productId,
                    size: selectedSize,
                    quantity: quantity
                },
                headers: {
                    'X-Requested-With': 'XMLHttpRequest'
                },
                success: function(response) {
                    if (response.status === 'success') {
                        window.location.href = '${pageContext.request.contextPath}/customer/cart';
                    } else {
                        showToast(response.message);
                    }
                },
                error: function() {
                    showToast('Có lỗi xảy ra, vui lòng thử lại!');
                }
            });
        });

        // 8. Logic nút "Yêu thích"
        $(".heart").off("click").on("click", function(e) {
            e.preventDefault();
            e.stopImmediatePropagation();

            if (!isLoggedIn) {
                showToast("Vui lòng đăng nhập để thực hiện chức năng này!");
                return false;
            }

            const button = $(this);
            const icon = button.find('i');
            const productCode = '${product.productCode}';

            $.ajax({
                url: '${pageContext.request.contextPath}/api/wishlist/toggle',
                type: 'POST',
                data: { productCode: productCode },
                dataType: 'json',
                success: function(response) {
                    if (response.status === 'unauthorized') {
                        showToast("Vui lòng đăng nhập lại!");
                    }
                    else if (response.status === 'added') {
                        button.addClass("active");
                        icon.removeClass("bi-heart").addClass("bi-heart-fill text-danger");
                        showToast(response.message);
                    }
                    else if (response.status === 'removed') {
                        button.removeClass("active");
                        icon.removeClass("bi-heart-fill text-danger").addClass("bi-heart");
                        showToast(response.message);
                    }
                },
                error: function() {
                    showToast('Lỗi kết nối, vui lòng thử lại!');
                }
            });
        });

        // 9. Logic Carousel zoom
        $('.carousel-item').mousemove(function(e) {
            const fare = $(this).offset();
            const x = ((e.pageX - fare.left) / $(this).width()) * 100;
            const y = ((e.pageY - fare.top) / $(this).height()) * 100;
            $(this).find('img').css('transform-origin', x + '% ' + y + '%');
        });

        $('.carousel-item').mouseleave(function() {
            $(this).find('img').css('transform-origin', 'center center');
        });

    });
</script>

    <jsp:include page="size.jsp" />
</body>
</html>