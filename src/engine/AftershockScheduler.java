package engine;

import enums.IncidentType;
import enums.Severity;
import factory.IncidentFactory;
import model.Barangay;
import model.Caller;
import model.Incident;
import model.Road;
import model.Zone;

import java.util.ArrayDeque;
import java.util.Queue;

public class AftershockScheduler {
    public static class AftershockEvent {
        private final double triggerAtElapsedSeconds;
        private final double intensity;
        private final String broadcastMessage;

        public AftershockEvent(double triggerAtElapsedSeconds, double intensity, String broadcastMessage) {
            this.triggerAtElapsedSeconds = triggerAtElapsedSeconds;
            this.intensity = intensity;
            this.broadcastMessage = broadcastMessage;
        }

        public double getTriggerAtElapsedSeconds() {
            return triggerAtElapsedSeconds;
        }

        public double getIntensity() {
            return intensity;
        }

        public String getBroadcastMessage() {
            return broadcastMessage;
        }
    }

    private final Queue<AftershockEvent> scheduledEvents;
    private final IncidentFactory incidentFactory;

    public AftershockScheduler(IncidentFactory incidentFactory) {
        this.scheduledEvents = new ArrayDeque<>();
        this.incidentFactory = incidentFactory;
    }

    public void scheduleForShift(int shiftNumber) {
        scheduledEvents.clear();
        if (shiftNumber == 2) {
            scheduledEvents.offer(new AftershockEvent(42.0, 0.75,
                    "SEISMIC ALERT: Magnitude 5.2 aftershock felt! Falling debris reported on district roads!"));
            scheduledEvents.offer(new AftershockEvent(108.0, 0.90,
                    "STRONG AFTERSHOCK: Magnitude 5.6 tremor! Check blocked routes and weakened buildings!"));
        } else if (shiftNumber == 3) {
            scheduledEvents.offer(new AftershockEvent(52.0, 0.85,
                    "MORNING TREMOR: Aftershock rattled weakened walls and cracked road pavement!"));
            scheduledEvents.offer(new AftershockEvent(118.0, 0.95,
                    "FINAL TREMOR WAVE: Secondary shockwave! Keep responders rotated and stay calm!"));
        }
    }

    public AftershockEvent pollDueEvent(double shiftElapsedSeconds) {
        AftershockEvent next = scheduledEvents.peek();
        if (next != null && shiftElapsedSeconds >= next.getTriggerAtElapsedSeconds()) {
            return scheduledEvents.poll();
        }
        return null;
    }

    public Incident createAftershockIncident(Barangay barangay, int shiftNumber, double deadlineScale, double panicScale) {
        Zone fallbackZone = barangay.getZoneById(2);
        Road road = incidentFactory.pickSafeRoadToBlock(barangay, fallbackZone);
        Caller caller = incidentFactory.createCaller(shiftNumber, panicScale);
        if (road != null) {
            return incidentFactory.createIncident(
                    IncidentType.BLOCKED_ROAD,
                    road.getFrom(),
                    Severity.HIGH,
                    caller,
                    road,
                    deadlineScale);
        }
        return incidentFactory.createIncident(
                IncidentType.COLLAPSED_BUILDING,
                barangay.getZoneById(3),
                Severity.HIGH,
                caller,
                null,
                deadlineScale);
    }

    public int getRemainingEventCount() {
        return scheduledEvents.size();
    }
}
