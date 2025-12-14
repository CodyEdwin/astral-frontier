package com.astral.inventory;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Manages inventory operations: sorting, searching, filtering.
 */
public class InventoryManager {
    private IInventory inventory;
    private InventoryFilter filter;

    public InventoryManager(IInventory inventory) {
        this.inventory = inventory;
        this.filter = new InventoryFilter();
    }

    public void sortInventory(SortType sortType) {
        List<ItemStack> items = new ArrayList<>(inventory.getItems());
        Comparator<ItemStack> comparator = switch (sortType) {
            case NAME -> Comparator.comparing(s -> s.getItem().getName());
            case TYPE -> Comparator.comparing(s -> s.getItem().getType().name());
            case VALUE -> Comparator.comparing(s -> s.getItem().getValue());
            case RARITY -> Comparator.comparing(s -> s.getItem().getRarity());
            case QUANTITY -> Comparator.comparing(ItemStack::getQuantity).reversed();
        };
        items.sort(comparator);

        // Clear and re-add sorted items
        clearInventory();
        for (ItemStack stack : items) {
            inventory.addItem(stack);
        }
    }

    public List<ItemStack> search(String query) {
        List<ItemStack> allItems = inventory.getItems();
        List<ItemStack> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();

        for (ItemStack stack : allItems) {
            if (stack.getItem().getName().toLowerCase().contains(lowerQuery) ||
                stack.getItem().getType().name().toLowerCase().contains(lowerQuery)) {
                results.add(stack);
            }
        }
        return results;
    }

    public List<ItemStack> filterByType(ItemType type) {
        return filter.filterByType(type, inventory.getItems());
    }

    public List<ItemStack> filterByRarity(int minRarity) {
        return inventory.getItems().stream()
            .filter(s -> s.getItem().getRarity() >= minRarity)
            .toList();
    }

    public void clearInventory() {
        List<ItemStack> items = new ArrayList<>(inventory.getItems());
        for (ItemStack stack : items) {
            inventory.removeItem(stack);
        }
    }

    public int getTotalValue() {
        return inventory.getItems().stream()
            .mapToInt(s -> (int) (s.getItem().getValue() * s.getQuantity()))
            .sum();
    }

    public double getTotalWeight() {
        return inventory.getItems().stream()
            .mapToDouble(s -> getItemWeight(s.getItem()) * s.getQuantity())
            .sum();
    }

    private double getItemWeight(IItem item) {
        // Placeholder weight calculation
        return item.getType() == ItemType.WEAPON ? 5.0 : item.getType() == ItemType.ARMOR ? 3.0 : 1.0;
    }

    public enum SortType {
        NAME, TYPE, VALUE, RARITY, QUANTITY
    }
}