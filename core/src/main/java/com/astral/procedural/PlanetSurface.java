package com.astral.procedural;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Represents a landable planet surface with streaming terrain and structures.
 * Uses ChunkManager for infinite procedural terrain.
 */
public class PlanetSurface implements Disposable {

    private final long seed;
    private final PlanetType type;
    private final String name;

    // Chunk-based terrain streaming
    private ChunkManager chunkManager;

    // Structures (generated around spawn point)
    private Array<StructureGenerator.StructurePlacement> structures;
    private Array<ModelInstance> structureInstances;
    private StructureGenerator structureGenerator;
    private boolean structuresGenerated = false;

    // Environment
    private Environment environment;
    private Color skyColor;
    private Color ambientColor;

    // Player tracking for chunk updates
    private final Vector3 playerPosition = new Vector3();

    public PlanetSurface(long seed, PlanetType type, String name) {
        this.seed = seed;
        this.type = type;
        this.name = name;
        this.structureGenerator = new StructureGenerator(seed);
        this.structures = new Array<>();
        this.structureInstances = new Array<>();

        setupEnvironment();
    }

    private void setupEnvironment() {
        environment = new Environment();
        skyColor = type.getSkyColor();

        // Lighting based on planet type
        switch (type) {
            case DESERT -> {
                ambientColor = new Color(0.4f, 0.35f, 0.3f, 1f);
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, ambientColor));
                environment.add(new DirectionalLight().set(1f, 0.95f, 0.8f, -0.5f, -1f, -0.3f));
            }
            case ICE -> {
                ambientColor = new Color(0.5f, 0.55f, 0.6f, 1f);
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, ambientColor));
                environment.add(new DirectionalLight().set(0.9f, 0.95f, 1f, -0.3f, -1f, -0.5f));
            }
            case LAVA -> {
                ambientColor = new Color(0.5f, 0.2f, 0.1f, 1f);
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, ambientColor));
                environment.add(new DirectionalLight().set(1f, 0.5f, 0.2f, -0.4f, -0.8f, -0.4f));
            }
            case FOREST -> {
                ambientColor = new Color(0.3f, 0.4f, 0.3f, 1f);
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, ambientColor));
                environment.add(new DirectionalLight().set(1f, 1f, 0.9f, -0.5f, -1f, -0.3f));
            }
            default -> {
                ambientColor = new Color(0.3f, 0.3f, 0.35f, 1f);
                environment.set(new ColorAttribute(ColorAttribute.AmbientLight, ambientColor));
                environment.add(new DirectionalLight().set(1f, 1f, 1f, -0.5f, -1f, -0.5f));
            }
        }
    }

    /**
     * Initialize the planet surface with chunk streaming
     */
    public void generate() {
        Gdx.app.log("PlanetSurface", "Initializing " + type.displayName + ": " + name + " with chunk streaming");

        // Create chunk manager for streaming terrain
        chunkManager = new ChunkManager(seed, type);
        chunkManager.enable();

        Gdx.app.log("PlanetSurface", "Chunk streaming initialized");
    }

    /**
     * Update terrain streaming based on player position
     */
    public void update(Vector3 playerPos, float deltaTime) {
        playerPosition.set(playerPos);

        // Update chunk loading
        if (chunkManager != null) {
            chunkManager.update(playerPos, deltaTime);
        }

        // Generate structures once chunks near spawn are loaded
        if (!structuresGenerated && chunkManager != null && chunkManager.getLoadedChunkCount() > 0) {
            generateStructuresNearSpawn();
            structuresGenerated = true;
        }
    }

    /**
     * Generate structures around the spawn point
     */
    private void generateStructuresNearSpawn() {
        if (type == PlanetType.DESERT) {
            // Generate structures in a 500m radius around spawn
            structures = structureGenerator.generateDesertStructures(
                500f, 500f,
                this::getHeightAt
            );

            // Create model instances for each structure
            for (StructureGenerator.StructurePlacement placement : structures) {
                ModelInstance instance = new ModelInstance(placement.model);
                instance.transform.setToTranslation(placement.position);
                instance.transform.rotate(Vector3.Y, placement.rotation);
                instance.transform.scale(placement.scale, placement.scale, placement.scale);
                structureInstances.add(instance);
            }

            Gdx.app.log("PlanetSurface", "Generated " + structures.size + " structures");
        }
    }

    /**
     * Get terrain height at world position
     */
    public float getHeightAt(float worldX, float worldZ) {
        if (chunkManager != null) {
            return chunkManager.getHeightAt(worldX, worldZ);
        }
        return 0f;
    }

    /**
     * Render the planet surface
     */
    public void render(ModelBatch modelBatch) {
        // Render terrain chunks
        if (chunkManager != null) {
            chunkManager.render(modelBatch, environment);
        }

        // Render structures
        for (ModelInstance structure : structureInstances) {
            modelBatch.render(structure, environment);
        }
    }

    /**
     * Check collision with structures
     */
    public boolean checkStructureCollision(float x, float z, float radius) {
        for (StructureGenerator.StructurePlacement placement : structures) {
            float dx = x - placement.position.x;
            float dz = z - placement.position.z;
            float dist = (float) Math.sqrt(dx * dx + dz * dz);

            float structureRadius = getStructureRadius(placement.type);

            if (dist < structureRadius + radius) {
                return true;
            }
        }
        return false;
    }

    private float getStructureRadius(String type) {
        return switch (type) {
            case "Great Pyramid" -> 35f;
            case "Lesser Pyramid" -> 15f;
            case "Ancient Temple" -> 25f;
            case "Ancient Sphinx" -> 20f;
            case "Obelisk" -> 3f;
            case "Ancient Ruins" -> 10f;
            default -> 8f;
        };
    }

    // Getters
    public PlanetType getType() { return type; }
    public String getName() { return name; }
    public Color getSkyColor() { return skyColor; }
    public Environment getEnvironment() { return environment; }
    public Array<StructureGenerator.StructurePlacement> getStructures() { return structures; }

    /**
     * Get terrain width (for compatibility - now infinite)
     */
    public float getTerrainWidth() {
        return Float.MAX_VALUE;
    }

    /**
     * Get loaded chunk count for debug display
     */
    public int getLoadedChunkCount() {
        return chunkManager != null ? chunkManager.getLoadedChunkCount() : 0;
    }

    /**
     * Get pending chunk count for debug display
     */
    public int getPendingChunkCount() {
        return chunkManager != null ? chunkManager.getPendingCount() : 0;
    }

    @Override
    public void dispose() {
        if (chunkManager != null) {
            chunkManager.dispose();
        }
        for (StructureGenerator.StructurePlacement placement : structures) {
            if (placement.model != null) placement.model.dispose();
        }
        structures.clear();
        structureInstances.clear();
    }
}
