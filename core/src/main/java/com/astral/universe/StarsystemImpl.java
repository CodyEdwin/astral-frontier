package com.astral.universe;

import java.util.List;
import java.util.ArrayList;

/**
 * Basic implementation of a star system.
 */
public class StarsystemImpl implements IStarsystem {
    private String name;
    private List<IPlanet> planets;
    private float x, y, z; // Position in galaxy
    private int securityLevel;
    private boolean hasEconomy;
    private boolean hasFactions;

    public StarsystemImpl(String name) {
        this.name = name;
        this.planets = new ArrayList<>();
        this.securityLevel = 1; // Default
        this.hasEconomy = true;
        this.hasFactions = true;
    }

    public void addPlanet(IPlanet planet) {
        planets.add(planet);
    }

    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getZ() { return z; }

    public int getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(int securityLevel) { this.securityLevel = securityLevel; }

    public boolean hasEconomy() { return hasEconomy; }
    public void setHasEconomy(boolean hasEconomy) { this.hasEconomy = hasEconomy; }

    public boolean hasFactions() { return hasFactions; }
    public void setHasFactions(boolean hasFactions) { this.hasFactions = hasFactions; }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<IPlanet> getPlanets() {
        return planets;
    }
}