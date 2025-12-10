package com.astral.exploration;

import com.astral.combat.BulletHoleManager;
import com.astral.combat.Enemy;
import com.astral.combat.GroundProjectile;
import com.astral.procedural.PlanetSurface;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Handles enemies, projectiles, spawning, and damage
 */
public class CombatManager implements Disposable {

    private final Array<Enemy> enemies = new Array<>();
    private final Array<GroundProjectile> projectiles = new Array<>();
    private BulletHoleManager bulletHoleManager;

    // Player stats
    private float playerHealth = 100f;
    private float playerMaxHealth = 100f;
    private int killCount = 0;

    // Enemy spawning
    private float enemySpawnTimer = 0f;
    private float enemySpawnInterval = 5f;
    private int maxEnemies = 20;

    // Effects
    private float damageFlashTime = 0f;
    private float lastHealth = 100f;
    private float hitmarkerTimer = 0f;
    private static final float HITMARKER_DURATION = 0.15f;

    public CombatManager() {
    }

    public void initialize(PerspectiveCamera camera) {
        bulletHoleManager = new BulletHoleManager();
        bulletHoleManager.initialize(camera);
    }

    public void update(float delta, Vector3 playerPosition, PlanetSurface surface) {
        // Damage flash effect
        damageFlashTime = Math.max(0, damageFlashTime - delta * 3f);
        if (playerHealth < lastHealth) {
            damageFlashTime = 1f;
        }
        lastHealth = playerHealth;

        // Hitmarker decay
        hitmarkerTimer = Math.max(0, hitmarkerTimer - delta);

        // Update bullet holes
        if (bulletHoleManager != null) {
            bulletHoleManager.update(delta);
        }

        // Spawn enemies periodically
        enemySpawnTimer += delta;
        if (enemySpawnTimer >= enemySpawnInterval && enemies.size < maxEnemies) {
            spawnEnemy(playerPosition, surface);
            enemySpawnTimer = 0;
        }

        // Update enemies
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta, playerPosition, (x, z) -> surface.getHeightAt(x, z));

            // Enemy attacks player
            if (enemy.canAttackPlayer(playerPosition)) {
                float damage = enemy.attack();
                playerHealth -= damage;
                Gdx.app.log("Combat", "Player hit for " + damage + " damage!");
            }

            // Clean up dead enemies
            if (!enemy.isAlive()) {
                killCount++;
                enemy.dispose();
                enemies.removeIndex(i);
            }
        }

        // Update projectiles
        for (int i = projectiles.size - 1; i >= 0; i--) {
            GroundProjectile proj = projectiles.get(i);
            proj.update(delta);

            // Check terrain collision
            float terrainHeight = surface.getHeightAt(proj.getPosition().x, proj.getPosition().z);
            if (proj.checkTerrainHit(terrainHeight)) {
                Vector3 hitPos = proj.getPosition().cpy();
                hitPos.y = terrainHeight;
                bulletHoleManager.addBulletHole(hitPos, Vector3.Y);
            }

            // Check enemy collisions
            for (Enemy enemy : enemies) {
                if (proj.checkHit(enemy)) {
                    hitmarkerTimer = HITMARKER_DURATION;
                    break;
                }
            }

            // Clean up dead projectiles
            if (!proj.isAlive()) {
                proj.dispose();
                projectiles.removeIndex(i);
            }
        }

        // Clamp player health
        if (playerHealth <= 0) {
            playerHealth = 0;
        }
    }

    private void spawnEnemy(Vector3 playerPosition, PlanetSurface surface) {
        float angle = MathUtils.random(360f) * MathUtils.degreesToRadians;
        float distance = 30f + MathUtils.random(30f);

        float spawnX = playerPosition.x + MathUtils.cos(angle) * distance;
        float spawnZ = playerPosition.z + MathUtils.sin(angle) * distance;
        float spawnY = surface.getHeightAt(spawnX, spawnZ);

        Enemy.EnemyType[] types = Enemy.EnemyType.values();
        Enemy.EnemyType type = types[MathUtils.random(types.length - 1)];

        Enemy enemy = new Enemy(type, spawnX, spawnY, spawnZ);
        enemies.add(enemy);
    }

    public void render(ModelBatch modelBatch, Environment environment) {
        // Render enemies
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                modelBatch.render(enemy.getModelInstance(), environment);
            }
        }

        // Render projectiles
        for (GroundProjectile proj : projectiles) {
            if (proj.isAlive()) {
                modelBatch.render(proj.getModelInstance(), environment);
            }
        }
    }

    public void renderBulletHoles() {
        if (bulletHoleManager != null) {
            bulletHoleManager.render();
        }
    }

    // Getters
    public Array<GroundProjectile> getProjectiles() { return projectiles; }
    public float getPlayerHealth() { return playerHealth; }
    public float getPlayerMaxHealth() { return playerMaxHealth; }
    public int getKillCount() { return killCount; }
    public int getEnemyCount() { return enemies.size; }
    public float getDamageFlashTime() { return damageFlashTime; }
    public float getHitmarkerTimer() { return hitmarkerTimer; }
    public float getHitmarkerDuration() { return HITMARKER_DURATION; }

    @Override
    public void dispose() {
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        enemies.clear();
        for (GroundProjectile proj : projectiles) {
            proj.dispose();
        }
        projectiles.clear();
        if (bulletHoleManager != null) bulletHoleManager.dispose();
    }
}
