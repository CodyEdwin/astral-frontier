package com.astral.network;

import com.astral.ecs.World;
import com.badlogic.gdx.Gdx;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;

import java.io.IOException;

/**
 * Game client for multiplayer
 */
public class GameClient {

    private Client client;
    private boolean connected = false;
    private String serverAddress;

    // Client prediction state
    private int currentInputSequence = 0;
    private final Packets.ShipInputPacket[] inputHistory = new Packets.ShipInputPacket[128];

    // Interpolation buffer
    private final Packets.EntityStatePacket[] stateBuffer = new Packets.EntityStatePacket[32];
    private int stateBufferHead = 0;

    public interface ClientListener {
        void onConnected();
        void onDisconnected();
        void onConnectionFailed(String reason);
        void onChatMessage(Packets.ChatMessage message);
        void onWorldSnapshot(Packets.WorldSnapshotPacket snapshot);
    }

    private ClientListener listener;

    public GameClient() {
        client = new Client(16384, 8192);
        PacketRegistry.register(client.getKryo());

        client.addListener(new Listener() {
            @Override
            public void connected(Connection connection) {
                GameClient.this.connected = true;
                Gdx.app.log("GameClient", "Connected to server");
                if (listener != null) listener.onConnected();
            }

            @Override
            public void disconnected(Connection connection) {
                GameClient.this.connected = false;
                Gdx.app.log("GameClient", "Disconnected from server");
                if (listener != null) listener.onDisconnected();
            }

            @Override
            public void received(Connection connection, Object object) {
                onPacketReceived(object);
            }
        });
    }

    public void connect(String address, int tcpPort, int udpPort) {
        this.serverAddress = address;

        new Thread(() -> {
            try {
                client.start();
                client.connect(5000, address, tcpPort, udpPort);
            } catch (IOException e) {
                Gdx.app.error("GameClient", "Failed to connect", e);
                if (listener != null) {
                    Gdx.app.postRunnable(() -> listener.onConnectionFailed(e.getMessage()));
                }
            }
        }).start();
    }

    public void disconnect() {
        if (client != null) {
            client.stop();
            connected = false;
        }
    }

    private void onPacketReceived(Object packet) {
        if (packet instanceof Packets.WorldSnapshotPacket snapshot) {
            handleWorldSnapshot(snapshot);
        } else if (packet instanceof Packets.EntityStatePacket state) {
            handleEntityState(state);
        } else if (packet instanceof Packets.ChatMessage chat) {
            if (listener != null) listener.onChatMessage(chat);
        } else if (packet instanceof Packets.DamageEventPacket damage) {
            handleDamageEvent(damage);
        }
    }

    private void handleWorldSnapshot(Packets.WorldSnapshotPacket snapshot) {
        // Store in interpolation buffer
        if (listener != null) {
            listener.onWorldSnapshot(snapshot);
        }
    }

    private void handleEntityState(Packets.EntityStatePacket state) {
        // Store for reconciliation
        stateBuffer[stateBufferHead] = state;
        stateBufferHead = (stateBufferHead + 1) % stateBuffer.length;

        // Check if this is our player entity
        // If so, reconcile with predicted state
    }

    private void handleDamageEvent(Packets.DamageEventPacket damage) {
        // TODO: Play damage effects
    }

    public void sendInput(Packets.ShipInputPacket input) {
        input.sequenceNumber = currentInputSequence++;

        // Store for prediction reconciliation
        inputHistory[input.sequenceNumber % inputHistory.length] = input;

        // Send to server
        client.sendUDP(input);
    }

    public void sendReliable(Object packet) {
        if (connected) {
            client.sendTCP(packet);
        }
    }

    public void sendUnreliable(Object packet) {
        if (connected) {
            client.sendUDP(packet);
        }
    }

    public void setListener(ClientListener listener) {
        this.listener = listener;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getLatency() {
        return client != null ? client.getReturnTripTime() : -1;
    }
}
