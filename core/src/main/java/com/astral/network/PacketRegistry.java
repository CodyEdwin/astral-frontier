package com.astral.network;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;

import java.util.UUID;

/**
 * Registers all network packet classes with Kryo
 */
public class PacketRegistry {

    public static void register(Kryo kryo) {
        // Java types
        kryo.register(String.class);
        kryo.register(String[].class);
        kryo.register(int[].class);
        kryo.register(float[].class);
        kryo.register(byte[].class);

        // LibGDX types
        kryo.register(Vector3.class);
        kryo.register(Quaternion.class);

        // UUID
        kryo.register(UUID.class, new UUIDSerializer());

        // Connection packets
        kryo.register(Packets.ConnectRequest.class);
        kryo.register(Packets.ConnectResponse.class);
        kryo.register(Packets.Disconnect.class);

        // Input packets
        kryo.register(Packets.ShipInputPacket.class);
        kryo.register(Packets.PlayerInputPacket.class);
        kryo.register(Packets.FireWeaponPacket.class);
        kryo.register(Packets.InteractPacket.class);

        // State packets
        kryo.register(Packets.EntityStatePacket.class);
        kryo.register(Packets.EntityStatePacket[].class);
        kryo.register(Packets.WorldSnapshotPacket.class);
        kryo.register(Packets.DamageEventPacket.class);
        kryo.register(Packets.SpawnEntityPacket.class);
        kryo.register(Packets.DestroyEntityPacket.class);

        // Chat
        kryo.register(Packets.ChatMessage.class);

        // Quest/Inventory
        kryo.register(Packets.QuestUpdatePacket.class);
        kryo.register(Packets.InventoryUpdatePacket.class);
    }

    /**
     * Custom UUID serializer for Kryo
     */
    public static class UUIDSerializer extends com.esotericsoftware.kryo.Serializer<UUID> {
        @Override
        public void write(Kryo kryo, com.esotericsoftware.kryo.io.Output output, UUID uuid) {
            output.writeLong(uuid.getMostSignificantBits());
            output.writeLong(uuid.getLeastSignificantBits());
        }

        @Override
        public UUID read(Kryo kryo, com.esotericsoftware.kryo.io.Input input, Class<? extends UUID> type) {
            return new UUID(input.readLong(), input.readLong());
        }
    }
}
