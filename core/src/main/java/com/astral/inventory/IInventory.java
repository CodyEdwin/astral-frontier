package com.astral.inventory;

import java.util.List;

/**
 * Interface for inventory management.
 */
public interface IInventory {
    boolean addItem(ItemStack stack);
    boolean removeItem(ItemStack stack);
    List<ItemStack> getItems();
}