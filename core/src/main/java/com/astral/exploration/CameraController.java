package com.astral.exploration;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Handles camera positioning, FOV, and look direction
 */
public class CameraController {

    private PerspectiveCamera camera;

    private float normalFOV = 75f;
    private float aimFOV = 45f;
    private float currentFOV = 75f;

    public CameraController() {
    }

    public void initialize(int width, int height) {
        camera = new PerspectiveCamera(normalFOV, width, height);
        camera.near = 0.1f;
        camera.far = 5000f;
    }

    public void update(PlayerController player) {
        if (camera == null) return;

        // Position camera at eye level
        Vector3 playerPos = player.getPosition();
        camera.position.set(
            playerPos.x,
            playerPos.y - player.getPlayerHeight() + player.getEyeHeight(),
            playerPos.z
        );

        // Calculate look direction from yaw/pitch
        float pitchRad = player.getCameraPitch() * MathUtils.degreesToRadians;
        float yawRad = player.getCameraYaw() * MathUtils.degreesToRadians;

        camera.direction.set(
            MathUtils.sin(yawRad) * MathUtils.cos(pitchRad),
            MathUtils.sin(pitchRad),
            -MathUtils.cos(yawRad) * MathUtils.cos(pitchRad)
        ).nor();

        camera.up.set(Vector3.Y);
        camera.fieldOfView = currentFOV;
        camera.update();
    }

    public void setAimTransition(float aimTransition) {
        currentFOV = MathUtils.lerp(normalFOV, aimFOV, aimTransition);
    }

    public void resize(int width, int height) {
        if (camera != null) {
            camera.viewportWidth = width;
            camera.viewportHeight = height;
            camera.update();
        }
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }
}
