package com.astral.combat;

import com.badlogic.gdx.math.Vector3;

/**
 * A weapon mounting point on a ship
 */
public class WeaponMount {

    public String id;
    public MountType type;
    public MountSize size;

    // Position relative to ship center
    public final Vector3 localPosition = new Vector3();
    public final Vector3 localRotation = new Vector3(); // Euler angles

    // Equipped weapon
    public WeaponData weaponData;

    // State
    public boolean isFiring = false;
    public long lastFireTime = 0;
    public float currentHeat = 0f;
    public int currentAmmo = 0;
    public int maxAmmo = 100;

    // Tracking state (for turrets/missiles)
    public int targetEntityId = -1;
    public float lockProgress = 0f;

    public enum MountType {
        FIXED,          // Fixed forward
        GIMBAL,         // Limited rotation
        TURRET,         // Full rotation
        MISSILE_BAY     // Missile/torpedo bay
    }

    public enum MountSize {
        SMALL(1),
        MEDIUM(2),
        LARGE(3),
        HUGE(4);

        public final int value;

        MountSize(int value) {
            this.value = value;
        }
    }

    public boolean canFire() {
        if (weaponData == null) return false;

        // Check fire rate
        long now = System.currentTimeMillis();
        float interval = weaponData.getFireInterval() * 1000;
        if (now - lastFireTime < interval) return false;

        // Check ammo
        if (weaponData.ammoCost > 0 && currentAmmo < weaponData.ammoCost) return false;

        // Check heat (overheat threshold at 100)
        if (currentHeat >= 100f) return false;

        return true;
    }

    public void consumeResources() {
        if (weaponData == null) return;

        if (weaponData.ammoCost > 0) {
            currentAmmo -= weaponData.ammoCost;
        }

        currentHeat += weaponData.heatGenerated;
    }

    public void coolDown(float deltaTime) {
        // Cool down at 10 heat per second
        currentHeat = Math.max(0, currentHeat - 10f * deltaTime);
    }

    public void reload() {
        currentAmmo = maxAmmo;
    }

    public float getHeatPercent() {
        return currentHeat / 100f;
    }

    public boolean isOverheated() {
        return currentHeat >= 100f;
    }
}
