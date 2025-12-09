package com.astral.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.astral.ecs.World;
import com.astral.network.GameServer;
import com.astral.systems.PhysicsSystem;
import com.astral.systems.NetworkSystem;
import com.astral.systems.GameLogicSystem;

/**
 * Dedicated server for AstralFrontier multiplayer
 */
public class DedicatedServer extends ApplicationAdapter {

    private static final float TICK_RATE = 1f / 60f; // 60Hz server tick

    private World ecsWorld;
    private GameServer gameServer;
    private PhysicsSystem physicsSystem;
    private NetworkSystem networkSystem;
    private GameLogicSystem gameLogicSystem;

    private float accumulator = 0f;
    private boolean running = true;

    public static void main(String[] args) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        config.updatesPerSecond = 60;
        new HeadlessApplication(new DedicatedServer(), config);
    }

    @Override
    public void create() {
        Gdx.app.log("Server", "AstralFrontier Dedicated Server starting...");

        ecsWorld = new World();
        physicsSystem = new PhysicsSystem(ecsWorld);
        networkSystem = new NetworkSystem(ecsWorld);
        gameLogicSystem = new GameLogicSystem(ecsWorld);

        gameServer = new GameServer(54555, 54777);
        gameServer.start();

        Gdx.app.log("Server", "Server started on TCP:54555 UDP:54777");
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        accumulator += delta;

        while (accumulator >= TICK_RATE) {
            // Process network messages
            networkSystem.tick(TICK_RATE);

            // Update game logic
            gameLogicSystem.update(TICK_RATE);

            // Step physics
            physicsSystem.step(TICK_RATE);

            // Broadcast state to clients
            gameServer.broadcastState(ecsWorld);

            accumulator -= TICK_RATE;
        }
    }

    @Override
    public void dispose() {
        Gdx.app.log("Server", "Server shutting down...");
        if (gameServer != null) {
            gameServer.stop();
        }
        if (ecsWorld != null) {
            ecsWorld.dispose();
        }
    }
}
