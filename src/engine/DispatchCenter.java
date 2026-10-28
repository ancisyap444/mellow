package engine;

import enums.IncidentStatus;
import enums.ResponderStatus;
import exception.InvalidDispatchException;
import exception.NoRouteException;
import exception.ResponderUnavailableException;
import model.Incident;
import model.Responder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class DispatchCenter {
    private final List<Responder> responders;
    private final PriorityQueue<Incident> priorityQueue;
    private final List<Incident> activeIncidents;
    private final List<Incident> resolvedHistory;
    private final RoutePlanner routePlanner;
    private int livesSavedTotal;
    private int casualtiesTotal;
    private int totalDispatches;
    private int effectiveDispatches;
    private double totalResponseTimeAccumulated;
    private int completedIncidentCount;

    public DispatchCenter(List<Responder> responders, RoutePlanner routePlanner) {
        if (routePlanner == null) {
            throw new IllegalArgumentException("RoutePlanner cannot be null");
        }
        this.responders = new ArrayList<>();
        if (responders != null) {
            this.responders.addAll(responders);
        }
        this.priorityQueue = new PriorityQueue<>();
        this.activeIncidents = new ArrayList<>();
        this.resolvedHistory = new ArrayList<>();
        this.routePlanner = routePlanner;
        this.livesSavedTotal = 0;
        this.casualtiesTotal = 0;
        this.totalDispatches = 0;
        this.effectiveDispatches = 0;
        this.totalResponseTimeAccumulated = 0.0;
        this.completedIncidentCount = 0;
    }

    public void registerIncident(Incident incident) {
        if (incident != null && !activeIncidents.contains(incident)) {
            activeIncidents.add(incident);
            rebuildPriorityQueue();
        }
    }

    public double dispatch(Incident incident, Responder responder)
            throws ResponderUnavailableException, InvalidDispatchException, NoRouteException {
        if (incident == null) {
            throw new InvalidDispatchException("Select an emergency call first.");
        }
        if (responder == null) {
            throw new InvalidDispatchException("Select a responder from the right roster first.");
        }
        if (incident.getStatus().isClosed()) {
            throw new InvalidDispatchException("Incident #" + incident.getId() + " is already closed.");
        }
        if (!responder.canRespond()) {
            String reason = responder.getStatus() == ResponderStatus.RESTING
                    ? responder.getName() + " (" + responder.getCallsign() + ") is resting to recover from fatigue."
                    : responder.getName() + " (" + responder.getCallsign() + ") is currently " + responder.getStatus().getLabel() + ".";
            throw new ResponderUnavailableException(responder.getName(), reason);
        }
        if (!incident.canAcceptResponder(responder)) {
            throw new InvalidDispatchException(responder.getCallsign() + " is already assigned or unit slots are full.");
        }

        RoutePlanner.RouteResult route = routePlanner.findShortestRoute(
                responder.getCurrentZone(), incident.getZone(), responder, incident);

        totalDispatches++;
        if (incident.getRequiredResponderTypes().contains(responder.getType())
                || responder.calculateEffectiveness(incident) >= 0.85) {
            effectiveDispatches++;
        }

        incident.assignResponder(responder);
        responder.assignToIncident(incident, route.getZones(), route.getTravelSeconds());
        rebuildPriorityQueue();
        return route.getTravelSeconds();
    }

    public double dispatch(int incidentId, int responderId)
            throws ResponderUnavailableException, InvalidDispatchException, NoRouteException {
        Incident foundIncident = null;
        for (Incident inc : activeIncidents) {
            if (inc.getId() == incidentId) {
                foundIncident = inc;
                break;
            }
        }
        Responder foundResponder = null;
        for (Responder r : responders) {
            if (r.getId() == responderId) {
                foundResponder = r;
                break;
            }
        }
        return dispatch(foundIncident, foundResponder);
    }

    public void resolveIncident(Incident incident, boolean saved) {
        if (incident == null || !activeIncidents.remove(incident)) {
            return;
        }
        for (Responder r : incident.getAssignedResponders()) {
            r.completeAssignment();
        }
        if (saved) {
            incident.setStatus(IncidentStatus.RESOLVED);
            incident.onResolved();
            livesSavedTotal += incident.getLivesAtRisk();
            totalResponseTimeAccumulated += incident.getWaitTime();
            completedIncidentCount++;
            incident.setOutcomeNote("Saved " + incident.getLivesAtRisk() + " lives in " + String.format("%.0fs", incident.getWaitTime()));
        } else {
            incident.setStatus(IncidentStatus.LOST);
            int lost = Math.max(1, incident.getLivesAtRisk() - 1);
            casualtiesTotal += lost;
            incident.setOutcomeNote("Deadline missed (" + lost + " casualty). Required: " + incident.getRequiredResponderTypes());
        }
        resolvedHistory.add(incident);
        rebuildPriorityQueue();
    }

    public void closeRemainingOnShiftEnd() {
        for (Incident inc : new ArrayList<>(activeIncidents)) {
            for (Responder r : inc.getAssignedResponders()) {
                r.completeAssignment();
            }
            if (inc.hasResponderOnScene() || inc.getDeadlineFraction() >= 0.35) {
                inc.setStatus(IncidentStatus.HANDED_OVER);
                inc.onResolved();
                livesSavedTotal += Math.max(1, inc.getLivesAtRisk() - 1);
                inc.setOutcomeNote("Handed over safely to relief shift.");
            } else {
                inc.setStatus(IncidentStatus.LOST);
                casualtiesTotal += 1;
                inc.setOutcomeNote("Unattended critical emergency at shift handover.");
            }
            resolvedHistory.add(inc);
        }
        activeIncidents.clear();
        priorityQueue.clear();
    }

    public void rebuildPriorityQueue() {
        priorityQueue.clear();
        priorityQueue.addAll(activeIncidents);
    }

    public List<Incident> getSortedActiveIncidents() {
        rebuildPriorityQueue();
        List<Incident> sorted = new ArrayList<>();
        PriorityQueue<Incident> copy = new PriorityQueue<>(priorityQueue);
        while (!copy.isEmpty()) {
            sorted.add(copy.poll());
        }
        return sorted;
    }

    public List<Responder> getResponders() {
        return Collections.unmodifiableList(responders);
    }

    public List<Incident> getActiveIncidents() {
        return Collections.unmodifiableList(activeIncidents);
    }

    public List<Incident> getResolvedHistory() {
        return Collections.unmodifiableList(resolvedHistory);
    }

    public int getLivesSavedTotal() {
        return livesSavedTotal;
    }

    public int getCasualtiesTotal() {
        return casualtiesTotal;
    }

    public double getAverageResponseTime() {
        if (completedIncidentCount == 0) {
            return 0.0;
        }
        return totalResponseTimeAccumulated / completedIncidentCount;
    }

    public double getResourceEfficiencyPercent() {
        if (totalDispatches == 0) {
            return 100.0;
        }
        return (effectiveDispatches * 100.0) / totalDispatches;
    }
}
