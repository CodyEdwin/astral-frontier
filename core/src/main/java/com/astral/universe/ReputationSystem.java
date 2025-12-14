package com.astral.universe;

/**
 * Manages faction reputation.
 */
public class ReputationSystem {
    private int reputation;

    public void adjustReputation(int delta) {
        reputation += delta;
    }

    public int getReputation() {
        return reputation;
    }
}