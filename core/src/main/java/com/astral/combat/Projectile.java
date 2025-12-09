package com.astral.combat;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

/**
 * Projectile data for combat
 */
public class Projectile implements Pool.Poolable {

    public int id;
    public int ownerId;

    public final Vector3 position = new Vector3();
    public final Vector3 previousPosition = new Vector3();
    public final Vector3 velocity = new Vector3();

    public float damage;
    public float speed;
    public float lifetime;
    public String damageType;

    // For homing missiles
    public boolean homing = false;
    public int targetId = -1;
    public float turnRate = 0f;

    // Visual
    public String trailEffect;
    public float trailWidth = 0.5f;

    @Override
    public void reset() {
        id = 0;
        ownerId = 0;
        position.setZero();
        previousPosition.setZero();
        velocity.setZero();
        damage = 0;
        speed = 0;
        lifetime = 0;
        damageType = null;
        homing = false;
        targetId = -1;
        turnRate = 0;
        trailEffect = null;
        trailWidth = 0.5f;
    }
}
