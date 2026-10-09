package recommend;

import dao.IProductDAO;
import dao.Impl.ProductImpl;
import model.ProductObject;
import service.BehaviorTracker;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class RecommendationService {

    private final IProductDAO productDAO = new ProductImpl();

    /**
     * Personalized recommendations for a user/session using hybrid scoring.
     */
    public List<RecommendedProduct> forUser(Integer userId, String sessionId, int limit) {
        if (limit <= 0) limit = 8;
        boolean hasUserId = (userId != null && userId > 0);
        boolean hasSessionId = (sessionId != null && !sessionId.isEmpty());

        if (!hasUserId && !hasSessionId) {
            return trending(limit);
        }

        // 1. Fetch user behavior log for category preferences and recent anchor products
        Map<Integer, Double> categoryWeights = new HashMap<>();
        List<Integer> anchorProducts = new ArrayList<>();
        Set<Integer> interactedProducts = new HashSet<>();
        boolean isColdStart = true;

        String logSql = "SELECT ubl.product_id, ubl.event_type, ubl.event_weight, "
                      + "DATEDIFF(NOW(), ubl.created_at) AS days_diff, p.category_id "
                      + "FROM user_behavior_log ubl "
                      + "LEFT JOIN Product p ON ubl.product_id = p.id "
                      + "WHERE " + (hasUserId ? "(ubl.user_id = ? OR ubl.session_id = ?) " : "ubl.session_id = ? ")
                      + "AND ubl.created_at >= NOW() - INTERVAL 30 DAY "
                      + "ORDER BY ubl.created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(logSql)) {

            int idx = 1;
            if (hasUserId) {
                ps.setInt(idx++, userId);
                ps.setString(idx++, sessionId != null ? sessionId : "");
            } else {
                ps.setString(idx++, sessionId);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    isColdStart = false;
                    int pId = rs.getInt("product_id");
                    int catId = rs.getInt("category_id");
                    double weight = rs.getDouble("event_weight");
                    double daysDiff = rs.getDouble("days_diff");

                    double timeDecay = RecommenderEngine.calculateTimeDecay(daysDiff, RecommendationConfig.HALF_LIFE_DAYS);
                    double scoreAdd = weight * timeDecay;

                    if (catId > 0) {
                        categoryWeights.put(catId, categoryWeights.getOrDefault(catId, 0.0) + scoreAdd);
                    }

                    if (pId > 0) {
                        interactedProducts.add(pId);
                        if (!anchorProducts.contains(pId) && anchorProducts.size() < 5) {
                            anchorProducts.add(pId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (isColdStart) {
            return trending(limit);
        }

        // 2. Fetch products user has already purchased to exclude them
        Set<Integer> purchasedProductIds = getPurchasedProductIds(userId);

        // 3. Calculate market-basket co-occurrence for anchor products
        Map<Integer, Double> rawBasketScores = getCoOccurrenceScores(anchorProducts);

        // 4. Calculate popularity scores for all candidate products
        Map<Integer, Double> rawPopularityScores = getPopularityScores();

        // 5. Fetch all active candidate products
        List<ProductObject> allProducts = productDAO.getAllProducts();
        Map<Integer, Double> rawContentScores = new HashMap<>();

        for (ProductObject p : allProducts) {
            if (p.isDeleted() || p.getProductQuantity() <= 0) continue;
            if (purchasedProductIds.contains(p.getProductId())) continue;

            double catWeight = categoryWeights.getOrDefault(p.getCategoryId(), 0.0);
            rawContentScores.put(p.getProductId(), catWeight);
        }

        // 6. Normalize raw scores
        Map<Integer, Double> normContent = RecommenderEngine.normalizeScores(rawContentScores);
        Map<Integer, Double> normBasket = RecommenderEngine.normalizeScores(rawBasketScores);
        Map<Integer, Double> normPopularity = RecommenderEngine.normalizeScores(rawPopularityScores);

        // 7. Compute final hybrid scores
        List<RecommendedProduct> candidateList = new ArrayList<>();
        for (ProductObject p : allProducts) {
            int pId = p.getProductId();
            if (p.isDeleted() || p.getProductQuantity() <= 0) continue;
            if (purchasedProductIds.contains(pId)) continue;

            double cScore = normContent.getOrDefault(pId, 0.0);
            double bScore = normBasket.getOrDefault(pId, 0.0);
            double pScore = normPopularity.getOrDefault(pId, 0.0);

            double finalScore = RecommenderEngine.computeHybridScore(cScore, bScore, pScore, false);

            if (finalScore > 0.0) {
                String reason = RecommenderEngine.generateReason(cScore, bScore, pScore, p.getCategoryName());
                candidateList.add(new RecommendedProduct(p, finalScore, reason, "HOME_PERSONAL"));
            }
        }

        // Sort descending by score
        candidateList.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // Apply category diversification
        List<RecommendedProduct> result = RecommenderEngine.diversifyByCategory(candidateList, RecommendationConfig.MAX_PER_CATEGORY);

        if (result.size() > limit) {
            result = result.subList(0, limit);
        }

        // Record IMPRESSION events
        for (RecommendedProduct item : result) {
            BehaviorTracker.trackImpression(userId, sessionId, item.getProduct().getProductId(), "HOME_PERSONAL");
        }

        return result;
    }

    /**
     * Recommendations for "Also Bought" (Khách mua sản phẩm này cũng mua)
     */
    public List<RecommendedProduct> alsoBought(int productId, int limit) {
        if (limit <= 0) limit = 6;
        List<RecommendedProduct> list = new ArrayList<>();

        String sql = "SELECT od2.product_id, COUNT(DISTINCT od1.order_id) AS co_count "
                   + "FROM OrderDetail od1 "
                   + "JOIN OrderDetail od2 ON od1.order_id = od2.order_id "
                   + "JOIN Product p ON od2.product_id = p.id "
                   + "WHERE od1.product_id = ? AND od2.product_id != ? "
                   + "AND p.isDeleted = FALSE AND p.product_quantity > 0 "
                   + "GROUP BY od2.product_id "
                   + "ORDER BY co_count DESC "
                   + "LIMIT ?";

        Set<Integer> addedIds = new HashSet<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            ps.setInt(2, productId);
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pId = rs.getInt("product_id");
                    double coCount = rs.getDouble("co_count");
                    ProductObject p = productDAO.getProductById(pId);
                    if (p != null && !p.isDeleted() && p.getProductQuantity() > 0) {
                        list.add(new RecommendedProduct(p, coCount, "Khách mua sản phẩm này cũng mua", "DETAIL_ALSO_BOUGHT"));
                        addedIds.add(pId);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // If co-occurrence is insufficient, fill with category recommendations
        if (list.size() < limit) {
            ProductObject currentProduct = productDAO.getProductById(productId);
            if (currentProduct != null) {
                List<RecommendedProduct> similar = similarTo(productId, limit - list.size());
                for (RecommendedProduct sim : similar) {
                    if (!addedIds.contains(sim.getProduct().getProductId())) {
                        sim.setReason("Sản phẩm cùng danh mục hot nhất");
                        sim.setSource("DETAIL_ALSO_BOUGHT");
                        list.add(sim);
                        addedIds.add(sim.getProduct().getProductId());
                    }
                }
            }
        }

        return list;
    }

    /**
     * Recommendations for "Similar Products" (Sản phẩm tương tự)
     */
    public List<RecommendedProduct> similarTo(int productId, int limit) {
        if (limit <= 0) limit = 6;
        List<RecommendedProduct> list = new ArrayList<>();

        ProductObject target = productDAO.getProductById(productId);
        if (target == null) return list;

        String sql = "SELECT p.*, c.category_name, "
                   + "COALESCE(sales.sold_qty, 0) AS total_sold "
                   + "FROM Product p "
                   + "LEFT JOIN category c ON p.category_id = c.category_id "
                   + "LEFT JOIN (SELECT product_id, SUM(quantity_sold) AS sold_qty FROM OrderDetail GROUP BY product_id) sales ON p.id = sales.product_id "
                   + "WHERE p.category_id = ? AND p.id != ? AND p.isDeleted = FALSE AND p.product_quantity > 0 "
                   + "ORDER BY total_sold DESC, p.id DESC "
                   + "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, target.getCategoryId());
            ps.setInt(2, productId);
            ps.setInt(3, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProductObject p = new ProductObject();
                    p.setProductId(rs.getInt("id"));
                    p.setProductCode(rs.getString("product_code"));
                    p.setProductName(rs.getString("product_name"));
                    p.setProductImage(rs.getString("product_image1"));
                    p.setProductImage1(rs.getString("product_image1"));
                    p.setProductImage2(rs.getString("product_image2"));
                    p.setProductImage3(rs.getString("product_image3"));
                    p.setProductImage4(rs.getString("product_image4"));
                    p.setProductPrice(rs.getDouble("product_price"));
                    p.setDiscountPercent(rs.getInt("discount_percent"));
                    p.setProductQuantity(rs.getInt("product_quantity"));
                    p.setProductColor(rs.getString("product_color"));
                    p.setProductSize(rs.getString("product_size"));
                    p.setProductDescription(rs.getString("product_description"));
                    p.setCategoryId(rs.getInt("category_id"));
                    p.setCategoryName(rs.getString("category_name"));

                    list.add(new RecommendedProduct(p, 1.0, "Cùng danh mục " + target.getCategoryName(), "DETAIL_SIMILAR"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Trending products (Đang được quan tâm nhiều) based on last 30 days sales & views.
     */
    public List<RecommendedProduct> trending(int limit) {
        if (limit <= 0) limit = 8;
        List<RecommendedProduct> list = new ArrayList<>();

        String sql = "SELECT p.id, "
                   + "(COALESCE(od_sum.purchases, 0) * 5 + COALESCE(ubl_sum.views, 0)) AS pop_score "
                   + "FROM Product p "
                   + "LEFT JOIN ( "
                   + "  SELECT od.product_id, SUM(od.quantity_sold) AS purchases "
                   + "  FROM OrderDetail od "
                   + "  JOIN `Order` o ON od.order_id = o.order_id "
                   + "  WHERE o.order_date >= NOW() - INTERVAL 30 DAY "
                   + "  GROUP BY od.product_id "
                   + ") od_sum ON p.id = od_sum.product_id "
                   + "LEFT JOIN ( "
                   + "  SELECT product_id, COUNT(*) AS views "
                   + "  FROM user_behavior_log "
                   + "  WHERE event_type IN ('VIEW', 'CLICK') AND created_at >= NOW() - INTERVAL 30 DAY "
                   + "  GROUP BY product_id "
                   + ") ubl_sum ON p.id = ubl_sum.product_id "
                   + "WHERE p.isDeleted = FALSE AND p.product_quantity > 0 "
                   + "ORDER BY pop_score DESC, p.id DESC "
                   + "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pId = rs.getInt("id");
                    double score = rs.getDouble("pop_score");
                    ProductObject p = productDAO.getProductById(pId);
                    if (p != null && !p.isDeleted() && p.getProductQuantity() > 0) {
                        list.add(new RecommendedProduct(p, score, "Đang được quan tâm nhiều", "HOME_TRENDING"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Recently viewed products by user/session.
     */
    public List<RecommendedProduct> recentlyViewed(Integer userId, String sessionId, int limit) {
        if (limit <= 0) limit = 6;
        List<RecommendedProduct> list = new ArrayList<>();
        boolean hasUserId = (userId != null && userId > 0);
        boolean hasSessionId = (sessionId != null && !sessionId.isEmpty());

        if (!hasUserId && !hasSessionId) return list;

        String sql = "SELECT DISTINCT product_id, MAX(created_at) AS last_view "
                   + "FROM user_behavior_log "
                   + "WHERE " + (hasUserId ? "(user_id = ? OR session_id = ?) " : "session_id = ? ")
                   + "AND product_id IS NOT NULL AND event_type IN ('VIEW', 'CLICK') "
                   + "GROUP BY product_id "
                   + "ORDER BY last_view DESC "
                   + "LIMIT ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            if (hasUserId) {
                ps.setInt(idx++, userId);
                ps.setString(idx++, sessionId != null ? sessionId : "");
            } else {
                ps.setString(idx++, sessionId);
            }
            ps.setInt(idx++, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int pId = rs.getInt("product_id");
                    ProductObject p = productDAO.getProductById(pId);
                    if (p != null && !p.isDeleted()) {
                        list.add(new RecommendedProduct(p, 1.0, "Bạn đã xem gần đây", "HOME_RECENT"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Helper: get set of product IDs user has purchased
    private Set<Integer> getPurchasedProductIds(Integer userId) {
        Set<Integer> set = new HashSet<>();
        if (userId == null || userId <= 0) return set;

        String sql = "SELECT DISTINCT od.product_id "
                   + "FROM OrderDetail od "
                   + "JOIN `Order` o ON od.order_id = o.order_id "
                   + "WHERE o.user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    set.add(rs.getInt("product_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return set;
    }

    // Helper: get market-basket co-occurrence scores for anchor products
    private Map<Integer, Double> getCoOccurrenceScores(List<Integer> anchorProducts) {
        Map<Integer, Double> map = new HashMap<>();
        if (anchorProducts == null || anchorProducts.isEmpty()) return map;

        StringBuilder inClause = new StringBuilder();
        for (int i = 0; i < anchorProducts.size(); i++) {
            inClause.append("?");
            if (i < anchorProducts.size() - 1) inClause.append(",");
        }

        String sql = "SELECT od2.product_id AS cand_id, COUNT(DISTINCT od1.order_id) AS co_count "
                   + "FROM OrderDetail od1 "
                   + "JOIN OrderDetail od2 ON od1.order_id = od2.order_id "
                   + "WHERE od1.product_id IN (" + inClause + ") "
                   + "AND od2.product_id NOT IN (" + inClause + ") "
                   + "GROUP BY od2.product_id";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int idx = 1;
            for (Integer id : anchorProducts) {
                ps.setInt(idx++, id);
            }
            for (Integer id : anchorProducts) {
                ps.setInt(idx++, id);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    map.put(rs.getInt("cand_id"), rs.getDouble("co_count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }

    // Helper: get raw popularity scores (purchases * 5 + views)
    private Map<Integer, Double> getPopularityScores() {
        Map<Integer, Double> map = new HashMap<>();
        String sql = "SELECT p.id, "
                   + "(COALESCE(sales.purchases, 0) * 5 + COALESCE(views.view_count, 0)) AS pop_score "
                   + "FROM Product p "
                   + "LEFT JOIN ("
                   + "  SELECT od.product_id, SUM(od.quantity_sold) AS purchases "
                   + "  FROM OrderDetail od "
                   + "  JOIN `Order` o ON od.order_id = o.order_id "
                   + "  WHERE o.order_date >= NOW() - INTERVAL 30 DAY "
                   + "  GROUP BY od.product_id "
                   + ") sales ON p.id = sales.product_id "
                   + "LEFT JOIN ("
                   + "  SELECT product_id, COUNT(*) AS view_count "
                   + "  FROM user_behavior_log "
                   + "  WHERE event_type IN ('VIEW', 'CLICK') AND created_at >= NOW() - INTERVAL 30 DAY "
                   + "  GROUP BY product_id "
                   + ") views ON p.id = views.product_id "
                   + "WHERE p.isDeleted = FALSE";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                map.put(rs.getInt("id"), rs.getDouble("pop_score"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return map;
    }
}
