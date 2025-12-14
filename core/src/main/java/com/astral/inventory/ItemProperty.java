package com.astral.inventory;

/**
 * Properties of an item.
 */
public class ItemProperty {
    private int durability;
    private double weight;
    private long value;

    // Getters/setters
    public int getDurability() { return durability; }
    public void setDurability(int durability) { this.durability = durability; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public long getValue() { return value; }
    public void setValue(long value) { this.value = value; }
}