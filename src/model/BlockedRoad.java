package model;

import enums.IncidentType;
import enums.ResponderType;
import enums.Severity;

import java.util.Collections;

public class BlockedRoad extends Incident {
    private final Road blockedRoad;

    public BlockedRoad(Zone zone, Severity severity, double deadlineSeconds, Caller caller, Road blockedRoad) {
        super(IncidentType.BLOCKED_ROAD, zone, severity, deadlineSeconds, caller,
                Collections.singletonList(ResponderType.ENGINEER), 1);
        this.blockedRoad = blockedRoad;
        if (this.blockedRoad != null) {
            this.blockedRoad.setBlocked(true);
        }
    }

    public BlockedRoad(Zone zone, Severity severity, Caller caller, Road blockedRoad) {
        this(zone, severity, IncidentType.BLOCKED_ROAD.getBaseDeadlineSeconds(), caller, blockedRoad);
    }

    public Road getBlockedRoad() {
        return blockedRoad;
    }

    @Override
    public void onResolved() {
        if (blockedRoad != null) {
            blockedRoad.setBlocked(false);
        }
    }

    @Override
    public double calculateUrgency() {
        double timePressure = (1.0 - getDeadlineFraction()) * 48.0;
        double roadBonus = (blockedRoad != null && blockedRoad.isBlocked()) ? 18.0 : 6.0;
        return (getSeverity().getWeight() * 17.0) + timePressure + roadBonus + (getCaller().getPanicLevel() * 0.16);
    }

    @Override
    public void escalate() {
        setSeverity(getSeverity().next());
        getCaller().increasePanic(10.0);
    }
}
