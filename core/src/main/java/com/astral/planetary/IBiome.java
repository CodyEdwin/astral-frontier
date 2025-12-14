package com.astral.planetary;

/**
 * Interface for biomes.
 */
public interface IBiome {
    String getName();
    void generateFeatures(Planet planet);
}