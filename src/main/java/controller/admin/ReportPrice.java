package controller.admin;

import dao.DashboardDAO;
import dao.Impl.DashboardDAOImpl;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@WebServlet("/admin/admin-report")
public class ReportPrice extends HttpServlet {

    private DashboardDAO dashboardDAO;

    @Override
    public void init() {
        dashboardDAO = new DashboardDAOImpl();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 1. Hứng tham số ngày tháng từ form bộ lọc trên JSP gửi xuống (nếu có)
            String fromDate = request.getParameter("fromDate");
            String toDate = request.getParameter("toDate");

            double totalRevenue = 0;
            Map<String, Integer> statusCounts = null;

            // 2. KIỂM TRA ĐIỀU KIỆN LỌC
            if (fromDate != null && !fromDate.isEmpty() && toDate != null && !toDate.isEmpty()) {
                // TH1: Nếu Admin có chọn Khoảng ngày -> Gọi hàm DAO theo DateRange
                totalRevenue = dashboardDAO.getTotalRevenueByDateRange(fromDate, toDate);
                statusCounts = dashboardDAO.getOrderStatusCountsByDateRange(fromDate, toDate);

                // Giữ lại giá trị ngày gửi ngược ra JSP để các ô input không bị trắng
                request.setAttribute("fromDate", fromDate);
                request.setAttribute("toDate", toDate);
            } else {
                // TH2: Nếu vừa mở trang (chưa lọc) -> Lấy dữ liệu toàn thời gian ("all")
                totalRevenue = dashboardDAO.getTotalRevenue("all");
                statusCounts = dashboardDAO.getOrderStatusCounts("all");
            }

            // 3. XỬ LÝ SỐ LIỆU ĐỂ ĐẨY RA GIAO DIỆN
            request.setAttribute("totalRevenue", totalRevenue);

            // Bóc tách Map đếm trạng thái đơn hàng an toàn (dùng getOrDefault để tránh lỗi Null)
            int successCount   = statusCounts.getOrDefault("Thành công", 0);
            int confirmedCount = statusCounts.getOrDefault("Đã xác nhận", 0);
            int pendingCount   = statusCounts.getOrDefault("Chờ xử lý", 0) + statusCounts.getOrDefault("pending", 0);
            int cancelCount    = statusCounts.getOrDefault("cancelled", 0) + statusCounts.getOrDefault("Đã hủy", 0);

            // Tính tổng số lượng tất cả các đơn hàng
            int totalOrdersReport = successCount + confirmedCount + pendingCount + cancelCount;

            // Truyền dữ liệu sang JSP để vẽ biểu đồ Donut
            request.setAttribute("successCount", successCount);
            request.setAttribute("confirmedCount", confirmedCount);
            request.setAttribute("pendingCount", pendingCount);
            request.setAttribute("cancelCount", cancelCount);

            // Truyền Tổng số đơn sang JSP cho thẻ Số lượng đơn bên phải
            request.setAttribute("totalOrdersReport", totalOrdersReport);

            RequestDispatcher rd = request.getRequestDispatcher("/views/admin/report.jsp");
            rd.forward(request, response);

        } catch (Exception e) {
            System.out.println("Error fetching statistics: " + e.getMessage());
            request.setAttribute("error", "Lỗi khi tải dữ liệu thống kê: " + e.getMessage());
            RequestDispatcher rd = request.getRequestDispatcher("/views/admin/report.jsp");
            rd.forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Form lọc dùng phương thức GET, nhưng gọi chung doGet cho an toàn nếu có lỡ dùng POST
        doGet(request, response);
    }
}