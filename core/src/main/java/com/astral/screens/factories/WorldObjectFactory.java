package com.astral.screens.factories;

import com.astral.components.RenderComponent;
import com.astral.components.TransformComponent;
import com.astral.ecs.Entity;
import com.astral.ecs.World;
import com.astral.procedural.Planet;
import com.astral.systems.TransitionManager;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.astral.game.UniverseManager;
import com.astral.game.UniverseManager.*;

/**
 * Factory for creating world objects like asteroids and planets.
 */
public class WorldObjectFactory implements Disposable {

    private Model asteroidModel;
    private Model planetModel;
    private final ModelBuilder modelBuilder = new ModelBuilder();

    /**
     * Create random asteroids in the world
     * @param count Number of asteroids to create
     * @param spread Maximum spread distance from origin
     */
    public void createAsteroids(World world, int count, float spread) {
        if (asteroidModel == null) {
            asteroidModel = modelBuilder.createSphere(10f, 10f, 10f, 16, 16,
                    new Material(ColorAttribute.createDiffuse(Color.BROWN)),
                    Usage.Position | Usage.Normal);
        }

        for (int i = 0; i < count; i++) {
            float x = (float) (Math.random() - 0.5) * spread;
            float y = (float) (Math.random() - 0.5) * spread;
            float z = (float) (Math.random() - 0.5) * spread;

            Entity asteroid = world.createEntity();
            asteroid.setTag("Asteroid");

            TransformComponent transform = new TransformComponent();
            transform.setPosition(x, y, z);
            float scale = 0.5f + (float) Math.random() * 2f;
            transform.setScale(scale, scale, scale);
            asteroid.add(transform);

            RenderComponent render = new RenderComponent();
            render.setModel(new ModelInstance(asteroidModel));
            asteroid.add(render);
        }

        world.processPending();
        Gdx.app.log("WorldObjectFactory", "Created " + count + " asteroids");
    }

    /**
     * Create a planet entity
     * @return The created planet entity
     */
    public Entity createPlanet(World world, Vector3 position, float radius, Color color, String name,
                               TransitionManager transitionManager) {
        planetModel = modelBuilder.createSphere(radius * 2, radius * 2, radius * 2, 32, 32,
                new Material(ColorAttribute.createDiffuse(color)),
                Usage.Position | Usage.Normal);

        Entity planet = world.createEntity();
        planet.setTag("Planet");

        TransformComponent transform = new TransformComponent();
        transform.setPosition(position.x, position.y, position.z);
        planet.add(transform);

        RenderComponent render = new RenderComponent();
        render.setModel(new ModelInstance(planetModel));
        planet.add(render);

        world.processPending();

        // Register planet with transition manager
        if (transitionManager != null) {
            Planet planetData = new Planet(12345L);
            planetData.setName(name);
            transitionManager.setNearestPlanet(planetData, position, radius);
        }

        Gdx.app.log("WorldObjectFactory", "Created planet '" + name + "' at " + position);
        return planet;
    }

    /**
     * Create a star entity based on star system data
     */
    public Entity createStar(World world, StarSystemData system, float scaleFactor) {
        float starRadius = switch (system.starType) {
            case RED_DWARF -> 30f;
            case YELLOW -> 50f;
            case BLUE_GIANT -> 80f;
            case WHITE_DWARF -> 25f;
        };
        starRadius *= scaleFactor;
        
        Color starColor = new Color(system.starColor[0], system.starColor[1], system.starColor[2], 1f);
        
        Model starModel = modelBuilder.createSphere(starRadius * 2, starRadius * 2, starRadius * 2, 32, 32,
                new Material(ColorAttribute.createDiffuse(starColor), ColorAttribute.createEmissive(starColor)),
                Usage.Position | Usage.Normal);
        
        Entity star = world.createEntity();
        star.setTag("Star");
        
        TransformComponent transform = new TransformComponent();
        transform.setPosition(0, 0, 0); // Star at center
        star.add(transform);
        
        RenderComponent render = new RenderComponent();
        render.setModel(new ModelInstance(starModel));
        star.add(render);
        
        world.processPending();
        Gdx.app.log("WorldObjectFactory", "Created star '" + system.name + "' (" + system.starType + ")");
        return star;
    }
    
    /**
     * Create planets from star system data
     */
    public Array<Entity> createPlanetsFromSystem(World world, StarSystemData system, TransitionManager transitionManager, float scaleFactor) {
        Array<Entity> planets = new Array<>();
        
        for (PlanetData planetData : system.planets) {
            // Calculate position based on orbit distance
            float angle = planetData.index * 0.8f; // Spread planets around star
            float orbitDist = planetData.orbitDistance * scaleFactor;
            Vector3 pos = new Vector3(
                (float) Math.cos(angle) * orbitDist,
                (float) Math.sin(angle * 0.3f) * orbitDist * 0.1f, // Slight vertical variance
                (float) Math.sin(angle) * orbitDist
            );
            
            // Size and color based on planet type
            float radius = 20f + planetData.size * 30f;
            Color color = getPlanetColor(planetData.type);
            
            Entity planet = createPlanetFromData(world, pos, radius, color, planetData, transitionManager);
            planets.add(planet);
        }
        
        return planets;
    }
    
    private Entity createPlanetFromData(World world, Vector3 position, float radius, Color color, 
                                        PlanetData planetData, TransitionManager transitionManager) {
        if (planetModel != null) {
            planetModel.dispose();
        }
        planetModel = modelBuilder.createSphere(radius * 2, radius * 2, radius * 2, 32, 32,
                new Material(ColorAttribute.createDiffuse(color)),
                Usage.Position | Usage.Normal);

        Entity planet = world.createEntity();
        planet.setTag("Planet_" + planetData.name);

        TransformComponent transform = new TransformComponent();
        transform.setPosition(position.x, position.y, position.z);
        planet.add(transform);

        RenderComponent render = new RenderComponent();
        render.setModel(new ModelInstance(planetModel));
        planet.add(render);

        world.processPending();

        // Register planet with transition manager
        if (transitionManager != null && planetData.landable) {
            Planet procPlanet = new Planet(planetData.seed);
            procPlanet.setName(planetData.name);
            transitionManager.setNearestPlanet(procPlanet, position, radius);
        }

        Gdx.app.log("WorldObjectFactory", "Created planet '" + planetData.name + "' (" + planetData.type + ") at " + position);
        return planet;
    }
    
    private Color getPlanetColor(UniverseManager.PlanetType type) {
        return switch (type) {
            case TERRAN -> new Color(0.2f, 0.6f, 0.3f, 1f);
            case OCEAN -> new Color(0.1f, 0.3f, 0.7f, 1f);
            case DESERT -> new Color(0.8f, 0.6f, 0.3f, 1f);
            case ICE -> new Color(0.8f, 0.9f, 1.0f, 1f);
            case ROCKY -> new Color(0.5f, 0.4f, 0.4f, 1f);
            case BARREN -> new Color(0.4f, 0.35f, 0.3f, 1f);
            case MOLTEN -> new Color(0.9f, 0.3f, 0.1f, 1f);
            case TOXIC -> new Color(0.5f, 0.7f, 0.2f, 1f);
            case GAS_GIANT -> new Color(0.7f, 0.5f, 0.4f, 1f);
        };
    }

    @Override
    public void dispose() {
        if (asteroidModel != null) {
            asteroidModel.dispose();
            asteroidModel = null;
        }
        if (planetModel != null) {
            planetModel.dispose();
            planetModel = null;
        }
    }
}
