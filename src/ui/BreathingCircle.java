package ui;

import interfaces.Drawable;
import interfaces.Updatable;
import model.Operator;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class BreathingCircle implements Drawable, Updatable {
    private final Operator operator;
    private double currentRadius;

    public BreathingCircle(Operator operator) {
        this.operator = operator;
        this.currentRadius = 44.0;
    }

    @Override
    public void update(double delta) {
        if (operator == null || !operator.isBreathingActive()) {
            return;
        }
        double progress = operator.getBreathingPhaseProgress();
        this.currentRadius = 42.0 + progress * 48.0;
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (operator == null || !operator.isBreathingActive()) {
            return;
        }
        int boxW = 430;
        int boxH = 260;
        int bx = (GameConfig.WINDOW_WIDTH - boxW) / 2;
        int by = (GameConfig.WINDOW_HEIGHT - boxH) / 2 - 20;

        g2d.setColor(new Color(10, 19, 31, 238));
        g2d.fillRoundRect(bx, by, boxW, boxH, 20, 20);
        g2d.setColor(GameConfig.JEEPNEY_TEAL);
        g2d.setStroke(new BasicStroke(2.2f));
        g2d.drawRoundRect(bx, by, boxW, boxH, 20, 20);

        g2d.setFont(GameConfig.FONT_SUBTITLE);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        String title = "MINDFUL BREATHING PAUSE (9s)";
        int tw = g2d.getFontMetrics().stringWidth(title);
        g2d.drawString(title, bx + (boxW - tw) / 2, by + 30);

        int cx = bx + boxW / 2;
        int cy = by + 132;
        int r = (int) Math.round(currentRadius);

        boolean inSync = operator.isBreathingSynced();
        Color ringColor = inSync ? GameConfig.JEEPNEY_GREEN : GameConfig.LAMP_YELLOW;

        g2d.setColor(new Color(ringColor.getRed(), ringColor.getGreen(), ringColor.getBlue(), 55));
        g2d.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2d.setColor(ringColor);
        g2d.setStroke(new BasicStroke(3.5f));
        g2d.drawOval(cx - r, cy - r, r * 2, r * 2);

        g2d.setFont(GameConfig.FONT_BODY_BOLD);
        g2d.setColor(Color.WHITE);
        String cue = operator.isInhaleTargetPhase() ? "INHALE - HOLD [SPACE]" : "EXHALE - RELEASE [SPACE]";
        int cw = g2d.getFontMetrics().stringWidth(cue);
        g2d.drawString(cue, cx - cw / 2, cy + 5);

        g2d.setFont(GameConfig.FONT_SMALL);
        g2d.setColor(GameConfig.TEXT_MUTED);
        double rem = Math.max(0.0, operator.getBreathingTimeRemaining());
        String footer = String.format("Remaining: %.1fs   |   Press [ESC] to return early", rem);
        int fw = g2d.getFontMetrics().stringWidth(footer);
        g2d.drawString(footer, bx + (boxW - fw) / 2, by + boxH - 18);
    }

    public boolean isActive() {
        return operator != null && operator.isBreathingActive();
    }

    public void setSpaceHeld(boolean spaceHeld) {
        if (operator != null) {
            operator.setInhalingInput(spaceHeld);
        }
    }
}
