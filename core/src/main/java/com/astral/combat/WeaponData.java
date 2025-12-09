package com.astral.combat;

/**
 * Weapon definition data
 */
public class WeaponData {

    public String id;
    public String name;
    public String description;
    public WeaponType type;

    // Damage
    public float damage;
    public String damageType; // "kinetic", "energy", "explosive", "plasma"

    // Fire rate
    public float fireRate;          // Rounds per second
    public float burstCount = 1;    // Shots per trigger pull
    public float burstDelay = 0.1f; // Delay between burst shots

    // Projectile
    public float projectileSpeed;
    public float range;
    public float spread;            // Degrees

    // Resource cost
    public float energyCost;
    public int ammoCost;
    public float heatGenerated;

    // Tracking (for missiles)
    public boolean tracking = false;
    public float lockOnTime = 2f;
    public float trackingRate = 90f; // Degrees per second

    // Effects
    public String[] effects;        // Special effects like "PIERCE_SHIELDS"

    // Visuals
    public String projectileModel;
    public String muzzleFlash;
    public String impactEffect;
    public String fireSound;

    public enum WeaponType {
        LASER,
        RAILGUN,
        CANNON,
        MISSILE,
        TORPEDO,
        BEAM,
        PLASMA
    }

    public float getFireInterval() {
        return 1f / fireRate;
    }

    public static WeaponData createLaserMk1() {
        WeaponData w = new WeaponData();
        w.id = "laser_mk1";
        w.name = "Laser Cannon Mk.I";
        w.type = WeaponType.LASER;
        w.damage = 25f;
        w.damageType = "energy";
        w.fireRate = 4f;
        w.projectileSpeed = 2000f;
        w.range = 3000f;
        w.spread = 0.5f;
        w.energyCost = 10f;
        w.heatGenerated = 15f;
        w.effects = new String[]{"PIERCE_SHIELDS_10"};
        return w;
    }

    public static WeaponData createRailgunMk1() {
        WeaponData w = new WeaponData();
        w.id = "railgun_mk1";
        w.name = "Railgun Mk.I";
        w.type = WeaponType.RAILGUN;
        w.damage = 150f;
        w.damageType = "kinetic";
        w.fireRate = 0.5f;
        w.projectileSpeed = 5000f;
        w.range = 8000f;
        w.spread = 0.1f;
        w.ammoCost = 1;
        w.heatGenerated = 50f;
        w.effects = new String[]{"ARMOR_PIERCE_25"};
        return w;
    }

    public static WeaponData createSeekerMissile() {
        WeaponData w = new WeaponData();
        w.id = "missile_seeker";
        w.name = "Seeker Missile";
        w.type = WeaponType.MISSILE;
        w.damage = 200f;
        w.damageType = "explosive";
        w.fireRate = 0.3f;
        w.projectileSpeed = 500f;
        w.range = 10000f;
        w.tracking = true;
        w.lockOnTime = 2f;
        w.ammoCost = 1;
        w.effects = new String[]{"EXPLOSIVE_RADIUS_20"};
        return w;
    }
}
