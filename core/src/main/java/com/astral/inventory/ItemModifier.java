package com.astral.inventory;

/**
 * Modifiers for items (enchantments).
 */
public class ItemModifier {
    private String name;
    private String effect;

    public ItemModifier(String name, String effect) {
        this.name = name;
        this.effect = effect;
    }

    // Getters
    public String getName() { return name; }
    public String getEffect() { return effect; }
}