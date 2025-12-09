package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;

/**
 * Types of planets with their characteristics
 */
public enum PlanetType {
    DESERT("Desert World", new Color(0.9f, 0.75f, 0.4f, 1f), 0.1f, 40f),
    ROCKY("Rocky Planet", new Color(0.5f, 0.45f, 0.4f, 1f), 0.0f, 80f),
    ICE("Ice World", new Color(0.85f, 0.92f, 1f, 1f), 0.0f, 50f),
    LAVA("Volcanic World", new Color(0.6f, 0.2f, 0.1f, 1f), 0.0f, 100f),
    FOREST("Forest World", new Color(0.2f, 0.6f, 0.25f, 1f), 0.7f, 30f),
    OCEAN("Ocean World", new Color(0.1f, 0.4f, 0.8f, 1f), 0.9f, 20f),
    GAS_GIANT("Gas Giant", new Color(0.8f, 0.6f, 0.4f, 1f), 0.0f, 0f),
    BARREN("Barren Moon", new Color(0.6f, 0.6f, 0.6f, 1f), 0.0f, 10f);

    public final String displayName;
    public final Color baseColor;
    public final float waterLevel;  // 0-1, percentage of surface covered by water
    public final float atmosphereDensity;  // affects sky color and sound

    PlanetType(String displayName, Color baseColor, float waterLevel, float atmosphereDensity) {
        this.displayName = displayName;
        this.baseColor = baseColor;
        this.waterLevel = waterLevel;
        this.atmosphereDensity = atmosphereDensity;
    }

    public boolean hasAtmosphere() {
        return atmosphereDensity > 5f;
    }

    public boolean isLandable() {
        return this != GAS_GIANT;
    }

    public Color getSkyColor() {
        return switch (this) {
            case DESERT -> new Color(0.95f, 0.85f, 0.6f, 1f);
            case ROCKY -> new Color(0.1f, 0.05f, 0.02f, 1f);
            case ICE -> new Color(0.7f, 0.85f, 1f, 1f);
            case LAVA -> new Color(0.4f, 0.15f, 0.05f, 1f);
            case FOREST -> new Color(0.5f, 0.7f, 0.9f, 1f);
            case OCEAN -> new Color(0.4f, 0.6f, 0.9f, 1f);
            case BARREN -> new Color(0.0f, 0.0f, 0.0f, 1f);
            default -> new Color(0.1f, 0.1f, 0.15f, 1f);
        };
    }
}
