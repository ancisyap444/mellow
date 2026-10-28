package model;

import enums.IncidentType;
import enums.ResponderType;
import enums.Severity;

import java.util.Collections;

public class MedicalEmergency extends Incident {
    private final String symptom;

    public MedicalEmergency(Zone zone, Severity severity, double deadlineSeconds, Caller caller, String symptom) {
        super(IncidentType.MEDICAL_EMERGENCY, zone, severity, deadlineSeconds, caller,
                Collections.singletonList(ResponderType.MEDIC), 2);
        this.symptom = (symptom == null || symptom.trim().isEmpty()) ? "Acute trauma injury" : symptom.trim();
    }

    public MedicalEmergency(Zone zone, Severity severity, Caller caller) {
        this(zone, severity, IncidentType.MEDICAL_EMERGENCY.getBaseDeadlineSeconds(), caller, "Fracture and shock");
    }

    public String getSymptom() {
        return symptom;
    }

    @Override
    public double calculateUrgency() {
        double timePressure = (1.0 - getDeadlineFraction()) * 66.0;
        return (getSeverity().getWeight() * 21.0) + timePressure + (getCaller().getPanicLevel() * 0.28);
    }

    @Override
    public void escalate() {
        setSeverity(getSeverity().next());
        getCaller().increasePanic(16.0);
    }
}
