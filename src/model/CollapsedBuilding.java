package model;

import enums.IncidentType;
import enums.ResponderType;
import enums.Severity;

import java.util.Arrays;

public class CollapsedBuilding extends Incident {
    private int trappedPeople;

    public CollapsedBuilding(Zone zone, Severity severity, double deadlineSeconds, Caller caller, int trappedPeople) {
        super(IncidentType.COLLAPSED_BUILDING, zone, severity, deadlineSeconds, caller,
                Arrays.asList(ResponderType.RESCUER, ResponderType.MEDIC), Math.max(2, trappedPeople));
        this.trappedPeople = Math.max(2, trappedPeople);
    }

    public CollapsedBuilding(Zone zone, Severity severity, Caller caller) {
        this(zone, severity, IncidentType.COLLAPSED_BUILDING.getBaseDeadlineSeconds(), caller, 3);
    }

    public int getTrappedPeople() {
        return trappedPeople;
    }

    @Override
    public double calculateUrgency() {
        double timePressure = (1.0 - getDeadlineFraction()) * 65.0;
        return (getSeverity().getWeight() * 24.0) + timePressure + (trappedPeople * 8.0) + (getCaller().getPanicLevel() * 0.25);
    }

    @Override
    public void escalate() {
        setSeverity(getSeverity().next());
        trappedPeople++;
        setLivesAtRisk(trappedPeople);
        getCaller().increasePanic(14.0);
    }
}
