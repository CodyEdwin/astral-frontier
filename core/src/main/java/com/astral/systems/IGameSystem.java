package com.astral.systems;

import com.badlogic.gdx.utils.Disposable;

/**
 * Interface for all game systems.
 * Systems contain logic and operate on game state.
 */
public interface IGameSystem extends Disposable {

    /**
     * Initialize the system
     */
    void initialize();

    /**
     * Update the system
     * @param deltaTime Time since last update
     */
    void update(float deltaTime);

    /**
     * Check if system is enabled
     */
    boolean isEnabled();

    /**
     * Enable or disable the system
     */
    void setEnabled(boolean enabled);

    /**
     * Get system priority for update ordering
     */
    int getPriority();
}
