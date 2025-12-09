package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Random;

/**
 * Generates 3D models for desert planet features
 */
public class DesertFeatureGenerator implements Disposable {

    private final long seed;
    private final Random random;
    private final ModelBuilder modelBuilder;
    private final Array<Texture> textures = new Array<>();
    private final Array<Model> models = new Array<>();

    public enum DesertFeature {
        ROCK_SPIRE,
        ROCK_ARCH,
        BOULDER_CLUSTER,
        CACTUS_TALL,
        CACTUS_ROUND,
        DEAD_TREE,
        SAND_DUNE,
        OASIS_PALM,
        SKULL,           // Alien skull decoration
        ANCIENT_PILLAR
    }

    public DesertFeatureGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.modelBuilder = new ModelBuilder();
    }

    public Model generate(DesertFeature feature, float scale, long featureSeed) {
        random.setSeed(featureSeed);

        Model model = switch (feature) {
            case ROCK_SPIRE -> generateRockSpire(scale);
            case ROCK_ARCH -> generateRockArch(scale);
            case BOULDER_CLUSTER -> generateBoulderCluster(scale);
            case CACTUS_TALL -> generateTallCactus(scale);
            case CACTUS_ROUND -> generateRoundCactus(scale);
            case DEAD_TREE -> generateDeadTree(scale);
            case SAND_DUNE -> generateSandDune(scale);
            case OASIS_PALM -> generatePalmTree(scale);
            case SKULL -> generateAlienSkull(scale);
            case ANCIENT_PILLAR -> generateAncientPillar(scale);
        };

        models.add(model);
        return model;
    }

    private Model generateRockSpire(float scale) {
        Texture tex = DesertTextures.desertRock(64, 64, random.nextLong());
        textures.add(tex);
        Material mat = new Material(TextureAttribute.createDiffuse(tex));

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("spire", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, mat);

        float baseRadius = 1.5f * scale;
        float height = (4f + random.nextFloat() * 3f) * scale;
        int segments = 8;
        int layers = 12;

        // Build tapered spire with irregularities
        for (int layer = 0; layer < layers; layer++) {
            float t1 = (float) layer / layers;
            float t2 = (float) (layer + 1) / layers;

            float y1 = t1 * height;
            float y2 = t2 * height;

            // Taper with random bulges
            float r1 = baseRadius * (1f - t1 * 0.7f) * (0.8f + random.nextFloat() * 0.4f);
            float r2 = baseRadius * (1f - t2 * 0.7f) * (0.8f + random.nextFloat() * 0.4f);

            for (int seg = 0; seg < segments; seg++) {
                float a1 = 2 * MathUtils.PI * seg / segments;
                float a2 = 2 * MathUtils.PI * (seg + 1) / segments;

                // Add per-vertex randomness for rocky look
                float jitter1 = 1f + (random.nextFloat() - 0.5f) * 0.3f;
                float jitter2 = 1f + (random.nextFloat() - 0.5f) * 0.3f;

                Vector3 v1 = new Vector3(MathUtils.cos(a1) * r1 * jitter1, y1, MathUtils.sin(a1) * r1 * jitter1);
                Vector3 v2 = new Vector3(MathUtils.cos(a2) * r1 * jitter2, y1, MathUtils.sin(a2) * r1 * jitter2);
                Vector3 v3 = new Vector3(MathUtils.cos(a1) * r2, y2, MathUtils.sin(a1) * r2);
                Vector3 v4 = new Vector3(MathUtils.cos(a2) * r2, y2, MathUtils.sin(a2) * r2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }

        // Top cap
        float topY = height;
        Vector3 peak = new Vector3(0, topY + scale * 0.5f, 0);
        float topR = baseRadius * 0.2f;
        for (int seg = 0; seg < segments; seg++) {
            float a1 = 2 * MathUtils.PI * seg / segments;
            float a2 = 2 * MathUtils.PI * (seg + 1) / segments;
            Vector3 v1 = new Vector3(MathUtils.cos(a1) * topR, topY, MathUtils.sin(a1) * topR);
            Vector3 v2 = new Vector3(MathUtils.cos(a2) * topR, topY, MathUtils.sin(a2) * topR);
            addTriangle(builder, v1, v2, peak);
        }

        return modelBuilder.end();
    }

    private Model generateRockArch(float scale) {
        Texture tex = DesertTextures.desertRock(64, 64, random.nextLong());
        textures.add(tex);
        Material mat = new Material(TextureAttribute.createDiffuse(tex));

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("arch", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, mat);

        float width = 4f * scale;
        float height = 3f * scale;
        float thickness = 0.8f * scale;

        // Create arch using tube segments along a curve
        int segments = 16;
        float tubeRadius = thickness / 2;

        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;

            // Arch curve (semi-circle with base pillars)
            float angle1 = MathUtils.PI * t1;
            float angle2 = MathUtils.PI * t2;

            float x1, y1, x2, y2;
            if (t1 < 0.2f) {
                // Left pillar
                x1 = -width / 2;
                y1 = t1 * 5f * height / 3f;
                x2 = -width / 2;
                y2 = t2 * 5f * height / 3f;
            } else if (t1 > 0.8f) {
                // Right pillar
                x1 = width / 2;
                y1 = (1f - t1) * 5f * height / 3f;
                x2 = width / 2;
                y2 = (1f - t2) * 5f * height / 3f;
            } else {
                // Arch portion
                float archT1 = (t1 - 0.2f) / 0.6f;
                float archT2 = (t2 - 0.2f) / 0.6f;
                float archAngle1 = MathUtils.PI * archT1;
                float archAngle2 = MathUtils.PI * archT2;
                x1 = -MathUtils.cos(archAngle1) * width / 2;
                y1 = MathUtils.sin(archAngle1) * height + height / 3f;
                x2 = -MathUtils.cos(archAngle2) * width / 2;
                y2 = MathUtils.sin(archAngle2) * height + height / 3f;
            }

            addTubeSegment(builder, x1, y1, 0, x2, y2, 0, tubeRadius, tubeRadius, 6);
        }

        return modelBuilder.end();
    }

    private Model generateBoulderCluster(float scale) {
        Texture tex = DesertTextures.desertRock(64, 64, random.nextLong());
        textures.add(tex);
        Material mat = new Material(TextureAttribute.createDiffuse(tex));

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("boulders", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, mat);

        int numBoulders = 3 + random.nextInt(4);

        for (int i = 0; i < numBoulders; i++) {
            float bx = (random.nextFloat() - 0.5f) * 3f * scale;
            float bz = (random.nextFloat() - 0.5f) * 3f * scale;
            float radius = (0.5f + random.nextFloat() * 0.8f) * scale;

            addDeformedSphere(builder, bx, radius, bz, radius, 8);
        }

        return modelBuilder.end();
    }

    private Model generateTallCactus(float scale) {
        Texture tex = DesertTextures.cactus(32, 64, random.nextLong());
        textures.add(tex);
        Material mat = new Material(TextureAttribute.createDiffuse(tex));

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("cactus", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, mat);

        float bodyRadius = 0.3f * scale;
        float bodyHeight = (2f + random.nextFloat()) * scale;

        // Main body with ridges
        addRidgedCylinder(builder, 0, 0, 0, bodyRadius, bodyHeight, 8, 8);

        // Arms
        int numArms = 1 + random.nextInt(3);
        for (int i = 0; i < numArms; i++) {
            float armAngle = random.nextFloat() * MathUtils.PI2;
            float armHeight = bodyHeight * (0.4f + random.nextFloat() * 0.3f);
            float armLength = (0.5f + random.nextFloat() * 0.5f) * scale;
            float armRadius = bodyRadius * 0.6f;

            // Horizontal portion
            float ax = MathUtils.cos(armAngle) * bodyRadius;
            float az = MathUtils.sin(armAngle) * bodyRadius;
            float ax2 = MathUtils.cos(armAngle) * (bodyRadius + armLength);
            float az2 = MathUtils.sin(armAngle) * (bodyRadius + armLength);

            addTubeSegment(builder, ax, armHeight, az, ax2, armHeight, az2, armRadius, armRadius, 6);

            // Upward portion
            float armUpHeight = armLength * (0.5f + random.nextFloat() * 0.5f);
            addTubeSegment(builder, ax2, armHeight, az2, ax2, armHeight + armUpHeight, az2, armRadius, armRadius * 0.8f, 6);

            // Cap
            addHemisphere(builder, ax2, armHeight + armUpHeight, az2, armRadius * 0.8f, 4);
        }

        // Top cap
        addHemisphere(builder, 0, bodyHeight, 0, bodyRadius, 6);

        return modelBuilder.end();
    }

    private Model generateRoundCactus(float scale) {
        Texture tex = DesertTextures.cactus(32, 32, random.nextLong());
        textures.add(tex);
        Material mat = new Material(TextureAttribute.createDiffuse(tex));

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("cactus", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, mat);

        float radius = (0.4f + random.nextFloat() * 0.3f) * scale;

        // Barrel cactus - slightly flattened sphere with ridges
        addRidgedSphere(builder, 0, radius * 0.9f, 0, radius, radius * 0.9f, 12, 8);

        return modelBuilder.end();
    }

    private Model generateDeadTree(float scale) {
        Texture tex = DesertTextures.crackedEarth(32, 32, random.nextLong());
        textures.add(tex);
        Material mat = new Material(TextureAttribute.createDiffuse(tex));

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("tree", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, mat);

        float trunkRadius = 0.15f * scale;
        float trunkHeight = (1.5f + random.nextFloat()) * scale;

        // Gnarled trunk
        addTwistedTrunk(builder, 0, 0, 0, trunkRadius, trunkHeight, 6);

        // Dead branches
        int numBranches = 3 + random.nextInt(4);
        for (int i = 0; i < numBranches; i++) {
            float branchAngle = random.nextFloat() * MathUtils.PI2;
            float branchHeight = trunkHeight * (0.5f + random.nextFloat() * 0.4f);
            float branchLength = (0.4f + random.nextFloat() * 0.5f) * scale;
            float branchRadius = trunkRadius * 0.4f;

            float branchPitch = MathUtils.PI * 0.1f + random.nextFloat() * MathUtils.PI * 0.3f;

            float bx2 = MathUtils.cos(branchAngle) * MathUtils.cos(branchPitch) * branchLength;
            float by2 = MathUtils.sin(branchPitch) * branchLength;
            float bz2 = MathUtils.sin(branchAngle) * MathUtils.cos(branchPitch) * branchLength;

            addTubeSegment(builder, 0, branchHeight, 0, bx2, branchHeight + by2, bz2, branchRadius, branchRadius * 0.3f, 4);
        }

        return modelBuilder.end();
    }

    private Model generateSandDune(float scale) {
        Texture tex = DesertTextures.sandDunes(64, 64, random.nextLong());
        textures.add(tex);
        Material mat = new Material(TextureAttribute.createDiffuse(tex));

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("dune", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, mat);

        float width = 6f * scale;
        float length = 10f * scale;
        float height = 1.5f * scale;

        int xSegs = 12;
        int zSegs = 16;

        for (int z = 0; z < zSegs; z++) {
            for (int x = 0; x < xSegs; x++) {
                float x1 = (float) x / xSegs * width - width / 2;
                float x2 = (float) (x + 1) / xSegs * width - width / 2;
                float z1 = (float) z / zSegs * length - length / 2;
                float z2 = (float) (z + 1) / zSegs * length - length / 2;

                // Dune height function
                float y11 = duneHeight(x1, z1, width, length, height);
                float y12 = duneHeight(x1, z2, width, length, height);
                float y21 = duneHeight(x2, z1, width, length, height);
                float y22 = duneHeight(x2, z2, width, length, height);

                Vector3 v1 = new Vector3(x1, y11, z1);
                Vector3 v2 = new Vector3(x2, y21, z2);
                Vector3 v3 = new Vector3(x2, y22, z2);
                Vector3 v4 = new Vector3(x1, y12, z2);

                addQuad(builder, v1, new Vector3(x2, y21, z1), v3, v4);
            }
        }

        return modelBuilder.end();
    }

    private float duneHeight(float x, float z, float width, float length, float maxHeight) {
        float nx = x / (width / 2);
        float nz = z / (length / 2);

        // Smooth dune shape
        float height = maxHeight * (1f - nx * nx) * MathUtils.cos(nz * MathUtils.PI / 2);
        height *= 0.5f + 0.5f * MathUtils.sin(z * 0.5f + x * 0.3f);

        return Math.max(0, height);
    }

    private Model generatePalmTree(float scale) {
        // Trunk texture
        Texture trunkTex = DesertTextures.crackedEarth(32, 64, random.nextLong());
        textures.add(trunkTex);
        Material trunkMat = new Material(TextureAttribute.createDiffuse(trunkTex));

        // Frond texture (reuse cactus green)
        Texture frondTex = DesertTextures.cactus(64, 32, random.nextLong());
        textures.add(frondTex);
        Material frondMat = new Material(TextureAttribute.createDiffuse(frondTex));

        modelBuilder.begin();

        // Trunk
        MeshPartBuilder trunk = modelBuilder.part("trunk", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, trunkMat);

        float trunkRadius = 0.2f * scale;
        float trunkHeight = 3f * scale;

        addTubeSegment(trunk, 0, 0, 0, 0, trunkHeight, 0, trunkRadius, trunkRadius * 0.7f, 8);

        // Fronds
        MeshPartBuilder fronds = modelBuilder.part("fronds", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, frondMat);

        int numFronds = 6 + random.nextInt(4);
        for (int i = 0; i < numFronds; i++) {
            float angle = MathUtils.PI2 * i / numFronds + random.nextFloat() * 0.3f;
            float droop = MathUtils.PI * 0.15f + random.nextFloat() * 0.2f;
            float frondLength = (1.5f + random.nextFloat() * 0.5f) * scale;

            addPalmFrond(fronds, 0, trunkHeight, 0, angle, droop, frondLength, 0.3f * scale);
        }

        return modelBuilder.end();
    }

    private Model generateAlienSkull(float scale) {
        Texture tex = DesertTextures.crackedEarth(32, 32, random.nextLong());
        textures.add(tex);
        Material mat = new Material(TextureAttribute.createDiffuse(tex));

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("skull", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, mat);

        float size = scale;

        // Elongated cranium
        addEllipsoid(builder, 0, size * 0.8f, 0, size * 0.6f, size * 0.9f, size * 0.5f, 8);

        // Eye sockets (indentations represented by smaller dark spheres conceptually - using geometry)
        // Jaw area
        addEllipsoid(builder, 0, size * 0.2f, size * 0.3f, size * 0.4f, size * 0.3f, size * 0.3f, 6);

        return modelBuilder.end();
    }

    private Model generateAncientPillar(float scale) {
        Texture tex = DesertTextures.desertRock(64, 64, random.nextLong());
        textures.add(tex);
        Material mat = new Material(TextureAttribute.createDiffuse(tex));

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("pillar", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates, mat);

        float radius = 0.4f * scale;
        float height = 3f * scale;

        // Base
        addCylinder(builder, 0, 0, 0, radius * 1.3f, height * 0.1f, 8);

        // Main shaft with subtle taper
        addTubeSegment(builder, 0, height * 0.1f, 0, 0, height * 0.9f, 0, radius, radius * 0.9f, 8);

        // Capital
        addCylinder(builder, 0, height * 0.9f, 0, radius * 1.2f, height * 0.1f, 8);

        // Add some weathering/damage (random chunks missing simulated by irregular top)
        if (random.nextFloat() < 0.5f) {
            // Broken pillar - cut off top portion
            // Already handled by the height
        }

        return modelBuilder.end();
    }

    // Helper methods
    private void addQuad(MeshPartBuilder builder, Vector3 v1, Vector3 v2, Vector3 v3, Vector3 v4) {
        Vector3 normal = new Vector3(v2).sub(v1).crs(new Vector3(v4).sub(v1)).nor();
        builder.rect(
            new MeshPartBuilder.VertexInfo().setPos(v1).setNor(normal).setUV(0, 0),
            new MeshPartBuilder.VertexInfo().setPos(v2).setNor(normal).setUV(1, 0),
            new MeshPartBuilder.VertexInfo().setPos(v3).setNor(normal).setUV(1, 1),
            new MeshPartBuilder.VertexInfo().setPos(v4).setNor(normal).setUV(0, 1)
        );
    }

    private void addTriangle(MeshPartBuilder builder, Vector3 v1, Vector3 v2, Vector3 v3) {
        Vector3 normal = new Vector3(v2).sub(v1).crs(new Vector3(v3).sub(v1)).nor();
        builder.triangle(
            new MeshPartBuilder.VertexInfo().setPos(v1).setNor(normal).setUV(0, 0),
            new MeshPartBuilder.VertexInfo().setPos(v2).setNor(normal).setUV(1, 0),
            new MeshPartBuilder.VertexInfo().setPos(v3).setNor(normal).setUV(0.5f, 1)
        );
    }

    private void addTubeSegment(MeshPartBuilder builder, float x1, float y1, float z1, float x2, float y2, float z2, float r1, float r2, int segments) {
        Vector3 dir = new Vector3(x2 - x1, y2 - y1, z2 - z1).nor();
        Vector3 up = Math.abs(dir.y) < 0.99f ? Vector3.Y : Vector3.X;
        Vector3 right = new Vector3(dir).crs(up).nor();
        Vector3 forward = new Vector3(right).crs(dir).nor();

        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 off1_1 = new Vector3(right).scl(MathUtils.cos(a1) * r1).add(new Vector3(forward).scl(MathUtils.sin(a1) * r1));
            Vector3 off1_2 = new Vector3(right).scl(MathUtils.cos(a2) * r1).add(new Vector3(forward).scl(MathUtils.sin(a2) * r1));
            Vector3 off2_1 = new Vector3(right).scl(MathUtils.cos(a1) * r2).add(new Vector3(forward).scl(MathUtils.sin(a1) * r2));
            Vector3 off2_2 = new Vector3(right).scl(MathUtils.cos(a2) * r2).add(new Vector3(forward).scl(MathUtils.sin(a2) * r2));

            Vector3 v1 = new Vector3(x1, y1, z1).add(off1_1);
            Vector3 v2 = new Vector3(x1, y1, z1).add(off1_2);
            Vector3 v3 = new Vector3(x2, y2, z2).add(off2_1);
            Vector3 v4 = new Vector3(x2, y2, z2).add(off2_2);

            addQuad(builder, v1, v2, v4, v3);
        }
    }

    private void addDeformedSphere(MeshPartBuilder builder, float cx, float cy, float cz, float radius, int divisions) {
        for (int lat = 0; lat < divisions; lat++) {
            float theta1 = MathUtils.PI * lat / divisions;
            float theta2 = MathUtils.PI * (lat + 1) / divisions;

            for (int lon = 0; lon < divisions; lon++) {
                float phi1 = 2 * MathUtils.PI * lon / divisions;
                float phi2 = 2 * MathUtils.PI * (lon + 1) / divisions;

                float r1 = radius * (0.7f + random.nextFloat() * 0.5f);
                float r2 = radius * (0.7f + random.nextFloat() * 0.5f);
                float r3 = radius * (0.7f + random.nextFloat() * 0.5f);
                float r4 = radius * (0.7f + random.nextFloat() * 0.5f);

                Vector3 v1 = spherePoint(cx, cy, cz, r1, theta1, phi1);
                Vector3 v2 = spherePoint(cx, cy, cz, r2, theta1, phi2);
                Vector3 v3 = spherePoint(cx, cy, cz, r3, theta2, phi1);
                Vector3 v4 = spherePoint(cx, cy, cz, r4, theta2, phi2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }
    }

    private Vector3 spherePoint(float cx, float cy, float cz, float r, float theta, float phi) {
        return new Vector3(
            cx + r * MathUtils.sin(theta) * MathUtils.cos(phi),
            cy + r * MathUtils.cos(theta),
            cz + r * MathUtils.sin(theta) * MathUtils.sin(phi)
        );
    }

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

    private void addRidgedCylinder(MeshPartBuilder builder, float cx, float cy, float cz, float radius, float height, int ridges, int segments) {
        for (int seg = 0; seg < segments; seg++) {
            float t1 = (float) seg / segments;
            float t2 = (float) (seg + 1) / segments;
            float y1 = cy + t1 * height;
            float y2 = cy + t2 * height;

            for (int r = 0; r < ridges; r++) {
                float a1 = 2 * MathUtils.PI * r / ridges;
                float a2 = 2 * MathUtils.PI * (r + 1) / ridges;
                float aMid = (a1 + a2) / 2;

                // Ridge effect
                float r1 = radius * (0.85f + 0.15f * MathUtils.cos((a1 - aMid) * ridges));
                float r2 = radius * (0.85f + 0.15f * MathUtils.cos((a2 - aMid) * ridges));

                Vector3 v1 = new Vector3(cx + MathUtils.cos(a1) * r1, y1, cz + MathUtils.sin(a1) * r1);
                Vector3 v2 = new Vector3(cx + MathUtils.cos(a2) * r2, y1, cz + MathUtils.sin(a2) * r2);
                Vector3 v3 = new Vector3(cx + MathUtils.cos(a1) * r1, y2, cz + MathUtils.sin(a1) * r1);
                Vector3 v4 = new Vector3(cx + MathUtils.cos(a2) * r2, y2, cz + MathUtils.sin(a2) * r2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }
    }

    private void addRidgedSphere(MeshPartBuilder builder, float cx, float cy, float cz, float rx, float ry, int latDiv, int ridges) {
        for (int lat = 0; lat < latDiv; lat++) {
            float theta1 = MathUtils.PI * lat / latDiv;
            float theta2 = MathUtils.PI * (lat + 1) / latDiv;

            for (int lon = 0; lon < ridges; lon++) {
                float phi1 = 2 * MathUtils.PI * lon / ridges;
                float phi2 = 2 * MathUtils.PI * (lon + 1) / ridges;
                float phiMid = (phi1 + phi2) / 2;

                float ridgeFactor1 = 0.85f + 0.15f * MathUtils.cos((phi1 - phiMid) * ridges);
                float ridgeFactor2 = 0.85f + 0.15f * MathUtils.cos((phi2 - phiMid) * ridges);

                Vector3 v1 = new Vector3(
                    cx + rx * ridgeFactor1 * MathUtils.sin(theta1) * MathUtils.cos(phi1),
                    cy + ry * MathUtils.cos(theta1),
                    cz + rx * ridgeFactor1 * MathUtils.sin(theta1) * MathUtils.sin(phi1)
                );
                Vector3 v2 = new Vector3(
                    cx + rx * ridgeFactor2 * MathUtils.sin(theta1) * MathUtils.cos(phi2),
                    cy + ry * MathUtils.cos(theta1),
                    cz + rx * ridgeFactor2 * MathUtils.sin(theta1) * MathUtils.sin(phi2)
                );
                Vector3 v3 = new Vector3(
                    cx + rx * ridgeFactor1 * MathUtils.sin(theta2) * MathUtils.cos(phi1),
                    cy + ry * MathUtils.cos(theta2),
                    cz + rx * ridgeFactor1 * MathUtils.sin(theta2) * MathUtils.sin(phi1)
                );
                Vector3 v4 = new Vector3(
                    cx + rx * ridgeFactor2 * MathUtils.sin(theta2) * MathUtils.cos(phi2),
                    cy + ry * MathUtils.cos(theta2),
                    cz + rx * ridgeFactor2 * MathUtils.sin(theta2) * MathUtils.sin(phi2)
                );

                addQuad(builder, v1, v2, v4, v3);
            }
        }
    }

    private void addHemisphere(MeshPartBuilder builder, float cx, float cy, float cz, float radius, int divisions) {
        for (int lat = 0; lat < divisions / 2; lat++) {
            float theta1 = MathUtils.PI * lat / divisions;
            float theta2 = MathUtils.PI * (lat + 1) / divisions;

            for (int lon = 0; lon < divisions; lon++) {
                float phi1 = 2 * MathUtils.PI * lon / divisions;
                float phi2 = 2 * MathUtils.PI * (lon + 1) / divisions;

                Vector3 v1 = spherePoint(cx, cy, cz, radius, theta1, phi1);
                Vector3 v2 = spherePoint(cx, cy, cz, radius, theta1, phi2);
                Vector3 v3 = spherePoint(cx, cy, cz, radius, theta2, phi1);
                Vector3 v4 = spherePoint(cx, cy, cz, radius, theta2, phi2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }
    }

    private void addTwistedTrunk(MeshPartBuilder builder, float cx, float cy, float cz, float radius, float height, int segments) {
        int layers = 8;
        for (int layer = 0; layer < layers; layer++) {
            float t1 = (float) layer / layers;
            float t2 = (float) (layer + 1) / layers;
            float y1 = cy + t1 * height;
            float y2 = cy + t2 * height;

            float twist1 = t1 * MathUtils.PI * 0.5f;
            float twist2 = t2 * MathUtils.PI * 0.5f;

            float r1 = radius * (1f - t1 * 0.3f);
            float r2 = radius * (1f - t2 * 0.3f);

            for (int seg = 0; seg < segments; seg++) {
                float a1 = 2 * MathUtils.PI * seg / segments + twist1;
                float a2 = 2 * MathUtils.PI * (seg + 1) / segments + twist1;
                float a3 = 2 * MathUtils.PI * seg / segments + twist2;
                float a4 = 2 * MathUtils.PI * (seg + 1) / segments + twist2;

                Vector3 v1 = new Vector3(cx + MathUtils.cos(a1) * r1, y1, cz + MathUtils.sin(a1) * r1);
                Vector3 v2 = new Vector3(cx + MathUtils.cos(a2) * r1, y1, cz + MathUtils.sin(a2) * r1);
                Vector3 v3 = new Vector3(cx + MathUtils.cos(a3) * r2, y2, cz + MathUtils.sin(a3) * r2);
                Vector3 v4 = new Vector3(cx + MathUtils.cos(a4) * r2, y2, cz + MathUtils.sin(a4) * r2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }
    }

    private void addCylinder(MeshPartBuilder builder, float cx, float cy, float cz, float radius, float height, int segments) {
        addTubeSegment(builder, cx, cy, cz, cx, cy + height, cz, radius, radius, segments);
    }

    private void addPalmFrond(MeshPartBuilder builder, float cx, float cy, float cz, float angle, float droop, float length, float width) {
        int segments = 8;
        float thickness = 0.02f;

        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;

            float droopAngle1 = droop * t1 * t1;
            float droopAngle2 = droop * t2 * t2;

            float x1 = cx + MathUtils.cos(angle) * MathUtils.cos(droopAngle1) * t1 * length;
            float y1 = cy + MathUtils.sin(droopAngle1) * t1 * length * 0.3f - t1 * t1 * length * 0.5f;
            float z1 = cz + MathUtils.sin(angle) * MathUtils.cos(droopAngle1) * t1 * length;

            float x2 = cx + MathUtils.cos(angle) * MathUtils.cos(droopAngle2) * t2 * length;
            float y2 = cy + MathUtils.sin(droopAngle2) * t2 * length * 0.3f - t2 * t2 * length * 0.5f;
            float z2 = cz + MathUtils.sin(angle) * MathUtils.cos(droopAngle2) * t2 * length;

            float w1 = width * (1f - t1 * 0.7f);
            float w2 = width * (1f - t2 * 0.7f);

            // Perpendicular to frond direction
            float perpX = -MathUtils.sin(angle);
            float perpZ = MathUtils.cos(angle);

            Vector3 v1 = new Vector3(x1 + perpX * w1, y1, z1 + perpZ * w1);
            Vector3 v2 = new Vector3(x1 - perpX * w1, y1, z1 - perpZ * w1);
            Vector3 v3 = new Vector3(x2 + perpX * w2, y2, z2 + perpZ * w2);
            Vector3 v4 = new Vector3(x2 - perpX * w2, y2, z2 - perpZ * w2);

            addQuad(builder, v1, v2, v4, v3);
        }
    }

    @Override
    public void dispose() {
        for (Texture t : textures) t.dispose();
        for (Model m : models) m.dispose();
        textures.clear();
        models.clear();
    }
}
