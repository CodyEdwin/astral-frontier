package com.astral.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * Renders procedural weapon models
 */
public class ProceduralWeaponRenderer implements Disposable {

    private Model weaponModel;
    private ModelInstance weaponInstance;

    // Current state
    private boolean initialized = false;

    // Weapon positioning (FPS view)
    private final Vector3 weaponOffset = new Vector3(0.08f, -0.06f, 0.15f);  // Right, down, forward
    private final Vector3 weaponScale = new Vector3(0.0004f, 0.0004f, 0.0004f);  // Scale down the model significantly
    private final Quaternion weaponRotation = new Quaternion();

    // Aim offset
    private final Vector3 aimOffset = new Vector3(0f, -0.05f, 0.12f);  // Center when aiming
    private float aimTransition = 0f;

    // Weapon bob
    private float bobTime = 0f;
    private float bobAmount = 0.01f;

    // Fire recoil
    private float recoilAmount = 0f;
    private float recoilRecovery = 15f;

    public ProceduralWeaponRenderer() {
    }

    /**
     * Initialize the procedural weapon model
     */
    public void initialize(Camera camera) {
        if (initialized) return;

        try {
            // Build procedural weapon model
            ModelBuilder modelBuilder = new ModelBuilder();
            modelBuilder.begin();

            // Create weapon parts: barrel, body, stock
            // Barrel
            modelBuilder.part("barrel", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.DARK_GRAY)))
                .box(0, 0, 0, 0.02f, 0.02f, 0.3f);

            // Body
            modelBuilder.part("body", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.GRAY)))
                .box(0, -0.05f, 0.1f, 0.05f, 0.1f, 0.2f);

            // Stock
            modelBuilder.part("stock", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal,
                new Material(ColorAttribute.createDiffuse(Color.BROWN)))
                .box(0, -0.1f, -0.1f, 0.03f, 0.15f, 0.08f);

            weaponModel = modelBuilder.end();
            weaponInstance = new ModelInstance(weaponModel);

            initialized = true;
            Gdx.app.log("ProceduralWeaponRenderer", "Initialized procedural weapon model");

        } catch (Exception e) {
            Gdx.app.error("ProceduralWeaponRenderer", "Failed to create procedural model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update positioning
     */
    public void update(float delta, Camera camera, boolean isMoving, boolean isAiming, boolean justFired, boolean isReloading) {
        if (!initialized || weaponInstance == null) return;

        // Recoil recovery
        recoilAmount = Math.max(0, recoilAmount - recoilRecovery * delta);

        // Aim transition
        float targetAim = isAiming ? 1f : 0f;
        aimTransition += (targetAim - aimTransition) * delta * 10f;

        // Weapon bob when moving
        if (isMoving && !isAiming) {
            bobTime += delta * 8f;
        } else {
            bobTime += delta * 2f;  // Subtle breathing motion
        }

        // Update weapon transform
        updateWeaponTransform(camera, isMoving);
    }

    private void updateWeaponTransform(Camera camera, boolean isMoving) {
        if (weaponInstance == null) return;

        // Calculate base offset (lerp between hip and aim)
        Vector3 offset = new Vector3();
        offset.set(weaponOffset).lerp(aimOffset, aimTransition);

        // Add weapon bob
        float bobX = (float) Math.sin(bobTime) * bobAmount * (1f - aimTransition);
        float bobY = (float) Math.sin(bobTime * 2f) * bobAmount * 0.5f * (1f - aimTransition);
        offset.add(bobX, bobY, 0);

        // Add recoil
        offset.z += recoilAmount;
        offset.y += recoilAmount * 0.5f;

        // Transform offset to world space relative to camera
        Vector3 right = new Vector3(camera.direction).crs(camera.up).nor();
        Vector3 up = new Vector3(camera.up);
        Vector3 forward = new Vector3(camera.direction);

        Vector3 worldPos = new Vector3(camera.position);
        worldPos.add(right.scl(offset.x));
        worldPos.add(up.scl(offset.y));
        worldPos.add(forward.nor().scl(offset.z));  // Positive Z is forward

        // Create rotation to face camera direction
        Matrix4 transform = weaponInstance.transform;
        transform.idt();
        transform.translate(worldPos);

        // Rotate to align with camera
        Vector3 camDir = new Vector3(camera.direction);
        float yaw = (float) Math.atan2(camDir.x, camDir.z) * (180f / (float) Math.PI);
        float pitch = (float) Math.asin(-camDir.y) * (180f / (float) Math.PI);

        transform.rotate(Vector3.Y, yaw + 180f);  // Face forward
        transform.rotate(Vector3.X, -pitch);

        // Apply scale
        transform.scale(weaponScale.x, weaponScale.y, weaponScale.z);
    }

    /**
     * Render the weapon
     */
    public void render() {
        // No-op for procedural, use render(ModelBatch, Environment)
    }

    /**
     * Render with a custom model batch (for depth sorting with other objects)
     */
    public void render(ModelBatch batch, Environment environment) {
        if (!initialized || weaponInstance == null) return;

        batch.render(weaponInstance, environment);
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void dispose() {
        if (weaponModel != null) {
            weaponModel.dispose();
            weaponModel = null;
        }
        if (weaponInstance != null) {
            weaponInstance = null;
        }
        initialized = false;
    }
}