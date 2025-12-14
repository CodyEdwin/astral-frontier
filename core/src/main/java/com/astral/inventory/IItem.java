package com.astral.inventory;

/**
 * Base interface for all items.
 */
public interface IItem {
    String getName();
    ItemType getType();
    int getRarity();
    long getValue();
}