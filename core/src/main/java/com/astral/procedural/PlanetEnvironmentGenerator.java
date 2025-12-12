package com.astral.procedural;

import com.astral.shipbuilding.LGMesh;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Random;

/**
 * PlanetEnvironmentGenerator - Creates realistic planetary environments
 * with rocks, vegetation, atmospheric effects, and detail objects.
 * Uses LGMesh for efficient custom geometry generation.
 */
public class PlanetEnvironmentGenerator implements Disposable {

    private final long seed;
    private final Random random;
    private final PlanetType planetType;

    // Cached models for reuse
    private Model rockModel1;
    private Model rockModel2;
    private Model rockModel3;
    private Model grassModel;
    private Model bushModel;
    private Model treeModel;
    private Model crystalModel;
    private Model cactusModel;
    private Model iceFormationModel;
    private Model deadTreeModel;

    // Textures
    private Array<Texture> generatedTextures;

    // Detail settings
    private float vegetationDensity = 1.0f;
    private float rockDensity = 1.0f;

    public PlanetEnvironmentGenerator(long seed, PlanetType planetType) {
        this.seed = seed;
        this.random = new Random(seed);
        this.planetType = planetType;
        this.generatedTextures = new Array<>();

        generateModels();
    }

    /**
     * Generate all environment models based on planet type
     */
    private void generateModels() {
        // Always generate rocks (present on all planet types)
        rockModel1 = generateRock(1.0f, 0.8f, 0.9f);
        rockModel2 = generateRock(1.2f, 1.0f, 0.7f);
        rockModel3 = generateRock(0.8f, 1.1f, 1.0f);

        // Generate planet-specific features
        switch (planetType) {
            case DESERT:
                cactusModel = generateCactus();
                deadTreeModel = generateDeadTree();
                crystalModel = generateCrystal(new Color(0.9f, 0.7f, 0.3f, 0.8f));
                break;
            case FOREST:
                grassModel = generateGrass();
                bushModel = generateBush();
                treeModel = generateTree();
                break;
            case ICE:
                iceFormationModel = generateIceFormation();
                crystalModel = generateCrystal(new Color(0.7f, 0.85f, 1.0f, 0.9f));
                break;
            case LAVA:
                crystalModel = generateCrystal(new Color(1.0f, 0.3f, 0.1f, 0.7f));
                deadTreeModel = generateDeadTree();
                break;
            case OCEAN:
                grassModel = generateGrass();
                bushModel = generateBush();
                break;
        }
    }

    /**
     * Populate an area with environmental objects
     */
    public Array<ModelInstance> populateArea(Vector3 center, float radius, int density) {
        Array<ModelInstance> instances = new Array<>();
        random.setSeed(seed + (long)(center.x * 1000) + (long)(center.z * 1000));

        int numObjects = (int)(density * (planetType == PlanetType.FOREST ? 1.5f : 1.0f));

        for (int i = 0; i < numObjects; i++) {
            // Random position within radius
            float angle = random.nextFloat() * MathUtils.PI2;
            float distance = random.nextFloat() * radius;
            float x = center.x + MathUtils.cos(angle) * distance;
            float z = center.z + MathUtils.sin(angle) * distance;

            // Determine what to spawn based on planet type
            ModelInstance instance = createEnvironmentObject(x, z);
            if (instance != null) {
                instances.add(instance);
            }
        }

        return instances;
    }

    /**
     * Create a single environment object at position
     */
    private ModelInstance createEnvironmentObject(float x, float z) {
        float roll = random.nextFloat();
        Model model = null;
        float scale = 1.0f;
        float yOffset = 0f;

        switch (planetType) {
            case DESERT:
                if (roll < 0.4f) {
                    model = rockModel1;
                    scale = 0.5f + random.nextFloat() * 1.5f;
                } else if (roll < 0.7f) {
                    model = cactusModel;
                    scale = 0.8f + random.nextFloat() * 0.6f;
                    yOffset = 0.1f;
                } else if (roll < 0.85f) {
                    model = deadTreeModel;
                    scale = 1.0f + random.nextFloat() * 0.5f;
                } else if (roll < 0.95f) {
                    model = crystalModel;
                    scale = 0.3f + random.nextFloat() * 0.5f;
                    yOffset = 0.2f;
                } else {
                    model = rockModel2;
                    scale = 0.8f + random.nextFloat() * 1.0f;
                }
                break;

            case FOREST:
                if (roll < 0.3f) {
                    model = treeModel;
                    scale = 0.8f + random.nextFloat() * 0.7f;
                } else if (roll < 0.6f) {
                    model = bushModel;
                    scale = 0.5f + random.nextFloat() * 0.5f;
                } else if (roll < 0.8f) {
                    model = grassModel;
                    scale = 0.7f + random.nextFloat() * 0.6f;
                } else {
                    model = rockModel1;
                    scale = 0.4f + random.nextFloat() * 1.0f;
                }
                break;

            case ICE:
                if (roll < 0.5f) {
                    model = rockModel1;
                    scale = 0.6f + random.nextFloat() * 1.4f;
                } else if (roll < 0.8f) {
                    model = iceFormationModel;
                    scale = 0.5f + random.nextFloat() * 1.0f;
                    yOffset = 0.1f;
                } else {
                    model = crystalModel;
                    scale = 0.4f + random.nextFloat() * 0.6f;
                    yOffset = 0.2f;
                }
                break;

            case LAVA:
                if (roll < 0.6f) {
                    model = rockModel1;
                    scale = 0.5f + random.nextFloat() * 1.5f;
                } else if (roll < 0.85f) {
                    model = crystalModel;
                    scale = 0.3f + random.nextFloat() * 0.7f;
                    yOffset = 0.15f;
                } else {
                    model = deadTreeModel;
                    scale = 0.8f + random.nextFloat() * 0.5f;
                }
                break;

            case OCEAN:
                if (roll < 0.5f) {
                    model = grassModel;
                    scale = 0.6f + random.nextFloat() * 0.6f;
                } else if (roll < 0.8f) {
                    model = bushModel;
                    scale = 0.5f + random.nextFloat() * 0.5f;
                } else {
                    model = rockModel1;
                    scale = 0.5f + random.nextFloat() * 1.0f;
                }
                break;
        }

        if (model == null) return null;

        ModelInstance instance = new ModelInstance(model);

        // Random rotation around Y axis
        float rotation = random.nextFloat() * 360f;

        // Apply transform
        instance.transform.setToTranslation(x, yOffset, z);
        instance.transform.rotate(Vector3.Y, rotation);
        instance.transform.scale(scale, scale, scale);

        return instance;
    }

    // ==================== Rock Generation ====================

    private Model generateRock(float widthScale, float heightScale, float depthScale) {
        LGMesh mesh = new LGMesh();
        Color rockColor = getRockColor();

        // Create irregular rock shape using deformed sphere
        int segments = 8;
        float radius = 1.0f;

        // Top vertex
        short topVertex = mesh.addVertex(
            0, heightScale * radius, 0,
            0, 1, 0,
            0.5f, 1.0f,
            rockColor.r, rockColor.g, rockColor.b, 1f
        );

        // Bottom vertex
        short bottomVertex = mesh.addVertex(
            0, -heightScale * radius * 0.7f, 0,
            0, -1, 0,
            0.5f, 0.0f,
            rockColor.r, rockColor.g, rockColor.b, 1f
        );

        // Middle vertices with deformation
        short[] middleVertices = new short[segments];
        for (int i = 0; i < segments; i++) {
            float angle = (float)i / segments * MathUtils.PI2;
            float deform = 0.7f + random.nextFloat() * 0.6f; // Random deformation
            float x = MathUtils.cos(angle) * radius * widthScale * deform;
            float z = MathUtils.sin(angle) * radius * depthScale * deform;
            float y = (random.nextFloat() - 0.5f) * 0.3f * heightScale;

            Vector3 normal = new Vector3(x, 0, z).nor();

            middleVertices[i] = mesh.addVertex(
                x, y, z,
                normal.x, normal.y, normal.z,
                (float)i / segments, 0.5f,
                rockColor.r, rockColor.g, rockColor.b, 1f
            );
        }

        // Build triangles
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;

            // Top triangles
            mesh.addTriangle(topVertex, middleVertices[next], middleVertices[i]);

            // Bottom triangles
            mesh.addTriangle(bottomVertex, middleVertices[i], middleVertices[next]);
        }

        Texture rockTexture = ProceduralTexture.rock(128, 128, rockColor, seed + 1);
        generatedTextures.add(rockTexture);

        Material material = new Material(
            TextureAttribute.createDiffuse(rockTexture),
            ColorAttribute.createDiffuse(rockColor)
        );

        return mesh.buildModel(material, "rock");
    }

    // ==================== Vegetation Generation ====================

    private Model generateGrass() {
        LGMesh mesh = new LGMesh();
        Color grassColor = new Color(0.3f, 0.6f, 0.2f, 1f);

        float height = 1.0f;
        float width = 0.3f;

        // Create crossed grass blades
        for (int blade = 0; blade < 2; blade++) {
            float angle = blade * MathUtils.PI / 2;
            float cos = MathUtils.cos(angle);
            float sin = MathUtils.sin(angle);

            short v0 = mesh.addVertex(-width * cos, 0, -width * sin, 0, 0, 1, 0, 0, grassColor.r, grassColor.g, grassColor.b, 1f);
            short v1 = mesh.addVertex(width * cos, 0, width * sin, 0, 0, 1, 1, 0, grassColor.r, grassColor.g, grassColor.b, 1f);
            short v2 = mesh.addVertex(width * cos * 0.5f, height, width * sin * 0.5f, 0, 0, 1, 1, 1, grassColor.r, grassColor.g, grassColor.b, 1f);
            short v3 = mesh.addVertex(-width * cos * 0.5f, height, -width * sin * 0.5f, 0, 0, 1, 0, 1, grassColor.r, grassColor.g, grassColor.b, 1f);

            mesh.addQuad(v0, v1, v2, v3);
        }

        Material material = new Material(ColorAttribute.createDiffuse(grassColor));
        return mesh.buildModel(material, "grass");
    }

    private Model generateBush() {
        LGMesh mesh = new LGMesh();
        Color bushColor = new Color(0.25f, 0.5f, 0.2f, 1f);

        // Create rounded bush shape using multiple spherical sections
        float radius = 0.6f;
        int segments = 6;
        int rings = 4;

        for (int ring = 0; ring < rings; ring++) {
            float v = (float)ring / rings;
            float ringRadius = radius * MathUtils.sin(v * MathUtils.PI);
            float y = radius * MathUtils.cos(v * MathUtils.PI);

            for (int seg = 0; seg < segments; seg++) {
                float u = (float)seg / segments;
                float angle = u * MathUtils.PI2;
                float x = MathUtils.cos(angle) * ringRadius;
                float z = MathUtils.sin(angle) * ringRadius;

                Vector3 normal = new Vector3(x, y, z).nor();
                mesh.addVertex(x, y, z, normal.x, normal.y, normal.z, u, v, bushColor.r, bushColor.g, bushColor.b, 1f);
            }
        }

        // Build triangles between rings
        for (int ring = 0; ring < rings - 1; ring++) {
            for (int seg = 0; seg < segments; seg++) {
                int nextSeg = (seg + 1) % segments;
                short v0 = (short)(ring * segments + seg);
                short v1 = (short)(ring * segments + nextSeg);
                short v2 = (short)((ring + 1) * segments + nextSeg);
                short v3 = (short)((ring + 1) * segments + seg);

                mesh.addQuad(v0, v1, v2, v3);
            }
        }

        Material material = new Material(ColorAttribute.createDiffuse(bushColor));
        return mesh.buildModel(material, "bush");
    }

    private Model generateTree() {
        LGMesh mesh = new LGMesh();
        Color trunkColor = new Color(0.4f, 0.3f, 0.2f, 1f);
        Color foliageColor = new Color(0.2f, 0.5f, 0.15f, 1f);

        // Trunk
        float trunkRadius = 0.2f;
        float trunkHeight = 2.5f;
        int segments = 6;

        for (int i = 0; i < segments; i++) {
            float angle = (float)i / segments * MathUtils.PI2;
            float x = MathUtils.cos(angle) * trunkRadius;
            float z = MathUtils.sin(angle) * trunkRadius;

            Vector3 normal = new Vector3(x, 0, z).nor();

            mesh.addVertex(x, 0, z, normal.x, 0, normal.z, (float)i/segments, 0, trunkColor.r, trunkColor.g, trunkColor.b, 1f);
            mesh.addVertex(x * 0.8f, trunkHeight, z * 0.8f, normal.x, 0, normal.z, (float)i/segments, 1, trunkColor.r, trunkColor.g, trunkColor.b, 1f);
        }

        // Build trunk triangles
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            short v0 = (short)(i * 2);
            short v1 = (short)(i * 2 + 1);
            short v2 = (short)(next * 2 + 1);
            short v3 = (short)(next * 2);

            mesh.addQuad(v0, v1, v2, v3);
        }

        // Foliage (simple cone shape)
        short topVertex = mesh.addVertex(0, trunkHeight + 2.0f, 0, 0, 1, 0, 0.5f, 1, foliageColor.r, foliageColor.g, foliageColor.b, 1f);

        int foliageSegments = 8;
        float foliageRadius = 1.2f;
        short[] foliageVerts = new short[foliageSegments];

        for (int i = 0; i < foliageSegments; i++) {
            float angle = (float)i / foliageSegments * MathUtils.PI2;
            float x = MathUtils.cos(angle) * foliageRadius;
            float z = MathUtils.sin(angle) * foliageRadius;
            Vector3 normal = new Vector3(x, 0.5f, z).nor();

            foliageVerts[i] = mesh.addVertex(
                x, trunkHeight, z,
                normal.x, normal.y, normal.z,
                (float)i/foliageSegments, 0.5f,
                foliageColor.r, foliageColor.g, foliageColor.b, 1f
            );
        }

        for (int i = 0; i < foliageSegments; i++) {
            int next = (i + 1) % foliageSegments;
            mesh.addTriangle(topVertex, foliageVerts[next], foliageVerts[i]);
        }

        Material material = new Material(
            ColorAttribute.createDiffuse(Color.WHITE)
        );
        return mesh.buildModel(material, "tree");
    }

    // ==================== Desert Features ====================

    private Model generateCactus() {
        LGMesh mesh = new LGMesh();
        Color cactusColor = new Color(0.3f, 0.6f, 0.3f, 1f);

        // Main vertical column
        float radius = 0.2f;
        float height = 2.0f;
        int segments = 6;

        for (int i = 0; i < segments; i++) {
            float angle = (float)i / segments * MathUtils.PI2;
            float x = MathUtils.cos(angle) * radius;
            float z = MathUtils.sin(angle) * radius;

            Vector3 normal = new Vector3(x, 0, z).nor();

            mesh.addVertex(x, 0, z, normal.x, 0, normal.z, (float)i/segments, 0, cactusColor.r, cactusColor.g, cactusColor.b, 1f);
            mesh.addVertex(x, height, z, normal.x, 0, normal.z, (float)i/segments, 1, cactusColor.r, cactusColor.g, cactusColor.b, 1f);
        }

        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            short v0 = (short)(i * 2);
            short v1 = (short)(i * 2 + 1);
            short v2 = (short)(next * 2 + 1);
            short v3 = (short)(next * 2);

            mesh.addQuad(v0, v1, v2, v3);
        }

        Material material = new Material(ColorAttribute.createDiffuse(cactusColor));
        return mesh.buildModel(material, "cactus");
    }

    private Model generateDeadTree() {
        LGMesh mesh = new LGMesh();
        Color deadColor = new Color(0.4f, 0.35f, 0.3f, 1f);

        float trunkRadius = 0.15f;
        float trunkHeight = 2.0f;
        int segments = 5;

        // Twisted trunk
        for (int ring = 0; ring < 3; ring++) {
            float y = ring * trunkHeight / 2;
            float twist = ring * 0.3f;

            for (int i = 0; i < segments; i++) {
                float angle = ((float)i / segments * MathUtils.PI2) + twist;
                float x = MathUtils.cos(angle) * trunkRadius;
                float z = MathUtils.sin(angle) * trunkRadius;

                Vector3 normal = new Vector3(x, 0, z).nor();

                mesh.addVertex(x, y, z, normal.x, 0.3f, normal.z, (float)i/segments, (float)ring/2, deadColor.r, deadColor.g, deadColor.b, 1f);
            }
        }

        // Build triangles
        for (int ring = 0; ring < 2; ring++) {
            for (int i = 0; i < segments; i++) {
                int next = (i + 1) % segments;
                short v0 = (short)(ring * segments + i);
                short v1 = (short)((ring + 1) * segments + i);
                short v2 = (short)((ring + 1) * segments + next);
                short v3 = (short)(ring * segments + next);

                mesh.addQuad(v0, v1, v2, v3);
            }
        }

        Material material = new Material(ColorAttribute.createDiffuse(deadColor));
        return mesh.buildModel(material, "dead_tree");
    }

    // ==================== Ice Features ====================

    private Model generateIceFormation() {
        LGMesh mesh = new LGMesh();
        Color iceColor = new Color(0.8f, 0.9f, 1.0f, 0.7f);

        // Irregular ice spike
        int segments = 6;
        float baseRadius = 0.5f;
        float topRadius = 0.1f;
        float height = 1.5f;

        // Bottom vertices
        for (int i = 0; i < segments; i++) {
            float angle = (float)i / segments * MathUtils.PI2;
            float deform = 0.8f + random.nextFloat() * 0.4f;
            float x = MathUtils.cos(angle) * baseRadius * deform;
            float z = MathUtils.sin(angle) * baseRadius * deform;

            Vector3 normal = new Vector3(x, 0, z).nor();
            mesh.addVertex(x, 0, z, normal.x, 0, normal.z, (float)i/segments, 0, iceColor.r, iceColor.g, iceColor.b, iceColor.a);
        }

        // Top vertices
        for (int i = 0; i < segments; i++) {
            float angle = (float)i / segments * MathUtils.PI2;
            float x = MathUtils.cos(angle) * topRadius;
            float z = MathUtils.sin(angle) * topRadius;

            Vector3 normal = new Vector3(x, 1, z).nor();
            mesh.addVertex(x, height, z, normal.x, normal.y, normal.z, (float)i/segments, 1, iceColor.r, iceColor.g, iceColor.b, iceColor.a);
        }

        // Build sides
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            short v0 = (short)i;
            short v1 = (short)(i + segments);
            short v2 = (short)(next + segments);
            short v3 = (short)next;

            mesh.addQuad(v0, v1, v2, v3);
        }

        Material material = new Material(
            ColorAttribute.createDiffuse(iceColor)
        );
        return mesh.buildModel(material, "ice_formation");
    }

    // ==================== Crystal Generation ====================

    private Model generateCrystal(Color crystalColor) {
        LGMesh mesh = new LGMesh();

        // Crystal is a simple elongated hexagonal prism
        int segments = 6;
        float baseRadius = 0.3f;
        float height = 1.2f;

        // Bottom point
        short bottomVertex = mesh.addVertex(0, 0, 0, 0, -1, 0, 0.5f, 0, crystalColor.r, crystalColor.g, crystalColor.b, crystalColor.a);

        // Middle ring
        short[] middleVerts = new short[segments];
        for (int i = 0; i < segments; i++) {
            float angle = (float)i / segments * MathUtils.PI2;
            float x = MathUtils.cos(angle) * baseRadius;
            float z = MathUtils.sin(angle) * baseRadius;
            float y = height * 0.3f;

            Vector3 normal = new Vector3(x, y * 0.5f, z).nor();
            middleVerts[i] = mesh.addVertex(x, y, z, normal.x, normal.y, normal.z, (float)i/segments, 0.5f, crystalColor.r, crystalColor.g, crystalColor.b, crystalColor.a);
        }

        // Top point
        short topVertex = mesh.addVertex(0, height, 0, 0, 1, 0, 0.5f, 1, crystalColor.r, crystalColor.g, crystalColor.b, crystalColor.a);

        // Build triangles
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;

            // Bottom
            mesh.addTriangle(bottomVertex, middleVerts[i], middleVerts[next]);

            // Top
            mesh.addTriangle(topVertex, middleVerts[next], middleVerts[i]);
        }

        Material material = new Material(
            ColorAttribute.createDiffuse(crystalColor)
        );
        return mesh.buildModel(material, "crystal");
    }

    // ==================== Helper Methods ====================

    private Color getRockColor() {
        switch (planetType) {
            case DESERT:
                return new Color(0.7f, 0.6f, 0.4f, 1f);
            case ICE:
                return new Color(0.7f, 0.75f, 0.8f, 1f);
            case LAVA:
                return new Color(0.3f, 0.25f, 0.2f, 1f);
            case FOREST:
                return new Color(0.5f, 0.5f, 0.5f, 1f);
            case OCEAN:
                return new Color(0.6f, 0.6f, 0.65f, 1f);
            default:
                return new Color(0.6f, 0.6f, 0.6f, 1f);
        }
    }

    public void setVegetationDensity(float density) {
        this.vegetationDensity = density;
    }

    public void setRockDensity(float density) {
        this.rockDensity = density;
    }

    @Override
    public void dispose() {
        if (rockModel1 != null) rockModel1.dispose();
        if (rockModel2 != null) rockModel2.dispose();
        if (rockModel3 != null) rockModel3.dispose();
        if (grassModel != null) grassModel.dispose();
        if (bushModel != null) bushModel.dispose();
        if (treeModel != null) treeModel.dispose();
        if (crystalModel != null) crystalModel.dispose();
        if (cactusModel != null) cactusModel.dispose();
        if (iceFormationModel != null) iceFormationModel.dispose();
        if (deadTreeModel != null) deadTreeModel.dispose();

        for (Texture texture : generatedTextures) {
            texture.dispose();
        }
        generatedTextures.clear();
    }
}
