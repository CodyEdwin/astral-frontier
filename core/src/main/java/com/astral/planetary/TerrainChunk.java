package com.astral.planetary;

/**
 * Represents a terrain chunk.
 */
public class TerrainChunk {
    private int x, z;
    private float[][] heightmap;

    public TerrainChunk(int x, int z, int size) {
        this.x = x;
        this.z = z;
        heightmap = new float[size][size];
    }

    // Getters
    public int getX() { return x; }
    public int getZ() { return z; }
    public float[][] getHeightmap() { return heightmap; }
}