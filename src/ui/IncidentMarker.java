package ui;

import enums.IncidentStatus;
import enums.IncidentType;
import enums.Severity;
import interfaces.Drawable;
import interfaces.Updatable;
import model.Incident;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class IncidentMarker implements Drawable, Updatable {
    private final Incident incident;
    private final int screenX;
    private final int screenY;
    private final boolean selected;
    private double pulsePhase;

    public IncidentMarker(Incident incident, int screenX, int screenY, boolean selected) {
        this.incident = incident;
        this.screenX = screenX;
        this.screenY = screenY;
        this.selected = selected;
        this.pulsePhase = 0.0;
    }

    @Override
    public void update(double delta) {
        pulsePhase += delta * (incident.getSeverity() == Severity.CRITICAL ? 7.5 : 4.2);
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (incident.getStatus().isClosed()) {
            return;
        }
        Color sevColor = getSeverityColor(incident.getSeverity());
        int pulseRadius = 18 + (int) (Math.sin(pulsePhase) * 4);

        g2d.setColor(new Color(sevColor.getRed(), sevColor.getGreen(), sevColor.getBlue(), 70));
        g2d.fillOval(screenX - pulseRadius, screenY - pulseRadius, pulseRadius * 2, pulseRadius * 2);

        if (selected) {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2.6f));
            g2d.drawOval(screenX - 22, screenY - 22, 44, 44);
        }

        int arcRadius = 17;
        g2d.setColor(new Color(15, 22, 34, 230));
        g2d.fillOval(screenX - arcRadius, screenY - arcRadius, arcRadius * 2, arcRadius * 2);

        if (incident.getStatus() == IncidentStatus.HANDLING) {
            int progressAngle = (int) Math.round(incident.getHandlingProgressFraction() * 360.0);
            g2d.setColor(GameConfig.JEEPNEY_GREEN);
            g2d.setStroke(new BasicStroke(3.4f));
            g2d.drawArc(screenX - arcRadius, screenY - arcRadius, arcRadius * 2, arcRadius * 2, 90, -progressAngle);
        } else {
            int timeAngle = (int) Math.round(incident.getDeadlineFraction() * 360.0);
            g2d.setColor(sevColor);
            g2d.setStroke(new BasicStroke(3.2f));
            g2d.drawArc(screenX - arcRadius, screenY - arcRadius, arcRadius * 2, arcRadius * 2, 90, -timeAngle);
        }

        g2d.setFont(GameConfig.FONT_SMALL_BOLD);
        g2d.setColor(Color.WHITE);
        String code = getShortTypeCode(incident.getType());
        int cw = g2d.getFontMetrics().stringWidth(code);
        g2d.drawString(code, screenX - cw / 2, screenY + 4);

        if (incident.getStatus() == IncidentStatus.HANDLING) {
            int pct = (int) Math.round(incident.getHandlingProgressFraction() * 100);
            String badge = pct + "%";
            g2d.setFont(GameConfig.FONT_SMALL_BOLD);
            g2d.setColor(GameConfig.JEEPNEY_GREEN);
            g2d.drawString(badge, screenX - 10, screenY - 20);
        }
    }

    public static String getShortTypeCode(IncidentType type) {
        if (type == IncidentType.COLLAPSED_BUILDING) {
            return "COL";
        }
        if (type == IncidentType.FIRE) {
            return "FIR";
        }
        if (type == IncidentType.MEDICAL_EMERGENCY) {
            return "MED";
        }
        if (type == IncidentType.BLOCKED_ROAD) {
            return "BLK";
        }
        return "GAS";
    }

    public static Color getSeverityColor(Severity severity) {
        if (severity == Severity.CRITICAL) {
            return GameConfig.JEEPNEY_RED;
        }
        if (severity == Severity.HIGH) {
            return GameConfig.JEEPNEY_ORANGE;
        }
        if (severity == Severity.MODERATE) {
            return GameConfig.LAMP_YELLOW;
        }
        return GameConfig.JEEPNEY_TEAL;
    }

    public boolean containsPoint(int px, int py) {
        int dx = px - screenX;
        int dy = py - screenY;
        return (dx * dx + dy * dy) <= (20 * 20);
    }

    public Incident getIncident() {
        return incident;
    }
}
