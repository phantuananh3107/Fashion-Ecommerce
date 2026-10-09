package recommend;

import model.ProductObject;

import java.util.*;

public class RecommenderTestRunner {

    public static void main(String[] args) {
        System.out.println("=== Running Pure Recommendation Logic Unit Tests ===");

        testTimeDecay();
        testScoreNormalization();
        testHybridScoringAndColdStart();
        testCategoryDiversification();
        testReasonGeneration();

        System.out.println("=== ALL RECOMMENDATION UNIT TESTS PASSED SUCCESSFULLY! ===");
    }

    private static void testTimeDecay() {
        double d0 = RecommenderEngine.calculateTimeDecay(0, 14);
        assertApprox(1.0, d0, "Day 0 decay");

        double d14 = RecommenderEngine.calculateTimeDecay(14, 14);
        assertApprox(0.5, d14, "Day 14 decay (half-life)");

        double d28 = RecommenderEngine.calculateTimeDecay(28, 14);
        assertApprox(0.25, d28, "Day 28 decay");
        System.out.println("[PASS] Time Decay calculation");
    }

    private static void testScoreNormalization() {
        Map<Integer, Double> raw = new HashMap<>();
        raw.put(1, 10.0);
        raw.put(2, 20.0);
        raw.put(3, 5.0);

        Map<Integer, Double> norm = RecommenderEngine.normalizeScores(raw);
        assertApprox(0.5, norm.get(1), "Product 1 norm");
        assertApprox(1.0, norm.get(2), "Product 2 norm");
        assertApprox(0.25, norm.get(3), "Product 3 norm");
        System.out.println("[PASS] Score Normalization");
    }

    private static void testHybridScoringAndColdStart() {
        double content = 0.8;
        double basket = 0.6;
        double popularity = 0.4;

        double normal = RecommenderEngine.computeHybridScore(content, basket, popularity, false);
        assertApprox(0.66, normal, "Normal hybrid score (0.5*0.8 + 0.3*0.6 + 0.2*0.4)");

        double cold = RecommenderEngine.computeHybridScore(content, basket, popularity, true);
        assertApprox(popularity, cold, "Cold-start fallback score");
        System.out.println("[PASS] Hybrid Scoring and Cold-Start Fallback");
    }

    private static void testCategoryDiversification() {
        List<RecommendedProduct> list = new ArrayList<>();
        list.add(createRec(101, 1, 0.95));
        list.add(createRec(102, 1, 0.90));
        list.add(createRec(103, 1, 0.85));
        list.add(createRec(201, 2, 0.80));
        list.add(createRec(202, 2, 0.75));

        List<RecommendedProduct> diversified = RecommenderEngine.diversifyByCategory(list, 2);
        if (diversified.size() != 4) {
            throw new RuntimeException("Expected 4 items after category capping, got: " + diversified.size());
        }
        System.out.println("[PASS] Category Diversification (capped at 2/category)");
    }

    private static void testReasonGeneration() {
        String r1 = RecommenderEngine.generateReason(0.2, 0.8, 0.3, "Áo sơ mi");
        assertEquals("Khách mua sản phẩm này cũng mua", r1, "Reason basket");

        String r2 = RecommenderEngine.generateReason(0.7, 0.2, 0.3, "Áo sơ mi");
        assertEquals("Vì bạn quan tâm Áo sơ mi", r2, "Reason content");

        String r3 = RecommenderEngine.generateReason(0.1, 0.1, 0.8, "Áo sơ mi");
        assertEquals("Đang được quan tâm nhiều", r3, "Reason popularity");

        System.out.println("[PASS] Vietnamese Reason Generation");
    }

    private static RecommendedProduct createRec(int id, int catId, double score) {
        ProductObject p = new ProductObject();
        p.setProductId(id);
        p.setCategoryId(catId);
        p.setProductName("Product " + id);
        return new RecommendedProduct(p, score, "Reason", "SOURCE");
    }

    private static void assertApprox(double expected, double actual, String msg) {
        if (Math.abs(expected - actual) > 0.0001) {
            throw new RuntimeException("Assertion failed [" + msg + "]: expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(String expected, String actual, String msg) {
        if (!Objects.equals(expected, actual)) {
            throw new RuntimeException("Assertion failed [" + msg + "]: expected '" + expected + "' but got '" + actual + "'");
        }
    }
}
