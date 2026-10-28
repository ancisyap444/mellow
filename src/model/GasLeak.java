package model;

import enums.IncidentType;
import enums.ResponderType;
import enums.Severity;

import java.util.Arrays;

public class GasLeak extends Incident {
    private double gasConcentrationPpm;

    public GasLeak(Zone zone, Severity severity, double deadlineSeconds, Caller caller) {
        super(IncidentType.GAS_LEAK, zone, severity, deadlineSeconds, caller,
                Arrays.asList(ResponderType.FIREFIGHTER, ResponderType.ENGINEER), 3);
        this.gasConcentrationPpm = 350.0;
    }

    public GasLeak(Zone zone, Severity severity, Caller caller) {
        this(zone, severity, IncidentType.GAS_LEAK.getBaseDeadlineSeconds(), caller);
    }

    public double getGasConcentrationPpm() {
        return gasConcentrationPpm;
    }

    @Override
    public double calculateUrgency() {
        double timePressure = (1.0 - getDeadlineFraction()) * 64.0;
        return (getSeverity().getWeight() * 23.0) + timePressure + (gasConcentrationPpm * 0.03) + (getCaller().getPanicLevel() * 0.24);
    }

    @Override
    public void escalate() {
        setSeverity(getSeverity().next());
        gasConcentrationPpm += 120.0;
        setLivesAtRisk(getLivesAtRisk() + 1);
        getCaller().increasePanic(15.0);
    }
}
