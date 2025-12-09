package com.astral.combat;

/**
 * Defines all weapon types available in the game
 */
public enum WeaponType {
    PLASMA_RIFLE(
        "Plasma Rifle",
        30,      // maxAmmo
        0.15f,   // fireRate (seconds between shots)
        25,      // damage
        1.5f,    // reloadTime
        0.4f, 0.7f, 0.9f  // glow color RGB
    ),
    LASER_PISTOL(
        "Laser Pistol",
        12,
        0.3f,
        40,
        1.0f,
        0.9f, 0.2f, 0.2f
    ),
    SCATTER_GUN(
        "Scatter Gun",
        8,
        0.6f,
        15,  // per pellet, fires 6 pellets
        2.0f,
        1.0f, 0.6f, 0.1f
    ),
    RAIL_CANNON(
        "Rail Cannon",
        5,
        1.2f,
        120,
        3.0f,
        0.3f, 0.9f, 0.3f
    ),
    PULSE_SMG(
        "Pulse SMG",
        50,
        0.08f,
        12,
        2.5f,
        0.8f, 0.3f, 0.9f
    );

    public final String name;
    public final int maxAmmo;
    public final float fireRate;
    public final int damage;
    public final float reloadTime;
    public final float glowR, glowG, glowB;

    WeaponType(String name, int maxAmmo, float fireRate, int damage, float reloadTime,
               float glowR, float glowG, float glowB) {
        this.name = name;
        this.maxAmmo = maxAmmo;
        this.fireRate = fireRate;
        this.damage = damage;
        this.reloadTime = reloadTime;
        this.glowR = glowR;
        this.glowG = glowG;
        this.glowB = glowB;
    }
}
