package enums;

public enum ResponderStatus {
    AVAILABLE("Available"),
    EN_ROUTE("En Route"),
    BUSY("On Scene"),
    RESTING("Resting");

    private final String label;

    ResponderStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
