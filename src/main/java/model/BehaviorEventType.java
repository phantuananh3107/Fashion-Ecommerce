package model;

public enum BehaviorEventType {
    VIEW(1.0),
    CLICK(2.0),
    SEARCH(1.0),
    ADD_TO_CART(3.0),
    PURCHASE(5.0);

    private final double weight;

    BehaviorEventType(double weight) {
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }
}
