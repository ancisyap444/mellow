package model;

import enums.IncidentType;
import enums.ResponderType;

public class Rescuer extends Responder {
    public Rescuer(String name, String callsign, Zone homeZone) {
        super(name, callsign, ResponderType.RESCUER, homeZone, 1.05);
    }

    public Rescuer(String name, Zone homeZone) {
        super(name, ResponderType.RESCUER, homeZone);
    }

    @Override
    public double calculateEffectiveness(Incident incident) {
        double base;
        if (incident.getType() == IncidentType.COLLAPSED_BUILDING) {
            base = 1.45;
        } else if (incident.getType() == IncidentType.BLOCKED_ROAD) {
            base = 0.85;
        } else {
            base = 0.65;
        }
        return base * getFatiguePerformanceMultiplier();
    }

    @Override
    public double getHandlingTime(Incident incident) {
        return incident.getType().getBaseHandlingSeconds() / Math.max(0.35, calculateEffectiveness(incident));
    }
}
