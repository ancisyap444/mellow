package model;

import interfaces.Updatable;
import util.GameConfig;

public class Operator implements Updatable {
    private final String name;
    private final StressMeter stressMeter;
    private double radioBusyRemaining;
    private String radioActionText;
    private boolean breathingActive;
    private double breathingTimeRemaining;
    private double breathingCycleElapsed;
    private boolean inhalingInput;
    private int calmActionsUsed;
    private int breathingPausesCompleted;

    public Operator(String name) {
        this(name, 12.0);
    }

    public Operator(String name, double initialStress) {
        this.name = (name == null || name.trim().isEmpty()) ? "Operator Taylor" : name.trim();
        this.stressMeter = new StressMeter(initialStress);
        this.radioBusyRemaining = 0.0;
        this.radioActionText = "Standby";
        this.breathingActive = false;
        this.breathingTimeRemaining = 0.0;
        this.breathingCycleElapsed = 0.0;
        this.inhalingInput = false;
        this.calmActionsUsed = 0;
        this.breathingPausesCompleted = 0;
    }

    @Override
    public void update(double delta) {
        if (radioBusyRemaining > 0.0) {
            radioBusyRemaining = Math.max(0.0, radioBusyRemaining - delta);
            if (radioBusyRemaining == 0.0) {
                radioActionText = "Standby";
            }
        }

        if (breathingActive) {
            breathingTimeRemaining -= delta;
            breathingCycleElapsed = (breathingCycleElapsed + delta) % GameConfig.BREATHING_CYCLE_SECONDS;
            boolean synced = isBreathingSynced();
            double reliefRate = (GameConfig.BREATHING_MAX_STRESS_RELIEF / GameConfig.BREATHING_DURATION)
                    * (synced ? 1.25 : 0.55);
            stressMeter.reduceStress(reliefRate * delta);

            if (breathingTimeRemaining <= 0.0) {
                breathingTimeRemaining = 0.0;
                breathingActive = false;
                inhalingInput = false;
                breathingPausesCompleted++;
            }
        }
    }

    public boolean calmCaller(Incident incident) {
        return calmCaller(incident, GameConfig.CALM_PANIC_REDUCTION);
    }

    public boolean calmCaller(Incident incident, double panicReduction) {
        if (incident == null || incident.getStatus().isClosed() || isRadioBusy()) {
            return false;
        }
        incident.getCaller().reducePanic(panicReduction);
        incident.setRemainingDeadline(Math.min(
                incident.getInitialDeadline() * 1.25,
                incident.getRemainingDeadline() + GameConfig.CALM_DEADLINE_BONUS));
        this.radioBusyRemaining = GameConfig.CALM_CALLER_DURATION * stressMeter.getActionDelayMultiplier();
        this.radioActionText = "Calming " + incident.getCaller().getName();
        this.stressMeter.reduceStress(3.0);
        this.calmActionsUsed++;
        return true;
    }

    public boolean startBreathingPause() {
        if (breathingActive) {
            return false;
        }
        this.breathingActive = true;
        this.breathingTimeRemaining = GameConfig.BREATHING_DURATION;
        this.breathingCycleElapsed = 0.0;
        this.inhalingInput = false;
        return true;
    }

    public void cancelBreathingPause() {
        this.breathingActive = false;
        this.breathingTimeRemaining = 0.0;
        this.inhalingInput = false;
    }

    public boolean isInhaleTargetPhase() {
        return breathingCycleElapsed < (GameConfig.BREATHING_CYCLE_SECONDS * 0.5);
    }

    public double getBreathingPhaseProgress() {
        double half = GameConfig.BREATHING_CYCLE_SECONDS * 0.5;
        if (breathingCycleElapsed < half) {
            return breathingCycleElapsed / half;
        }
        return 1.0 - ((breathingCycleElapsed - half) / half);
    }

    public boolean isBreathingSynced() {
        return inhalingInput == isInhaleTargetPhase();
    }

    public String getName() {
        return name;
    }

    public StressMeter getStressMeter() {
        return stressMeter;
    }

    public double getStress() {
        return stressMeter.getCurrentStress();
    }

    public void addStress(double amount) {
        stressMeter.addStress(amount);
    }

    public void reduceStress(double amount) {
        stressMeter.reduceStress(amount);
    }

    public boolean isRadioBusy() {
        return radioBusyRemaining > 0.0;
    }

    public double getRadioBusyRemaining() {
        return radioBusyRemaining;
    }

    public String getRadioActionText() {
        return radioActionText;
    }

    public boolean isBreathingActive() {
        return breathingActive;
    }

    public double getBreathingTimeRemaining() {
        return breathingTimeRemaining;
    }

    public boolean isInhalingInput() {
        return inhalingInput;
    }

    public void setInhalingInput(boolean inhalingInput) {
        this.inhalingInput = inhalingInput;
    }

    public int getCalmActionsUsed() {
        return calmActionsUsed;
    }

    public int getBreathingPausesCompleted() {
        return breathingPausesCompleted;
    }
}
