package recommend;

import model.ProductObject;

import java.util.*;
import java.util.stream.Collectors;

public class RecommenderEngine {

    /**
     * Calculates time decay factor: exp(-ln(2) * numDays / halfLifeDays)
     */
    public static double calculateTimeDecay(double numDays, double halfLifeDays) {
        if (numDays < 0) numDays = 0;
        if (halfLifeDays <= 0) halfLifeDays = RecommendationConfig.HALF_LIFE_DAYS;
        return Math.exp(-Math.log(2.0) * numDays / halfLifeDays);
    }

    /**
     * Normalizes a map of product scores to [0.0, 1.0] scale relative to max value.
     */
    public static Map<Integer, Double> normalizeScores(Map<Integer, Double> rawScores) {
        Map<Integer, Double> normalized = new HashMap<>();
        if (rawScores == null || rawScores.isEmpty()) {
            return normalized;
        }

        double max = 0.0;
        for (double v : rawScores.values()) {
            if (v > max) max = v;
        }

        if (max <= 0) {
            for (Integer key : rawScores.keySet()) {
                normalized.put(key, 0.0);
            }
        } else {
            for (Map.Entry<Integer, Double> entry : rawScores.entrySet()) {
                normalized.put(entry.getKey(), Math.min(1.0, Math.max(0.0, entry.getValue() / max)));
            }
        }
        return normalized;
    }

    /**
     * Computes final hybrid score for candidate products.
     */
    public static double computeHybridScore(double contentScore, double basketScore, double popularityScore, boolean isColdStart) {
        if (isColdStart) {
            return popularityScore;
        }
        return RecommendationConfig.WEIGHT_CONTENT * contentScore +
               RecommendationConfig.WEIGHT_BASKET * basketScore +
               RecommendationConfig.WEIGHT_POPULARITY * popularityScore;
    }

    /**
     * Filters and diversifies recommendations capping max products per category.
     */
    public static List<RecommendedProduct> diversifyByCategory(
            List<RecommendedProduct> candidates,
            int maxPerCategory) {

        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }

        if (maxPerCategory <= 0) {
            maxPerCategory = RecommendationConfig.MAX_PER_CATEGORY;
        }

        // Sort candidates by score descending
        List<RecommendedProduct> sorted = new ArrayList<>(candidates);
        sorted.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        List<RecommendedProduct> result = new ArrayList<>();
        Map<Integer, Integer> categoryCounts = new HashMap<>();

        for (RecommendedProduct item : sorted) {
            if (item == null || item.getProduct() == null) continue;

            int catId = item.getProduct().getCategoryId();
            int currentCount = categoryCounts.getOrDefault(catId, 0);

            if (currentCount < maxPerCategory) {
                result.add(item);
                categoryCounts.put(catId, currentCount + 1);
            }
        }

        return result;
    }

    /**
     * Generates Vietnamese reason string based on constituent scores and category name.
     */
    public static String generateReason(double contentScore, double basketScore, double popularityScore, String categoryName) {
        if (basketScore >= 0.5 && basketScore >= contentScore) {
            return "Khách mua sản phẩm này cũng mua";
        }
        if (contentScore >= 0.3 && categoryName != null && !categoryName.isEmpty()) {
            return "Vì bạn quan tâm " + categoryName;
        }
        if (contentScore >= 0.3) {
            return "Gợi ý phù hợp với sở thích của bạn";
        }
        if (popularityScore >= 0.5) {
            return "Đang được quan tâm nhiều";
        }
        return "Sản phẩm nổi bật cho bạn";
    }
}
