package com.astral.components;

import com.astral.ecs.Component;
import com.astral.rpg.ItemStack;
import com.badlogic.gdx.utils.Array;

/**
 * Inventory component for entities that can hold items
 */
public class InventoryComponent implements Component {

    public int slotCount;
    public ItemStack[] slots;
    public float maxWeight;
    public float currentWeight;

    public InventoryComponent() {
        this(50, 1000f); // Default 50 slots, 1000kg capacity
    }

    public InventoryComponent(int slotCount, float maxWeight) {
        this.slotCount = slotCount;
        this.slots = new ItemStack[slotCount];
        this.maxWeight = maxWeight;
        this.currentWeight = 0f;
    }

    /**
     * Add item to inventory
     * @return Number of items that couldn't be added (0 = all added)
     */
    public int addItem(ItemStack item) {
        if (item == null || item.quantity <= 0) return 0;

        int remaining = item.quantity;

        // First, try to stack with existing items
        if (item.data.maxStackSize > 1) {
            for (int i = 0; i < slotCount && remaining > 0; i++) {
                if (slots[i] != null && slots[i].data.id.equals(item.data.id)) {
                    int space = item.data.maxStackSize - slots[i].quantity;
                    int toAdd = Math.min(space, remaining);
                    slots[i].quantity += toAdd;
                    remaining -= toAdd;
                }
            }
        }

        // Then, find empty slots
        while (remaining > 0) {
            int emptySlot = findEmptySlot();
            if (emptySlot == -1) break;

            int toAdd = Math.min(item.data.maxStackSize, remaining);
            slots[emptySlot] = new ItemStack(item.data, toAdd);
            remaining -= toAdd;
        }

        recalculateWeight();
        return remaining;
    }

    /**
     * Remove item from inventory
     * @return True if successful
     */
    public boolean removeItem(String itemId, int quantity) {
        int remaining = quantity;

        for (int i = slotCount - 1; i >= 0 && remaining > 0; i--) {
            if (slots[i] != null && slots[i].data.id.equals(itemId)) {
                int toRemove = Math.min(slots[i].quantity, remaining);
                slots[i].quantity -= toRemove;
                remaining -= toRemove;

                if (slots[i].quantity <= 0) {
                    slots[i] = null;
                }
            }
        }

        recalculateWeight();
        return remaining == 0;
    }

    /**
     * Check if inventory contains item
     */
    public boolean hasItem(String itemId, int quantity) {
        int count = 0;
        for (ItemStack slot : slots) {
            if (slot != null && slot.data.id.equals(itemId)) {
                count += slot.quantity;
            }
        }
        return count >= quantity;
    }

    /**
     * Get total count of an item
     */
    public int getItemCount(String itemId) {
        int count = 0;
        for (ItemStack slot : slots) {
            if (slot != null && slot.data.id.equals(itemId)) {
                count += slot.quantity;
            }
        }
        return count;
    }

    /**
     * Get item at slot
     */
    public ItemStack getSlot(int slot) {
        if (slot >= 0 && slot < slotCount) {
            return slots[slot];
        }
        return null;
    }

    /**
     * Set item at slot
     */
    public void setSlot(int slot, ItemStack item) {
        if (slot >= 0 && slot < slotCount) {
            slots[slot] = item;
            recalculateWeight();
        }
    }

    /**
     * Swap two slots
     */
    public void swapSlots(int slot1, int slot2) {
        if (slot1 >= 0 && slot1 < slotCount && slot2 >= 0 && slot2 < slotCount) {
            ItemStack temp = slots[slot1];
            slots[slot1] = slots[slot2];
            slots[slot2] = temp;
        }
    }

    /**
     * Find first empty slot
     */
    public int findEmptySlot() {
        for (int i = 0; i < slotCount; i++) {
            if (slots[i] == null) return i;
        }
        return -1;
    }

    /**
     * Get number of used slots
     */
    public int getUsedSlots() {
        int count = 0;
        for (ItemStack slot : slots) {
            if (slot != null) count++;
        }
        return count;
    }

    /**
     * Get all items as array
     */
    public Array<ItemStack> getAllItems() {
        Array<ItemStack> items = new Array<>();
        for (ItemStack slot : slots) {
            if (slot != null) {
                items.add(slot);
            }
        }
        return items;
    }

    /**
     * Check if inventory is full
     */
    public boolean isFull() {
        return findEmptySlot() == -1;
    }

    /**
     * Check if weight limit exceeded
     */
    public boolean isOverweight() {
        return currentWeight > maxWeight;
    }

    private void recalculateWeight() {
        currentWeight = 0;
        for (ItemStack slot : slots) {
            if (slot != null) {
                currentWeight += slot.data.weight * slot.quantity;
            }
        }
    }

    @Override
    public void reset() {
        for (int i = 0; i < slotCount; i++) {
            slots[i] = null;
        }
        currentWeight = 0;
    }
}
