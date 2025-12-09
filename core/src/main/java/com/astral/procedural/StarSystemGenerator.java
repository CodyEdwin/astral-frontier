package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

/**
 * Generates a star system with planets and moons
 */
public class StarSystemGenerator {

    private final long seed;
    private final Random random;

    public StarSystemGenerator(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public StarSystem generate(Vector3 galacticPosition) {
        StarSystem system = new StarSystem(seed);

        // Generate primary star
        Star primaryStar = generateStar(seed);
        system.setPrimaryStar(primaryStar);

        // Binary star chance (15%)
        if (random.nextFloat() < 0.15f) {
            Star secondaryStar = generateCompanionStar(primaryStar);
            system.setSecondaryStar(secondaryStar);
        }

        // Determine planet count based on star type
        int planetCount = calculatePlanetCount(primaryStar.getType());

        // Generate planetary orbits using modified Titius-Bode
        float[] orbitalDistances = generateOrbitalDistances(
                primaryStar.getHabitableZoneInner(),
                primaryStar.getHabitableZoneOuter(),
                planetCount
        );

        // Generate planets
        for (int i = 0; i < planetCount; i++) {
            long planetSeed = seed ^ (i * 0x517CC1B727220A95L);
            Planet planet = new PlanetGenerator(planetSeed).generate(
                    orbitalDistances[i],
                    primaryStar
            );
            system.addPlanet(planet);

            // Generate moons
            int moonCount = calculateMoonCount(planet.getType());
            for (int j = 0; j < moonCount; j++) {
                long moonSeed = planetSeed ^ (j * 0x1B873593L);
                Moon moon = generateMoon(moonSeed, planet);
                planet.addMoon(moon);
            }
        }

        // Generate asteroid belt (50% chance)
        if (random.nextFloat() < 0.5f) {
            float beltDistance = orbitalDistances.length > 3 ?
                    (orbitalDistances[2] + orbitalDistances[3]) / 2f :
                    primaryStar.getHabitableZoneOuter() * 2f;
            system.setAsteroidBelt(beltDistance, beltDistance * 0.2f);
        }

        return system;
    }

    private Star generateStar(long starSeed) {
        random.setSeed(starSeed);

        // Star type distribution based on real stellar populations
        float roll = random.nextFloat();
        Star.StarType type;

        if (roll < 0.001f) type = Star.StarType.O;
        else if (roll < 0.006f) type = Star.StarType.B;
        else if (roll < 0.026f) type = Star.StarType.A;
        else if (roll < 0.076f) type = Star.StarType.F;
        else if (roll < 0.176f) type = Star.StarType.G;
        else if (roll < 0.326f) type = Star.StarType.K;
        else type = Star.StarType.M;

        Star star = new Star(starSeed, type);
        star.setName(generateStarName(starSeed));

        return star;
    }

    private Star generateCompanionStar(Star primary) {
        // Companion is typically same or smaller class
        int typeIndex = primary.getType().ordinal();
        int companionIndex = Math.min(Star.StarType.values().length - 1,
                typeIndex + random.nextInt(3));

        Star.StarType type = Star.StarType.values()[companionIndex];
        Star companion = new Star(seed ^ 0xDEADBEEFL, type);
        companion.setName(primary.getName() + " B");

        return companion;
    }

    private int calculatePlanetCount(Star.StarType type) {
        return switch (type) {
            case O, B -> random.nextInt(3) + 1;      // 1-3 (hostile radiation)
            case A, F -> random.nextInt(5) + 3;      // 3-7
            case G -> random.nextInt(6) + 4;         // 4-9 (Sun-like, optimal)
            case K -> random.nextInt(7) + 5;         // 5-11
            case M -> random.nextInt(5) + 2;         // 2-6 (dim, close habitable zone)
        };
    }

    private float[] generateOrbitalDistances(float innerHz, float outerHz, int count) {
        float[] distances = new float[count];

        // Modified Titius-Bode: d = 0.4 + 0.3 * 2^n (AU)
        for (int n = 0; n < count; n++) {
            float base = 0.4f + 0.3f * (float) Math.pow(2, n);
            float variance = (random.nextFloat() - 0.5f) * 0.2f * base;
            distances[n] = base + variance;
        }

        return distances;
    }

    private int calculateMoonCount(Planet.PlanetType type) {
        return switch (type) {
            case GAS_GIANT -> random.nextInt(9); // 0-8 moons
            case ICE_GIANT -> random.nextInt(6); // 0-5 moons
            case TERRAN, OCEAN -> random.nextInt(3); // 0-2 moons
            default -> random.nextInt(2); // 0-1 moons
        };
    }

    private Moon generateMoon(long moonSeed, Planet parent) {
        Random r = new Random(moonSeed);

        Moon moon = new Moon(moonSeed);
        moon.setName(parent.getName() + " " + toRomanNumeral(parent.getMoons().size + 1));

        // Moon size relative to parent
        float sizeRatio = 0.05f + r.nextFloat() * 0.2f;
        moon.setRadius(parent.getRadius() * sizeRatio);

        // Orbital distance from parent
        float orbitDistance = parent.getRadius() * (3f + r.nextFloat() * 10f);
        moon.setOrbitalDistance(orbitDistance);

        // Moon type (mostly rocky/ice)
        if (r.nextFloat() < 0.7f) {
            moon.setType(Moon.MoonType.ROCKY);
        } else {
            moon.setType(Moon.MoonType.ICE);
        }

        return moon;
    }

    private String generateStarName(long seed) {
        String[] prefixes = {"Alpha", "Beta", "Gamma", "Delta", "Epsilon", "Zeta", "Eta", "Theta"};
        String[] constellations = {"Centauri", "Eridani", "Cygni", "Draconis", "Pegasi",
                "Orionis", "Lyrae", "Aquilae", "Tauri", "Leonis"};

        Random r = new Random(seed);
        return prefixes[r.nextInt(prefixes.length)] + " " +
                constellations[r.nextInt(constellations.length)];
    }

    private String toRomanNumeral(int num) {
        String[] numerals = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        if (num >= 1 && num <= 10) return numerals[num - 1];
        return String.valueOf(num);
    }
}
