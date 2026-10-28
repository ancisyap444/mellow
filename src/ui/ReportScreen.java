package ui;

import engine.GameManager;
import engine.ScoreCalculator;
import enums.Difficulty;
import enums.GameStage;
import interfaces.Drawable;
import interfaces.Updatable;
import model.Incident;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

public class ReportScreen implements Drawable, Updatable {
    private final Rectangle primaryButtonBounds;
    private final Rectangle saveButtonBounds;
    private final Rectangle difficultyButtonBounds;

    public ReportScreen() {
        int centerX = GameConfig.WINDOW_WIDTH / 2;
        this.primaryButtonBounds = new Rectangle(centerX - 180, 590, 210, 44);
        this.saveButtonBounds = new Rectangle(centerX + 45, 590, 150, 44);
        this.difficultyButtonBounds = new Rectangle(centerX - 155, 525, 310, 38);
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public void draw(Graphics2D g2d) {
        GameManager gm = GameManager.getInstance();
        GameStage stage = gm.getStage();
        if (stage == GameStage.TITLE || stage == GameStage.SHIFT_INTRO) {
            drawTitleOrIntroOverlay(g2d, gm, stage == GameStage.SHIFT_INTRO);
        } else if (stage == GameStage.SHIFT_REPORT || stage == GameStage.FINAL_REPORT) {
            drawReportOverlay(g2d, gm, stage == GameStage.FINAL_REPORT);
        }
    }

    private void drawTitleOrIntroOverlay(Graphics2D g2d, GameManager gm, boolean isShiftIntro) {
        g2d.setColor(new Color(8, 15, 26, 232));
        g2d.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        int cardW = 820;
        int cardH = 540;
        int cx = (GameConfig.WINDOW_WIDTH - cardW) / 2;
        int cy = (GameConfig.WINDOW_HEIGHT - cardH) / 2;

        g2d.setColor(new Color(18, 32, 52));
        g2d.fillRoundRect(cx, cy, cardW, cardH, 22, 22);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        g2d.setStroke(new BasicStroke(2.4f));
        g2d.drawRoundRect(cx, cy, cardW, cardH, 22, 22);

        g2d.setFont(new Font("SansSerif", Font.BOLD, 32));
        g2d.setColor(GameConfig.LAMP_YELLOW);
        String title = isShiftIntro
                ? gm.getShiftController().getShiftTitle().toUpperCase()
                : "MELLOW: EMERGENCY DISPATCH";
        int tw = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, cx + (cardW - tw) / 2, cy + 56);

        g2d.setFont(GameConfig.FONT_SUBTITLE);
        g2d.setColor(GameConfig.JEEPNEY_TEAL);
        String sub = isShiftIntro
                ? gm.getShiftController().getShiftSubtitle()
                : "San Isidro Valley Emergency Hotline Simulator  |  CIS 2103 OOP Project";
        int sw = g2d.getFontMetrics().stringWidth(sub);
        g2d.drawString(sub, cx + (cardW - sw) / 2, cy + 88);

        g2d.setFont(GameConfig.FONT_BODY);
        g2d.setColor(GameConfig.TEXT_PRIMARY);
        String[] instructions = {
                "• MISSION: Coordinate Medics, Rescuers, Firefighters, Engineers, and Moto-Transports across 6 Districts.",
                "• 3 REAL-TIME SHIFTS: Shift 1 (Mainshock Night), Shift 2 (Aftershocks & Roadblocks), Shift 3 (Recovery Day).",
                "• ROUTE PREVIEW & AUTO-MATCH: Select an incident and press [A] to auto-pick the best unit with live ETA.",
                "• CALM & BREATHE: Use 'Calm Call [C]' to lower caller panic & extend deadlines, or '[SPACE]' for Breathing Pause.",
                "• UNIT FATIGUE: Rotate tired units or press [R] to manually rest/wake a responder before exhaustion hits 82%.",
                "• SHORTCUTS: [N] Next Call  [A] Auto-Match  [D] Dispatch  [C] Calm  [R] Rest  [Tab] 1x/2x Speed  [P] Pause  [M] Mute"
        };
        int iy = cy + 132;
        for (String line : instructions) {
            g2d.drawString(line, cx + 44, iy);
            iy += 28;
        }

        g2d.setFont(GameConfig.FONT_SUBTITLE);
        g2d.setColor(GameConfig.JEEPNEY_GREEN);
        String hs = "ALL-TIME HIGH SCORE: " + gm.getHighScore() + " PTS   |   BEST GRADE: " + gm.getBestRating();
        int hsw = g2d.getFontMetrics().stringWidth(hs);
        g2d.drawString(hs, cx + (cardW - hsw) / 2, cy + 320);

        Difficulty diff = gm.getDifficulty();
        drawButton(g2d, difficultyButtonBounds, "DIFFICULTY: " + diff.getLabel().toUpperCase() + " (CLICK TO CYCLE)", GameConfig.JEEPNEY_TEAL);
        g2d.setFont(GameConfig.FONT_SMALL);
        g2d.setColor(GameConfig.TEXT_MUTED);
        int ddw = g2d.getFontMetrics().stringWidth(diff.getDescription());
        g2d.drawString(diff.getDescription(), cx + (cardW - ddw) / 2, difficultyButtonBounds.y + 54);

        String startText = isShiftIntro ? "BEGIN SHIFT DISPATCH" : "START CAMPAIGN";
        drawButton(g2d, primaryButtonBounds, startText, GameConfig.JEEPNEY_GREEN);
        drawButton(g2d, saveButtonBounds, "SAVE PROGRESS", GameConfig.LAMP_YELLOW);
    }

    private void drawReportOverlay(Graphics2D g2d, GameManager gm, boolean finalReport) {
        g2d.setColor(new Color(8, 15, 26, 236));
        g2d.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        int cardW = 880;
        int cardH = 560;
        int cx = (GameConfig.WINDOW_WIDTH - cardW) / 2;
        int cy = (GameConfig.WINDOW_HEIGHT - cardH) / 2;

        g2d.setColor(new Color(18, 32, 52));
        g2d.fillRoundRect(cx, cy, cardW, cardH, 22, 22);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        g2d.setStroke(new BasicStroke(2.2f));
        g2d.drawRoundRect(cx, cy, cardW, cardH, 22, 22);

        ScoreCalculator sc = gm.getScoreCalculator();
        int totalScore = sc.calculateTotalScore(gm.getDispatchCenter(), gm.getOperator());
        String rating = sc.calculateRatingGrade(gm.getDispatchCenter(), gm.getOperator());

        g2d.setFont(GameConfig.FONT_TITLE);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        String header = finalReport
                ? "FINAL POST-DISASTER OPERATIONS REPORT"
                : "END OF SHIFT " + gm.getShiftController().getCurrentShift() + " DEBRIEFING";
        g2d.drawString(header, cx + 32, cy + 44);

        g2d.setFont(GameConfig.FONT_SUBTITLE);
        g2d.setColor(GameConfig.JEEPNEY_GREEN);
        g2d.drawString("RATING: " + rating + "   (" + totalScore + " PTS | HIGH: " + gm.getHighScore() + ")", cx + 32, cy + 76);

        g2d.setFont(GameConfig.FONT_BODY);
        g2d.setColor(GameConfig.TEXT_PRIMARY);
        String statsLine = String.format(
                "Lives Saved: %d   |   Casualties: %d   |   Avg Response: %.1fs   |   Efficiency: %.0f%%   |   Operator Calm: %.0f%%",
                gm.getDispatchCenter().getLivesSavedTotal(),
                gm.getDispatchCenter().getCasualtiesTotal(),
                gm.getDispatchCenter().getAverageResponseTime(),
                gm.getDispatchCenter().getResourceEfficiencyPercent(),
                100.0 - gm.getOperator().getStress()
        );
        g2d.drawString(statsLine, cx + 32, cy + 106);

        g2d.setFont(GameConfig.FONT_BODY_BOLD);
        g2d.setColor(GameConfig.JEEPNEY_TEAL);
        g2d.drawString("INCIDENT OUTCOME LOG:", cx + 32, cy + 138);

        g2d.setFont(GameConfig.FONT_MONO);
        g2d.setColor(GameConfig.TEXT_PRIMARY);
        List<Incident> history = gm.getDispatchCenter().getResolvedHistory();
        int rowY = cy + 160;
        int maxRows = Math.min(8, history.size());
        if (maxRows == 0) {
            g2d.drawString("No incidents logged during this shift window.", cx + 40, rowY);
        } else {
            for (int i = history.size() - maxRows; i < history.size(); i++) {
                Incident inc = history.get(i);
                String outcome = String.format(
                        "#%d %-20s | %-4s | %-11s | %s",
                        inc.getId(),
                        inc.getType().getLabel(),
                        inc.getZone().getShortCode(),
                        inc.getStatus().getLabel(),
                        inc.getOutcomeNote()
                );
                if (outcome.length() > 86) {
                    outcome = outcome.substring(0, 84) + "..";
                }
                g2d.drawString(outcome, cx + 40, rowY);
                rowY += 20;
            }
        }

        g2d.setFont(GameConfig.FONT_BODY_BOLD);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        g2d.drawString("COMMANDER COACHING & IMPROVEMENT TIPS:", cx + 32, cy + 345);

        g2d.setFont(GameConfig.FONT_SMALL);
        g2d.setColor(GameConfig.TEXT_PRIMARY);
        String[] tips = sc.buildImprovementAdvice(gm.getDispatchCenter(), gm.getOperator()).split("\n");
        int tipY = cy + 368;
        for (String tip : tips) {
            g2d.drawString(tip, cx + 40, tipY);
            tipY += 22;
        }

        String primaryLabel = finalReport ? "NEW CAMPAIGN" : "NEXT SHIFT >>";
        drawButton(g2d, primaryButtonBounds, primaryLabel, GameConfig.JEEPNEY_GREEN);
        drawButton(g2d, saveButtonBounds, "SAVE PROGRESS", GameConfig.LAMP_YELLOW);
    }

    private void drawButton(Graphics2D g2d, Rectangle rect, String label, Color accent) {
        g2d.setColor(new Color(accent.getRed() / 4, accent.getGreen() / 4, accent.getBlue() / 4, 230));
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 12, 12);
        g2d.setColor(accent);
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 12, 12);

        g2d.setFont(GameConfig.FONT_BODY_BOLD);
        g2d.setColor(Color.WHITE);
        int lw = g2d.getFontMetrics().stringWidth(label);
        g2d.drawString(label, rect.x + (rect.width - lw) / 2, rect.y + 27);
    }

    public boolean isPrimaryButtonClicked(int px, int py) {
        return primaryButtonBounds.contains(px, py);
    }

    public boolean isSaveButtonClicked(int px, int py) {
        return saveButtonBounds.contains(px, py);
    }

    public boolean isDifficultyButtonClicked(int px, int py) {
        GameStage stage = GameManager.getInstance().getStage();
        return difficultyButtonBounds.contains(px, py)
                && (stage == GameStage.TITLE || stage == GameStage.SHIFT_INTRO);
    }
}
