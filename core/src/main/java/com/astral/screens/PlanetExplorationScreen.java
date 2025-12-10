package com.astral.screens;

import com.astral.AstralFrontier;
import com.astral.exploration.CameraController;
import com.astral.exploration.CombatManager;
import com.astral.exploration.ExplorationUI;
import com.astral.exploration.PlayerController;
import com.astral.exploration.WeaponSystem;
import com.astral.procedural.PlanetSurface;
import com.astral.procedural.PlanetType;
import com.astral.systems.InputSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

/**
 * First-person exploration screen for planet surfaces
 * Refactored to use modular components
 */
public class PlanetExplorationScreen implements Screen {

    private final AstralFrontier game;
    private PlanetSurface planetSurface;

    // Modular components
    private PlayerController playerController;
    private CameraController cameraController;
    private WeaponSystem weaponSystem;
    private CombatManager combatManager;
    private ExplorationUI ui;

    // Input
    private InputSystem inputSystem;

    // Rendering
    private ModelBatch modelBatch;

    public PlanetExplorationScreen(AstralFrontier game, long planetSeed, PlanetType planetType, String planetName) {
        this.game = game;
        this.planetSurface = new PlanetSurface(planetSeed, planetType, planetName);
    }

    @Override
    public void show() {
        Gdx.app.log("PlanetExploration", "Landing on " + planetSurface.getName());

        // Generate planet
        planetSurface.generate();

        // Initialize components
        playerController = new PlayerController();
        cameraController = new CameraController();
        weaponSystem = new WeaponSystem();
        combatManager = new CombatManager();
        ui = new ExplorationUI();

        // Setup camera
        cameraController.initialize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Get input system
        inputSystem = game.getInputSystem();
        Gdx.input.setInputProcessor(inputSystem);
        inputSystem.setMouseLocked(true);

        // Spawn player at center
        playerController.setPosition(0f, 50f, 0f);

        // Trigger initial chunk loading
        planetSurface.update(playerController.getPosition(), 0f);

        // Setup rendering
        modelBatch = new ModelBatch();

        // Initialize UI (creates shapeRenderer)
        ui.initialize();

        // Initialize weapon system (needs shapeRenderer and camera)
        weaponSystem.initialize(ui.getShapeRenderer(), cameraController.getCamera());

        // Initialize combat manager
        combatManager.initialize(cameraController.getCamera());

        Gdx.app.log("PlanetExploration", "Spawned at " + playerController.getPosition());
    }

    @Override
    public void render(float delta) {
        // Handle ESC to return to space
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            returnToSpace();
            return;
        }

        // Toggle debug
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            ui.toggleDebug();
        }

        // Update player movement
        playerController.update(delta, inputSystem, planetSurface);

        // Update camera
        cameraController.update(playerController);

        // Update terrain chunk streaming
        planetSurface.update(playerController.getPosition(), delta);

        // Update weapon system
        boolean isMoving = playerController.getVelocity().len2() > 0.5f && playerController.isGrounded();
        weaponSystem.update(delta, inputSystem, cameraController.getCamera(), isMoving, combatManager.getProjectiles());

        // Update FOV based on aim
        cameraController.setAimTransition(weaponSystem.getAimTransition());

        // Update combat
        combatManager.update(delta, playerController.getPosition(), planetSurface);

        // Clear screen
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render sky gradient
        ui.renderSkyGradient(planetSurface.getSkyColor());

        // Re-enable depth test for 3D
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        // Render 3D world
        modelBatch.begin(cameraController.getCamera());
        planetSurface.render(modelBatch);
        combatManager.render(modelBatch, planetSurface.getEnvironment());
        modelBatch.end();

        // Render bullet holes
        combatManager.renderBulletHoles();

        // Render GLTF weapon (3D)
        weaponSystem.render();

        // Render UI
        ui.render(playerController, weaponSystem, combatManager, planetSurface);
    }

    private void returnToSpace() {
        Gdx.app.log("PlanetExploration", "Returning to space...");
        game.startGame();
    }

    @Override
    public void resize(int width, int height) {
        cameraController.resize(width, height);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (modelBatch != null) modelBatch.dispose();
        if (planetSurface != null) planetSurface.dispose();
        if (weaponSystem != null) weaponSystem.dispose();
        if (combatManager != null) combatManager.dispose();
        if (ui != null) ui.dispose();
    }
}
