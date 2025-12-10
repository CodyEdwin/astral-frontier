package com.astral.exploration;

import com.astral.combat.GltfWeaponRenderer;
import com.astral.combat.GroundProjectile;
import com.astral.combat.WeaponRenderer;
import com.astral.combat.WeaponType;
import com.astral.systems.InputSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Handles weapon switching, firing, reloading, and sounds
 */
public class WeaponSystem implements Disposable {

    // Weapon renderers
    private WeaponRenderer weaponRenderer;
    private GltfWeaponRenderer gltfWeaponRenderer;

    // Current weapon state
    private WeaponType currentWeapon = WeaponType.PLASMA_RIFLE;
    private WeaponType pendingWeapon = null;
    private int currentWeaponIndex = 0;
    private int[] weaponAmmo;
    private int[] weaponReserve;
    private boolean weaponEquipped = true;
    private boolean reloading = false;
    private float reloadTimer = 0f;
    private float weaponCooldown = 0f;
    private boolean justFired = false;
    private boolean aiming = false;

    // Projectile settings
    private float projectileSpeed = 80f;

    // Weapon sounds
    private Sound plasmaRifleSound;
    private Sound laserPistolSound;
    private Sound scatterGunSound;
    private Sound railCannonSound;
    private Sound pulseSMGSound;

    public WeaponSystem() {
    }

    public void initialize(ShapeRenderer shapeRenderer, Camera camera) {
        // Initialize renderers
        weaponRenderer = new WeaponRenderer(shapeRenderer);
        gltfWeaponRenderer = new GltfWeaponRenderer();
        gltfWeaponRenderer.initialize(camera);

        // Initialize ammo
        WeaponType[] weapons = WeaponType.values();
        weaponAmmo = new int[weapons.length];
        weaponReserve = new int[weapons.length];
        for (int i = 0; i < weapons.length; i++) {
            weaponAmmo[i] = weapons[i].maxAmmo;
            weaponReserve[i] = weapons[i].maxAmmo * 4;
        }

        // Load sounds
        plasmaRifleSound = loadSound("audio/laster-rifle.mp3");
        laserPistolSound = loadSound("audio/pistol-shot.mp3");
        scatterGunSound = loadSound("audio/shotgun.mp3");
        railCannonSound = loadSound("audio/rail-gun.mp3");
        pulseSMGSound = loadSound("audio/smg.mp3");
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

    public void update(float delta, InputSystem input, Camera camera, boolean isMoving, Array<GroundProjectile> projectiles) {
        justFired = false;
        WeaponType[] weapons = WeaponType.values();
        boolean isSwitching = pendingWeapon != null;

        // Weapon switching with number keys
        if (!isSwitching && !reloading) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1) && weapons.length > 0) switchToWeapon(0);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2) && weapons.length > 1) switchToWeapon(1);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3) && weapons.length > 2) switchToWeapon(2);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_4) && weapons.length > 3) switchToWeapon(3);
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_5) && weapons.length > 4) switchToWeapon(4);
        }

        // Mouse scroll weapon switching
        if (!isSwitching && !reloading && Math.abs(input.scrollY) > 0.1f) {
            if (input.scrollY > 0) {
                switchToWeapon((currentWeaponIndex + 1) % weapons.length);
            } else {
                switchToWeapon((currentWeaponIndex - 1 + weapons.length) % weapons.length);
            }
            input.scrollY = 0f;
        }

        // Handle weapon switch animation
        if (pendingWeapon != null && weaponRenderer.isWeaponLowered()) {
            currentWeapon = pendingWeapon;
            currentWeaponIndex = pendingWeapon.ordinal();
            pendingWeapon = null;
            reloading = false;
            Gdx.app.log("Weapon", "Switched to " + currentWeapon.name);
        }

        // Update weapon renderer
        aiming = Gdx.input.isButtonPressed(Input.Buttons.RIGHT) && weaponEquipped && !reloading && !isSwitching;
        weaponRenderer.update(delta, isMoving, aiming, justFired, isSwitching);

        // Update GLTF weapon if using PLASMA_RIFLE
        if (currentWeapon == WeaponType.PLASMA_RIFLE && gltfWeaponRenderer != null && gltfWeaponRenderer.isInitialized()) {
            gltfWeaponRenderer.update(delta, camera, isMoving, aiming, justFired, reloading);
        }

        // Equip/unequip weapon
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            weaponEquipped = !weaponEquipped;
            Gdx.app.log("Weapon", weaponEquipped ? "Equipped " + currentWeapon.name : "Holstered");
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

        // Fire weapon
        ammo = weaponAmmo[currentWeaponIndex];
        if (Gdx.input.isButtonPressed(Input.Buttons.LEFT) && weaponCooldown <= 0 && weaponEquipped && !reloading && !isSwitching && ammo > 0) {
            fireWeapon(camera, projectiles);
            weaponCooldown = currentWeapon.fireRate;
            weaponAmmo[currentWeaponIndex]--;
            justFired = true;
            playWeaponSound();

            // Auto-reload when empty
            if (weaponAmmo[currentWeaponIndex] <= 0 && weaponReserve[currentWeaponIndex] > 0) {
                reloading = true;
                reloadTimer = currentWeapon.reloadTime;
            }
        }
    }

    private void switchToWeapon(int index) {
        WeaponType[] weapons = WeaponType.values();
        if (index < 0 || index >= weapons.length) return;
        if (index == currentWeaponIndex) return;

        pendingWeapon = weapons[index];
        Gdx.app.log("Weapon", "Switching to " + pendingWeapon.name + "...");
    }

    private void fireWeapon(Camera camera, Array<GroundProjectile> projectiles) {
        Vector3 shootDir = new Vector3(camera.direction);
        Vector3 spawnPos = new Vector3(camera.position).add(shootDir.x * 0.5f, shootDir.y * 0.5f, shootDir.z * 0.5f);

        if (currentWeapon == WeaponType.SCATTER_GUN) {
            for (int i = 0; i < 6; i++) {
                Vector3 spreadDir = new Vector3(shootDir);
                spreadDir.x += MathUtils.random(-0.1f, 0.1f);
                spreadDir.y += MathUtils.random(-0.1f, 0.1f);
                spreadDir.z += MathUtils.random(-0.1f, 0.1f);
                spreadDir.nor();
                projectiles.add(new GroundProjectile(new Vector3(spawnPos), spreadDir, projectileSpeed * 0.8f, currentWeapon.damage));
            }
        } else {
            projectiles.add(new GroundProjectile(spawnPos, shootDir, projectileSpeed, currentWeapon.damage));
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
            float volume = (currentWeapon == WeaponType.PULSE_SMG) ? 0.5f : 0.7f;
            sound.play(volume);
        }
    }

    public void render() {
        boolean useGltfWeapon = currentWeapon == WeaponType.PLASMA_RIFLE &&
                                gltfWeaponRenderer != null && gltfWeaponRenderer.isInitialized();

        if (weaponEquipped && useGltfWeapon) {
            Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_DEPTH_BUFFER_BIT);
            gltfWeaponRenderer.render();
        }
    }

    public void renderWeapon2D() {
        boolean useGltfWeapon = currentWeapon == WeaponType.PLASMA_RIFLE &&
                                gltfWeaponRenderer != null && gltfWeaponRenderer.isInitialized();

        if (weaponEquipped && !useGltfWeapon) {
            int ammo = weaponAmmo[currentWeaponIndex];
            int maxAmmo = currentWeapon.maxAmmo;
            float reloadProgress = reloading ? 1f - (reloadTimer / currentWeapon.reloadTime) : 0f;
            weaponRenderer.render(currentWeapon, ammo, maxAmmo, reloading, reloadProgress);
        }
    }

    // Getters
    public WeaponType getCurrentWeapon() { return currentWeapon; }
    public int getCurrentAmmo() { return weaponAmmo[currentWeaponIndex]; }
    public int getReserveAmmo() { return weaponReserve[currentWeaponIndex]; }
    public boolean isReloading() { return reloading; }
    public boolean isWeaponEquipped() { return weaponEquipped; }
    public boolean isAiming() { return aiming; }
    public boolean justFired() { return justFired; }
    public float getAimTransition() { return weaponRenderer.getAimTransition(); }
    public int getCurrentWeaponIndex() { return currentWeaponIndex; }

    @Override
    public void dispose() {
        if (plasmaRifleSound != null) plasmaRifleSound.dispose();
        if (laserPistolSound != null) laserPistolSound.dispose();
        if (scatterGunSound != null) scatterGunSound.dispose();
        if (railCannonSound != null) railCannonSound.dispose();
        if (pulseSMGSound != null) pulseSMGSound.dispose();
        if (gltfWeaponRenderer != null) gltfWeaponRenderer.dispose();
    }
}
