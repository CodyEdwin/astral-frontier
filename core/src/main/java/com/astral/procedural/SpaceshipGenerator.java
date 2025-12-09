package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
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
 * Generates procedural spaceships with various styles and configurations
 */
public class SpaceshipGenerator implements Disposable {

    private final long seed;
    private final Random random;
    private final ModelBuilder modelBuilder;
    private final Array<Texture> generatedTextures = new Array<>();
    private final Array<Model> generatedModels = new Array<>();

    public enum ShipClass {
        FIGHTER,      // Small, fast, agile
        FREIGHTER,    // Large cargo ship
        CRUISER,      // Medium combat vessel
        DESTROYER,    // Heavy warship
        SHUTTLE,      // Small transport
        INTERCEPTOR,  // Very fast attack craft
        BOMBER        // Heavy attack craft
    }

    public enum ShipStyle {
        MILITARY,     // Angular, armored
        CIVILIAN,     // Rounded, colorful
        ALIEN,        // Organic, unusual
        PIRATE,       // Rugged, patched
        ANCIENT       // Mysterious, ornate
    }

    public SpaceshipGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
        this.modelBuilder = new ModelBuilder();
    }

    /**
     * Generate a complete spaceship
     */
    public Model generateShip(ShipClass shipClass, ShipStyle style, long shipSeed) {
        random.setSeed(shipSeed);

        // Determine ship parameters based on class
        ShipParams params = getShipParams(shipClass);

        // Get colors based on style
        Color hullColor = getHullColor(style);
        Color accentColor = getAccentColor(style);
        Color engineColor = getEngineColor(style);

        // Generate textures
        Texture hullTexture = ProceduralTexture.metalHull(128, 128, hullColor, shipSeed);
        Texture engineTexture = ProceduralTexture.energyGlow(64, 64, engineColor, new Color(engineColor).mul(0.3f), shipSeed);
        generatedTextures.add(hullTexture);
        generatedTextures.add(engineTexture);

        Material hullMaterial = new Material(TextureAttribute.createDiffuse(hullTexture));
        Material accentMaterial = new Material(ColorAttribute.createDiffuse(accentColor));
        Material engineMaterial = new Material(TextureAttribute.createDiffuse(engineTexture));

        modelBuilder.begin();

        // Build hull based on style
        switch (style) {
            case MILITARY -> buildMilitaryHull(params, hullMaterial, accentMaterial);
            case CIVILIAN -> buildCivilianHull(params, hullMaterial, accentMaterial);
            case ALIEN -> buildAlienHull(params, hullMaterial, accentMaterial);
            case PIRATE -> buildPirateHull(params, hullMaterial, accentMaterial);
            case ANCIENT -> buildAncientHull(params, hullMaterial, accentMaterial);
        }

        // Add engines
        addEngines(params, engineMaterial);

        // Add weapons based on class
        if (params.hasWeapons) {
            addWeapons(params, accentMaterial);
        }

        // Add wings/fins
        if (params.hasWings) {
            addWings(params, hullMaterial);
        }

        // Add cockpit
        addCockpit(params, new Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.3f, 0.5f, 0.8f))));

        Model model = modelBuilder.end();
        generatedModels.add(model);
        return model;
    }

    private ShipParams getShipParams(ShipClass shipClass) {
        return switch (shipClass) {
            case FIGHTER -> new ShipParams(8f, 3f, 2f, 2, true, true, true);
            case FREIGHTER -> new ShipParams(30f, 15f, 12f, 4, false, false, false);
            case CRUISER -> new ShipParams(20f, 8f, 6f, 4, true, true, true);
            case DESTROYER -> new ShipParams(35f, 12f, 10f, 6, true, true, true);
            case SHUTTLE -> new ShipParams(6f, 4f, 3f, 2, false, false, false);
            case INTERCEPTOR -> new ShipParams(10f, 2f, 1.5f, 2, true, true, true);
            case BOMBER -> new ShipParams(15f, 6f, 4f, 4, true, true, false);
        };
    }

    private Color getHullColor(ShipStyle style) {
        return switch (style) {
            case MILITARY -> new Color(0.4f, 0.45f, 0.5f, 1f);   // Steel gray
            case CIVILIAN -> new Color(0.9f, 0.9f, 0.95f, 1f);  // White
            case ALIEN -> new Color(0.3f, 0.5f, 0.4f, 1f);      // Organic green
            case PIRATE -> new Color(0.3f, 0.25f, 0.2f, 1f);    // Rusty brown
            case ANCIENT -> new Color(0.6f, 0.55f, 0.4f, 1f);   // Bronze
        };
    }

    private Color getAccentColor(ShipStyle style) {
        return switch (style) {
            case MILITARY -> new Color(0.8f, 0.2f, 0.1f, 1f);   // Red
            case CIVILIAN -> new Color(0.2f, 0.5f, 0.9f, 1f);   // Blue
            case ALIEN -> new Color(0.8f, 0.4f, 0.9f, 1f);      // Purple
            case PIRATE -> new Color(0.9f, 0.7f, 0.1f, 1f);     // Gold
            case ANCIENT -> new Color(0.3f, 0.8f, 0.6f, 1f);    // Cyan
        };
    }

    private Color getEngineColor(ShipStyle style) {
        return switch (style) {
            case MILITARY -> new Color(0.3f, 0.5f, 1f, 1f);     // Blue
            case CIVILIAN -> new Color(0.9f, 0.6f, 0.2f, 1f);   // Orange
            case ALIEN -> new Color(0.4f, 1f, 0.6f, 1f);        // Green
            case PIRATE -> new Color(1f, 0.3f, 0.1f, 1f);       // Red/orange
            case ANCIENT -> new Color(0.8f, 0.9f, 1f, 1f);      // White/blue
        };
    }

    private void buildMilitaryHull(ShipParams p, Material hull, Material accent) {
        MeshPartBuilder builder = modelBuilder.part("hull", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            hull);

        // Angular main body
        float hl = p.length / 2, hw = p.width / 2, hh = p.height / 2;

        // Front wedge
        Vector3 nose = new Vector3(hl * 1.2f, 0, 0);
        Vector3 fl1 = new Vector3(hl * 0.3f, hh * 0.5f, hw);
        Vector3 fl2 = new Vector3(hl * 0.3f, hh * 0.5f, -hw);
        Vector3 fl3 = new Vector3(hl * 0.3f, -hh * 0.5f, hw);
        Vector3 fl4 = new Vector3(hl * 0.3f, -hh * 0.5f, -hw);

        // Nose triangles
        addTriangle(builder, nose, fl1, fl2);
        addTriangle(builder, nose, fl4, fl3);
        addTriangle(builder, nose, fl2, fl4);
        addTriangle(builder, nose, fl3, fl1);

        // Main body box
        Vector3 bl1 = new Vector3(-hl, hh, hw);
        Vector3 bl2 = new Vector3(-hl, hh, -hw);
        Vector3 bl3 = new Vector3(-hl, -hh, hw);
        Vector3 bl4 = new Vector3(-hl, -hh, -hw);

        // Connect front to back
        addQuad(builder, fl1, fl3, bl3, bl1); // Left
        addQuad(builder, fl4, fl2, bl2, bl4); // Right
        addQuad(builder, fl1, bl1, bl2, fl2); // Top
        addQuad(builder, fl3, fl4, bl4, bl3); // Bottom
        addQuad(builder, bl1, bl3, bl4, bl2); // Back
    }

    private void buildCivilianHull(ShipParams p, Material hull, Material accent) {
        MeshPartBuilder builder = modelBuilder.part("hull", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            hull);

        // Rounded ellipsoid-ish body
        int segments = 12;
        float hl = p.length / 2, hw = p.width / 2, hh = p.height / 2;

        for (int i = 0; i < segments; i++) {
            float theta1 = MathUtils.PI * i / segments;
            float theta2 = MathUtils.PI * (i + 1) / segments;

            for (int j = 0; j < segments; j++) {
                float phi1 = 2 * MathUtils.PI * j / segments;
                float phi2 = 2 * MathUtils.PI * (j + 1) / segments;

                Vector3 v1 = ellipsoidPoint(hl, hh, hw, theta1, phi1);
                Vector3 v2 = ellipsoidPoint(hl, hh, hw, theta1, phi2);
                Vector3 v3 = ellipsoidPoint(hl, hh, hw, theta2, phi1);
                Vector3 v4 = ellipsoidPoint(hl, hh, hw, theta2, phi2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }
    }

    private void buildAlienHull(ShipParams p, Material hull, Material accent) {
        MeshPartBuilder builder = modelBuilder.part("hull", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            hull);

        // Organic asymmetric shape
        int segments = 16;
        float hl = p.length / 2;

        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;

            float r1 = alienRadius(t1, p.width / 2, p.height / 2);
            float r2 = alienRadius(t2, p.width / 2, p.height / 2);

            float x1 = MathUtils.lerp(-hl, hl, t1);
            float x2 = MathUtils.lerp(-hl, hl, t2);

            for (int j = 0; j < 8; j++) {
                float a1 = 2 * MathUtils.PI * j / 8;
                float a2 = 2 * MathUtils.PI * (j + 1) / 8;

                // Add organic wobble
                float wobble1 = 1f + 0.2f * MathUtils.sin(j * 1.5f + i * 0.5f);
                float wobble2 = 1f + 0.2f * MathUtils.sin((j + 1) * 1.5f + i * 0.5f);

                Vector3 v1 = new Vector3(x1, r1 * wobble1 * MathUtils.sin(a1), r1 * wobble1 * MathUtils.cos(a1));
                Vector3 v2 = new Vector3(x1, r1 * wobble2 * MathUtils.sin(a2), r1 * wobble2 * MathUtils.cos(a2));
                Vector3 v3 = new Vector3(x2, r2 * wobble1 * MathUtils.sin(a1), r2 * wobble1 * MathUtils.cos(a1));
                Vector3 v4 = new Vector3(x2, r2 * wobble2 * MathUtils.sin(a2), r2 * wobble2 * MathUtils.cos(a2));

                addQuad(builder, v1, v2, v4, v3);
            }
        }
    }

    private float alienRadius(float t, float maxWidth, float maxHeight) {
        // Organic tapered shape
        float base = MathUtils.sin(t * MathUtils.PI);
        float bulge = 0.3f * MathUtils.sin(t * MathUtils.PI * 3);
        return (base + bulge) * Math.max(maxWidth, maxHeight) * 0.5f + 0.5f;
    }

    private void buildPirateHull(ShipParams p, Material hull, Material accent) {
        // Similar to military but with asymmetric additions
        buildMilitaryHull(p, hull, accent);

        // Add "patched" sections
        MeshPartBuilder patchBuilder = modelBuilder.part("patches", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);

        float hl = p.length / 2;
        // Random armor plates
        for (int i = 0; i < 3; i++) {
            float px = random.nextFloat() * hl - hl / 2;
            float py = (random.nextFloat() - 0.5f) * p.height * 0.3f;
            float pz = (random.nextBoolean() ? 1 : -1) * p.width / 2 * 1.02f;

            float size = 1f + random.nextFloat() * 2f;
            addPlate(patchBuilder, px, py, pz, size, pz > 0);
        }
    }

    private void buildAncientHull(ShipParams p, Material hull, Material accent) {
        // Ring-based design
        MeshPartBuilder builder = modelBuilder.part("hull", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            hull);

        float hl = p.length / 2;

        // Central core
        int segments = 16;
        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;
            float r = p.width * 0.3f;

            Vector3 f1 = new Vector3(hl, r * MathUtils.sin(a1), r * MathUtils.cos(a1));
            Vector3 f2 = new Vector3(hl, r * MathUtils.sin(a2), r * MathUtils.cos(a2));
            Vector3 b1 = new Vector3(-hl, r * MathUtils.sin(a1), r * MathUtils.cos(a1));
            Vector3 b2 = new Vector3(-hl, r * MathUtils.sin(a2), r * MathUtils.cos(a2));

            addQuad(builder, f1, f2, b2, b1);
        }

        // Outer ring
        MeshPartBuilder ringBuilder = modelBuilder.part("ring", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            accent);

        float ringR = p.width * 0.6f;
        float ringThick = p.width * 0.1f;
        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 o1 = new Vector3(0, (ringR + ringThick) * MathUtils.sin(a1), (ringR + ringThick) * MathUtils.cos(a1));
            Vector3 o2 = new Vector3(0, (ringR + ringThick) * MathUtils.sin(a2), (ringR + ringThick) * MathUtils.cos(a2));
            Vector3 i1 = new Vector3(0, (ringR - ringThick) * MathUtils.sin(a1), (ringR - ringThick) * MathUtils.cos(a1));
            Vector3 i2 = new Vector3(0, (ringR - ringThick) * MathUtils.sin(a2), (ringR - ringThick) * MathUtils.cos(a2));

            // Ring faces
            addQuad(ringBuilder, o1, o2, i2, i1);
        }
    }

    private void addEngines(ShipParams p, Material engineMaterial) {
        MeshPartBuilder builder = modelBuilder.part("engines", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            engineMaterial);

        float hl = p.length / 2;
        float engineRadius = p.height * 0.25f;
        float engineLength = p.length * 0.15f;

        float spacing = p.width / (p.engineCount + 1);
        for (int e = 0; e < p.engineCount; e++) {
            float ez = -p.width / 2 + spacing * (e + 1);
            float ey = -p.height * 0.3f;

            // Engine cylinder
            addEngineCylinder(builder, -hl, ey, ez, engineRadius, engineLength, 8);
        }
    }

    private void addEngineCylinder(MeshPartBuilder builder, float x, float y, float z, float radius, float length, int segments) {
        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 f1 = new Vector3(x, y + radius * MathUtils.sin(a1), z + radius * MathUtils.cos(a1));
            Vector3 f2 = new Vector3(x, y + radius * MathUtils.sin(a2), z + radius * MathUtils.cos(a2));
            Vector3 b1 = new Vector3(x - length, y + radius * MathUtils.sin(a1), z + radius * MathUtils.cos(a1));
            Vector3 b2 = new Vector3(x - length, y + radius * MathUtils.sin(a2), z + radius * MathUtils.cos(a2));

            addQuad(builder, f1, f2, b2, b1);
        }

        // Engine glow (back cap)
        Vector3 center = new Vector3(x - length, y, z);
        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;
            Vector3 v1 = new Vector3(x - length, y + radius * MathUtils.sin(a1), z + radius * MathUtils.cos(a1));
            Vector3 v2 = new Vector3(x - length, y + radius * MathUtils.sin(a2), z + radius * MathUtils.cos(a2));
            addTriangle(builder, center, v2, v1);
        }
    }

    private void addWeapons(ShipParams p, Material weaponMaterial) {
        MeshPartBuilder builder = modelBuilder.part("weapons", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            weaponMaterial);

        float hl = p.length / 2;

        // Wing-mounted guns
        float gunLength = p.length * 0.2f;
        float gunRadius = 0.15f;

        // Left gun
        addGun(builder, hl * 0.5f, 0, p.width / 2 + 0.5f, gunLength, gunRadius);
        // Right gun
        addGun(builder, hl * 0.5f, 0, -p.width / 2 - 0.5f, gunLength, gunRadius);
    }

    private void addGun(MeshPartBuilder builder, float x, float y, float z, float length, float radius) {
        int segments = 6;
        for (int i = 0; i < segments; i++) {
            float a1 = 2 * MathUtils.PI * i / segments;
            float a2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 f1 = new Vector3(x + length, y + radius * MathUtils.sin(a1), z + radius * MathUtils.cos(a1));
            Vector3 f2 = new Vector3(x + length, y + radius * MathUtils.sin(a2), z + radius * MathUtils.cos(a2));
            Vector3 b1 = new Vector3(x, y + radius * MathUtils.sin(a1), z + radius * MathUtils.cos(a1));
            Vector3 b2 = new Vector3(x, y + radius * MathUtils.sin(a2), z + radius * MathUtils.cos(a2));

            addQuad(builder, f1, f2, b2, b1);
        }
    }

    private void addWings(ShipParams p, Material wingMaterial) {
        MeshPartBuilder builder = modelBuilder.part("wings", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates,
            wingMaterial);

        float wingSpan = p.width * 0.8f;
        float wingChord = p.length * 0.4f;
        float wingThick = 0.3f;

        // Left wing
        addWingGeometry(builder, 0, 0, p.width / 2, wingChord, wingSpan, wingThick, false);
        // Right wing
        addWingGeometry(builder, 0, 0, -p.width / 2, wingChord, wingSpan, wingThick, true);
    }

    private void addWingGeometry(MeshPartBuilder builder, float x, float y, float z, float chord, float span, float thick, boolean mirror) {
        float dir = mirror ? -1 : 1;
        float ht = thick / 2;

        Vector3 root1 = new Vector3(x + chord / 2, y + ht, z);
        Vector3 root2 = new Vector3(x - chord / 2, y + ht, z);
        Vector3 root3 = new Vector3(x + chord / 2, y - ht, z);
        Vector3 root4 = new Vector3(x - chord / 2, y - ht, z);

        Vector3 tip1 = new Vector3(x + chord / 4, y + ht * 0.5f, z + span * dir);
        Vector3 tip2 = new Vector3(x - chord / 4, y + ht * 0.5f, z + span * dir);
        Vector3 tip3 = new Vector3(x + chord / 4, y - ht * 0.5f, z + span * dir);
        Vector3 tip4 = new Vector3(x - chord / 4, y - ht * 0.5f, z + span * dir);

        // Top
        addQuad(builder, root1, tip1, tip2, root2);
        // Bottom
        addQuad(builder, root4, tip4, tip3, root3);
        // Front
        addQuad(builder, root1, root3, tip3, tip1);
        // Back
        addQuad(builder, root2, tip2, tip4, root4);
        // Tip
        addQuad(builder, tip1, tip3, tip4, tip2);
    }

    private void addCockpit(ShipParams p, Material cockpitMaterial) {
        MeshPartBuilder builder = modelBuilder.part("cockpit", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal,
            cockpitMaterial);

        float hl = p.length / 2;
        float cockpitSize = p.height * 0.4f;

        // Simple dome
        int segments = 8;
        Vector3 center = new Vector3(hl * 0.6f, p.height / 2 + cockpitSize * 0.3f, 0);

        for (int i = 0; i < segments; i++) {
            float a1 = MathUtils.PI * i / segments;
            float a2 = MathUtils.PI * (i + 1) / segments;

            for (int j = 0; j < segments; j++) {
                float b1 = 2 * MathUtils.PI * j / segments;
                float b2 = 2 * MathUtils.PI * (j + 1) / segments;

                Vector3 v1 = spherePoint(center, cockpitSize, a1, b1);
                Vector3 v2 = spherePoint(center, cockpitSize, a1, b2);
                Vector3 v3 = spherePoint(center, cockpitSize, a2, b1);
                Vector3 v4 = spherePoint(center, cockpitSize, a2, b2);

                if (v1.y >= center.y - cockpitSize * 0.3f) {
                    addQuad(builder, v1, v2, v4, v3);
                }
            }
        }
    }

    private Vector3 spherePoint(Vector3 center, float radius, float theta, float phi) {
        return new Vector3(
            center.x + radius * MathUtils.sin(theta) * MathUtils.cos(phi),
            center.y + radius * MathUtils.cos(theta),
            center.z + radius * MathUtils.sin(theta) * MathUtils.sin(phi)
        );
    }

    private Vector3 ellipsoidPoint(float a, float b, float c, float theta, float phi) {
        return new Vector3(
            a * MathUtils.cos(phi),
            b * MathUtils.cos(theta) * MathUtils.sin(phi),
            c * MathUtils.sin(theta) * MathUtils.sin(phi)
        );
    }

    private void addPlate(MeshPartBuilder builder, float x, float y, float z, float size, boolean faceOut) {
        float hs = size / 2;
        float dir = faceOut ? 1 : -1;

        Vector3 v1 = new Vector3(x - hs, y - hs, z);
        Vector3 v2 = new Vector3(x + hs, y - hs, z);
        Vector3 v3 = new Vector3(x + hs, y + hs, z);
        Vector3 v4 = new Vector3(x - hs, y + hs, z);

        if (faceOut) {
            addQuad(builder, v1, v2, v3, v4);
        } else {
            addQuad(builder, v4, v3, v2, v1);
        }
    }

    // Helper methods
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
        for (Texture t : generatedTextures) {
            t.dispose();
        }
        for (Model m : generatedModels) {
            m.dispose();
        }
        generatedTextures.clear();
        generatedModels.clear();
    }

    // Inner class for ship parameters
    private static class ShipParams {
        float length, width, height;
        int engineCount;
        boolean hasWeapons, hasWings, hasCockpit;

        ShipParams(float length, float width, float height, int engines, boolean weapons, boolean wings, boolean cockpit) {
            this.length = length;
            this.width = width;
            this.height = height;
            this.engineCount = engines;
            this.hasWeapons = weapons;
            this.hasWings = wings;
            this.hasCockpit = cockpit;
        }
    }
}
