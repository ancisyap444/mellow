package model;

public class TanodCaller extends Caller {
    public TanodCaller(String name, double initialPanic) {
        super(name, "Patrol Officer", initialPanic);
    }

    public TanodCaller(String name) {
        this(name, 20.0);
    }

    @Override
    public double getPanicRiseRate() {
        return 0.90;
    }

    @Override
    public String buildRawTranscript(String localIncidentName, String locationHint, boolean highPanic) {
        if (highPanic) {
            return "Command Base, come in! The " + localIncidentName.toLowerCase() + " " + locationHint
                    + " is escalating out of control! Requesting immediate heavy backup, over!";
        }
        return "Base, Patrol Officer " + getName() + " on channel: confirmed " + localIncidentName.toLowerCase()
                + " at " + locationHint + ". Area is cordoned off, awaiting specialist units, over.";
    }
}
