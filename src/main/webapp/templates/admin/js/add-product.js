document.getElementById("addProductForm").addEventListener("submit", function (e) {
    e.preventDefault(); // Ngăn submit mặc định

    // ĐÃ SỬA: Lấy đúng ID 'categoryId' của thẻ select
    const fields = {
        productName: document.getElementById("productName").value.trim(),
        productCode: document.getElementById("productCode").value.trim(),
        productPrice: document.getElementById("productPrice").value,

        categoryId: document.getElementById("categoryId").value, // Sửa ở đây

        productColor: document.getElementById("productColor").value.trim(),
        productSize: document.getElementById("productSize").value.trim(),
        productQuantity: document.getElementById("productQuantity").value,
        productImage: document.getElementById("productImage").files,
        productDescription: tinymce.get("productDescription")?.getContent()?.trim()
    };

    // Xóa thông báo lỗi cũ
    Object.keys(fields).forEach(id => {
        const errorElement = document.getElementById(`error-${id}`);
        if (errorElement) errorElement.innerText = "";
    });

    let isValid = true;

    // Kiểm tra từng trường
    if (!fields.productName) {
        showError("productName", "Vui lòng nhập tên sản phẩm");
        isValid = false;
    }

    if (!fields.productCode) {
        showError("productCode", "Vui lòng nhập mã sản phẩm");
        isValid = false;
    }

    if (!fields.productPrice || parseFloat(fields.productPrice) <= 0) {
        showError("productPrice", "Giá sản phẩm phải là số dương");
        isValid = false;
    } else if (!/^\d+(\.\d{1,2})?$/.test(fields.productPrice)) {
        showError("productPrice", "Giá sản phẩm phải có tối đa 2 chữ số thập phân");
        isValid = false;
    }

    // ĐÃ SỬA: Check fields.categoryId và giá trị "0"
    if (!fields.categoryId || fields.categoryId === "0") {
        showError("categoryId", "Vui lòng chọn danh mục");
        isValid = false;
    }

    if (!fields.productColor) {
        showError("productColor", "Vui lòng nhập màu sắc");
        isValid = false;
    }

    if (!fields.productSize) {
        showError("productSize", "Vui lòng nhập kích cỡ");
        isValid = false;
    }

    if (!fields.productQuantity || parseInt(fields.productQuantity) < 0) {
        showError("productQuantity", "Số lượng phải là số không âm");
        isValid = false;
    }

    if (!fields.productImage || fields.productImage.length === 0) {
        showError("productImage", "Vui lòng chọn ảnh sản phẩm");
        isValid = false;
    }

    if (!fields.productDescription) {
        showError("productDescription", "Vui lòng nhập mô tả sản phẩm");
        isValid = false;
    }

    // Nếu hợp lệ thì cho submit
    if (isValid) {
        this.submit(); // Gửi form nếu không có lỗi
    }

    function showError(id, message) {
        const errorDiv = document.getElementById(`error-${id}`);
        if (errorDiv) {
            errorDiv.innerText = message;
        }
    }
});