package com.astral.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Animated UI Panel with Starfield-style aesthetics
 * Supports slide-in/fade animations and various panel types
 */
public class UIPanel {
    
    public enum PanelType {
        DEFAULT,        // Standard dark panel
        HEADER,         // Panel with header section
        TRANSPARENT,    // More transparent background
        ACCENT          // Highlighted border
    }
    
    public enum AnimationType {
        NONE,
        FADE,
        SLIDE_LEFT,
        SLIDE_RIGHT,
        SLIDE_UP,
        SLIDE_DOWN,
        SCALE
    }
    
    // Position and size
    private float x, y, width, height;
    private float targetX, targetY;
    
    // Panel properties
    private PanelType type = PanelType.DEFAULT;
    private String title = "";
    private Color accentColor = UITheme.ACCENT_CYAN;
    private boolean visible = true;
    private boolean focused = false;
    
    // Animation state
    private AnimationType animationType = AnimationType.NONE;
    private float animProgress = 1f;
    private float animSpeed = 4f;
    private boolean animating = false;
    private boolean animatingIn = true;
    
    // Content offset for header panels
    private float contentOffsetY = 0f;
    
    public UIPanel(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.width = width;
        this.height = height;
    }
    
    public UIPanel setType(PanelType type) {
        this.type = type;
        if (type == PanelType.HEADER) {
            contentOffsetY = 40f;
        }
        return this;
    }
    
    public UIPanel setTitle(String title) {
        this.title = title != null ? title : "";
        return this;
    }
    
    public UIPanel setAccentColor(Color color) {
        this.accentColor = color;
        return this;
    }
    
    public UIPanel setAnimation(AnimationType anim) {
        this.animationType = anim;
        return this;
    }
    
    public void show() {
        if (animationType != AnimationType.NONE) {
            animating = true;
            animatingIn = true;
            animProgress = 0f;
        }
        visible = true;
    }
    
    public void hide() {
        if (animationType != AnimationType.NONE) {
            animating = true;
            animatingIn = false;
            animProgress = 1f;
        } else {
            visible = false;
        }
    }
    
    public void update(float delta) {
        if (animating) {
            if (animatingIn) {
                animProgress += delta * animSpeed;
                if (animProgress >= 1f) {
                    animProgress = 1f;
                    animating = false;
                }
            } else {
                animProgress -= delta * animSpeed;
                if (animProgress <= 0f) {
                    animProgress = 0f;
                    animating = false;
                    visible = false;
                }
            }
        }
    }
    
    public void render(UIRenderer renderer) {
        if (!visible && !animating) return;
        
        // Calculate animated position and alpha
        float displayX = x;
        float displayY = y;
        float alpha = 1f;
        float scale = 1f;
        
        float easeProgress = easeOutCubic(animProgress);
        
        switch (animationType) {
            case FADE:
                alpha = easeProgress;
                break;
            case SLIDE_LEFT:
                displayX = x - (1f - easeProgress) * 100f;
                alpha = easeProgress;
                break;
            case SLIDE_RIGHT:
                displayX = x + (1f - easeProgress) * 100f;
                alpha = easeProgress;
                break;
            case SLIDE_UP:
                displayY = y - (1f - easeProgress) * 100f;
                alpha = easeProgress;
                break;
            case SLIDE_DOWN:
                displayY = y + (1f - easeProgress) * 100f;
                alpha = easeProgress;
                break;
            case SCALE:
                scale = easeProgress;
                alpha = easeProgress;
                displayX = x + width * (1f - scale) / 2;
                displayY = y + height * (1f - scale) / 2;
                break;
            default:
                break;
        }
        
        renderPanel(renderer, displayX, displayY, width * scale, height * scale, alpha);
    }
    
    private void renderPanel(UIRenderer renderer, float px, float py, float pw, float ph, float alpha) {
        ShapeRenderer sr = renderer.getShapeRenderer();
        SpriteBatch batch = renderer.getSpriteBatch();
        BitmapFont font = renderer.getFont();
        
        com.badlogic.gdx.Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        
        // Background color based on type
        Color bgColor = UITheme.BACKGROUND_DARK;
        if (type == PanelType.TRANSPARENT) {
            bgColor = UITheme.withAlpha(UITheme.BACKGROUND_DARK, 0.7f);
        }
        bgColor = UITheme.withAlpha(bgColor, bgColor.a * alpha);
        
        // Draw main background
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(bgColor);
        drawRoundedRect(sr, px, py, pw, ph, UITheme.BORDER_RADIUS);
        sr.end();
        
        // Header section for HEADER type
        if (type == PanelType.HEADER && ph > 40) {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(UITheme.withAlpha(UITheme.BACKGROUND_MEDIUM, alpha));
            drawRoundedRectTop(sr, px, py + ph - 40, pw, 40, UITheme.BORDER_RADIUS);
            sr.end();
            
            // Header separator
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(UITheme.withAlpha(UITheme.BORDER_DEFAULT, alpha));
            sr.line(px, py + ph - 40, px + pw, py + ph - 40);
            sr.end();
            
            // Title
            if (!title.isEmpty()) {
                batch.begin();
                font.getData().setScale(UITheme.FONT_SCALE_MEDIUM);
                font.setColor(UITheme.withAlpha(accentColor, alpha));
                font.draw(batch, title.toUpperCase(), px + UITheme.PANEL_PADDING, py + ph - 12);
                batch.end();
            }
        }
        
        // Border
        Color borderColor = focused ? UITheme.BORDER_HIGHLIGHT : UITheme.BORDER_DEFAULT;
        if (type == PanelType.ACCENT) {
            borderColor = accentColor;
        }
        
        sr.begin(ShapeRenderer.ShapeType.Line);
        com.badlogic.gdx.Gdx.gl.glLineWidth(UITheme.BORDER_THICKNESS);
        sr.setColor(UITheme.withAlpha(borderColor, alpha));
        drawRoundedRectOutline(sr, px, py, pw, ph, UITheme.BORDER_RADIUS);
        sr.end();
        
        // Glow effect when focused
        if (focused && alpha > 0.5f) {
            sr.begin(ShapeRenderer.ShapeType.Line);
            sr.setColor(UITheme.withAlpha(UITheme.BORDER_GLOW, alpha * 0.3f));
            drawRoundedRectOutline(sr, px - 2, py - 2, pw + 4, ph + 4, UITheme.BORDER_RADIUS + 2);
            sr.end();
        }
    }
    
    // ========== HELPER METHODS ==========
    
    private float easeOutCubic(float t) {
        return 1f - (float)Math.pow(1 - t, 3);
    }
    
    private void drawRoundedRect(ShapeRenderer sr, float x, float y, float w, float h, float r) {
        sr.rect(x + r, y, w - 2 * r, h);
        sr.rect(x, y + r, w, h - 2 * r);
        sr.arc(x + r, y + r, r, 180, 90, 8);
        sr.arc(x + w - r, y + r, r, 270, 90, 8);
        sr.arc(x + w - r, y + h - r, r, 0, 90, 8);
        sr.arc(x + r, y + h - r, r, 90, 90, 8);
    }
    
    private void drawRoundedRectTop(ShapeRenderer sr, float x, float y, float w, float h, float r) {
        sr.rect(x, y, w, h - r);
        sr.rect(x + r, y + h - r, w - 2 * r, r);
        sr.arc(x + w - r, y + h - r, r, 0, 90, 8);
        sr.arc(x + r, y + h - r, r, 90, 90, 8);
    }
    
    private void drawRoundedRectOutline(ShapeRenderer sr, float x, float y, float w, float h, float r) {
        sr.line(x + r, y, x + w - r, y);
        sr.line(x + r, y + h, x + w - r, y + h);
        sr.line(x, y + r, x, y + h - r);
        sr.line(x + w, y + r, x + w, y + h - r);
        sr.arc(x + r, y + r, r, 180, 90, 8);
        sr.arc(x + w - r, y + r, r, 270, 90, 8);
        sr.arc(x + w - r, y + h - r, r, 0, 90, 8);
        sr.arc(x + r, y + h - r, r, 90, 90, 8);
    }
    
    // ========== GETTERS & SETTERS ==========
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getContentY() { return y; }
    public float getContentHeight() { return height - contentOffsetY; }
    public boolean isVisible() { return visible; }
    public boolean isAnimating() { return animating; }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
    
    public void setFocused(boolean focused) {
        this.focused = focused;
    }
    
    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
}
