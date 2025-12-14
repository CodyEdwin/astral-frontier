package com.astral.inventory;

/**
 * Interface for equipment.
 */
public interface IEquipment {
    EquipmentSlot getSlot();
    ItemStack getItem();
}