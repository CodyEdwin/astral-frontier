package com.astral.screens;

import com.astral.AstralFrontier;
import com.astral.components.*;
import com.astral.ecs.Entity;
import com.astral.ecs.World;
import com.astral.procedural.PlanetSurface;
import com.astral.procedural.PlanetType;
import com.astral.systems.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.astral.procedural.StructureGenerator;
import com.astral.combat.Enemy;
import com.astral.combat.GroundProjectile;
import com.badlogic.gdx.utils.Array;

/**
 * First-person exploration screen for planet surfaces
 */
public class PlanetExplorationScreen implements Screen {

    private final AstralFrontier game;
    private PlanetSurface planetSurface;

    // Camera
    private PerspectiveCamera camera;
    private float cameraYaw = 0f;
    private float cameraPitch = 0f;

    // Player state
    private Vector3 playerPosition = new Vector3(0, 10, 0);
    private Vector3 playerVelocity = new Vector3();
    private boolean isGrounded = false;
    private float playerHeight = 1.8f;
    private float eyeHeight = 1.6f;

    // Movement parameters
    private float walkSpeed = 6f;
    private float sprintSpeed = 12f;
    private float jumpVelocity = 8f;
    private float gravity = -20f;

    // Jetpack
    private boolean jetpackActive = false;
    private float jetpackFuel = 100f;
    private float jetpackMaxFuel = 100f;
    private float jetpackThrust = 15f;
    private float jetpackFuelConsumption = 2f;  // per second while thrusting (lasts ~50 seconds)
    private float jetpackFuelRecharge = 15f;    // per second when grounded (fast recharge)
    private float lastSpacePress = 0f;
    private float doubleTapThreshold = 0.3f;    // seconds
    private boolean spaceWasPressed = false;

    // Input
    private InputSystem inputSystem;

    // Rendering
    private ModelBatch modelBatch;
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    // Collision
    private float playerRadius = 0.5f;

    // UI state
    private boolean showDebug = true;

    // Combat
    private Array<Enemy> enemies = new Array<>();
    private Array<GroundProjectile> projectiles = new Array<>();
    private float playerHealth = 100f;
    private float playerMaxHealth = 100f;
    private float weaponCooldown = 0f;
    private float weaponFireRate = 0.15f;  // seconds between shots
    private float weaponDamage = 25f;
    private float projectileSpeed = 80f;
    private int killCount = 0;
    private float enemySpawnTimer = 0f;
    private float enemySpawnInterval = 5f;

    // Weapon state
    private boolean weaponEquipped = true;
    private int ammo = 30;
    private int maxAmmo = 30;
    private int reserveAmmo = 120;
    private boolean reloading = false;
    private float reloadTime = 1.5f;
    private float reloadTimer = 0f;
    private float weaponBobTime = 0f;
    private float weaponRecoil = 0f;

    // Damage effect
    private float damageFlashTime = 0f;
    private float lastHealth = 100f;

    public PlanetExplorationScreen(AstralFrontier game, long planetSeed, PlanetType planetType, String planetName) {
        this.game = game;

        // Create planet surface
        this.planetSurface = new PlanetSurface(planetSeed, planetType, planetName);
    }

    @Override
    public void show() {
        Gdx.app.log("PlanetExploration", "Landing on " + planetSurface.getName());

        // Generate planet
        planetSurface.generate();

        // Setup camera with extended far plane for infinite terrain
        camera = new PerspectiveCamera(75f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = 5000f;

        // Get input system
        inputSystem = game.getInputSystem();
        Gdx.input.setInputProcessor(inputSystem);
        inputSystem.setMouseLocked(true);

        // Spawn player at center - initial chunk loading will happen
        float spawnX = 0f;
        float spawnZ = 0f;
        // Start high - will fall to terrain once chunks load
        playerPosition.set(spawnX, 50f, spawnZ);

        // Trigger initial chunk loading around spawn
        planetSurface.update(playerPosition, 0f);

        // Setup rendering
        modelBatch = new ModelBatch();
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        Gdx.app.log("PlanetExploration", "Spawned at " + playerPosition);
    }

    @Override
    public void render(float delta) {
        // Input is processed in AstralFrontier.render() - don't call again here

        // Handle ESC to return to space
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            returnToSpace();
            return;
        }

        // Toggle debug
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showDebug = !showDebug;
        }

        // Update player
        updatePlayer(delta);

        // Update camera
        updateCamera();

        // Update terrain chunk streaming
        planetSurface.update(playerPosition, delta);

        // Update combat
        updateCombat(delta);

        // Clear with dark color first
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render sky gradient (before 3D)
        renderSkyGradient();

        // Re-enable depth test for 3D
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        // Render 3D world
        modelBatch.begin(camera);
        planetSurface.render(modelBatch);

        // Render enemies
        for (Enemy enemy : enemies) {
            if (enemy.isAlive()) {
                modelBatch.render(enemy.getModelInstance(), planetSurface.getEnvironment());
            }
        }

        // Render projectiles
        for (GroundProjectile proj : projectiles) {
            if (proj.isAlive()) {
                modelBatch.render(proj.getModelInstance(), planetSurface.getEnvironment());
            }
        }

        modelBatch.end();

        // Render UI
        renderUI();
    }

    private void renderSkyGradient() {
        Color skyColor = planetSurface.getSkyColor();
        Color horizonColor = new Color(skyColor).lerp(Color.WHITE, 0.3f);
        Color zenithColor = new Color(skyColor).lerp(new Color(0.1f, 0.1f, 0.2f, 1f), 0.5f);

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw gradient from horizon (bottom) to zenith (top)
        shapeRenderer.rect(0, 0, width, height,
            horizonColor, horizonColor, zenithColor, zenithColor);

        shapeRenderer.end();
    }

    private void updatePlayer(float delta) {
        // Double-tap space detection for jetpack toggle
        boolean spacePressed = inputSystem.jump;
        if (spacePressed && !spaceWasPressed) {
            float currentTime = System.nanoTime() / 1_000_000_000f;
            if (currentTime - lastSpacePress < doubleTapThreshold) {
                // Double tap detected - toggle jetpack
                jetpackActive = !jetpackActive;
                if (jetpackActive) {
                    Gdx.app.log("Jetpack", "ACTIVATED");
                } else {
                    Gdx.app.log("Jetpack", "DEACTIVATED");
                }
                lastSpacePress = 0; // Reset to prevent triple-tap toggle
            } else {
                lastSpacePress = currentTime;
            }
        }
        spaceWasPressed = spacePressed;

        // Recharge fuel when grounded
        if (isGrounded && jetpackFuel < jetpackMaxFuel) {
            jetpackFuel = Math.min(jetpackMaxFuel, jetpackFuel + jetpackFuelRecharge * delta);
        }

        // Get movement input
        Vector3 moveDir = new Vector3();

        if (inputSystem.forward) moveDir.z -= 1;
        if (inputSystem.backward) moveDir.z += 1;
        if (inputSystem.left) moveDir.x -= 1;
        if (inputSystem.right) moveDir.x += 1;

        // Normalize and rotate by camera yaw
        if (moveDir.len2() > 0) {
            moveDir.nor();
            moveDir.rotate(Vector3.Y, -cameraYaw);
        }

        // Apply speed (faster when jetpacking)
        float speed = inputSystem.sprint ? sprintSpeed : walkSpeed;
        if (jetpackActive && !isGrounded) speed *= 1.5f; // Faster in air with jetpack
        moveDir.scl(speed);

        // Set horizontal velocity
        playerVelocity.x = moveDir.x;
        playerVelocity.z = moveDir.z;

        // Apply gravity (reduced when jetpack active)
        float effectiveGravity = jetpackActive ? gravity * 0.3f : gravity;
        playerVelocity.y += effectiveGravity * delta;

        // Clamp falling speed
        playerVelocity.y = Math.max(playerVelocity.y, -50f);

        // Jetpack thrust or normal jump
        if (inputSystem.jump) {
            if (jetpackActive && jetpackFuel > 0) {
                // Jetpack thrust
                playerVelocity.y = jetpackThrust;
                jetpackFuel -= jetpackFuelConsumption * delta;
                jetpackFuel = Math.max(0, jetpackFuel);
                isGrounded = false;
            } else if (isGrounded) {
                // Normal jump
                playerVelocity.y = jumpVelocity;
                isGrounded = false;
            }
        }

        // Calculate new position
        float newX = playerPosition.x + playerVelocity.x * delta;
        float newZ = playerPosition.z + playerVelocity.z * delta;

        // Structure collision - only move if no collision
        if (!planetSurface.checkStructureCollision(newX, newZ, playerRadius)) {
            playerPosition.x = newX;
            playerPosition.z = newZ;
        } else {
            // Try sliding along X axis only
            if (!planetSurface.checkStructureCollision(newX, playerPosition.z, playerRadius)) {
                playerPosition.x = newX;
            }
            // Try sliding along Z axis only
            else if (!planetSurface.checkStructureCollision(playerPosition.x, newZ, playerRadius)) {
                playerPosition.z = newZ;
            }
            // Blocked completely - don't move horizontally
        }

        // Apply vertical movement (gravity/jumping)
        playerPosition.y += playerVelocity.y * delta;

        // Ground collision
        float terrainHeight = planetSurface.getHeightAt(playerPosition.x, playerPosition.z);
        float groundLevel = terrainHeight + playerHeight;

        if (playerPosition.y <= groundLevel) {
            playerPosition.y = groundLevel;
            playerVelocity.y = 0;
            isGrounded = true;
        } else {
            isGrounded = false;
        }

        // No bounds - terrain is infinite with chunk streaming

        // Update camera yaw/pitch from mouse
        float sensitivity = 15f;
        cameraYaw -= inputSystem.yaw * sensitivity;
        cameraPitch += inputSystem.pitch * sensitivity;  // Inverted for natural feel

        // Clamp pitch
        cameraPitch = MathUtils.clamp(cameraPitch, -89f, 89f);
    }

    private void updateCamera() {
        // Position camera at eye level
        camera.position.set(
            playerPosition.x,
            playerPosition.y - playerHeight + eyeHeight,
            playerPosition.z
        );

        // Calculate look direction
        float pitchRad = cameraPitch * MathUtils.degreesToRadians;
        float yawRad = cameraYaw * MathUtils.degreesToRadians;

        camera.direction.set(
            MathUtils.sin(yawRad) * MathUtils.cos(pitchRad),
            MathUtils.sin(pitchRad),
            -MathUtils.cos(yawRad) * MathUtils.cos(pitchRad)
        ).nor();

        camera.up.set(Vector3.Y);
        camera.update();
    }

    private void updateCombat(float delta) {
        // Damage flash effect
        damageFlashTime = Math.max(0, damageFlashTime - delta * 3f);
        if (playerHealth < lastHealth) {
            damageFlashTime = 1f;
        }
        lastHealth = playerHealth;

        // Weapon bob animation
        if (playerVelocity.len2() > 0.5f && isGrounded) {
            weaponBobTime += delta * 10f;
        }
        weaponRecoil = Math.max(0, weaponRecoil - delta * 15f);

        // Equip/unequip weapon
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            weaponEquipped = !weaponEquipped;
            if (weaponEquipped) {
                Gdx.app.log("Weapon", "Equipped");
            } else {
                Gdx.app.log("Weapon", "Holstered");
            }
        }

        // Reload
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && !reloading && ammo < maxAmmo && reserveAmmo > 0 && weaponEquipped) {
            reloading = true;
            reloadTimer = reloadTime;
            Gdx.app.log("Weapon", "Reloading...");
        }

        if (reloading) {
            reloadTimer -= delta;
            if (reloadTimer <= 0) {
                int ammoNeeded = maxAmmo - ammo;
                int ammoToLoad = Math.min(ammoNeeded, reserveAmmo);
                ammo += ammoToLoad;
                reserveAmmo -= ammoToLoad;
                reloading = false;
                Gdx.app.log("Weapon", "Reloaded!");
            }
        }

        // Weapon cooldown
        weaponCooldown = Math.max(0, weaponCooldown - delta);

        // Fire weapon on left click
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && weaponCooldown <= 0 && weaponEquipped && !reloading && ammo > 0) {
            fireWeapon();
            weaponCooldown = weaponFireRate;
            ammo--;
            weaponRecoil = 1f;

            // Auto-reload when empty
            if (ammo <= 0 && reserveAmmo > 0) {
                reloading = true;
                reloadTimer = reloadTime;
            }
        }

        // Spawn enemies periodically
        enemySpawnTimer += delta;
        if (enemySpawnTimer >= enemySpawnInterval && enemies.size < 20) {
            spawnEnemy();
            enemySpawnTimer = 0;
        }

        // Update enemies
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta, playerPosition, (x, z) -> planetSurface.getHeightAt(x, z));

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
            float terrainHeight = planetSurface.getHeightAt(proj.getPosition().x, proj.getPosition().z);
            proj.checkTerrainHit(terrainHeight);

            // Check enemy collisions
            for (Enemy enemy : enemies) {
                if (proj.checkHit(enemy)) {
                    break;
                }
            }

            // Clean up dead projectiles
            if (!proj.isAlive()) {
                proj.dispose();
                projectiles.removeIndex(i);
            }
        }

        // Player death
        if (playerHealth <= 0) {
            playerHealth = 0;
            // Could add death screen here
        }
    }

    private void fireWeapon() {
        // Get camera direction for aiming
        Vector3 shootDir = new Vector3(camera.direction);

        // Spawn projectile from camera position (slightly forward)
        Vector3 spawnPos = new Vector3(camera.position).add(shootDir.x * 0.5f, shootDir.y * 0.5f, shootDir.z * 0.5f);

        GroundProjectile proj = new GroundProjectile(spawnPos, shootDir, projectileSpeed, weaponDamage);
        projectiles.add(proj);
    }

    private void spawnEnemy() {
        // Spawn at random position around player
        float angle = MathUtils.random(360f) * MathUtils.degreesToRadians;
        float distance = 30f + MathUtils.random(30f);

        float spawnX = playerPosition.x + MathUtils.cos(angle) * distance;
        float spawnZ = playerPosition.z + MathUtils.sin(angle) * distance;
        float spawnY = planetSurface.getHeightAt(spawnX, spawnZ);

        // Random enemy type
        Enemy.EnemyType[] types = Enemy.EnemyType.values();
        Enemy.EnemyType type = types[MathUtils.random(types.length - 1)];

        Enemy enemy = new Enemy(type, spawnX, spawnY, spawnZ);
        enemies.add(enemy);
    }

    private void renderWeapon() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Weapon bob effect
        float bobX = MathUtils.sin(weaponBobTime) * 6f;
        float bobY = Math.abs(MathUtils.cos(weaponBobTime * 2f)) * 4f;

        // Recoil effect - kicks back and up
        float recoilY = weaponRecoil * 25f;
        float recoilX = weaponRecoil * -8f;

        // Base position (bottom right, angled like holding a rifle)
        float baseX = width * 0.55f + bobX + recoilX;
        float baseY = -height * 0.15f + bobY - recoilY;

        // Reload animation - weapon tilts down and to side
        float reloadTilt = 0f;
        if (reloading) {
            float reloadProgress = 1f - (reloadTimer / reloadTime);
            float dipAmount = MathUtils.sin(reloadProgress * MathUtils.PI);
            baseY -= dipAmount * 60f;
            baseX -= dipAmount * 30f;
            reloadTilt = dipAmount * 15f;
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // === ARM ===
        // Upper arm (coming from bottom right)
        shapeRenderer.setColor(0.7f, 0.55f, 0.45f, 1f); // Skin tone
        drawRotatedRect(baseX - 50, baseY + 20, 120, 55, -25 + reloadTilt);

        // Forearm
        shapeRenderer.setColor(0.65f, 0.5f, 0.4f, 1f);
        drawRotatedRect(baseX + 40, baseY + 60, 100, 50, -15 + reloadTilt);

        // Glove/hand
        shapeRenderer.setColor(0.2f, 0.2f, 0.22f, 1f);
        drawRotatedRect(baseX + 110, baseY + 85, 70, 45, -10 + reloadTilt);

        // Fingers wrapped around grip
        shapeRenderer.setColor(0.22f, 0.22f, 0.24f, 1f);
        shapeRenderer.rect(baseX + 155, baseY + 95, 25, 35);
        shapeRenderer.rect(baseX + 145, baseY + 90, 20, 30);

        // === GUN ===
        // Main receiver (angled, perspective)
        shapeRenderer.setColor(0.28f, 0.28f, 0.3f, 1f);
        drawRotatedRect(baseX + 100, baseY + 130, 200, 50, -5 + reloadTilt);

        // Upper receiver / top rail
        shapeRenderer.setColor(0.35f, 0.35f, 0.38f, 1f);
        drawRotatedRect(baseX + 120, baseY + 175, 160, 18, -5 + reloadTilt);

        // Barrel shroud
        shapeRenderer.setColor(0.32f, 0.32f, 0.35f, 1f);
        drawRotatedRect(baseX + 250, baseY + 145, 120, 35, -5 + reloadTilt);

        // Barrel
        shapeRenderer.setColor(0.4f, 0.4f, 0.42f, 1f);
        drawRotatedRect(baseX + 340, baseY + 155, 80, 20, -5 + reloadTilt);

        // Muzzle / plasma emitter (glowing)
        float glowPulse = 0.7f + 0.3f * MathUtils.sin(weaponBobTime * 3f);
        shapeRenderer.setColor(0.1f * glowPulse, 0.6f * glowPulse, 0.9f * glowPulse, 1f);
        shapeRenderer.rect(baseX + 415, baseY + 152, 15, 26);

        // Muzzle flash when firing
        if (weaponRecoil > 0.5f) {
            shapeRenderer.setColor(0.3f, 0.8f, 1f, weaponRecoil);
            shapeRenderer.rect(baseX + 430, baseY + 145, 25, 40);
            shapeRenderer.setColor(0.6f, 0.9f, 1f, weaponRecoil * 0.7f);
            shapeRenderer.rect(baseX + 435, baseY + 155, 35, 20);
        }

        // Magazine / energy cell
        shapeRenderer.setColor(0.22f, 0.22f, 0.25f, 1f);
        shapeRenderer.rect(baseX + 170, baseY + 90, 35, 50);

        // Energy cell glow
        float cellGlow = (ammo / (float) maxAmmo) * 0.8f + 0.2f;
        shapeRenderer.setColor(0.05f * cellGlow, 0.4f * cellGlow, 0.7f * cellGlow, 1f);
        shapeRenderer.rect(baseX + 175, baseY + 95, 25, 40);

        // Grip
        shapeRenderer.setColor(0.18f, 0.18f, 0.2f, 1f);
        shapeRenderer.rect(baseX + 150, baseY + 70, 30, 65);

        // Trigger guard
        shapeRenderer.setColor(0.25f, 0.25f, 0.28f, 1f);
        shapeRenderer.rect(baseX + 135, baseY + 105, 50, 8);

        // Stock (extending back)
        shapeRenderer.setColor(0.25f, 0.25f, 0.28f, 1f);
        drawRotatedRect(baseX + 50, baseY + 120, 80, 35, -8 + reloadTilt);

        // Cheek rest
        shapeRenderer.setColor(0.3f, 0.3f, 0.32f, 1f);
        drawRotatedRect(baseX + 70, baseY + 150, 50, 15, -8 + reloadTilt);

        // Scope/sight
        shapeRenderer.setColor(0.2f, 0.2f, 0.22f, 1f);
        shapeRenderer.rect(baseX + 200, baseY + 190, 60, 25);
        // Scope lens
        shapeRenderer.setColor(0.1f, 0.3f, 0.5f, 0.8f);
        shapeRenderer.rect(baseX + 205, baseY + 195, 20, 15);

        // Ammo indicator LEDs on side
        int lightsOn = (int) ((ammo / (float) maxAmmo) * 6);
        for (int i = 0; i < 6; i++) {
            if (i < lightsOn) {
                shapeRenderer.setColor(0.2f, 0.9f, 0.3f, 1f);
            } else {
                shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f);
            }
            shapeRenderer.rect(baseX + 220 + i * 10, baseY + 180, 6, 6);
        }

        // Details - vents on barrel shroud
        shapeRenderer.setColor(0.15f, 0.15f, 0.18f, 1f);
        for (int i = 0; i < 4; i++) {
            shapeRenderer.rect(baseX + 260 + i * 20, baseY + 150, 8, 25);
        }

        shapeRenderer.end();
    }

    // Helper to draw rotated rectangles (simplified - just offsets for now)
    private void drawRotatedRect(float x, float y, float width, float height, float angleDeg) {
        // Simple approximation - shift corners based on angle
        float rad = angleDeg * MathUtils.degreesToRadians;
        float cos = MathUtils.cos(rad);
        float sin = MathUtils.sin(rad);

        float x1 = x, y1 = y;
        float x2 = x + width * cos, y2 = y + width * sin;
        float x3 = x + width * cos - height * sin, y3 = y + width * sin + height * cos;
        float x4 = x - height * sin, y4 = y + height * cos;

        shapeRenderer.triangle(x1, y1, x2, y2, x3, y3);
        shapeRenderer.triangle(x1, y1, x3, y3, x4, y4);
    }

    private void renderUI() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Damage flash effect (red screen overlay)
        if (damageFlashTime > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0f, 0f, damageFlashTime * 0.4f);
            shapeRenderer.rect(0, 0, width, height);
            shapeRenderer.end();
        }

        // Draw weapon (first-person gun view)
        if (weaponEquipped) {
            renderWeapon();
        }

        spriteBatch.begin();

        // Crosshair
        int cx = width / 2;
        int cy = height / 2;
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "+", cx - 5, cy + 8);

        // Ammo display (bottom right)
        if (weaponEquipped) {
            font.setColor(ammo > 5 ? Color.WHITE : Color.RED);
            String ammoText = reloading ? "RELOADING..." : ammo + " / " + reserveAmmo;
            font.draw(spriteBatch, ammoText, width - 150, 80);
            font.setColor(Color.GRAY);
            font.draw(spriteBatch, "PLASMA RIFLE", width - 150, 55);
        } else {
            font.setColor(Color.GRAY);
            font.draw(spriteBatch, "[E] to equip", width - 150, 80);
        }

        // Planet name
        font.setColor(new Color(0.8f, 0.9f, 1f, 1f));
        font.draw(spriteBatch, planetSurface.getType().displayName + ": " + planetSurface.getName(), 20, Gdx.graphics.getHeight() - 20);

        // Jetpack status
        if (jetpackActive) {
            font.setColor(new Color(0.3f, 1f, 0.5f, 1f));
            font.draw(spriteBatch, "JETPACK ACTIVE", Gdx.graphics.getWidth() - 180, Gdx.graphics.getHeight() - 20);
        }

        // Jetpack fuel bar
        int fuelBarWidth = 150;
        int fuelBarHeight = 12;
        int fuelBarX = Gdx.graphics.getWidth() - 170;
        int fuelBarY = Gdx.graphics.getHeight() - 55;

        spriteBatch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Background
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        shapeRenderer.rect(fuelBarX, fuelBarY, fuelBarWidth, fuelBarHeight);
        // Fuel level
        float fuelPercent = jetpackFuel / jetpackMaxFuel;
        if (fuelPercent > 0.3f) {
            shapeRenderer.setColor(0.3f, 0.8f, 1f, 1f); // Blue
        } else {
            shapeRenderer.setColor(1f, 0.3f, 0.3f, 1f); // Red when low
        }
        shapeRenderer.rect(fuelBarX, fuelBarY, fuelBarWidth * fuelPercent, fuelBarHeight);
        shapeRenderer.end();
        spriteBatch.begin();

        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "FUEL", fuelBarX - 50, fuelBarY + 12);

        // Health bar (bottom left)
        int healthBarWidth = 200;
        int healthBarHeight = 20;
        int healthBarX = 20;
        int healthBarY = 60;

        spriteBatch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        // Background
        shapeRenderer.setColor(0.3f, 0.1f, 0.1f, 0.8f);
        shapeRenderer.rect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);
        // Health level
        float healthPercent = playerHealth / playerMaxHealth;
        if (healthPercent > 0.3f) {
            shapeRenderer.setColor(0.2f, 0.8f, 0.3f, 1f); // Green
        } else {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f); // Red when low
        }
        shapeRenderer.rect(healthBarX, healthBarY, healthBarWidth * healthPercent, healthBarHeight);
        shapeRenderer.end();
        spriteBatch.begin();

        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "HEALTH", healthBarX, healthBarY + 38);

        // Kill count
        font.setColor(new Color(1f, 0.8f, 0.2f, 1f));
        font.draw(spriteBatch, "KILLS: " + killCount, healthBarX + healthBarWidth + 30, healthBarY + 18);

        // Enemy count
        font.setColor(new Color(1f, 0.4f, 0.4f, 1f));
        font.draw(spriteBatch, "ENEMIES: " + enemies.size, healthBarX + healthBarWidth + 130, healthBarY + 18);

        // Controls hint
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        font.draw(spriteBatch, "WASD: Move | SHIFT: Sprint | SPACE: Jump | 2xSPACE: Jetpack | LMB: Shoot | R: Reload | E: Equip | ESC: Return", 20, 30);

        // Debug info
        if (showDebug) {
            font.setColor(Color.YELLOW);
            float terrainHeight = planetSurface.getHeightAt(playerPosition.x, playerPosition.z);
            font.draw(spriteBatch, String.format("Pos: %.1f, %.1f, %.1f", playerPosition.x, playerPosition.y, playerPosition.z), 20, Gdx.graphics.getHeight() - 50);
            font.draw(spriteBatch, String.format("Ground: %.1f | Grounded: %s", terrainHeight, isGrounded), 20, Gdx.graphics.getHeight() - 75);
            font.draw(spriteBatch, String.format("Look: Yaw %.1f, Pitch %.1f", cameraYaw, cameraPitch), 20, Gdx.graphics.getHeight() - 100);

            // Chunk streaming info
            font.setColor(Color.CYAN);
            font.draw(spriteBatch, String.format("Chunks: %d loaded, %d pending",
                planetSurface.getLoadedChunkCount(), planetSurface.getPendingChunkCount()),
                20, Gdx.graphics.getHeight() - 125);

            font.setColor(Color.GREEN);
            font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, Gdx.graphics.getHeight() - 150);
        }

        spriteBatch.end();
    }

    private void returnToSpace() {
        Gdx.app.log("PlanetExploration", "Returning to space...");
        game.startGame();
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (modelBatch != null) modelBatch.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (planetSurface != null) planetSurface.dispose();

        // Clean up combat
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        enemies.clear();
        for (GroundProjectile proj : projectiles) {
            proj.dispose();
        }
        projectiles.clear();
    }
}
