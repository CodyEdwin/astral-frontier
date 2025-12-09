package com.astral.ecs;

/**
 * Base interface for all ECS components.
 * Components are pure data containers with no logic.
 */
public interface Component {

    /**
     * Reset component to default state for pooling
     */
    default void reset() {}
}
