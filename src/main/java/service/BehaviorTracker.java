package service;

import dao.BehaviorLogDAO;
import dao.Impl.BehaviorLogDAOImpl;
import model.BehaviorEventType;

import java.util.List;

public class BehaviorTracker {
    private static final BehaviorLogDAO logDAO = new BehaviorLogDAOImpl();

    public static void trackView(Integer userId, String sessionId, int productId) {
        try {
            if (productId <= 0) return;
            // Anti-spam check for VIEW (30 seconds)
            if (logDAO.isSpamView(userId, sessionId, productId, 30)) {
                return;
            }
            logDAO.logBehavior(userId, sessionId, productId, BehaviorEventType.VIEW, null);
        } catch (Throwable t) {
            System.err.println("[BehaviorTracker] Error tracking view: " + t.getMessage());
        }
    }

    public static void trackClick(Integer userId, String sessionId, int productId) {
        try {
            if (productId <= 0) return;
            logDAO.logBehavior(userId, sessionId, productId, BehaviorEventType.CLICK, null);
        } catch (Throwable t) {
            System.err.println("[BehaviorTracker] Error tracking click: " + t.getMessage());
        }
    }

    public static void trackSearch(Integer userId, String sessionId, String keyword, List<Integer> topProductIds) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) return;
            // Log search keyword entry
            logDAO.logBehavior(userId, sessionId, null, BehaviorEventType.SEARCH, keyword.trim());

            // If top result products exist, log search behavior for top candidate products (limit to top 5)
            if (topProductIds != null && !topProductIds.isEmpty()) {
                int count = 0;
                for (Integer pId : topProductIds) {
                    if (pId != null && pId > 0) {
                        logDAO.logBehavior(userId, sessionId, pId, BehaviorEventType.SEARCH, keyword.trim());
                        count++;
                        if (count >= 5) break;
                    }
                }
            }
        } catch (Throwable t) {
            System.err.println("[BehaviorTracker] Error tracking search: " + t.getMessage());
        }
    }

    public static void trackAddToCart(Integer userId, String sessionId, int productId) {
        try {
            if (productId <= 0) return;
            logDAO.logBehavior(userId, sessionId, productId, BehaviorEventType.ADD_TO_CART, null);
        } catch (Throwable t) {
            System.err.println("[BehaviorTracker] Error tracking add to cart: " + t.getMessage());
        }
    }

    public static void trackPurchase(Integer userId, String sessionId, int productId) {
        try {
            if (productId <= 0) return;
            logDAO.logBehavior(userId, sessionId, productId, BehaviorEventType.PURCHASE, null);
        } catch (Throwable t) {
            System.err.println("[BehaviorTracker] Error tracking purchase: " + t.getMessage());
        }
    }

    public static void syncSessionUser(String sessionId, int userId) {
        try {
            if (sessionId != null && !sessionId.isEmpty() && userId > 0) {
                logDAO.syncSessionToUser(sessionId, userId);
            }
        } catch (Throwable t) {
            System.err.println("[BehaviorTracker] Error syncing session to user: " + t.getMessage());
        }
    }

    public static void trackImpression(Integer userId, String sessionId, int productId, String source) {
        try {
            if (productId <= 0 || source == null || source.isEmpty()) return;
            logDAO.logRecommendationEvent(userId, sessionId, productId, source, "IMPRESSION");
        } catch (Throwable t) {
            System.err.println("[BehaviorTracker] Error tracking impression: " + t.getMessage());
        }
    }

    public static void trackRecClick(Integer userId, String sessionId, int productId, String source) {
        try {
            if (productId <= 0 || source == null || source.isEmpty()) return;
            logDAO.logRecommendationEvent(userId, sessionId, productId, source, "CLICK");
            // Also log CLICK behavior event
            trackClick(userId, sessionId, productId);
        } catch (Throwable t) {
            System.err.println("[BehaviorTracker] Error tracking rec click: " + t.getMessage());
        }
    }

    public static void trackRecOrdered(Integer userId, String sessionId, int productId, String source) {
        try {
            if (productId <= 0 || source == null || source.isEmpty()) return;
            logDAO.logRecommendationEvent(userId, sessionId, productId, source, "ORDERED");
        } catch (Throwable t) {
            System.err.println("[BehaviorTracker] Error tracking rec order: " + t.getMessage());
        }
    }
}
