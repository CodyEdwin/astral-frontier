package com.astral.ui;

import com.badlogic.gdx.graphics.Color;

/**
 * Starfield-inspired UI Theme
 * Defines all colors, sizes, and styling constants for the game UI
 */
public class UITheme {
    
    // === PRIMARY COLORS (Starfield-inspired dark sci-fi palette) ===
    public static final Color BACKGROUND_DARK = new Color(0.02f, 0.03f, 0.06f, 0.95f);
    public static final Color BACKGROUND_MEDIUM = new Color(0.05f, 0.07f, 0.12f, 0.92f);
    public static final Color BACKGROUND_LIGHT = new Color(0.08f, 0.10f, 0.16f, 0.88f);
    
    // === ACCENT COLORS ===
    public static final Color ACCENT_CYAN = new Color(0.2f, 0.8f, 0.9f, 1f);
    public static final Color ACCENT_BLUE = new Color(0.3f, 0.5f, 0.9f, 1f);
    public static final Color ACCENT_TEAL = new Color(0.1f, 0.7f, 0.7f, 1f);
    public static final Color ACCENT_GOLD = new Color(0.9f, 0.75f, 0.3f, 1f);
    
    // === STATUS COLORS ===
    public static final Color HEALTH_COLOR = new Color(0.2f, 0.85f, 0.4f, 1f);
    public static final Color SHIELD_COLOR = new Color(0.3f, 0.6f, 1f, 1f);
    public static final Color FUEL_COLOR = new Color(0.9f, 0.6f, 0.2f, 1f);
    public static final Color OXYGEN_COLOR = new Color(0.4f, 0.8f, 0.9f, 1f);
    public static final Color DANGER_COLOR = new Color(0.95f, 0.25f, 0.2f, 1f);
    public static final Color WARNING_COLOR = new Color(0.95f, 0.7f, 0.2f, 1f);
    public static final Color SUCCESS_COLOR = new Color(0.2f, 0.9f, 0.4f, 1f);
    
    // === TEXT COLORS ===
    public static final Color TEXT_PRIMARY = new Color(0.95f, 0.95f, 0.97f, 1f);
    public static final Color TEXT_SECONDARY = new Color(0.7f, 0.72f, 0.78f, 1f);
    public static final Color TEXT_MUTED = new Color(0.5f, 0.52f, 0.58f, 1f);
    public static final Color TEXT_HIGHLIGHT = new Color(0.2f, 0.85f, 0.95f, 1f);
    
    // === BORDER COLORS ===
    public static final Color BORDER_DEFAULT = new Color(0.25f, 0.28f, 0.35f, 0.8f);
    public static final Color BORDER_HIGHLIGHT = new Color(0.3f, 0.7f, 0.9f, 0.9f);
    public static final Color BORDER_GLOW = new Color(0.2f, 0.8f, 0.9f, 0.6f);
    
    // === RARITY COLORS (for items) ===
    public static final Color RARITY_COMMON = new Color(0.7f, 0.7f, 0.7f, 1f);
    public static final Color RARITY_UNCOMMON = new Color(0.3f, 0.8f, 0.3f, 1f);
    public static final Color RARITY_RARE = new Color(0.3f, 0.5f, 0.95f, 1f);
    public static final Color RARITY_EPIC = new Color(0.7f, 0.3f, 0.9f, 1f);
    public static final Color RARITY_LEGENDARY = new Color(0.95f, 0.7f, 0.2f, 1f);
    public static final Color RARITY_MYTHICAL = new Color(0.95f, 0.3f, 0.4f, 1f);
    
    // === SIZING ===
    public static final float BORDER_THICKNESS = 1.5f;
    public static final float BORDER_RADIUS = 4f;
    public static final float PANEL_PADDING = 12f;
    public static final float ELEMENT_SPACING = 8f;
    public static final float ICON_SIZE_SMALL = 16f;
    public static final float ICON_SIZE_MEDIUM = 24f;
    public static final float ICON_SIZE_LARGE = 32f;
    
    // === HUD SIZING ===
    public static final float HUD_BAR_HEIGHT = 8f;
    public static final float HUD_BAR_WIDTH = 200f;
    public static final float HUD_MARGIN = 20f;
    public static final float HUD_CORNER_RADIUS = 4f;
    
    // === FONT SCALES ===
    public static final float FONT_SCALE_TINY = 0.8f;
    public static final float FONT_SCALE_SMALL = 1.0f;
    public static final float FONT_SCALE_MEDIUM = 1.3f;
    public static final float FONT_SCALE_LARGE = 1.6f;
    public static final float FONT_SCALE_TITLE = 2.0f;
    public static final float FONT_SCALE_HEADER = 2.5f;
    
    // === ANIMATION TIMING (seconds) ===
    public static final float ANIM_FADE_FAST = 0.15f;
    public static final float ANIM_FADE_NORMAL = 0.25f;
    public static final float ANIM_FADE_SLOW = 0.4f;
    public static final float ANIM_SLIDE_DURATION = 0.3f;
    public static final float ANIM_PULSE_SPEED = 2f;
    
    // === GLOW EFFECTS ===
    public static final float GLOW_INTENSITY = 0.4f;
    public static final float GLOW_RADIUS = 8f;
    
    /**
     * Get interpolated color for smooth transitions
     */
    public static Color lerp(Color from, Color to, float progress) {
        return new Color(
            from.r + (to.r - from.r) * progress,
            from.g + (to.g - from.g) * progress,
            from.b + (to.b - from.b) * progress,
            from.a + (to.a - from.a) * progress
        );
    }
    
    /**
     * Get color with modified alpha
     */
    public static Color withAlpha(Color color, float alpha) {
        return new Color(color.r, color.g, color.b, alpha);
    }
    
    /**
     * Get brightened color for hover effects
     */
    public static Color brighten(Color color, float amount) {
        return new Color(
            Math.min(1f, color.r + amount),
            Math.min(1f, color.g + amount),
            Math.min(1f, color.b + amount),
            color.a
        );
    }
    
    /**
     * Get dimmed color for disabled states
     */
    public static Color dim(Color color, float amount) {
        return new Color(
            Math.max(0f, color.r - amount),
            Math.max(0f, color.g - amount),
            Math.max(0f, color.b - amount),
            color.a
        );
    }
}
