package com.astral.rpg;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Manages quests and quest progression
 */
public class QuestManager {

    private final ObjectMap<String, Quest> allQuests = new ObjectMap<>();
    private final Array<Quest> activeQuests = new Array<>();
    private final Array<Quest> completedQuests = new Array<>();

    public interface QuestEventListener {
        void onQuestStarted(Quest quest);
        void onObjectiveProgress(Quest quest, QuestObjective objective);
        void onObjectiveComplete(Quest quest, QuestObjective objective);
        void onQuestComplete(Quest quest);
    }

    private QuestEventListener listener;

    public void loadQuests(String filename) {
        try {
            FileHandle file = Gdx.files.internal(filename);
            JsonValue root = new JsonReader().parse(file);

            for (JsonValue questJson : root.get("quests")) {
                Quest quest = parseQuest(questJson);
                allQuests.put(quest.id, quest);
            }

            Gdx.app.log("QuestManager", "Loaded " + allQuests.size + " quests");

        } catch (Exception e) {
            Gdx.app.error("QuestManager", "Failed to load quests", e);
        }
    }

    private Quest parseQuest(JsonValue json) {
        Quest quest = new Quest();
        quest.id = json.getString("id");
        quest.name = json.getString("name");
        quest.description = json.getString("description");
        quest.type = Quest.QuestType.valueOf(json.getString("type", "SIDE"));

        // Parse objectives
        JsonValue objectives = json.get("objectives");
        if (objectives != null) {
            for (JsonValue objJson : objectives) {
                QuestObjective obj = new QuestObjective();
                obj.id = objJson.getString("id");
                obj.description = objJson.getString("description");
                obj.type = QuestObjective.ObjectiveType.valueOf(objJson.getString("type"));
                obj.target = objJson.getString("target", null);
                obj.requiredCount = objJson.getInt("count", 1);
                quest.objectives.add(obj);
            }
        }

        // Parse rewards
        JsonValue rewards = json.get("rewards");
        if (rewards != null) {
            for (JsonValue rewardJson : rewards) {
                QuestReward reward = new QuestReward();
                reward.type = QuestReward.RewardType.valueOf(rewardJson.getString("type"));
                reward.value = rewardJson.getString("value");
                reward.amount = rewardJson.getInt("amount", 1);
                quest.rewards.add(reward);
            }
        }

        // Parse prerequisites
        JsonValue prereqs = json.get("prerequisites");
        if (prereqs != null) {
            for (JsonValue pre : prereqs) {
                quest.prerequisites.add(pre.asString());
            }
        }

        return quest;
    }

    public void startQuest(String questId) {
        Quest quest = allQuests.get(questId);
        if (quest == null) {
            Gdx.app.error("QuestManager", "Quest not found: " + questId);
            return;
        }

        if (activeQuests.contains(quest, true)) {
            return; // Already active
        }

        // Check prerequisites
        for (String prereq : quest.prerequisites) {
            if (!isQuestComplete(prereq)) {
                Gdx.app.log("QuestManager", "Prerequisites not met for: " + questId);
                return;
            }
        }

        // Start the quest
        quest.state = Quest.QuestState.ACTIVE;
        activeQuests.add(quest);

        if (listener != null) {
            listener.onQuestStarted(quest);
        }

        Gdx.app.log("QuestManager", "Quest started: " + quest.name);
    }

    public void onGameEvent(GameEvent event) {
        for (Quest quest : activeQuests) {
            for (QuestObjective obj : quest.objectives) {
                if (obj.isComplete) continue;

                if (matchesObjective(obj, event)) {
                    obj.currentCount++;

                    if (listener != null) {
                        listener.onObjectiveProgress(quest, obj);
                    }

                    if (obj.currentCount >= obj.requiredCount) {
                        obj.isComplete = true;

                        if (listener != null) {
                            listener.onObjectiveComplete(quest, obj);
                        }
                    }
                }
            }

            if (quest.isComplete()) {
                completeQuest(quest);
            }
        }
    }

    private boolean matchesObjective(QuestObjective obj, GameEvent event) {
        return switch (obj.type) {
            case KILL_ENEMY -> event.type.equals("enemy_killed") &&
                    event.data.equals(obj.target);
            case COLLECT_ITEM -> event.type.equals("item_collected") &&
                    event.data.equals(obj.target);
            case VISIT_LOCATION -> event.type.equals("location_visited") &&
                    event.data.equals(obj.target);
            case TALK_TO_NPC -> event.type.equals("dialogue_completed") &&
                    event.data.equals(obj.target);
            case DELIVER_ITEM -> event.type.equals("item_delivered") &&
                    event.data.equals(obj.target);
            case SCAN_OBJECT -> event.type.equals("object_scanned") &&
                    event.data.equals(obj.target);
        };
    }

    private void completeQuest(Quest quest) {
        quest.state = Quest.QuestState.COMPLETED;
        activeQuests.removeValue(quest, true);
        completedQuests.add(quest);

        // Grant rewards
        for (QuestReward reward : quest.rewards) {
            grantReward(reward);
        }

        if (listener != null) {
            listener.onQuestComplete(quest);
        }

        Gdx.app.log("QuestManager", "Quest completed: " + quest.name);
    }

    private void grantReward(QuestReward reward) {
        switch (reward.type) {
            case CREDITS -> {
                // TODO: Add credits to player
                Gdx.app.log("QuestManager", "Reward: " + reward.amount + " credits");
            }
            case EXPERIENCE -> {
                // TODO: Add XP to player
                Gdx.app.log("QuestManager", "Reward: " + reward.amount + " XP");
            }
            case ITEM -> {
                // TODO: Add item to inventory
                Gdx.app.log("QuestManager", "Reward: " + reward.amount + "x " + reward.value);
            }
            case REPUTATION -> {
                // TODO: Add reputation
                Gdx.app.log("QuestManager", "Reward: " + reward.amount + " reputation");
            }
            case UNLOCK -> {
                // TODO: Unlock feature/item
                Gdx.app.log("QuestManager", "Reward: Unlocked " + reward.value);
            }
        }
    }

    public boolean isQuestComplete(String questId) {
        Quest quest = allQuests.get(questId);
        return quest != null && quest.state == Quest.QuestState.COMPLETED;
    }

    public Array<Quest> getActiveQuests() {
        return activeQuests;
    }

    public Array<Quest> getCompletedQuests() {
        return completedQuests;
    }

    public Quest getQuest(String id) {
        return allQuests.get(id);
    }

    public void setListener(QuestEventListener listener) {
        this.listener = listener;
    }

    // Simple game event class
    public static class GameEvent {
        public String type;
        public String data;

        public GameEvent(String type, String data) {
            this.type = type;
            this.data = data;
        }
    }
}
