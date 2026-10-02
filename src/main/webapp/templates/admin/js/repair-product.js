document.getElementById("editProductForm").addEventListener("submit", function (e) {
    e.preventDefault(); // Ngăn submit mặc định
    console.log("Form submit triggered at", new Date().toISOString());

    // ĐÃ SỬA: Đồng bộ tên key 'categoryId' cho chuẩn với HTML
    const fields = {
        productName: document.getElementById("productName")?.value.trim() || "",
        productCode: document.getElementById("productCode")?.value.trim() || "",
        productPrice: document.getElementById("productPrice")?.value || "",

        categoryId: document.getElementById("categoryId")?.value || "", // Thay productCategory thành categoryId

        productColor: document.getElementById("productColor")?.value.trim() || "",
        productSize: document.getElementById("productSize")?.value.trim() || "",
        productQuantity: document.getElementById("productQuantity")?.value || "",
        productImage: document.getElementById("productImage")?.files || [],
        productDescription: tinymce?.get("productDescription")?.getContent()?.trim() || "",
        productImagePath: document.querySelector("input[name='productImagePath']")?.value || ""
    };

    console.log("Form fields:", JSON.stringify(fields, (key, value) => {
        if (key === "productImage") {
            return Array.from(value).map(file => ({ name: file.name, size: file.size, type: file.type }));
        }
        return value;
    }, 2));

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

    if (!fields.productPrice || isNaN(parseFloat(fields.productPrice)) || parseFloat(fields.productPrice) <= 0) {
        showError("productPrice", "Giá sản phẩm phải là số dương");
        isValid = false;
    } else if (!/^\d+(\.\d{1,2})?$/.test(fields.productPrice)) {
        showError("productPrice", "Giá sản phẩm phải có tối đa 2 chữ số thập phân");
        isValid = false;
    }

    // ĐÃ SỬA: Dùng fields.categoryId và chặn luôn giá trị "0"
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

    if (!fields.productQuantity || isNaN(parseInt(fields.productQuantity)) || parseInt(fields.productQuantity) < 0) {
        showError("productQuantity", "Số lượng phải là số không âm");
        isValid = false;
    }

    if (!fields.productDescription) {
        showError("productDescription", "Vui lòng nhập mô tả sản phẩm");
        isValid = false;
    }

    if (fields.productImage.length > 0) {
        const file = fields.productImage[0];
        const validImageTypes = ['image/jpeg', 'image/png', 'image/gif'];
        if (!validImageTypes.includes(file.type)) {
            showError("productImage", "Vui lòng chọn tệp ảnh (JPEG, PNG, GIF)");
            isValid = false;
        } else if (file.size > 5 * 1024 * 1024) {
            showError("productImage", "Kích thước ảnh không được vượt quá 5MB");
            isValid = false;
        }
    }

    // Nếu hợp lệ thì gửi form
    if (isValid) {
        console.log("Form is valid, submitting to", this.action);
        this.submit();
    } else {
        console.error("Form validation failed. Errors:", document.querySelectorAll(".text-danger:not(:empty)").length);
    }

    function showError(id, message) {
        const errorDiv = document.getElementById(`error-${id}`);
        if (errorDiv) {
            errorDiv.innerText = message;
            console.log(`Validation error on ${id}: ${message}`);
        }
    }
});
