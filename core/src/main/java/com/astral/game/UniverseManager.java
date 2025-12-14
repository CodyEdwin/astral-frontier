package com.astral.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.LongMap;

import java.util.Random;

/**
 * Core Universe Manager - Handles infinite procedural universe generation
 * Uses seed-based generation so any star system can be generated on demand
 */
public class UniverseManager {
    
    private static UniverseManager instance;
    
    private final long universeSeed;
    private final Random random;
    
    // Current state
    private StarSystemData currentSystem;
    private Vector3 galacticPosition = new Vector3();
    
    // Cache of generated systems (LRU would be better but this works for now)
    private final LongMap<StarSystemData> systemCache = new LongMap<>();
    private static final int MAX_CACHE_SIZE = 100;
    
    // Galaxy parameters
    public static final float SECTOR_SIZE = 100f; // Light years per sector
    public static final int SYSTEMS_PER_SECTOR = 5;
    public static final float JUMP_RANGE = 50f; // Max jump distance in light years
    
    // Player state
    private long credits = 1000;
    private float fuel = 100f;
    private float maxFuel = 100f;
    
    private UniverseManager(long seed) {
        this.universeSeed = seed;
        this.random = new Random(seed);
        
        // Start in Sol system
        this.currentSystem = generateSystemAt(0, 0, 0);
        this.currentSystem.name = "Sol";
        this.currentSystem.discovered = true;
        this.currentSystem.visited = true;
    }
    
    public static UniverseManager getInstance() {
        if (instance == null) {
            instance = new UniverseManager(System.currentTimeMillis());
        }
        return instance;
    }
    
    public static void initialize(long seed) {
        instance = new UniverseManager(seed);
    }
    
    /**
     * Get or generate a star system at the given sector coordinates
     */
    public StarSystemData getSystemAt(int sectorX, int sectorY, int sectorZ) {
        long sectorSeed = getSectorSeed(sectorX, sectorY, sectorZ);
        
        StarSystemData cached = systemCache.get(sectorSeed);
        if (cached != null) {
            return cached;
        }
        
        return generateSystemAt(sectorX, sectorY, sectorZ);
    }
    
    private StarSystemData generateSystemAt(int sectorX, int sectorY, int sectorZ) {
        long sectorSeed = getSectorSeed(sectorX, sectorY, sectorZ);
        Random sectorRandom = new Random(sectorSeed);
        
        StarSystemData system = new StarSystemData();
        system.seed = sectorSeed;
        system.sectorX = sectorX;
        system.sectorY = sectorY;
        system.sectorZ = sectorZ;
        
        // Position within sector (with some variance)
        system.position = new Vector3(
            sectorX * SECTOR_SIZE + (sectorRandom.nextFloat() - 0.5f) * SECTOR_SIZE * 0.8f,
            sectorY * SECTOR_SIZE + (sectorRandom.nextFloat() - 0.5f) * SECTOR_SIZE * 0.3f,
            sectorZ * SECTOR_SIZE + (sectorRandom.nextFloat() - 0.5f) * SECTOR_SIZE * 0.8f
        );
        
        // Generate star type
        float starRoll = sectorRandom.nextFloat();
        if (starRoll < 0.6f) {
            system.starType = StarType.RED_DWARF;
            system.starColor = new float[]{1f, 0.4f, 0.3f};
        } else if (starRoll < 0.85f) {
            system.starType = StarType.YELLOW;
            system.starColor = new float[]{1f, 0.95f, 0.8f};
        } else if (starRoll < 0.95f) {
            system.starType = StarType.BLUE_GIANT;
            system.starColor = new float[]{0.7f, 0.8f, 1f};
        } else {
            system.starType = StarType.WHITE_DWARF;
            system.starColor = new float[]{0.9f, 0.9f, 1f};
        }
        
        // Generate name
        system.name = generateStarName(sectorSeed);
        
        // Generate planets (1-8 based on star type)
        int basePlanets = switch (system.starType) {
            case RED_DWARF -> 1 + sectorRandom.nextInt(4);
            case YELLOW -> 3 + sectorRandom.nextInt(6);
            case BLUE_GIANT -> 2 + sectorRandom.nextInt(3);
            case WHITE_DWARF -> sectorRandom.nextInt(3);
        };
        
        for (int i = 0; i < basePlanets; i++) {
            PlanetData planet = generatePlanet(sectorSeed, i, system.starType, sectorRandom);
            system.planets.add(planet);
        }
        
        // Economy/faction
        system.economyType = EconomyType.values()[sectorRandom.nextInt(EconomyType.values().length)];
        system.techLevel = 1 + sectorRandom.nextInt(10);
        system.dangerLevel = sectorRandom.nextInt(5);
        system.hasStation = sectorRandom.nextFloat() < 0.7f;
        
        // Cache it
        if (systemCache.size >= MAX_CACHE_SIZE) {
            // Simple eviction - just clear half
            LongMap.Keys keys = systemCache.keys();
            int count = 0;
            while (keys.hasNext && count < MAX_CACHE_SIZE / 2) {
                systemCache.remove(keys.next());
                count++;
            }
        }
        systemCache.put(sectorSeed, system);
        
        return system;
    }
    
    private PlanetData generatePlanet(long systemSeed, int index, StarType starType, Random r) {
        long planetSeed = systemSeed ^ (index * 0x9E3779B97F4A7C15L);
        Random pr = new Random(planetSeed);
        
        PlanetData planet = new PlanetData();
        planet.seed = planetSeed;
        planet.index = index;
        planet.orbitDistance = 50f + index * 80f + pr.nextFloat() * 40f;
        planet.size = 0.3f + pr.nextFloat() * 1.5f;
        
        // Planet type based on distance and star
        float habZoneMin = switch (starType) {
            case RED_DWARF -> 30f;
            case YELLOW -> 80f;
            case BLUE_GIANT -> 200f;
            case WHITE_DWARF -> 20f;
        };
        float habZoneMax = habZoneMin * 2f;
        
        boolean inHabZone = planet.orbitDistance >= habZoneMin && planet.orbitDistance <= habZoneMax;
        
        if (planet.orbitDistance < habZoneMin * 0.5f) {
            planet.type = PlanetType.MOLTEN;
        } else if (inHabZone && pr.nextFloat() < 0.3f) {
            planet.type = PlanetType.TERRAN;
        } else if (inHabZone && pr.nextFloat() < 0.5f) {
            planet.type = PlanetType.OCEAN;
        } else if (planet.orbitDistance > habZoneMax * 1.5f) {
            planet.type = pr.nextFloat() < 0.5f ? PlanetType.ICE : PlanetType.GAS_GIANT;
        } else {
            float roll = pr.nextFloat();
            if (roll < 0.3f) planet.type = PlanetType.DESERT;
            else if (roll < 0.5f) planet.type = PlanetType.ROCKY;
            else if (roll < 0.7f) planet.type = PlanetType.TOXIC;
            else planet.type = PlanetType.BARREN;
        }
        
        // Name
        planet.name = generatePlanetName(planetSeed, index);
        
        // Resources based on type
        planet.resources = generatePlanetResources(planet.type, pr);
        
        // Can land on non-gas giants
        planet.landable = planet.type != PlanetType.GAS_GIANT;
        
        // Moons
        int moonCount = pr.nextInt(planet.type == PlanetType.GAS_GIANT ? 6 : 3);
        for (int m = 0; m < moonCount; m++) {
            MoonData moon = new MoonData();
            moon.name = planet.name + " " + (char)('a' + m);
            moon.size = 0.1f + pr.nextFloat() * 0.3f;
            moon.resources = generatePlanetResources(PlanetType.ROCKY, pr);
            planet.moons.add(moon);
        }
        
        return planet;
    }
    
    private ResourceDeposit[] generatePlanetResources(PlanetType type, Random r) {
        Array<ResourceDeposit> deposits = new Array<>();
        
        // Common resources
        if (r.nextFloat() < 0.8f) {
            deposits.add(new ResourceDeposit(ResourceType.IRON, 100 + r.nextInt(500)));
        }
        if (r.nextFloat() < 0.6f) {
            deposits.add(new ResourceDeposit(ResourceType.COPPER, 50 + r.nextInt(300)));
        }
        
        // Type-specific resources
        switch (type) {
            case TERRAN -> {
                deposits.add(new ResourceDeposit(ResourceType.WOOD, 200 + r.nextInt(800)));
                deposits.add(new ResourceDeposit(ResourceType.WATER, 500 + r.nextInt(1000)));
                if (r.nextFloat() < 0.5f) deposits.add(new ResourceDeposit(ResourceType.FISH, 100 + r.nextInt(400)));
            }
            case OCEAN -> {
                deposits.add(new ResourceDeposit(ResourceType.WATER, 1000 + r.nextInt(2000)));
                deposits.add(new ResourceDeposit(ResourceType.FISH, 300 + r.nextInt(700)));
                if (r.nextFloat() < 0.3f) deposits.add(new ResourceDeposit(ResourceType.RARE_MINERALS, 20 + r.nextInt(50)));
            }
            case DESERT -> {
                deposits.add(new ResourceDeposit(ResourceType.SILICON, 200 + r.nextInt(500)));
                if (r.nextFloat() < 0.4f) deposits.add(new ResourceDeposit(ResourceType.GOLD, 30 + r.nextInt(100)));
            }
            case ICE -> {
                deposits.add(new ResourceDeposit(ResourceType.WATER, 300 + r.nextInt(600)));
                if (r.nextFloat() < 0.5f) deposits.add(new ResourceDeposit(ResourceType.RARE_GAS, 50 + r.nextInt(150)));
            }
            case ROCKY, BARREN -> {
                if (r.nextFloat() < 0.5f) deposits.add(new ResourceDeposit(ResourceType.TITANIUM, 50 + r.nextInt(200)));
                if (r.nextFloat() < 0.3f) deposits.add(new ResourceDeposit(ResourceType.URANIUM, 20 + r.nextInt(80)));
            }
            case MOLTEN -> {
                if (r.nextFloat() < 0.6f) deposits.add(new ResourceDeposit(ResourceType.RARE_METALS, 40 + r.nextInt(120)));
            }
            case TOXIC -> {
                if (r.nextFloat() < 0.5f) deposits.add(new ResourceDeposit(ResourceType.EXOTIC_MATTER, 10 + r.nextInt(50)));
            }
            default -> {}
        }
        
        return deposits.toArray(ResourceDeposit.class);
    }
    
    private long getSectorSeed(int x, int y, int z) {
        // Combine coordinates with universe seed for unique sector seed
        long hash = universeSeed;
        hash ^= x * 0x9E3779B97F4A7C15L;
        hash ^= y * 0x6A09E667F3BCC909L;
        hash ^= z * 0xBB67AE8584CAA73BL;
        return hash;
    }
    
    private String generateStarName(long seed) {
        String[] prefixes = {"Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta", "Kappa", "Lambda"};
        String[] names = {"Centauri", "Cygni", "Eridani", "Orionis", "Pegasi", "Draconis", "Ursae", "Lyrae", "Aquilae", "Tauri",
                         "Kepler", "Proxima", "Barnard", "Sirius", "Vega", "Altair", "Rigel", "Polaris", "Arcturus", "Capella"};
        String[] suffixes = {"Prime", "Major", "Minor", "", "", "", "II", "III", "IV", "V"};
        
        Random r = new Random(seed);
        String name = "";
        if (r.nextFloat() < 0.4f) {
            name = prefixes[r.nextInt(prefixes.length)] + " ";
        }
        name += names[r.nextInt(names.length)];
        String suffix = suffixes[r.nextInt(suffixes.length)];
        if (!suffix.isEmpty()) {
            name += " " + suffix;
        }
        return name;
    }
    
    private String generatePlanetName(long seed, int index) {
        String[] prefixes = {"New", "Nova", "Neo", "", "", ""};
        String[] names = {"Terra", "Gaia", "Eden", "Haven", "Reach", "Zenith", "Horizon", "Frontier", 
                         "Aurora", "Crimson", "Azure", "Amber", "Jade", "Obsidian", "Crystal"};
        
        Random r = new Random(seed);
        String prefix = prefixes[r.nextInt(prefixes.length)];
        String name = names[r.nextInt(names.length)];
        
        if (!prefix.isEmpty()) {
            return prefix + " " + name;
        }
        // Use Roman numerals for some
        if (r.nextFloat() < 0.3f) {
            String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII"};
            return name + " " + numerals[index % numerals.length];
        }
        return name;
    }
    
    /**
     * Get all star systems within jump range of current position
     */
    public Array<StarSystemData> getSystemsInRange() {
        return getSystemsInRange(galacticPosition, JUMP_RANGE);
    }
    
    public Array<StarSystemData> getSystemsInRange(Vector3 position, float range) {
        Array<StarSystemData> systems = new Array<>();
        
        int sectorRange = (int) Math.ceil(range / SECTOR_SIZE) + 1;
        int currentSectorX = (int) Math.floor(position.x / SECTOR_SIZE);
        int currentSectorY = (int) Math.floor(position.y / SECTOR_SIZE);
        int currentSectorZ = (int) Math.floor(position.z / SECTOR_SIZE);
        
        for (int dx = -sectorRange; dx <= sectorRange; dx++) {
            for (int dy = -sectorRange; dy <= sectorRange; dy++) {
                for (int dz = -sectorRange; dz <= sectorRange; dz++) {
                    StarSystemData system = getSystemAt(
                        currentSectorX + dx,
                        currentSectorY + dy,
                        currentSectorZ + dz
                    );
                    
                    float dist = position.dst(system.position);
                    if (dist <= range && dist > 0.1f) {
                        system.distanceFromPlayer = dist;
                        systems.add(system);
                    }
                }
            }
        }
        
        // Sort by distance
        systems.sort((a, b) -> Float.compare(a.distanceFromPlayer, b.distanceFromPlayer));
        
        return systems;
    }
    
    /**
     * Jump to a star system (consumes fuel)
     */
    public boolean jumpToSystem(StarSystemData target) {
        float distance = galacticPosition.dst(target.position);
        float fuelCost = distance * 0.5f; // 0.5 fuel per light year
        
        if (fuel < fuelCost) {
            return false; // Not enough fuel
        }
        
        fuel -= fuelCost;
        galacticPosition.set(target.position);
        currentSystem = target;
        target.visited = true;
        target.discovered = true;
        
        return true;
    }
    
    /**
     * Calculate fuel cost to reach a system
     */
    public float getFuelCost(StarSystemData target) {
        return galacticPosition.dst(target.position) * 0.5f;
    }
    
    // Getters and setters
    public StarSystemData getCurrentSystem() { return currentSystem; }
    public Vector3 getGalacticPosition() { return galacticPosition; }
    public long getCredits() { return credits; }
    public void addCredits(long amount) { credits += amount; }
    public boolean spendCredits(long amount) {
        if (credits >= amount) {
            credits -= amount;
            return true;
        }
        return false;
    }
    public float getFuel() { return fuel; }
    public float getMaxFuel() { return maxFuel; }
    public void addFuel(float amount) { fuel = Math.min(fuel + amount, maxFuel); }
    public long getUniverseSeed() { return universeSeed; }
    
    // === Inner Data Classes ===
    
    public enum StarType {
        RED_DWARF, YELLOW, BLUE_GIANT, WHITE_DWARF
    }
    
    public enum PlanetType {
        TERRAN, OCEAN, DESERT, ICE, ROCKY, BARREN, MOLTEN, TOXIC, GAS_GIANT
    }
    
    public enum EconomyType {
        AGRICULTURAL, INDUSTRIAL, MINING, TECH, MILITARY, TRADING
    }
    
    public enum ResourceType {
        IRON, COPPER, GOLD, TITANIUM, URANIUM, SILICON, RARE_METALS, RARE_MINERALS,
        WOOD, WATER, FISH, RARE_GAS, EXOTIC_MATTER, FUEL
    }
    
    public static class StarSystemData {
        public long seed;
        public int sectorX, sectorY, sectorZ;
        public Vector3 position;
        public String name;
        public StarType starType;
        public float[] starColor;
        public Array<PlanetData> planets = new Array<>();
        public EconomyType economyType;
        public int techLevel;
        public int dangerLevel;
        public boolean hasStation;
        public boolean discovered;
        public boolean visited;
        public float distanceFromPlayer; // Temp for sorting
    }
    
    public static class PlanetData {
        public long seed;
        public int index;
        public String name;
        public PlanetType type;
        public float orbitDistance;
        public float size;
        public boolean landable;
        public ResourceDeposit[] resources;
        public Array<MoonData> moons = new Array<>();
    }
    
    public static class MoonData {
        public String name;
        public float size;
        public ResourceDeposit[] resources;
    }
    
    public static class ResourceDeposit {
        public ResourceType type;
        public int amount;
        public int extracted;
        
        public ResourceDeposit(ResourceType type, int amount) {
            this.type = type;
            this.amount = amount;
            this.extracted = 0;
        }
        
        public int getRemaining() {
            return amount - extracted;
        }
    }
}
