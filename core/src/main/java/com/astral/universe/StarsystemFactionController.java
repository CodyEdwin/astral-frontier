package com.astral.universe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controls faction presence, influence, and conflicts.
 */
public class StarsystemFactionController {
    private List<Faction> factions;
    private Random random;

    public StarsystemFactionController() {
        factions = new ArrayList<>();
        random = new Random();

        // Initialize factions
        initializeFactions();
    }

    private void initializeFactions() {
        String[] factionNames = {"United Earth", "Alien Syndicate", "Free Traders", "Military Alliance"};
        for (String name : factionNames) {
            Faction faction = new Faction(name);
            faction.setInfluence(random.nextFloat() * 100f);
            factions.add(faction);
        }
    }

    public void updateFactions() {
        // Simulate faction dynamics
        for (Faction faction : factions) {
            // Random influence changes
            float change = (random.nextFloat() - 0.5f) * 10f;
            faction.adjustInfluence(change);

            // Clamp influence
            faction.setInfluence(Math.max(0f, Math.min(100f, faction.getInfluence())));
        }

        // Check for conflicts
        checkConflicts();
    }

    private void checkConflicts() {
        // If two factions have high influence, simulate conflict
        for (int i = 0; i < factions.size(); i++) {
            for (int j = i + 1; j < factions.size(); j++) {
                Faction f1 = factions.get(i);
                Faction f2 = factions.get(j);
                if (f1.getInfluence() > 50 && f2.getInfluence() > 50) {
                    // Conflict: reduce both influences
                    f1.adjustInfluence(-5f);
                    f2.adjustInfluence(-5f);
                }
            }
        }
    }

    public List<Faction> getFactions() {
        return new ArrayList<>(factions);
    }

    public Faction getDominantFaction() {
        return factions.stream().max((a, b) -> Float.compare(a.getInfluence(), b.getInfluence())).orElse(null);
    }

    // Inner class for simplicity
    public static class Faction {
        private String name;
        private float influence;

        public Faction(String name) {
            this.name = name;
            this.influence = 0f;
        }

        public String getName() { return name; }
        public float getInfluence() { return influence; }
        public void setInfluence(float influence) { this.influence = influence; }
        public void adjustInfluence(float delta) { this.influence += delta; }
    }
}