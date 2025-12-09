package com.astral.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

/**
 * Renders the first-person weapon view for all weapon types
 */
public class WeaponRenderer {

    private ShapeRenderer shapeRenderer;

    // Animation state
    private float weaponBobTime = 0f;
    private float weaponRecoil = 0f;
    private float aimTransition = 0f;
    private float switchProgress = 1f; // 1 = fully visible, 0 = hidden for switching

    public WeaponRenderer(ShapeRenderer shapeRenderer) {
        this.shapeRenderer = shapeRenderer;
    }

    /**
     * Update weapon animations
     */
    public void update(float delta, boolean isMoving, boolean isAiming, boolean justFired, boolean isSwitching) {
        // Weapon bob when moving
        if (isMoving) {
            weaponBobTime += delta * 10f;
        }

        // Recoil decay
        weaponRecoil = Math.max(0, weaponRecoil - delta * 15f);

        // Fire recoil
        if (justFired) {
            weaponRecoil = 1f;
        }

        // Smooth aim transition
        float aimSpeed = 8f;
        if (isAiming) {
            aimTransition = Math.min(1f, aimTransition + delta * aimSpeed);
        } else {
            aimTransition = Math.max(0f, aimTransition - delta * aimSpeed);
        }

        // Weapon switch animation
        float switchSpeed = 6f;
        if (isSwitching) {
            switchProgress = Math.max(0f, switchProgress - delta * switchSpeed);
        } else {
            switchProgress = Math.min(1f, switchProgress + delta * switchSpeed);
        }
    }

    /**
     * Render the weapon based on type
     */
    public void render(WeaponType type, int ammo, int maxAmmo, boolean reloading, float reloadProgress) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Weapon bob effect (reduced when aiming)
        float bobMultiplier = 1f - aimTransition * 0.9f;
        float bobX = MathUtils.sin(weaponBobTime) * 4f * bobMultiplier;
        float bobY = Math.abs(MathUtils.cos(weaponBobTime * 2f)) * 3f * bobMultiplier;

        // Recoil - gun kicks back toward player
        float recoilAmt = weaponRecoil * 12f * (1f - aimTransition * 0.5f);

        // Hip fire position (lower, slightly right)
        float hipX = width * 0.55f;
        float hipY = height * -0.15f;

        // ADS position (centered, raised up to current level)
        float adsX = width * 0.5f;
        float adsY = height * 0.08f;

        float baseX = MathUtils.lerp(hipX, adsX, aimTransition) + bobX;
        float baseY = MathUtils.lerp(hipY, adsY, aimTransition) + bobY - recoilAmt;

        // Calculate tilt toward crosshair (center of screen) when hip-firing
        // Tilt decreases as we aim down sights
        float tiltAmount = (1f - aimTransition) * -0.15f;  // Negative to tilt left toward center

        // Weapon switch animation - slide down
        baseY -= (1f - switchProgress) * height * 0.4f;

        // Reload animation
        if (reloading) {
            float dipAmount = MathUtils.sin(reloadProgress * MathUtils.PI) * 50f;
            baseY -= dipAmount;
        }

        // Don't render if mostly hidden
        if (switchProgress < 0.05f) return;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        float s = height / 550f;  // scale

        // Apply rotation for hip-fire tilt toward crosshair
        // Rotate around the base of the weapon
        float rotationDegrees = tiltAmount * 60f;  // Convert tilt to degrees
        shapeRenderer.identity();
        shapeRenderer.translate(baseX, baseY, 0);
        shapeRenderer.rotate(0, 0, 1, rotationDegrees);
        shapeRenderer.translate(-baseX, -baseY, 0);

        switch (type) {
            case PLASMA_RIFLE:
                renderPlasmaRifle(baseX, baseY, s, ammo, maxAmmo, type);
                break;
            case LASER_PISTOL:
                renderLaserPistol(baseX, baseY, s, ammo, maxAmmo, type);
                break;
            case SCATTER_GUN:
                renderScatterGun(baseX, baseY, s, ammo, maxAmmo, type);
                break;
            case RAIL_CANNON:
                renderRailCannon(baseX, baseY, s, ammo, maxAmmo, type);
                break;
            case PULSE_SMG:
                renderPulseSMG(baseX, baseY, s, ammo, maxAmmo, type);
                break;
        }

        // Reset transformation
        shapeRenderer.identity();

        shapeRenderer.end();
    }

    // ==================== PLASMA RIFLE (original weapon) ====================
    private void renderPlasmaRifle(float baseX, float baseY, float s, int ammo, int maxAmmo, WeaponType type) {
        renderArmsAndHands(baseX, baseY, s);

        // Stock
        shapeRenderer.setColor(0.24f, 0.22f, 0.2f, 1f);
        shapeRenderer.rect(baseX - 50*s, baseY - 50*s, 100*s, 60*s);
        shapeRenderer.setColor(0.28f, 0.26f, 0.24f, 1f);
        shapeRenderer.rect(baseX - 40*s, baseY - 40*s, 80*s, 45*s);

        // Grip
        shapeRenderer.setColor(0.18f, 0.18f, 0.2f, 1f);
        shapeRenderer.rect(baseX + 35*s, baseY - 30*s, 35*s, 45*s);

        // Lower receiver
        shapeRenderer.setColor(0.3f, 0.3f, 0.32f, 1f);
        shapeRenderer.rect(baseX - 45*s, baseY + 5*s, 90*s, 55*s);

        // Magazine well with glow
        shapeRenderer.setColor(0.22f, 0.22f, 0.24f, 1f);
        shapeRenderer.rect(baseX - 20*s, baseY + 20*s, 40*s, 35*s);
        renderEnergyCell(baseX, baseY, s, ammo, maxAmmo, type);

        // Upper receiver
        shapeRenderer.setColor(0.32f, 0.32f, 0.35f, 1f);
        shapeRenderer.rect(baseX - 38*s, baseY + 55*s, 76*s, 50*s);

        // Rail
        shapeRenderer.setColor(0.25f, 0.25f, 0.28f, 1f);
        shapeRenderer.rect(baseX - 25*s, baseY + 60*s, 50*s, 45*s);
        renderRailGrooves(baseX, baseY, s);

        // Handguard
        shapeRenderer.setColor(0.28f, 0.28f, 0.3f, 1f);
        float hgY = baseY + 100*s;
        shapeRenderer.triangle(baseX - 32*s, hgY, baseX + 32*s, hgY, baseX + 22*s, hgY + 70*s);
        shapeRenderer.triangle(baseX - 32*s, hgY, baseX + 22*s, hgY + 70*s, baseX - 22*s, hgY + 70*s);

        // Barrel
        shapeRenderer.setColor(0.4f, 0.4f, 0.42f, 1f);
        float bY = baseY + 165*s;
        shapeRenderer.triangle(baseX - 18*s, bY, baseX + 18*s, bY, baseX + 10*s, bY + 55*s);
        shapeRenderer.triangle(baseX - 18*s, bY, baseX + 10*s, bY + 55*s, baseX - 10*s, bY + 55*s);

        renderMuzzle(baseX, baseY + 225*s, s, type);
        renderIronSights(baseX, baseY, s);
        renderAmmoIndicator(baseX, baseY, s, ammo, maxAmmo, type);
    }

    // ==================== LASER PISTOL ====================
    private void renderLaserPistol(float baseX, float baseY, float s, int ammo, int maxAmmo, WeaponType type) {
        // Single hand grip
        shapeRenderer.setColor(0.72f, 0.56f, 0.45f, 1f);
        shapeRenderer.rect(baseX + 15*s, baseY - 50*s, 50*s, 40*s);

        shapeRenderer.setColor(0.2f, 0.2f, 0.22f, 1f);
        shapeRenderer.rect(baseX - 5*s, baseY - 20*s, 45*s, 40*s);

        // Compact grip
        shapeRenderer.setColor(0.22f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(baseX - 20*s, baseY - 30*s, 55*s, 45*s);

        // Frame
        shapeRenderer.setColor(0.35f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(baseX - 25*s, baseY + 10*s, 50*s, 40*s);

        // Energy cell
        renderEnergyCell(baseX - 5*s, baseY - 5*s, s * 0.7f, ammo, maxAmmo, type);

        // Slide
        shapeRenderer.setColor(0.4f, 0.35f, 0.35f, 1f);
        shapeRenderer.rect(baseX - 22*s, baseY + 45*s, 44*s, 70*s);

        // Barrel
        shapeRenderer.setColor(0.45f, 0.4f, 0.4f, 1f);
        shapeRenderer.rect(baseX - 10*s, baseY + 110*s, 20*s, 50*s);

        renderMuzzle(baseX, baseY + 160*s, s * 0.8f, type);
        renderAmmoIndicator(baseX + 20*s, baseY, s * 0.8f, ammo, maxAmmo, type);
    }

    // ==================== SCATTER GUN (Shotgun) ====================
    private void renderScatterGun(float baseX, float baseY, float s, int ammo, int maxAmmo, WeaponType type) {
        renderArmsAndHands(baseX, baseY, s);

        // Beefy stock
        shapeRenderer.setColor(0.35f, 0.25f, 0.15f, 1f);
        shapeRenderer.rect(baseX - 60*s, baseY - 55*s, 120*s, 70*s);
        shapeRenderer.setColor(0.4f, 0.3f, 0.2f, 1f);
        shapeRenderer.rect(baseX - 50*s, baseY - 45*s, 100*s, 55*s);

        // Grip
        shapeRenderer.setColor(0.3f, 0.22f, 0.14f, 1f);
        shapeRenderer.rect(baseX + 40*s, baseY - 35*s, 40*s, 50*s);

        // Receiver
        shapeRenderer.setColor(0.32f, 0.32f, 0.34f, 1f);
        shapeRenderer.rect(baseX - 50*s, baseY + 10*s, 100*s, 60*s);

        // Shell tube
        shapeRenderer.setColor(0.28f, 0.28f, 0.3f, 1f);
        shapeRenderer.rect(baseX - 22*s, baseY + 65*s, 44*s, 100*s);

        // Double barrel
        shapeRenderer.setColor(0.38f, 0.38f, 0.4f, 1f);
        shapeRenderer.rect(baseX - 25*s, baseY + 160*s, 22*s, 60*s);
        shapeRenderer.rect(baseX + 3*s, baseY + 160*s, 22*s, 60*s);

        // Muzzles
        float mY = baseY + 220*s;
        float glowPulse = 0.7f + 0.3f * MathUtils.sin(weaponBobTime * 3f);
        shapeRenderer.setColor(type.glowR * glowPulse * 0.3f, type.glowG * glowPulse * 0.3f, type.glowB * glowPulse * 0.3f, 1f);
        shapeRenderer.circle(baseX - 14*s, mY, 8*s);
        shapeRenderer.circle(baseX + 14*s, mY, 8*s);

        // Muzzle flash
        if (weaponRecoil > 0.5f) {
            shapeRenderer.setColor(type.glowR, type.glowG, type.glowB, weaponRecoil);
            shapeRenderer.circle(baseX - 14*s, mY + 8*s, 15*s * weaponRecoil);
            shapeRenderer.circle(baseX + 14*s, mY + 8*s, 15*s * weaponRecoil);
        }

        // Shell indicator
        for (int i = 0; i < maxAmmo; i++) {
            if (i < ammo) {
                shapeRenderer.setColor(type.glowR, type.glowG, type.glowB, 1f);
            } else {
                shapeRenderer.setColor(0.15f, 0.15f, 0.15f, 1f);
            }
            shapeRenderer.rect(baseX + 55*s, baseY + (10 + i * 14)*s, 8*s, 10*s);
        }
    }

    // ==================== RAIL CANNON ====================
    private void renderRailCannon(float baseX, float baseY, float s, int ammo, int maxAmmo, WeaponType type) {
        renderArmsAndHands(baseX, baseY, s);

        // Massive stock
        shapeRenderer.setColor(0.2f, 0.25f, 0.2f, 1f);
        shapeRenderer.rect(baseX - 70*s, baseY - 60*s, 140*s, 80*s);

        // Power pack
        shapeRenderer.setColor(0.15f, 0.2f, 0.15f, 1f);
        shapeRenderer.rect(baseX - 80*s, baseY - 20*s, 30*s, 60*s);
        float powerGlow = ammo / (float)maxAmmo;
        shapeRenderer.setColor(type.glowR * powerGlow, type.glowG * powerGlow, type.glowB * powerGlow, 1f);
        shapeRenderer.rect(baseX - 75*s, baseY - 10*s, 20*s, 40*s);

        // Main body
        shapeRenderer.setColor(0.25f, 0.3f, 0.25f, 1f);
        shapeRenderer.rect(baseX - 55*s, baseY + 15*s, 110*s, 70*s);

        // Charging rails (sides)
        shapeRenderer.setColor(0.35f, 0.4f, 0.35f, 1f);
        shapeRenderer.rect(baseX - 50*s, baseY + 80*s, 18*s, 130*s);
        shapeRenderer.rect(baseX + 32*s, baseY + 80*s, 18*s, 130*s);

        // Rail glow
        float railGlow = 0.5f + 0.5f * MathUtils.sin(weaponBobTime * 4f);
        shapeRenderer.setColor(type.glowR * railGlow * 0.4f, type.glowG * railGlow * 0.4f, type.glowB * railGlow * 0.4f, 1f);
        shapeRenderer.rect(baseX - 48*s, baseY + 85*s, 14*s, 120*s);
        shapeRenderer.rect(baseX + 34*s, baseY + 85*s, 14*s, 120*s);

        // Central barrel
        shapeRenderer.setColor(0.4f, 0.45f, 0.4f, 1f);
        shapeRenderer.rect(baseX - 15*s, baseY + 80*s, 30*s, 140*s);

        // Muzzle aperture
        float mY = baseY + 220*s;
        shapeRenderer.setColor(0.1f, 0.15f, 0.1f, 1f);
        shapeRenderer.circle(baseX, mY, 12*s);
        shapeRenderer.setColor(type.glowR * railGlow, type.glowG * railGlow, type.glowB * railGlow, 1f);
        shapeRenderer.circle(baseX, mY, 6*s);

        // Massive muzzle flash
        if (weaponRecoil > 0.3f) {
            shapeRenderer.setColor(type.glowR, type.glowG, type.glowB, weaponRecoil);
            shapeRenderer.circle(baseX, mY + 15*s, 35*s * weaponRecoil);
            shapeRenderer.setColor(1f, 1f, 1f, weaponRecoil * 0.7f);
            shapeRenderer.circle(baseX, mY + 20*s, 15*s * weaponRecoil);
        }

        // Charge indicator
        shapeRenderer.setColor(type.glowR, type.glowG, type.glowB, 1f);
        for (int i = 0; i < ammo; i++) {
            shapeRenderer.circle(baseX + 60*s, baseY + (30 + i * 25)*s, 6*s);
        }
    }

    // ==================== PULSE SMG ====================
    private void renderPulseSMG(float baseX, float baseY, float s, int ammo, int maxAmmo, WeaponType type) {
        renderArmsAndHands(baseX, baseY, s);

        // Compact stock
        shapeRenderer.setColor(0.25f, 0.2f, 0.28f, 1f);
        shapeRenderer.rect(baseX - 40*s, baseY - 45*s, 80*s, 50*s);

        // Grip
        shapeRenderer.setColor(0.2f, 0.18f, 0.22f, 1f);
        shapeRenderer.rect(baseX + 25*s, baseY - 30*s, 30*s, 40*s);

        // Receiver
        shapeRenderer.setColor(0.3f, 0.28f, 0.35f, 1f);
        shapeRenderer.rect(baseX - 35*s, baseY, 70*s, 45*s);

        // Extended magazine
        shapeRenderer.setColor(0.22f, 0.2f, 0.25f, 1f);
        shapeRenderer.rect(baseX - 15*s, baseY + 10*s, 30*s, 50*s);
        renderEnergyCell(baseX, baseY + 5*s, s * 0.9f, ammo, maxAmmo, type);

        // Upper
        shapeRenderer.setColor(0.35f, 0.32f, 0.4f, 1f);
        shapeRenderer.rect(baseX - 30*s, baseY + 40*s, 60*s, 40*s);

        // Cooling vents
        shapeRenderer.setColor(0.15f, 0.13f, 0.18f, 1f);
        for (int i = 0; i < 3; i++) {
            shapeRenderer.rect(baseX - 25*s, baseY + (50 + i * 12)*s, 50*s, 4*s);
        }

        // Compact barrel
        shapeRenderer.setColor(0.38f, 0.35f, 0.42f, 1f);
        shapeRenderer.rect(baseX - 12*s, baseY + 75*s, 24*s, 80*s);

        // Barrel shroud
        shapeRenderer.setColor(0.28f, 0.25f, 0.32f, 1f);
        shapeRenderer.rect(baseX - 18*s, baseY + 90*s, 36*s, 50*s);

        renderMuzzle(baseX, baseY + 155*s, s * 0.7f, type);
        renderAmmoIndicator(baseX + 35*s, baseY, s * 0.7f, ammo, maxAmmo, type);
    }

    // ==================== SHARED COMPONENTS ====================

    private void renderArmsAndHands(float baseX, float baseY, float s) {
        // Forearm (skin)
        shapeRenderer.setColor(0.72f, 0.56f, 0.45f, 1f);
        shapeRenderer.rect(baseX + 30*s, baseY - 70*s, 90*s, 55*s);

        // Hand on grip (glove)
        shapeRenderer.setColor(0.2f, 0.2f, 0.22f, 1f);
        shapeRenderer.rect(baseX - 10*s, baseY - 25*s, 60*s, 50*s);

        // Left hand on handguard
        shapeRenderer.rect(baseX - 35*s, baseY + 115*s, 35*s, 28*s);
    }

    private void renderEnergyCell(float baseX, float baseY, float s, int ammo, int maxAmmo, WeaponType type) {
        float cellGlow = (ammo / (float) maxAmmo) * 0.8f + 0.2f;
        shapeRenderer.setColor(type.glowR * cellGlow * 0.15f, type.glowG * cellGlow * 0.6f, type.glowB * cellGlow * 0.9f, 1f);
        shapeRenderer.rect(baseX - 15*s, baseY + 25*s, 30*s, 25*s);
    }

    private void renderRailGrooves(float baseX, float baseY, float s) {
        shapeRenderer.setColor(0.18f, 0.18f, 0.2f, 1f);
        for (int i = 0; i < 4; i++) {
            shapeRenderer.rect(baseX - 22*s, baseY + (65 + i*10)*s, 44*s, 2*s);
        }
    }

    private void renderMuzzle(float baseX, float muzzleY, float s, WeaponType type) {
        float glowPulse = 0.7f + 0.3f * MathUtils.sin(weaponBobTime * 3f);
        shapeRenderer.setColor(type.glowR * glowPulse * 0.3f, type.glowG * glowPulse * 0.3f, type.glowB * glowPulse * 0.3f, 1f);
        shapeRenderer.circle(baseX, muzzleY, 10*s);

        // Muzzle flash
        if (weaponRecoil > 0.5f) {
            shapeRenderer.setColor(type.glowR, type.glowG, type.glowB, weaponRecoil);
            shapeRenderer.circle(baseX, muzzleY + 8*s, 20*s * weaponRecoil);
            shapeRenderer.setColor(type.glowR * 1.2f, type.glowG * 1.2f, type.glowB * 1.2f, weaponRecoil * 0.6f);
            shapeRenderer.circle(baseX, muzzleY + 12*s, 12*s * weaponRecoil);
        }
    }

    private void renderIronSights(float baseX, float baseY, float s) {
        // Rear sight
        shapeRenderer.setColor(0.15f, 0.15f, 0.18f, 1f);
        shapeRenderer.rect(baseX - 28*s, baseY + 85*s, 8*s, 22*s);
        shapeRenderer.rect(baseX + 20*s, baseY + 85*s, 8*s, 22*s);
        shapeRenderer.rect(baseX - 28*s, baseY + 104*s, 56*s, 4*s);

        // Front sight
        shapeRenderer.setColor(0.18f, 0.18f, 0.2f, 1f);
        shapeRenderer.rect(baseX - 4*s, baseY + 195*s, 8*s, 18*s);

        // Glow dot
        float sightGlow = 0.8f + 0.2f * MathUtils.sin(weaponBobTime * 2f);
        shapeRenderer.setColor(0.1f * sightGlow, 0.9f * sightGlow, 0.3f * sightGlow, 1f);
        shapeRenderer.circle(baseX, baseY + 210*s, 3*s);

        // ADS alignment indicator
        if (aimTransition > 0.8f) {
            shapeRenderer.setColor(0f, 0.5f, 0.15f, 0.15f);
            shapeRenderer.circle(baseX, baseY + 150*s, 12*s);
        }
    }

    private void renderAmmoIndicator(float baseX, float baseY, float s, int ammo, int maxAmmo, WeaponType type) {
        if (aimTransition < 0.6f) {
            int bars = Math.min(8, maxAmmo);
            int lit = (int)((ammo / (float)maxAmmo) * bars);
            for (int i = 0; i < bars; i++) {
                if (i < lit) {
                    shapeRenderer.setColor(type.glowR * 0.5f + 0.3f, type.glowG * 0.5f + 0.3f, type.glowB * 0.2f, 1f);
                } else {
                    shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f);
                }
                shapeRenderer.rect(baseX + 48*s, baseY + (15 + i*12)*s, 6*s, 8*s);
            }
        }
    }

    public float getAimTransition() {
        return aimTransition;
    }

    public float getWeaponRecoil() {
        return weaponRecoil;
    }

    public boolean isSwitchComplete() {
        return switchProgress >= 0.95f;
    }

    public boolean isWeaponLowered() {
        return switchProgress <= 0.1f;
    }
}
