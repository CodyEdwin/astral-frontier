package com.astral.procedural;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Represents a star system with stars, planets, and other bodies
 */
public class StarSystem {

    private final long seed;
    private String name;

    private Star primaryStar;
    private Star secondaryStar;
    private final Array<Planet> planets = new Array<>();

    private final Vector3 galacticPosition = new Vector3();

    // Asteroid belt
    private boolean hasAsteroidBelt = false;
    private float asteroidBeltDistance;
    private float asteroidBeltWidth;

    // Discovery state
    private boolean discovered = false;
    private boolean visited = false;

    public StarSystem(long seed) {
        this.seed = seed;
    }

    public long getSeed() {
        return seed;
    }

    public String getName() {
        return name != null ? name : primaryStar != null ? primaryStar.getName() : "Unknown";
    }

    public void setName(String name) {
        this.name = name;
    }

    public Star getPrimaryStar() {
        return primaryStar;
    }

    public void setPrimaryStar(Star star) {
        this.primaryStar = star;
    }

    public Star getSecondaryStar() {
        return secondaryStar;
    }

    public void setSecondaryStar(Star star) {
        this.secondaryStar = star;
    }

    public boolean isBinarySystem() {
        return secondaryStar != null;
    }

    public Array<Planet> getPlanets() {
        return planets;
    }

    public void addPlanet(Planet planet) {
        planets.add(planet);
    }

    public Planet getPlanet(int index) {
        if (index >= 0 && index < planets.size) {
            return planets.get(index);
        }
        return null;
    }

    public Vector3 getGalacticPosition() {
        return galacticPosition;
    }

    public void setGalacticPosition(Vector3 position) {
        galacticPosition.set(position);
    }

    public void setAsteroidBelt(float distance, float width) {
        this.hasAsteroidBelt = true;
        this.asteroidBeltDistance = distance;
        this.asteroidBeltWidth = width;
    }

    public boolean hasAsteroidBelt() {
        return hasAsteroidBelt;
    }

    public float getAsteroidBeltDistance() {
        return asteroidBeltDistance;
    }

    public float getAsteroidBeltWidth() {
        return asteroidBeltWidth;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
        if (visited) this.discovered = true;
    }

    public int getTotalBodies() {
        int count = 1; // Primary star
        if (secondaryStar != null) count++;
        count += planets.size;
        for (Planet planet : planets) {
            count += planet.getMoons().size;
        }
        return count;
    }

    @Override
    public String toString() {
        return String.format("StarSystem[%s, %d planets, pos=%.0f,%.0f,%.0f]",
                getName(), planets.size,
                galacticPosition.x, galacticPosition.y, galacticPosition.z);
    }
}
