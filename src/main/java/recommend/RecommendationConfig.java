package recommend;

public class RecommendationConfig {
    public static final double WEIGHT_CONTENT = 0.5;
    public static final double WEIGHT_BASKET = 0.3;
    public static final double WEIGHT_POPULARITY = 0.2;
    public static final double HALF_LIFE_DAYS = 14.0;
    public static final int LOOKBACK_DAYS = 30;
    public static final int MAX_PER_CATEGORY = 3;
}
