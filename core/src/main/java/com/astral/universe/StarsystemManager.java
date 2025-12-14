package com.astral.universe;

/**
 * Manages loading and unloading of star systems.
 * Uses chunking for performance.
 */
public class StarsystemManager {
    private IStarsystem currentSystem;

    public void loadSystem(IStarsystem system) {
        currentSystem = system;
        // Placeholder: load assets
    }

    public void unloadSystem() {
        // Placeholder: unload
    }

    public IStarsystem getCurrentSystem() {
        return currentSystem;
    }
}