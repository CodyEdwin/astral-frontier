package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Random;

/**
 * Generates procedural alien creatures for planetary fauna
 */
public class CreatureGenerator implements Disposable {

    private final long seed;
    private final Random random;
    private final ModelBuilder modelBuilder;
    private final Array<Texture> generatedTextures = new Array<>();
    private final Array<Model> generatedModels = new Array<>();

    public enum CreatureType {
        QUADRUPED,    // 4-legged animal
        BIPED,        // 2-legged humanoid
        SERPENT,      // Snake-like
        INSECTOID,    // Bug-like with many legs
        FLYING,       // Winged creature
        AMORPHOUS,    // Blob-like
        CRUSTACEAN    // Crab-like
    }

    public enum CreatureSize {
        TINY(0.3f),
        SMALL(0.8f),
        MEDIUM(1.5f),
        LARGE(3f),
        HUGE(6f);

        public final float scale;
        CreatureSize(float scale) { this.scale = scale; }
    }

    public CreatureGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.modelBuilder = new ModelBuilder();
    }

    /**
     * Generate a random creature based on planet type
     */
    public Model generateCreature(PlanetType planetType, long creatureSeed) {
        random.setSeed(creatureSeed);

        CreatureType type = getCreatureTypeForPlanet(planetType);
        CreatureSize size = CreatureSize.values()[random.nextInt(CreatureSize.values().length)];
        Color baseColor = getCreatureColor(planetType);

        return generateCreature(type, size, baseColor, creatureSeed);
    }

    /**
     * Generate a specific creature type
     */
    public Model generateCreature(CreatureType type, CreatureSize size, Color baseColor, long creatureSeed) {
        random.setSeed(creatureSeed);

        Texture skinTexture = ProceduralTexture.organicSkin(64, 64, baseColor, creatureSeed);
        generatedTextures.add(skinTexture);
        Material skinMaterial = new Material(TextureAttribute.createDiffuse(skinTexture));

        float scale = size.scale;

        Model model = switch (type) {
            case QUADRUPED -> generateQuadruped(scale, skinMaterial);
            case BIPED -> generateBiped(scale, skinMaterial);
            case SERPENT -> generateSerpent(scale, skinMaterial);
            case INSECTOID -> generateInsectoid(scale, skinMaterial);
            case FLYING -> generateFlying(scale, skinMaterial);
            case AMORPHOUS -> generateAmorphous(scale, skinMaterial);
            case CRUSTACEAN -> generateCrustacean(scale, skinMaterial);
        };

        generatedModels.add(model);
        return model;
    }

    private CreatureType getCreatureTypeForPlanet(PlanetType planetType) {
        return switch (planetType) {
            case DESERT -> random.nextFloat() < 0.5f ? CreatureType.INSECTOID : CreatureType.SERPENT;
            case FOREST -> random.nextFloat() < 0.5f ? CreatureType.QUADRUPED : CreatureType.BIPED;
            case ICE -> random.nextFloat() < 0.5f ? CreatureType.QUADRUPED : CreatureType.AMORPHOUS;
            case VOLCANIC -> CreatureType.CRUSTACEAN;
            case OCEAN -> random.nextFloat() < 0.5f ? CreatureType.SERPENT : CreatureType.AMORPHOUS;
            case ROCKY -> CreatureType.INSECTOID;
            case TOXIC -> CreatureType.AMORPHOUS;
            case PARADISE -> random.nextFloat() < 0.5f ? CreatureType.FLYING : CreatureType.QUADRUPED;
        };
    }

    private Color getCreatureColor(PlanetType planetType) {
        return switch (planetType) {
            case DESERT -> new Color(0.7f + random.nextFloat() * 0.2f, 0.5f + random.nextFloat() * 0.2f, 0.3f, 1f);
            case FOREST -> new Color(0.3f + random.nextFloat() * 0.2f, 0.5f + random.nextFloat() * 0.3f, 0.2f, 1f);
            case ICE -> new Color(0.8f + random.nextFloat() * 0.2f, 0.85f + random.nextFloat() * 0.15f, 0.9f, 1f);
            case VOLCANIC -> new Color(0.4f + random.nextFloat() * 0.2f, 0.2f, 0.1f, 1f);
            case OCEAN -> new Color(0.2f, 0.4f + random.nextFloat() * 0.2f, 0.6f + random.nextFloat() * 0.2f, 1f);
            case ROCKY -> new Color(0.5f + random.nextFloat() * 0.2f, 0.45f, 0.4f, 1f);
            case TOXIC -> new Color(0.5f + random.nextFloat() * 0.3f, 0.8f, 0.2f, 1f);
            case PARADISE -> new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 1f);
        };
    }

    private Model generateQuadruped(float scale, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("body", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material);

        float bodyLength = 2f * scale;
        float bodyWidth = 0.8f * scale;
        float bodyHeight = 0.6f * scale;
        float legLength = 0.8f * scale;
        float legRadius = 0.15f * scale;

        // Body (ellipsoid)
        addEllipsoid(builder, 0, legLength + bodyHeight / 2, 0, bodyLength / 2, bodyHeight / 2, bodyWidth / 2, 8);

        // Head
        float headSize = 0.4f * scale;
        addEllipsoid(builder, bodyLength / 2 + headSize * 0.8f, legLength + bodyHeight / 2 + headSize * 0.3f, 0,
            headSize, headSize * 0.8f, headSize * 0.7f, 6);

        // Legs
        float legOffsetX = bodyLength * 0.35f;
        float legOffsetZ = bodyWidth * 0.4f;

        addLeg(builder, legOffsetX, legLength, legOffsetZ, legRadius, legLength);
        addLeg(builder, legOffsetX, legLength, -legOffsetZ, legRadius, legLength);
        addLeg(builder, -legOffsetX, legLength, legOffsetZ, legRadius, legLength);
        addLeg(builder, -legOffsetX, legLength, -legOffsetZ, legRadius, legLength);

        // Tail
        float tailLength = 1f * scale;
        addTail(builder, -bodyLength / 2, legLength + bodyHeight / 2, 0, tailLength, 0.1f * scale);

        return modelBuilder.end();
    }

    private Model generateBiped(float scale, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("body", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material);

        float torsoHeight = 1f * scale;
        float torsoWidth = 0.5f * scale;
        float legLength = 0.8f * scale;
        float armLength = 0.7f * scale;

        // Torso
        addEllipsoid(builder, 0, legLength + torsoHeight / 2, 0,
            torsoWidth * 0.4f, torsoHeight / 2, torsoWidth / 2, 8);

        // Head
        float headSize = 0.3f * scale;
        addEllipsoid(builder, 0, legLength + torsoHeight + headSize, 0,
            headSize * 0.8f, headSize, headSize * 0.7f, 6);

        // Legs
        addLeg(builder, 0, legLength, torsoWidth * 0.3f, 0.12f * scale, legLength);
        addLeg(builder, 0, legLength, -torsoWidth * 0.3f, 0.12f * scale, legLength);

        // Arms
        addArm(builder, 0, legLength + torsoHeight * 0.8f, torsoWidth * 0.5f, 0.08f * scale, armLength, true);
        addArm(builder, 0, legLength + torsoHeight * 0.8f, -torsoWidth * 0.5f, 0.08f * scale, armLength, false);

        return modelBuilder.end();
    }

    private Model generateSerpent(float scale, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("body", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material);

        int segments = 20;
        float length = 4f * scale;
        float maxRadius = 0.3f * scale;

        for (int i = 0; i < segments - 1; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;

            float x1 = (t1 - 0.5f) * length;
            float x2 = (t2 - 0.5f) * length;

            // Sine wave for body shape
            float y1 = MathUtils.sin(t1 * MathUtils.PI * 2) * 0.2f * scale;
            float y2 = MathUtils.sin(t2 * MathUtils.PI * 2) * 0.2f * scale;

            // Radius tapers at ends
            float r1 = maxRadius * MathUtils.sin(t1 * MathUtils.PI);
            float r2 = maxRadius * MathUtils.sin(t2 * MathUtils.PI);

            if (r1 < 0.01f) r1 = 0.01f;
            if (r2 < 0.01f) r2 = 0.01f;

            addTubeSegment(builder, x1, y1, 0, x2, y2, 0, r1, r2, 8);
        }

        // Head
        float headX = length / 2 - maxRadius;
        addEllipsoid(builder, headX + maxRadius * 1.5f, 0, 0, maxRadius * 1.2f, maxRadius, maxRadius, 6);

        return modelBuilder.end();
    }

    private Model generateInsectoid(float scale, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("body", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material);

        float bodyLength = 1.5f * scale;
        float thoraxSize = 0.5f * scale;
        float abdomenSize = 0.7f * scale;
        float headSize = 0.3f * scale;
        float legLength = 0.6f * scale;

        // Head
        addEllipsoid(builder, bodyLength / 2 + headSize, 0.3f * scale, 0,
            headSize, headSize * 0.8f, headSize * 0.7f, 6);

        // Thorax
        addEllipsoid(builder, 0, 0.3f * scale, 0,
            thoraxSize * 0.6f, thoraxSize * 0.5f, thoraxSize * 0.5f, 6);

        // Abdomen
        addEllipsoid(builder, -bodyLength / 2, 0.25f * scale, 0,
            abdomenSize, abdomenSize * 0.6f, abdomenSize * 0.5f, 6);

        // 6 legs
        int legPairs = 3;
        for (int i = 0; i < legPairs; i++) {
            float lx = (i - 1) * thoraxSize * 0.5f;
            addInsectLeg(builder, lx, 0, thoraxSize * 0.4f, legLength, 0.05f * scale, true);
            addInsectLeg(builder, lx, 0, -thoraxSize * 0.4f, legLength, 0.05f * scale, false);
        }

        // Antennae
        addAntenna(builder, bodyLength / 2 + headSize * 1.5f, 0.5f * scale, headSize * 0.3f, 0.4f * scale);
        addAntenna(builder, bodyLength / 2 + headSize * 1.5f, 0.5f * scale, -headSize * 0.3f, 0.4f * scale);

        return modelBuilder.end();
    }

    private Model generateFlying(float scale, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("body", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material);

        float bodyLength = 1f * scale;
        float bodyRadius = 0.3f * scale;
        float wingSpan = 2.5f * scale;

        // Body
        addEllipsoid(builder, 0, 0, 0, bodyLength / 2, bodyRadius, bodyRadius * 0.8f, 8);

        // Head
        addEllipsoid(builder, bodyLength / 2 + bodyRadius, bodyRadius * 0.3f, 0,
            bodyRadius * 0.8f, bodyRadius * 0.7f, bodyRadius * 0.6f, 6);

        // Wings
        addWing(builder, 0, bodyRadius * 0.5f, bodyRadius, wingSpan, bodyLength * 0.8f, true);
        addWing(builder, 0, bodyRadius * 0.5f, -bodyRadius, wingSpan, bodyLength * 0.8f, false);

        // Tail feathers
        addTailFeathers(builder, -bodyLength / 2, 0, 0, bodyLength * 0.5f, bodyRadius);

        return modelBuilder.end();
    }

    private Model generateAmorphous(float scale, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("body", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material);

        // Blob-like creature with random deformation
        float radius = 0.8f * scale;
        int divisions = 12;

        for (int lat = 0; lat < divisions; lat++) {
            float theta1 = MathUtils.PI * lat / divisions;
            float theta2 = MathUtils.PI * (lat + 1) / divisions;

            for (int lon = 0; lon < divisions; lon++) {
                float phi1 = 2 * MathUtils.PI * lon / divisions;
                float phi2 = 2 * MathUtils.PI * (lon + 1) / divisions;

                // Add random bulges
                float r1 = radius * (0.7f + 0.6f * random.nextFloat());
                float r2 = radius * (0.7f + 0.6f * random.nextFloat());
                float r3 = radius * (0.7f + 0.6f * random.nextFloat());
                float r4 = radius * (0.7f + 0.6f * random.nextFloat());

                Vector3 v1 = spherePoint(r1, theta1, phi1);
                Vector3 v2 = spherePoint(r2, theta1, phi2);
                Vector3 v3 = spherePoint(r3, theta2, phi1);
                Vector3 v4 = spherePoint(r4, theta2, phi2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }

        // Pseudopods/tentacles
        int tentacles = 3 + random.nextInt(4);
        for (int i = 0; i < tentacles; i++) {
            float angle = 2 * MathUtils.PI * i / tentacles;
            float tx = MathUtils.cos(angle) * radius * 0.8f;
            float tz = MathUtils.sin(angle) * radius * 0.8f;
            addTentacle(builder, tx, -radius * 0.3f, tz, 0.1f * scale, 0.5f * scale);
        }

        return modelBuilder.end();
    }

    private Model generateCrustacean(float scale, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("body", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            material);

        float bodyWidth = 1.2f * scale;
        float bodyLength = 0.8f * scale;
        float bodyHeight = 0.3f * scale;

        // Shell (flattened ellipsoid)
        addEllipsoid(builder, 0, bodyHeight, 0, bodyLength / 2, bodyHeight, bodyWidth / 2, 10);

        // Claws
        float clawSize = 0.4f * scale;
        addClaw(builder, bodyLength / 2 + clawSize, bodyHeight, bodyWidth / 3, clawSize, true);
        addClaw(builder, bodyLength / 2 + clawSize, bodyHeight, -bodyWidth / 3, clawSize, false);

        // Legs (4 pairs)
        for (int i = 0; i < 4; i++) {
            float lx = (i - 1.5f) * bodyLength * 0.2f;
            addCrabLeg(builder, lx, 0, bodyWidth / 2, 0.3f * scale, true);
            addCrabLeg(builder, lx, 0, -bodyWidth / 2, 0.3f * scale, false);
        }

        // Eyes on stalks
        addEyeStalk(builder, bodyLength / 2, bodyHeight * 1.5f, bodyWidth * 0.15f, 0.15f * scale);
        addEyeStalk(builder, bodyLength / 2, bodyHeight * 1.5f, -bodyWidth * 0.15f, 0.15f * scale);

        return modelBuilder.end();
    }

    // Helper geometry methods
    private void addEllipsoid(MeshPartBuilder builder, float cx, float cy, float cz, float rx, float ry, float rz, int div) {
        for (int lat = 0; lat < div; lat++) {
            float theta1 = MathUtils.PI * lat / div;
            float theta2 = MathUtils.PI * (lat + 1) / div;

            for (int lon = 0; lon < div; lon++) {
                float phi1 = 2 * MathUtils.PI * lon / div;
                float phi2 = 2 * MathUtils.PI * (lon + 1) / div;

                Vector3 v1 = ellipsoidPoint(cx, cy, cz, rx, ry, rz, theta1, phi1);
                Vector3 v2 = ellipsoidPoint(cx, cy, cz, rx, ry, rz, theta1, phi2);
                Vector3 v3 = ellipsoidPoint(cx, cy, cz, rx, ry, rz, theta2, phi1);
                Vector3 v4 = ellipsoidPoint(cx, cy, cz, rx, ry, rz, theta2, phi2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }
    }

    private Vector3 ellipsoidPoint(float cx, float cy, float cz, float rx, float ry, float rz, float theta, float phi) {
        return new Vector3(
            cx + rx * MathUtils.sin(theta) * MathUtils.cos(phi),
            cy + ry * MathUtils.cos(theta),
            cz + rz * MathUtils.sin(theta) * MathUtils.sin(phi)
        );
    }

    private Vector3 spherePoint(float r, float theta, float phi) {
        return new Vector3(
            r * MathUtils.sin(theta) * MathUtils.cos(phi),
            r * MathUtils.cos(theta),
            r * MathUtils.sin(theta) * MathUtils.sin(phi)
        );
    }

    private void addLeg(MeshPartBuilder builder, float x, float topY, float z, float radius, float length) {
        addTubeSegment(builder, x, topY, z, x, 0, z, radius, radius * 0.7f, 6);
    }

    private void addArm(MeshPartBuilder builder, float x, float y, float z, float radius, float length, boolean left) {
        float dir = left ? 1 : -1;
        addTubeSegment(builder, x, y, z, x, y - length * 0.5f, z + dir * length * 0.7f, radius, radius * 0.6f, 6);
    }

    private void addTail(MeshPartBuilder builder, float x, float y, float z, float length, float radius) {
        int segments = 8;
        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;

            float x1 = x - t1 * length;
            float x2 = x - t2 * length;
            float y1 = y + MathUtils.sin(t1 * MathUtils.PI) * length * 0.2f;
            float y2 = y + MathUtils.sin(t2 * MathUtils.PI) * length * 0.2f;

            float r1 = radius * (1 - t1 * 0.8f);
            float r2 = radius * (1 - t2 * 0.8f);

            addTubeSegment(builder, x1, y1, z, x2, y2, z, r1, r2, 6);
        }
    }

    private void addTubeSegment(MeshPartBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2,
                                 float r1, float r2, int segments) {
        Vector3 dir = new Vector3(x2 - x1, y2 - y1, z2 - z1).nor();
        Vector3 up = Math.abs(dir.y) < 0.99f ? Vector3.Y : Vector3.X;
        Vector3 right = new Vector3(dir).crs(up).nor();
        Vector3 forward = new Vector3(right).crs(dir).nor();

        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 offset1_1 = new Vector3(right).scl(MathUtils.cos(a1) * r1).add(new Vector3(forward).scl(MathUtils.sin(a1) * r1));
            Vector3 offset1_2 = new Vector3(right).scl(MathUtils.cos(a2) * r1).add(new Vector3(forward).scl(MathUtils.sin(a2) * r1));
            Vector3 offset2_1 = new Vector3(right).scl(MathUtils.cos(a1) * r2).add(new Vector3(forward).scl(MathUtils.sin(a1) * r2));
            Vector3 offset2_2 = new Vector3(right).scl(MathUtils.cos(a2) * r2).add(new Vector3(forward).scl(MathUtils.sin(a2) * r2));

            Vector3 v1 = new Vector3(x1, y1, z1).add(offset1_1);
            Vector3 v2 = new Vector3(x1, y1, z1).add(offset1_2);
            Vector3 v3 = new Vector3(x2, y2, z2).add(offset2_1);
            Vector3 v4 = new Vector3(x2, y2, z2).add(offset2_2);

            addQuad(builder, v1, v2, v4, v3);
        }
    }

    private void addInsectLeg(MeshPartBuilder builder, float x, float y, float z, float length, float radius, boolean left) {
        float dir = left ? 1 : -1;
        // Upper leg segment (angled out)
        addTubeSegment(builder, x, y, z, x, y - length * 0.3f, z + dir * length * 0.5f, radius, radius * 0.8f, 4);
        // Lower leg segment (angled down)
        addTubeSegment(builder, x, y - length * 0.3f, z + dir * length * 0.5f,
            x, y - length, z + dir * length * 0.7f, radius * 0.8f, radius * 0.5f, 4);
    }

    private void addAntenna(MeshPartBuilder builder, float x, float y, float z, float length) {
        addTubeSegment(builder, x, y, z, x + length * 0.5f, y + length, z, 0.02f, 0.01f, 4);
    }

    private void addWing(MeshPartBuilder builder, float x, float y, float z, float span, float chord, boolean left) {
        float dir = left ? 1 : -1;
        float thickness = 0.02f;

        Vector3 root1 = new Vector3(x + chord / 3, y + thickness, z);
        Vector3 root2 = new Vector3(x - chord / 2, y + thickness, z);
        Vector3 tip1 = new Vector3(x, y + thickness, z + dir * span);
        Vector3 tip2 = new Vector3(x - chord / 3, y + thickness, z + dir * span * 0.8f);

        addQuad(builder, root1, root2, tip2, tip1);

        // Bottom surface
        Vector3 root3 = new Vector3(x + chord / 3, y - thickness, z);
        Vector3 root4 = new Vector3(x - chord / 2, y - thickness, z);
        Vector3 tip3 = new Vector3(x, y - thickness, z + dir * span);
        Vector3 tip4 = new Vector3(x - chord / 3, y - thickness, z + dir * span * 0.8f);

        addQuad(builder, root4, root3, tip3, tip4);
    }

    private void addTailFeathers(MeshPartBuilder builder, float x, float y, float z, float length, float spread) {
        for (int i = -2; i <= 2; i++) {
            float angle = i * 0.2f;
            Vector3 start = new Vector3(x, y, z);
            Vector3 end = new Vector3(x - length, y + MathUtils.sin(angle) * spread, z + MathUtils.cos(angle) * spread * 0.3f);
            addTubeSegment(builder, start.x, start.y, start.z, end.x, end.y, end.z, 0.03f, 0.01f, 4);
        }
    }

    private void addTentacle(MeshPartBuilder builder, float x, float y, float z, float radius, float length) {
        int segments = 6;
        float px = x, py = y, pz = z;
        float r = radius;

        for (int i = 0; i < segments; i++) {
            float nx = px + (random.nextFloat() - 0.5f) * length * 0.3f;
            float ny = py - length / segments;
            float nz = pz + (random.nextFloat() - 0.5f) * length * 0.3f;
            float nr = r * 0.8f;

            addTubeSegment(builder, px, py, pz, nx, ny, nz, r, nr, 4);
            px = nx; py = ny; pz = nz; r = nr;
        }
    }

    private void addClaw(MeshPartBuilder builder, float x, float y, float z, float size, boolean left) {
        float dir = left ? 1 : -1;
        // Claw arm
        addTubeSegment(builder, x - size, y, z, x, y, z, size * 0.2f, size * 0.15f, 6);
        // Upper pincer
        addTubeSegment(builder, x, y, z, x + size * 0.6f, y + size * 0.3f, z, size * 0.1f, size * 0.02f, 4);
        // Lower pincer
        addTubeSegment(builder, x, y, z, x + size * 0.6f, y - size * 0.1f, z, size * 0.08f, size * 0.02f, 4);
    }

    private void addCrabLeg(MeshPartBuilder builder, float x, float y, float z, float length, boolean left) {
        float dir = left ? 1 : -1;
        addTubeSegment(builder, x, y, z, x, y, z + dir * length * 0.5f, 0.04f, 0.03f, 4);
        addTubeSegment(builder, x, y, z + dir * length * 0.5f, x, y - length * 0.5f, z + dir * length, 0.03f, 0.02f, 4);
    }

    private void addEyeStalk(MeshPartBuilder builder, float x, float y, float z, float size) {
        addTubeSegment(builder, x, y - size, z, x, y, z, size * 0.15f, size * 0.1f, 4);
        addEllipsoid(builder, x, y + size * 0.3f, z, size * 0.3f, size * 0.25f, size * 0.25f, 4);
    }

    private void addQuad(MeshPartBuilder builder, Vector3 v1, Vector3 v2, Vector3 v3, Vector3 v4) {
        Vector3 normal = new Vector3(v2).sub(v1).crs(new Vector3(v4).sub(v1)).nor();
        builder.rect(
            new MeshPartBuilder.VertexInfo().setPos(v1).setNor(normal).setUV(0, 0),
            new MeshPartBuilder.VertexInfo().setPos(v2).setNor(normal).setUV(1, 0),
            new MeshPartBuilder.VertexInfo().setPos(v3).setNor(normal).setUV(1, 1),
            new MeshPartBuilder.VertexInfo().setPos(v4).setNor(normal).setUV(0, 1)
        );
    }

    @Override
    public void dispose() {
        for (Texture t : generatedTextures) t.dispose();
        for (Model m : generatedModels) m.dispose();
        generatedTextures.clear();
        generatedModels.clear();
    }
}
