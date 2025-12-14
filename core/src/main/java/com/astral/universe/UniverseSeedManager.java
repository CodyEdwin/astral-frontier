package com.astral.universe;

/**
 * Manages seeds for universe generation.
 * Ensures consistency across sessions.
 */
public class UniverseSeedManager {
    private long masterSeed;

    public UniverseSeedManager(long masterSeed) {
        this.masterSeed = masterSeed;
    }

    public long getMasterSeed() {
        return masterSeed;
    }

    public long deriveSeed(String context) {
        return masterSeed ^ context.hashCode();
    }
}