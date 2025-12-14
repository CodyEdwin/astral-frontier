package com.astral.inventory;

import java.util.List;

/**
 * Defines crafting recipes.
 */
public class ItemCraftingRecipe {
    private String name;
    private List<ItemStack> ingredients;
    private ItemStack result;

    // Constructor and getters
    public ItemCraftingRecipe(String name, List<ItemStack> ingredients, ItemStack result) {
        this.name = name;
        this.ingredients = ingredients;
        this.result = result;
    }

    public String getName() { return name; }
    public List<ItemStack> getIngredients() { return ingredients; }
    public ItemStack getResult() { return result; }
}