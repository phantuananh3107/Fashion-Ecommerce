package dao;

import java.util.List;
import java.util.Map;

public interface DashboardDAO {
    // 1. Doanh số (Tổng số đơn hàng)
    int getTotalOrders(String timeFilter);

    // 2. Doanh thu (Tổng tiền các đơn thành công)
    double getTotalRevenue(String timeFilter);

    // 3. Khách hàng (Tổng số tài khoản user)
    int getTotalCustomers(String timeFilter);

    // HÀM : Lấy dữ liệu cho biểu đồ
    // Trả về Map chứa: "labels" (List<String>), "orders" (List<Integer>), "revenue" (List<Double>)
    Map<String, List<Object>> getLineChartData(String timeFilter);

    Map<String, Double> getCategoryRadarData();

    Map<String, Integer> getOrderStatusCounts(String timeFilter);
    double getTotalRevenueByDateRange(String fromDate, String toDate);
    Map<String, Integer> getOrderStatusCountsByDateRange(String fromDate, String toDate);
}