package enums;

public enum Severity {
    LOW("Low", 1, 1.0),
    MODERATE("Moderate", 2, 0.95),
    HIGH("High", 3, 0.85),
    CRITICAL("Critical", 4, 0.7);

    private final String label;
    private final int weight;
    private final double survivalFactor;

    Severity(String label, int weight, double survivalFactor) {
        this.label = label;
        this.weight = weight;
        this.survivalFactor = survivalFactor;
    }

    public String getLabel() {
        return label;
    }

    public int getWeight() {
        return weight;
    }

    public double getSurvivalFactor() {
        return survivalFactor;
    }

    public boolean isCritical() {
        return this == CRITICAL;
    }

    public Severity next() {
        return this == CRITICAL ? CRITICAL : values()[ordinal() + 1];
    }
}
