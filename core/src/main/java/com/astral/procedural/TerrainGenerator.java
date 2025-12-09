package com.astral.procedural;

import com.badlogic.gdx.math.Vector2;

/**
 * Generates terrain heightmaps for planetary surfaces
 */
public class TerrainGenerator {

    private final long seed;
    private final SimplexNoise baseNoise;
    private final SimplexNoise detailNoise;
    private final SimplexNoise ridgeNoise;

    // Noise configuration per terrain type
    public static class NoiseConfig {
        public int octaves;
        public float scale;
        public float lacunarity;
        public float persistence;
        public float heightScale;

        public NoiseConfig(int octaves, float scale, float lacunarity, float persistence, float heightScale) {
            this.octaves = octaves;
            this.scale = scale;
            this.lacunarity = lacunarity;
            this.persistence = persistence;
            this.heightScale = heightScale;
        }
    }

    // Noise configs tuned for realistic terrain with gentle hills
    // scale controls feature frequency, heightScale controls max elevation
    public static final NoiseConfig ROCKY = new NoiseConfig(5, 0.003f, 2.0f, 0.45f, 50f);
    public static final NoiseConfig EARTH_LIKE = new NoiseConfig(5, 0.0025f, 2.0f, 0.4f, 40f);
    public static final NoiseConfig DESERT = new NoiseConfig(4, 0.002f, 2.0f, 0.4f, 25f);  // Gentle rolling hills
    public static final NoiseConfig ICE = new NoiseConfig(4, 0.0025f, 2.0f, 0.4f, 30f);
    public static final NoiseConfig VOLCANIC = new NoiseConfig(5, 0.003f, 2.2f, 0.45f, 60f);

    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.baseNoise = new SimplexNoise(seed);
        this.detailNoise = new SimplexNoise(seed ^ 0xDEADBEEFL);
        this.ridgeNoise = new SimplexNoise(seed ^ 0xCAFEBABEL);
    }

    /**
     * Generate a heightmap chunk
     * @param config Noise configuration for terrain type
     * @param resolution Vertices per edge (typically 64)
     * @param chunkOffset Chunk coordinates (not world position)
     * @return 2D array of height values
     */
    public float[][] generateHeightmap(NoiseConfig config, int resolution, Vector2 chunkOffset) {
        float[][] heightmap = new float[resolution][resolution];

        // Use (resolution-1) for chunk offset to align edges between adjacent chunks
        int chunkCells = resolution - 1;

        for (int y = 0; y < resolution; y++) {
            for (int x = 0; x < resolution; x++) {
                float worldX = (chunkOffset.x * chunkCells + x) * config.scale;
                float worldY = (chunkOffset.y * chunkCells + y) * config.scale;

                // Base terrain using fBm - creates the overall shape
                float height = fbm(baseNoise, worldX, worldY, config);

                // Apply gentle plateau effect - softens extreme values while keeping natural variation
                height = applyPlateau(height, 0.15f);

                // Very subtle mountain regions using ridged noise (sparse)
                float mountainMask = baseNoise.noise(worldX * 0.1f, worldY * 0.1f);
                if (mountainMask > 0.5f) {
                    float ridged = ridgedNoise(ridgeNoise, worldX * 0.3f, worldY * 0.3f, 3);
                    float mountainBlend = (mountainMask - 0.5f) * 2f; // 0 to 1
                    height += ridged * mountainBlend * 0.4f;
                }

                // Very subtle micro-detail for texture
                float detail = detailNoise.noise(worldX * 2f, worldY * 2f) * 0.02f;
                height += detail;

                heightmap[y][x] = height * config.heightScale;
            }
        }

        return heightmap;
    }

    /**
     * Apply gentle plateau effect - softens terrain while keeping natural hills
     */
    private float applyPlateau(float value, float threshold) {
        float absVal = Math.abs(value);
        if (absVal < threshold) {
            // Gentle reduction for low values (creates subtle flat areas)
            return value * 0.7f;
        } else {
            // Keep variation but slightly soften extreme peaks
            float excess = absVal - threshold;
            float sign = value >= 0 ? 1f : -1f;
            return sign * (threshold * 0.7f + excess * 0.85f);
        }
    }

    private float fbm(SimplexNoise noise, float x, float y, NoiseConfig config) {
        float height = 0f;
        float amplitude = 1f;
        float frequency = 1f;
        float maxAmplitude = 0f;

        for (int octave = 0; octave < config.octaves; octave++) {
            height += noise.noise(x * frequency, y * frequency) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= config.persistence;
            frequency *= config.lacunarity;
        }

        return height / maxAmplitude;
    }

    private float ridgedNoise(SimplexNoise noise, float x, float y, int octaves) {
        float height = 0f;
        float amplitude = 1f;
        float frequency = 1f;

        for (int i = 0; i < octaves; i++) {
            float n = noise.noise(x * frequency, y * frequency);
            n = 1f - Math.abs(n); // Ridge effect
            n = n * n;            // Sharpen ridges
            height += n * amplitude;
            amplitude *= 0.5f;
            frequency *= 2f;
        }

        return height;
    }

    /**
     * Generate crater at specific location
     */
    public void addCrater(float[][] heightmap, int centerX, int centerY, float radius, float depth) {
        int resolution = heightmap.length;

        for (int y = 0; y < resolution; y++) {
            for (int x = 0; x < resolution; x++) {
                float dx = x - centerX;
                float dy = y - centerY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < radius) {
                    float t = dist / radius;
                    // Crater profile: bowl shape with raised rim
                    float craterDepth;
                    if (t < 0.8f) {
                        // Inside crater
                        craterDepth = -depth * (1f - t / 0.8f);
                    } else {
                        // Raised rim
                        float rimT = (t - 0.8f) / 0.2f;
                        craterDepth = depth * 0.3f * (float) Math.sin(rimT * Math.PI);
                    }
                    heightmap[y][x] += craterDepth;
                }
            }
        }
    }

    /**
     * Simple 2D Simplex noise implementation
     */
    public static class SimplexNoise {
        private final int[] perm;

        private static final float F2 = (float) (0.5 * (Math.sqrt(3.0) - 1.0));
        private static final float G2 = (float) ((3.0 - Math.sqrt(3.0)) / 6.0);

        public SimplexNoise(long seed) {
            perm = new int[512];
            java.util.Random random = new java.util.Random(seed);

            int[] p = new int[256];
            for (int i = 0; i < 256; i++) p[i] = i;

            // Shuffle
            for (int i = 255; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temp = p[i];
                p[i] = p[j];
                p[j] = temp;
            }

            for (int i = 0; i < 512; i++) {
                perm[i] = p[i & 255];
            }
        }

        public float noise(float x, float y) {
            float s = (x + y) * F2;
            int i = fastFloor(x + s);
            int j = fastFloor(y + s);

            float t = (i + j) * G2;
            float X0 = i - t;
            float Y0 = j - t;
            float x0 = x - X0;
            float y0 = y - Y0;

            int i1, j1;
            if (x0 > y0) {
                i1 = 1;
                j1 = 0;
            } else {
                i1 = 0;
                j1 = 1;
            }

            float x1 = x0 - i1 + G2;
            float y1 = y0 - j1 + G2;
            float x2 = x0 - 1f + 2f * G2;
            float y2 = y0 - 1f + 2f * G2;

            int ii = i & 255;
            int jj = j & 255;

            float n0 = contribution(x0, y0, perm[ii + perm[jj]]);
            float n1 = contribution(x1, y1, perm[ii + i1 + perm[jj + j1]]);
            float n2 = contribution(x2, y2, perm[ii + 1 + perm[jj + 1]]);

            return 70f * (n0 + n1 + n2);
        }

        private float contribution(float x, float y, int gi) {
            float t = 0.5f - x * x - y * y;
            if (t < 0) return 0;
            t *= t;
            return t * t * dot(GRAD[gi % 12], x, y);
        }

        private static final float[][] GRAD = {
                {1, 1}, {-1, 1}, {1, -1}, {-1, -1},
                {1, 0}, {-1, 0}, {0, 1}, {0, -1},
                {1, 1}, {-1, 1}, {1, -1}, {-1, -1}
        };

        private float dot(float[] g, float x, float y) {
            return g[0] * x + g[1] * y;
        }

        private int fastFloor(float x) {
            return x > 0 ? (int) x : (int) x - 1;
        }
    }
}
