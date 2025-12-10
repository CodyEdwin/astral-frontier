package com.astral.combat;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * A projectile fired by ground weapons.
 * Implements IProjectile for standardized projectile behavior.
 */
public class GroundProjectile implements IProjectile {

    private Vector3 position;
    private Vector3 velocity;
    private float damage;
    private float lifetime;
    private float maxLifetime = 3f;
    private boolean alive = true;
    private float size = 0.15f;

    private Model model;
    private ModelInstance modelInstance;

    private static Model sharedModel;
    private static int sharedModelRefs = 0;

    public GroundProjectile(Vector3 startPos, Vector3 direction, float speed, float damage) {
        this.position = new Vector3(startPos);
        this.velocity = new Vector3(direction).nor().scl(speed);
        this.damage = damage;
        this.lifetime = 0;

        createModel();
    }

    private void createModel() {
        // Create shared model if needed
        if (sharedModel == null) {
            // Simple elongated box for projectile
            float w = size * 0.3f;
            float h = size * 0.3f;
            float l = size * 1.5f;

            float[] vertices = {
                // Front
                -w, -h, l,  0, 0, 1,
                w, -h, l,   0, 0, 1,
                w, h, l,    0, 0, 1,
                -w, h, l,   0, 0, 1,
                // Back
                -w, -h, -l, 0, 0, -1,
                w, -h, -l,  0, 0, -1,
                w, h, -l,   0, 0, -1,
                -w, h, -l,  0, 0, -1,
            };

            short[] indices = {
                0, 1, 2, 0, 2, 3,  // Front
                5, 4, 7, 5, 7, 6,  // Back
                4, 0, 3, 4, 3, 7,  // Left
                1, 5, 6, 1, 6, 2,  // Right
                3, 2, 6, 3, 6, 7,  // Top
                4, 5, 1, 4, 1, 0,  // Bottom
            };

            Mesh mesh = new Mesh(true, 8, indices.length,
                new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
                new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")
            );
            mesh.setVertices(vertices);
            mesh.setIndices(indices);

            // Bright plasma color
            Material material = new Material(
                ColorAttribute.createDiffuse(new Color(0.2f, 0.8f, 1f, 1f)),
                ColorAttribute.createEmissive(new Color(0.4f, 0.9f, 1f, 1f))
            );

            sharedModel = new Model();
            MeshPart meshPart = new MeshPart();
            meshPart.id = "projectile";
            meshPart.mesh = mesh;
            meshPart.offset = 0;
            meshPart.size = indices.length;
            meshPart.primitiveType = GL20.GL_TRIANGLES;

            Node node = new Node();
            node.id = "node";
            NodePart nodePart = new NodePart();
            nodePart.meshPart = meshPart;
            nodePart.material = material;
            node.parts.add(nodePart);

            sharedModel.nodes.add(node);
            sharedModel.meshes.add(mesh);
            sharedModel.meshParts.add(meshPart);
            sharedModel.materials.add(material);
            sharedModel.manageDisposable(mesh);
        }

        sharedModelRefs++;
        model = sharedModel;
        modelInstance = new ModelInstance(model);
    }

    public void update(float delta) {
        if (!alive) return;

        lifetime += delta;
        if (lifetime >= maxLifetime) {
            alive = false;
            return;
        }

        position.add(velocity.x * delta, velocity.y * delta, velocity.z * delta);

        // Update transform
        modelInstance.transform.setToTranslation(position);

        // Rotate to face velocity direction
        if (velocity.len2() > 0) {
            Vector3 dir = new Vector3(velocity).nor();
            float yaw = (float) Math.atan2(dir.x, dir.z);
            float pitch = (float) Math.asin(-dir.y);
            modelInstance.transform.rotate(Vector3.Y, yaw * 57.2958f);
            modelInstance.transform.rotate(Vector3.X, pitch * 57.2958f);
        }
    }

    public boolean checkHit(Enemy enemy) {
        if (!alive || !enemy.isAlive()) return false;

        float dist = position.dst(enemy.getPosition());
        if (dist < enemy.getSize() * 0.6f + size) {
            enemy.takeDamage(damage);
            alive = false;
            return true;
        }
        return false;
    }

    public boolean checkTerrainHit(float terrainHeight) {
        if (!alive) return false;
        if (position.y < terrainHeight) {
            alive = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean isAlive() { return alive; }

    @Override
    public Vector3 getPosition() { return position; }

    @Override
    public ModelInstance getModelInstance() { return modelInstance; }

    @Override
    public float getDamage() { return damage; }

    @Override
    public Vector3 getDirection() {
        return new Vector3(velocity).nor();
    }

    @Override
    public float getSpeed() {
        return velocity.len();
    }

    @Override
    public boolean checkHit(ICombatEntity entity) {
        if (!alive || !entity.isAlive()) return false;

        // For enemies, use their size for collision detection
        float targetSize = 1.5f; // Default size
        if (entity instanceof Enemy) {
            targetSize = ((Enemy) entity).getSize() * 0.6f;
        }

        float dist = position.dst(entity.getPosition());
        if (dist < targetSize + size) {
            entity.takeDamage(damage);
            alive = false;
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        sharedModelRefs--;
        if (sharedModelRefs <= 0 && sharedModel != null) {
            sharedModel.dispose();
            sharedModel = null;
        }
    }
}
