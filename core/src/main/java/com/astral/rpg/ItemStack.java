package com.astral.rpg;

/**
 * A stack of items in inventory
 */
public class ItemStack {

    public ItemData data;
    public int quantity;
    public ItemInstance instance; // For unique items with durability, mods, etc.

    public ItemStack() {
    }

    public ItemStack(ItemData data, int quantity) {
        this.data = data;
        this.quantity = quantity;
    }

    public ItemStack(ItemData data, int quantity, ItemInstance instance) {
        this.data = data;
        this.quantity = quantity;
        this.instance = instance;
    }

    public boolean canStackWith(ItemStack other) {
        if (other == null) return false;
        if (!data.id.equals(other.data.id)) return false;
        if (instance != null || other.instance != null) return false; // Unique items don't stack
        return true;
    }

    public int getMaxStack() {
        return data != null ? data.maxStackSize : 1;
    }

    public float getTotalWeight() {
        return data != null ? data.weight * quantity : 0;
    }

    public int getTotalValue() {
        return data != null ? data.baseValue * quantity : 0;
    }

    public ItemStack copy() {
        ItemStack copy = new ItemStack(data, quantity);
        if (instance != null) {
            copy.instance = instance.copy();
        }
        return copy;
    }

    public ItemStack split(int amount) {
        if (amount >= quantity) {
            return null; // Can't split more than we have
        }

        ItemStack split = new ItemStack(data, amount);
        this.quantity -= amount;
        return split;
    }

    @Override
    public String toString() {
        return String.format("ItemStack[%s x%d]", data != null ? data.id : "null", quantity);
    }
}
