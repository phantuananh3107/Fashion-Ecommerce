package recommend;

import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecommendationSeeder {

    public static void main(String[] args) {
        System.out.println("Starting Recommendation Data Seeder...");
        try (Connection conn = DBUtil.getConnection()) {
            // Get available active product IDs and category IDs
            List<Integer> productIds = new ArrayList<>();
            String pSql = "SELECT id FROM Product WHERE isDeleted = FALSE AND product_quantity > 0 LIMIT 20";
            try (PreparedStatement ps = conn.prepareStatement(pSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    productIds.add(rs.getInt("id"));
                }
            }

            if (productIds.isEmpty()) {
                System.out.println("No active products found to seed data.");
                return;
            }

            // Clear old demo data
            String delSql = "DELETE FROM user_behavior_log WHERE session_id LIKE 'demo_%'";
            try (PreparedStatement ps = conn.prepareStatement(delSql)) {
                ps.executeUpdate();
            }

            String delEvtSql = "DELETE FROM recommendation_event WHERE session_id LIKE 'demo_%'";
            try (PreparedStatement ps = conn.prepareStatement(delEvtSql)) {
                ps.executeUpdate();
            }

            // Generate behavior logs for 20 mock users/sessions
            String insertLogSql = "INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at) "
                                + "VALUES (?, ?, ?, ?, ?, ?, NOW() - INTERVAL ? DAY)";

            String insertEvtSql = "INSERT INTO recommendation_event (user_id, session_id, product_id, source, event_type, created_at) "
                                + "VALUES (?, ?, ?, ?, ?, NOW() - INTERVAL ? DAY)";

            try (PreparedStatement psLog = conn.prepareStatement(insertLogSql);
                 PreparedStatement psEvt = conn.prepareStatement(insertEvtSql)) {

                for (int u = 1; u <= 20; u++) {
                    int userId = 900 + u;
                    String sessionId = "demo_session_" + u;

                    // Assign 2 favorite products per user
                    int p1 = productIds.get((u * 2) % productIds.size());
                    int p2 = productIds.get((u * 2 + 1) % productIds.size());

                    // Log VIEW events
                    psLog.setInt(1, userId);
                    psLog.setString(2, sessionId);
                    psLog.setInt(3, p1);
                    psLog.setString(4, "VIEW");
                    psLog.setDouble(5, 1.0);
                    psLog.setNull(6, java.sql.Types.VARCHAR);
                    psLog.setInt(7, u % 7);
                    psLog.addBatch();

                    psLog.setInt(1, userId);
                    psLog.setString(2, sessionId);
                    psLog.setInt(3, p2);
                    psLog.setString(4, "VIEW");
                    psLog.setDouble(5, 1.0);
                    psLog.setNull(6, java.sql.Types.VARCHAR);
                    psLog.setInt(7, u % 5);
                    psLog.addBatch();

                    // Log ADD_TO_CART / PURCHASE for even users
                    if (u % 2 == 0) {
                        psLog.setInt(1, userId);
                        psLog.setString(2, sessionId);
                        psLog.setInt(3, p1);
                        psLog.setString(4, "ADD_TO_CART");
                        psLog.setDouble(5, 3.0);
                        psLog.setNull(6, java.sql.Types.VARCHAR);
                        psLog.setInt(7, u % 3);
                        psLog.addBatch();
                    }

                    if (u % 4 == 0) {
                        psLog.setInt(1, userId);
                        psLog.setString(2, sessionId);
                        psLog.setInt(3, p2);
                        psLog.setString(4, "PURCHASE");
                        psLog.setDouble(5, 5.0);
                        psLog.setNull(6, java.sql.Types.VARCHAR);
                        psLog.setInt(7, u % 2);
                        psLog.addBatch();
                    }

                    // Log Recommendation Events (IMPRESSION & CLICK)
                    psEvt.setInt(1, userId);
                    psEvt.setString(2, sessionId);
                    psEvt.setInt(3, p1);
                    psEvt.setString(4, "HOME_PERSONAL");
                    psEvt.setString(5, "IMPRESSION");
                    psEvt.setInt(6, u % 4);
                    psEvt.addBatch();

                    if (u % 3 == 0) {
                        psEvt.setInt(1, userId);
                        psEvt.setString(2, sessionId);
                        psEvt.setInt(3, p1);
                        psEvt.setString(4, "HOME_PERSONAL");
                        psEvt.setString(5, "CLICK");
                        psEvt.setInt(6, u % 4);
                        psEvt.addBatch();
                    }
                }

                psLog.executeBatch();
                psEvt.executeBatch();
            }

            System.out.println("Seeding completed successfully! Inserted behavior logs for 20 mock users.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
