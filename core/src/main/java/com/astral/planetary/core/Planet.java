package com.astral.planetary.core;

/**
 * Planet class.
 */
public class Planet {
    private long seed;

    public Planet(String name, long seed) {
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }

    public String getType() {
        return "Terran";
    }
}