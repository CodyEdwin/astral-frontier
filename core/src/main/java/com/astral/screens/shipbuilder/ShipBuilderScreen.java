package com.astral.screens.shipbuilder;

import com.astral.AstralFrontier;
import com.astral.components.ShipComponent;
import com.astral.shipbuilding.ShipBuilder;
import com.astral.shipbuilding.ShipPart;
import com.astral.shipbuilding.ShipPartType;
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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Ship Builder Screen - Interactive UI for building and customizing ships
 * Uses simple programmatic UI instead of Scene2D
 */
public class ShipBuilderScreen implements Screen, Disposable {

    private final AstralFrontier game;
    private final Screen returnScreen;

    // 3D Rendering
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;

    // Ship Builder
    private ShipBuilder shipBuilder;
    private ShipBuilderState builderState;
    private Array<ModelInstance> shipModelInstances;

    // UI
    private SpriteBatch spriteBatch;
    private BitmapFont font;
    private BitmapFont titleFont;
    private GlyphLayout glyphLayout;

    // UI Buttons and Areas
    private Array<UIButton> buttons;
    private Array<UIPartButton> partButtons;
    private Rectangle partsPanelRect;
    private Rectangle statsPanelRect;
    private float partScrollOffset = 0;
    private float maxPartScroll = 0;

    // Camera controls
    private float cameraDistance = 30f;
    private float cameraYaw = 45f;
    private float cameraPitch = 20f;
    private Vector3 cameraTarget = new Vector3(0, 0, 0);

    // Selection state
    private ShipPartType selectedPartType = null;
    private Vector3 ghostPartPosition = new Vector3();
    private boolean showGhostPart = false;

    // Grid settings
    private float gridSnapSize = 0.5f;
    private boolean snapToGrid = true;

    // Colors
    private Color primaryColor = new Color(0.5f, 0.5f, 0.55f, 1f);
    private Color secondaryColor = new Color(0.3f, 0.3f, 0.35f, 1f);
    private Color accentColor = new Color(0.2f, 0.6f, 0.9f, 1f);

    // UI Colors
    private Color panelColor = new Color(0.1f, 0.1f, 0.15f, 0.9f);
    private Color buttonColor = new Color(0.2f, 0.3f, 0.4f, 1f);
    private Color buttonHoverColor = new Color(0.3f, 0.4f, 0.5f, 1f);
    private Color selectedColor = new Color(0.4f, 0.6f, 0.8f, 1f);

    // Input state
    private int mouseX, mouseY;
    private boolean leftMousePressed = false;
    private boolean rightMousePressed = false;
    private boolean middleMousePressed = false;
    private int lastMouseX, lastMouseY;

    public ShipBuilderScreen(AstralFrontier game, Screen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
        this.builderState = new ShipBuilderState();
    }

    @Override
    public void show() {
        Gdx.app.log("ShipBuilderScreen", "Initializing ship builder...");

        // Initialize 3D rendering
        initializeRendering();

        // Initialize ship builder
        shipBuilder = new ShipBuilder();
        shipBuilder.setColors(primaryColor, secondaryColor, accentColor);

        // Start with a basic fighter template
        if (builderState.loadedConfiguration != null) {
            applyConfiguration(builderState.loadedConfiguration);
        } else {
            shipBuilder.buildFighter();
        }

        updateShipModel();

        // Initialize UI
        initializeUI();

        // Setup input
        setupInput();

        // Make sure mouse cursor is visible
        Gdx.input.setCursorCatched(false);

        Gdx.app.log("ShipBuilderScreen", "Ship builder ready!");
    }

    private void initializeRendering() {
        // Setup camera
        camera = new PerspectiveCamera(
            67,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );
        updateCameraPosition();
        camera.near = 0.1f;
        camera.far = 1000f;
        camera.update();

        // Setup rendering
        modelBatch = new ModelBatch();
        shapeRenderer = new ShapeRenderer();

        // Setup environment
        environment = new Environment();
        environment.set(
            new ColorAttribute(
                ColorAttribute.AmbientLight,
                0.4f,
                0.4f,
                0.4f,
                1f
            )
        );

        DirectionalLight keyLight = new DirectionalLight();
        keyLight.set(1f, 1f, 0.95f, -1f, -0.8f, -0.2f);
        environment.add(keyLight);

        DirectionalLight fillLight = new DirectionalLight();
        fillLight.set(0.4f, 0.5f, 0.6f, 1f, 0.5f, 0.5f);
        environment.add(fillLight);

        DirectionalLight rimLight = new DirectionalLight();
        rimLight.set(0.6f, 0.6f, 0.7f, 0.5f, 0.2f, -1f);
        environment.add(rimLight);

        shipModelInstances = new Array<>();
    }

    private void initializeUI() {
        spriteBatch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(1.0f);

        titleFont = new BitmapFont();
        titleFont.setColor(Color.WHITE);
        titleFont.getData().setScale(1.5f);

        glyphLayout = new GlyphLayout();

        buttons = new Array<>();
        partButtons = new Array<>();

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        // Define panel areas
        partsPanelRect = new Rectangle(10, 100, 250, screenHeight - 200);
        statsPanelRect = new Rectangle(
            screenWidth - 310,
            100,
            300,
            screenHeight - 200
        );

        // Create top toolbar buttons
        float buttonY = screenHeight - 50;
        buttons.add(
            new UIButton("Presets", 150, buttonY, 80, 30, this::showPresetsMenu)
        );
        buttons.add(
            new UIButton("Clear", 240, buttonY, 70, 30, this::clearShip)
        );
        buttons.add(
            new UIButton("Save", 320, buttonY, 60, 30, this::saveConfiguration)
        );
        buttons.add(
            new UIButton("Apply", 390, buttonY, 70, 30, this::applyToPlayerShip)
        );
        buttons.add(
            new UIButton(
                "Exit",
                screenWidth - 80,
                buttonY,
                70,
                30,
                this::exitBuilder
            )
        );

        // Create part buttons
        float partY = screenHeight - 130;
        for (ShipPartType.PartCategory category : ShipPartType.PartCategory.values()) {
            partY -= 30;
            for (ShipPartType partType : ShipPartType.values()) {
                if (partType.getCategory() == category) {
                    UIPartButton btn = new UIPartButton(
                        partType,
                        20,
                        partY,
                        230,
                        25
                    );
                    partButtons.add(btn);
                    partY -= 27;
                }
            }
        }

        maxPartScroll = Math.max(0, -partY + 200);
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(
            new InputAdapter() {
                @Override
                public boolean touchDown(
                    int screenX,
                    int screenY,
                    int pointer,
                    int button
                ) {
                    mouseX = screenX;
                    mouseY = Gdx.graphics.getHeight() - screenY;

                    if (button == Input.Buttons.LEFT) {
                        leftMousePressed = true;
                        handleLeftClick(mouseX, mouseY);
                    } else if (button == Input.Buttons.RIGHT) {
                        rightMousePressed = true;
                        removePart();
                    } else if (button == Input.Buttons.MIDDLE) {
                        middleMousePressed = true;
                    }

                    lastMouseX = screenX;
                    lastMouseY = screenY;
                    return true;
                }

                @Override
                public boolean touchUp(
                    int screenX,
                    int screenY,
                    int pointer,
                    int button
                ) {
                    if (button == Input.Buttons.LEFT) leftMousePressed = false;
                    if (button == Input.Buttons.RIGHT) rightMousePressed =
                        false;
                    if (button == Input.Buttons.MIDDLE) middleMousePressed =
                        false;
                    return true;
                }

                @Override
                public boolean touchDragged(
                    int screenX,
                    int screenY,
                    int pointer
                ) {
                    mouseX = screenX;
                    mouseY = Gdx.graphics.getHeight() - screenY;
                    return true;
                }

                @Override
                public boolean mouseMoved(int screenX, int screenY) {
                    mouseX = screenX;
                    mouseY = Gdx.graphics.getHeight() - screenY;
                    return true;
                }

                @Override
                public boolean scrolled(float amountX, float amountY) {
                    if (
                        mouseX >= partsPanelRect.x &&
                        mouseX <= partsPanelRect.x + partsPanelRect.width &&
                        mouseY >= partsPanelRect.y &&
                        mouseY <= partsPanelRect.y + partsPanelRect.height
                    ) {
                        // Scroll parts panel
                        partScrollOffset = Math.max(
                            0,
                            Math.min(
                                maxPartScroll,
                                partScrollOffset + amountY * 20
                            )
                        );
                    } else {
                        // Zoom camera
                        cameraDistance += amountY * 2f;
                        cameraDistance = Math.max(
                            5,
                            Math.min(100, cameraDistance)
                        );
                    }
                    return true;
                }

                @Override
                public boolean keyDown(int keycode) {
                    if (keycode == Input.Keys.ESCAPE) {
                        exitBuilder();
                        return true;
                    }
                    return false;
                }
            }
        );
    }

    private void handleLeftClick(int x, int y) {
        // Check buttons
        for (UIButton button : buttons) {
            if (button.contains(x, y)) {
                button.action.run();
                return;
            }
        }

        // Check part buttons
        for (UIPartButton button : partButtons) {
            Rectangle adjustedRect = new Rectangle(
                button.rect.x,
                button.rect.y + partScrollOffset,
                button.rect.width,
                button.rect.height
            );
            if (
                adjustedRect.contains(x, y) &&
                adjustedRect.y >= partsPanelRect.y &&
                adjustedRect.y + adjustedRect.height <=
                partsPanelRect.y + partsPanelRect.height
            ) {
                selectPartType(button.partType);
                return;
            }
        }

        // Place part if one is selected
        if (selectedPartType != null && !isOverUI(x, y)) {
            placePart();
        }
    }

    private boolean isOverUI(int x, int y) {
        if (partsPanelRect.contains(x, y)) return true;
        if (statsPanelRect.contains(x, y)) return true;
        if (y > Gdx.graphics.getHeight() - 80) return true; // Top toolbar
        return false;
    }

    private void selectPartType(ShipPartType type) {
        selectedPartType = type;
        showGhostPart = true;
        ghostPartPosition.set(0, 0, 0);
        Gdx.app.log(
            "ShipBuilder",
            "Selected part type: " + type.getDisplayName()
        );
    }

    private void showPresetsMenu() {
        // Cycle through presets for simplicity
        String[] presets = {
            "Fighter",
            "Interceptor",
            "Assault",
            "Freighter",
            "Scout",
        };
        int currentIndex = 0;

        // For now, just load Fighter
        loadPreset("Fighter");
        Gdx.app.log(
            "ShipBuilder",
            "Preset menu - loading Fighter (cycle through presets not yet implemented)"
        );
    }

    private void loadPreset(String presetName) {
        shipBuilder.clear();

        switch (presetName) {
            case "Fighter":
                shipBuilder.buildFighter();
                break;
            case "Interceptor":
                shipBuilder.buildInterceptor();
                break;
            case "Assault":
                shipBuilder.buildAssault();
                break;
            case "Freighter":
                shipBuilder.buildFreighter();
                break;
            case "Scout":
                shipBuilder.buildScout();
                break;
        }

        updateShipModel();
        Gdx.app.log("ShipBuilder", "Loaded preset: " + presetName);
    }

    private void clearShip() {
        shipBuilder.clear();
        updateShipModel();
        Gdx.app.log("ShipBuilder", "Ship cleared");
    }

    private void saveConfiguration() {
        builderState.saveConfiguration(shipBuilder);
        Gdx.app.log("ShipBuilder", "Configuration saved");
    }

    private void applyToPlayerShip() {
        builderState.saveConfiguration(shipBuilder);
        Gdx.app.log(
            "ShipBuilder",
            "Configuration applied - will take effect on next ship creation"
        );
    }

    private void exitBuilder() {
        // Restore input state when exiting
        Gdx.input.setCursorCatched(false);
        game.setScreen(returnScreen);
    }

    private void updateShipModel() {
        shipModelInstances = shipBuilder.getModelInstances();
    }

    private void placePart() {
        if (selectedPartType == null) return;

        Vector3 pos = new Vector3(ghostPartPosition);
        if (snapToGrid) {
            pos.x = Math.round(pos.x / gridSnapSize) * gridSnapSize;
            pos.y = Math.round(pos.y / gridSnapSize) * gridSnapSize;
            pos.z = Math.round(pos.z / gridSnapSize) * gridSnapSize;
        }

        shipBuilder.addPart(selectedPartType, pos.x, pos.y, pos.z);
        updateShipModel();

        Gdx.app.log(
            "ShipBuilder",
            "Placed part: " + selectedPartType.getDisplayName() + " at " + pos
        );
    }

    private void removePart() {
        Array<ShipPart> parts = shipBuilder.getParts();
        if (parts.size > 0) {
            parts.removeIndex(parts.size - 1);
            updateShipModel();
            Gdx.app.log("ShipBuilder", "Removed last part");
        }
    }

    @Override
    public void render(float delta) {
        handleInput(delta);
        updateCamera();

        // Clear screen
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render 3D
        render3D();

        // Render UI
        renderUI();
    }

    private void handleInput(float delta) {
        // Camera rotation with middle mouse
        if (middleMousePressed) {
            int currentX = Gdx.input.getX();
            int currentY = Gdx.input.getY();
            if (lastMouseX != 0 || lastMouseY != 0) {
                int deltaX = currentX - lastMouseX;
                int deltaY = currentY - lastMouseY;
                cameraYaw -= deltaX * 0.3f;
                cameraPitch += deltaY * 0.3f;
                cameraPitch = Math.max(-89, Math.min(89, cameraPitch));
            }
            lastMouseX = currentX;
            lastMouseY = currentY;
        } else {
            // Update mouse position for next drag
            lastMouseX = Gdx.input.getX();
            lastMouseY = Gdx.input.getY();
        }

        // Part position adjustment with WASD
        if (showGhostPart && selectedPartType != null) {
            float moveSpeed = 0.5f * delta * 10f;
            if (Gdx.input.isKeyPressed(Input.Keys.W)) ghostPartPosition.z +=
                moveSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) ghostPartPosition.z -=
                moveSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) ghostPartPosition.x -=
                moveSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) ghostPartPosition.x +=
                moveSpeed;
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) ghostPartPosition.y +=
                moveSpeed;
            if (
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
            ) ghostPartPosition.y -= moveSpeed;
        }
    }

    private void updateCamera() {
        updateCameraPosition();
        camera.update();
    }

    private void updateCameraPosition() {
        double yawRad = Math.toRadians(cameraYaw);
        double pitchRad = Math.toRadians(cameraPitch);

        camera.position.x =
            cameraTarget.x +
            (float) (cameraDistance * Math.cos(pitchRad) * Math.cos(yawRad));
        camera.position.y =
            cameraTarget.y + (float) (cameraDistance * Math.sin(pitchRad));
        camera.position.z =
            cameraTarget.z +
            (float) (cameraDistance * Math.cos(pitchRad) * Math.sin(yawRad));

        camera.lookAt(cameraTarget);
        camera.up.set(0, 1, 0);
    }

    private void render3D() {
        modelBatch.begin(camera);

        // Render ship parts
        for (ModelInstance instance : shipModelInstances) {
            modelBatch.render(instance, environment);
        }

        modelBatch.end();

        // Render grid
        renderGrid();
    }

    private void renderGrid() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 0.5f);

        float gridSize = 20f;
        float step = 1f;

        // XZ grid
        for (float i = -gridSize; i <= gridSize; i += step) {
            shapeRenderer.line(i, 0, -gridSize, i, 0, gridSize);
            shapeRenderer.line(-gridSize, 0, i, gridSize, 0, i);
        }

        // Origin axes
        shapeRenderer.setColor(1, 0, 0, 0.7f);
        shapeRenderer.line(0, 0, 0, 5, 0, 0); // X axis
        shapeRenderer.setColor(0, 1, 0, 0.7f);
        shapeRenderer.line(0, 0, 0, 0, 5, 0); // Y axis
        shapeRenderer.setColor(0, 0, 1, 0.7f);
        shapeRenderer.line(0, 0, 0, 0, 0, 5); // Z axis

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderUI() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw camera rotation indicator when middle mouse is pressed
        if (middleMousePressed) {
            shapeRenderer.setColor(0.2f, 0.6f, 0.9f, 0.3f);
            shapeRenderer.circle(mouseX, mouseY, 30);
        }

        // Top toolbar background
        shapeRenderer.setColor(panelColor);
        shapeRenderer.rect(
            0,
            Gdx.graphics.getHeight() - 80,
            Gdx.graphics.getWidth(),
            80
        );

        // Parts panel background
        shapeRenderer.setColor(panelColor);
        shapeRenderer.rect(
            partsPanelRect.x,
            partsPanelRect.y,
            partsPanelRect.width,
            partsPanelRect.height
        );

        // Stats panel background
        shapeRenderer.setColor(panelColor);
        shapeRenderer.rect(
            statsPanelRect.x,
            statsPanelRect.y,
            statsPanelRect.width,
            statsPanelRect.height
        );

        // Render buttons
        for (UIButton button : buttons) {
            boolean hover = button.contains(mouseX, mouseY);
            shapeRenderer.setColor(hover ? buttonHoverColor : buttonColor);
            shapeRenderer.rect(
                button.rect.x,
                button.rect.y,
                button.rect.width,
                button.rect.height
            );
        }

        // Render part buttons (with scrolling)
        for (UIPartButton button : partButtons) {
            float adjustedY = button.rect.y + partScrollOffset;
            if (
                adjustedY < partsPanelRect.y ||
                adjustedY + button.rect.height >
                partsPanelRect.y + partsPanelRect.height
            ) {
                continue; // Skip if outside visible area
            }

            boolean selected = button.partType == selectedPartType;
            boolean hover = new Rectangle(
                button.rect.x,
                adjustedY,
                button.rect.width,
                button.rect.height
            ).contains(mouseX, mouseY);

            shapeRenderer.setColor(
                selected
                    ? selectedColor
                    : (hover ? buttonHoverColor : buttonColor)
            );
            shapeRenderer.rect(
                button.rect.x,
                adjustedY,
                button.rect.width,
                button.rect.height
            );
        }

        shapeRenderer.end();

        // Render text
        spriteBatch.begin();

        // Title
        titleFont.draw(
            spriteBatch,
            "SHIP BUILDER",
            20,
            Gdx.graphics.getHeight() - 20
        );

        // Button labels
        for (UIButton button : buttons) {
            font.draw(
                spriteBatch,
                button.label,
                button.rect.x + 10,
                button.rect.y + button.rect.height / 2 + 5
            );
        }

        // Part button labels (with scrolling)
        for (UIPartButton button : partButtons) {
            float adjustedY = button.rect.y + partScrollOffset;
            if (
                adjustedY < partsPanelRect.y ||
                adjustedY + button.rect.height >
                partsPanelRect.y + partsPanelRect.height
            ) {
                continue;
            }
            font.draw(
                spriteBatch,
                button.partType.getDisplayName(),
                button.rect.x + 5,
                adjustedY + button.rect.height / 2 + 5
            );
        }

        // Stats
        float statsY = statsPanelRect.y + statsPanelRect.height - 30;
        font.draw(spriteBatch, "SHIP STATS", statsPanelRect.x + 10, statsY);
        statsY -= 25;
        font.draw(
            spriteBatch,
            "Parts: " + shipBuilder.getParts().size,
            statsPanelRect.x + 10,
            statsY
        );
        statsY -= 20;
        font.draw(
            spriteBatch,
            String.format("Mass: %.0f kg", shipBuilder.getTotalMass()),
            statsPanelRect.x + 10,
            statsY
        );
        statsY -= 20;
        font.draw(
            spriteBatch,
            String.format("Hull: %.0f HP", shipBuilder.getTotalHull()),
            statsPanelRect.x + 10,
            statsY
        );
        statsY -= 20;
        font.draw(
            spriteBatch,
            String.format("Shield: %.0f HP", shipBuilder.getTotalShield()),
            statsPanelRect.x + 10,
            statsY
        );
        statsY -= 20;
        font.draw(
            spriteBatch,
            String.format("Thrust: %.0f N", shipBuilder.getTotalThrust()),
            statsPanelRect.x + 10,
            statsY
        );
        statsY -= 20;
        font.draw(
            spriteBatch,
            String.format("Fuel: %.0f L", shipBuilder.getTotalFuel()),
            statsPanelRect.x + 10,
            statsY
        );
        statsY -= 20;
        font.draw(
            spriteBatch,
            String.format("T/W: %.2f", shipBuilder.getThrustToWeight()),
            statsPanelRect.x + 10,
            statsY
        );

        // Instructions
        font.draw(
            spriteBatch,
            "WASD+Space/Shift: Move Part | Left Click: Place | Right Click: Remove | Middle Mouse+Drag: Rotate View | Scroll: Zoom",
            10,
            80
        );

        // Show hint when middle mouse is pressed
        if (middleMousePressed) {
            font.setColor(Color.CYAN);
            font.draw(
                spriteBatch,
                "Drag to rotate camera view",
                mouseX + 40,
                Gdx.graphics.getHeight() - mouseY
            );
            font.setColor(Color.WHITE);
        }

        // Selected part info
        if (selectedPartType != null) {
            font.draw(
                spriteBatch,
                "Selected: " + selectedPartType.getDisplayName(),
                10,
                50
            );
            font.draw(
                spriteBatch,
                String.format(
                    "Position: %.1f, %.1f, %.1f",
                    ghostPartPosition.x,
                    ghostPartPosition.y,
                    ghostPartPosition.z
                ),
                10,
                30
            );
        }

        spriteBatch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();

        // Recreate UI layout
        initializeUI();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        // Clean up input state when hiding
        Gdx.app.log("ShipBuilderScreen", "Hiding ship builder");
    }

    @Override
    public void dispose() {
        if (spriteBatch != null) spriteBatch.dispose();
        if (font != null) font.dispose();
        if (titleFont != null) titleFont.dispose();
        if (modelBatch != null) modelBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (shipBuilder != null) shipBuilder.dispose();
    }

    public ShipBuilderState getBuilderState() {
        return builderState;
    }

    // Simple UI button class
    private static class UIButton {

        Rectangle rect;
        String label;
        Runnable action;

        UIButton(
            String label,
            float x,
            float y,
            float width,
            float height,
            Runnable action
        ) {
            this.label = label;
            this.rect = new Rectangle(x, y, width, height);
            this.action = action;
        }

        boolean contains(float x, float y) {
            return rect.contains(x, y);
        }
    }

    // UI button for ship parts
    private static class UIPartButton {

        Rectangle rect;
        ShipPartType partType;

        UIPartButton(
            ShipPartType partType,
            float x,
            float y,
            float width,
            float height
        ) {
            this.partType = partType;
            this.rect = new Rectangle(x, y, width, height);
        }
    }

    /**
     * Holds the ship builder state for persistence
     */
    public static class ShipBuilderState {

        public ShipConfiguration loadedConfiguration;

        public void saveConfiguration(ShipBuilder builder) {
            loadedConfiguration = new ShipConfiguration(builder);
        }
    }

    /**
     * Represents a saved ship configuration
     */
    public static class ShipConfiguration {

        public Array<PartData> parts;
        public Color primaryColor;
        public Color secondaryColor;
        public Color accentColor;

        public ShipConfiguration(ShipBuilder builder) {
            this.parts = new Array<>();
            for (ShipPart part : builder.getParts()) {
                parts.add(new PartData(part));
            }
            this.primaryColor = new Color(0.5f, 0.5f, 0.55f, 1f);
            this.secondaryColor = new Color(0.3f, 0.3f, 0.35f, 1f);
            this.accentColor = new Color(0.2f, 0.6f, 0.9f, 1f);
        }

        public static class PartData {

            public ShipPartType type;
            public Vector3 position;
            public float yaw, pitch, roll;
            public float scale;
            public boolean mirrored;

            public PartData(ShipPart part) {
                this.type = part.getType();
                this.position = new Vector3(part.getPosition());
                this.scale = part.getScale().x;
                this.mirrored = part.isMirrored();
                this.yaw = 0;
                this.pitch = 0;
                this.roll = 0;
            }
        }
    }

    private void applyConfiguration(ShipConfiguration config) {
        shipBuilder.clear();
        shipBuilder.setColors(
            config.primaryColor,
            config.secondaryColor,
            config.accentColor
        );

        for (ShipConfiguration.PartData partData : config.parts) {
            shipBuilder.addPart(
                partData.type,
                partData.position.x,
                partData.position.y,
                partData.position.z,
                partData.yaw,
                partData.pitch,
                partData.roll,
                partData.scale,
                partData.mirrored
            );
        }

        primaryColor.set(config.primaryColor);
        secondaryColor.set(config.secondaryColor);
        accentColor.set(config.accentColor);
    }
}
