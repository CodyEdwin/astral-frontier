package com.astral.components;

import com.astral.ecs.Component;
import java.util.UUID;

/**
 * Player component - player-specific data
 */
public class PlayerComponent implements Component {

    public UUID playerId;
    public String playerName = "Pilot";
    public boolean isLocalPlayer = false;

    // Player state
    public PlayerState state = PlayerState.SHIP;

    // Health and survival
    public float health = 100f;
    public float maxHealth = 100f;
    public float armor = 0f;
    public float oxygen = 100f;
    public float maxOxygen = 100f;
    public float stamina = 100f;
    public float maxStamina = 100f;

    // Equipment slots
    public int currentWeaponSlot = 0;

    // Experience and level
    public int level = 1;
    public long experience = 0;
    public long experienceToNextLevel = 1000;

    // Currency
    public long credits = 5000;

    // Reputation
    public int reputation = 0;

    public enum PlayerState {
        SHIP,           // Piloting ship
        FPS,            // On foot
        MENU,           // In menu
        DIALOGUE,       // In dialogue
        DOCKED,         // Docked at station
        DEAD            // Dead/respawning
    }

    public float getHealthPercent() {
        return health / maxHealth;
    }

    public float getOxygenPercent() {
        return oxygen / maxOxygen;
    }

    public float getStaminaPercent() {
        return stamina / maxStamina;
    }

    public void damage(float amount) {
        // Armor reduces damage
        float damageReduction = armor / (armor + 100f);
        float actualDamage = amount * (1f - damageReduction);
        health = Math.max(0, health - actualDamage);
    }

    public void heal(float amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void addExperience(long amount) {
        experience += amount;
        while (experience >= experienceToNextLevel) {
            experience -= experienceToNextLevel;
            level++;
            experienceToNextLevel = calculateExpForLevel(level + 1);
        }
    }

    private long calculateExpForLevel(int lvl) {
        return (long) (1000 * Math.pow(1.5, lvl - 1));
    }

    @Override
    public void reset() {
        playerId = null;
        playerName = "Pilot";
        isLocalPlayer = false;
        state = PlayerState.SHIP;
        health = maxHealth = 100f;
        armor = 0f;
        oxygen = maxOxygen = 100f;
        stamina = maxStamina = 100f;
        currentWeaponSlot = 0;
        level = 1;
        experience = 0;
        experienceToNextLevel = 1000;
        credits = 5000;
        reputation = 0;
    }
}
