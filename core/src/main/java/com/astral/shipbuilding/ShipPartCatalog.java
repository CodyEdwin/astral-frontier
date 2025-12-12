package com.astral.shipbuilding;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * ShipPartCatalog - Complete catalog of all available ship parts.
 * Starfield-inspired part library with costs, requirements, and metadata.
 */
public class ShipPartCatalog {

    private static ShipPartCatalog instance;

    private final ObjectMap<ShipPartType, PartCatalogEntry> catalog;
    private final Array<PartCatalogEntry> allParts;
    private final ObjectMap<
        ShipPartType.PartCategory,
        Array<PartCatalogEntry>
    > partsByCategory;

    private ShipPartCatalog() {
        this.catalog = new ObjectMap<>();
        this.allParts = new Array<>();
        this.partsByCategory = new ObjectMap<>();

        // Initialize categories
        for (ShipPartType.PartCategory category : ShipPartType.PartCategory.values()) {
            partsByCategory.put(category, new Array<>());
        }

        // Populate catalog
        initializeCatalog();
    }

    public static ShipPartCatalog getInstance() {
        if (instance == null) {
            instance = new ShipPartCatalog();
        }
        return instance;
    }

    /**
     * Initialize the complete part catalog
     */
    private void initializeCatalog() {
        // ============== COCKPITS ==============
        addPart(
            ShipPartType.HULL_COCKPIT,
            new PartCatalogEntry(
                ShipPartType.HULL_COCKPIT,
                "Standard Cockpit",
                "Basic cockpit module with crew seating for 2.",
                5000,
                1, // Level requirement
                new PartStats(500, 50, 0, 0, 0, 2, 0),
                new String[] { "Required", "Command Center" }
            )
        );

        // ============== HULL SECTIONS ==============
        addPart(
            ShipPartType.HULL_FORWARD,
            new PartCatalogEntry(
                ShipPartType.HULL_FORWARD,
                "Forward Hull Section",
                "Reinforced forward hull plating.",
                2000,
                1,
                new PartStats(600, 80, 0, 0, 0, 0, 0),
                new String[] { "Structural" }
            )
        );

        addPart(
            ShipPartType.HULL_MID,
            new PartCatalogEntry(
                ShipPartType.HULL_MID,
                "Mid Hull Section",
                "Central hull section with interior space.",
                2500,
                1,
                new PartStats(800, 100, 0, 0, 0, 0, 50),
                new String[] { "Structural", "Cargo Space" }
            )
        );

        addPart(
            ShipPartType.HULL_AFT,
            new PartCatalogEntry(
                ShipPartType.HULL_AFT,
                "Aft Hull Section",
                "Rear hull section with engine mounting points.",
                2200,
                1,
                new PartStats(650, 85, 0, 0, 0, 0, 0),
                new String[] { "Structural" }
            )
        );

        addPart(
            ShipPartType.HULL_TAIL,
            new PartCatalogEntry(
                ShipPartType.HULL_TAIL,
                "Tail Section",
                "Tapered tail section for aerodynamics.",
                1500,
                1,
                new PartStats(400, 60, 0, 0, 0, 0, 0),
                new String[] { "Structural" }
            )
        );

        addPart(
            ShipPartType.HULL_NOSE,
            new PartCatalogEntry(
                ShipPartType.HULL_NOSE,
                "Nose Cone",
                "Pointed nose cone for reduced drag.",
                1200,
                1,
                new PartStats(300, 40, 0, 0, 0, 0, 0),
                new String[] { "Structural" }
            )
        );

        // ============== WINGS ==============
        addPart(
            ShipPartType.WING_DELTA,
            new PartCatalogEntry(
                ShipPartType.WING_DELTA,
                "Delta Wing",
                "Large delta-shaped wing for stability.",
                3000,
                1,
                new PartStats(150, 30, 0, 0, 0, 0, 0),
                new String[] { "Flight Surface", "Hardpoint" }
            )
        );

        addPart(
            ShipPartType.WING_SWEPT,
            new PartCatalogEntry(
                ShipPartType.WING_SWEPT,
                "Swept Wing",
                "Angled wing for high-speed flight.",
                2800,
                2,
                new PartStats(120, 25, 0, 0, 0, 0, 0),
                new String[] { "Flight Surface", "Hardpoint" }
            )
        );

        addPart(
            ShipPartType.WING_STUB,
            new PartCatalogEntry(
                ShipPartType.WING_STUB,
                "Stub Wing",
                "Short wing stub for weapon mounting.",
                1500,
                1,
                new PartStats(80, 15, 0, 0, 0, 0, 0),
                new String[] { "Hardpoint" }
            )
        );

        addPart(
            ShipPartType.WING_VARIABLE,
            new PartCatalogEntry(
                ShipPartType.WING_VARIABLE,
                "Variable Geometry Wing",
                "Advanced wing with adjustable sweep.",
                5500,
                3,
                new PartStats(180, 35, 0, 0, 0, 0, 0),
                new String[] { "Flight Surface", "Hardpoint", "Advanced" }
            )
        );

        // ============== ENGINES ==============
        addPart(
            ShipPartType.ENGINE_SMALL,
            new PartCatalogEntry(
                ShipPartType.ENGINE_SMALL,
                "Light Engine",
                "Compact ion engine for small craft.",
                4000,
                1,
                new PartStats(200, 0, 0, 40000, 0, 0, 0),
                new String[] { "Propulsion" }
            )
        );

        addPart(
            ShipPartType.ENGINE_MEDIUM,
            new PartCatalogEntry(
                ShipPartType.ENGINE_MEDIUM,
                "Medium Engine",
                "Balanced thrust and efficiency.",
                8000,
                2,
                new PartStats(350, 0, 0, 80000, 0, 0, 0),
                new String[] { "Propulsion" }
            )
        );

        addPart(
            ShipPartType.ENGINE_LARGE,
            new PartCatalogEntry(
                ShipPartType.ENGINE_LARGE,
                "Heavy Engine",
                "Powerful fusion drive for large vessels.",
                15000,
                3,
                new PartStats(500, 0, 0, 150000, 0, 0, 0),
                new String[] { "Propulsion" }
            )
        );

        addPart(
            ShipPartType.ENGINE_NACELLE,
            new PartCatalogEntry(
                ShipPartType.ENGINE_NACELLE,
                "Engine Nacelle",
                "Streamlined engine pod with integrated fuel.",
                12000,
                3,
                new PartStats(400, 0, 0, 120000, 100, 0, 0),
                new String[] { "Propulsion" }
            )
        );

        addPart(
            ShipPartType.ENGINE_AFTERBURNER,
            new PartCatalogEntry(
                ShipPartType.ENGINE_AFTERBURNER,
                "Afterburner",
                "Boost engine for burst speed.",
                6000,
                2,
                new PartStats(150, 0, 0, 60000, 0, 0, 0),
                new String[] { "Propulsion", "Boost" }
            )
        );

        // ============== WEAPONS ==============
        addPart(
            ShipPartType.WEAPON_MOUNT_SMALL,
            new PartCatalogEntry(
                ShipPartType.WEAPON_MOUNT_SMALL,
                "Light Weapon Mount",
                "Mounting point for light weapons.",
                1000,
                1,
                new PartStats(50, 0, 0, 0, 0, 0, 0),
                new String[] { "Weapon Hardpoint" }
            )
        );

        addPart(
            ShipPartType.WEAPON_MOUNT_MEDIUM,
            new PartCatalogEntry(
                ShipPartType.WEAPON_MOUNT_MEDIUM,
                "Medium Weapon Mount",
                "Mounting point for medium weapons.",
                2000,
                2,
                new PartStats(80, 0, 0, 0, 0, 0, 0),
                new String[] { "Weapon Hardpoint" }
            )
        );

        addPart(
            ShipPartType.WEAPON_MOUNT_LARGE,
            new PartCatalogEntry(
                ShipPartType.WEAPON_MOUNT_LARGE,
                "Heavy Weapon Mount",
                "Mounting point for heavy weapons.",
                4000,
                3,
                new PartStats(120, 0, 0, 0, 0, 0, 0),
                new String[] { "Weapon Hardpoint" }
            )
        );

        addPart(
            ShipPartType.WEAPON_TURRET,
            new PartCatalogEntry(
                ShipPartType.WEAPON_TURRET,
                "Point Defense Turret",
                "Automated turret for defense.",
                5000,
                2,
                new PartStats(100, 0, 0, 0, 0, 0, 0),
                new String[] { "Weapon", "Automated" }
            )
        );

        addPart(
            ShipPartType.WEAPON_MISSILE_POD,
            new PartCatalogEntry(
                ShipPartType.WEAPON_MISSILE_POD,
                "Missile Pod",
                "Pod with 6 missile hardpoints.",
                3500,
                2,
                new PartStats(60, 0, 0, 0, 0, 0, 0),
                new String[] { "Weapon", "Missile" }
            )
        );

        // ============== UTILITIES ==============
        addPart(
            ShipPartType.UTIL_SHIELD_GENERATOR,
            new PartCatalogEntry(
                ShipPartType.UTIL_SHIELD_GENERATOR,
                "Shield Generator",
                "Energy shield projector. Only one per ship.",
                10000,
                2,
                new PartStats(200, 0, 100, 0, 0, 0, 0),
                new String[] { "Defense", "Unique" }
            )
        );

        addPart(
            ShipPartType.UTIL_FUEL_TANK,
            new PartCatalogEntry(
                ShipPartType.UTIL_FUEL_TANK,
                "Fuel Tank",
                "Extended fuel capacity.",
                3000,
                1,
                new PartStats(150, 0, 0, 0, 200, 0, 0),
                new String[] { "Fuel" }
            )
        );

        addPart(
            ShipPartType.UTIL_CARGO_POD,
            new PartCatalogEntry(
                ShipPartType.UTIL_CARGO_POD,
                "Cargo Pod",
                "External cargo container.",
                2500,
                1,
                new PartStats(200, 0, 0, 0, 0, 0, 100),
                new String[] { "Cargo" }
            )
        );

        addPart(
            ShipPartType.UTIL_SENSOR_ARRAY,
            new PartCatalogEntry(
                ShipPartType.UTIL_SENSOR_ARRAY,
                "Sensor Array",
                "Advanced sensor package.",
                4000,
                2,
                new PartStats(80, 0, 0, 0, 0, 0, 0),
                new String[] { "Sensors" }
            )
        );

        addPart(
            ShipPartType.UTIL_ANTENNA,
            new PartCatalogEntry(
                ShipPartType.UTIL_ANTENNA,
                "Communications Antenna",
                "Long-range communications.",
                1500,
                1,
                new PartStats(30, 0, 0, 0, 0, 0, 0),
                new String[] { "Communications" }
            )
        );

        // ============== STRUCTURAL ==============
        addPart(
            ShipPartType.STRUCT_PYLON,
            new PartCatalogEntry(
                ShipPartType.STRUCT_PYLON,
                "Structural Pylon",
                "Support strut for mounting components.",
                800,
                1,
                new PartStats(100, 20, 0, 0, 0, 0, 0),
                new String[] { "Structural" }
            )
        );

        addPart(
            ShipPartType.STRUCT_FIN,
            new PartCatalogEntry(
                ShipPartType.STRUCT_FIN,
                "Stabilizer Fin",
                "Vertical stabilizer for control.",
                1000,
                1,
                new PartStats(60, 15, 0, 0, 0, 0, 0),
                new String[] { "Structural", "Control Surface" }
            )
        );

        addPart(
            ShipPartType.STRUCT_STRUT,
            new PartCatalogEntry(
                ShipPartType.STRUCT_STRUT,
                "Support Strut",
                "Reinforcement strut.",
                500,
                1,
                new PartStats(80, 10, 0, 0, 0, 0, 0),
                new String[] { "Structural" }
            )
        );

        // ============== DECORATIVE ==============
        addPart(
            ShipPartType.DECOR_LIGHT,
            new PartCatalogEntry(
                ShipPartType.DECOR_LIGHT,
                "Navigation Light",
                "External lighting.",
                200,
                1,
                new PartStats(5, 0, 0, 0, 0, 0, 0),
                new String[] { "Cosmetic" }
            )
        );
    }

    /**
     * Add a part to the catalog
     */
    private void addPart(ShipPartType type, PartCatalogEntry entry) {
        catalog.put(type, entry);
        allParts.add(entry);
        partsByCategory.get(type.getCategory()).add(entry);
    }

    /**
     * Get catalog entry for a part type
     */
    public PartCatalogEntry getEntry(ShipPartType type) {
        return catalog.get(type);
    }

    /**
     * Get all parts
     */
    public Array<PartCatalogEntry> getAllParts() {
        return allParts;
    }

    /**
     * Get parts by category
     */
    public Array<PartCatalogEntry> getPartsByCategory(
        ShipPartType.PartCategory category
    ) {
        return partsByCategory.get(category);
    }

    /**
     * Get parts available at a given level
     */
    public Array<PartCatalogEntry> getPartsForLevel(int level) {
        Array<PartCatalogEntry> available = new Array<>();
        for (PartCatalogEntry entry : allParts) {
            if (entry.levelRequirement <= level) {
                available.add(entry);
            }
        }
        return available;
    }

    /**
     * Search parts by name
     */
    public Array<PartCatalogEntry> searchParts(String query) {
        Array<PartCatalogEntry> results = new Array<>();
        String lowerQuery = query.toLowerCase();
        for (PartCatalogEntry entry : allParts) {
            if (
                entry.displayName.toLowerCase().contains(lowerQuery) ||
                entry.description.toLowerCase().contains(lowerQuery)
            ) {
                results.add(entry);
            }
        }
        return results;
    }

    /**
     * Get parts with specific tags
     */
    public Array<PartCatalogEntry> getPartsByTag(String tag) {
        Array<PartCatalogEntry> results = new Array<>();
        for (PartCatalogEntry entry : allParts) {
            for (String entryTag : entry.tags) {
                if (entryTag.equalsIgnoreCase(tag)) {
                    results.add(entry);
                    break;
                }
            }
        }
        return results;
    }

    /**
     * Calculate total cost of parts
     */
    public int calculateTotalCost(Array<ShipPart> parts) {
        int total = 0;
        for (ShipPart part : parts) {
            PartCatalogEntry entry = getEntry(part.getType());
            if (entry != null) {
                total += entry.cost;
            }
        }
        return total;
    }

    /**
     * Catalog entry for a ship part
     */
    public static class PartCatalogEntry {

        public final ShipPartType type;
        public final String displayName;
        public final String description;
        public final int cost;
        public final int levelRequirement;
        public final PartStats stats;
        public final String[] tags;

        public PartCatalogEntry(
            ShipPartType type,
            String displayName,
            String description,
            int cost,
            int levelRequirement,
            PartStats stats,
            String[] tags
        ) {
            this.type = type;
            this.displayName = displayName;
            this.description = description;
            this.cost = cost;
            this.levelRequirement = levelRequirement;
            this.stats = stats;
            this.tags = tags;
        }

        public boolean hasTag(String tag) {
            for (String t : tags) {
                if (t.equalsIgnoreCase(tag)) {
                    return true;
                }
            }
            return false;
        }

        public String getFormattedCost() {
            return String.format("%,d", cost);
        }

        public String getCategoryName() {
            return type.getCategory().toString();
        }
    }

    /**
     * Part statistics
     */
    public static class PartStats {

        public final float mass;
        public final float hull;
        public final float shield;
        public final float thrust;
        public final float fuel;
        public final int crew;
        public final int cargo;

        public PartStats(
            float mass,
            float hull,
            float shield,
            float thrust,
            float fuel,
            int crew,
            int cargo
        ) {
            this.mass = mass;
            this.hull = hull;
            this.shield = shield;
            this.thrust = thrust;
            this.fuel = fuel;
            this.crew = crew;
            this.cargo = cargo;
        }
    }
}
