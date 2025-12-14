package com.astral.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Professional UI Button with Starfield-style aesthetics
 * Supports hover effects, press states, and various styles
 */
public class UIButton {
    
    public enum ButtonStyle {
        PRIMARY,        // Cyan accent, filled
        SECONDARY,      // Subtle, outline only
        DANGER,         // Red accent
        SUCCESS,        // Green accent
        GHOST           // Minimal, text only
    }
    
    public interface ButtonCallback {
        void onClick();
    }
    
    // Position and size
    private float x, y, width, height;
    
    // Button properties
    private String label;
    private ButtonStyle style = ButtonStyle.PRIMARY;
    private boolean enabled = true;
    private boolean visible = true;
    
    // State
    private boolean hovered = false;
    private boolean pressed = false;
    private float hoverProgress = 0f;
    private float pressProgress = 0f;
    
    // Callback
    private ButtonCallback callback;
    
    // Animation
    private float glowPhase = 0f;
    
    public UIButton(float x, float y, float width, float height, String label) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.label = label != null ? label : "";
    }
    
    public UIButton setStyle(ButtonStyle style) {
        this.style = style;
        return this;
    }
    
    public UIButton setCallback(ButtonCallback callback) {
        this.callback = callback;
        return this;
    }
    
    public UIButton setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    public void update(float delta, float mouseX, float mouseY, boolean mouseDown) {
        glowPhase += delta * UITheme.ANIM_PULSE_SPEED;
        
        // Check hover
        boolean wasHovered = hovered;
        hovered = enabled && contains(mouseX, mouseY);
        
        // Animate hover
        if (hovered) {
            hoverProgress = MathUtils.clamp(hoverProgress + delta * 8f, 0f, 1f);
        } else {
            hoverProgress = MathUtils.clamp(hoverProgress - delta * 8f, 0f, 1f);
        }
        
        // Check press
        boolean wasPressed = pressed;
        pressed = hovered && mouseDown;
        
        // Animate press
        if (pressed) {
            pressProgress = MathUtils.clamp(pressProgress + delta * 12f, 0f, 1f);
        } else {
            pressProgress = MathUtils.clamp(pressProgress - delta * 12f, 0f, 1f);
        }
        
        // Trigger callback on release
        if (wasPressed && !pressed && hovered && callback != null) {
            callback.onClick();
        }
    }
    
    public void render(UIRenderer renderer) {
        if (!visible) return;
        
        ShapeRenderer sr = renderer.getShapeRenderer();
        SpriteBatch batch = renderer.getSpriteBatch();
        BitmapFont font = renderer.getFont();
        GlyphLayout layout = renderer.getLayout();
        
        Gdx.gl.glEnable(GL20.GL_BLEND);
        
        // Get colors based on style
        Color accentColor = getAccentColor();
        Color bgColor = getBackgroundColor(accentColor);
        Color borderColor = getBorderColor(accentColor);
        Color textColor = getTextColor(accentColor);
        
        // Apply disabled state
        if (!enabled) {
            bgColor = UITheme.dim(bgColor, 0.3f);
            borderColor = UITheme.dim(borderColor, 0.3f);
            textColor = UITheme.TEXT_MUTED;
        }
        
        // Calculate visual offset for press effect
        float pressOffset = pressProgress * 2f;
        float drawY = y - pressOffset;
        
        // Glow effect when hovered
        if (hoverProgress > 0 && enabled) {
            float glowAlpha = hoverProgress * (0.15f + 0.05f * MathUtils.sin(glowPhase));
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(UITheme.withAlpha(accentColor, glowAlpha));
            drawRoundedRect(sr, x - 3, drawY - 3, width + 6, height + 6, UITheme.BORDER_RADIUS + 3);
            sr.end();
        }
        
        // Background
        if (style != ButtonStyle.GHOST) {
            sr.begin(ShapeRenderer.ShapeType.Filled);
            sr.setColor(bgColor);
            drawRoundedRect(sr, x, drawY, width, height, UITheme.BORDER_RADIUS);
            sr.end();
            
            // Highlight gradient at top
            if (enabled && style == ButtonStyle.PRIMARY) {
                sr.begin(ShapeRenderer.ShapeType.Filled);
                sr.setColor(UITheme.withAlpha(Color.WHITE, 0.08f + hoverProgress * 0.05f));
                sr.rect(x + 2, drawY + height - 6, width - 4, 4);
                sr.end();
            }
        }
        
        // Border
        if (style != ButtonStyle.GHOST) {
            sr.begin(ShapeRenderer.ShapeType.Line);
            Gdx.gl.glLineWidth(UITheme.BORDER_THICKNESS);
            sr.setColor(borderColor);
            drawRoundedRectOutline(sr, x, drawY, width, height, UITheme.BORDER_RADIUS);
            sr.end();
        }
        
        // Label
        batch.begin();
        font.getData().setScale(UITheme.FONT_SCALE_SMALL);
        font.setColor(textColor);
        
        layout.setText(font, label.toUpperCase());
        float labelX = x + (width - layout.width) / 2;
        float labelY = drawY + (height + layout.height) / 2;
        
        font.draw(batch, label.toUpperCase(), labelX, labelY);
        batch.end();
    }
    
    // ========== COLOR HELPERS ==========
    
    private Color getAccentColor() {
        switch (style) {
            case DANGER: return UITheme.DANGER_COLOR;
            case SUCCESS: return UITheme.SUCCESS_COLOR;
            case SECONDARY: return UITheme.TEXT_SECONDARY;
            case GHOST: return UITheme.TEXT_PRIMARY;
            default: return UITheme.ACCENT_CYAN;
        }
    }
    
    private Color getBackgroundColor(Color accent) {
        switch (style) {
            case PRIMARY:
                Color base = UITheme.withAlpha(accent, 0.15f);
                return UITheme.lerp(base, UITheme.withAlpha(accent, 0.25f), hoverProgress);
            case SECONDARY:
                return UITheme.lerp(UITheme.BACKGROUND_DARK, UITheme.BACKGROUND_LIGHT, hoverProgress);
            case DANGER:
            case SUCCESS:
                Color baseColor = UITheme.withAlpha(accent, 0.1f);
                return UITheme.lerp(baseColor, UITheme.withAlpha(accent, 0.2f), hoverProgress);
            default:
                return UITheme.BACKGROUND_DARK;
        }
    }
    
    private Color getBorderColor(Color accent) {
        switch (style) {
            case PRIMARY:
            case DANGER:
            case SUCCESS:
                return UITheme.lerp(UITheme.withAlpha(accent, 0.6f), accent, hoverProgress);
            case SECONDARY:
                return UITheme.lerp(UITheme.BORDER_DEFAULT, UITheme.BORDER_HIGHLIGHT, hoverProgress);
            default:
                return UITheme.BORDER_DEFAULT;
        }
    }
    
    private Color getTextColor(Color accent) {
        switch (style) {
            case PRIMARY:
            case DANGER:
            case SUCCESS:
                return UITheme.lerp(UITheme.TEXT_PRIMARY, accent, hoverProgress * 0.5f);
            case SECONDARY:
                return UITheme.lerp(UITheme.TEXT_SECONDARY, UITheme.TEXT_PRIMARY, hoverProgress);
            case GHOST:
                return UITheme.lerp(UITheme.TEXT_MUTED, UITheme.TEXT_PRIMARY, hoverProgress);
            default:
                return UITheme.TEXT_PRIMARY;
        }
    }
    
    // ========== GEOMETRY HELPERS ==========
    
    private void drawRoundedRect(ShapeRenderer sr, float x, float y, float w, float h, float r) {
        sr.rect(x + r, y, w - 2 * r, h);
        sr.rect(x, y + r, w, h - 2 * r);
        sr.arc(x + r, y + r, r, 180, 90, 8);
        sr.arc(x + w - r, y + r, r, 270, 90, 8);
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
    
    public boolean contains(float px, float py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }
    
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public String getLabel() { return label; }
    public boolean isEnabled() { return enabled; }
    public boolean isHovered() { return hovered; }
    public boolean isPressed() { return pressed; }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
    }
    
    public void setLabel(String label) {
        this.label = label != null ? label : "";
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
