const tableSection6 = document.querySelector(".section-6");

if (tableSection6) {
    const parent = tableSection6.querySelector("tbody");

    tableSection6.addEventListener("click", async (event) => {
        const removeBtn = event.target.closest(".inner-remove");

        if (removeBtn) {
            const orderId = removeBtn.dataset.orderId;

            const confirmed = confirm("Bạn có chắc chắn muốn xóa không?");
            if (!confirmed) return;

            try {
                const response = await fetch(`/admin-delete-order`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: `orderId=${orderId}`
                });

                if (response.ok) {
                    // Xoá khỏi giao diện
                    const row = removeBtn.closest("tr");
                    parent.removeChild(row);
                    alert("Đã xóa đơn hàng thành công.");
                } else {
                    alert("Xóa đơn hàng thất bại!");
                }
            } catch (error) {
                console.error("Lỗi khi gửi yêu cầu:", error);
                alert("Lỗi khi gửi yêu cầu đến server.");
            }
        }
    });
}
