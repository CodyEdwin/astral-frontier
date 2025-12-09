package com.astral.components;

import com.astral.ecs.Component;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * Transform component - position, rotation, scale in 3D space
 */
public class TransformComponent implements Component {

    public final Vector3 position = new Vector3();
    public final Quaternion rotation = new Quaternion();
    public final Vector3 scale = new Vector3(1, 1, 1);
    public final Matrix4 worldMatrix = new Matrix4();
    public boolean dirty = true;

    // Parent-child hierarchy
    public TransformComponent parent;

    public void updateMatrix() {
        if (dirty) {
            worldMatrix.set(position, rotation, scale);

            if (parent != null) {
                parent.updateMatrix();
                worldMatrix.mulLeft(parent.worldMatrix);
            }

            dirty = false;
        }
    }

    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        dirty = true;
    }

    public void setPosition(Vector3 pos) {
        position.set(pos);
        dirty = true;
    }

    public void translate(float x, float y, float z) {
        position.add(x, y, z);
        dirty = true;
    }

    public void translate(Vector3 delta) {
        position.add(delta);
        dirty = true;
    }

    public void setRotation(Quaternion q) {
        rotation.set(q);
        dirty = true;
    }

    public void rotate(Vector3 axis, float degrees) {
        rotation.mul(new Quaternion(axis, degrees));
        dirty = true;
    }

    public void setScale(float x, float y, float z) {
        scale.set(x, y, z);
        dirty = true;
    }

    public void setScale(float uniformScale) {
        scale.set(uniformScale, uniformScale, uniformScale);
        dirty = true;
    }

    public Vector3 getForward() {
        return new Vector3(0, 0, -1).mul(rotation);
    }

    public Vector3 getRight() {
        return new Vector3(1, 0, 0).mul(rotation);
    }

    public Vector3 getUp() {
        return new Vector3(0, 1, 0).mul(rotation);
    }

    @Override
    public void reset() {
        position.setZero();
        rotation.idt();
        scale.set(1, 1, 1);
        worldMatrix.idt();
        dirty = true;
        parent = null;
    }
}
