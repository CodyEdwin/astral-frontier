package com.astral.planetary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages biomes with flora, fauna, weather, and resources.
 */
public class BiomeManager {
    private List<Biome> biomes;
    private Random random;

    public BiomeManager() {
        biomes = new ArrayList<>();
        random = new Random();
    }

    public void generateBiomes(Planet planet) {
        // Create diverse biomes
        biomes.add(new Biome("Desert"));
        biomes.add(new Biome("Forest"));
        biomes.add(new Biome("Tundra"));
        biomes.add(new Biome("Ocean"));
        // More biomes...

        for (Biome biome : biomes) {
            biome.generateFlora();
            biome.spawnFauna();
            biome.setupWeather();
        }
    }

    public void updateBiomes(float deltaTime) {
        for (Biome biome : biomes) {
            biome.updateWeather(deltaTime);
            biome.updateFauna(deltaTime);
        }
    }

    public Biome getBiomeAt(int x, int z) {
        // Determine biome based on position
        return biomes.get((x + z) % biomes.size());
    }
}