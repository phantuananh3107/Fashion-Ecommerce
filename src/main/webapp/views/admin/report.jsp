<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Báo cáo thống kê | TA shop</title>
</head>
<body>
<main id="main" class="main">
    <div class="pagetitle">
        <h1>Báo cáo thống kê</h1>
    </div>

    <c:if test="${not empty error}">
        <div class="alert alert-danger">${error}</div>
    </c:if>

    <section class="section dashboard">

        <div class="row mb-3">
            <div class="col-12">
                <div class="card mb-0 shadow-sm">
                    <div class="card-body p-3 d-flex justify-content-between align-items-center flex-wrap gap-2">

                        <div class="d-flex align-items-center flex-wrap gap-2">
                            <h6 class="mb-0 me-2 fw-bold text-primary text-nowrap"><i class="bi bi-calendar-range"></i> Lọc dữ liệu:</h6>
                            <form action="${pageContext.request.contextPath}/admin/admin-report" method="GET" class="d-flex align-items-center m-0 gap-2">
                                <input type="date" name="fromDate" value="${fromDate}" class="form-control form-control-sm" style="width: 150px;" required>
                                <span class="text-muted fw-bold">-</span>
                                <input type="date" name="toDate" value="${toDate}" class="form-control form-control-sm" style="width: 150px;" required>
                                <button type="submit" class="btn btn-primary btn-sm text-nowrap">
                                    <i class="bi bi-filter"></i> Xem báo cáo
                                </button>
                            </form>
                        </div>

                        <div>
                            <a href="admin-report" class="btn btn-light btn-sm border text-nowrap">
                                <i class="bi bi-arrow-clockwise"></i> Làm mới
                            </a>
                        </div>

                    </div>
                </div>
            </div>
        </div>
        <div class="row">
            <div class="col-xxl-6 col-md-6">
                <div class="card info-card revenue-card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">Tổng Doanh Thu
                            <span>|
                                <c:choose>
                                    <c:when test="${not empty fromDate}">${fromDate} đến ${toDate}</c:when>
                                    <c:otherwise>Toàn thời gian</c:otherwise>
                                </c:choose>
                            </span>
                        </h5>
                        <div class="d-flex align-items-center">
                            <div class="card-icon rounded-circle d-flex align-items-center justify-content-center">
                                <i class="bi bi-currency-dollar"></i>
                            </div>
                            <div class="ps-3">
                                <h6>
                                    <c:choose>
                                        <c:when test="${totalRevenue > 0}">
                                            <fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/> đ
                                        </c:when>
                                        <c:otherwise>0 đ</c:otherwise>
                                    </c:choose>
                                </h6>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-xxl-6 col-md-6">
                <div class="card info-card sales-card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">Số Lượng Đơn
                            <span>|
                                <c:choose>
                                    <c:when test="${not empty fromDate}">${fromDate} đến ${toDate}</c:when>
                                    <c:otherwise>Toàn thời gian</c:otherwise>
                                </c:choose>
                            </span>
                        </h5>
                        <div class="d-flex align-items-center">
                            <div class="card-icon rounded-circle d-flex align-items-center justify-content-center">
                                <i class="bi bi-cart"></i>
                            </div>
                            <div class="ps-3">
                                <h6>${empty totalOrdersReport ? 0 : totalOrdersReport}</h6>
                                <span class="text-muted small pt-2 ps-1">đơn hàng trên hệ thống</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <div class="row">
            <div class="col-lg-8 mx-auto">
                <div class="card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">Tỷ lệ Trạng thái Đơn hàng <span>| Phân tích hiệu suất</span></h5>

                        <div id="donutChart" style="min-height: 400px;" class="echart"></div>

                        <script>
                            document.addEventListener("DOMContentLoaded", () => {
                                const success   = ${empty successCount ? 0 : successCount};
                                const confirmed = ${empty confirmedCount ? 0 : confirmedCount};
                                const pending   = ${empty pendingCount ? 0 : pendingCount};
                                const cancel    = ${empty cancelCount ? 0 : cancelCount};

                                echarts.init(document.querySelector("#donutChart")).setOption({
                                    tooltip: {
                                        trigger: 'item',
                                        formatter: '{a} <br/>{b}: <b>{c}</b> đơn ({d}%)'
                                    },
                                    legend: {
                                        top: '5%',
                                        left: 'center'
                                    },
                                    color: ['#2eca6a', '#0dcaf0', '#ff771d', '#dc3545'],
                                    series: [{
                                        name: 'Số lượng đơn',
                                        type: 'pie',
                                        radius: ['45%', '75%'],
                                        avoidLabelOverlap: false,
                                        itemStyle: {
                                            borderRadius: 10,
                                            borderColor: '#fff',
                                            borderWidth: 2
                                        },
                                        label: {
                                            show: false,
                                            position: 'center'
                                        },
                                        emphasis: {
                                            label: {
                                                show: true,
                                                fontSize: '22',
                                                fontWeight: 'bold'
                                            }
                                        },
                                        labelLine: {
                                            show: false
                                        },
                                        data: [
                                            { value: success, name: 'Thành công' },
                                            { value: confirmed, name: 'Đã xác nhận' },
                                            { value: pending, name: 'Chờ xử lý' },
                                            { value: cancel, name: 'Đã hủy' }
                                        ]
                                    }]
                                });
                            });
                        </script>

                    </div>
                </div>
            </div>
        </div>
    </section>
</main>
</body>
</html>