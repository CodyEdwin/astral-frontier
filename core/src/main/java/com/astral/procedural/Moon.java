package com.astral.procedural;

/**
 * Represents a moon orbiting a planet
 */
public class Moon {

    private final long seed;
    private String name;
    private MoonType type;

    private float radius;           // Earth radii
    private float orbitalDistance;  // From parent planet
    private float orbitalPeriod;    // Days

    private boolean hasAtmosphere;
    private boolean tidallyLocked;

    public enum MoonType {
        ROCKY,
        ICE,
        VOLCANIC,
        CAPTURED_ASTEROID
    }

    public Moon(long seed) {
        this.seed = seed;
        this.tidallyLocked = true; // Most moons are tidally locked
    }

    public long getSeed() {
        return seed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MoonType getType() {
        return type;
    }

    public void setType(MoonType type) {
        this.type = type;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getOrbitalDistance() {
        return orbitalDistance;
    }

    public void setOrbitalDistance(float distance) {
        this.orbitalDistance = distance;
    }

    public float getOrbitalPeriod() {
        return orbitalPeriod;
    }

    public void setOrbitalPeriod(float period) {
        this.orbitalPeriod = period;
    }

    public boolean hasAtmosphere() {
        return hasAtmosphere;
    }

    public void setHasAtmosphere(boolean hasAtmosphere) {
        this.hasAtmosphere = hasAtmosphere;
    }

    public boolean isTidallyLocked() {
        return tidallyLocked;
    }

    public void setTidallyLocked(boolean tidallyLocked) {
        this.tidallyLocked = tidallyLocked;
    }

    public float getGravity() {
        // Approximate gravity based on radius (assuming similar density)
        return radius * radius * 0.5f; // Very simplified
    }

    @Override
    public String toString() {
        return String.format("Moon[%s, %s, r=%.3f]", name, type, radius);
    }
}
