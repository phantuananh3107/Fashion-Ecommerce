<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${categoryName} | TA shop</title>
    <style>
        /* Thêm style để khi click vào dấu tích hiện ra mượt mà */
        .color-label.active {
            border-color: #000 !important;
            box-shadow: 0 0 0 1px #fff, 0 0 0 2px #000 !important;
        }
    </style>
</head>
<body>

<div class="breadcumb">
    <div class="container">
        <nav class="inner-links">
            <ul>
                <li><a href="${pageContext.request.contextPath}/public/trang-chu">Trang chủ <i class="fa-solid fa-minus"></i></a></li>
                <li><a href="#" style="text-transform: uppercase;">${categoryName}</a></li>
            </ul>
        </nav>
    </div>
</div>
<div class="section-4">
    <div class="container">
        <div class="inner-wrap">

            <form action="" method="GET">
                <input type="hidden" name="id" value="${param.id}">

                <div class="box-filter">
                    <ul>
                        <li>
                            <a href="#collapseColor" data-bs-toggle="collapse" role="button" aria-expanded="${not empty param.color ? 'true' : 'false'}" aria-controls="collapseColor">
                                Màu sắc <i class="fa-solid fa-plus"></i>
                            </a>
                            <%-- SỬA: Nếu param.color không trống (đang lọc màu) thì thêm class 'show' --%>
                            <div class="collapse ${not empty param.color ? 'show' : ''}" id="collapseColor">
                                <div class="inner-wrap-item" id="colorFilterGroup" style="display: flex; flex-wrap: wrap; gap: 6px; padding: 5px 0;">
                                    <c:forEach var="c" items="${colorList}">
                                        <c:set var="bgColor" value="#e0e0e0" />
                                        <c:set var="textLower" value="${c.toLowerCase()}" />

                                        <c:choose>
                                            <%-- (Giữ nguyên toàn bộ logic c:choose màu sắc của bạn) --%>
                                            <c:when test="${textLower.contains('trắng ngà')}"><c:set var="bgColor" value="#F0F0E0" /></c:when>
                                            <c:when test="${textLower.contains('trắng')}"><c:set var="bgColor" value="#FFFFFF" /></c:when>
                                            <c:when test="${textLower.contains('be vàng')}"><c:set var="bgColor" value="#F0E68C" /></c:when>
                                            <c:when test="${textLower.contains('be sáng')}"><c:set var="bgColor" value="#F5F5DC" /></c:when>
                                            <c:when test="${textLower.contains('be')}"><c:set var="bgColor" value="#E5D3B3" /></c:when>
                                            <c:when test="${textLower.contains('bạc hà') || textLower.contains('xanh bơ')}"><c:set var="bgColor" value="#A3D2CA" /></c:when>
                                            <c:when test="${textLower.contains('xanh bầu trời')}"><c:set var="bgColor" value="#87CEEB" /></c:when>
                                            <c:when test="${textLower.contains('xanh dương') || textLower.contains('xanh thẫm')}"><c:set var="bgColor" value="#0000FF" /></c:when>
                                            <c:when test="${textLower.contains('xanh')}"><c:set var="bgColor" value="#3498DB" /></c:when>
                                            <c:when test="${textLower.contains('hồng san hô') || textLower.contains('hồng lòng tôm')}"><c:set var="bgColor" value="#FF7F50" /></c:when>
                                            <c:when test="${textLower.contains('hồng nhạt')}"><c:set var="bgColor" value="#FFB6C1" /></c:when>
                                            <c:when test="${textLower.contains('hồng cánh sen') || textLower.contains('hồng thắm')}"><c:set var="bgColor" value="#FF007F" /></c:when>
                                            <c:when test="${textLower.contains('hồng')}"><c:set var="bgColor" value="#FF69B4" /></c:when>
                                            <c:when test="${textLower.contains('đỏ tươi') || textLower.contains('đỏ đô')}"><c:set var="bgColor" value="#C21807" /></c:when>
                                            <c:when test="${textLower.contains('đỏ mận')}"><c:set var="bgColor" value="#800000" /></c:when>
                                            <c:when test="${textLower.contains('vàng bơ') || textLower.contains('vàng nhạt')}"><c:set var="bgColor" value="#F9E79F" /></c:when>
                                            <c:when test="${textLower.contains('vàng hoa cúc') || textLower.contains('vàng nghệ')}"><c:set var="bgColor" value="#FFC300" /></c:when>
                                            <c:when test="${textLower.contains('vàng')}"><c:set var="bgColor" value="#FFD700" /></c:when>
                                            <c:when test="${textLower.contains('cam đất') || textLower.contains('cam thẫm')}"><c:set var="bgColor" value="#D35400" /></c:when>
                                            <c:when test="${textLower.contains('cam')}"><c:set var="bgColor" value="#E67E22" /></c:when>
                                            <c:when test="${textLower.contains('đen')}"><c:set var="bgColor" value="#111111" /></c:when>
                                            <c:when test="${textLower.contains('nâu')}"><c:set var="bgColor" value="#8B4513" /></c:when>
                                            <c:when test="${textLower.contains('ghi sáng') || textLower.contains('xám nhẹ')}"><c:set var="bgColor" value="#BDC3C7" /></c:when>
                                            <c:when test="${textLower.contains('ghi đậm') || textLower.contains('xám chì')}"><c:set var="bgColor" value="#7F8C8D" /></c:when>
                                            <c:when test="${textLower.contains('tím nhạt') || textLower.contains('hoa oải hương')}"><c:set var="bgColor" value="#E6E6FA" /></c:when>
                                            <c:when test="${textLower.contains('tím')}"><c:set var="bgColor" value="#8E44AD" /></c:when>
                                        </c:choose>

                                        <label class="color-label ${param.color == c ? 'active' : ''}" title="${c}"
                                               style="cursor: pointer; display: inline-block; width: 20px; height: 20px; border-radius: 50%; background-color: ${bgColor}; border: 1px solid #ddd; position: relative; transition: all 0.2s;
                                                       ${param.color == c ? 'border-color: #000; box-shadow: 0 0 0 1px #fff, 0 0 0 2px #000;' : ''}">
                                            <input type="radio" name="color" value="${c}" style="display: none;"
                                                ${param.color == c ? 'checked' : ''} onclick="updateColorUI(this)">
                                            <i class="fa-solid fa-check check-icon"
                                               style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); font-size: 10px;
                                                       display: ${param.color == c ? 'block' : 'none'};
                                                       color: ${bgColor == '#ffffff' || bgColor == '#F0F0E0' || bgColor == '#F5F5DC' ? '#000' : '#fff'};"></i>
                                        </label>
                                    </c:forEach>
                                </div>
                            </div>
                        </li>

                        <%-- MỨC GIÁ --%>
                        <li>
                            <a href="#collapsePrice" data-bs-toggle="collapse" role="button" aria-expanded="${(not empty param.minPrice || not empty param.maxPrice) ? 'true' : 'false'}" aria-controls="collapsePrice">
                                Mức giá <i class="fa-solid fa-plus"></i>
                            </a>
                            <%-- SỬA: Nếu có nhập giá min hoặc max thì tự xổ ra sau khi load trang --%>
                            <div class="collapse ${(not empty param.minPrice || not empty param.maxPrice) ? 'show' : ''}" id="collapsePrice">
                                <div style="padding: 10px 0; display: flex; align-items: center; justify-content: space-between;">
                                    <input type="number" name="minPrice" value="${param.minPrice}"
                                           step="200000" min="0" placeholder="Từ(VNĐ)"
                                           style="width: 45%; padding: 5px; border: 1px solid #ccc; border-radius: 4px;">
                                    <span>-</span>
                                    <input type="number" name="maxPrice" value="${param.maxPrice}"
                                           step="200000" min="0" placeholder="Đến(VNĐ)"
                                           style="width: 45%; padding: 5px; border: 1px solid #ccc; border-radius: 4px;">
                                </div>
                            </div>
                        </li>

                        <%-- CHIẾT KHẤU --%>
                        <li>
                            <a href="#collapsePercent" data-bs-toggle="collapse" role="button" aria-expanded="${not empty param.discountType ? 'true' : 'false'}" aria-controls="collapsePercent">
                                Mức chiết khấu <i class="fa-solid fa-plus"></i>
                            </a>
                            <%-- SỬA: Nếu có chọn discountType thì tự xổ ra --%>
                            <div class="collapse ${not empty param.discountType ? 'show' : ''}" id="collapsePercent">
                                <div class="inner-wrap-percent">
                                    <div class="inner-item-percent">
                                        <input type="radio" name="discountType" value="1" id="percent1" ${param.discountType == '1' ? 'checked' : ''}>
                                        <label for="percent1">Dưới 30%</label>
                                    </div>
                                    <div class="inner-item-percent">
                                        <input type="radio" name="discountType" value="2" id="percent2" ${param.discountType == '2' ? 'checked' : ''}>
                                        <label for="percent2">Từ 30% - 50%</label>
                                    </div>
                                    <div class="inner-item-percent">
                                        <input type="radio" name="discountType" value="3" id="percent3" ${param.discountType == '3' ? 'checked' : ''}>
                                        <label for="percent3">Trên 50%</label>
                                    </div>
                                </div>
                            </div>
                        </li>
                    </ul>
                    <div class="inner-actions">
                        <div class="inner-button">
                            <a href="?" class="button-outline" style="text-decoration: none;">Bỏ lọc</a>
                        </div>
                        <div class="inner-button">
                            <button type="submit" class="button-main" style="border: none; cursor: pointer; width: 100%;">Lọc</button>
                        </div>
                    </div>
                </div>
            </form>

            <div class="overlay-filter"></div>

            <div class="list-product">
                <%-- ... (Phần Header List Sản phẩm giữ nguyên của bạn) ... --%>
                <div class="inner-head">
                    <div class="inner-title" style="text-transform: uppercase;">${categoryName}</div>
                    <%-- (Code Sort giữ nguyên) --%>
                    <div class="inner-button-filter">
                        <i class="fa-solid fa-filter"></i>
                    </div>
                </div>

                <div class="wrap-item">
                    <c:forEach var="product" items="${products}">
                        <%-- (Phần Product Item giữ nguyên 100% của bạn) --%>
                        <c:url value="/public/product-detail" var="detailUrl">
                            <c:param name="id" value="${product.productId}" />
                        </c:url>
                        <c:url value="${product.productImage1}" var="imgUrl" />
                        <div class="product-item">
                            <c:choose>
                                <c:when test="${product.discountPercent > 0}">
                                    <div class="inner-tag tag-sale" style="background-color: #dc3545; color: #fff; font-weight: bold;">
                                        -${product.discountPercent}%
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="inner-tag tag-uppercase">New</div>
                                </c:otherwise>
                            </c:choose>
                            <div class="inner-image">
                                <a href="${detailUrl}">
                                    <img src="${imgUrl}" alt="${product.productName}">
                                    <img src="${imgUrl}" alt="${product.productName}" class="hover-img">
                                </a>
                            </div>
                            <div class="inner-content">
                                <h3 class="inner-title"><a href="${detailUrl}">${product.productName}</a></h3>
                                <div class="price-product">
                                    <div class="inner-price">
                                        <c:choose>
                                            <c:when test="${product.discountPercent > 0}">
                                                <div class="inner-price-new"><fmt:formatNumber value="${product.productPrice - (product.productPrice * product.discountPercent / 100)}" pattern="###,###"/>đ</div>
                                                <div class="inner-price-old"><fmt:formatNumber value="${product.productPrice}" pattern="###,###"/>đ</div>
                                            </c:when>
                                            <c:otherwise>
                                                <div class="inner-price-new"><fmt:formatNumber value="${product.productPrice}" pattern="###,###"/>đ</div>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <%-- PHÂN TRANG THÔNG MINH --%>
                <nav class="pagination">
                    <ul>
                        <c:forEach begin="1" end="${totalPages}" var="pageNum">
                            <li>
                                <c:url value="" var="pageUrl">
                                    <c:if test="${not empty param.id}"><c:param name="id" value="${param.id}"/></c:if>
                                    <c:if test="${not empty param.color}"><c:param name="color" value="${param.color}"/></c:if>
                                    <c:if test="${not empty param.minPrice}"><c:param name="minPrice" value="${param.minPrice}"/></c:if>
                                    <c:if test="${not empty param.maxPrice}"><c:param name="maxPrice" value="${param.maxPrice}"/></c:if>
                                    <c:if test="${not empty param.discountType}"><c:param name="discountType" value="${param.discountType}"/></c:if>
                                    <c:param name="page" value="${pageNum}" />
                                </c:url>
                                <a href="${pageUrl}" class="${pageNum == currentPage ? 'active' : ''}">${pageNum}</a>
                            </li>
                        </c:forEach>
                    </ul>
                </nav>
            </div>
        </div>
    </div>
</div>

<script>
    function updateColorUI(input) {
        // Tìm đến đúng nhóm chứa các ô màu
        const group = document.getElementById('colorFilterGroup');
        if(!group) return;

        const labels = group.querySelectorAll('.color-label');

        // 1. Reset tất cả các ô màu
        labels.forEach(label => {
            label.classList.remove('active'); // Xóa class active
            label.style.borderColor = '#ddd';
            label.style.boxShadow = 'none';
            const icon = label.querySelector('.check-icon');
            if(icon) icon.style.display = 'none';
        });

        // 2. Kích hoạt ô vừa click
        const parent = input.parentElement;
        parent.classList.add('active'); // Thêm class active
        parent.style.borderColor = '#000';
        parent.style.boxShadow = '0 0 0 1px #fff, 0 0 0 2px #000';
        const currentIcon = parent.querySelector('.check-icon');
        if(currentIcon) currentIcon.style.display = 'block';
    }
</script>
</body>
</html>