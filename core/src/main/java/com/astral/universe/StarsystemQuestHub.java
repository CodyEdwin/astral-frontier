package com.astral.universe;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates dynamic quests for the system based on system properties.
 */
public class StarsystemQuestHub {
    private List<Quest> activeQuests;
    private Random random;

    public StarsystemQuestHub() {
        activeQuests = new ArrayList<>();
        random = new Random();
    }

    public void generateQuests() {
        // Generate new quests periodically
        if (random.nextFloat() < 0.05f) { // 5% chance per update
            Quest quest = generateRandomQuest();
            activeQuests.add(quest);
        }

        // Limit active quests
        while (activeQuests.size() > 10) {
            activeQuests.remove(0);
        }
    }

    private Quest generateRandomQuest() {
        String[] objectives = {"Deliver cargo to planet", "Eliminate threat", "Explore anomaly", "Trade goods", "Assist faction"};
        String objective = objectives[random.nextInt(objectives.length)];
        int reward = random.nextInt(10000) + 1000; // 1000-11000 credits
        return new Quest("System Quest: " + objective, objective, reward);
    }

    public List<Quest> getActiveQuests() {
        return new ArrayList<>(activeQuests);
    }

    public void completeQuest(Quest quest) {
        activeQuests.remove(quest);
        // Grant reward (would integrate with progression)
    }

    // Inner class
    public static class Quest {
        private String title;
        private String description;
        private int reward;

        public Quest(String title, String description, int reward) {
            this.title = title;
            this.description = description;
            this.reward = reward;
        }

        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public int getReward() { return reward; }
    }
}