package model;

public class ResidentCaller extends Caller {
    public ResidentCaller(String name, double initialPanic) {
        super(name, "Civilian Resident", initialPanic);
    }

    public ResidentCaller(String name) {
        this(name, 34.0);
    }

    @Override
    public double getPanicRiseRate() {
        return 1.35;
    }

    @Override
    public String buildRawTranscript(String localIncidentName, String locationHint, boolean highPanic) {
        if (highPanic) {
            return "Please hurry!! We have a " + localIncidentName.toLowerCase() + " " + locationHint
                    + "! Dust and smoke are filling the street and families are trapped!";
        }
        return "Emergency Dispatch, this is " + getName() + "! Reporting a " + localIncidentName.toLowerCase()
                + " at " + locationHint + ". Please send a response unit right away!";
    }
}
