package model;

import enums.IncidentType;
import enums.ResponderType;

public class TricycleDriver extends Responder {
    public TricycleDriver(String name, String callsign, Zone homeZone) {
        super(name, callsign, ResponderType.TRICYCLE_DRIVER, homeZone, 1.50);
    }

    public TricycleDriver(String name, Zone homeZone) {
        super(name, ResponderType.TRICYCLE_DRIVER, homeZone);
    }

    @Override
    public double calculateEffectiveness(Incident incident) {
        double base;
        if (incident.getType() == IncidentType.MEDICAL_EMERGENCY) {
            base = 1.15;
        } else if (incident.getType() == IncidentType.COLLAPSED_BUILDING) {
            base = 0.90;
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
