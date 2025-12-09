package com.astral.systems;

import com.astral.components.RigidBodyComponent;
import com.astral.components.TransformComponent;
import com.astral.ecs.Entity;
import com.astral.ecs.GameSystem;
import com.astral.ecs.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Physics system using Bullet Physics
 */
public class PhysicsSystem extends GameSystem implements Disposable {

    private static final float FIXED_TIMESTEP = 1f / 60f;
    private static final int MAX_SUBSTEPS = 5;

    // Bullet physics objects
    private btDynamicsWorld dynamicsWorld;
    private btDbvtBroadphase broadphase;
    private btDefaultCollisionConfiguration collisionConfig;
    private btCollisionDispatcher dispatcher;
    private btSequentialImpulseConstraintSolver solver;

    // Collision detection callback
    private ContactListener contactListener;

    // Reusable objects
    private final Matrix4 tempMatrix = new Matrix4();
    private final Vector3 tempVector = new Vector3();

    private boolean initialized = false;

    public PhysicsSystem(World world) {
        super(world);
        setPriority(90);
    }

    @Override
    public void initialize() {
        if (initialized) return;

        Gdx.app.log("PhysicsSystem", "Initializing Bullet Physics...");

        // Initialize Bullet
        Bullet.init();

        // Setup collision configuration
        collisionConfig = new btDefaultCollisionConfiguration();
        dispatcher = new btCollisionDispatcher(collisionConfig);
        broadphase = new btDbvtBroadphase();
        solver = new btSequentialImpulseConstraintSolver();

        // Create dynamics world
        dynamicsWorld = new btDiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfig);
        dynamicsWorld.setGravity(new Vector3(0, 0, 0)); // Space - no gravity

        // Setup contact listener
        contactListener = new ContactListener();

        initialized = true;
        Gdx.app.log("PhysicsSystem", "Bullet Physics initialized");
    }

    public void step(float deltaTime) {
        if (!initialized || dynamicsWorld == null) return;

        // Step the simulation
        dynamicsWorld.stepSimulation(deltaTime, MAX_SUBSTEPS, FIXED_TIMESTEP);

        // Sync transforms from physics
        Array<Entity> entities = getEntitiesWith(TransformComponent.class, RigidBodyComponent.class);
        for (Entity entity : entities) {
            TransformComponent transform = entity.get(TransformComponent.class);
            RigidBodyComponent rb = entity.get(RigidBodyComponent.class);

            if (rb.body != null && !rb.kinematic) {
                rb.body.getWorldTransform(tempMatrix);
                tempMatrix.getTranslation(transform.position);
                // Only sync rotation if flag is set (not for player-controlled ships)
                if (rb.syncRotationFromPhysics) {
                    tempMatrix.getRotation(transform.rotation);
                }
                transform.dirty = true;
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        // Main update handled by step() called from game loop
    }

    public void addRigidBody(Entity entity) {
        if (!initialized) return;

        RigidBodyComponent rb = entity.get(RigidBodyComponent.class);
        TransformComponent transform = entity.get(TransformComponent.class);

        if (rb != null && rb.body != null && transform != null) {
            // Set initial transform
            tempMatrix.set(transform.position, transform.rotation, transform.scale);
            rb.body.setWorldTransform(tempMatrix);

            dynamicsWorld.addRigidBody(rb.body, rb.collisionGroup, rb.collisionMask);
        }
    }

    public void removeRigidBody(Entity entity) {
        if (!initialized) return;

        RigidBodyComponent rb = entity.get(RigidBodyComponent.class);
        if (rb != null && rb.body != null) {
            dynamicsWorld.removeRigidBody(rb.body);
        }
    }

    public void setGravity(Vector3 gravity) {
        if (dynamicsWorld != null) {
            dynamicsWorld.setGravity(gravity);
        }
    }

    public btDynamicsWorld getDynamicsWorld() {
        return dynamicsWorld;
    }

    public RaycastResult raycast(Vector3 from, Vector3 to, short mask) {
        if (!initialized || dynamicsWorld == null) return null;

        ClosestRayResultCallback callback = new ClosestRayResultCallback(from, to);
        callback.setCollisionFilterMask(mask);

        dynamicsWorld.rayTest(from, to, callback);

        RaycastResult result = null;
        if (callback.hasHit()) {
            result = new RaycastResult();
            callback.getHitPointWorld(result.point);
            callback.getHitNormalWorld(result.normal);
            result.collisionObject = callback.getCollisionObject();
        }

        callback.dispose();
        return result;
    }

    public static class RaycastResult {
        public Vector3 point = new Vector3();
        public Vector3 normal = new Vector3();
        public btCollisionObject collisionObject;
        public Entity entity;
    }

    @Override
    public void dispose() {
        Gdx.app.log("PhysicsSystem", "Disposing Bullet Physics...");

        if (dynamicsWorld != null) dynamicsWorld.dispose();
        if (solver != null) solver.dispose();
        if (broadphase != null) broadphase.dispose();
        if (dispatcher != null) dispatcher.dispose();
        if (collisionConfig != null) collisionConfig.dispose();
        if (contactListener != null) contactListener.dispose();

        initialized = false;
    }

    private class ContactListener extends com.badlogic.gdx.physics.bullet.collision.ContactListener {
        @Override
        public boolean onContactAdded(btManifoldPoint cp,
                                       btCollisionObjectWrapper colObj0Wrap, int partId0, int index0,
                                       btCollisionObjectWrapper colObj1Wrap, int partId1, int index1) {
            // Handle collision events
            return true;
        }
    }
}
