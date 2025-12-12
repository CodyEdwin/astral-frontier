package com.astral.shipbuilding;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * ShipBuilder - Assembles ship parts into complete ships.
 * Provides both preset ship configurations and custom ship building.
 */
public class ShipBuilder implements Disposable {

    private final ShipPartMeshFactory meshFactory;
    private final Array<ShipPart> parts;
    private final ShipValidator validator;

    private Color primaryColor;
    private Color secondaryColor;
    private Color accentColor;

    // Ship statistics
    private float totalMass;
    private float totalHull;
    private float totalShield;
    private float totalThrust;
    private float totalFuel;

    // Validation state
    private boolean validationDirty = true;
    private boolean isValid = false;

    // Snap points for precise part placement
    private final Array<SnapPoint> snapPoints;
    private boolean enableSnapPoints = true;
    private float snapDistance = 1.0f;

    public ShipBuilder() {
        this.meshFactory = new ShipPartMeshFactory();
        this.parts = new Array<>();
        this.validator = new ShipValidator(this);
        this.snapPoints = new Array<>();
        this.primaryColor = new Color(0.5f, 0.5f, 0.55f, 1f);
        this.secondaryColor = new Color(0.3f, 0.3f, 0.35f, 1f);
        this.accentColor = new Color(0.2f, 0.6f, 0.9f, 1f);
    }

    /**
     * Set the ship's color scheme
     */
    public ShipBuilder setColors(Color primary, Color secondary, Color accent) {
        this.primaryColor = primary;
        this.secondaryColor = secondary;
        this.accentColor = accent;
        return this;
    }

    /**
     * Clear all parts
     */
    public ShipBuilder clear() {
        parts.clear();
        snapPoints.clear();
        totalMass = 0;
        totalHull = 0;
        totalShield = 0;
        totalThrust = 0;
        totalFuel = 0;
        validationDirty = true;
        return this;
    }

    /**
     * Add a part at a specific position
     */
    public ShipBuilder addPart(ShipPartType type, float x, float y, float z) {
        return addPart(type, x, y, z, 0, 0, 0, 1f, false);
    }

    /**
     * Add a part with full transform
     */
    public ShipBuilder addPart(
        ShipPartType type,
        float x,
        float y,
        float z,
        float yaw,
        float pitch,
        float roll,
        float scale,
        boolean mirrored
    ) {
        ShipPart part = new ShipPart(type);
        part.setPrimaryColor(primaryColor);
        part.setSecondaryColor(secondaryColor);

        // Set transform properties BEFORE generating snap points
        part.setMirrored(mirrored);
        part.setScale(scale);
        part.setRotation(yaw, pitch, roll);
        part.setPosition(x, y, z);

        Model model = meshFactory.createPartModel(
            type,
            primaryColor,
            secondaryColor,
            0
        );
        part.setModel(model);

        parts.add(part);
        updateStats(part);

        // Generate snap points AFTER all transforms are set
        generateSnapPointsForPart(part);
        validationDirty = true;

        return this;
    }

    /**
     * Add a mirrored pair of parts (left and right)
     */
    public ShipBuilder addMirroredPair(
        ShipPartType type,
        float x,
        float y,
        float z,
        float yaw,
        float pitch,
        float roll,
        float scale
    ) {
        // Right side
        addPart(type, x, y, z, yaw, pitch, roll, scale, false);
        // Left side (mirrored X position)
        addPart(type, -x, y, z, -yaw, pitch, -roll, scale, true);
        return this;
    }

    /**
     * Remove a part from the ship
     */
    public ShipBuilder removePart(ShipPart part) {
        if (parts.removeValue(part, true)) {
            totalMass -= part.getMassContribution();
            totalHull -= part.getHullContribution();
            totalShield -= part.getShieldContribution();
            totalThrust -= part.getThrustContribution();
            totalFuel -= part.getFuelCapacity();
            validationDirty = true;
            regenerateSnapPoints();
        }
        return this;
    }

    /**
     * Update ship statistics from a part
     */
    private void updateStats(ShipPart part) {
        totalMass += part.getMassContribution();
        totalHull += part.getHullContribution();
        totalShield += part.getShieldContribution();
        totalThrust += part.getThrustContribution();
        totalFuel += part.getFuelCapacity();
    }

    /**
     * Generate snap points for a newly added part
     */
    private void generateSnapPointsForPart(ShipPart part) {
        Vector3 pos = part.getPosition();
        float scale = part.getScale().x; // Assume uniform scale

        // Generate snap points directly in world space based on part position
        switch (part.getType().getCategory()) {
            case HULL:
                // Front, back, left, right, top, bottom
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x, pos.y, pos.z + 2 * scale),
                        new Vector3(0, 0, 1),
                        part,
                        "front"
                    )
                );
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x, pos.y, pos.z - 2 * scale),
                        new Vector3(0, 0, -1),
                        part,
                        "back"
                    )
                );
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x - 1.5f * scale, pos.y, pos.z),
                        new Vector3(-1, 0, 0),
                        part,
                        "left"
                    )
                );
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x + 1.5f * scale, pos.y, pos.z),
                        new Vector3(1, 0, 0),
                        part,
                        "right"
                    )
                );
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x, pos.y + 1 * scale, pos.z),
                        new Vector3(0, 1, 0),
                        part,
                        "top"
                    )
                );
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x, pos.y - 1 * scale, pos.z),
                        new Vector3(0, -1, 0),
                        part,
                        "bottom"
                    )
                );
                break;
            case WING:
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x, pos.y, pos.z),
                        new Vector3(-1, 0, 0),
                        part,
                        "root"
                    )
                );
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x + 2 * scale, pos.y, pos.z),
                        new Vector3(1, 0, 0),
                        part,
                        "tip"
                    )
                );
                break;
            case ENGINE:
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x, pos.y, pos.z + 0.5f * scale),
                        new Vector3(0, 0, 1),
                        part,
                        "mount"
                    )
                );
                break;
            case WEAPON:
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x, pos.y, pos.z + 0.2f * scale),
                        new Vector3(0, 0, 1),
                        part,
                        "mount"
                    )
                );
                break;
            case UTILITY:
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x, pos.y + 0.5f * scale, pos.z),
                        new Vector3(0, 1, 0),
                        part,
                        "mount"
                    )
                );
                break;
            case STRUCTURAL:
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x, pos.y, pos.z + 0.5f * scale),
                        new Vector3(0, 0, 1),
                        part,
                        "front"
                    )
                );
                snapPoints.add(
                    new SnapPoint(
                        new Vector3(pos.x, pos.y, pos.z - 0.5f * scale),
                        new Vector3(0, 0, -1),
                        part,
                        "back"
                    )
                );
                break;
            case DECORATIVE:
                // Decorative parts don't need snap points
                break;
        }
    }

    /**
     * Regenerate all snap points (call after parts move/rotate)
     */
    public void regenerateSnapPoints() {
        snapPoints.clear();
        for (ShipPart part : parts) {
            generateSnapPointsForPart(part);
        }
    }

    /**
     * Find the nearest snap point to a given position
     */
    public SnapPoint findNearestSnapPoint(Vector3 position) {
        if (!enableSnapPoints || snapPoints.size == 0) {
            return null;
        }

        SnapPoint nearest = null;
        float nearestDist = snapDistance;

        for (SnapPoint snap : snapPoints) {
            float dist = snap.position.dst(position);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = snap;
            }
        }

        return nearest;
    }

    /**
     * Check if a part placement at the given position is valid
     */
    public boolean isValidPlacement(ShipPartType type, Vector3 position) {
        // Check if too close to existing parts (collision detection)
        for (ShipPart existingPart : parts) {
            float minDist = 0.5f; // Minimum separation
            if (existingPart.getPosition().dst(position) < minDist) {
                // Allow overlap only if snapping to attachment point
                SnapPoint snap = findNearestSnapPoint(position);
                if (snap == null || snap.position.dst(position) > 0.1f) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Get all model instances for rendering
     */
    public Array<ModelInstance> getModelInstances() {
        Array<ModelInstance> instances = new Array<>();
        for (ShipPart part : parts) {
            if (part.getModelInstance() != null) {
                instances.add(part.getModelInstance());
            }
        }
        return instances;
    }

    /**
     * Get all parts
     */
    public Array<ShipPart> getParts() {
        return parts;
    }

    // ============== Preset Ship Builders ==============

    /**
     * Build a small fighter ship
     */
    public ShipBuilder buildFighter() {
        clear();

        // Cockpit and nose
        addPart(ShipPartType.HULL_COCKPIT, 0, 0, 2.5f);
        addPart(ShipPartType.HULL_NOSE, 0, 0, 4.0f, 0, 90, 0, 1f, false);

        // Main hull
        addPart(ShipPartType.HULL_FORWARD, 0, 0, 1.0f);
        addPart(ShipPartType.HULL_MID, 0, 0, -1.5f);
        addPart(ShipPartType.HULL_AFT, 0, 0, -4.0f);

        // Wings
        addMirroredPair(ShipPartType.WING_SWEPT, 2.0f, 0, -1.0f, 0, 0, 0, 1f);

        // Engines
        addMirroredPair(ShipPartType.ENGINE_SMALL, 0.8f, 0, -5.5f, 0, 0, 0, 1f);

        // Weapons
        addMirroredPair(
            ShipPartType.WEAPON_MOUNT_SMALL,
            1.5f,
            -0.2f,
            1.0f,
            0,
            0,
            0,
            1f
        );

        // Stabilizers
        addPart(ShipPartType.STRUCT_FIN, 0, 0.5f, -4.5f, 0, 0, 0, 1f, false);

        return this;
    }

    /**
     * Build a medium interceptor ship
     */
    public ShipBuilder buildInterceptor() {
        clear();

        // Cockpit
        addPart(ShipPartType.HULL_COCKPIT, 0, 0.2f, 3.0f);
        addPart(ShipPartType.HULL_NOSE, 0, 0, 4.5f, 0, 90, 0, 0.8f, false);

        // Hull sections
        addPart(ShipPartType.HULL_FORWARD, 0, 0, 1.5f);
        addPart(ShipPartType.HULL_MID, 0, 0, -1.0f);
        addPart(ShipPartType.HULL_AFT, 0, 0, -3.5f);
        addPart(ShipPartType.HULL_TAIL, 0, 0, -5.5f);

        // Delta wings
        addMirroredPair(
            ShipPartType.WING_DELTA,
            2.5f,
            -0.1f,
            -1.5f,
            0,
            0,
            0,
            1.2f
        );

        // Twin engines
        addMirroredPair(
            ShipPartType.ENGINE_MEDIUM,
            1.0f,
            0,
            -6.5f,
            0,
            0,
            0,
            1f
        );

        // Weapons
        addMirroredPair(
            ShipPartType.WEAPON_MOUNT_MEDIUM,
            2.0f,
            -0.3f,
            0.5f,
            0,
            0,
            0,
            1f
        );
        addPart(ShipPartType.WEAPON_MOUNT_SMALL, 0, -0.4f, 2.0f);

        // Utilities
        addPart(ShipPartType.UTIL_SENSOR_ARRAY, 0, 0.4f, 3.5f);

        // Stabilizer fins
        addMirroredPair(
            ShipPartType.STRUCT_FIN,
            1.5f,
            0.3f,
            -5.0f,
            0,
            0,
            15,
            0.8f
        );

        return this;
    }

    /**
     * Build a heavy assault ship
     */
    public ShipBuilder buildAssault() {
        clear();

        // Cockpit section
        addPart(ShipPartType.HULL_COCKPIT, 0, 0.5f, 4.0f, 0, 0, 0, 1.2f, false);

        // Heavy hull
        addPart(ShipPartType.HULL_FORWARD, 0, 0, 2.0f, 0, 0, 0, 1.3f, false);
        addPart(ShipPartType.HULL_MID, 0, 0, -1.0f, 0, 0, 0, 1.4f, false);
        addPart(ShipPartType.HULL_MID, 0, 0, -4.0f, 0, 0, 0, 1.3f, false);
        addPart(ShipPartType.HULL_AFT, 0, 0, -7.0f, 0, 0, 0, 1.2f, false);

        // Stub wings with pylons
        addMirroredPair(ShipPartType.WING_STUB, 2.0f, 0, -2.0f, 0, 0, 0, 1.5f);
        addMirroredPair(
            ShipPartType.STRUCT_PYLON,
            2.5f,
            -0.4f,
            -2.0f,
            0,
            0,
            0,
            1f
        );

        // Engine nacelles
        addMirroredPair(
            ShipPartType.ENGINE_NACELLE,
            1.8f,
            0,
            -6.0f,
            0,
            0,
            0,
            1f
        );
        addPart(ShipPartType.ENGINE_LARGE, 0, 0, -8.5f, 0, 0, 0, 1f, false);

        // Heavy weapons
        addMirroredPair(
            ShipPartType.WEAPON_MOUNT_LARGE,
            2.5f,
            0,
            0,
            0,
            0,
            0,
            1f
        );
        addMirroredPair(
            ShipPartType.WEAPON_MOUNT_MEDIUM,
            1.5f,
            -0.3f,
            2.0f,
            0,
            0,
            0,
            1f
        );
        addPart(ShipPartType.WEAPON_TURRET, 0, 0.8f, -3.0f, 0, 0, 0, 1f, false);
        addMirroredPair(
            ShipPartType.WEAPON_MISSILE_POD,
            3.0f,
            -0.5f,
            -3.0f,
            0,
            0,
            0,
            1f
        );

        // Utilities
        addPart(ShipPartType.UTIL_SHIELD_GENERATOR, 0, 0.6f, -1.0f);
        addPart(ShipPartType.UTIL_SENSOR_ARRAY, 0, 0.5f, 4.5f);

        // Decorations
        addMirroredPair(ShipPartType.DECOR_LIGHT, 2.8f, 0, 1.0f, 0, 0, 0, 1f);
        addPart(ShipPartType.DECOR_LIGHT, 0, 0.3f, 5.0f);

        return this;
    }

    /**
     * Build a cargo freighter
     */
    public ShipBuilder buildFreighter() {
        clear();

        // Bridge
        addPart(ShipPartType.HULL_COCKPIT, 0, 1.5f, 5.0f, 0, 0, 0, 1.5f, false);

        // Large hull sections
        addPart(ShipPartType.HULL_FORWARD, 0, 0, 3.0f, 0, 0, 0, 1.5f, false);
        addPart(ShipPartType.HULL_MID, 0, 0, 0, 0, 0, 0, 2.0f, false);
        addPart(ShipPartType.HULL_MID, 0, 0, -4.0f, 0, 0, 0, 2.0f, false);
        addPart(ShipPartType.HULL_MID, 0, 0, -8.0f, 0, 0, 0, 1.8f, false);
        addPart(ShipPartType.HULL_AFT, 0, 0, -11.0f, 0, 0, 0, 1.5f, false);

        // Cargo pods
        addMirroredPair(
            ShipPartType.UTIL_CARGO_POD,
            2.5f,
            0,
            -2.0f,
            0,
            0,
            0,
            1.5f
        );
        addMirroredPair(
            ShipPartType.UTIL_CARGO_POD,
            2.5f,
            0,
            -6.0f,
            0,
            0,
            0,
            1.5f
        );

        // Engine array
        addMirroredPair(
            ShipPartType.ENGINE_MEDIUM,
            1.5f,
            0,
            -13.0f,
            0,
            0,
            0,
            1f
        );
        addPart(ShipPartType.ENGINE_LARGE, 0, 0, -13.5f, 0, 0, 0, 1f, false);

        // Minimal weapons (defensive)
        addMirroredPair(
            ShipPartType.WEAPON_MOUNT_SMALL,
            2.0f,
            0.5f,
            2.0f,
            0,
            0,
            0,
            1f
        );
        addPart(ShipPartType.WEAPON_TURRET, 0, 1.0f, -5.0f, 0, 0, 0, 1f, false);

        // Utilities
        addPart(ShipPartType.UTIL_ANTENNA, 0, 2.0f, 4.0f);
        addPart(
            ShipPartType.UTIL_FUEL_TANK,
            0,
            -1.0f,
            -10.0f,
            0,
            0,
            0,
            1.5f,
            false
        );

        // Navigation lights
        addMirroredPair(ShipPartType.DECOR_LIGHT, 3.0f, 0, 3.0f, 0, 0, 0, 1f);

        return this;
    }

    /**
     * Build a scout/exploration ship
     */
    public ShipBuilder buildScout() {
        clear();

        // Compact cockpit
        addPart(ShipPartType.HULL_COCKPIT, 0, 0, 2.0f, 0, 0, 0, 0.9f, false);
        addPart(ShipPartType.HULL_NOSE, 0, 0, 3.2f, 0, 90, 0, 0.7f, false);

        // Slim hull
        addPart(ShipPartType.HULL_FORWARD, 0, 0, 0.5f, 0, 0, 0, 0.8f, false);
        addPart(ShipPartType.HULL_MID, 0, 0, -1.5f, 0, 0, 0, 0.9f, false);
        addPart(ShipPartType.HULL_AFT, 0, 0, -3.5f, 0, 0, 0, 0.8f, false);

        // Variable geometry wings
        addMirroredPair(
            ShipPartType.WING_VARIABLE,
            1.8f,
            0,
            -1.0f,
            0,
            0,
            0,
            0.9f
        );

        // High-performance engines
        addMirroredPair(ShipPartType.ENGINE_SMALL, 0.6f, 0, -4.5f, 0, 0, 0, 1f);
        addMirroredPair(
            ShipPartType.ENGINE_AFTERBURNER,
            0.6f,
            0,
            -5.0f,
            0,
            0,
            0,
            0.8f
        );

        // Sensor suite
        addPart(
            ShipPartType.UTIL_SENSOR_ARRAY,
            0,
            0.3f,
            2.5f,
            0,
            0,
            0,
            1.2f,
            false
        );
        addPart(ShipPartType.UTIL_ANTENNA, 0, 0.5f, -0.5f);

        // Minimal weapons
        addMirroredPair(
            ShipPartType.WEAPON_MOUNT_SMALL,
            1.2f,
            -0.2f,
            1.0f,
            0,
            0,
            0,
            0.8f
        );

        // Extra fuel for range
        addPart(
            ShipPartType.UTIL_FUEL_TANK,
            0,
            -0.5f,
            -2.5f,
            0,
            0,
            0,
            0.8f,
            false
        );

        return this;
    }

    // ============== Getters ==============

    public float getTotalMass() {
        return totalMass;
    }

    public float getTotalHull() {
        return totalHull;
    }

    public float getTotalShield() {
        return totalShield;
    }

    public float getTotalThrust() {
        return totalThrust;
    }

    public float getTotalFuel() {
        return totalFuel;
    }

    public ShipValidator getValidator() {
        return validator;
    }

    public Array<SnapPoint> getSnapPoints() {
        return snapPoints;
    }

    public void setEnableSnapPoints(boolean enable) {
        this.enableSnapPoints = enable;
    }

    public boolean isEnableSnapPoints() {
        return enableSnapPoints;
    }

    public void setSnapDistance(float distance) {
        this.snapDistance = distance;
    }

    public float getSnapDistance() {
        return snapDistance;
    }

    /**
     * Calculate thrust-to-weight ratio
     */
    public float getThrustToWeight() {
        return totalMass > 0 ? totalThrust / totalMass : 0;
    }

    /**
     * Validate the ship and return if it's valid
     */
    public boolean validate() {
        if (validationDirty) {
            isValid = validator.validate();
            validationDirty = false;
        }
        return isValid;
    }

    /**
     * Get validation errors
     */
    public Array<ShipValidator.ValidationError> getErrors() {
        validate(); // Ensure validation is up to date
        return validator.getErrors();
    }

    /**
     * Get validation warnings
     */
    public Array<ShipValidator.ValidationWarning> getWarnings() {
        validate(); // Ensure validation is up to date
        return validator.getWarnings();
    }

    /**
     * Check if ship is valid (no critical errors)
     */
    public boolean isValid() {
        validate();
        return isValid;
    }

    /**
     * Get a formatted validation report
     */
    public String getValidationReport() {
        validate();
        return validator.getValidationReport();
    }

    /**
     * Get a short validation summary for UI
     */
    public String getValidationSummary() {
        validate();
        return validator.getSummary();
    }

    /**
     * Check if ship can fly (passed validation)
     */
    public boolean canFly() {
        return validate();
    }

    /**
     * Mark validation as dirty (needs revalidation)
     */
    public void invalidateValidation() {
        validationDirty = true;
    }

    @Override
    public void dispose() {
        meshFactory.dispose();
        parts.clear();
        snapPoints.clear();
    }

    /**
     * Represents a snap point for precise part placement
     */
    public static class SnapPoint {

        public final Vector3 position;
        public final Vector3 direction;
        public final ShipPart parentPart;
        public final String attachmentId;

        public SnapPoint(
            Vector3 position,
            Vector3 direction,
            ShipPart parentPart,
            String attachmentId
        ) {
            this.position = new Vector3(position);
            this.direction = new Vector3(direction);
            this.parentPart = parentPart;
            this.attachmentId = attachmentId;
        }

        /**
         * Get the opposite direction (for matching attachment)
         */
        public Vector3 getOppositeDirection() {
            return new Vector3(-direction.x, -direction.y, -direction.z);
        }
    }
}
