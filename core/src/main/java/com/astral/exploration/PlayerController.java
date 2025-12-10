package com.astral.exploration;

import com.astral.procedural.PlanetSurface;
import com.astral.systems.InputSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

/**
 * Handles player movement, jumping, jetpack, and collision.
 * Implements IPlayerController for standardized player behavior.
 */
public class PlayerController implements IPlayerController {

    // Player state
    private final Vector3 position = new Vector3(0, 10, 0);
    private final Vector3 velocity = new Vector3();
    private boolean isGrounded = false;

    // Dimensions
    private float playerHeight = 1.8f;
    private float eyeHeight = 1.6f;
    private float playerRadius = 0.5f;

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
    private float jetpackFuelConsumption = 2f;
    private float jetpackFuelRecharge = 15f;
    private float lastSpacePress = 0f;
    private float doubleTapThreshold = 0.3f;
    private boolean spaceWasPressed = false;

    // Camera rotation (controlled by input, read by camera controller)
    private float cameraYaw = 0f;
    private float cameraPitch = 0f;

    public PlayerController() {
    }

    @Override
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
    }

    public void update(float delta, InputSystem input, PlanetSurface surface) {
        handleJetpackToggle(input);
        rechargeJetpack(delta);
        handleMovement(delta, input, surface);
        handleCameraLook(input);
    }

    private void handleJetpackToggle(InputSystem input) {
        boolean spacePressed = input.jump;
        if (spacePressed && !spaceWasPressed) {
            float currentTime = System.nanoTime() / 1_000_000_000f;
            if (currentTime - lastSpacePress < doubleTapThreshold) {
                jetpackActive = !jetpackActive;
                Gdx.app.log("Jetpack", jetpackActive ? "ACTIVATED" : "DEACTIVATED");
                lastSpacePress = 0;
            } else {
                lastSpacePress = currentTime;
            }
        }
        spaceWasPressed = spacePressed;
    }

    private void rechargeJetpack(float delta) {
        if (isGrounded && jetpackFuel < jetpackMaxFuel) {
            jetpackFuel = Math.min(jetpackMaxFuel, jetpackFuel + jetpackFuelRecharge * delta);
        }
    }

    private void handleMovement(float delta, InputSystem input, PlanetSurface surface) {
        // Get movement input
        Vector3 moveDir = new Vector3();
        if (input.forward) moveDir.z -= 1;
        if (input.backward) moveDir.z += 1;
        if (input.left) moveDir.x -= 1;
        if (input.right) moveDir.x += 1;

        // Normalize and rotate by camera yaw
        if (moveDir.len2() > 0) {
            moveDir.nor();
            moveDir.rotate(Vector3.Y, -cameraYaw);
        }

        // Apply speed
        float speed = input.sprint ? sprintSpeed : walkSpeed;
        if (jetpackActive && !isGrounded) speed *= 1.5f;
        moveDir.scl(speed);

        // Set horizontal velocity
        velocity.x = moveDir.x;
        velocity.z = moveDir.z;

        // Apply gravity
        float effectiveGravity = jetpackActive ? gravity * 0.3f : gravity;
        velocity.y += effectiveGravity * delta;
        velocity.y = Math.max(velocity.y, -50f);

        // Jetpack thrust or normal jump
        if (input.jump) {
            if (jetpackActive && jetpackFuel > 0) {
                velocity.y = jetpackThrust;
                jetpackFuel -= jetpackFuelConsumption * delta;
                jetpackFuel = Math.max(0, jetpackFuel);
                isGrounded = false;
            } else if (isGrounded) {
                velocity.y = jumpVelocity;
                isGrounded = false;
            }
        }

        // Calculate new position with collision
        float newX = position.x + velocity.x * delta;
        float newZ = position.z + velocity.z * delta;

        // Structure collision - slide along walls
        if (!surface.checkStructureCollision(newX, newZ, playerRadius)) {
            position.x = newX;
            position.z = newZ;
        } else {
            if (!surface.checkStructureCollision(newX, position.z, playerRadius)) {
                position.x = newX;
            } else if (!surface.checkStructureCollision(position.x, newZ, playerRadius)) {
                position.z = newZ;
            }
        }

        // Apply vertical movement
        position.y += velocity.y * delta;

        // Ground collision
        float terrainHeight = surface.getHeightAt(position.x, position.z);
        float groundLevel = terrainHeight + playerHeight;

        if (position.y <= groundLevel) {
            position.y = groundLevel;
            velocity.y = 0;
            isGrounded = true;
        } else {
            isGrounded = false;
        }
    }

    private void handleCameraLook(InputSystem input) {
        float sensitivity = 15f;
        cameraYaw -= input.yaw * sensitivity;
        cameraPitch += input.pitch * sensitivity;
        cameraPitch = Math.max(-89f, Math.min(89f, cameraPitch));
    }

    // Getters - IPlayerController implementation
    @Override
    public Vector3 getPosition() { return position; }

    @Override
    public Vector3 getVelocity() { return velocity; }

    @Override
    public boolean isGrounded() { return isGrounded; }

    public boolean isJetpackActive() { return jetpackActive; }
    public float getJetpackFuel() { return jetpackFuel; }
    public float getJetpackMaxFuel() { return jetpackMaxFuel; }

    @Override
    public float getCameraYaw() { return cameraYaw; }

    @Override
    public float getCameraPitch() { return cameraPitch; }

    @Override
    public float getPlayerHeight() { return playerHeight; }

    @Override
    public float getEyeHeight() { return eyeHeight; }
}
