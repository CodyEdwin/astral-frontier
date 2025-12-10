package com.astral.exploration;

import com.astral.combat.WeaponType;
import com.astral.procedural.PlanetSurface;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

/**
 * Handles all HUD rendering for planet exploration
 */
public class ExplorationUI implements Disposable {

    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private boolean showDebug = true;

    public ExplorationUI() {
    }

    public void initialize() {
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
    }

    public void toggleDebug() {
        showDebug = !showDebug;
    }

    public void renderSkyGradient(Color skyColor) {
        Color horizonColor = new Color(skyColor).lerp(Color.WHITE, 0.3f);
        Color zenithColor = new Color(skyColor).lerp(new Color(0.1f, 0.1f, 0.2f, 1f), 0.5f);

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(0, 0, width, height,
            horizonColor, horizonColor, zenithColor, zenithColor);
        shapeRenderer.end();
    }

    public void render(PlayerController player, WeaponSystem weapons, CombatManager combat, PlanetSurface surface) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Damage flash effect
        float damageFlash = combat.getDamageFlashTime();
        if (damageFlash > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0f, 0f, damageFlash * 0.4f);
            shapeRenderer.rect(0, 0, width, height);
            shapeRenderer.end();
        }

        // Render 2D weapon
        weapons.renderWeapon2D();

        // Draw crosshair
        renderCrosshair(combat);

        // Draw HUD elements
        spriteBatch.begin();
        renderAmmoDisplay(weapons, width);
        renderPlanetName(surface, height);
        renderJetpackStatus(player, width, height);
        spriteBatch.end();

        renderFuelBar(player, width, height);
        renderHealthBar(combat, width);

        spriteBatch.begin();
        renderKillCount(combat);
        renderControlsHint();

        if (showDebug) {
            renderDebugInfo(player, surface, height);
        }
        spriteBatch.end();
    }

    private void renderCrosshair(CombatManager combat) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        int cx = width / 2;
        int cy = height / 2;
        float crosshairSize = 12f;
        float crosshairThickness = 2f;
        float gap = 4f;
        float hitmarkerTimer = combat.getHitmarkerTimer();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Crosshair color
        if (hitmarkerTimer > 0) {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        } else {
            shapeRenderer.setColor(1f, 1f, 1f, 0.9f);
        }

        // Draw crosshair lines
        shapeRenderer.rectLine(cx, cy + gap, cx, cy + crosshairSize, crosshairThickness);
        shapeRenderer.rectLine(cx, cy - gap, cx, cy - crosshairSize, crosshairThickness);
        shapeRenderer.rectLine(cx - gap, cy, cx - crosshairSize, cy, crosshairThickness);
        shapeRenderer.rectLine(cx + gap, cy, cx + crosshairSize, cy, crosshairThickness);

        // Outer ring
        shapeRenderer.setColor(1f, 1f, 1f, 0.4f);
        float ringRadius = crosshairSize + 4f;
        int segments = 32;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);
            float x1 = cx + (float) Math.cos(angle1) * ringRadius;
            float y1 = cy + (float) Math.sin(angle1) * ringRadius;
            float x2 = cx + (float) Math.cos(angle2) * ringRadius;
            float y2 = cy + (float) Math.sin(angle2) * ringRadius;
            shapeRenderer.rectLine(x1, y1, x2, y2, 1f);
        }

        // Hitmarker X
        if (hitmarkerTimer > 0) {
            float hitSize = 8f + (hitmarkerTimer / combat.getHitmarkerDuration()) * 4f;
            float hitAlpha = hitmarkerTimer / combat.getHitmarkerDuration();
            shapeRenderer.setColor(1f, 0.2f, 0.2f, hitAlpha);
            shapeRenderer.rectLine(cx - hitSize, cy - hitSize, cx + hitSize, cy + hitSize, 3f);
            shapeRenderer.rectLine(cx - hitSize, cy + hitSize, cx + hitSize, cy - hitSize, 3f);
        }

        shapeRenderer.end();

        // Center dot
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (hitmarkerTimer > 0) {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        } else {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
        }
        shapeRenderer.circle(cx, cy, 2f);
        shapeRenderer.end();
    }

    private void renderAmmoDisplay(WeaponSystem weapons, int width) {
        if (weapons.isWeaponEquipped()) {
            WeaponType weapon = weapons.getCurrentWeapon();
            int ammo = weapons.getCurrentAmmo();
            int reserve = weapons.getReserveAmmo();

            font.setColor(ammo > weapon.maxAmmo * 0.2f ? Color.WHITE : Color.RED);
            String ammoText = weapons.isReloading() ? "RELOADING..." : ammo + " / " + reserve;
            font.draw(spriteBatch, ammoText, width - 180, 80);

            font.setColor(new Color(weapon.glowR, weapon.glowG, weapon.glowB, 1f));
            font.draw(spriteBatch, weapon.name, width - 180, 55);

            // Weapon slot indicators
            WeaponType[] allWeapons = WeaponType.values();
            for (int i = 0; i < allWeapons.length; i++) {
                font.setColor(i == weapons.getCurrentWeaponIndex() ? Color.WHITE : new Color(0.4f, 0.4f, 0.4f, 1f));
                font.draw(spriteBatch, "[" + (i + 1) + "]", width - 180 + i * 35, 30);
            }
        } else {
            font.setColor(Color.GRAY);
            font.draw(spriteBatch, "[E] to equip", width - 150, 80);
        }
    }

    private void renderPlanetName(PlanetSurface surface, int height) {
        font.setColor(new Color(0.8f, 0.9f, 1f, 1f));
        font.draw(spriteBatch, surface.getType().displayName + ": " + surface.getName(), 20, height - 20);
    }

    private void renderJetpackStatus(PlayerController player, int width, int height) {
        if (player.isJetpackActive()) {
            font.setColor(new Color(0.3f, 1f, 0.5f, 1f));
            font.draw(spriteBatch, "JETPACK ACTIVE", width - 180, height - 20);
        }
    }

    private void renderFuelBar(PlayerController player, int width, int height) {
        int barWidth = 150;
        int barHeight = 12;
        int barX = width - 170;
        int barY = height - 55;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        float fuelPercent = player.getJetpackFuel() / player.getJetpackMaxFuel();
        if (fuelPercent > 0.3f) {
            shapeRenderer.setColor(0.3f, 0.8f, 1f, 1f);
        } else {
            shapeRenderer.setColor(1f, 0.3f, 0.3f, 1f);
        }
        shapeRenderer.rect(barX, barY, barWidth * fuelPercent, barHeight);
        shapeRenderer.end();

        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "FUEL", barX - 50, barY + 12);
        spriteBatch.end();
    }

    private void renderHealthBar(CombatManager combat, int width) {
        int barWidth = 200;
        int barHeight = 20;
        int barX = 20;
        int barY = 60;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.1f, 0.1f, 0.8f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        float healthPercent = combat.getPlayerHealth() / combat.getPlayerMaxHealth();
        if (healthPercent > 0.3f) {
            shapeRenderer.setColor(0.2f, 0.8f, 0.3f, 1f);
        } else {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        }
        shapeRenderer.rect(barX, barY, barWidth * healthPercent, barHeight);
        shapeRenderer.end();

        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "HEALTH", barX, barY + 38);
        spriteBatch.end();
    }

    private void renderKillCount(CombatManager combat) {
        int barX = 20;
        int barY = 60;
        int barWidth = 200;

        font.setColor(new Color(1f, 0.8f, 0.2f, 1f));
        font.draw(spriteBatch, "KILLS: " + combat.getKillCount(), barX + barWidth + 30, barY + 18);

        font.setColor(new Color(1f, 0.4f, 0.4f, 1f));
        font.draw(spriteBatch, "ENEMIES: " + combat.getEnemyCount(), barX + barWidth + 130, barY + 18);
    }

    private void renderControlsHint() {
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        font.draw(spriteBatch, "WASD: Move | SHIFT: Sprint | SPACE: Jump | 2xSPACE: Jetpack | LMB: Shoot | RMB: Aim | R: Reload | 1-5/Scroll: Weapons | ESC: Return", 20, 30);
    }

    private void renderDebugInfo(PlayerController player, PlanetSurface surface, int height) {
        font.setColor(Color.YELLOW);
        float terrainHeight = surface.getHeightAt(player.getPosition().x, player.getPosition().z);
        font.draw(spriteBatch, String.format("Pos: %.1f, %.1f, %.1f",
            player.getPosition().x, player.getPosition().y, player.getPosition().z), 20, height - 50);
        font.draw(spriteBatch, String.format("Ground: %.1f | Grounded: %s",
            terrainHeight, player.isGrounded()), 20, height - 75);
        font.draw(spriteBatch, String.format("Look: Yaw %.1f, Pitch %.1f",
            player.getCameraYaw(), player.getCameraPitch()), 20, height - 100);

        font.setColor(Color.CYAN);
        font.draw(spriteBatch, String.format("Chunks: %d loaded, %d pending",
            surface.getLoadedChunkCount(), surface.getPendingChunkCount()), 20, height - 125);

        font.setColor(Color.GREEN);
        font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, height - 150);
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    @Override
    public void dispose() {
        if (spriteBatch != null) spriteBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
    }
}
