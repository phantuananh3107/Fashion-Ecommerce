$(document).ready(function() {
    // ==========================================
    // 1. XỬ LÝ VOUCHER (MÃ GIẢM GIÁ)
    // ==========================================
    $(".btn-apply").click(function(e) {
        e.preventDefault();

        // Lấy mã từ ô input (Dùng ID đã thêm ở JSP)
        var voucherInput = $("#voucher-input");

        // Kiểm tra nếu không tìm thấy ô input thì báo lỗi nhẹ để debug
        if (voucherInput.length === 0) {
            console.error("Không tìm thấy ô nhập mã giảm giá (#voucher-input)");
            return;
        }

        var voucherCode = voucherInput.val().trim();

        if (voucherCode === "") {
            alert("Vui lòng nhập mã giảm giá!");
            return;
        }

        $.ajax({
            // Đảm bảo đường dẫn này đúng với Context Path của bạn
            url: window.location.origin + "/jsp_servlet_war_exploded/api/check-voucher",
            type: "GET",
            data: { code: voucherCode },
            success: function(response) {
                if (response.success) {
                    var discount = response.discount;

                    // 1. Hiển thị dòng Giảm giá và số tiền giảm
                    $("#discount-row").show();
                    $("#discount-display").text("-" + discount.toLocaleString() + "đ");

                    // 2. Tính lại tổng tiền thanh toán
                    var totalOriginal = parseInt($("#final-total-display").attr("data-original-sum"));
                    var totalAfter = totalOriginal - discount;
                    if(totalAfter < 0) totalAfter = 0;

                    // 3. Cập nhật số tiền cuối cùng
                    $("#final-total-display").text(totalAfter.toLocaleString() + "đ");

                    showToast("Áp dụng mã " + voucherCode + " thành công!");
                } else {
                    alert(response.message);
                }
            },
            error: function() {
                showToast("Không thể kết nối với máy chủ để kiểm tra mã!");
            }
        });
    });

    // ==========================================
    // 2. XỬ LÝ ĐỊA CHỈ (Giữ nguyên vì đã chuẩn)
    // ==========================================

    const addressData = {
        "Hà Nội": {
            "Quận Ba Đình": ["Phường Cống Vị", "Phường Điện Biên", "Phường Đội Cấn", "Phường Giảng Võ", "Phường Kim Mã", "Phường Liễu Giai", "Phường Ngọc Hà", "Phường Ngọc Khánh", "Phường Nguyễn Trung Trực", "Phường Phúc Xá", "Phường Quán Thánh", "Phường Thành Công", "Phường Trúc Bạch", "Phường Vĩnh Phúc"],
            "Quận Hoàn Kiếm": ["Phường Chương Dương", "Phường Cửa Đông", "Phường Cửa Nam", "Phường Đồng Xuân", "Phường Hàng Bạc", "Phường Hàng Bài", "Phường Hàng Bồ", "Phường Hàng Bông", "Phường Hàng Buồm", "Phường Hàng Đào", "Phường Hàng Gai", "Phường Hàng Mã", "Phường Hàng Trống", "Phường Lý Thái Tổ", "Phường Phan Chu Trinh", "Phường Phúc Tân", "Phường Trần Hưng Đạo", "Phường Tràng Tiền"],
            "Quận Tây Hồ": ["Phường Bưởi", "Phường Nhật Tân", "Phường Phú Thượng", "Phường Quảng An", "Phường Thụy Khuê", "Phường Tứ Liên", "Phường Xuân La", "Phường Yên Phụ"],
            "Quận Long Biên": ["Phường Bồ Đề", "Phường Cự Khối", "Phường Đức Giang", "Phường Gia Thụy", "Phường Giang Biên", "Phường Long Biên", "Phường Ngọc Lâm", "Phường Ngọc Thụy", "Phường Phúc Đồng", "Phường Phúc Lợi", "Phường Sài Đồng", "Phường Thạch Bàn", "Phường Thượng Thanh", "Phường Việt Hưng"],
            "Quận Cầu Giấy": ["Phường Dịch Vọng", "Phường Dịch Vọng Hậu", "Phường Mai Dịch", "Phường Nghĩa Đô", "Phường Nghĩa Tân", "Phường Quan Hoa", "Phường Trung Hòa", "Phường Yên Hòa"],
            "Quận Đống Đa": ["Phường Cát Linh", "Phường Hàng Bột", "Phường Khâm Thiên", "Phường Khương Thượng", "Phường Kim Liên", "Phường Láng Hạ", "Phường Láng Thượng", "Phường Nam Đồng", "Phường Ngã Tư Sở", "Phường Ô Chợ Dừa", "Phường Phương Liên", "Phường Phương Mai", "Phường Quang Trung", "Phường Quốc Tử Giám", "Phường Thịnh Quang", "Phường Thổ Quan", "Phường Trung Liệt", "Phường Trung Phụng", "Phường Trung Tự", "Phường Văn Chương", "Phường Văn Miếu"],
            "Quận Hai Bà Trưng": ["Phường Bạch Đằng", "Phường Bách Khoa", "Phường Bạch Mai", "Phường Cầu Dền", "Phường Đống Mác", "Phường Đồng Nhân", "Phường Đồng Tâm", "Phường Lê Đại Hành", "Phường Minh Khai", "Phường Nguyễn Du", "Phường Phạm Đình Hổ", "Phường Phố Huế", "Phường Quỳnh Lôi", "Phường Quỳnh Mai", "Phường Thanh Lương", "Phường Thanh Nhàn", "Phường Trương Định", "Phường Vĩnh Tuy"],
            "Quận Hoàng Mai": ["Phường Đại Kim", "Phường Định Công", "Phường Giáp Bát", "Phường Hoàng Liệt", "Phường Hoàng Văn Thụ", "Phường Lĩnh Nam", "Phường Mai Động", "Phường Tân Mai", "Phường Thanh Trì", "Phường Thịnh Liệt", "Phường Trần Phú", "Phường Tương Mai", "Phường Vĩnh Hưng", "Phường Yên Sở"],
            "Quận Thanh Xuân": ["Phường Hạ Đình", "Phường Khương Đình", "Phường Khương Mai", "Phường Khương Trung", "Phường Kim Giang", "Phường Nhân Chính", "Phường Phương Liệt", "Phường Thanh Xuân Bắc", "Phường Thanh Xuân Nam", "Phường Thanh Xuân Trung", "Phường Thượng Đình"],
            "Quận Nam Từ Liêm": ["Phường Cầu Diễn", "Phường Đại Mỗ", "Phường Mễ Trì", "Phường Mỹ Đình 1", "Phường Mỹ Đình 2", "Phường Phú Đô", "Phường Phương Canh", "Phường Tây Mỗ", "Phường Trung Văn", "Phường Xuân Phương"],
            "Quận Bắc Từ Liêm": ["Phường Cổ Nhuế 1", "Phường Cổ Nhuế 2", "Phường Đông Ngạc", "Phường Đức Thắng", "Phường Liên Mạc", "Phường Minh Khai", "Phường Phú Diễn", "Phường Phúc Diễn", "Phường Tây Tựu", "Phường Thượng Cát", "Phường Thụy Phương", "Phường Xuân Đỉnh", "Phường Xuân Tảo"],
            "Quận Hà Đông": ["Phường Biên Giang", "Phường Đồng Mai", "Phường Dương Nội", "Phường Hà Cầu", "Phường Kiến Hưng", "Phường La Khê", "Phường Mộ Lao", "Phường Nguyễn Trãi", "Phường Phú La", "Phường Phú Lãm", "Phường Phú Lương", "Phường Phúc La", "Phường Quang Trung", "Phường Vạn Phúc", "Phường Văn Quán", "Phường Yên Nghĩa", "Phường Yết Kiêu"],
            "Huyện Gia Lâm": ["Thị trấn Trâu Quỳ", "Thị trấn Yên Viên", "Xã Bát Tràng", "Xã Cổ Bi", "Xã Đa Tốn", "Xã Đặng Xá", "Xã Ninh Hiệp", "Xã Phù Đổng"],
            "Huyện Đông Anh": ["Thị trấn Đông Anh", "Xã Hải Bối", "Xã Kim Chung", "Xã Nam Hồng", "Xã Tiên Dương", "Xã Uy Nỗ", "Xã Vĩnh Ngọc", "Xã Xuân Canh"]
        },
        "TP HCM": {
            "Quận 1": ["Phường Bến Nghé", "Phường Bến Thành", "Phường Cô Giang", "Phường Đa Kao", "Phường Nguyễn Thái Bình", "Phường Phạm Ngũ Lão", "Phường Tân Định"],
            "Quận 3": ["Phường Võ Thị Sáu", "Phường 1", "Phường 2", "Phường 3", "Phường 4", "Phường 5", "Phường 9", "Phường 10"],
            "Quận 10": ["Phường 1", "Phường 2", "Phường 4", "Phường 9", "Phường 12", "Phường 13", "Phường 14", "Phường 15"],
            "Quận Tân Bình": ["Phường 1", "Phường 2", "Phường 4", "Phường 12", "Phường 13", "Phường 15"],
            "Quận Gò Vấp": ["Phường 1", "Phường 3", "Phường 5", "Phường 10", "Phường 11", "Phường 17"],
            "TP Thủ Đức": ["Phường An Phú", "Phường Bình Thọ", "Phường Hiệp Bình Chánh", "Phường Linh Trung", "Phường Thảo Điền", "Phường Thủ Thiêm"]
        },
        "Đà Nẵng": {
            "Quận Hải Châu": ["Phường Hải Châu I", "Phường Hải Châu II", "Phường Hòa Cường Bắc", "Phường Hòa Cường Nam", "Phường Thạch Thang", "Phường Thuận Phước"],
            "Quận Thanh Khê": ["Phường An Khê", "Phường Chính Gián", "Phường Tân Chính", "Phường Thạc Gián", "Phường Vĩnh Trung"],
            "Quận Sơn Trà": ["Phường An Hải Bắc", "Phường An Hải Đông", "Phường Mân Thái", "Phường Phước Mỹ", "Phường Thọ Quang"],
            "Quận Liên Chiểu": ["Phường Hòa Hiệp Bắc", "Phường Hòa Hiệp Nam", "Phường Hòa Khánh Bắc", "Phường Hòa Khánh Nam", "Phường Hòa Minh"]
        },
        "Hải Phòng": {
            "Quận Hồng Bàng": ["Phường Hoàng Văn Thụ", "Phường Minh Khai", "Phường Phan Bội Châu", "Phường Quán Toan", "Phường Sở Dầu", "Phường Thượng Lý"],
            "Quận Ngô Quyền": ["Phường Cầu Đất", "Phường Đằng Giang", "Phường Đông Khê", "Phường Gia Viên", "Phường Lạc Viên", "Phường Máy Tơ"],
            "Quận Lê Chân": ["Phường An Biên", "Phường Dư Hàng Kênh", "Phường Hàng Kênh", "Phường Hồ Nam", "Phường Nghĩa Xá", "Phường Trần Nguyên Hãn"]
        },
        "Cần Thơ": {
            "Quận Ninh Kiều": ["Phường An Bình", "Phường An Cư", "Phường An Hòa", "Phường An Nghiệp", "Phường Cái Khế", "Phường Hưng Lợi", "Phường Tân An", "Phường Xuân Khánh"],
            "Quận Bình Thủy": ["Phường An Thới", "Phường Bình Thủy", "Phường Bùi Hữu Nghĩa", "Phường Long Hòa", "Phường Trà An", "Phường Trà Nóc"],
            "Quận Cái Răng": ["Phường Ba Láng", "Phường Hưng Phú", "Phường Hưng Thạnh", "Phường Lê Bình", "Phường Phú Thứ", "Phường Tân Phú"]
        },
        "Bình Dương": {
            "TP Thủ Dầu Một": ["Phường Chánh Mỹ", "Phường Chánh Nghĩa", "Phường Hiệp Thành", "Phường Phú Cường", "Phường Phú Hòa", "Phường Phú Lợi"],
            "TP Dĩ An": ["Phường An Bình", "Phường Bình An", "Phường Bình Thắng", "Phường Dĩ An", "Phường Đông Hòa", "Phường Tân Bình"],
            "TP Thuận An": ["Phường An Phú", "Phường An Thạnh", "Phường Bình Chuẩn", "Phường Bình Hòa", "Phường Lái Thiêu", "Phường Thuận Giao"]
        },
        "Đồng Nai": {
            "TP Biên Hòa": ["Phường An Bình", "Phường Bửu Long", "Phường Hiệp Hòa", "Phường Hố Nai", "Phường Long Bình", "Phường Long Bình Tân", "Phường Quyết Thắng", "Phường Tân Phong", "Phường Thống Nhất"],
            "TP Long Khánh": ["Phường Bảo Vinh", "Phường Bàu Sen", "Phường Phú Bình", "Phường Suối Tre", "Phường Xuân An", "Phường Xuân Bình"],
            "Huyện Long Thành": ["Thị trấn Long Thành", "Xã An Phước", "Xã Bình Sơn", "Xã Lộc An", "Xã Long Đức"]
        },
        "Quảng Ninh": {
            "TP Hạ Long": ["Phường Bãi Cháy", "Phường Bạch Đằng", "Phường Cao Xanh", "Phường Giếng Đáy", "Phường Hồng Gai", "Phường Hồng Hải", "Phường Trần Hưng Đạo", "Phường Tuần Châu"],
            "TP Cẩm Phả": ["Phường Cẩm Bình", "Phường Cẩm Đông", "Phường Cẩm Phú", "Phường Cẩm Sơn", "Phường Cẩm Tây", "Phường Cẩm Thủy"],
            "TP Uông Bí": ["Phường Nam Khê", "Phường Phương Đông", "Phường Phương Nam", "Phường Quang Trung", "Phường Thanh Sơn"]
        },
        "Thanh Hóa": {
            "TP Thanh Hóa": ["Phường Ba Đình", "Phường Điện Biên", "Phường Đông Cương", "Phường Đông Hải", "Phường Đông Thọ", "Phường Lam Sơn", "Phường Ngọc Trạo", "Phường Phú Sơn"],
            "TP Sầm Sơn": ["Phường Bắc Sơn", "Phường Quảng Cư", "Phường Quảng Tiến", "Phường Trung Sơn", "Phường Trường Sơn"],
            "Thị xã Bỉm Sơn": ["Phường Ba Đình", "Phường Bắc Sơn", "Phường Đông Sơn", "Phường Lam Sơn", "Phường Ngọc Trạo", "Phường Phú Sơn"]
        },
        "Bà Rịa - Vũng Tàu": {
            "TP Vũng Tàu": ["Phường 1", "Phường 2", "Phường 3", "Phường 4", "Phường 5", "Phường 7", "Phường 8", "Phường Thắng Nhất", "Phường Thắng Nhì", "Phường Thắng Tam"],
            "TP Bà Rịa": ["Phường Kim Dinh", "Phường Long Hương", "Phường Long Tâm", "Phường Long Toàn", "Phường Phước Hiệp", "Phường Phước Hưng", "Phường Phước Nguyên", "Phường Phước Trung"],
            "Thị xã Phú Mỹ": ["Phường Hắc Dịch", "Phường Mỹ Xuân", "Phường Phú Mỹ", "Phường Phước Hòa", "Phường Tân Phước"]
        }
    };

    $('#province').on('change', function() {
        const province = $(this).val();
        $('#district').html('<option value="" selected disabled>Quận/Huyện</option>');
        $('#ward').html('<option value="" selected disabled>Phường/Xã</option>');

        if (addressData[province]) {
            Object.keys(addressData[province]).forEach(district => {
                $('#district').append('<option value="'+district+'">'+district+'</option>');
            });
        }

        // CHỐT CHẶN: Ép thư viện giao diện phải cập nhật lại dữ liệu Quận/Huyện
        updateSelectUI();
    });

    $('#district').on('change', function() {
        const province = $('#province').val();
        const district = $(this).val();
        $('#ward').html('<option value="" selected disabled>Phường/Xã</option>');

        if (addressData[province] && addressData[province][district]) {
            addressData[province][district].forEach(ward => {
                $('#ward').append('<option value="'+ward+'">'+ward+'</option>');
            });
        }

        // CHỐT CHẶN: Ép thư viện giao diện phải cập nhật lại dữ liệu Phường/Xã
        updateSelectUI();
    });

    // Hàm cập nhật giao diện thông minh, tự nhận diện thư viện em đang dùng
    function updateSelectUI() {
        if ($.fn.niceSelect) {
            $('#district, #ward').niceSelect('update');
        } else if ($.fn.select2) {
            $('#district, #ward').trigger('change.select2');
        }
    }
});