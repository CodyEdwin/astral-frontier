package com.astral.rpg;

/**
 * Reward for completing a quest
 */
public class QuestReward {

    public RewardType type;
    public String value;    // Item ID, faction ID, etc.
    public int amount = 1;

    public enum RewardType {
        CREDITS,
        EXPERIENCE,
        ITEM,
        REPUTATION,
        UNLOCK
    }

    @Override
    public String toString() {
        return switch (type) {
            case CREDITS -> amount + " Credits";
            case EXPERIENCE -> amount + " XP";
            case ITEM -> amount + "x " + value;
            case REPUTATION -> amount + " Reputation";
            case UNLOCK -> "Unlock: " + value;
        };
    }
}
