package com.astral.network;

import com.astral.ecs.World;
import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

/**
 * Game server for multiplayer
 */
public class GameServer {

    private Server server;
    private final int tcpPort;
    private final int udpPort;
    private boolean running = false;

    // Server tick rate
    private static final float TICK_RATE = 1f / 60f;
    private static final float BROADCAST_RATE = 1f / 20f; // 20Hz state broadcast

    private float broadcastAccumulator = 0f;

    public GameServer(int tcpPort, int udpPort) {
        this.tcpPort = tcpPort;
        this.udpPort = udpPort;
    }

    public void start() {
        try {
            server = new Server(16384, 8192);

            // Register packet classes
            PacketRegistry.register(server.getKryo());

            // Add listener
            server.addListener(new Listener() {
                @Override
                public void connected(Connection connection) {
                    onClientConnected(connection);
                }

                @Override
                public void disconnected(Connection connection) {
                    onClientDisconnected(connection);
                }

                @Override
                public void received(Connection connection, Object object) {
                    onPacketReceived(connection, object);
                }
            });

            server.bind(tcpPort, udpPort);
            server.start();
            running = true;

            Gdx.app.log("GameServer", "Server started on TCP:" + tcpPort + " UDP:" + udpPort);

        } catch (IOException e) {
            Gdx.app.error("GameServer", "Failed to start server", e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop();
            running = false;
            Gdx.app.log("GameServer", "Server stopped");
        }
    }

    private void onClientConnected(Connection connection) {
        Gdx.app.log("GameServer", "Client connected: " + connection.getID());
        // TODO: Send world state to new client
    }

    private void onClientDisconnected(Connection connection) {
        Gdx.app.log("GameServer", "Client disconnected: " + connection.getID());
        // TODO: Clean up client entity
    }

    private void onPacketReceived(Connection connection, Object packet) {
        if (packet instanceof Packets.ShipInputPacket input) {
            handleShipInput(connection, input);
        } else if (packet instanceof Packets.PlayerInputPacket input) {
            handlePlayerInput(connection, input);
        } else if (packet instanceof Packets.ChatMessage chat) {
            handleChatMessage(connection, chat);
        }
    }

    private void handleShipInput(Connection connection, Packets.ShipInputPacket input) {
        // TODO: Apply input to player's ship entity
        // Validate and process server-authoritative simulation
    }

    private void handlePlayerInput(Connection connection, Packets.PlayerInputPacket input) {
        // TODO: Apply input to player's character entity
    }

    private void handleChatMessage(Connection connection, Packets.ChatMessage chat) {
        // Broadcast chat to all clients
        server.sendToAllTCP(chat);
    }

    public void broadcastState(World world) {
        if (!running) return;

        // TODO: Build world snapshot from entities
        Packets.WorldSnapshotPacket snapshot = new Packets.WorldSnapshotPacket();
        snapshot.serverTick = System.currentTimeMillis();

        // Send to all clients via UDP for fast state updates
        server.sendToAllUDP(snapshot);
    }

    public void sendToClient(int connectionId, Object packet, boolean reliable) {
        Connection connection = getConnection(connectionId);
        if (connection != null) {
            if (reliable) {
                connection.sendTCP(packet);
            } else {
                connection.sendUDP(packet);
            }
        }
    }

    public void broadcast(Object packet, boolean reliable) {
        if (reliable) {
            server.sendToAllTCP(packet);
        } else {
            server.sendToAllUDP(packet);
        }
    }

    private Connection getConnection(int id) {
        for (Connection c : server.getConnections()) {
            if (c.getID() == id) return c;
        }
        return null;
    }

    public boolean isRunning() {
        return running;
    }

    public int getConnectionCount() {
        return server != null ? server.getConnections().length : 0;
    }
}
