package com.astral.inventory.skills;

import java.util.HashMap;
import java.util.Map;

/**
 * Runescape-style skills system with levels 1-99.
 */
public class Skills {
    private Map<SkillType, Skill> skills;

    public Skills() {
        skills = new HashMap<>();
        for (SkillType type : SkillType.values()) {
            skills.put(type, new Skill(type));
        }
    }

    public void addXP(SkillType type, long xp) {
        Skill skill = skills.get(type);
        if (skill != null) {
            skill.addXP(xp);
        }
    }

    public int getLevel(SkillType type) {
        Skill skill = skills.get(type);
        return skill != null ? skill.getLevel() : 1;
    }

    public long getXP(SkillType type) {
        Skill skill = skills.get(type);
        return skill != null ? skill.getXP() : 0;
    }

    public Map<SkillType, Skill> getAllSkills() {
        return new HashMap<>(skills);
    }

    public enum SkillType {
        MINING, WOODCUTTING, SMITHING, CRAFTING, FISHING, COOKING, FARMING, CONSTRUCTION
    }

    public static class Skill {
        private SkillType type;
        private long xp;
        private int level;

        public Skill(SkillType type) {
            this.type = type;
            this.xp = 0;
            this.level = 1;
        }

        public void addXP(long amount) {
            xp += amount;
            int newLevel = calculateLevel(xp);
            level = Math.min(99, newLevel);
        }

        private int calculateLevel(long xp) {
            // Runescape XP formula approximation
            for (int lvl = 1; lvl <= 99; lvl++) {
                if (xp < getXPForLevel(lvl + 1)) {
                    return lvl;
                }
            }
            return 99;
        }

        private long getXPForLevel(int level) {
            // Simplified Runescape XP: roughly (level-1)^2 * 75 or similar
            if (level <= 1) return 0;
            return (long) ((level - 1) * (level - 1) * 75);
        }

        public long getXP() { return xp; }
        public int getLevel() { return level; }
        public long getXPToNextLevel() {
            return getXPForLevel(level + 1) - xp;
        }

        public String getName() { return type.name(); }
        public int getMaxLevel() { return 99; }
        public long getCurrentXp() { return xp; }
        public long getXpForNextLevel() { return getXPForLevel(level + 1); }
    }
}