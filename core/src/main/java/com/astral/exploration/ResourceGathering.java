package com.astral.exploration;

import com.astral.game.PlayerInventory;
import com.astral.game.UniverseManager;
import com.astral.game.UniverseManager.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import com.astral.procedural.PlanetSurface;
import java.util.Random;

/**
 * Resource Gathering System - Mining, Fishing, Woodcutting, etc.
 * Integrated with PlayerInventory for resource storage
 */
public class ResourceGathering implements Disposable {
    
    public enum GatheringType {
        MINING("Mining", "E", 2f),
        FISHING("Fishing", "E", 3f),
        WOODCUTTING("Woodcutting", "E", 1.5f),
        HARVESTING("Harvesting", "E", 1f);
        
        public final String name;
        public final String key;
        public final float baseTime;
        
        GatheringType(String name, String key, float baseTime) {
            this.name = name;
            this.key = key;
            this.baseTime = baseTime;
        }
    }
    
    // Resource nodes near player
    private final Array<ResourceNode> activeNodes = new Array<>();
    private ResourceNode targetNode;
    
    // Gathering state
    private boolean isGathering = false;
    private float gatherProgress = 0f;
    private float gatherTime = 0f;
    
    // Planet data
    private PlanetData currentPlanet;
    private long planetSeed;
    private Random random;
    private PlanetSurface terrain;
    
    // Rendering
    private ShapeRenderer shapeRenderer;
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    
    // Settings
    private static final float NODE_DETECTION_RANGE = 50f;
    private static final float INTERACTION_RANGE = 5f;
    private static final int MAX_NODES = 20;
    
    public ResourceGathering() {
        shapeRenderer = new ShapeRenderer();
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.2f);
    }
    
    /**
     * Initialize for a specific planet
     */
    public void initForPlanet(long seed, PlanetData planet) {
        initForPlanet(seed, planet, null);
    }
    
    /**
     * Initialize for a specific planet with terrain reference for height sampling
     */
    public void initForPlanet(long seed, PlanetData planet, PlanetSurface terrain) {
        this.planetSeed = seed;
        this.currentPlanet = planet;
        this.random = new Random(seed);
        this.terrain = terrain;
        activeNodes.clear();
        
        // Generate initial nodes based on planet resources
        generateInitialNodes();
    }
    
    /**
     * Set terrain reference for height sampling
     */
    public void setTerrain(PlanetSurface terrain) {
        this.terrain = terrain;
        // Update existing node heights
        updateNodeHeights();
    }
    
    private void updateNodeHeights() {
        if (terrain == null) return;
        for (ResourceNode node : activeNodes) {
            float height = terrain.getHeightAt(node.position.x, node.position.z);
            node.position.y = height + 1f; // Slightly above terrain
        }
    }
    
    private void generateInitialNodes() {
        if (currentPlanet == null || currentPlanet.resources == null) return;
        
        for (ResourceDeposit deposit : currentPlanet.resources) {
            // Create nodes for each resource type
            int nodeCount = Math.min(deposit.getRemaining() / 10, 5);
            
            for (int i = 0; i < nodeCount; i++) {
                ResourceNode node = new ResourceNode();
                node.resourceType = mapToResourceName(deposit.type);
                node.gatherType = getGatherTypeFor(deposit.type);
                float x = (random.nextFloat() - 0.5f) * 200f;
                float z = (random.nextFloat() - 0.5f) * 200f;
                float y = terrain != null ? terrain.getHeightAt(x, z) + 1f : 1f;
                node.position = new Vector3(x, y, z);
                node.remainingYield = 3 + random.nextInt(5);
                node.respawnTime = 30f + random.nextFloat() * 60f;
                node.deposit = deposit;
                
                activeNodes.add(node);
            }
        }
        
        Gdx.app.log("ResourceGathering", "Generated " + activeNodes.size + " resource nodes");
    }
    
    private String mapToResourceName(ResourceType type) {
        return switch (type) {
            case IRON -> "Iron Ore";
            case COPPER -> "Copper Ore";
            case GOLD -> "Gold Ore";
            case TITANIUM -> "Titanium Ore";
            case URANIUM -> "Uranium Ore";
            case SILICON -> "Silicon";
            case RARE_METALS -> "Rare Metals";
            case RARE_MINERALS -> "Rare Minerals";
            case WOOD -> "Wood";
            case WATER -> "Water";
            case FISH -> "Fish";
            case RARE_GAS -> "Rare Gas";
            case EXOTIC_MATTER -> "Exotic Matter";
            case FUEL -> "Fuel";
        };
    }
    
    private GatheringType getGatherTypeFor(ResourceType type) {
        return switch (type) {
            case IRON, COPPER, GOLD, TITANIUM, URANIUM, SILICON, RARE_METALS, RARE_MINERALS -> GatheringType.MINING;
            case WOOD -> GatheringType.WOODCUTTING;
            case FISH -> GatheringType.FISHING;
            case WATER, RARE_GAS, EXOTIC_MATTER, FUEL -> GatheringType.HARVESTING;
        };
    }
    
    /**
     * Update gathering system
     */
    public void update(float delta, Vector3 playerPosition, boolean interactPressed) {
        // Update node heights from terrain
        if (terrain != null) {
            for (ResourceNode node : activeNodes) {
                if (node.position.y <= 1f) {
                    float height = terrain.getHeightAt(node.position.x, node.position.z);
                    node.position.y = height + 1f;
                }
            }
        }
        
        // Find closest node
        targetNode = null;
        float closestDist = INTERACTION_RANGE;
        
        for (ResourceNode node : activeNodes) {
            if (node.depleted) continue;
            
            float dist = playerPosition.dst(node.position);
            if (dist < closestDist) {
                closestDist = dist;
                targetNode = node;
            }
        }
        
        // Handle gathering
        if (isGathering) {
            if (targetNode == null || !interactPressed) {
                // Cancelled
                isGathering = false;
                gatherProgress = 0f;
            } else {
                // Progress gathering
                float speedMultiplier = getSpeedMultiplier(targetNode.gatherType);
                gatherProgress += delta * speedMultiplier;
                
                if (gatherProgress >= gatherTime) {
                    // Gather complete
                    completeGathering();
                }
            }
        } else if (targetNode != null && interactPressed) {
            // Start gathering
            startGathering(targetNode);
        }
        
        // Update respawning nodes
        for (ResourceNode node : activeNodes) {
            if (node.depleted) {
                node.respawnTimer -= delta;
                if (node.respawnTimer <= 0) {
                    node.depleted = false;
                    node.remainingYield = 2 + random.nextInt(4);
                }
            }
        }
    }
    
    private void startGathering(ResourceNode node) {
        isGathering = true;
        gatherProgress = 0f;
        gatherTime = node.gatherType.baseTime;
        
        Gdx.app.log("ResourceGathering", "Started " + node.gatherType.name + " " + node.resourceType);
    }
    
    private void completeGathering() {
        if (targetNode == null) return;
        
        // Calculate yield
        int baseYield = 1;
        float skillBonus = 1f + (getSkillLevel(targetNode.gatherType) * 0.1f);
        int totalYield = (int)(baseYield * skillBonus);
        
        // Rare bonus
        if (random.nextFloat() < 0.1f) {
            totalYield *= 2;
            Gdx.app.log("ResourceGathering", "Lucky! Double yield!");
        }
        
        // Add to inventory
        PlayerInventory.getInstance().addResource(targetNode.resourceType, totalYield);
        
        // Update node
        targetNode.remainingYield--;
        if (targetNode.remainingYield <= 0) {
            targetNode.depleted = true;
            targetNode.respawnTimer = targetNode.respawnTime;
        }
        
        // Award XP (would connect to skill system)
        // For now just log
        Gdx.app.log("ResourceGathering", 
            "Gathered " + totalYield + "x " + targetNode.resourceType + 
            " (Remaining: " + targetNode.remainingYield + ")");
        
        // Reset
        isGathering = false;
        gatherProgress = 0f;
    }
    
    private float getSpeedMultiplier(GatheringType type) {
        PlayerInventory inv = PlayerInventory.getInstance();
        return switch (type) {
            case MINING -> inv.getMiningSpeed();
            case FISHING -> inv.getFishingSpeed();
            default -> 1f;
        };
    }
    
    private int getSkillLevel(GatheringType type) {
        // Would connect to skill system
        return 1;
    }
    
    /**
     * Render gathering UI
     */
    public void render(Vector3 playerPosition) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        
        // Render node indicators
        renderNodeIndicators(playerPosition);
        
        // Render gathering progress
        if (isGathering && targetNode != null) {
            renderGatheringProgress(w, h);
        }
        
        // Render interaction prompt
        if (targetNode != null && !isGathering) {
            renderInteractionPrompt(w, h);
        }
        
        // Render resource HUD
        renderResourceHUD(w, h);
    }
    
    private void renderNodeIndicators(Vector3 playerPosition) {
        // Would render 3D markers on nodes
        // For now this is handled by the main exploration renderer
    }
    
    private void renderGatheringProgress(int w, int h) {
        float barWidth = 300;
        float barHeight = 20;
        float x = (w - barWidth) / 2f;
        float y = h / 2f - 100;
        
        Gdx.gl.glEnable(com.badlogic.gdx.graphics.GL20.GL_BLEND);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        // Background
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 0.8f);
        shapeRenderer.rect(x - 5, y - 5, barWidth + 10, barHeight + 30);
        
        // Progress bar background
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 1f);
        shapeRenderer.rect(x, y, barWidth, barHeight);
        
        // Progress bar fill
        float progress = gatherProgress / gatherTime;
        Color barColor = switch (targetNode.gatherType) {
            case MINING -> new Color(0.8f, 0.5f, 0.2f, 1f);
            case FISHING -> new Color(0.2f, 0.6f, 0.9f, 1f);
            case WOODCUTTING -> new Color(0.4f, 0.7f, 0.3f, 1f);
            case HARVESTING -> new Color(0.7f, 0.8f, 0.3f, 1f);
        };
        shapeRenderer.setColor(barColor);
        shapeRenderer.rect(x, y, barWidth * progress, barHeight);
        
        shapeRenderer.end();
        
        // Text
        spriteBatch.begin();
        font.setColor(Color.WHITE);
        String text = targetNode.gatherType.name + " " + targetNode.resourceType + "...";
        font.draw(spriteBatch, text, x, y + barHeight + 20);
        spriteBatch.end();
    }
    
    private void renderInteractionPrompt(int w, int h) {
        String prompt = "[E] " + targetNode.gatherType.name + " " + targetNode.resourceType;
        
        spriteBatch.begin();
        font.setColor(Color.CYAN);
        font.draw(spriteBatch, prompt, w / 2f - 100, h / 2f - 50);
        spriteBatch.end();
    }
    
    private void renderResourceHUD(int w, int h) {
        PlayerInventory inv = PlayerInventory.getInstance();
        
        // Show recent resources in corner
        float x = 20;
        float y = h - 200;
        
        spriteBatch.begin();
        font.setColor(0.8f, 0.8f, 0.8f, 1f);
        font.draw(spriteBatch, "Resources:", x, y);
        y -= 25;
        
        // Show key resources
        String[] keyResources = {"Iron Ore", "Copper Ore", "Gold Ore", "Wood", "Fish", "Water"};
        for (String res : keyResources) {
            int amount = inv.getResource(res);
            if (amount > 0) {
                font.draw(spriteBatch, res + ": " + amount, x, y);
                y -= 20;
            }
        }
        
        spriteBatch.end();
    }
    
    /**
     * Check if currently gathering
     */
    public boolean isGathering() {
        return isGathering;
    }
    
    /**
     * Get target node info
     */
    public ResourceNode getTargetNode() {
        return targetNode;
    }
    
    /**
     * Get all active nodes
     */
    public Array<ResourceNode> getActiveNodes() {
        return activeNodes;
    }
    
    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (font != null) font.dispose();
    }
    
    // === Resource Node Class ===
    
    public static class ResourceNode {
        public Vector3 position;
        public String resourceType;
        public GatheringType gatherType;
        public int remainingYield;
        public boolean depleted;
        public float respawnTime;
        public float respawnTimer;
        public ResourceDeposit deposit;
    }
}
