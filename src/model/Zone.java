package model;

import java.util.Objects;

public class Zone {
    private final int id;
    private String name;
    private String landmark;
    private double normX;
    private double normY;
    private int houseCount;

    public Zone(int id, String name, String landmark, double normX, double normY, int houseCount) {
        this.id = id;
        setName(name);
        setLandmark(landmark);
        setNormX(normX);
        setNormY(normY);
        setHouseCount(houseCount);
    }

    public Zone(int id, String name, double normX, double normY) {
        this(id, name, "Residential Block", normX, normY, 6);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Zone name cannot be empty");
        }
        this.name = name.trim();
    }

    public String getLandmark() {
        return landmark;
    }

    public void setLandmark(String landmark) {
        this.landmark = (landmark == null || landmark.trim().isEmpty()) ? "District Center" : landmark.trim();
    }

    public double getNormX() {
        return normX;
    }

    public void setNormX(double normX) {
        this.normX = Math.max(0.05, Math.min(0.95, normX));
    }

    public double getNormY() {
        return normY;
    }

    public void setNormY(double normY) {
        this.normY = Math.max(0.05, Math.min(0.95, normY));
    }

    public int getHouseCount() {
        return houseCount;
    }

    public void setHouseCount(int houseCount) {
        this.houseCount = Math.max(1, Math.min(16, houseCount));
    }

    public String getShortCode() {
        return "D" + id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Zone)) {
            return false;
        }
        Zone zone = (Zone) o;
        return id == zone.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + landmark + ")";
    }
}
