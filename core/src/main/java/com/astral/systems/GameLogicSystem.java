package com.astral.systems;

import com.astral.components.*;
import com.astral.ecs.Entity;
import com.astral.ecs.GameSystem;
import com.astral.ecs.World;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Game logic system - handles game rules, ship controls, AI, etc.
 */
public class GameLogicSystem extends GameSystem {

    private InputSystem inputSystem;

    // Reusable vectors
    private final Vector3 thrustVector = new Vector3();
    private final Vector3 torqueVector = new Vector3();
    private final Quaternion tempQuat = new Quaternion();
    private final com.badlogic.gdx.math.Matrix4 tempMatrix = new com.badlogic.gdx.math.Matrix4();

    public GameLogicSystem(World world) {
        super(world);
        setPriority(70);
    }

    @Override
    public void initialize() {
        // Get reference to input system
    }

    public void setInputSystem(InputSystem inputSystem) {
        this.inputSystem = inputSystem;
    }

    @Override
    public void update(float deltaTime) {
        // Update ships
        updateShips(deltaTime);

        // Update players
        updatePlayers(deltaTime);
    }

    private void updateShips(float deltaTime) {
        Array<Entity> ships = getEntitiesWith(ShipComponent.class, RigidBodyComponent.class, TransformComponent.class);

        for (Entity entity : ships) {
            ShipComponent ship = entity.get(ShipComponent.class);
            RigidBodyComponent rb = entity.get(RigidBodyComponent.class);
            TransformComponent transform = entity.get(TransformComponent.class);
            PlayerComponent player = entity.get(PlayerComponent.class);

            // Update shield regen
            ship.updateShields(deltaTime);

            // Only apply player input to player-controlled ships
            if (player != null && player.isLocalPlayer && inputSystem != null) {
                applyShipControls(ship, rb, transform, deltaTime);
            }

            // Consume fuel
            if (ship.enginesOn && ship.fuel > 0) {
                float fuelUse = ship.fuelConsumption * deltaTime;
                if (ship.boostActive) fuelUse *= 2f;
                ship.fuel = Math.max(0, ship.fuel - fuelUse);
            }
        }
    }

    private void applyShipControls(ShipComponent ship, RigidBodyComponent rb, TransformComponent transform, float deltaTime) {
        if (!ship.enginesOn) return;

        thrustVector.setZero();
        ship.boostActive = inputSystem.boost;
        float thrustMod = inputSystem.boost ? ship.boostMultiplier : 1f;

        // Direct rotation from mouse input (immediate response)
        float pitchAmount = inputSystem.pitch * ship.pitchRate * 50f;
        float yawAmount = inputSystem.yaw * ship.yawRate * 50f;
        float rollAmount = inputSystem.roll * ship.rollRate * deltaTime * 100f;

        // Apply rotation directly to transform
        if (Math.abs(pitchAmount) > 0.0001f || Math.abs(yawAmount) > 0.0001f || Math.abs(rollAmount) > 0.0001f) {
            // Get current orientation axes
            Vector3 right = new Vector3(1, 0, 0).mul(transform.rotation);
            Vector3 up = new Vector3(0, 1, 0).mul(transform.rotation);
            Vector3 forward = new Vector3(0, 0, -1).mul(transform.rotation);

            // Apply rotations
            tempQuat.setFromAxis(right, pitchAmount);
            transform.rotation.mul(tempQuat);

            tempQuat.setFromAxis(Vector3.Y, yawAmount);
            transform.rotation.mul(tempQuat);

            tempQuat.setFromAxis(forward, rollAmount);
            transform.rotation.mul(tempQuat);

            transform.rotation.nor();
            transform.dirty = true;

            // Sync physics body transform (position + rotation)
            if (rb.body != null) {
                tempMatrix.set(transform.position, transform.rotation, transform.scale);
                rb.body.setWorldTransform(tempMatrix);
            }
        }

        // Calculate thrust in local space
        if (inputSystem.throttle > 0) {
            thrustVector.z = -ship.mainThrust * inputSystem.throttle * thrustMod;
        } else if (inputSystem.throttle < 0) {
            thrustVector.z = ship.mainThrust * 0.5f * -inputSystem.throttle;
        }

        thrustVector.x = ship.maneuverThrust * inputSystem.strafe;
        thrustVector.y = ship.maneuverThrust * inputSystem.vertical;

        // Transform thrust to world space and apply
        if (thrustVector.len2() > 0 && rb.body != null) {
            thrustVector.mul(transform.rotation);
            rb.applyForce(thrustVector);
        }

        // Brake
        if (inputSystem.brake && rb.body != null) {
            Vector3 velocity = rb.getLinearVelocity();
            Vector3 brakeForce = velocity.cpy().scl(-ship.mass * 2f);
            rb.applyForce(brakeForce);
        }

        // Apply damping
        if (rb.body != null) {
            rb.setDamping(ship.linearDamping, ship.angularDamping);
        }
    }

    private void updatePlayers(float deltaTime) {
        Array<Entity> players = getEntitiesWith(PlayerComponent.class);

        for (Entity entity : players) {
            PlayerComponent player = entity.get(PlayerComponent.class);

            // Regen stamina
            if (player.stamina < player.maxStamina) {
                player.stamina = Math.min(player.maxStamina, player.stamina + 10f * deltaTime);
            }

            // Consume oxygen in space (if not in ship)
            if (player.state == PlayerComponent.PlayerState.FPS) {
                // TODO: Check if in atmosphere
                player.oxygen = Math.max(0, player.oxygen - 1f * deltaTime);

                if (player.oxygen <= 0) {
                    player.damage(5f * deltaTime); // Suffocation damage
                }
            }
        }
    }
}
