package model;

import enums.IncidentStatus;
import enums.IncidentType;
import enums.ResponderStatus;
import enums.ResponderType;
import enums.Severity;
import interfaces.Prioritizable;
import util.GameConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Incident implements Prioritizable {
    private static int nextId = 101;

    private final int id;
    private final IncidentType type;
    private final Zone zone;
    private Severity severity;
    private final double initialDeadline;
    private double remainingDeadline;
    private final Caller caller;
    private IncidentStatus status;
    private final List<ResponderType> requiredResponderTypes;
    private final List<Responder> assignedResponders;
    private double handlingProgress;
    private final double totalHandlingRequired;
    private double waitTime;
    private double escalationTimer;
    private int livesAtRisk;
    private String outcomeNote;

    public Incident(IncidentType type, Zone zone, Severity severity, double deadlineSeconds, Caller caller,
                    List<ResponderType> requiredResponderTypes, int livesAtRisk) {
        if (type == null || zone == null || severity == null || caller == null) {
            throw new IllegalArgumentException("Incident parameters cannot be null");
        }
        this.id = nextId++;
        this.type = type;
        this.zone = zone;
        this.severity = severity;
        this.initialDeadline = Math.max(18.0, deadlineSeconds);
        this.remainingDeadline = this.initialDeadline;
        this.caller = caller;
        this.status = IncidentStatus.WAITING;
        this.requiredResponderTypes = new ArrayList<>();
        if (requiredResponderTypes != null) {
            this.requiredResponderTypes.addAll(requiredResponderTypes);
        }
        this.assignedResponders = new ArrayList<>();
        this.handlingProgress = 0.0;
        this.totalHandlingRequired = type.getBaseHandlingSeconds();
        this.waitTime = 0.0;
        this.escalationTimer = 0.0;
        this.livesAtRisk = Math.max(1, livesAtRisk);
        this.outcomeNote = "Pending";
    }

    public Incident(IncidentType type, Zone zone, Severity severity, Caller caller) {
        this(type, zone, severity, type.getBaseDeadlineSeconds(), caller, new ArrayList<>(), 2);
    }

    public static void resetIdCounter() {
        nextId = 101;
    }

    public int getId() {
        return id;
    }

    public IncidentType getType() {
        return type;
    }

    public Zone getZone() {
        return zone;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        if (severity != null) {
            this.severity = severity;
        }
    }

    public double getInitialDeadline() {
        return initialDeadline;
    }

    public double getRemainingDeadline() {
        return remainingDeadline;
    }

    public void setRemainingDeadline(double remainingDeadline) {
        this.remainingDeadline = GameConfig.clamp(remainingDeadline, 0.0, initialDeadline * 1.4);
    }

    public double getDeadlineFraction() {
        return GameConfig.clamp(remainingDeadline / initialDeadline, 0.0, 1.0);
    }

    public Caller getCaller() {
        return caller;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        if (status != null) {
            this.status = status;
        }
    }

    public List<ResponderType> getRequiredResponderTypes() {
        return Collections.unmodifiableList(requiredResponderTypes);
    }

    public List<Responder> getAssignedResponders() {
        return Collections.unmodifiableList(assignedResponders);
    }

    public List<ResponderType> getMissingResponderTypes() {
        List<ResponderType> missing = new ArrayList<>(requiredResponderTypes);
        for (Responder r : assignedResponders) {
            missing.remove(r.getType());
        }
        return missing;
    }

    public boolean canAcceptResponder(Responder responder) {
        if (responder == null || assignedResponders.contains(responder)) {
            return false;
        }
        return assignedResponders.size() < requiredResponderTypes.size() + 1;
    }

    public void assignResponder(Responder responder) {
        if (responder != null && !assignedResponders.contains(responder)) {
            assignedResponders.add(responder);
            if (status == IncidentStatus.WAITING) {
                status = IncidentStatus.DISPATCHED;
            }
        }
    }

    public boolean hasResponderOnScene() {
        for (Responder r : assignedResponders) {
            if (r.getStatus() == ResponderStatus.BUSY) {
                return true;
            }
        }
        return false;
    }

    public boolean isFullyStaffed() {
        return getMissingResponderTypes().isEmpty();
    }

    public void addHandlingProgress(double amount) {
        this.handlingProgress = GameConfig.clamp(this.handlingProgress + amount, 0.0, totalHandlingRequired);
    }

    public double getHandlingProgress() {
        return handlingProgress;
    }

    public double getHandlingProgressFraction() {
        return GameConfig.clamp(handlingProgress / totalHandlingRequired, 0.0, 1.0);
    }

    public boolean isHandlingComplete() {
        return handlingProgress >= totalHandlingRequired;
    }

    public void addWaitTime(double delta) {
        this.waitTime += delta;
        this.escalationTimer += delta;
    }

    public double getWaitTime() {
        return waitTime;
    }

    public double getEscalationTimer() {
        return escalationTimer;
    }

    public void resetEscalationTimer() {
        this.escalationTimer = 0.0;
    }

    public int getLivesAtRisk() {
        return livesAtRisk;
    }

    public void setLivesAtRisk(int livesAtRisk) {
        this.livesAtRisk = Math.max(1, livesAtRisk);
    }

    public String getOutcomeNote() {
        return outcomeNote;
    }

    public void setOutcomeNote(String outcomeNote) {
        this.outcomeNote = outcomeNote == null ? "" : outcomeNote;
    }

    public void onResolved() {
    }

    public abstract void escalate();
}
