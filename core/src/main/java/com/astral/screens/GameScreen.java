package com.astral.screens;

import com.astral.AstralFrontier;
import com.astral.components.*;
import com.astral.ecs.Entity;
import com.astral.ecs.World;
import com.astral.procedural.PlanetType;
import com.astral.screens.factories.PlayerShipFactory;
import com.astral.screens.factories.WorldObjectFactory;
import com.astral.screens.shipbuilder.StarfieldStyleBuilder;
import com.astral.screens.ui.PauseMenuRenderer;
import com.astral.systems.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;

/**
 * Main game screen - where space gameplay happens.
 * Refactored to use factory classes for entity creation.
 */
public class GameScreen implements Screen {

    private final AstralFrontier game;
    private World world;

    // Systems
    private InputSystem inputSystem;
    private RenderSystem renderSystem;
    private PhysicsSystem physicsSystem;
    private GameLogicSystem gameLogicSystem;
    private UISystem uiSystem;
    private TransitionManager transitionManager;
    private FPSCharacterController fpsController;

    // Factories
    private PlayerShipFactory playerShipFactory;
    private WorldObjectFactory worldObjectFactory;

    // UI
    private PauseMenuRenderer pauseMenuRenderer;

    // Entities
    private Entity playerEntity;

    // Test planet config
    private static final Vector3 PLANET_POSITION = new Vector3(0, 0, -500);
    private static final float PLANET_RADIUS = 100f;

    private boolean paused = false;

    public GameScreen(AstralFrontier game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "Starting game...");

        initializeSystems();
        initializeFactories();
        createWorld();
        setupInput();

        Gdx.app.log("GameScreen", "Game started!");
    }

    private void initializeSystems() {
        world = game.getEcsWorld();
        inputSystem = game.getInputSystem();
        renderSystem = game.getRenderSystem();
        physicsSystem = game.getPhysicsSystem();

        // Clear old entities when returning from planet
        world.clearEntities();

        // Get game logic system and connect input
        gameLogicSystem = game.getGameLogicSystem();
        gameLogicSystem.setInputSystem(inputSystem);

        // Initialize UI system
        uiSystem = new UISystem(world);
        uiSystem.initialize();

        // Initialize render and physics
        renderSystem.initialize();
        physicsSystem.dispose();
        physicsSystem.initialize();

        // Initialize transition manager
        transitionManager = new TransitionManager(world);
        transitionManager.setPhysicsSystem(physicsSystem);

        // Initialize FPS controller
        fpsController = new FPSCharacterController(world);
        fpsController.setInputSystem(inputSystem);

        // Initialize pause menu
        pauseMenuRenderer = new PauseMenuRenderer();
        pauseMenuRenderer.initialize();
    }

    private void initializeFactories() {
        playerShipFactory = new PlayerShipFactory();
        worldObjectFactory = new WorldObjectFactory();
    }

    private void createWorld() {
        // Create player ship using factory
        playerEntity = playerShipFactory.createPlayerShip(world, physicsSystem);

        // Create asteroids
        worldObjectFactory.createAsteroids(world, 20, 500f);

        // Create test planet
        worldObjectFactory.createPlanet(
            world,
            PLANET_POSITION,
            PLANET_RADIUS,
            new Color(0.2f, 0.5f, 0.3f, 1f),
            "Test Planet",
            transitionManager
        );
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(inputSystem);
        inputSystem.setMouseLocked(true);
    }

    @Override
    public void render(float delta) {
        handleInput();

        if (!paused) {
            update(delta);
        }

        renderWorld(delta);
        renderUI(delta);

        if (paused) {
            pauseMenuRenderer.render();
        }
    }

    private void handleInput() {
        // Pause toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            inputSystem.setMouseLocked(!paused);
        }

        // Debug toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            renderSystem.toggleDebugInfo();
        }

        // Ship Builder - open with H key
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            openShipBuilder();
        }

        // FPS mode toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) && playerEntity != null) {
            transitionManager.toggleFPSMode(playerEntity);
        }

        // Land on planet
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            game.landOnPlanet(12345L, PlanetType.DESERT, "Khepri Prime");
        }
    }

    private void openShipBuilder() {
        // Unlock mouse cursor before switching to ship builder
        inputSystem.setMouseLocked(false);
        Gdx.input.setCursorCatched(false);

        StarfieldStyleBuilder builderScreen = new StarfieldStyleBuilder(
            game,
            this
        );
        game.setScreen(builderScreen);
        Gdx.app.log("GameScreen", "Opening Starfield-style ship builder...");
    }

    private void update(float delta) {
        updateCamera();
        fpsController.update(delta);
        transitionManager.update(delta);

        // Update UI with planet info
        uiSystem.setPlanetInfo("Test Planet", PLANET_POSITION);
        uiSystem.setTransitionState(
            transitionManager.getCurrentState().name(),
            transitionManager.getTransitionProgress()
        );

        world.update(delta);
    }

    private void renderWorld(float delta) {
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        float alpha = 0f;
        renderSystem.render(delta, alpha);
    }

    private void renderUI(float delta) {
        uiSystem.render(delta);

        if (!paused) {
            pauseMenuRenderer.renderHints();
        }
    }

    private void updateCamera() {
        if (playerEntity == null) return;

        TransformComponent transform = playerEntity.get(
            TransformComponent.class
        );
        CameraComponent camComp = playerEntity.get(CameraComponent.class);

        if (
            transform == null || camComp == null || camComp.camera == null
        ) return;

        Vector3 camPos = transform.position.cpy();

        if (camComp.mode == CameraComponent.CameraMode.COCKPIT) {
            Vector3 offset = camComp.offset.cpy().mul(transform.rotation);
            camPos.add(offset);
            camComp.camera.position.set(camPos);
            camComp.camera.direction.set(transform.getForward());
            camComp.camera.up.set(transform.getUp());
        } else if (camComp.mode == CameraComponent.CameraMode.CHASE) {
            Vector3 back = transform
                .getForward()
                .scl(-camComp.thirdPersonDistance);
            Vector3 up = transform.getUp().scl(camComp.thirdPersonHeight);
            camPos.add(back).add(up);
            camComp.camera.position.set(camPos);
            camComp.camera.lookAt(transform.position);
        }

        camComp.camera.update();
    }

    @Override
    public void resize(int width, int height) {
        if (renderSystem != null) renderSystem.resize(width, height);
        if (inputSystem != null) inputSystem.resize(width, height);
        if (playerEntity != null) {
            CameraComponent cam = playerEntity.get(CameraComponent.class);
            if (cam != null) cam.resize(width, height);
        }
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        // Re-lock mouse cursor when returning from ship builder or other screens
        if (inputSystem != null) {
            Gdx.input.setInputProcessor(inputSystem);
            inputSystem.setMouseLocked(true);
            Gdx.input.setCursorCatched(false); // Make sure it's visible but locked
        }
        Gdx.app.log("GameScreen", "Game resumed - mouse locked");
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (playerShipFactory != null) playerShipFactory.dispose();
        if (worldObjectFactory != null) worldObjectFactory.dispose();
        if (pauseMenuRenderer != null) pauseMenuRenderer.dispose();
        if (uiSystem != null) uiSystem.dispose();
    }
}
