package model;

import enums.IncidentType;
import enums.ResponderType;

public class Medic extends Responder {
    public Medic(String name, String callsign, Zone homeZone) {
        super(name, callsign, ResponderType.MEDIC, homeZone, 1.15);
    }

    public Medic(String name, Zone homeZone) {
        super(name, ResponderType.MEDIC, homeZone);
    }

    @Override
    public double calculateEffectiveness(Incident incident) {
        double base;
        if (incident.getType() == IncidentType.MEDICAL_EMERGENCY) {
            base = 1.45;
        } else if (incident.getType() == IncidentType.COLLAPSED_BUILDING) {
            base = 1.15;
        } else {
            base = 0.55;
        }
        return base * getFatiguePerformanceMultiplier();
    }

    @Override
    public double getHandlingTime(Incident incident) {
        return incident.getType().getBaseHandlingSeconds() / Math.max(0.35, calculateEffectiveness(incident));
    }
}
