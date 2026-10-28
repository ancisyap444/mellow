package model;

import enums.Mood;
import util.GameConfig;

public abstract class Caller {
    private final String name;
    private final String roleLabel;
    private double panicLevel;
    private Mood mood;
    private int calmedCount;

    public Caller(String name, String roleLabel, double initialPanic) {
        this.name = (name == null || name.trim().isEmpty()) ? "Resident" : name.trim();
        this.roleLabel = (roleLabel == null || roleLabel.trim().isEmpty()) ? "Citizen" : roleLabel.trim();
        setPanicLevel(initialPanic);
        this.calmedCount = 0;
    }

    public Caller(String name, double initialPanic) {
        this(name, "Resident", initialPanic);
    }

    public String getName() {
        return name;
    }

    public String getRoleLabel() {
        return roleLabel;
    }

    public double getPanicLevel() {
        return panicLevel;
    }

    public void setPanicLevel(double panicLevel) {
        this.panicLevel = GameConfig.clamp(panicLevel, 0.0, GameConfig.MAX_PANIC);
        this.mood = Mood.fromPanic(this.panicLevel);
    }

    public void increasePanic(double amount) {
        setPanicLevel(this.panicLevel + amount);
    }

    public void reducePanic(double amount) {
        setPanicLevel(this.panicLevel - amount);
        this.calmedCount++;
    }

    public Mood getMood() {
        return mood;
    }

    public int getCalmedCount() {
        return calmedCount;
    }

    public String getDisplayedLocation(Zone zone, double operatorStress) {
        if (zone == null) {
            return "Unknown District";
        }
        if (panicLevel >= 82.0) {
            return "District " + zone.getId() + " (Unclear / Caller Panicking!)";
        }
        if (panicLevel >= 60.0) {
            return zone.getName() + " (Near " + zone.getLandmark().split(" ")[0] + "...)";
        }
        String full = zone.getName() + " - " + zone.getLandmark();
        if (operatorStress >= 78.0) {
            return garbleText(full, 0.22);
        }
        return full;
    }

    public String getDialogueLine(Incident incident, double operatorStress) {
        Zone zone = incident.getZone();
        String locationHint = panicLevel >= 75.0
                ? "somewhere around District " + zone.getId()
                : zone.getName() + " (" + zone.getLandmark() + ")";
        String raw = buildRawTranscript(incident.getType().getLocalLabel(), locationHint, panicLevel >= 65.0);
        if (operatorStress >= 62.0) {
            double garbleRatio = (operatorStress - 55.0) / 140.0;
            return garbleText(raw, garbleRatio);
        }
        return raw;
    }

    protected String garbleText(String input, double ratio) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && ((i * 17 + 5) % 100) < (int) (ratio * 100)) {
                sb.append((i % 2 == 0) ? '~' : '#');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public abstract double getPanicRiseRate();

    public abstract String buildRawTranscript(String localIncidentName, String locationHint, boolean highPanic);
}
