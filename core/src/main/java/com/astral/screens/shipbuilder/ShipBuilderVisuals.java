package com.astral.screens.shipbuilder;

import com.astral.shipbuilding.ShipBuilder;
import com.astral.shipbuilding.ShipPart;
import com.astral.shipbuilding.ShipPartType;
import com.astral.shipbuilding.ShipValidator;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * ShipBuilderVisuals - Helper class for rendering ship builder visual feedback.
 * Includes snap points, validation indicators, ghost parts, and connection lines.
 */
public class ShipBuilderVisuals {

    private final ShapeRenderer shapeRenderer;

    // Colors for visual feedback
    private final Color validColor = new Color(0.2f, 0.8f, 0.3f, 0.8f);
    private final Color invalidColor = new Color(0.9f, 0.2f, 0.2f, 0.8f);
    private final Color warningColor = new Color(0.9f, 0.7f, 0.2f, 0.8f);
    private final Color snapPointColor = new Color(0.3f, 0.6f, 1.0f, 0.7f);
    private final Color connectionLineColor = new Color(0.4f, 0.7f, 1.0f, 0.5f);
    private final Color ghostValidColor = new Color(0.2f, 0.8f, 0.3f, 0.4f);
    private final Color ghostInvalidColor = new Color(0.9f, 0.2f, 0.2f, 0.4f);

    private boolean showSnapPoints = true;
    private boolean showConnectionLines = true;
    private boolean showValidationIndicators = true;

    public ShipBuilderVisuals() {
        this.shapeRenderer = new ShapeRenderer();
        shapeRenderer.setAutoShapeType(true);
    }

    /**
     * Render snap points in 3D space
     */
    public void renderSnapPoints(
        ShipBuilder shipBuilder,
        PerspectiveCamera camera
    ) {
        if (!showSnapPoints) return;

        Array<ShipBuilder.SnapPoint> snapPoints = shipBuilder.getSnapPoints();
        if (snapPoints.size == 0) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDepthMask(false);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (ShipBuilder.SnapPoint snap : snapPoints) {
            // Draw snap point as a larger sphere for visibility
            shapeRenderer.setColor(snapPointColor);
            drawSphere(snap.position, 0.3f, 12);

            // Draw direction indicator (longer)
            Vector3 endPoint = new Vector3(snap.direction)
                .scl(1.0f)
                .add(snap.position);
            shapeRenderer.setColor(
                snapPointColor.r,
                snapPointColor.g,
                snapPointColor.b,
                0.9f
            );
            shapeRenderer.line(snap.position, endPoint);

            // Draw arrow head
            Vector3 perpendicular1 = new Vector3();
            Vector3 perpendicular2 = new Vector3();
            perpendicular1.set(snap.direction).crs(0, 1, 0);
            if (perpendicular1.len2() < 0.01f) {
                perpendicular1.set(snap.direction).crs(1, 0, 0);
            }
            perpendicular1.nor().scl(0.1f);
            perpendicular2
                .set(snap.direction)
                .crs(perpendicular1)
                .nor()
                .scl(0.1f);

            Vector3 arrowBase = new Vector3(snap.direction)
                .scl(0.8f)
                .add(snap.position);
            Vector3 arrowTip1 = new Vector3(arrowBase).add(perpendicular1);
            Vector3 arrowTip2 = new Vector3(arrowBase).sub(perpendicular1);

            shapeRenderer.line(endPoint, arrowTip1);
            shapeRenderer.line(endPoint, arrowTip2);
        }

        shapeRenderer.end();

        Gdx.gl.glDepthMask(true);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Render connection lines between connected parts
     */
    public void renderConnectionLines(
        ShipBuilder shipBuilder,
        PerspectiveCamera camera
    ) {
        if (!showConnectionLines) return;

        Array<ShipPart> parts = shipBuilder.getParts();
        if (parts.size < 2) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDepthMask(false);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(connectionLineColor);

        // Draw lines between parts that are close (connected)
        for (int i = 0; i < parts.size; i++) {
            ShipPart part1 = parts.get(i);
            Vector3 pos1 = part1.getPosition();

            for (int j = i + 1; j < parts.size; j++) {
                ShipPart part2 = parts.get(j);
                Vector3 pos2 = part2.getPosition();

                float distance = pos1.dst(pos2);
                if (distance < 3.0f) {
                    // Draw connection line with thickness based on distance
                    float alpha = Math.max(0.1f, 1.0f - (distance / 3.0f));
                    shapeRenderer.setColor(
                        connectionLineColor.r,
                        connectionLineColor.g,
                        connectionLineColor.b,
                        alpha * 0.5f
                    );
                    drawThickLine(pos1, pos2, 0.05f);
                }
            }
        }

        shapeRenderer.end();

        Gdx.gl.glDepthMask(true);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Render validation indicators on parts with issues
     */
    public void renderValidationIndicators(
        ShipBuilder shipBuilder,
        PerspectiveCamera camera
    ) {
        if (!showValidationIndicators) return;

        Array<ShipValidator.ValidationError> errors = shipBuilder.getErrors();
        Array<ShipValidator.ValidationWarning> warnings =
            shipBuilder.getWarnings();

        if (errors.size == 0 && warnings.size == 0) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDepthMask(false);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Array<ShipPart> parts = shipBuilder.getParts();

        // Highlight parts related to errors
        if (errors.size > 0) {
            for (ShipPart part : parts) {
                // Check if this part is relevant to any error
                boolean hasError = isPartRelevantToErrors(part, errors);
                if (hasError) {
                    drawPartIndicator(part.getPosition(), invalidColor, 0.3f);
                }
            }
        }

        // Highlight parts with warnings (only if no errors)
        if (errors.size == 0 && warnings.size > 0) {
            for (ShipPart part : parts) {
                boolean hasWarning = isPartRelevantToWarnings(part, warnings);
                if (hasWarning) {
                    drawPartIndicator(part.getPosition(), warningColor, 0.25f);
                }
            }
        }

        shapeRenderer.end();

        Gdx.gl.glDepthMask(true);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Render a ghost part preview at the given position
     */
    public void renderGhostPart(
        Vector3 position,
        ShipPartType type,
        boolean isValid,
        PerspectiveCamera camera
    ) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glDepthMask(false);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        Color ghostColor = isValid ? ghostValidColor : ghostInvalidColor;
        shapeRenderer.setColor(ghostColor);

        // Draw a simple placeholder based on part type
        float size = getPartSize(type);
        drawBox(position, size, size * 0.6f, size * 1.5f);

        shapeRenderer.end();

        Gdx.gl.glDepthMask(true);
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    /**
     * Render validation summary in 2D UI space
     */
    public void renderValidationSummary(
        ShipBuilder shipBuilder,
        SpriteBatch batch,
        BitmapFont font,
        float x,
        float y
    ) {
        String summary = shipBuilder.getValidationSummary();
        Array<ShipValidator.ValidationError> errors = shipBuilder.getErrors();
        Array<ShipValidator.ValidationWarning> warnings =
            shipBuilder.getWarnings();

        batch.begin();

        // Draw summary with appropriate color
        if (errors.size > 0) {
            font.setColor(invalidColor);
        } else if (warnings.size > 0) {
            font.setColor(warningColor);
        } else {
            font.setColor(validColor);
        }

        font.draw(batch, summary, x, y);
        font.setColor(Color.WHITE);

        batch.end();
    }

    /**
     * Render detailed validation messages
     */
    public void renderValidationDetails(
        ShipBuilder shipBuilder,
        SpriteBatch batch,
        BitmapFont font,
        float x,
        float y,
        float maxWidth
    ) {
        Array<ShipValidator.ValidationError> errors = shipBuilder.getErrors();
        Array<ShipValidator.ValidationWarning> warnings =
            shipBuilder.getWarnings();

        batch.begin();

        float currentY = y;
        float lineHeight = font.getLineHeight() * 1.2f;

        // Draw errors
        if (errors.size > 0) {
            font.setColor(invalidColor);
            font.draw(batch, "ERRORS:", x, currentY);
            currentY -= lineHeight;

            font.setColor(Color.WHITE);
            for (ShipValidator.ValidationError error : errors) {
                String text = "• " + error.type.title;
                font.draw(batch, text, x + 10, currentY);
                currentY -= lineHeight;

                // Limit number of displayed errors
                if (currentY < y - 200) {
                    font.draw(
                        batch,
                        "... and " +
                            (errors.size - errors.indexOf(error, true) - 1) +
                            " more",
                        x + 10,
                        currentY
                    );
                    break;
                }
            }
        }

        // Draw warnings
        if (warnings.size > 0) {
            currentY -= lineHeight * 0.5f;
            font.setColor(warningColor);
            font.draw(batch, "WARNINGS:", x, currentY);
            currentY -= lineHeight;

            font.setColor(Color.WHITE);
            for (ShipValidator.ValidationWarning warning : warnings) {
                String text = "• " + warning.type.title;
                font.draw(batch, text, x + 10, currentY);
                currentY -= lineHeight;

                // Limit number of displayed warnings
                if (currentY < y - 400) {
                    font.draw(
                        batch,
                        "... and " +
                            (warnings.size -
                                warnings.indexOf(warning, true) -
                                1) +
                            " more",
                        x + 10,
                        currentY
                    );
                    break;
                }
            }
        }

        font.setColor(Color.WHITE);
        batch.end();
    }

    // ============== Helper Methods ==============

    /**
     * Check if a part is relevant to any validation errors
     */
    private boolean isPartRelevantToErrors(
        ShipPart part,
        Array<ShipValidator.ValidationError> errors
    ) {
        for (ShipValidator.ValidationError error : errors) {
            switch (error.type) {
                case MISSING_COCKPIT:
                case MULTIPLE_COCKPITS:
                    if (
                        part.getType() == ShipPartType.HULL_COCKPIT
                    ) return true;
                    break;
                case MISSING_ENGINE:
                case INSUFFICIENT_ENGINE_THRUST:
                    if (
                        part.getType().getCategory() ==
                        ShipPartType.PartCategory.ENGINE
                    ) return true;
                    break;
                case MISSING_FUEL_TANK:
                    if (
                        part.getType() == ShipPartType.UTIL_FUEL_TANK
                    ) return true;
                    break;
                case MULTIPLE_SHIELD_GENERATORS:
                    if (
                        part.getType() == ShipPartType.UTIL_SHIELD_GENERATOR
                    ) return true;
                    break;
            }
        }
        return false;
    }

    /**
     * Check if a part is relevant to any validation warnings
     */
    private boolean isPartRelevantToWarnings(
        ShipPart part,
        Array<ShipValidator.ValidationWarning> warnings
    ) {
        for (ShipValidator.ValidationWarning warning : warnings) {
            switch (warning.type) {
                case NO_WEAPONS:
                    if (
                        part.getType().getCategory() ==
                        ShipPartType.PartCategory.WEAPON
                    ) return true;
                    break;
                case NO_SHIELDS:
                    if (
                        part.getType() == ShipPartType.UTIL_SHIELD_GENERATOR
                    ) return true;
                    break;
                case LOW_FUEL_CAPACITY:
                    if (
                        part.getType() == ShipPartType.UTIL_FUEL_TANK
                    ) return true;
                    break;
            }
        }
        return false;
    }

    /**
     * Draw a validation indicator around a part
     */
    private void drawPartIndicator(
        Vector3 position,
        Color color,
        float radius
    ) {
        // Pulsing effect
        float time = System.currentTimeMillis() / 1000.0f;
        float pulse = 0.5f + 0.5f * (float) Math.sin(time * 3.0f);
        float actualRadius = radius + pulse * 0.15f;

        shapeRenderer.setColor(color.r, color.g, color.b, color.a * pulse);
        drawSphere(position, actualRadius, 12);
    }

    /**
     * Draw a simple sphere using line approximation
     */
    private void drawSphere(Vector3 center, float radius, int segments) {
        // Draw circles in multiple planes to approximate a sphere
        for (int i = 0; i < segments; i++) {
            float angle1 = ((float) i / segments) * 360;
            float angle2 = ((float) (i + 1) / segments) * 360;

            // Draw circle in XY plane
            float x1 =
                center.x + radius * (float) Math.cos(Math.toRadians(angle1));
            float y1 =
                center.y + radius * (float) Math.sin(Math.toRadians(angle1));
            float x2 =
                center.x + radius * (float) Math.cos(Math.toRadians(angle2));
            float y2 =
                center.y + radius * (float) Math.sin(Math.toRadians(angle2));

            shapeRenderer.line(x1, y1, center.z, x2, y2, center.z);

            // Draw circle in XZ plane
            float z1 =
                center.z + radius * (float) Math.sin(Math.toRadians(angle1));
            float z2 =
                center.z + radius * (float) Math.sin(Math.toRadians(angle2));

            shapeRenderer.line(x1, center.y, z1, x2, center.y, z2);

            // Draw circle in YZ plane
            shapeRenderer.line(center.x, y1, z1, center.x, y2, z2);
        }
    }

    /**
     * Draw a thick line using simple line rendering
     */
    private void drawThickLine(Vector3 start, Vector3 end, float thickness) {
        // ShapeRenderer doesn't support 3D thick lines directly, use regular line
        shapeRenderer.line(start, end);
    }

    /**
     * Draw a simple box
     */
    private void drawBox(
        Vector3 center,
        float width,
        float height,
        float depth
    ) {
        // Draw a simplified box representation using lines
        float hw = width / 2;
        float hh = height / 2;
        float hd = depth / 2;

        // Draw box edges
        Vector3 v000 = new Vector3(center.x - hw, center.y - hh, center.z - hd);
        Vector3 v001 = new Vector3(center.x - hw, center.y - hh, center.z + hd);
        Vector3 v010 = new Vector3(center.x - hw, center.y + hh, center.z - hd);
        Vector3 v011 = new Vector3(center.x - hw, center.y + hh, center.z + hd);
        Vector3 v100 = new Vector3(center.x + hw, center.y - hh, center.z - hd);
        Vector3 v101 = new Vector3(center.x + hw, center.y - hh, center.z + hd);
        Vector3 v110 = new Vector3(center.x + hw, center.y + hh, center.z - hd);
        Vector3 v111 = new Vector3(center.x + hw, center.y + hh, center.z + hd);

        // Bottom face
        shapeRenderer.line(v000, v001);
        shapeRenderer.line(v001, v101);
        shapeRenderer.line(v101, v100);
        shapeRenderer.line(v100, v000);

        // Top face
        shapeRenderer.line(v010, v011);
        shapeRenderer.line(v011, v111);
        shapeRenderer.line(v111, v110);
        shapeRenderer.line(v110, v010);

        // Vertical edges
        shapeRenderer.line(v000, v010);
        shapeRenderer.line(v001, v011);
        shapeRenderer.line(v100, v110);
        shapeRenderer.line(v101, v111);
    }

    /**
     * Get approximate size for a part type
     */
    private float getPartSize(ShipPartType type) {
        switch (type.getCategory()) {
            case HULL:
                return 2.0f;
            case WING:
                return 1.5f;
            case ENGINE:
                if (type == ShipPartType.ENGINE_LARGE) return 1.8f;
                if (type == ShipPartType.ENGINE_MEDIUM) return 1.2f;
                return 0.8f;
            case WEAPON:
                return 0.6f;
            case UTILITY:
                return 1.0f;
            case STRUCTURAL:
                return 0.5f;
            case DECORATIVE:
                return 0.3f;
            default:
                return 1.0f;
        }
    }

    // ============== Setters ==============

    public void setShowSnapPoints(boolean show) {
        this.showSnapPoints = show;
    }

    public void setShowConnectionLines(boolean show) {
        this.showConnectionLines = show;
    }

    public void setShowValidationIndicators(boolean show) {
        this.showValidationIndicators = show;
    }

    public boolean isShowSnapPoints() {
        return showSnapPoints;
    }

    public boolean isShowConnectionLines() {
        return showConnectionLines;
    }

    public boolean isShowValidationIndicators() {
        return showValidationIndicators;
    }

    public void dispose() {
        shapeRenderer.dispose();
    }
}
