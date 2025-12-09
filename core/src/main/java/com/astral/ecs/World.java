package com.astral.ecs;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * ECS World - manages entities, components, and systems
 */
public class World implements Disposable {

    private final IntMap<Entity> entities;
    private final Array<GameSystem> systems;
    private final Array<Entity> pendingAdd;
    private final Array<Entity> pendingRemove;
    private final EventBus eventBus;

    private int nextEntityId = 0;

    public World() {
        entities = new IntMap<>();
        systems = new Array<>();
        pendingAdd = new Array<>();
        pendingRemove = new Array<>();
        eventBus = new EventBus();
    }

    /**
     * Create a new entity
     */
    public Entity createEntity() {
        Entity entity = new Entity(nextEntityId++);
        pendingAdd.add(entity);
        return entity;
    }

    /**
     * Destroy an entity
     */
    public void destroyEntity(Entity entity) {
        pendingRemove.add(entity);
    }

    /**
     * Get entity by ID
     */
    public Entity getEntity(int id) {
        return entities.get(id);
    }

    /**
     * Add a system
     */
    public void addSystem(GameSystem system) {
        systems.add(system);
        systems.sort((a, b) -> b.getPriority() - a.getPriority());
        system.initialize();
    }

    /**
     * Get all entities with the specified component types
     */
    @SafeVarargs
    public final Array<Entity> getEntitiesWith(Class<? extends Component>... types) {
        Array<Entity> result = new Array<>();

        for (Entity entity : entities.values()) {
            if (!entity.isActive()) continue;

            boolean hasAll = true;
            for (Class<? extends Component> type : types) {
                if (!entity.has(type)) {
                    hasAll = false;
                    break;
                }
            }

            if (hasAll) {
                result.add(entity);
            }
        }

        return result;
    }

    /**
     * Get all active entities
     */
    public Array<Entity> getAllEntities() {
        Array<Entity> result = new Array<>();
        for (Entity entity : entities.values()) {
            if (entity.isActive()) {
                result.add(entity);
            }
        }
        return result;
    }

    /**
     * Process pending entity operations
     */
    public void processPending() {
        // Add pending entities
        for (Entity entity : pendingAdd) {
            entities.put(entity.getId(), entity);
        }
        pendingAdd.clear();

        // Remove pending entities
        for (Entity entity : pendingRemove) {
            entities.remove(entity.getId());
        }
        pendingRemove.clear();
    }

    /**
     * Update all systems
     */
    public void update(float deltaTime) {
        processPending();

        for (GameSystem system : systems) {
            if (system.isEnabled()) {
                system.update(deltaTime);
            }
        }

        eventBus.dispatchQueued();
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public int getEntityCount() {
        return entities.size;
    }

    /**
     * Clear all entities (for screen transitions)
     */
    public void clearEntities() {
        for (Entity entity : entities.values()) {
            entity.setActive(false);
        }
        entities.clear();
        pendingAdd.clear();
        pendingRemove.clear();
    }

    @Override
    public void dispose() {
        for (GameSystem system : systems) {
            system.dispose();
        }
        systems.clear();
        entities.clear();
    }
}
