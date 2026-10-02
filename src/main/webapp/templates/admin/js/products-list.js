let currentPage = 1;
let itemsPerPage = 10;
let filteredProducts = [...products];

const container = document.getElementById("productContainer");
const pagination = document.getElementById("pagination");
const searchInput = document.getElementById("searchInput");
const itemsPerPageSelect = document.getElementById("itemsPerPage");

function renderProducts() {
  container.innerHTML = "";
  const start = (currentPage - 1) * itemsPerPage;
  const end = start + itemsPerPage;

  let pageItems;
  if (itemsPerPage >= filteredProducts.length) {
    pageItems = filteredProducts;
  } else {
    pageItems = filteredProducts.slice(start, end);
  }

  if (pageItems.length === 0) {
    container.innerHTML =
      "<p class='text-muted'>Không tìm thấy sản phẩm nào.</p>";
    return;
  }

  pageItems.forEach((product) => {
    const col = document.createElement("div");
    col.className = "col-md-6 mb-3";
    col.innerHTML = `
      <div class="card h-100" style="margin-bottom: 0px;">
        <div class="row g-0">
          <div class="col-auto">
            <img src="${product.image}" alt="${product.name}" class="img-fluid rounded-start product-thumb" style="width: 200px; height: 100%; object-fit: cover;" />
          </div>
          <div class="col">
            <div class="card-body d-flex flex-column justify-content-between h-100">
              <div>
                <h6 class="card-title mb-1">${product.name}</h6>
                <p class="mb-1 text-muted">${product.code}</p>
                <p class="mb-1">Giá: <strong>${product.price}</strong></p>
                
                <p class="mb-2 text-muted">Danh mục: ${product.category}</p>
              </div>
              <div class="d-flex justify-content-end gap-2">
                <a href="admin-repair-product?id=${product.id}" class="btn btn-sm btn-outline-warning" title="Sửa"><i class="bi bi-pencil"></i></a>
                <a href="admin-delete-product?id=${product.id}" class="btn btn-sm btn-outline-danger inner-remove" title="Xóa"><i class="bi bi-trash"></i></a>
                <a href="admin-detail-product?id=${product.id}" class="btn btn-sm btn-outline-info" title="Chi tiết"><i class="bi bi-eye"></i></a>
              </div>
            </div>
          </div>
        </div>
      </div>
    `;
    container.appendChild(col);
  });

  renderPagination();
}

function renderPagination() {
  pagination.innerHTML = "";

  if (itemsPerPage >= filteredProducts.length) return;

  const totalPages = Math.ceil(filteredProducts.length / itemsPerPage);

  for (let i = 1; i <= totalPages; i++) {
    const li = document.createElement("li");
    li.className = `page-item ${i === currentPage ? "active" : ""}`;
    li.innerHTML = `<a class="page-link" href="#">${i}</a>`;
    li.addEventListener("click", function (e) {
      e.preventDefault();
      currentPage = i;
      renderProducts();
    });
    pagination.appendChild(li);
  }
}

searchInput.addEventListener("input", function () {
  const keyword = this.value.toLowerCase();
  filteredProducts = products.filter((p) =>
    p.name.toLowerCase().includes(keyword)
  );
  currentPage = 1;
  renderProducts();
});

const dateFilter = document.getElementById("dateFilter");
const categoryFilter = document.getElementById("categoryFilter");

function applyFilters() {
  const keyword = searchInput.value.toLowerCase();
  const selectedCategory = categoryFilter.value;
  const selectedDate = dateFilter.value;

  filteredProducts = products.filter((product) => {
    const matchName = product.name.toLowerCase().includes(keyword);
    const matchCategory = !selectedCategory || product.category === selectedCategory;
	
    const productDate = product.createdDate?.substring(0, 10); // Cắt phần 'YYYY-MM-DD' từ 'YYYY-MM-DD HH:mm:ss'
    const matchDate = !selectedDate || productDate === selectedDate;

    return matchName && matchCategory && matchDate;
  });

  currentPage = 1;
  renderProducts();
}


searchInput.addEventListener("input", applyFilters);
categoryFilter.addEventListener("change", applyFilters);
dateFilter.addEventListener("change", applyFilters);


itemsPerPageSelect.addEventListener("change", function () {
  const value = this.value;
  if (value === "all") {
    itemsPerPage = filteredProducts.length;
  } else {
    itemsPerPage = parseInt(value);
  }
  currentPage = 1;
  renderProducts();
});

// Khởi tạo lần đầu
renderProducts();