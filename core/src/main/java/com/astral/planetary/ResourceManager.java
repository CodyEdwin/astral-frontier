package com.astral.planetary;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Manages resource nodes with skill checks and respawning.
 */
public class ResourceManager {
    private List<ResourceNode> nodes;
    private Random random;

    public ResourceManager() {
        nodes = new ArrayList<>();
        random = new Random();
    }

    public void spawnResources(Planet planet) {
        // Spawn resource nodes
        for (int i = 0; i < 200; i++) {
            int x = random.nextInt(2000) - 1000;
            int z = random.nextInt(2000) - 1000;
            String type = getRandomResource();
            ResourceNode node = new ResourceNode(type, x, z, planet.getSeed());
            nodes.add(node);
        }
    }

    private String getRandomResource() {
        String[] resources = {"Iron Ore", "Copper Ore", "Log", "Water", "Organics"};
        return resources[random.nextInt(resources.length)];
    }

    public void updateResources(float deltaTime) {
        // Respawn depleted nodes
        for (ResourceNode node : nodes) {
            if (node.isDepleted()) {
                node.respawn(deltaTime);
            }
        }
    }

    public ResourceNode getNodeAt(int x, int z) {
        for (ResourceNode node : nodes) {
            if (Math.abs(node.x - x) < 5 && Math.abs(node.z - z) < 5) {
                return node;
            }
        }
        return null;
    }

    public static class ResourceNode {
        public String type;
        public int x, z;
        public int amount;
        public float respawnTime;

        public ResourceNode(String type, int x, int z, long seed) {
            this.type = type;
            this.x = x;
            this.z = z;
            this.amount = 10 + new Random(seed).nextInt(20);
        }

        public boolean isDepleted() {
            return amount <= 0;
        }

        public void harvest() {
            if (amount > 0) amount--;
        }

        public void respawn(float deltaTime) {
            respawnTime += deltaTime;
            if (respawnTime > 60) { // 1 minute
                amount = 10;
                respawnTime = 0;
            }
        }
    }
}