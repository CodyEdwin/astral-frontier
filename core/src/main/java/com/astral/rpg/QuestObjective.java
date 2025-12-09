package com.astral.rpg;

/**
 * A single objective within a quest
 */
public class QuestObjective {

    public String id;
    public String description;
    public ObjectiveType type;
    public String target;        // Target ID (enemy type, item ID, location ID, etc.)
    public int requiredCount = 1;
    public int currentCount = 0;
    public boolean isComplete = false;
    public boolean isOptional = false;

    public enum ObjectiveType {
        KILL_ENEMY,      // Kill specific enemy type
        COLLECT_ITEM,    // Collect items
        VISIT_LOCATION,  // Visit a location
        TALK_TO_NPC,     // Complete dialogue with NPC
        DELIVER_ITEM,    // Deliver item to NPC/location
        SCAN_OBJECT      // Scan/analyze an object
    }

    public float getProgress() {
        return (float) currentCount / requiredCount;
    }

    public String getProgressText() {
        if (type == ObjectiveType.VISIT_LOCATION ||
                type == ObjectiveType.TALK_TO_NPC) {
            return isComplete ? "Complete" : "Incomplete";
        }
        return currentCount + " / " + requiredCount;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", description, getProgressText());
    }
}
