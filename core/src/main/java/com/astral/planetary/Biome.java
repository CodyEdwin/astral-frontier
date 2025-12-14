package com.astral.planetary;

import java.util.Random;

/**
 * Represents a biome with flora, fauna, weather.
 */
public class Biome {
    private String name;
    private Random random;

    public Biome(String name) {
        this.name = name;
        this.random = new Random();
    }

    public void generateFlora() {
        // Generate plants
    }

    public void spawnFauna() {
        // Spawn animals
    }

    public void setupWeather() {
        // Initialize weather
    }

    public void updateWeather(float deltaTime) {
        // Update weather
    }

    public void updateFauna(float deltaTime) {
        // Update animals
    }

    public String getName() {
        return name;
    }
}