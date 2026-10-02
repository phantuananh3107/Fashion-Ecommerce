package util;

import java.util.HashMap;
import java.util.Map;

/**
 * ProductSearchHelper - Hỗ trợ phân tích ngữ cảnh và tìm kiếm sản phẩm thông minh
 * Cung cấp mapping giữa ngữ cảnh sử dụng và đặc điểm sản phẩm phù hợp
 */
public class ProductSearchHelper {

    // Mapping: Context keywords → Product characteristics
    private static final Map<String, ContextProfile> CONTEXT_PROFILES = new HashMap<>();

    static {
        // Buổi tiệc tối / Sự kiện trang trọng
        CONTEXT_PROFILES.put("tiệc", new ContextProfile()
            .addKeywords("trang trọng", "formal", "công sở", "sang trọng", "lịch sự")
            .addCategories("váy", "áo công sở", "quần tây")
            .setPriority(100));

        CONTEXT_PROFILES.put("formal", new ContextProfile()
            .addKeywords("sang trọng", "chính thức", "buổi tiệc", "lịch sự")
            .addCategories("váy", "áo công sở", "quần tây")
            .setPriority(100));

        // Casual / Đi chơi hàng ngày
        CONTEXT_PROFILES.put("casual", new ContextProfile()
            .addKeywords("thoải mái", "năng động", "thường ngày", "dễ chịu")
            .addCategories("áo thun", "jean", "áo sơ mi")
            .setPriority(80));

        CONTEXT_PROFILES.put("đi chơi", new ContextProfile()
            .addKeywords("thoải mái", "casual", "năng động", "dễ chịu")
            .addCategories("áo thun", "jean", "váy ngắn")
            .setPriority(80));

        // Hẹn hò / Tình yêu
        CONTEXT_PROFILES.put("hẹn hò", new ContextProfile()
            .addKeywords("duyên dáng", "quyến rũ", "xinh xắn", "cổ điển", "nữ tính")
            .addCategories("váy", "áo nữ tính", "váy midi")
            .setPriority(90));

        CONTEXT_PROFILES.put("tình yêu", new ContextProfile()
            .addKeywords("duyên dáng", "quyến rũ", "xinh xắn", "nữ tính")
            .addCategories("váy", "áo nữ tính")
            .setPriority(90));

        // Du lịch
        CONTEXT_PROFILES.put("du lịch", new ContextProfile()
            .addKeywords("nhẹ", "thoải mái", "thực dụng", "dễ vệ sinh")
            .addCategories("áo thun", "quần jean", "váy nhẹ")
            .setPriority(85));

        CONTEXT_PROFILES.put("chuyến đi", new ContextProfile()
            .addKeywords("thoải mái", "nhẹ", "thực dụng")
            .addCategories("áo thun", "quần")
            .setPriority(85));

        // Công sở / Làm việc
        CONTEXT_PROFILES.put("công sở", new ContextProfile()
            .addKeywords("chuyên nghiệp", "lịch sự", "sang trọng", "trang trọng")
            .addCategories("áo công sở", "quần tây", "váy công sở")
            .setPriority(95));

        CONTEXT_PROFILES.put("làm việc", new ContextProfile()
            .addKeywords("chuyên nghiệp", "lịch sự")
            .addCategories("áo công sở", "quần tây")
            .setPriority(90));
    }

    /**
     * Lấy ngữ cảnh sử dụng từ câu hỏi
     */
    public static ContextProfile getContextProfile(String userMessage) {
        String messageLower = userMessage.toLowerCase();

        // Tìm context phù hợp nhất
        ContextProfile bestMatch = null;
        int highestScore = 0;

        for (Map.Entry<String, ContextProfile> entry : CONTEXT_PROFILES.entrySet()) {
            if (messageLower.contains(entry.getKey())) {
                if (entry.getValue().getPriority() > highestScore) {
                    highestScore = entry.getValue().getPriority();
                    bestMatch = entry.getValue();
                }
            }
        }

        return bestMatch;
    }

    /**
     * Inner class: Đại diện cho ngữ cảnh sử dụng sản phẩm
     */
    public static class ContextProfile {
        private String[] keywords;
        private String[] categories;
        private int priority;

        public ContextProfile addKeywords(String... keywords) {
            this.keywords = keywords;
            return this;
        }

        public ContextProfile addCategories(String... categories) {
            this.categories = categories;
            return this;
        }

        public ContextProfile setPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public String[] getKeywords() {
            return keywords;
        }

        public String[] getCategories() {
            return categories;
        }

        public int getPriority() {
            return priority;
        }

        // Kiểm tra xem sản phẩm có phù hợp với context không
        public boolean matchesProduct(String productName, String description, String category) {
            String allText = (productName + " " + description + " " + category).toLowerCase();

            // Kiểm tra keywords
            if (keywords != null) {
                for (String keyword : keywords) {
                    if (allText.contains(keyword)) {
                        return true;
                    }
                }
            }

            // Kiểm tra category
            if (categories != null && category != null) {
                String categoryLower = category.toLowerCase();
                for (String cat : categories) {
                    if (categoryLower.contains(cat)) {
                        return true;
                    }
                }
            }

            return false;
        }
    }
}

