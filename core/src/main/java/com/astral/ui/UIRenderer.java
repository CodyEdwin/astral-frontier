package com.astral.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

/**
 * Professional UI Renderer with Starfield-inspired aesthetic
 * Handles all primitive drawing with proper styling
 */
public class UIRenderer implements Disposable {
    
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private GlyphLayout layout;
    
    private float time = 0f;
    
    public UIRenderer() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(UITheme.FONT_SCALE_SMALL);
        layout = new GlyphLayout();
    }
    
    public void update(float delta) {
        time += delta;
    }
    
    // ========== PANEL DRAWING ==========
    
    /**
     * Draw a styled panel with border and optional glow
     */
    public void drawPanel(float x, float y, float width, float height, boolean glow) {
        enableBlend();
        
        // Background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UITheme.BACKGROUND_DARK);
        drawRoundedRect(x, y, width, height, UITheme.BORDER_RADIUS);
        shapeRenderer.end();
        
        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(UITheme.BORDER_THICKNESS);
        shapeRenderer.setColor(glow ? UITheme.BORDER_HIGHLIGHT : UITheme.BORDER_DEFAULT);
        drawRoundedRectOutline(x, y, width, height, UITheme.BORDER_RADIUS);
        shapeRenderer.end();
        
        // Glow effect
        if (glow) {
            float glowAlpha = 0.15f + 0.1f * MathUtils.sin(time * UITheme.ANIM_PULSE_SPEED);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(UITheme.withAlpha(UITheme.BORDER_GLOW, glowAlpha));
            drawRoundedRect(x - 2, y - 2, width + 4, height + 4, UITheme.BORDER_RADIUS + 2);
            shapeRenderer.end();
        }
    }
    
    /**
     * Draw a header panel (darker top section)
     */
    public void drawHeaderPanel(float x, float y, float width, float height, String title) {
        enableBlend();
        
        // Header background (slightly lighter)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UITheme.BACKGROUND_MEDIUM);
        drawRoundedRectTop(x, y + height - 40, width, 40, UITheme.BORDER_RADIUS);
        shapeRenderer.end();
        
        // Main panel background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UITheme.BACKGROUND_DARK);
        shapeRenderer.rect(x, y, width, height - 40);
        shapeRenderer.end();
        
        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UITheme.BORDER_DEFAULT);
        drawRoundedRectOutline(x, y, width, height, UITheme.BORDER_RADIUS);
        // Header separator line
        shapeRenderer.line(x, y + height - 40, x + width, y + height - 40);
        shapeRenderer.end();
        
        // Title
        if (title != null && !title.isEmpty()) {
            spriteBatch.begin();
            font.getData().setScale(UITheme.FONT_SCALE_MEDIUM);
            font.setColor(UITheme.ACCENT_CYAN);
            layout.setText(font, title.toUpperCase());
            font.draw(spriteBatch, title.toUpperCase(), x + UITheme.PANEL_PADDING, y + height - 12);
            spriteBatch.end();
        }
    }
    
    // ========== STATUS BARS ==========
    
    /**
     * Draw a horizontal status bar (health, shield, fuel, etc.)
     */
    public void drawStatusBar(float x, float y, float width, float height, 
                              float value, float maxValue, Color fillColor, String label) {
        float progress = MathUtils.clamp(value / maxValue, 0f, 1f);
        
        enableBlend();
        
        // Background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UITheme.BACKGROUND_DARK);
        drawRoundedRect(x, y, width, height, UITheme.HUD_CORNER_RADIUS);
        shapeRenderer.end();
        
        // Fill
        if (progress > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            
            // Gradient effect - darker at bottom
            Color darkFill = UITheme.dim(fillColor, 0.3f);
            float fillWidth = (width - 4) * progress;
            
            shapeRenderer.rect(x + 2, y + 2, fillWidth, height - 4,
                darkFill, darkFill, fillColor, fillColor);
            shapeRenderer.end();
            
            // Shine highlight at top
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(UITheme.withAlpha(Color.WHITE, 0.15f));
            shapeRenderer.rect(x + 2, y + height - 4, fillWidth, 2);
            shapeRenderer.end();
        }
        
        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UITheme.BORDER_DEFAULT);
        drawRoundedRectOutline(x, y, width, height, UITheme.HUD_CORNER_RADIUS);
        shapeRenderer.end();
        
        // Label
        if (label != null) {
            spriteBatch.begin();
            font.getData().setScale(UITheme.FONT_SCALE_TINY);
            font.setColor(UITheme.TEXT_SECONDARY);
            font.draw(spriteBatch, label, x + 4, y + height + 14);
            spriteBatch.end();
        }
    }
    
    /**
     * Draw a circular status indicator
     */
    public void drawCircularIndicator(float cx, float cy, float radius, 
                                       float value, float maxValue, Color fillColor) {
        float progress = MathUtils.clamp(value / maxValue, 0f, 1f);
        float angle = progress * 360f;
        
        enableBlend();
        
        // Background ring
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(4f);
        shapeRenderer.setColor(UITheme.BACKGROUND_LIGHT);
        shapeRenderer.arc(cx, cy, radius, 0, 360, 64);
        shapeRenderer.end();
        
        // Progress arc
        if (progress > 0) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(4f);
            shapeRenderer.setColor(fillColor);
            shapeRenderer.arc(cx, cy, radius, 90, -angle, (int)(64 * progress) + 1);
            shapeRenderer.end();
        }
        
        // Center glow
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UITheme.withAlpha(fillColor, 0.2f));
        shapeRenderer.circle(cx, cy, radius * 0.6f, 32);
        shapeRenderer.end();
    }
    
    // ========== CROSSHAIR ==========
    
    /**
     * Draw a professional crosshair
     */
    public void drawCrosshair(float cx, float cy, boolean hit, float hitTimer) {
        float size = 12f;
        float gap = 4f;
        float thickness = 2f;
        
        enableBlend();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Crosshair color
        if (hit && hitTimer > 0) {
            float pulse = hitTimer * 2f;
            shapeRenderer.setColor(UITheme.lerp(UITheme.DANGER_COLOR, Color.WHITE, pulse * 0.5f));
        } else {
            shapeRenderer.setColor(UITheme.TEXT_PRIMARY);
        }
        
        // Draw crosshair lines
        shapeRenderer.rectLine(cx, cy + gap, cx, cy + size + gap, thickness);
        shapeRenderer.rectLine(cx, cy - gap, cx, cy - size - gap, thickness);
        shapeRenderer.rectLine(cx - gap, cy, cx - size - gap, cy, thickness);
        shapeRenderer.rectLine(cx + gap, cy, cx + size + gap, cy, thickness);
        
        // Center dot
        shapeRenderer.circle(cx, cy, 2f, 8);
        
        // Hit marker X
        if (hit && hitTimer > 0) {
            float hitSize = 8f + hitTimer * 8f;
            shapeRenderer.setColor(UITheme.withAlpha(UITheme.DANGER_COLOR, hitTimer));
            shapeRenderer.rectLine(cx - hitSize, cy - hitSize, cx + hitSize, cy + hitSize, 3f);
            shapeRenderer.rectLine(cx - hitSize, cy + hitSize, cx + hitSize, cy - hitSize, 3f);
        }
        
        shapeRenderer.end();
        
        // Outer ring
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        Gdx.gl.glLineWidth(1f);
        shapeRenderer.setColor(UITheme.withAlpha(UITheme.TEXT_PRIMARY, 0.4f));
        shapeRenderer.circle(cx, cy, size + 6, 32);
        shapeRenderer.end();
    }
    
    // ========== COMPASS ==========
    
    /**
     * Draw a compass bar at top of screen
     */
    public void drawCompass(float cx, float y, float width, float playerYaw) {
        float height = 24f;
        float x = cx - width / 2;
        
        enableBlend();
        
        // Background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UITheme.withAlpha(UITheme.BACKGROUND_DARK, 0.8f));
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UITheme.BORDER_DEFAULT);
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        // Direction markers
        spriteBatch.begin();
        font.getData().setScale(UITheme.FONT_SCALE_SMALL);
        
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        float[] angles = {0, 45, 90, 135, 180, 225, 270, 315};
        
        for (int i = 0; i < directions.length; i++) {
            float relAngle = normalizeAngle(angles[i] - playerYaw);
            if (Math.abs(relAngle) < 90) {
                float xPos = cx + (relAngle / 90f) * (width / 2 - 20);
                
                boolean isCardinal = i % 2 == 0;
                font.setColor(isCardinal ? UITheme.ACCENT_CYAN : UITheme.TEXT_MUTED);
                layout.setText(font, directions[i]);
                font.draw(spriteBatch, directions[i], xPos - layout.width / 2, y + height - 4);
            }
        }
        
        spriteBatch.end();
        
        // Center indicator
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UITheme.ACCENT_CYAN);
        shapeRenderer.triangle(cx - 4, y + height, cx + 4, y + height, cx, y + height - 6);
        shapeRenderer.end();
    }
    
    // ========== NOTIFICATIONS ==========
    
    /**
     * Draw a notification popup
     */
    public void drawNotification(float x, float y, String message, Color accentColor, float alpha) {
        if (alpha <= 0) return;
        
        layout.setText(font, message);
        float width = layout.width + UITheme.PANEL_PADDING * 2;
        float height = 36f;
        
        enableBlend();
        
        // Background with accent stripe
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(UITheme.withAlpha(UITheme.BACKGROUND_DARK, alpha * 0.9f));
        shapeRenderer.rect(x, y, width, height);
        
        // Accent stripe on left
        shapeRenderer.setColor(UITheme.withAlpha(accentColor, alpha));
        shapeRenderer.rect(x, y, 4, height);
        shapeRenderer.end();
        
        // Border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(UITheme.withAlpha(UITheme.BORDER_DEFAULT, alpha));
        shapeRenderer.rect(x, y, width, height);
        shapeRenderer.end();
        
        // Text
        spriteBatch.begin();
        font.getData().setScale(UITheme.FONT_SCALE_SMALL);
        font.setColor(UITheme.withAlpha(UITheme.TEXT_PRIMARY, alpha));
        font.draw(spriteBatch, message, x + UITheme.PANEL_PADDING + 4, y + height - 10);
        spriteBatch.end();
    }
    
    // ========== HELPER METHODS ==========
    
    private void enableBlend() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    private void drawRoundedRect(float x, float y, float width, float height, float radius) {
        // Main rect
        shapeRenderer.rect(x + radius, y, width - 2 * radius, height);
        shapeRenderer.rect(x, y + radius, width, height - 2 * radius);
        
        // Corners
        shapeRenderer.arc(x + radius, y + radius, radius, 180, 90, 8);
        shapeRenderer.arc(x + width - radius, y + radius, radius, 270, 90, 8);
        shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0, 90, 8);
        shapeRenderer.arc(x + radius, y + height - radius, radius, 90, 90, 8);
    }
    
    private void drawRoundedRectTop(float x, float y, float width, float height, float radius) {
        // Main rect
        shapeRenderer.rect(x, y, width, height - radius);
        shapeRenderer.rect(x + radius, y + height - radius, width - 2 * radius, radius);
        
        // Top corners only
        shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0, 90, 8);
        shapeRenderer.arc(x + radius, y + height - radius, radius, 90, 90, 8);
    }
    
    private void drawRoundedRectOutline(float x, float y, float width, float height, float radius) {
        // Lines
        shapeRenderer.line(x + radius, y, x + width - radius, y);
        shapeRenderer.line(x + radius, y + height, x + width - radius, y + height);
        shapeRenderer.line(x, y + radius, x, y + height - radius);
        shapeRenderer.line(x + width, y + radius, x + width, y + height - radius);
        
        // Corner arcs
        shapeRenderer.arc(x + radius, y + radius, radius, 180, 90, 8);
        shapeRenderer.arc(x + width - radius, y + radius, radius, 270, 90, 8);
        shapeRenderer.arc(x + width - radius, y + height - radius, radius, 0, 90, 8);
        shapeRenderer.arc(x + radius, y + height - radius, radius, 90, 90, 8);
    }
    
    private float normalizeAngle(float angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }
    
    public ShapeRenderer getShapeRenderer() { return shapeRenderer; }
    public SpriteBatch getSpriteBatch() { return spriteBatch; }
    public BitmapFont getFont() { return font; }
    public GlyphLayout getLayout() { return layout; }
    
    @Override
    public void dispose() {
        shapeRenderer.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
