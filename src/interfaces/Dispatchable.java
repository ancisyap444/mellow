package interfaces;

import model.Incident;
import model.Zone;
import java.util.List;

public interface Dispatchable {
    boolean canRespond();
    void assignToIncident(Incident incident, List<Zone> route, double travelSeconds);
    void completeAssignment();
}
