package com.astral.screens.shipbuilder;

import com.astral.AstralFrontier;
import com.astral.shipbuilding.*;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Starfield-inspired Ship Builder with:
 * - Blue snap point highlights
 * - Power allocation system
 * - Ship class (A/B/C) based on reactor
 * - Modular hab system
 * - Grav drive jump range
 * - Full part catalog
 */
public class StarfieldShipBuilderScreen implements Screen {

    private final AstralFrontier game;
    private final Screen returnScreen;

    // Core systems
    private ShipBuilder shipBuilder;
    private ShipPartCatalog catalog;
    private ShipPowerSystem powerSystem;

    // 3D rendering
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;

    // 2D UI
    private SpriteBatch batch;
    private BitmapFont font;
    private BitmapFont fontLarge;
    private GlyphLayout layout;

    // UI State
    private ShipPartType.PartCategory selectedCategory = ShipPartType.PartCategory.HULL;
    private Array<ShipPartCatalog.PartCatalogEntry> availableParts;
    private int selectedPartIndex = -1;
    private ShipPartType selectedPartType = null;
    private int scrollOffset = 0;

    // Camera controls
    private float cameraDistance = 50f;
    private float cameraYaw = 45f;
    private float cameraPitch = 25f;
    private Vector3 cameraTarget = new Vector3(0, 0, 0);

    // Part placement
    private Vector3 ghostPosition = new Vector3(0, 0, 0);
    private boolean placingPart = false;
    private ShipBuilder.SnapPoint nearestSnap = null;

    // Input state
    private int mouseX, mouseY;
    private int lastMouseX, lastMouseY;
    private boolean rightMouseDown = false;
    private boolean middleMouseDown = false;

    // Player resources
    private int credits = 250000;

    // UI Constants
    private static final Color STARFIELD_BLUE = new Color(0.2f, 0.6f, 1.0f, 1f);
    private static final Color STARFIELD_DARK = new Color(0.05f, 0.08f, 0.12f, 0.95f);
    private static final Color STARFIELD_PANEL = new Color(0.1f, 0.12f, 0.18f, 0.9f);
    private static final Color SNAP_POINT_COLOR = new Color(0.3f, 0.7f, 1.0f, 0.8f);
    private static final Color SNAP_POINT_ACTIVE = new Color(0.4f, 0.9f, 1.0f, 1.0f);

    private static final int LEFT_PANEL_WIDTH = 320;
    private static final int RIGHT_PANEL_WIDTH = 280;
    private static final int BOTTOM_PANEL_HEIGHT = 120;

    public StarfieldShipBuilderScreen(AstralFrontier game, Screen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
    }

    @Override
    public void show() {
        // Initialize 3D
        camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = 0.1f;
        camera.far = 1000f;
        updateCamera();

        modelBatch = new ModelBatch();
        shapeRenderer = new ShapeRenderer();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.5f, 0.5f, 0.55f, 1f));
        environment.add(new DirectionalLight().set(1f, 0.95f, 0.9f, -1f, -0.8f, -0.2f));
        environment.add(new DirectionalLight().set(0.3f, 0.35f, 0.4f, 1f, 0.5f, 0.2f));

        // Initialize 2D
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        fontLarge = new BitmapFont();
        fontLarge.getData().setScale(1.3f);
        layout = new GlyphLayout();

        // Initialize ship builder
        catalog = ShipPartCatalog.getInstance();
        shipBuilder = new ShipBuilder();
        powerSystem = new ShipPowerSystem();

        // Build default starter ship
        buildStarterShip();

        // Load parts for default category
        updateAvailableParts();

        // Setup input
        setupInput();
        Gdx.input.setCursorCatched(false);
    }

    private void buildStarterShip() {
        shipBuilder.clear();
        
        // Minimal flyable ship with proper spacing (parts are ~2-3 units long)
        shipBuilder.addPart(ShipPartType.HULL_COCKPIT, 0, 0, 6f);    // Front
        shipBuilder.addPart(ShipPartType.HULL_MID, 0, 0, 2f);        // Center
        shipBuilder.addPart(ShipPartType.HULL_AFT, 0, 0, -2f);       // Aft
        shipBuilder.addPart(ShipPartType.REACTOR_CLASS_A, 0, -1f, 0); // Below center
        shipBuilder.addPart(ShipPartType.GRAV_DRIVE_BASIC, 0, 0, -5f); // Behind aft
        shipBuilder.addPart(ShipPartType.ENGINE_SMALL, 0, 0, -8f);   // Rear engine
        shipBuilder.addPart(ShipPartType.LANDING_GEAR_SMALL, -2f, -2f, 3f);  // Front left gear
        shipBuilder.addPart(ShipPartType.LANDING_GEAR_SMALL, 2f, -2f, 3f);   // Front right gear
        shipBuilder.addPart(ShipPartType.LANDING_GEAR_SMALL, 0, -2f, -4f);   // Rear gear

        updatePowerSystem();
    }

    private void updatePowerSystem() {
        // Find reactor type and set power
        ShipPartType reactorType = null;
        int weaponReq = 0, shieldReq = 0, engineReq = 0, gravReq = 0;

        for (ShipPart part : shipBuilder.getParts()) {
            ShipPartType type = part.getType();
            if (type.getCategory() == ShipPartType.PartCategory.REACTOR) {
                reactorType = type;
            }
            weaponReq += part.getPowerConsumption();
            if (type.getCategory() == ShipPartType.PartCategory.SHIELD) {
                shieldReq += part.getPowerConsumption();
            }
            if (type.getCategory() == ShipPartType.PartCategory.ENGINE) {
                engineReq += part.getPowerConsumption();
            }
            if (type.getCategory() == ShipPartType.PartCategory.GRAV_DRIVE) {
                gravReq += part.getPowerConsumption();
            }
        }

        if (reactorType != null) {
            powerSystem.setReactor(reactorType);
        }
        powerSystem.setRequirements(
            Math.min(weaponReq, 6),
            Math.min(shieldReq, 6),
            Math.min(engineReq, 6),
            Math.min(gravReq, 6)
        );
        powerSystem.resetAllocation();
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                handleKey(keycode);
                return true;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                mouseX = screenX;
                mouseY = Gdx.graphics.getHeight() - screenY;
                lastMouseX = screenX;
                lastMouseY = screenY;

                if (button == Input.Buttons.LEFT) {
                    handleLeftClick();
                } else if (button == Input.Buttons.RIGHT) {
                    rightMouseDown = true;
                } else if (button == Input.Buttons.MIDDLE) {
                    middleMouseDown = true;
                }
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.RIGHT) rightMouseDown = false;
                if (button == Input.Buttons.MIDDLE) middleMouseDown = false;
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                mouseX = screenX;
                mouseY = Gdx.graphics.getHeight() - screenY;

                if (rightMouseDown || middleMouseDown) {
                    int deltaX = screenX - lastMouseX;
                    int deltaY = screenY - lastMouseY;
                    cameraYaw -= deltaX * 0.3f;
                    cameraPitch += deltaY * 0.3f;
                    cameraPitch = MathUtils.clamp(cameraPitch, -80f, 80f);
                    updateCamera();
                }

                lastMouseX = screenX;
                lastMouseY = screenY;
                return true;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (mouseX < LEFT_PANEL_WIDTH) {
                    // Scroll part list
                    scrollOffset = (int) MathUtils.clamp(
                        scrollOffset + amountY * 2,
                        0,
                        Math.max(0, availableParts.size - 8)
                    );
                } else {
                    // Zoom camera
                    cameraDistance += amountY * 3f;
                    cameraDistance = MathUtils.clamp(cameraDistance, 20f, 200f);
                    updateCamera();
                }
                return true;
            }

            @Override
            public boolean mouseMoved(int screenX, int screenY) {
                mouseX = screenX;
                mouseY = Gdx.graphics.getHeight() - screenY;
                return true;
            }
        });
    }

    private void handleKey(int keycode) {
        switch (keycode) {
            case Input.Keys.ESCAPE:
                if (placingPart) {
                    placingPart = false;
                    selectedPartType = null;
                } else {
                    game.setScreen(returnScreen);
                }
                break;
            case Input.Keys.NUM_1:
                selectCategory(ShipPartType.PartCategory.HULL);
                break;
            case Input.Keys.NUM_2:
                selectCategory(ShipPartType.PartCategory.HAB);
                break;
            case Input.Keys.NUM_3:
                selectCategory(ShipPartType.PartCategory.REACTOR);
                break;
            case Input.Keys.NUM_4:
                selectCategory(ShipPartType.PartCategory.GRAV_DRIVE);
                break;
            case Input.Keys.NUM_5:
                selectCategory(ShipPartType.PartCategory.ENGINE);
                break;
            case Input.Keys.NUM_6:
                selectCategory(ShipPartType.PartCategory.WEAPON);
                break;
            case Input.Keys.NUM_7:
                selectCategory(ShipPartType.PartCategory.SHIELD);
                break;
            case Input.Keys.NUM_8:
                selectCategory(ShipPartType.PartCategory.LANDING_GEAR);
                break;
            case Input.Keys.NUM_9:
                selectCategory(ShipPartType.PartCategory.DOCKER);
                break;
            case Input.Keys.NUM_0:
                selectCategory(ShipPartType.PartCategory.UTILITY);
                break;
            case Input.Keys.W:
                if (placingPart) ghostPosition.z -= 0.5f;
                break;
            case Input.Keys.S:
                if (placingPart) ghostPosition.z += 0.5f;
                break;
            case Input.Keys.A:
                if (placingPart) ghostPosition.x -= 0.5f;
                break;
            case Input.Keys.D:
                if (placingPart) ghostPosition.x += 0.5f;
                break;
            case Input.Keys.SPACE:
                if (placingPart) ghostPosition.y += 0.5f;
                break;
            case Input.Keys.SHIFT_LEFT:
                if (placingPart) ghostPosition.y -= 0.5f;
                break;
            case Input.Keys.ENTER:
                if (placingPart && selectedPartType != null) {
                    placePart();
                } else if (selectedPartIndex >= 0) {
                    startPlacingPart();
                }
                break;
            case Input.Keys.BACKSPACE:
                removeLastPart();
                break;
            case Input.Keys.C:
                shipBuilder.clear();
                buildStarterShip();
                break;
            // Power allocation
            case Input.Keys.F1:
                powerSystem.incrementWeaponPower();
                break;
            case Input.Keys.F2:
                powerSystem.decrementWeaponPower();
                break;
            case Input.Keys.F3:
                powerSystem.incrementShieldPower();
                break;
            case Input.Keys.F4:
                powerSystem.decrementShieldPower();
                break;
            case Input.Keys.F5:
                powerSystem.incrementEnginePower();
                break;
            case Input.Keys.F6:
                powerSystem.decrementEnginePower();
                break;
            case Input.Keys.F7:
                powerSystem.incrementGravDrivePower();
                break;
            case Input.Keys.F8:
                powerSystem.decrementGravDrivePower();
                break;
        }
    }

    private void handleLeftClick() {
        // Check category buttons
        if (mouseY > Gdx.graphics.getHeight() - 40) {
            int catIndex = mouseX / 90;
            ShipPartType.PartCategory[] cats = ShipPartType.PartCategory.values();
            if (catIndex < cats.length) {
                selectCategory(cats[catIndex]);
                return;
            }
        }

        // Check parts list
        if (mouseX < LEFT_PANEL_WIDTH && mouseY > BOTTOM_PANEL_HEIGHT && mouseY < Gdx.graphics.getHeight() - 50) {
            int index = scrollOffset + (int) ((Gdx.graphics.getHeight() - mouseY - 50) / 55);
            if (index >= 0 && index < availableParts.size) {
                selectedPartIndex = index;
                startPlacingPart();
                return;
            }
        }

        // Place part if in placing mode
        if (placingPart && selectedPartType != null) {
            placePart();
        }
    }

    private void selectCategory(ShipPartType.PartCategory category) {
        selectedCategory = category;
        selectedPartIndex = -1;
        scrollOffset = 0;
        updateAvailableParts();
    }

    private void updateAvailableParts() {
        availableParts = catalog.getPartsByCategory(selectedCategory);
    }

    private void startPlacingPart() {
        if (selectedPartIndex >= 0 && selectedPartIndex < availableParts.size) {
            ShipPartCatalog.PartCatalogEntry entry = availableParts.get(selectedPartIndex);
            if (entry.cost <= credits) {
                selectedPartType = entry.type;
                placingPart = true;
                ghostPosition.set(0, 0, 0);
            }
        }
    }

    private void placePart() {
        if (selectedPartType == null) return;

        ShipPartCatalog.PartCatalogEntry entry = catalog.getEntry(selectedPartType);
        if (entry == null || entry.cost > credits) return;

        // Use snap point position if near one
        Vector3 placePos = new Vector3(ghostPosition);
        if (nearestSnap != null) {
            placePos.set(nearestSnap.position);
        }

        // Snap to grid
        placePos.x = Math.round(placePos.x * 2) / 2f;
        placePos.y = Math.round(placePos.y * 2) / 2f;
        placePos.z = Math.round(placePos.z * 2) / 2f;

        shipBuilder.addPart(selectedPartType, placePos.x, placePos.y, placePos.z);
        credits -= entry.cost;

        updatePowerSystem();

        placingPart = false;
        selectedPartType = null;
    }

    private void removeLastPart() {
        Array<ShipPart> parts = shipBuilder.getParts();
        if (parts.size > 0) {
            ShipPart lastPart = parts.get(parts.size - 1);
            ShipPartCatalog.PartCatalogEntry entry = catalog.getEntry(lastPart.getType());
            if (entry != null) {
                credits += entry.cost / 2; // 50% refund
            }
            shipBuilder.removePart(lastPart);
            updatePowerSystem();
        }
    }

    private void updateCamera() {
        float yawRad = cameraYaw * MathUtils.degreesToRadians;
        float pitchRad = cameraPitch * MathUtils.degreesToRadians;

        float h = cameraDistance * MathUtils.cos(pitchRad);
        float v = cameraDistance * MathUtils.sin(pitchRad);

        camera.position.set(
            cameraTarget.x + h * MathUtils.sin(yawRad),
            cameraTarget.y + v,
            cameraTarget.z + h * MathUtils.cos(yawRad)
        );
        camera.lookAt(cameraTarget);
        camera.up.set(0, 1, 0);
        camera.update();
    }

    private void updateGhostPosition() {
        if (!placingPart) {
            nearestSnap = null;
            return;
        }

        // Find nearest snap point
        nearestSnap = shipBuilder.findNearestSnapPoint(ghostPosition);
    }

    @Override
    public void render(float delta) {
        updateGhostPosition();

        // Clear
        Gdx.gl.glClearColor(0.02f, 0.03f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // 3D rendering
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        // Draw grid
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.15f, 0.18f, 0.22f, 0.5f);
        for (int i = -15; i <= 15; i++) {
            shapeRenderer.line(i * 2, 0, -30, i * 2, 0, 30);
            shapeRenderer.line(-30, 0, i * 2, 30, 0, i * 2);
        }
        shapeRenderer.end();

        // Render ship
        modelBatch.begin(camera);
        for (ModelInstance instance : shipBuilder.getModelInstances()) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();

        // Draw snap points (Starfield-style blue highlights)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (ShipBuilder.SnapPoint snap : shipBuilder.getSnapPoints()) {
            boolean isNearest = (snap == nearestSnap);
            Color c = isNearest ? SNAP_POINT_ACTIVE : SNAP_POINT_COLOR;
            float size = isNearest ? 0.4f : 0.25f;
            
            shapeRenderer.setColor(c);
            drawSnapPoint(snap.position, size);
        }

        // Draw ghost part position
        if (placingPart) {
            shapeRenderer.setColor(0.3f, 0.9f, 0.4f, 0.4f);
            drawBox(ghostPosition, 1.5f, 1f, 2f);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        // 2D UI
        renderUI();
    }

    private void drawSnapPoint(Vector3 pos, float size) {
        // Draw a simple box for snap points (ShapeRenderer.triangle is 2D only)
        float hs = size / 2;
        shapeRenderer.box(pos.x - hs, pos.y - hs, pos.z + hs, size, size, size);
    }

    private void drawBox(Vector3 center, float w, float h, float d) {
        float hw = w / 2, hh = h / 2, hd = d / 2;
        shapeRenderer.box(center.x - hw, center.y - hh, center.z + hd, w, h, d);
    }

    private void renderUI() {
        batch.begin();

        int screenW = Gdx.graphics.getWidth();
        int screenH = Gdx.graphics.getHeight();

        // Draw panels
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Left panel (parts list)
        shapeRenderer.setColor(STARFIELD_DARK);
        shapeRenderer.rect(0, BOTTOM_PANEL_HEIGHT, LEFT_PANEL_WIDTH, screenH - BOTTOM_PANEL_HEIGHT);

        // Right panel (stats)
        shapeRenderer.setColor(STARFIELD_DARK);
        shapeRenderer.rect(screenW - RIGHT_PANEL_WIDTH, BOTTOM_PANEL_HEIGHT, RIGHT_PANEL_WIDTH, screenH - BOTTOM_PANEL_HEIGHT);

        // Bottom panel (power allocation)
        shapeRenderer.setColor(STARFIELD_PANEL);
        shapeRenderer.rect(0, 0, screenW, BOTTOM_PANEL_HEIGHT);

        // Category tabs
        shapeRenderer.setColor(STARFIELD_PANEL);
        shapeRenderer.rect(0, screenH - 40, screenW, 40);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
        batch.begin();

        // Title
        fontLarge.setColor(STARFIELD_BLUE);
        fontLarge.draw(batch, "SHIP BUILDER", 10, screenH - 50);
        font.setColor(Color.WHITE);

        // Category tabs
        ShipPartType.PartCategory[] cats = ShipPartType.PartCategory.values();
        for (int i = 0; i < Math.min(cats.length, 10); i++) {
            boolean selected = cats[i] == selectedCategory;
            font.setColor(selected ? STARFIELD_BLUE : Color.LIGHT_GRAY);
            String label = (i + 1) % 10 + ":" + cats[i].getDisplayName();
            font.draw(batch, label, 10 + i * 90, screenH - 15);
        }

        // Parts list
        font.setColor(Color.WHITE);
        float y = screenH - 80;
        for (int i = scrollOffset; i < Math.min(scrollOffset + 10, availableParts.size); i++) {
            ShipPartCatalog.PartCatalogEntry entry = availableParts.get(i);
            boolean selected = (i == selectedPartIndex);

            if (selected) {
                font.setColor(STARFIELD_BLUE);
            } else {
                font.setColor(Color.WHITE);
            }
            font.draw(batch, entry.displayName, 15, y);

            font.setColor(entry.cost <= credits ? Color.GREEN : Color.RED);
            font.draw(batch, entry.cost + " cr", 15, y - 15);

            font.setColor(Color.GRAY);
            font.draw(batch, "Mass: " + (int)entry.stats.mass + "kg", 150, y - 15);

            y -= 55;
        }

        // Right panel - Ship Stats
        float rx = screenW - RIGHT_PANEL_WIDTH + 15;
        float ry = screenH - 80;

        fontLarge.setColor(STARFIELD_BLUE);
        fontLarge.draw(batch, "SHIP STATS", rx, ry);
        ry -= 35;

        font.setColor(Color.WHITE);
        
        // Ship class
        font.setColor(STARFIELD_BLUE);
        font.draw(batch, "CLASS " + powerSystem.getShipClass().getLabel(), rx, ry);
        ry -= 25;

        font.setColor(Color.WHITE);
        font.draw(batch, "Mass: " + (int) shipBuilder.getTotalMass() + " kg", rx, ry);
        ry -= 20;

        // Mass limit warning
        if (!powerSystem.isValidMass(shipBuilder.getTotalMass())) {
            font.setColor(Color.RED);
            font.draw(batch, "EXCEEDS CLASS LIMIT!", rx, ry);
            ry -= 20;
        }

        font.setColor(Color.WHITE);
        font.draw(batch, "Hull: " + (int) shipBuilder.getTotalHull() + " HP", rx, ry);
        ry -= 20;
        font.draw(batch, "Shield: " + (int) shipBuilder.getTotalShield() + " HP", rx, ry);
        ry -= 20;
        font.draw(batch, "Thrust: " + (int) (shipBuilder.getTotalThrust() / 1000) + " kN", rx, ry);
        ry -= 20;

        float tw = shipBuilder.getThrustToWeight();
        font.setColor(tw >= 0.5f ? Color.GREEN : tw >= 0.3f ? Color.YELLOW : Color.RED);
        font.draw(batch, "T/W Ratio: " + String.format("%.2f", tw), rx, ry);
        ry -= 20;

        font.setColor(Color.WHITE);
        font.draw(batch, "Fuel: " + (int) shipBuilder.getTotalFuel() + " L", rx, ry);
        ry -= 30;

        // Crew capacity
        int crew = 0;
        float jumpRange = 0;
        for (ShipPart part : shipBuilder.getParts()) {
            crew += part.getCrewCapacity();
            jumpRange = Math.max(jumpRange, part.getJumpRange());
        }
        font.setColor(Color.CYAN);
        font.draw(batch, "Crew Capacity: " + crew, rx, ry);
        ry -= 20;
        font.draw(batch, "Jump Range: " + (int) jumpRange + " LY", rx, ry);
        ry -= 30;

        // Validation
        font.setColor(Color.WHITE);
        font.draw(batch, "Validation:", rx, ry);
        ry -= 20;
        String status = shipBuilder.getValidationSummary();
        font.setColor(shipBuilder.isValid() ? Color.GREEN : Color.RED);
        font.draw(batch, status, rx, ry);

        // Bottom panel - Power allocation
        font.setColor(STARFIELD_BLUE);
        font.draw(batch, "POWER ALLOCATION", 20, BOTTOM_PANEL_HEIGHT - 15);

        font.setColor(Color.WHITE);
        font.draw(batch, "Reactor: " + powerSystem.getUsedPower() + "/" + powerSystem.getReactorPower(), 180, BOTTOM_PANEL_HEIGHT - 15);

        // Power bars
        renderPowerBar("WPN", powerSystem.getWeaponPower(), powerSystem.getWeaponPowerReq(), 20, 30, Color.RED);
        renderPowerBar("SHD", powerSystem.getShieldPower(), powerSystem.getShieldPowerReq(), 170, 30, Color.CYAN);
        renderPowerBar("ENG", powerSystem.getEnginePower(), powerSystem.getEnginePowerReq(), 320, 30, Color.YELLOW);
        renderPowerBar("GRV", powerSystem.getGravDrivePower(), powerSystem.getGravDrivePowerReq(), 470, 30, Color.PURPLE);

        // Credits
        font.setColor(Color.GOLD);
        font.draw(batch, "Credits: " + String.format("%,d", credits), screenW - 200, BOTTOM_PANEL_HEIGHT - 15);

        // Controls help
        font.setColor(Color.GRAY);
        font.draw(batch, "[ESC] Exit  [RMB] Rotate  [Scroll] Zoom  [WASD/Space/Shift] Move part  [Enter] Place  [Del] Remove", 
            620, BOTTOM_PANEL_HEIGHT - 15);

        batch.end();
    }

    private void renderPowerBar(String label, int current, int max, float x, float y, Color color) {
        batch.end();
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background
        shapeRenderer.setColor(0.2f, 0.2f, 0.25f, 1f);
        for (int i = 0; i < 6; i++) {
            shapeRenderer.rect(x + i * 20, y, 16, 40);
        }

        // Filled bars
        shapeRenderer.setColor(color);
        for (int i = 0; i < current; i++) {
            shapeRenderer.rect(x + i * 20 + 2, y + 2, 12, 36);
        }

        // Max indicator
        shapeRenderer.setColor(0.4f, 0.4f, 0.45f, 1f);
        for (int i = current; i < max && i < 6; i++) {
            shapeRenderer.rect(x + i * 20 + 2, y + 2, 12, 36);
        }

        shapeRenderer.end();
        batch.begin();

        font.setColor(Color.WHITE);
        font.draw(batch, label, x, y + 55);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        modelBatch.dispose();
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
        fontLarge.dispose();
        shipBuilder.dispose();
    }
}
