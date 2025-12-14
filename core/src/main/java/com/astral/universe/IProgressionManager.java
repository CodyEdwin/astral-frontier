package com.astral.universe;

/**
 * Interface for managing player progression.
 */
public interface IProgressionManager {
    void addXP(int amount);
    int getLevel();
    void checkMilestones();
}