package enums;

public enum IncidentStatus {
    WAITING("Waiting", false),
    DISPATCHED("En Route", false),
    HANDLING("On Scene", false),
    RESOLVED("Resolved", true),
    LOST("Casualty", true),
    HANDED_OVER("Handed Over", true);

    private final String label;
    private final boolean closed;

    IncidentStatus(String label, boolean closed) {
        this.label = label;
        this.closed = closed;
    }

    public String getLabel() {
        return label;
    }

    public boolean isClosed() {
        return closed;
    }
}
