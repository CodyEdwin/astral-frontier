package com.astral.network;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import java.util.UUID;

/**
 * Network packet definitions
 */
public class Packets {

    // Connection
    public static class ConnectRequest {
        public String playerName;
        public String version;
    }

    public static class ConnectResponse {
        public boolean accepted;
        public String reason;
        public int playerId;
        public UUID playerUUID;
    }

    public static class Disconnect {
        public String reason;
    }

    // Ship input (client → server)
    public static class ShipInputPacket {
        public int entityId;
        public int sequenceNumber;
        public long timestamp;
        public float throttle;
        public float strafe;
        public float vertical;
        public float pitch;
        public float yaw;
        public float roll;
        public boolean boost;
        public boolean brake;
        public boolean primaryFire;
        public boolean secondaryFire;
    }

    // Player input (FPS mode)
    public static class PlayerInputPacket {
        public int entityId;
        public int sequenceNumber;
        public long timestamp;
        public float moveX;
        public float moveZ;
        public float lookYaw;
        public float lookPitch;
        public boolean jump;
        public boolean sprint;
        public boolean crouch;
        public boolean primaryFire;
        public boolean secondaryFire;
        public boolean reload;
        public boolean interact;
    }

    // Fire weapon
    public static class FireWeaponPacket {
        public int entityId;
        public int weaponSlot;
        public Vector3 aimDirection;
        public long timestamp;
    }

    // Interact with object
    public static class InteractPacket {
        public int entityId;
        public int targetEntityId;
        public String interactionType;
    }

    // Entity state (server → client)
    public static class EntityStatePacket {
        public int entityId;
        public int lastProcessedInput;
        public Vector3 position;
        public Quaternion rotation;
        public Vector3 velocity;
        public Vector3 angularVelocity;
        public float health;
        public float shield;
    }

    // Full world state snapshot
    public static class WorldSnapshotPacket {
        public long serverTick;
        public long serverTime;
        public EntityStatePacket[] entities;
    }

    // Damage event
    public static class DamageEventPacket {
        public int targetEntityId;
        public int sourceEntityId;
        public float damage;
        public Vector3 hitPoint;
        public String damageType;
    }

    // Spawn entity
    public static class SpawnEntityPacket {
        public int entityId;
        public String entityType;
        public UUID ownerId;
        public Vector3 position;
        public Quaternion rotation;
        public String dataJson; // Additional entity data
    }

    // Destroy entity
    public static class DestroyEntityPacket {
        public int entityId;
        public String reason;
    }

    // Chat message
    public static class ChatMessage {
        public UUID senderId;
        public String senderName;
        public String message;
        public long timestamp;
        public String channel; // "all", "team", "system"
    }

    // Quest update
    public static class QuestUpdatePacket {
        public String questId;
        public String objectiveId;
        public int progress;
        public boolean completed;
    }

    // Inventory update
    public static class InventoryUpdatePacket {
        public int slot;
        public String itemId;
        public int quantity;
        public String operation; // "add", "remove", "set"
    }
}
