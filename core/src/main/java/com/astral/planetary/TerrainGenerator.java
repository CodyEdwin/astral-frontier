package com.astral.planetary;

import java.util.Random;

/**
 * Detailed terrain generator with noise, LOD, and chunking.
 */
public class TerrainGenerator implements ITerrainGenerator {
    private Random random;
    private final int CHUNK_SIZE = 64;
    private final int LOD_LEVELS = 4;

    public TerrainGenerator() {
        random = new Random();
    }

    @Override
    public void generateTerrain(Planet planet) {
        long seed = planet.getSeed();
        random.setSeed(seed);

        // Generate chunks in a grid
        for (int x = -10; x <= 10; x++) {
            for (int z = -10; z <= 10; z++) {
                generateChunk(planet, x * CHUNK_SIZE, z * CHUNK_SIZE);
            }
        }
    }

    private void generateChunk(Planet planet, int chunkX, int chunkZ) {
        TerrainChunk chunk = new TerrainChunk(chunkX, chunkZ, CHUNK_SIZE);

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                float height = generateHeight(chunkX + x, chunkZ + z, planet.getSeed());
                chunk.getHeightmap()[x][z] = height;
            }
        }

        // Apply LOD
        for (int lod = 1; lod < LOD_LEVELS; lod++) {
            applyLOD(chunk, lod);
        }

        // Store chunk (placeholder)
    }

    private float generateHeight(int x, int z, long seed) {
        // Simplex noise-like generation
        float noise = (float) Math.sin(x * 0.01) * (float) Math.cos(z * 0.01) * 10f;
        noise += (float) Math.sin(x * 0.05 + seed) * 5f;
        return Math.max(0, noise + 50f); // Base height
    }

    private void applyLOD(TerrainChunk chunk, int lod) {
        int step = 1 << lod;
        for (int x = 0; x < CHUNK_SIZE; x += step) {
            for (int z = 0; z < CHUNK_SIZE; z += step) {
                // Simplify heightmap
                chunk.getHeightmap()[x][z] = averageNeighbors(chunk, x, z, step);
            }
        }
    }

    private float averageNeighbors(TerrainChunk chunk, int x, int z, int step) {
        float sum = 0;
        int count = 0;
        for (int dx = -step; dx <= step; dx += step) {
            for (int dz = -step; dz <= step; dz += step) {
                int nx = Math.max(0, Math.min(CHUNK_SIZE - 1, x + dx));
                int nz = Math.max(0, Math.min(CHUNK_SIZE - 1, z + dz));
                sum += chunk.getHeightmap()[nx][nz];
                count++;
            }
        }
        return sum / count;
    }
}