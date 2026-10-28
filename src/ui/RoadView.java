package ui;

import interfaces.Drawable;
import interfaces.Updatable;
import model.Road;
import model.Zone;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class RoadView implements Drawable, Updatable {
    private final Road road;
    private final int mapOriginX;
    private final int mapOriginY;
    private final int mapWidth;
    private final int mapHeight;
    private double warningPulse;

    public RoadView(Road road, int mapOriginX, int mapOriginY, int mapWidth, int mapHeight) {
        this.road = road;
        this.mapOriginX = mapOriginX;
        this.mapOriginY = mapOriginY;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.warningPulse = 0.0;
    }

    @Override
    public void update(double delta) {
        if (road.isBlocked()) {
            warningPulse += delta * 5.0;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        Zone a = road.getFrom();
        Zone b = road.getTo();
        int x1 = mapOriginX + (int) Math.round(a.getNormX() * mapWidth);
        int y1 = mapOriginY + (int) Math.round(a.getNormY() * mapHeight);
        int x2 = mapOriginX + (int) Math.round(b.getNormX() * mapWidth);
        int y2 = mapOriginY + (int) Math.round(b.getNormY() * mapHeight);

        float strokeW = road.isNarrowAlley() ? 9.0f : 13.0f;
        g2d.setColor(new Color(12, 20, 32));
        g2d.setStroke(new BasicStroke(strokeW + 3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x1, y1, x2, y2);

        if (road.isBlocked()) {
            g2d.setColor(new Color(95, 35, 45));
        } else if (road.isNarrowAlley()) {
            g2d.setColor(new Color(45, 62, 84));
        } else {
            g2d.setColor(new Color(56, 74, 102));
        }
        g2d.setStroke(new BasicStroke(strokeW, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x1, y1, x2, y2);

        float[] dash = {8.0f, 8.0f};
        g2d.setColor(road.isBlocked() ? GameConfig.JEEPNEY_RED : new Color(185, 198, 218, 140));
        g2d.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f));
        g2d.drawLine(x1, y1, x2, y2);

        int midX = (x1 + x2) / 2;
        int midY = (y1 + y2) / 2;

        if (road.isBlocked()) {
            int pulseR = 12 + (int) (Math.sin(warningPulse) * 3);
            g2d.setColor(new Color(228, 73, 73, 215));
            g2d.fillOval(midX - pulseR, midY - pulseR, pulseR * 2, pulseR * 2);

            g2d.setColor(GameConfig.LAMP_YELLOW);
            g2d.setStroke(new BasicStroke(2.2f));
            g2d.drawLine(midX - 6, midY - 6, midX + 6, midY + 6);
            g2d.drawLine(midX + 6, midY - 6, midX - 6, midY + 6);

            g2d.setFont(GameConfig.FONT_SMALL_BOLD);
            g2d.setColor(Color.WHITE);
            g2d.drawString("BLOCKED", midX - 24, midY - 15);
        } else {
            g2d.setFont(GameConfig.FONT_SMALL);
            g2d.setColor(new Color(170, 192, 215, 185));
            String distLabel = String.format("%.0fs", road.getBaseTravelSeconds());
            g2d.drawString(distLabel, midX - 8, midY - 8);
        }
    }

    public Road getRoad() {
        return road;
    }
}
