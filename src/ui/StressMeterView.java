package ui;

import interfaces.Drawable;
import interfaces.Updatable;
import model.StressMeter;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

public class StressMeterView implements Drawable, Updatable {
    private final StressMeter stressMeter;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private double displayedStress;
    private double pulsePhase;

    public StressMeterView(StressMeter stressMeter, int x, int y, int width, int height) {
        this.stressMeter = stressMeter;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.displayedStress = stressMeter != null ? stressMeter.getCurrentStress() : 0.0;
        this.pulsePhase = 0.0;
    }

    @Override
    public void update(double delta) {
        double target = stressMeter.getCurrentStress();
        displayedStress += (target - displayedStress) * Math.min(1.0, delta * 8.0);
        pulsePhase += delta * (displayedStress > 65.0 ? 8.0 : 2.5);
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(14, 24, 38));
        g2d.fillRoundRect(x, y, width, height, 12, 12);

        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.setColor(GameConfig.TEXT_PRIMARY);
        g2d.drawString("OPERATOR STRESS METER", x + 12, y + 20);

        String valText = String.format("%.0f%% (%s)", displayedStress, stressMeter.getStatusLabel());
        g2d.setColor(displayedStress >= 70 ? GameConfig.JEEPNEY_RED : GameConfig.LAMP_YELLOW);
        int vw = g2d.getFontMetrics().stringWidth(valText);
        g2d.drawString(valText, x + width - vw - 12, y + 20);

        int barX = x + 12;
        int barY = y + 30;
        int barW = width - 24;
        int barH = 18;

        g2d.setColor(new Color(28, 42, 62));
        g2d.fillRoundRect(barX, barY, barW, barH, 8, 8);

        int fillW = (int) Math.round((displayedStress / 100.0) * barW);
        if (fillW > 0) {
            Color start = GameConfig.JEEPNEY_GREEN;
            Color end = displayedStress > 65 ? GameConfig.JEEPNEY_RED : GameConfig.JEEPNEY_ORANGE;
            g2d.setPaint(new GradientPaint(barX, barY, start, barX + barW, barY, end));
            g2d.fillRoundRect(barX, barY, fillW, barH, 8, 8);
        }

        g2d.setColor(GameConfig.BORDER_SUBTLE);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(barX, barY, barW, barH, 8, 8);

        g2d.setFont(GameConfig.FONT_SMALL);
        String statusLine;
        if (displayedStress >= 75.0) {
            statusLine = "HIGH STRESS! Audio garbled. Press [SPACE] for Breathing Pause!";
            int glowAlpha = 140 + (int) (Math.sin(pulsePhase) * 70);
            g2d.setColor(new Color(228, 73, 73, Math.max(60, Math.min(240, glowAlpha))));
        } else if (displayedStress >= 48.0) {
            statusLine = "Moderate tension: Slight radio static & slower dispatch.";
            g2d.setColor(GameConfig.JEEPNEY_ORANGE);
        } else {
            statusLine = "Calm & Focused: Clear audio and optimal dispatch speed.";
            g2d.setColor(GameConfig.JEEPNEY_GREEN);
        }
        g2d.drawString(statusLine, x + 12, y + 68);

        g2d.setColor(GameConfig.BORDER_SUBTLE);
        g2d.drawRoundRect(x, y, width, height, 12, 12);
    }
}
