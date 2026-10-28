package ui;

import engine.GameManager;
import interfaces.Drawable;
import interfaces.Updatable;
import model.Incident;
import model.Operator;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

public class RadioPanel implements Drawable, Updatable {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final WaveformView waveformView;
    private final StressMeterView stressMeterView;
    private final Rectangle dispatchButtonBounds;
    private final Rectangle autoMatchButtonBounds;
    private final Rectangle calmButtonBounds;
    private final Rectangle breatheButtonBounds;
    private final Rectangle restUnitButtonBounds;

    public RadioPanel(int x, int y, int width, int height, Operator operator) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.waveformView = new WaveformView(x + 14, y + 14, 195, height - 28);
        this.stressMeterView = new StressMeterView(operator.getStressMeter(), x + width - 320, y + 14, 306, height - 28);

        int btnX = x + 590;
        int btnY = y + 14;
        int btnW = 112;
        int btnH = 40;
        int gap = 8;
        this.dispatchButtonBounds = new Rectangle(btnX, btnY, btnW, btnH);
        this.autoMatchButtonBounds = new Rectangle(btnX + btnW + gap, btnY, btnW, btnH);
        this.calmButtonBounds = new Rectangle(btnX + (btnW + gap) * 2, btnY, btnW, btnH);

        int row2Y = btnY + btnH + 10;
        int wideW = (btnW * 3 + gap * 2 - gap) / 2;
        this.breatheButtonBounds = new Rectangle(btnX, row2Y, wideW, 38);
        this.restUnitButtonBounds = new Rectangle(btnX + wideW + gap, row2Y, wideW, 38);
    }

    @Override
    public void update(double delta) {
        GameManager gm = GameManager.getInstance();
        Incident selected = gm.getSelectedIncident();
        Operator op = gm.getOperator();
        boolean speaking = selected != null && !selected.getStatus().isClosed();
        double panic = speaking ? selected.getCaller().getPanicLevel() : 15.0;
        double stress = op != null ? op.getStress() : 10.0;
        waveformView.setSignalState(speaking, panic, stress);
        waveformView.update(delta);
        stressMeterView.update(delta);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(GameConfig.BG_PANEL);
        g2d.fillRect(x, y, width, height);

        g2d.setColor(GameConfig.BORDER_SUBTLE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawLine(x, y, x + width, y);

        waveformView.draw(g2d);

        int transX = x + 220;
        int transY = y + 14;
        int transW = 358;
        int transH = height - 28;

        g2d.setColor(new Color(12, 22, 34));
        g2d.fillRoundRect(transX, transY, transW, transH, 10, 10);
        g2d.setColor(GameConfig.BORDER_SUBTLE);
        g2d.drawRoundRect(transX, transY, transW, transH, 10, 10);

        GameManager gm = GameManager.getInstance();
        Incident selected = gm.getSelectedIncident();
        Operator op = gm.getOperator();

        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        if (selected != null && !selected.getStatus().isClosed()) {
            String callerLine = selected.getCaller().getRoleLabel() + ": " + selected.getCaller().getName()
                    + "  [" + selected.getCaller().getMood().getLabel() + "]";
            g2d.drawString(callerLine, transX + 10, transY + 18);

            g2d.setFont(new Font("SansSerif", Font.ITALIC, 11));
            g2d.setColor(GameConfig.TEXT_PRIMARY);
            double stressVal = op != null ? op.getStress() : 10.0;
            String speech = selected.getCaller().getDialogueLine(selected, stressVal);
            drawWrappedText(g2d, speech, transX + 10, transY + 36, transW - 20, 14);
        } else {
            g2d.drawString("VHF EMERGENCY DISPATCH LOG", transX + 10, transY + 18);
            g2d.setFont(GameConfig.FONT_SMALL);
            g2d.setColor(GameConfig.TEXT_MUTED);
            List<String> history = gm.getRadioHistoryLog();
            int lineY = transY + 36;
            for (int i = Math.max(0, history.size() - 3); i < history.size(); i++) {
                String entry = history.get(i);
                if (entry.length() > 54) {
                    entry = entry.substring(0, 52) + "...";
                }
                g2d.drawString("• " + entry, transX + 10, lineY);
                lineY += 15;
            }
        }

        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.setColor(GameConfig.JEEPNEY_TEAL);
        String feedback = gm.getLastStatusBanner();
        if (feedback.length() > 56) {
            feedback = feedback.substring(0, 54) + "...";
        }
        g2d.drawString(feedback, transX + 10, transY + transH - 8);

        boolean radioLocked = op != null && op.isRadioBusy();
        String calmLabel = radioLocked ? String.format("BUSY %.1fs", op.getRadioBusyRemaining()) : "CALM CALL [C]";

        drawActionButton(g2d, dispatchButtonBounds, "DISPATCH [D]", GameConfig.JEEPNEY_GREEN, true);
        drawActionButton(g2d, autoMatchButtonBounds, "AUTO-MATCH [A]", GameConfig.JEEPNEY_TEAL, true);
        drawActionButton(g2d, calmButtonBounds, calmLabel, GameConfig.LAMP_YELLOW, !radioLocked);
        drawActionButton(g2d, breatheButtonBounds, "BREATHING PAUSE [SPACE]", new Color(138, 201, 38), true);
        drawActionButton(g2d, restUnitButtonBounds, "REST / WAKE UNIT [R]", GameConfig.JEEPNEY_ORANGE, gm.getSelectedResponder() != null);

        stressMeterView.draw(g2d);
    }

    private void drawActionButton(Graphics2D g2d, Rectangle rect, String text, Color accent, boolean enabled) {
        Color fill = enabled
                ? new Color(accent.getRed() / 4, accent.getGreen() / 4, accent.getBlue() / 4, 220)
                : new Color(35, 45, 58);
        g2d.setColor(fill);
        g2d.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

        g2d.setColor(enabled ? accent : new Color(90, 102, 118));
        g2d.setStroke(new BasicStroke(1.8f));
        g2d.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 10, 10);

        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.setColor(enabled ? Color.WHITE : GameConfig.TEXT_MUTED);
        int tw = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, rect.x + (rect.width - tw) / 2, rect.y + (rect.height + 5) / 2);
    }

    private void drawWrappedText(Graphics2D g2d, String text, int startX, int startY, int maxWidth, int lineHeight) {
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = startY;
        int linesDrawn = 0;
        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (g2d.getFontMetrics().stringWidth(candidate) > maxWidth && line.length() > 0) {
                g2d.drawString(line.toString(), startX, curY);
                line = new StringBuilder(word);
                curY += lineHeight;
                linesDrawn++;
                if (linesDrawn >= 2) {
                    break;
                }
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0 && linesDrawn < 3) {
            g2d.drawString(line.toString(), startX, curY);
        }
    }

    public boolean isDispatchClicked(int px, int py) {
        return dispatchButtonBounds.contains(px, py);
    }

    public boolean isAutoMatchClicked(int px, int py) {
        return autoMatchButtonBounds.contains(px, py);
    }

    public boolean isCalmClicked(int px, int py) {
        return calmButtonBounds.contains(px, py);
    }

    public boolean isBreatheClicked(int px, int py) {
        return breatheButtonBounds.contains(px, py);
    }

    public boolean isRestUnitClicked(int px, int py) {
        return restUnitButtonBounds.contains(px, py);
    }
}
