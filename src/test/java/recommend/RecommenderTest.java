package recommend;

import model.ProductObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RecommenderTest {

    @Test
    @DisplayName("Test exponential time decay with half-life of 14 days")
    public void testTimeDecay() {
        double decayDay0 = RecommenderEngine.calculateTimeDecay(0, 14);
        assertEquals(1.0, decayDay0, 0.0001, "Decay on day 0 should be 1.0");

        double decayDay14 = RecommenderEngine.calculateTimeDecay(14, 14);
        assertEquals(0.5, decayDay14, 0.0001, "Decay on day 14 (half-life) should be 0.5");

        double decayDay28 = RecommenderEngine.calculateTimeDecay(28, 14);
        assertEquals(0.25, decayDay28, 0.0001, "Decay on day 28 should be 0.25");
    }

    @Test
    @DisplayName("Test score normalization to [0.0, 1.0]")
    public void testScoreNormalization() {
        Map<Integer, Double> rawMap = new HashMap<>();
        rawMap.put(1, 10.0);
        rawMap.put(2, 20.0);
        rawMap.put(3, 5.0);

        Map<Integer, Double> normalized = RecommenderEngine.normalizeScores(rawMap);

        assertEquals(0.5, normalized.get(1), 0.0001);
        assertEquals(1.0, normalized.get(2), 0.0001);
        assertEquals(0.25, normalized.get(3), 0.0001);
    }

    @Test
    @DisplayName("Test hybrid scoring and cold-start fallback")
    public void testHybridScoringAndColdStart() {
        double content = 0.8;
        double basket = 0.6;
        double popularity = 0.4;

        // Normal user hybrid scoring: 0.5*0.8 + 0.3*0.6 + 0.2*0.4 = 0.40 + 0.18 + 0.08 = 0.66
        double normalScore = RecommenderEngine.computeHybridScore(content, basket, popularity, false);
        assertEquals(0.66, normalScore, 0.0001);

        // Cold start fallback: uses popularity score only
        double coldStartScore = RecommenderEngine.computeHybridScore(content, basket, popularity, true);
        assertEquals(popularity, coldStartScore, 0.0001);
    }

    @Test
    @DisplayName("Test category diversification capping max N items per category")
    public void testCategoryDiversification() {
        List<RecommendedProduct> candidates = new ArrayList<>();

        // Category 1 products (3 products)
        candidates.add(createDummyRecProduct(101, 1, 0.95));
        candidates.add(createDummyRecProduct(102, 1, 0.90));
        candidates.add(createDummyRecProduct(103, 1, 0.85));

        // Category 2 products (2 products)
        candidates.add(createDummyRecProduct(201, 2, 0.80));
        candidates.add(createDummyRecProduct(202, 2, 0.75));

        // Apply diversification with max 2 per category
        List<RecommendedProduct> diversified = RecommenderEngine.diversifyByCategory(candidates, 2);

        assertEquals(4, diversified.size(), "Should contain 2 from cat 1 and 2 from cat 2");

        int countCat1 = 0;
        int countCat2 = 0;
        for (RecommendedProduct rec : diversified) {
            if (rec.getProduct().getCategoryId() == 1) countCat1++;
            if (rec.getProduct().getCategoryId() == 2) countCat2++;
        }

        assertEquals(2, countCat1, "Cat 1 should be capped at 2");
        assertEquals(2, countCat2, "Cat 2 should have 2");
    }

    @Test
    @DisplayName("Test Vietnamese reason string generation")
    public void testReasonGeneration() {
        String reasonBasket = RecommenderEngine.generateReason(0.2, 0.8, 0.3, "Áo sơ mi");
        assertEquals("Khách mua sản phẩm này cũng mua", reasonBasket);

        String reasonContent = RecommenderEngine.generateReason(0.7, 0.2, 0.3, "Áo sơ mi");
        assertEquals("Vì bạn quan tâm Áo sơ mi", reasonContent);

        String reasonPopularity = RecommenderEngine.generateReason(0.1, 0.1, 0.8, "Áo sơ mi");
        assertEquals("Đang được quan tâm nhiều", reasonPopularity);
    }

    private RecommendedProduct createDummyRecProduct(int productId, int categoryId, double score) {
        ProductObject p = new ProductObject();
        p.setProductId(productId);
        p.setCategoryId(categoryId);
        p.setProductName("Test Product " + productId);
        return new RecommendedProduct(p, score, "Test Reason", "TEST_SOURCE");
    }
}
