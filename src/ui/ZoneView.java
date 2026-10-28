package ui;

import interfaces.Drawable;
import interfaces.Updatable;
import model.Zone;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class ZoneView implements Drawable, Updatable {
    private final Zone zone;
    private final int mapOriginX;
    private final int mapOriginY;
    private final int mapWidth;
    private final int mapHeight;
    private double lampGlowPhase;

    public ZoneView(Zone zone, int mapOriginX, int mapOriginY, int mapWidth, int mapHeight) {
        this.zone = zone;
        this.mapOriginX = mapOriginX;
        this.mapOriginY = mapOriginY;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.lampGlowPhase = zone.getId() * 0.9;
    }

    @Override
    public void update(double delta) {
        lampGlowPhase += delta * 2.2;
    }

    public int getCenterX() {
        return mapOriginX + (int) Math.round(zone.getNormX() * mapWidth);
    }

    public int getCenterY() {
        return mapOriginY + (int) Math.round(zone.getNormY() * mapHeight);
    }

    @Override
    public void draw(Graphics2D g2d) {
        int cx = getCenterX();
        int cy = getCenterY();

        int glowAlpha = 26 + (int) (Math.sin(lampGlowPhase) * 10);
        g2d.setColor(new Color(247, 197, 72, Math.max(12, glowAlpha)));
        g2d.fillOval(cx - 52, cy - 52, 104, 104);

        g2d.setColor(new Color(24, 42, 66));
        g2d.fillRoundRect(cx - 42, cy - 32, 84, 64, 18, 18);

        g2d.setColor(new Color(68, 104, 148));
        g2d.setStroke(new BasicStroke(2.0f));
        g2d.drawRoundRect(cx - 42, cy - 32, 84, 64, 18, 18);

        drawLandmarkArt(g2d, cx, cy);

        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        g2d.drawString(zone.getShortCode(), cx - 34, cy - 16);

        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.setColor(GameConfig.TEXT_PRIMARY);
        String shortName = zone.getName().replace("District " + zone.getId() + " - ", "");
        int nw = g2d.getFontMetrics().stringWidth(shortName);
        g2d.drawString(shortName, cx - nw / 2, cy + 46);

        g2d.setFont(GameConfig.FONT_SMALL);
        g2d.setColor(GameConfig.TEXT_MUTED);
        String lm = zone.getLandmark();
        int lw = g2d.getFontMetrics().stringWidth(lm);
        g2d.drawString(lm, cx - lw / 2, cy + 59);
    }

    private void drawLandmarkArt(Graphics2D g2d, int cx, int cy) {
        int id = zone.getId();
        if (id == 1) {
            g2d.setColor(new Color(225, 182, 84));
            g2d.fillRect(cx - 14, cy - 8, 28, 18);
            int[] rx = {cx - 18, cx, cx + 18};
            int[] ry = {cy - 8, cy - 20, cy - 8};
            g2d.setColor(new Color(195, 72, 68));
            g2d.fillPolygon(rx, ry, 3);
        } else if (id == 2) {
            g2d.setColor(new Color(235, 200, 112));
            g2d.fillRect(cx - 18, cy - 6, 36, 16);
            g2d.setColor(GameConfig.JEEPNEY_RED);
            g2d.fillRect(cx - 2, cy - 19, 10, 7);
            g2d.setColor(Color.WHITE);
            g2d.drawLine(cx - 2, cy - 19, cx - 2, cy - 6);
        } else if (id == 3) {
            g2d.setColor(new Color(88, 164, 218));
            g2d.fillRoundRect(cx - 18, cy - 12, 36, 22, 8, 8);
            g2d.setColor(Color.WHITE);
            g2d.drawRect(cx - 12, cy - 7, 24, 12);
        } else {
            g2d.setColor(new Color(148, 175, 205));
            g2d.fillRect(cx - 15, cy - 4, 12, 12);
            g2d.fillRect(cx + 3, cy - 6, 12, 14);
            g2d.setColor(new Color(215, 115, 75));
            g2d.fillPolygon(new int[]{cx - 17, cx - 9, cx - 1}, new int[]{cy - 4, cy - 12, cy - 4}, 3);
            g2d.fillPolygon(new int[]{cx + 1, cx + 9, cx + 17}, new int[]{cy - 6, cy - 14, cy - 6}, 3);
        }
    }

    public boolean containsPoint(int px, int py) {
        int cx = getCenterX();
        int cy = getCenterY();
        return px >= cx - 44 && px <= cx + 44 && py >= cy - 34 && py <= cy + 34;
    }

    public Zone getZone() {
        return zone;
    }
}
