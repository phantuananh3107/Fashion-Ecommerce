$(() => {
  $(".product-item .inner-meta .inner-favorite i").click((e) => {
    $(e.target).toggleClass("favorite-active")
  })
})
