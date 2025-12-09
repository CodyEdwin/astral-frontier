package com.astral.ecs;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Base class for all game systems in the ECS.
 * Systems contain logic and operate on entities with specific components.
 */
public abstract class GameSystem implements Disposable {

    protected World world;
    protected boolean enabled = true;
    protected int priority = 0;

    public GameSystem(World world) {
        this.world = world;
    }

    /**
     * Initialize the system
     */
    public void initialize() {}

    /**
     * Update the system
     * @param deltaTime Time since last update
     */
    public abstract void update(float deltaTime);

    /**
     * Get the required component types for this system
     */
    public Class<? extends Component>[] getRequiredComponents() {
        return null;
    }

    /**
     * Get all entities that have the required components
     */
    @SafeVarargs
    protected final Array<Entity> getEntitiesWith(Class<? extends Component>... componentTypes) {
        return world.getEntitiesWith(componentTypes);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public void dispose() {}
}
