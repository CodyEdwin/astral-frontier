package com.astral.shipbuilding;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.Random;

/**
 * Generates procedural textures for ship parts.
 */
public class ShipTextureGenerator implements Disposable {

    private static final int TEXTURE_SIZE = 256;

    private final ObjectMap<String, Texture> textureCache;
    private final Random random;

    public ShipTextureGenerator() {
        this.textureCache = new ObjectMap<>();
        this.random = new Random();
    }

    /**
     * Generate a texture for a ship part
     */
    public Texture generateTexture(ShipPartType type, Color primary, Color secondary) {
        String cacheKey = type.name() + "_" + primary.toString() + "_" + secondary.toString();

        if (textureCache.containsKey(cacheKey)) {
            return textureCache.get(cacheKey);
        }

        Pixmap pixmap = new Pixmap(TEXTURE_SIZE, TEXTURE_SIZE, Pixmap.Format.RGBA8888);

        switch (type.getCategory()) {
            case HULL:
                generateHullTexture(pixmap, type, primary, secondary);
                break;
            case WING:
                generateWingTexture(pixmap, type, primary, secondary);
                break;
            case ENGINE:
                generateEngineTexture(pixmap, type, primary, secondary);
                break;
            case WEAPON:
                generateWeaponTexture(pixmap, type, primary, secondary);
                break;
            case UTILITY:
                generateUtilityTexture(pixmap, type, primary, secondary);
                break;
            case STRUCTURAL:
                generateStructuralTexture(pixmap, type, primary, secondary);
                break;
            case DECORATIVE:
                generateDecorativeTexture(pixmap, type, primary, secondary);
                break;
        }

        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        pixmap.dispose();

        textureCache.put(cacheKey, texture);
        return texture;
    }

    // ============== Hull Textures ==============

    private void generateHullTexture(Pixmap pixmap, ShipPartType type, Color primary, Color secondary) {
        // Base hull color
        pixmap.setColor(primary);
        pixmap.fill();

        // Add panel lines
        pixmap.setColor(darken(primary, 0.2f));
        int panelSize = TEXTURE_SIZE / 4;
        for (int x = 0; x < TEXTURE_SIZE; x += panelSize) {
            pixmap.drawLine(x, 0, x, TEXTURE_SIZE);
        }
        for (int y = 0; y < TEXTURE_SIZE; y += panelSize) {
            pixmap.drawLine(0, y, TEXTURE_SIZE, y);
        }

        // Add rivet details
        pixmap.setColor(secondary);
        for (int x = panelSize / 2; x < TEXTURE_SIZE; x += panelSize) {
            for (int y = panelSize / 2; y < TEXTURE_SIZE; y += panelSize) {
                pixmap.fillCircle(x, y, 3);
            }
        }

        // Add subtle noise for weathering
        addNoise(pixmap, 0.05f);

        // Cockpit gets special treatment
        if (type == ShipPartType.HULL_COCKPIT) {
            // Add canopy reflection area
            pixmap.setColor(new Color(0.4f, 0.6f, 0.9f, 0.7f));
            pixmap.fillRectangle(TEXTURE_SIZE / 4, TEXTURE_SIZE / 4, TEXTURE_SIZE / 2, TEXTURE_SIZE / 2);

            // Canopy frame
            pixmap.setColor(secondary);
            pixmap.drawRectangle(TEXTURE_SIZE / 4, TEXTURE_SIZE / 4, TEXTURE_SIZE / 2, TEXTURE_SIZE / 2);
        }
    }

    // ============== Wing Textures ==============

    private void generateWingTexture(Pixmap pixmap, ShipPartType type, Color primary, Color secondary) {
        // Base wing color
        pixmap.setColor(primary);
        pixmap.fill();

        // Leading edge highlight
        pixmap.setColor(lighten(primary, 0.15f));
        pixmap.fillRectangle(0, 0, TEXTURE_SIZE, TEXTURE_SIZE / 8);

        // Control surface lines
        pixmap.setColor(secondary);
        pixmap.fillRectangle(0, TEXTURE_SIZE - TEXTURE_SIZE / 6, TEXTURE_SIZE, 2);

        // Aileron/flap detail
        pixmap.setColor(darken(primary, 0.15f));
        pixmap.fillRectangle(0, TEXTURE_SIZE - TEXTURE_SIZE / 6 + 3, TEXTURE_SIZE, TEXTURE_SIZE / 6 - 3);

        // Panel lines
        pixmap.setColor(darken(primary, 0.1f));
        int spacing = TEXTURE_SIZE / 6;
        for (int x = spacing; x < TEXTURE_SIZE; x += spacing) {
            pixmap.drawLine(x, 0, x - TEXTURE_SIZE / 8, TEXTURE_SIZE);
        }

        addNoise(pixmap, 0.03f);
    }

    // ============== Engine Textures ==============

    private void generateEngineTexture(Pixmap pixmap, ShipPartType type, Color primary, Color secondary) {
        // Base engine color (darker than hull)
        Color engineBase = darken(primary, 0.2f);
        pixmap.setColor(engineBase);
        pixmap.fill();

        // Heat gradient near exhaust
        for (int y = TEXTURE_SIZE * 3 / 4; y < TEXTURE_SIZE; y++) {
            float t = (float)(y - TEXTURE_SIZE * 3 / 4) / (TEXTURE_SIZE / 4);
            Color heatColor = new Color(
                engineBase.r + t * 0.3f,
                engineBase.g + t * 0.1f,
                engineBase.b - t * 0.1f,
                1f
            );
            pixmap.setColor(heatColor);
            pixmap.drawLine(0, y, TEXTURE_SIZE, y);
        }

        // Intake rings
        pixmap.setColor(secondary);
        int ringSpacing = TEXTURE_SIZE / 8;
        for (int y = ringSpacing; y < TEXTURE_SIZE / 2; y += ringSpacing) {
            pixmap.drawLine(0, y, TEXTURE_SIZE, y);
            pixmap.drawLine(0, y + 1, TEXTURE_SIZE, y + 1);
        }

        // Warning stripes near exhaust
        if (type == ShipPartType.ENGINE_LARGE || type == ShipPartType.ENGINE_AFTERBURNER) {
            pixmap.setColor(new Color(0.9f, 0.7f, 0.1f, 1f));
            int stripeWidth = TEXTURE_SIZE / 16;
            for (int x = 0; x < TEXTURE_SIZE; x += stripeWidth * 2) {
                pixmap.fillRectangle(x, TEXTURE_SIZE - TEXTURE_SIZE / 8, stripeWidth, TEXTURE_SIZE / 8);
            }
        }

        addNoise(pixmap, 0.04f);
    }

    // ============== Weapon Textures ==============

    private void generateWeaponTexture(Pixmap pixmap, ShipPartType type, Color primary, Color secondary) {
        // Dark metallic base
        Color weaponBase = new Color(0.25f, 0.25f, 0.28f, 1f);
        pixmap.setColor(weaponBase);
        pixmap.fill();

        // Barrel rifling pattern
        pixmap.setColor(darken(weaponBase, 0.15f));
        for (int y = 0; y < TEXTURE_SIZE; y += 8) {
            pixmap.drawLine(0, y, TEXTURE_SIZE, y);
        }

        // Heat vents
        pixmap.setColor(secondary);
        int ventSpacing = TEXTURE_SIZE / 4;
        for (int x = ventSpacing; x < TEXTURE_SIZE; x += ventSpacing) {
            pixmap.fillRectangle(x - 2, TEXTURE_SIZE / 3, 4, TEXTURE_SIZE / 3);
        }

        // Ammo indicator light area
        if (type == ShipPartType.WEAPON_MOUNT_MEDIUM || type == ShipPartType.WEAPON_MOUNT_LARGE) {
            pixmap.setColor(new Color(0.2f, 0.8f, 0.2f, 1f));
            pixmap.fillCircle(TEXTURE_SIZE - 20, 20, 8);
        }

        addNoise(pixmap, 0.06f);
    }

    // ============== Utility Textures ==============

    private void generateUtilityTexture(Pixmap pixmap, ShipPartType type, Color primary, Color secondary) {
        pixmap.setColor(primary);
        pixmap.fill();

        switch (type) {
            case UTIL_SENSOR_ARRAY:
                // Grid pattern for sensor
                pixmap.setColor(new Color(0.2f, 0.5f, 0.8f, 0.8f));
                int gridSize = TEXTURE_SIZE / 16;
                for (int x = 0; x < TEXTURE_SIZE; x += gridSize) {
                    pixmap.drawLine(x, 0, x, TEXTURE_SIZE);
                }
                for (int y = 0; y < TEXTURE_SIZE; y += gridSize) {
                    pixmap.drawLine(0, y, TEXTURE_SIZE, y);
                }
                break;

            case UTIL_SHIELD_GENERATOR:
                // Energy field pattern
                pixmap.setColor(new Color(0.3f, 0.6f, 0.9f, 0.6f));
                for (int i = 0; i < 8; i++) {
                    int r = TEXTURE_SIZE / 8 + i * TEXTURE_SIZE / 12;
                    pixmap.drawCircle(TEXTURE_SIZE / 2, TEXTURE_SIZE / 2, r);
                }
                break;

            case UTIL_CARGO_POD:
                // Cargo container markings
                pixmap.setColor(secondary);
                pixmap.drawRectangle(10, 10, TEXTURE_SIZE - 20, TEXTURE_SIZE - 20);
                // Handle marks
                pixmap.setColor(new Color(0.8f, 0.6f, 0.1f, 1f));
                pixmap.fillRectangle(TEXTURE_SIZE / 4, 5, TEXTURE_SIZE / 2, 10);
                break;

            case UTIL_FUEL_TANK:
                // Fuel warning stripes
                pixmap.setColor(new Color(0.9f, 0.2f, 0.2f, 1f));
                int stripeW = TEXTURE_SIZE / 12;
                for (int x = 0; x < TEXTURE_SIZE; x += stripeW * 2) {
                    pixmap.fillRectangle(x, TEXTURE_SIZE / 2 - 10, stripeW, 20);
                }
                break;

            default:
                // Generic utility pattern
                pixmap.setColor(secondary);
                pixmap.drawRectangle(5, 5, TEXTURE_SIZE - 10, TEXTURE_SIZE - 10);
                break;
        }

        addNoise(pixmap, 0.03f);
    }

    // ============== Structural Textures ==============

    private void generateStructuralTexture(Pixmap pixmap, ShipPartType type, Color primary, Color secondary) {
        // Industrial metallic look
        Color metalBase = new Color(0.4f, 0.42f, 0.45f, 1f);
        pixmap.setColor(metalBase);
        pixmap.fill();

        // Bolt pattern
        pixmap.setColor(darken(metalBase, 0.3f));
        int boltSpacing = TEXTURE_SIZE / 8;
        for (int x = boltSpacing; x < TEXTURE_SIZE; x += boltSpacing) {
            for (int y = boltSpacing; y < TEXTURE_SIZE; y += boltSpacing) {
                pixmap.fillCircle(x, y, 4);
            }
        }

        // Edge highlights
        pixmap.setColor(lighten(metalBase, 0.2f));
        pixmap.fillRectangle(0, 0, TEXTURE_SIZE, 3);
        pixmap.fillRectangle(0, 0, 3, TEXTURE_SIZE);

        addNoise(pixmap, 0.05f);
    }

    // ============== Decorative Textures ==============

    private void generateDecorativeTexture(Pixmap pixmap, ShipPartType type, Color primary, Color secondary) {
        switch (type) {
            case DECOR_STRIPE:
                // Racing stripe
                pixmap.setColor(primary);
                pixmap.fill();
                pixmap.setColor(secondary);
                pixmap.fillRectangle(0, TEXTURE_SIZE / 3, TEXTURE_SIZE, TEXTURE_SIZE / 3);
                break;

            case DECOR_EMBLEM:
                // Emblem background
                pixmap.setColor(primary);
                pixmap.fill();
                // Simple star emblem
                pixmap.setColor(secondary);
                drawStar(pixmap, TEXTURE_SIZE / 2, TEXTURE_SIZE / 2, TEXTURE_SIZE / 3, 5);
                break;

            case DECOR_LIGHT:
                // Navigation light glow
                pixmap.setColor(new Color(0, 0, 0, 0));
                pixmap.fill();
                // Radial gradient for glow
                for (int r = TEXTURE_SIZE / 2; r > 0; r--) {
                    float t = (float)r / (TEXTURE_SIZE / 2);
                    pixmap.setColor(new Color(primary.r, primary.g, primary.b, (1 - t) * 0.8f));
                    pixmap.fillCircle(TEXTURE_SIZE / 2, TEXTURE_SIZE / 2, r);
                }
                break;

            case DECOR_EXHAUST:
                // Exhaust vent
                pixmap.setColor(new Color(0.2f, 0.2f, 0.22f, 1f));
                pixmap.fill();
                // Grill pattern
                pixmap.setColor(new Color(0.1f, 0.1f, 0.12f, 1f));
                for (int y = 0; y < TEXTURE_SIZE; y += 16) {
                    pixmap.fillRectangle(0, y, TEXTURE_SIZE, 8);
                }
                break;

            default:
                pixmap.setColor(primary);
                pixmap.fill();
                break;
        }
    }

    // ============== Utility Methods ==============

    private void addNoise(Pixmap pixmap, float intensity) {
        for (int x = 0; x < pixmap.getWidth(); x++) {
            for (int y = 0; y < pixmap.getHeight(); y++) {
                int pixel = pixmap.getPixel(x, y);
                Color c = new Color();
                Color.rgba8888ToColor(c, pixel);

                float noise = (random.nextFloat() - 0.5f) * intensity;
                c.r = Math.max(0, Math.min(1, c.r + noise));
                c.g = Math.max(0, Math.min(1, c.g + noise));
                c.b = Math.max(0, Math.min(1, c.b + noise));

                pixmap.setColor(c);
                pixmap.drawPixel(x, y);
            }
        }
    }

    private Color darken(Color color, float amount) {
        return new Color(
            Math.max(0, color.r - amount),
            Math.max(0, color.g - amount),
            Math.max(0, color.b - amount),
            color.a
        );
    }

    private Color lighten(Color color, float amount) {
        return new Color(
            Math.min(1, color.r + amount),
            Math.min(1, color.g + amount),
            Math.min(1, color.b + amount),
            color.a
        );
    }

    private void drawStar(Pixmap pixmap, int cx, int cy, int radius, int points) {
        float angleStep = (float)(Math.PI * 2 / points);
        float innerRadius = radius * 0.4f;

        int[] xPoints = new int[points * 2];
        int[] yPoints = new int[points * 2];

        for (int i = 0; i < points * 2; i++) {
            float angle = i * angleStep / 2 - (float)Math.PI / 2;
            float r = (i % 2 == 0) ? radius : innerRadius;
            xPoints[i] = cx + (int)(Math.cos(angle) * r);
            yPoints[i] = cy + (int)(Math.sin(angle) * r);
        }

        // Draw filled star using triangles from center
        for (int i = 0; i < points * 2; i++) {
            int next = (i + 1) % (points * 2);
            fillTriangle(pixmap, cx, cy, xPoints[i], yPoints[i], xPoints[next], yPoints[next]);
        }
    }

    private void fillTriangle(Pixmap pixmap, int x1, int y1, int x2, int y2, int x3, int y3) {
        // Simple triangle fill using scanline
        int minY = Math.min(y1, Math.min(y2, y3));
        int maxY = Math.max(y1, Math.max(y2, y3));

        for (int y = minY; y <= maxY; y++) {
            int minX = TEXTURE_SIZE;
            int maxX = 0;

            // Check intersection with each edge
            minX = Math.min(minX, intersectEdge(x1, y1, x2, y2, y));
            minX = Math.min(minX, intersectEdge(x2, y2, x3, y3, y));
            minX = Math.min(minX, intersectEdge(x3, y3, x1, y1, y));

            maxX = Math.max(maxX, intersectEdge(x1, y1, x2, y2, y));
            maxX = Math.max(maxX, intersectEdge(x2, y2, x3, y3, y));
            maxX = Math.max(maxX, intersectEdge(x3, y3, x1, y1, y));

            if (minX <= maxX) {
                pixmap.drawLine(minX, y, maxX, y);
            }
        }
    }

    private int intersectEdge(int x1, int y1, int x2, int y2, int y) {
        if ((y1 <= y && y <= y2) || (y2 <= y && y <= y1)) {
            if (y1 == y2) return Math.min(x1, x2);
            return x1 + (x2 - x1) * (y - y1) / (y2 - y1);
        }
        return y1 < y2 ? Integer.MAX_VALUE : Integer.MIN_VALUE;
    }

    @Override
    public void dispose() {
        for (Texture texture : textureCache.values()) {
            texture.dispose();
        }
        textureCache.clear();
    }
}
