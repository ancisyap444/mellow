package ui;

import engine.DispatchCenter;
import engine.GameManager;
import enums.Difficulty;
import enums.GameStage;
import enums.ResponderStatus;
import interfaces.GameListener;
import model.Incident;
import model.Responder;
import util.GameConfig;
import util.SoundSynthesizer;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements GameListener {
    private static final int LEFT_PANEL_WIDTH = 320;
    private static final int RIGHT_PANEL_WIDTH = 320;
    private static final int CENTER_MAP_WIDTH = 640;

    private final MapView mapView;
    private RadioPanel radioPanel;
    private BreathingCircle breathingCircle;
    private final AftershockShake aftershockShake;
    private final ReportScreen reportScreen;
    private volatile List<CallCard> callCards;
    private volatile List<Rectangle> responderCardBounds;
    private final Rectangle speedButtonBounds;
    private final Rectangle pauseButtonBounds;
    private final Rectangle muteButtonBounds;
    private final Rectangle skipShiftButtonBounds;
    private final Timer gameLoopTimer;
    private long lastTickNanos;
    private double fadeAlpha;
    private double fadeDirection;

    public GamePanel() {
        setPreferredSize(new Dimension(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT));
        setBackground(GameConfig.BG_DEEP_NIGHT);
        setFocusable(true);

        GameManager gm = GameManager.getInstance();
        gm.addListener(this);

        int centerH = GameConfig.WINDOW_HEIGHT - GameConfig.TOP_BAR_HEIGHT - GameConfig.BOTTOM_BAR_HEIGHT;
        this.mapView = new MapView(
                LEFT_PANEL_WIDTH + 8,
                GameConfig.TOP_BAR_HEIGHT + 8,
                CENTER_MAP_WIDTH - 16,
                centerH - 16
        );
        this.radioPanel = new RadioPanel(
                0,
                GameConfig.WINDOW_HEIGHT - GameConfig.BOTTOM_BAR_HEIGHT,
                GameConfig.WINDOW_WIDTH,
                GameConfig.BOTTOM_BAR_HEIGHT,
                gm.getOperator()
        );
        this.breathingCircle = new BreathingCircle(gm.getOperator());
        this.aftershockShake = new AftershockShake();
        this.reportScreen = new ReportScreen();
        this.callCards = new ArrayList<>();
        this.responderCardBounds = new ArrayList<>();

        this.speedButtonBounds = new Rectangle(GameConfig.WINDOW_WIDTH - 405, 14, 86, 32);
        this.pauseButtonBounds = new Rectangle(GameConfig.WINDOW_WIDTH - 312, 14, 86, 32);
        this.muteButtonBounds = new Rectangle(GameConfig.WINDOW_WIDTH - 219, 14, 92, 32);
        this.skipShiftButtonBounds = new Rectangle(GameConfig.WINDOW_WIDTH - 120, 14, 106, 32);

        this.fadeAlpha = 0.0;
        this.fadeDirection = 0.0;

        setupInputHandlers();

        this.lastTickNanos = System.nanoTime();
        this.gameLoopTimer = new Timer(GameConfig.FRAME_DELAY_MS, e -> onFrameTick());
        this.gameLoopTimer.start();
    }

    private void setupInputHandlers() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                handleMouseClick(e.getX(), e.getY());
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPress(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    breathingCircle.setSpaceHeld(false);
                }
            }
        });
    }

    private void onFrameTick() {
        long now = System.nanoTime();
        double delta = (now - lastTickNanos) / 1_000_000_000.0;
        lastTickNanos = now;
        delta = Math.min(0.1, Math.max(0.001, delta));

        GameManager gm = GameManager.getInstance();
        gm.update(delta);
        mapView.update(delta);
        radioPanel.update(delta);
        breathingCircle.update(delta);
        aftershockShake.update(delta);
        reportScreen.update(delta);
        rebuildCallCards(delta);

        if (fadeDirection != 0.0) {
            fadeAlpha += fadeDirection * delta * 2.2;
            if (fadeAlpha >= 0.85) {
                fadeAlpha = 0.85;
                fadeDirection = -1.0;
            } else if (fadeAlpha <= 0.0) {
                fadeAlpha = 0.0;
                fadeDirection = 0.0;
            }
        }

        repaint();
    }

    private void rebuildCallCards(double delta) {
        GameManager gm = GameManager.getInstance();
        DispatchCenter dc = gm.getDispatchCenter();
        if (dc == null) {
            return;
        }
        List<Incident> sorted = dc.getSortedActiveIncidents();
        List<CallCard> nextCards = new ArrayList<>();
        int cardX = 12;
        int cardY = GameConfig.TOP_BAR_HEIGHT + 40;
        int cardW = LEFT_PANEL_WIDTH - 24;
        int cardH = 84;
        for (int i = 0; i < Math.min(5, sorted.size()); i++) {
            Incident inc = sorted.get(i);
            boolean sel = inc.equals(gm.getSelectedIncident());
            CallCard card = new CallCard(inc, cardX, cardY + i * (cardH + 8), cardW, cardH, sel);
            card.update(delta);
            nextCards.add(card);
        }
        this.callCards = nextCards;
    }

    private void handleMouseClick(int mx, int my) {
        GameManager gm = GameManager.getInstance();
        GameStage stage = gm.getStage();

        if (stage != GameStage.PLAYING) {
            if (reportScreen.isDifficultyButtonClicked(mx, my)) {
                Difficulty[] vals = Difficulty.values();
                Difficulty next = vals[(gm.getDifficulty().ordinal() + 1) % vals.length];
                gm.setDifficulty(next);
                SoundSynthesizer.playDispatchConfirm();
                return;
            }
            if (reportScreen.isPrimaryButtonClicked(mx, my)) {
                triggerFadeTransition();
                if (stage == GameStage.TITLE || stage == GameStage.FINAL_REPORT) {
                    gm.startNewCampaign(gm.getDifficulty());
                    gm.enterPlayingStage();
                    rebindSubViews();
                } else if (stage == GameStage.SHIFT_INTRO) {
                    gm.enterPlayingStage();
                    rebindSubViews();
                } else if (stage == GameStage.SHIFT_REPORT) {
                    gm.advanceAfterShiftReport();
                    if (gm.getStage() == GameStage.SHIFT_INTRO) {
                        gm.enterPlayingStage();
                    }
                    rebindSubViews();
                }
            } else if (reportScreen.isSaveButtonClicked(mx, my)) {
                gm.saveProgress();
                SoundSynthesizer.playCalmChime();
            }
            return;
        }

        if (breathingCircle.isActive()) {
            return;
        }

        if (speedButtonBounds.contains(mx, my)) {
            gm.toggleTimeScale();
            return;
        }
        if (pauseButtonBounds.contains(mx, my)) {
            gm.togglePause();
            return;
        }
        if (muteButtonBounds.contains(mx, my)) {
            SoundSynthesizer.toggleMute();
            return;
        }
        if (skipShiftButtonBounds.contains(mx, my)) {
            triggerFadeTransition();
            gm.skipToShiftEndForTesting();
            return;
        }

        if (radioPanel.isDispatchClicked(mx, my)) {
            gm.attemptSelectedDispatch();
            return;
        }
        if (radioPanel.isAutoMatchClicked(mx, my)) {
            gm.autoSelectBestResponder();
            SoundSynthesizer.playDispatchConfirm();
            return;
        }
        if (radioPanel.isCalmClicked(mx, my)) {
            gm.attemptCalmSelectedCaller();
            return;
        }
        if (radioPanel.isBreatheClicked(mx, my)) {
            gm.attemptBreathingPause();
            return;
        }
        if (radioPanel.isRestUnitClicked(mx, my)) {
            gm.toggleSelectedResponderRest();
            return;
        }

        for (CallCard card : callCards) {
            if (card.containsPoint(mx, my)) {
                gm.setSelectedIncident(card.getIncident());
                return;
            }
        }

        Incident clickedMapIncident = mapView.findIncidentAt(mx, my);
        if (clickedMapIncident != null) {
            gm.setSelectedIncident(clickedMapIncident);
            return;
        }

        List<Responder> responders = gm.getDispatchCenter().getResponders();
        List<Rectangle> boundsSnapshot = this.responderCardBounds;
        for (int i = 0; i < Math.min(boundsSnapshot.size(), responders.size()); i++) {
            if (boundsSnapshot.get(i).contains(mx, my)) {
                gm.setSelectedResponder(responders.get(i));
                return;
            }
        }
    }

    private void handleKeyPress(KeyEvent e) {
        GameManager gm = GameManager.getInstance();
        int code = e.getKeyCode();

        if (code == KeyEvent.VK_ESCAPE && breathingCircle.isActive()) {
            gm.getOperator().cancelBreathingPause();
            return;
        }

        if (code == KeyEvent.VK_SPACE) {
            if (!breathingCircle.isActive() && gm.getStage() == GameStage.PLAYING) {
                gm.attemptBreathingPause();
            }
            breathingCircle.setSpaceHeld(true);
            return;
        }

        if (breathingCircle.isActive()) {
            return;
        }

        if (code == KeyEvent.VK_N) {
            gm.selectNextUrgentIncident();
        } else if (code == KeyEvent.VK_A) {
            gm.autoSelectBestResponder();
            SoundSynthesizer.playDispatchConfirm();
        } else if (code == KeyEvent.VK_D) {
            gm.attemptSelectedDispatch();
        } else if (code == KeyEvent.VK_C) {
            gm.attemptCalmSelectedCaller();
        } else if (code == KeyEvent.VK_R) {
            gm.toggleSelectedResponderRest();
        } else if (code == KeyEvent.VK_P) {
            gm.togglePause();
        } else if (code == KeyEvent.VK_TAB) {
            gm.toggleTimeScale();
        } else if (code == KeyEvent.VK_M) {
            SoundSynthesizer.toggleMute();
        } else if (code == KeyEvent.VK_F5) {
            triggerFadeTransition();
            gm.skipToShiftEndForTesting();
        }
    }

    private void rebindSubViews() {
        GameManager gm = GameManager.getInstance();
        mapView.rebuildStaticViews();
        this.radioPanel = new RadioPanel(
                0,
                GameConfig.WINDOW_HEIGHT - GameConfig.BOTTOM_BAR_HEIGHT,
                GameConfig.WINDOW_WIDTH,
                GameConfig.BOTTOM_BAR_HEIGHT,
                gm.getOperator()
        );
        this.breathingCircle = new BreathingCircle(gm.getOperator());
    }

    private void triggerFadeTransition() {
        this.fadeAlpha = 0.1;
        this.fadeDirection = 1.0;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (aftershockShake.isActive()) {
            g2d.translate(aftershockShake.getOffsetX(), aftershockShake.getOffsetY());
        }

        drawTopHeaderBar(g2d);
        drawLeftCallQueuePanel(g2d);
        mapView.draw(g2d);
        drawRightResponderRoster(g2d);
        radioPanel.draw(g2d);
        aftershockShake.draw(g2d);
        breathingCircle.draw(g2d);

        GameStage stage = GameManager.getInstance().getStage();
        if (stage != GameStage.PLAYING) {
            reportScreen.draw(g2d);
        } else if (GameManager.getInstance().isPaused()) {
            drawPauseBanner(g2d);
        }

        if (fadeAlpha > 0.01) {
            int alpha = (int) Math.min(240, fadeAlpha * 255);
            g2d.setColor(new Color(6, 12, 22, alpha));
            g2d.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        }

        g2d.dispose();
    }

    private void drawTopHeaderBar(Graphics2D g2d) {
        GameManager gm = GameManager.getInstance();
        g2d.setColor(GameConfig.BG_PANEL);
        g2d.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.TOP_BAR_HEIGHT);

        g2d.setColor(GameConfig.BORDER_SUBTLE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(0, GameConfig.TOP_BAR_HEIGHT, GameConfig.WINDOW_WIDTH, GameConfig.TOP_BAR_HEIGHT);

        g2d.setFont(GameConfig.FONT_SUBTITLE);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        String shiftTitle = gm.getShiftController().getShiftTitle();
        g2d.drawString("MELLOW  |  " + shiftTitle, 16, 25);

        g2d.setFont(GameConfig.FONT_SMALL);
        g2d.setColor(GameConfig.TEXT_MUTED);
        g2d.drawString(gm.getShiftController().getShiftSubtitle() + "  [" + gm.getDifficulty().getLabel() + "]", 16, 44);

        int timerX = 485;
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.setColor(GameConfig.JEEPNEY_TEAL);
        g2d.drawString("TIME " + gm.getShiftController().getFormattedRemainingTime(), timerX, 36);

        g2d.setFont(GameConfig.FONT_BODY_BOLD);
        g2d.setColor(GameConfig.JEEPNEY_GREEN);
        g2d.drawString("LIVES SAVED: " + gm.getDispatchCenter().getLivesSavedTotal(), 625, 26);

        g2d.setColor(GameConfig.JEEPNEY_RED);
        g2d.drawString("CASUALTIES: " + gm.getDispatchCenter().getCasualtiesTotal(), 625, 46);

        g2d.setColor(GameConfig.LAMP_YELLOW);
        g2d.drawString("HIGH: " + gm.getHighScore(), 770, 36);

        boolean fast = gm.getTimeScale() > 1.2;
        drawHeaderButton(g2d, speedButtonBounds, fast ? "SPEED: 2x" : "SPEED: 1x", fast ? GameConfig.LAMP_YELLOW : GameConfig.JEEPNEY_TEAL);
        drawHeaderButton(g2d, pauseButtonBounds, gm.isPaused() ? "RESUME [P]" : "PAUSE [P]", gm.isPaused() ? GameConfig.JEEPNEY_GREEN : GameConfig.TEXT_PRIMARY);
        drawHeaderButton(g2d, muteButtonBounds, SoundSynthesizer.isMuted() ? "AUDIO: OFF" : "AUDIO: ON", SoundSynthesizer.isMuted() ? GameConfig.TEXT_MUTED : GameConfig.JEEPNEY_GREEN);
        drawHeaderButton(g2d, skipShiftButtonBounds, "END SHIFT [F5]", GameConfig.JEEPNEY_ORANGE);
    }

    private void drawHeaderButton(Graphics2D g2d, Rectangle rect, String label, Color accent) {
        g2d.setColor(new Color(22, 38, 60));
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);
        g2d.setColor(accent);
        g2d.setStroke(new BasicStroke(1.3f));
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 8, 8);

        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.setColor(accent);
        int w = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, rect.x + (rect.width - w) / 2, rect.y + 20);
    }

    private void drawPauseBanner(Graphics2D g2d) {
        int bw = 390;
        int bh = 74;
        int bx = (GameConfig.WINDOW_WIDTH - bw) / 2;
        int by = (GameConfig.WINDOW_HEIGHT - bh) / 2 - 30;
        g2d.setColor(new Color(10, 18, 30, 225));
        g2d.fillRoundRect(bx, by, bw, bh, 16, 16);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(bx, by, bw, bh, 16, 16);

        g2d.setFont(GameConfig.FONT_SUBTITLE);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        String t1 = "TACTICAL PAUSE ACTIVE";
        g2d.drawString(t1, bx + (bw - g2d.getFontMetrics().stringWidth(t1)) / 2, by + 32);

        g2d.setFont(GameConfig.FONT_SMALL);
        g2d.setColor(Color.WHITE);
        String t2 = "You can still inspect calls & plan dispatches. Press [P] to resume.";
        g2d.drawString(t2, bx + (bw - g2d.getFontMetrics().stringWidth(t2)) / 2, by + 54);
    }

    private void drawLeftCallQueuePanel(Graphics2D g2d) {
        int panelH = GameConfig.WINDOW_HEIGHT - GameConfig.TOP_BAR_HEIGHT - GameConfig.BOTTOM_BAR_HEIGHT;
        g2d.setColor(GameConfig.BG_PANEL);
        g2d.fillRect(0, GameConfig.TOP_BAR_HEIGHT, LEFT_PANEL_WIDTH, panelH);

        g2d.setColor(GameConfig.BORDER_SUBTLE);
        g2d.drawLine(LEFT_PANEL_WIDTH, GameConfig.TOP_BAR_HEIGHT, LEFT_PANEL_WIDTH, GameConfig.TOP_BAR_HEIGHT + panelH);

        g2d.setFont(GameConfig.FONT_BODY_BOLD);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        g2d.drawString("INCOMING CALL QUEUE (BY URGENCY)", 14, GameConfig.TOP_BAR_HEIGHT + 24);

        List<CallCard> snapshot = this.callCards;
        if (snapshot.isEmpty()) {
            g2d.setFont(GameConfig.FONT_SMALL);
            g2d.setColor(GameConfig.TEXT_MUTED);
            g2d.drawString("Monitoring radio... No active emergency calls.", 16, GameConfig.TOP_BAR_HEIGHT + 65);
        } else {
            for (CallCard card : snapshot) {
                card.draw(g2d);
            }
        }
    }

    private void drawRightResponderRoster(Graphics2D g2d) {
        int rx = GameConfig.WINDOW_WIDTH - RIGHT_PANEL_WIDTH;
        int ry = GameConfig.TOP_BAR_HEIGHT;
        int panelH = GameConfig.WINDOW_HEIGHT - GameConfig.TOP_BAR_HEIGHT - GameConfig.BOTTOM_BAR_HEIGHT;

        g2d.setColor(GameConfig.BG_PANEL);
        g2d.fillRect(rx, ry, RIGHT_PANEL_WIDTH, panelH);

        g2d.setColor(GameConfig.BORDER_SUBTLE);
        g2d.drawLine(rx, ry, rx, ry + panelH);

        g2d.setFont(GameConfig.FONT_BODY_BOLD);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        g2d.drawString("RESPONDER ROSTER & FATIGUE", rx + 14, ry + 24);

        GameManager gm = GameManager.getInstance();
        List<Responder> responders = gm.getDispatchCenter().getResponders();
        List<Rectangle> nextBounds = new ArrayList<>();

        int cardX = rx + 12;
        int cardY = ry + 36;
        int cardW = RIGHT_PANEL_WIDTH - 24;
        int cardH = 58;

        for (int i = 0; i < responders.size(); i++) {
            Responder r = responders.get(i);
            int cy = cardY + i * (cardH + 5);
            Rectangle bounds = new Rectangle(cardX, cy, cardW, cardH);
            nextBounds.add(bounds);

            boolean selected = r.equals(gm.getSelectedResponder());
            g2d.setColor(selected ? GameConfig.BG_CARD_SELECTED : GameConfig.BG_CARD);
            g2d.fillRoundRect(cardX, cy, cardW, cardH, 10, 10);

            Color typeColor = ResponderIcon.getTypeColor(r.getType());
            g2d.setColor(typeColor);
            g2d.fillRoundRect(cardX, cy, 6, cardH, 6, 6);

            g2d.setColor(selected ? GameConfig.LAMP_YELLOW : GameConfig.BORDER_SUBTLE);
            g2d.setStroke(new BasicStroke(selected ? 2.0f : 1.1f));
            g2d.drawRoundRect(cardX, cy, cardW, cardH, 10, 10);

            g2d.setFont(GameConfig.FONT_SMALL_BOLD);
            g2d.setColor(GameConfig.TEXT_PRIMARY);
            g2d.drawString(r.getCallsign() + " - " + r.getName() + " (" + r.getType().getLabel() + ")", cardX + 14, cy + 17);

            Color statusColor = getStatusColor(r.getStatus());
            g2d.setFont(GameConfig.FONT_SMALL_BOLD);
            g2d.setColor(statusColor);
            String statusStr = r.getStatus().getLabel().toUpperCase() + " @ " + r.getCurrentZone().getShortCode();
            g2d.drawString(statusStr, cardX + 14, cy + 32);

            Incident selInc = gm.getSelectedIncident();
            if (selInc != null && !selInc.getStatus().isClosed()) {
                int effPct = (int) Math.round(r.calculateEffectiveness(selInc) * 100);
                g2d.setFont(GameConfig.FONT_SMALL_BOLD);
                g2d.setColor(effPct >= 100 ? GameConfig.JEEPNEY_GREEN : GameConfig.TEXT_MUTED);
                String matchStr = "Match: " + effPct + "%";
                int mw = g2d.getFontMetrics().stringWidth(matchStr);
                g2d.drawString(matchStr, cardX + cardW - mw - 10, cy + 32);
            }

            int barX = cardX + 14;
            int barY = cy + 41;
            int barW = cardW - 95;
            int barH = 7;
            g2d.setColor(new Color(12, 20, 32));
            g2d.fillRoundRect(barX, barY, barW, barH, 4, 4);

            double fatRatio = r.getFatigue() / 100.0;
            Color fatColor = fatRatio > 0.75 ? GameConfig.JEEPNEY_RED
                    : (fatRatio > 0.45 ? GameConfig.JEEPNEY_ORANGE : GameConfig.JEEPNEY_GREEN);
            g2d.setColor(fatColor);
            g2d.fillRoundRect(barX, barY, (int) Math.round(barW * fatRatio), barH, 4, 4);

            g2d.setFont(GameConfig.FONT_SMALL);
            g2d.setColor(GameConfig.TEXT_MUTED);
            g2d.drawString("Fatigue " + (int) r.getFatigue() + "%", barX + barW + 8, barY + 7);
        }
        this.responderCardBounds = nextBounds;
    }

    private Color getStatusColor(ResponderStatus status) {
        if (status == ResponderStatus.AVAILABLE) {
            return GameConfig.JEEPNEY_GREEN;
        }
        if (status == ResponderStatus.EN_ROUTE) {
            return GameConfig.JEEPNEY_TEAL;
        }
        if (status == ResponderStatus.BUSY) {
            return GameConfig.LAMP_YELLOW;
        }
        return GameConfig.JEEPNEY_RED;
    }

    @Override
    public void onIncidentSpawned(Incident incident) {
    }

    @Override
    public void onIncidentUpdated(Incident incident) {
    }

    @Override
    public void onIncidentResolved(Incident incident, boolean saved) {
    }

    @Override
    public void onResponderUpdated(Responder responder) {
    }

    @Override
    public void onStressChanged(double newStress) {
    }

    @Override
    public void onPanicChanged(Incident incident, double newPanic) {
    }

    @Override
    public void onAftershockTriggered(String message, double intensity) {
        aftershockShake.trigger(2.6, intensity, message);
    }

    @Override
    public void onRadioTransmission(String speaker, String message) {
    }

    @Override
    public void onStageChanged(GameStage newStage) {
        triggerFadeTransition();
    }
}
