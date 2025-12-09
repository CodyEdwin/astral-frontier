package com.astral.ecs;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Event bus for decoupled communication between systems
 */
public class EventBus {

    private final ObjectMap<Class<?>, Array<EventListener<?>>> listeners;
    private final Array<Object> queuedEvents;

    public EventBus() {
        listeners = new ObjectMap<>();
        queuedEvents = new Array<>();
    }

    /**
     * Register a listener for a specific event type
     */
    public <T> void register(Class<T> eventType, EventListener<T> listener) {
        Array<EventListener<?>> typeListeners = listeners.get(eventType);
        if (typeListeners == null) {
            typeListeners = new Array<>();
            listeners.put(eventType, typeListeners);
        }
        typeListeners.add(listener);
    }

    /**
     * Unregister a listener
     */
    public <T> void unregister(Class<T> eventType, EventListener<T> listener) {
        Array<EventListener<?>> typeListeners = listeners.get(eventType);
        if (typeListeners != null) {
            typeListeners.removeValue(listener, true);
        }
    }

    /**
     * Post an event immediately
     */
    @SuppressWarnings("unchecked")
    public <T> void post(T event) {
        Array<EventListener<?>> typeListeners = listeners.get(event.getClass());
        if (typeListeners != null) {
            for (EventListener<?> listener : typeListeners) {
                ((EventListener<T>) listener).onEvent(event);
            }
        }
    }

    /**
     * Queue an event for later dispatch
     */
    public void queue(Object event) {
        queuedEvents.add(event);
    }

    /**
     * Dispatch all queued events
     */
    public void dispatchQueued() {
        for (Object event : queuedEvents) {
            post(event);
        }
        queuedEvents.clear();
    }

    public interface EventListener<T> {
        void onEvent(T event);
    }
}
