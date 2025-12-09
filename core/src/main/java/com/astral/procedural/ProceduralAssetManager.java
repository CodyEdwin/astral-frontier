package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.LongMap;

/**
 * Central manager for all procedural assets in the game.
 * Handles generation, caching, and disposal of procedural models and textures.
 */
public class ProceduralAssetManager implements Disposable {

    private final long worldSeed;

    // Generators
    private final SpaceshipGenerator spaceshipGenerator;
    private final CreatureGenerator creatureGenerator;
    private final BuildingGenerator buildingGenerator;

    // Cached assets
    private final LongMap<Model> cachedShips = new LongMap<>();
    private final LongMap<Model> cachedCreatures = new LongMap<>();
    private final LongMap<Model> cachedBuildings = new LongMap<>();
    private final Array<Texture> managedTextures = new Array<>();
    private final Array<Model> managedModels = new Array<>();

    // Player ship
    private Model playerShipModel;
    private ModelInstance playerShipInstance;

    public ProceduralAssetManager(long worldSeed) {
        this.worldSeed = worldSeed;
        this.spaceshipGenerator = new SpaceshipGenerator(worldSeed);
        this.creatureGenerator = new CreatureGenerator(worldSeed);
        this.buildingGenerator = new BuildingGenerator(worldSeed);
    }

    /**
     * Generate and cache the player's ship
     */
    public ModelInstance getPlayerShip(SpaceshipGenerator.ShipClass shipClass,
                                        SpaceshipGenerator.ShipStyle style) {
        if (playerShipModel == null) {
            playerShipModel = spaceshipGenerator.generateShip(shipClass, style, worldSeed);
            playerShipInstance = new ModelInstance(playerShipModel);
        }
        return playerShipInstance;
    }

    /**
     * Get a cached or generate a new NPC ship
     */
    public Model getNPCShip(long shipSeed) {
        Model cached = cachedShips.get(shipSeed);
        if (cached != null) return cached;

        // Determine ship class and style from seed
        java.util.Random random = new java.util.Random(shipSeed);
        SpaceshipGenerator.ShipClass[] classes = SpaceshipGenerator.ShipClass.values();
        SpaceshipGenerator.ShipStyle[] styles = SpaceshipGenerator.ShipStyle.values();

        SpaceshipGenerator.ShipClass shipClass = classes[random.nextInt(classes.length)];
        SpaceshipGenerator.ShipStyle style = styles[random.nextInt(styles.length)];

        Model ship = spaceshipGenerator.generateShip(shipClass, style, shipSeed);
        cachedShips.put(shipSeed, ship);
        return ship;
    }

    /**
     * Generate a creature for a specific planet
     */
    public Model getCreature(PlanetType planetType, long creatureSeed) {
        Model cached = cachedCreatures.get(creatureSeed);
        if (cached != null) return cached;

        Model creature = creatureGenerator.generateCreature(planetType, creatureSeed);
        cachedCreatures.put(creatureSeed, creature);
        return creature;
    }

    /**
     * Generate a specific creature type
     */
    public Model getCreature(CreatureGenerator.CreatureType type,
                             CreatureGenerator.CreatureSize size,
                             PlanetType planetType, long creatureSeed) {
        long key = creatureSeed ^ (type.ordinal() * 31L) ^ (size.ordinal() * 17L);
        Model cached = cachedCreatures.get(key);
        if (cached != null) return cached;

        Model creature = creatureGenerator.generateCreature(type, size, planetType, creatureSeed);
        cachedCreatures.put(key, creature);
        return creature;
    }

    /**
     * Generate a building for a specific planet
     */
    public Model getBuilding(PlanetType planetType, long buildingSeed) {
        Model cached = cachedBuildings.get(buildingSeed);
        if (cached != null) return cached;

        Model building = buildingGenerator.generateBuilding(planetType, buildingSeed);
        cachedBuildings.put(buildingSeed, building);
        return building;
    }

    /**
     * Generate a specific building type
     */
    public Model getBuilding(BuildingGenerator.BuildingType type,
                             BuildingGenerator.ArchitectureStyle style,
                             float scale, long buildingSeed) {
        long key = buildingSeed ^ (type.ordinal() * 31L) ^ (style.ordinal() * 17L);
        Model cached = cachedBuildings.get(key);
        if (cached != null) return cached;

        Model building = buildingGenerator.generateBuilding(type, style, scale, buildingSeed);
        cachedBuildings.put(key, building);
        return building;
    }

    /**
     * Create a procedural texture and track it for disposal
     */
    public Texture createMetalTexture(int size, Color color, long seed) {
        Texture tex = ProceduralTexture.metalHull(size, size, color, seed);
        managedTextures.add(tex);
        return tex;
    }

    public Texture createOrganicTexture(int size, Color color, long seed) {
        Texture tex = ProceduralTexture.organicSkin(size, size, color, seed);
        managedTextures.add(tex);
        return tex;
    }

    public Texture createRockTexture(int size, Color color, long seed) {
        Texture tex = ProceduralTexture.rock(size, size, color, seed);
        managedTextures.add(tex);
        return tex;
    }

    public Texture createEnergyTexture(int size, Color core, Color edge, long seed) {
        Texture tex = ProceduralTexture.energyGlow(size, size, core, edge, seed);
        managedTextures.add(tex);
        return tex;
    }

    /**
     * Create a procedural mesh and track it for disposal
     */
    public Model createSphere(float radius, int divisions, Material material, float deformation, long seed) {
        Model model = ProceduralMesh.sphere(radius, divisions, material, deformation, seed);
        managedModels.add(model);
        return model;
    }

    public Model createCylinder(float radius, float height, int segments, Material material) {
        Model model = ProceduralMesh.cylinder(radius, height, segments, material);
        managedModels.add(model);
        return model;
    }

    public Model createTorus(float majorR, float minorR, int majSegs, int minSegs, Material material) {
        Model model = ProceduralMesh.torus(majorR, minorR, majSegs, minSegs, material);
        managedModels.add(model);
        return model;
    }

    /**
     * Generate asteroid model
     */
    public Model createAsteroid(float radius, long seed) {
        Color asteroidColor = new Color(0.4f, 0.35f, 0.3f, 1f);
        Texture tex = ProceduralTexture.rock(64, 64, asteroidColor, seed);
        managedTextures.add(tex);

        Material mat = ProceduralTexture.createMaterial(tex);
        Model asteroid = ProceduralMesh.sphere(radius, 8, mat, radius * 0.3f, seed);
        managedModels.add(asteroid);
        return asteroid;
    }

    /**
     * Generate space station model
     */
    public Model createSpaceStation(long seed) {
        java.util.Random random = new java.util.Random(seed);

        // Use building generator for station modules
        BuildingGenerator.ArchitectureStyle style = random.nextBoolean() ?
            BuildingGenerator.ArchitectureStyle.HUMAN :
            BuildingGenerator.ArchitectureStyle.ALIEN_CRYSTAL;

        return buildingGenerator.generateBuilding(
            BuildingGenerator.BuildingType.HABITAT,
            style,
            3f + random.nextFloat() * 2f,
            seed
        );
    }

    /**
     * Generate flora/vegetation model
     */
    public Model createFlora(PlanetType planetType, long seed) {
        // Use creature generator with AMORPHOUS type for plant-like entities
        return creatureGenerator.generateCreature(
            CreatureGenerator.CreatureType.AMORPHOUS,
            CreatureGenerator.CreatureSize.SMALL,
            planetType,
            seed
        );
    }

    /**
     * Clear cached assets to free memory
     */
    public void clearCache() {
        // Don't dispose - just clear references (generators manage their own models)
        cachedShips.clear();
        cachedCreatures.clear();
        cachedBuildings.clear();
    }

    /**
     * Get memory statistics
     */
    public String getStats() {
        return String.format("Cached: Ships=%d, Creatures=%d, Buildings=%d, Textures=%d, Models=%d",
            cachedShips.size, cachedCreatures.size, cachedBuildings.size,
            managedTextures.size, managedModels.size);
    }

    @Override
    public void dispose() {
        // Dispose generators (they manage their own resources)
        spaceshipGenerator.dispose();
        creatureGenerator.dispose();
        buildingGenerator.dispose();

        // Dispose managed resources
        for (Texture tex : managedTextures) {
            tex.dispose();
        }
        for (Model model : managedModels) {
            model.dispose();
        }

        if (playerShipModel != null) {
            playerShipModel.dispose();
        }

        managedTextures.clear();
        managedModels.clear();
        cachedShips.clear();
        cachedCreatures.clear();
        cachedBuildings.clear();
    }
}
