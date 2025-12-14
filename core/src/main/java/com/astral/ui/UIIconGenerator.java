package com.astral.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Procedural Icon Generator for UI elements
 * Creates all icons programmatically - no external assets needed
 */
public class UIIconGenerator implements Disposable {
    
    private ObjectMap<String, Texture> iconCache;
    
    // Default icon size
    private static final int DEFAULT_SIZE = 32;
    
    public UIIconGenerator() {
        iconCache = new ObjectMap<>();
        generateCommonIcons();
    }
    
    private void generateCommonIcons() {
        // Generate all common UI icons
        generateHealthIcon();
        generateShieldIcon();
        generateFuelIcon();
        generateAmmoIcon();
        generateCreditsIcon();
        generateWarningIcon();
        generateCheckmarkIcon();
        generateCloseIcon();
        generateArrowIcons();
        generateWeaponIcons();
        generateInventoryIcons();
    }
    
    // ========== ICON GENERATION METHODS ==========
    
    private void generateHealthIcon() {
        Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        
        // Draw heart shape
        Color c = UITheme.HEALTH_COLOR;
        pm.setColor(c);
        
        int cx = DEFAULT_SIZE / 2;
        int cy = DEFAULT_SIZE / 2;
        int size = DEFAULT_SIZE / 3;
        
        // Heart using circles and triangle
        drawFilledCircle(pm, cx - size/3, cy + size/4, size/2, c);
        drawFilledCircle(pm, cx + size/3, cy + size/4, size/2, c);
        
        // Bottom point
        for (int y = cy; y >= cy - size; y--) {
            int halfWidth = (cy - y) * size / (size + 2);
            for (int x = cx - halfWidth; x <= cx + halfWidth; x++) {
                pm.drawPixel(x, y);
            }
        }
        
        iconCache.put("health", new Texture(pm));
        pm.dispose();
    }
    
    private void generateShieldIcon() {
        Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        
        Color c = UITheme.SHIELD_COLOR;
        pm.setColor(c);
        
        int cx = DEFAULT_SIZE / 2;
        int top = 4;
        int bottom = DEFAULT_SIZE - 4;
        int width = DEFAULT_SIZE / 2 - 2;
        
        // Shield outline
        for (int y = top; y < bottom; y++) {
            float progress = (float)(y - top) / (bottom - top);
            int halfWidth = (int)(width * (1f - progress * 0.6f));
            
            pm.drawPixel(cx - halfWidth, y);
            pm.drawPixel(cx + halfWidth, y);
            
            // Fill at top
            if (y < top + 4) {
                for (int x = cx - halfWidth; x <= cx + halfWidth; x++) {
                    pm.drawPixel(x, y);
                }
            }
        }
        
        // Point at bottom
        pm.drawPixel(cx, bottom);
        
        iconCache.put("shield", new Texture(pm));
        pm.dispose();
    }
    
    private void generateFuelIcon() {
        Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        
        Color c = UITheme.FUEL_COLOR;
        pm.setColor(c);
        
        // Fuel drop shape
        int cx = DEFAULT_SIZE / 2;
        int top = 4;
        int bottom = DEFAULT_SIZE - 4;
        
        for (int y = top; y < bottom; y++) {
            float progress = (float)(y - top) / (bottom - top);
            int radius;
            if (progress < 0.4f) {
                radius = (int)(progress * 12);
            } else {
                radius = (int)((1f - (progress - 0.4f) * 1.2f) * 8);
            }
            radius = Math.max(1, radius);
            
            for (int x = cx - radius; x <= cx + radius; x++) {
                pm.drawPixel(x, y);
            }
        }
        
        iconCache.put("fuel", new Texture(pm));
        pm.dispose();
    }
    
    private void generateAmmoIcon() {
        Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        
        Color c = UITheme.WARNING_COLOR;
        pm.setColor(c);
        
        // Bullet shape
        int left = 8;
        int right = DEFAULT_SIZE - 8;
        int top = 6;
        int bottom = DEFAULT_SIZE - 6;
        int tipHeight = 8;
        
        // Body
        pm.fillRectangle(left, top + tipHeight, right - left, bottom - top - tipHeight);
        
        // Tip (triangle)
        int cx = DEFAULT_SIZE / 2;
        for (int y = top; y < top + tipHeight; y++) {
            int halfWidth = (y - top) * (right - left) / (2 * tipHeight);
            for (int x = cx - halfWidth; x <= cx + halfWidth; x++) {
                pm.drawPixel(x, y);
            }
        }
        
        iconCache.put("ammo", new Texture(pm));
        pm.dispose();
    }
    
    private void generateCreditsIcon() {
        Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        
        Color c = UITheme.ACCENT_GOLD;
        pm.setColor(c);
        
        // Coin/credit symbol (hexagon with line through)
        int cx = DEFAULT_SIZE / 2;
        int cy = DEFAULT_SIZE / 2;
        int radius = DEFAULT_SIZE / 3;
        
        // Hexagon outline
        for (int i = 0; i < 6; i++) {
            double a1 = Math.PI / 3 * i - Math.PI / 6;
            double a2 = Math.PI / 3 * (i + 1) - Math.PI / 6;
            int x1 = cx + (int)(Math.cos(a1) * radius);
            int y1 = cy + (int)(Math.sin(a1) * radius);
            int x2 = cx + (int)(Math.cos(a2) * radius);
            int y2 = cy + (int)(Math.sin(a2) * radius);
            drawLine(pm, x1, y1, x2, y2, c);
        }
        
        // Vertical line through center
        drawLine(pm, cx, cy - radius + 2, cx, cy + radius - 2, c);
        
        iconCache.put("credits", new Texture(pm));
        pm.dispose();
    }
    
    private void generateWarningIcon() {
        Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        
        Color c = UITheme.WARNING_COLOR;
        pm.setColor(c);
        
        // Triangle
        int cx = DEFAULT_SIZE / 2;
        int top = 4;
        int bottom = DEFAULT_SIZE - 4;
        int halfBase = DEFAULT_SIZE / 2 - 4;
        
        // Outline
        drawLine(pm, cx, top, cx - halfBase, bottom, c);
        drawLine(pm, cx, top, cx + halfBase, bottom, c);
        drawLine(pm, cx - halfBase, bottom, cx + halfBase, bottom, c);
        
        // Exclamation mark
        pm.fillRectangle(cx - 1, top + 8, 3, 10);
        pm.fillRectangle(cx - 1, bottom - 6, 3, 3);
        
        iconCache.put("warning", new Texture(pm));
        pm.dispose();
    }
    
    private void generateCheckmarkIcon() {
        Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        
        Color c = UITheme.SUCCESS_COLOR;
        
        // Checkmark
        int x1 = 6, y1 = DEFAULT_SIZE / 2;
        int x2 = DEFAULT_SIZE / 3, y2 = DEFAULT_SIZE - 8;
        int x3 = DEFAULT_SIZE - 6, y3 = 8;
        
        drawThickLine(pm, x1, y1, x2, y2, 3, c);
        drawThickLine(pm, x2, y2, x3, y3, 3, c);
        
        iconCache.put("checkmark", new Texture(pm));
        pm.dispose();
    }
    
    private void generateCloseIcon() {
        Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        
        Color c = UITheme.TEXT_PRIMARY;
        
        // X shape
        int margin = 8;
        drawThickLine(pm, margin, margin, DEFAULT_SIZE - margin, DEFAULT_SIZE - margin, 2, c);
        drawThickLine(pm, DEFAULT_SIZE - margin, margin, margin, DEFAULT_SIZE - margin, 2, c);
        
        iconCache.put("close", new Texture(pm));
        pm.dispose();
    }
    
    private void generateArrowIcons() {
        String[] directions = {"up", "down", "left", "right"};
        int[][] offsets = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int d = 0; d < 4; d++) {
            Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
            pm.setColor(0, 0, 0, 0);
            pm.fill();
            
            Color c = UITheme.TEXT_PRIMARY;
            pm.setColor(c);
            
            int cx = DEFAULT_SIZE / 2;
            int cy = DEFAULT_SIZE / 2;
            int size = DEFAULT_SIZE / 3;
            
            // Arrow chevron
            int dx = offsets[d][0];
            int dy = offsets[d][1];
            
            if (dx != 0) {
                // Horizontal arrow
                int tipX = cx + dx * size;
                drawThickLine(pm, tipX, cy, cx - dx * size/2, cy - size/2, 2, c);
                drawThickLine(pm, tipX, cy, cx - dx * size/2, cy + size/2, 2, c);
            } else {
                // Vertical arrow  
                int tipY = cy + dy * size;
                drawThickLine(pm, cx, tipY, cx - size/2, cy - dy * size/2, 2, c);
                drawThickLine(pm, cx, tipY, cx + size/2, cy - dy * size/2, 2, c);
            }
            
            iconCache.put("arrow_" + directions[d], new Texture(pm));
            pm.dispose();
        }
    }
    
    private void generateWeaponIcons() {
        // Pistol icon
        Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        
        Color c = UITheme.TEXT_PRIMARY;
        pm.setColor(c);
        
        // Simple pistol shape
        pm.fillRectangle(4, 12, 20, 6);  // Barrel
        pm.fillRectangle(14, 18, 6, 10); // Grip
        
        iconCache.put("weapon_pistol", new Texture(pm));
        pm.dispose();
        
        // Rifle icon
        pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        pm.setColor(c);
        
        pm.fillRectangle(2, 12, 28, 5);   // Barrel
        pm.fillRectangle(18, 17, 6, 10);  // Grip
        pm.fillRectangle(6, 10, 8, 2);    // Scope
        
        iconCache.put("weapon_rifle", new Texture(pm));
        pm.dispose();
    }
    
    private void generateInventoryIcons() {
        // Backpack/inventory icon
        Pixmap pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        
        Color c = UITheme.TEXT_PRIMARY;
        pm.setColor(c);
        
        // Bag shape
        pm.fillRectangle(6, 10, 20, 18);
        pm.setColor(UITheme.BACKGROUND_DARK);
        pm.fillRectangle(10, 14, 12, 10);
        pm.setColor(c);
        
        // Handle
        pm.fillRectangle(10, 4, 12, 4);
        pm.fillRectangle(8, 6, 4, 6);
        pm.fillRectangle(20, 6, 4, 6);
        
        iconCache.put("inventory", new Texture(pm));
        pm.dispose();
        
        // Gear/settings icon
        pm = new Pixmap(DEFAULT_SIZE, DEFAULT_SIZE, Pixmap.Format.RGBA8888);
        pm.setColor(0, 0, 0, 0);
        pm.fill();
        pm.setColor(c);
        
        int cx = DEFAULT_SIZE / 2;
        int cy = DEFAULT_SIZE / 2;
        
        // Gear teeth
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI / 4 * i;
            int x = cx + (int)(Math.cos(angle) * 10);
            int y = cy + (int)(Math.sin(angle) * 10);
            pm.fillRectangle(x - 2, y - 2, 5, 5);
        }
        
        // Center circle
        drawFilledCircle(pm, cx, cy, 6, c);
        pm.setColor(UITheme.BACKGROUND_DARK);
        drawFilledCircle(pm, cx, cy, 3, UITheme.BACKGROUND_DARK);
        
        iconCache.put("settings", new Texture(pm));
        pm.dispose();
    }
    
    // ========== DRAWING HELPERS ==========
    
    private void drawFilledCircle(Pixmap pm, int cx, int cy, int radius, Color color) {
        pm.setColor(color);
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                if (x*x + y*y <= radius*radius) {
                    pm.drawPixel(cx + x, cy + y);
                }
            }
        }
    }
    
    private void drawLine(Pixmap pm, int x1, int y1, int x2, int y2, Color color) {
        pm.setColor(color);
        
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        
        while (true) {
            pm.drawPixel(x1, y1);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x1 += sx; }
            if (e2 < dx) { err += dx; y1 += sy; }
        }
    }
    
    private void drawThickLine(Pixmap pm, int x1, int y1, int x2, int y2, int thickness, Color color) {
        for (int t = -thickness/2; t <= thickness/2; t++) {
            if (Math.abs(x2 - x1) > Math.abs(y2 - y1)) {
                drawLine(pm, x1, y1 + t, x2, y2 + t, color);
            } else {
                drawLine(pm, x1 + t, y1, x2 + t, y2, color);
            }
        }
    }
    
    // ========== PUBLIC API ==========
    
    public Texture getIcon(String name) {
        return iconCache.get(name);
    }
    
    public boolean hasIcon(String name) {
        return iconCache.containsKey(name);
    }
    
    @Override
    public void dispose() {
        for (Texture tex : iconCache.values()) {
            tex.dispose();
        }
        iconCache.clear();
    }
}
