package com.astral.utils;

/**
 * Game configuration settings
 */
public class GameConfig {

    // Graphics settings
    public int targetFps = 60;
    public boolean vsync = true;
    public float fov = 75f;
    public int shadowQuality = 2; // 0=off, 1=low, 2=medium, 3=high
    public int textureQuality = 2;
    public boolean bloom = true;
    public boolean motionBlur = false;
    public int antiAliasing = 4; // MSAA samples

    // Audio settings
    public float masterVolume = 1.0f;
    public float musicVolume = 0.7f;
    public float sfxVolume = 1.0f;
    public float voiceVolume = 1.0f;

    // Controls
    public float mouseSensitivity = 0.3f;
    public boolean invertY = false;
    public boolean invertFlightY = false;

    // Gameplay
    public int difficulty = 1; // 0=easy, 1=normal, 2=hard
    public boolean showHints = true;
    public boolean autosave = true;
    public int autosaveInterval = 300; // seconds

    // World generation
    public long worldSeed = System.currentTimeMillis(); // Random seed by default

    // Network
    public String playerName = "Pilot";
    public int serverPort = 54555;
    public int udpPort = 54777;

    // Debug
    public boolean debugMode = false;
    public boolean showFps = true;
    public boolean showPhysicsDebug = false;

    public GameConfig() {
        // Load from file if exists
        load();
    }

    public void load() {
        // TODO: Load from JSON config file
    }

    public void save() {
        // TODO: Save to JSON config file
    }

    public long getWorldSeed() {
        return worldSeed;
    }

    public void setWorldSeed(long seed) {
        this.worldSeed = seed;
    }
}
