package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * A single terrain chunk with heightmap-based mesh generation and textures
 */
public class TerrainChunk implements Disposable {

    public final int chunkX, chunkZ;
    public final int size;
    public final float cellSize;

    private float[][] heightmap;
    private Mesh mesh;
    private Model model;
    private ModelInstance modelInstance;
    private boolean meshBuilt = false;

    // Chunk state
    public float priority = 0f;
    public boolean isLoading = false;
    public boolean isVisible = true;

    // Planet type for coloring/texturing
    private PlanetType planetType;

    // Shared textures per planet type (static to avoid regenerating per chunk)
    private static Texture desertTexture;
    private static Texture desertRockTexture;
    private static Texture iceTexture;
    private static Texture lavaTexture;
    private static Texture forestTexture;
    private static Texture rockyTexture;
    private static Texture oceanTexture;
    private static boolean texturesInitialized = false;

    // Desert features for this chunk
    private Array<ModelInstance> featureInstances;
    private static DesertFeatureGenerator desertFeatureGenerator;

    public TerrainChunk(int chunkX, int chunkZ, int size, float cellSize, PlanetType planetType) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.size = size;
        this.cellSize = cellSize;
        this.planetType = planetType;
        this.featureInstances = new Array<>();
    }

    /**
     * Initialize shared textures (call once on GL thread)
     */
    public static void initializeTextures(long seed) {
        if (texturesInitialized) return;

        desertTexture = DesertTextures.sand(256, 256, seed);
        desertRockTexture = DesertTextures.desertRock(256, 256, seed + 1000);
        iceTexture = ProceduralTexture.rock(256, 256, new Color(0.85f, 0.92f, 1f, 1f), seed);
        lavaTexture = ProceduralTexture.rock(256, 256, new Color(0.3f, 0.1f, 0.05f, 1f), seed);
        forestTexture = ProceduralTexture.organicSkin(256, 256, new Color(0.3f, 0.5f, 0.2f, 1f), seed);
        rockyTexture = ProceduralTexture.rock(256, 256, new Color(0.5f, 0.45f, 0.4f, 1f), seed);
        oceanTexture = ProceduralTexture.rock(256, 256, new Color(0.9f, 0.85f, 0.7f, 1f), seed);

        desertFeatureGenerator = new DesertFeatureGenerator(seed);
        texturesInitialized = true;
    }

    /**
     * Dispose shared textures (call on game shutdown)
     */
    public static void disposeSharedTextures() {
        if (desertTexture != null) { desertTexture.dispose(); desertTexture = null; }
        if (desertRockTexture != null) { desertRockTexture.dispose(); desertRockTexture = null; }
        if (iceTexture != null) { iceTexture.dispose(); iceTexture = null; }
        if (lavaTexture != null) { lavaTexture.dispose(); lavaTexture = null; }
        if (forestTexture != null) { forestTexture.dispose(); forestTexture = null; }
        if (rockyTexture != null) { rockyTexture.dispose(); rockyTexture = null; }
        if (oceanTexture != null) { oceanTexture.dispose(); oceanTexture = null; }
        if (desertFeatureGenerator != null) { desertFeatureGenerator.dispose(); desertFeatureGenerator = null; }
        texturesInitialized = false;
    }

    /**
     * Set the heightmap data for this chunk
     */
    public void setHeightmap(float[][] heightmap) {
        this.heightmap = heightmap;
    }

    /**
     * Build the mesh from heightmap data - must be called on GL thread
     */
    public void buildMesh() {
        if (heightmap == null || meshBuilt) return;

        // Ensure textures are initialized
        if (!texturesInitialized) {
            initializeTextures(chunkX * 12345L + chunkZ * 67890L);
        }

        int resolution = heightmap.length;
        int vertexCount = resolution * resolution;
        int indexCount = (resolution - 1) * (resolution - 1) * 6;

        // Vertex format: position(3) + normal(3) + texcoord(2) + color(4)
        int vertexSize = 12;
        float[] vertices = new float[vertexCount * vertexSize];
        short[] indices = new short[indexCount];

        // World offset for this chunk
        float chunkWorldSize = (size - 1) * cellSize;
        float offsetX = chunkX * chunkWorldSize;
        float offsetZ = chunkZ * chunkWorldSize;

        // Texture tiling
        float textureTiling = 8f;

        // Build vertices
        int vIdx = 0;
        for (int z = 0; z < resolution; z++) {
            for (int x = 0; x < resolution; x++) {
                float worldX = offsetX + x * cellSize;
                float worldZ = offsetZ + z * cellSize;
                float height = heightmap[z][x];

                // Position
                vertices[vIdx++] = worldX;
                vertices[vIdx++] = height;
                vertices[vIdx++] = worldZ;

                // Normal (calculate from neighbors)
                float hL = x > 0 ? heightmap[z][x - 1] : height;
                float hR = x < resolution - 1 ? heightmap[z][x + 1] : height;
                float hD = z > 0 ? heightmap[z - 1][x] : height;
                float hU = z < resolution - 1 ? heightmap[z + 1][x] : height;

                float nx = (hL - hR) * 0.5f;
                float ny = cellSize;
                float nz = (hD - hU) * 0.5f;
                float nLen = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                vertices[vIdx++] = nx / nLen;
                vertices[vIdx++] = ny / nLen;
                vertices[vIdx++] = nz / nLen;

                // Texture coordinates (world-space for seamless tiling)
                float u = worldX * textureTiling / chunkWorldSize;
                float v = worldZ * textureTiling / chunkWorldSize;
                vertices[vIdx++] = u;
                vertices[vIdx++] = v;

                // Color (height-based tint for variation)
                Color color = getColorForHeight(height);
                vertices[vIdx++] = color.r;
                vertices[vIdx++] = color.g;
                vertices[vIdx++] = color.b;
                vertices[vIdx++] = color.a;
            }
        }

        // Build indices
        int iIdx = 0;
        for (int z = 0; z < resolution - 1; z++) {
            for (int x = 0; x < resolution - 1; x++) {
                int topLeft = z * resolution + x;
                int topRight = topLeft + 1;
                int bottomLeft = (z + 1) * resolution + x;
                int bottomRight = bottomLeft + 1;

                indices[iIdx++] = (short) topLeft;
                indices[iIdx++] = (short) bottomLeft;
                indices[iIdx++] = (short) topRight;

                indices[iIdx++] = (short) topRight;
                indices[iIdx++] = (short) bottomLeft;
                indices[iIdx++] = (short) bottomRight;
            }
        }

        // Create mesh with texture coordinates
        mesh = new Mesh(true, vertexCount, indexCount,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
            new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_color")
        );
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        // Create textured material
        Material material = createTerrainMaterial();

        model = new Model();
        MeshPart meshPart = new MeshPart();
        meshPart.id = "terrain_" + chunkX + "_" + chunkZ;
        meshPart.mesh = mesh;
        meshPart.offset = 0;
        meshPart.size = indexCount;
        meshPart.primitiveType = GL20.GL_TRIANGLES;

        Node node = new Node();
        node.id = "node_" + chunkX + "_" + chunkZ;
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

        // Generate desert features for this chunk
        if (planetType == PlanetType.DESERT) {
            generateDesertFeatures();
        }

        meshBuilt = true;
    }

    private Material createTerrainMaterial() {
        Texture texture = switch (planetType) {
            case DESERT -> desertTexture;
            case ICE -> iceTexture;
            case LAVA -> lavaTexture;
            case FOREST -> forestTexture;
            case ROCKY -> rockyTexture;
            case OCEAN -> oceanTexture;
            default -> desertTexture;
        };

        // Get base color for this planet type for ambient/emissive lighting
        Color baseColor = switch (planetType) {
            case DESERT -> new Color(0.85f, 0.72f, 0.5f, 1f);
            case ICE -> new Color(0.85f, 0.92f, 1f, 1f);
            case LAVA -> new Color(0.4f, 0.15f, 0.1f, 1f);
            case FOREST -> new Color(0.25f, 0.45f, 0.2f, 1f);
            case ROCKY -> new Color(0.5f, 0.45f, 0.4f, 1f);
            case OCEAN -> new Color(0.3f, 0.5f, 0.6f, 1f);
            default -> new Color(0.7f, 0.6f, 0.5f, 1f);
        };

        if (texture != null) {
            // Use texture with emissive for baseline visibility + diffuse for lighting
            Color emissive = new Color(baseColor).mul(0.5f); // 50% self-illumination
            return new Material(
                TextureAttribute.createDiffuse(texture),
                ColorAttribute.createDiffuse(baseColor),
                ColorAttribute.createEmissive(emissive)
            );
        } else {
            // No texture - use solid color with emissive
            Color emissive = new Color(baseColor).mul(0.5f);
            return new Material(
                ColorAttribute.createDiffuse(baseColor),
                ColorAttribute.createEmissive(emissive)
            );
        }
    }

    private void generateDesertFeatures() {
        if (desertFeatureGenerator == null || heightmap == null) return;

        java.util.Random random = new java.util.Random(chunkX * 73856093L ^ chunkZ * 19349663L);

        float chunkWorldSize = (size - 1) * cellSize;
        float worldOffsetX = chunkX * chunkWorldSize;
        float worldOffsetZ = chunkZ * chunkWorldSize;

        // Number of features per chunk
        int numFeatures = 2 + random.nextInt(4);

        for (int i = 0; i < numFeatures; i++) {
            // Random position within chunk
            float localX = random.nextFloat() * chunkWorldSize;
            float localZ = random.nextFloat() * chunkWorldSize;
            float height = getHeightAt(localX, localZ);

            float worldX = worldOffsetX + localX;
            float worldZ = worldOffsetZ + localZ;

            // Pick random feature type
            DesertFeatureGenerator.DesertFeature[] features = DesertFeatureGenerator.DesertFeature.values();
            DesertFeatureGenerator.DesertFeature feature = features[random.nextInt(features.length)];

            // Scale based on feature type
            float scale = switch (feature) {
                case ROCK_SPIRE, ROCK_ARCH -> 0.8f + random.nextFloat() * 0.6f;
                case BOULDER_CLUSTER -> 0.5f + random.nextFloat() * 0.5f;
                case CACTUS_TALL -> 0.6f + random.nextFloat() * 0.4f;
                case CACTUS_ROUND -> 0.4f + random.nextFloat() * 0.3f;
                case DEAD_TREE -> 0.5f + random.nextFloat() * 0.4f;
                case SAND_DUNE -> 1f + random.nextFloat() * 0.5f;
                case OASIS_PALM -> 0.7f + random.nextFloat() * 0.3f;
                case SKULL -> 0.3f + random.nextFloat() * 0.2f;
                case ANCIENT_PILLAR -> 0.6f + random.nextFloat() * 0.4f;
            };

            long featureSeed = random.nextLong();
            Model featureModel = desertFeatureGenerator.generate(feature, scale, featureSeed);
            ModelInstance instance = new ModelInstance(featureModel);

            // Position the feature on the terrain
            instance.transform.setToTranslation(worldX, height, worldZ);

            // Random Y rotation
            instance.transform.rotate(Vector3.Y, random.nextFloat() * 360f);

            featureInstances.add(instance);
        }
    }

    private Color getColorForHeight(float height) {
        float normalizedHeight = (height + 20f) / 40f;
        normalizedHeight = Math.max(0, Math.min(1, normalizedHeight));

        return switch (planetType) {
            case DESERT -> getDesertColor(normalizedHeight);
            case ICE -> getIceColor(normalizedHeight);
            case LAVA -> getLavaColor(normalizedHeight);
            case FOREST -> getForestColor(normalizedHeight);
            case ROCKY -> getRockyColor(normalizedHeight);
            case OCEAN -> getOceanColor(normalizedHeight);
            default -> new Color(0.5f, 0.5f, 0.5f, 1f);
        };
    }

    private Color getDesertColor(float h) {
        // Bright colors to ensure visibility - these multiply with texture
        if (h < 0.25f) return new Color(0.9f, 0.8f, 0.65f, 1f);
        else if (h < 0.45f) return new Color(0.95f, 0.85f, 0.7f, 1f);
        else if (h < 0.55f) return new Color(1f, 0.9f, 0.75f, 1f);
        else if (h < 0.75f) return new Color(1f, 0.95f, 0.8f, 1f);
        else return new Color(0.95f, 0.85f, 0.7f, 1f);
    }

    private Color getIceColor(float h) {
        if (h < 0.3f) return new Color(0.7f, 0.8f, 0.95f, 1f);
        else if (h < 0.6f) return new Color(0.9f, 0.95f, 1f, 1f);
        else return new Color(1f, 1f, 1f, 1f);
    }

    private Color getLavaColor(float h) {
        if (h < 0.2f) return new Color(1f, 0.4f, 0.1f, 1f);
        else if (h < 0.4f) return new Color(0.5f, 0.2f, 0.1f, 1f);
        else return new Color(0.25f, 0.12f, 0.08f, 1f);
    }

    private Color getForestColor(float h) {
        if (h < 0.3f) return new Color(0.2f, 0.45f, 0.2f, 1f);
        else if (h < 0.6f) return new Color(0.3f, 0.6f, 0.25f, 1f);
        else return new Color(0.5f, 0.45f, 0.35f, 1f);
    }

    private Color getRockyColor(float h) {
        if (h < 0.3f) return new Color(0.45f, 0.4f, 0.38f, 1f);
        else if (h < 0.6f) return new Color(0.6f, 0.55f, 0.5f, 1f);
        else return new Color(0.75f, 0.7f, 0.65f, 1f);
    }

    private Color getOceanColor(float h) {
        if (h < 0.3f) return new Color(0.15f, 0.4f, 0.6f, 1f);
        else if (h < 0.5f) return new Color(0.3f, 0.6f, 0.8f, 1f);
        else return new Color(0.95f, 0.9f, 0.8f, 1f);
    }

    public float getHeightAt(float localX, float localZ) {
        if (heightmap == null) return 0;

        float fx = localX / cellSize;
        float fz = localZ / cellSize;

        int x0 = (int) Math.floor(fx);
        int z0 = (int) Math.floor(fz);

        x0 = Math.max(0, Math.min(heightmap.length - 2, x0));
        z0 = Math.max(0, Math.min(heightmap.length - 2, z0));

        float xFrac = fx - x0;
        float zFrac = fz - z0;

        float h00 = heightmap[z0][x0];
        float h10 = heightmap[z0][x0 + 1];
        float h01 = heightmap[z0 + 1][x0];
        float h11 = heightmap[z0 + 1][x0 + 1];

        float h0 = h00 * (1 - xFrac) + h10 * xFrac;
        float h1 = h01 * (1 - xFrac) + h11 * xFrac;

        return h0 * (1 - zFrac) + h1 * zFrac;
    }

    public ModelInstance getModelInstance() {
        return modelInstance;
    }

    public Array<ModelInstance> getFeatureInstances() {
        return featureInstances;
    }

    public boolean isMeshBuilt() {
        return meshBuilt;
    }

    public float getWorldX() {
        return chunkX * (size - 1) * cellSize;
    }

    public float getWorldZ() {
        return chunkZ * (size - 1) * cellSize;
    }

    @Override
    public void dispose() {
        if (model != null) {
            model.dispose();
            model = null;
        }
        mesh = null;
        modelInstance = null;
        heightmap = null;
        featureInstances.clear();
        meshBuilt = false;
    }
}
