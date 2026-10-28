package model;

import util.GameConfig;

public class StressMeter {
    private double currentStress;
    private double peakStress;

    public StressMeter() {
        this(12.0);
    }

    public StressMeter(double initialStress) {
        setCurrentStress(initialStress);
        this.peakStress = this.currentStress;
    }

    public double getCurrentStress() {
        return currentStress;
    }

    public void setCurrentStress(double stress) {
        this.currentStress = GameConfig.clamp(stress, 0.0, GameConfig.MAX_STRESS);
        if (this.currentStress > this.peakStress) {
            this.peakStress = this.currentStress;
        }
    }

    public void addStress(double delta) {
        setCurrentStress(this.currentStress + delta);
    }

    public void reduceStress(double delta) {
        setCurrentStress(this.currentStress - delta);
    }

    public double getPeakStress() {
        return peakStress;
    }

    public double getActionDelayMultiplier() {
        if (currentStress >= 80.0) {
            return 1.32;
        }
        if (currentStress >= 55.0) {
            return 1.15;
        }
        return 1.0;
    }

    public double getStaticNoiseLevel() {
        return GameConfig.clamp((currentStress - 25.0) / 75.0, 0.0, 1.0);
    }

    public String getStatusLabel() {
        if (currentStress < 30.0) {
            return "FOCUSED (Steady)";
        }
        if (currentStress < 60.0) {
            return "TENSE (Elevated)";
        }
        if (currentStress < 82.0) {
            return "STRESSED (High Load)";
        }
        return "OVERWHELMED (Take a Breath!)";
    }
}
