package com.astral.procedural;

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
 * A single terrain chunk with heightmap-based mesh generation
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

    // Planet type for coloring
    private PlanetType planetType;

    public TerrainChunk(int chunkX, int chunkZ, int size, float cellSize, PlanetType planetType) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.size = size;
        this.cellSize = cellSize;
        this.planetType = planetType;
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

        int resolution = heightmap.length;
        int vertexCount = resolution * resolution;
        int indexCount = (resolution - 1) * (resolution - 1) * 6;

        // Vertex format: position(3) + normal(3) + color(4)
        int vertexSize = 10;
        float[] vertices = new float[vertexCount * vertexSize];
        short[] indices = new short[indexCount];

        // World offset for this chunk - use (resolution-1) cells per chunk for seamless tiling
        float chunkWorldSize = (size - 1) * cellSize;
        float offsetX = chunkX * chunkWorldSize;
        float offsetZ = chunkZ * chunkWorldSize;

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

                // Color based on height and planet type
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

                // Triangle 1
                indices[iIdx++] = (short) topLeft;
                indices[iIdx++] = (short) bottomLeft;
                indices[iIdx++] = (short) topRight;

                // Triangle 2
                indices[iIdx++] = (short) topRight;
                indices[iIdx++] = (short) bottomLeft;
                indices[iIdx++] = (short) bottomRight;
            }
        }

        // Create mesh
        mesh = new Mesh(true, vertexCount, indexCount,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"),
            new VertexAttribute(VertexAttributes.Usage.ColorUnpacked, 4, "a_color")
        );
        mesh.setVertices(vertices);
        mesh.setIndices(indices);

        // Create model
        Material material = new Material(ColorAttribute.createDiffuse(Color.WHITE));

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
        meshBuilt = true;
    }

    private Color getColorForHeight(float height) {
        // Normalize height based on terrain generator output
        // With plateau effect, heights are mostly flat with range around -15 to +15 for desert
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
        // Smooth gradient for realistic desert terrain
        if (h < 0.25f) return new Color(0.65f, 0.5f, 0.35f, 1f);     // Low dark sand
        else if (h < 0.45f) return new Color(0.78f, 0.65f, 0.45f, 1f); // Sand
        else if (h < 0.55f) return new Color(0.85f, 0.72f, 0.5f, 1f);  // Mid sand (most common)
        else if (h < 0.75f) return new Color(0.9f, 0.78f, 0.55f, 1f);  // Light sand
        else return new Color(0.7f, 0.55f, 0.4f, 1f);                  // High rocky areas
    }

    private Color getIceColor(float h) {
        if (h < 0.3f) return new Color(0.6f, 0.7f, 0.85f, 1f);      // Deep ice
        else if (h < 0.6f) return new Color(0.85f, 0.92f, 1f, 1f);  // Ice
        else return new Color(1f, 1f, 1f, 1f);                      // Snow
    }

    private Color getLavaColor(float h) {
        if (h < 0.2f) return new Color(1f, 0.3f, 0f, 1f);           // Lava
        else if (h < 0.4f) return new Color(0.4f, 0.15f, 0.05f, 1f); // Cooled rock
        else return new Color(0.2f, 0.1f, 0.05f, 1f);               // Obsidian
    }

    private Color getForestColor(float h) {
        if (h < 0.3f) return new Color(0.15f, 0.35f, 0.15f, 1f);    // Dark grass
        else if (h < 0.6f) return new Color(0.2f, 0.5f, 0.2f, 1f);  // Grass
        else return new Color(0.4f, 0.35f, 0.25f, 1f);              // Dirt/rock
    }

    private Color getRockyColor(float h) {
        if (h < 0.3f) return new Color(0.35f, 0.3f, 0.28f, 1f);     // Dark rock
        else if (h < 0.6f) return new Color(0.5f, 0.45f, 0.4f, 1f); // Rock
        else return new Color(0.65f, 0.6f, 0.55f, 1f);              // Light rock
    }

    private Color getOceanColor(float h) {
        if (h < 0.3f) return new Color(0.1f, 0.3f, 0.5f, 1f);       // Deep water
        else if (h < 0.5f) return new Color(0.2f, 0.5f, 0.7f, 1f);  // Shallow water
        else return new Color(0.9f, 0.85f, 0.7f, 1f);               // Beach
    }

    /**
     * Get height at local coordinates within this chunk
     */
    public float getHeightAt(float localX, float localZ) {
        if (heightmap == null) return 0;

        // Convert to heightmap indices
        float fx = localX / cellSize;
        float fz = localZ / cellSize;

        int x0 = (int) Math.floor(fx);
        int z0 = (int) Math.floor(fz);

        // Clamp to valid range
        x0 = Math.max(0, Math.min(heightmap.length - 2, x0));
        z0 = Math.max(0, Math.min(heightmap.length - 2, z0));

        // Bilinear interpolation
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
        meshBuilt = false;
    }
}
