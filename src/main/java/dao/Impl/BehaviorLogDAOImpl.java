package dao.Impl;

import dao.BehaviorLogDAO;
import model.BehaviorEventType;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class BehaviorLogDAOImpl implements BehaviorLogDAO {

    @Override
    public boolean logBehavior(Integer userId, String sessionId, Integer productId, BehaviorEventType eventType, String searchKeyword) {
        String sql = "INSERT INTO user_behavior_log (user_id, session_id, product_id, event_type, event_weight, search_keyword, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (userId != null && userId > 0) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setString(2, sessionId != null ? sessionId : "");

            if (productId != null && productId > 0) {
                ps.setInt(3, productId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, eventType.name());
            ps.setDouble(5, eventType.getWeight());

            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                ps.setString(6, searchKeyword.trim());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean isSpamView(Integer userId, String sessionId, int productId, int windowSeconds) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM user_behavior_log WHERE ");

        boolean hasUserId = (userId != null && userId > 0);
        if (hasUserId) {
            sql.append("(user_id = ? OR session_id = ?) ");
        } else {
            sql.append("session_id = ? ");
        }
        sql.append("AND product_id = ? AND event_type = 'VIEW' AND created_at >= NOW() - INTERVAL ? SECOND");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (hasUserId) {
                ps.setInt(idx++, userId);
                ps.setString(idx++, sessionId != null ? sessionId : "");
            } else {
                ps.setString(idx++, sessionId != null ? sessionId : "");
            }
            ps.setInt(idx++, productId);
            ps.setInt(idx++, windowSeconds);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public int syncSessionToUser(String sessionId, int userId) {
        if (sessionId == null || sessionId.isEmpty() || userId <= 0) {
            return 0;
        }
        String sql = "UPDATE user_behavior_log SET user_id = ? WHERE session_id = ? AND user_id IS NULL";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, sessionId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean logRecommendationEvent(Integer userId, String sessionId, int productId, String source, String eventType) {
        String sql = "INSERT INTO recommendation_event (user_id, session_id, product_id, source, event_type, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (userId != null && userId > 0) {
                ps.setInt(1, userId);
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            ps.setString(2, sessionId != null ? sessionId : "");
            ps.setInt(3, productId);
            ps.setString(4, source != null ? source : "UNKNOWN");
            ps.setString(5, eventType != null ? eventType : "IMPRESSION");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
