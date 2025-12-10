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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

/**
 * Hostile NPC enemy that chases and attacks the player.
 * Implements ICombatEntity for standardized combat interactions.
 */
public class Enemy implements ICombatEntity {

    public enum EnemyType {
        SAND_CRAWLER(30f, 4f, 10f, 1.5f, new Color(0.6f, 0.4f, 0.2f, 1f)),
        DUST_STALKER(20f, 8f, 15f, 1.2f, new Color(0.8f, 0.6f, 0.3f, 1f)),
        ROCK_BEAST(80f, 2f, 25f, 2.5f, new Color(0.5f, 0.45f, 0.4f, 1f));

        public final float health;
        public final float speed;
        public final float damage;
        public final float size;
        public final Color color;

        EnemyType(float health, float speed, float damage, float size, Color color) {
            this.health = health;
            this.speed = speed;
            this.damage = damage;
            this.size = size;
            this.color = color;
        }
    }

    private Vector3 position;
    private Vector3 velocity;
    private float health;
    private float maxHealth;
    private EnemyType type;
    private Model model;
    private ModelInstance modelInstance;
    private boolean alive = true;
    private float attackCooldown = 0f;
    private float hitFlashTime = 0f;

    // AI state
    private float thinkTimer = 0f;
    private Vector3 targetDirection = new Vector3();
    private boolean aggro = false;
    private float aggroRange = 50f;
    private float attackRange = 3f;

    public Enemy(EnemyType type, float x, float y, float z) {
        this.type = type;
        this.position = new Vector3(x, y, z);
        this.velocity = new Vector3();
        this.health = type.health;
        this.maxHealth = type.health;

        createModel();
    }

    private void createModel() {
        // Create a simple enemy mesh (blocky creature)
        float s = type.size;

        // Body vertices - low-poly creature shape
        float[] vertices = {
            // Body (box)
            -s*0.5f, 0, -s*0.4f,   0,-1,0,
            s*0.5f, 0, -s*0.4f,    0,-1,0,
            s*0.5f, 0, s*0.4f,     0,-1,0,
            -s*0.5f, 0, s*0.4f,    0,-1,0,

            -s*0.5f, s*0.6f, -s*0.4f,   0,1,0,
            s*0.5f, s*0.6f, -s*0.4f,    0,1,0,
            s*0.5f, s*0.6f, s*0.4f,     0,1,0,
            -s*0.5f, s*0.6f, s*0.4f,    0,1,0,

            // Head (front protrusion)
            s*0.5f, s*0.2f, -s*0.2f,    1,0,0,
            s*0.8f, s*0.3f, 0,          1,0,0,
            s*0.5f, s*0.2f, s*0.2f,     1,0,0,
            s*0.5f, s*0.5f, 0,          1,0,0,
        };

        short[] indices = {
            // Bottom
            0, 2, 1, 0, 3, 2,
            // Top
            4, 5, 6, 4, 6, 7,
            // Front
            0, 1, 5, 0, 5, 4,
            // Back
            2, 3, 7, 2, 7, 6,
            // Left
            3, 0, 4, 3, 4, 7,
            // Right
            1, 2, 6, 1, 6, 5,
            // Head
            8, 9, 11, 9, 10, 11,
        };

        Mesh mesh = new Mesh(true, vertices.length / 6, indices.length,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal")
        );
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        Material material = new Material(
            ColorAttribute.createDiffuse(type.color),
            ColorAttribute.createEmissive(new Color(type.color).mul(0.3f))
        );

        model = new Model();
        MeshPart meshPart = new MeshPart();
        meshPart.id = "enemy";
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

        model.nodes.add(node);
        model.meshes.add(mesh);
        model.meshParts.add(meshPart);
        model.materials.add(material);
        model.manageDisposable(mesh);

        modelInstance = new ModelInstance(model);
    }

    public void update(float delta, Vector3 playerPos, java.util.function.BiFunction<Float, Float, Float> getHeight) {
        if (!alive) return;

        hitFlashTime = Math.max(0, hitFlashTime - delta);
        attackCooldown = Math.max(0, attackCooldown - delta);

        // AI thinking
        thinkTimer -= delta;
        if (thinkTimer <= 0) {
            thinkTimer = 0.2f + MathUtils.random(0.3f);

            // Check distance to player
            float distToPlayer = position.dst(playerPos);
            aggro = distToPlayer < aggroRange;

            if (aggro) {
                // Move toward player
                targetDirection.set(playerPos).sub(position);
                targetDirection.y = 0;
                targetDirection.nor();
            } else {
                // Wander randomly
                if (MathUtils.random() < 0.3f) {
                    targetDirection.set(MathUtils.random(-1f, 1f), 0, MathUtils.random(-1f, 1f)).nor();
                }
            }
        }

        // Movement
        float speed = aggro ? type.speed : type.speed * 0.3f;
        velocity.x = targetDirection.x * speed;
        velocity.z = targetDirection.z * speed;

        position.x += velocity.x * delta;
        position.z += velocity.z * delta;

        // Ground height
        float groundHeight = getHeight.apply(position.x, position.z);
        position.y = groundHeight + type.size * 0.3f;

        // Update model transform
        modelInstance.transform.setToTranslation(position);

        // Face movement direction
        if (velocity.len2() > 0.1f) {
            float angle = MathUtils.atan2(velocity.x, -velocity.z) * MathUtils.radiansToDegrees;
            modelInstance.transform.rotate(Vector3.Y, angle);
        }

        // Hit flash effect
        if (hitFlashTime > 0) {
            Material mat = modelInstance.materials.get(0);
            mat.set(ColorAttribute.createEmissive(Color.WHITE));
        } else {
            Material mat = modelInstance.materials.get(0);
            mat.set(ColorAttribute.createEmissive(new Color(type.color).mul(0.3f)));
        }
    }

    public boolean canAttackPlayer(Vector3 playerPos) {
        if (!alive || attackCooldown > 0) return false;
        return position.dst(playerPos) < attackRange;
    }

    public float attack() {
        attackCooldown = 1.5f;
        return type.damage;
    }

    public void takeDamage(float damage) {
        if (!alive) return;
        health -= damage;
        hitFlashTime = 0.15f;

        if (health <= 0) {
            alive = false;
        }
    }

    @Override
    public boolean isAlive() { return alive; }

    @Override
    public Vector3 getPosition() { return position; }

    public float getSize() { return type.size; }

    @Override
    public float getHealth() { return health; }

    @Override
    public float getMaxHealth() { return maxHealth; }

    @Override
    public ModelInstance getModelInstance() { return modelInstance; }

    public boolean isAggro() { return aggro; }

    /**
     * Simple update without player tracking (for ICombatEntity interface)
     */
    @Override
    public void update(float delta) {
        // Basic update - timers only, no movement
        if (!alive) return;
        hitFlashTime = Math.max(0, hitFlashTime - delta);
        attackCooldown = Math.max(0, attackCooldown - delta);
    }

    @Override
    public void dispose() {
        if (model != null) model.dispose();
    }
}
