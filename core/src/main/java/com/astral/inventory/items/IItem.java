package com.astral.inventory.items;

/**
 * Interface for all items.
 */
public interface IItem {
    String getName();
    ItemType getType();
    int getRarity();
    long getValue();
}