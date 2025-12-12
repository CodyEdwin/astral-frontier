package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.Random;

/**
 * AtmosphericEffects - Manages weather, fog, particles, and atmospheric conditions
 * Creates immersive planetary environments with dynamic weather systems.
 */
public class AtmosphericEffects implements Disposable {

    private final long seed;
    private final Random random;
    private final PlanetType planetType;

    // Fog settings
    private boolean fogEnabled = true;
    private Color fogColor;
    private float fogDensity = 0.015f;
    private float fogStart = 10f;
    private float fogEnd = 200f;

    // Weather
    private WeatherType currentWeather;
    private float weatherIntensity = 1.0f;
    private float weatherTimer = 0f;
    private float weatherDuration = 60f; // seconds

    // Particle systems
    private Array<AtmosphericParticle> particles;
    private int maxParticles = 500;
    private float particleSpawnRate = 0.1f;
    private float particleTimer = 0f;

    // Wind
    private Vector3 windDirection;
    private float windSpeed = 2.0f;

    // Time of day (0-1, where 0.5 is noon)
    private float timeOfDay = 0.5f;
    private float dayNightCycleSpeed = 0.01f; // How fast day/night changes

    // Sky colors
    private Color skyColorDay;
    private Color skyColorNight;
    private Color currentSkyColor;

    // Rendering
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;

    public enum WeatherType {
        CLEAR,
        DUST_STORM, // Desert
        SNOW, // Ice
        RAIN, // Forest/Ocean
        ASH_FALL, // Lava
        FOG, // Any
        WIND, // Any
    }

    public AtmosphericEffects(long seed, PlanetType planetType) {
        this.seed = seed;
        this.random = new Random(seed);
        this.planetType = planetType;
        this.particles = new Array<>();
        this.windDirection = new Vector3(1, 0, 1).nor();
        this.shapeRenderer = new ShapeRenderer();
        this.spriteBatch = new SpriteBatch();

        initializeAtmosphere();
        selectInitialWeather();
    }

    /**
     * Initialize atmosphere based on planet type
     */
    private void initializeAtmosphere() {
        switch (planetType) {
            case DESERT:
                fogColor = new Color(0.8f, 0.7f, 0.5f, 1f);
                skyColorDay = new Color(0.9f, 0.8f, 0.6f, 1f);
                skyColorNight = new Color(0.2f, 0.15f, 0.1f, 1f);
                fogDensity = 0.008f;
                fogEnd = 300f;
                break;
            case ICE:
                fogColor = new Color(0.9f, 0.95f, 1.0f, 1f);
                skyColorDay = new Color(0.8f, 0.9f, 1.0f, 1f);
                skyColorNight = new Color(0.05f, 0.1f, 0.15f, 1f);
                fogDensity = 0.012f;
                fogEnd = 250f;
                break;
            case LAVA:
                fogColor = new Color(0.5f, 0.2f, 0.1f, 1f);
                skyColorDay = new Color(0.6f, 0.3f, 0.2f, 1f);
                skyColorNight = new Color(0.3f, 0.1f, 0.05f, 1f);
                fogDensity = 0.02f;
                fogEnd = 150f;
                break;
            case FOREST:
                fogColor = new Color(0.7f, 0.8f, 0.75f, 1f);
                skyColorDay = new Color(0.6f, 0.8f, 1.0f, 1f);
                skyColorNight = new Color(0.05f, 0.08f, 0.12f, 1f);
                fogDensity = 0.01f;
                fogEnd = 200f;
                break;
            case OCEAN:
                fogColor = new Color(0.8f, 0.85f, 0.9f, 1f);
                skyColorDay = new Color(0.5f, 0.7f, 1.0f, 1f);
                skyColorNight = new Color(0.02f, 0.05f, 0.1f, 1f);
                fogDensity = 0.015f;
                fogEnd = 180f;
                break;
        }

        currentSkyColor = new Color(skyColorDay);
    }

    /**
     * Select initial weather based on planet type
     */
    private void selectInitialWeather() {
        switch (planetType) {
            case DESERT:
                currentWeather = random.nextFloat() < 0.3f
                    ? WeatherType.DUST_STORM
                    : WeatherType.CLEAR;
                break;
            case ICE:
                currentWeather = random.nextFloat() < 0.4f
                    ? WeatherType.SNOW
                    : WeatherType.CLEAR;
                break;
            case LAVA:
                currentWeather = random.nextFloat() < 0.5f
                    ? WeatherType.ASH_FALL
                    : WeatherType.CLEAR;
                break;
            case FOREST:
                currentWeather = random.nextFloat() < 0.3f
                    ? WeatherType.RAIN
                    : WeatherType.CLEAR;
                break;
            case OCEAN:
                currentWeather = random.nextFloat() < 0.4f
                    ? WeatherType.RAIN
                    : WeatherType.CLEAR;
                break;
        }

        weatherIntensity = 0.5f + random.nextFloat() * 0.5f;
    }

    /**
     * Update atmospheric effects
     */
    public void update(
        float delta,
        Vector3 playerPosition,
        PerspectiveCamera camera
    ) {
        // Update time of day
        timeOfDay += dayNightCycleSpeed * delta;
        if (timeOfDay > 1.0f) timeOfDay -= 1.0f;

        updateSkyColor();

        // Update weather
        weatherTimer += delta;
        if (weatherTimer >= weatherDuration) {
            changeWeather();
            weatherTimer = 0f;
        }

        // Update wind
        windDirection.rotate(Vector3.Y, delta * 2f); // Slowly rotate wind direction

        // Update particles
        updateParticles(delta, playerPosition, camera);

        // Spawn new particles based on weather
        if (currentWeather != WeatherType.CLEAR) {
            particleTimer += delta;
            if (particleTimer >= particleSpawnRate) {
                spawnWeatherParticles(playerPosition, camera);
                particleTimer = 0f;
            }
        }
    }

    /**
     * Update sky color based on time of day
     */
    private void updateSkyColor() {
        // Simple day/night cycle
        // 0.0 = midnight, 0.25 = dawn, 0.5 = noon, 0.75 = dusk, 1.0 = midnight

        float dayNightBlend;
        if (timeOfDay < 0.25f) {
            // Night to dawn
            dayNightBlend = timeOfDay * 4f;
        } else if (timeOfDay < 0.75f) {
            // Day
            dayNightBlend = 1.0f;
        } else {
            // Dusk to night
            dayNightBlend = 1.0f - ((timeOfDay - 0.75f) * 4f);
        }

        currentSkyColor.set(skyColorNight).lerp(skyColorDay, dayNightBlend);
    }

    /**
     * Change to a new weather pattern
     */
    private void changeWeather() {
        WeatherType[] possibleWeather = getPossibleWeather();
        currentWeather = possibleWeather[random.nextInt(
            possibleWeather.length
        )];
        weatherIntensity = 0.3f + random.nextFloat() * 0.7f;
        weatherDuration = 30f + random.nextFloat() * 60f;
    }

    /**
     * Get possible weather types for current planet
     */
    private WeatherType[] getPossibleWeather() {
        switch (planetType) {
            case DESERT:
                return new WeatherType[] {
                    WeatherType.CLEAR,
                    WeatherType.DUST_STORM,
                    WeatherType.WIND,
                };
            case ICE:
                return new WeatherType[] {
                    WeatherType.CLEAR,
                    WeatherType.SNOW,
                    WeatherType.WIND,
                    WeatherType.FOG,
                };
            case LAVA:
                return new WeatherType[] {
                    WeatherType.CLEAR,
                    WeatherType.ASH_FALL,
                    WeatherType.FOG,
                };
            case FOREST:
                return new WeatherType[] {
                    WeatherType.CLEAR,
                    WeatherType.RAIN,
                    WeatherType.FOG,
                };
            case OCEAN:
                return new WeatherType[] {
                    WeatherType.CLEAR,
                    WeatherType.RAIN,
                    WeatherType.FOG,
                    WeatherType.WIND,
                };
            default:
                return new WeatherType[] {
                    WeatherType.CLEAR,
                    WeatherType.WIND,
                };
        }
    }

    /**
     * Spawn weather-appropriate particles
     */
    private void spawnWeatherParticles(
        Vector3 playerPosition,
        PerspectiveCamera camera
    ) {
        if (particles.size >= maxParticles) return;

        // Spawn in a circle around the player
        float spawnRadius = 50f;
        float angle = random.nextFloat() * MathUtils.PI2;
        float distance = 10f + random.nextFloat() * spawnRadius;

        float x = playerPosition.x + MathUtils.cos(angle) * distance;
        float z = playerPosition.z + MathUtils.sin(angle) * distance;
        float y = playerPosition.y + 10f + random.nextFloat() * 30f;

        AtmosphericParticle particle = new AtmosphericParticle();
        particle.position.set(x, y, z);
        particle.lifetime = 5f + random.nextFloat() * 10f;
        particle.age = 0f;

        switch (currentWeather) {
            case DUST_STORM:
                particle.velocity
                    .set(windDirection)
                    .scl(windSpeed * 3f * weatherIntensity);
                particle.velocity.y = -0.5f - random.nextFloat() * 0.5f;
                particle.size = 0.3f + random.nextFloat() * 0.5f;
                particle.color.set(0.8f, 0.7f, 0.5f, 0.6f * weatherIntensity);
                break;
            case SNOW:
                particle.velocity.set(windDirection).scl(windSpeed * 0.5f);
                particle.velocity.y = -1f - random.nextFloat() * 1f;
                particle.size = 0.1f + random.nextFloat() * 0.2f;
                particle.color.set(1f, 1f, 1f, 0.8f * weatherIntensity);
                particle.rotation = random.nextFloat() * 360f;
                particle.rotationSpeed = (random.nextFloat() - 0.5f) * 90f;
                break;
            case RAIN:
                particle.velocity.set(windDirection).scl(windSpeed);
                particle.velocity.y = -10f - random.nextFloat() * 5f;
                particle.size = 0.05f;
                particle.color.set(0.7f, 0.8f, 0.9f, 0.5f * weatherIntensity);
                particle.isRainDrop = true;
                break;
            case ASH_FALL:
                particle.velocity.set(windDirection).scl(windSpeed * 0.3f);
                particle.velocity.y = -0.5f - random.nextFloat() * 0.5f;
                particle.size = 0.2f + random.nextFloat() * 0.3f;
                particle.color.set(0.3f, 0.3f, 0.3f, 0.7f * weatherIntensity);
                break;
            case FOG:
                particle.velocity.set(windDirection).scl(windSpeed * 0.2f);
                particle.velocity.y = 0f;
                particle.size = 2f + random.nextFloat() * 3f;
                particle.color.set(
                    fogColor.r,
                    fogColor.g,
                    fogColor.b,
                    0.3f * weatherIntensity
                );
                particle.lifetime = 15f + random.nextFloat() * 15f;
                break;
            default:
                return;
        }

        particles.add(particle);
    }

    /**
     * Update all particles
     */
    private void updateParticles(
        float delta,
        Vector3 playerPosition,
        PerspectiveCamera camera
    ) {
        for (int i = particles.size - 1; i >= 0; i--) {
            AtmosphericParticle particle = particles.get(i);
            particle.age += delta;

            // Remove old or distant particles
            if (
                particle.age >= particle.lifetime ||
                particle.position.dst(playerPosition) > 100f
            ) {
                particles.removeIndex(i);
                continue;
            }

            // Update position
            particle.position.add(
                particle.velocity.x * delta,
                particle.velocity.y * delta,
                particle.velocity.z * delta
            );

            // Update rotation
            particle.rotation += particle.rotationSpeed * delta;

            // Fade out at end of lifetime
            float lifetimePercent = particle.age / particle.lifetime;
            if (lifetimePercent > 0.8f) {
                float fadeOut = 1.0f - ((lifetimePercent - 0.8f) / 0.2f);
                particle.color.a *= fadeOut;
            }
        }
    }

    /**
     * Render atmospheric particles
     */
    public void renderParticles(PerspectiveCamera camera) {
        if (particles.size == 0) return;

        com.badlogic.gdx.Gdx.gl.glEnable(GL20.GL_BLEND);
        com.badlogic.gdx.Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );
        com.badlogic.gdx.Gdx.gl.glDepthMask(false);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Project 3D particle positions to 2D screen space
        Vector3 screenPos = new Vector3();

        for (AtmosphericParticle particle : particles) {
            // Project particle position to screen coordinates
            screenPos.set(particle.position);
            camera.project(screenPos);

            // Skip if behind camera
            if (screenPos.z < 0 || screenPos.z > 1) continue;

            shapeRenderer.setColor(particle.color);

            if (particle.isRainDrop) {
                // Rain drops are lines - project end point too
                Vector3 endPos = new Vector3(particle.position).add(
                    particle.velocity.x * 0.1f,
                    particle.velocity.y * 0.1f,
                    particle.velocity.z * 0.1f
                );
                camera.project(endPos);

                shapeRenderer.rectLine(
                    screenPos.x,
                    screenPos.y,
                    endPos.x,
                    endPos.y,
                    particle.size * 2f
                );
            } else {
                // Other particles are circles - scale size based on distance
                float distanceScale =
                    1.0f /
                    (1.0f + particle.position.dst(camera.position) * 0.01f);
                float screenSize = particle.size * 10f * distanceScale;

                shapeRenderer.circle(screenPos.x, screenPos.y, screenSize, 16);
            }
        }

        shapeRenderer.end();

        com.badlogic.gdx.Gdx.gl.glDepthMask(true);
        com.badlogic.gdx.Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Render fog overlay
     */
    public void renderFogOverlay(
        PerspectiveCamera camera,
        float screenWidth,
        float screenHeight
    ) {
        if (!fogEnabled || currentWeather == WeatherType.CLEAR) return;

        float fogAlpha = 0.2f;
        if (currentWeather == WeatherType.FOG) {
            fogAlpha = 0.4f * weatherIntensity;
        } else if (currentWeather == WeatherType.DUST_STORM) {
            fogAlpha = 0.3f * weatherIntensity;
        }

        com.badlogic.gdx.Gdx.gl.glEnable(GL20.GL_BLEND);
        com.badlogic.gdx.Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(fogColor.r, fogColor.g, fogColor.b, fogAlpha);
        shapeRenderer.rect(0, 0, screenWidth, screenHeight);
        shapeRenderer.end();

        com.badlogic.gdx.Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    // ==================== Getters and Setters ====================

    public Color getFogColor() {
        return fogColor;
    }

    public float getFogDensity() {
        return (
            fogDensity *
            (currentWeather == WeatherType.FOG ? weatherIntensity * 2f : 1f)
        );
    }

    public float getFogStart() {
        return fogStart;
    }

    public float getFogEnd() {
        return fogEnd;
    }

    public WeatherType getCurrentWeather() {
        return currentWeather;
    }

    public float getWeatherIntensity() {
        return weatherIntensity;
    }

    public Color getCurrentSkyColor() {
        return currentSkyColor;
    }

    public float getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(float time) {
        this.timeOfDay = MathUtils.clamp(time, 0f, 1f);
        updateSkyColor();
    }

    public Vector3 getWindDirection() {
        return windDirection;
    }

    public float getWindSpeed() {
        return windSpeed * weatherIntensity;
    }

    public void setFogEnabled(boolean enabled) {
        this.fogEnabled = enabled;
    }

    public boolean isFogEnabled() {
        return fogEnabled;
    }

    public int getParticleCount() {
        return particles.size;
    }

    public String getWeatherDescription() {
        switch (currentWeather) {
            case CLEAR:
                return "Clear";
            case DUST_STORM:
                return "Dust Storm";
            case SNOW:
                return "Snowing";
            case RAIN:
                return "Raining";
            case ASH_FALL:
                return "Ash Fall";
            case FOG:
                return "Foggy";
            case WIND:
                return "Windy";
            default:
                return "Unknown";
        }
    }

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        particles.clear();
    }

    /**
     * Individual atmospheric particle
     */
    private static class AtmosphericParticle {

        Vector3 position = new Vector3();
        Vector3 velocity = new Vector3();
        Color color = new Color(Color.WHITE);
        float size = 1f;
        float rotation = 0f;
        float rotationSpeed = 0f;
        float lifetime = 10f;
        float age = 0f;
        boolean isRainDrop = false;
    }
}
