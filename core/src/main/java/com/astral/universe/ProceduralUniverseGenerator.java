package com.astral.universe;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Procedural implementation of universe generation.
 * Uses seeds for reproducibility and generates systems algorithmically with detailed properties.
 */
public class ProceduralUniverseGenerator implements IUniverseGenerator {
    private Random random;
    private long seed;

    @Override
    public List<IStarsystem> generateSystems(UniverseConfig config) {
        random = new Random(seed);
        List<IStarsystem> systems = new ArrayList<>();
        for (int i = 0; i < config.getSystemCount(); i++) {
            IStarsystem system = generateSystem(config, i);
            systems.add(system);
        }
        return systems;
    }

    private IStarsystem generateSystem(UniverseConfig config, int index) {
        // Generate system name
        String name = generateSystemName(index);

        // Generate position in galaxy
        float x = (random.nextFloat() - 0.5f) * 2 * (float) config.getGalaxyRadius();
        float y = (random.nextFloat() - 0.5f) * 2 * (float) config.getGalaxyRadius();
        float z = (random.nextFloat() - 0.5f) * 2 * (float) config.getGalaxyRadius();

        // Create system
        StarsystemImpl system = new StarsystemImpl(name);
        system.setPosition(x, y, z);
        system.setHasEconomy(config.isEnableEconomy());
        system.setHasFactions(config.isEnableFactions());
        system.setSecurityLevel(random.nextInt(5) + 1); // 1-5

        // Generate planets
        int planetCount = random.nextInt(10) + 1; // 1-10 planets
        for (int p = 0; p < planetCount; p++) {
            IPlanet planet = generatePlanet(p);
            system.addPlanet(planet);
        }

        return system;
    }

    private String generateSystemName(int index) {
        String[] prefixes = {"Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta", "Iota", "Kappa"};
        String[] suffixes = {"Centauri", "Orionis", "Ursae", "Canis", "Leonis", "Draconis", "Bootis", "Corvi", "Librae", "Scorpii"};
        return prefixes[random.nextInt(prefixes.length)] + " " + suffixes[random.nextInt(suffixes.length)] + "-" + index;
    }

    private IPlanet generatePlanet(int index) {
        String[] types = {"Terran", "Ocean", "Desert", "Ice", "Volcanic", "Gas Giant", "Barren"};
        String type = types[random.nextInt(types.length)];
        String name = "Planet " + (char)('A' + index);
        long planetSeed = random.nextLong();

        // Create a basic planet (extend IPlanet later)
        return new Planet(name, planetSeed);
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed;
    }
}