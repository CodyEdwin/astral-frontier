package com.astral.inventory.items;

/**
 * Enum for item rarity levels with stat multipliers and colors.
 */
public enum ItemRarity {
    COMMON(1.0f, 0xFFFFFF),
    UNCOMMON(1.15f, 0x00FF00),
    RARE(1.35f, 0x0066FF),
    EPIC(1.6f, 0xAA00FF),
    LEGENDARY(2.0f, 0xFFAA00),
    MYTHICAL(2.5f, 0xFF0000);

    public final float statMultiplier;
    public final int color;

    ItemRarity(float mult, int color) {
        this.statMultiplier = mult;
        this.color = color;
    }

    public int getLevel() {
        return ordinal() + 1;
    }
}