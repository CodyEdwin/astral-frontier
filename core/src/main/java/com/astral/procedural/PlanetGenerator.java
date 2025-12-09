package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import java.util.Random;

/**
 * Generates procedural planets
 */
public class PlanetGenerator {

    private final long seed;
    private final Random random;

    public PlanetGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public Planet generate(float orbitalDistance, Star star) {
        Planet planet = new Planet(seed);

        // Determine planet type based on distance and star properties
        Planet.PlanetType type = determinePlanetType(orbitalDistance, star);
        planet.setType(type);

        // Generate name
        planet.setName(generatePlanetName());

        // Physical properties
        float radius = type.minRadius + random.nextFloat() * (type.maxRadius - type.minRadius);
        planet.setRadius(radius);

        // Mass scales roughly with radius^3 for rocky, less for gas giants
        float density = type == Planet.PlanetType.GAS_GIANT ? 0.7f :
                type == Planet.PlanetType.ICE_GIANT ? 1.3f : 5.5f;
        planet.setMass((float) Math.pow(radius, 3) * density / 5.5f);

        // Surface gravity
        planet.setGravity(planet.getMass() / (radius * radius));

        // Orbital properties
        planet.setOrbitalDistance(orbitalDistance);
        // Kepler's third law: P^2 = a^3 / M (simplified)
        planet.setOrbitalPeriod((float) Math.pow(orbitalDistance, 1.5) / (float) Math.sqrt(star.getMass()));

        // Temperature based on distance and star luminosity
        float equilibriumTemp = 278f * (float) Math.pow(star.getLuminosity(), 0.25f) /
                (float) Math.sqrt(orbitalDistance);
        planet.setTemperature(equilibriumTemp);

        // Atmosphere
        generateAtmosphere(planet, type);

        // Water and habitability
        generateSurfaceConditions(planet, star);

        // Resources
        generateResources(planet, type);

        return planet;
    }

    private Planet.PlanetType determinePlanetType(float distance, Star star) {
        float innerHz = star.getHabitableZoneInner();
        float outerHz = star.getHabitableZoneOuter();
        float frostLine = outerHz * 2.5f;

        // Too close - barren or volcanic
        if (distance < innerHz * 0.5f) {
            return random.nextFloat() < 0.7f ? Planet.PlanetType.BARREN : Planet.PlanetType.VOLCANIC;
        }

        // Inner system - desert or barren
        if (distance < innerHz) {
            return random.nextFloat() < 0.6f ? Planet.PlanetType.DESERT : Planet.PlanetType.BARREN;
        }

        // Habitable zone
        if (distance >= innerHz && distance <= outerHz) {
            float roll = random.nextFloat();
            if (roll < 0.3f) return Planet.PlanetType.TERRAN;
            if (roll < 0.5f) return Planet.PlanetType.OCEAN;
            if (roll < 0.7f) return Planet.PlanetType.DESERT;
            return Planet.PlanetType.BARREN;
        }

        // Outer habitable to frost line
        if (distance <= frostLine) {
            float roll = random.nextFloat();
            if (roll < 0.4f) return Planet.PlanetType.FROZEN;
            if (roll < 0.6f) return Planet.PlanetType.BARREN;
            return Planet.PlanetType.DWARF;
        }

        // Beyond frost line - gas/ice giants more common
        float roll = random.nextFloat();
        if (roll < 0.4f) return Planet.PlanetType.GAS_GIANT;
        if (roll < 0.7f) return Planet.PlanetType.ICE_GIANT;
        if (roll < 0.9f) return Planet.PlanetType.FROZEN;
        return Planet.PlanetType.DWARF;
    }

    private void generateAtmosphere(Planet planet, Planet.PlanetType type) {
        switch (type) {
            case TERRAN, OCEAN -> {
                planet.setHasAtmosphere(true);
                planet.setAtmosphereDensity(0.5f + random.nextFloat());
                planet.setAtmosphereColor(new Color(0.5f, 0.7f, 1f, 0.5f));
            }
            case DESERT -> {
                planet.setHasAtmosphere(random.nextFloat() < 0.7f);
                planet.setAtmosphereDensity(random.nextFloat() * 0.5f);
                planet.setAtmosphereColor(new Color(0.9f, 0.7f, 0.5f, 0.3f));
            }
            case VOLCANIC -> {
                planet.setHasAtmosphere(true);
                planet.setAtmosphereDensity(1f + random.nextFloat() * 2f);
                planet.setAtmosphereColor(new Color(0.8f, 0.4f, 0.2f, 0.6f));
            }
            case FROZEN -> {
                planet.setHasAtmosphere(random.nextFloat() < 0.5f);
                planet.setAtmosphereDensity(random.nextFloat() * 0.3f);
                planet.setAtmosphereColor(new Color(0.8f, 0.9f, 1f, 0.2f));
            }
            case GAS_GIANT, ICE_GIANT -> {
                planet.setHasAtmosphere(true);
                planet.setAtmosphereDensity(100f + random.nextFloat() * 900f);
                Color color = type == Planet.PlanetType.GAS_GIANT ?
                        new Color(0.8f, 0.6f, 0.4f, 1f) :
                        new Color(0.5f, 0.7f, 0.9f, 1f);
                planet.setAtmosphereColor(color);
            }
            default -> {
                planet.setHasAtmosphere(false);
                planet.setAtmosphereDensity(0);
            }
        }
    }

    private void generateSurfaceConditions(Planet planet, Star star) {
        Planet.PlanetType type = planet.getType();

        // Water coverage
        switch (type) {
            case OCEAN -> planet.setWaterCoverage(0.85f + random.nextFloat() * 0.15f);
            case TERRAN -> planet.setWaterCoverage(0.4f + random.nextFloat() * 0.3f);
            case FROZEN -> planet.setWaterCoverage(random.nextFloat() * 0.2f); // Ice
            default -> planet.setWaterCoverage(0);
        }

        // Habitability check
        float temp = planet.getTemperature();
        boolean tempOk = temp > 250 && temp < 320; // Rough habitable range
        boolean atmosOk = planet.hasAtmosphere() && planet.getAtmosphereDensity() > 0.3f
                && planet.getAtmosphereDensity() < 3f;
        boolean gravityOk = planet.getGravity() > 0.4f && planet.getGravity() < 2f;

        planet.setHabitable(type.canBeHabitable && tempOk && atmosOk && gravityOk);
    }

    private void generateResources(Planet planet, Planet.PlanetType type) {
        // Resource types: 0=Iron, 1=Water, 2=Organics, 3=RareMetals, 4=Gas, 5=Crystals, 6=Radioactive, 7=Exotic
        switch (type) {
            case BARREN -> {
                planet.setResourceAbundance(0, 0.3f + random.nextFloat() * 0.4f); // Iron
                planet.setResourceAbundance(3, random.nextFloat() * 0.3f);        // Rare metals
                planet.setResourceAbundance(6, random.nextFloat() * 0.2f);        // Radioactive
            }
            case DESERT -> {
                planet.setResourceAbundance(0, 0.2f + random.nextFloat() * 0.3f);
                planet.setResourceAbundance(5, random.nextFloat() * 0.4f);        // Crystals
            }
            case VOLCANIC -> {
                planet.setResourceAbundance(0, 0.5f + random.nextFloat() * 0.5f);
                planet.setResourceAbundance(3, 0.2f + random.nextFloat() * 0.4f);
                planet.setResourceAbundance(5, 0.3f + random.nextFloat() * 0.4f);
            }
            case FROZEN -> {
                planet.setResourceAbundance(1, 0.8f + random.nextFloat() * 0.2f); // Water (ice)
                planet.setResourceAbundance(4, random.nextFloat() * 0.3f);        // Gas
            }
            case TERRAN -> {
                planet.setResourceAbundance(0, 0.3f + random.nextFloat() * 0.3f);
                planet.setResourceAbundance(1, 0.6f + random.nextFloat() * 0.4f);
                planet.setResourceAbundance(2, 0.7f + random.nextFloat() * 0.3f); // Organics
            }
            case OCEAN -> {
                planet.setResourceAbundance(1, 0.95f + random.nextFloat() * 0.05f);
                planet.setResourceAbundance(2, 0.5f + random.nextFloat() * 0.3f);
            }
            case GAS_GIANT -> {
                planet.setResourceAbundance(4, 0.9f + random.nextFloat() * 0.1f);
                planet.setResourceAbundance(7, random.nextFloat() * 0.1f);        // Exotic
            }
            case ICE_GIANT -> {
                planet.setResourceAbundance(1, 0.4f + random.nextFloat() * 0.3f);
                planet.setResourceAbundance(4, 0.6f + random.nextFloat() * 0.3f);
            }
        }
    }

    private String generatePlanetName() {
        String[] prefixes = {"New", "Alpha", "Beta", "Outer", "Inner", "Far", "Prime"};
        String[] names = {"Terra", "Haven", "Reach", "Hope", "Fortune", "Frontier",
                "Vista", "Horizon", "Eden", "Prospect", "Sanctuary"};
        String[] suffixes = {"I", "II", "III", "IV", "V", "Prime", "Major", "Minor"};

        StringBuilder sb = new StringBuilder();

        if (random.nextFloat() < 0.3f) {
            sb.append(prefixes[random.nextInt(prefixes.length)]).append(" ");
        }

        sb.append(names[random.nextInt(names.length)]);

        if (random.nextFloat() < 0.5f) {
            sb.append(" ").append(suffixes[random.nextInt(suffixes.length)]);
        }

        return sb.toString();
    }
}
