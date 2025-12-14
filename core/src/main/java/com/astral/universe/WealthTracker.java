package com.astral.universe;

/**
 * Tracks player wealth.
 */
public class WealthTracker {
    private long credits;

    public void addCredits(long amount) {
        credits += amount;
    }

    public long getCredits() {
        return credits;
    }
}