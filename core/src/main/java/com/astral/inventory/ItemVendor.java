package com.astral.inventory;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents NPC vendors.
 */
public class ItemVendor {
    private String name;
    private List<ItemStack> inventory;

    public ItemVendor(String name) {
        this.name = name;
        this.inventory = new ArrayList<>();
    }

    public void addItem(ItemStack stack) {
        inventory.add(stack);
    }

    public List<ItemStack> getInventory() { return inventory; }
}