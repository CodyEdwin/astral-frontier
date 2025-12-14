package com.astral.universe;

import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.List;

/**
 * Manages economy within a star system.
 * Trade routes, markets, price fluctuations, tied to planet resources.
 */
public class StarsystemEconomy {
    private Map<String, Float> commodityPrices;
    private Random random;
    private float marketVolatility;
    private List<IPlanet> planets;

    public StarsystemEconomy(List<IPlanet> planets) {
        this.planets = planets;
        commodityPrices = new HashMap<>();
        random = new Random();
        marketVolatility = 0.1f; // 10% volatility

        // Initialize commodities based on planet resources
        initializeCommodities();
    }

    private void initializeCommodities() {
        // Base commodities
        String[] baseCommodities = {"Iron Ore", "Copper Ore", "Water", "Organics", "Fuel", "Weapons", "Luxury Goods", "Log", "Metal Bar", "Stick", "Pickaxe", "Medkit"};
        for (String commodity : baseCommodities) {
            commodityPrices.put(commodity, 10f + random.nextFloat() * 90f); // Base price 10-100
        }

        // Adjust prices based on planet availability
        for (IPlanet planet : planets) {
            String resource = getPlanetResource(planet);
            if (resource != null && commodityPrices.containsKey(resource)) {
                // Abundant resource: lower price
                commodityPrices.put(resource, commodityPrices.get(resource) * 0.5f);
            }
        }
    }

    private String getPlanetResource(IPlanet planet) {
        // Simplified: assign resources based on planet type
        switch (planet.getType()) {
            case "Terran": return "Organics";
            case "Desert": return "Iron Ore";
            case "Ice": return "Water";
            case "Volcanic": return "Metal Bar";
            default: return null;
        }
    }

    public void updateMarkets() {
        // Fluctuate prices based on supply/demand simulation
        for (String commodity : commodityPrices.keySet()) {
            float currentPrice = commodityPrices.get(commodity);
            float change = (random.nextFloat() - 0.5f) * 2 * marketVolatility * currentPrice;
            float newPrice = Math.max(1f, currentPrice + change); // Min price 1
            commodityPrices.put(commodity, newPrice);
        }

        // Simulate trade routes: adjust based on neighboring systems (placeholder)
        // In full impl, would check connected systems
    }

    public float getPrice(String commodity) {
        return commodityPrices.getOrDefault(commodity, 50f);
    }

    public void setVolatility(float volatility) {
        this.marketVolatility = volatility;
    }

    public Map<String, Float> getAllPrices() {
        return new HashMap<>(commodityPrices);
    }

    public void buyItem(String item, int quantity, float playerCredits) {
        float price = getPrice(item) * quantity;
        if (playerCredits >= price) {
            // Deduct credits, add item (would integrate with inventory)
        }
    }

    public void sellItem(String item, int quantity) {
        float price = getPrice(item) * quantity * 0.8f; // 80% sell price
        // Add credits, remove item
    }
}