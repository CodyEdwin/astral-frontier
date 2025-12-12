package com.astral.shipbuilding;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;

/**
 * LGMesh - Low-level Geometry Mesh Builder
 * A custom mesh building system for creating ship parts with full control
 * over vertices, normals, UVs, and indices.
 */
public class LGMesh {

    private final FloatArray vertices;
    private final ShortArray indices;
    private final int vertexSize; // floats per vertex
    private short vertexCount;

    // Vertex format: position(3) + normal(3) + uv(2) + color(4) = 12 floats
    private static final int POSITION_OFFSET = 0;
    private static final int NORMAL_OFFSET = 3;
    private static final int UV_OFFSET = 6;
    private static final int COLOR_OFFSET = 8;

    private final Vector3 tempV1 = new Vector3();
    private final Vector3 tempV2 = new Vector3();
    private final Vector3 tempV3 = new Vector3();
    private final Vector3 tempNormal = new Vector3();

    public LGMesh() {
        this.vertices = new FloatArray(1024);
        this.indices = new ShortArray(512);
        this.vertexSize = 12; // pos(3) + normal(3) + uv(2) + color(4)
        this.vertexCount = 0;
    }

    /**
     * Clear all mesh data
     */
    public void clear() {
        vertices.clear();
        indices.clear();
        vertexCount = 0;
    }

    /**
     * Add a vertex with all attributes
     * @return vertex index
     */
    public short addVertex(float x, float y, float z,
                           float nx, float ny, float nz,
                           float u, float v,
                           float r, float g, float b, float a) {
        vertices.add(x, y, z);
        vertices.add(nx, ny, nz);
        vertices.add(u, v);
        vertices.add(r, g, b, a);
        return vertexCount++;
    }

    /**
     * Add a vertex with position, normal, UV (white color)
     */
    public short addVertex(float x, float y, float z,
                           float nx, float ny, float nz,
                           float u, float v) {
        return addVertex(x, y, z, nx, ny, nz, u, v, 1f, 1f, 1f, 1f);
    }

    /**
     * Add a vertex with Vector3 position and normal
     */
    public short addVertex(Vector3 pos, Vector3 normal, float u, float v, Color color) {
        return addVertex(pos.x, pos.y, pos.z,
                        normal.x, normal.y, normal.z,
                        u, v,
                        color.r, color.g, color.b, color.a);
    }

    /**
     * Add a triangle by indices
     */
    public void addTriangle(short i1, short i2, short i3) {
        indices.add(i1);
        indices.add(i2);
        indices.add(i3);
    }

    /**
     * Add a quad (two triangles)
     */
    public void addQuad(short i1, short i2, short i3, short i4) {
        // First triangle
        indices.add(i1);
        indices.add(i2);
        indices.add(i3);
        // Second triangle
        indices.add(i1);
        indices.add(i3);
        indices.add(i4);
    }

    // ============== PRIMITIVE SHAPES ==============

    /**
     * Create a box mesh
     */
    public void addBox(float width, float height, float depth, Color color) {
        addBox(0, 0, 0, width, height, depth, color);
    }

    /**
     * Create a box mesh at position
     */
    public void addBox(float cx, float cy, float cz,
                       float width, float height, float depth, Color color) {
        float hw = width / 2f;
        float hh = height / 2f;
        float hd = depth / 2f;

        // Front face (+Z)
        short v0 = addVertex(cx - hw, cy - hh, cz + hd, 0, 0, 1, 0, 0, color.r, color.g, color.b, color.a);
        short v1 = addVertex(cx + hw, cy - hh, cz + hd, 0, 0, 1, 1, 0, color.r, color.g, color.b, color.a);
        short v2 = addVertex(cx + hw, cy + hh, cz + hd, 0, 0, 1, 1, 1, color.r, color.g, color.b, color.a);
        short v3 = addVertex(cx - hw, cy + hh, cz + hd, 0, 0, 1, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Back face (-Z)
        v0 = addVertex(cx + hw, cy - hh, cz - hd, 0, 0, -1, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx - hw, cy - hh, cz - hd, 0, 0, -1, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx - hw, cy + hh, cz - hd, 0, 0, -1, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx + hw, cy + hh, cz - hd, 0, 0, -1, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Right face (+X)
        v0 = addVertex(cx + hw, cy - hh, cz + hd, 1, 0, 0, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx + hw, cy - hh, cz - hd, 1, 0, 0, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx + hw, cy + hh, cz - hd, 1, 0, 0, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx + hw, cy + hh, cz + hd, 1, 0, 0, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Left face (-X)
        v0 = addVertex(cx - hw, cy - hh, cz - hd, -1, 0, 0, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx - hw, cy - hh, cz + hd, -1, 0, 0, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx - hw, cy + hh, cz + hd, -1, 0, 0, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx - hw, cy + hh, cz - hd, -1, 0, 0, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Top face (+Y)
        v0 = addVertex(cx - hw, cy + hh, cz + hd, 0, 1, 0, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx + hw, cy + hh, cz + hd, 0, 1, 0, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx + hw, cy + hh, cz - hd, 0, 1, 0, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx - hw, cy + hh, cz - hd, 0, 1, 0, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Bottom face (-Y)
        v0 = addVertex(cx - hw, cy - hh, cz - hd, 0, -1, 0, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx + hw, cy - hh, cz - hd, 0, -1, 0, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx + hw, cy - hh, cz + hd, 0, -1, 0, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx - hw, cy - hh, cz + hd, 0, -1, 0, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);
    }

    /**
     * Create a cylinder mesh (Y-axis aligned)
     */
    public void addCylinder(float radius, float height, int segments, Color color) {
        addCylinder(0, 0, 0, radius, height, segments, color);
    }

    /**
     * Create a cylinder at position
     */
    public void addCylinder(float cx, float cy, float cz,
                            float radius, float height, int segments, Color color) {
        float hh = height / 2f;
        short baseIndex = vertexCount;

        // Create ring vertices for top and bottom
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * Math.PI * 2 / segments);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            float u = (float) i / segments;

            // Bottom vertex
            addVertex(cx + radius * cos, cy - hh, cz + radius * sin,
                     cos, 0, sin, u, 0, color.r, color.g, color.b, color.a);
            // Top vertex
            addVertex(cx + radius * cos, cy + hh, cz + radius * sin,
                     cos, 0, sin, u, 1, color.r, color.g, color.b, color.a);
        }

        // Side faces
        for (int i = 0; i < segments; i++) {
            short b0 = (short) (baseIndex + i * 2);
            short t0 = (short) (baseIndex + i * 2 + 1);
            short b1 = (short) (baseIndex + (i + 1) * 2);
            short t1 = (short) (baseIndex + (i + 1) * 2 + 1);
            addQuad(b0, b1, t1, t0);
        }

        // Top cap
        short topCenter = addVertex(cx, cy + hh, cz, 0, 1, 0, 0.5f, 0.5f, color.r, color.g, color.b, color.a);
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);
            short v1 = addVertex(cx + radius * (float)Math.cos(angle1), cy + hh, cz + radius * (float)Math.sin(angle1),
                                0, 1, 0, 0.5f + 0.5f * (float)Math.cos(angle1), 0.5f + 0.5f * (float)Math.sin(angle1),
                                color.r, color.g, color.b, color.a);
            short v2 = addVertex(cx + radius * (float)Math.cos(angle2), cy + hh, cz + radius * (float)Math.sin(angle2),
                                0, 1, 0, 0.5f + 0.5f * (float)Math.cos(angle2), 0.5f + 0.5f * (float)Math.sin(angle2),
                                color.r, color.g, color.b, color.a);
            addTriangle(topCenter, v1, v2);
        }

        // Bottom cap
        short bottomCenter = addVertex(cx, cy - hh, cz, 0, -1, 0, 0.5f, 0.5f, color.r, color.g, color.b, color.a);
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);
            short v1 = addVertex(cx + radius * (float)Math.cos(angle1), cy - hh, cz + radius * (float)Math.sin(angle1),
                                0, -1, 0, 0.5f + 0.5f * (float)Math.cos(angle1), 0.5f + 0.5f * (float)Math.sin(angle1),
                                color.r, color.g, color.b, color.a);
            short v2 = addVertex(cx + radius * (float)Math.cos(angle2), cy - hh, cz + radius * (float)Math.sin(angle2),
                                0, -1, 0, 0.5f + 0.5f * (float)Math.cos(angle2), 0.5f + 0.5f * (float)Math.sin(angle2),
                                color.r, color.g, color.b, color.a);
            addTriangle(bottomCenter, v2, v1);
        }
    }

    /**
     * Create a cone mesh (Y-axis aligned, point at top)
     */
    public void addCone(float radius, float height, int segments, Color color) {
        addCone(0, 0, 0, radius, height, segments, color);
    }

    /**
     * Create a cone at position
     */
    public void addCone(float cx, float cy, float cz,
                        float radius, float height, int segments, Color color) {
        float hh = height / 2f;

        // Apex vertex
        short apex = addVertex(cx, cy + hh, cz, 0, 1, 0, 0.5f, 1, color.r, color.g, color.b, color.a);

        // Base center
        short baseCenter = addVertex(cx, cy - hh, cz, 0, -1, 0, 0.5f, 0.5f, color.r, color.g, color.b, color.a);

        // Create side faces
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * Math.PI * 2 / segments);
            float angle2 = (float) ((i + 1) * Math.PI * 2 / segments);

            float cos1 = (float) Math.cos(angle1);
            float sin1 = (float) Math.sin(angle1);
            float cos2 = (float) Math.cos(angle2);
            float sin2 = (float) Math.sin(angle2);

            // Calculate normals for cone surface
            float nx1 = cos1 * height / (float) Math.sqrt(radius * radius + height * height);
            float ny = radius / (float) Math.sqrt(radius * radius + height * height);
            float nz1 = sin1 * height / (float) Math.sqrt(radius * radius + height * height);
            float nx2 = cos2 * height / (float) Math.sqrt(radius * radius + height * height);
            float nz2 = sin2 * height / (float) Math.sqrt(radius * radius + height * height);

            short v1 = addVertex(cx + radius * cos1, cy - hh, cz + radius * sin1,
                                nx1, ny, nz1, (float)i / segments, 0,
                                color.r, color.g, color.b, color.a);
            short v2 = addVertex(cx + radius * cos2, cy - hh, cz + radius * sin2,
                                nx2, ny, nz2, (float)(i + 1) / segments, 0,
                                color.r, color.g, color.b, color.a);

            addTriangle(apex, v2, v1);

            // Base triangle
            short b1 = addVertex(cx + radius * cos1, cy - hh, cz + radius * sin1,
                                0, -1, 0, 0.5f + 0.5f * cos1, 0.5f + 0.5f * sin1,
                                color.r, color.g, color.b, color.a);
            short b2 = addVertex(cx + radius * cos2, cy - hh, cz + radius * sin2,
                                0, -1, 0, 0.5f + 0.5f * cos2, 0.5f + 0.5f * sin2,
                                color.r, color.g, color.b, color.a);
            addTriangle(baseCenter, b1, b2);
        }
    }

    /**
     * Create a wedge/ramp shape (triangular prism)
     */
    public void addWedge(float width, float height, float depth, Color color) {
        addWedge(0, 0, 0, width, height, depth, color);
    }

    /**
     * Create a wedge at position
     */
    public void addWedge(float cx, float cy, float cz,
                         float width, float height, float depth, Color color) {
        float hw = width / 2f;
        float hh = height / 2f;
        float hd = depth / 2f;

        // Calculate slope normal
        float slopeLen = (float) Math.sqrt(height * height + depth * depth);
        float slopeNy = depth / slopeLen;
        float slopeNz = height / slopeLen;

        // Left triangle face
        short v0 = addVertex(cx - hw, cy - hh, cz - hd, -1, 0, 0, 0, 0, color.r, color.g, color.b, color.a);
        short v1 = addVertex(cx - hw, cy - hh, cz + hd, -1, 0, 0, 1, 0, color.r, color.g, color.b, color.a);
        short v2 = addVertex(cx - hw, cy + hh, cz - hd, -1, 0, 0, 0, 1, color.r, color.g, color.b, color.a);
        addTriangle(v0, v1, v2);

        // Right triangle face
        v0 = addVertex(cx + hw, cy - hh, cz + hd, 1, 0, 0, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx + hw, cy - hh, cz - hd, 1, 0, 0, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx + hw, cy + hh, cz - hd, 1, 0, 0, 1, 1, color.r, color.g, color.b, color.a);
        addTriangle(v0, v1, v2);

        // Bottom face
        v0 = addVertex(cx - hw, cy - hh, cz - hd, 0, -1, 0, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx + hw, cy - hh, cz - hd, 0, -1, 0, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx + hw, cy - hh, cz + hd, 0, -1, 0, 1, 1, color.r, color.g, color.b, color.a);
        short v3 = addVertex(cx - hw, cy - hh, cz + hd, 0, -1, 0, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Back face (vertical)
        v0 = addVertex(cx - hw, cy - hh, cz - hd, 0, 0, -1, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx - hw, cy + hh, cz - hd, 0, 0, -1, 0, 1, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx + hw, cy + hh, cz - hd, 0, 0, -1, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx + hw, cy - hh, cz - hd, 0, 0, -1, 1, 0, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Slope face
        v0 = addVertex(cx - hw, cy - hh, cz + hd, 0, slopeNy, slopeNz, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx + hw, cy - hh, cz + hd, 0, slopeNy, slopeNz, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx + hw, cy + hh, cz - hd, 0, slopeNy, slopeNz, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx - hw, cy + hh, cz - hd, 0, slopeNy, slopeNz, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);
    }

    /**
     * Create a hexagonal prism (for engine nacelles, etc.)
     */
    public void addHexPrism(float radius, float length, Color color) {
        addHexPrism(0, 0, 0, radius, length, color);
    }

    /**
     * Create a hexagonal prism at position
     */
    public void addHexPrism(float cx, float cy, float cz,
                            float radius, float length, Color color) {
        addCylinder(cx, cy, cz, radius, length, 6, color);
    }

    /**
     * Create a tapered hull section (for ship fuselage)
     */
    public void addTaperedHull(float frontWidth, float frontHeight,
                               float backWidth, float backHeight,
                               float length, Color color) {
        addTaperedHull(0, 0, 0, frontWidth, frontHeight, backWidth, backHeight, length, color);
    }

    /**
     * Create a tapered hull at position
     */
    public void addTaperedHull(float cx, float cy, float cz,
                               float frontWidth, float frontHeight,
                               float backWidth, float backHeight,
                               float length, Color color) {
        float hl = length / 2f;
        float fhw = frontWidth / 2f;
        float fhh = frontHeight / 2f;
        float bhw = backWidth / 2f;
        float bhh = backHeight / 2f;

        // Front face
        short v0 = addVertex(cx - fhw, cy - fhh, cz + hl, 0, 0, 1, 0, 0, color.r, color.g, color.b, color.a);
        short v1 = addVertex(cx + fhw, cy - fhh, cz + hl, 0, 0, 1, 1, 0, color.r, color.g, color.b, color.a);
        short v2 = addVertex(cx + fhw, cy + fhh, cz + hl, 0, 0, 1, 1, 1, color.r, color.g, color.b, color.a);
        short v3 = addVertex(cx - fhw, cy + fhh, cz + hl, 0, 0, 1, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Back face
        v0 = addVertex(cx + bhw, cy - bhh, cz - hl, 0, 0, -1, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx - bhw, cy - bhh, cz - hl, 0, 0, -1, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx - bhw, cy + bhh, cz - hl, 0, 0, -1, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx + bhw, cy + bhh, cz - hl, 0, 0, -1, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Calculate normals for angled sides
        float rightNx = length / (float) Math.sqrt(length * length + (bhw - fhw) * (bhw - fhw));
        float rightNz = (fhw - bhw) / (float) Math.sqrt(length * length + (bhw - fhw) * (bhw - fhw));

        // Right face
        v0 = addVertex(cx + fhw, cy - fhh, cz + hl, rightNx, 0, rightNz, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx + bhw, cy - bhh, cz - hl, rightNx, 0, rightNz, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx + bhw, cy + bhh, cz - hl, rightNx, 0, rightNz, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx + fhw, cy + fhh, cz + hl, rightNx, 0, rightNz, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Left face
        v0 = addVertex(cx - bhw, cy - bhh, cz - hl, -rightNx, 0, rightNz, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx - fhw, cy - fhh, cz + hl, -rightNx, 0, rightNz, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx - fhw, cy + fhh, cz + hl, -rightNx, 0, rightNz, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx - bhw, cy + bhh, cz - hl, -rightNx, 0, rightNz, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Top face
        float topNy = length / (float) Math.sqrt(length * length + (bhh - fhh) * (bhh - fhh));
        float topNz = (fhh - bhh) / (float) Math.sqrt(length * length + (bhh - fhh) * (bhh - fhh));
        v0 = addVertex(cx - fhw, cy + fhh, cz + hl, 0, topNy, topNz, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx + fhw, cy + fhh, cz + hl, 0, topNy, topNz, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx + bhw, cy + bhh, cz - hl, 0, topNy, topNz, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx - bhw, cy + bhh, cz - hl, 0, topNy, topNz, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);

        // Bottom face
        v0 = addVertex(cx - bhw, cy - bhh, cz - hl, 0, -topNy, topNz, 0, 0, color.r, color.g, color.b, color.a);
        v1 = addVertex(cx + bhw, cy - bhh, cz - hl, 0, -topNy, topNz, 1, 0, color.r, color.g, color.b, color.a);
        v2 = addVertex(cx + fhw, cy - fhh, cz + hl, 0, -topNy, topNz, 1, 1, color.r, color.g, color.b, color.a);
        v3 = addVertex(cx - fhw, cy - fhh, cz + hl, 0, -topNy, topNz, 0, 1, color.r, color.g, color.b, color.a);
        addQuad(v0, v1, v2, v3);
    }

    /**
     * Transform all vertices by a matrix
     */
    public void transform(Matrix4 matrix) {
        Vector3 pos = new Vector3();
        Vector3 normal = new Vector3();

        for (int i = 0; i < vertexCount; i++) {
            int offset = i * vertexSize;

            // Transform position
            pos.set(vertices.get(offset), vertices.get(offset + 1), vertices.get(offset + 2));
            pos.mul(matrix);
            vertices.set(offset, pos.x);
            vertices.set(offset + 1, pos.y);
            vertices.set(offset + 2, pos.z);

            // Transform normal (rotation only)
            normal.set(vertices.get(offset + 3), vertices.get(offset + 4), vertices.get(offset + 5));
            normal.rot(matrix).nor();
            vertices.set(offset + 3, normal.x);
            vertices.set(offset + 4, normal.y);
            vertices.set(offset + 5, normal.z);
        }
    }

    /**
     * Append another LGMesh to this one
     */
    public void append(LGMesh other) {
        short indexOffset = vertexCount;

        // Copy vertices
        for (int i = 0; i < other.vertices.size; i++) {
            vertices.add(other.vertices.get(i));
        }
        vertexCount += other.vertexCount;

        // Copy indices with offset
        for (int i = 0; i < other.indices.size; i++) {
            indices.add((short) (other.indices.get(i) + indexOffset));
        }
    }

    /**
     * Build a LibGDX Mesh from the current data
     */
    public Mesh buildMesh() {
        Mesh mesh = new Mesh(true, vertexCount, indices.size,
            new VertexAttribute(VertexAttributes.Usage.Position, 3, "a_position"),
            new VertexAttribute(VertexAttributes.Usage.Normal, 3, "a_normal"),
            new VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, "a_texCoord0"),
            new VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_color")
        );

        // Convert color from 4 floats to packed format
        float[] packedVerts = new float[vertexCount * 9]; // pos(3) + normal(3) + uv(2) + packedColor(1)
        for (int i = 0; i < vertexCount; i++) {
            int srcOffset = i * vertexSize;
            int dstOffset = i * 9;

            // Position
            packedVerts[dstOffset] = vertices.get(srcOffset);
            packedVerts[dstOffset + 1] = vertices.get(srcOffset + 1);
            packedVerts[dstOffset + 2] = vertices.get(srcOffset + 2);
            // Normal
            packedVerts[dstOffset + 3] = vertices.get(srcOffset + 3);
            packedVerts[dstOffset + 4] = vertices.get(srcOffset + 4);
            packedVerts[dstOffset + 5] = vertices.get(srcOffset + 5);
            // UV
            packedVerts[dstOffset + 6] = vertices.get(srcOffset + 6);
            packedVerts[dstOffset + 7] = vertices.get(srcOffset + 7);
            // Packed color
            Color c = new Color(
                vertices.get(srcOffset + 8),
                vertices.get(srcOffset + 9),
                vertices.get(srcOffset + 10),
                vertices.get(srcOffset + 11)
            );
            packedVerts[dstOffset + 8] = c.toFloatBits();
        }

        mesh.setVertices(packedVerts);
        mesh.setIndices(indices.toArray());

        return mesh;
    }

    /**
     * Build a Model from the current mesh data
     */
    public Model buildModel(Material material, String partId) {
        Mesh mesh = buildMesh();

        Model model = new Model();
        MeshPart meshPart = new MeshPart();
        meshPart.id = partId;
        meshPart.mesh = mesh;
        meshPart.offset = 0;
        meshPart.size = indices.size;
        meshPart.primitiveType = GL20.GL_TRIANGLES;

        Node node = new Node();
        node.id = "node_" + partId;
        NodePart nodePart = new NodePart();
        nodePart.meshPart = meshPart;
        nodePart.material = material;
        node.parts.add(nodePart);

        model.nodes.add(node);
        model.meshes.add(mesh);
        model.meshParts.add(meshPart);
        model.materials.add(material);
        model.manageDisposable(mesh);

        return model;
    }

    // Getters
    public int getVertexCount() { return vertexCount; }
    public int getIndexCount() { return indices.size; }
}
