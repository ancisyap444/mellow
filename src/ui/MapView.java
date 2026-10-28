package ui;

import engine.DispatchCenter;
import engine.GameManager;
import engine.RoutePlanner;
import exception.NoRouteException;
import interfaces.Drawable;
import interfaces.Updatable;
import model.Barangay;
import model.Incident;
import model.Responder;
import model.Road;
import model.Zone;
import util.GameConfig;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapView implements Drawable, Updatable {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final List<RoadView> roadViews;
    private final List<ZoneView> zoneViews;
    private volatile List<IncidentMarker> incidentMarkers;
    private volatile List<ResponderIcon> responderIcons;

    public MapView(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.roadViews = new ArrayList<>();
        this.zoneViews = new ArrayList<>();
        this.incidentMarkers = new ArrayList<>();
        this.responderIcons = new ArrayList<>();
        rebuildStaticViews();
    }

    public final void rebuildStaticViews() {
        roadViews.clear();
        zoneViews.clear();
        Barangay barangay = GameManager.getInstance().getBarangay();
        if (barangay == null) {
            return;
        }
        for (Road road : barangay.getRoads()) {
            roadViews.add(new RoadView(road, x, y, width, height));
        }
        for (Zone zone : barangay.getZones()) {
            zoneViews.add(new ZoneView(zone, x, y, width, height));
        }
    }

    @Override
    public void update(double delta) {
        for (RoadView rv : roadViews) {
            rv.update(delta);
        }
        for (ZoneView zv : zoneViews) {
            zv.update(delta);
        }

        GameManager gm = GameManager.getInstance();
        DispatchCenter dc = gm.getDispatchCenter();
        if (dc == null) {
            return;
        }

        List<IncidentMarker> nextMarkers = new ArrayList<>();
        Map<Integer, Integer> zoneIncidentCount = new HashMap<>();
        for (Incident inc : dc.getSortedActiveIncidents()) {
            int zId = inc.getZone().getId();
            int idx = zoneIncidentCount.getOrDefault(zId, 0);
            zoneIncidentCount.put(zId, idx + 1);

            int angleDeg = -45 + idx * 55;
            double rad = Math.toRadians(angleDeg);
            int baseX = x + (int) Math.round(inc.getZone().getNormX() * width);
            int baseY = y + (int) Math.round(inc.getZone().getNormY() * height);
            int mx = baseX + (int) Math.round(Math.cos(rad) * 38);
            int my = baseY - 28 + (int) Math.round(Math.sin(rad) * 20);
            boolean sel = inc.equals(gm.getSelectedIncident());
            IncidentMarker marker = new IncidentMarker(inc, mx, my, sel);
            marker.update(delta);
            nextMarkers.add(marker);
        }
        this.incidentMarkers = nextMarkers;

        List<ResponderIcon> nextIcons = new ArrayList<>();
        Map<Integer, Integer> zoneRespCount = new HashMap<>();
        for (Responder r : dc.getResponders()) {
            int zId = r.getCurrentZone().getId();
            int idx = zoneRespCount.getOrDefault(zId, 0);
            zoneRespCount.put(zId, idx + 1);
            boolean sel = r.equals(gm.getSelectedResponder());
            ResponderIcon icon = new ResponderIcon(r, x, y, width, height, idx, sel);
            icon.update(delta);
            nextIcons.add(icon);
        }
        this.responderIcons = nextIcons;
    }

    @Override
    public void draw(Graphics2D g2d) {
        GradientPaint bgPaint = new GradientPaint(
                x, y, new Color(13, 25, 42),
                x, y + height, new Color(18, 36, 58)
        );
        g2d.setPaint(bgPaint);
        g2d.fillRoundRect(x, y, width, height, 14, 14);

        g2d.setColor(new Color(28, 52, 82, 95));
        g2d.setStroke(new BasicStroke(1.0f));
        for (int gx = x + 32; gx < x + width; gx += 40) {
            g2d.drawLine(gx, y + 8, gx, y + height - 8);
        }
        for (int gy = y + 32; gy < y + height; gy += 40) {
            g2d.drawLine(x + 8, gy, x + width - 8, gy);
        }

        g2d.setColor(new Color(24, 65, 98, 95));
        g2d.setStroke(new BasicStroke(18.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.drawLine(x + 25, y + height - 35, x + width - 25, y + height - 65);

        g2d.setFont(GameConfig.FONT_BODY_BOLD);
        g2d.setColor(GameConfig.LAMP_YELLOW);
        g2d.drawString("SAN ISIDRO VALLEY TACTICAL DISPATCH MAP", x + 16, y + 22);

        g2d.setFont(GameConfig.FONT_SMALL);
        g2d.setColor(GameConfig.TEXT_MUTED);
        g2d.drawString("Click an incident marker or call card, select a unit, or press [A] to Auto-Match", x + 16, y + 38);

        for (RoadView rv : roadViews) {
            rv.draw(g2d);
        }

        drawRoutePreview(g2d);

        for (ZoneView zv : zoneViews) {
            zv.draw(g2d);
        }
        for (ResponderIcon icon : responderIcons) {
            icon.draw(g2d);
        }
        for (IncidentMarker marker : incidentMarkers) {
            marker.draw(g2d);
        }

        drawMapLegend(g2d);

        g2d.setColor(GameConfig.BORDER_SUBTLE);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawRoundRect(x, y, width, height, 14, 14);
    }

    private void drawRoutePreview(Graphics2D g2d) {
        GameManager gm = GameManager.getInstance();
        Incident inc = gm.getSelectedIncident();
        Responder resp = gm.getSelectedResponder();
        RoutePlanner rp = gm.getRoutePlanner();
        if (inc == null || resp == null || rp == null || inc.getStatus().isClosed()) {
            return;
        }

        try {
            RoutePlanner.RouteResult res = rp.findShortestRoute(resp.getCurrentZone(), inc.getZone(), resp, inc);
            List<Zone> route = res.getZones();
            double eta = res.getTravelSeconds();

            float[] dash = {6.0f, 6.0f};
            g2d.setColor(new Color(46, 196, 182, 220));
            g2d.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f));

            for (int i = 0; i < route.size() - 1; i++) {
                Zone a = route.get(i);
                Zone b = route.get(i + 1);
                int ax = x + (int) Math.round(a.getNormX() * width);
                int ay = y + (int) Math.round(a.getNormY() * height);
                int bx = x + (int) Math.round(b.getNormX() * width);
                int by = y + (int) Math.round(b.getNormY() * height);
                g2d.drawLine(ax, ay, bx, by);
            }

            int badgeX = x + width - 195;
            int badgeY = y + 12;
            g2d.setColor(new Color(10, 26, 44, 230));
            g2d.fillRoundRect(badgeX, badgeY, 180, 30, 8, 8);
            g2d.setColor(GameConfig.JEEPNEY_TEAL);
            g2d.setStroke(new BasicStroke(1.3f));
            g2d.drawRoundRect(badgeX, badgeY, 180, 30, 8, 8);

            g2d.setFont(GameConfig.FONT_SMALL_BOLD);
            g2d.setColor(GameConfig.JEEPNEY_GREEN);
            String etaText = String.format("ROUTE CLEAR | ETA: %.1fs", eta);
            g2d.drawString(etaText, badgeX + 12, badgeY + 19);
        } catch (NoRouteException ex) {
            int badgeX = x + width - 205;
            int badgeY = y + 12;
            g2d.setColor(new Color(68, 16, 28, 230));
            g2d.fillRoundRect(badgeX, badgeY, 190, 30, 8, 8);
            g2d.setColor(GameConfig.JEEPNEY_RED);
            g2d.setStroke(new BasicStroke(1.3f));
            g2d.drawRoundRect(badgeX, badgeY, 190, 30, 8, 8);

            g2d.setFont(GameConfig.FONT_SMALL_BOLD);
            g2d.setColor(GameConfig.LAMP_YELLOW);
            g2d.drawString("NO CLEAR ROUTE! ROAD BLOCKED", badgeX + 10, badgeY + 19);
        }
    }

    private void drawMapLegend(Graphics2D g2d) {
        int lx = x + 14;
        int ly = y + height - 28;
        g2d.setColor(new Color(10, 18, 28, 200));
        g2d.fillRoundRect(lx, ly - 14, width - 28, 22, 8, 8);

        g2d.setFont(GameConfig.FONT_SMALL);
        g2d.setColor(GameConfig.TEXT_PRIMARY);
        g2d.drawString(
                "LEGEND:  [COL] Collapse (RES+MED)   [FIR] Fire (FIR)   [MED] Medical (MED)   [BLK] Roadblock (ENG)   [GAS] Gas Leak (FIR+ENG)",
                lx + 10,
                ly
        );
    }

    public Incident findIncidentAt(int px, int py) {
        for (IncidentMarker marker : incidentMarkers) {
            if (marker.containsPoint(px, py)) {
                return marker.getIncident();
            }
        }
        for (ZoneView zv : zoneViews) {
            if (zv.containsPoint(px, py)) {
                DispatchCenter dc = GameManager.getInstance().getDispatchCenter();
                if (dc != null) {
                    for (Incident inc : dc.getSortedActiveIncidents()) {
                        if (inc.getZone().equals(zv.getZone())) {
                            return inc;
                        }
                    }
                }
            }
        }
        return null;
    }
}
