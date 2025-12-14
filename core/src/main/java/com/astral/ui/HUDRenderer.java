package com.astral.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Disposable;

/**
 * Professional HUD Renderer - Starfield-inspired game overlay
 * Handles all in-game HUD elements with consistent styling
 */
public class HUDRenderer implements Disposable {
    
    private UIRenderer uiRenderer;
    
    // Screen dimensions (updated on resize)
    private int screenWidth;
    private int screenHeight;
    
    // HUD state data
    private float health = 100f;
    private float maxHealth = 100f;
    private float shield = 100f;
    private float maxShield = 100f;
    private float fuel = 100f;
    private float maxFuel = 100f;
    private float oxygen = 100f;
    private float maxOxygen = 100f;
    
    private int ammo = 30;
    private int maxAmmo = 30;
    private int totalAmmo = 120;
    private String weaponName = "LASER RIFLE";
    
    private float playerYaw = 0f;
    private String locationName = "UNKNOWN SECTOR";
    private String objectiveName = "";
    private float objectiveDistance = 0f;
    
    private int credits = 0;
    private int kills = 0;
    
    // Notifications
    private String notification = "";
    private float notificationTimer = 0f;
    private Color notificationColor = UITheme.ACCENT_CYAN;
    
    // Combat state
    private boolean inCombat = false;
    private float hitTimer = 0f;
    private float damageFlash = 0f;
    
    public HUDRenderer() {
        uiRenderer = new UIRenderer();
        updateScreenSize();
    }
    
    public void updateScreenSize() {
        screenWidth = Gdx.graphics.getWidth();
        screenHeight = Gdx.graphics.getHeight();
    }
    
    public void update(float delta) {
        uiRenderer.update(delta);
        
        // Update timers
        if (notificationTimer > 0) {
            notificationTimer -= delta;
        }
        if (hitTimer > 0) {
            hitTimer -= delta;
        }
        if (damageFlash > 0) {
            damageFlash -= delta * 3f;
        }
    }
    
    /**
     * Render the complete HUD overlay
     */
    public void render() {
        // Damage flash effect
        if (damageFlash > 0) {
            renderDamageFlash();
        }
        
        // Bottom-left: Health & Shield
        renderVitalBars();
        
        // Bottom-right: Ammo & Weapon
        renderWeaponInfo();
        
        // Top: Compass
        renderCompass();
        
        // Top-right: Location & Objective
        renderLocationInfo();
        
        // Top-left: Credits & Stats
        renderPlayerStats();
        
        // Center: Crosshair
        renderCrosshair();
        
        // Notifications
        renderNotifications();
    }
    
    private void renderDamageFlash() {
        ShapeRenderer sr = uiRenderer.getShapeRenderer();
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(UITheme.withAlpha(UITheme.DANGER_COLOR, damageFlash * 0.4f));
        sr.rect(0, 0, screenWidth, screenHeight);
        sr.end();
    }
    
    private void renderVitalBars() {
        float margin = UITheme.HUD_MARGIN;
        float barWidth = 180f;
        float barHeight = 10f;
        float spacing = 6f;
        
        float x = margin;
        float y = margin;
        
        // Health bar
        uiRenderer.drawStatusBar(x, y + barHeight + spacing, barWidth, barHeight,
            health, maxHealth, UITheme.HEALTH_COLOR, "HEALTH");
        
        // Shield bar
        uiRenderer.drawStatusBar(x, y, barWidth, barHeight,
            shield, maxShield, UITheme.SHIELD_COLOR, "SHIELD");
        
        // Fuel indicator (circular, next to bars)
        if (fuel < maxFuel) {
            float indicatorX = x + barWidth + 30;
            float indicatorY = y + barHeight;
            uiRenderer.drawCircularIndicator(indicatorX, indicatorY, 18f,
                fuel, maxFuel, UITheme.FUEL_COLOR);
            
            // Fuel label
            SpriteBatch batch = uiRenderer.getSpriteBatch();
            BitmapFont font = uiRenderer.getFont();
            batch.begin();
            font.getData().setScale(UITheme.FONT_SCALE_TINY);
            font.setColor(UITheme.TEXT_MUTED);
            font.draw(batch, "FUEL", indicatorX - 12, indicatorY - 25);
            batch.end();
        }
        
        // Oxygen indicator (only show if not full)
        if (oxygen < maxOxygen) {
            float indicatorX = x + barWidth + 80;
            float indicatorY = y + barHeight;
            uiRenderer.drawCircularIndicator(indicatorX, indicatorY, 18f,
                oxygen, maxOxygen, UITheme.OXYGEN_COLOR);
            
            SpriteBatch batch = uiRenderer.getSpriteBatch();
            BitmapFont font = uiRenderer.getFont();
            batch.begin();
            font.getData().setScale(UITheme.FONT_SCALE_TINY);
            font.setColor(UITheme.TEXT_MUTED);
            font.draw(batch, "O2", indicatorX - 6, indicatorY - 25);
            batch.end();
        }
    }
    
    private void renderWeaponInfo() {
        float margin = UITheme.HUD_MARGIN;
        float panelWidth = 160f;
        float panelHeight = 60f;
        float x = screenWidth - margin - panelWidth;
        float y = margin;
        
        // Weapon panel background
        uiRenderer.drawPanel(x, y, panelWidth, panelHeight, inCombat);
        
        SpriteBatch batch = uiRenderer.getSpriteBatch();
        BitmapFont font = uiRenderer.getFont();
        GlyphLayout layout = uiRenderer.getLayout();
        
        batch.begin();
        
        // Weapon name
        font.getData().setScale(UITheme.FONT_SCALE_TINY);
        font.setColor(UITheme.TEXT_MUTED);
        font.draw(batch, weaponName, x + 10, y + panelHeight - 8);
        
        // Ammo count (large)
        font.getData().setScale(UITheme.FONT_SCALE_LARGE);
        String ammoText = ammo + "/" + maxAmmo;
        
        // Color based on ammo level
        if (ammo == 0) {
            font.setColor(UITheme.DANGER_COLOR);
        } else if (ammo <= maxAmmo * 0.25f) {
            font.setColor(UITheme.WARNING_COLOR);
        } else {
            font.setColor(UITheme.TEXT_PRIMARY);
        }
        
        layout.setText(font, ammoText);
        font.draw(batch, ammoText, x + panelWidth - layout.width - 10, y + 35);
        
        // Total ammo (small)
        font.getData().setScale(UITheme.FONT_SCALE_TINY);
        font.setColor(UITheme.TEXT_MUTED);
        font.draw(batch, "TOTAL: " + totalAmmo, x + 10, y + 18);
        
        batch.end();
    }
    
    private void renderCompass() {
        float compassWidth = 400f;
        float compassY = screenHeight - 35f;
        uiRenderer.drawCompass(screenWidth / 2f, compassY, compassWidth, playerYaw);
    }
    
    private void renderLocationInfo() {
        float margin = UITheme.HUD_MARGIN;
        float x = screenWidth - margin;
        float y = screenHeight - margin;
        
        SpriteBatch batch = uiRenderer.getSpriteBatch();
        BitmapFont font = uiRenderer.getFont();
        GlyphLayout layout = uiRenderer.getLayout();
        
        batch.begin();
        
        // Location name (right-aligned)
        font.getData().setScale(UITheme.FONT_SCALE_SMALL);
        font.setColor(UITheme.TEXT_SECONDARY);
        layout.setText(font, locationName);
        font.draw(batch, locationName, x - layout.width, y - 10);
        
        // Objective (if any)
        if (objectiveName != null && !objectiveName.isEmpty()) {
            font.getData().setScale(UITheme.FONT_SCALE_TINY);
            font.setColor(UITheme.ACCENT_GOLD);
            
            String objText = objectiveName;
            if (objectiveDistance > 0) {
                objText += " - " + formatDistance(objectiveDistance);
            }
            
            layout.setText(font, objText);
            font.draw(batch, objText, x - layout.width, y - 30);
        }
        
        batch.end();
    }
    
    private void renderPlayerStats() {
        float margin = UITheme.HUD_MARGIN;
        float x = margin;
        float y = screenHeight - margin;
        
        SpriteBatch batch = uiRenderer.getSpriteBatch();
        BitmapFont font = uiRenderer.getFont();
        
        batch.begin();
        
        // Credits
        font.getData().setScale(UITheme.FONT_SCALE_SMALL);
        font.setColor(UITheme.ACCENT_GOLD);
        font.draw(batch, formatCredits(credits) + " CR", x, y - 10);
        
        // Kill count (if any)
        if (kills > 0) {
            font.getData().setScale(UITheme.FONT_SCALE_TINY);
            font.setColor(UITheme.TEXT_MUTED);
            font.draw(batch, "KILLS: " + kills, x, y - 30);
        }
        
        batch.end();
    }
    
    private void renderCrosshair() {
        float cx = screenWidth / 2f;
        float cy = screenHeight / 2f;
        uiRenderer.drawCrosshair(cx, cy, hitTimer > 0, hitTimer);
    }
    
    private void renderNotifications() {
        if (notificationTimer > 0 && notification != null && !notification.isEmpty()) {
            float alpha = MathUtils.clamp(notificationTimer, 0f, 1f);
            float x = screenWidth / 2f - 150;
            float y = screenHeight - 100;
            uiRenderer.drawNotification(x, y, notification, notificationColor, alpha);
        }
    }
    
    // ========== UTILITY METHODS ==========
    
    private String formatCredits(int amount) {
        if (amount >= 1000000) {
            return String.format("%.1fM", amount / 1000000f);
        } else if (amount >= 1000) {
            return String.format("%.1fK", amount / 1000f);
        }
        return String.valueOf(amount);
    }
    
    private String formatDistance(float meters) {
        if (meters >= 1000) {
            return String.format("%.1f km", meters / 1000f);
        }
        return String.format("%.0f m", meters);
    }
    
    // ========== SETTERS FOR HUD DATA ==========
    
    public void setHealth(float health, float max) {
        this.health = health;
        this.maxHealth = max;
    }
    
    public void setShield(float shield, float max) {
        this.shield = shield;
        this.maxShield = max;
    }
    
    public void setFuel(float fuel, float max) {
        this.fuel = fuel;
        this.maxFuel = max;
    }
    
    public void setOxygen(float oxygen, float max) {
        this.oxygen = oxygen;
        this.maxOxygen = max;
    }
    
    public void setAmmo(int ammo, int maxAmmo, int total) {
        this.ammo = ammo;
        this.maxAmmo = maxAmmo;
        this.totalAmmo = total;
    }
    
    public void setWeaponName(String name) {
        this.weaponName = name != null ? name.toUpperCase() : "UNKNOWN";
    }
    
    public void setPlayerYaw(float yaw) {
        this.playerYaw = yaw;
    }
    
    public void setLocation(String name) {
        this.locationName = name != null ? name.toUpperCase() : "UNKNOWN";
    }
    
    public void setObjective(String name, float distance) {
        this.objectiveName = name;
        this.objectiveDistance = distance;
    }
    
    public void setCredits(int credits) {
        this.credits = credits;
    }
    
    public void setKills(int kills) {
        this.kills = kills;
    }
    
    public void setInCombat(boolean inCombat) {
        this.inCombat = inCombat;
    }
    
    public void setSpeed(float speed, float maxSpeed) {
        // Could add speed display later
    }
    
    public void setHeading(float heading) {
        this.playerYaw = heading;
    }
    
    public void setCoordinates(int x, int y, int z) {
        // Could add coordinate display later
    }
    
    public void setLocation(String system, String region) {
        this.locationName = (system + " - " + region).toUpperCase();
    }
    
    public void triggerHitMarker() {
        this.hitTimer = 0.3f;
    }
    
    public void triggerDamageFlash(float intensity) {
        this.damageFlash = MathUtils.clamp(intensity, 0f, 1f);
    }
    
    public void showNotification(String message, Color color) {
        this.notification = message;
        this.notificationColor = color;
        this.notificationTimer = 3f;
    }
    
    public void showNotification(String message) {
        showNotification(message, UITheme.ACCENT_CYAN);
    }
    
    public UIRenderer getUIRenderer() {
        return uiRenderer;
    }
    
    @Override
    public void dispose() {
        uiRenderer.dispose();
    }
}
