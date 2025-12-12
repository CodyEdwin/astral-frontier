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
 * Starfield-Style Ship Builder
 * Exact workflow: Browse parts -> Select -> Place in 3D -> Validate
 */
public class StarfieldStyleBuilder implements Screen {

    private final AstralFrontier game;
    private final Screen returnScreen;

    // Core systems
    private ShipBuilder shipBuilder;
    private ShipPartCatalog catalog;

    // Rendering
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    // State machine
    private enum BuilderState {
        BROWSING, // Looking at parts list
        PLACING, // Selected a part, moving it in 3D
        ROTATING, // Rotating the part before placing
    }

    private BuilderState state = BuilderState.BROWSING;

    // Part categories (like Starfield)
    private enum Category {
        COCKPIT("Cockpit", ShipPartType.PartCategory.HULL),
        HULL("Hull", ShipPartType.PartCategory.HULL),
        ENGINES("Engines", ShipPartType.PartCategory.ENGINE),
        WINGS("Wings", ShipPartType.PartCategory.WING),
        WEAPONS("Weapons", ShipPartType.PartCategory.WEAPON),
        SYSTEMS("Systems", ShipPartType.PartCategory.UTILITY);

        final String displayName;
        final ShipPartType.PartCategory partCategory;

        Category(String displayName, ShipPartType.PartCategory partCategory) {
            this.displayName = displayName;
            this.partCategory = partCategory;
        }
    }

    private Category selectedCategory = Category.COCKPIT;

    // Parts list
    private Array<ShipPartCatalog.PartCatalogEntry> availableParts;
    private int selectedPartIndex = 0;
    private int partListScroll = 0;

    // Placement
    private ShipPartType partToPlace = null;
    private Vector3 ghostPosition = new Vector3();
    private float ghostRotation = 0f;
    private ModelInstance ghostModel = null;

    // Camera
    private float cameraDistance = 30f;
    private float cameraYaw = 45f;
    private float cameraPitch = 20f;
    private Vector3 cameraTarget = new Vector3(0, 0, 0);

    // Input
    private int mouseX, mouseY;
    private boolean rightMouseDown = false;
    private int lastMouseX, lastMouseY;

    // UI Colors
    private final Color uiBackground = new Color(0.1f, 0.1f, 0.15f, 0.85f);
    private final Color uiBorder = new Color(0.3f, 0.4f, 0.5f, 0.8f);
    private final Color selectedColor = new Color(0.4f, 0.6f, 0.9f, 1f);
    private final Color validColor = new Color(0.3f, 0.9f, 0.4f, 1f);
    private final Color invalidColor = new Color(0.9f, 0.3f, 0.3f, 1f);

    // Credits
    private int credits = 100000;

    public StarfieldStyleBuilder(AstralFrontier game, Screen returnScreen) {
        this.game = game;
        this.returnScreen = returnScreen;
    }

    @Override
    public void show() {
        // Initialize rendering
        camera = new PerspectiveCamera(
            67,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );
        camera.near = 0.1f;
        camera.far = 1000f;
        updateCamera();

        modelBatch = new ModelBatch();
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        // Setup lighting
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
        environment.add(
            new DirectionalLight().set(1f, 1f, 1f, -1f, -0.8f, -0.2f)
        );
        environment.add(
            new DirectionalLight().set(0.5f, 0.5f, 0.6f, 1f, 0.5f, 0.5f)
        );

        // Initialize ship builder
        catalog = ShipPartCatalog.getInstance();
        shipBuilder = new ShipBuilder();
        shipBuilder.buildFighter(); // Start with fighter

        // Load parts for first category
        updatePartsList();

        // Setup input
        setupInput();
        Gdx.input.setCursorCatched(false);

        Gdx.app.log("StarfieldBuilder", "Ready! Press H for help");
    }

    private void setupInput() {
        Gdx.input.setInputProcessor(
            new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    handleKeyPress(keycode);
                    return true;
                }

                @Override
                public boolean touchDown(
                    int screenX,
                    int screenY,
                    int pointer,
                    int button
                ) {
                    mouseX = screenX;
                    mouseY = Gdx.graphics.getHeight() - screenY;
                    lastMouseX = screenX;
                    lastMouseY = screenY;

                    if (button == Input.Buttons.LEFT) {
                        handleLeftClick();
                    } else if (button == Input.Buttons.RIGHT) {
                        rightMouseDown = true;
                        if (state == BuilderState.PLACING) {
                            cancelPlacement();
                        }
                    }
                    return true;
                }

                @Override
                public boolean touchUp(
                    int screenX,
                    int screenY,
                    int pointer,
                    int button
                ) {
                    if (button == Input.Buttons.RIGHT) {
                        rightMouseDown = false;
                    }
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

                    if (rightMouseDown && state == BuilderState.BROWSING) {
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
                    if (mouseX < 250) {
                        // Scroll parts list
                        partListScroll += (int) amountY;
                        partListScroll = MathUtils.clamp(
                            partListScroll,
                            0,
                            Math.max(0, availableParts.size - 8)
                        );
                    } else {
                        // Zoom camera
                        cameraDistance += amountY * 2f;
                        cameraDistance = MathUtils.clamp(
                            cameraDistance,
                            10f,
                            100f
                        );
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
            }
        );
    }

    private void handleKeyPress(int keycode) {
        switch (keycode) {
            case Input.Keys.ESCAPE:
                if (state == BuilderState.PLACING) {
                    cancelPlacement();
                } else {
                    game.setScreen(returnScreen);
                }
                break;
            case Input.Keys.TAB:
                cycleCategory();
                break;
            case Input.Keys.R:
                if (state == BuilderState.PLACING) {
                    ghostRotation += 45f;
                }
                break;
            case Input.Keys.UP:
                selectedPartIndex = Math.max(0, selectedPartIndex - 1);
                break;
            case Input.Keys.DOWN:
                selectedPartIndex = Math.min(
                    availableParts.size - 1,
                    selectedPartIndex + 1
                );
                break;
            case Input.Keys.ENTER:
            case Input.Keys.SPACE:
                if (state == BuilderState.BROWSING && selectedPartIndex >= 0) {
                    startPlacingPart();
                } else if (state == BuilderState.PLACING) {
                    confirmPlacement();
                }
                break;
            case Input.Keys.C:
                shipBuilder.clear();
                shipBuilder.buildFighter(); // Reset to fighter
                break;
            case Input.Keys.NUM_1:
                selectCategory(Category.COCKPIT);
                break;
            case Input.Keys.NUM_2:
                selectCategory(Category.HULL);
                break;
            case Input.Keys.NUM_3:
                selectCategory(Category.ENGINES);
                break;
            case Input.Keys.NUM_4:
                selectCategory(Category.WINGS);
                break;
            case Input.Keys.NUM_5:
                selectCategory(Category.WEAPONS);
                break;
            case Input.Keys.NUM_6:
                selectCategory(Category.SYSTEMS);
                break;
        }
    }

    private void handleLeftClick() {
        if (
            mouseX < 250 &&
            mouseY > 100 &&
            mouseY < Gdx.graphics.getHeight() - 60
        ) {
            // Clicking in parts list - calculate which card was clicked
            float y = Gdx.graphics.getHeight() - 110;
            float cardHeight = 90;
            int maxVisible = (int) ((Gdx.graphics.getHeight() - 200) /
                cardHeight);
            int displayCount = Math.min(
                maxVisible,
                availableParts.size - partListScroll
            );

            for (int i = 0; i < displayCount; i++) {
                int index = i + partListScroll;
                if (index >= availableParts.size) break;

                float cardY = y - cardHeight + 10;

                if (mouseY >= cardY && mouseY <= cardY + cardHeight) {
                    selectedPartIndex = index;
                    startPlacingPart();
                    return;
                }

                y -= cardHeight + 5;
            }
        } else if (mouseX < 300 && mouseY > Gdx.graphics.getHeight() - 60) {
            // Clicking category tabs
            int tabWidth = 50;
            int clickedTab = mouseX / tabWidth;
            if (clickedTab < Category.values().length) {
                selectCategory(Category.values()[clickedTab]);
            }
        } else if (state == BuilderState.PLACING) {
            // Placing part in 3D
            confirmPlacement();
        }
    }

    private void cycleCategory() {
        int nextIndex =
            (selectedCategory.ordinal() + 1) % Category.values().length;
        selectCategory(Category.values()[nextIndex]);
    }

    private void selectCategory(Category category) {
        selectedCategory = category;
        selectedPartIndex = 0;
        partListScroll = 0;
        updatePartsList();
    }

    private void updatePartsList() {
        availableParts = catalog.getPartsByCategory(
            selectedCategory.partCategory
        );
        if (availableParts.size == 0) {
            availableParts = new Array<>();
        }
    }

    private void startPlacingPart() {
        if (
            selectedPartIndex < 0 || selectedPartIndex >= availableParts.size
        ) return;

        ShipPartCatalog.PartCatalogEntry entry = availableParts.get(
            selectedPartIndex
        );
        if (entry.cost > credits) {
            Gdx.app.log("Builder", "Not enough credits!");
            return;
        }

        partToPlace = entry.type;
        state = BuilderState.PLACING;
        ghostRotation = 0f;
        ghostPosition.set(0, 0, -5);

        // Create ghost model
        ShipPartMeshFactory factory = new ShipPartMeshFactory();
        ghostModel = new ModelInstance(
            factory.createPartModel(
                partToPlace,
                new Color(0.3f, 0.8f, 0.4f, 0.6f),
                new Color(0.2f, 0.6f, 0.3f, 0.6f),
                0
            )
        );

        Gdx.app.log(
            "Builder",
            "Placing " +
                entry.displayName +
                " - Press R to rotate, Click to place, Right-click to cancel"
        );
    }

    private void confirmPlacement() {
        if (partToPlace == null) return;

        ShipPartCatalog.PartCatalogEntry entry = catalog.getEntry(partToPlace);
        if (entry == null || entry.cost > credits) return;

        // Place the part
        shipBuilder.addPart(
            partToPlace,
            ghostPosition.x,
            ghostPosition.y,
            ghostPosition.z,
            ghostRotation,
            0,
            0,
            1f,
            false
        );
        credits -= entry.cost;

        Gdx.app.log(
            "Builder",
            "Placed " + entry.displayName + " at " + ghostPosition
        );

        // Stay in placing mode so user can place multiple
        ghostPosition.add(0, 0, -2); // Offset for next placement
    }

    private void cancelPlacement() {
        state = BuilderState.BROWSING;
        partToPlace = null;
        ghostModel = null;
        Gdx.app.log("Builder", "Cancelled placement");
    }

    private void updateCamera() {
        float rad = cameraYaw * MathUtils.degreesToRadians;
        float pitch = cameraPitch * MathUtils.degreesToRadians;
        float h = cameraDistance * MathUtils.cos(pitch);
        float v = cameraDistance * MathUtils.sin(pitch);

        camera.position.set(
            cameraTarget.x + h * MathUtils.sin(rad),
            cameraTarget.y + v,
            cameraTarget.z + h * MathUtils.cos(rad)
        );
        camera.lookAt(cameraTarget);
        camera.up.set(0, 1, 0);
        camera.update();
    }

    private void updateGhostPosition() {
        if (state != BuilderState.PLACING) return;

        // Ray cast from mouse to ground plane
        Vector3 ray = camera.getPickRay(
            mouseX,
            Gdx.graphics.getHeight() - mouseY
        ).direction;
        float t = -camera.position.y / ray.y;
        if (t > 0) {
            ghostPosition.set(
                camera.position.x + ray.x * t,
                0,
                camera.position.z + ray.z * t
            );
            // Snap to grid
            ghostPosition.x = Math.round(ghostPosition.x);
            ghostPosition.z = Math.round(ghostPosition.z);
        }

        // Update ghost model transform
        if (ghostModel != null) {
            ghostModel.transform.idt();
            ghostModel.transform.translate(ghostPosition);
            ghostModel.transform.rotate(Vector3.Y, ghostRotation);
        }
    }

    @Override
    public void render(float delta) {
        updateGhostPosition();

        // Clear
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Render 3D
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        // Render grid
        renderGrid();

        // Render ship models
        modelBatch.begin(camera);
        for (ModelInstance instance : shipBuilder.getModelInstances()) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();

        // Render ghost part
        if (state == BuilderState.PLACING) {
            renderGhostPart();
        }

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        // Render UI on top
        renderUI();
    }

    private void renderGrid() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 0.5f);
        for (int i = -20; i <= 20; i++) {
            shapeRenderer.line(i, 0, -20, i, 0, 20);
            shapeRenderer.line(-20, 0, i, 20, 0, i);
        }
        shapeRenderer.end();
    }

    private void renderGhostPart() {
        if (ghostModel == null) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Render the actual part model with transparency
        modelBatch.begin(camera);
        modelBatch.render(ghostModel, environment);
        modelBatch.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void drawBox(Vector3 center, float w, float h, float d) {
        float hw = w / 2,
            hh = h / 2,
            hd = d / 2;
        // Draw box outline with lines
        // Bottom face
        shapeRenderer.line(
            center.x - hw,
            center.y - hh,
            center.z - hd,
            center.x + hw,
            center.y - hh,
            center.z - hd
        );
        shapeRenderer.line(
            center.x + hw,
            center.y - hh,
            center.z - hd,
            center.x + hw,
            center.y - hh,
            center.z + hd
        );
        shapeRenderer.line(
            center.x + hw,
            center.y - hh,
            center.z + hd,
            center.x - hw,
            center.y - hh,
            center.z + hd
        );
        shapeRenderer.line(
            center.x - hw,
            center.y - hh,
            center.z + hd,
            center.x - hw,
            center.y - hh,
            center.z - hd
        );
        // Top face
        shapeRenderer.line(
            center.x - hw,
            center.y + hh,
            center.z - hd,
            center.x + hw,
            center.y + hh,
            center.z - hd
        );
        shapeRenderer.line(
            center.x + hw,
            center.y + hh,
            center.z - hd,
            center.x + hw,
            center.y + hh,
            center.z + hd
        );
        shapeRenderer.line(
            center.x + hw,
            center.y + hh,
            center.z + hd,
            center.x - hw,
            center.y + hh,
            center.z + hd
        );
        shapeRenderer.line(
            center.x - hw,
            center.y + hh,
            center.z + hd,
            center.x - hw,
            center.y + hh,
            center.z - hd
        );
        // Vertical edges
        shapeRenderer.line(
            center.x - hw,
            center.y - hh,
            center.z - hd,
            center.x - hw,
            center.y + hh,
            center.z - hd
        );
        shapeRenderer.line(
            center.x + hw,
            center.y - hh,
            center.z - hd,
            center.x + hw,
            center.y + hh,
            center.z - hd
        );
        shapeRenderer.line(
            center.x + hw,
            center.y - hh,
            center.z + hd,
            center.x + hw,
            center.y + hh,
            center.z + hd
        );
        shapeRenderer.line(
            center.x - hw,
            center.y - hh,
            center.z + hd,
            center.x - hw,
            center.y + hh,
            center.z + hd
        );
    }

    private void renderUI() {
        // Disable depth test for UI
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Set up orthographic projection for 2D UI
        batch
            .getProjectionMatrix()
            .setToOrtho2D(
                0,
                0,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight()
            );
        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());

        // Category tabs
        renderCategoryTabs();

        // Left panel background - narrower
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(uiBackground);
        shapeRenderer.rect(0, 0, 250, Gdx.graphics.getHeight() - 60);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(uiBorder);
        shapeRenderer.rect(0, 0, 250, Gdx.graphics.getHeight() - 60);
        shapeRenderer.end();

        // Right panel background - narrower
        float rightX = Gdx.graphics.getWidth() - 250;
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(uiBackground);
        shapeRenderer.rect(rightX, 0, 250, Gdx.graphics.getHeight());
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(uiBorder);
        shapeRenderer.rect(rightX, 0, 250, Gdx.graphics.getHeight());
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Text
        batch.begin();

        // Left panel - parts list
        renderPartsList();

        // Right panel - stats and validation
        renderStatsPanel();

        // Bottom bar - instructions
        renderInstructions();

        batch.end();
    }

    private void renderCategoryTabs() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        int tabWidth = 50;
        for (int i = 0; i < Category.values().length; i++) {
            Category cat = Category.values()[i];
            if (cat == selectedCategory) {
                shapeRenderer.setColor(selectedColor);
            } else {
                shapeRenderer.setColor(uiBackground);
            }
            shapeRenderer.rect(
                i * tabWidth,
                Gdx.graphics.getHeight() - 60,
                tabWidth,
                60
            );
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(uiBorder);
        for (int i = 0; i < Category.values().length; i++) {
            shapeRenderer.rect(
                i * tabWidth,
                Gdx.graphics.getHeight() - 60,
                tabWidth,
                60
            );
        }
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Tab labels
        batch.begin();
        for (int i = 0; i < Category.values().length; i++) {
            Category cat = Category.values()[i];
            font.draw(
                batch,
                "" + (i + 1),
                i * tabWidth + 20,
                Gdx.graphics.getHeight() - 20
            );
        }
        batch.end();
    }

    private void renderPartsList() {
        // Category title
        font.setColor(Color.YELLOW);
        font.draw(
            batch,
            selectedCategory.displayName.toUpperCase(),
            10,
            Gdx.graphics.getHeight() - 70
        );
        font.setColor(Color.WHITE);

        // End batch to draw selection boxes
        batch.end();

        // Calculate visible parts
        float y = Gdx.graphics.getHeight() - 110;
        float cardHeight = 90;
        int maxVisible = (int) ((Gdx.graphics.getHeight() - 200) / cardHeight);
        int displayCount = Math.min(
            maxVisible,
            availableParts.size - partListScroll
        );

        // Draw part cards with backgrounds
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < displayCount; i++) {
            int index = i + partListScroll;
            if (index >= availableParts.size) break;

            ShipPartCatalog.PartCatalogEntry entry = availableParts.get(index);

            // Calculate card bounds
            float cardX = 5;
            float cardY = y - cardHeight + 10;
            float cardW = 240;

            // Check if mouse is over this card
            boolean isHovered =
                mouseX >= cardX &&
                mouseX <= cardX + cardW &&
                mouseY >= cardY &&
                mouseY <= cardY + cardHeight;

            // Draw card background
            if (index == selectedPartIndex) {
                shapeRenderer.setColor(0.3f, 0.5f, 0.7f, 0.8f);
            } else if (isHovered) {
                shapeRenderer.setColor(0.2f, 0.3f, 0.4f, 0.8f);
            } else {
                shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 0.6f);
            }
            shapeRenderer.rect(cardX, cardY, cardW, cardHeight);

            y -= cardHeight + 5;
        }

        shapeRenderer.end();

        // Draw card borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        y = Gdx.graphics.getHeight() - 110;

        for (int i = 0; i < displayCount; i++) {
            int index = i + partListScroll;
            if (index >= availableParts.size) break;

            float cardX = 5;
            float cardY = y - cardHeight + 10;
            float cardW = 240;

            if (index == selectedPartIndex) {
                shapeRenderer.setColor(0.4f, 0.7f, 1.0f, 1.0f);
            } else {
                shapeRenderer.setColor(0.3f, 0.4f, 0.5f, 0.6f);
            }
            shapeRenderer.rect(cardX, cardY, cardW, cardHeight);

            y -= cardHeight + 5;
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Resume batch for text
        batch.begin();

        // Draw part info on cards
        y = Gdx.graphics.getHeight() - 110;
        for (int i = 0; i < displayCount; i++) {
            int index = i + partListScroll;
            if (index >= availableParts.size) break;

            ShipPartCatalog.PartCatalogEntry entry = availableParts.get(index);

            float textX = 15;
            float textY = y - 10;

            // Part name
            font.setColor(entry.cost <= credits ? Color.WHITE : Color.RED);
            font.draw(batch, entry.displayName, textX, textY);
            textY -= 20;

            // Description (truncated)
            font.setColor(Color.LIGHT_GRAY);
            String desc = entry.description;
            if (desc.length() > 35) {
                desc = desc.substring(0, 32) + "...";
            }
            font.draw(batch, desc, textX, textY);
            textY -= 20;

            // Cost
            font.setColor(entry.cost <= credits ? Color.GREEN : Color.RED);
            font.draw(batch, entry.cost + " cr", textX, textY);

            // Stats preview
            font.setColor(Color.CYAN);
            textY -= 20;
            String stats = "";
            if (entry.stats.mass > 0) stats +=
                "M:" + (int) entry.stats.mass + " ";
            if (entry.stats.thrust > 0) stats +=
                "T:" + (int) entry.stats.thrust + " ";
            if (entry.stats.hull > 0) stats +=
                "H:" + (int) entry.stats.hull + " ";
            font.draw(batch, stats, textX, textY);

            y -= cardHeight + 5;
        }

        font.setColor(Color.WHITE);

        // Scroll indicator
        if (availableParts.size > maxVisible) {
            font.setColor(Color.GRAY);
            int showing = Math.min(
                partListScroll + maxVisible,
                availableParts.size
            );
            font.draw(
                batch,
                (partListScroll + 1) +
                    "-" +
                    showing +
                    " of " +
                    availableParts.size,
                10,
                25
            );
            font.setColor(Color.WHITE);
        }
    }

    private void renderStatsPanel() {
        float x = Gdx.graphics.getWidth() - 240;
        float y = Gdx.graphics.getHeight() - 20;

        font.setColor(Color.CYAN);
        font.draw(batch, "SHIP STATUS", x, y);
        y -= 30;

        font.setColor(Color.WHITE);
        font.draw(
            batch,
            "Mass: " + (int) shipBuilder.getTotalMass() + " kg",
            x,
            y
        );
        y -= 20;
        font.draw(batch, "Hull: " + (int) shipBuilder.getTotalHull(), x, y);
        y -= 20;
        font.draw(
            batch,
            "Thrust: " + (int) shipBuilder.getTotalThrust() + " N",
            x,
            y
        );
        y -= 20;
        font.draw(
            batch,
            "T/W: " + String.format("%.2f", shipBuilder.getThrustToWeight()),
            x,
            y
        );
        y -= 40;

        // Validation
        boolean valid = shipBuilder.validate();
        font.setColor(valid ? validColor : invalidColor);
        font.draw(batch, valid ? "FLIGHT READY" : "CANNOT FLY", x, y);
        y -= 20;

        font.setColor(Color.WHITE);
        if (!valid) {
            for (ShipValidator.ValidationError error : shipBuilder.getErrors()) {
                font.setColor(invalidColor);
                font.draw(batch, "X " + error.type.title, x, y);
                y -= 20;
            }
        }

        // Credits
        font.setColor(Color.GREEN);
        y = 40;
        font.draw(batch, "Credits: " + credits, x, y);
    }

    private void renderInstructions() {
        font.setColor(Color.GRAY);
        String instructions = "";
        switch (state) {
            case BROWSING:
                instructions =
                    "[1-6] Category | [UP/DOWN] Select | [ENTER/CLICK] Place | [C] Clear | [ESC] Exit";
                break;
            case PLACING:
                instructions =
                    "[R] Rotate | [CLICK] Confirm | [RIGHT CLICK] Cancel | [SPACE] Place";
                break;
        }
        font.draw(batch, instructions, 10, 20);
        font.setColor(Color.WHITE);
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
        shipBuilder.dispose();
        if (ghostModel != null && ghostModel.model != null) {
            ghostModel.model.dispose();
        }
    }
}
