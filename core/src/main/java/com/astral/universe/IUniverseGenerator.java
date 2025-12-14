package com.astral.universe;

import java.util.List;

/**
 * Interface for universe generation strategies.
 * Allows modular generation of star systems, planets, and cosmic features.
 */
public interface IUniverseGenerator {
    /**
     * Generates a list of star systems based on configuration.
     * @param config The universe configuration.
     * @return List of generated star systems.
     */
    List<IStarsystem> generateSystems(UniverseConfig config);

    /**
     * Seeds the generator for reproducible results.
     * @param seed The seed value.
     */
    void setSeed(long seed);
}