package com.astral.shipbuilding;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Represents an individual ship part instance with position, rotation, and configuration.
 */
public class ShipPart implements Disposable {

    private final ShipPartType type;
    private final Vector3 position;
    private final Quaternion rotation;
    private final Vector3 scale;
    private final Matrix4 transform;

    private Model model;
    private ModelInstance modelInstance;

    private Color primaryColor;
    private Color secondaryColor;
    private Color emissiveColor;

    private boolean mirrored;
    private int variant;

    // Attachment points for connecting other parts
    private final Array<AttachmentPoint> attachmentPoints;

    // Parent part (if attached to another part)
    private ShipPart parentPart;
    private String parentAttachmentId;

    // Stats contribution
    private float massContribution;
    private float hullContribution;
    private float shieldContribution;
    private float thrustContribution;
    private float fuelCapacity;

    public ShipPart(ShipPartType type) {
        this.type = type;
        this.position = new Vector3();
        this.rotation = new Quaternion();
        this.scale = new Vector3(1, 1, 1);
        this.transform = new Matrix4();

        this.primaryColor = new Color(0.6f, 0.6f, 0.65f, 1f);
        this.secondaryColor = new Color(0.3f, 0.3f, 0.35f, 1f);
        this.emissiveColor = new Color(0, 0, 0, 0);

        this.mirrored = false;
        this.variant = 0;

        this.attachmentPoints = new Array<>();
        initializeStats();
    }

    private void initializeStats() {
        switch (type.getCategory()) {
            case HULL:
                massContribution = 500f;
                hullContribution = 100f;
                break;
            case WING:
                massContribution = 100f;
                hullContribution = 20f;
                break;
            case ENGINE:
                massContribution = 200f;
                thrustContribution = type == ShipPartType.ENGINE_LARGE ? 150000f :
                                     type == ShipPartType.ENGINE_MEDIUM ? 80000f : 40000f;
                break;
            case WEAPON:
                massContribution = 50f;
                break;
            case UTILITY:
                massContribution = 30f;
                if (type == ShipPartType.UTIL_SHIELD_GENERATOR) {
                    shieldContribution = 100f;
                } else if (type == ShipPartType.UTIL_FUEL_TANK) {
                    fuelCapacity = 200f;
                }
                break;
            case STRUCTURAL:
                massContribution = 20f;
                hullContribution = 5f;
                break;
            case DECORATIVE:
                massContribution = 5f;
                break;
        }
    }

    /**
     * Set the part's position
     */
    public void setPosition(float x, float y, float z) {
        position.set(x, y, z);
        updateTransform();
    }

    public void setPosition(Vector3 pos) {
        position.set(pos);
        updateTransform();
    }

    /**
     * Set the part's rotation (Euler angles in degrees)
     */
    public void setRotation(float yaw, float pitch, float roll) {
        rotation.setEulerAngles(yaw, pitch, roll);
        updateTransform();
    }

    /**
     * Set the part's scale
     */
    public void setScale(float x, float y, float z) {
        scale.set(x, y, z);
        updateTransform();
    }

    public void setScale(float uniform) {
        scale.set(uniform, uniform, uniform);
        updateTransform();
    }

    /**
     * Update the transform matrix from position, rotation, scale
     */
    private void updateTransform() {
        transform.idt();
        transform.translate(position);
        transform.rotate(rotation);
        transform.scale(scale.x, mirrored ? -scale.y : scale.y, scale.z);

        if (modelInstance != null) {
            modelInstance.transform.set(transform);
        }
    }

    /**
     * Set the model for this part
     */
    public void setModel(Model model) {
        this.model = model;
        this.modelInstance = new ModelInstance(model);
        updateTransform();
    }

    /**
     * Add an attachment point
     */
    public void addAttachmentPoint(String id, Vector3 localPosition, Vector3 direction) {
        attachmentPoints.add(new AttachmentPoint(id, localPosition, direction));
    }

    /**
     * Get attachment point by ID
     */
    public AttachmentPoint getAttachmentPoint(String id) {
        for (AttachmentPoint ap : attachmentPoints) {
            if (ap.id.equals(id)) {
                return ap;
            }
        }
        return null;
    }

    /**
     * Attach to a parent part
     */
    public void attachTo(ShipPart parent, String attachmentId) {
        this.parentPart = parent;
        this.parentAttachmentId = attachmentId;

        AttachmentPoint ap = parent.getAttachmentPoint(attachmentId);
        if (ap != null) {
            Vector3 worldPos = new Vector3(ap.localPosition);
            worldPos.mul(parent.transform);
            setPosition(worldPos);
        }
    }

    // ============== Getters and Setters ==============

    public ShipPartType getType() { return type; }
    public Vector3 getPosition() { return position; }
    public Quaternion getRotation() { return rotation; }
    public Vector3 getScale() { return scale; }
    public Matrix4 getTransform() { return transform; }
    public Model getModel() { return model; }
    public ModelInstance getModelInstance() { return modelInstance; }

    public Color getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(Color color) { this.primaryColor = color; }

    public Color getSecondaryColor() { return secondaryColor; }
    public void setSecondaryColor(Color color) { this.secondaryColor = color; }

    public Color getEmissiveColor() { return emissiveColor; }
    public void setEmissiveColor(Color color) { this.emissiveColor = color; }

    public boolean isMirrored() { return mirrored; }
    public void setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
        updateTransform();
    }

    public int getVariant() { return variant; }
    public void setVariant(int variant) { this.variant = variant; }

    public float getMassContribution() { return massContribution; }
    public float getHullContribution() { return hullContribution; }
    public float getShieldContribution() { return shieldContribution; }
    public float getThrustContribution() { return thrustContribution; }
    public float getFuelCapacity() { return fuelCapacity; }

    public Array<AttachmentPoint> getAttachmentPoints() { return attachmentPoints; }
    public ShipPart getParentPart() { return parentPart; }
    public String getParentAttachmentId() { return parentAttachmentId; }

    @Override
    public void dispose() {
        // Model disposal is handled by the factory that created it
    }

    /**
     * Represents an attachment point on a ship part
     */
    public static class AttachmentPoint {
        public final String id;
        public final Vector3 localPosition;
        public final Vector3 direction;
        public boolean occupied;

        public AttachmentPoint(String id, Vector3 localPosition, Vector3 direction) {
            this.id = id;
            this.localPosition = new Vector3(localPosition);
            this.direction = new Vector3(direction).nor();
            this.occupied = false;
        }

        /**
         * Get world position given parent transform
         */
        public Vector3 getWorldPosition(Matrix4 parentTransform) {
            Vector3 worldPos = new Vector3(localPosition);
            worldPos.mul(parentTransform);
            return worldPos;
        }

        /**
         * Get world direction given parent transform
         */
        public Vector3 getWorldDirection(Matrix4 parentTransform) {
            Vector3 worldDir = new Vector3(direction);
            worldDir.rot(parentTransform).nor();
            return worldDir;
        }
    }
}
