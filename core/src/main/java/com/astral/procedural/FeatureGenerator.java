package com.astral.procedural;

import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.utils.Disposable;

/**
 * Base interface for all procedural feature generators.
 * Feature generators create 3D models for environmental features.
 */
public interface FeatureGenerator extends Disposable {

    /**
     * Generate a model for a specific feature type
     * @param featureType The type of feature to generate (enum value)
     * @return Generated 3D model
     */
    Model generate(Enum<?> featureType);

    /**
     * Generate a random feature from available types
     * @return Generated 3D model
     */
    Model generateRandom();

    /**
     * Get the seed used for generation
     * @return The seed value
     */
    long getSeed();
}
