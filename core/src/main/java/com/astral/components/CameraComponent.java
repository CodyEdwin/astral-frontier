package com.astral.components;

import com.astral.ecs.Component;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;

/**
 * Camera component for entities that have a view
 */
public class CameraComponent implements Component {

    public PerspectiveCamera camera;
    public CameraMode mode = CameraMode.COCKPIT;

    // Camera settings
    public float fieldOfView = 75f;
    public float nearClip = 0.1f;
    public float farClip = 100000f;

    // Third-person settings
    public float thirdPersonDistance = 20f;
    public float thirdPersonHeight = 5f;

    // Offset from entity position
    public final Vector3 offset = new Vector3();

    // Mouse look sensitivity
    public float sensitivity = 0.3f;

    // Current look angles
    public float yaw = 0f;
    public float pitch = 0f;

    // Shake effect
    public float shakeIntensity = 0f;
    public float shakeDuration = 0f;

    public enum CameraMode {
        COCKPIT,        // First-person in cockpit
        CHASE,          // Third-person behind
        ORBIT,          // Free orbit around ship
        FPS,            // First-person on foot
        CINEMATIC       // Cutscene camera
    }

    public void initialize(int width, int height) {
        camera = new PerspectiveCamera(fieldOfView, width, height);
        camera.near = nearClip;
        camera.far = farClip;
        camera.update();
    }

    public void resize(int width, int height) {
        if (camera != null) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }

    public void shake(float intensity, float duration) {
        this.shakeIntensity = intensity;
        this.shakeDuration = duration;
    }

    @Override
    public void reset() {
        camera = null;
        mode = CameraMode.COCKPIT;
        fieldOfView = 75f;
        nearClip = 0.1f;
        farClip = 100000f;
        thirdPersonDistance = 20f;
        thirdPersonHeight = 5f;
        offset.setZero();
        sensitivity = 0.3f;
        yaw = 0f;
        pitch = 0f;
        shakeIntensity = 0f;
        shakeDuration = 0f;
    }
}
