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
  $(".rate").click(function () {
    $(this).find("i").toggleClass("fa-plus fa-minus");
    $(".discount-rate").toggleClass("d-none d-flex");
  });
  $(".discount-rate .form-check-input").on("change", function () {
    $(".discount-rate .form-check-input").removeClass("checked");

    if ($(this).is(":checked")) {
      $(this).addClass("checked");
    }
  });
  let lastChecked = null;

  $(".discount-rate .form-check-input").on("click", function () {
    if (lastChecked && lastChecked[0] === this) {
      $(this).prop("checked", false).removeClass("checked");
      lastChecked = null;
    } else {
      $(".discount-rate .form-check-input").removeClass("checked");
      $(this).addClass("checked");
      lastChecked = $(this);
    }
  });

$(".lift-high").click(function() {
    $(this).find("i").toggleClass("fa-plus fa-minus");
    $(".lift-high-sub").toggleClass("d-none d-flex");
});

$(".lift-high-sub .form-check").on("click", function(e) {
    e.preventDefault(); // Ngăn chặn hành vi mặc định nếu cần
    const $input = $(this).find(".form-check-input");

    if (lastChecked && lastChecked[0] === $input[0] && $input.is(":checked")) {
        $input.prop("checked", false).removeClass("checked");
        lastChecked = null; // Xóa lastChecked khi bỏ chọn
    } else {
        $(".lift-high-sub .form-check-input").prop("checked", false).removeClass("checked");
        $input.prop("checked", true).addClass("checked");
        lastChecked = $input;
    }
});
});
