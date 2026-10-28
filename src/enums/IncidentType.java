package enums;

public enum IncidentType {
    COLLAPSED_BUILDING("Collapsed Building", "Structural Collapse", 64, 13),
    FIRE("Structure Fire", "Active Blaze", 54, 11),
    MEDICAL_EMERGENCY("Medical Emergency", "Critical Injury", 58, 8),
    BLOCKED_ROAD("Blocked Road", "Debris Roadblock", 80, 10),
    GAS_LEAK("Gas Leak", "Ruptured Gas Line", 48, 9);

    private final String label;
    private final String localLabel;
    private final double baseDeadlineSeconds;
    private final double baseHandlingSeconds;

    IncidentType(String label, String localLabel, double baseDeadlineSeconds, double baseHandlingSeconds) {
        this.label = label;
        this.localLabel = localLabel;
        this.baseDeadlineSeconds = baseDeadlineSeconds;
        this.baseHandlingSeconds = baseHandlingSeconds;
    }

    public String getLabel() {
        return label;
    }

    public String getLocalLabel() {
        return localLabel;
    }

    public double getBaseDeadlineSeconds() {
        return baseDeadlineSeconds;
    }

    public double getBaseHandlingSeconds() {
        return baseHandlingSeconds;
    }
}
