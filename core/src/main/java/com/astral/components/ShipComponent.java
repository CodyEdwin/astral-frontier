package com.astral.components;

import com.astral.ecs.Component;

/**
 * Ship-specific component - contains all ship data
 */
public class ShipComponent implements Component {

    // Ship class/type
    public String shipClassId;
    public ShipClass shipClass;

    // Hull and shields
    public float hullIntegrity = 100f;
    public float maxHull = 100f;
    public float shieldStrength = 50f;
    public float maxShield = 50f;
    public float shieldRegenRate = 5f;
    public float shieldRegenDelay = 3f;
    public float timeSinceLastDamage = 0f;

    // Fuel
    public float fuel = 100f;
    public float maxFuel = 100f;
    public float fuelConsumption = 0.5f;

    // Propulsion
    public float mainThrust = 400000f;
    public float maneuverThrust = 120000f;
    public float boostMultiplier = 2.5f;
    public float maxSpeed = 800f;

    // Handling
    public float pitchRate = 2.0f;
    public float yawRate = 1.8f;
    public float rollRate = 2.5f;
    public float linearDamping = 0.15f;
    public float angularDamping = 0.9f;

    // Mass
    public float mass = 10000f;

    // State
    public boolean landingGearDeployed = false;
    public boolean isLanded = false;
    public boolean isDocked = false;
    public boolean enginesOn = true;
    public boolean boostActive = false;

    // Cargo
    public float cargoCapacity = 50f;
    public float currentCargo = 0f;

    public enum ShipClass {
        FIGHTER,
        INTERCEPTOR,
        BOMBER,
        FREIGHTER,
        CORVETTE,
        CRUISER,
        CARRIER,
        EXPLORER
    }

    public float getHullPercent() {
        return hullIntegrity / maxHull;
    }

    public float getShieldPercent() {
        return shieldStrength / maxShield;
    }

    public float getFuelPercent() {
        return fuel / maxFuel;
    }

    public void damage(float amount) {
        timeSinceLastDamage = 0f;

        // Shields absorb damage first
        if (shieldStrength > 0) {
            float shieldDamage = Math.min(amount, shieldStrength);
            shieldStrength -= shieldDamage;
            amount -= shieldDamage * 0.8f; // Shields absorb 80%
        }

        // Remaining damage hits hull
        hullIntegrity = Math.max(0, hullIntegrity - amount);
    }

    public boolean isDestroyed() {
        return hullIntegrity <= 0;
    }

    public void updateShields(float deltaTime) {
        timeSinceLastDamage += deltaTime;

        if (timeSinceLastDamage >= shieldRegenDelay && shieldStrength < maxShield) {
            shieldStrength = Math.min(maxShield, shieldStrength + shieldRegenRate * deltaTime);
        }
    }

    @Override
    public void reset() {
        shipClassId = null;
        shipClass = null;
        hullIntegrity = maxHull = 100f;
        shieldStrength = maxShield = 50f;
        shieldRegenRate = 5f;
        fuel = maxFuel = 100f;
        fuelConsumption = 0.5f;
        mainThrust = 400000f;
        maneuverThrust = 120000f;
        boostMultiplier = 2.5f;
        maxSpeed = 800f;
        pitchRate = 2.0f;
        yawRate = 1.8f;
        rollRate = 2.5f;
        linearDamping = 0.15f;
        angularDamping = 0.9f;
        mass = 10000f;
        landingGearDeployed = false;
        isLanded = false;
        isDocked = false;
        enginesOn = true;
        boostActive = false;
        cargoCapacity = 50f;
        currentCargo = 0f;
    }
}
