// Delete Product in Product List
/*const productList = document.querySelector("#productContainer");
if (productList) {
  productList.addEventListener("click", (event) => {
    if (event.target.closest(".inner-remove")) {
      const confirmed = confirm("Bạn có chắc chắn muốn xóa không?");

      if (confirmed) {
        const item = event.target.closest(".col-md-6");
        productList.removeChild(item);
      }
    }
  });
}*/

container.addEventListener("click", function (e) {
  const btn = e.target.closest(".inner-remove");
  if (btn) {
    const confirmed = confirm("Bạn có chắc muốn xóa sản phẩm này?");
    if (!confirmed) {
      e.preventDefault(); // Ngăn chuyển hướng nếu không xác nhận
    }
  }
});



// End Delete Product in Product List

// Confirm Delete Order
const tableSection6 = document.querySelector(".section-6");

if (tableSection6) {
  const parent = tableSection6.querySelector("tbody");
  tableSection6.addEventListener("click", (event) => {
    if (event.target.closest(".inner-remove")) {
      const confirmed = confirm("Bạn có chắc chắn muốn xóa không?");

      if (confirmed) {
        const item = event.target.closest("tr");
        parent.removeChild(item);
      }
    }
  });
}

// End Confirm Delete Order

// Validate Order Edit
const formOrderEdit = document.querySelector(".form-order-edit");
if (formOrderEdit) {
  const btnUpdate = formOrderEdit.querySelector(".btn-update");
  console.log(btnUpdate);

  const inputOrderName = formOrderEdit.querySelector(".inner-name");
  const inputOrderPhone = formOrderEdit.querySelector(".inner-phone");

  const messageName = formOrderEdit.querySelector(".message-name");
  const messagePhone = formOrderEdit.querySelector(".message-phone");

  btnUpdate.addEventListener("click", (e) => {
    e.preventDefault();

    let isValid = true;

    const orderNameValue = inputOrderName.value.trim();
    const orderPhoneValue = inputOrderPhone.value.trim();
    if (orderNameValue == "") {
      inputOrderName.style.border = "1px solid #EF3826";
      messageName.innerHTML = "Vui lòng nhập tên!";
      isValid = false;
    } else {
      inputOrderName.style.border = "1px solid #D5D5D5";
      messageName.innerHTML = "";
    }

    const regexPhone = /^\d{10}$/;
    if (orderPhoneValue == "") {
      inputOrderPhone.style.border = "1px solid #EF3826";
      messagePhone.innerHTML = "Vui lòng nhập số điện thoại!";
      isValid = false;
    } else if (!regexPhone.test(orderPhoneValue)) {
      inputOrderPhone.style.border = "1px solid #EF3826";
      messagePhone.innerHTML = "Số điện thoại phải từ 10 chữ số!";
      isValid = false;
    } else {
      inputOrderPhone.style.border = "1px solid #D5D5D5";
      messagePhone.innerHTML = "";
    }

    if (isValid) {
      formOrderEdit.submit();
      alert("Cập nhật thành công!");
    }
  });
}

// End Validate Order Edit

const btnUpdateProduct = document.querySelector(".btn-update-product");
if(btnUpdateProduct) {
  btnUpdateProduct.addEventListener("click", () => {
    alert("Cập nhật thành công")
  })
}
