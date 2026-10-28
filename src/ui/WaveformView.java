package ui;

import interfaces.Drawable;
import interfaces.Updatable;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class WaveformView implements Drawable, Updatable {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private double phase;
    private double panicAmplitude;
    private double stressStaticRatio;
    private boolean callerSpeaking;
    private final Random random;

    public WaveformView(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.phase = 0.0;
        this.panicAmplitude = 0.3;
        this.stressStaticRatio = 0.1;
        this.callerSpeaking = false;
        this.random = new Random();
    }

    public void setSignalState(boolean speaking, double panicLevel, double stressLevel) {
        this.callerSpeaking = speaking;
        this.panicAmplitude = Math.max(0.15, Math.min(1.0, panicLevel / 100.0));
        this.stressStaticRatio = Math.max(0.0, Math.min(1.0, stressLevel / 100.0));
    }

    @Override
    public void update(double delta) {
        double speed = callerSpeaking ? (8.0 + panicAmplitude * 12.0) : 3.2;
        phase += delta * speed;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(8, 16, 26));
        g2d.fillRoundRect(x, y, width, height, 10, 10);

        g2d.setColor(new Color(28, 52, 78));
        for (int gx = x + 16; gx < x + width; gx += 20) {
            g2d.drawLine(gx, y + 4, gx, y + height - 4);
        }
        int midY = y + height / 2;
        g2d.drawLine(x + 4, midY, x + width - 4, midY);

        Color waveColor = callerSpeaking
                ? (panicAmplitude > 0.65 ? GameConfig.JEEPNEY_RED : GameConfig.JEEPNEY_GREEN)
                : GameConfig.JEEPNEY_TEAL;
        g2d.setColor(waveColor);
        g2d.setStroke(new BasicStroke(2.0f));

        int prevX = x + 6;
        int prevY = midY;
        for (int px = x + 8; px < x + width - 6; px += 3) {
            double normX = (px - x) / (double) width;
            double envelope = Math.sin(normX * Math.PI);
            double wave1 = Math.sin((normX * 18.0) + phase);
            double wave2 = Math.cos((normX * 33.0) - phase * 1.4) * 0.5;
            double amp = callerSpeaking ? (height * 0.38 * (0.35 + panicAmplitude * 0.65)) : (height * 0.10);
            double jitter = (stressStaticRatio > 0.35)
                    ? (random.nextDouble() - 0.5) * height * 0.35 * stressStaticRatio
                    : 0.0;
            int curY = (int) Math.round(midY + (wave1 + wave2) * envelope * amp + jitter);
            curY = Math.max(y + 5, Math.min(y + height - 5, curY));
            g2d.drawLine(prevX, prevY, px, curY);
            prevX = px;
            prevY = curY;
        }

        int staticDots = (int) (stressStaticRatio * 48);
        g2d.setColor(new Color(255, 255, 255, 110));
        for (int i = 0; i < staticDots; i++) {
            int sx = x + 5 + random.nextInt(Math.max(1, width - 10));
            int sy = y + 5 + random.nextInt(Math.max(1, height - 10));
            g2d.fillRect(sx, sy, 2, 2);
        }

        g2d.setColor(GameConfig.BORDER_SUBTLE);
        g2d.setStroke(new BasicStroke(1.2f));
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.setColor(GameConfig.TEXT_MUTED);
        g2d.drawString("144.72 MHz VHF", x + 8, y + 14);
    }
}
