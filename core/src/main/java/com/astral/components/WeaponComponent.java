package com.astral.components;

import com.astral.combat.WeaponMount;
import com.astral.ecs.Component;

/**
 * Component for entities with weapons
 */
public class WeaponComponent implements Component {

    public WeaponMount[] mounts;
    public int weaponCount;

    // Active weapon group
    public int activeGroup = 0;
    public int[][] weaponGroups; // Groups of weapon indices

    // Targeting
    public int targetEntityId = -1;
    public float targetDistance;
    public boolean targetLocked = false;

    public WeaponComponent() {
        this(4); // Default 4 weapon slots
    }

    public WeaponComponent(int maxWeapons) {
        mounts = new WeaponMount[maxWeapons];
        weaponGroups = new int[4][]; // 4 weapon groups
    }

    public void addWeapon(WeaponMount mount) {
        for (int i = 0; i < mounts.length; i++) {
            if (mounts[i] == null) {
                mounts[i] = mount;
                weaponCount++;
                return;
            }
        }
    }

    public void removeWeapon(int slot) {
        if (slot >= 0 && slot < mounts.length) {
            mounts[slot] = null;
            weaponCount--;
        }
    }

    public WeaponMount getWeapon(int slot) {
        if (slot >= 0 && slot < mounts.length) {
            return mounts[slot];
        }
        return null;
    }

    public void setFiring(boolean firing) {
        for (WeaponMount mount : mounts) {
            if (mount != null) {
                mount.isFiring = firing;
            }
        }
    }

    public void setGroupFiring(int group, boolean firing) {
        if (group >= 0 && group < weaponGroups.length && weaponGroups[group] != null) {
            for (int idx : weaponGroups[group]) {
                if (idx >= 0 && idx < mounts.length && mounts[idx] != null) {
                    mounts[idx].isFiring = firing;
                }
            }
        }
    }

    public void update(float deltaTime) {
        for (WeaponMount mount : mounts) {
            if (mount != null) {
                mount.coolDown(deltaTime);
            }
        }
    }

    @Override
    public void reset() {
        for (int i = 0; i < mounts.length; i++) {
            mounts[i] = null;
        }
        weaponCount = 0;
        activeGroup = 0;
        targetEntityId = -1;
        targetDistance = 0;
        targetLocked = false;
    }
}
