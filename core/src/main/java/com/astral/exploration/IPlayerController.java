package com.astral.exploration;

import com.badlogic.gdx.math.Vector3;

/**
 * Interface for player controller implementations.
 * Handles player movement, physics, and state.
 */
public interface IPlayerController {

    /**
     * Get the player's current position
     */
    Vector3 getPosition();

    /**
     * Set the player's position
     */
    void setPosition(float x, float y, float z);

    /**
     * Get the player's current velocity
     */
    Vector3 getVelocity();

    /**
     * Check if player is on the ground
     */
    boolean isGrounded();

    /**
     * Get camera yaw angle in degrees
     */
    float getCameraYaw();

    /**
     * Get camera pitch angle in degrees
     */
    float getCameraPitch();

    /**
     * Get player height
     */
    float getPlayerHeight();

    /**
     * Get eye height (camera position relative to feet)
     */
    float getEyeHeight();
}
