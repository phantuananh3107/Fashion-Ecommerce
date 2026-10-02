/**
 * TA shop - Cart Logic (SSR Version)
 * Fix: Validate ngay khi người dùng gõ số, bắt sự kiện phím Enter và gửi API chuẩn xác.
 */

const contextPath = window.location.pathname.includes('/jsp_servlet_war_exploded') ? '/jsp_servlet_war_exploded' : '';

$(document).ready(function() {
    if (sessionStorage.getItem('keepMiniCartOpen') === 'true') {
        $('#miniCartSidebar').css('transition', 'none');
        $('#miniCartSidebar, #miniCartOverlay').addClass('active');
        setTimeout(function() {
            $('#miniCartSidebar').css('transition', 'transform 0.3s ease-in-out');
        }, 50);
        sessionStorage.removeItem('keepMiniCartOpen');
    }

    $(document).on('click', '.header .inner-cart a, .cart-icon, .bag-icon, #openMiniCart', function(e) {
        e.preventDefault();
        e.stopPropagation();

        if (window.location.pathname.includes('/customer/cart') || window.location.pathname.includes('/customer/checkout')) {
            window.location.href = contextPath + '/customer/cart';
            return;
        }
        $('#miniCartSidebar, #miniCartOverlay').addClass('active');
    });

    $(document).on('click', '#miniCartClose, #miniCartOverlay', function() {
        $('#miniCartSidebar, #miniCartOverlay').removeClass('active');
    });

    // ==========================================
    // TĂNG SỐ LƯỢNG (+)
    // ==========================================
    $(document).on('click', '.btn-qty-up', function() {
        const pid = $(this).attr('data-product-id');
        const size = $(this).attr('data-product-size');
        const $input = $(this).closest('.item-quantity-control').find('.qty-input-manual');
        const currentQty = parseInt($input.val()) || 1;
        const maxQty = parseInt($(this).attr('data-product-max')) || 0;

        if (currentQty < maxQty) {
            updateCartQuantity(pid, size, 'up');
        } else {
            showToast("Kho chỉ còn tối đa " + maxQty + " sản phẩm!");
        }
    });

    // ==========================================
    // GIẢM SỐ LƯỢNG (-)
    // ==========================================
    $(document).on('click', '.btn-qty-down', function() {
        const pid = $(this).attr('data-product-id');
        const size = $(this).attr('data-product-size');
        const $input = $(this).closest('.item-quantity-control').find('.qty-input-manual');
        const currentQty = parseInt($input.val()) || 1;

        if (currentQty <= 1) {
            showToast('! Đã xóa sản phẩm khỏi giỏ hàng');
        }
        updateCartQuantity(pid, size, 'down');
    });

    // ==========================================
    // BẮT SỰ KIỆN GÕ PHÍM TRỰC TIẾP (GIÁM SÁT REALTIME)
    // ==========================================
    $(document).on('input', '.qty-input-manual', function() {
        let val = parseInt($(this).val());
        const maxQty = parseInt($(this).attr('data-product-max')) || 0;

        if (!isNaN(val)) {
            if (val > maxQty) {
                showToast("Kho chỉ còn tối đa " + maxQty + " sản phẩm!");
                $(this).val(maxQty); // Ép lùi về số max của kho ngay lập tức
            }
        }
    });

    // THÊM MỚI: Bắt sự kiện ấn phím ENTER (Mã 13) để tương thích trên Chrome
    $(document).on('keypress', '.qty-input-manual', function(e) {
        if (e.which === 13) {
            e.preventDefault();
            $(this).blur(); // Ép trình duyệt kích hoạt sự kiện 'change' bên dưới
        }
    });

    // Khi gõ xong (Enter hoặc Click chuột ra ngoài) -> Gửi AJAX lưu xuống Backend
    $(document).on('change', '.qty-input-manual', function() {
        const pid = $(this).attr('data-product-id');
        const size = $(this).attr('data-product-size');
        let val = parseInt($(this).val());
        const maxQty = parseInt($(this).attr('data-product-max')) || 0;

        if (isNaN(val) || val < 1) {
            val = 1;
            $(this).val(val);
        }

        // Gọi Backend thông qua hàm dùng chung đã được sửa
        updateCartQuantity(pid, size, 'set', val);
    });

    // XÓA ITEM
    $(document).on('click', '.btn-remove-item', function() {
        if (confirm("Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?")) {
            const pid = $(this).attr('data-product-id');
            const size = $(this).attr('data-product-size');
            deleteCartItem(pid, size);
        }
    });
});

// ========== AJAX FUNCTIONS ==========

// SỬA QUAN TRỌNG: Thêm tham số qty=0 và truyền quantity vào $.post
function updateCartQuantity(pid, size, action, qty = 0) {
    $.post(contextPath + '/customer/update-cart', {
        product_id: pid,
        size: size,
        action: action,
        quantity: qty // Đẩy con số khách hàng vừa gõ xuống Java
    }, function() {
        refreshCartUI(true);
    }).fail(function() {
        showToast('✗ Lỗi cập nhật số lượng');
    });
}

function refreshCartUI(openSidebar = false) {
    if (window.location.pathname.includes('/customer/cart')) {
        window.location.reload();
        return;
    }

    const refreshUrl = window.location.href + (window.location.href.includes('?') ? '&' : '?') + "t=" + new Date().getTime();

    fetch(refreshUrl)
        .then(response => response.text())
        .then(htmlData => {
            const parser = new DOMParser();
            const doc = parser.parseFromString(htmlData, "text/html");

            const newItemsElement = doc.getElementById('miniCartItems');
            const newTotalWrapperElement = doc.getElementById('miniCartTotalWrapper');

            if (newItemsElement) {
                document.getElementById('miniCartItems').innerHTML = newItemsElement.innerHTML;
            }
            if (newTotalWrapperElement) {
                document.getElementById('miniCartTotalWrapper').innerHTML = newTotalWrapperElement.innerHTML;
            }

            updateCartBadge();

            if (openSidebar) {
                $('#miniCartSidebar, #miniCartOverlay').addClass('active');
            }
        })
        .catch(error => console.error('Lỗi khi cập nhật giỏ hàng:', error));
}

function deleteCartItem(pid, size) {
    $.post(contextPath + '/customer/cart-delete-item', {
        productId: pid,
        size: size
    }, function() {
        showToast('! Đã xóa sản phẩm khỏi giỏ hàng');
        refreshCartUI(true);
    });
}

function updateCartBadge() {
    let rowCount = $('#miniCartItems .cart-item').length;
    if ($('#miniCartItems .mini-cart-empty').length > 0) rowCount = 0;
    $('#cartBadge').text(rowCount);
}

// ========== TOAST ENGINE ==========
let toastTimeout;
function showToast(message, duration = 3000) {
    $('#ivyAppToast').remove();
    clearTimeout(toastTimeout);

    if ($('#ivyToastCSS').length === 0) {
        $('head').append(`
            <style id="ivyToastCSS">
                #ivyAppToast {
                    position: fixed !important;
                    top: 100px !important;
                    right: -450px; 
                    min-width: 300px !important;
                    padding: 16px 24px !important;
                    border-radius: 8px !important;
                    z-index: 9999999 !important;
                    color: #fff !important;
                    font-weight: 600 !important;
                    box-shadow: 0 4px 15px rgba(0,0,0,0.25) !important;
                    transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275) !important;
                    bottom: auto !important;
                }
                #ivyAppToast.active { right: 20px !important; }
            </style>
        `);
    }

    const $toast = $('<div id="ivyAppToast"></div>');
    $('body').append($toast);

    let bgColor = '#28a745';
    let msgLower = message.toLowerCase();
    if (msgLower.includes('xóa') || message.includes('!')) bgColor = '#dc3545';
    else if (msgLower.includes('lỗi') || msgLower.includes('vui lòng') || msgLower.includes('chỉ còn')) bgColor = '#ffc107';

    $toast.text(message).css({ 'background-color': bgColor, 'color': (bgColor === '#ffc107' ? '#000' : '#fff') });
    setTimeout(() => { $toast.addClass('active'); }, 10);

    toastTimeout = setTimeout(() => {
        $toast.removeClass('active');
        setTimeout(() => { $toast.remove(); }, 500);
    }, duration);
}