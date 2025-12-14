package com.astral.universe;

/**
 * Tracks universe statistics.
 * E.g., systems visited, total wealth generated.
 */
public class UniverseStatisticsTracker {
    private int systemsVisited;
    private long totalWealth;

    public void incrementSystemsVisited() {
        systemsVisited++;
    }

    public void addWealth(long amount) {
        totalWealth += amount;
    }

    // Getters
    public int getSystemsVisited() { return systemsVisited; }
    public long getTotalWealth() { return totalWealth; }
}