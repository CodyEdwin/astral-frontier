package com.astral.universe;

/**
 * Defines progression milestones.
 */
public class ProgressionMilestone {
    private String name;
    private boolean achieved;

    public ProgressionMilestone(String name) {
        this.name = name;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public void setAchieved(boolean achieved) {
        this.achieved = achieved;
    }
}