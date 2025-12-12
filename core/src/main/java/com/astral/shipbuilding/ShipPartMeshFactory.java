package com.astral.shipbuilding;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Factory for creating ship part meshes using the LGMesh system.
 */
public class ShipPartMeshFactory implements Disposable {

    private final ObjectMap<String, Model> modelCache;
    private final ShipTextureGenerator textureGenerator;

    public ShipPartMeshFactory() {
        this.modelCache = new ObjectMap<>();
        this.textureGenerator = new ShipTextureGenerator();
    }

    /**
     * Create a model for a ship part with the given colors
     */
    public Model createPartModel(ShipPartType type, Color primary, Color secondary, int variant) {
        String cacheKey = type.name() + "_" + variant + "_" + primary.toString() + "_" + secondary.toString();

        if (modelCache.containsKey(cacheKey)) {
            return modelCache.get(cacheKey);
        }

        LGMesh mesh = new LGMesh();
        buildPartMesh(mesh, type, primary, secondary, variant);

        Texture texture = textureGenerator.generateTexture(type, primary, secondary);
        Material material = new Material(
            ColorAttribute.createDiffuse(Color.WHITE),
            TextureAttribute.createDiffuse(texture)
        );

        Model model = mesh.buildModel(material, type.name().toLowerCase());
        modelCache.put(cacheKey, model);

        return model;
    }

    /**
     * Create a model with default colors
     */
    public Model createPartModel(ShipPartType type) {
        return createPartModel(type,
            new Color(0.6f, 0.6f, 0.65f, 1f),
            new Color(0.3f, 0.3f, 0.35f, 1f),
            0);
    }

    /**
     * Build the mesh geometry for a specific part type
     */
    private void buildPartMesh(LGMesh mesh, ShipPartType type, Color primary, Color secondary, int variant) {
        switch (type) {
            // Hull Parts
            case HULL_COCKPIT:
                buildCockpitMesh(mesh, primary, secondary);
                break;
            case HULL_NOSE:
                buildNoseMesh(mesh, primary, secondary);
                break;
            case HULL_FORWARD:
                buildForwardHullMesh(mesh, primary, secondary);
                break;
            case HULL_MID:
                buildMidHullMesh(mesh, primary, secondary);
                break;
            case HULL_AFT:
                buildAftHullMesh(mesh, primary, secondary);
                break;
            case HULL_TAIL:
                buildTailMesh(mesh, primary, secondary);
                break;

            // Wing Parts
            case WING_STANDARD:
                buildStandardWingMesh(mesh, primary, secondary);
                break;
            case WING_SWEPT:
                buildSweptWingMesh(mesh, primary, secondary);
                break;
            case WING_DELTA:
                buildDeltaWingMesh(mesh, primary, secondary);
                break;
            case WING_STUB:
                buildStubWingMesh(mesh, primary, secondary);
                break;
            case WING_VARIABLE:
                buildVariableWingMesh(mesh, primary, secondary);
                break;

            // Engine Parts
            case ENGINE_SMALL:
                buildSmallEngineMesh(mesh, primary, secondary);
                break;
            case ENGINE_MEDIUM:
                buildMediumEngineMesh(mesh, primary, secondary);
                break;
            case ENGINE_LARGE:
                buildLargeEngineMesh(mesh, primary, secondary);
                break;
            case ENGINE_NACELLE:
                buildEngineNacelleMesh(mesh, primary, secondary);
                break;
            case ENGINE_AFTERBURNER:
                buildAfterburnerMesh(mesh, primary, secondary);
                break;

            // Weapon Parts
            case WEAPON_MOUNT_SMALL:
                buildSmallWeaponMountMesh(mesh, primary, secondary);
                break;
            case WEAPON_MOUNT_MEDIUM:
                buildMediumWeaponMountMesh(mesh, primary, secondary);
                break;
            case WEAPON_MOUNT_LARGE:
                buildLargeWeaponMountMesh(mesh, primary, secondary);
                break;
            case WEAPON_TURRET:
                buildTurretMesh(mesh, primary, secondary);
                break;
            case WEAPON_MISSILE_POD:
                buildMissilePodMesh(mesh, primary, secondary);
                break;

            // Utility Parts
            case UTIL_SENSOR_ARRAY:
                buildSensorArrayMesh(mesh, primary, secondary);
                break;
            case UTIL_ANTENNA:
                buildAntennaMesh(mesh, primary, secondary);
                break;
            case UTIL_CARGO_POD:
                buildCargoPodMesh(mesh, primary, secondary);
                break;
            case UTIL_FUEL_TANK:
                buildFuelTankMesh(mesh, primary, secondary);
                break;
            case UTIL_SHIELD_GENERATOR:
                buildShieldGeneratorMesh(mesh, primary, secondary);
                break;

            // Structural Parts
            case STRUCT_STRUT:
                buildStrutMesh(mesh, primary, secondary);
                break;
            case STRUCT_CONNECTOR:
                buildConnectorMesh(mesh, primary, secondary);
                break;
            case STRUCT_PYLON:
                buildPylonMesh(mesh, primary, secondary);
                break;
            case STRUCT_FIN:
                buildFinMesh(mesh, primary, secondary);
                break;

            // Decorative Parts
            case DECOR_STRIPE:
                buildStripeMesh(mesh, primary, secondary);
                break;
            case DECOR_EMBLEM:
                buildEmblemMesh(mesh, primary, secondary);
                break;
            case DECOR_LIGHT:
                buildLightMesh(mesh, primary, secondary);
                break;
            case DECOR_EXHAUST:
                buildExhaustMesh(mesh, primary, secondary);
                break;

            // Starfield-style Parts
            case HULL_COCKPIT_LARGE:
                buildLargeCockpitMesh(mesh, primary, secondary);
                break;

            // Habitation Modules
            case HAB_LIVING_QUARTERS:
            case HAB_MESS_HALL:
            case HAB_CAPTAIN_QUARTERS:
            case HAB_CREW_STATION:
            case HAB_ARMORY:
            case HAB_WORKSHOP:
            case HAB_SCIENCE_LAB:
            case HAB_INFIRMARY:
                buildHabModuleMesh(mesh, type, primary, secondary);
                break;

            // Reactors
            case REACTOR_CLASS_A:
                buildReactorMesh(mesh, primary, secondary, 0.8f);
                break;
            case REACTOR_CLASS_B:
                buildReactorMesh(mesh, primary, secondary, 1.0f);
                break;
            case REACTOR_CLASS_C:
                buildReactorMesh(mesh, primary, secondary, 1.3f);
                break;

            // Grav Drives
            case GRAV_DRIVE_BASIC:
                buildGravDriveMesh(mesh, primary, secondary, 0.8f);
                break;
            case GRAV_DRIVE_ADVANCED:
                buildGravDriveMesh(mesh, primary, secondary, 1.0f);
                break;
            case GRAV_DRIVE_MILITARY:
                buildGravDriveMesh(mesh, primary, secondary, 1.2f);
                break;

            // Landing Gear
            case LANDING_GEAR_SMALL:
                buildLandingGearMesh(mesh, primary, secondary, 0.7f);
                break;
            case LANDING_GEAR_MEDIUM:
                buildLandingGearMesh(mesh, primary, secondary, 1.0f);
                break;
            case LANDING_GEAR_LARGE:
                buildLandingGearMesh(mesh, primary, secondary, 1.4f);
                break;

            // Dockers
            case DOCKER_STANDARD:
                buildDockerMesh(mesh, primary, secondary, false);
                break;
            case DOCKER_SLIM:
                buildDockerMesh(mesh, primary, secondary, true);
                break;
            case LANDING_BAY:
                buildLandingBayMesh(mesh, primary, secondary);
                break;

            // Shield Modules
            case SHIELD_LIGHT:
                buildShieldModuleMesh(mesh, primary, secondary, 0.8f);
                break;
            case SHIELD_MEDIUM:
                buildShieldModuleMesh(mesh, primary, secondary, 1.0f);
                break;
            case SHIELD_HEAVY:
                buildShieldModuleMesh(mesh, primary, secondary, 1.3f);
                break;

            default:
                // Fallback box for unknown parts
                mesh.addBox(0, 0, 0, 1f, 1f, 1f, primary);
                break;
        }
    }

    // ============== Hull Part Meshes ==============

    private void buildCockpitMesh(LGMesh mesh, Color primary, Color secondary) {
        // Main cockpit body - tapered forward section
        mesh.addTaperedHull(0, 0, 0, 1.0f, 0.8f, 1.5f, 1.2f, 2.0f, primary);

        // Canopy (glass-like section on top)
        LGMesh canopy = new LGMesh();
        canopy.addTaperedHull(0, 0.5f, 0.3f, 0.7f, 0.4f, 1.0f, 0.5f, 1.2f, new Color(0.3f, 0.5f, 0.8f, 0.8f));
        mesh.append(canopy);

        // Instrument cowling
        LGMesh cowl = new LGMesh();
        cowl.addBox(0, 0.1f, 0.8f, 0.6f, 0.2f, 0.4f, secondary);
        mesh.append(cowl);
    }

    private void buildNoseMesh(LGMesh mesh, Color primary, Color secondary) {
        // Pointed nose cone
        mesh.addCone(0.4f, 1.5f, 12, primary);

        // Nose base connector
        LGMesh base = new LGMesh();
        base.addCylinder(0, -0.85f, 0, 0.5f, 0.2f, 12, secondary);
        mesh.append(base);
    }

    private void buildForwardHullMesh(LGMesh mesh, Color primary, Color secondary) {
        // Main forward hull section
        mesh.addTaperedHull(0, 0, 0, 1.8f, 1.4f, 2.2f, 1.6f, 3.0f, primary);

        // Hull detail ridges
        LGMesh ridge1 = new LGMesh();
        ridge1.addBox(0, 0.75f, 0, 1.6f, 0.1f, 2.8f, secondary);
        mesh.append(ridge1);

        LGMesh ridge2 = new LGMesh();
        ridge2.addBox(0, -0.75f, 0, 1.6f, 0.1f, 2.8f, secondary);
        mesh.append(ridge2);
    }

    private void buildMidHullMesh(LGMesh mesh, Color primary, Color secondary) {
        // Main mid hull - largest section
        mesh.addBox(0, 0, 0, 2.5f, 1.8f, 4.0f, primary);

        // Ventral details
        LGMesh ventralPlate = new LGMesh();
        ventralPlate.addBox(0, -0.95f, 0, 2.0f, 0.1f, 3.5f, secondary);
        mesh.append(ventralPlate);

        // Side panels
        for (int side = -1; side <= 1; side += 2) {
            LGMesh panel = new LGMesh();
            panel.addBox(side * 1.3f, 0, 0, 0.1f, 1.4f, 3.0f, secondary);
            mesh.append(panel);
        }
    }

    private void buildAftHullMesh(LGMesh mesh, Color primary, Color secondary) {
        // Aft hull section - tapers toward engines
        mesh.addTaperedHull(0, 0, 0, 2.2f, 1.6f, 1.8f, 1.4f, 2.5f, primary);

        // Engine mounting frame
        LGMesh frame = new LGMesh();
        frame.addBox(0, 0, -1.4f, 2.0f, 1.2f, 0.3f, secondary);
        mesh.append(frame);
    }

    private void buildTailMesh(LGMesh mesh, Color primary, Color secondary) {
        // Tail section
        mesh.addTaperedHull(0, 0, 0, 1.2f, 1.0f, 0.6f, 0.5f, 1.5f, primary);

        // Tail fin mounts
        LGMesh finMount = new LGMesh();
        finMount.addBox(0, 0.4f, -0.5f, 0.3f, 0.3f, 0.8f, secondary);
        mesh.append(finMount);
    }

    // ============== Wing Part Meshes ==============

    private void buildStandardWingMesh(LGMesh mesh, Color primary, Color secondary) {
        // Main wing surface
        mesh.addWedge(3.0f, 0.15f, 1.5f, primary);

        // Wing root fairing
        LGMesh fairing = new LGMesh();
        fairing.addBox(-1.4f, 0, 0, 0.3f, 0.25f, 1.3f, secondary);
        mesh.append(fairing);

        // Wingtip
        LGMesh tip = new LGMesh();
        tip.addWedge(0.4f, 0.1f, 0.8f, secondary);
        Matrix4 tipTransform = new Matrix4().translate(1.4f, 0, -0.3f);
        tip.transform(tipTransform);
        mesh.append(tip);
    }

    private void buildSweptWingMesh(LGMesh mesh, Color primary, Color secondary) {
        // Swept wing - angled back
        mesh.addWedge(2.5f, 0.12f, 2.0f, primary);

        // Leading edge slat
        LGMesh slat = new LGMesh();
        slat.addBox(0, 0.08f, 0.95f, 2.3f, 0.06f, 0.15f, secondary);
        mesh.append(slat);
    }

    private void buildDeltaWingMesh(LGMesh mesh, Color primary, Color secondary) {
        // Delta wing - triangular
        LGMesh wing = new LGMesh();

        // Create triangular wing shape manually
        short v0 = wing.addVertex(-2.0f, 0, -1.5f, 0, 1, 0, 0, 0, primary.r, primary.g, primary.b, 1);
        short v1 = wing.addVertex(2.0f, 0, -1.5f, 0, 1, 0, 1, 0, primary.r, primary.g, primary.b, 1);
        short v2 = wing.addVertex(0, 0, 1.5f, 0, 1, 0, 0.5f, 1, primary.r, primary.g, primary.b, 1);
        wing.addTriangle(v0, v1, v2);

        // Bottom face
        v0 = wing.addVertex(-2.0f, -0.1f, -1.5f, 0, -1, 0, 0, 0, primary.r, primary.g, primary.b, 1);
        v1 = wing.addVertex(0, -0.1f, 1.5f, 0, -1, 0, 0.5f, 1, primary.r, primary.g, primary.b, 1);
        v2 = wing.addVertex(2.0f, -0.1f, -1.5f, 0, -1, 0, 1, 0, primary.r, primary.g, primary.b, 1);
        wing.addTriangle(v0, v1, v2);

        mesh.append(wing);
    }

    private void buildStubWingMesh(LGMesh mesh, Color primary, Color secondary) {
        // Short stub wing
        mesh.addBox(0, 0, 0, 1.2f, 0.15f, 0.8f, primary);

        // Wing fence
        LGMesh fence = new LGMesh();
        fence.addBox(0.4f, 0.1f, 0, 0.05f, 0.15f, 0.7f, secondary);
        mesh.append(fence);
    }

    private void buildVariableWingMesh(LGMesh mesh, Color primary, Color secondary) {
        // Variable geometry wing - swept position
        mesh.addWedge(2.8f, 0.14f, 1.8f, primary);

        // Wing pivot housing
        LGMesh pivot = new LGMesh();
        pivot.addCylinder(-1.2f, 0, 0, 0.15f, 0.3f, 8, secondary);
        mesh.append(pivot);
    }

    // ============== Engine Part Meshes ==============

    private void buildSmallEngineMesh(LGMesh mesh, Color primary, Color secondary) {
        // Engine housing
        mesh.addCylinder(0, 0, 0, 0.25f, 0.8f, 10, primary);

        // Exhaust nozzle
        LGMesh nozzle = new LGMesh();
        nozzle.addCone(0.2f, 0.3f, 10, secondary);
        Matrix4 nozzleTransform = new Matrix4().translate(0, -0.55f, 0).rotate(1, 0, 0, 180);
        nozzle.transform(nozzleTransform);
        mesh.append(nozzle);

        // Intake ring
        LGMesh intake = new LGMesh();
        intake.addCylinder(0, 0.45f, 0, 0.28f, 0.1f, 10, secondary);
        mesh.append(intake);
    }

    private void buildMediumEngineMesh(LGMesh mesh, Color primary, Color secondary) {
        // Main engine body
        mesh.addCylinder(0, 0, 0, 0.4f, 1.2f, 12, primary);

        // Exhaust bell
        LGMesh bell = new LGMesh();
        bell.addCone(0.35f, 0.5f, 12, secondary);
        Matrix4 bellTransform = new Matrix4().translate(0, -0.85f, 0).rotate(1, 0, 0, 180);
        bell.transform(bellTransform);
        mesh.append(bell);

        // Mounting bracket
        LGMesh bracket = new LGMesh();
        bracket.addBox(0, 0.3f, 0, 0.6f, 0.1f, 0.6f, secondary);
        mesh.append(bracket);
    }

    private void buildLargeEngineMesh(LGMesh mesh, Color primary, Color secondary) {
        // Large engine housing
        mesh.addCylinder(0, 0, 0, 0.6f, 1.8f, 16, primary);

        // Primary exhaust nozzle
        LGMesh nozzle = new LGMesh();
        nozzle.addCone(0.55f, 0.7f, 16, secondary);
        Matrix4 nozzleTransform = new Matrix4().translate(0, -1.25f, 0).rotate(1, 0, 0, 180);
        nozzle.transform(nozzleTransform);
        mesh.append(nozzle);

        // Engine intake cowling
        LGMesh cowl = new LGMesh();
        cowl.addCylinder(0, 0.95f, 0, 0.65f, 0.15f, 16, secondary);
        mesh.append(cowl);

        // Mounting pylons
        for (int i = 0; i < 4; i++) {
            float angle = i * 90f;
            LGMesh pylon = new LGMesh();
            pylon.addBox(0.55f, 0.3f, 0, 0.15f, 0.1f, 0.3f, secondary);
            Matrix4 pylonTransform = new Matrix4().rotate(0, 1, 0, angle);
            pylon.transform(pylonTransform);
            mesh.append(pylon);
        }
    }

    private void buildEngineNacelleMesh(LGMesh mesh, Color primary, Color secondary) {
        // Nacelle body
        mesh.addTaperedHull(0, 0, 0, 0.6f, 0.5f, 0.8f, 0.7f, 2.0f, primary);

        // Air intake
        LGMesh intake = new LGMesh();
        intake.addCylinder(0, 0, 1.1f, 0.25f, 0.2f, 8, secondary);
        mesh.append(intake);
    }

    private void buildAfterburnerMesh(LGMesh mesh, Color primary, Color secondary) {
        // Afterburner section
        mesh.addCylinder(0, 0, 0, 0.35f, 0.6f, 12, primary);

        // Flame holder rings
        for (int i = 0; i < 3; i++) {
            LGMesh ring = new LGMesh();
            ring.addCylinder(0, -0.2f + i * 0.15f, 0, 0.38f, 0.03f, 12, secondary);
            mesh.append(ring);
        }
    }

    // ============== Weapon Part Meshes ==============

    private void buildSmallWeaponMountMesh(LGMesh mesh, Color primary, Color secondary) {
        // Mount base
        mesh.addBox(0, 0, 0, 0.2f, 0.15f, 0.3f, primary);

        // Weapon barrel
        LGMesh barrel = new LGMesh();
        barrel.addCylinder(0, 0.12f, 0.1f, 0.04f, 0.4f, 6, secondary);
        Matrix4 barrelTransform = new Matrix4().rotate(1, 0, 0, 90);
        barrel.transform(barrelTransform);
        mesh.append(barrel);
    }

    private void buildMediumWeaponMountMesh(LGMesh mesh, Color primary, Color secondary) {
        // Mount housing
        mesh.addBox(0, 0, 0, 0.35f, 0.25f, 0.5f, primary);

        // Dual barrels
        for (int side = -1; side <= 1; side += 2) {
            LGMesh barrel = new LGMesh();
            barrel.addCylinder(side * 0.08f, 0.15f, 0.15f, 0.05f, 0.5f, 6, secondary);
            Matrix4 barrelTransform = new Matrix4().rotate(1, 0, 0, 90);
            barrel.transform(barrelTransform);
            mesh.append(barrel);
        }
    }

    private void buildLargeWeaponMountMesh(LGMesh mesh, Color primary, Color secondary) {
        // Heavy weapon mount
        mesh.addBox(0, 0, 0, 0.5f, 0.35f, 0.7f, primary);

        // Large barrel
        LGMesh barrel = new LGMesh();
        barrel.addCylinder(0, 0.2f, 0.25f, 0.1f, 0.8f, 8, secondary);
        Matrix4 barrelTransform = new Matrix4().rotate(1, 0, 0, 90);
        barrel.transform(barrelTransform);
        mesh.append(barrel);

        // Cooling vents
        LGMesh vents = new LGMesh();
        vents.addBox(0, 0.1f, -0.2f, 0.4f, 0.15f, 0.15f, secondary);
        mesh.append(vents);
    }

    private void buildTurretMesh(LGMesh mesh, Color primary, Color secondary) {
        // Turret base
        mesh.addCylinder(0, 0, 0, 0.3f, 0.2f, 10, primary);

        // Turret dome
        LGMesh dome = new LGMesh();
        dome.addCylinder(0, 0.2f, 0, 0.25f, 0.15f, 10, secondary);
        mesh.append(dome);

        // Gun barrels
        for (int side = -1; side <= 1; side += 2) {
            LGMesh barrel = new LGMesh();
            barrel.addCylinder(side * 0.1f, 0.2f, 0.3f, 0.04f, 0.5f, 6, secondary);
            Matrix4 barrelTransform = new Matrix4().rotate(1, 0, 0, 90);
            barrel.transform(barrelTransform);
            mesh.append(barrel);
        }
    }

    private void buildMissilePodMesh(LGMesh mesh, Color primary, Color secondary) {
        // Pod housing
        mesh.addBox(0, 0, 0, 0.4f, 0.3f, 0.8f, primary);

        // Missile tubes (2x3 grid)
        for (int x = -1; x <= 1; x += 2) {
            for (int y = 0; y < 2; y++) {
                LGMesh tube = new LGMesh();
                tube.addCylinder(x * 0.1f, -0.05f + y * 0.15f, 0.45f, 0.06f, 0.15f, 6, secondary);
                Matrix4 tubeTransform = new Matrix4().rotate(1, 0, 0, 90);
                tube.transform(tubeTransform);
                mesh.append(tube);
            }
        }
    }

    // ============== Utility Part Meshes ==============

    private void buildSensorArrayMesh(LGMesh mesh, Color primary, Color secondary) {
        // Array base
        mesh.addBox(0, 0, 0, 0.4f, 0.1f, 0.4f, primary);

        // Sensor dish
        LGMesh dish = new LGMesh();
        dish.addCone(0.3f, 0.15f, 12, secondary);
        Matrix4 dishTransform = new Matrix4().translate(0, 0.12f, 0);
        dish.transform(dishTransform);
        mesh.append(dish);
    }

    private void buildAntennaMesh(LGMesh mesh, Color primary, Color secondary) {
        // Antenna mast
        mesh.addCylinder(0, 0.3f, 0, 0.03f, 0.6f, 6, primary);

        // Antenna elements
        for (int i = 0; i < 3; i++) {
            LGMesh element = new LGMesh();
            element.addBox(0, 0.2f + i * 0.15f, 0, 0.25f - i * 0.05f, 0.02f, 0.02f, secondary);
            mesh.append(element);
        }
    }

    private void buildCargoPodMesh(LGMesh mesh, Color primary, Color secondary) {
        // Main cargo container
        mesh.addBox(0, 0, 0, 0.8f, 0.6f, 1.5f, primary);

        // Reinforcement ribs
        for (int i = -1; i <= 1; i++) {
            LGMesh rib = new LGMesh();
            rib.addBox(0, 0, i * 0.5f, 0.85f, 0.65f, 0.05f, secondary);
            mesh.append(rib);
        }
    }

    private void buildFuelTankMesh(LGMesh mesh, Color primary, Color secondary) {
        // Cylindrical fuel tank
        mesh.addCylinder(0, 0, 0, 0.3f, 1.2f, 12, primary);

        // End caps
        LGMesh topCap = new LGMesh();
        topCap.addCone(0.3f, 0.2f, 12, secondary);
        Matrix4 topTransform = new Matrix4().translate(0, 0.7f, 0);
        topCap.transform(topTransform);
        mesh.append(topCap);

        LGMesh bottomCap = new LGMesh();
        bottomCap.addCone(0.3f, 0.2f, 12, secondary);
        Matrix4 bottomTransform = new Matrix4().translate(0, -0.7f, 0).rotate(1, 0, 0, 180);
        bottomCap.transform(bottomTransform);
        mesh.append(bottomCap);
    }

    private void buildShieldGeneratorMesh(LGMesh mesh, Color primary, Color secondary) {
        // Generator housing
        mesh.addHexPrism(0.35f, 0.5f, primary);

        // Emitter nodes
        for (int i = 0; i < 6; i++) {
            float angle = i * 60f;
            LGMesh node = new LGMesh();
            node.addCylinder(0.4f, 0, 0, 0.08f, 0.15f, 6, new Color(0.3f, 0.6f, 0.9f, 1f));
            Matrix4 nodeTransform = new Matrix4().rotate(0, 1, 0, angle);
            node.transform(nodeTransform);
            mesh.append(node);
        }
    }

    // ============== Structural Part Meshes ==============

    private void buildStrutMesh(LGMesh mesh, Color primary, Color secondary) {
        // Simple structural strut
        mesh.addBox(0, 0, 0, 0.1f, 0.1f, 1.0f, primary);
    }

    private void buildConnectorMesh(LGMesh mesh, Color primary, Color secondary) {
        // Section connector ring
        mesh.addCylinder(0, 0, 0, 0.4f, 0.15f, 12, primary);

        // Connection bolts
        for (int i = 0; i < 8; i++) {
            float angle = i * 45f;
            LGMesh bolt = new LGMesh();
            bolt.addCylinder(0.35f, 0, 0, 0.03f, 0.2f, 4, secondary);
            Matrix4 boltTransform = new Matrix4().rotate(0, 1, 0, angle);
            bolt.transform(boltTransform);
            mesh.append(bolt);
        }
    }

    private void buildPylonMesh(LGMesh mesh, Color primary, Color secondary) {
        // Wing pylon
        mesh.addWedge(0.15f, 0.3f, 0.6f, primary);

        // Hardpoint
        LGMesh hardpoint = new LGMesh();
        hardpoint.addBox(0, -0.2f, 0, 0.2f, 0.08f, 0.5f, secondary);
        mesh.append(hardpoint);
    }

    private void buildFinMesh(LGMesh mesh, Color primary, Color secondary) {
        // Stabilizer fin
        mesh.addWedge(0.08f, 0.6f, 0.5f, primary);

        // Fin root
        LGMesh root = new LGMesh();
        root.addBox(0, -0.35f, 0, 0.1f, 0.1f, 0.45f, secondary);
        mesh.append(root);
    }

    // ============== Decorative Part Meshes ==============

    private void buildStripeMesh(LGMesh mesh, Color primary, Color secondary) {
        // Decorative hull stripe - thin panel
        mesh.addBox(0, 0, 0, 2.0f, 0.02f, 0.15f, primary);
    }

    private void buildEmblemMesh(LGMesh mesh, Color primary, Color secondary) {
        // Emblem plate
        mesh.addCylinder(0, 0, 0, 0.2f, 0.02f, 12, primary);
    }

    private void buildLightMesh(LGMesh mesh, Color primary, Color secondary) {
        // Navigation light
        mesh.addCylinder(0, 0, 0, 0.05f, 0.08f, 8, new Color(1f, 0.2f, 0.2f, 1f));
    }

    private void buildExhaustMesh(LGMesh mesh, Color primary, Color secondary) {
        // Exhaust vent
        mesh.addBox(0, 0, 0, 0.15f, 0.08f, 0.25f, secondary);

        // Vent grille
        for (int i = 0; i < 3; i++) {
            LGMesh bar = new LGMesh();
            bar.addBox(0, 0.045f, -0.08f + i * 0.08f, 0.12f, 0.01f, 0.02f, primary);
            mesh.append(bar);
        }
    }

    // ============== Starfield-Style Part Meshes ==============

    private void buildLargeCockpitMesh(LGMesh mesh, Color primary, Color secondary) {
        // Larger cockpit with expanded canopy
        mesh.addTaperedHull(0, 0, 0, 1.4f, 1.0f, 2.0f, 1.6f, 2.5f, primary);

        // Large canopy
        LGMesh canopy = new LGMesh();
        canopy.addTaperedHull(0, 0.6f, 0.2f, 1.0f, 0.6f, 1.4f, 0.7f, 1.8f, new Color(0.3f, 0.5f, 0.8f, 0.8f));
        mesh.append(canopy);

        // Side instrument panels
        for (int side = -1; side <= 1; side += 2) {
            LGMesh panel = new LGMesh();
            panel.addBox(side * 0.9f, 0.2f, 0.5f, 0.15f, 0.4f, 0.6f, secondary);
            mesh.append(panel);
        }
    }

    private void buildHabModuleMesh(LGMesh mesh, ShipPartType type, Color primary, Color secondary) {
        // Standard hab module body
        mesh.addBox(0, 0, 0, 2.0f, 1.5f, 2.5f, primary);

        // Windows based on type
        Color windowColor = new Color(0.3f, 0.5f, 0.7f, 0.7f);
        
        switch (type) {
            case HAB_LIVING_QUARTERS:
                // Multiple small windows
                for (int i = 0; i < 3; i++) {
                    LGMesh window = new LGMesh();
                    window.addBox(1.01f, 0.3f, -0.8f + i * 0.8f, 0.05f, 0.3f, 0.4f, windowColor);
                    mesh.append(window);
                    LGMesh window2 = new LGMesh();
                    window2.addBox(-1.01f, 0.3f, -0.8f + i * 0.8f, 0.05f, 0.3f, 0.4f, windowColor);
                    mesh.append(window2);
                }
                break;
            case HAB_MESS_HALL:
                // Large window
                LGMesh bigWindow = new LGMesh();
                bigWindow.addBox(1.01f, 0.2f, 0, 0.05f, 0.6f, 1.5f, windowColor);
                mesh.append(bigWindow);
                break;
            case HAB_SCIENCE_LAB:
                // Dome on top
                LGMesh dome = new LGMesh();
                dome.addCylinder(0, 0.85f, 0, 0.5f, 0.4f, 12, windowColor);
                mesh.append(dome);
                break;
            default:
                // Standard porthole
                LGMesh porthole = new LGMesh();
                porthole.addCylinder(1.01f, 0.2f, 0, 0.2f, 0.05f, 8, windowColor);
                mesh.append(porthole);
                break;
        }

        // Top detail ridge
        LGMesh ridge = new LGMesh();
        ridge.addBox(0, 0.76f, 0, 1.8f, 0.05f, 2.3f, secondary);
        mesh.append(ridge);
    }

    private void buildReactorMesh(LGMesh mesh, Color primary, Color secondary, float scale) {
        // Main reactor housing
        mesh.addCylinder(0, 0, 0, 0.7f * scale, 1.2f * scale, 12, primary);

        // Core glow section
        Color glowColor = new Color(0.4f, 0.8f, 1.0f, 1f);
        LGMesh core = new LGMesh();
        core.addCylinder(0, 0, 0, 0.4f * scale, 0.8f * scale, 8, glowColor);
        mesh.append(core);

        // Cooling fins
        for (int i = 0; i < 6; i++) {
            float angle = i * 60f;
            LGMesh fin = new LGMesh();
            fin.addBox(0.8f * scale, 0, 0, 0.05f, 0.3f * scale, 0.8f * scale, secondary);
            Matrix4 finTransform = new Matrix4().rotate(0, 1, 0, angle);
            fin.transform(finTransform);
            mesh.append(fin);
        }

        // Power conduits
        for (int side = -1; side <= 1; side += 2) {
            LGMesh conduit = new LGMesh();
            conduit.addCylinder(side * 0.5f * scale, 0.7f * scale, 0, 0.08f * scale, 0.3f * scale, 6, secondary);
            mesh.append(conduit);
        }
    }

    private void buildGravDriveMesh(LGMesh mesh, Color primary, Color secondary, float scale) {
        // Main drive housing
        mesh.addBox(0, 0, 0, 1.0f * scale, 0.8f * scale, 1.5f * scale, primary);

        // Drive ring
        Color ringColor = new Color(0.5f, 0.3f, 0.8f, 1f);
        LGMesh ring = new LGMesh();
        ring.addCylinder(0, 0, -0.8f * scale, 0.6f * scale, 0.15f * scale, 16, ringColor);
        mesh.append(ring);

        // Inner ring (darker)
        LGMesh innerRing = new LGMesh();
        innerRing.addCylinder(0, 0, -0.8f * scale, 0.4f * scale, 0.2f * scale, 12, secondary);
        mesh.append(innerRing);

        // Power coupling
        LGMesh coupling = new LGMesh();
        coupling.addBox(0, 0.5f * scale, 0.3f * scale, 0.3f * scale, 0.15f * scale, 0.4f * scale, secondary);
        mesh.append(coupling);
    }

    private void buildLandingGearMesh(LGMesh mesh, Color primary, Color secondary, float scale) {
        // Main strut
        mesh.addCylinder(0, -0.3f * scale, 0, 0.08f * scale, 0.6f * scale, 8, secondary);

        // Shock absorber
        LGMesh shock = new LGMesh();
        shock.addCylinder(0, -0.1f * scale, 0, 0.12f * scale, 0.25f * scale, 8, primary);
        mesh.append(shock);

        // Wheel/Pad
        LGMesh pad = new LGMesh();
        pad.addCylinder(0, -0.6f * scale, 0, 0.2f * scale, 0.1f * scale, 12, secondary);
        mesh.append(pad);

        // Mounting bracket
        LGMesh mount = new LGMesh();
        mount.addBox(0, 0.1f * scale, 0, 0.25f * scale, 0.1f * scale, 0.25f * scale, primary);
        mesh.append(mount);
    }

    private void buildDockerMesh(LGMesh mesh, Color primary, Color secondary, boolean slim) {
        float height = slim ? 0.6f : 1.0f;
        float width = slim ? 0.8f : 1.2f;

        // Docking collar
        mesh.addCylinder(0, 0, 0, width / 2, height, 12, primary);

        // Inner airlock
        LGMesh airlock = new LGMesh();
        airlock.addCylinder(0, 0, 0, width / 2 - 0.1f, height - 0.1f, 10, secondary);
        mesh.append(airlock);

        // Docking clamps
        for (int i = 0; i < 4; i++) {
            float angle = i * 90f + 45f;
            LGMesh clamp = new LGMesh();
            clamp.addBox(width / 2 + 0.1f, 0, 0, 0.08f, 0.15f, 0.2f, secondary);
            Matrix4 clampTransform = new Matrix4().rotate(0, 1, 0, angle);
            clamp.transform(clampTransform);
            mesh.append(clamp);
        }

        // Status lights
        Color lightColor = new Color(0.2f, 0.9f, 0.3f, 1f);
        for (int i = 0; i < 4; i++) {
            float angle = i * 90f;
            LGMesh light = new LGMesh();
            light.addCylinder(width / 2 + 0.05f, height / 2, 0, 0.03f, 0.05f, 6, lightColor);
            Matrix4 lightTransform = new Matrix4().rotate(0, 1, 0, angle);
            light.transform(lightTransform);
            mesh.append(light);
        }
    }

    private void buildLandingBayMesh(LGMesh mesh, Color primary, Color secondary) {
        // Main bay structure
        mesh.addBox(0, 0, 0, 3.0f, 1.5f, 4.0f, primary);

        // Bay door (underneath, open position)
        LGMesh door = new LGMesh();
        door.addBox(0, -0.8f, 1.5f, 2.5f, 0.08f, 1.5f, secondary);
        mesh.append(door);

        // Interior floor
        LGMesh floor = new LGMesh();
        floor.addBox(0, -0.6f, 0, 2.6f, 0.05f, 3.5f, secondary);
        mesh.append(floor);

        // Bay lights
        Color lightColor = new Color(0.9f, 0.9f, 0.7f, 1f);
        for (int i = 0; i < 4; i++) {
            LGMesh light = new LGMesh();
            light.addCylinder(-1.0f + i * 0.7f, 0.7f, 0, 0.1f, 0.05f, 8, lightColor);
            mesh.append(light);
        }
    }

    private void buildShieldModuleMesh(LGMesh mesh, Color primary, Color secondary, float scale) {
        // Shield generator base
        mesh.addHexPrism(0.5f * scale, 0.6f * scale, primary);

        // Emitter array
        Color emitterColor = new Color(0.3f, 0.7f, 1.0f, 1f);
        LGMesh emitter = new LGMesh();
        emitter.addCylinder(0, 0.35f * scale, 0, 0.3f * scale, 0.15f * scale, 12, emitterColor);
        mesh.append(emitter);

        // Energy coils
        for (int i = 0; i < 6; i++) {
            float angle = i * 60f;
            LGMesh coil = new LGMesh();
            coil.addCylinder(0.4f * scale, 0, 0, 0.06f * scale, 0.5f * scale, 6, secondary);
            Matrix4 coilTransform = new Matrix4().rotate(0, 1, 0, angle);
            coil.transform(coilTransform);
            mesh.append(coil);
        }

        // Power indicator
        LGMesh indicator = new LGMesh();
        indicator.addCylinder(0, 0.5f * scale, 0, 0.08f * scale, 0.1f * scale, 8, emitterColor);
        mesh.append(indicator);
    }

    @Override
    public void dispose() {
        for (Model model : modelCache.values()) {
            model.dispose();
        }
        modelCache.clear();
        textureGenerator.dispose();
    }
}
