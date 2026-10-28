package ui;

import enums.ResponderStatus;
import enums.ResponderType;
import interfaces.Drawable;
import interfaces.Updatable;
import model.Responder;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class ResponderIcon implements Drawable, Updatable {
    private final Responder responder;
    private final int mapOriginX;
    private final int mapOriginY;
    private final int mapWidth;
    private final int mapHeight;
    private final int stackOffsetIndex;
    private final boolean selected;
    private double strobeTimer;

    public ResponderIcon(Responder responder, int mapOriginX, int mapOriginY, int mapWidth, int mapHeight,
                         int stackOffsetIndex, boolean selected) {
        this.responder = responder;
        this.mapOriginX = mapOriginX;
        this.mapOriginY = mapOriginY;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.stackOffsetIndex = stackOffsetIndex;
        this.selected = selected;
        this.strobeTimer = 0.0;
    }

    @Override
    public void update(double delta) {
        strobeTimer += delta * 10.0;
    }

    @Override
    public void draw(Graphics2D g2d) {
        int offsetX = (stackOffsetIndex % 3 - 1) * 20;
        int offsetY = 20 + (stackOffsetIndex / 3) * 16;
        if (responder.getStatus() == ResponderStatus.EN_ROUTE) {
            offsetX = 0;
            offsetY = 0;
        }

        int sx = mapOriginX + (int) Math.round(responder.getMapNormX() * mapWidth) + offsetX;
        int sy = mapOriginY + (int) Math.round(responder.getMapNormY() * mapHeight) + offsetY;

        if (responder.getStatus() == ResponderStatus.EN_ROUTE) {
            boolean redStrobe = ((int) strobeTimer % 2) == 0;
            Color strobe = redStrobe ? new Color(255, 65, 95, 165) : new Color(56, 189, 248, 165);
            g2d.setColor(strobe);
            g2d.fillOval(sx - 15, sy - 15, 30, 30);
        }

        Color badgeColor = getTypeColor(responder.getType());
        if (responder.getStatus() == ResponderStatus.RESTING) {
            badgeColor = new Color(110, 122, 140);
        }

        if (selected) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.4f));
            g2d.drawRoundRect(sx - 13, sy - 11, 26, 22, 8, 8);
        }

        g2d.setColor(badgeColor);
        g2d.fillRoundRect(sx - 11, sy - 9, 22, 18, 6, 6);
        g2d.setColor(new Color(10, 18, 28));
        g2d.setStroke(new BasicStroke(1.3f));
        g2d.drawRoundRect(sx - 11, sy - 9, 22, 18, 6, 6);

        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.setColor(new Color(10, 16, 24));
        String tag = responder.getType().getCode();
        int tw = g2d.getFontMetrics().stringWidth(tag);
        g2d.drawString(tag, sx - tw / 2, sy + 4);
    }

    public static Color getTypeColor(ResponderType type) {
        if (type == ResponderType.MEDIC) {
            return GameConfig.JEEPNEY_GREEN;
        }
        if (type == ResponderType.FIREFIGHTER) {
            return GameConfig.JEEPNEY_RED;
        }
        if (type == ResponderType.RESCUER) {
            return GameConfig.JEEPNEY_ORANGE;
        }
        if (type == ResponderType.ENGINEER) {
            return GameConfig.LAMP_YELLOW;
        }
        return GameConfig.JEEPNEY_TEAL;
    }

    public Responder getResponder() {
        return responder;
    }
}
