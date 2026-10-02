$(document).ready(function () {
  $(".arrange").click(function () {
    const list = $(this).next(".list");

    const icon = $(this).find(".icon-arrow");

    list.slideToggle(300);

    icon.toggleClass("rotate");
  });

  $(".uncollect").hover(
    function () {
      $(this).css({
        "background-color": "#000",
        color: "#fff",
      });
    },
    function () {
      $(this).css({
        "background-color": "#fff",
        color: "#000",
      });
    }
  );
  $(".collect").hover(
    function () {
      $(this).css({
        "background-color": "#fff",
        color: "#000",
      });
    },
    function () {
      $(this).css({
        "background-color": "#000",
        color: "#fff",
      });
    }
  );
  $(".size").click(function () {
    $(this).find("i").toggleClass("fa-plus fa-minus");
  });
  $(".size").click(function () {
    $(".list-size").toggleClass("d-none d-flex");
  });
  $(".list-size span ").click(function () {
    $(this).toggleClass("active");
  });
  // list-color
  $(".colorq").click(function () {
    $(this).find("i").toggleClass("fa-plus fa-minus");
  });
  $(".colorq").click(function () {
    $(".list-color").toggleClass("d-none d-flex");
  });
  // discount-rate
  $(".rate").click(function () {
    $(this).find("i").toggleClass("fa-plus fa-minus");
    $(".discount-rate").toggleClass("d-none d-flex");
  });
  $(".discount-rate .form-check-input").on("change", function () {
    // Xóa class checked ở tất cả radio khác
    $(".discount-rate .form-check-input").removeClass("checked");

    // Thêm class checked cho radio đang được chọn
    if ($(this).is(":checked")) {
      $(this).addClass("checked");
    }
  });
  let lastChecked = null;

  $(".discount-rate .form-check-input").on("click", function () {
    // Nếu click lại chính nó -> bỏ chọn
    if (lastChecked && lastChecked[0] === this) {
      $(this).prop("checked", false).removeClass("checked");
      lastChecked = null;
    } else {
      // Gán lại lastChecked và xử lý class
      $(".discount-rate .form-check-input").removeClass("checked");
      $(this).addClass("checked");
      lastChecked = $(this);
    }
  });

  // Nâng cao
$(".lift-high").click(function() {
    $(this).find("i").toggleClass("fa-plus fa-minus");
    $(".lift-high-sub").toggleClass("d-none d-flex");
});

// Xử lý khi click vào toàn bộ hàng (form-check)
$(".lift-high-sub .form-check").on("click", function(e) {
    e.preventDefault(); // Ngăn chặn hành vi mặc định nếu cần
    const $input = $(this).find(".form-check-input");

    // Nếu click lại vào hàng của radio button đang được chọn
    if (lastChecked && lastChecked[0] === $input[0] && $input.is(":checked")) {
        $input.prop("checked", false).removeClass("checked");
        lastChecked = null; // Xóa lastChecked khi bỏ chọn
    } else {
        // Bỏ chọn tất cả radio button khác (do thuộc tính radio)
        $(".lift-high-sub .form-check-input").prop("checked", false).removeClass("checked");
        // Chọn radio button hiện tại và thêm class checked
        $input.prop("checked", true).addClass("checked");
        lastChecked = $input; // Lưu lại radio button vừa được chọn
    }
});
});
