package com.astral.game;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Player Inventory System - Simple but functional
 * Handles items, resources, equipment, and crafting
 */
public class PlayerInventory {
    
    private static PlayerInventory instance;
    
    // Resources (stackable, no limit)
    private final ObjectMap<String, Integer> resources = new ObjectMap<>();
    
    // Items (individual items with properties)
    private final Array<Item> items = new Array<>();
    private static final int MAX_ITEMS = 50;
    
    // Equipment slots
    private Item equippedWeapon;
    private Item equippedArmor;
    private Item equippedTool;
    private Item equippedAccessory;
    
    // Quick stats derived from equipment
    private int bonusDamage = 0;
    private int bonusDefense = 0;
    private float bonusMiningSpeed = 1f;
    private float bonusFishingSpeed = 1f;
    
    private PlayerInventory() {
        // Start with some basic resources
        addResource("Credits", 1000);
        addResource("Fuel", 100);
        
        // Starter tools
        items.add(new Item("Basic Mining Laser", ItemCategory.TOOL, 1)
            .setProperty("miningSpeed", 1.0f)
            .setProperty("durability", 100));
        items.add(new Item("Fishing Rod", ItemCategory.TOOL, 1)
            .setProperty("fishingSpeed", 1.0f)
            .setProperty("durability", 80));
    }
    
    public static PlayerInventory getInstance() {
        if (instance == null) {
            instance = new PlayerInventory();
        }
        return instance;
    }
    
    public static void reset() {
        instance = new PlayerInventory();
    }
    
    // === Resource Management ===
    
    public void addResource(String name, int amount) {
        int current = resources.get(name, 0);
        resources.put(name, current + amount);
    }
    
    public boolean removeResource(String name, int amount) {
        int current = resources.get(name, 0);
        if (current >= amount) {
            resources.put(name, current - amount);
            return true;
        }
        return false;
    }
    
    public int getResource(String name) {
        return resources.get(name, 0);
    }
    
    public boolean hasResource(String name, int amount) {
        return getResource(name) >= amount;
    }
    
    public ObjectMap<String, Integer> getAllResources() {
        return resources;
    }
    
    // === Item Management ===
    
    public boolean addItem(Item item) {
        if (items.size >= MAX_ITEMS) {
            return false;
        }
        items.add(item);
        return true;
    }
    
    public boolean removeItem(Item item) {
        return items.removeValue(item, true);
    }
    
    public Array<Item> getItems() {
        return items;
    }
    
    public Array<Item> getItemsByCategory(ItemCategory category) {
        Array<Item> result = new Array<>();
        for (Item item : items) {
            if (item.category == category) {
                result.add(item);
            }
        }
        return result;
    }
    
    public boolean hasItem(String name) {
        for (Item item : items) {
            if (item.name.equals(name)) return true;
        }
        return false;
    }
    
    // === Equipment ===
    
    public void equipWeapon(Item item) {
        if (item.category == ItemCategory.WEAPON) {
            equippedWeapon = item;
            recalculateBonuses();
        }
    }
    
    public void equipArmor(Item item) {
        if (item.category == ItemCategory.ARMOR) {
            equippedArmor = item;
            recalculateBonuses();
        }
    }
    
    public void equipTool(Item item) {
        if (item.category == ItemCategory.TOOL) {
            equippedTool = item;
            recalculateBonuses();
        }
    }
    
    public Item getEquippedWeapon() { return equippedWeapon; }
    public Item getEquippedArmor() { return equippedArmor; }
    public Item getEquippedTool() { return equippedTool; }
    
    private void recalculateBonuses() {
        bonusDamage = 0;
        bonusDefense = 0;
        bonusMiningSpeed = 1f;
        bonusFishingSpeed = 1f;
        
        if (equippedWeapon != null) {
            bonusDamage += equippedWeapon.getIntProperty("damage", 0);
        }
        if (equippedArmor != null) {
            bonusDefense += equippedArmor.getIntProperty("defense", 0);
        }
        if (equippedTool != null) {
            bonusMiningSpeed = equippedTool.getFloatProperty("miningSpeed", 1f);
            bonusFishingSpeed = equippedTool.getFloatProperty("fishingSpeed", 1f);
        }
    }
    
    public int getBonusDamage() { return bonusDamage; }
    public int getBonusDefense() { return bonusDefense; }
    public float getMiningSpeed() { return bonusMiningSpeed; }
    public float getFishingSpeed() { return bonusFishingSpeed; }
    
    // === Crafting ===
    
    public boolean canCraft(Recipe recipe) {
        for (ObjectMap.Entry<String, Integer> ingredient : recipe.ingredients) {
            if (!hasResource(ingredient.key, ingredient.value)) {
                return false;
            }
        }
        return true;
    }
    
    public boolean craft(Recipe recipe) {
        if (!canCraft(recipe)) return false;
        
        // Consume ingredients
        for (ObjectMap.Entry<String, Integer> ingredient : recipe.ingredients) {
            removeResource(ingredient.key, ingredient.value);
        }
        
        // Create result
        if (recipe.resultItem != null) {
            addItem(recipe.resultItem.copy());
        } else if (recipe.resultResource != null) {
            addResource(recipe.resultResource, recipe.resultAmount);
        }
        
        return true;
    }
    
    // === Serialization (for save/load) ===
    
    public String serialize() {
        StringBuilder sb = new StringBuilder();
        // Resources
        for (ObjectMap.Entry<String, Integer> entry : resources) {
            sb.append("R:").append(entry.key).append(":").append(entry.value).append("\n");
        }
        // Items
        for (Item item : items) {
            sb.append("I:").append(item.serialize()).append("\n");
        }
        return sb.toString();
    }
    
    // === Inner Classes ===
    
    public enum ItemCategory {
        WEAPON, ARMOR, TOOL, CONSUMABLE, MATERIAL, QUEST
    }
    
    public enum ItemRarity {
        COMMON(1, 0.7f, 0.7f, 0.7f),
        UNCOMMON(2, 0.3f, 0.8f, 0.3f),
        RARE(3, 0.3f, 0.5f, 1f),
        EPIC(4, 0.7f, 0.3f, 0.9f),
        LEGENDARY(5, 1f, 0.7f, 0.2f);
        
        public final int level;
        public final float r, g, b;
        
        ItemRarity(int level, float r, float g, float b) {
            this.level = level;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }
    
    public static class Item {
        public String name;
        public ItemCategory category;
        public ItemRarity rarity;
        public int value;
        public ObjectMap<String, Object> properties = new ObjectMap<>();
        
        public Item(String name, ItemCategory category, int value) {
            this.name = name;
            this.category = category;
            this.value = value;
            this.rarity = ItemRarity.COMMON;
        }
        
        public Item setRarity(ItemRarity rarity) {
            this.rarity = rarity;
            return this;
        }
        
        public Item setProperty(String key, Object value) {
            properties.put(key, value);
            return this;
        }
        
        public int getIntProperty(String key, int defaultValue) {
            Object val = properties.get(key);
            if (val instanceof Number) return ((Number) val).intValue();
            return defaultValue;
        }
        
        public float getFloatProperty(String key, float defaultValue) {
            Object val = properties.get(key);
            if (val instanceof Number) return ((Number) val).floatValue();
            return defaultValue;
        }
        
        public String getStringProperty(String key, String defaultValue) {
            Object val = properties.get(key);
            if (val instanceof String) return (String) val;
            return defaultValue;
        }
        
        public Item copy() {
            Item copy = new Item(name, category, value);
            copy.rarity = rarity;
            copy.properties.putAll(properties);
            return copy;
        }
        
        public String serialize() {
            return name + "|" + category + "|" + rarity + "|" + value;
        }
    }
    
    public static class Recipe {
        public String name;
        public ObjectMap<String, Integer> ingredients = new ObjectMap<>();
        public Item resultItem;
        public String resultResource;
        public int resultAmount;
        
        public Recipe(String name) {
            this.name = name;
        }
        
        public Recipe addIngredient(String resource, int amount) {
            ingredients.put(resource, amount);
            return this;
        }
        
        public Recipe setResultItem(Item item) {
            this.resultItem = item;
            return this;
        }
        
        public Recipe setResultResource(String resource, int amount) {
            this.resultResource = resource;
            this.resultAmount = amount;
            return this;
        }
    }
    
    // === Recipe Registry ===
    
    private static final Array<Recipe> ALL_RECIPES = new Array<>();
    
    static {
        // Basic smelting
        ALL_RECIPES.add(new Recipe("Smelt Iron Bar")
            .addIngredient("Iron Ore", 2)
            .addIngredient("Fuel", 1)
            .setResultResource("Iron Bar", 1));
        
        ALL_RECIPES.add(new Recipe("Smelt Copper Bar")
            .addIngredient("Copper Ore", 2)
            .addIngredient("Fuel", 1)
            .setResultResource("Copper Bar", 1));
        
        ALL_RECIPES.add(new Recipe("Smelt Gold Bar")
            .addIngredient("Gold Ore", 3)
            .addIngredient("Fuel", 2)
            .setResultResource("Gold Bar", 1));
        
        // Tools
        ALL_RECIPES.add(new Recipe("Craft Advanced Mining Laser")
            .addIngredient("Iron Bar", 5)
            .addIngredient("Copper Bar", 3)
            .addIngredient("Circuit Board", 2)
            .setResultItem(new Item("Advanced Mining Laser", ItemCategory.TOOL, 500)
                .setRarity(ItemRarity.UNCOMMON)
                .setProperty("miningSpeed", 2.0f)
                .setProperty("durability", 200)));
        
        ALL_RECIPES.add(new Recipe("Craft Advanced Fishing Rod")
            .addIngredient("Titanium Bar", 3)
            .addIngredient("Fiber", 5)
            .setResultItem(new Item("Advanced Fishing Rod", ItemCategory.TOOL, 400)
                .setRarity(ItemRarity.UNCOMMON)
                .setProperty("fishingSpeed", 2.0f)
                .setProperty("durability", 150)));
        
        // Weapons
        ALL_RECIPES.add(new Recipe("Craft Laser Pistol")
            .addIngredient("Iron Bar", 3)
            .addIngredient("Circuit Board", 1)
            .addIngredient("Energy Cell", 2)
            .setResultItem(new Item("Laser Pistol", ItemCategory.WEAPON, 300)
                .setRarity(ItemRarity.COMMON)
                .setProperty("damage", 15)));
        
        ALL_RECIPES.add(new Recipe("Craft Plasma Rifle")
            .addIngredient("Titanium Bar", 5)
            .addIngredient("Circuit Board", 3)
            .addIngredient("Rare Minerals", 2)
            .setResultItem(new Item("Plasma Rifle", ItemCategory.WEAPON, 1500)
                .setRarity(ItemRarity.RARE)
                .setProperty("damage", 40)));
        
        // Armor
        ALL_RECIPES.add(new Recipe("Craft Basic Armor")
            .addIngredient("Iron Bar", 8)
            .addIngredient("Fiber", 3)
            .setResultItem(new Item("Basic Armor", ItemCategory.ARMOR, 400)
                .setRarity(ItemRarity.COMMON)
                .setProperty("defense", 10)));
        
        // Materials
        ALL_RECIPES.add(new Recipe("Craft Circuit Board")
            .addIngredient("Copper Bar", 2)
            .addIngredient("Silicon", 3)
            .setResultResource("Circuit Board", 1));
        
        ALL_RECIPES.add(new Recipe("Craft Energy Cell")
            .addIngredient("Rare Gas", 2)
            .addIngredient("Copper Bar", 1)
            .setResultResource("Energy Cell", 1));
        
        // Ship parts
        ALL_RECIPES.add(new Recipe("Craft Hull Plating")
            .addIngredient("Titanium Bar", 10)
            .addIngredient("Iron Bar", 5)
            .setResultResource("Hull Plating", 1));
        
        ALL_RECIPES.add(new Recipe("Craft Fuel")
            .addIngredient("Rare Gas", 5)
            .addIngredient("Water", 10)
            .setResultResource("Fuel", 20));
    }
    
    public static Array<Recipe> getAllRecipes() {
        return ALL_RECIPES;
    }
}
