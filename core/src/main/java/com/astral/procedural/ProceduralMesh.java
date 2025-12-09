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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.Random;

/**
 * Procedural mesh generation - creates 3D models entirely in code
 */
public class ProceduralMesh {

    private static final ModelBuilder modelBuilder = new ModelBuilder();
    private static final long VERTEX_ATTR = VertexAttributes.Usage.Position |
                                            VertexAttributes.Usage.Normal |
                                            VertexAttributes.Usage.TextureCoordinates;

    /**
     * Create a sphere with optional deformation for organic shapes
     */
    public static Model sphere(float radius, int divisions, Material material, float deformation, long seed) {
        Random random = new Random(seed);

        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("sphere", GL20.GL_TRIANGLES, VERTEX_ATTR, material);

        for (int lat = 0; lat < divisions; lat++) {
            float theta1 = (float) Math.PI * lat / divisions;
            float theta2 = (float) Math.PI * (lat + 1) / divisions;

            for (int lon = 0; lon < divisions; lon++) {
                float phi1 = 2 * (float) Math.PI * lon / divisions;
                float phi2 = 2 * (float) Math.PI * (lon + 1) / divisions;

                Vector3 v1 = spherePoint(radius, theta1, phi1, deformation, random);
                Vector3 v2 = spherePoint(radius, theta1, phi2, deformation, random);
                Vector3 v3 = spherePoint(radius, theta2, phi1, deformation, random);
                Vector3 v4 = spherePoint(radius, theta2, phi2, deformation, random);

                addQuad(builder, v1, v2, v4, v3);
            }
        }

        return modelBuilder.end();
    }

    private static Vector3 spherePoint(float radius, float theta, float phi, float deform, Random random) {
        float r = radius + (deform > 0 ? (random.nextFloat() - 0.5f) * deform : 0);
        return new Vector3(
            r * MathUtils.sin(theta) * MathUtils.cos(phi),
            r * MathUtils.cos(theta),
            r * MathUtils.sin(theta) * MathUtils.sin(phi)
        );
    }

    /**
     * Create a cylinder/tube
     */
    public static Model cylinder(float radius, float height, int segments, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("cylinder", GL20.GL_TRIANGLES, VERTEX_ATTR, material);

        float halfHeight = height / 2;

        // Side faces
        for (int i = 0; i < segments; i++) {
            float angle1 = 2 * MathUtils.PI * i / segments;
            float angle2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 v1 = new Vector3(MathUtils.cos(angle1) * radius, -halfHeight, MathUtils.sin(angle1) * radius);
            Vector3 v2 = new Vector3(MathUtils.cos(angle2) * radius, -halfHeight, MathUtils.sin(angle2) * radius);
            Vector3 v3 = new Vector3(MathUtils.cos(angle1) * radius, halfHeight, MathUtils.sin(angle1) * radius);
            Vector3 v4 = new Vector3(MathUtils.cos(angle2) * radius, halfHeight, MathUtils.sin(angle2) * radius);

            addQuad(builder, v1, v2, v4, v3);
        }

        // Top and bottom caps
        Vector3 topCenter = new Vector3(0, halfHeight, 0);
        Vector3 bottomCenter = new Vector3(0, -halfHeight, 0);

        for (int i = 0; i < segments; i++) {
            float angle1 = 2 * MathUtils.PI * i / segments;
            float angle2 = 2 * MathUtils.PI * (i + 1) / segments;

            // Top
            Vector3 t1 = new Vector3(MathUtils.cos(angle1) * radius, halfHeight, MathUtils.sin(angle1) * radius);
            Vector3 t2 = new Vector3(MathUtils.cos(angle2) * radius, halfHeight, MathUtils.sin(angle2) * radius);
            addTriangle(builder, topCenter, t2, t1);

            // Bottom
            Vector3 b1 = new Vector3(MathUtils.cos(angle1) * radius, -halfHeight, MathUtils.sin(angle1) * radius);
            Vector3 b2 = new Vector3(MathUtils.cos(angle2) * radius, -halfHeight, MathUtils.sin(angle2) * radius);
            addTriangle(builder, bottomCenter, b1, b2);
        }

        return modelBuilder.end();
    }

    /**
     * Create a cone
     */
    public static Model cone(float radius, float height, int segments, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("cone", GL20.GL_TRIANGLES, VERTEX_ATTR, material);

        Vector3 apex = new Vector3(0, height, 0);
        Vector3 center = new Vector3(0, 0, 0);

        for (int i = 0; i < segments; i++) {
            float angle1 = 2 * MathUtils.PI * i / segments;
            float angle2 = 2 * MathUtils.PI * (i + 1) / segments;

            Vector3 v1 = new Vector3(MathUtils.cos(angle1) * radius, 0, MathUtils.sin(angle1) * radius);
            Vector3 v2 = new Vector3(MathUtils.cos(angle2) * radius, 0, MathUtils.sin(angle2) * radius);

            // Side
            addTriangle(builder, v1, v2, apex);

            // Base
            addTriangle(builder, center, v1, v2);
        }

        return modelBuilder.end();
    }

    /**
     * Create a torus (donut shape)
     */
    public static Model torus(float majorRadius, float minorRadius, int majorSegments, int minorSegments, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("torus", GL20.GL_TRIANGLES, VERTEX_ATTR, material);

        for (int i = 0; i < majorSegments; i++) {
            float theta1 = 2 * MathUtils.PI * i / majorSegments;
            float theta2 = 2 * MathUtils.PI * (i + 1) / majorSegments;

            for (int j = 0; j < minorSegments; j++) {
                float phi1 = 2 * MathUtils.PI * j / minorSegments;
                float phi2 = 2 * MathUtils.PI * (j + 1) / minorSegments;

                Vector3 v1 = torusPoint(majorRadius, minorRadius, theta1, phi1);
                Vector3 v2 = torusPoint(majorRadius, minorRadius, theta2, phi1);
                Vector3 v3 = torusPoint(majorRadius, minorRadius, theta1, phi2);
                Vector3 v4 = torusPoint(majorRadius, minorRadius, theta2, phi2);

                addQuad(builder, v1, v2, v4, v3);
            }
        }

        return modelBuilder.end();
    }

    private static Vector3 torusPoint(float R, float r, float theta, float phi) {
        return new Vector3(
            (R + r * MathUtils.cos(phi)) * MathUtils.cos(theta),
            r * MathUtils.sin(phi),
            (R + r * MathUtils.cos(phi)) * MathUtils.sin(theta)
        );
    }

    /**
     * Create a prism with n sides
     */
    public static Model prism(int sides, float radius, float height, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("prism", GL20.GL_TRIANGLES, VERTEX_ATTR, material);

        float halfHeight = height / 2;

        Vector3[] top = new Vector3[sides];
        Vector3[] bottom = new Vector3[sides];

        for (int i = 0; i < sides; i++) {
            float angle = 2 * MathUtils.PI * i / sides;
            float x = MathUtils.cos(angle) * radius;
            float z = MathUtils.sin(angle) * radius;
            top[i] = new Vector3(x, halfHeight, z);
            bottom[i] = new Vector3(x, -halfHeight, z);
        }

        // Side faces
        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            addQuad(builder, bottom[i], bottom[next], top[next], top[i]);
        }

        // Top and bottom caps (fan triangulation)
        Vector3 topCenter = new Vector3(0, halfHeight, 0);
        Vector3 bottomCenter = new Vector3(0, -halfHeight, 0);

        for (int i = 0; i < sides; i++) {
            int next = (i + 1) % sides;
            addTriangle(builder, topCenter, top[i], top[next]);
            addTriangle(builder, bottomCenter, bottom[next], bottom[i]);
        }

        return modelBuilder.end();
    }

    /**
     * Create a box with beveled edges
     */
    public static Model beveledBox(float width, float height, float depth, float bevel, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("beveledBox", GL20.GL_TRIANGLES, VERTEX_ATTR, material);

        float hw = width / 2, hh = height / 2, hd = depth / 2;
        float b = bevel;

        // Main faces (slightly inset)
        // Front
        addQuad(builder,
            new Vector3(-hw + b, -hh + b, hd),
            new Vector3(hw - b, -hh + b, hd),
            new Vector3(hw - b, hh - b, hd),
            new Vector3(-hw + b, hh - b, hd)
        );
        // Back
        addQuad(builder,
            new Vector3(hw - b, -hh + b, -hd),
            new Vector3(-hw + b, -hh + b, -hd),
            new Vector3(-hw + b, hh - b, -hd),
            new Vector3(hw - b, hh - b, -hd)
        );
        // Left
        addQuad(builder,
            new Vector3(-hw, -hh + b, -hd + b),
            new Vector3(-hw, -hh + b, hd - b),
            new Vector3(-hw, hh - b, hd - b),
            new Vector3(-hw, hh - b, -hd + b)
        );
        // Right
        addQuad(builder,
            new Vector3(hw, -hh + b, hd - b),
            new Vector3(hw, -hh + b, -hd + b),
            new Vector3(hw, hh - b, -hd + b),
            new Vector3(hw, hh - b, hd - b)
        );
        // Top
        addQuad(builder,
            new Vector3(-hw + b, hh, hd - b),
            new Vector3(hw - b, hh, hd - b),
            new Vector3(hw - b, hh, -hd + b),
            new Vector3(-hw + b, hh, -hd + b)
        );
        // Bottom
        addQuad(builder,
            new Vector3(-hw + b, -hh, -hd + b),
            new Vector3(hw - b, -hh, -hd + b),
            new Vector3(hw - b, -hh, hd - b),
            new Vector3(-hw + b, -hh, hd - b)
        );

        // Edge bevels would go here (simplified version without full bevels)

        return modelBuilder.end();
    }

    /**
     * Create an extruded star shape
     */
    public static Model star(int points, float outerRadius, float innerRadius, float depth, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("star", GL20.GL_TRIANGLES, VERTEX_ATTR, material);

        float halfDepth = depth / 2;
        int totalVerts = points * 2;
        Vector3[] front = new Vector3[totalVerts];
        Vector3[] back = new Vector3[totalVerts];

        for (int i = 0; i < totalVerts; i++) {
            float angle = MathUtils.PI * i / points - MathUtils.PI / 2;
            float r = (i % 2 == 0) ? outerRadius : innerRadius;
            front[i] = new Vector3(MathUtils.cos(angle) * r, MathUtils.sin(angle) * r, halfDepth);
            back[i] = new Vector3(MathUtils.cos(angle) * r, MathUtils.sin(angle) * r, -halfDepth);
        }

        // Front and back faces
        Vector3 frontCenter = new Vector3(0, 0, halfDepth);
        Vector3 backCenter = new Vector3(0, 0, -halfDepth);

        for (int i = 0; i < totalVerts; i++) {
            int next = (i + 1) % totalVerts;
            addTriangle(builder, frontCenter, front[i], front[next]);
            addTriangle(builder, backCenter, back[next], back[i]);

            // Side edges
            addQuad(builder, front[i], front[next], back[next], back[i]);
        }

        return modelBuilder.end();
    }

    /**
     * Create a wing/fin shape
     */
    public static Model wing(float length, float width, float thickness, float sweep, Material material) {
        modelBuilder.begin();
        MeshPartBuilder builder = modelBuilder.part("wing", GL20.GL_TRIANGLES, VERTEX_ATTR, material);

        float ht = thickness / 2;

        // Wing profile points (top view is triangle with sweep)
        Vector3 root1 = new Vector3(0, ht, 0);
        Vector3 root2 = new Vector3(0, ht, width);
        Vector3 root3 = new Vector3(0, -ht, 0);
        Vector3 root4 = new Vector3(0, -ht, width);

        Vector3 tip1 = new Vector3(length, ht * 0.3f, sweep);
        Vector3 tip2 = new Vector3(length, ht * 0.3f, sweep + width * 0.2f);
        Vector3 tip3 = new Vector3(length, -ht * 0.3f, sweep);
        Vector3 tip4 = new Vector3(length, -ht * 0.3f, sweep + width * 0.2f);

        // Top surface
        addQuad(builder, root1, root2, tip2, tip1);
        // Bottom surface
        addQuad(builder, root4, root3, tip3, tip4);
        // Front edge
        addQuad(builder, root1, tip1, tip3, root3);
        // Back edge
        addQuad(builder, root2, root4, tip4, tip2);
        // Tip
        addQuad(builder, tip1, tip2, tip4, tip3);
        // Root
        addQuad(builder, root2, root1, root3, root4);

        return modelBuilder.end();
    }

    // Helper methods
    private static void addTriangle(MeshPartBuilder builder, Vector3 v1, Vector3 v2, Vector3 v3) {
        Vector3 normal = new Vector3(v2).sub(v1).crs(new Vector3(v3).sub(v1)).nor();
        builder.triangle(
            new MeshPartBuilder.VertexInfo().setPos(v1).setNor(normal).setUV(0, 0),
            new MeshPartBuilder.VertexInfo().setPos(v2).setNor(normal).setUV(1, 0),
            new MeshPartBuilder.VertexInfo().setPos(v3).setNor(normal).setUV(0.5f, 1)
        );
    }

    private static void addQuad(MeshPartBuilder builder, Vector3 v1, Vector3 v2, Vector3 v3, Vector3 v4) {
        Vector3 normal = new Vector3(v2).sub(v1).crs(new Vector3(v4).sub(v1)).nor();
        builder.rect(
            new MeshPartBuilder.VertexInfo().setPos(v1).setNor(normal).setUV(0, 0),
            new MeshPartBuilder.VertexInfo().setPos(v2).setNor(normal).setUV(1, 0),
            new MeshPartBuilder.VertexInfo().setPos(v3).setNor(normal).setUV(1, 1),
            new MeshPartBuilder.VertexInfo().setPos(v4).setNor(normal).setUV(0, 1)
        );
    }
}
