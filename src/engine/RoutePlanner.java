package engine;

import enums.ResponderType;
import exception.NoRouteException;
import model.Barangay;
import model.BlockedRoad;
import model.Incident;
import model.Responder;
import model.Road;
import model.Zone;
import util.GameConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class RoutePlanner {
    public static class RouteResult {
        private final List<Zone> zones;
        private final double travelSeconds;

        public RouteResult(List<Zone> zones, double travelSeconds) {
            this.zones = zones == null ? new ArrayList<>() : new ArrayList<>(zones);
            this.travelSeconds = Math.max(0.2, travelSeconds);
        }

        public List<Zone> getZones() {
            return Collections.unmodifiableList(zones);
        }

        public double getTravelSeconds() {
            return travelSeconds;
        }
    }

    private final Barangay barangay;

    public RoutePlanner(Barangay barangay) {
        if (barangay == null) {
            throw new IllegalArgumentException("Barangay cannot be null");
        }
        this.barangay = barangay;
    }

    public RouteResult findShortestRoute(Zone origin, Zone target) throws NoRouteException {
        return findShortestRoute(origin, target, null, null);
    }

    public RouteResult findShortestRoute(Zone origin, Zone target, Responder responder, Incident incident) throws NoRouteException {
        if (origin == null || target == null) {
            throw new NoRouteException("Origin or target zone is missing.");
        }
        if (origin.equals(target)) {
            return new RouteResult(Collections.singletonList(origin), 0.3);
        }

        Map<Zone, List<Road>> graph = barangay.getAdjacencyMap();
        Map<Zone, Double> dist = new HashMap<>();
        Map<Zone, Zone> prev = new HashMap<>();

        for (Zone z : barangay.getZones()) {
            dist.put(z, Double.POSITIVE_INFINITY);
        }
        dist.put(origin, 0.0);

        PriorityQueue<Zone> pq = new PriorityQueue<>(Comparator.comparingDouble(z -> dist.getOrDefault(z, Double.POSITIVE_INFINITY)));
        pq.add(origin);

        boolean isMoto = responder != null && responder.getType() == ResponderType.TRICYCLE_DRIVER;
        boolean isClearingEngineer = responder != null
                && responder.getType() == ResponderType.ENGINEER
                && incident instanceof BlockedRoad;

        while (!pq.isEmpty()) {
            Zone current = pq.poll();
            if (current.equals(target)) {
                break;
            }
            double currentDist = dist.getOrDefault(current, Double.POSITIVE_INFINITY);
            if (Double.isInfinite(currentDist)) {
                break;
            }

            List<Road> edges = graph.getOrDefault(current, Collections.emptyList());
            for (Road road : edges) {
                Zone neighbor = road.getOtherEnd(current);
                if (neighbor == null) {
                    continue;
                }
                double edgeTime = computeRoadTravelSeconds(road, isMoto, isClearingEngineer, responder);
                if (Double.isInfinite(edgeTime)) {
                    continue;
                }
                double nextDist = currentDist + edgeTime;
                if (nextDist < dist.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    dist.put(neighbor, nextDist);
                    prev.put(neighbor, current);
                    pq.remove(neighbor);
                    pq.add(neighbor);
                }
            }
        }

        double totalTime = dist.getOrDefault(target, Double.POSITIVE_INFINITY);
        if (Double.isInfinite(totalTime)) {
            throw new NoRouteException(
                    origin.getShortCode(),
                    target.getShortCode(),
                    "Roads between " + origin.getShortCode() + " and " + target.getShortCode()
                            + " are blocked by debris! Dispatch an Engineer first."
            );
        }

        List<Zone> path = new ArrayList<>();
        Zone cursor = target;
        while (cursor != null) {
            path.add(cursor);
            if (cursor.equals(origin)) {
                break;
            }
            cursor = prev.get(cursor);
        }
        Collections.reverse(path);
        return new RouteResult(path, totalTime);
    }

    private double computeRoadTravelSeconds(Road road, boolean isMoto, boolean isClearingEngineer, Responder responder) {
        if (road.isBlocked()) {
            if (isClearingEngineer) {
                return road.getBaseTravelSeconds() * 1.15;
            }
            if (isMoto && road.isNarrowAlley()) {
                return road.getBaseTravelSeconds() * 1.35;
            }
            if (GameConfig.BLOCKED_ROAD_AS_PENALTY_FALLBACK) {
                return road.getBaseTravelSeconds() + GameConfig.BLOCKED_ROAD_PENALTY_SECONDS;
            }
            return Double.POSITIVE_INFINITY;
        }

        double base = road.getBaseTravelSeconds();
        if (road.isNarrowAlley() && isMoto) {
            base *= 0.68;
        }
        double speedFactor = responder != null ? responder.getTravelSpeedFactor() : 1.0;
        return base / Math.max(0.45, speedFactor);
    }
}
