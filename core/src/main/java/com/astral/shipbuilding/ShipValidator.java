package com.astral.shipbuilding;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;

/**
 * ShipValidator - Validates ship configurations similar to Starfield's ship builder.
 * Checks for required parts, proper connections, and structural integrity.
 */
public class ShipValidator {

    private final ShipBuilder shipBuilder;
    private final Array<ValidationError> errors;
    private final Array<ValidationWarning> warnings;

    public enum ErrorType {
        // Critical Errors (Ship cannot be used)
        MISSING_COCKPIT("Missing Cockpit", "Ship requires at least one cockpit."),
        MISSING_REACTOR("Missing Reactor", "Ship requires a reactor to provide power."),
        MISSING_ENGINE("Missing Engine", "Ship requires at least one engine for propulsion."),
        MISSING_GRAV_DRIVE("Missing Grav Drive", "Ship requires a grav drive for FTL travel."),
        MISSING_FUEL_TANK("Missing Fuel Tank", "Ship requires at least one fuel tank."),
        MISSING_LANDING_GEAR("Missing Landing Gear", "Ship requires landing gear to land on planets."),

        MULTIPLE_COCKPITS("Multiple Cockpits", "Only one cockpit is allowed per ship."),
        MULTIPLE_REACTORS("Multiple Reactors", "Only one reactor is allowed per ship."),
        MULTIPLE_GRAV_DRIVES("Multiple Grav Drives", "Only one grav drive is allowed per ship."),
        MULTIPLE_SHIELD_GENERATORS("Multiple Shield Generators", "Only one shield generator is allowed per ship."),

        UNATTACHED_MODULES("Unattached Modules", "Ship has parts that are not properly connected."),
        INSUFFICIENT_LANDING_GEAR("Insufficient Landing Gear", "Landing gear cannot support ship mass."),
        INSUFFICIENT_POWER("Insufficient Power", "Reactor does not provide enough power for all systems."),
        INSUFFICIENT_ENGINE_THRUST("Insufficient Engine Thrust", "Engines cannot move ship mass effectively."),
        INSUFFICIENT_GRAV_THRUST("Insufficient Grav Thrust", "Grav drive cannot jump ship mass."),

        INVALID_WEAPON_ASSIGNMENT("Invalid Weapon Assignment", "Weapons must be assigned to weapon groups."),
        UNASSIGNED_WEAPONS("Unassigned Weapons", "Ship has weapons not assigned to any group."),
        EXCEEDED_WEAPON_GROUPS("Exceeded Weapon Groups", "Maximum of 3 weapon groups allowed."),

        INVALID_DOCKER_POSITION("Invalid Docker Position", "Docker must be on the top/exterior of ship."),
        BLOCKED_DOCKER("Blocked Docker", "Docker is blocked by other modules."),

        STRUCTURAL_WEAKNESS("Structural Weakness", "Ship structure is not properly braced."),
        CENTER_OF_MASS_OFFSET("Center of Mass Offset", "Ship is unbalanced - center of mass too far from center.");

        public final String title;
        public final String description;

        ErrorType(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    public enum WarningType {
        LOW_MOBILITY("Low Mobility", "Ship may handle poorly due to high mass or low thrust."),
        LOW_FUEL_CAPACITY("Low Fuel Capacity", "Ship has limited operational range."),
        NO_WEAPONS("No Weapons", "Ship has no offensive capabilities."),
        NO_SHIELDS("No Shields", "Ship is vulnerable without shield protection."),
        NO_CARGO("No Cargo Space", "Ship has no cargo capacity."),
        EXPENSIVE_BUILD("Expensive Build", "Ship cost is very high."),
        HEAVY_SHIP("Heavy Ship", "Ship mass is very high - may be slow to maneuver."),
        ASYMMETRIC_DESIGN("Asymmetric Design", "Ship is not symmetrical - may affect handling.");

        public final String title;
        public final String description;

        WarningType(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    public static class ValidationError {
        public final ErrorType type;
        public final String details;

        public ValidationError(ErrorType type) {
            this(type, null);
        }

        public ValidationError(ErrorType type, String details) {
            this.type = type;
            this.details = details;
        }

        @Override
        public String toString() {
            return type.title + (details != null ? ": " + details : "");
        }
    }

    public static class ValidationWarning {
        public final WarningType type;
        public final String details;

        public ValidationWarning(WarningType type) {
            this(type, null);
        }

        public ValidationWarning(WarningType type, String details) {
            this.type = type;
            this.details = details;
        }

        @Override
        public String toString() {
            return type.title + (details != null ? ": " + details : "");
        }
    }

    public ShipValidator(ShipBuilder shipBuilder) {
        this.shipBuilder = shipBuilder;
        this.errors = new Array<>();
        this.warnings = new Array<>();
    }

    /**
     * Validate the entire ship and return whether it's flyable
     */
    public boolean validate() {
        errors.clear();
        warnings.clear();

        // Check required parts
        checkRequiredParts();

        // Check part limits
        checkPartLimits();

        // Check connections
        checkConnections();

        // Check stats and performance
        checkPerformance();

        // Check weapons
        checkWeapons();

        // Check balance and structure
        checkStructure();

        return errors.size == 0;
    }

    /**
     * Check for required parts (Starfield-style requirements)
     */
    private void checkRequiredParts() {
        Array<ShipPart> parts = shipBuilder.getParts();

        boolean hasCockpit = false;
        boolean hasReactor = false;
        boolean hasEngine = false;
        boolean hasGravDrive = false;
        boolean hasFuelTank = false;
        int landingGearCount = 0;

        for (ShipPart part : parts) {
            ShipPartType type = part.getType();

            // Check for cockpit (any type)
            if (type == ShipPartType.HULL_COCKPIT || type == ShipPartType.HULL_COCKPIT_LARGE) {
                hasCockpit = true;
            }

            // Check for reactor
            if (type.getCategory() == ShipPartType.PartCategory.REACTOR) {
                hasReactor = true;
            }

            // Check for engine
            if (type.getCategory() == ShipPartType.PartCategory.ENGINE) {
                hasEngine = true;
            }

            // Check for grav drive
            if (type.getCategory() == ShipPartType.PartCategory.GRAV_DRIVE) {
                hasGravDrive = true;
            }

            // Check for fuel tank
            if (type == ShipPartType.UTIL_FUEL_TANK) {
                hasFuelTank = true;
            }

            // Count landing gear
            if (type.getCategory() == ShipPartType.PartCategory.LANDING_GEAR) {
                landingGearCount++;
            }
        }

        // Critical errors - ship cannot fly without these
        if (!hasCockpit) {
            errors.add(new ValidationError(ErrorType.MISSING_COCKPIT));
        }

        if (!hasReactor) {
            errors.add(new ValidationError(ErrorType.MISSING_REACTOR));
        }

        if (!hasEngine) {
            errors.add(new ValidationError(ErrorType.MISSING_ENGINE));
        }

        if (!hasGravDrive) {
            errors.add(new ValidationError(ErrorType.MISSING_GRAV_DRIVE));
        }

        // Landing gear requirement (need at least 3 for stability)
        if (landingGearCount == 0) {
            errors.add(new ValidationError(ErrorType.MISSING_LANDING_GEAR));
        } else if (landingGearCount < 3) {
            errors.add(new ValidationError(ErrorType.INSUFFICIENT_LANDING_GEAR,
                "Need at least 3 landing gear (have " + landingGearCount + ")"));
        }

        // Warnings for missing but not critical parts
        if (!hasFuelTank) {
            warnings.add(new ValidationWarning(WarningType.LOW_FUEL_CAPACITY,
                "No external fuel tanks - limited range"));
        }
    }

    /**
     * Check part limits (some parts can only have one)
     */
    private void checkPartLimits() {
        Array<ShipPart> parts = shipBuilder.getParts();

        int cockpitCount = 0;
        int reactorCount = 0;
        int gravDriveCount = 0;
        int shieldGenCount = 0;

        for (ShipPart part : parts) {
            ShipPartType type = part.getType();

            if (type == ShipPartType.HULL_COCKPIT || type == ShipPartType.HULL_COCKPIT_LARGE) {
                cockpitCount++;
            }
            if (type.getCategory() == ShipPartType.PartCategory.REACTOR) {
                reactorCount++;
            }
            if (type.getCategory() == ShipPartType.PartCategory.GRAV_DRIVE) {
                gravDriveCount++;
            }
            if (type.getCategory() == ShipPartType.PartCategory.SHIELD || 
                type == ShipPartType.UTIL_SHIELD_GENERATOR) {
                shieldGenCount++;
            }
        }

        if (cockpitCount > 1) {
            errors.add(new ValidationError(ErrorType.MULTIPLE_COCKPITS,
                "Found " + cockpitCount + " cockpits"));
        }

        if (reactorCount > 1) {
            errors.add(new ValidationError(ErrorType.MULTIPLE_REACTORS,
                "Found " + reactorCount + " reactors"));
        }

        if (gravDriveCount > 1) {
            errors.add(new ValidationError(ErrorType.MULTIPLE_GRAV_DRIVES,
                "Found " + gravDriveCount + " grav drives"));
        }

        if (shieldGenCount > 1) {
            errors.add(new ValidationError(ErrorType.MULTIPLE_SHIELD_GENERATORS,
                "Found " + shieldGenCount + " shield generators"));
        }
    }

    /**
     * Check that all parts are properly connected
     */
    private void checkConnections() {
        Array<ShipPart> parts = shipBuilder.getParts();

        if (parts.size == 0) return;

        // Find cockpit as the root
        ShipPart cockpit = null;
        for (ShipPart part : parts) {
            if (part.getType() == ShipPartType.HULL_COCKPIT) {
                cockpit = part;
                break;
            }
        }

        if (cockpit == null) return; // Already flagged as error

        // Check if all parts are connected to cockpit via other parts
        ObjectSet<ShipPart> connected = new ObjectSet<>();
        connected.add(cockpit);

        boolean changed = true;
        int iterations = 0;
        int maxIterations = parts.size * 2;

        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;

            for (ShipPart part : parts) {
                if (connected.contains(part)) continue;

                // Check if this part is close to any connected part
                for (ShipPart connectedPart : connected) {
                    if (isConnected(part, connectedPart)) {
                        connected.add(part);
                        changed = true;
                        break;
                    }
                }
            }
        }

        // Check for unattached modules
        if (connected.size < parts.size) {
            int unattachedCount = parts.size - connected.size;
            errors.add(new ValidationError(ErrorType.UNATTACHED_MODULES,
                unattachedCount + " part(s) not connected to main structure"));
        }
    }

    /**
     * Check if two parts are connected (close enough to touch)
     */
    private boolean isConnected(ShipPart part1, ShipPart part2) {
        Vector3 pos1 = part1.getPosition();
        Vector3 pos2 = part2.getPosition();

        // Simple distance check - in a full implementation, this would check
        // actual attachment points and snap positions
        float distance = pos1.dst(pos2);

        // Parts are considered connected if within 3 units
        return distance < 3.0f;
    }

    /**
     * Check ship performance metrics
     */
    private void checkPerformance() {
        float mass = shipBuilder.getTotalMass();
        float thrust = shipBuilder.getTotalThrust();
        float thrustToWeight = shipBuilder.getThrustToWeight();

        // Check thrust to weight ratio
        if (thrustToWeight < 0.3f) {
            errors.add(new ValidationError(ErrorType.INSUFFICIENT_ENGINE_THRUST,
                String.format("T/W ratio %.2f is too low (minimum 0.3)", thrustToWeight)));
        } else if (thrustToWeight < 0.5f) {
            warnings.add(new ValidationWarning(WarningType.LOW_MOBILITY,
                String.format("T/W ratio %.2f may result in sluggish handling", thrustToWeight)));
        }

        // Check if ship is very heavy
        if (mass > 50000f) {
            warnings.add(new ValidationWarning(WarningType.HEAVY_SHIP,
                String.format("Ship mass %.0f kg is very high", mass)));
        }

        // Check fuel capacity
        float fuel = shipBuilder.getTotalFuel();
        if (fuel < 200f) {
            warnings.add(new ValidationWarning(WarningType.LOW_FUEL_CAPACITY,
                String.format("Fuel capacity %.0f L is low", fuel)));
        }
    }

    /**
     * Check weapon systems
     */
    private void checkWeapons() {
        Array<ShipPart> parts = shipBuilder.getParts();

        int weaponCount = 0;

        for (ShipPart part : parts) {
            if (part.getType().getCategory() == ShipPartType.PartCategory.WEAPON) {
                weaponCount++;
            }
        }

        if (weaponCount == 0) {
            warnings.add(new ValidationWarning(WarningType.NO_WEAPONS));
        }
    }

    /**
     * Check structural integrity and balance
     */
    private void checkStructure() {
        Array<ShipPart> parts = shipBuilder.getParts();

        if (parts.size == 0) return;

        // Calculate center of mass
        Vector3 centerOfMass = new Vector3();
        float totalMass = 0f;

        for (ShipPart part : parts) {
            float partMass = part.getMassContribution();
            Vector3 pos = part.getPosition();

            centerOfMass.add(pos.x * partMass, pos.y * partMass, pos.z * partMass);
            totalMass += partMass;
        }

        if (totalMass > 0) {
            centerOfMass.scl(1f / totalMass);
        }

        // Check if center of mass is too far from origin (0,0,0)
        float comDistance = centerOfMass.len();
        if (comDistance > 5.0f) {
            warnings.add(new ValidationWarning(WarningType.ASYMMETRIC_DESIGN,
                String.format("Center of mass offset: %.1f units", comDistance)));
        }

        // Check shields
        boolean hasShields = false;
        for (ShipPart part : parts) {
            if (part.getShieldContribution() > 0) {
                hasShields = true;
                break;
            }
        }

        if (!hasShields) {
            warnings.add(new ValidationWarning(WarningType.NO_SHIELDS));
        }
    }

    /**
     * Get all validation errors
     */
    public Array<ValidationError> getErrors() {
        return errors;
    }

    /**
     * Get all validation warnings
     */
    public Array<ValidationWarning> getWarnings() {
        return warnings;
    }

    /**
     * Check if ship is valid (has no errors)
     */
    public boolean isValid() {
        return errors.size == 0;
    }

    /**
     * Get a formatted report of all issues
     */
    public String getValidationReport() {
        StringBuilder report = new StringBuilder();

        if (errors.size > 0) {
            report.append("=== ERRORS (").append(errors.size).append(") ===\n");
            for (ValidationError error : errors) {
                report.append("❌ ").append(error.toString()).append("\n");
                report.append("   ").append(error.type.description).append("\n\n");
            }
        }

        if (warnings.size > 0) {
            report.append("=== WARNINGS (").append(warnings.size).append(") ===\n");
            for (ValidationWarning warning : warnings) {
                report.append("⚠️  ").append(warning.toString()).append("\n");
                report.append("   ").append(warning.type.description).append("\n\n");
            }
        }

        if (errors.size == 0 && warnings.size == 0) {
            report.append("✅ SHIP IS VALID\n");
            report.append("All systems nominal. Ready for launch!");
        }

        return report.toString();
    }

    /**
     * Get a summary string for UI display
     */
    public String getSummary() {
        if (errors.size == 0 && warnings.size == 0) {
            return "✅ Ship Valid - Ready to fly";
        } else if (errors.size > 0) {
            return "❌ " + errors.size + " Error(s) - Cannot fly";
        } else {
            return "⚠️  " + warnings.size + " Warning(s) - Can fly";
        }
    }

    /**
     * Quick check if ship can be used (has no critical errors)
     */
    public boolean canFly() {
        validate();
        return errors.size == 0;
    }
}
