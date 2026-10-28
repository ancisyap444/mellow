package ui;

import engine.GameManager;
import enums.IncidentStatus;
import enums.ResponderType;
import interfaces.Drawable;
import interfaces.Updatable;
import model.Incident;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class CallCard implements Drawable, Updatable {
    private final Incident incident;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final boolean selected;

    public CallCard(Incident incident, int x, int y, int width, int height, boolean selected) {
        this.incident = incident;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.selected = selected;
    }

    @Override
    public void update(double delta) {
    }

    @Override
    public void draw(Graphics2D g2d) {
        Color sevColor = IncidentMarker.getSeverityColor(incident.getSeverity());
        Color bg = selected ? GameConfig.BG_CARD_SELECTED : GameConfig.BG_CARD;
        g2d.setColor(bg);
        g2d.fillRoundRect(x, y, width, height, 10, 10);

        g2d.setColor(sevColor);
        g2d.fillRoundRect(x, y, 6, height, 6, 6);

        if (selected) {
            g2d.setColor(GameConfig.LAMP_YELLOW);
            g2d.setStroke(new BasicStroke(2.0f));
        } else {
            g2d.setColor(GameConfig.BORDER_SUBTLE);
            g2d.setStroke(new BasicStroke(1.1f));
        }
        g2d.drawRoundRect(x, y, width, height, 10, 10);

        g2d.setFont(GameConfig.FONT_BODY_BOLD);
        g2d.setColor(GameConfig.TEXT_PRIMARY);
        String header = "#" + incident.getId() + " " + incident.getType().getLabel();
        g2d.drawString(header, x + 14, y + 19);

        double stress = GameManager.getInstance().getOperator().getStress();
        g2d.setFont(GameConfig.FONT_SMALL);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        String locText = incident.getCaller().getDisplayedLocation(incident.getZone(), stress);
        if (locText.length() > 30) {
            locText = locText.substring(0, 28) + "...";
        }
        g2d.drawString(locText, x + 14, y + 36);

        List<ResponderType> missing = incident.getMissingResponderTypes();
        String reqSummary;
        if (missing.isEmpty()) {
            reqSummary = "All Required Units Assigned";
            g2d.setColor(GameConfig.JEEPNEY_GREEN);
        } else {
            List<String> names = new ArrayList<>();
            for (ResponderType rt : missing) {
                names.add(rt.getLabel());
            }
            reqSummary = "Needs: " + String.join(" + ", names);
            g2d.setColor(GameConfig.JEEPNEY_TEAL);
        }
        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.drawString(reqSummary, x + 14, y + 52);

        int panicX = x + 14;
        int panicY = y + 60;
        int panicW = width - 92;
        int panicH = 7;
        g2d.setColor(new Color(12, 22, 34));
        g2d.fillRoundRect(panicX, panicY, panicW, panicH, 4, 4);

        double panicRatio = incident.getCaller().getPanicLevel() / 100.0;
        Color panicColor = panicRatio > 0.7 ? GameConfig.JEEPNEY_RED : GameConfig.JEEPNEY_ORANGE;
        g2d.setColor(panicColor);
        g2d.fillRoundRect(panicX, panicY, (int) Math.round(panicW * panicRatio), panicH, 4, 4);

        g2d.setFont(GameConfig.FONT_SMALL);
        g2d.setColor(GameConfig.TEXT_MUTED);
        g2d.drawString("Panic " + (int) incident.getCaller().getPanicLevel() + "% (" + incident.getCaller().getMood().getLabel() + ")", panicX, y + 77);

        int ringCx = x + width - 34;
        int ringCy = y + height / 2 + 2;
        int ringR = 20;
        g2d.setColor(new Color(12, 20, 32));
        g2d.fillOval(ringCx - ringR, ringCy - ringR, ringR * 2, ringR * 2);

        if (incident.getStatus() == IncidentStatus.HANDLING) {
            int angle = (int) Math.round(incident.getHandlingProgressFraction() * 360.0);
            g2d.setColor(GameConfig.JEEPNEY_GREEN);
            g2d.setStroke(new BasicStroke(3.2f));
            g2d.drawArc(ringCx - ringR, ringCy - ringR, ringR * 2, ringR * 2, 90, -angle);

            g2d.setFont(GameConfig.FONT_SMALL_BOLD);
            String pct = (int) (incident.getHandlingProgressFraction() * 100) + "%";
            int pw = g2d.getFontMetrics().stringWidth(pct);
            g2d.drawString(pct, ringCx - pw / 2, ringCy + 4);
        } else {
            int angle = (int) Math.round(incident.getDeadlineFraction() * 360.0);
            g2d.setColor(sevColor);
            g2d.setStroke(new BasicStroke(3.2f));
            g2d.drawArc(ringCx - ringR, ringCy - ringR, ringR * 2, ringR * 2, 90, -angle);

            g2d.setFont(GameConfig.FONT_SMALL_BOLD);
            g2d.setColor(Color.WHITE);
            String sec = (int) Math.ceil(incident.getRemainingDeadline()) + "s";
            int sw = g2d.getFontMetrics().stringWidth(sec);
            g2d.drawString(sec, ringCx - sw / 2, ringCy + 4);
        }
    }

    public boolean containsPoint(int px, int py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    public Incident getIncident() {
        return incident;
    }
}
