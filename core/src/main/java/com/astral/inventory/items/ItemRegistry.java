package com.astral.inventory.items;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for all items.
 */
public class ItemRegistry {
    private Map<String, IItem> registry;

    public ItemRegistry() {
        registry = new HashMap<>();
    }

    /**
     * Registers an item in the registry.
     * @param item The item to register.
     */
    public void registerItem(IItem item) {
        registry.put(item.getName(), item);
    }

    /**
     * Retrieves an item by name.
     * @param name The name of the item.
     * @return The item, or null if not found.
     */
    public IItem getItem(String name) {
        return registry.get(name);
    }

    /**
     * Gets all registered items.
     * @return A map of item names to items.
     */
    public Map<String, IItem> getAllItems() {
        return new HashMap<>(registry);
    }
}