package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Barangay {
    private final String name;
    private final List<Zone> zones;
    private final List<Road> roads;
    private final HashMap<Zone, List<Road>> adjacencyMap;

    public Barangay(String name) {
        this.name = (name == null || name.trim().isEmpty()) ? "San Isidro District" : name.trim();
        this.zones = new ArrayList<>();
        this.roads = new ArrayList<>();
        this.adjacencyMap = new HashMap<>();
        initializeDefaultLayout();
    }

    public Barangay() {
        this("San Isidro Valley");
    }

    private void initializeDefaultLayout() {
        Zone d1 = new Zone(1, "District 1 - Civic Center", "Town Hall & Covered Court", 0.20, 0.26, 8);
        Zone d2 = new Zone(2, "District 2 - School Zone", "San Isidro Elementary School", 0.50, 0.22, 7);
        Zone d3 = new Zone(3, "District 3 - Market Square", "Central Market & Plaza", 0.80, 0.27, 10);
        Zone d4 = new Zone(4, "District 4 - Riverside", "Westbridge & Riverfront", 0.21, 0.74, 9);
        Zone d5 = new Zone(5, "District 5 - Medical Park", "Community Clinic & Chapel", 0.50, 0.76, 8);
        Zone d6 = new Zone(6, "District 6 - Highland Ridge", "Water Tower & Overlook", 0.80, 0.72, 7);

        addZone(d1);
        addZone(d2);
        addZone(d3);
        addZone(d4);
        addZone(d5);
        addZone(d6);

        addRoad(new Road(1, "Commonwealth Ave", d1, d2, 4.5, false));
        addRoad(new Road(2, "Market Blvd", d2, d3, 4.8, false));
        addRoad(new Road(3, "Riverfront Lane", d1, d4, 5.0, true));
        addRoad(new Road(4, "Central Pkwy", d2, d5, 4.5, false));
        addRoad(new Road(5, "Highland Pass", d3, d6, 5.2, true));
        addRoad(new Road(6, "Bridgeview Rd", d4, d5, 4.6, false));
        addRoad(new Road(7, "Clinic Way", d5, d6, 4.8, false));
        addRoad(new Road(8, "West Shortcut", d1, d5, 6.2, true));
        addRoad(new Road(9, "East Alleyway", d2, d6, 6.4, true));
    }

    public void addZone(Zone zone) {
        if (zone != null && !zones.contains(zone)) {
            zones.add(zone);
            adjacencyMap.put(zone, new ArrayList<>());
        }
    }

    public void addRoad(Road road) {
        if (road != null) {
            roads.add(road);
            adjacencyMap.computeIfAbsent(road.getFrom(), k -> new ArrayList<>()).add(road);
            adjacencyMap.computeIfAbsent(road.getTo(), k -> new ArrayList<>()).add(road);
        }
    }

    public String getName() {
        return name;
    }

    public List<Zone> getZones() {
        return Collections.unmodifiableList(zones);
    }

    public List<Road> getRoads() {
        return Collections.unmodifiableList(roads);
    }

    public Map<Zone, List<Road>> getAdjacencyMap() {
        return Collections.unmodifiableMap(adjacencyMap);
    }

    public Zone getZoneById(int id) {
        for (Zone zone : zones) {
            if (zone.getId() == id) {
                return zone;
            }
        }
        return zones.isEmpty() ? null : zones.get(0);
    }

    public Road getRoadBetween(Zone a, Zone b) {
        List<Road> connected = adjacencyMap.get(a);
        if (connected == null) {
            return null;
        }
        for (Road road : connected) {
            if (road.connects(a, b)) {
                return road;
            }
        }
        return null;
    }

    public void clearAllBlockedRoads() {
        for (Road road : roads) {
            road.setBlocked(false);
        }
    }
}
