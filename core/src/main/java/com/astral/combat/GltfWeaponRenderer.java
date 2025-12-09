package com.astral.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.animation.AnimationControllerHack;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;

/**
 * Renders GLTF weapon models with animations
 */
public class GltfWeaponRenderer implements Disposable {

    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Scene scene;
    private AnimationControllerHack animationController;

    // Animation names from the SCAR model
    private static final String ANIM_IDLE = "Akito Rig|Idle";
    private static final String ANIM_SHOOT = "Akito Rig|Shoot";
    private static final String ANIM_RELOAD = "Akito Rig|Reload";
    private static final String ANIM_TAKE = "Akito Rig|Take";

    // Current state
    private String currentAnimation = ANIM_IDLE;
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

    public GltfWeaponRenderer() {
    }

    /**
     * Initialize the GLTF weapon model
     */
    public void initialize(Camera camera) {
        if (initialized) return;

        try {
            // Create scene manager with enough bones for the SCAR model (120 bones)
            sceneManager = new SceneManager(128);  // maxBones = 128

            // Set up lighting
            Environment env = new Environment();
            env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.6f, 0.6f, 0.6f, 1f));
            env.add(new DirectionalLight().set(0.9f, 0.9f, 0.9f, -1f, -0.8f, -0.2f));
            sceneManager.setAmbientLight(0.6f);

            // Load GLTF model
            sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/animated_scar/scene.gltf"));
            scene = new Scene(sceneAsset.scene);
            sceneManager.addScene(scene);

            // Set up animation controller
            if (scene.animationController != null) {
                animationController = new AnimationControllerHack(scene.modelInstance);
                animationController.setAnimation(ANIM_IDLE, -1);  // Loop idle
            }

            sceneManager.setCamera(camera);

            initialized = true;
            Gdx.app.log("GltfWeaponRenderer", "Initialized SCAR model with animations");

        } catch (Exception e) {
            Gdx.app.error("GltfWeaponRenderer", "Failed to load GLTF model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Update animation and positioning
     */
    public void update(float delta, Camera camera, boolean isMoving, boolean isAiming, boolean justFired, boolean isReloading) {
        if (!initialized || scene == null) return;

        // Update animation controller
        if (animationController != null) {
            animationController.update(delta);
        }

        // Handle animations
        if (justFired) {
            playAnimation(ANIM_SHOOT, 1);
            recoilAmount = 0.03f;
        } else if (isReloading && !currentAnimation.equals(ANIM_RELOAD)) {
            playAnimation(ANIM_RELOAD, 1);
        } else if (!isReloading && currentAnimation.equals(ANIM_RELOAD)) {
            playAnimation(ANIM_IDLE, -1);
        }

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

        // Update scene manager
        sceneManager.update(delta);
    }

    private void playAnimation(String animName, int loopCount) {
        if (animationController != null && !currentAnimation.equals(animName)) {
            animationController.setAnimation(animName, loopCount, 1f, new AnimationControllerHack.AnimationListener() {
                @Override
                public void onEnd(AnimationControllerHack.AnimationDesc animation) {
                    // Return to idle after one-shot animations
                    if (loopCount > 0) {
                        animationController.setAnimation(ANIM_IDLE, -1);
                        currentAnimation = ANIM_IDLE;
                    }
                }

                @Override
                public void onLoop(AnimationControllerHack.AnimationDesc animation) {
                }
            });
            currentAnimation = animName;
        }
    }

    private void updateWeaponTransform(Camera camera, boolean isMoving) {
        if (scene == null || scene.modelInstance == null) return;

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
        Matrix4 transform = scene.modelInstance.transform;
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
        if (!initialized || sceneManager == null) return;

        // Render the scene
        sceneManager.render();
    }

    /**
     * Render with a custom model batch (for depth sorting with other objects)
     */
    public void render(ModelBatch batch, Environment environment) {
        if (!initialized || scene == null) return;

        batch.render(scene.modelInstance, environment);
    }

    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void dispose() {
        if (sceneManager != null) {
            sceneManager.dispose();
            sceneManager = null;
        }
        if (sceneAsset != null) {
            sceneAsset.dispose();
            sceneAsset = null;
        }
        initialized = false;
    }
}
