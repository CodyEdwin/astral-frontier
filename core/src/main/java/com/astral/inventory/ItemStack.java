package com.astral.inventory;

/**
 * Represents a stack of items.
 */
public class ItemStack {
    private IItem item;
    private int quantity;

    public ItemStack(IItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    // Getters/setters
    public IItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}