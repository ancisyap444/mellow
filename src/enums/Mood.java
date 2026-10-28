package enums;

public enum Mood {
    CALM("Calm", 0, 0.8),
    WORRIED("Anxious", 30, 1.0),
    PANICKED("Panicked", 55, 1.15),
    HYSTERICAL("Hysterical", 75, 1.3);

    private final String label;
    private final double threshold;
    private final double deadlineDrainMultiplier;

    Mood(String label, double threshold, double deadlineDrainMultiplier) {
        this.label = label;
        this.threshold = threshold;
        this.deadlineDrainMultiplier = deadlineDrainMultiplier;
    }

    public String getLabel() {
        return label;
    }

    public double getThreshold() {
        return threshold;
    }

    public double getDeadlineDrainMultiplier() {
        return deadlineDrainMultiplier;
    }

    public static Mood fromPanic(double panic) {
        Mood result = CALM;
        for (Mood mood : values()) {
            if (panic >= mood.threshold) {
                result = mood;
            }
        }
        return result;
    }
}
