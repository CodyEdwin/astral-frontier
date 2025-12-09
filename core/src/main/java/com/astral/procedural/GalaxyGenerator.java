package com.astral.procedural;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

/**
 * Generates a procedural galaxy with star systems
 */
public class GalaxyGenerator {

    private final long seed;
    private final Random random;

    // Galaxy parameters
    private static final int DEFAULT_SYSTEM_COUNT = 1000;
    private static final float GALAXY_RADIUS = 50000f; // Light years
    private static final float GALAXY_HEIGHT = 2000f;
    private static final int SPIRAL_ARMS = 4;
    private static final float ARM_SPREAD = 0.5f;

    public GalaxyGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public Galaxy generate() {
        return generate(DEFAULT_SYSTEM_COUNT);
    }

    public Galaxy generate(int systemCount) {
        Galaxy galaxy = new Galaxy(seed);

        // Generate star systems using spiral arm distribution
        for (int i = 0; i < systemCount; i++) {
            long systemSeed = seed ^ (i * 0x9E3779B97F4A7C15L);
            random.setSeed(systemSeed);

            // Position in spiral galaxy
            Vector3 position = generateSpiralPosition();

            // Generate star system
            StarSystem system = new StarSystemGenerator(systemSeed).generate(position);
            system.setGalacticPosition(position);

            galaxy.addStarSystem(system);
        }

        return galaxy;
    }

    private Vector3 generateSpiralPosition() {
        // Distance from center (weighted toward middle)
        float distance = (float) Math.pow(random.nextFloat(), 0.5) * GALAXY_RADIUS;

        // Angle with spiral arm offset
        int arm = random.nextInt(SPIRAL_ARMS);
        float armOffset = (float) (arm * 2 * Math.PI / SPIRAL_ARMS);

        // Spiral curve: angle increases with distance
        float spiralAngle = distance / GALAXY_RADIUS * 2f * (float) Math.PI;

        // Add some random spread
        float spread = (random.nextFloat() - 0.5f) * ARM_SPREAD * distance / GALAXY_RADIUS;

        float angle = spiralAngle + armOffset + spread;

        float x = (float) Math.cos(angle) * distance;
        float z = (float) Math.sin(angle) * distance;

        // Height above/below galactic plane (gaussian distribution)
        float height = (float) random.nextGaussian() * GALAXY_HEIGHT * 0.1f;

        // Core has more vertical spread
        float coreMultiplier = 1f - (distance / GALAXY_RADIUS);
        height *= (1f + coreMultiplier * 2f);

        return new Vector3(x, height, z);
    }

    public static class Galaxy {
        private final long seed;
        private final Array<StarSystem> starSystems;
        private String name;

        public Galaxy(long seed) {
            this.seed = seed;
            this.starSystems = new Array<>();
            this.name = generateGalaxyName(seed);
        }

        public void addStarSystem(StarSystem system) {
            starSystems.add(system);
        }

        public Array<StarSystem> getStarSystems() {
            return starSystems;
        }

        public StarSystem getClosestSystem(Vector3 position) {
            StarSystem closest = null;
            float minDist = Float.MAX_VALUE;

            for (StarSystem system : starSystems) {
                float dist = position.dst(system.getGalacticPosition());
                if (dist < minDist) {
                    minDist = dist;
                    closest = system;
                }
            }

            return closest;
        }

        public Array<StarSystem> getSystemsInRange(Vector3 position, float range) {
            Array<StarSystem> result = new Array<>();

            for (StarSystem system : starSystems) {
                if (position.dst(system.getGalacticPosition()) <= range) {
                    result.add(system);
                }
            }

            return result;
        }

        public long getSeed() {
            return seed;
        }

        public String getName() {
            return name;
        }

        private static String generateGalaxyName(long seed) {
            String[] prefixes = {"Andromeda", "Nova", "Celestia", "Stellar", "Astral", "Cosmic"};
            String[] suffixes = {"Prime", "Major", "Cluster", "Expanse", "Reach", "Frontier"};

            Random r = new Random(seed);
            return prefixes[r.nextInt(prefixes.length)] + " " + suffixes[r.nextInt(suffixes.length)];
        }
    }
}
