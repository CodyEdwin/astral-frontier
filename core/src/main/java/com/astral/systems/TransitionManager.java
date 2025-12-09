package com.astral.systems;

import com.astral.components.*;
import com.astral.ecs.Entity;
import com.astral.ecs.GameSystem;
import com.astral.ecs.World;
import com.astral.procedural.Planet;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

/**
 * Manages transitions between space flight and planetary exploration
 */
public class TransitionManager extends GameSystem {

    private static final float TRANSITION_START_ALTITUDE = 500f;  // meters
    private static final float ATMOSPHERE_ENTRY_ALTITUDE = 100000f;
    private static final float LANDING_SPEED_THRESHOLD = 10f; // m/s

    private TransitionState currentState = TransitionState.SPACE;
    private float transitionProgress = 0f;

    // Current planet reference (if any)
    private Planet nearestPlanet;
    private Vector3 planetCenter = new Vector3();
    private float planetRadius = 1000f;

    private PhysicsSystem physicsSystem;

    public enum TransitionState {
        SPACE,              // Full space physics
        ATMOSPHERE_ENTRY,   // Entering atmosphere
        PLANETARY,          // On planet surface
        TAKEOFF,            // Leaving planet
        FPS_TRANSITION,     // Switching to/from FPS mode
        DOCKED              // Docked at station
    }

    public TransitionManager(World world) {
        super(world);
        setPriority(85);
    }

    public void setPhysicsSystem(PhysicsSystem physicsSystem) {
        this.physicsSystem = physicsSystem;
    }

    @Override
    public void update(float deltaTime) {
        // Find player ship
        Entity playerShip = findPlayerShip();
        if (playerShip == null) return;

        ShipComponent ship = playerShip.get(ShipComponent.class);
        TransformComponent transform = playerShip.get(TransformComponent.class);
        RigidBodyComponent rb = playerShip.get(RigidBodyComponent.class);
        PlayerComponent player = playerShip.get(PlayerComponent.class);

        if (ship == null || transform == null) return;

        // Calculate altitude from nearest planet (simplified)
        float altitude = calculateAltitude(transform.position);

        switch (currentState) {
            case SPACE -> {
                if (altitude < ATMOSPHERE_ENTRY_ALTITUDE && nearestPlanet != null) {
                    beginAtmosphereEntry(playerShip);
                }
            }

            case ATMOSPHERE_ENTRY -> {
                transitionProgress = 1f - (altitude / ATMOSPHERE_ENTRY_ALTITUDE);
                updateAtmosphereEntry(playerShip, transitionProgress, deltaTime);

                if (altitude < TRANSITION_START_ALTITUDE) {
                    currentState = TransitionState.PLANETARY;
                    completeLanding(playerShip);
                }

                if (altitude > ATMOSPHERE_ENTRY_ALTITUDE * 1.1f) {
                    currentState = TransitionState.SPACE;
                    exitToSpace(playerShip);
                }
            }

            case PLANETARY -> {
                // Apply planetary gravity
                if (rb != null && physicsSystem != null) {
                    Vector3 gravityDir = planetCenter.cpy().sub(transform.position).nor();
                    Vector3 gravity = gravityDir.scl(9.81f * ship.mass);
                    rb.applyForce(gravity);
                }

                // Check for takeoff
                float speed = rb != null ? rb.getLinearVelocity().len() : 0;
                if (ship.enginesOn && !ship.isLanded && altitude > TRANSITION_START_ALTITUDE) {
                    currentState = TransitionState.TAKEOFF;
                }

                // Check for landing
                if (!ship.isLanded && altitude < 5f && speed < LANDING_SPEED_THRESHOLD) {
                    performLanding(ship);
                }
            }

            case TAKEOFF -> {
                if (altitude > ATMOSPHERE_ENTRY_ALTITUDE) {
                    currentState = TransitionState.SPACE;
                    exitToSpace(playerShip);
                } else {
                    // Gradual physics transition
                    float t = altitude / ATMOSPHERE_ENTRY_ALTITUDE;
                    updateTakeoff(playerShip, t, deltaTime);
                }
            }
        }
    }

    private Entity findPlayerShip() {
        for (Entity entity : getEntitiesWith(PlayerComponent.class, ShipComponent.class)) {
            PlayerComponent player = entity.get(PlayerComponent.class);
            if (player.isLocalPlayer && player.state == PlayerComponent.PlayerState.SHIP) {
                return entity;
            }
        }
        return null;
    }

    private float calculateAltitude(Vector3 position) {
        if (nearestPlanet == null) {
            return Float.MAX_VALUE;
        }
        return position.dst(planetCenter) - planetRadius;
    }

    private void beginAtmosphereEntry(Entity ship) {
        currentState = TransitionState.ATMOSPHERE_ENTRY;
        transitionProgress = 0f;

        Gdx.app.log("TransitionManager", "Beginning atmosphere entry");

        // TODO: Start atmospheric effects (glow, shake)
    }

    private void updateAtmosphereEntry(Entity ship, float progress, float deltaTime) {
        ShipComponent shipComp = ship.get(ShipComponent.class);
        RigidBodyComponent rb = ship.get(RigidBodyComponent.class);

        if (rb != null) {
            // Interpolate drag (more drag as we go deeper)
            float drag = MathUtils.lerp(0.1f, 0.5f, progress);
            rb.setDamping(drag, shipComp.angularDamping);

            // Apply gravity gradient
            float gravity = MathUtils.lerp(0f, 9.81f, progress);
            Vector3 gravityDir = planetCenter.cpy().sub(ship.get(TransformComponent.class).position).nor();
            rb.applyForce(gravityDir.scl(gravity * shipComp.mass));
        }

        // TODO: Increase atmospheric effects based on progress
    }

    private void completeLanding(Entity ship) {
        Gdx.app.log("TransitionManager", "Entered planetary flight mode");

        // Enable high-detail terrain streaming
        // TODO: TerrainStreamingSystem.enableHighDetail()

        // Activate landing gear
        ShipComponent shipComp = ship.get(ShipComponent.class);
        shipComp.landingGearDeployed = true;
    }

    private void performLanding(ShipComponent ship) {
        ship.isLanded = true;
        ship.landingGearDeployed = true;
        Gdx.app.log("TransitionManager", "Ship landed");

        // TODO: Play landing sound, disable engines
    }

    private void updateTakeoff(Entity ship, float progress, float deltaTime) {
        RigidBodyComponent rb = ship.get(RigidBodyComponent.class);
        ShipComponent shipComp = ship.get(ShipComponent.class);

        if (rb != null) {
            // Reduce drag as we ascend
            float drag = MathUtils.lerp(0.5f, 0.1f, progress);
            rb.setDamping(drag, shipComp.angularDamping);

            // Reduce gravity as we ascend
            float gravity = MathUtils.lerp(9.81f, 0f, progress);
            Vector3 gravityDir = planetCenter.cpy().sub(ship.get(TransformComponent.class).position).nor();
            rb.applyForce(gravityDir.scl(gravity * shipComp.mass));
        }
    }

    private void exitToSpace(Entity ship) {
        Gdx.app.log("TransitionManager", "Exited to space");

        ShipComponent shipComp = ship.get(ShipComponent.class);
        RigidBodyComponent rb = ship.get(RigidBodyComponent.class);

        // Reset space physics
        if (rb != null) {
            rb.setDamping(shipComp.linearDamping, shipComp.angularDamping);
        }

        // Disable high-detail terrain
        // TODO: TerrainStreamingSystem.disableHighDetail()

        // Reset planet reference
        nearestPlanet = null;
    }

    /**
     * Transition player between ship and FPS mode
     */
    public void toggleFPSMode(Entity playerEntity) {
        PlayerComponent player = playerEntity.get(PlayerComponent.class);
        ShipComponent ship = playerEntity.get(ShipComponent.class);

        if (player == null) return;

        if (player.state == PlayerComponent.PlayerState.SHIP) {
            // Can only exit ship if landed or docked
            if (ship != null && (ship.isLanded || ship.isDocked)) {
                player.state = PlayerComponent.PlayerState.FPS;
                currentState = TransitionState.FPS_TRANSITION;
                onExitShip(playerEntity);
            }
        } else if (player.state == PlayerComponent.PlayerState.FPS) {
            // Return to ship
            player.state = PlayerComponent.PlayerState.SHIP;
            currentState = TransitionState.PLANETARY;
            onEnterShip(playerEntity);
        }
    }

    private void onExitShip(Entity entity) {
        Gdx.app.log("TransitionManager", "Player exited ship - FPS mode");

        // Create FPS character entity
        // TODO: Create separate character entity or reuse

        // Position player next to ship
        TransformComponent shipTransform = entity.get(TransformComponent.class);
        if (shipTransform != null) {
            // Offset position to be beside the ship
            Vector3 exitPos = shipTransform.position.cpy();
            exitPos.add(shipTransform.getRight().scl(5f)); // 5m to the right
        }
    }

    private void onEnterShip(Entity entity) {
        Gdx.app.log("TransitionManager", "Player entered ship - Ship mode");

        // Return camera to cockpit
        CameraComponent camera = entity.get(CameraComponent.class);
        if (camera != null) {
            camera.mode = CameraComponent.CameraMode.COCKPIT;
        }
    }

    public void setNearestPlanet(Planet planet, Vector3 center, float radius) {
        this.nearestPlanet = planet;
        this.planetCenter.set(center);
        this.planetRadius = radius;
    }

    public TransitionState getCurrentState() {
        return currentState;
    }

    public float getTransitionProgress() {
        return transitionProgress;
    }

    public boolean isInSpace() {
        return currentState == TransitionState.SPACE;
    }

    public boolean isOnPlanet() {
        return currentState == TransitionState.PLANETARY;
    }

    public boolean isInTransition() {
        return currentState == TransitionState.ATMOSPHERE_ENTRY ||
                currentState == TransitionState.TAKEOFF ||
                currentState == TransitionState.FPS_TRANSITION;
    }
}
