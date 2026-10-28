package ui;

import interfaces.Drawable;
import interfaces.Updatable;
import util.GameConfig;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class AftershockShake implements Drawable, Updatable {
    private double remainingSeconds;
    private double intensity;
    private int offsetX;
    private int offsetY;
    private String bannerMessage;
    private final Random random;

    public AftershockShake() {
        this.remainingSeconds = 0.0;
        this.intensity = 0.0;
        this.offsetX = 0;
        this.offsetY = 0;
        this.bannerMessage = "";
        this.random = new Random();
    }

    public void trigger(double durationSeconds, double intensity, String message) {
        this.remainingSeconds = Math.max(0.5, durationSeconds);
        this.intensity = Math.max(2.0, intensity * 8.0);
        this.bannerMessage = message == null ? "AFTERSHOCK DETECTED!" : message;
    }

    @Override
    public void update(double delta) {
        if (remainingSeconds > 0.0) {
            remainingSeconds -= delta;
            double decay = Math.max(0.0, remainingSeconds / 2.6);
            int maxShift = (int) Math.ceil(intensity * decay);
            if (maxShift > 0) {
                offsetX = random.nextInt(maxShift * 2 + 1) - maxShift;
                offsetY = random.nextInt(maxShift * 2 + 1) - maxShift;
            } else {
                offsetX = 0;
                offsetY = 0;
            }
            if (remainingSeconds <= 0.0) {
                remainingSeconds = 0.0;
                offsetX = 0;
                offsetY = 0;
            }
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (remainingSeconds <= 0.0) {
            return;
        }
        int alpha = (int) Math.min(180, remainingSeconds * 85);
        g2d.setColor(new Color(228, 73, 73, Math.min(75, alpha / 2)));
        g2d.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);

        int bannerW = 700;
        int bannerH = 38;
        int bx = (GameConfig.WINDOW_WIDTH - bannerW) / 2;
        int by = GameConfig.TOP_BAR_HEIGHT + 10;

        g2d.setColor(new Color(180, 28, 55, Math.min(235, alpha + 60)));
        g2d.fillRoundRect(bx, by, bannerW, bannerH, 12, 12);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        g2d.drawRoundRect(bx, by, bannerW, bannerH, 12, 12);

        g2d.setFont(GameConfig.FONT_BODY_BOLD);
        g2d.setColor(Color.WHITE);
        String text = bannerMessage;
        int textW = g2d.getFontMetrics().stringWidth(text);
        g2d.drawString(text, bx + (bannerW - textW) / 2, by + 24);
    }

    public boolean isActive() {
        return remainingSeconds > 0.0;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }
}
