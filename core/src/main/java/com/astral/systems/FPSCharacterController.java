package com.astral.systems;

import com.astral.components.*;
import com.astral.ecs.Entity;
import com.astral.ecs.GameSystem;
import com.astral.ecs.World;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btKinematicCharacterController;

/**
 * FPS character controller for on-foot gameplay
 */
public class FPSCharacterController extends GameSystem {

    // Character parameters
    private static final float CHARACTER_HEIGHT = 1.8f;
    private static final float CHARACTER_RADIUS = 0.4f;
    private static final float STEP_HEIGHT = 0.35f;
    private static final float WALK_SPEED = 5f;      // m/s
    private static final float SPRINT_SPEED = 8f;    // m/s
    private static final float JUMP_VELOCITY = 6f;   // m/s
    private static final float GRAVITY = -9.81f;     // m/s²

    private InputSystem inputSystem;

    // Reusable vectors
    private final Vector3 walkDirection = new Vector3();
    private final Vector3 tempVector = new Vector3();

    public FPSCharacterController(World world) {
        super(world);
        setPriority(75);
    }

    public void setInputSystem(InputSystem inputSystem) {
        this.inputSystem = inputSystem;
    }

    @Override
    public void update(float deltaTime) {
        if (inputSystem == null) return;

        // Find player entities in FPS mode
        for (Entity entity : getEntitiesWith(PlayerComponent.class, TransformComponent.class)) {
            PlayerComponent player = entity.get(PlayerComponent.class);

            if (player.isLocalPlayer && player.state == PlayerComponent.PlayerState.FPS) {
                updateCharacter(entity, player, deltaTime);
            }
        }
    }

    private void updateCharacter(Entity entity, PlayerComponent player, float deltaTime) {
        TransformComponent transform = entity.get(TransformComponent.class);
        CameraComponent camera = entity.get(CameraComponent.class);

        if (transform == null) return;

        // Calculate movement speed
        float speed = inputSystem.sprint ? SPRINT_SPEED : WALK_SPEED;

        // Consume stamina when sprinting
        if (inputSystem.sprint && player.stamina > 0) {
            player.stamina -= 20f * deltaTime;
        }

        // Calculate movement direction from input
        walkDirection.setZero();

        if (inputSystem.forward) walkDirection.z -= 1;
        if (inputSystem.backward) walkDirection.z += 1;
        if (inputSystem.left) walkDirection.x -= 1;
        if (inputSystem.right) walkDirection.x += 1;

        // Normalize and apply speed
        if (walkDirection.len2() > 0) {
            walkDirection.nor().scl(speed * deltaTime);
        }

        // Rotate movement by camera yaw
        if (camera != null) {
            walkDirection.rotate(Vector3.Y, camera.yaw);
        }

        // Apply movement to transform (simplified - no physics)
        transform.translate(walkDirection);

        // Update camera look from mouse input
        if (camera != null) {
            camera.yaw -= inputSystem.yaw;
            camera.pitch -= inputSystem.pitch;

            // Clamp pitch
            camera.pitch = Math.max(-89f, Math.min(89f, camera.pitch));
        }

        // Handle jumping (simplified)
        if (inputSystem.jump) {
            // TODO: Implement proper jumping with physics
        }

        // Update player health/oxygen
        updatePlayerState(player, deltaTime);
    }

    private void updatePlayerState(PlayerComponent player, float deltaTime) {
        // Regen stamina when not sprinting
        if (!inputSystem.sprint && player.stamina < player.maxStamina) {
            player.stamina = Math.min(player.maxStamina, player.stamina + 15f * deltaTime);
        }

        // Natural health regen (slow)
        if (player.health < player.maxHealth && player.health > 0) {
            player.health = Math.min(player.maxHealth, player.health + 1f * deltaTime);
        }
    }

    /**
     * Create character collision shapes for FPS mode
     */
    public void createCharacterCollision(Entity entity, btCollisionWorld collisionWorld) {
        // Create capsule shape
        btCapsuleShape capsuleShape = new btCapsuleShape(CHARACTER_RADIUS, CHARACTER_HEIGHT - 2 * CHARACTER_RADIUS);

        // Create ghost object for character controller
        btPairCachingGhostObject ghostObject = new btPairCachingGhostObject();
        ghostObject.setCollisionShape(capsuleShape);
        ghostObject.setCollisionFlags(btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);

        // Create kinematic character controller
        btKinematicCharacterController characterController = new btKinematicCharacterController(
                ghostObject,
                capsuleShape,
                STEP_HEIGHT
        );

        characterController.setGravity(new Vector3(0, GRAVITY, 0));
        characterController.setJumpSpeed(JUMP_VELOCITY);

        // Store in component
        CharacterPhysicsComponent physics = new CharacterPhysicsComponent();
        physics.ghostObject = ghostObject;
        physics.characterController = characterController;
        physics.capsuleShape = capsuleShape;
        entity.add(physics);
    }

    /**
     * Component to hold FPS character physics objects
     */
    public static class CharacterPhysicsComponent implements com.astral.ecs.Component {
        public btPairCachingGhostObject ghostObject;
        public btKinematicCharacterController characterController;
        public btCapsuleShape capsuleShape;

        public boolean isGrounded() {
            return characterController != null && characterController.onGround();
        }

        @Override
        public void reset() {
            if (characterController != null) characterController.dispose();
            if (ghostObject != null) ghostObject.dispose();
            if (capsuleShape != null) capsuleShape.dispose();
            characterController = null;
            ghostObject = null;
            capsuleShape = null;
        }
    }
}
