package com.astral.universe;

/**
 * Factory for creating universe generators.
 * Enables dependency injection and modularity.
 */
public class UniverseFactory {
    public static IUniverseGenerator createProceduralGenerator() {
        return new ProceduralUniverseGenerator();
    }

    public static IUniverseGenerator createHandcraftedGenerator() {
        return new HandcraftedUniverseGenerator();
    }
}