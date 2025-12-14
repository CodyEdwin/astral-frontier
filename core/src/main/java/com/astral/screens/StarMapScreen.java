package com.astral.screens;

import com.astral.AstralFrontier;
import com.astral.game.UniverseManager;
import com.astral.game.UniverseManager.*;
import com.astral.ui.UIRenderer;
import com.astral.ui.UITheme;
import com.astral.ui.UIButton;
import com.astral.ui.UIPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Star Map Screen - Navigate the galaxy and jump between star systems
 */
public class StarMapScreen implements Screen {
    
    private final AstralFrontier game;
    private final Screen previousScreen;
    
    private UIRenderer uiRenderer;
    private OrthographicCamera mapCamera;
    
    private UniverseManager universe;
    private Array<StarSystemData> nearbySystems;
    private StarSystemData selectedSystem;
    private StarSystemData hoveredSystem;
    
    // Camera controls
    private float zoom = 1f;
    private Vector2 mapOffset = new Vector2();
    private boolean dragging = false;
    private float lastMouseX, lastMouseY;
    
    // UI panels
    private UIPanel infoPanel;
    private UIButton jumpButton;
    private UIButton closeButton;
    
    // Animation
    private float time = 0f;
    private float[] starFieldX = new float[150];
    private float[] starFieldY = new float[150];
    private float[] starFieldBright = new float[150];
    
    public StarMapScreen(AstralFrontier game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
    }
    
    @Override
    public void show() {
        uiRenderer = new UIRenderer();
        mapCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        mapCamera.position.set(0, 0, 0);
        
        universe = UniverseManager.getInstance();
        refreshNearbySystems();
        
        // Initialize starfield background
        for (int i = 0; i < starFieldX.length; i++) {
            starFieldX[i] = MathUtils.random(Gdx.graphics.getWidth());
            starFieldY[i] = MathUtils.random(Gdx.graphics.getHeight());
            starFieldBright[i] = MathUtils.random(0.2f, 0.6f);
        }
        
        createUI();
        Gdx.input.setCursorCatched(false);
    }
    
    private void createUI() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        
        // Info panel on the right
        infoPanel = new UIPanel(w - 320, 100, 300, h - 200);
        infoPanel.show();
        
        // Jump button
        jumpButton = new UIButton(w - 300, 120, 260, 50, "JUMP");
        jumpButton.setVisible(true);
        
        // Close button
        closeButton = new UIButton(w - 120, h - 60, 100, 40, "CLOSE");
        closeButton.setVisible(true);
    }
    
    private void refreshNearbySystems() {
        nearbySystems = universe.getSystemsInRange(universe.getGalacticPosition(), 150f);
    }
    
    @Override
    public void render(float delta) {
        time += delta;
        uiRenderer.update(delta);
        
        handleInput(delta);
        
        // Clear
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        renderBackground();
        renderStarMap();
        renderUI();
    }
    
    private void renderBackground() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        
        ShapeRenderer sr = uiRenderer.getShapeRenderer();
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        // Animated starfield
        for (int i = 0; i < starFieldX.length; i++) {
            float twinkle = 0.7f + 0.3f * MathUtils.sin(time * 2f + i);
            float b = starFieldBright[i] * twinkle;
            sr.setColor(b, b, b + 0.05f, 1f);
            sr.circle(starFieldX[i], starFieldY[i], 1f);
        }
        
        sr.end();
    }
    
    private void renderStarMap() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        float centerX = w / 2f + mapOffset.x;
        float centerY = h / 2f + mapOffset.y;
        float scale = 3f * zoom; // Pixels per light year
        
        ShapeRenderer sr = uiRenderer.getShapeRenderer();
        Vector3 playerPos = universe.getGalacticPosition();
        
        // Draw jump range circle
        Gdx.gl.glEnable(GL20.GL_BLEND);
        sr.begin(ShapeRenderer.ShapeType.Line);
        sr.setColor(UITheme.ACCENT_CYAN.r, UITheme.ACCENT_CYAN.g, UITheme.ACCENT_CYAN.b, 0.3f);
        sr.circle(centerX, centerY, UniverseManager.JUMP_RANGE * scale);
        sr.end();
        
        // Draw connections (jump routes)
        sr.begin(ShapeRenderer.ShapeType.Line);
        for (StarSystemData system : nearbySystems) {
            float dist = playerPos.dst(system.position);
            if (dist <= UniverseManager.JUMP_RANGE) {
                float sx = centerX + (system.position.x - playerPos.x) * scale;
                float sy = centerY + (system.position.z - playerPos.z) * scale;
                
                sr.setColor(0.2f, 0.3f, 0.4f, 0.5f);
                sr.line(centerX, centerY, sx, sy);
            }
        }
        sr.end();
        
        // Draw star systems
        sr.begin(ShapeRenderer.ShapeType.Filled);
        
        // Current system (center)
        sr.setColor(UITheme.ACCENT_GOLD);
        sr.circle(centerX, centerY, 8f);
        
        hoveredSystem = null;
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        
        for (StarSystemData system : nearbySystems) {
            float sx = centerX + (system.position.x - playerPos.x) * scale;
            float sy = centerY + (system.position.z - playerPos.z) * scale;
            
            // Check hover
            float dist = Vector2.dst(mouseX, mouseY, sx, sy);
            boolean hovered = dist < 12f;
            if (hovered) {
                hoveredSystem = system;
            }
            
            // Size based on star type
            float size = switch (system.starType) {
                case BLUE_GIANT -> 7f;
                case YELLOW -> 5f;
                case RED_DWARF -> 4f;
                case WHITE_DWARF -> 3f;
            };
            
            // Color
            if (system == selectedSystem) {
                sr.setColor(UITheme.ACCENT_CYAN);
                size += 2f;
            } else if (hovered) {
                sr.setColor(1f, 1f, 1f, 1f);
                size += 1f;
            } else if (system.visited) {
                sr.setColor(system.starColor[0], system.starColor[1], system.starColor[2], 1f);
            } else if (system.discovered) {
                sr.setColor(system.starColor[0] * 0.7f, system.starColor[1] * 0.7f, system.starColor[2] * 0.7f, 0.8f);
            } else {
                sr.setColor(0.4f, 0.4f, 0.5f, 0.5f);
            }
            
            sr.circle(sx, sy, size);
            
            // Pulsing glow for selected
            if (system == selectedSystem) {
                float pulse = 0.3f + 0.2f * MathUtils.sin(time * 4f);
                sr.setColor(UITheme.ACCENT_CYAN.r, UITheme.ACCENT_CYAN.g, UITheme.ACCENT_CYAN.b, pulse);
                sr.circle(sx, sy, size + 4f);
            }
        }
        
        sr.end();
        
        // Draw system names
        uiRenderer.getSpriteBatch().begin();
        uiRenderer.getFont().getData().setScale(0.9f);
        
        // Current system name
        uiRenderer.getFont().setColor(UITheme.ACCENT_GOLD);
        String currentName = universe.getCurrentSystem().name + " (You are here)";
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), currentName, centerX - 60, centerY - 15);
        
        // Nearby system names (only show some)
        for (StarSystemData system : nearbySystems) {
            float sx = centerX + (system.position.x - playerPos.x) * scale;
            float sy = centerY + (system.position.z - playerPos.z) * scale;
            
            boolean showName = system == selectedSystem || system == hoveredSystem || 
                              (system.visited && system.distanceFromPlayer < 30f);
            
            if (showName) {
                uiRenderer.getFont().setColor(system == selectedSystem ? UITheme.ACCENT_CYAN : UITheme.TEXT_SECONDARY);
                uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), system.name, sx + 10, sy + 5);
            }
        }
        
        uiRenderer.getSpriteBatch().end();
    }
    
    private void renderUI() {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        float mouseX = Gdx.input.getX();
        float mouseY = h - Gdx.input.getY();
        boolean mouseDown = Gdx.input.isTouched();
        
        // Update buttons
        jumpButton.update(delta(), mouseX, mouseY, mouseDown);
        closeButton.update(delta(), mouseX, mouseY, mouseDown);
        
        // Header
        uiRenderer.getSpriteBatch().begin();
        uiRenderer.getFont().getData().setScale(2.5f);
        uiRenderer.getFont().setColor(UITheme.ACCENT_CYAN);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "STAR MAP", 30, h - 30);
        
        // Current location
        uiRenderer.getFont().getData().setScale(1.2f);
        uiRenderer.getFont().setColor(UITheme.TEXT_PRIMARY);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Current: " + universe.getCurrentSystem().name, 30, h - 70);
        
        // Fuel display
        uiRenderer.getFont().setColor(UITheme.FUEL_COLOR);
        String fuelText = String.format("Fuel: %.0f / %.0f", universe.getFuel(), universe.getMaxFuel());
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), fuelText, 30, h - 100);
        
        // Credits
        uiRenderer.getFont().setColor(UITheme.ACCENT_GOLD);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Credits: " + universe.getCredits(), 30, h - 130);
        
        uiRenderer.getSpriteBatch().end();
        
        // Info panel
        infoPanel.update(delta());
        infoPanel.render(uiRenderer);
        
        // Panel content
        uiRenderer.getSpriteBatch().begin();
        uiRenderer.getFont().getData().setScale(1.3f);
        
        float panelX = w - 300;
        float panelY = h - 130;
        
        if (selectedSystem != null) {
            renderSystemInfo(selectedSystem, panelX, panelY);
        } else if (hoveredSystem != null) {
            renderSystemInfo(hoveredSystem, panelX, panelY);
        } else {
            uiRenderer.getFont().setColor(UITheme.TEXT_MUTED);
            uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Select a star system", panelX, panelY);
            uiRenderer.getFont().getData().setScale(1f);
            uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Click to select", panelX, panelY - 30);
            uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Scroll to zoom", panelX, panelY - 55);
            uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Drag to pan", panelX, panelY - 80);
        }
        
        uiRenderer.getSpriteBatch().end();
        
        // Jump button (only if system selected and in range)
        if (selectedSystem != null) {
            float fuelCost = universe.getFuelCost(selectedSystem);
            boolean canJump = fuelCost <= universe.getFuel() && selectedSystem.distanceFromPlayer <= UniverseManager.JUMP_RANGE;
            
            jumpButton.setEnabled(canJump);
            jumpButton.render(uiRenderer);
        }
        
        // Close button
        closeButton.render(uiRenderer);
        
        // Controls hint
        uiRenderer.getSpriteBatch().begin();
        uiRenderer.getFont().getData().setScale(0.9f);
        uiRenderer.getFont().setColor(UITheme.TEXT_MUTED);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "[ESC] Close  [SCROLL] Zoom  [DRAG] Pan", 30, 30);
        uiRenderer.getSpriteBatch().end();
    }
    
    private void renderSystemInfo(StarSystemData system, float x, float y) {
        uiRenderer.getFont().setColor(UITheme.ACCENT_CYAN);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), system.name, x, y);
        
        uiRenderer.getFont().getData().setScale(1f);
        y -= 35;
        
        // Star type
        uiRenderer.getFont().setColor(system.starColor[0], system.starColor[1], system.starColor[2], 1f);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Star: " + formatStarType(system.starType), x, y);
        y -= 25;
        
        // Distance
        uiRenderer.getFont().setColor(UITheme.TEXT_PRIMARY);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), String.format("Distance: %.1f LY", system.distanceFromPlayer), x, y);
        y -= 25;
        
        // Fuel cost
        float fuelCost = universe.getFuelCost(system);
        boolean enoughFuel = fuelCost <= universe.getFuel();
        uiRenderer.getFont().setColor(enoughFuel ? UITheme.FUEL_COLOR : UITheme.DANGER_COLOR);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), String.format("Fuel Cost: %.1f", fuelCost), x, y);
        y -= 25;
        
        // Planets
        uiRenderer.getFont().setColor(UITheme.TEXT_SECONDARY);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Planets: " + system.planets.size, x, y);
        y -= 25;
        
        // Economy
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Economy: " + formatEconomy(system.economyType), x, y);
        y -= 25;
        
        // Tech level
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Tech Level: " + system.techLevel, x, y);
        y -= 25;
        
        // Station
        if (system.hasStation) {
            uiRenderer.getFont().setColor(UITheme.SUCCESS_COLOR);
            uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Station Available", x, y);
            y -= 25;
        }
        
        // Danger level
        if (system.dangerLevel > 0) {
            uiRenderer.getFont().setColor(system.dangerLevel > 2 ? UITheme.DANGER_COLOR : UITheme.WARNING_COLOR);
            uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "Danger: " + "!".repeat(system.dangerLevel), x, y);
            y -= 25;
        }
        
        // Status
        y -= 10;
        if (system.visited) {
            uiRenderer.getFont().setColor(UITheme.SUCCESS_COLOR);
            uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "VISITED", x, y);
        } else if (system.discovered) {
            uiRenderer.getFont().setColor(UITheme.ACCENT_CYAN);
            uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "DISCOVERED", x, y);
        } else {
            uiRenderer.getFont().setColor(UITheme.TEXT_MUTED);
            uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), "UNEXPLORED", x, y);
        }
    }
    
    private String formatStarType(StarType type) {
        return switch (type) {
            case RED_DWARF -> "Red Dwarf";
            case YELLOW -> "Yellow Star";
            case BLUE_GIANT -> "Blue Giant";
            case WHITE_DWARF -> "White Dwarf";
        };
    }
    
    private String formatEconomy(EconomyType type) {
        return switch (type) {
            case AGRICULTURAL -> "Agricultural";
            case INDUSTRIAL -> "Industrial";
            case MINING -> "Mining";
            case TECH -> "High Tech";
            case MILITARY -> "Military";
            case TRADING -> "Trading Hub";
        };
    }
    
    private void handleInput(float delta) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        
        // Close
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            game.setScreen(previousScreen);
            return;
        }
        
        // Close button
        if (closeButton.contains(mouseX, mouseY) && Gdx.input.justTouched()) {
            game.setScreen(previousScreen);
            return;
        }
        
        // Jump button
        if (selectedSystem != null && jumpButton.contains(mouseX, mouseY) && Gdx.input.justTouched()) {
            if (universe.jumpToSystem(selectedSystem)) {
                refreshNearbySystems();
                selectedSystem = null;
            }
        }
        
        // System selection
        if (Gdx.input.justTouched() && hoveredSystem != null) {
            selectedSystem = hoveredSystem;
        }
        
        // Zoom
        float scroll = Gdx.input.getDeltaX() == 0 ? 0 : 0; // Would need scroll input
        // For now use +/- keys
        if (Gdx.input.isKeyPressed(Input.Keys.EQUALS) || Gdx.input.isKeyPressed(Input.Keys.PLUS)) {
            zoom *= 1.02f;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
            zoom *= 0.98f;
        }
        zoom = MathUtils.clamp(zoom, 0.2f, 5f);
        
        // Pan with right mouse or middle mouse
        if (Gdx.input.isButtonPressed(Input.Buttons.RIGHT) || Gdx.input.isButtonPressed(Input.Buttons.MIDDLE)) {
            if (!dragging) {
                dragging = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
            } else {
                mapOffset.x += mouseX - lastMouseX;
                mapOffset.y += mouseY - lastMouseY;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
            }
        } else {
            dragging = false;
        }
        
        // Arrow keys to pan
        float panSpeed = 200f * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) mapOffset.x += panSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) mapOffset.x -= panSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) mapOffset.y -= panSpeed;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) mapOffset.y += panSpeed;
    }
    
    private float delta() {
        return Gdx.graphics.getDeltaTime();
    }
    
    @Override
    public void resize(int width, int height) {
        mapCamera.viewportWidth = width;
        mapCamera.viewportHeight = height;
        mapCamera.update();
        createUI();
    }
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {}
    
    @Override
    public void dispose() {
        if (uiRenderer != null) uiRenderer.dispose();
    }
}
