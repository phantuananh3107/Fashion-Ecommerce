package controller.admin;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.OrderDAO;
import dao.Impl.OrderDAOImpl;
import dao.DashboardDAO;
import dao.Impl.DashboardDAOImpl;
// THÊM IMPORT CHO TOP SELLING
import dao.ITopSelling;
import dao.Impl.TopSellingImpl;
import model.OrderInfo;
import model.TopSellingProduct;

@WebServlet(urlPatterns = {"/admin/admin-home"})
public class HomeController extends HttpServlet {

	private static final long serialVersionUID = 2686801510274002166L;

	private DashboardDAO dashboardDAO;
	private ITopSelling topSellingDAO; // Khai báo thêm DAO cho Top Selling

	@Override
	public void init() {
		dashboardDAO = new DashboardDAOImpl();
		topSellingDAO = new TopSellingImpl(); // Khởi tạo DAO
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String timeFilter = request.getParameter("time");
		if (timeFilter == null || timeFilter.isEmpty()) {
			timeFilter = "month";
		}

		String timeLabel = "Tháng này";
		if ("today".equals(timeFilter)) timeLabel = "Hôm nay";
		else if ("year".equals(timeFilter)) timeLabel = "Năm này";
		else if ("all".equals(timeFilter)) timeLabel = "Tất cả";

		// Đã xóa hàm đếm Review
		int totalOrders = dashboardDAO.getTotalOrders(timeFilter);
		double totalRevenue = dashboardDAO.getTotalRevenue(timeFilter);
		int totalCustomers = dashboardDAO.getTotalCustomers(timeFilter);

		request.setAttribute("timeFilter", timeFilter);
		request.setAttribute("timeLabel", timeLabel);
		request.setAttribute("totalOrders", totalOrders);
		request.setAttribute("totalRevenue", totalRevenue);
		request.setAttribute("totalCustomers", totalCustomers);


		Map<String, List<Object>> chartData = dashboardDAO.getLineChartData(timeFilter);
		request.setAttribute("chartLabels", chartData.get("labels").toString());
		request.setAttribute("chartOrders", chartData.get("orders").toString());
		request.setAttribute("chartRevenue", chartData.get("revenue").toString());

		Map<String, Double> radarData = dashboardDAO.getCategoryRadarData();
		request.setAttribute("valSoMi", radarData.get("Áo sơ mi"));
		request.setAttribute("valThun", radarData.get("Áo thun"));
		request.setAttribute("valQuan", radarData.get("Quần dài"));
		request.setAttribute("valChanVay", radarData.get("Chân váy"));
		request.setAttribute("valDamCS", radarData.get("Đầm công sở"));
		request.setAttribute("valDamDH", radarData.get("Đầm dạ hội"));

		OrderDAO orderDAO = new OrderDAOImpl();
		List<OrderInfo> orderList = orderDAO.getRecentOrders(50);
		request.setAttribute("orderList", orderList);

		// THÊM MỚI: Lấy danh sách Top 5 sản phẩm bán chạy nhất truyền sang JSP
		List<TopSellingProduct> topProductList = topSellingDAO.getTopSellingProducts(5, 0);
		request.setAttribute("topProductList", topProductList);

		RequestDispatcher rd = request.getRequestDispatcher("/views/admin/home.jsp");
		rd.forward(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
}