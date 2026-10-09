package recommend;

import model.ProductObject;

public class RecommendedProduct {
    private ProductObject product;
    private double score;
    private String reason;
    private String source;

    public RecommendedProduct() {
    }

    public RecommendedProduct(ProductObject product, double score, String reason, String source) {
        this.product = product;
        this.score = score;
        this.reason = reason;
        this.source = source;
    }

    public ProductObject getProduct() {
        return product;
    }

    public void setProduct(ProductObject product) {
        this.product = product;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "RecommendedProduct{" +
                "productId=" + (product != null ? product.getProductId() : null) +
                ", score=" + score +
                ", reason='" + reason + '\'' +
                ", source='" + source + '\'' +
                '}';
    }
}
