package factory;

import enums.IncidentType;
import enums.Severity;
import model.Barangay;
import model.BlockedRoad;
import model.Caller;
import model.CollapsedBuilding;
import model.ElderlyCaller;
import model.Fire;
import model.GasLeak;
import model.Incident;
import model.MedicalEmergency;
import model.ResidentCaller;
import model.Road;
import model.TanodCaller;
import model.Zone;

import java.util.List;
import java.util.Random;

public class IncidentFactory {
    private static final String[] CALLER_NAMES = {
        "Clara Vance", "Anthony Miller", "Jason Cruz", "Maricel Santos", "Evelyn Carter",
        "Officer Berto", "Capt. Dino", "Arthur Hayes", "Principal Joy", "Lorna Reyes",
        "Daniel Brooks", "helen Park"
    };

    private static final String[] MEDICAL_SYMPTOMS = {
        "Pinned leg from fallen beam",
        "Acute chest pain and shock",
        "Deep laceration from shattered glass",
        "Severe dust inhalation",
        "Fractured collarbone on stairwell"
    };

    private final Random random;

    public IncidentFactory() {
        this.random = new Random();
    }

    public IncidentFactory(long seed) {
        this.random = new Random(seed);
    }

    public Caller createCaller(int shiftNumber, double panicScale) {
        String rawName = CALLER_NAMES[random.nextInt(CALLER_NAMES.length)];
        String name = Character.toUpperCase(rawName.charAt(0)) + rawName.substring(1);
        double basePanic = (18.0 + shiftNumber * 8.0 + random.nextInt(18)) * panicScale;
        int roll = random.nextInt(3);
        if (roll == 0) {
            return new TanodCaller(name, basePanic * 0.85);
        }
        if (roll == 1) {
            return new ElderlyCaller(name, basePanic * 1.12);
        }
        return new ResidentCaller(name, basePanic);
    }

    public Incident createIncident(IncidentType type, Zone zone, Severity severity, Caller caller, Road roadForBlockage, double deadlineScale) {
        double deadline = type.getBaseDeadlineSeconds() * deadlineScale;
        switch (type) {
            case COLLAPSED_BUILDING:
                int trapped = 2 + random.nextInt(3);
                return new CollapsedBuilding(zone, severity, deadline, caller, trapped);
            case FIRE:
                return new Fire(zone, severity, deadline, caller);
            case MEDICAL_EMERGENCY:
                String symptom = MEDICAL_SYMPTOMS[random.nextInt(MEDICAL_SYMPTOMS.length)];
                return new MedicalEmergency(zone, severity, deadline, caller, symptom);
            case BLOCKED_ROAD:
                return new BlockedRoad(zone, severity, deadline, caller, roadForBlockage);
            case GAS_LEAK:
                return new GasLeak(zone, severity, deadline, caller);
            default:
                return new MedicalEmergency(zone, severity, deadline, caller, "Emergency injury");
        }
    }

    public Incident createShiftIncident(Barangay barangay, int shiftNumber, double deadlineScale, double panicScale) {
        List<Zone> zones = barangay.getZones();
        Zone zone = zones.get(random.nextInt(zones.size()));
        Caller caller = createCaller(shiftNumber, panicScale);
        Severity severity = pickSeverityForShift(shiftNumber);
        IncidentType type = pickTypeForShift(shiftNumber);

        Road roadToBlock = null;
        if (type == IncidentType.BLOCKED_ROAD) {
            roadToBlock = pickSafeRoadToBlock(barangay, zone);
            if (roadToBlock != null) {
                zone = roadToBlock.getFrom();
            } else {
                type = IncidentType.MEDICAL_EMERGENCY;
            }
        }
        return createIncident(type, zone, severity, caller, roadToBlock, deadlineScale);
    }

    private IncidentType pickTypeForShift(int shiftNumber) {
        int roll = random.nextInt(100);
        if (shiftNumber == 1) {
            if (roll < 42) {
                return IncidentType.MEDICAL_EMERGENCY;
            }
            if (roll < 72) {
                return IncidentType.FIRE;
            }
            if (roll < 88) {
                return IncidentType.BLOCKED_ROAD;
            }
            return IncidentType.COLLAPSED_BUILDING;
        } else if (shiftNumber == 2) {
            if (roll < 26) {
                return IncidentType.MEDICAL_EMERGENCY;
            }
            if (roll < 50) {
                return IncidentType.FIRE;
            }
            if (roll < 72) {
                return IncidentType.BLOCKED_ROAD;
            }
            if (roll < 88) {
                return IncidentType.COLLAPSED_BUILDING;
            }
            return IncidentType.GAS_LEAK;
        } else {
            if (roll < 22) {
                return IncidentType.MEDICAL_EMERGENCY;
            }
            if (roll < 44) {
                return IncidentType.FIRE;
            }
            if (roll < 64) {
                return IncidentType.COLLAPSED_BUILDING;
            }
            if (roll < 84) {
                return IncidentType.GAS_LEAK;
            }
            return IncidentType.BLOCKED_ROAD;
        }
    }

    private Severity pickSeverityForShift(int shiftNumber) {
        int roll = random.nextInt(100);
        if (shiftNumber == 1) {
            return roll < 55 ? Severity.LOW : Severity.MODERATE;
        }
        if (shiftNumber == 2) {
            if (roll < 30) {
                return Severity.LOW;
            }
            if (roll < 75) {
                return Severity.MODERATE;
            }
            return Severity.HIGH;
        }
        if (roll < 20) {
            return Severity.MODERATE;
        }
        if (roll < 75) {
            return Severity.HIGH;
        }
        return Severity.CRITICAL;
    }

    public Road pickSafeRoadToBlock(Barangay barangay, Zone preferredZone) {
        List<Road> roads = barangay.getRoads();
        int currentlyBlocked = 0;
        for (Road r : roads) {
            if (r.isBlocked()) {
                currentlyBlocked++;
            }
        }
        if (currentlyBlocked >= 2) {
            return null;
        }
        for (Road road : roads) {
            if (!road.isBlocked() && (road.getFrom().equals(preferredZone) || road.getTo().equals(preferredZone))) {
                return road;
            }
        }
        for (Road road : roads) {
            if (!road.isBlocked()) {
                return road;
            }
        }
        return null;
    }
}
