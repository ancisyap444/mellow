package model;

public class Road {
    private final int id;
    private final String name;
    private final Zone from;
    private final Zone to;
    private double baseTravelSeconds;
    private final boolean narrowAlley;
    private boolean blocked;

    public Road(int id, String name, Zone from, Zone to, double baseTravelSeconds, boolean narrowAlley) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Road endpoints cannot be null");
        }
        this.id = id;
        this.name = (name == null || name.trim().isEmpty()) ? "District Road " + id : name.trim();
        this.from = from;
        this.to = to;
        this.baseTravelSeconds = Math.max(1.0, baseTravelSeconds);
        this.narrowAlley = narrowAlley;
        this.blocked = false;
    }

    public Road(int id, String name, Zone from, Zone to, double baseTravelSeconds) {
        this(id, name, from, to, baseTravelSeconds, false);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Zone getFrom() {
        return from;
    }

    public Zone getTo() {
        return to;
    }

    public double getBaseTravelSeconds() {
        return baseTravelSeconds;
    }

    public void setBaseTravelSeconds(double baseTravelSeconds) {
        this.baseTravelSeconds = Math.max(1.0, baseTravelSeconds);
    }

    public boolean isNarrowAlley() {
        return narrowAlley;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean connects(Zone a, Zone b) {
        if (a == null || b == null) {
            return false;
        }
        return (from.equals(a) && to.equals(b)) || (from.equals(b) && to.equals(a));
    }

    public Zone getOtherEnd(Zone current) {
        if (from.equals(current)) {
            return to;
        }
        if (to.equals(current)) {
            return from;
        }
        return null;
    }
}
