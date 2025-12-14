package com.astral.universe;

/**
 * Configuration class for universe generation parameters.
 * Loaded from JSON for modularity.
 */
public class UniverseConfig {
    private int systemCount;
    private double galaxyRadius;
    private boolean enableFactions;
    private boolean enableEconomy;

    // Getters and setters
    public int getSystemCount() { return systemCount; }
    public void setSystemCount(int systemCount) { this.systemCount = systemCount; }

    public double getGalaxyRadius() { return galaxyRadius; }
    public void setGalaxyRadius(double galaxyRadius) { this.galaxyRadius = galaxyRadius; }

    public boolean isEnableFactions() { return enableFactions; }
    public void setEnableFactions(boolean enableFactions) { this.enableFactions = enableFactions; }

    public boolean isEnableEconomy() { return enableEconomy; }
    public void setEnableEconomy(boolean enableEconomy) { this.enableEconomy = enableEconomy; }
}