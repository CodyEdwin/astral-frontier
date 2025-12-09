package com.astral.systems;

import com.astral.components.PlayerComponent;
import com.astral.components.RigidBodyComponent;
import com.astral.components.ShipComponent;
import com.astral.components.TransformComponent;
import com.astral.ecs.Entity;
import com.astral.ecs.GameSystem;
import com.astral.ecs.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * UI System - handles HUD and game interface rendering
 */
public class UISystem extends GameSystem implements Disposable {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont largeFont;

    private boolean showHUD = true;

    // Planet tracking for HUD display
    private Vector3 planetPosition = new Vector3();
    private String planetName = "";
    private String transitionState = "SPACE";
    private float transitionProgress = 0f;

    public UISystem(World world) {
        super(world);
        setPriority(1); // Render UI last
    }

    @Override
    public void initialize() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(2f); // Scale up for readability
        font.setColor(Color.WHITE);
        largeFont = new BitmapFont();
        largeFont.getData().setScale(3f);
    }

    @Override
    public void update(float deltaTime) {
        // UI updates handled in render
    }

    public void render(float deltaTime) {
        if (!showHUD) return;

        // Find player entity
        Entity playerEntity = findPlayerEntity();
        if (playerEntity == null) return;

        PlayerComponent player = playerEntity.get(PlayerComponent.class);
        ShipComponent ship = playerEntity.get(ShipComponent.class);

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Render HUD based on player state
        if (player.state == PlayerComponent.PlayerState.SHIP && ship != null) {
            renderShipHUD(ship, player, width, height);
        } else if (player.state == PlayerComponent.PlayerState.FPS) {
            renderFPSHUD(player, width, height);
        }

        // Render crosshair
        renderCrosshair(width, height);

        // Render controls help (bottom left)
        renderControlsHelp(player.state, width, height);
    }

    private void renderControlsHelp(PlayerComponent.PlayerState state, int width, int height) {
        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(0.7f, 0.7f, 0.7f, 0.8f);

        int startY = 150;
        int margin = 20;

        if (state == PlayerComponent.PlayerState.SHIP) {
            font.draw(batch, "WASD - Move  |  Mouse - Look", margin, startY);
            font.draw(batch, "Q/E - Vertical  |  Shift - Boost", margin, startY - 25);
            font.draw(batch, "Space+A/D - Roll  |  Ctrl - Brake", margin, startY - 50);
            font.draw(batch, "F1 - Debug  |  ESC - Pause", margin, startY - 75);
        } else {
            font.draw(batch, "WASD - Walk  |  Shift - Sprint", margin, startY);
            font.draw(batch, "Space - Jump  |  F - Enter Ship", margin, startY - 25);
            font.draw(batch, "ESC - Pause", margin, startY - 50);
        }

        font.getData().setScale(2f);
        batch.end();
    }

    private void renderShipHUD(ShipComponent ship, PlayerComponent player, int width, int height) {
        // Find player entity for additional data
        Entity playerEntity = findPlayerEntity();
        RigidBodyComponent rb = playerEntity != null ? playerEntity.get(RigidBodyComponent.class) : null;
        TransformComponent transform = playerEntity != null ? playerEntity.get(TransformComponent.class) : null;

        // Draw status bars
        int barWidth = 200;
        int barHeight = 15;
        int margin = 20;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Shield bar (blue)
        float shieldPercent = ship.getShieldPercent();
        shapeRenderer.setColor(0.1f, 0.1f, 0.3f, 0.7f);
        shapeRenderer.rect(margin, height - margin - barHeight, barWidth, barHeight);
        shapeRenderer.setColor(0.3f, 0.6f, 1f, 0.9f);
        shapeRenderer.rect(margin, height - margin - barHeight, barWidth * shieldPercent, barHeight);

        // Hull bar (red/orange)
        float hullPercent = ship.getHullPercent();
        shapeRenderer.setColor(0.3f, 0.1f, 0.1f, 0.7f);
        shapeRenderer.rect(margin, height - margin - barHeight * 2 - 5, barWidth, barHeight);
        Color hullColor = hullPercent > 0.5f ? new Color(0.2f, 0.8f, 0.2f, 0.9f) :
                hullPercent > 0.25f ? new Color(1f, 0.7f, 0.2f, 0.9f) :
                        new Color(1f, 0.2f, 0.2f, 0.9f);
        shapeRenderer.setColor(hullColor);
        shapeRenderer.rect(margin, height - margin - barHeight * 2 - 5, barWidth * hullPercent, barHeight);

        // Fuel bar (yellow)
        float fuelPercent = ship.getFuelPercent();
        shapeRenderer.setColor(0.2f, 0.2f, 0.1f, 0.7f);
        shapeRenderer.rect(margin, height - margin - barHeight * 3 - 10, barWidth, barHeight);
        shapeRenderer.setColor(1f, 0.9f, 0.2f, 0.9f);
        shapeRenderer.rect(margin, height - margin - barHeight * 3 - 10, barWidth * fuelPercent, barHeight);

        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);

        // Draw labels and info
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, String.format("SHD: %.0f%%", shieldPercent * 100), margin + barWidth + 10, height - margin - 2);
        font.draw(batch, String.format("HUL: %.0f%%", hullPercent * 100), margin + barWidth + 10, height - margin - barHeight - 7);
        font.draw(batch, String.format("FUEL: %.0f%%", fuelPercent * 100), margin + barWidth + 10, height - margin - barHeight * 2 - 12);

        // Velocity indicator (bottom right)
        float velocity = 0f;
        if (rb != null) {
            velocity = rb.getLinearVelocity().len();
        }
        font.draw(batch, String.format("VEL: %.1f m/s", velocity), width - 200, margin + 60);

        // Planet distance (bottom right)
        if (transform != null && planetName != null && !planetName.isEmpty()) {
            float distance = transform.position.dst(planetPosition);
            String distStr;
            if (distance > 1000) {
                distStr = String.format("%.1f km", distance / 1000f);
            } else {
                distStr = String.format("%.0f m", distance);
            }
            font.draw(batch, planetName + ": " + distStr, width - 200, margin + 100);
        }

        // Transition state (top center)
        font.setColor(Color.CYAN);
        String stateText = "[ " + transitionState + " ]";
        if (transitionProgress > 0 && transitionProgress < 1) {
            stateText += String.format(" %.0f%%", transitionProgress * 100);
        }
        font.draw(batch, stateText, width / 2f - 60, height - margin);

        // Landing status
        if (ship.isLanded) {
            font.setColor(Color.GREEN);
            font.draw(batch, "LANDED - Press F to exit ship", width / 2f - 150, height / 2f - 100);
        }

        // Credits
        font.setColor(Color.WHITE);
        font.draw(batch, String.format("Credits: %,d", player.credits), width - 200, margin + 20);

        batch.end();
    }

    private void renderFPSHUD(PlayerComponent player, int width, int height) {
        int barWidth = 150;
        int barHeight = 12;
        int margin = 20;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Health bar
        float healthPercent = player.getHealthPercent();
        shapeRenderer.setColor(0.2f, 0.1f, 0.1f, 0.7f);
        shapeRenderer.rect(margin, margin, barWidth, barHeight);
        shapeRenderer.setColor(0.8f, 0.2f, 0.2f, 0.9f);
        shapeRenderer.rect(margin, margin, barWidth * healthPercent, barHeight);

        // Oxygen bar
        float oxygenPercent = player.getOxygenPercent();
        shapeRenderer.setColor(0.1f, 0.1f, 0.2f, 0.7f);
        shapeRenderer.rect(margin, margin + barHeight + 5, barWidth, barHeight);
        shapeRenderer.setColor(0.3f, 0.7f, 1f, 0.9f);
        shapeRenderer.rect(margin, margin + barHeight + 5, barWidth * oxygenPercent, barHeight);

        // Stamina bar
        float staminaPercent = player.getStaminaPercent();
        shapeRenderer.setColor(0.2f, 0.2f, 0.1f, 0.7f);
        shapeRenderer.rect(margin, margin + barHeight * 2 + 10, barWidth, barHeight);
        shapeRenderer.setColor(1f, 0.9f, 0.3f, 0.9f);
        shapeRenderer.rect(margin, margin + barHeight * 2 + 10, barWidth * staminaPercent, barHeight);

        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    private void renderCrosshair(int width, int height) {
        int centerX = width / 2;
        int centerY = height / 2;
        int size = 10;

        Gdx.gl.glEnable(Gdx.gl.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 1f, 1f, 0.7f);

        // Crosshair lines
        shapeRenderer.rect(centerX - size, centerY - 1, size * 2, 2);
        shapeRenderer.rect(centerX - 1, centerY - size, 2, size * 2);

        shapeRenderer.end();
        Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
    }

    private Entity findPlayerEntity() {
        Array<Entity> players = getEntitiesWith(PlayerComponent.class);
        for (Entity entity : players) {
            PlayerComponent player = entity.get(PlayerComponent.class);
            if (player.isLocalPlayer) {
                return entity;
            }
        }
        return null;
    }

    public void setShowHUD(boolean show) {
        this.showHUD = show;
    }

    public void toggleHUD() {
        this.showHUD = !this.showHUD;
    }

    public void setPlanetInfo(String name, Vector3 position) {
        this.planetName = name;
        this.planetPosition.set(position);
    }

    public void setTransitionState(String state, float progress) {
        this.transitionState = state;
        this.transitionProgress = progress;
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (largeFont != null) largeFont.dispose();
    }
}
