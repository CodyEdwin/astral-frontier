package com.astral.inventory;

import java.util.List;

/**
 * Filters for inventory.
 */
public class InventoryFilter {
    public List<ItemStack> filterByType(ItemType type, List<ItemStack> items) {
        return items.stream().filter(s -> s.getItem().getType() == type).toList();
    }
}