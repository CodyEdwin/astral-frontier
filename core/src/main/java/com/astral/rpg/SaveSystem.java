package com.astral.rpg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.*;
import java.util.UUID;

/**
 * Save/Load system for game state persistence
 */
public class SaveSystem {

    private static final String SAVE_DIRECTORY = "saves/";
    private static final int SAVE_VERSION = 1;

    private final Kryo kryo;

    public SaveSystem() {
        kryo = new Kryo();
        registerClasses();
    }

    private void registerClasses() {
        kryo.register(SaveData.class);
        kryo.register(PlayerSaveData.class);
        kryo.register(ShipSaveData.class);
        kryo.register(InventorySaveData.class);
        kryo.register(QuestSaveData.class);
        kryo.register(WorldSaveData.class);
        kryo.register(ItemSaveData.class);
        kryo.register(ItemSaveData[].class);
        kryo.register(String.class);
        kryo.register(String[].class);
        kryo.register(int[].class);
        kryo.register(float[].class);
        kryo.register(UUID.class);
    }

    public void save(GameState state, String slotName) {
        SaveData data = new SaveData();
        data.version = SAVE_VERSION;
        data.timestamp = System.currentTimeMillis();
        data.playTime = state.getTotalPlayTime();
        data.slotName = slotName;

        // Serialize player state
        data.player = serializePlayer(state);

        // Serialize ship state
        data.ship = serializeShip(state);

        // Serialize inventory
        data.inventory = serializeInventory(state);

        // Serialize quest progress
        data.quests = serializeQuests(state);

        // Serialize world state
        data.world = serializeWorld(state);

        // Ensure save directory exists
        FileHandle saveDir = Gdx.files.local(SAVE_DIRECTORY);
        if (!saveDir.exists()) {
            saveDir.mkdirs();
        }

        // Write to file
        String filePath = SAVE_DIRECTORY + slotName + ".sav";
        try {
            FileHandle file = Gdx.files.local(filePath);
            Output output = new Output(file.write(false));
            kryo.writeObject(output, data);
            output.close();

            Gdx.app.log("SaveSystem", "Game saved to: " + filePath);

        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Failed to save game", e);
        }
    }

    public GameState load(String slotName) {
        String filePath = SAVE_DIRECTORY + slotName + ".sav";
        FileHandle file = Gdx.files.local(filePath);

        if (!file.exists()) {
            Gdx.app.error("SaveSystem", "Save file not found: " + filePath);
            return null;
        }

        try {
            Input input = new Input(file.read());
            SaveData data = kryo.readObject(input, SaveData.class);
            input.close();

            // Version migration if needed
            if (data.version < SAVE_VERSION) {
                data = migrateSave(data);
            }

            // Reconstruct game state
            GameState state = new GameState();
            state.setTotalPlayTime(data.playTime);

            // Restore player
            deserializePlayer(data.player, state);

            // Restore ship
            deserializeShip(data.ship, state);

            // Restore inventory
            deserializeInventory(data.inventory, state);

            // Restore quests
            deserializeQuests(data.quests, state);

            // Restore world
            deserializeWorld(data.world, state);

            Gdx.app.log("SaveSystem", "Game loaded from: " + filePath);
            return state;

        } catch (Exception e) {
            Gdx.app.error("SaveSystem", "Failed to load game", e);
            return null;
        }
    }

    public SaveSlotInfo[] getSaveSlots() {
        FileHandle saveDir = Gdx.files.local(SAVE_DIRECTORY);
        if (!saveDir.exists()) {
            return new SaveSlotInfo[0];
        }

        FileHandle[] files = saveDir.list(".sav");
        SaveSlotInfo[] slots = new SaveSlotInfo[files.length];

        for (int i = 0; i < files.length; i++) {
            try {
                Input input = new Input(files[i].read());
                SaveData data = kryo.readObject(input, SaveData.class);
                input.close();

                slots[i] = new SaveSlotInfo();
                slots[i].slotName = files[i].nameWithoutExtension();
                slots[i].timestamp = data.timestamp;
                slots[i].playTime = data.playTime;
                slots[i].playerName = data.player != null ? data.player.name : "Unknown";
                slots[i].location = data.world != null ? data.world.currentSystemName : "Unknown";

            } catch (Exception e) {
                slots[i] = new SaveSlotInfo();
                slots[i].slotName = files[i].nameWithoutExtension();
                slots[i].corrupted = true;
            }
        }

        return slots;
    }

    public void deleteSave(String slotName) {
        String filePath = SAVE_DIRECTORY + slotName + ".sav";
        FileHandle file = Gdx.files.local(filePath);
        if (file.exists()) {
            file.delete();
            Gdx.app.log("SaveSystem", "Deleted save: " + filePath);
        }
    }

    private SaveData migrateSave(SaveData data) {
        // Handle version migrations here
        Gdx.app.log("SaveSystem", "Migrating save from v" + data.version + " to v" + SAVE_VERSION);
        data.version = SAVE_VERSION;
        return data;
    }

    // Serialization helpers
    private PlayerSaveData serializePlayer(GameState state) {
        PlayerSaveData data = new PlayerSaveData();
        // TODO: Copy player data
        data.name = "Commander";
        data.level = 1;
        data.experience = 0;
        data.credits = 5000;
        return data;
    }

    private ShipSaveData serializeShip(GameState state) {
        ShipSaveData data = new ShipSaveData();
        // TODO: Copy ship data
        data.shipClassId = "fighter_vanguard";
        data.hull = 500;
        data.shield = 300;
        data.fuel = 500;
        return data;
    }

    private InventorySaveData serializeInventory(GameState state) {
        InventorySaveData data = new InventorySaveData();
        // TODO: Copy inventory data
        data.items = new ItemSaveData[0];
        return data;
    }

    private QuestSaveData serializeQuests(GameState state) {
        QuestSaveData data = new QuestSaveData();
        // TODO: Copy quest data
        data.activeQuestIds = new String[0];
        data.completedQuestIds = new String[0];
        return data;
    }

    private WorldSaveData serializeWorld(GameState state) {
        WorldSaveData data = new WorldSaveData();
        // TODO: Copy world data
        data.universeSeed = 12345L;
        data.currentSystemName = "Alpha Centauri";
        return data;
    }

    private void deserializePlayer(PlayerSaveData data, GameState state) {
        // TODO: Restore player from save data
    }

    private void deserializeShip(ShipSaveData data, GameState state) {
        // TODO: Restore ship from save data
    }

    private void deserializeInventory(InventorySaveData data, GameState state) {
        // TODO: Restore inventory from save data
    }

    private void deserializeQuests(QuestSaveData data, GameState state) {
        // TODO: Restore quests from save data
    }

    private void deserializeWorld(WorldSaveData data, GameState state) {
        // TODO: Restore world from save data
    }

    // Data classes
    public static class SaveData {
        public int version;
        public long timestamp;
        public long playTime;
        public String slotName;
        public PlayerSaveData player;
        public ShipSaveData ship;
        public InventorySaveData inventory;
        public QuestSaveData quests;
        public WorldSaveData world;
    }

    public static class PlayerSaveData {
        public String name;
        public int level;
        public long experience;
        public long credits;
        public float health;
        public float[] position;
    }

    public static class ShipSaveData {
        public String shipClassId;
        public float hull;
        public float shield;
        public float fuel;
        public String[] equippedWeapons;
        public String[] installedModules;
    }

    public static class InventorySaveData {
        public ItemSaveData[] items;
    }

    public static class ItemSaveData {
        public String itemId;
        public int quantity;
        public int slot;
        public float durability;
        public String[] modifications;
    }

    public static class QuestSaveData {
        public String[] activeQuestIds;
        public String[] completedQuestIds;
        public int[] objectiveProgress;
    }

    public static class WorldSaveData {
        public long universeSeed;
        public String currentSystemName;
        public String[] discoveredSystems;
        public String[] visitedPlanets;
    }

    public static class SaveSlotInfo {
        public String slotName;
        public long timestamp;
        public long playTime;
        public String playerName;
        public String location;
        public boolean corrupted = false;

        public String getFormattedPlayTime() {
            long hours = playTime / 3600000;
            long minutes = (playTime % 3600000) / 60000;
            return String.format("%dh %dm", hours, minutes);
        }
    }
}
