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
import com.astral.combat.BulletHoleManager;
import com.astral.combat.Enemy;
import com.astral.combat.GroundProjectile;
import com.astral.combat.WeaponType;
import com.astral.combat.WeaponRenderer;
import com.badlogic.gdx.audio.Sound;
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

    // Weapon system
    private WeaponRenderer weaponRenderer;
    private WeaponType currentWeapon = WeaponType.PLASMA_RIFLE;
    private WeaponType pendingWeapon = null;  // For weapon switching
    private int currentWeaponIndex = 0;
    private int[] weaponAmmo = new int[WeaponType.values().length];
    private int[] weaponReserve = new int[WeaponType.values().length];
    private boolean weaponEquipped = true;
    private boolean reloading = false;
    private float reloadTimer = 0f;
    private boolean justFired = false;
    private float lastScrollTime = 0f;

    // Damage effect
    private float damageFlashTime = 0f;
    private float lastHealth = 100f;

    // Aim down sights
    private boolean aiming = false;
    private float normalFOV = 75f;
    private float aimFOV = 45f;

    // Weapon sounds - one for each weapon type
    private Sound plasmaRifleSound;
    private Sound laserPistolSound;
    private Sound scatterGunSound;
    private Sound railCannonSound;
    private Sound pulseSMGSound;

    // Hitmarker
    private float hitmarkerTimer = 0f;
    private static final float HITMARKER_DURATION = 0.15f;

    // Bullet holes
    private BulletHoleManager bulletHoleManager;

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

        // Initialize weapon system
        weaponRenderer = new WeaponRenderer(shapeRenderer);
        WeaponType[] weapons = WeaponType.values();
        for (int i = 0; i < weapons.length; i++) {
            weaponAmmo[i] = weapons[i].maxAmmo;
            weaponReserve[i] = weapons[i].maxAmmo * 4;  // 4 reloads worth
        }
        currentWeapon = WeaponType.PLASMA_RIFLE;
        currentWeaponIndex = 0;

        // Load weapon sounds - each weapon has its own sound
        plasmaRifleSound = loadSound("audio/laster-rifle.mp3");
        laserPistolSound = loadSound("audio/pistol-shot.mp3");
        scatterGunSound = loadSound("audio/shotgun.mp3");
        railCannonSound = loadSound("audio/rail-gun.mp3");
        pulseSMGSound = loadSound("audio/smg.mp3");

        // Initialize bullet hole manager
        bulletHoleManager = new BulletHoleManager();
        bulletHoleManager.initialize(camera);

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

        // Render bullet hole decals
        if (bulletHoleManager != null) {
            bulletHoleManager.render();
        }

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

        // Hitmarker decay
        hitmarkerTimer = Math.max(0, hitmarkerTimer - delta);

        // Update bullet holes
        if (bulletHoleManager != null) {
            bulletHoleManager.update(delta);
        }

        // Reset just fired flag
        justFired = false;

        // Weapon switching with number keys 1-5
        WeaponType[] weapons = WeaponType.values();
        boolean isSwitching = pendingWeapon != null;

        if (!isSwitching && !reloading) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && weapons.length > 0) switchToWeapon(0);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && weapons.length > 1) switchToWeapon(1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && weapons.length > 2) switchToWeapon(2);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) && weapons.length > 3) switchToWeapon(3);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5) && weapons.length > 4) switchToWeapon(4);
        }

        // Mouse scroll weapon switching
        if (!isSwitching && !reloading && Math.abs(inputSystem.scrollY) > 0.1f) {
            if (inputSystem.scrollY > 0) {
                // Scroll down - next weapon
                switchToWeapon((currentWeaponIndex + 1) % weapons.length);
            } else {
                // Scroll up - previous weapon
                switchToWeapon((currentWeaponIndex - 1 + weapons.length) % weapons.length);
            }
            // Consume the scroll input
            inputSystem.scrollY = 0f;
        }

        // Handle weapon switch animation - swap weapon when fully lowered
        if (pendingWeapon != null && weaponRenderer.isWeaponLowered()) {
            // Weapon is lowered - swap to new weapon
            currentWeapon = pendingWeapon;
            currentWeaponIndex = pendingWeapon.ordinal();
            pendingWeapon = null;
            reloading = false;  // Cancel reload on switch
            Gdx.app.log("Weapon", "Switched to " + currentWeapon.name);
        }

        // Update weapon renderer
        boolean isMoving = playerVelocity.len2() > 0.5f && isGrounded;
        aiming = Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && weaponEquipped && !reloading && !isSwitching;
        weaponRenderer.update(delta, isMoving, aiming, justFired, isSwitching);

        // Adjust FOV based on aim
        camera.fieldOfView = MathUtils.lerp(normalFOV, aimFOV, weaponRenderer.getAimTransition());

        // Equip/unequip weapon
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            weaponEquipped = !weaponEquipped;
            if (weaponEquipped) {
                Gdx.app.log("Weapon", "Equipped " + currentWeapon.name);
            } else {
                Gdx.app.log("Weapon", "Holstered");
            }
        }

        // Reload
        int ammo = weaponAmmo[currentWeaponIndex];
        int maxAmmo = currentWeapon.maxAmmo;
        int reserveAmmo = weaponReserve[currentWeaponIndex];

        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && !reloading && ammo < maxAmmo && reserveAmmo > 0 && weaponEquipped && !isSwitching) {
            reloading = true;
            reloadTimer = currentWeapon.reloadTime;
            Gdx.app.log("Weapon", "Reloading " + currentWeapon.name + "...");
        }

        if (reloading) {
            reloadTimer -= delta;
            if (reloadTimer <= 0) {
                int ammoNeeded = maxAmmo - ammo;
                int ammoToLoad = Math.min(ammoNeeded, reserveAmmo);
                weaponAmmo[currentWeaponIndex] += ammoToLoad;
                weaponReserve[currentWeaponIndex] -= ammoToLoad;
                reloading = false;
                Gdx.app.log("Weapon", "Reloaded!");
            }
        }

        // Weapon cooldown
        weaponCooldown = Math.max(0, weaponCooldown - delta);

        // Fire weapon on left click
        ammo = weaponAmmo[currentWeaponIndex];  // Refresh after potential reload
        reserveAmmo = weaponReserve[currentWeaponIndex];

        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && weaponCooldown <= 0 && weaponEquipped && !reloading && !isSwitching && ammo > 0) {
            fireWeapon();
            weaponCooldown = currentWeapon.fireRate;
            weaponAmmo[currentWeaponIndex]--;
            justFired = true;

            // Play weapon-specific sound
            playWeaponSound();

            // Auto-reload when empty
            if (weaponAmmo[currentWeaponIndex] <= 0 && weaponReserve[currentWeaponIndex] > 0) {
                reloading = true;
                reloadTimer = currentWeapon.reloadTime;
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
            if (proj.checkTerrainHit(terrainHeight)) {
                // Add bullet hole at impact point
                Vector3 hitPos = proj.getPosition().cpy();
                hitPos.y = terrainHeight;
                bulletHoleManager.addBulletHole(hitPos, Vector3.Y);
            }

            // Check enemy collisions
            for (Enemy enemy : enemies) {
                if (proj.checkHit(enemy)) {
                    hitmarkerTimer = HITMARKER_DURATION;  // Trigger hitmarker
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

    private void switchToWeapon(int index) {
        WeaponType[] weapons = WeaponType.values();
        if (index < 0 || index >= weapons.length) return;
        if (index == currentWeaponIndex) return;  // Already equipped

        pendingWeapon = weapons[index];
        Gdx.app.log("Weapon", "Switching to " + pendingWeapon.name + "...");
    }

    private void fireWeapon() {
        // Get camera direction for aiming
        Vector3 shootDir = new Vector3(camera.direction);

        // Spawn projectile from camera position (slightly forward)
        Vector3 spawnPos = new Vector3(camera.position).add(shootDir.x * 0.5f, shootDir.y * 0.5f, shootDir.z * 0.5f);

        // Scatter gun fires multiple pellets
        if (currentWeapon == WeaponType.SCATTER_GUN) {
            for (int i = 0; i < 6; i++) {
                Vector3 spreadDir = new Vector3(shootDir);
                spreadDir.x += MathUtils.random(-0.1f, 0.1f);
                spreadDir.y += MathUtils.random(-0.1f, 0.1f);
                spreadDir.z += MathUtils.random(-0.1f, 0.1f);
                spreadDir.nor();
                GroundProjectile proj = new GroundProjectile(new Vector3(spawnPos), spreadDir, projectileSpeed * 0.8f, currentWeapon.damage);
                projectiles.add(proj);
            }
        } else {
            GroundProjectile proj = new GroundProjectile(spawnPos, shootDir, projectileSpeed, currentWeapon.damage);
            projectiles.add(proj);
        }
    }

    private Sound loadSound(String path) {
        try {
            Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
            Gdx.app.log("Audio", "Loaded: " + path);
            return sound;
        } catch (Exception e) {
            Gdx.app.error("Audio", "Failed to load " + path + ": " + e.getMessage());
            return null;
        }
    }

    private void playWeaponSound() {
        Sound sound = switch (currentWeapon) {
            case PLASMA_RIFLE -> plasmaRifleSound;
            case LASER_PISTOL -> laserPistolSound;
            case SCATTER_GUN -> scatterGunSound;
            case RAIL_CANNON -> railCannonSound;
            case PULSE_SMG -> pulseSMGSound;
        };

        if (sound != null) {
            // Lower volume for rapid-fire weapons
            float volume = (currentWeapon == WeaponType.PULSE_SMG) ? 0.5f : 0.7f;
            sound.play(volume);
        }
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
            int ammo = weaponAmmo[currentWeaponIndex];
            int maxAmmo = currentWeapon.maxAmmo;
            float reloadProgress = reloading ? 1f - (reloadTimer / currentWeapon.reloadTime) : 0f;
            weaponRenderer.render(currentWeapon, ammo, maxAmmo, reloading, reloadProgress);
        }

        // Draw crosshair with ShapeRenderer
        int cx = width / 2;
        int cy = height / 2;
        float crosshairSize = 12f;
        float crosshairThickness = 2f;
        float gap = 4f;  // Gap in center for visibility

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Crosshair color - white normally, red when hitmarker active
        if (hitmarkerTimer > 0) {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);  // Red for hit
        } else {
            shapeRenderer.setColor(1f, 1f, 1f, 0.9f);  // White
        }

        // Draw round crosshair - 4 lines with gap in center
        // Top line
        shapeRenderer.rectLine(cx, cy + gap, cx, cy + crosshairSize, crosshairThickness);
        // Bottom line
        shapeRenderer.rectLine(cx, cy - gap, cx, cy - crosshairSize, crosshairThickness);
        // Left line
        shapeRenderer.rectLine(cx - gap, cy, cx - crosshairSize, cy, crosshairThickness);
        // Right line
        shapeRenderer.rectLine(cx + gap, cy, cx + crosshairSize, cy, crosshairThickness);

        // Draw outer circle/ring (using arc segments)
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

        // Draw hitmarker X when hit
        if (hitmarkerTimer > 0) {
            float hitSize = 8f + (hitmarkerTimer / HITMARKER_DURATION) * 4f;  // Animate size
            float hitAlpha = hitmarkerTimer / HITMARKER_DURATION;
            shapeRenderer.setColor(1f, 0.2f, 0.2f, hitAlpha);

            // X shape - diagonal lines
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

        spriteBatch.begin();

        // Ammo display (bottom right)
        if (weaponEquipped) {
            int ammo = weaponAmmo[currentWeaponIndex];
            int reserveAmmo = weaponReserve[currentWeaponIndex];
            font.setColor(ammo > currentWeapon.maxAmmo * 0.2f ? Color.WHITE : Color.RED);
            String ammoText = reloading ? "RELOADING..." : ammo + " / " + reserveAmmo;
            font.draw(spriteBatch, ammoText, width - 180, 80);
            font.setColor(new Color(currentWeapon.glowR, currentWeapon.glowG, currentWeapon.glowB, 1f));
            font.draw(spriteBatch, currentWeapon.name, width - 180, 55);

            // Weapon slot indicators
            font.setColor(Color.GRAY);
            WeaponType[] weapons = WeaponType.values();
            for (int i = 0; i < weapons.length; i++) {
                if (i == currentWeaponIndex) {
                    font.setColor(Color.WHITE);
                } else {
                    font.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
                }
                font.draw(spriteBatch, "[" + (i + 1) + "]", width - 180 + i * 35, 30);
            }
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
        font.draw(spriteBatch, "WASD: Move | SHIFT: Sprint | SPACE: Jump | 2xSPACE: Jetpack | LMB: Shoot | RMB: Aim | R: Reload | 1-5/Scroll: Weapons | ESC: Return", 20, 30);

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

        // Dispose weapon sounds
        if (plasmaRifleSound != null) plasmaRifleSound.dispose();
        if (laserPistolSound != null) laserPistolSound.dispose();
        if (scatterGunSound != null) scatterGunSound.dispose();
        if (railCannonSound != null) railCannonSound.dispose();
        if (pulseSMGSound != null) pulseSMGSound.dispose();

        // Dispose bullet hole manager
        if (bulletHoleManager != null) bulletHoleManager.dispose();
    }
}
