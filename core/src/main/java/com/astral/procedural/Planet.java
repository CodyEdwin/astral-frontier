package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

/**
 * Represents a planet in a star system
 */
public class Planet {

    private final long seed;
    private String name;
    private PlanetType type;

    // Physical properties
    private float radius;           // Earth radii
    private float mass;             // Earth masses
    private float gravity;          // Earth g
    private float orbitalDistance;  // AU
    private float orbitalPeriod;    // Earth years
    private float rotationPeriod;   // Earth hours
    private float axialTilt;        // Degrees

    // Atmosphere
    private boolean hasAtmosphere;
    private float atmosphereDensity; // 1.0 = Earth-like
    private Color atmosphereColor;

    // Surface
    private float waterCoverage;    // 0-1
    private float temperature;      // Kelvin
    private boolean habitable;

    // Moons
    private final Array<Moon> moons = new Array<>();

    // Resources
    private float[] resourceAbundance; // Per resource type

    // Discovery state
    private boolean discovered = false;
    private boolean surveyed = false;

    public enum PlanetType {
        BARREN(0.3f, 0.8f, false, new Color(0.5f, 0.5f, 0.5f, 1f)),
        DESERT(0.5f, 1.2f, false, new Color(0.9f, 0.7f, 0.4f, 1f)),
        VOLCANIC(0.4f, 1.0f, false, new Color(0.3f, 0.2f, 0.2f, 1f)),
        FROZEN(0.4f, 1.5f, false, new Color(0.8f, 0.9f, 1f, 1f)),
        TERRAN(0.8f, 1.2f, true, new Color(0.3f, 0.5f, 0.8f, 1f)),
        OCEAN(0.9f, 1.1f, true, new Color(0.2f, 0.4f, 0.9f, 1f)),
        GAS_GIANT(10f, 50f, false, new Color(0.8f, 0.6f, 0.4f, 1f)),
        ICE_GIANT(4f, 15f, false, new Color(0.5f, 0.7f, 0.9f, 1f)),
        DWARF(0.1f, 0.4f, false, new Color(0.4f, 0.4f, 0.4f, 1f));

        public final float minRadius;
        public final float maxRadius;
        public final boolean canBeHabitable;
        public final Color baseColor;

        PlanetType(float minR, float maxR, boolean habitable, Color color) {
            this.minRadius = minR;
            this.maxRadius = maxR;
            this.canBeHabitable = habitable;
            this.baseColor = color;
        }
    }

    public Planet(long seed) {
        this.seed = seed;
        this.resourceAbundance = new float[8]; // Different resource types
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

    public PlanetType getType() {
        return type;
    }

    public void setType(PlanetType type) {
        this.type = type;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
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

    public float getAtmosphereDensity() {
        return atmosphereDensity;
    }

    public void setAtmosphereDensity(float density) {
        this.atmosphereDensity = density;
    }

    public Color getAtmosphereColor() {
        return atmosphereColor;
    }

    public void setAtmosphereColor(Color color) {
        this.atmosphereColor = color;
    }

    public float getWaterCoverage() {
        return waterCoverage;
    }

    public void setWaterCoverage(float coverage) {
        this.waterCoverage = coverage;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temp) {
        this.temperature = temp;
    }

    public boolean isHabitable() {
        return habitable;
    }

    public void setHabitable(boolean habitable) {
        this.habitable = habitable;
    }

    public Array<Moon> getMoons() {
        return moons;
    }

    public void addMoon(Moon moon) {
        moons.add(moon);
    }

    public boolean isGasGiant() {
        return type == PlanetType.GAS_GIANT || type == PlanetType.ICE_GIANT;
    }

    public boolean isLandable() {
        return !isGasGiant();
    }

    public float getResourceAbundance(int resourceType) {
        if (resourceType >= 0 && resourceType < resourceAbundance.length) {
            return resourceAbundance[resourceType];
        }
        return 0;
    }

    public void setResourceAbundance(int resourceType, float abundance) {
        if (resourceType >= 0 && resourceType < resourceAbundance.length) {
            resourceAbundance[resourceType] = abundance;
        }
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public boolean isSurveyed() {
        return surveyed;
    }

    public void setSurveyed(boolean surveyed) {
        this.surveyed = surveyed;
    }

    @Override
    public String toString() {
        return String.format("Planet[%s, %s, r=%.2f, d=%.2f AU, %d moons]",
                name, type, radius, orbitalDistance, moons.size);
    }
}
