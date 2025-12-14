package com.astral.inventory.items;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating items from templates or IDs.
 * Supports procedural generation and modding.
 */
public class ItemFactory {
    private Map<String, ItemTemplate> templates;

    public ItemFactory() {
        templates = new HashMap<>();
        initializeTemplates();
    }

    private void initializeTemplates() {
        // Generate 10,000+ procedural items
        generateMassiveItemDatabase();

        // Add specific examples
        templates.put("Laser Rifle", new ItemTemplate("Laser Rifle", ItemType.WEAPON, 5000, ItemRarity.RARE,
            Map.of("damage", 50, "fireRate", 10, "ammoType", "Energy Cell")));
        templates.put("Plasma Pistol", new ItemTemplate("Plasma Pistol", ItemType.WEAPON, 3000, ItemRarity.UNCOMMON,
            Map.of("damage", 30, "fireRate", 15, "ammoType", "Plasma Cartridge")));
        templates.put("Armor Vest", new ItemTemplate("Armor Vest", ItemType.ARMOR, 2000, ItemRarity.COMMON,
            Map.of("defense", 20, "durability", 100)));
        templates.put("Medkit", new ItemTemplate("Medkit", ItemType.CONSUMABLE, 500, ItemRarity.COMMON,
            Map.of("healAmount", 50, "uses", 1)));

        // Basic resources
        templates.put("Iron Ore", new ItemTemplate("Iron Ore", ItemType.RESOURCE, 10, ItemRarity.COMMON, Map.of()));
        templates.put("Copper Ore", new ItemTemplate("Copper Ore", ItemType.RESOURCE, 15, ItemRarity.COMMON, Map.of()));
        templates.put("Gold Ore", new ItemTemplate("Gold Ore", ItemType.RESOURCE, 50, ItemRarity.UNCOMMON, Map.of()));
        templates.put("Diamond", new ItemTemplate("Diamond", ItemType.RESOURCE, 200, ItemRarity.RARE, Map.of()));
        templates.put("Log", new ItemTemplate("Log", ItemType.RESOURCE, 5, ItemRarity.COMMON, Map.of()));
        templates.put("Stick", new ItemTemplate("Stick", ItemType.RESOURCE, 2, ItemRarity.COMMON, Map.of()));
        templates.put("Leather", new ItemTemplate("Leather", ItemType.MATERIAL, 20, ItemRarity.COMMON, Map.of()));
        templates.put("Metal Bar", new ItemTemplate("Metal Bar", ItemType.MATERIAL, 50, ItemRarity.COMMON, Map.of()));
        templates.put("Circuit Board", new ItemTemplate("Circuit Board", ItemType.MATERIAL, 100, ItemRarity.UNCOMMON, Map.of()));
        templates.put("Energy Cell", new ItemTemplate("Energy Cell", ItemType.FUEL, 30, ItemRarity.COMMON, Map.of()));
        templates.put("Pickaxe Head", new ItemTemplate("Pickaxe Head", ItemType.MATERIAL, 100, ItemRarity.COMMON, Map.of("miningSpeed", 1.0)));
        templates.put("Pickaxe", new ItemTemplate("Pickaxe", ItemType.TOOL, 200, ItemRarity.COMMON, Map.of("miningSpeed", 2.0, "durability", 100)));
        templates.put("Axe", new ItemTemplate("Axe", ItemType.TOOL, 150, ItemRarity.COMMON, Map.of("choppingSpeed", 1.5, "durability", 80)));
        templates.put("Ship Hull Plate", new ItemTemplate("Ship Hull Plate", ItemType.SHIP_PART, 500, ItemRarity.UNCOMMON, Map.of()));
        templates.put("Ancient Artifact", new ItemTemplate("Ancient Artifact", ItemType.QUEST_ITEM, 1000, ItemRarity.EPIC, Map.of()));
    }

    private void generateMassiveItemDatabase() {
        // Generate 10,000+ procedural items
        String[] prefixes = {"Basic", "Advanced", "Elite", "Legendary", "Mythical", "Ancient", "Futuristic", "Alien", "Military", "Civilian"};
        String[] weaponBases = {"Blaster", "Cannon", "Rifle", "Pistol", "Launcher", "Sword", "Axe", "Hammer"};
        String[] armorBases = {"Helmet", "Chestplate", "Leggings", "Boots", "Gloves", "Shield"};
        String[] resourceBases = {"Ore", "Crystal", "Wood", "Metal", "Gem", "Herb", "Fiber", "Powder"};
        String[] toolBases = {"Pickaxe", "Axe", "Hammer", "Shovel", "Knife", "Wrench", "Scanner", "Welder"};

        int id = 1000; // Start after specific items
        for (int i = 0; i < 10000; i++) {
            ItemType type = ItemType.values()[(int)(Math.random() * ItemType.values().length)];
            ItemRarity rarity = ItemRarity.values()[(int)(Math.random() * ItemRarity.values().length)];
            String prefix = prefixes[(int)(Math.random() * prefixes.length)];
            String base = switch (type) {
                case WEAPON -> weaponBases[(int)(Math.random() * weaponBases.length)];
                case ARMOR -> armorBases[(int)(Math.random() * armorBases.length)];
                case RESOURCE -> resourceBases[(int)(Math.random() * resourceBases.length)];
                case TOOL -> toolBases[(int)(Math.random() * toolBases.length)];
                case CONSUMABLE -> "Potion"; // Simplified
                default -> "Item";
            };
            String name = prefix + " " + base + " " + id;
            long value = (long) (10 * Math.pow(1.5, rarity.getLevel()) * (1 + Math.random()));
            templates.put(name, new ItemTemplate(name, type, value, rarity, Map.of("id", id)));
            id++;
        }
    }

    public IItem createItem(String templateName) {
        ItemTemplate template = templates.get(templateName);
        if (template != null) {
            return new ProceduralItem(template);
        }
        return null;
    }

    public IItem createProceduralItem(ItemType type, ItemRarity rarity) {
        // Generate random item of type and rarity
        String name = generateName(type, rarity);
        long value = calculateValue(type, rarity);
        return new ProceduralItem(new ItemTemplate(name, type, value, rarity, Map.of()));
    }

    private String generateName(ItemType type, ItemRarity rarity) {
        String[] prefixes = {"Basic", "Advanced", "Elite", "Legendary", "Mythical"};
        String[] weaponNames = {"Blaster", "Cannon", "Rifle", "Pistol", "Launcher"};
        String[] armorNames = {"Vest", "Helmet", "Boots", "Gloves", "Shield"};

        String prefix = prefixes[rarity.getLevel() - 1];
        String base = type == ItemType.WEAPON ? weaponNames[(int)(Math.random() * weaponNames.length)] :
                     type == ItemType.ARMOR ? armorNames[(int)(Math.random() * armorNames.length)] : "Item";

        return prefix + " " + base;
    }

    private long calculateValue(ItemType type, ItemRarity rarity) {
        long baseValue = type == ItemType.WEAPON ? 1000 : type == ItemType.ARMOR ? 500 : 100;
        return baseValue * rarity.getLevel();
    }

    // Inner classes
    private static class ItemTemplate {
        String name;
        ItemType type;
        long value;
        ItemRarity rarity;
        Map<String, Object> properties;

        ItemTemplate(String name, ItemType type, long value, ItemRarity rarity, Map<String, Object> properties) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.rarity = rarity;
            this.properties = properties;
        }
    }

    private static class ProceduralItem implements IItem {
        private ItemTemplate template;

        ProceduralItem(ItemTemplate template) {
            this.template = template;
        }

        @Override
        public String getName() { return template.name; }
        @Override
        public ItemType getType() { return template.type; }
        @Override
        public int getRarity() { return template.rarity.getLevel(); }
        @Override
        public long getValue() { return template.value; }
    }
}