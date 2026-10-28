package model;

import enums.IncidentType;
import enums.ResponderType;

public class Engineer extends Responder {
    public Engineer(String name, String callsign, Zone homeZone) {
        super(name, callsign, ResponderType.ENGINEER, homeZone, 1.00);
    }

    public Engineer(String name, Zone homeZone) {
        super(name, ResponderType.ENGINEER, homeZone);
    }

    @Override
    public double calculateEffectiveness(Incident incident) {
        double base;
        if (incident.getType() == IncidentType.BLOCKED_ROAD) {
            base = 1.55;
        } else if (incident.getType() == IncidentType.GAS_LEAK) {
            base = 1.30;
        } else {
            base = 0.60;
        }
        return base * getFatiguePerformanceMultiplier();
    }

    @Override
    public double getHandlingTime(Incident incident) {
        return incident.getType().getBaseHandlingSeconds() / Math.max(0.35, calculateEffectiveness(incident));
    }
}
