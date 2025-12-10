package com.astral.combat;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * Interface for projectile entities in combat.
 */
public interface IProjectile extends Disposable {

    /**
     * Get the projectile's current position
     */
    Vector3 getPosition();

    /**
     * Get the projectile's direction
     */
    Vector3 getDirection();

    /**
     * Get the projectile's speed
     */
    float getSpeed();

    /**
     * Get the damage this projectile deals
     */
    float getDamage();

    /**
     * Check if the projectile is still active
     */
    boolean isAlive();

    /**
     * Update the projectile
     * @param delta Time since last update
     */
    void update(float delta);

    /**
     * Check if the projectile hit terrain at given height
     * @param terrainHeight Height of terrain at projectile position
     * @return true if hit terrain
     */
    boolean checkTerrainHit(float terrainHeight);

    /**
     * Check if the projectile hit an enemy
     * @param enemy The enemy to check against
     * @return true if hit
     */
    boolean checkHit(ICombatEntity enemy);

    /**
     * Get the model instance for rendering
     */
    ModelInstance getModelInstance();
}
