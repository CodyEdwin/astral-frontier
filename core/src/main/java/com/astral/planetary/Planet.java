package com.astral.planetary;

/**
 * Represents a planet.
 */
public class Planet {
    private String name;
    private long seed;

    public Planet(String name, long seed) {
        this.name = name;
        this.seed = seed;
    }

    // Getters
    public String getName() { return name; }
    public long getSeed() { return seed; }
}