package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Random;

/**
 * Generates procedural buildings and structures for planetary surfaces
 */
public class BuildingGenerator implements Disposable {

    private final long seed;
    private final Random random;
    private final ModelBuilder modelBuilder;
    private final Array<Texture> generatedTextures = new Array<>();
    private final Array<Model> generatedModels = new Array<>();

    public enum BuildingType {
        TOWER,          // Tall vertical structure
        DOME,           // Rounded dome building
        BUNKER,         // Low fortified structure
        HABITAT,        // Living quarters
        FACTORY,        // Industrial building
        LANDING_PAD,    // Ship landing platform
        ANTENNA,        // Communication tower
        WAREHOUSE,      // Storage building
        MONUMENT,       // Decorative structure
        OUTPOST         // Small frontier building
    }

    public enum ArchitectureStyle {
        HUMAN,          // Earth-like industrial
        ALIEN_ORGANIC,  // Grown/organic structures
        ALIEN_CRYSTAL,  // Crystalline geometric
        ANCIENT,        // Mysterious ruins-like
        MILITARY        // Fortified bunker style
    }

    public BuildingGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.modelBuilder = new ModelBuilder();
    }

    /**
     * Generate a building for a specific planet type
     */
    public Model generateBuilding(PlanetType planetType, long buildingSeed) {
        random.setSeed(buildingSeed);

        BuildingType type = BuildingType.values()[random.nextInt(BuildingType.values().length)];
        ArchitectureStyle style = getStyleForPlanet(planetType);
        float scale = 1f + random.nextFloat() * 2f;

        return generateBuilding(type, style, scale, buildingSeed);
    }

    /**
     * Generate a specific building type
     */
    public Model generateBuilding(BuildingType type, ArchitectureStyle style, float scale, long buildingSeed) {
        random.setSeed(buildingSeed);

        Color primaryColor = getPrimaryColor(style);
        Color accentColor = getAccentColor(style);

        Texture wallTexture = style == ArchitectureStyle.ALIEN_ORGANIC ?
            ProceduralTexture.organicSkin(64, 64, primaryColor, buildingSeed) :
            ProceduralTexture.metalHull(64, 64, primaryColor, buildingSeed);
        generatedTextures.add(wallTexture);

        Material wallMaterial = new Material(TextureAttribute.createDiffuse(wallTexture));
        Material accentMaterial = new Material(ColorAttribute.createDiffuse(accentColor));

        Model model = switch (type) {
            case TOWER -> generateTower(scale, style, wallMaterial, accentMaterial);
            case DOME -> generateDome(scale, style, wallMaterial, accentMaterial);
            case BUNKER -> generateBunker(scale, style, wallMaterial, accentMaterial);
            case HABITAT -> generateHabitat(scale, style, wallMaterial, accentMaterial);
            case FACTORY -> generateFactory(scale, style, wallMaterial, accentMaterial);
            case LANDING_PAD -> generateLandingPad(scale, style, wallMaterial, accentMaterial);
            case ANTENNA -> generateAntenna(scale, style, wallMaterial, accentMaterial);
            case WAREHOUSE -> generateWarehouse(scale, style, wallMaterial, accentMaterial);
            case MONUMENT -> generateMonument(scale, style, wallMaterial, accentMaterial);
            case OUTPOST -> generateOutpost(scale, style, wallMaterial, accentMaterial);
        };

        generatedModels.add(model);
        return model;
    }

    private ArchitectureStyle getStyleForPlanet(PlanetType planetType) {
        return switch (planetType) {
            case DESERT, ROCKY -> ArchitectureStyle.ANCIENT;
            case FOREST -> ArchitectureStyle.ALIEN_ORGANIC;
            case ICE -> ArchitectureStyle.ALIEN_CRYSTAL;
            case LAVA -> ArchitectureStyle.MILITARY;
            case OCEAN -> ArchitectureStyle.HUMAN;
            case GAS_GIANT -> ArchitectureStyle.ALIEN_CRYSTAL;
            case BARREN -> ArchitectureStyle.ANCIENT;
        };
    }

    private Color getPrimaryColor(ArchitectureStyle style) {
        return switch (style) {
            case HUMAN -> new Color(0.6f, 0.6f, 0.65f, 1f);
            case ALIEN_ORGANIC -> new Color(0.4f, 0.5f, 0.35f, 1f);
            case ALIEN_CRYSTAL -> new Color(0.5f, 0.7f, 0.8f, 1f);
            case ANCIENT -> new Color(0.7f, 0.6f, 0.45f, 1f);
            case MILITARY -> new Color(0.35f, 0.4f, 0.35f, 1f);
        };
    }

    private Color getAccentColor(ArchitectureStyle style) {
        return switch (style) {
            case HUMAN -> new Color(0.2f, 0.5f, 0.9f, 1f);
            case ALIEN_ORGANIC -> new Color(0.8f, 0.6f, 0.9f, 1f);
            case ALIEN_CRYSTAL -> new Color(0.9f, 0.95f, 1f, 1f);
            case ANCIENT -> new Color(0.3f, 0.8f, 0.6f, 1f);
            case MILITARY -> new Color(0.8f, 0.2f, 0.1f, 1f);
        };
    }

    private Model generateTower(float scale, ArchitectureStyle style, Material wall, Material accent) {
        modelBuilder.begin();

        float baseRadius = 3f * scale;
        float height = 15f * scale + random.nextFloat() * 10f * scale;
        int floors = 3 + random.nextInt(4);

        MeshPartBuilder builder = modelBuilder.part("tower", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wall);

        if (style == ArchitectureStyle.ALIEN_CRYSTAL) {
            // Crystal tower - hexagonal prism
            addPrism(builder, 0, height / 2, 0, 6, baseRadius, height);
        } else if (style == ArchitectureStyle.ALIEN_ORGANIC) {
            // Organic tower - tapered cylinder with bulges
            addOrganicTower(builder, baseRadius, height);
        } else {
            // Standard tower - stacked sections
            float sectionHeight = height / floors;
            for (int i = 0; i < floors; i++) {
                float y = i * sectionHeight;
                float r = baseRadius * (1f - i * 0.1f);
                addCylinder(builder, 0, y + sectionHeight / 2, 0, r, sectionHeight, 12);
            }
        }

        // Antenna on top
        MeshPartBuilder antennaBuilder = modelBuilder.part("antenna", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);
        addCylinder(antennaBuilder, 0, height + 2f * scale, 0, 0.2f * scale, 4f * scale, 6);

        return modelBuilder.end();
    }

    private Model generateDome(float scale, ArchitectureStyle style, Material wall, Material accent) {
        modelBuilder.begin();

        float radius = 5f * scale + random.nextFloat() * 3f * scale;

        MeshPartBuilder builder = modelBuilder.part("dome", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wall);

        // Dome (half sphere)
        addHemiSphere(builder, 0, 0, 0, radius, 16);

        // Base ring
        addTorus(builder, 0, 0, 0, radius, radius * 0.1f, 16, 8);

        // Entrance
        MeshPartBuilder entranceBuilder = modelBuilder.part("entrance", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);
        addBox(entranceBuilder, radius * 0.8f, radius * 0.2f, 0, radius * 0.4f, radius * 0.4f, radius * 0.3f);

        return modelBuilder.end();
    }

    private Model generateBunker(float scale, ArchitectureStyle style, Material wall, Material accent) {
        modelBuilder.begin();

        float width = 8f * scale;
        float depth = 6f * scale;
        float height = 3f * scale;

        MeshPartBuilder builder = modelBuilder.part("bunker", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wall);

        // Main structure - sloped top
        addBunkerShape(builder, width, height, depth);

        // Gun ports
        MeshPartBuilder portBuilder = modelBuilder.part("ports", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);

        float portSize = 0.5f * scale;
        addBox(portBuilder, width / 2, height * 0.6f, depth / 3, portSize, portSize, portSize * 0.5f);
        addBox(portBuilder, width / 2, height * 0.6f, -depth / 3, portSize, portSize, portSize * 0.5f);
        addBox(portBuilder, -width / 2, height * 0.6f, 0, portSize * 0.5f, portSize, portSize);

        return modelBuilder.end();
    }

    private Model generateHabitat(float scale, ArchitectureStyle style, Material wall, Material accent) {
        modelBuilder.begin();

        MeshPartBuilder builder = modelBuilder.part("habitat", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wall);

        int modules = 2 + random.nextInt(3);
        float moduleRadius = 3f * scale;
        float moduleLength = 5f * scale;

        // Connected cylindrical modules
        for (int i = 0; i < modules; i++) {
            float angle = 2 * MathUtils.PI * i / modules;
            float x = MathUtils.cos(angle) * moduleLength * 0.8f;
            float z = MathUtils.sin(angle) * moduleLength * 0.8f;

            // Horizontal module
            addCylinderHorizontal(builder, x, moduleRadius, z, moduleRadius, moduleLength, angle);
        }

        // Central hub
        addCylinder(builder, 0, moduleRadius, 0, moduleRadius * 1.2f, moduleRadius * 2, 12);

        // Windows
        MeshPartBuilder windowBuilder = modelBuilder.part("windows", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);

        for (int i = 0; i < 8; i++) {
            float angle = 2 * MathUtils.PI * i / 8;
            float wx = MathUtils.cos(angle) * moduleRadius * 1.22f;
            float wz = MathUtils.sin(angle) * moduleRadius * 1.22f;
            addBox(windowBuilder, wx, moduleRadius * 1.5f, wz, 0.3f * scale, 0.5f * scale, 0.1f * scale);
        }

        return modelBuilder.end();
    }

    private Model generateFactory(float scale, ArchitectureStyle style, Material wall, Material accent) {
        modelBuilder.begin();

        float width = 12f * scale;
        float depth = 8f * scale;
        float height = 6f * scale;

        MeshPartBuilder builder = modelBuilder.part("factory", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wall);

        // Main building
        addBox(builder, 0, height / 2, 0, width, height, depth);

        // Smokestacks
        int stacks = 2 + random.nextInt(2);
        for (int i = 0; i < stacks; i++) {
            float sx = (i - stacks / 2f + 0.5f) * 3f * scale;
            addCylinder(builder, sx, height + 3f * scale, -depth / 3, 0.8f * scale, 6f * scale, 8);
        }

        // Loading bay
        MeshPartBuilder bayBuilder = modelBuilder.part("bay", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);
        addBox(bayBuilder, width / 2 + 1f * scale, height * 0.4f, 0, 2f * scale, height * 0.8f, depth * 0.6f);

        return modelBuilder.end();
    }

    private Model generateLandingPad(float scale, ArchitectureStyle style, Material wall, Material accent) {
        modelBuilder.begin();

        float radius = 10f * scale;
        float height = 0.5f * scale;

        MeshPartBuilder builder = modelBuilder.part("pad", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wall);

        // Main platform
        addCylinder(builder, 0, height / 2, 0, radius, height, 16);

        // Landing circle marking
        MeshPartBuilder markBuilder = modelBuilder.part("marking", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);
        addTorus(markBuilder, 0, height + 0.1f, 0, radius * 0.6f, 0.2f * scale, 16, 4);

        // Support legs
        for (int i = 0; i < 4; i++) {
            float angle = MathUtils.PI / 4 + i * MathUtils.PI / 2;
            float x = MathUtils.cos(angle) * radius * 0.9f;
            float z = MathUtils.sin(angle) * radius * 0.9f;
            addCylinder(builder, x, -1f * scale, z, 0.5f * scale, 2f * scale, 6);
        }

        // Control tower
        addCylinder(builder, radius + 2f * scale, 3f * scale, 0, 1.5f * scale, 6f * scale, 8);
        addHemiSphere(builder, radius + 2f * scale, 6f * scale, 0, 1.5f * scale, 8);

        return modelBuilder.end();
    }

    private Model generateAntenna(float scale, ArchitectureStyle style, Material wall, Material accent) {
        modelBuilder.begin();

        float height = 20f * scale + random.nextFloat() * 10f * scale;

        MeshPartBuilder builder = modelBuilder.part("antenna", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wall);

        // Lattice tower (simplified as tapered cylinder)
        addTaperedCylinder(builder, 0, height / 2, 0, 2f * scale, 0.5f * scale, height, 4);

        // Dish
        MeshPartBuilder dishBuilder = modelBuilder.part("dish", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);
        addDish(dishBuilder, 0, height * 0.7f, 0, 3f * scale, 8);

        // Base platform
        addCylinder(builder, 0, 0.25f * scale, 0, 3f * scale, 0.5f * scale, 8);

        return modelBuilder.end();
    }

    private Model generateWarehouse(float scale, ArchitectureStyle style, Material wall, Material accent) {
        modelBuilder.begin();

        float width = 15f * scale;
        float depth = 10f * scale;
        float height = 5f * scale;

        MeshPartBuilder builder = modelBuilder.part("warehouse", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wall);

        // Main structure with curved roof
        addWarehouseShape(builder, width, height, depth);

        // Loading doors
        MeshPartBuilder doorBuilder = modelBuilder.part("doors", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);

        for (int i = 0; i < 3; i++) {
            float dx = (i - 1) * width / 3;
            addBox(doorBuilder, dx, height * 0.4f, depth / 2 + 0.1f, width / 4, height * 0.7f, 0.1f * scale);
        }

        return modelBuilder.end();
    }

    private Model generateMonument(float scale, ArchitectureStyle style, Material wall, Material accent) {
        modelBuilder.begin();

        float height = 12f * scale + random.nextFloat() * 8f * scale;

        MeshPartBuilder builder = modelBuilder.part("monument", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wall);

        if (style == ArchitectureStyle.ANCIENT) {
            // Obelisk
            addTaperedBox(builder, 0, height / 2, 0, 2f * scale, 1f * scale, height);
            // Pyramid cap
            addPyramid(builder, 0, height, 0, 1f * scale, 2f * scale);
        } else if (style == ArchitectureStyle.ALIEN_CRYSTAL) {
            // Crystal spire
            addPrism(builder, 0, height / 2, 0, 5, 1.5f * scale, height);
        } else {
            // Abstract sculpture
            addSpiralMonument(builder, height, scale);
        }

        // Base platform
        addBox(builder, 0, 0.25f * scale, 0, 4f * scale, 0.5f * scale, 4f * scale);

        return modelBuilder.end();
    }

    private Model generateOutpost(float scale, ArchitectureStyle style, Material wall, Material accent) {
        modelBuilder.begin();

        float size = 4f * scale;

        MeshPartBuilder builder = modelBuilder.part("outpost", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wall);

        // Main pod
        addHemiSphere(builder, 0, 0, 0, size, 10);

        // Entrance tube
        addCylinderHorizontal(builder, size, size * 0.3f, 0, size * 0.4f, size * 0.8f, 0);

        // Solar panels
        MeshPartBuilder panelBuilder = modelBuilder.part("panels", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);

        addBox(panelBuilder, 0, size * 1.2f, size * 1.5f, size * 0.1f, size, size * 1.5f);
        addBox(panelBuilder, 0, size * 1.2f, -size * 1.5f, size * 0.1f, size, size * 1.5f);

        return modelBuilder.end();
    }

    // Geometry helper methods
    private void addBox(MeshPartBuilder builder, float x, float y, float z, float w, float h, float d) {
        builder.box(x, y, z, w, h, d);
    }

    private void addCylinder(MeshPartBuilder builder, float x, float y, float z, float radius, float height, int segments) {
        float halfH = height / 2;

        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 b1 = new Vector3(x + MathUtils.cos(a1) * radius, y - halfH, z + MathUtils.sin(a1) * radius);
            Vector3 b2 = new Vector3(x + MathUtils.cos(a2) * radius, y - halfH, z + MathUtils.sin(a2) * radius);
            Vector3 t1 = new Vector3(x + MathUtils.cos(a1) * radius, y + halfH, z + MathUtils.sin(a1) * radius);
            Vector3 t2 = new Vector3(x + MathUtils.cos(a2) * radius, y + halfH, z + MathUtils.sin(a2) * radius);

            addQuad(builder, b1, b2, t2, t1);
        }

        // Caps
        Vector3 topCenter = new Vector3(x, y + halfH, z);
        Vector3 bottomCenter = new Vector3(x, y - halfH, z);
        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 t1 = new Vector3(x + MathUtils.cos(a1) * radius, y + halfH, z + MathUtils.sin(a1) * radius);
            Vector3 t2 = new Vector3(x + MathUtils.cos(a2) * radius, y + halfH, z + MathUtils.sin(a2) * radius);
            addTriangle(builder, topCenter, t1, t2);

            Vector3 b1 = new Vector3(x + MathUtils.cos(a1) * radius, y - halfH, z + MathUtils.sin(a1) * radius);
            Vector3 b2 = new Vector3(x + MathUtils.cos(a2) * radius, y - halfH, z + MathUtils.sin(a2) * radius);
            addTriangle(builder, bottomCenter, b2, b1);
        }
    }

    private void addCylinderHorizontal(MeshPartBuilder builder, float x, float y, float z, float radius, float length, float angle) {
        int segments = 8;
        float dx = MathUtils.cos(angle) * length / 2;
        float dz = MathUtils.sin(angle) * length / 2;

        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;

            float r1y = MathUtils.cos(a1) * radius;
            float r1z = MathUtils.sin(a1) * radius;
            float r2y = MathUtils.cos(a2) * radius;
            float r2z = MathUtils.sin(a2) * radius;

            Vector3 v1 = new Vector3(x - dx, y + r1y, z - dz + r1z);
            Vector3 v2 = new Vector3(x - dx, y + r2y, z - dz + r2z);
            Vector3 v3 = new Vector3(x + dx, y + r1y, z + dz + r1z);
            Vector3 v4 = new Vector3(x + dx, y + r2y, z + dz + r2z);

            addQuad(builder, v1, v2, v4, v3);
        }
    }

    private void addTaperedCylinder(MeshPartBuilder builder, float x, float y, float z, float r1, float r2, float h, int segments) {
        float halfH = h / 2;
        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 b1 = new Vector3(x + MathUtils.cos(a1) * r1, y - halfH, z + MathUtils.sin(a1) * r1);
            Vector3 b2 = new Vector3(x + MathUtils.cos(a2) * r1, y - halfH, z + MathUtils.sin(a2) * r1);
            Vector3 t1 = new Vector3(x + MathUtils.cos(a1) * r2, y + halfH, z + MathUtils.sin(a1) * r2);
            Vector3 t2 = new Vector3(x + MathUtils.cos(a2) * r2, y + halfH, z + MathUtils.sin(a2) * r2);

            addQuad(builder, b1, b2, t2, t1);
        }
    }

    private void addHemiSphere(MeshPartBuilder builder, float x, float y, float z, float radius, int div) {
        for (int lat = 0; lat < div / 2; lat++) {
            float theta1 = MathUtils.PI * lat / div;
            float theta2 = MathUtils.PI * (lat + 1) / div;

            for (int lon = 0; lon < div; lon++) {
                float phi1 = 2 * MathUtils.PI * lon / div;
                float phi2 = 2 * MathUtils.PI * (lon + 1) / div;

                Vector3 v1 = spherePoint(x, y, z, radius, theta1, phi1);
                Vector3 v2 = spherePoint(x, y, z, radius, theta1, phi2);
                Vector3 v3 = spherePoint(x, y, z, radius, theta2, phi1);
                Vector3 v4 = spherePoint(x, y, z, radius, theta2, phi2);

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

    private void addTorus(MeshPartBuilder builder, float x, float y, float z, float R, float r, int majorSeg, int minorSeg) {
        for (int i = 0; i < majorSeg; i++) {
            float theta1 = 2 * MathUtils.PI * i / majorSeg;
            float theta2 = 2 * MathUtils.PI * (i + 1) / majorSeg;

            for (int j = 0; j < minorSeg; j++) {
                float phi1 = 2 * MathUtils.PI * j / minorSeg;
                float phi2 = 2 * MathUtils.PI * (j + 1) / minorSeg;

                Vector3 v1 = torusPoint(x, y, z, R, r, theta1, phi1);
                Vector3 v2 = torusPoint(x, y, z, R, r, theta2, phi1);
                Vector3 v3 = torusPoint(x, y, z, R, r, theta1, phi2);
                Vector3 v4 = torusPoint(x, y, z, R, r, theta2, phi2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }
    }

    private Vector3 torusPoint(float cx, float cy, float cz, float R, float r, float theta, float phi) {
        return new Vector3(
            cx + (R + r * MathUtils.cos(phi)) * MathUtils.cos(theta),
            cy + r * MathUtils.sin(phi),
            cz + (R + r * MathUtils.cos(phi)) * MathUtils.sin(theta)
        );
    }

    private void addPrism(MeshPartBuilder builder, float x, float y, float z, int sides, float radius, float height) {
        float halfH = height / 2;
        Vector3[] top = new Vector3[sides];
        Vector3[] bottom = new Vector3[sides];

        for (int i = 0; i < sides; i++) {
            float angle = 2 * MathUtils.PI * i / sides;
            top[i] = new Vector3(x + MathUtils.cos(angle) * radius, y + halfH, z + MathUtils.sin(angle) * radius);
            bottom[i] = new Vector3(x + MathUtils.cos(angle) * radius, y - halfH, z + MathUtils.sin(angle) * radius);
        }

        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            addQuad(builder, bottom[i], bottom[next], top[next], top[i]);
        }

        Vector3 topCenter = new Vector3(x, y + halfH, z);
        Vector3 bottomCenter = new Vector3(x, y - halfH, z);
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            addTriangle(builder, topCenter, top[i], top[next]);
            addTriangle(builder, bottomCenter, bottom[next], bottom[i]);
        }
    }

    private void addPyramid(MeshPartBuilder builder, float x, float y, float z, float base, float height) {
        float hb = base / 2;
        Vector3 apex = new Vector3(x, y + height, z);
        Vector3 v1 = new Vector3(x - hb, y, z - hb);
        Vector3 v2 = new Vector3(x + hb, y, z - hb);
        Vector3 v3 = new Vector3(x + hb, y, z + hb);
        Vector3 v4 = new Vector3(x - hb, y, z + hb);

        addTriangle(builder, v1, v2, apex);
        addTriangle(builder, v2, v3, apex);
        addTriangle(builder, v3, v4, apex);
        addTriangle(builder, v4, v1, apex);
    }

    private void addTaperedBox(MeshPartBuilder builder, float x, float y, float z, float baseSize, float topSize, float height) {
        float hb = baseSize / 2, ht = topSize / 2, hh = height / 2;

        Vector3 b1 = new Vector3(x - hb, y - hh, z - hb);
        Vector3 b2 = new Vector3(x + hb, y - hh, z - hb);
        Vector3 b3 = new Vector3(x + hb, y - hh, z + hb);
        Vector3 b4 = new Vector3(x - hb, y - hh, z + hb);

        Vector3 t1 = new Vector3(x - ht, y + hh, z - ht);
        Vector3 t2 = new Vector3(x + ht, y + hh, z - ht);
        Vector3 t3 = new Vector3(x + ht, y + hh, z + ht);
        Vector3 t4 = new Vector3(x - ht, y + hh, z + ht);

        addQuad(builder, b1, b2, t2, t1);
        addQuad(builder, b2, b3, t3, t2);
        addQuad(builder, b3, b4, t4, t3);
        addQuad(builder, b4, b1, t1, t4);
        addQuad(builder, t1, t2, t3, t4);
        addQuad(builder, b4, b3, b2, b1);
    }

    private void addDish(MeshPartBuilder builder, float x, float y, float z, float radius, int segments) {
        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 center = new Vector3(x, y, z);
            Vector3 v1 = new Vector3(x + MathUtils.cos(a1) * radius, y - radius * 0.3f, z + MathUtils.sin(a1) * radius);
            Vector3 v2 = new Vector3(x + MathUtils.cos(a2) * radius, y - radius * 0.3f, z + MathUtils.sin(a2) * radius);

            addTriangle(builder, center, v1, v2);
        }
    }

    private void addOrganicTower(MeshPartBuilder builder, float baseRadius, float height) {
        int segments = 12;
        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;

            float y1 = t1 * height;
            float y2 = t2 * height;

            float r1 = baseRadius * (1 - t1 * 0.5f) * (1 + 0.1f * MathUtils.sin(t1 * MathUtils.PI * 4));
            float r2 = baseRadius * (1 - t2 * 0.5f) * (1 + 0.1f * MathUtils.sin(t2 * MathUtils.PI * 4));

            for (int j = 0; j < 8; j++) {
                float a1 = 2 * MathUtils.PI * j / 8;
                float a2 = 2 * MathUtils.PI * (j + 1) / 8;

                Vector3 v1 = new Vector3(MathUtils.cos(a1) * r1, y1, MathUtils.sin(a1) * r1);
                Vector3 v2 = new Vector3(MathUtils.cos(a2) * r1, y1, MathUtils.sin(a2) * r1);
                Vector3 v3 = new Vector3(MathUtils.cos(a1) * r2, y2, MathUtils.sin(a1) * r2);
                Vector3 v4 = new Vector3(MathUtils.cos(a2) * r2, y2, MathUtils.sin(a2) * r2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }
    }

    private void addBunkerShape(MeshPartBuilder builder, float w, float h, float d) {
        float hw = w / 2, hd = d / 2;

        // Sloped sides
        Vector3 b1 = new Vector3(-hw, 0, -hd);
        Vector3 b2 = new Vector3(hw, 0, -hd);
        Vector3 b3 = new Vector3(hw, 0, hd);
        Vector3 b4 = new Vector3(-hw, 0, hd);

        Vector3 t1 = new Vector3(-hw * 0.8f, h, -hd * 0.8f);
        Vector3 t2 = new Vector3(hw * 0.8f, h, -hd * 0.8f);
        Vector3 t3 = new Vector3(hw * 0.8f, h, hd * 0.8f);
        Vector3 t4 = new Vector3(-hw * 0.8f, h, hd * 0.8f);

        addQuad(builder, b1, b2, t2, t1); // Front
        addQuad(builder, b2, b3, t3, t2); // Right
        addQuad(builder, b3, b4, t4, t3); // Back
        addQuad(builder, b4, b1, t1, t4); // Left
        addQuad(builder, t1, t2, t3, t4); // Top
        addQuad(builder, b4, b3, b2, b1); // Bottom
    }

    private void addWarehouseShape(MeshPartBuilder builder, float w, float h, float d) {
        float hw = w / 2, hd = d / 2;

        // Walls
        addQuad(builder,
            new Vector3(-hw, 0, -hd), new Vector3(hw, 0, -hd),
            new Vector3(hw, h, -hd), new Vector3(-hw, h, -hd));
        addQuad(builder,
            new Vector3(hw, 0, hd), new Vector3(-hw, 0, hd),
            new Vector3(-hw, h, hd), new Vector3(hw, h, hd));

        // Curved roof (approximated)
        int roofSegs = 8;
        for (int i = 0; i < roofSegs; i++) {
            float a1 = MathUtils.PI * i / roofSegs;
            float a2 = MathUtils.PI * (i + 1) / roofSegs;

            float y1 = h + MathUtils.sin(a1) * h * 0.3f;
            float y2 = h + MathUtils.sin(a2) * h * 0.3f;
            float z1 = MathUtils.cos(a1) * hd;
            float z2 = MathUtils.cos(a2) * hd;

            addQuad(builder,
                new Vector3(-hw, y1, z1), new Vector3(hw, y1, z1),
                new Vector3(hw, y2, z2), new Vector3(-hw, y2, z2));
        }

        // End walls (triangular tops)
        Vector3 apex1 = new Vector3(-hw, h + h * 0.3f, 0);
        Vector3 apex2 = new Vector3(hw, h + h * 0.3f, 0);

        addTriangle(builder, new Vector3(-hw, h, -hd), new Vector3(-hw, h, hd), apex1);
        addTriangle(builder, new Vector3(hw, h, hd), new Vector3(hw, h, -hd), apex2);
    }

    private void addSpiralMonument(MeshPartBuilder builder, float height, float scale) {
        int segments = 20;
        float radius = 1.5f * scale;

        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;

            float y1 = t1 * height;
            float y2 = t2 * height;
            float a1 = t1 * MathUtils.PI * 4;
            float a2 = t2 * MathUtils.PI * 4;
            float r1 = radius * (1 - t1 * 0.7f);
            float r2 = radius * (1 - t2 * 0.7f);

            Vector3 v1 = new Vector3(MathUtils.cos(a1) * r1, y1, MathUtils.sin(a1) * r1);
            Vector3 v2 = new Vector3(MathUtils.cos(a2) * r2, y2, MathUtils.sin(a2) * r2);

            // Tube segment
            for (int j = 0; j < 4; j++) {
                float b1 = 2 * MathUtils.PI * j / 4;
                float b2 = 2 * MathUtils.PI * (j + 1) / 4;
                float tr = 0.2f * scale * (1 - t1 * 0.5f);

                Vector3 offset1 = new Vector3(MathUtils.cos(b1) * tr, MathUtils.sin(b1) * tr, 0);
                Vector3 offset2 = new Vector3(MathUtils.cos(b2) * tr, MathUtils.sin(b2) * tr, 0);

                addQuad(builder,
                    new Vector3(v1).add(offset1), new Vector3(v1).add(offset2),
                    new Vector3(v2).add(offset2), new Vector3(v2).add(offset1));
            }
        }
    }

    private void addTriangle(MeshPartBuilder builder, Vector3 v1, Vector3 v2, Vector3 v3) {
        Vector3 normal = new Vector3(v2).sub(v1).crs(new Vector3(v3).sub(v1)).nor();
        builder.triangle(
            new MeshPartBuilder.VertexInfo().setPos(v1).setNor(normal).setUV(0, 0),
            new MeshPartBuilder.VertexInfo().setPos(v2).setNor(normal).setUV(1, 0),
            new MeshPartBuilder.VertexInfo().setPos(v3).setNor(normal).setUV(0.5f, 1)
        );
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
