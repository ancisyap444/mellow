package engine;

import enums.Difficulty;
import enums.GameStage;
import enums.ResponderStatus;
import enums.ResponderType;
import exception.InvalidDispatchException;
import exception.NoRouteException;
import exception.ResponderUnavailableException;
import factory.IncidentFactory;
import factory.ResponderFactory;
import interfaces.GameListener;
import interfaces.Saveable;
import model.Barangay;
import model.Incident;
import model.Operator;
import model.Responder;
import util.FileHandler;
import util.GameConfig;
import util.SoundSynthesizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameManager implements Saveable {
    private static GameManager instance;

    private final List<GameListener> listeners;
    private final List<String> radioHistoryLog;
    private Barangay barangay;
    private Operator operator;
    private DispatchCenter dispatchCenter;
    private RoutePlanner routePlanner;
    private ShiftController shiftController;
    private IncidentFactory incidentFactory;
    private ResponderFactory responderFactory;
    private IncidentSpawner incidentSpawner;
    private AftershockScheduler aftershockScheduler;
    private ScoreCalculator scoreCalculator;
    private FileHandler fileHandler;

    private GameStage stage;
    private Difficulty difficulty;
    private Incident selectedIncident;
    private Responder selectedResponder;
    private boolean paused;
    private double timeScale;
    private int highScore;
    private int highestUnlockedShift;
    private String bestRating;
    private String lastStatusBanner;

    private GameManager() {
        this.listeners = new ArrayList<>();
        this.radioHistoryLog = new ArrayList<>();
        this.difficulty = Difficulty.NORMAL;
        this.paused = false;
        this.timeScale = 1.0;
        this.highScore = 0;
        this.highestUnlockedShift = 1;
        this.bestRating = "None";
        this.lastStatusBanner = "Stand by for emergency transmissions...";
        this.fileHandler = new FileHandler(GameConfig.SAVE_FILE_PATH);
        this.scoreCalculator = new ScoreCalculator();
        initializeWorld();
        this.fileHandler.load(this);
        this.stage = GameStage.TITLE;
    }

    public static synchronized GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public void initializeWorld() {
        Incident.resetIdCounter();
        Responder.resetIdCounter();
        this.barangay = new Barangay("San Isidro Valley");
        this.operator = new Operator("Operator Taylor");
        this.routePlanner = new RoutePlanner(barangay);
        this.incidentFactory = new IncidentFactory();
        this.responderFactory = new ResponderFactory();
        List<Responder> roster = responderFactory.createDefaultRoster(barangay);
        this.dispatchCenter = new DispatchCenter(roster, routePlanner);
        this.shiftController = new ShiftController();
        this.incidentSpawner = new IncidentSpawner(incidentFactory);
        this.aftershockScheduler = new AftershockScheduler(incidentFactory);
        this.selectedIncident = null;
        this.selectedResponder = null;
        this.paused = false;
        this.timeScale = 1.0;
        this.radioHistoryLog.clear();
    }

    public void startNewCampaign(Difficulty chosenDifficulty) {
        if (chosenDifficulty != null) {
            this.difficulty = chosenDifficulty;
        }
        initializeWorld();
        beginShift(1);
    }

    public void beginShift(int shiftNumber) {
        barangay.clearAllBlockedRoads();
        shiftController.startShift(shiftNumber);
        incidentSpawner.resetForShift(shiftNumber);
        aftershockScheduler.scheduleForShift(shiftNumber);
        operator.cancelBreathingPause();
        operator.reduceStress(20.0);
        this.paused = false;

        for (Responder responder : dispatchCenter.getResponders()) {
            responder.completeAssignment();
            if (shiftNumber == 3) {
                responder.setFatigue(Math.max(18.0, responder.getFatigue() * 0.55));
            } else {
                responder.setFatigue(responder.getFatigue() * 0.35);
            }
        }

        Incident initialIncident = incidentFactory.createShiftIncident(
                barangay, shiftNumber, difficulty.getDeadlineScale(), difficulty.getPanicScale());
        dispatchCenter.registerIncident(initialIncident);
        selectedIncident = initialIncident;
        autoSelectBestResponder();
        setStage(GameStage.SHIFT_INTRO);
        notifyIncidentSpawned(initialIncident);
        notifyRadioTransmission(initialIncident.getCaller().getName(),
                initialIncident.getCaller().getDialogueLine(initialIncident, operator.getStress()));
    }

    public void enterPlayingStage() {
        setStage(GameStage.PLAYING);
        lastStatusBanner = shiftController.getShiftTitle() + " - Hotline Active! Press [A] to Auto-Match unit or [D] to Dispatch.";
        SoundSynthesizer.playIncomingCallBeep();
    }

    public void advanceAfterShiftReport() {
        if (shiftController.hasNextShift()) {
            int nextShift = shiftController.getCurrentShift() + 1;
            if (nextShift > highestUnlockedShift) {
                highestUnlockedShift = nextShift;
            }
            saveProgress();
            beginShift(nextShift);
        } else {
            finalizeCampaignScore();
            setStage(GameStage.FINAL_REPORT);
        }
    }

    public void skipToShiftEndForTesting() {
        if (stage == GameStage.PLAYING) {
            dispatchCenter.closeRemainingOnShiftEnd();
            finalizeCampaignScore();
            setStage(GameStage.SHIFT_REPORT);
        }
    }

    public void togglePause() {
        if (stage == GameStage.PLAYING) {
            this.paused = !this.paused;
            this.lastStatusBanner = paused ? "GAME PAUSED - Press [P] to Resume" : "Simulation Resumed";
        }
    }

    public void toggleTimeScale() {
        if (this.timeScale < 1.5) {
            this.timeScale = 2.0;
        } else {
            this.timeScale = 1.0;
        }
        this.lastStatusBanner = String.format("Simulation Speed set to %.0fx [TAB]", this.timeScale);
    }

    public String autoSelectBestResponder() {
        if (selectedIncident == null || selectedIncident.getStatus().isClosed()) {
            selectNextUrgentIncident();
        }
        if (selectedIncident == null) {
            return "No active incident to match.";
        }
        List<ResponderType> missing = selectedIncident.getMissingResponderTypes();
        Responder bestCandidate = null;
        double bestEta = Double.POSITIVE_INFINITY;

        for (Responder responder : dispatchCenter.getResponders()) {
            if (!responder.canRespond() || !selectedIncident.canAcceptResponder(responder)) {
                continue;
            }
            boolean isPrimaryRequired = missing.contains(responder.getType());
            try {
                RoutePlanner.RouteResult route = routePlanner.findShortestRoute(
                        responder.getCurrentZone(), selectedIncident.getZone(), responder, selectedIncident);
                double scoreEta = route.getTravelSeconds() + (isPrimaryRequired ? 0.0 : 25.0);
                if (scoreEta < bestEta) {
                    bestEta = scoreEta;
                    bestCandidate = responder;
                }
            } catch (NoRouteException ignored) {
            }
        }

        if (bestCandidate != null) {
            this.selectedResponder = bestCandidate;
            this.lastStatusBanner = "Auto-matched " + bestCandidate.getName() + " (" + bestCandidate.getCallsign()
                    + ") for #" + selectedIncident.getId() + " - Press [D] to Dispatch!";
            return this.lastStatusBanner;
        }
        this.lastStatusBanner = "No available responder with an open route matches Incident #" + selectedIncident.getId() + "!";
        return this.lastStatusBanner;
    }

    public String toggleSelectedResponderRest() {
        if (selectedResponder == null) {
            return "Select a responder first.";
        }
        if (selectedResponder.toggleManualRest()) {
            String state = selectedResponder.getStatus() == ResponderStatus.RESTING
                    ? "sent to rest early (fast fatigue recovery)."
                    : "returned to active duty.";
            this.lastStatusBanner = selectedResponder.getName() + " (" + selectedResponder.getCallsign() + ") " + state;
            notifyResponderUpdated(selectedResponder);
            return this.lastStatusBanner;
        }
        this.lastStatusBanner = selectedResponder.getName() + " cannot toggle rest right now (" + selectedResponder.getStatus().getLabel() + ").";
        return this.lastStatusBanner;
    }

    private void finalizeCampaignScore() {
        int currentScore = scoreCalculator.calculateTotalScore(dispatchCenter, operator);
        if (currentScore > highScore) {
            highScore = currentScore;
            bestRating = scoreCalculator.calculateRatingGrade(dispatchCenter, operator);
        }
        saveProgress();
    }

    public void saveProgress() {
        fileHandler.save(this);
    }

    public void update(double rawDelta) {
        if (stage != GameStage.PLAYING || paused) {
            return;
        }
        double delta = rawDelta * timeScale;

        shiftController.update(delta);
        operator.update(delta);

        int activeCount = dispatchCenter.getActiveIncidents().size();
        if (activeCount > 0 && !operator.isBreathingActive()) {
            operator.addStress(activeCount * 0.32 * delta);
            notifyStressChanged(operator.getStress());
        }

        Incident newlySpawned = incidentSpawner.updateAndTrySpawn(
                delta, barangay, shiftController.getCurrentShift(), activeCount, difficulty);
        if (newlySpawned != null) {
            dispatchCenter.registerIncident(newlySpawned);
            if (selectedIncident == null || selectedIncident.getStatus().isClosed()) {
                selectedIncident = newlySpawned;
                autoSelectBestResponder();
            }
            SoundSynthesizer.playIncomingCallBeep();
            notifyIncidentSpawned(newlySpawned);
            notifyRadioTransmission(newlySpawned.getCaller().getName(),
                    newlySpawned.getCaller().getDialogueLine(newlySpawned, operator.getStress()));
        }

        AftershockScheduler.AftershockEvent tremor = aftershockScheduler.pollDueEvent(shiftController.getElapsedSeconds());
        if (tremor != null) {
            Incident tremorIncident = aftershockScheduler.createAftershockIncident(
                    barangay, shiftController.getCurrentShift(), difficulty.getDeadlineScale(), difficulty.getPanicScale());
            dispatchCenter.registerIncident(tremorIncident);
            operator.addStress(9.0);
            lastStatusBanner = tremor.getBroadcastMessage();
            SoundSynthesizer.playAftershockRumble();
            notifyAftershock(tremor.getBroadcastMessage(), tremor.getIntensity());
            notifyIncidentSpawned(tremorIncident);
            notifyRadioTransmission("SEISMIC ALERT", tremor.getBroadcastMessage());
        }

        for (Responder responder : dispatchCenter.getResponders()) {
            ResponderStatus before = responder.getStatus();
            responder.updateState(delta);
            if (before != responder.getStatus()) {
                notifyResponderUpdated(responder);
                if (responder.getStatus() == ResponderStatus.BUSY && responder.getAssignedIncident() != null) {
                    notifyRadioTransmission(responder.getCallsign(),
                            responder.getName() + " arrived on scene at " + responder.getCurrentZone().getShortCode() + "!");
                }
            }
        }

        for (Incident incident : new ArrayList<>(dispatchCenter.getActiveIncidents())) {
            updateSingleIncident(incident, delta);
        }
        dispatchCenter.rebuildPriorityQueue();

        if (shiftController.isShiftFinished()) {
            dispatchCenter.closeRemainingOnShiftEnd();
            finalizeCampaignScore();
            setStage(GameStage.SHIFT_REPORT);
        }
    }

    private void updateSingleIncident(Incident incident, double delta) {
        incident.addWaitTime(delta);

        boolean hasOnScene = incident.hasResponderOnScene();
        double panicRise = incident.getCaller().getPanicRiseRate() * difficulty.getPanicScale();
        if (hasOnScene) {
            panicRise *= 0.20;
        }
        incident.getCaller().increasePanic(panicRise * delta);
        notifyPanicChanged(incident, incident.getCaller().getPanicLevel());

        double drainMult = incident.getCaller().getMood().getDeadlineDrainMultiplier();
        if (hasOnScene) {
            drainMult *= incident.isFullyStaffed() ? 0.18 : 0.42;
        }
        incident.setRemainingDeadline(incident.getRemainingDeadline() - (delta * drainMult));

        if (!hasOnScene && incident.getEscalationTimer() >= 20.0) {
            incident.resetEscalationTimer();
            incident.escalate();
            operator.addStress(3.5);
            lastStatusBanner = "Escalation: " + incident.getType().getLabel() + " at " + incident.getZone().getShortCode()
                    + " worsened to " + incident.getSeverity().getLabel() + "!";
            notifyIncidentUpdated(incident);
        }

        if (hasOnScene) {
            double workRate = 0.0;
            for (Responder responder : incident.getAssignedResponders()) {
                if (responder.getStatus() == ResponderStatus.BUSY) {
                    workRate += responder.calculateEffectiveness(incident);
                }
            }
            if (!incident.isFullyStaffed()) {
                workRate *= 0.55;
            }
            incident.addHandlingProgress(workRate * delta);
            if (incident.isHandlingComplete()) {
                operator.reduceStress(7.5);
                lastStatusBanner = "RESOLVED: " + incident.getType().getLabel() + " at " + incident.getZone().getName() + "!";
                dispatchCenter.resolveIncident(incident, true);
                SoundSynthesizer.playResolvedFanfare();
                notifyIncidentResolved(incident, true);
                notifyRadioTransmission("DISPATCH BASE",
                        incident.getType().getLabel() + " at " + incident.getZone().getShortCode() + " resolved! Lives saved!");
                selectNextUrgentIncident();
                return;
            }
        }

        if (incident.getRemainingDeadline() <= 0.0) {
            operator.addStress(15.0);
            lastStatusBanner = "MISSED DEADLINE: Casualty reported at " + incident.getZone().getName() + "!";
            dispatchCenter.resolveIncident(incident, false);
            SoundSynthesizer.playRejectBuzz();
            notifyIncidentResolved(incident, false);
            notifyRadioTransmission("EMERGENCY ALERT",
                    "Deadline expired at " + incident.getZone().getShortCode() + " (" + incident.getType().getLabel() + ").");
            selectNextUrgentIncident();
        }
    }

    public String attemptSelectedDispatch() {
        if (operator.isRadioBusy()) {
            return "Radio is currently occupied (" + operator.getRadioActionText() + ")!";
        }
        if (selectedIncident == null) {
            selectNextUrgentIncident();
        }
        try {
            double eta = dispatchCenter.dispatch(selectedIncident, selectedResponder);
            String msg = String.format("Dispatched %s (%s) to %s [ETA: %.1fs]",
                    selectedResponder.getName(),
                    selectedResponder.getCallsign(),
                    selectedIncident.getZone().getShortCode(),
                    eta);
            lastStatusBanner = msg;
            SoundSynthesizer.playDispatchConfirm();
            notifyRadioTransmission("OPERATOR", msg);
            notifyResponderUpdated(selectedResponder);
            notifyIncidentUpdated(selectedIncident);
            if (!selectedIncident.getMissingResponderTypes().isEmpty()) {
                autoSelectBestResponder();
            }
            return msg;
        } catch (ResponderUnavailableException | InvalidDispatchException | NoRouteException ex) {
            operator.addStress(3.0);
            lastStatusBanner = "DISPATCH REJECTED: " + ex.getMessage();
            SoundSynthesizer.playRejectBuzz();
            notifyRadioTransmission("SYSTEM WARNING", ex.getMessage());
            return lastStatusBanner;
        }
    }

    public String attemptCalmSelectedCaller() {
        if (selectedIncident == null || selectedIncident.getStatus().isClosed()) {
            selectNextUrgentIncident();
        }
        if (selectedIncident == null) {
            return "No active caller to calm.";
        }
        if (operator.isRadioBusy()) {
            return "Radio busy: " + operator.getRadioActionText();
        }
        boolean calmed = operator.calmCaller(selectedIncident);
        if (calmed) {
            String msg = "Stay calm, " + selectedIncident.getCaller().getName()
                    + "! Emergency units are being routed to " + selectedIncident.getZone().getShortCode() + " right now!";
            lastStatusBanner = "Calmed caller " + selectedIncident.getCaller().getName() + " (-Panic, +Deadline)";
            SoundSynthesizer.playCalmChime();
            notifyRadioTransmission(operator.getName(), msg);
            notifyPanicChanged(selectedIncident, selectedIncident.getCaller().getPanicLevel());
            return lastStatusBanner;
        }
        return "Could not calm caller right now.";
    }

    public String attemptBreathingPause() {
        if (operator.isBreathingActive()) {
            operator.cancelBreathingPause();
            lastStatusBanner = "Breathing pause ended early.";
            return lastStatusBanner;
        }
        if (operator.startBreathingPause()) {
            lastStatusBanner = "Breathing Pause: Hold SPACE while circle expands (Inhale), release on Exhale.";
            SoundSynthesizer.playCalmChime();
            notifyRadioTransmission(operator.getName(), "[Taking a deep calming breath...]");
            return lastStatusBanner;
        }
        return "Already in breathing pause.";
    }

    public void selectNextUrgentIncident() {
        List<Incident> sorted = dispatchCenter.getSortedActiveIncidents();
        if (sorted.isEmpty()) {
            selectedIncident = null;
            return;
        }
        if (selectedIncident == null || !sorted.contains(selectedIncident)) {
            selectedIncident = sorted.get(0);
        } else {
            int idx = (sorted.indexOf(selectedIncident) + 1) % sorted.size();
            selectedIncident = sorted.get(idx);
        }
        if (selectedIncident != null) {
            autoSelectBestResponder();
            notifyRadioTransmission(selectedIncident.getCaller().getName(),
                    selectedIncident.getCaller().getDialogueLine(selectedIncident, operator.getStress()));
        }
    }

    public void addListener(GameListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notifyIncidentSpawned(Incident incident) {
        for (GameListener l : listeners) {
            l.onIncidentSpawned(incident);
        }
    }

    private void notifyIncidentUpdated(Incident incident) {
        for (GameListener l : listeners) {
            l.onIncidentUpdated(incident);
        }
    }

    private void notifyIncidentResolved(Incident incident, boolean saved) {
        for (GameListener l : listeners) {
            l.onIncidentResolved(incident, saved);
        }
    }

    private void notifyResponderUpdated(Responder responder) {
        for (GameListener l : listeners) {
            l.onResponderUpdated(responder);
        }
    }

    private void notifyStressChanged(double stress) {
        for (GameListener l : listeners) {
            l.onStressChanged(stress);
        }
    }

    private void notifyPanicChanged(Incident incident, double panic) {
        for (GameListener l : listeners) {
            l.onPanicChanged(incident, panic);
        }
    }

    private void notifyAftershock(String message, double intensity) {
        for (GameListener l : listeners) {
            l.onAftershockTriggered(message, intensity);
        }
    }

    private void notifyRadioTransmission(String speaker, String message) {
        String entry = "[" + speaker + "] " + message;
        radioHistoryLog.add(entry);
        while (radioHistoryLog.size() > 5) {
            radioHistoryLog.remove(0);
        }
        for (GameListener l : listeners) {
            l.onRadioTransmission(speaker, message);
        }
    }

    public void setStage(GameStage newStage) {
        this.stage = newStage;
        for (GameListener l : listeners) {
            l.onStageChanged(newStage);
        }
    }

    @Override
    public String serialize() {
        return "HIGH_SCORE=" + highScore + "\n"
                + "HIGHEST_SHIFT=" + highestUnlockedShift + "\n"
                + "DIFFICULTY=" + difficulty.name() + "\n"
                + "BEST_RATING=" + bestRating + "\n";
    }

    @Override
    public void deserialize(String data) {
        if (data == null || data.trim().isEmpty()) {
            return;
        }
        String[] lines = data.split("\\r?\\n");
        for (String line : lines) {
            String[] parts = line.split("=", 2);
            if (parts.length != 2) {
                continue;
            }
            String key = parts[0].trim();
            String val = parts[1].trim();
            try {
                if ("HIGH_SCORE".equals(key)) {
                    this.highScore = Math.max(0, Integer.parseInt(val));
                } else if ("HIGHEST_SHIFT".equals(key)) {
                    this.highestUnlockedShift = Math.max(1, Math.min(GameConfig.TOTAL_SHIFTS, Integer.parseInt(val)));
                } else if ("DIFFICULTY".equals(key)) {
                    this.difficulty = Difficulty.fromName(val);
                } else if ("BEST_RATING".equals(key)) {
                    this.bestRating = val;
                }
            } catch (NumberFormatException ignored) {
            }
        }
    }

    public Barangay getBarangay() {
        return barangay;
    }

    public Operator getOperator() {
        return operator;
    }

    public DispatchCenter getDispatchCenter() {
        return dispatchCenter;
    }

    public RoutePlanner getRoutePlanner() {
        return routePlanner;
    }

    public ShiftController getShiftController() {
        return shiftController;
    }

    public ScoreCalculator getScoreCalculator() {
        return scoreCalculator;
    }

    public GameStage getStage() {
        return stage;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
    }

    public Incident getSelectedIncident() {
        return selectedIncident;
    }

    public void setSelectedIncident(Incident selectedIncident) {
        this.selectedIncident = selectedIncident;
        if (selectedIncident != null) {
            notifyRadioTransmission(selectedIncident.getCaller().getName(),
                    selectedIncident.getCaller().getDialogueLine(selectedIncident, operator.getStress()));
        }
    }

    public Responder getSelectedResponder() {
        return selectedResponder;
    }

    public void setSelectedResponder(Responder selectedResponder) {
        this.selectedResponder = selectedResponder;
    }

    public boolean isPaused() {
        return paused;
    }

    public double getTimeScale() {
        return timeScale;
    }

    public List<String> getRadioHistoryLog() {
        return Collections.unmodifiableList(radioHistoryLog);
    }

    public int getHighScore() {
        return highScore;
    }

    public int getHighestUnlockedShift() {
        return highestUnlockedShift;
    }

    public String getBestRating() {
        return bestRating;
    }

    public String getLastStatusBanner() {
        return lastStatusBanner;
    }
}
