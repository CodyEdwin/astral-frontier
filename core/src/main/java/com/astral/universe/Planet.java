package com.astral.universe;

/**
 * Implementation of a planet.
 */
public class Planet implements IPlanet {
    private String name;
    private long seed;
    private String type;
    private float radius;
    private float mass;

    public Planet(String name, long seed) {
        this.name = name;
        this.seed = seed;
        // Generate properties based on seed
        // Placeholder
        this.type = "Terran";
        this.radius = 6000f + (seed % 1000);
        this.mass = radius * 0.1f;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    public long getSeed() { return seed; }
    public float getRadius() { return radius; }
    public float getMass() { return mass; }
}