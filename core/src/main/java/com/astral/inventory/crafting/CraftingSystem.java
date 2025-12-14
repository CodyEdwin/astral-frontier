package com.astral.inventory.crafting;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.astral.inventory.items.ItemFactory;
import com.astral.inventory.ItemStack;
import com.astral.inventory.core.InventoryGrid;
import com.astral.inventory.skills.Skills;

/**
 * Extensive crafting system with detailed cookbook.
 * Supports complex recipes, multi-step crafting, and procedural generation.
 */
public class CraftingSystem {
    private Map<String, CraftingRecipe> recipes;
    private ItemFactory itemFactory;
    private Skills skills; // Reference to skills system

    public CraftingSystem(ItemFactory itemFactory, Skills skills) {
        this.itemFactory = itemFactory;
        this.skills = skills;
        recipes = new HashMap<>();
        initializeCookbook();
    }

    private void initializeCookbook() {
        // Basic recipes
        addRecipe("Stick", List.of(new Ingredient("Log", 1)), 4, "Workbench", Map.of(Skills.SkillType.CRAFTING, 1));
        addRecipe("Metal Bar", List.of(new Ingredient("Iron Ore", 2)), 1, "Furnace", Map.of(Skills.SkillType.SMITHING, 1));
        addRecipe("Copper Bar", List.of(new Ingredient("Copper Ore", 2)), 1, "Furnace", Map.of(Skills.SkillType.SMITHING, 5));
        addRecipe("Gold Bar", List.of(new Ingredient("Gold Ore", 2)), 1, "Furnace", Map.of(Skills.SkillType.SMITHING, 10));
        addRecipe("Leather", List.of(new Ingredient("Log", 1)), 1, "Tailor Station", Map.of(Skills.SkillType.CRAFTING, 3));
        addRecipe("Circuit Board", List.of(new Ingredient("Copper Bar", 1), new Ingredient("Metal Bar", 1)), 1, "Electronics Bench", Map.of(Skills.SkillType.CRAFTING, 15));
        addRecipe("Energy Cell", List.of(new Ingredient("Metal Bar", 1), new Ingredient("Copper Bar", 1)), 1, "Chemistry Lab", Map.of(Skills.SkillType.CRAFTING, 20));
        addRecipe("Pickaxe Head", List.of(new Ingredient("Metal Bar", 3)), 1, "Anvil", Map.of(Skills.SkillType.SMITHING, 5));
        addRecipe("Pickaxe", List.of(new Ingredient("Pickaxe Head", 1), new Ingredient("Stick", 2)), 1, "Workbench", Map.of(Skills.SkillType.CRAFTING, 5));
        addRecipe("Axe", List.of(new Ingredient("Metal Bar", 2), new Ingredient("Stick", 1)), 1, "Workbench", Map.of(Skills.SkillType.CRAFTING, 10));

        // Advanced recipes
        addRecipe("Laser Rifle", List.of(new Ingredient("Metal Bar", 5), new Ingredient("Energy Cell", 10), new Ingredient("Circuit Board", 2)), 1, "Advanced Workbench", Map.of(Skills.SkillType.CRAFTING, 30, Skills.SkillType.SMITHING, 25));
        addRecipe("Plasma Pistol", List.of(new Ingredient("Metal Bar", 3), new Ingredient("Energy Cell", 5), new Ingredient("Circuit Board", 1)), 1, "Advanced Workbench", Map.of(Skills.SkillType.CRAFTING, 25));
        addRecipe("Armor Vest", List.of(new Ingredient("Metal Bar", 8), new Ingredient("Leather", 4)), 1, "Tailor Station", Map.of(Skills.SkillType.CRAFTING, 20));
        addRecipe("Ship Hull Plate", List.of(new Ingredient("Metal Bar", 10), new Ingredient("Gold Bar", 2)), 1, "Shipyard", Map.of(Skills.SkillType.CONSTRUCTION, 40, Skills.SkillType.SMITHING, 35));
        addRecipe("Medkit", List.of(new Ingredient("Leather", 1), new Ingredient("Metal Bar", 1)), 1, "Chemistry Lab", Map.of(Skills.SkillType.CRAFTING, 10));

        // Complex multi-step chains
        addRecipe("Diamond Pickaxe", List.of(new Ingredient("Diamond", 3), new Ingredient("Stick", 2)), 1, "Workbench", Map.of(Skills.SkillType.CRAFTING, 50, Skills.SkillType.MINING, 40));
        addRecipe("Jetpack", List.of(new Ingredient("Metal Bar", 20), new Ingredient("Energy Cell", 5), new Ingredient("Circuit Board", 3)), 1, "Advanced Workbench", Map.of(Skills.SkillType.CRAFTING, 60, Skills.SkillType.CONSTRUCTION, 50));

        // Generate 1000+ procedural recipes
        generateProceduralRecipes();
    }

    private void addRecipe(String result, List<Ingredient> ingredients, int quantity, String station, Map<Skills.SkillType, Integer> skillRequirements) {
        recipes.put(result, new CraftingRecipe(result, ingredients, quantity, station, skillRequirements));
    }

    private void generateProceduralRecipes() {
        String[] stations = {"Workbench", "Furnace", "Anvil", "Tailor Station", "Chemistry Lab", "Electronics Bench", "Shipyard", "Forge", "Alchemy Lab"};
        String[] materials = {"Iron", "Copper", "Gold", "Diamond", "Wood", "Leather", "Plastic", "Crystal", "Titanium", "Uranium"};
        String[] prefixes = {"Basic", "Advanced", "Elite", "Legendary", "Mythical"};

        for (int i = 0; i < 1000; i++) {
            String prefix = prefixes[(int)(Math.random() * prefixes.length)];
            String material = materials[(int)(Math.random() * materials.length)];
            String result = prefix + " " + material + " Item " + i;
            List<Ingredient> ingredients = List.of(
                new Ingredient(material + " Bar", (int)(Math.random() * 5) + 1),
                new Ingredient(material + " Ore", (int)(Math.random() * 3) + 1),
                new Ingredient("Energy Cell", (int)(Math.random() * 2) + 1)
            );
            int quantity = 1;
            String station = stations[(int)(Math.random() * stations.length)];
            Map<Skills.SkillType, Integer> reqs = Map.of(Skills.SkillType.CRAFTING, (int)(Math.random() * 50) + 1);
            addRecipe(result, ingredients, quantity, station, reqs);
        }
    }

    public boolean canCraft(String result, InventoryGrid inventory) {
        CraftingRecipe recipe = recipes.get(result);
        if (recipe == null) return false;

        // Check ingredients - simplified
        // Placeholder: assume can craft

        // Check skill requirements
        if (skills != null) {
            for (Map.Entry<Skills.SkillType, Integer> req : recipe.skillRequirements.entrySet()) {
                if (skills.getLevel(req.getKey()) < req.getValue()) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean craft(String result, InventoryGrid inventory) {
        if (!canCraft(result, inventory)) return false;

        CraftingRecipe recipe = recipes.get(result);
        // Consume ingredients
        for (Ingredient ing : recipe.ingredients) {
            inventory.removeItem(new ItemStack((com.astral.inventory.IItem) itemFactory.createItem(ing.item), ing.quantity));
        }
        // Add result
        inventory.addItem(new ItemStack((com.astral.inventory.IItem) itemFactory.createItem(result), recipe.quantity));

        // Award XP based on recipe complexity
        int complexity = recipe.ingredients.size();
        long xp = complexity * 10; // Base XP
        if (skills != null) {
            skills.addXP(Skills.SkillType.CRAFTING, xp);
            // Additional skills based on station
            switch (recipe.station) {
                case "Furnace" -> skills.addXP(Skills.SkillType.SMITHING, xp / 2);
                case "Anvil" -> skills.addXP(Skills.SkillType.SMITHING, xp);
                case "Tailor Station" -> skills.addXP(Skills.SkillType.CRAFTING, xp / 2);
                // Add more
            }
        }

        return true;
    }

    public Map<String, CraftingRecipe> getAllRecipes() {
        return new HashMap<>(recipes);
    }

    public List<CraftingRecipe> getRecipesForStation(String station) {
        return recipes.values().stream().filter(r -> r.station.equals(station)).toList();
    }

    // Inner classes
    public static class CraftingRecipe {
        public String result;
        public List<Ingredient> ingredients;
        public int quantity;
        public String station;
        public Map<Skills.SkillType, Integer> skillRequirements;

        public CraftingRecipe(String result, List<Ingredient> ingredients, int quantity, String station) {
            this.result = result;
            this.ingredients = ingredients;
            this.quantity = quantity;
            this.station = station;
            this.skillRequirements = new HashMap<>();
        }

        public CraftingRecipe(String result, List<Ingredient> ingredients, int quantity, String station, Map<Skills.SkillType, Integer> skillRequirements) {
            this.result = result;
            this.ingredients = ingredients;
            this.quantity = quantity;
            this.station = station;
            this.skillRequirements = skillRequirements != null ? skillRequirements : new HashMap<>();
        }
    }

    public static class Ingredient {
        public String item;
        public int quantity;

        public Ingredient(String item, int quantity) {
            this.item = item;
            this.quantity = quantity;
        }
    }
}