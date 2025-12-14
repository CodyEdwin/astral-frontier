package com.astral.inventory;

/**
 * Generates tooltips for items.
 */
public class ItemTooltipGenerator {
    public String generateTooltip(IItem item) {
        return item.getName() + " - " + item.getType();
    }
}