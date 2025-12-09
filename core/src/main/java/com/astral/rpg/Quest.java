package com.astral.rpg;

import com.badlogic.gdx.utils.Array;

/**
 * Quest definition
 */
public class Quest {

    public String id;
    public String name;
    public String description;
    public QuestType type = QuestType.SIDE;
    public QuestState state = QuestState.AVAILABLE;

    public Array<QuestObjective> objectives = new Array<>();
    public Array<QuestReward> rewards = new Array<>();
    public Array<String> prerequisites = new Array<>();

    // Optional quest giver
    public String questGiverId;
    public String questGiverName;

    // Time limits (optional)
    public float timeLimit = -1; // -1 = no limit
    public float elapsedTime = 0;

    // Tracking
    public boolean isTracked = false;

    public enum QuestType {
        MAIN,       // Main story quest
        SIDE,       // Side quest
        FACTION,    // Faction quest
        BOUNTY,     // Kill target quest
        EXPLORATION,// Discover locations
        DELIVERY,   // Deliver items
        REPEATABLE  // Can be done multiple times
    }

    public enum QuestState {
        LOCKED,     // Prerequisites not met
        AVAILABLE,  // Can be started
        ACTIVE,     // In progress
        COMPLETED,  // All objectives done
        FAILED      // Time expired or failed
    }

    public boolean isComplete() {
        if (objectives.isEmpty()) return false;

        for (QuestObjective obj : objectives) {
            if (!obj.isComplete) return false;
        }
        return true;
    }

    public float getProgress() {
        if (objectives.isEmpty()) return 0;

        float total = 0;
        for (QuestObjective obj : objectives) {
            total += (float) obj.currentCount / obj.requiredCount;
        }
        return total / objectives.size;
    }

    public QuestObjective getCurrentObjective() {
        for (QuestObjective obj : objectives) {
            if (!obj.isComplete) return obj;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Quest[%s: %s (%s)]", id, name, state);
    }
}
