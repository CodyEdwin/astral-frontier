package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.math.MathUtils;

import java.util.Random;

/**
 * High-quality procedural textures for desert planets
 */
public class DesertTextures {

    /**
     * Creates detailed sand texture with subtle color variations and grain
     */
    public static Texture sand(int width, int height, long seed) {
        Random random = new Random(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Base sand colors
        float baseR = 0.82f;
        float baseG = 0.68f;
        float baseB = 0.42f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Multi-octave noise for natural variation - MUCH stronger contrast
                float noise = 0;
                noise += perlinNoise(x * 0.08f, y * 0.08f, seed) * 0.6f;
                noise += perlinNoise(x * 0.15f, y * 0.15f, seed + 1000) * 0.4f;
                noise += perlinNoise(x * 0.3f, y * 0.3f, seed + 2000) * 0.25f;

                // Fine grain detail - more visible
                float grain = (random.nextFloat() - 0.5f) * 0.15f;

                // Dune ripple pattern overlay
                float ripple = MathUtils.sin((x + y * 0.5f) * 0.12f) * 0.15f;

                // Color variation - MUCH stronger
                float variation = noise * 0.35f + grain + ripple;
                float r = clamp(baseR + variation);
                float g = clamp(baseG + variation * 0.85f);
                float b = clamp(baseB + variation * 0.6f);

                pixmap.setColor(r, g, b, 1f);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates sand dune texture with ripple patterns
     */
    public static Texture sandDunes(int width, int height, long seed) {
        Random random = new Random(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        float baseR = 0.9f;
        float baseG = 0.78f;
        float baseB = 0.5f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Dune ripple pattern
                float ripple = MathUtils.sin((x + y * 0.3f) * 0.15f) * 0.5f + 0.5f;
                ripple += MathUtils.sin((x * 0.8f - y * 0.2f) * 0.08f) * 0.3f;

                // Wind-swept highlight on dune crests
                float highlight = ripple > 0.7f ? (ripple - 0.7f) * 2f : 0f;

                // Shadow in dune troughs
                float shadow = ripple < 0.3f ? (0.3f - ripple) * 1.5f : 0f;

                float noise = perlinNoise(x * 0.05f, y * 0.05f, seed) * 0.1f;

                float r = clamp(baseR + highlight * 0.1f - shadow * 0.15f + noise);
                float g = clamp(baseG + highlight * 0.08f - shadow * 0.12f + noise * 0.9f);
                float b = clamp(baseB + highlight * 0.05f - shadow * 0.1f + noise * 0.7f);

                pixmap.setColor(r, g, b, 1f);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates cracked dry earth texture
     */
    public static Texture crackedEarth(int width, int height, long seed) {
        Random random = new Random(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Base dry earth color
        float baseR = 0.6f + random.nextFloat() * 0.1f;
        float baseG = 0.5f + random.nextFloat() * 0.08f;
        float baseB = 0.35f + random.nextFloat() * 0.05f;

        // First pass: base color
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float noise = perlinNoise(x * 0.08f, y * 0.08f, seed) * 0.15f;
                float r = clamp(baseR + noise);
                float g = clamp(baseG + noise * 0.9f);
                float b = clamp(baseB + noise * 0.7f);
                pixmap.setColor(r, g, b, 1f);
                pixmap.drawPixel(x, y);
            }
        }

        // Second pass: add cracks using Voronoi-like pattern
        pixmap.setColor(baseR * 0.4f, baseG * 0.35f, baseB * 0.3f, 1f);

        int numCells = 12;
        float[] cellX = new float[numCells];
        float[] cellY = new float[numCells];

        for (int i = 0; i < numCells; i++) {
            cellX[i] = random.nextFloat() * width;
            cellY[i] = random.nextFloat() * height;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float minDist = Float.MAX_VALUE;
                float secondDist = Float.MAX_VALUE;

                for (int i = 0; i < numCells; i++) {
                    float dx = x - cellX[i];
                    float dy = y - cellY[i];
                    float dist = dx * dx + dy * dy;

                    if (dist < minDist) {
                        secondDist = minDist;
                        minDist = dist;
                    } else if (dist < secondDist) {
                        secondDist = dist;
                    }
                }

                // Draw crack where cell boundaries meet
                float crackWidth = 2.5f;
                if (Math.sqrt(secondDist) - Math.sqrt(minDist) < crackWidth) {
                    pixmap.drawPixel(x, y);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates desert rock/boulder texture
     */
    public static Texture desertRock(int width, int height, long seed) {
        Random random = new Random(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Warm sandstone colors
        float baseR = 0.65f + random.nextFloat() * 0.15f;
        float baseG = 0.5f + random.nextFloat() * 0.1f;
        float baseB = 0.35f + random.nextFloat() * 0.08f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Layered sediment look
                float layers = MathUtils.sin(y * 0.3f + perlinNoise(x * 0.1f, y * 0.02f, seed) * 5f) * 0.15f;

                // Rocky texture noise
                float noise = 0;
                noise += perlinNoise(x * 0.15f, y * 0.15f, seed) * 0.4f;
                noise += perlinNoise(x * 0.3f, y * 0.3f, seed + 500) * 0.2f;
                noise += perlinNoise(x * 0.6f, y * 0.6f, seed + 1000) * 0.1f;

                // Occasional darker spots (weathering)
                float weathering = perlinNoise(x * 0.08f, y * 0.08f, seed + 2000);
                float darken = weathering > 0.6f ? (weathering - 0.6f) * 0.5f : 0f;

                float r = clamp(baseR + noise * 0.2f + layers - darken);
                float g = clamp(baseG + noise * 0.15f + layers * 0.8f - darken * 0.9f);
                float b = clamp(baseB + noise * 0.1f + layers * 0.6f - darken * 0.8f);

                pixmap.setColor(r, g, b, 1f);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates cactus/succulent texture
     */
    public static Texture cactus(int width, int height, long seed) {
        Random random = new Random(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Desert plant green
        float baseR = 0.35f + random.nextFloat() * 0.1f;
        float baseG = 0.55f + random.nextFloat() * 0.15f;
        float baseB = 0.3f + random.nextFloat() * 0.08f;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Vertical ridges typical of cacti
                float ridges = MathUtils.sin(x * 0.4f) * 0.3f + 0.5f;

                // Subtle noise
                float noise = perlinNoise(x * 0.1f, y * 0.1f, seed) * 0.15f;

                // Highlights on ridge peaks
                float highlight = ridges > 0.7f ? (ridges - 0.7f) * 0.4f : 0f;

                float r = clamp(baseR + noise + highlight * 0.3f);
                float g = clamp(baseG + noise * 1.2f + highlight * 0.5f);
                float b = clamp(baseB + noise * 0.8f + highlight * 0.2f);

                pixmap.setColor(r, g, b, 1f);
                pixmap.drawPixel(x, y);
            }
        }

        // Add spine dots
        pixmap.setColor(0.9f, 0.85f, 0.7f, 1f);
        for (int i = 0; i < width * height / 80; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            pixmap.drawPixel(x, y);
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates sky gradient for desert (warm orange/blue transition)
     */
    public static Texture desertSky(int width, int height, long seed) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < height; y++) {
            float t = (float) y / height;

            // Horizon is warm orange/yellow, zenith is pale blue
            float r, g, b;
            if (t < 0.3f) {
                // Near horizon - warm colors
                float blend = t / 0.3f;
                r = lerp(0.95f, 0.85f, blend);
                g = lerp(0.75f, 0.7f, blend);
                b = lerp(0.5f, 0.55f, blend);
            } else if (t < 0.6f) {
                // Mid sky - transition
                float blend = (t - 0.3f) / 0.3f;
                r = lerp(0.85f, 0.6f, blend);
                g = lerp(0.7f, 0.75f, blend);
                b = lerp(0.55f, 0.85f, blend);
            } else {
                // Upper sky - pale blue
                float blend = (t - 0.6f) / 0.4f;
                r = lerp(0.6f, 0.4f, blend);
                g = lerp(0.75f, 0.6f, blend);
                b = lerp(0.85f, 0.9f, blend);
            }

            for (int x = 0; x < width; x++) {
                pixmap.setColor(r, g, b, 1f);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates sun flare/glow texture
     */
    public static Texture sunGlow(int size, long seed) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        float cx = size / 2f;
        float cy = size / 2f;
        float maxRadius = size / 2f;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - cx;
                float dy = y - cy;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float t = dist / maxRadius;

                if (t > 1f) {
                    pixmap.setColor(0, 0, 0, 0);
                } else {
                    // Bright core fading to orange corona
                    float intensity = 1f - t * t;
                    float r = clamp(1f * intensity);
                    float g = clamp(0.9f * intensity * intensity);
                    float b = clamp(0.5f * intensity * intensity * intensity);
                    float a = intensity * intensity;

                    pixmap.setColor(r, g, b, a);
                }
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        pixmap.dispose();
        return texture;
    }

    /**
     * Creates heat shimmer/distortion pattern
     */
    public static Texture heatShimmer(int width, int height, long seed) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Wavy distortion pattern stored as displacement in R and G channels
                float wave1 = MathUtils.sin(x * 0.1f + y * 0.05f) * 0.5f + 0.5f;
                float wave2 = MathUtils.sin(x * 0.07f - y * 0.08f) * 0.5f + 0.5f;

                float noise = perlinNoise(x * 0.05f, y * 0.05f, seed);

                float dispX = (wave1 + noise * 0.3f) * 0.5f + 0.25f;
                float dispY = (wave2 + noise * 0.3f) * 0.5f + 0.25f;

                pixmap.setColor(dispX, dispY, 0.5f, 0.3f);
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        texture.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
        pixmap.dispose();
        return texture;
    }

    // Utility functions
    private static float perlinNoise(float x, float y, long seed) {
        Random rand = new Random((long) (x * 12345 + y * 67890 + seed));
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);
        float xf = x - xi;
        float yf = y - yi;

        float aa = hash(xi, yi, seed);
        float ab = hash(xi, yi + 1, seed);
        float ba = hash(xi + 1, yi, seed);
        float bb = hash(xi + 1, yi + 1, seed);

        float u = fade(xf);
        float v = fade(yf);

        return lerp(lerp(aa, ba, u), lerp(ab, bb, u), v);
    }

    private static float hash(int x, int y, long seed) {
        long n = x + y * 57 + seed * 131;
        n = (n << 13) ^ n;
        return (1.0f - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0f);
    }

    private static float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
