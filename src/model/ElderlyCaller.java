package model;

public class ElderlyCaller extends Caller {
    public ElderlyCaller(String name, double initialPanic) {
        super(name, "Senior Citizen", initialPanic);
    }

    public ElderlyCaller(String name) {
        this(name, 42.0);
    }

    @Override
    public double getPanicRiseRate() {
        return 1.55;
    }

    @Override
    public String buildRawTranscript(String localIncidentName, String locationHint, boolean highPanic) {
        if (highPanic) {
            return "Oh dear, someone help us! There is a terrible " + localIncidentName.toLowerCase() + " "
                    + locationHint + "! My chest hurts and we cannot get out on our own!";
        }
        return "Hello operator... we have a " + localIncidentName.toLowerCase() + " right here at "
                + locationHint + ". Please send help as soon as you can.";
    }
}
