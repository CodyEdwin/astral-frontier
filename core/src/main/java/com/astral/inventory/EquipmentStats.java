package com.astral.inventory;

/**
 * Stats modifiers from equipment.
 */
public class EquipmentStats {
    private int healthBonus;
    private int speedBonus;
    private int damageBonus;

    // Getters/setters
    public int getHealthBonus() { return healthBonus; }
    public void setHealthBonus(int healthBonus) { this.healthBonus = healthBonus; }

    public int getSpeedBonus() { return speedBonus; }
    public void setSpeedBonus(int speedBonus) { this.speedBonus = speedBonus; }

    public int getDamageBonus() { return damageBonus; }
    public void setDamageBonus(int damageBonus) { this.damageBonus = damageBonus; }
}