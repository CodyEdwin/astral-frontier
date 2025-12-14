package com.astral.universe;

import java.util.List;

/**
 * Handcrafted universe generator for predefined systems.
 * Useful for story-driven content or mods.
 */
public class HandcraftedUniverseGenerator implements IUniverseGenerator {
    @Override
    public List<IStarsystem> generateSystems(UniverseConfig config) {
        // Placeholder: return hardcoded systems
        return List.of(new StarsystemImpl("Sol"), new StarsystemImpl("Alpha Centauri"));
    }

    @Override
    public void setSeed(long seed) {
        // No-op for handcrafted
    }
}