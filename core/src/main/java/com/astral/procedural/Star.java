package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;

/**
 * Represents a star in a star system
 */
public class Star {

    private final long seed;
    private final StarType type;
    private String name;

    private float radius;       // Solar radii
    private float mass;         // Solar masses
    private float luminosity;   // Solar luminosity
    private float temperature;  // Kelvin
    private Color color;

    public enum StarType {
        O(30000, 60000, new Color(0.6f, 0.7f, 1f, 1f), 16f, 25000f),
        B(10000, 30000, new Color(0.7f, 0.8f, 1f, 1f), 6f, 800f),
        A(7500, 10000, new Color(0.9f, 0.9f, 1f, 1f), 2f, 25f),
        F(6000, 7500, new Color(1f, 1f, 0.95f, 1f), 1.3f, 4f),
        G(5200, 6000, new Color(1f, 1f, 0.8f, 1f), 1f, 1f),      // Sun-like
        K(3700, 5200, new Color(1f, 0.85f, 0.6f, 1f), 0.7f, 0.3f),
        M(2400, 3700, new Color(1f, 0.6f, 0.4f, 1f), 0.3f, 0.04f);

        public final int minTemp;
        public final int maxTemp;
        public final Color baseColor;
        public final float typicalMass;      // Solar masses
        public final float typicalLuminosity; // Solar luminosity

        StarType(int minTemp, int maxTemp, Color color, float mass, float luminosity) {
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.baseColor = color;
            this.typicalMass = mass;
            this.typicalLuminosity = luminosity;
        }
    }

    public Star(long seed, StarType type) {
        this.seed = seed;
        this.type = type;
        generateProperties();
    }

    private void generateProperties() {
        java.util.Random r = new java.util.Random(seed);

        // Temperature within type range
        temperature = type.minTemp + r.nextFloat() * (type.maxTemp - type.minTemp);

        // Mass with some variance
        mass = type.typicalMass * (0.8f + r.nextFloat() * 0.4f);

        // Luminosity with variance
        luminosity = type.typicalLuminosity * (0.7f + r.nextFloat() * 0.6f);

        // Radius derived from luminosity and temperature (Stefan-Boltzmann)
        radius = (float) Math.sqrt(luminosity) * (5778f / temperature) * (5778f / temperature);

        // Color with slight variance
        color = type.baseColor.cpy();
        float variance = (r.nextFloat() - 0.5f) * 0.1f;
        color.r = Math.max(0, Math.min(1, color.r + variance));
        color.g = Math.max(0, Math.min(1, color.g + variance));
        color.b = Math.max(0, Math.min(1, color.b + variance));
    }

    public StarType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getRadius() {
        return radius;
    }

    public float getMass() {
        return mass;
    }

    public float getLuminosity() {
        return luminosity;
    }

    public float getTemperature() {
        return temperature;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Inner edge of habitable zone in AU
     */
    public float getHabitableZoneInner() {
        return (float) Math.sqrt(luminosity / 1.1f);
    }

    /**
     * Outer edge of habitable zone in AU
     */
    public float getHabitableZoneOuter() {
        return (float) Math.sqrt(luminosity / 0.53f);
    }

    public String getSpectralClass() {
        return type.name() + (int) ((temperature - type.minTemp) / (type.maxTemp - type.minTemp) * 10);
    }

    @Override
    public String toString() {
        return String.format("Star[%s, %s, %.0fK, %.2fM☉, %.2fL☉]",
                name, getSpectralClass(), temperature, mass, luminosity);
    }
}
