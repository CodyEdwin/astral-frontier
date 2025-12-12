package com.astral.screens;

import com.astral.AstralFrontier;
import com.astral.exploration.CameraController;
import com.astral.exploration.CombatManager;
import com.astral.exploration.ExplorationUI;
import com.astral.exploration.PlayerController;
import com.astral.exploration.WeaponSystem;
import com.astral.procedural.AtmosphericEffects;
import com.astral.procedural.PlanetEnvironmentGenerator;
import com.astral.procedural.PlanetSurface;
import com.astral.procedural.PlanetType;
import com.astral.systems.InputSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Enhanced first-person exploration screen for planet surfaces
 * Now with realistic environments, atmospheric effects, and dynamic weather
 */
public class PlanetExplorationScreen implements Screen {

    private final AstralFrontier game;
    private PlanetSurface planetSurface;
    private final long planetSeed;
    private final PlanetType planetType;
    private final String planetName;

    // Modular components
    private PlayerController playerController;
    private CameraController cameraController;
    private WeaponSystem weaponSystem;
    private CombatManager combatManager;
    private ExplorationUI ui;

    // Enhanced environment systems
    private PlanetEnvironmentGenerator environmentGenerator;
    private AtmosphericEffects atmosphericEffects;
    private Array<ModelInstance> environmentObjects;

    // Environment streaming
    private Vector3 lastEnvironmentUpdatePosition = new Vector3();
    private float environmentUpdateDistance = 50f;
    private float environmentStreamRadius = 100f;

    // Input
    private InputSystem inputSystem;

    // Rendering
    private ModelBatch modelBatch;

    // Performance settings
    private boolean highQualityMode = true;
    private int environmentDensity = 50;

    // Debug
    private boolean showDebugInfo = false;

    public PlanetExplorationScreen(
        AstralFrontier game,
        long planetSeed,
        PlanetType planetType,
        String planetName
    ) {
        this.game = game;
        this.planetSeed = planetSeed;
        this.planetType = planetType;
        this.planetName = planetName;
        this.planetSurface = new PlanetSurface(
            planetSeed,
            planetType,
            planetName
        );
        this.environmentObjects = new Array<>();
    }

    @Override
    public void show() {
        Gdx.app.log(
            "PlanetExploration",
            "Landing on " + planetName + " (" + planetType.displayName + ")"
        );
        Gdx.app.log(
            "PlanetExploration",
            "Initializing enhanced environment systems..."
        );

        // Generate planet terrain
        planetSurface.generate();

        // Initialize environment generator
        environmentGenerator = new PlanetEnvironmentGenerator(
            planetSeed,
            planetType
        );
        Gdx.app.log("PlanetExploration", "Environment generator initialized");

        // Initialize atmospheric effects
        atmosphericEffects = new AtmosphericEffects(planetSeed, planetType);
        Gdx.app.log(
            "PlanetExploration",
            "Atmospheric effects initialized - Weather: " +
                atmosphericEffects.getWeatherDescription()
        );

        // Initialize components
        playerController = new PlayerController();
        cameraController = new CameraController();
        weaponSystem = new WeaponSystem();
        combatManager = new CombatManager();
        ui = new ExplorationUI();

        // Setup camera
        cameraController.initialize(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        // Get input system
        inputSystem = game.getInputSystem();
        Gdx.input.setInputProcessor(inputSystem);
        inputSystem.setMouseLocked(true);

        // Spawn player at elevated position
        Vector3 spawnPosition = new Vector3(0f, 50f, 0f);
        playerController.setPosition(
            spawnPosition.x,
            spawnPosition.y,
            spawnPosition.z
        );
        lastEnvironmentUpdatePosition.set(spawnPosition);

        // Initial chunk loading
        planetSurface.update(playerController.getPosition(), 0f);

        // Generate initial environment objects around spawn
        generateEnvironmentAroundPlayer();

        // Setup rendering
        modelBatch = new ModelBatch();

        // Initialize UI
        ui.initialize();

        // Initialize weapon system
        weaponSystem.initialize(
            ui.getShapeRenderer(),
            cameraController.getCamera()
        );

        // Initialize combat manager
        combatManager.initialize(cameraController.getCamera());

        Gdx.app.log("PlanetExploration", "Exploration environment ready!");
        Gdx.app.log(
            "PlanetExploration",
            "Controls: WASD - Move, Mouse - Look, LMB - Shoot, RMB - Aim"
        );
        Gdx.app.log(
            "PlanetExploration",
            "F1 - Debug Info, F2 - Toggle Weather, F3 - Change Time, ESC - Return to Space"
        );
    }

    @Override
    public void render(float delta) {
        handleInput(delta);

        // Update player movement
        playerController.update(delta, inputSystem, planetSurface);

        // Update camera
        cameraController.update(playerController);

        // Update atmospheric effects
        atmosphericEffects.update(
            delta,
            playerController.getPosition(),
            cameraController.getCamera()
        );

        // Update terrain chunk streaming
        planetSurface.update(playerController.getPosition(), delta);

        // Stream environment objects
        updateEnvironmentStreaming();

        // Update weapon system
        boolean isMoving =
            playerController.getVelocity().len2() > 0.5f &&
            playerController.isGrounded();
        weaponSystem.update(
            delta,
            inputSystem,
            cameraController.getCamera(),
            isMoving,
            combatManager.getProjectiles()
        );

        // Update FOV based on aim
        cameraController.setAimTransition(weaponSystem.getAimTransition());

        // Update combat
        combatManager.update(
            delta,
            playerController.getPosition(),
            planetSurface
        );

        // Render scene
        renderScene(delta);
    }

    /**
     * Handle input events
     */
    private void handleInput(float delta) {
        // Return to space
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            returnToSpace();
            return;
        }

        // Toggle debug info
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            showDebugInfo = !showDebugInfo;
            ui.toggleDebug();
            Gdx.app.log(
                "PlanetExploration",
                "Debug info: " + (showDebugInfo ? "ON" : "OFF")
            );
        }

        // Cycle weather (debug)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
            // Weather changes automatically, but we can force a change
            Gdx.app.log(
                "PlanetExploration",
                "Weather: " +
                    atmosphericEffects.getWeatherDescription() +
                    " (Intensity: " +
                    String.format(
                        "%.1f",
                        atmosphericEffects.getWeatherIntensity()
                    ) +
                    ")"
            );
        }

        // Change time of day (debug)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            float newTime = (atmosphericEffects.getTimeOfDay() + 0.25f) % 1.0f;
            atmosphericEffects.setTimeOfDay(newTime);
            String timeDesc = newTime < 0.25f
                ? "Night"
                : newTime < 0.5f
                    ? "Morning"
                    : newTime < 0.75f
                        ? "Day"
                        : "Evening";
            Gdx.app.log("PlanetExploration", "Time of day: " + timeDesc);
        }

        // Toggle quality (debug)
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            highQualityMode = !highQualityMode;
            environmentDensity = highQualityMode ? 50 : 25;
            // Regenerate environment with new density
            clearEnvironmentObjects();
            generateEnvironmentAroundPlayer();
            Gdx.app.log(
                "PlanetExploration",
                "Quality: " +
                    (highQualityMode ? "HIGH" : "LOW") +
                    " (Density: " +
                    environmentDensity +
                    ")"
            );
        }
    }

    /**
     * Render the entire scene
     */
    private void renderScene(float delta) {
        // Get atmospheric sky color
        Color skyColor = atmosphericEffects.getCurrentSkyColor();

        // Clear screen with atmospheric color
        Gdx.gl.glClearColor(
            skyColor.r * 0.5f,
            skyColor.g * 0.5f,
            skyColor.b * 0.5f,
            1f
        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render sky gradient
        ui.renderSkyGradient(skyColor);

        // Enable depth test for 3D rendering
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        // Render 3D world
        modelBatch.begin(cameraController.getCamera());

        // Render terrain
        planetSurface.render(modelBatch);

        // Render environment objects (rocks, trees, etc.)
        for (ModelInstance instance : environmentObjects) {
            modelBatch.render(instance, planetSurface.getEnvironment());
        }

        // Render combat entities (enemies, projectiles)
        combatManager.render(modelBatch, planetSurface.getEnvironment());

        modelBatch.end();

        // Render atmospheric particles (rain, snow, dust, etc.)
        atmosphericEffects.renderParticles(cameraController.getCamera());

        // Render bullet holes
        combatManager.renderBulletHoles();

        // Render GLTF weapon (3D)
        weaponSystem.render();

        // Render fog overlay
        atmosphericEffects.renderFogOverlay(
            cameraController.getCamera(),
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

        // Render UI
        ui.render(playerController, weaponSystem, combatManager, planetSurface);

        // Render additional debug info if enabled
        if (showDebugInfo) {
            renderDebugInfo();
        }
    }

    /**
     * Update environment object streaming based on player position
     */
    private void updateEnvironmentStreaming() {
        float distanceMoved = playerController
            .getPosition()
            .dst(lastEnvironmentUpdatePosition);

        // Only update when player has moved significantly
        if (distanceMoved > environmentUpdateDistance) {
            // Remove distant objects
            cullDistantEnvironmentObjects();

            // Generate new objects in the direction of travel
            generateEnvironmentAroundPlayer();

            lastEnvironmentUpdatePosition.set(playerController.getPosition());
        }
    }

    /**
     * Generate environment objects around the player's current position
     */
    private void generateEnvironmentAroundPlayer() {
        Vector3 playerPos = playerController.getPosition();

        // Generate in a grid pattern around player for better distribution
        int gridSize = 5;
        float cellSize = (environmentStreamRadius / gridSize) * 2f;

        for (int x = -gridSize; x < gridSize; x++) {
            for (int z = -gridSize; z < gridSize; z++) {
                Vector3 cellCenter = new Vector3(
                    playerPos.x + x * cellSize,
                    playerPos.y,
                    playerPos.z + z * cellSize
                );

                // Check if we already have objects near this cell
                boolean hasNearbyObjects = false;
                for (ModelInstance existing : environmentObjects) {
                    Vector3 existingPos = new Vector3();
                    existing.transform.getTranslation(existingPos);
                    if (
                        existingPos.dst2(cellCenter) <
                        cellSize * cellSize * 0.25f
                    ) {
                        hasNearbyObjects = true;
                        break;
                    }
                }

                // Generate if no nearby objects
                if (!hasNearbyObjects) {
                    Array<ModelInstance> newObjects =
                        environmentGenerator.populateArea(
                            cellCenter,
                            cellSize * 0.5f,
                            environmentDensity / (gridSize * gridSize)
                        );
                    environmentObjects.addAll(newObjects);
                }
            }
        }

        Gdx.app.log(
            "PlanetExploration",
            "Environment objects: " + environmentObjects.size
        );
    }

    /**
     * Remove environment objects that are too far from player
     */
    private void cullDistantEnvironmentObjects() {
        Vector3 playerPos = playerController.getPosition();
        float cullDistance = environmentStreamRadius * 1.5f;
        float cullDistanceSquared = cullDistance * cullDistance;

        Vector3 tempPos = new Vector3();
        for (int i = environmentObjects.size - 1; i >= 0; i--) {
            ModelInstance instance = environmentObjects.get(i);
            instance.transform.getTranslation(tempPos);

            if (tempPos.dst2(playerPos) > cullDistanceSquared) {
                environmentObjects.removeIndex(i);
            }
        }
    }

    /**
     * Clear all environment objects
     */
    private void clearEnvironmentObjects() {
        environmentObjects.clear();
        lastEnvironmentUpdatePosition.set(Vector3.Zero);
    }

    /**
     * Render additional debug information
     */
    private void renderDebugInfo() {
        // This will be rendered by the UI system, just log some info
        if (Gdx.graphics.getFrameId() % 60 == 0) {
            // Every 60 frames
            Gdx.app.log(
                "PlanetExploration",
                String.format(
                    "Env Objects: %d | Weather: %s | Particles: %d | Chunks: Active",
                    environmentObjects.size,
                    atmosphericEffects.getWeatherDescription(),
                    atmosphericEffects.getParticleCount()
                )
            );
        }
    }

    /**
     * Return to space
     */
    private void returnToSpace() {
        Gdx.app.log(
            "PlanetExploration",
            "Returning to space from " + planetName + "..."
        );
        game.startGame();
    }

    @Override
    public void resize(int width, int height) {
        cameraController.resize(width, height);
    }

    @Override
    public void pause() {
        Gdx.app.log("PlanetExploration", "Exploration paused");
    }

    @Override
    public void resume() {
        Gdx.app.log("PlanetExploration", "Exploration resumed");
    }

    @Override
    public void hide() {
        Gdx.app.log("PlanetExploration", "Leaving planet surface");
    }

    @Override
    public void dispose() {
        Gdx.app.log("PlanetExploration", "Disposing exploration resources...");

        if (modelBatch != null) modelBatch.dispose();
        if (planetSurface != null) planetSurface.dispose();
        if (weaponSystem != null) weaponSystem.dispose();
        if (combatManager != null) combatManager.dispose();
        if (ui != null) ui.dispose();
        if (environmentGenerator != null) environmentGenerator.dispose();
        if (atmosphericEffects != null) atmosphericEffects.dispose();

        environmentObjects.clear();

        Gdx.app.log("PlanetExploration", "Exploration resources disposed");
    }

    // ==================== Getters ====================

    public PlanetSurface getPlanetSurface() {
        return planetSurface;
    }

    public PlanetType getPlanetType() {
        return planetType;
    }

    public String getPlanetName() {
        return planetName;
    }

    public AtmosphericEffects getAtmosphericEffects() {
        return atmosphericEffects;
    }

    public PlanetEnvironmentGenerator getEnvironmentGenerator() {
        return environmentGenerator;
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public CameraController getCameraController() {
        return cameraController;
    }

    public int getEnvironmentObjectCount() {
        return environmentObjects.size;
    }

    public boolean isHighQualityMode() {
        return highQualityMode;
    }

    public void setHighQualityMode(boolean enabled) {
        this.highQualityMode = enabled;
        this.environmentDensity = enabled ? 50 : 25;
    }
}
