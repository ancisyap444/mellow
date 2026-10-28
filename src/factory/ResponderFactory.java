package factory;

import enums.ResponderType;
import model.Barangay;
import model.Engineer;
import model.Firefighter;
import model.Medic;
import model.Rescuer;
import model.Responder;
import model.TricycleDriver;
import model.Zone;

import java.util.ArrayList;
import java.util.List;

public class ResponderFactory {
    public Responder createResponder(ResponderType type, String name, String callsign, Zone homeZone) {
        switch (type) {
            case MEDIC:
                return new Medic(name, callsign, homeZone);
            case RESCUER:
                return new Rescuer(name, callsign, homeZone);
            case FIREFIGHTER:
                return new Firefighter(name, callsign, homeZone);
            case ENGINEER:
                return new Engineer(name, callsign, homeZone);
            case TRICYCLE_DRIVER:
                return new TricycleDriver(name, callsign, homeZone);
            default:
                return new Medic(name, callsign, homeZone);
        }
    }

    public Responder createResponder(ResponderType type, String name, Zone homeZone) {
        return createResponder(type, name, null, homeZone);
    }

    public List<Responder> createDefaultRoster(Barangay barangay) {
        Responder.resetIdCounter();
        List<Responder> roster = new ArrayList<>();
        Zone d1 = barangay.getZoneById(1);
        Zone d2 = barangay.getZoneById(2);
        Zone d3 = barangay.getZoneById(3);
        Zone d5 = barangay.getZoneById(5);

        roster.add(createResponder(ResponderType.MEDIC, "Paramedic Joy", "MED-1", d5));
        roster.add(createResponder(ResponderType.MEDIC, "Dr. Marcus", "MED-2", d1));
        roster.add(createResponder(ResponderType.RESCUER, "Capt. Dante", "RES-1", d1));
        roster.add(createResponder(ResponderType.RESCUER, "Lt. Elena", "RES-2", d2));
        roster.add(createResponder(ResponderType.FIREFIGHTER, "Capt. Alonzo", "FIR-1", d3));
        roster.add(createResponder(ResponderType.FIREFIGHTER, "Lt. Reyes", "FIR-2", d1));
        roster.add(createResponder(ResponderType.ENGINEER, "Engr. Thomas", "ENG-1", d2));
        roster.add(createResponder(ResponderType.TRICYCLE_DRIVER, "Rider Ethan", "MTO-1", d3));
        return roster;
    }
}
