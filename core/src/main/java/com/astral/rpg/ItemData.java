package com.astral.rpg;

/**
 * Item definition data (shared between all instances of an item)
 */
public class ItemData {

    public String id;
    public String name;
    public String description;
    public ItemType type;
    public ItemRarity rarity = ItemRarity.COMMON;

    public float weight;
    public int maxStackSize = 99;
    public int baseValue;

    public String icon;
    public String model;

    // Type-specific stats
    public WeaponStats weaponStats;
    public ArmorStats armorStats;
    public ConsumableEffect consumableEffect;
    public String resourceType;

    public enum ItemType {
        WEAPON_RANGED,
        WEAPON_MELEE,
        ARMOR_HELMET,
        ARMOR_CHEST,
        ARMOR_LEGS,
        ARMOR_BOOTS,
        CONSUMABLE,
        RESOURCE,
        SHIP_MODULE,
        QUEST_ITEM,
        JUNK
    }

    public enum ItemRarity {
        COMMON(1.0f, 0xFFFFFF),
        UNCOMMON(1.15f, 0x00FF00),
        RARE(1.35f, 0x0066FF),
        EPIC(1.6f, 0xAA00FF),
        LEGENDARY(2.0f, 0xFFAA00);

        public final float statMultiplier;
        public final int color;

        ItemRarity(float mult, int color) {
            this.statMultiplier = mult;
            this.color = color;
        }
    }

    public static class WeaponStats {
        public float damage;
        public float fireRate;
        public int magazineSize;
        public float reloadTime;
        public float accuracy;
        public float range;
        public String damageType;
    }

    public static class ArmorStats {
        public float armor;
        public float shieldBonus;
        public float speedModifier;
        public String[] resistances;
    }

    public static class ConsumableEffect {
        public String effectType; // "heal", "buff", "repair"
        public float value;
        public float duration;
    }

    public boolean isStackable() {
        return maxStackSize > 1;
    }

    public boolean isEquipable() {
        return type == ItemType.WEAPON_RANGED ||
                type == ItemType.WEAPON_MELEE ||
                type == ItemType.ARMOR_HELMET ||
                type == ItemType.ARMOR_CHEST ||
                type == ItemType.ARMOR_LEGS ||
                type == ItemType.ARMOR_BOOTS;
    }

    public boolean isConsumable() {
        return type == ItemType.CONSUMABLE;
    }
}
