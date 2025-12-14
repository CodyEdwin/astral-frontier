package com.astral.inventory.core;

import com.astral.inventory.IInventory;
import com.astral.inventory.ItemStack;
import com.astral.inventory.InventoryCapacity;
import com.astral.inventory.ItemType;
import java.util.ArrayList;
import java.util.List;

/**
 * Grid-based inventory with stacking and placement logic.
 */
public class InventoryGrid implements IInventory {
    private ItemStack[][] grid;
    private int width, height;
    private InventoryCapacity capacity;

    public InventoryGrid(int width, int height) {
        this.width = width;
        this.height = height;
        grid = new ItemStack[width][height];
        capacity = new InventoryCapacity();
    }

    @Override
    public boolean addItem(ItemStack stack) {
        if (stack == null || stack.getQuantity() <= 0) return false;

        // Try to stack with existing items first
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[x][y] != null && canStack(grid[x][y], stack)) {
                    int space = getMaxStackSize(grid[x][y]) - grid[x][y].getQuantity();
                    int toAdd = Math.min(space, stack.getQuantity());
                    grid[x][y].setQuantity(grid[x][y].getQuantity() + toAdd);
                    stack.setQuantity(stack.getQuantity() - toAdd);
                    if (stack.getQuantity() <= 0) return true;
                }
            }
        }

        // Find empty slots
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[x][y] == null) {
                    int toAdd = Math.min(getMaxStackSize(stack), stack.getQuantity());
                    grid[x][y] = new ItemStack(stack.getItem(), toAdd);
                    stack.setQuantity(stack.getQuantity() - toAdd);
                    if (stack.getQuantity() <= 0) return true;
                }
            }
        }

        return stack.getQuantity() == 0;
    }

    @Override
    public boolean removeItem(ItemStack stack) {
        if (stack == null || stack.getQuantity() <= 0) return false;

        int remaining = stack.getQuantity();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[x][y] != null && grid[x][y].getItem().equals(stack.getItem())) {
                    int toRemove = Math.min(grid[x][y].getQuantity(), remaining);
                    grid[x][y].setQuantity(grid[x][y].getQuantity() - toRemove);
                    remaining -= toRemove;
                    if (grid[x][y].getQuantity() <= 0) {
                        grid[x][y] = null;
                    }
                    if (remaining <= 0) return true;
                }
            }
        }
        return remaining == 0;
    }

    @Override
    public List<ItemStack> getItems() {
        List<ItemStack> items = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (grid[x][y] != null) {
                    items.add(grid[x][y]);
                }
            }
        }
        return items;
    }

    public ItemStack getItemAt(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return grid[x][y];
        }
        return null;
    }

    public boolean setItemAt(int x, int y, ItemStack stack) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[x][y] = stack;
            return true;
        }
        return false;
    }

    public boolean canPlaceAt(int x, int y, ItemStack stack) {
        return x >= 0 && x < width && y >= 0 && y < height && (grid[x][y] == null || canStack(grid[x][y], stack));
    }

    private boolean canStack(ItemStack existing, ItemStack newStack) {
        return existing.getItem().equals(newStack.getItem()) && existing.getQuantity() < getMaxStackSize(existing);
    }

    private int getMaxStackSize(ItemStack stack) {
        // Placeholder: assume 99 for stackable items
        return stack.getItem().getType() == ItemType.CONSUMABLE ? 99 : 1;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}