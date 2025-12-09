package com.astral.rpg;

import java.util.UUID;

/**
 * Unique instance data for a specific item (durability, mods, etc.)
 */
public class ItemInstance {

    public UUID uuid;

    // Durability
    public float durability = 1f;
    public float maxDurability = 1f;

    // Modifications/enchantments
    public String[] modifications;
    public float[] modValues;

    // Custom name
    public String customName;

    // Creation info
    public long createdTime;
    public String createdBy;

    public ItemInstance() {
        this.uuid = UUID.randomUUID();
        this.createdTime = System.currentTimeMillis();
    }

    public float getDurabilityPercent() {
        return durability / maxDurability;
    }

    public boolean isBroken() {
        return durability <= 0;
    }

    public void damage(float amount) {
        durability = Math.max(0, durability - amount);
    }

    public void repair(float amount) {
        durability = Math.min(maxDurability, durability + amount);
    }

    public void addModification(String modId, float value) {
        if (modifications == null) {
            modifications = new String[]{modId};
            modValues = new float[]{value};
        } else {
            String[] newMods = new String[modifications.length + 1];
            float[] newVals = new float[modValues.length + 1];
            System.arraycopy(modifications, 0, newMods, 0, modifications.length);
            System.arraycopy(modValues, 0, newVals, 0, modValues.length);
            newMods[newMods.length - 1] = modId;
            newVals[newVals.length - 1] = value;
            modifications = newMods;
            modValues = newVals;
        }
    }

    public float getModValue(String modId) {
        if (modifications == null) return 0;
        for (int i = 0; i < modifications.length; i++) {
            if (modifications[i].equals(modId)) {
                return modValues[i];
            }
        }
        return 0;
    }

    public ItemInstance copy() {
        ItemInstance copy = new ItemInstance();
        copy.durability = this.durability;
        copy.maxDurability = this.maxDurability;
        copy.customName = this.customName;
        if (this.modifications != null) {
            copy.modifications = this.modifications.clone();
            copy.modValues = this.modValues.clone();
        }
        return copy;
    }
}
