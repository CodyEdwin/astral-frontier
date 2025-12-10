package com.astral.combat;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * Interface for entities that participate in combat.
 * Both enemies and players can implement this interface.
 */
public interface ICombatEntity extends Disposable {

    /**
     * Get the entity's current position
     */
    Vector3 getPosition();

    /**
     * Get the entity's current health
     */
    float getHealth();

    /**
     * Get the entity's maximum health
     */
    float getMaxHealth();

    /**
     * Check if the entity is alive
     */
    boolean isAlive();

    /**
     * Apply damage to this entity
     * @param amount Damage amount
     */
    void takeDamage(float amount);

    /**
     * Get the entity's model instance for rendering
     */
    ModelInstance getModelInstance();

    /**
     * Update the entity
     * @param delta Time since last update
     */
    void update(float delta);
}
