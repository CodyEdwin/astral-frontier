package com.astral.inventory;

/**
 * Represents item rarity levels.
 */
public enum ItemRarity {
    COMMON(1), UNCOMMON(2), RARE(3), EPIC(4), LEGENDARY(5), MYTHICAL(6);

    private int level;

    ItemRarity(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}