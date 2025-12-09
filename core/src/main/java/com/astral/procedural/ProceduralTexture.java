package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.MathUtils;

import java.util.Random;

/**
 * Procedural texture generation - creates textures entirely in code
 */
public class ProceduralTexture {

    private static final Random random = new Random();

    /**
     * Generate a metal/hull texture with panels and rivets
     */
    public static Texture metalHull(int width, int height, Color baseColor, long seed) {
        random.setSeed(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Base color with slight variation
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float variation = 0.95f + random.nextFloat() * 0.1f;
                pixmap.setColor(
                    baseColor.r * variation,
                    baseColor.g * variation,
                    baseColor.b * variation,
                    1f
                );
                pixmap.drawPixel(x, y);
            }
        }

        // Panel lines
        pixmap.setColor(baseColor.r * 0.6f, baseColor.g * 0.6f, baseColor.b * 0.6f, 1f);
        int panelSize = width / 4;
        for (int i = 0; i <= 4; i++) {
            pixmap.drawLine(i * panelSize, 0, i * panelSize, height);
            pixmap.drawLine(0, i * panelSize, width, i * panelSize);
        }

        // Rivets
        pixmap.setColor(baseColor.r * 0.8f, baseColor.g * 0.8f, baseColor.b * 0.8f, 1f);
        int rivetSpacing = panelSize / 4;
        for (int py = 0; py < 4; py++) {
            for (int px = 0; px < 4; px++) {
                int baseX = px * panelSize;
                int baseY = py * panelSize;
                // Corner rivets
                pixmap.fillCircle(baseX + rivetSpacing, baseY + rivetSpacing, 2);
                pixmap.fillCircle(baseX + panelSize - rivetSpacing, baseY + rivetSpacing, 2);
                pixmap.fillCircle(baseX + rivetSpacing, baseY + panelSize - rivetSpacing, 2);
                pixmap.fillCircle(baseX + panelSize - rivetSpacing, baseY + panelSize - rivetSpacing, 2);
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Generate a glowing energy/tech texture
     */
    public static Texture energyGlow(int width, int height, Color coreColor, Color edgeColor, long seed) {
        random.setSeed(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        float centerX = width / 2f;
        float centerY = height / 2f;
        float maxDist = (float) Math.sqrt(centerX * centerX + centerY * centerY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float dist = (float) Math.sqrt((x - centerX) * (x - centerX) + (y - centerY) * (y - centerY));
                float t = dist / maxDist;

                // Pulsing effect via noise
                float noise = (float) Math.sin(x * 0.1 + y * 0.1 + seed * 0.01) * 0.1f;
                t = MathUtils.clamp(t + noise, 0, 1);

                pixmap.setColor(
                    MathUtils.lerp(coreColor.r, edgeColor.r, t),
                    MathUtils.lerp(coreColor.g, edgeColor.g, t),
                    MathUtils.lerp(coreColor.b, edgeColor.b, t),
                    MathUtils.lerp(1f, 0.3f, t)
                );
                pixmap.drawPixel(x, y);
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Generate organic/creature skin texture
     */
    public static Texture organicSkin(int width, int height, Color baseColor, long seed) {
        random.setSeed(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Base with cellular noise pattern
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float noise = cellularNoise(x * 0.05f, y * 0.05f, seed);
                float variation = 0.7f + noise * 0.3f;

                pixmap.setColor(
                    baseColor.r * variation,
                    baseColor.g * variation,
                    baseColor.b * variation,
                    1f
                );
                pixmap.drawPixel(x, y);
            }
        }

        // Add spots/scales
        int spotCount = 20 + random.nextInt(30);
        for (int i = 0; i < spotCount; i++) {
            int sx = random.nextInt(width);
            int sy = random.nextInt(height);
            int radius = 3 + random.nextInt(8);
            float darken = 0.6f + random.nextFloat() * 0.2f;

            pixmap.setColor(baseColor.r * darken, baseColor.g * darken, baseColor.b * darken, 1f);
            pixmap.fillCircle(sx, sy, radius);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Generate rock/stone texture
     */
    public static Texture rock(int width, int height, Color baseColor, long seed) {
        random.setSeed(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Multi-octave noise for rocky appearance
                float noise = 0;
                noise += simplexNoise(x * 0.02f, y * 0.02f, seed) * 0.5f;
                noise += simplexNoise(x * 0.05f, y * 0.05f, seed + 1000) * 0.3f;
                noise += simplexNoise(x * 0.1f, y * 0.1f, seed + 2000) * 0.2f;

                float variation = 0.6f + noise * 0.4f;

                pixmap.setColor(
                    MathUtils.clamp(baseColor.r * variation, 0, 1),
                    MathUtils.clamp(baseColor.g * variation, 0, 1),
                    MathUtils.clamp(baseColor.b * variation, 0, 1),
                    1f
                );
                pixmap.drawPixel(x, y);
            }
        }

        // Add cracks
        pixmap.setColor(baseColor.r * 0.4f, baseColor.g * 0.4f, baseColor.b * 0.4f, 1f);
        for (int i = 0; i < 5; i++) {
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = x1 + random.nextInt(60) - 30;
            int y2 = y1 + random.nextInt(60) - 30;
            pixmap.drawLine(x1, y1, x2, y2);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Generate wood/bark texture
     */
    public static Texture wood(int width, int height, Color baseColor, long seed) {
        random.setSeed(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Wood grain pattern
                float grain = (float) Math.sin(y * 0.3 + simplexNoise(x * 0.05f, y * 0.01f, seed) * 5);
                grain = (grain + 1) * 0.5f;

                float variation = 0.7f + grain * 0.3f;

                pixmap.setColor(
                    baseColor.r * variation,
                    baseColor.g * variation,
                    baseColor.b * variation,
                    1f
                );
                pixmap.drawPixel(x, y);
            }
        }

        // Knots
        for (int i = 0; i < 3; i++) {
            int kx = random.nextInt(width);
            int ky = random.nextInt(height);
            pixmap.setColor(baseColor.r * 0.5f, baseColor.g * 0.5f, baseColor.b * 0.5f, 1f);
            pixmap.fillCircle(kx, ky, 5 + random.nextInt(5));
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Generate circuit/tech pattern texture
     */
    public static Texture circuit(int width, int height, Color lineColor, Color bgColor, long seed) {
        random.setSeed(seed);
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        // Background
        pixmap.setColor(bgColor);
        pixmap.fill();

        // Circuit lines
        pixmap.setColor(lineColor);
        int gridSize = 16;

        for (int gy = 0; gy < height / gridSize; gy++) {
            for (int gx = 0; gx < width / gridSize; gx++) {
                int x = gx * gridSize;
                int y = gy * gridSize;

                if (random.nextFloat() < 0.6f) {
                    // Horizontal line
                    pixmap.drawLine(x, y + gridSize / 2, x + gridSize, y + gridSize / 2);
                }
                if (random.nextFloat() < 0.6f) {
                    // Vertical line
                    pixmap.drawLine(x + gridSize / 2, y, x + gridSize / 2, y + gridSize);
                }
                if (random.nextFloat() < 0.3f) {
                    // Node
                    pixmap.fillCircle(x + gridSize / 2, y + gridSize / 2, 3);
                }
            }
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Generate a gradient texture
     */
    public static Texture gradient(int width, int height, Color top, Color bottom) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);

        for (int y = 0; y < height; y++) {
            float t = (float) y / height;
            pixmap.setColor(
                MathUtils.lerp(top.r, bottom.r, t),
                MathUtils.lerp(top.g, bottom.g, t),
                MathUtils.lerp(top.b, bottom.b, t),
                MathUtils.lerp(top.a, bottom.a, t)
            );
            pixmap.drawLine(0, y, width, y);
        }

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Generate solid color texture
     */
    public static Texture solid(int width, int height, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /**
     * Create material with procedural texture
     */
    public static Material createMaterial(Texture texture) {
        Material material = new Material();
        material.set(TextureAttribute.createDiffuse(texture));
        return material;
    }

    /**
     * Create simple colored material
     */
    public static Material createColorMaterial(Color color) {
        Material material = new Material();
        material.set(ColorAttribute.createDiffuse(color));
        return material;
    }

    // Noise functions
    private static float simplexNoise(float x, float y, long seed) {
        // Simplified 2D noise approximation
        float n = (float) Math.sin(x * 12.9898 + y * 78.233 + seed * 0.001) * 43758.5453f;
        return (n - (int) n) * 2 - 1;
    }

    private static float cellularNoise(float x, float y, long seed) {
        random.setSeed(seed);
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);

        float minDist = Float.MAX_VALUE;
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                random.setSeed(seed + (xi + dx) * 31337 + (yi + dy) * 7919);
                float px = xi + dx + random.nextFloat();
                float py = yi + dy + random.nextFloat();
                float dist = (x - px) * (x - px) + (y - py) * (y - py);
                minDist = Math.min(minDist, dist);
            }
        }
        return (float) Math.sqrt(minDist);
    }
}
