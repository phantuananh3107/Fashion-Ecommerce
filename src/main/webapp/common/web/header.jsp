<%@page import="model.UserObject"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@include file="/common/taglib.jsp"%>

<style>
  /* Cố định khung chứa để Mega Menu không bị tràn viền */
  .inner-wrap { position: relative; }
  .inner-menu > ul > li.has-mega { position: static; }

  .inner-menu > ul > li.has-mega:hover .mega-content {
    display: block; opacity: 1; visibility: visible;
  }

  .mega-content {
    position: absolute;
    top: 100%;
    left: 0;
    width: 100%; /* Bám chính xác theo chiều rộng của thẻ inner-wrap */
    background: #fff;
    border: 1px solid #e1e1e1; /* Viền giống Bộ sưu tập */
    border-top: none;
    box-shadow: 0 10px 20px rgba(0,0,0,0.05); /* Bóng mờ giống Bộ sưu tập */
    display: none;
    z-index: 9999;
    padding: 30px;
    margin-top: -2px; /* Xích lên trên 1 chút để nối liền với Header */
  }

  /* CẦU NỐI VÔ HÌNH: Khắc phục lỗi di chuột bị mất menu */
  .mega-content::before {
    content: ""; position: absolute; top: -15px; left: 0; width: 100%; height: 15px; background: transparent;
  }

  /* Tận dụng lại css chữ từ sub-title-menu của Bộ sưu tập */
  .mega-content .sub-title-menu { margin-bottom: 12px; }
  .mega-content .sub-title-menu a {
    font-weight: bold !important; color: #222 !important;
    text-transform: uppercase; font-size: 14px;
  }

  .mega-content ul {
    position: static !important; display: block !important;
    box-shadow: none !important; padding: 0 !important;
    border: none !important; background: transparent !important;
  }

  .mega-content ul li { margin-bottom: 8px !important; display: block !important; }
  .mega-content ul li a {
    font-weight: normal !important; color: #555 !important;
    font-size: 14px !important; text-transform: none !important; padding: 0 !important;
  }
  .mega-content ul li a:hover { color: #dc3545 !important; background: transparent !important; }

  .mobile-sub-title { font-weight: bold; color: #222; margin-top: 15px; margin-bottom: 5px; font-size: 14px; text-transform: uppercase; display: block; }

  /* 1. Bao quát toàn bộ khối tìm kiếm */
  .search-wrapper {
    position: relative;
    width: 300px; /* Bạn có thể thay đổi độ rộng tùy giao diện thực tế */
  }

  /* 2. Form chứa Input và Icon */
  .search-form {
    position: relative;
    width: 100%;
  }

  .search-form input {
    width: 100%;
    padding: 8px 35px 8px 12px; /* Chừa 35px bên phải để không bị chữ đè lên icon */
    border: 1px solid #e3e5e5;
    border-radius: 4px;
    outline: none;
    font-size: 13px;
  }

  /* Ép icon kính lúp sát mép phải ô input */
  .search-form button {
    position: absolute;
    top: 50%;
    right: 10px; /* Cách mép phải 10px */
    transform: translateY(-50%); /* Căn giữa theo chiều dọc */
    background: transparent;
    border: none;
    cursor: pointer;
    color: #222;
    padding: 0;
  }

  /* 3. Bảng gợi ý */
  .search-suggest {
    display: none;
    position: absolute;
    top: calc(100% + 3px); /* Tách khỏi ô input đúng 3px như yêu cầu */
    left: 0;
    width: 100%;
    background: #fff;
    border: 1px solid #e3e5e5;
    padding: 15px;
    z-index: 1000;
    box-shadow: 0 4px 10px rgba(0,0,0,0.08);
    border-radius: 4px;
  }

  /* Hiện bảng khi active */
  .search-wrapper.active .search-suggest {
    display: block;
  }

  /* 4. Chữ "Tìm kiếm nhiều nhất" */
  .suggest-header {
    margin-bottom: 12px;
  }

  .suggest-header strong {
    font-size: 11px; /* Chữ thu nhỏ lại */
    font-weight: 700;
    color: #333;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }

  /* 5. Các thẻ tag */
  .suggest-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .suggest-tags a {
    padding: 6px 12px;
    border: 1px solid #e3e5e5;
    font-size: 12px;
    color: #555;
    text-decoration: none;
    border-radius: 2px;
    transition: all 0.2s;
  }

  .suggest-tags a:hover {
    border-color: #222;
    color: #222;
  }
</style>

<header class="header">
  <div class="container">
    <div class="inner-wrap">
      <button class="inner-menu-mobile" data-bs-toggle="offcanvas" data-bs-target="#menu-mobile"
              aria-controls="menu-mobile">
        <i class="fa-solid fa-bars"></i>
      </button>
      <div class="offcanvas offcanvas-start" data-bs-scroll="true" tabindex="-1" id="menu-mobile">
        <div class="offcanvas-header">
          <button class="button-close-menu" data-bs-dismiss="offcanvas" aria-label="Close">
            <i class="fa-solid fa-xmark"></i>
          </button>
        </div>
        <div class="offcanvas-body">
          <div class="menu-mobile-button">
            <c:choose>
              <c:when test="${empty sessionScope.user}"><a href="${pageContext.request.contextPath}/public/login">Đăng nhập</a></c:when>
              <c:otherwise><a href="${pageContext.request.contextPath}/customer/profile">${sessionScope.user.fullname}</a></c:otherwise>
            </c:choose>
          </div>
          <nav class="menu-mobile-content">
            <ul>
              <li>
                <a data-bs-toggle="collapse" href="#sub-menu-mobile1" role="button" aria-expanded="false"
                   aria-controls="sub-menu-mobile1">
                  <span>Nữ</span>
                  <i class="fa-solid fa-plus"></i>
                </a>
                <ul class="collapse" id="sub-menu-mobile1" style="padding-left: 15px;">
                  <span class="mobile-sub-title">Áo Nữ</span>
                  <li><a href="${pageContext.request.contextPath}/category?id=1">Áo Sơ Mi</a></li>
                  <li><a href="${pageContext.request.contextPath}/category?id=2">Áo Thun</a></li>
                  <li><a href="${pageContext.request.contextPath}/category?id=3">Áo Croptop / Áo Kiểu</a></li>

                  <span class="mobile-sub-title">Set Bộ</span>
                  <li><a href="${pageContext.request.contextPath}/category?id=4">Set bộ công sở</a></li>
                  <li><a href="${pageContext.request.contextPath}/category?id=5">Set bộ thun len</a></li>

                  <span class="mobile-sub-title">Quần & Jumpsuit</span>
                  <li><a href="${pageContext.request.contextPath}/category?id=6">Quần Jeans</a></li>
                  <li><a href="${pageContext.request.contextPath}/category?id=7">Quần dài</a></li>

                  <span class="mobile-sub-title">Chân Váy</span>
                  <li><a href="${pageContext.request.contextPath}/category?id=8">Chân váy chữ A</a></li>
                  <li><a href="${pageContext.request.contextPath}/category?id=9">Chân váy Jeans</a></li>

                  <span class="mobile-sub-title">Đầm / Áo dài</span>
                  <li><a href="${pageContext.request.contextPath}/category?id=10">Đầm công sở</a></li>
                  <li><a href="${pageContext.request.contextPath}/category?id=11">Đầm dạ hội</a></li>
                </ul>
              </li>

              <li>
                <a data-bs-toggle="collapse" href="#sub-menu-mobile2" role="button" aria-expanded="false"
                   aria-controls="sub-menu-mobile2">
                  <span>Nam</span>
                  <i class="fa-solid fa-plus"></i>
                </a>
                <ul class="collapse" id="sub-menu-mobile2" style="padding-left: 15px;">
                  <span class="mobile-sub-title">Áo Nam</span>
                  <li><a href="${pageContext.request.contextPath}/category?id=12">Áo sơ mi nam</a></li>
                  <li><a href="${pageContext.request.contextPath}/category?id=13">Áo thun nam</a></li>
                  <li><a href="${pageContext.request.contextPath}/category?id=14">Áo polo</a></li>

                  <span class="mobile-sub-title">Quần Nam</span>
                  <li><a href="${pageContext.request.contextPath}/category?id=15">Quần tây</a></li>
                  <li><a href="${pageContext.request.contextPath}/category?id=16">Quần jeans</a></li>
                </ul>
              </li>

              <li><a href="saleT5" class="active"><span>ƯU ĐÃI LỚN THÁNG 5</span><i class="fa-solid fa-plus"></i></a></li>
              <li><a href="#"><span>Bộ sưu tập</span><i class="fa-solid fa-plus"></i></a></li>
              <li><a href="#"><span>LIFESTYLE</span></a></li>
              <li><a href="#"><span>Về chúng tôi</span><i class="fa-solid fa-plus"></i></a></li>
            </ul>
          </nav>
        </div>
      </div>
      <nav class="inner-menu">
        <ul>

          <li class="has-mega">
            <a href="#">Nữ</a>
            <div class="mega-content">
              <div class="row">
                <div class="col">
                  <div class="sub-title-menu"><a href="#">ÁO NỮ</a></div>
                  <ul>
                    <li><a href="${pageContext.request.contextPath}/category?id=10">Áo Sơ Mi</a></li>
                    <li><a href="${pageContext.request.contextPath}/category?id=11">Áo Thun</a></li>
                    <li><a href="${pageContext.request.contextPath}/category?id=12">Áo Croptop / Áo Kiểu</a></li>
                  </ul>
                </div>
                <div class="col">
                  <div class="sub-title-menu"><a href="#">SET BỘ</a></div>
                  <ul>
                    <li><a href="${pageContext.request.contextPath}/category?id=13">Set bộ công sở</a></li>
                    <li><a href="${pageContext.request.contextPath}/category?id=14">Set bộ thun len</a></li>
                  </ul>
                </div>
                <div class="col">
                  <div class="sub-title-menu"><a href="#">QUẦN & JUMPSUIT</a></div>
                  <ul>
                    <li><a href="${pageContext.request.contextPath}/category?id=15">Quần Jeans</a></li>
                    <li><a href="${pageContext.request.contextPath}/category?id=16">Quần dài</a></li>
                  </ul>
                </div>
                <div class="col">
                  <div class="sub-title-menu"><a href="#">CHÂN VÁY</a></div>
                  <ul>
                    <li><a href="${pageContext.request.contextPath}/category?id=17">Chân váy chữ A</a></li>
                    <li><a href="${pageContext.request.contextPath}/category?id=18">Chân váy Jeans</a></li>
                  </ul>
                </div>
                <div class="col">
                  <div class="sub-title-menu"><a href="#">ĐẦM / ÁO DÀI</a></div>
                  <ul>
                    <li><a href="${pageContext.request.contextPath}/category?id=19">Đầm công sở</a></li>
                    <li><a href="${pageContext.request.contextPath}/category?id=20">Đầm dạ hội</a></li>
                  </ul>
                </div>
              </div>
            </div>
          </li>

          <li class="has-mega">
            <a href="#">Nam</a>
            <div class="mega-content">
              <div class="row">
                <div class="col-md-3">
                  <div class="sub-title-menu"><a href="#">ÁO NAM</a></div>
                  <ul>
                    <li><a href="${pageContext.request.contextPath}/category?id=21">Áo sơ mi nam</a></li>
                    <li><a href="${pageContext.request.contextPath}/category?id=22">Áo thun nam</a></li>
                    <li><a href="${pageContext.request.contextPath}/category?id=23">Áo polo</a></li>
                  </ul>
                </div>
                <div class="col-md-3">
                  <div class="sub-title-menu"><a href="#">QUẦN NAM</a></div>
                  <ul>
                    <li><a href="${pageContext.request.contextPath}/category?id=24">Quần tây</a></li>
                    <li><a href="${pageContext.request.contextPath}/category?id=25">Quần jeans</a></li>
                  </ul>
                </div>
              </div>
            </div>
          </li>

          <li>
            <a href="saleT5" class="active">Ưu đãi lớn tháng 5</a>
          </li>
          <li>
            <a href="#">Bộ sưu tập</a>
            <ul>
              <div class="sub-title-menu">
                <a href="#">SALE 50% Toàn bộ SP</a>
              </div>
              <li><a href="#">BLUE SONATA</a></li>
              <li><a href="#">Đầm kiểu</a></li>
              <li><a href="#">Áo khoác / blazer</a></li>
              <li><a href="#">Áo thun / len</a></li>
              <li><a href="#">Quần tây / jeans</a></li>
              <li><a href="#">Chân váy công sở</a></li>
            </ul>
          </li>
          <li>
            <a href="#">Về chúng tôi</a>
          </li>
        </ul>
      </nav>

      <div class="inner-logo">
        <a href="${pageContext.request.contextPath}/public/trang-chu">
          <img src="<c:url value='/templates/web/images/logo.png'/>" alt="TA shop" />
        </a>
      </div>
      <div class="inner-right-header">
        <div class="search-wrapper">
          <form action="${pageContext.request.contextPath}/public/search" method="GET" class="search-form">
            <input type="text" name="keyword" id="searchInput" placeholder="TÌM KIẾM SẢN PHẨM" autocomplete="off">
            <button type="submit"><i class="fa-solid fa-magnifying-glass"></i></button>
          </form>

          <div class="search-suggest" id="searchSuggest">
            <div class="suggest-header">
              <strong>Tìm kiếm nhiều nhất</strong>
            </div>
            <div class="suggest-tags">
              <a href="${pageContext.request.contextPath}/public/search?keyword=Áo thun">Áo thun</a>
              <a href="${pageContext.request.contextPath}/public/search?keyword=Sơ mi">Sơ mi</a>
              <a href="${pageContext.request.contextPath}/public/search?keyword=Jeans">Jeans</a>
              <a href="${pageContext.request.contextPath}/public/search?keyword=Áo khoác">Áo khoác</a>
              <a href="${pageContext.request.contextPath}/public/search?keyword=Đầm dạ hội">Đầm dạ hội</a>
            </div>
          </div>
        </div>
        <div class="inner-actions">
          <div class="inner-headphone">
            <a data-bs-toggle="collapse" href="#collapseHeadphone" role="button" aria-expanded="false" aria-controls="collapseHeadphone">
              <i class="fa-solid fa-headphones"></i>
            </a>
            <div class="collapse" id="collapseHeadphone">
              <div class="card card-body">
                <div class="inner-head">Trợ giúp</div>
                <nav class="inner-body">
                  <ul>
                    <li><a href="#"><i class="fa-solid fa-phone-volume"></i> Hotline</a></li>
                    <li><a href="#"><i class="fa-regular fa-comment-dots"></i> Live chat</a></li>
                    <li><a href="#"><i class="fa-solid fa-repeat"></i> Messenger</a></li>
                    <li><a href="#"><i class="fa-solid fa-envelope"></i> Email</a></li>
                    <li><a href="#"><i class="fa-solid fa-paw"></i> Tra cứu đơn hàng</a></li>
                  </ul>
                </nav>
              </div>
            </div>
          </div>
          <li>
            <c:choose>
              <c:when test="${empty sessionScope.user}">
                <a href="${pageContext.request.contextPath}/public/login"><i class="fa-regular fa-user"></i></a>
              </c:when>
              <c:otherwise>
                <a href="#"><i class="fa-regular fa-user"></i></a>
                <c:if test="${not empty sessionScope.user}">
                  <ul id="userDropdownMenu">
                    <div class="sub-title-menu"><a href="#">${sessionScope.user.fullname}</a></div>
                    <li><a href="${pageContext.request.contextPath}/customer/profile">Thông tin tài khoản</a></li>
                    <li><a href="${pageContext.request.contextPath}/customer/order-history">Quản lý đơn hàng</a></li>
                    <li><a href="${pageContext.request.contextPath}/customer/wishlist">Danh sách yêu thích</a></li>
                    <li><a href="${pageContext.request.contextPath}/customer/address">Địa chỉ giao hàng</a></li>
                    <li><a href="javascript:void(0)" onclick="confirmLogout()">Đăng xuất</a></li>
                  </ul>
                </c:if>
              </c:otherwise>
            </c:choose>
          </li>
          <a href="javascript:void(0)" class="cart-icon inner-cart" id="cartIconBtn">
            <i class="bi bi-bag"></i>
            <span class="cart-badge cart-count" id="cartBadge">${not empty sessionScope.cart ? sessionScope.cart.size() : 0}</span>
          </a>
          <%@ include file="/views/web/mini-cart.jsp" %>
        </div>
      </div>
    </div>
  </div>
</header>
<script>
  $(document).ready(function() {
    $('#cartIconBtn').on('click', function(e) {
      e.preventDefault();
      if (typeof showMiniCart === "function") {
        showMiniCart();
      }
    });
  });

  function confirmLogout() {
    if (confirm("Bạn có chắc chắn muốn đăng xuất khỏi hệ thống TA shop không?")) {
      if (window.clearChatHistoryOnLogout) {
        window.clearChatHistoryOnLogout();
      }
      // 2. CHỐT CHẶN AN TOÀN TRỰC TIẾP (Bảo hiểm 100%)
      // Tự tay xóa luôn bộ nhớ phòng khi file JS trên bị lỗi mạng không chạy được
      sessionStorage.removeItem('chatbot_chat_history'); // Xóa dữ liệu mới
       localStorage.removeItem('chatbot_chat_history');   // Dọn dẹp nốt dữ liệu cũ của ngày hôm trước (nếu còn kẹt)
      window.location.href = '${pageContext.request.contextPath}/logout';
    }
  }

  const searchWrapper = document.querySelector('.search-wrapper');
  const searchInput = document.getElementById('searchInput');

  // Khi click vào ô input
  searchInput.addEventListener('focus', () => {
    searchWrapper.classList.add('active');
  });

  // Khi click ra ngoài vùng tìm kiếm
  document.addEventListener('click', (e) => {
    if (!searchWrapper.contains(e.target)) {
      searchWrapper.classList.remove('active');
    }
  });
</script>

<%@ include file="/common/web/chatbot.jsp" %>
<script src="<c:url value='/templates/web/js/chatbot.js'/>"></script>