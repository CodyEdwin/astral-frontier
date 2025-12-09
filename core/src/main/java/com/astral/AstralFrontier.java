package com.astral;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.TimeUtils;
import com.astral.screens.GameScreen;
import com.astral.screens.LoadingScreen;
import com.astral.screens.MenuScreen;
import com.astral.screens.PlanetExplorationScreen;
import com.astral.procedural.PlanetType;
import com.astral.systems.*;
import com.astral.ecs.World;
import com.astral.utils.GameConfig;

/**
 * AstralFrontier - Main game class
 * AAA-quality open-world space exploration RPG
 */
public class AstralFrontier extends Game {

    // Fixed timestep configuration
    private static final float PHYSICS_STEP = 1f / 60f;  // 60Hz physics
    private static final float MAX_FRAME_TIME = 0.25f;   // Prevent spiral of death
    private static final int MAX_PHYSICS_STEPS = 5;

    private float accumulator = 0f;
    private double currentTime;
    private double previousTime;

    // Core systems
    private World ecsWorld;
    private InputSystem inputSystem;
    private PhysicsSystem physicsSystem;
    private RenderSystem renderSystem;
    private NetworkSystem networkSystem;
    private AudioSystem audioSystem;
    private GameLogicSystem gameLogicSystem;
    private UISystem uiSystem;

    // State
    private GameConfig config;
    private boolean initialized = false;

    public static AstralFrontier instance;

    @Override
    public void create() {
        instance = this;

        Gdx.app.log("AstralFrontier", "Initializing game...");
        Gdx.app.log("AstralFrontier", "LibGDX Version: " + com.badlogic.gdx.Version.VERSION);

        // Load configuration
        config = new GameConfig();

        // Initialize ECS world
        ecsWorld = new World();

        // Initialize core systems
        initializeSystems();

        // Initialize input system (sets input processor)
        inputSystem.initialize();

        // Start with loading screen
        setScreen(new LoadingScreen(this));

        previousTime = TimeUtils.millis() / 1000.0;
        initialized = true;

        Gdx.app.log("AstralFrontier", "Game initialized successfully!");
    }

    private void initializeSystems() {
        inputSystem = new InputSystem(ecsWorld);
        physicsSystem = new PhysicsSystem(ecsWorld);
        renderSystem = new RenderSystem(ecsWorld);
        networkSystem = new NetworkSystem(ecsWorld);
        audioSystem = new AudioSystem(ecsWorld);
        gameLogicSystem = new GameLogicSystem(ecsWorld);
        uiSystem = new UISystem(ecsWorld);

        // Register systems with ECS world
        ecsWorld.addSystem(inputSystem);
        ecsWorld.addSystem(physicsSystem);
        ecsWorld.addSystem(renderSystem);
        ecsWorld.addSystem(networkSystem);
        ecsWorld.addSystem(audioSystem);
        ecsWorld.addSystem(gameLogicSystem);
        ecsWorld.addSystem(uiSystem);
    }

    @Override
    public void render() {
        if (!initialized) return;

        currentTime = TimeUtils.millis() / 1000.0;
        float frameTime = Math.min((float)(currentTime - previousTime), MAX_FRAME_TIME);
        previousTime = currentTime;

        accumulator += frameTime;

        // Input processing (immediate)
        inputSystem.processInput();

        // Fixed timestep physics/simulation
        int steps = 0;
        while (accumulator >= PHYSICS_STEP && steps < MAX_PHYSICS_STEPS) {
            physicsSystem.step(PHYSICS_STEP);
            networkSystem.tick(PHYSICS_STEP);
            gameLogicSystem.update(PHYSICS_STEP);
            accumulator -= PHYSICS_STEP;
            steps++;
        }

        // Interpolation factor for rendering
        float alpha = accumulator / PHYSICS_STEP;

        // Variable timestep rendering
        super.render(); // Renders current screen
        audioSystem.update(frameTime);
    }

    @Override
    public void dispose() {
        Gdx.app.log("AstralFrontier", "Shutting down...");

        if (screen != null) {
            screen.dispose();
        }

        if (ecsWorld != null) {
            ecsWorld.dispose();
        }

        if (physicsSystem != null) {
            physicsSystem.dispose();
        }

        if (audioSystem != null) {
            audioSystem.dispose();
        }

        Gdx.app.log("AstralFrontier", "Shutdown complete.");
    }

    // Accessors
    public World getEcsWorld() { return ecsWorld; }
    public InputSystem getInputSystem() { return inputSystem; }
    public PhysicsSystem getPhysicsSystem() { return physicsSystem; }
    public RenderSystem getRenderSystem() { return renderSystem; }
    public NetworkSystem getNetworkSystem() { return networkSystem; }
    public AudioSystem getAudioSystem() { return audioSystem; }
    public GameConfig getConfig() { return config; }
    public GameLogicSystem getGameLogicSystem() { return gameLogicSystem; }

    public void showMainMenu() {
        setScreen(new MenuScreen(this));
    }

    public void startGame() {
        setScreen(new GameScreen(this));
    }

    public void landOnPlanet(long planetSeed, PlanetType planetType, String planetName) {
        Gdx.app.log("AstralFrontier", "Landing on planet: " + planetName);
        setScreen(new PlanetExplorationScreen(this, planetSeed, planetType, planetName));
    }
}
