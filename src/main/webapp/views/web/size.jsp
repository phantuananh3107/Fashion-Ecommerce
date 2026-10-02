<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<style>
    /* Lớp phủ mờ mờ màu đen (Opacity) */
    .size-modal-overlay {
        display: none; /* Mặc định ẩn */
        position: fixed;
        z-index: 9999;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.6); /* Nền đen mờ 60% */
        align-items: center;
        justify-content: center;
        backdrop-filter: blur(2px); /* Hiệu ứng làm mờ background phía sau (nếu muốn) */
    }

    /* Hộp Modal màu trắng ở giữa */
    .size-modal-box {
        background-color: #fff;
        border-radius: 8px;
        width: 600px;
        max-width: 90%;
        padding: 25px;
        position: relative;
        box-shadow: 0 10px 30px rgba(0,0,0,0.3);
        animation: slideDown 0.3s ease-out;
    }

    @keyframes slideDown {
        from { transform: translateY(-30px); opacity: 0; }
        to { transform: translateY(0); opacity: 1; }
    }

    /* Nút tắt (X) */
    .size-modal-close {
        position: absolute;
        top: 15px;
        right: 20px;
        font-size: 24px;
        font-weight: bold;
        color: #999;
        cursor: pointer;
        transition: 0.2s;
    }
    .size-modal-close:hover { color: #000; }

    /* Tiêu đề và Tabs */
    .size-modal-title {
        text-align: center;
        font-size: 20px;
        font-weight: bold;
        margin-bottom: 20px;
        text-transform: uppercase;
    }

    .size-tabs {
        display: flex;
        border-bottom: 2px solid #eee;
        margin-bottom: 20px;
    }

    .size-tab-btn {
        flex: 1;
        background: none;
        border: none;
        padding: 10px 0;
        font-size: 16px;
        font-weight: bold;
        color: #999;
        cursor: pointer;
        transition: 0.3s;
    }

    .size-tab-btn.active {
        color: #000;
        border-bottom: 2px solid #000; /* Gạch chân tab đang chọn */
    }

    /* Bảng thông số */
    .size-tab-content { display: none; }
    .size-tab-content.active { display: block; }

    .size-table {
        width: 100%;
        border-collapse: collapse;
        text-align: center;
    }
    .size-table th, .size-table td {
        border: 1px solid #eee;
        padding: 12px;
    }
    .size-table th { background-color: #f9f9f9; font-weight: bold; }
    .size-table tbody tr:hover { background-color: #fdfdfd; }
</style>

<div id="sizeGuideModal" class="size-modal-overlay">
    <div class="size-modal-box">
        <span class="size-modal-close" onclick="closeSizeModal()">&times;</span>
        <div class="size-modal-title">Bảng Tư Vấn Size</div>

        <div class="size-tabs">
            <button class="size-tab-btn active" onclick="switchSizeTab(event, 'tab-nam')">NAM</button>
            <button class="size-tab-btn" onclick="switchSizeTab(event, 'tab-nu')">NỮ</button>
        </div>

        <div id="tab-nam" class="size-tab-content active">
            <table class="size-table">
                <thead>
                <tr><th>Size</th><th>S</th><th>M</th><th>L</th><th>XL</th><th>XXL</th></tr>
                </thead>
                <tbody>
                <tr><td>Chiều cao (cm)</td><td>162-168</td><td>168-172</td><td>172-176</td><td>176-180</td><td>180-184</td></tr>
                <tr><td>Cân nặng (kg)</td><td>55-60</td><td>60-65</td><td>65-70</td><td>70-75</td><td>75-80</td></tr>
                <tr><td>Vòng ngực (cm)</td><td>86-90</td><td>90-94</td><td>94-98</td><td>98-102</td><td>102-106</td></tr>
                <tr><td>Vòng eo (cm)</td><td>74-78</td><td>78-82</td><td>82-86</td><td>86-90</td><td>90-94</td></tr>
                </tbody>
            </table>
        </div>

        <div id="tab-nu" class="size-tab-content">
            <table class="size-table">
                <thead>
                <tr><th>Size</th><th>S</th><th>M</th><th>L</th><th>XL</th><th>XXL</th></tr>
                </thead>
                <tbody>
                <tr><td>Chiều cao (cm)</td><td>150-155</td><td>156-160</td><td>160-164</td><td>165-168</td><td>169-172</td></tr>
                <tr><td>Cân nặng (kg)</td><td>43-46</td><td>47-50</td><td>51-55</td><td>56-60</td><td>61-65</td></tr>
                <tr><td>Vòng ngực (cm)</td><td>82-86</td><td>86-90</td><td>90-94</td><td>94-98</td><td>98-102</td></tr>
                <tr><td>Vòng eo (cm)</td><td>64-68</td><td>68-72</td><td>72-76</td><td>76-80</td><td>80-84</td></tr>
                </tbody>
            </table>
        </div>

    </div>
</div>

<script>
    // Hàm mở Modal
    function openSizeModal() {
        document.getElementById('sizeGuideModal').style.display = 'flex';
    }

    // Hàm đóng Modal
    function closeSizeModal() {
        document.getElementById('sizeGuideModal').style.display = 'none';
    }

    // Đóng Modal khi click ra ngoài vùng mờ đen
    window.onclick = function(event) {
        var modal = document.getElementById('sizeGuideModal');
        if (event.target == modal) {
            modal.style.display = "none";
        }
    }

    // Hàm chuyển đổi Tab Nam / Nữ
    function switchSizeTab(evt, tabId) {
        var i, tabcontent, tablinks;

        // Ẩn tất cả các bảng
        tabcontent = document.getElementsByClassName("size-tab-content");
        for (i = 0; i < tabcontent.length; i++) {
            tabcontent[i].classList.remove("active");
        }

        // Bỏ viền gạch chân ở tất cả các nút
        tablinks = document.getElementsByClassName("size-tab-btn");
        for (i = 0; i < tablinks.length; i++) {
            tablinks[i].classList.remove("active");
        }

        // Hiển thị bảng được chọn và thêm gạch chân cho nút đó
        document.getElementById(tabId).classList.add("active");
        evt.currentTarget.classList.add("active");
    }
</script>