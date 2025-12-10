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
import com.badlogic.gdx.utils.Disposable;

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
