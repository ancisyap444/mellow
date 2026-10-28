package interfaces;

import enums.GameStage;
import model.Incident;
import model.Responder;

public interface GameListener {
    void onIncidentSpawned(Incident incident);
    void onIncidentUpdated(Incident incident);
    void onIncidentResolved(Incident incident, boolean saved);
    void onResponderUpdated(Responder responder);
    void onStressChanged(double newStress);
    void onPanicChanged(Incident incident, double newPanic);
    void onAftershockTriggered(String message, double intensity);
    void onRadioTransmission(String speaker, String message);
    void onStageChanged(GameStage newStage);
}
