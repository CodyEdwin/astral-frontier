package com.astral.procedural;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.concurrent.*;

/**
 * Manages terrain chunk loading and streaming for infinite procedural terrain.
 * Loads chunks around the player and unloads distant ones.
 */
public class ChunkManager implements Disposable {

    // Chunk specifications
    private static final int CHUNK_RESOLUTION = 64;    // Vertices per side
    private static final float CELL_SIZE = 4f;         // Meters per cell
    private static final int CHUNK_SIZE = CHUNK_RESOLUTION; // Vertices per chunk
    private static final float CHUNK_WORLD_SIZE = (CHUNK_RESOLUTION - 1) * CELL_SIZE; // 252m per chunk
    private static final int LOAD_RADIUS = 5;          // Chunks around player
    private static final float UNLOAD_DELAY = 3f;      // Seconds before unload
    private static final int MAX_LOADS_PER_FRAME = 2;  // Limit mesh builds

    // Chunk storage
    private final ObjectMap<Long, TerrainChunk> loadedChunks = new ObjectMap<>();
    private final ObjectMap<Long, Float> pendingUnload = new ObjectMap<>();
    private final Array<ChunkLoadRequest> loadQueue = new Array<>();
    private final Array<TerrainChunk> pendingMeshBuild = new Array<>();

    // Vegetation per chunk
    private final ObjectMap<Long, Array<VegetationGenerator.VegetationInstance>> chunkVegetation = new ObjectMap<>();
    private final Array<Long> pendingVegetation = new Array<>();

    // Structures per chunk
    private final ObjectMap<Long, Array<StructureGenerator.StructurePlacement>> chunkStructures = new ObjectMap<>();
    private final ObjectMap<Long, Array<com.badlogic.gdx.graphics.g3d.ModelInstance>> chunkStructureInstances = new ObjectMap<>();
    private final Array<Long> pendingStructures = new Array<>();

    // Thread pool for async heightmap generation
    private final ExecutorService threadPool;

    // Generators
    private final TerrainGenerator terrainGenerator;
    private final VegetationGenerator vegetationGenerator;
    private final StructureGenerator structureGenerator;
    private final long planetSeed;
    private final PlanetType planetType;

    // Animation time
    private float animTime = 0f;

    // State
    private final Vector3 lastPlayerPos = new Vector3(Float.MAX_VALUE, 0, Float.MAX_VALUE);
    private boolean enabled = false;
    private int loadedCount = 0;

    // Debug stats
    private int totalChunksGenerated = 0;

    public ChunkManager(long planetSeed, PlanetType planetType) {
        this.planetSeed = planetSeed;
        this.planetType = planetType;
        this.terrainGenerator = new TerrainGenerator(planetSeed);
        this.vegetationGenerator = new VegetationGenerator(planetSeed);
        this.structureGenerator = new StructureGenerator(planetSeed);
        this.threadPool = Executors.newFixedThreadPool(2);

        Gdx.app.log("ChunkManager", "Initialized for " + planetType.displayName +
            " (seed: " + planetSeed + ", chunk size: " + CHUNK_WORLD_SIZE + "m)");
    }

    /**
     * Enable terrain streaming
     */
    public void enable() {
        enabled = true;
        lastPlayerPos.set(Float.MAX_VALUE, 0, Float.MAX_VALUE);
        Gdx.app.log("ChunkManager", "Terrain streaming enabled");
    }

    /**
     * Disable and unload all chunks
     */
    public void disable() {
        enabled = false;
        unloadAllChunks();
        Gdx.app.log("ChunkManager", "Terrain streaming disabled");
    }

    /**
     * Update chunk loading based on player position
     */
    public void update(Vector3 playerPosition, float deltaTime) {
        if (!enabled) return;

        // Update animation time
        animTime += deltaTime;

        // Check if player moved enough to update chunks
        float moveDist = playerPosition.dst(lastPlayerPos);

        if (moveDist > CHUNK_WORLD_SIZE * 0.25f || loadedChunks.size == 0) {
            lastPlayerPos.set(playerPosition);
            updateChunkLoading(playerPosition);
        }

        // Process pending unloads
        processPendingUnloads(deltaTime);

        // Build meshes on main thread (must be done on GL thread)
        processMeshBuilds();

        // Generate vegetation for newly built chunks
        processVegetationGeneration();

        // Generate structures for newly built chunks
        processStructureGeneration();

        // Update vegetation animations
        updateVegetationAnimations();

        // Process async load results
        processLoadQueue();
    }

    private void processVegetationGeneration() {
        // Generate vegetation for chunks that just had meshes built
        int generated = 0;
        while (pendingVegetation.size > 0 && generated < 2) {
            long key = pendingVegetation.removeIndex(0);
            TerrainChunk chunk = loadedChunks.get(key);
            if (chunk != null && chunk.isMeshBuilt()) {
                Array<VegetationGenerator.VegetationInstance> veg = vegetationGenerator.generateForChunk(
                    chunk.chunkX, chunk.chunkZ, CHUNK_WORLD_SIZE, planetType, this::getHeightAt
                );
                chunkVegetation.put(key, veg);
                generated++;
            }
        }
    }

    private void processStructureGeneration() {
        // Generate structures for chunks that just had meshes built
        int generated = 0;
        while (pendingStructures.size > 0 && generated < 1) {
            long key = pendingStructures.removeIndex(0);
            TerrainChunk chunk = loadedChunks.get(key);
            if (chunk != null && chunk.isMeshBuilt() && !chunkStructures.containsKey(key)) {
                Array<StructureGenerator.StructurePlacement> placements = structureGenerator.generateForChunk(
                    chunk.chunkX, chunk.chunkZ, CHUNK_WORLD_SIZE, planetType, this::getHeightAt
                );
                chunkStructures.put(key, placements);

                // Create model instances for structures
                Array<com.badlogic.gdx.graphics.g3d.ModelInstance> instances = new Array<>();
                for (StructureGenerator.StructurePlacement p : placements) {
                    com.badlogic.gdx.graphics.g3d.ModelInstance inst = structureGenerator.createInstance(p);
                    if (inst != null) {
                        instances.add(inst);
                    }
                }
                chunkStructureInstances.put(key, instances);
                generated++;
            }
        }
    }

    private void updateVegetationAnimations() {
        for (Array<VegetationGenerator.VegetationInstance> vegList : chunkVegetation.values()) {
            for (VegetationGenerator.VegetationInstance veg : vegList) {
                veg.update(animTime);
            }
        }
    }

    private void updateChunkLoading(Vector3 playerPos) {
        int playerChunkX = (int) Math.floor(playerPos.x / CHUNK_WORLD_SIZE);
        int playerChunkZ = (int) Math.floor(playerPos.z / CHUNK_WORLD_SIZE);

        // Determine which chunks should be loaded
        Array<Long> shouldBeLoaded = new Array<>();

        for (int dx = -LOAD_RADIUS; dx <= LOAD_RADIUS; dx++) {
            for (int dz = -LOAD_RADIUS; dz <= LOAD_RADIUS; dz++) {
                float dist = (float) Math.sqrt(dx * dx + dz * dz);
                if (dist <= LOAD_RADIUS) {
                    int cx = playerChunkX + dx;
                    int cz = playerChunkZ + dz;
                    long key = chunkKey(cx, cz);
                    shouldBeLoaded.add(key);

                    // Queue for loading if not loaded and not queued
                    if (!loadedChunks.containsKey(key) && !isInLoadQueue(cx, cz)) {
                        float priority = 1f / (dist + 1f);
                        loadQueue.add(new ChunkLoadRequest(cx, cz, priority));
                    }

                    // Cancel pending unload
                    pendingUnload.remove(key);
                }
            }
        }

        // Sort by priority (closest first)
        loadQueue.sort((a, b) -> Float.compare(b.priority, a.priority));

        // Mark far chunks for unload
        for (ObjectMap.Entry<Long, TerrainChunk> entry : loadedChunks) {
            if (!shouldBeLoaded.contains(entry.key, false)) {
                if (!pendingUnload.containsKey(entry.key)) {
                    pendingUnload.put(entry.key, UNLOAD_DELAY);
                }
            }
        }
    }

    private boolean isInLoadQueue(int cx, int cz) {
        for (ChunkLoadRequest req : loadQueue) {
            if (req.chunkX == cx && req.chunkZ == cz) return true;
        }
        return false;
    }

    private void processLoadQueue() {
        int toProcess = Math.min(MAX_LOADS_PER_FRAME, loadQueue.size);

        for (int i = 0; i < toProcess; i++) {
            ChunkLoadRequest request = loadQueue.removeIndex(0);
            long key = chunkKey(request.chunkX, request.chunkZ);

            // Skip if already loading/loaded
            if (loadedChunks.containsKey(key)) continue;

            loadChunkAsync(request.chunkX, request.chunkZ);
        }
    }

    private void loadChunkAsync(int chunkX, int chunkZ) {
        TerrainChunk chunk = new TerrainChunk(chunkX, chunkZ, CHUNK_SIZE, CELL_SIZE, planetType);
        chunk.isLoading = true;

        // Add to loaded chunks immediately (marked as loading)
        loadedChunks.put(chunkKey(chunkX, chunkZ), chunk);

        // Generate heightmap on background thread
        threadPool.submit(() -> {
            try {
                TerrainGenerator.NoiseConfig config = getNoiseConfig();
                // Pass chunk coordinates directly - generateHeightmap handles the scaling
                Vector2 chunkOffset = new Vector2(chunkX, chunkZ);

                float[][] heightmap = terrainGenerator.generateHeightmap(config, CHUNK_RESOLUTION, chunkOffset);
                chunk.setHeightmap(heightmap);
                chunk.isLoading = false;

                // Queue for mesh building on GL thread
                synchronized (pendingMeshBuild) {
                    pendingMeshBuild.add(chunk);
                }

                totalChunksGenerated++;
            } catch (Exception e) {
                Gdx.app.error("ChunkManager", "Failed to generate chunk " + chunkX + "," + chunkZ, e);
                chunk.isLoading = false;
            }
        });
    }

    private void processMeshBuilds() {
        synchronized (pendingMeshBuild) {
            if (pendingMeshBuild.size > 0) {
                Gdx.app.log("ChunkManager", "processMeshBuilds: " + pendingMeshBuild.size + " chunks pending");
            }
            int built = 0;
            while (pendingMeshBuild.size > 0 && built < MAX_LOADS_PER_FRAME) {
                TerrainChunk chunk = pendingMeshBuild.removeIndex(0);
                Gdx.app.log("ChunkManager", "Building mesh for chunk " + chunk.chunkX + "," + chunk.chunkZ +
                    " isLoading=" + chunk.isLoading + " isMeshBuilt=" + chunk.isMeshBuilt());
                if (!chunk.isLoading && !chunk.isMeshBuilt()) {
                    chunk.buildMesh();
                    loadedCount++;
                    built++;

                    // Queue for vegetation and structure generation
                    long key = chunkKey(chunk.chunkX, chunk.chunkZ);
                    if (!pendingVegetation.contains(key, false)) {
                        pendingVegetation.add(key);
                    }
                    if (!pendingStructures.contains(key, false)) {
                        pendingStructures.add(key);
                    }
                }
            }
        }
    }

    private void processPendingUnloads(float deltaTime) {
        Array<Long> toRemove = new Array<>();

        for (ObjectMap.Entry<Long, Float> entry : pendingUnload) {
            float remaining = entry.value - deltaTime;
            if (remaining <= 0) {
                unloadChunk(entry.key);
                toRemove.add(entry.key);
            } else {
                pendingUnload.put(entry.key, remaining);
            }
        }

        for (Long key : toRemove) {
            pendingUnload.remove(key);
        }
    }

    private void unloadChunk(long key) {
        TerrainChunk chunk = loadedChunks.remove(key);
        if (chunk != null) {
            if (chunk.isMeshBuilt()) {
                loadedCount--;
            }
            chunk.dispose();
        }
        // Remove vegetation for this chunk
        chunkVegetation.remove(key);
        // Remove structures for this chunk
        chunkStructures.remove(key);
        chunkStructureInstances.remove(key);
    }

    private void unloadAllChunks() {
        for (TerrainChunk chunk : loadedChunks.values()) {
            chunk.dispose();
        }
        loadedChunks.clear();
        loadQueue.clear();
        pendingUnload.clear();
        chunkVegetation.clear();
        pendingVegetation.clear();
        chunkStructures.clear();
        chunkStructureInstances.clear();
        pendingStructures.clear();
        synchronized (pendingMeshBuild) {
            pendingMeshBuild.clear();
        }
        loadedCount = 0;
    }

    private TerrainGenerator.NoiseConfig getNoiseConfig() {
        return switch (planetType) {
            case DESERT -> TerrainGenerator.DESERT;
            case ROCKY -> TerrainGenerator.ROCKY;
            case ICE -> TerrainGenerator.ICE;
            case LAVA -> TerrainGenerator.VOLCANIC;
            case FOREST -> TerrainGenerator.EARTH_LIKE;
            case OCEAN -> TerrainGenerator.EARTH_LIKE;
            default -> TerrainGenerator.ROCKY;
        };
    }

    /**
     * Get terrain height at world position
     */
    public float getHeightAt(float worldX, float worldZ) {
        int chunkX = (int) Math.floor(worldX / CHUNK_WORLD_SIZE);
        int chunkZ = (int) Math.floor(worldZ / CHUNK_WORLD_SIZE);

        TerrainChunk chunk = loadedChunks.get(chunkKey(chunkX, chunkZ));
        if (chunk != null && chunk.isMeshBuilt()) {
            float localX = worldX - chunk.getWorldX();
            float localZ = worldZ - chunk.getWorldZ();
            return chunk.getHeightAt(localX, localZ);
        }

        return 0;
    }

    /**
     * Render all visible chunks and vegetation
     */
    public void render(ModelBatch modelBatch, Environment environment) {
        // Render terrain
        for (TerrainChunk chunk : loadedChunks.values()) {
            if (chunk.isMeshBuilt() && chunk.isVisible && chunk.getModelInstance() != null) {
                modelBatch.render(chunk.getModelInstance(), environment);

                // Render desert features (cacti, rocks, etc.)
                for (com.badlogic.gdx.graphics.g3d.ModelInstance feature : chunk.getFeatureInstances()) {
                    modelBatch.render(feature, environment);
                }
            }
        }

        // Render vegetation
        for (Array<VegetationGenerator.VegetationInstance> vegList : chunkVegetation.values()) {
            for (VegetationGenerator.VegetationInstance veg : vegList) {
                modelBatch.render(veg.modelInstance, environment);
            }
        }

        // Render structures
        for (Array<com.badlogic.gdx.graphics.g3d.ModelInstance> structList : chunkStructureInstances.values()) {
            for (com.badlogic.gdx.graphics.g3d.ModelInstance inst : structList) {
                modelBatch.render(inst, environment);
            }
        }
    }

    /**
     * Get loaded chunk count
     */
    public int getLoadedChunkCount() {
        return loadedCount;
    }

    /**
     * Get pending load count
     */
    public int getPendingCount() {
        synchronized (pendingMeshBuild) {
            return loadQueue.size + pendingMeshBuild.size;
        }
    }

    /**
     * Get total chunks generated
     */
    public int getTotalChunksGenerated() {
        return totalChunksGenerated;
    }

    /**
     * Get chunk world size in meters
     */
    public float getChunkWorldSize() {
        return CHUNK_WORLD_SIZE;
    }

    private static long chunkKey(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    @Override
    public void dispose() {
        disable();
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(1, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
        }
        if (vegetationGenerator != null) {
            vegetationGenerator.dispose();
        }
        Gdx.app.log("ChunkManager", "Disposed. Total chunks generated: " + totalChunksGenerated);
    }

    /**
     * Chunk load request with priority
     */
    private static class ChunkLoadRequest {
        final int chunkX, chunkZ;
        final float priority;

        ChunkLoadRequest(int chunkX, int chunkZ, float priority) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.priority = priority;
        }
    }
}
