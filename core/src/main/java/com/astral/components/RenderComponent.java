package com.astral.components;

import com.astral.ecs.Component;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;

/**
 * Render component for 3D models
 */
public class RenderComponent implements Component {

    public ModelInstance modelInstance;
    public Array<ModelInstance> additionalInstances; // For multi-part models like modular ships
    public Material[] materials;
    public BoundingBox bounds;
    public int lodLevel = 0;
    public boolean castsShadows = true;
    public boolean visible = true;
    public float renderDistance = 10000f;

    // Animation state
    public String currentAnimation;
    public float animationTime;

    public void setModel(ModelInstance instance) {
        this.modelInstance = instance;
        if (instance != null) {
            bounds = new BoundingBox();
            instance.calculateBoundingBox(bounds);
        }
    }

    @Override
    public void reset() {
        modelInstance = null;
        additionalInstances = null;
        materials = null;
        bounds = null;
        lodLevel = 0;
        castsShadows = true;
        visible = true;
        renderDistance = 10000f;
        currentAnimation = null;
        animationTime = 0f;
    }
}
