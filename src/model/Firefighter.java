package model;

import enums.IncidentType;
import enums.ResponderType;

public class Firefighter extends Responder {
    public Firefighter(String name, String callsign, Zone homeZone) {
        super(name, callsign, ResponderType.FIREFIGHTER, homeZone, 1.08);
    }

    public Firefighter(String name, Zone homeZone) {
        super(name, ResponderType.FIREFIGHTER, homeZone);
    }

    @Override
    public double calculateEffectiveness(Incident incident) {
        double base;
        if (incident.getType() == IncidentType.FIRE) {
            base = 1.50;
        } else if (incident.getType() == IncidentType.GAS_LEAK) {
            base = 1.25;
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
