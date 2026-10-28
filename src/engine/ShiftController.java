package engine;

import interfaces.Updatable;
import util.GameConfig;

public class ShiftController implements Updatable {
    private int currentShift;
    private double elapsedSeconds;
    private final double shiftDurationSeconds;

    public ShiftController() {
        this(GameConfig.SHIFT_DURATION_SECONDS);
    }

    public ShiftController(double shiftDurationSeconds) {
        this.currentShift = 1;
        this.elapsedSeconds = 0.0;
        this.shiftDurationSeconds = Math.max(30.0, shiftDurationSeconds);
    }

    public void startShift(int shiftNumber) {
        this.currentShift = Math.max(1, Math.min(GameConfig.TOTAL_SHIFTS, shiftNumber));
        this.elapsedSeconds = 0.0;
    }

    @Override
    public void update(double delta) {
        if (elapsedSeconds < shiftDurationSeconds) {
            elapsedSeconds = Math.min(shiftDurationSeconds, elapsedSeconds + delta);
        }
    }

    public boolean isShiftFinished() {
        return elapsedSeconds >= shiftDurationSeconds;
    }

    public boolean hasNextShift() {
        return currentShift < GameConfig.TOTAL_SHIFTS;
    }

    public int getCurrentShift() {
        return currentShift;
    }

    public double getElapsedSeconds() {
        return elapsedSeconds;
    }

    public double getRemainingSeconds() {
        return Math.max(0.0, shiftDurationSeconds - elapsedSeconds);
    }

    public String getFormattedRemainingTime() {
        int sec = (int) Math.ceil(getRemainingSeconds());
        return String.format("%02d:%02d", sec / 60, sec % 60);
    }

    public String getShiftTitle() {
        if (currentShift == 1) {
            return "Shift 1: Mainshock Night";
        }
        if (currentShift == 2) {
            return "Shift 2: Aftershocks";
        }
        return "Shift 3: Recovery Day";
    }

    public String getShiftSubtitle() {
        if (currentShift == 1) {
            return "Steady calls — Prioritize emergencies, match responders, and calm callers.";
        }
        if (currentShift == 2) {
            return "Seismic tremors block district roads — Send Engineers to clear debris!";
        }
        return "High emergency volume — Rotate fatigued responders and stay composed!";
    }
}
