package com.astral.screens.factories;

import com.astral.components.*;
import com.astral.ecs.Entity;
import com.astral.ecs.World;
import com.astral.systems.PhysicsSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.utils.Disposable;

/**
 * Factory for creating player ship entities with all required components.
 */
public class PlayerShipFactory implements Disposable {

    private Model shipModel;
    private final ModelBuilder modelBuilder = new ModelBuilder();

    /**
     * Create a player ship entity with all components
     */
    public Entity createPlayerShip(World world, PhysicsSystem physicsSystem) {
        // Create ship model (simple box for now)
        shipModel = modelBuilder.createBox(5f, 2f, 10f,
                new Material(ColorAttribute.createDiffuse(Color.GRAY)),
                Usage.Position | Usage.Normal);

        // Create player entity
        Entity playerEntity = world.createEntity();
        playerEntity.setTag("Player");

        // Transform component
        TransformComponent transform = new TransformComponent();
        transform.setPosition(0, 0, 50);
        playerEntity.add(transform);

        // Render component
        RenderComponent render = new RenderComponent();
        render.setModel(new ModelInstance(shipModel));
        playerEntity.add(render);

        // Ship component
        ShipComponent ship = createShipComponent();
        playerEntity.add(ship);

        // Player component
        PlayerComponent player = new PlayerComponent();
        player.isLocalPlayer = true;
        player.playerName = "Commander";
        player.state = PlayerComponent.PlayerState.SHIP;
        playerEntity.add(player);

        // Camera component
        CameraComponent camera = new CameraComponent();
        camera.mode = CameraComponent.CameraMode.COCKPIT;
        camera.offset.set(0, 2, 5);
        camera.initialize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        playerEntity.add(camera);

        // Physics component
        RigidBodyComponent rb = createRigidBodyComponent(ship, transform);
        playerEntity.add(rb);

        // Add to physics world
        physicsSystem.addRigidBody(playerEntity);

        // Network component (for future MP)
        NetworkComponent network = new NetworkComponent();
        network.isOwner = true;
        playerEntity.add(network);

        world.processPending();

        Gdx.app.log("PlayerShipFactory", "Player ship created");
        return playerEntity;
    }

    private ShipComponent createShipComponent() {
        ShipComponent ship = new ShipComponent();
        ship.shipClass = ShipComponent.ShipClass.FIGHTER;
        ship.maxHull = 500f;
        ship.hullIntegrity = 500f;
        ship.maxShield = 300f;
        ship.shieldStrength = 300f;
        ship.maxFuel = 500f;
        ship.fuel = 500f;
        ship.mainThrust = 400000f;
        ship.maneuverThrust = 120000f;
        ship.mass = 8000f;
        return ship;
    }

    private RigidBodyComponent createRigidBodyComponent(ShipComponent ship, TransformComponent transform) {
        RigidBodyComponent rb = new RigidBodyComponent();
        rb.mass = ship.mass;
        rb.shape = new btBoxShape(new Vector3(2.5f, 1f, 5f));

        Vector3 localInertia = new Vector3();
        rb.shape.calculateLocalInertia(rb.mass, localInertia);

        btDefaultMotionState motionState = new btDefaultMotionState();
        motionState.setWorldTransform(new Matrix4().setToTranslation(transform.position));

        btRigidBody.btRigidBodyConstructionInfo constructionInfo =
                new btRigidBody.btRigidBodyConstructionInfo(rb.mass, motionState, rb.shape, localInertia);
        rb.body = new btRigidBody(constructionInfo);
        rb.body.setDamping(ship.linearDamping, ship.angularDamping);
        rb.body.setActivationState(4); // Disable deactivation
        rb.syncRotationFromPhysics = false; // Player controls rotation directly

        return rb;
    }

    @Override
    public void dispose() {
        if (shipModel != null) {
            shipModel.dispose();
            shipModel = null;
        }
    }
}
