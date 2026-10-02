<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Trang chi tiết người dùng | TA shop</title>
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
</head>
<body>
<main id="main" class="main">
    <div class="pagetitle">
        <h1>Thống kê người dùng</h1>
    </div>
    <div class="chart-container">
        <div class="chart-box">
            <canvas id="monthlyChart"></canvas>
            <h5 class="chart-title">Người dùng mới theo tháng</h5>
        </div>
        <div class="chart-box">
            <canvas id="statusChart"></canvas>
            <h5 class="chart-title">Trạng thái hoạt động</h5>
        </div>
        <div class="chart-box">
            <canvas id="genderChart"></canvas>
            <h5 class="chart-title">Phân bố giới tính</h5>
        </div>
        <div class="chart-box">
            <canvas id="roleChart"></canvas>
            <h5 class="chart-title">Phân loại theo vai trò</h5>
        </div>
    </div>
        <a href="admin-manage-account" class="btn btn-secondary">
            <i class="bi bi-arrow-left"></i> Quay lại danh sách
        </a>
    </div>
</main>
<style>
    body {
        margin: 0;
        padding: 0;
        font-family: Arial, sans-serif;
        background: #f0f0f0;
    }

    h2 {
        margin-top: 20px;
        text-align: center;
    }

    .chart-container {
        display: flex;
        flex-wrap: wrap;
        justify-content: center;
        gap: 30px;
        margin: 30px auto;
        max-width: 1200px;
    }

    .chart-box {
        background: #fff;
        padding: 20px;
        border-radius: 16px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        width: 45%;
        min-width: 400px;
    }

    canvas {
        width: 100% !important;
        height: 300px !important;
    }

    .chart-title {
        text-align: center;
        margin-top: 10px;
        font-size: 16px;
        font-weight: 500;
        color: red;
    }
</style>
<c:set var="monthlyData" value="${newUsersByMonth}" />
<c:set var="genderData" value="${genderCounts}" />
<c:set var="statusData" value="${statusCounts}" />
<c:set var="totalUser" value="${totalUsers}" />
<c:set var="roleData" value="${roleCounts}" />
<script>
    window.onload = function () {
        // 1. Biểu đồ người dùng mới theo tháng
        const monthlyLabels = [];
        const monthlyValues = [];

        <c:forEach var="entry" items="${monthlyData}">
        monthlyLabels.push("${entry.key}");
        monthlyValues.push(${entry.value});
        </c:forEach>

        new Chart(document.getElementById('monthlyChart'), {
            type: 'line',
            data: {
                labels: monthlyLabels,
                datasets: [{
                    label: 'Người dùng mới',
                    data: monthlyValues,
                    backgroundColor: 'rgba(54, 162, 235, 0.5)',
                    borderColor: 'blue',
                    fill: true,
                    tension: 0.4
                }]
            }
        });

        // 2. Biểu đồ trạng thái
        const statusLabels = [];
        const statusValues = [];
        <c:forEach var="entry" items="${statusData}">
        statusLabels.push("${entry.key}");
        statusValues.push(${entry.value});
        </c:forEach>

        new Chart(document.getElementById('statusChart'), {
            type: 'pie',
            data: {
                labels: statusLabels,
                datasets: [{
                    data: statusValues,
                    backgroundColor: ['#28a745', '#dc3545']
                }]
            }
        });

        // 3. Biểu đồ giới tính
        const genderLabels = [];
        const genderValues = [];
        <c:forEach var="entry" items="${genderData}">
        genderLabels.push("${entry.key}");
        genderValues.push(${entry.value});
        </c:forEach>

        new Chart(document.getElementById('genderChart'), {
            type: 'doughnut',
            data: {
                labels: genderLabels,
                datasets: [{
                    data: genderValues,
                    backgroundColor: ['#007bff', '#e83e8c', '#6c757d']
                }]
            }
        });

        // 4. Biểu đồ phân loại vai trò
        const roleLabels = [];
        const roleValues = [];
        <c:forEach var="entry" items="${roleData}">
        roleLabels.push("${entry.key}");
        roleValues.push(${entry.value});
        </c:forEach>

        new Chart(document.getElementById('roleChart'), {
            type: 'bar',
            data: {
                labels: roleLabels,
                datasets: [{
                    label: 'Số lượng',
                    data: roleValues,
                    backgroundColor: ['#17a2b8', '#ffc107', '#6f42c1']
                }]
            },
            options: {
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: { precision: 0 }
                    }
                }
            }
        });

    };
</script>

</body>
</html>


