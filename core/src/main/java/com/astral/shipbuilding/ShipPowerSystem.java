package com.astral.shipbuilding;

/**
 * Starfield-style power allocation system.
 * Power from reactor is distributed between weapons, shields, engines, and grav drive.
 */
public class ShipPowerSystem {

    public enum ShipClass {
        CLASS_A("A", 12, 20000),   // Light ships, low mass limit
        CLASS_B("B", 24, 50000),   // Medium ships
        CLASS_C("C", 36, 100000);  // Heavy capital ships

        private final String label;
        private final int maxPower;
        private final float maxMass;

        ShipClass(String label, int maxPower, float maxMass) {
            this.label = label;
            this.maxPower = maxPower;
            this.maxMass = maxMass;
        }

        public String getLabel() { return label; }
        public int getMaxPower() { return maxPower; }
        public float getMaxMass() { return maxMass; }
    }

    // Power allocation (each bar = 1 power unit)
    private int weaponPower = 0;
    private int shieldPower = 0;
    private int enginePower = 0;
    private int gravDrivePower = 0;

    // Reactor stats
    private ShipClass shipClass = ShipClass.CLASS_A;
    private int reactorPower = 12;
    private int usedPower = 0;

    // Component requirements
    private int weaponPowerReq = 0;
    private int shieldPowerReq = 0;
    private int enginePowerReq = 0;
    private int gravDrivePowerReq = 0;

    public ShipPowerSystem() {
        resetAllocation();
    }

    /**
     * Set ship class based on reactor type
     */
    public void setReactor(ShipPartType reactorType) {
        switch (reactorType) {
            case REACTOR_CLASS_A:
                shipClass = ShipClass.CLASS_A;
                reactorPower = 12;
                break;
            case REACTOR_CLASS_B:
                shipClass = ShipClass.CLASS_B;
                reactorPower = 24;
                break;
            case REACTOR_CLASS_C:
                shipClass = ShipClass.CLASS_C;
                reactorPower = 36;
                break;
            default:
                break;
        }
        clampAllocation();
    }

    /**
     * Set power requirements based on installed components
     */
    public void setRequirements(int weapons, int shields, int engines, int gravDrive) {
        this.weaponPowerReq = weapons;
        this.shieldPowerReq = shields;
        this.enginePowerReq = engines;
        this.gravDrivePowerReq = gravDrive;
    }

    /**
     * Allocate power to weapons (0-6 bars typically)
     */
    public void setWeaponPower(int power) {
        this.weaponPower = Math.min(power, Math.min(weaponPowerReq, 6));
        updateUsedPower();
    }

    public void setShieldPower(int power) {
        this.shieldPower = Math.min(power, Math.min(shieldPowerReq, 6));
        updateUsedPower();
    }

    public void setEnginePower(int power) {
        this.enginePower = Math.min(power, Math.min(enginePowerReq, 6));
        updateUsedPower();
    }

    public void setGravDrivePower(int power) {
        this.gravDrivePower = Math.min(power, Math.min(gravDrivePowerReq, 6));
        updateUsedPower();
    }

    /**
     * Increment power to a system
     */
    public boolean incrementWeaponPower() {
        if (usedPower < reactorPower && weaponPower < weaponPowerReq && weaponPower < 6) {
            weaponPower++;
            updateUsedPower();
            return true;
        }
        return false;
    }

    public boolean incrementShieldPower() {
        if (usedPower < reactorPower && shieldPower < shieldPowerReq && shieldPower < 6) {
            shieldPower++;
            updateUsedPower();
            return true;
        }
        return false;
    }

    public boolean incrementEnginePower() {
        if (usedPower < reactorPower && enginePower < enginePowerReq && enginePower < 6) {
            enginePower++;
            updateUsedPower();
            return true;
        }
        return false;
    }

    public boolean incrementGravDrivePower() {
        if (usedPower < reactorPower && gravDrivePower < gravDrivePowerReq && gravDrivePower < 6) {
            gravDrivePower++;
            updateUsedPower();
            return true;
        }
        return false;
    }

    /**
     * Decrement power from a system
     */
    public boolean decrementWeaponPower() {
        if (weaponPower > 0) {
            weaponPower--;
            updateUsedPower();
            return true;
        }
        return false;
    }

    public boolean decrementShieldPower() {
        if (shieldPower > 0) {
            shieldPower--;
            updateUsedPower();
            return true;
        }
        return false;
    }

    public boolean decrementEnginePower() {
        if (enginePower > 0) {
            enginePower--;
            updateUsedPower();
            return true;
        }
        return false;
    }

    public boolean decrementGravDrivePower() {
        if (gravDrivePower > 0) {
            gravDrivePower--;
            updateUsedPower();
            return true;
        }
        return false;
    }

    private void updateUsedPower() {
        usedPower = weaponPower + shieldPower + enginePower + gravDrivePower;
    }

    private void clampAllocation() {
        while (usedPower > reactorPower) {
            // Remove power from lowest priority first
            if (gravDrivePower > 0) gravDrivePower--;
            else if (shieldPower > 0) shieldPower--;
            else if (weaponPower > 0) weaponPower--;
            else if (enginePower > 0) enginePower--;
            updateUsedPower();
        }
    }

    /**
     * Reset to balanced allocation
     */
    public void resetAllocation() {
        int perSystem = reactorPower / 4;
        weaponPower = Math.min(perSystem, weaponPowerReq);
        shieldPower = Math.min(perSystem, shieldPowerReq);
        enginePower = Math.min(perSystem, enginePowerReq);
        gravDrivePower = Math.min(perSystem, gravDrivePowerReq);
        updateUsedPower();
    }

    /**
     * Get effectiveness multiplier for weapons (0.0 - 1.0)
     */
    public float getWeaponEffectiveness() {
        return weaponPowerReq > 0 ? (float) weaponPower / weaponPowerReq : 0;
    }

    public float getShieldEffectiveness() {
        return shieldPowerReq > 0 ? (float) shieldPower / shieldPowerReq : 0;
    }

    public float getEngineEffectiveness() {
        return enginePowerReq > 0 ? (float) enginePower / enginePowerReq : 0;
    }

    public float getGravDriveEffectiveness() {
        return gravDrivePowerReq > 0 ? (float) gravDrivePower / gravDrivePowerReq : 0;
    }

    // Getters
    public ShipClass getShipClass() { return shipClass; }
    public int getReactorPower() { return reactorPower; }
    public int getUsedPower() { return usedPower; }
    public int getAvailablePower() { return reactorPower - usedPower; }

    public int getWeaponPower() { return weaponPower; }
    public int getShieldPower() { return shieldPower; }
    public int getEnginePower() { return enginePower; }
    public int getGravDrivePower() { return gravDrivePower; }

    public int getWeaponPowerReq() { return weaponPowerReq; }
    public int getShieldPowerReq() { return shieldPowerReq; }
    public int getEnginePowerReq() { return enginePowerReq; }
    public int getGravDrivePowerReq() { return gravDrivePowerReq; }

    /**
     * Check if ship mass is valid for current class
     */
    public boolean isValidMass(float mass) {
        return mass <= shipClass.getMaxMass();
    }
}
