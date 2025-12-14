package com.astral.exploration;

import com.astral.combat.WeaponType;
import com.astral.procedural.PlanetSurface;
import com.astral.inventory.core.InventoryGrid;
import com.astral.inventory.crafting.CraftingSystem;
import com.astral.inventory.equipment.EquipmentManager;
import com.astral.inventory.skills.Skills;
import com.astral.inventory.ItemStack;
import com.astral.inventory.EquipmentSlot;
import com.astral.inventory.IEquipment;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.Disposable;

/**
 * Handles all HUD rendering for planet exploration
 */
public class ExplorationUI implements Disposable {

    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private boolean showDebug = true;

    // Tabbed UI components
    private Stage uiStage;
    private Skin uiSkin;
    private Window inventoryWindow;
    private Table tabButtons;
    private Table tabContent;
    private Table inventoryTab, skillsTab, equipmentTab, craftingTab;
    private boolean showInventoryUI = false;

    // Inventory data
    private InventoryGrid playerInventory;
    private Skills skillSystem;
    private EquipmentManager equipmentManager;
    private CraftingSystem craftingSystem;
    private InputProcessor gameInputProcessor;

    public ExplorationUI() {
    }

    public void initialize() {
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.5f);

        // Initialize Scene2D UI
        uiStage = new Stage(new ScreenViewport());
        uiSkin = createCustomSkin();
        createTabbedUI();
    }

    public void toggleDebug() {
        showDebug = !showDebug;
    }

    public void renderSkyGradient(Color skyColor) {
        Color horizonColor = new Color(skyColor).lerp(Color.WHITE, 0.3f);
        Color zenithColor = new Color(skyColor).lerp(new Color(0.1f, 0.1f, 0.2f, 1f), 0.5f);

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(0, 0, width, height,
            horizonColor, horizonColor, zenithColor, zenithColor);
        shapeRenderer.end();
    }

    public void render(PlayerController player, WeaponSystem weapons, CombatManager combat, PlanetSurface surface) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Damage flash effect
        float damageFlash = combat.getDamageFlashTime();
        if (damageFlash > 0) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0f, 0f, damageFlash * 0.4f);
            shapeRenderer.rect(0, 0, width, height);
            shapeRenderer.end();
        }

        // Render 2D weapon
        weapons.renderWeapon2D();

        // Draw crosshair
        renderCrosshair(combat);

        // Draw HUD elements
        spriteBatch.begin();
        renderAmmoDisplay(weapons, width);
        renderPlanetName(surface, height);
        renderJetpackStatus(player, width, height);
        spriteBatch.end();

        renderFuelBar(player, width, height);
        renderHealthBar(combat, width);

        spriteBatch.begin();
        renderKillCount(combat);
        renderControlsHint();
        renderInventoryHint();

        if (showDebug) {
            renderDebugInfo(player, surface, height);
        }
        spriteBatch.end();

        // Render UI overlay
        if (showInventoryUI) {
            uiStage.act(Gdx.graphics.getDeltaTime());
            uiStage.draw();
        }
    }

    private void renderCrosshair(CombatManager combat) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        int cx = width / 2;
        int cy = height / 2;
        float crosshairSize = 12f;
        float crosshairThickness = 2f;
        float gap = 4f;
        float hitmarkerTimer = combat.getHitmarkerTimer();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Crosshair color
        if (hitmarkerTimer > 0) {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        } else {
            shapeRenderer.setColor(1f, 1f, 1f, 0.9f);
        }

        // Draw crosshair lines
        shapeRenderer.rectLine(cx, cy + gap, cx, cy + crosshairSize, crosshairThickness);
        shapeRenderer.rectLine(cx, cy - gap, cx, cy - crosshairSize, crosshairThickness);
        shapeRenderer.rectLine(cx - gap, cy, cx - crosshairSize, cy, crosshairThickness);
        shapeRenderer.rectLine(cx + gap, cy, cx + crosshairSize, cy, crosshairThickness);

        // Outer ring
        shapeRenderer.setColor(1f, 1f, 1f, 0.4f);
        float ringRadius = crosshairSize + 4f;
        int segments = 32;
        for (int i = 0; i < segments; i++) {
            float angle1 = (float) (i * 2 * Math.PI / segments);
            float angle2 = (float) ((i + 1) * 2 * Math.PI / segments);
            float x1 = cx + (float) Math.cos(angle1) * ringRadius;
            float y1 = cy + (float) Math.sin(angle1) * ringRadius;
            float x2 = cx + (float) Math.cos(angle2) * ringRadius;
            float y2 = cy + (float) Math.sin(angle2) * ringRadius;
            shapeRenderer.rectLine(x1, y1, x2, y2, 1f);
        }

        // Hitmarker X
        if (hitmarkerTimer > 0) {
            float hitSize = 8f + (hitmarkerTimer / combat.getHitmarkerDuration()) * 4f;
            float hitAlpha = hitmarkerTimer / combat.getHitmarkerDuration();
            shapeRenderer.setColor(1f, 0.2f, 0.2f, hitAlpha);
            shapeRenderer.rectLine(cx - hitSize, cy - hitSize, cx + hitSize, cy + hitSize, 3f);
            shapeRenderer.rectLine(cx - hitSize, cy + hitSize, cx + hitSize, cy - hitSize, 3f);
        }

        shapeRenderer.end();

        // Center dot
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        if (hitmarkerTimer > 0) {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        } else {
            shapeRenderer.setColor(1f, 1f, 1f, 1f);
        }
        shapeRenderer.circle(cx, cy, 2f);
        shapeRenderer.end();
    }

    private void renderAmmoDisplay(WeaponSystem weapons, int width) {
        if (weapons.isWeaponEquipped()) {
            WeaponType weapon = weapons.getCurrentWeapon();
            int ammo = weapons.getCurrentAmmo();
            int reserve = weapons.getReserveAmmo();

            font.setColor(ammo > weapon.maxAmmo * 0.2f ? Color.WHITE : Color.RED);
            String ammoText = weapons.isReloading() ? "RELOADING..." : ammo + " / " + reserve;
            font.draw(spriteBatch, ammoText, width - 180, 80);

            font.setColor(new Color(weapon.glowR, weapon.glowG, weapon.glowB, 1f));
            font.draw(spriteBatch, weapon.name, width - 180, 55);

            // Weapon slot indicators
            WeaponType[] allWeapons = WeaponType.values();
            for (int i = 0; i < allWeapons.length; i++) {
                font.setColor(i == weapons.getCurrentWeaponIndex() ? Color.WHITE : new Color(0.4f, 0.4f, 0.4f, 1f));
                font.draw(spriteBatch, "[" + (i + 1) + "]", width - 180 + i * 35, 30);
            }
        } else {
            font.setColor(Color.GRAY);
            font.draw(spriteBatch, "[E] to equip", width - 150, 80);
        }
    }

    private void renderPlanetName(PlanetSurface surface, int height) {
        font.setColor(new Color(0.8f, 0.9f, 1f, 1f));
        font.draw(spriteBatch, surface.getType().displayName + ": " + surface.getName(), 20, height - 20);
    }

    private void renderJetpackStatus(PlayerController player, int width, int height) {
        if (player.isJetpackActive()) {
            font.setColor(new Color(0.3f, 1f, 0.5f, 1f));
            font.draw(spriteBatch, "JETPACK ACTIVE", width - 180, height - 20);
        }
    }

    private void renderFuelBar(PlayerController player, int width, int height) {
        int barWidth = 150;
        int barHeight = 12;
        int barX = width - 170;
        int barY = height - 55;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.2f, 0.8f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        float fuelPercent = player.getJetpackFuel() / player.getJetpackMaxFuel();
        if (fuelPercent > 0.3f) {
            shapeRenderer.setColor(0.3f, 0.8f, 1f, 1f);
        } else {
            shapeRenderer.setColor(1f, 0.3f, 0.3f, 1f);
        }
        shapeRenderer.rect(barX, barY, barWidth * fuelPercent, barHeight);
        shapeRenderer.end();

        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "FUEL", barX - 50, barY + 12);
        spriteBatch.end();
    }

    private void renderHealthBar(CombatManager combat, int width) {
        int barWidth = 200;
        int barHeight = 20;
        int barX = 20;
        int barY = 60;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.1f, 0.1f, 0.8f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        float healthPercent = combat.getPlayerHealth() / combat.getPlayerMaxHealth();
        if (healthPercent > 0.3f) {
            shapeRenderer.setColor(0.2f, 0.8f, 0.3f, 1f);
        } else {
            shapeRenderer.setColor(1f, 0.2f, 0.2f, 1f);
        }
        shapeRenderer.rect(barX, barY, barWidth * healthPercent, barHeight);
        shapeRenderer.end();

        spriteBatch.begin();
        font.setColor(Color.WHITE);
        font.draw(spriteBatch, "HEALTH", barX, barY + 38);
        spriteBatch.end();
    }

    private void renderKillCount(CombatManager combat) {
        int barX = 20;
        int barY = 60;
        int barWidth = 200;

        font.setColor(new Color(1f, 0.8f, 0.2f, 1f));
        font.draw(spriteBatch, "KILLS: " + combat.getKillCount(), barX + barWidth + 30, barY + 18);

        font.setColor(new Color(1f, 0.4f, 0.4f, 1f));
        font.draw(spriteBatch, "ENEMIES: " + combat.getEnemyCount(), barX + barWidth + 130, barY + 18);
    }

    private void renderControlsHint() {
        font.setColor(new Color(0.7f, 0.7f, 0.7f, 1f));
        font.draw(spriteBatch, "WASD: Move | SHIFT: Sprint | SPACE: Jump | 2xSPACE: Jetpack | LMB: Shoot | RMB: Aim | R: Reload | 1-5/Scroll: Weapons | I: Inventory | ESC: Return", 20, 30);
    }

    private void renderInventoryHint() {
        font.setColor(new Color(0.8f, 0.9f, 1f, 1f));
        font.draw(spriteBatch, "Inventory: Press I to toggle", Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight() - 50);
    }

    private void renderDebugInfo(PlayerController player, PlanetSurface surface, int height) {
        font.setColor(Color.YELLOW);
        float terrainHeight = surface.getHeightAt(player.getPosition().x, player.getPosition().z);
        font.draw(spriteBatch, String.format("Pos: %.1f, %.1f, %.1f",
            player.getPosition().x, player.getPosition().y, player.getPosition().z), 20, height - 50);
        font.draw(spriteBatch, String.format("Ground: %.1f | Grounded: %s",
            terrainHeight, player.isGrounded()), 20, height - 75);
        font.draw(spriteBatch, String.format("Look: Yaw %.1f, Pitch %.1f",
            player.getCameraYaw(), player.getCameraPitch()), 20, height - 100);

        font.setColor(Color.CYAN);
        font.draw(spriteBatch, String.format("Chunks: %d loaded, %d pending",
            surface.getLoadedChunkCount(), surface.getPendingChunkCount()), 20, height - 125);

        font.setColor(Color.GREEN);
        font.draw(spriteBatch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 20, height - 150);
    }

    private Skin createCustomSkin() {
        Skin skin = new Skin();

        // Create fonts
        BitmapFont defaultFont = new BitmapFont();
        defaultFont.getData().setScale(1.2f);
        skin.add("default-font", defaultFont);

        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(1.5f);
        skin.add("title-font", titleFont);

        // Create textures for buttons and backgrounds
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.DARK_GRAY);
        pixmap.fill();
        skin.add("background", new Texture(pixmap));

        pixmap.setColor(new Color(0.2f, 0.2f, 0.2f, 0.9f));
        pixmap.fill();
        skin.add("window-background", new Texture(pixmap));

        pixmap.setColor(new Color(0.3f, 0.3f, 0.3f, 1f));
        pixmap.fill();
        skin.add("button-up", new Texture(pixmap));

        pixmap.setColor(new Color(0.4f, 0.4f, 0.4f, 1f));
        pixmap.fill();
        skin.add("button-down", new Texture(pixmap));

        pixmap.setColor(new Color(0.1f, 0.1f, 0.1f, 1f));
        pixmap.fill();
        skin.add("button-checked", new Texture(pixmap));

        pixmap.dispose();

        // Button style
        Button.ButtonStyle buttonStyle = new Button.ButtonStyle();
        buttonStyle.up = skin.newDrawable("button-up");
        buttonStyle.down = skin.newDrawable("button-down");
        buttonStyle.checked = skin.newDrawable("button-checked");
        skin.add("default", buttonStyle);

        // TextButton style
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = skin.newDrawable("button-up");
        textButtonStyle.down = skin.newDrawable("button-down");
        textButtonStyle.checked = skin.newDrawable("button-checked");
        textButtonStyle.font = skin.getFont("default-font");
        textButtonStyle.fontColor = Color.WHITE;
        skin.add("default", textButtonStyle);

        // Window style
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.background = skin.newDrawable("window-background");
        windowStyle.titleFont = skin.getFont("title-font");
        windowStyle.titleFontColor = Color.CYAN;
        skin.add("default", windowStyle);

        // ScrollPane style
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        skin.add("default", scrollStyle);

        return skin;
    }

    private void createTabbedUI() {
        inventoryWindow = new Window("Player Menu", uiSkin);
        inventoryWindow.setSize(800, 600);
        inventoryWindow.setPosition(Gdx.graphics.getWidth() / 2f - 400, Gdx.graphics.getHeight() / 2f - 300);
        inventoryWindow.setMovable(true);
        inventoryWindow.setResizable(true);
        inventoryWindow.setModal(false);

        // Create tab buttons
        tabButtons = new Table(uiSkin);
        TextButton inventoryBtn = new TextButton("Inventory", uiSkin);
        TextButton skillsBtn = new TextButton("Skills", uiSkin);
        TextButton equipmentBtn = new TextButton("Equipment", uiSkin);
        TextButton craftingBtn = new TextButton("Crafting", uiSkin);

        tabButtons.add(inventoryBtn).pad(5);
        tabButtons.add(skillsBtn).pad(5);
        tabButtons.add(equipmentBtn).pad(5);
        tabButtons.add(craftingBtn).pad(5);

        // Create tab content
        tabContent = new Table(uiSkin);

        inventoryTab = createInventoryTab();
        skillsTab = createSkillsTab();
        equipmentTab = createEquipmentTab();
        craftingTab = createCraftingTab();

        // Default to inventory tab
        tabContent.add(inventoryTab).expand().fill();

        // Tab switching listeners
        inventoryBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                switchTab(inventoryTab);
            }
        });
        skillsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                switchTab(skillsTab);
            }
        });
        equipmentBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                switchTab(equipmentTab);
            }
        });
        craftingBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                switchTab(craftingTab);
            }
        });

        inventoryWindow.add(tabButtons).row();
        inventoryWindow.add(tabContent).expand().fill();

        // Close button
        TextButton closeButton = new TextButton("X", uiSkin);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleInventoryUI();
            }
        });
        inventoryWindow.getTitleTable().add(closeButton).size(30, 30);

        uiStage.addActor(inventoryWindow);
        inventoryWindow.setVisible(false);
    }

    private void switchTab(Table newTab) {
        tabContent.clear();
        tabContent.add(newTab).expand().fill();
    }

    private Table createInventoryTab() {
        Table table = new Table(uiSkin);
        table.setFillParent(false);

        if (playerInventory != null) {
            int cols = playerInventory.getWidth();
            int rows = playerInventory.getHeight();

            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    ItemStack stack = playerInventory.getItemAt(x, y);
                    Image slot = new Image(createSlotTexture(stack));
                    slot.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            // Handle item click
                        }
                    });
                    table.add(slot).size(50, 50).pad(2);
                }
                table.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(table, uiSkin);
        Table container = new Table(uiSkin);
        container.add(scrollPane).expand().fill();
        return container;
    }

    private Table createSkillsTab() {
        Table table = new Table(uiSkin);
        table.setFillParent(false);

        if (skillSystem != null) {
            for (Skills.Skill skill : skillSystem.getAllSkills().values()) {
                table.add(new Label(skill.getName(), uiSkin)).left();
                table.add(new Label("Level " + skill.getLevel(), uiSkin)).center();
                table.add(new Label(skill.getXP() + " XP", uiSkin)).right();
                table.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(table, uiSkin);
        Table container = new Table(uiSkin);
        container.add(scrollPane).expand().fill();
        return container;
    }

    private Table createEquipmentTab() {
        Table table = new Table(uiSkin);
        table.setFillParent(false);

        if (equipmentManager != null) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                table.add(new Label(slot.name(), uiSkin)).left();
                IEquipment equipped = equipmentManager.getEquipped(slot);
                Image slotImage = new Image(createSlotTexture(null));
                table.add(slotImage).size(50, 50);
                table.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(table, uiSkin);
        Table container = new Table(uiSkin);
        container.add(scrollPane).expand().fill();
        return container;
    }

    private Table createCraftingTab() {
        Table table = new Table(uiSkin);
        table.setFillParent(false);

        if (craftingSystem != null) {
            for (CraftingSystem.CraftingRecipe recipe : craftingSystem.getAllRecipes().values()) {
                table.add(new Label(recipe.result, uiSkin)).left();
                TextButton craftButton = new TextButton("Craft", uiSkin);
                craftButton.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        // Handle crafting
                    }
                });
                table.add(craftButton).right();
                table.row();
            }
        }

        ScrollPane scrollPane = new ScrollPane(table, uiSkin);
        Table container = new Table(uiSkin);
        container.add(scrollPane).expand().fill();
        return container;
    }



    private TextureRegionDrawable createSlotTexture(ItemStack stack) {
        Pixmap pixmap = new Pixmap(50, 50, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.GRAY);
        pixmap.fillRectangle(0, 0, 50, 50);
        pixmap.setColor(Color.BLACK);
        pixmap.drawRectangle(0, 0, 50, 50);
        if (stack != null) {
            pixmap.setColor(Color.BLUE); // Placeholder for item color
            pixmap.fillRectangle(5, 5, 40, 40);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    public void toggleInventoryUI() {
        showInventoryUI = !showInventoryUI;
        inventoryWindow.setVisible(showInventoryUI);
        if (showInventoryUI) {
            Gdx.input.setInputProcessor(uiStage);
        } else {
            if (gameInputProcessor != null) {
                Gdx.input.setInputProcessor(gameInputProcessor);
            }
        }
    }

    public boolean isInventoryUIVisible() {
        return showInventoryUI;
    }

    public void setPlayerInventory(InventoryGrid inventory) {
        this.playerInventory = inventory;
    }

    public void setSkillSystem(Skills skills) {
        this.skillSystem = skills;
    }

    public void setEquipmentManager(EquipmentManager equipment) {
        this.equipmentManager = equipment;
    }

    public void setCraftingSystem(CraftingSystem crafting) {
        this.craftingSystem = crafting;
    }

    public void setGameInputProcessor(InputProcessor processor) {
        this.gameInputProcessor = processor;
    }

    public ShapeRenderer getShapeRenderer() {
        return shapeRenderer;
    }

    @Override
    public void dispose() {
        if (spriteBatch != null) spriteBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (uiStage != null) uiStage.dispose();
        if (uiSkin != null) uiSkin.dispose();
    }
}
