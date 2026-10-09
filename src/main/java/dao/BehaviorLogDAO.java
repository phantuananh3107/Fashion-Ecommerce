package dao;

import model.BehaviorEventType;

public interface BehaviorLogDAO {
    boolean logBehavior(Integer userId, String sessionId, Integer productId, BehaviorEventType eventType, String searchKeyword);
    boolean isSpamView(Integer userId, String sessionId, int productId, int windowSeconds);
    int syncSessionToUser(String sessionId, int userId);
    boolean logRecommendationEvent(Integer userId, String sessionId, int productId, String source, String eventType);
}
