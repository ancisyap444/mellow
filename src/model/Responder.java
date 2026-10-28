package model;

import enums.ResponderStatus;
import enums.ResponderType;
import interfaces.Dispatchable;
import util.GameConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Responder implements Dispatchable {
    private static int nextId = 1;

    private final int id;
    private final String name;
    private final String callsign;
    private final ResponderType type;
    private ResponderStatus status;
    private double fatigue;
    private final Zone homeZone;
    private Zone currentZone;
    private Zone targetZone;
    private List<Zone> currentRoute;
    private double totalTravelTime;
    private double elapsedTravelTime;
    private Incident assignedIncident;
    private final double travelSpeedFactor;

    public Responder(String name, String callsign, ResponderType type, Zone homeZone, double travelSpeedFactor) {
        if (type == null || homeZone == null) {
            throw new IllegalArgumentException("Responder type and homeZone cannot be null");
        }
        this.id = nextId++;
        this.name = (name == null || name.trim().isEmpty()) ? type.getLabel() : name.trim();
        this.callsign = (callsign == null || callsign.trim().isEmpty()) ? type.getCode() + "-" + id : callsign.trim();
        this.type = type;
        this.status = ResponderStatus.AVAILABLE;
        this.fatigue = 0.0;
        this.homeZone = homeZone;
        this.currentZone = homeZone;
        this.targetZone = homeZone;
        this.currentRoute = new ArrayList<>();
        this.totalTravelTime = 0.0;
        this.elapsedTravelTime = 0.0;
        this.assignedIncident = null;
        this.travelSpeedFactor = Math.max(0.5, travelSpeedFactor);
    }

    public Responder(String name, ResponderType type, Zone homeZone) {
        this(name, type.getCode() + "-" + nextId, type, homeZone, 1.0);
    }

    public static void resetIdCounter() {
        nextId = 1;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCallsign() {
        return callsign;
    }

    public ResponderType getType() {
        return type;
    }

    public ResponderStatus getStatus() {
        return status;
    }

    public void setStatus(ResponderStatus status) {
        if (status != null) {
            this.status = status;
        }
    }

    public double getFatigue() {
        return fatigue;
    }

    public void setFatigue(double fatigue) {
        this.fatigue = GameConfig.clamp(fatigue, 0.0, GameConfig.MAX_FATIGUE);
    }

    public void addFatigue(double amount) {
        setFatigue(this.fatigue + amount);
    }

    public double getFatiguePerformanceMultiplier() {
        if (fatigue >= 75.0) {
            return 0.62;
        }
        if (fatigue >= 45.0) {
            return 0.82;
        }
        return 1.0;
    }

    public Zone getHomeZone() {
        return homeZone;
    }

    public Zone getCurrentZone() {
        return currentZone;
    }

    public void setCurrentZone(Zone currentZone) {
        if (currentZone != null) {
            this.currentZone = currentZone;
        }
    }

    public Zone getTargetZone() {
        return targetZone;
    }

    public List<Zone> getCurrentRoute() {
        return Collections.unmodifiableList(currentRoute);
    }

    public double getTotalTravelTime() {
        return totalTravelTime;
    }

    public double getRemainingTravelTime() {
        return Math.max(0.0, totalTravelTime - elapsedTravelTime);
    }

    public Incident getAssignedIncident() {
        return assignedIncident;
    }

    public double getTravelSpeedFactor() {
        return travelSpeedFactor * getFatiguePerformanceMultiplier();
    }

    public boolean toggleManualRest() {
        if (status == ResponderStatus.AVAILABLE && fatigue > 8.0) {
            status = ResponderStatus.RESTING;
            return true;
        }
        if (status == ResponderStatus.RESTING && fatigue < GameConfig.FATIGUE_REST_THRESHOLD) {
            status = ResponderStatus.AVAILABLE;
            return true;
        }
        return false;
    }

    @Override
    public boolean canRespond() {
        return status == ResponderStatus.AVAILABLE && fatigue < GameConfig.FATIGUE_REST_THRESHOLD;
    }

    @Override
    public void assignToIncident(Incident incident, List<Zone> route, double travelSeconds) {
        if (incident == null) {
            throw new IllegalArgumentException("Incident cannot be null");
        }
        this.assignedIncident = incident;
        this.targetZone = incident.getZone();
        this.currentRoute = route == null ? new ArrayList<>() : new ArrayList<>(route);
        this.totalTravelTime = Math.max(0.1, travelSeconds);
        this.elapsedTravelTime = 0.0;
        if (currentZone.equals(targetZone) || travelSeconds <= 0.25) {
            this.status = ResponderStatus.BUSY;
            this.currentZone = targetZone;
            incident.setStatus(enums.IncidentStatus.HANDLING);
        } else {
            this.status = ResponderStatus.EN_ROUTE;
        }
    }

    @Override
    public void completeAssignment() {
        this.assignedIncident = null;
        this.currentRoute.clear();
        this.totalTravelTime = 0.0;
        this.elapsedTravelTime = 0.0;
        if (this.fatigue >= GameConfig.FATIGUE_REST_THRESHOLD) {
            this.status = ResponderStatus.RESTING;
        } else {
            this.status = ResponderStatus.AVAILABLE;
        }
    }

    public void updateState(double delta) {
        if (status == ResponderStatus.EN_ROUTE) {
            elapsedTravelTime += delta;
            if (currentRoute.size() >= 2 && totalTravelTime > 0.0) {
                double ratio = GameConfig.clamp(elapsedTravelTime / totalTravelTime, 0.0, 1.0);
                int segCount = currentRoute.size() - 1;
                int idx = Math.min(segCount - 1, (int) (ratio * segCount));
                this.currentZone = currentRoute.get(idx);
            }
            addFatigue(delta * 0.45);
            if (elapsedTravelTime >= totalTravelTime) {
                this.currentZone = targetZone;
                this.status = ResponderStatus.BUSY;
                if (assignedIncident != null && !assignedIncident.getStatus().isClosed()) {
                    assignedIncident.setStatus(enums.IncidentStatus.HANDLING);
                }
            }
        } else if (status == ResponderStatus.BUSY) {
            addFatigue(delta * 1.45);
        } else if (status == ResponderStatus.RESTING) {
            setFatigue(fatigue - GameConfig.FATIGUE_RECOVERY_PER_SECOND * delta);
            if (fatigue <= 18.0) {
                status = ResponderStatus.AVAILABLE;
            }
        } else if (status == ResponderStatus.AVAILABLE) {
            setFatigue(fatigue - (GameConfig.FATIGUE_RECOVERY_PER_SECOND * 0.55) * delta);
        }
    }

    public double getMapNormX() {
        if (status == ResponderStatus.EN_ROUTE && currentRoute.size() >= 2 && totalTravelTime > 0.0) {
            double ratio = GameConfig.clamp(elapsedTravelTime / totalTravelTime, 0.0, 1.0);
            int segments = currentRoute.size() - 1;
            double scaled = ratio * segments;
            int index = Math.min(segments - 1, (int) Math.floor(scaled));
            double localT = scaled - index;
            Zone a = currentRoute.get(index);
            Zone b = currentRoute.get(index + 1);
            return a.getNormX() + (b.getNormX() - a.getNormX()) * localT;
        }
        return currentZone.getNormX();
    }

    public double getMapNormY() {
        if (status == ResponderStatus.EN_ROUTE && currentRoute.size() >= 2 && totalTravelTime > 0.0) {
            double ratio = GameConfig.clamp(elapsedTravelTime / totalTravelTime, 0.0, 1.0);
            int segments = currentRoute.size() - 1;
            double scaled = ratio * segments;
            int index = Math.min(segments - 1, (int) Math.floor(scaled));
            double localT = scaled - index;
            Zone a = currentRoute.get(index);
            Zone b = currentRoute.get(index + 1);
            return a.getNormY() + (b.getNormY() - a.getNormY()) * localT;
        }
        return currentZone.getNormY();
    }

    public abstract double calculateEffectiveness(Incident incident);

    public abstract double getHandlingTime(Incident incident);
}
