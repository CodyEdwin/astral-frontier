package com.astral.ecs;

import com.badlogic.gdx.utils.ObjectMap;
import java.util.UUID;

/**
 * Entity in the ECS - a container for components
 */
public class Entity {

    private final int id;
    private final UUID uuid;
    private final ObjectMap<Class<? extends Component>, Component> components;
    private boolean active = true;
    private String tag;

    public Entity(int id) {
        this.id = id;
        this.uuid = UUID.randomUUID();
        this.components = new ObjectMap<>();
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public <T extends Component> Entity add(T component) {
        components.put(component.getClass(), component);
        return this;
    }

    public <T extends Component> T get(Class<T> type) {
        return type.cast(components.get(type));
    }

    public <T extends Component> boolean has(Class<T> type) {
        return components.containsKey(type);
    }

    public <T extends Component> T remove(Class<T> type) {
        return type.cast(components.remove(type));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getTag() {
        return tag;
    }

    public Entity setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public Iterable<Component> getAllComponents() {
        return components.values();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Entity other) {
            return this.id == other.id;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "Entity[" + id + (tag != null ? ":" + tag : "") + "]";
    }
}
