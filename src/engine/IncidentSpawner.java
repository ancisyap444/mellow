package engine;

import enums.Difficulty;
import factory.IncidentFactory;
import model.Barangay;
import model.Incident;
import util.GameConfig;

public class IncidentSpawner {
    private final IncidentFactory incidentFactory;
    private double spawnTimer;

    public IncidentSpawner(IncidentFactory incidentFactory) {
        if (incidentFactory == null) {
            throw new IllegalArgumentException("IncidentFactory cannot be null");
        }
        this.incidentFactory = incidentFactory;
        this.spawnTimer = 8.0;
    }

    public void resetForShift(int shiftNumber) {
        this.spawnTimer = Math.max(5.0, getBaseInterval(shiftNumber) * 0.55);
    }

    public Incident updateAndTrySpawn(double delta, Barangay barangay, int shiftNumber, int activeCount, Difficulty difficulty) {
        spawnTimer -= delta;
        if (spawnTimer > 0.0) {
            return null;
        }
        double freqScale = difficulty != null ? difficulty.getCallFrequencyScale() : 1.0;
        spawnTimer = getBaseInterval(shiftNumber) / Math.max(0.5, freqScale);

        int maxCap = getMaxSimultaneous(shiftNumber);
        if (activeCount >= maxCap) {
            return null;
        }
        double deadlineScale = difficulty != null ? difficulty.getDeadlineScale() : 1.0;
        double panicScale = difficulty != null ? difficulty.getPanicScale() : 1.0;
        return incidentFactory.createShiftIncident(barangay, shiftNumber, deadlineScale, panicScale);
    }

    private double getBaseInterval(int shiftNumber) {
        if (shiftNumber == 1) {
            return GameConfig.BASE_SPAWN_INTERVAL_SHIFT_1;
        }
        if (shiftNumber == 2) {
            return GameConfig.BASE_SPAWN_INTERVAL_SHIFT_2;
        }
        return GameConfig.BASE_SPAWN_INTERVAL_SHIFT_3;
    }

    private int getMaxSimultaneous(int shiftNumber) {
        if (shiftNumber == 1) {
            return GameConfig.MAX_SIMULTANEOUS_SHIFT_1;
        }
        if (shiftNumber == 2) {
            return GameConfig.MAX_SIMULTANEOUS_SHIFT_2;
        }
        return GameConfig.MAX_SIMULTANEOUS_SHIFT_3;
    }
}
