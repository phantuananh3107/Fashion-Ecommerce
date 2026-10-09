package controller.admin;

import util.DBUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/admin/recommendation-report")
public class RecommendationReport extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        List<Map<String, Object>> ctrList = new ArrayList<>();
        int recOrdersCount = 0;
        List<Map<String, Object>> topRecProducts = new ArrayList<>();

        // 1. CTR per recommendation source
        String ctrSql = "SELECT source, "
                      + "SUM(CASE WHEN event_type = 'IMPRESSION' THEN 1 ELSE 0 END) AS impressions, "
                      + "SUM(CASE WHEN event_type = 'CLICK' THEN 1 ELSE 0 END) AS clicks "
                      + "FROM recommendation_event "
                      + "GROUP BY source";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(ctrSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                String source = rs.getString("source");
                long impressions = rs.getLong("impressions");
                long clicks = rs.getLong("clicks");
                double ctr = impressions > 0 ? (clicks * 100.0 / impressions) : 0.0;

                map.put("source", source);
                map.put("impressions", impressions);
                map.put("clicks", clicks);
                map.put("ctr", Math.round(ctr * 100.0) / 100.0);
                ctrList.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. Orders containing recommended products
        String recOrdersSql = "SELECT COUNT(DISTINCT o.order_id) AS rec_orders "
                            + "FROM `Order` o "
                            + "JOIN OrderDetail od ON o.order_id = od.order_id "
                            + "JOIN recommendation_event re ON od.product_id = re.product_id "
                            + "AND re.event_type = 'CLICK' AND re.created_at <= o.order_date";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(recOrdersSql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                recOrdersCount = rs.getInt("rec_orders");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 3. Top recommended products (most clicked & impressed)
        String topProductsSql = "SELECT p.id, p.product_name, p.product_image, "
                              + "SUM(CASE WHEN re.event_type = 'IMPRESSION' THEN 1 ELSE 0 END) AS impressions, "
                              + "SUM(CASE WHEN re.event_type = 'CLICK' THEN 1 ELSE 0 END) AS clicks "
                              + "FROM recommendation_event re "
                              + "JOIN Product p ON re.product_id = p.id "
                              + "GROUP BY p.id, p.product_name, p.product_image "
                              + "ORDER BY clicks DESC, impressions DESC "
                              + "LIMIT 10";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(topProductsSql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("productId", rs.getInt("id"));
                map.put("productName", rs.getString("product_name"));
                map.put("productImage", rs.getString("product_image"));
                map.put("impressions", rs.getLong("impressions"));
                map.put("clicks", rs.getLong("clicks"));
                topRecProducts.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("ctrList", ctrList);
        request.setAttribute("recOrdersCount", recOrdersCount);
        request.setAttribute("topRecProducts", topRecProducts);

        RequestDispatcher rd = request.getRequestDispatcher("/views/admin/recommendation-report.jsp");
        rd.forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
