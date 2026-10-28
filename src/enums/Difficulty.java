package enums;

import util.GameConfig;

public enum Difficulty {
    KALMADO("Relaxed", "Longer deadlines, fewer calls", 1.3, 0.75, 0.8),
    NORMAL("Standard", "The intended dispatch challenge", 1.0, 1.0, 1.0),
    BAYANI("Heroic", "Tight deadlines, nonstop calls", 0.8, 1.25, 1.2);

    private final String label;
    private final String description;
    private final double deadlineMultiplier;
    private final double callFrequencyMultiplier;
    private final double panicMultiplier;

    Difficulty(String label, String description, double deadlineMultiplier, double callFrequencyMultiplier, double panicMultiplier) {
        this.label = label;
        this.description = description;
        this.deadlineMultiplier = deadlineMultiplier;
        this.callFrequencyMultiplier = callFrequencyMultiplier;
        this.panicMultiplier = panicMultiplier;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public double getDeadlineScale() {
        return deadlineMultiplier * GameConfig.DEADLINE_SCALE;
    }

    public double getCallFrequencyScale() {
        return callFrequencyMultiplier * GameConfig.CALL_FREQUENCY_SCALE;
    }

    public double getPanicScale() {
        return panicMultiplier * GameConfig.PANIC_SCALE;
    }

    public static Difficulty fromName(String name) {
        if (name == null) {
            return NORMAL;
        }
        for (Difficulty difficulty : values()) {
            if (difficulty.name().equalsIgnoreCase(name) || difficulty.label.equalsIgnoreCase(name)) {
                return difficulty;
            }
        }
        return NORMAL;
    }
}
