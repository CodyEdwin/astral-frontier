package com.astral.planetary;

/**
 * Configuration for biomes.
 */
public class BiomeConfig {
    private String name;
    private float temperature;
    private float humidity;

    // Getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public float getTemperature() { return temperature; }
    public void setTemperature(float temperature) { this.temperature = temperature; }

    public float getHumidity() { return humidity; }
    public void setHumidity(float humidity) { this.humidity = humidity; }
}