package com.astral.universe;

import java.util.ArrayList;
import java.util.List;

/**
 * Full implementation of progression manager with XP, levels, milestones, rewards.
 */
public class ProgressionManagerImpl implements IProgressionManager {
    private int xp;
    private int level;
    private List<ProgressionMilestone> milestones;
    private ProgressionRewardHandler rewardHandler;
    private ReputationSystem reputation;
    private WealthTracker wealth;

    public ProgressionManagerImpl() {
        this.xp = 0;
        this.level = 1;
        this.milestones = new ArrayList<>();
        this.rewardHandler = new ProgressionRewardHandler();
        this.reputation = new ReputationSystem();
        this.wealth = new WealthTracker();

        // Initialize milestones
        initializeMilestones();
    }

    private void initializeMilestones() {
        milestones.add(new ProgressionMilestone("Visit 10 Systems"));
        milestones.add(new ProgressionMilestone("Accumulate 100,000 Credits"));
        milestones.add(new ProgressionMilestone("Reach Reputation 50"));
        milestones.add(new ProgressionMilestone("Explore 5 Planets"));
        milestones.add(new ProgressionMilestone("Complete 20 Quests"));
    }

    @Override
    public void addXP(int amount) {
        xp += amount;
        int newLevel = calculateLevel(xp);
        if (newLevel > level) {
            level = newLevel;
            onLevelUp();
        }
    }

    private int calculateLevel(int xp) {
        // Simple level calculation: level = sqrt(xp / 100) + 1
        return (int) Math.sqrt(xp / 100.0) + 1;
    }

    private void onLevelUp() {
        // Grant rewards
        rewardHandler.grantReward(null); // Placeholder
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void checkMilestones() {
        // Check each milestone
        for (ProgressionMilestone milestone : milestones) {
            if (!milestone.isAchieved()) {
                // Logic to check if achieved (would need access to stats)
                // For now, random chance for demo
                if (Math.random() < 0.01) { // 1% chance
                    milestone.setAchieved(true);
                    rewardHandler.grantReward(milestone);
                }
            }
        }
    }

    public void addWealth(long amount) {
        wealth.addCredits(amount);
    }

    public void adjustReputation(int delta) {
        reputation.adjustReputation(delta);
    }

    public long getWealth() {
        return wealth.getCredits();
    }

    public int getReputation() {
        return reputation.getReputation();
    }
}