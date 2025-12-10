package com.astral.shipbuilding;

/**
 * Enumeration of all ship part types for the ship building system.
 */
public enum ShipPartType {
    // Hull Parts
    HULL_COCKPIT("Cockpit", PartCategory.HULL, 1),
    HULL_NOSE("Nose Section", PartCategory.HULL, 1),
    HULL_FORWARD("Forward Hull", PartCategory.HULL, 3),
    HULL_MID("Mid Hull", PartCategory.HULL, 5),
    HULL_AFT("Aft Hull", PartCategory.HULL, 3),
    HULL_TAIL("Tail Section", PartCategory.HULL, 1),

    // Wing Parts
    WING_STANDARD("Standard Wing", PartCategory.WING, 2),
    WING_SWEPT("Swept Wing", PartCategory.WING, 2),
    WING_DELTA("Delta Wing", PartCategory.WING, 2),
    WING_STUB("Stub Wing", PartCategory.WING, 2),
    WING_VARIABLE("Variable Geometry Wing", PartCategory.WING, 2),

    // Engine Parts
    ENGINE_SMALL("Small Engine", PartCategory.ENGINE, 4),
    ENGINE_MEDIUM("Medium Engine", PartCategory.ENGINE, 2),
    ENGINE_LARGE("Large Engine", PartCategory.ENGINE, 1),
    ENGINE_NACELLE("Engine Nacelle", PartCategory.ENGINE, 2),
    ENGINE_AFTERBURNER("Afterburner Module", PartCategory.ENGINE, 2),

    // Weapon Hardpoints
    WEAPON_MOUNT_SMALL("Small Weapon Mount", PartCategory.WEAPON, 6),
    WEAPON_MOUNT_MEDIUM("Medium Weapon Mount", PartCategory.WEAPON, 4),
    WEAPON_MOUNT_LARGE("Large Weapon Mount", PartCategory.WEAPON, 2),
    WEAPON_TURRET("Turret Mount", PartCategory.WEAPON, 2),
    WEAPON_MISSILE_POD("Missile Pod", PartCategory.WEAPON, 4),

    // Utility Parts
    UTIL_SENSOR_ARRAY("Sensor Array", PartCategory.UTILITY, 2),
    UTIL_ANTENNA("Communication Antenna", PartCategory.UTILITY, 2),
    UTIL_CARGO_POD("Cargo Pod", PartCategory.UTILITY, 2),
    UTIL_FUEL_TANK("External Fuel Tank", PartCategory.UTILITY, 2),
    UTIL_SHIELD_GENERATOR("Shield Generator", PartCategory.UTILITY, 1),

    // Structural Parts
    STRUCT_STRUT("Structural Strut", PartCategory.STRUCTURAL, 8),
    STRUCT_CONNECTOR("Section Connector", PartCategory.STRUCTURAL, 6),
    STRUCT_PYLON("Wing Pylon", PartCategory.STRUCTURAL, 4),
    STRUCT_FIN("Stabilizer Fin", PartCategory.STRUCTURAL, 4),

    // Decorative Parts
    DECOR_STRIPE("Hull Stripe", PartCategory.DECORATIVE, 10),
    DECOR_EMBLEM("Ship Emblem", PartCategory.DECORATIVE, 2),
    DECOR_LIGHT("Navigation Light", PartCategory.DECORATIVE, 8),
    DECOR_EXHAUST("Exhaust Vent", PartCategory.DECORATIVE, 4);

    public enum PartCategory {
        HULL("Hull"),
        WING("Wings"),
        ENGINE("Engines"),
        WEAPON("Weapons"),
        UTILITY("Utility"),
        STRUCTURAL("Structure"),
        DECORATIVE("Decorative");

        private final String displayName;

        PartCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    private final String displayName;
    private final PartCategory category;
    private final int maxCount;

    ShipPartType(String displayName, PartCategory category, int maxCount) {
        this.displayName = displayName;
        this.category = category;
        this.maxCount = maxCount;
    }

    public String getDisplayName() { return displayName; }
    public PartCategory getCategory() { return category; }
    public int getMaxCount() { return maxCount; }
}
