package com.astral.screens;

import com.astral.AstralFrontier;
import com.astral.components.*;
import com.astral.ecs.Entity;
import com.astral.ecs.World;
import com.astral.systems.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btBoxShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.linearmath.btDefaultMotionState;
import com.badlogic.gdx.math.Matrix4;
import com.astral.procedural.Planet;
import com.astral.procedural.PlanetType;

/**
 * Main game screen - where gameplay happens
 */
public class GameScreen implements Screen {

    private final AstralFrontier game;
    private World world;

    private InputSystem inputSystem;
    private RenderSystem renderSystem;
    private PhysicsSystem physicsSystem;
    private GameLogicSystem gameLogicSystem;
    private UISystem uiSystem;
    private AudioSystem audioSystem;
    private TransitionManager transitionManager;
    private FPSCharacterController fpsController;

    private Entity playerEntity;
    private Model shipModel;
    private Model asteroidModel;
    private Model planetModel;

    // Test planet position
    private Vector3 planetPosition = new Vector3(0, 0, -500);
    private float planetRadius = 100f;

    private boolean paused = false;

    // Pause menu rendering
    private SpriteBatch pauseBatch;
    private ShapeRenderer pauseShapeRenderer;
    private BitmapFont pauseFont;

    public GameScreen(AstralFrontier game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "Starting game...");

        world = game.getEcsWorld();
        inputSystem = game.getInputSystem();
        renderSystem = game.getRenderSystem();
        physicsSystem = game.getPhysicsSystem();
        audioSystem = game.getAudioSystem();

        // Clear old entities when returning from planet
        world.clearEntities();

        // Get game logic system and connect input
        gameLogicSystem = game.getGameLogicSystem();
        gameLogicSystem.setInputSystem(inputSystem);

        // Get UI system
        uiSystem = new UISystem(world);
        uiSystem.initialize();

        // Initialize systems (reinitialize physics to clear old bodies)
        renderSystem.initialize();
        physicsSystem.dispose();
        physicsSystem.initialize();

        // Initialize transition manager
        transitionManager = new TransitionManager(world);
        transitionManager.setPhysicsSystem(physicsSystem);

        // Initialize FPS controller
        fpsController = new FPSCharacterController(world);
        fpsController.setInputSystem(inputSystem);

        // Create player ship
        createPlayerShip();

        // Create some test objects
        createTestObjects();

        // Create test planet
        createTestPlanet();

        // Set input processor and lock mouse for flight controls
        Gdx.input.setInputProcessor(inputSystem);
        inputSystem.setMouseLocked(true);

        // Initialize pause menu resources
        pauseBatch = new SpriteBatch();
        pauseShapeRenderer = new ShapeRenderer();
        pauseFont = new BitmapFont();
        pauseFont.getData().setScale(3f);

        Gdx.app.log("GameScreen", "Game started!");
    }

    private void createPlayerShip() {
        // Create ship model (simple box for now)
        ModelBuilder modelBuilder = new ModelBuilder();
        shipModel = modelBuilder.createBox(5f, 2f, 10f,
                new Material(ColorAttribute.createDiffuse(Color.GRAY)),
                Usage.Position | Usage.Normal);

        // Create player entity
        playerEntity = world.createEntity();
        playerEntity.setTag("Player");

        // Add components
        TransformComponent transform = new TransformComponent();
        transform.setPosition(0, 0, 50);
        playerEntity.add(transform);

        // Render component
        RenderComponent render = new RenderComponent();
        render.setModel(new ModelInstance(shipModel));
        playerEntity.add(render);

        // Ship component
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
        camera.offset.set(0, 2, 5); // Slightly above and behind
        camera.initialize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        playerEntity.add(camera);

        // Physics component
        RigidBodyComponent rb = new RigidBodyComponent();
        rb.mass = ship.mass;
        rb.shape = new btBoxShape(new Vector3(2.5f, 1f, 5f));

        // Create rigid body
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

        playerEntity.add(rb);

        // Add to physics world
        physicsSystem.addRigidBody(playerEntity);

        // Network component (for future MP)
        NetworkComponent network = new NetworkComponent();
        network.isOwner = true;
        playerEntity.add(network);

        world.processPending();

        Gdx.app.log("GameScreen", "Player ship created");
    }

    private void createTestObjects() {
        ModelBuilder modelBuilder = new ModelBuilder();
        asteroidModel = modelBuilder.createSphere(10f, 10f, 10f, 16, 16,
                new Material(ColorAttribute.createDiffuse(Color.BROWN)),
                Usage.Position | Usage.Normal);

        // Create some asteroids
        for (int i = 0; i < 20; i++) {
            float x = (float) (Math.random() - 0.5) * 500;
            float y = (float) (Math.random() - 0.5) * 500;
            float z = (float) (Math.random() - 0.5) * 500;

            Entity asteroid = world.createEntity();
            asteroid.setTag("Asteroid");

            TransformComponent transform = new TransformComponent();
            transform.setPosition(x, y, z);
            float scale = 0.5f + (float) Math.random() * 2f;
            transform.setScale(scale, scale, scale);
            asteroid.add(transform);

            RenderComponent render = new RenderComponent();
            render.setModel(new ModelInstance(asteroidModel));
            asteroid.add(render);
        }

        world.processPending();
        Gdx.app.log("GameScreen", "Test objects created");
    }

    private void createTestPlanet() {
        // Create a large planet to approach
        ModelBuilder modelBuilder = new ModelBuilder();
        planetModel = modelBuilder.createSphere(planetRadius * 2, planetRadius * 2, planetRadius * 2, 32, 32,
                new Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.5f, 0.3f, 1f))),
                Usage.Position | Usage.Normal);

        Entity planet = world.createEntity();
        planet.setTag("Planet");

        TransformComponent transform = new TransformComponent();
        transform.setPosition(planetPosition.x, planetPosition.y, planetPosition.z);
        planet.add(transform);

        RenderComponent render = new RenderComponent();
        render.setModel(new ModelInstance(planetModel));
        planet.add(render);

        world.processPending();

        // Register planet with transition manager
        Planet testPlanet = new Planet(12345L);
        testPlanet.setName("Test Planet");
        transitionManager.setNearestPlanet(testPlanet, planetPosition, planetRadius);

        Gdx.app.log("GameScreen", "Test planet created at " + planetPosition);
    }

    @Override
    public void render(float delta) {
        // Handle pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            inputSystem.setMouseLocked(!paused);
        }

        // Toggle debug info
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            renderSystem.toggleDebugInfo();
        }

        // Toggle FPS mode (F key when landed)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) && playerEntity != null) {
            transitionManager.toggleFPSMode(playerEntity);
        }

        // Land on planet (L key) - works from any distance for testing
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            game.landOnPlanet(12345L, PlanetType.DESERT, "Khepri Prime");
            return;
        }

        if (!paused) {
            // Input is processed in AstralFrontier.render() - don't call again here

            // Update camera from player position
            updateCamera();

            // Game logic is updated in AstralFrontier.render() fixed timestep loop
            // Only update screen-specific systems here

            // Update FPS controller (for on-foot mode)
            fpsController.update(delta);

            // Update transition manager (handles planet approach/landing)
            transitionManager.update(delta);

            // Update UI with planet info and transition state
            uiSystem.setPlanetInfo("Test Planet", planetPosition);
            uiSystem.setTransitionState(
                transitionManager.getCurrentState().name(),
                transitionManager.getTransitionProgress()
            );

            // World update happens in main game loop
            world.update(delta);
        }

        // Clear screen
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render 3D
        float alpha = 0f; // Interpolation factor
        renderSystem.render(delta, alpha);

        // Render UI
        uiSystem.render(delta);

        // Render landing hint
        if (!paused) {
            pauseBatch.begin();
            pauseFont.getData().setScale(1.5f);
            pauseFont.setColor(0.5f, 0.8f, 1f, 1f);
            pauseFont.draw(pauseBatch, "Press L to land on planet", 20, 60);
            pauseFont.draw(pauseBatch, "WASD: Move | Mouse: Look | Shift: Boost", 20, 30);
            pauseFont.getData().setScale(3f);
            pauseBatch.end();
        }

        // Render pause menu if paused
        if (paused) {
            renderPauseMenu();
        }
    }

    private void updateCamera() {
        if (playerEntity == null) return;

        TransformComponent transform = playerEntity.get(TransformComponent.class);
        CameraComponent camComp = playerEntity.get(CameraComponent.class);

        if (transform == null || camComp == null || camComp.camera == null) return;

        // Position camera relative to ship
        Vector3 camPos = transform.position.cpy();

        if (camComp.mode == CameraComponent.CameraMode.COCKPIT) {
            // Cockpit view - inside the ship
            Vector3 offset = camComp.offset.cpy().mul(transform.rotation);
            camPos.add(offset);
            camComp.camera.position.set(camPos);
            camComp.camera.direction.set(transform.getForward());
            camComp.camera.up.set(transform.getUp());
        } else if (camComp.mode == CameraComponent.CameraMode.CHASE) {
            // Chase camera - behind and above
            Vector3 back = transform.getForward().scl(-camComp.thirdPersonDistance);
            Vector3 up = transform.getUp().scl(camComp.thirdPersonHeight);
            camPos.add(back).add(up);
            camComp.camera.position.set(camPos);
            camComp.camera.lookAt(transform.position);
        }

        camComp.camera.update();
    }

    private void renderPauseMenu() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Dark overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        pauseShapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        pauseShapeRenderer.setColor(0, 0, 0, 0.7f);
        pauseShapeRenderer.rect(0, 0, width, height);
        pauseShapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Pause text
        pauseBatch.begin();
        pauseFont.setColor(Color.WHITE);
        String pauseText = "PAUSED";
        pauseFont.draw(pauseBatch, pauseText, width / 2f - 80, height / 2f + 50);
        pauseFont.getData().setScale(1.5f);
        pauseFont.draw(pauseBatch, "Press ESC to resume", width / 2f - 120, height / 2f - 20);
        pauseFont.getData().setScale(3f);
        pauseBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (renderSystem != null) {
            renderSystem.resize(width, height);
        }
        if (inputSystem != null) {
            inputSystem.resize(width, height);
        }
        if (playerEntity != null) {
            CameraComponent cam = playerEntity.get(CameraComponent.class);
            if (cam != null) {
                cam.resize(width, height);
            }
        }
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (shipModel != null) shipModel.dispose();
        if (asteroidModel != null) asteroidModel.dispose();
        if (planetModel != null) planetModel.dispose();
        if (pauseBatch != null) pauseBatch.dispose();
        if (pauseShapeRenderer != null) pauseShapeRenderer.dispose();
        if (pauseFont != null) pauseFont.dispose();
        if (uiSystem != null) uiSystem.dispose();
    }
}
