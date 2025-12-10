package com.astral.procedural;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

/**
 * Abstract base class for procedural feature generators.
 * Provides common utilities for texture loading and model building.
 */
public abstract class AbstractFeatureGenerator implements FeatureGenerator {

    protected final long seed;
    protected final Random random;
    protected final ModelBuilder modelBuilder;
    protected final Array<Texture> textures = new Array<>();
    protected final Array<Model> models = new Array<>();

    protected AbstractFeatureGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.modelBuilder = new ModelBuilder();
    }

    @Override
    public long getSeed() {
        return seed;
    }

    /**
     * Load a texture from internal file path with default settings
     * @param path Path to texture file
     * @return Loaded texture or null if failed
     */
    protected Texture loadTexture(String path) {
        try {
            FileHandle file = Gdx.files.internal(path);
            if (file.exists()) {
                Texture tex = new Texture(file, true);
                tex.setFilter(TextureFilter.MipMapLinearLinear, TextureFilter.Linear);
                tex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
                textures.add(tex);
                Gdx.app.log(getClass().getSimpleName(), "Loaded texture: " + path);
                return tex;
            }
        } catch (Exception e) {
            Gdx.app.error(getClass().getSimpleName(), "Failed to load " + path + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Register a model for disposal tracking
     * @param model The model to track
     * @return The same model for chaining
     */
    protected Model trackModel(Model model) {
        if (model != null) {
            models.add(model);
        }
        return model;
    }

    /**
     * Get a random float in range
     */
    protected float randomRange(float min, float max) {
        return min + random.nextFloat() * (max - min);
    }

    /**
     * Get a random int in range (inclusive)
     */
    protected int randomRange(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    @Override
    public void dispose() {
        for (Model model : models) {
            model.dispose();
        }
        models.clear();

        for (Texture texture : textures) {
            texture.dispose();
        }
        textures.clear();
    }
}
