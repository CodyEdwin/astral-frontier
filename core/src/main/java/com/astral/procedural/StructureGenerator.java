package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

/**
 * Generates structures for planetary surfaces: pyramids, ruins, temples, etc.
 */
public class StructureGenerator {

    private final long seed;
    private final Random random;
    private final ModelBuilder modelBuilder;

    public StructureGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.modelBuilder = new ModelBuilder();
    }

    /**
     * Structure placement data
     */
    public static class StructurePlacement {
        public Model model;
        public Vector3 position;
        public float rotation;
        public float scale;
        public String type;

        public StructurePlacement(Model model, Vector3 position, float rotation, float scale, String type) {
            this.model = model;
            this.position = position;
            this.rotation = rotation;
            this.scale = scale;
            this.type = type;
        }
    }

    /**
     * Generate structures for a desert planet
     */
    public Array<StructurePlacement> generateDesertStructures(float terrainWidth, float terrainDepth, 
                                                               java.util.function.BiFunction<Float, Float, Float> heightGetter) {
        Array<StructurePlacement> structures = new Array<>();

        // Main pyramid complex
        Vector3 pyramidPos = findFlatArea(terrainWidth, terrainDepth, heightGetter, 80f);
        if (pyramidPos != null) {
            structures.add(createPyramid(pyramidPos, 60f, 40f, "Great Pyramid"));
            
            // Smaller surrounding pyramids
            for (int i = 0; i < 3; i++) {
                float angle = i * 120f + random.nextFloat() * 30f;
                float dist = 80f + random.nextFloat() * 40f;
                Vector3 smallPos = new Vector3(
                    pyramidPos.x + MathUtils.cosDeg(angle) * dist,
                    0,
                    pyramidPos.z + MathUtils.sinDeg(angle) * dist
                );
                smallPos.y = heightGetter.apply(smallPos.x, smallPos.z);
                float size = 20f + random.nextFloat() * 15f;
                structures.add(createPyramid(smallPos, size, size * 0.7f, "Lesser Pyramid"));
            }
        }

        // Scattered ruins
        int ruinCount = 8 + random.nextInt(5);
        for (int i = 0; i < ruinCount; i++) {
            float x = (random.nextFloat() - 0.5f) * terrainWidth * 0.8f;
            float z = (random.nextFloat() - 0.5f) * terrainDepth * 0.8f;
            float y = heightGetter.apply(x, z);
            
            Vector3 ruinPos = new Vector3(x, y, z);
            structures.add(createRuins(ruinPos, 10f + random.nextFloat() * 15f));
        }

        // Obelisks
        int obeliskCount = 4 + random.nextInt(3);
        for (int i = 0; i < obeliskCount; i++) {
            float x = (random.nextFloat() - 0.5f) * terrainWidth * 0.7f;
            float z = (random.nextFloat() - 0.5f) * terrainDepth * 0.7f;
            float y = heightGetter.apply(x, z);
            
            Vector3 obeliskPos = new Vector3(x, y, z);
            structures.add(createObelisk(obeliskPos, 3f + random.nextFloat() * 2f, 15f + random.nextFloat() * 10f));
        }

        // Ancient temple
        Vector3 templePos = findFlatArea(terrainWidth * 0.6f, terrainDepth * 0.6f, heightGetter, 50f);
        if (templePos != null && templePos.dst(pyramidPos != null ? pyramidPos : Vector3.Zero) > 150f) {
            structures.add(createTemple(templePos, 40f, 25f));
        }

        // Sphinx-like structure
        Vector3 sphinxPos = findFlatArea(terrainWidth * 0.5f, terrainDepth * 0.5f, heightGetter, 30f);
        if (sphinxPos != null) {
            structures.add(createSphinx(sphinxPos, 35f, 15f));
        }

        return structures;
    }

    /**
     * Generate structures for a specific chunk (sparse distribution)
     */
    public Array<StructurePlacement> generateForChunk(int chunkX, int chunkZ, float chunkWorldSize,
                                                       PlanetType planetType,
                                                       java.util.function.BiFunction<Float, Float, Float> heightGetter) {
        Array<StructurePlacement> structures = new Array<>();

        // Use deterministic random based on chunk coordinates
        Random chunkRandom = new Random(seed ^ ((long) chunkX << 16) ^ chunkZ);

        // Only some chunks have structures (sparse distribution)
        float structureChance = switch (planetType) {
            case DESERT -> 0.15f;  // 15% of chunks have structures
            case ROCKY -> 0.08f;
            default -> 0.05f;
        };

        if (chunkRandom.nextFloat() > structureChance) {
            return structures; // No structures in this chunk
        }

        float offsetX = chunkX * chunkWorldSize;
        float offsetZ = chunkZ * chunkWorldSize;

        // Choose structure type based on planet
        if (planetType == PlanetType.DESERT) {
            float r = chunkRandom.nextFloat();
            float x = offsetX + chunkRandom.nextFloat() * chunkWorldSize * 0.6f + chunkWorldSize * 0.2f;
            float z = offsetZ + chunkRandom.nextFloat() * chunkWorldSize * 0.6f + chunkWorldSize * 0.2f;
            float y = heightGetter.apply(x, z);
            Vector3 pos = new Vector3(x, y, z);

            if (r < 0.2f) {
                // Small pyramid
                float size = 15f + chunkRandom.nextFloat() * 20f;
                structures.add(createPyramid(pos, size, size * 0.7f, "Lesser Pyramid"));
            } else if (r < 0.5f) {
                // Ruins
                structures.add(createRuins(pos, 15f + chunkRandom.nextFloat() * 10f));
            } else if (r < 0.7f) {
                // Obelisk
                structures.add(createObelisk(pos, 3f + chunkRandom.nextFloat() * 4f, 15f + chunkRandom.nextFloat() * 10f));
            } else {
                // Small temple
                structures.add(createTemple(pos, 20f + chunkRandom.nextFloat() * 15f, 10f + chunkRandom.nextFloat() * 8f));
            }
        }

        return structures;
    }

    private Vector3 findFlatArea(float width, float depth,
                                  java.util.function.BiFunction<Float, Float, Float> heightGetter,
                                  float requiredSize) {
        // Try random positions to find a relatively flat area
        for (int attempt = 0; attempt < 20; attempt++) {
            float x = (random.nextFloat() - 0.5f) * width * 0.6f;
            float z = (random.nextFloat() - 0.5f) * depth * 0.6f;
            
            float centerHeight = heightGetter.apply(x, z);
            
            // Check surrounding heights
            float maxVariation = 0f;
            for (int i = 0; i < 8; i++) {
                float angle = i * 45f;
                float checkX = x + MathUtils.cosDeg(angle) * requiredSize * 0.5f;
                float checkZ = z + MathUtils.sinDeg(angle) * requiredSize * 0.5f;
                float checkHeight = heightGetter.apply(checkX, checkZ);
                maxVariation = Math.max(maxVariation, Math.abs(checkHeight - centerHeight));
            }
            
            if (maxVariation < 5f) {
                return new Vector3(x, centerHeight, z);
            }
        }
        
        // Fallback to random position
        float x = (random.nextFloat() - 0.5f) * width * 0.4f;
        float z = (random.nextFloat() - 0.5f) * depth * 0.4f;
        return new Vector3(x, heightGetter.apply(x, z), z);
    }

    public StructurePlacement createPyramid(Vector3 position, float baseSize, float height, String name) {
        modelBuilder.begin();
        
        Material stoneMaterial = new Material(
            ColorAttribute.createDiffuse(new Color(0.85f, 0.75f, 0.55f, 1f))
        );
        
        MeshPartBuilder builder = modelBuilder.part("pyramid", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            stoneMaterial);
        
        float half = baseSize / 2f;
        Vector3 apex = new Vector3(0, height, 0);
        Vector3 v0 = new Vector3(-half, 0, -half);
        Vector3 v1 = new Vector3(half, 0, -half);
        Vector3 v2 = new Vector3(half, 0, half);
        Vector3 v3 = new Vector3(-half, 0, half);
        
        // Four triangular faces
        buildTriangle(builder, v0, v1, apex);
        buildTriangle(builder, v1, v2, apex);
        buildTriangle(builder, v2, v3, apex);
        buildTriangle(builder, v3, v0, apex);
        
        // Base
        buildTriangle(builder, v0, v3, v2);
        buildTriangle(builder, v0, v2, v1);
        
        // Add weathered details - stepped sides
        Material weatheredMaterial = new Material(
            ColorAttribute.createDiffuse(new Color(0.75f, 0.65f, 0.45f, 1f))
        );
        
        MeshPartBuilder detailBuilder = modelBuilder.part("details", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            weatheredMaterial);
        
        // Add entrance
        float entranceWidth = baseSize * 0.15f;
        float entranceHeight = height * 0.2f;
        float entranceDepth = baseSize * 0.1f;
        addBox(detailBuilder, 0, entranceHeight / 2f, -half + entranceDepth / 2f, 
               entranceWidth, entranceHeight, entranceDepth);
        
        Model model = modelBuilder.end();
        
        return new StructurePlacement(model, position, random.nextFloat() * 360f, 1f, name);
    }

    public StructurePlacement createRuins(Vector3 position, float size) {
        modelBuilder.begin();
        
        Material ruinMaterial = new Material(
            ColorAttribute.createDiffuse(new Color(0.7f, 0.6f, 0.45f, 1f))
        );
        
        MeshPartBuilder builder = modelBuilder.part("ruins", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            ruinMaterial);
        
        // Scattered broken columns
        int columnCount = 3 + random.nextInt(5);
        for (int i = 0; i < columnCount; i++) {
            float x = (random.nextFloat() - 0.5f) * size;
            float z = (random.nextFloat() - 0.5f) * size;
            float height = 2f + random.nextFloat() * 8f;
            float radius = 0.8f + random.nextFloat() * 0.5f;
            
            // Column (simplified as octagonal prism)
            addColumn(builder, x, 0, z, radius, height, random.nextFloat() < 0.3f);
        }
        
        // Broken walls
        int wallCount = 2 + random.nextInt(3);
        for (int i = 0; i < wallCount; i++) {
            float x = (random.nextFloat() - 0.5f) * size;
            float z = (random.nextFloat() - 0.5f) * size;
            float length = 3f + random.nextFloat() * 6f;
            float height = 1f + random.nextFloat() * 4f;
            float rotation = random.nextFloat() * 180f;
            
            addWall(builder, x, 0, z, length, height, 0.5f, rotation);
        }
        
        // Scattered stone blocks
        int blockCount = 5 + random.nextInt(8);
        for (int i = 0; i < blockCount; i++) {
            float x = (random.nextFloat() - 0.5f) * size * 1.2f;
            float z = (random.nextFloat() - 0.5f) * size * 1.2f;
            float blockSize = 0.5f + random.nextFloat() * 1.5f;
            
            addBox(builder, x, blockSize / 2f, z, blockSize, blockSize * 0.6f, blockSize * 0.8f);
        }
        
        Model model = modelBuilder.end();
        
        return new StructurePlacement(model, position, random.nextFloat() * 360f, 1f, "Ancient Ruins");
    }

    public StructurePlacement createObelisk(Vector3 position, float baseSize, float height) {
        modelBuilder.begin();
        
        Material obeliskMaterial = new Material(
            ColorAttribute.createDiffuse(new Color(0.3f, 0.25f, 0.2f, 1f))
        );
        
        MeshPartBuilder builder = modelBuilder.part("obelisk", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            obeliskMaterial);
        
        // Tapered shaft
        float topSize = baseSize * 0.6f;
        float pyramidHeight = height * 0.15f;
        float shaftHeight = height - pyramidHeight;
        
        // Main shaft (tapered box)
        addTaperedBox(builder, 0, shaftHeight / 2f, 0, baseSize, topSize, shaftHeight);
        
        // Pyramidion (top cap)
        float half = topSize / 2f;
        Vector3 apex = new Vector3(0, shaftHeight + pyramidHeight, 0);
        Vector3 v0 = new Vector3(-half, shaftHeight, -half);
        Vector3 v1 = new Vector3(half, shaftHeight, -half);
        Vector3 v2 = new Vector3(half, shaftHeight, half);
        Vector3 v3 = new Vector3(-half, shaftHeight, half);
        
        buildTriangle(builder, v0, v1, apex);
        buildTriangle(builder, v1, v2, apex);
        buildTriangle(builder, v2, v3, apex);
        buildTriangle(builder, v3, v0, apex);
        
        Model model = modelBuilder.end();
        
        return new StructurePlacement(model, position, random.nextFloat() * 360f, 1f, "Obelisk");
    }

    public StructurePlacement createTemple(Vector3 position, float width, float height) {
        modelBuilder.begin();
        
        Material templeMaterial = new Material(
            ColorAttribute.createDiffuse(new Color(0.8f, 0.7f, 0.5f, 1f))
        );
        
        MeshPartBuilder builder = modelBuilder.part("temple", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            templeMaterial);
        
        float depth = width * 0.8f;
        
        // Platform base
        float platformHeight = 3f;
        addBox(builder, 0, platformHeight / 2f, 0, width * 1.2f, platformHeight, depth * 1.2f);
        
        // Steps
        for (int i = 0; i < 5; i++) {
            float stepY = i * 0.6f;
            float stepWidth = width * 0.4f;
            float stepDepth = 1.5f;
            addBox(builder, 0, stepY + 0.3f, -depth * 0.6f - i * stepDepth / 2f, 
                   stepWidth, 0.6f, stepDepth);
        }
        
        // Columns
        int columnRows = 2;
        int columnsPerRow = 6;
        float columnSpacing = width / (columnsPerRow + 1);
        float columnRadius = 1.2f;
        float columnHeight = height - platformHeight - 4f;
        
        for (int row = 0; row < columnRows; row++) {
            float z = (row - 0.5f) * (depth * 0.6f);
            for (int col = 0; col < columnsPerRow; col++) {
                float x = (col - (columnsPerRow - 1) / 2f) * columnSpacing;
                addColumn(builder, x, platformHeight, z, columnRadius, columnHeight, false);
            }
        }
        
        // Roof
        float roofHeight = 4f;
        addBox(builder, 0, platformHeight + columnHeight + roofHeight / 2f, 0, 
               width * 1.1f, roofHeight, depth * 0.9f);
        
        Model model = modelBuilder.end();
        
        return new StructurePlacement(model, position, random.nextFloat() * 360f, 1f, "Ancient Temple");
    }

    public StructurePlacement createSphinx(Vector3 position, float length, float height) {
        modelBuilder.begin();
        
        Material sphinxMaterial = new Material(
            ColorAttribute.createDiffuse(new Color(0.85f, 0.75f, 0.55f, 1f))
        );
        
        MeshPartBuilder builder = modelBuilder.part("sphinx", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            sphinxMaterial);
        
        // Body (elongated box)
        float bodyHeight = height * 0.5f;
        float bodyWidth = length * 0.3f;
        addBox(builder, 0, bodyHeight / 2f, 0, bodyWidth, bodyHeight, length * 0.7f);
        
        // Head (raised)
        float headSize = bodyWidth * 0.8f;
        float headHeight = height * 0.4f;
        addBox(builder, 0, bodyHeight + headHeight / 2f, length * 0.3f, 
               headSize, headHeight, headSize);
        
        // Front paws
        float pawLength = length * 0.4f;
        float pawWidth = bodyWidth * 0.25f;
        float pawHeight = bodyHeight * 0.3f;
        addBox(builder, bodyWidth * 0.3f, pawHeight / 2f, length * 0.35f + pawLength / 2f, 
               pawWidth, pawHeight, pawLength);
        addBox(builder, -bodyWidth * 0.3f, pawHeight / 2f, length * 0.35f + pawLength / 2f, 
               pawWidth, pawHeight, pawLength);
        
        // Headdress
        float headdressWidth = headSize * 1.3f;
        addBox(builder, 0, bodyHeight + headHeight * 0.7f, length * 0.3f - headSize * 0.3f, 
               headdressWidth, headHeight * 0.6f, headSize * 0.3f);
        
        Model model = modelBuilder.end();
        
        return new StructurePlacement(model, position, random.nextFloat() * 30f - 15f, 1f, "Ancient Sphinx");
    }

    // Helper methods for building geometry
    private void buildTriangle(MeshPartBuilder builder, Vector3 v0, Vector3 v1, Vector3 v2) {
        Vector3 normal = new Vector3(v1).sub(v0).crs(new Vector3(v2).sub(v0)).nor();
        builder.triangle(
            new MeshPartBuilder.VertexInfo().setPos(v0).setNor(normal),
            new MeshPartBuilder.VertexInfo().setPos(v1).setNor(normal),
            new MeshPartBuilder.VertexInfo().setPos(v2).setNor(normal)
        );
    }

    private void addBox(MeshPartBuilder builder, float x, float y, float z, 
                        float width, float height, float depth) {
        builder.box(x, y, z, width, height, depth);
    }

    private void addTaperedBox(MeshPartBuilder builder, float x, float y, float z,
                                float bottomSize, float topSize, float height) {
        float hb = bottomSize / 2f;
        float ht = topSize / 2f;
        float hh = height / 2f;
        
        // Bottom
        Vector3 b0 = new Vector3(x - hb, y - hh, z - hb);
        Vector3 b1 = new Vector3(x + hb, y - hh, z - hb);
        Vector3 b2 = new Vector3(x + hb, y - hh, z + hb);
        Vector3 b3 = new Vector3(x - hb, y - hh, z + hb);
        
        // Top
        Vector3 t0 = new Vector3(x - ht, y + hh, z - ht);
        Vector3 t1 = new Vector3(x + ht, y + hh, z - ht);
        Vector3 t2 = new Vector3(x + ht, y + hh, z + ht);
        Vector3 t3 = new Vector3(x - ht, y + hh, z + ht);
        
        // Sides
        buildQuad(builder, b0, b1, t1, t0); // Front
        buildQuad(builder, b1, b2, t2, t1); // Right
        buildQuad(builder, b2, b3, t3, t2); // Back
        buildQuad(builder, b3, b0, t0, t3); // Left
        
        // Top face
        buildQuad(builder, t0, t1, t2, t3);
        // Bottom face
        buildQuad(builder, b3, b2, b1, b0);
    }

    private void buildQuad(MeshPartBuilder builder, Vector3 v0, Vector3 v1, Vector3 v2, Vector3 v3) {
        buildTriangle(builder, v0, v1, v2);
        buildTriangle(builder, v0, v2, v3);
    }

    private void addColumn(MeshPartBuilder builder, float x, float y, float z, 
                           float radius, float height, boolean broken) {
        float actualHeight = broken ? height * (0.3f + random.nextFloat() * 0.4f) : height;
        
        // Simplified column as octagonal prism
        int segments = 8;
        Vector3[] bottom = new Vector3[segments];
        Vector3[] top = new Vector3[segments];
        
        for (int i = 0; i < segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float cos = (float) Math.cos(angle) * radius;
            float sin = (float) Math.sin(angle) * radius;
            bottom[i] = new Vector3(x + cos, y, z + sin);
            top[i] = new Vector3(x + cos, y + actualHeight, z + sin);
        }
        
        // Build sides
        for (int i = 0; i < segments; i++) {
            int next = (i + 1) % segments;
            buildQuad(builder, bottom[i], bottom[next], top[next], top[i]);
        }
        
        // Top cap (if not broken)
        if (!broken) {
            for (int i = 1; i < segments - 1; i++) {
                buildTriangle(builder, top[0], top[i], top[i + 1]);
            }
        }
    }

    private void addWall(MeshPartBuilder builder, float x, float y, float z,
                         float length, float height, float thickness, float rotationDeg) {
        float cos = MathUtils.cosDeg(rotationDeg);
        float sin = MathUtils.sinDeg(rotationDeg);
        
        float hl = length / 2f;
        float ht = thickness / 2f;
        
        // Rotated corners
        Vector3[] bottom = new Vector3[4];
        Vector3[] top = new Vector3[4];
        
        float[][] offsets = {{-hl, -ht}, {hl, -ht}, {hl, ht}, {-hl, ht}};
        
        for (int i = 0; i < 4; i++) {
            float ox = offsets[i][0] * cos - offsets[i][1] * sin;
            float oz = offsets[i][0] * sin + offsets[i][1] * cos;
            bottom[i] = new Vector3(x + ox, y, z + oz);
            top[i] = new Vector3(x + ox, y + height, z + oz);
        }
        
        // Build sides
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            buildQuad(builder, bottom[i], bottom[next], top[next], top[i]);
        }
        
        // Top
        buildQuad(builder, top[0], top[1], top[2], top[3]);
    }

    /**
     * Create a ModelInstance from a StructurePlacement
     */
    public ModelInstance createInstance(StructurePlacement placement) {
        if (placement == null || placement.model == null) return null;

        ModelInstance instance = new ModelInstance(placement.model);
        instance.transform.setToTranslation(placement.position);
        instance.transform.rotate(Vector3.Y, placement.rotation);
        instance.transform.scale(placement.scale, placement.scale, placement.scale);
        return instance;
    }

    public void dispose() {
        // Models are disposed by the caller
    }
}
