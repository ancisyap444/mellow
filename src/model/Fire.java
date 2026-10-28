package model;

import enums.IncidentType;
import enums.ResponderType;
import enums.Severity;

import java.util.Collections;

public class Fire extends Incident {
    private int structuresThreatened;

    public Fire(Zone zone, Severity severity, double deadlineSeconds, Caller caller) {
        super(IncidentType.FIRE, zone, severity, deadlineSeconds, caller,
                Collections.singletonList(ResponderType.FIREFIGHTER), 3);
        this.structuresThreatened = 2;
    }

    public Fire(Zone zone, Severity severity, Caller caller) {
        this(zone, severity, IncidentType.FIRE.getBaseDeadlineSeconds(), caller);
    }

    @Override
    public double calculateUrgency() {
        double timePressure = (1.0 - getDeadlineFraction()) * 62.0;
        return (getSeverity().getWeight() * 22.0) + timePressure + (structuresThreatened * 6.0) + (getCaller().getPanicLevel() * 0.22);
    }

    @Override
    public void escalate() {
        setSeverity(getSeverity().next());
        structuresThreatened++;
        setLivesAtRisk(getLivesAtRisk() + 1);
        getCaller().increasePanic(15.0);
    }
}
