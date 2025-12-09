package com.astral.rpg;

/**
 * Container for all game state that needs to be saved/loaded
 */
public class GameState {

    private long totalPlayTime = 0;
    private long sessionStartTime;

    // Player state
    private String playerName = "Commander";
    private int playerLevel = 1;
    private long playerExperience = 0;
    private long playerCredits = 5000;

    // Current location
    private long universeSeed = System.currentTimeMillis();
    private String currentSystemId;
    private String currentPlanetId;

    public GameState() {
        sessionStartTime = System.currentTimeMillis();
    }

    public long getTotalPlayTime() {
        return totalPlayTime + (System.currentTimeMillis() - sessionStartTime);
    }

    public void setTotalPlayTime(long time) {
        this.totalPlayTime = time;
        this.sessionStartTime = System.currentTimeMillis();
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public int getPlayerLevel() {
        return playerLevel;
    }

    public void setPlayerLevel(int level) {
        this.playerLevel = level;
    }

    public long getPlayerExperience() {
        return playerExperience;
    }

    public void setPlayerExperience(long experience) {
        this.playerExperience = experience;
    }

    public long getPlayerCredits() {
        return playerCredits;
    }

    public void setPlayerCredits(long credits) {
        this.playerCredits = credits;
    }

    public void addCredits(long amount) {
        this.playerCredits += amount;
    }

    public boolean spendCredits(long amount) {
        if (playerCredits >= amount) {
            playerCredits -= amount;
            return true;
        }
        return false;
    }

    public long getUniverseSeed() {
        return universeSeed;
    }

    public void setUniverseSeed(long seed) {
        this.universeSeed = seed;
    }

    public String getCurrentSystemId() {
        return currentSystemId;
    }

    public void setCurrentSystemId(String id) {
        this.currentSystemId = id;
    }

    public String getCurrentPlanetId() {
        return currentPlanetId;
    }

    public void setCurrentPlanetId(String id) {
        this.currentPlanetId = id;
    }
}
