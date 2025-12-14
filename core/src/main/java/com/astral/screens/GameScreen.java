package com.astral.screens;

import com.astral.AstralFrontier;
import com.astral.components.*;
import com.astral.ecs.Entity;
import com.astral.ecs.World;
import com.astral.procedural.PlanetType;
import com.astral.screens.factories.PlayerShipFactory;
import com.astral.screens.factories.WorldObjectFactory;
import com.astral.screens.shipbuilder.StarfieldShipBuilderScreen;
import com.astral.screens.ui.PauseMenuRenderer;
import com.astral.ui.HUDRenderer;
import com.astral.ui.UITheme;
import com.astral.game.UniverseManager;
import com.astral.systems.*;
import com.astral.inventory.core.InventorySystem;
import com.astral.inventory.core.InventoryGrid;
import com.astral.inventory.skills.Skills;
import com.astral.inventory.equipment.EquipmentManager;
import com.astral.inventory.EquipmentSlot;
import com.astral.inventory.ItemStack;
import com.astral.inventory.IEquipment;
import com.astral.inventory.crafting.CraftingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.graphics.g2d.BitmapFont;

import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Main game screen - where space gameplay happens.
 * Refactored to use factory classes for entity creation.
 */
public class GameScreen implements Screen {

    private final AstralFrontier game;
    private World world;

    // Systems
    private InputSystem inputSystem;
    private RenderSystem renderSystem;
    private PhysicsSystem physicsSystem;
    private GameLogicSystem gameLogicSystem;
    private UISystem uiSystem;
    private InventorySystem inventorySystem;
    private TransitionManager transitionManager;
    private FPSCharacterController fpsController;

    // Factories
    private PlayerShipFactory playerShipFactory;
    private WorldObjectFactory worldObjectFactory;

    // UI
    private PauseMenuRenderer pauseMenuRenderer;
    private HUDRenderer hudRenderer;
    private Stage hudStage;
    private Window inventoryWindow;
    private boolean showInventory = false;

    // Entities
    private Entity playerEntity;

    // Test planet config
    private static final Vector3 PLANET_POSITION = new Vector3(0, 0, -500);
    private static final float PLANET_RADIUS = 100f;

    private boolean paused = false;

    public GameScreen(AstralFrontier game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "Starting game...");

        initializeSystems();
        initializeFactories();
        createWorld();
        setupInput();
        setupHUD();

        Gdx.app.log("GameScreen", "Game started!");
    }

    private void initializeSystems() {
        world = game.getEcsWorld();
        inputSystem = game.getInputSystem();
        renderSystem = game.getRenderSystem();
        physicsSystem = game.getPhysicsSystem();

        // Clear old entities when returning from planet
        world.clearEntities();

        // Get game logic system and connect input
        gameLogicSystem = game.getGameLogicSystem();
        gameLogicSystem.setInputSystem(inputSystem);

        // Initialize UI system
        uiSystem = new UISystem(world);
        uiSystem.initialize();

        // Initialize inventory system
        inventorySystem = game.getInventorySystem();

        // Initialize render and physics
        renderSystem.initialize();
        physicsSystem.dispose();
        physicsSystem.initialize();

        // Initialize transition manager
        transitionManager = new TransitionManager(world);
        transitionManager.setPhysicsSystem(physicsSystem);

        // Initialize FPS controller
        fpsController = new FPSCharacterController(world);
        fpsController.setInputSystem(inputSystem);

        // Initialize pause menu
        pauseMenuRenderer = new PauseMenuRenderer();
        pauseMenuRenderer.initialize();
        
        // Initialize HUD renderer
        hudRenderer = new HUDRenderer();
    }

    private void initializeFactories() {
        playerShipFactory = new PlayerShipFactory();
        worldObjectFactory = new WorldObjectFactory();
    }

    private void createWorld() {
        // Create player ship using factory
        playerEntity = playerShipFactory.createPlayerShip(world, physicsSystem);

        // Create asteroids
        worldObjectFactory.createAsteroids(world, 20, 500f);

        // Create test planet
        worldObjectFactory.createPlanet(
            world,
            PLANET_POSITION,
            PLANET_RADIUS,
            new Color(0.2f, 0.5f, 0.3f, 1f),
            "Test Planet",
            transitionManager
        );
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(inputSystem);
        inputSystem.setMouseLocked(true);
    }

    private void setupHUD() {
        hudStage = new Stage(new ScreenViewport());
        Skin skin = createCustomSkin();

        // Create main inventory window
        inventoryWindow = new Window("Inventory", skin);
        inventoryWindow.setSize(800, 600);
        inventoryWindow.setPosition(Gdx.graphics.getWidth() / 2 - 400, Gdx.graphics.getHeight() / 2 - 300);
        inventoryWindow.setVisible(false);
        inventoryWindow.setMovable(true);
        inventoryWindow.setResizable(false);

        // Create tab buttons
        Table tabTable = new Table(skin);
        TextButton inventoryTab = new TextButton("Inventory", skin);
        TextButton skillsTab = new TextButton("Skills", skin);
        TextButton equipmentTab = new TextButton("Equipment", skin);
        TextButton craftingTab = new TextButton("Crafting", skin);

        tabTable.add(inventoryTab).pad(5).uniform();
        tabTable.add(skillsTab).pad(5).uniform();
        tabTable.add(equipmentTab).pad(5).uniform();
        tabTable.add(craftingTab).pad(5).uniform();

        inventoryWindow.add(tabTable).expandX().fillX().row();

        // Content area
        Table contentTable = new Table(skin);

        // Real content for each tab
        Table inventoryTable = createInventoryTable(skin);
        Table skillsTable = createSkillsTable(skin);
        Table equipmentTable = createEquipmentTable(skin);
        Table craftingTable = createCraftingTable(skin);

        contentTable.add(inventoryTable).expand().fill();
        inventoryWindow.add(contentTable).expand().fill().pad(10);

        // Tab switching logic
        inventoryTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                contentTable.clear();
                contentTable.add(inventoryTable).expand().fill();
            }
        });
        skillsTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                contentTable.clear();
                contentTable.add(skillsTable).expand().fill();
            }
        });
        equipmentTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                contentTable.clear();
                contentTable.add(equipmentTable).expand().fill();
            }
        });
        craftingTab.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                contentTable.clear();
                contentTable.add(craftingTable).expand().fill();
            }
        });

        // Default to inventory tab
        contentTable.add(inventoryTable).expand().fill();

        hudStage.addActor(inventoryWindow);
    }

    private Table createInventoryTable(Skin skin) {
        Table table = new Table(skin);
        table.add(new Label("Inventory", skin, "title")).colspan(10).pad(10).row();

        // Get inventory grid from system
        InventoryGrid grid = inventorySystem.getInventoryGrid();
        if (grid != null) {
            for (int y = 0; y < Math.min(10, grid.getHeight()); y++) {
                for (int x = 0; x < Math.min(10, grid.getWidth()); x++) {
                    ItemStack stack = grid.getItemAt(x, y);
                    Image slot = new Image();
                    slot.setSize(32, 32);
                    if (stack != null) {
                        // Set icon (procedural)
                        // For now, just image
                    }
                    table.add(slot).size(32).pad(2);
                }
                table.row();
            }
        }
        return table;
    }

    private Table createSkillsTable(Skin skin) {
        Table table = new Table(skin);
        table.add(new Label("Skills", skin, "title")).colspan(3).pad(10).row();

        Skills skills = inventorySystem.getSkills();
        for (Skills.SkillType type : Skills.SkillType.values()) {
            int level = skills.getLevel(type);
            long xp = skills.getXP(type);
            long xpToNext = skills.getAllSkills().get(type).getXPToNextLevel();
            table.add(new Label(type.name().replace("_", " "), skin)).left().pad(5);
            table.add(new Label("Lvl " + level, skin)).center().pad(5);
            ProgressBar xpBar = new ProgressBar(0, xpToNext, 1, false, skin);
            xpBar.setValue(xp);
            table.add(xpBar).expandX().fillX().pad(5);
            table.row();
        }
        return table;
    }

    private Table createEquipmentTable(Skin skin) {
        Table table = new Table(skin);
        table.add(new Label("Equipment", skin, "title")).colspan(2).pad(10).row();

        EquipmentManager eqManager = inventorySystem.getEquipmentManager();
        EquipmentSlot[] slots = EquipmentSlot.values();
        for (EquipmentSlot slot : slots) {
            IEquipment equipped = eqManager.getEquipped(slot);
            String itemName = equipped != null ? equipped.getSlot().toString() : "Empty";
            table.add(new Label(slot.name() + ":", skin)).left().pad(5);
            table.add(new Label(itemName, skin)).left().pad(5).row();
        }
        return table;
    }

    private Table createCraftingTable(Skin skin) {
        Table table = new Table(skin);
        table.add(new Label("Crafting", skin, "title")).colspan(2).pad(10).row();

        CraftingSystem crafting = inventorySystem.getCraftingSystem();
        var recipes = crafting.getAllRecipes();
        int count = 0;
        for (var entry : recipes.entrySet()) {
            if (count >= 10) break; // Limit to 10 for display
            table.add(new Label(entry.getKey(), skin)).left().pad(5);
            TextButton craftBtn = new TextButton("Craft", skin);
            final String recipeName = entry.getKey();
            craftBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    inventorySystem.craftItem(recipeName, inventorySystem.getInventoryGrid());
                }
            });
            table.add(craftBtn).right().pad(5).row();
            count++;
        }
        return table;
    }

    private Skin createCustomSkin() {
        Skin skin = new Skin();

        // Create fonts
        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);

        // Create textures for drawables
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fill();
        skin.add("background", new Texture(pixmap));

        pixmap.setColor(Color.GRAY);
        pixmap.fill();
        skin.add("button", new Texture(pixmap));

        pixmap.dispose();

        // Create colors
        skin.add("white", Color.WHITE);
        skin.add("black", Color.BLACK);
        skin.add("gray", Color.GRAY);
        skin.add("dark-gray", Color.DARK_GRAY);
        skin.add("blue", Color.BLUE);
        skin.add("green", Color.GREEN);
        skin.add("red", Color.RED);

        // Label style
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // Title label style
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = font;
        titleStyle.fontColor = Color.CYAN;
        skin.add("title", titleStyle);

        // Button style
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = skin.newDrawable("button");
        buttonStyle.down = skin.newDrawable("button", Color.LIGHT_GRAY);
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.downFontColor = Color.LIGHT_GRAY;
        skin.add("default", buttonStyle);

        // Window style
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.background = skin.newDrawable("background");
        windowStyle.titleFont = font;
        windowStyle.titleFontColor = Color.WHITE;
        skin.add("default", windowStyle);

        // ProgressBar style
        ProgressBar.ProgressBarStyle progressStyle = new ProgressBar.ProgressBarStyle();
        progressStyle.background = skin.newDrawable("background", Color.DARK_GRAY);
        progressStyle.knob = skin.newDrawable("background", Color.GREEN);
        progressStyle.knobBefore = skin.newDrawable("background", Color.GREEN);
        skin.add("default-horizontal", progressStyle);

        return skin;
    }

    @Override
    public void render(float delta) {
        handleInput();

        if (!paused) {
            update(delta);
        }

        renderWorld(delta);
        renderUI(delta);
        renderHUD(delta);

        if (paused) {
            pauseMenuRenderer.render();
        }
    }

    private void handleInput() {
        // Pause toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            inputSystem.setMouseLocked(!paused);
        }

        // Debug toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
            renderSystem.toggleDebugInfo();
        }

        // Ship Builder - open with H key
        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            openShipBuilder();
        }

        // Inventory toggle - I key
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            showInventory = !showInventory;
            inventoryWindow.setVisible(showInventory);
            if (showInventory) {
                inputSystem.setMouseLocked(false);
            } else {
                inputSystem.setMouseLocked(true);
            }
        }

        // FPS mode toggle
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) && playerEntity != null) {
            transitionManager.toggleFPSMode(playerEntity);
        }

        // Land on planet
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            game.landOnPlanet(12345L, PlanetType.DESERT, "Khepri Prime");
        }
        
        // Star Map - open with M key
        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
            openStarMap();
        }
    }
    
    private void openStarMap() {
        inputSystem.setMouseLocked(false);
        Gdx.input.setCursorCatched(false);
        
        // Initialize universe if needed
        if (UniverseManager.getInstance() == null) {
            UniverseManager.initialize(System.currentTimeMillis());
        }
        
        StarMapScreen starMap = new StarMapScreen(game, this);
        game.setScreen(starMap);
        Gdx.app.log("GameScreen", "Opening Star Map...");
    }

    private void openShipBuilder() {
        // Unlock mouse cursor before switching to ship builder
        inputSystem.setMouseLocked(false);
        Gdx.input.setCursorCatched(false);

        StarfieldShipBuilderScreen builderScreen = new StarfieldShipBuilderScreen(
            game,
            this
        );
        game.setScreen(builderScreen);
        Gdx.app.log("GameScreen", "Opening Starfield-style ship builder...");
    }

    private void update(float delta) {
        updateCamera();
        fpsController.update(delta);
        transitionManager.update(delta);

        // Update UI with planet info
        uiSystem.setPlanetInfo("Test Planet", PLANET_POSITION);
        uiSystem.setTransitionState(
            transitionManager.getCurrentState().name(),
            transitionManager.getTransitionProgress()
        );

        world.update(delta);
    }

    private void renderWorld(float delta) {
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        float alpha = 0f;
        renderSystem.render(delta, alpha);
    }

    private void renderUI(float delta) {
        uiSystem.render(delta);
    }

    private void renderHUD(float delta) {
        if (showInventory) {
            Gdx.input.setInputProcessor(hudStage);
        } else {
            Gdx.input.setInputProcessor(inputSystem);
        }
        
        // Render Starfield-style HUD
        if (!paused && !showInventory) {
            updateHUDData();
            hudRenderer.update(delta);
            hudRenderer.render();
        }
        
        hudStage.act(delta);
        hudStage.draw();
    }
    
    private void updateHUDData() {
        // Update HUD with player data
        if (playerEntity != null) {
            // Get player health/shield from components (mock data for now)
            hudRenderer.setHealth(85f, 100f);
            hudRenderer.setShield(60f, 100f);
            hudRenderer.setFuel(75f, 100f);
            hudRenderer.setOxygen(90f, 100f);
            
            // Set speed and heading from transform
            TransformComponent transform = playerEntity.get(TransformComponent.class);
            if (transform != null) {
                hudRenderer.setSpeed(50f, 500f); // Mock speed for now
                hudRenderer.setHeading(transform.rotation.getYaw());
                hudRenderer.setCoordinates(
                    (int)transform.position.x,
                    (int)transform.position.y,
                    (int)transform.position.z
                );
            }
            
            // Set location
            hudRenderer.setLocation("Sol System", "Near Test Planet");
        }
    }

    private void updateCamera() {
        if (playerEntity == null) return;

        TransformComponent transform = playerEntity.get(
            TransformComponent.class
        );
        CameraComponent camComp = playerEntity.get(CameraComponent.class);

        if (
            transform == null || camComp == null || camComp.camera == null
        ) return;

        Vector3 camPos = transform.position.cpy();

        if (camComp.mode == CameraComponent.CameraMode.COCKPIT) {
            Vector3 offset = camComp.offset.cpy().mul(transform.rotation);
            camPos.add(offset);
            camComp.camera.position.set(camPos);
            camComp.camera.direction.set(transform.getForward());
            camComp.camera.up.set(transform.getUp());
        } else if (camComp.mode == CameraComponent.CameraMode.CHASE) {
            Vector3 back = transform
                .getForward()
                .scl(-camComp.thirdPersonDistance);
            Vector3 up = transform.getUp().scl(camComp.thirdPersonHeight);
            camPos.add(back).add(up);
            camComp.camera.position.set(camPos);
            camComp.camera.lookAt(transform.position);
        }

        camComp.camera.update();
    }

    @Override
    public void resize(int width, int height) {
        if (renderSystem != null) renderSystem.resize(width, height);
        if (inputSystem != null) inputSystem.resize(width, height);
        if (playerEntity != null) {
            CameraComponent cam = playerEntity.get(CameraComponent.class);
            if (cam != null) cam.resize(width, height);
        }
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        // Re-lock mouse cursor when returning from ship builder or other screens
        if (inputSystem != null) {
            Gdx.input.setInputProcessor(inputSystem);
            inputSystem.setMouseLocked(true);
            Gdx.input.setCursorCatched(false); // Make sure it's visible but locked
        }
        Gdx.app.log("GameScreen", "Game resumed - mouse locked");
    }

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        if (playerShipFactory != null) playerShipFactory.dispose();
        if (worldObjectFactory != null) worldObjectFactory.dispose();
        if (pauseMenuRenderer != null) pauseMenuRenderer.dispose();
        if (uiSystem != null) uiSystem.dispose();
        if (hudStage != null) hudStage.dispose();
        if (hudRenderer != null) hudRenderer.dispose();
        // Font disposed with stage
    }
}
