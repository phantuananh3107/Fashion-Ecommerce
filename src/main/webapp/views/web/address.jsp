<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<div class="container mt-5 mb-5" style="min-height: 70vh;">
    <div class="row">
        <div class="col-md-3">
            <div class="sidebar-wrapper">
                <div class="sidebar-user-profile d-flex align-items-center mb-3 pb-3 border-bottom">
                    <div class="user-avatar text-secondary me-3">
                        <i class="fa-solid fa-circle-user fa-3x"></i>
                    </div>
                    <div class="user-info">
                        <h6 class="mb-0 fw-bold">${user.fullname}</h6>
                    </div>
                </div>

                <div class="list-group sidebar-menu border-0">
                    <a href="${pageContext.request.contextPath}/customer/profile" class="list-group-item list-group-item-action border-0 mb-1">
                        <i class="fa-regular fa-user me-2"></i> Thông tin tài khoản
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/order-history" class="list-group-item list-group-item-action border-0 mb-1">
                        <i class="fa-solid fa-clock-rotate-left me-2"></i> Quản lý đơn hàng
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/wishlist" class="list-group-item list-group-item-action border-0 mb-1">
                        <i class="fa-regular fa-heart me-2"></i> Danh sách yêu thích
                    </a>
                    <a href="${pageContext.request.contextPath}/customer/address" class="list-group-item list-group-item-action active-menu border-0 mb-1 fw-bold">
                        <i class="fa-solid fa-location-dot me-2"></i> Sổ địa chỉ
                    </a>
                </div>
            </div>
        </div>

        <div class="col-md-9">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h3 class="mb-0 fw-bold">Sổ địa chỉ</h3>
                <button type="button" class="btn btn-add-address" data-bs-toggle="modal" data-bs-target="#addAddressModal">
                    <i class="fa-solid fa-plus"></i> THÊM ĐỊA CHỈ
                </button>
            </div>

            <c:choose>
                <c:when test="${empty addressList}">
                    <div class="alert alert-info">Bạn chưa có địa chỉ giao hàng nào. Vui lòng thêm địa chỉ mới.</div>
                </c:when>
                <c:otherwise>
                    <c:forEach var="addr" items="${addressList}">
                        <div class="address-card position-relative">
                            <div class="row align-items-center">
                                <div class="col-md-9">
                                    <h6 class="fw-bold mb-2">${addr.receiverName}</h6>
                                    <p class="mb-1 text-muted" style="font-size: 14px;">Điện thoại: ${addr.receiverPhone}</p>
                                    <p class="mb-0 text-muted" style="font-size: 14px;">Địa chỉ: ${addr.addressDetail}</p>
                                </div>
                                <div class="col-md-3 text-end d-flex flex-column align-items-end justify-content-center">
                                    <div class="mb-2">
                                        <a href="#" class="edit-link text-muted me-3"
                                           data-bs-toggle="modal"
                                           data-bs-target="#editAddressModal"
                                           onclick="fillEditModal(${addr.addressId}, '${addr.receiverName}', '${addr.receiverPhone}', '${addr.addressDetail}', ${addr.isDefault()})">
                                            Sửa
                                        </a>
                                    </div>
                                    <c:if test="${addr.isDefault()}">
                                        <span class="badge-default mt-1">Mặc định</span>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<div class="modal fade" id="addAddressModal" tabindex="-1" aria-labelledby="addAddressModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header border-bottom-0">
                <h5 class="modal-title fw-bold" id="addAddressModalLabel">Thêm địa chỉ giao hàng mới</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="${pageContext.request.contextPath}/customer/address" method="POST">
                <input type="hidden" name="action" value="add">

                <div class="modal-body pt-0">
                    <div class="mb-3">
                        <label class="form-label fw-bold">Họ và tên người nhận</label>
                        <input type="text" class="form-control" name="receiverName" placeholder="Nhập họ tên" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Số điện thoại</label>
                        <input type="text" class="form-control" name="receiverPhone" placeholder="Nhập số điện thoại" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Địa chỉ chi tiết</label>
                        <textarea class="form-control" name="addressDetail" rows="3" placeholder="Ví dụ: 121 Mỹ Đình, Nam Từ Liêm, Hà Nội" required></textarea>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="isDefault" value="true" id="isDefaultCheck">
                        <label class="form-check-label" for="isDefaultCheck">
                            Đặt làm địa chỉ mặc định
                        </label>
                    </div>
                </div>
                <div class="modal-footer border-top-0">
                    <button type="button" class="btn btn-outline-dark" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-dark">Lưu địa chỉ</button>
                </div>
            </form>
        </div>
    </div>
</div>

<div class="modal fade" id="editAddressModal" tabindex="-1" aria-labelledby="editAddressModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header border-bottom-0">
                <h5 class="modal-title fw-bold" id="editAddressModalLabel">Cập nhật địa chỉ</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form action="${pageContext.request.contextPath}/customer/address" method="POST">
                <input type="hidden" name="action" value="edit">
                <input type="hidden" name="addressId" id="editAddressId">

                <div class="modal-body pt-0">
                    <div class="mb-3">
                        <label class="form-label fw-bold">Họ và tên người nhận</label>
                        <input type="text" class="form-control" name="receiverName" id="editReceiverName" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Số điện thoại</label>
                        <input type="text" class="form-control" name="receiverPhone" id="editReceiverPhone" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label fw-bold">Địa chỉ chi tiết</label>
                        <textarea class="form-control" name="addressDetail" id="editAddressDetail" rows="3" required></textarea>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="checkbox" name="isDefault" value="true" id="editIsDefaultCheck">
                        <label class="form-check-label" for="editIsDefaultCheck">
                            Đặt làm địa chỉ mặc định
                        </label>
                    </div>
                </div>
                <div class="modal-footer border-top-0">
                    <button type="button" class="btn btn-outline-dark" data-bs-dismiss="modal">Hủy</button>
                    <button type="submit" class="btn btn-dark">Lưu thay đổi</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    function fillEditModal(id, name, phone, detail, isDefault) {
        document.getElementById('editAddressId').value = id;
        document.getElementById('editReceiverName').value = name;
        document.getElementById('editReceiverPhone').value = phone;
        document.getElementById('editAddressDetail').value = detail;
        document.getElementById('editIsDefaultCheck').checked = isDefault;
    }
</script>