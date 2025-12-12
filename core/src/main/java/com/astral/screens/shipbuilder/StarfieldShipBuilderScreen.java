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
 * Simple, functional ship builder
 */
public class StarfieldShipBuilderScreen implements Screen {

    private final AstralFrontier game;
    private final Screen returnScreen;

    // Core
    private ShipBuilder shipBuilder;
    private ShipPartCatalog catalog;

    // 3D
    private PerspectiveCamera camera;
    private ModelBatch modelBatch;
    private Environment environment;
    private ShapeRenderer shapeRenderer;

    // 2D
    private SpriteBatch batch;
    private BitmapFont font;

    // State
    private ShipPartType.PartCategory selectedCategory =
        ShipPartType.PartCategory.HULL;
    private Array<ShipPartCatalog.PartCatalogEntry> availableParts;
    private int selectedPartIndex = -1;
    private ShipPartType selectedPartType = null;

    // Camera
    private float cameraDistance = 30f;
    private float cameraYaw = 45f;
    private float cameraPitch = 20f;
    private Vector3 cameraTarget = new Vector3(0, 0, 0);

    // Ghost part
    private Vector3 ghostPosition = new Vector3(0, 0, -5);
    private boolean placingPart = false;

    // Input
    private int mouseX, mouseY;
    private int lastMouseX, lastMouseY;
    private boolean rightMouseDown = false;

    // Credits
    private int credits = 100000;

    public StarfieldShipBuilderScreen(
        AstralFrontier game,
        Screen returnScreen
    ) {
        this.game = game;
        this.returnScreen = returnScreen;
    }

    @Override
    public void show() {
        // Initialize 3D
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

        // Initialize 2D
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        // Initialize ship builder
        catalog = ShipPartCatalog.getInstance();
        shipBuilder = new ShipBuilder();
        shipBuilder.buildFighter(); // Start with a fighter

        // Load parts for default category
        updateAvailableParts();

        // Setup input
        Gdx.input.setInputProcessor(
            new InputAdapter() {
                @Override
                public boolean keyDown(int keycode) {
                    handleKey(keycode);
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
                    if (button == Input.Buttons.RIGHT) rightMouseDown = false;
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

                    if (rightMouseDown) {
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
                    if (mouseX < 300) {
                        // Scroll part list
                        selectedPartIndex = (int) MathUtils.clamp(
                            selectedPartIndex + amountY,
                            -1,
                            availableParts.size - 1
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

        Gdx.input.setCursorCatched(false);
    }

    private void handleKey(int keycode) {
        switch (keycode) {
            case Input.Keys.ESCAPE:
                game.setScreen(returnScreen);
                break;
            case Input.Keys.NUM_1:
                selectCategory(ShipPartType.PartCategory.HULL);
                break;
            case Input.Keys.NUM_2:
                selectCategory(ShipPartType.PartCategory.WING);
                break;
            case Input.Keys.NUM_3:
                selectCategory(ShipPartType.PartCategory.ENGINE);
                break;
            case Input.Keys.NUM_4:
                selectCategory(ShipPartType.PartCategory.WEAPON);
                break;
            case Input.Keys.NUM_5:
                selectCategory(ShipPartType.PartCategory.UTILITY);
                break;
            case Input.Keys.NUM_6:
                selectCategory(ShipPartType.PartCategory.STRUCTURAL);
                break;
            case Input.Keys.UP:
                selectedPartIndex = Math.max(-1, selectedPartIndex - 1);
                break;
            case Input.Keys.DOWN:
                selectedPartIndex = Math.min(
                    availableParts.size - 1,
                    selectedPartIndex + 1
                );
                break;
            case Input.Keys.ENTER:
                if (selectedPartIndex >= 0) {
                    startPlacingPart();
                }
                break;
            case Input.Keys.C:
                shipBuilder.clear();
                break;
        }
    }

    private void handleLeftClick() {
        // Check if clicking in parts list
        if (mouseX < 300 && mouseY > 100) {
            int index = (int) ((Gdx.graphics.getHeight() - mouseY - 100) / 60);
            if (index >= 0 && index < availableParts.size) {
                selectedPartIndex = index;
                startPlacingPart();
            }
        } else if (placingPart && selectedPartType != null) {
            // Place the part
            placePart();
        }
    }

    private void selectCategory(ShipPartType.PartCategory category) {
        selectedCategory = category;
        selectedPartIndex = -1;
        updateAvailableParts();
    }

    private void updateAvailableParts() {
        availableParts = catalog.getPartsByCategory(selectedCategory);
    }

    private void startPlacingPart() {
        if (selectedPartIndex >= 0 && selectedPartIndex < availableParts.size) {
            ShipPartCatalog.PartCatalogEntry entry = availableParts.get(
                selectedPartIndex
            );
            if (entry.cost <= credits) {
                selectedPartType = entry.type;
                placingPart = true;
            }
        }
    }

    private void placePart() {
        if (selectedPartType == null) return;

        ShipPartCatalog.PartCatalogEntry entry = catalog.getEntry(
            selectedPartType
        );
        if (entry == null || entry.cost > credits) return;

        // Place at ghost position
        shipBuilder.addPart(
            selectedPartType,
            ghostPosition.x,
            ghostPosition.y,
            ghostPosition.z
        );
        credits -= entry.cost;

        placingPart = false;
        selectedPartType = null;
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
        if (!placingPart) return;

        // Simple ray to ground plane
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
            ghostPosition.x = Math.round(ghostPosition.x * 2) / 2f;
            ghostPosition.z = Math.round(ghostPosition.z * 2) / 2f;
        }
    }

    @Override
    public void render(float delta) {
        updateGhostPosition();

        // Clear
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // 3D
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);

        modelBatch.begin(camera);
        for (ModelInstance instance : shipBuilder.getModelInstances()) {
            modelBatch.render(instance, environment);
        }
        modelBatch.end();

        // Grid
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 0.5f);
        for (int i = -10; i <= 10; i++) {
            shapeRenderer.line(i * 2, 0, -20, i * 2, 0, 20);
            shapeRenderer.line(-20, 0, i * 2, 20, 0, i * 2);
        }
        shapeRenderer.end();

        // Ghost part
        if (placingPart) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.2f, 0.8f, 0.3f, 0.3f);
            drawBox(ghostPosition, 1f, 1f, 2f);
            shapeRenderer.end();
        }

        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);

        // 2D UI
        batch.begin();

        // Left panel background
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.1f, 0.15f, 0.9f);
        shapeRenderer.rect(0, 0, 300, Gdx.graphics.getHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Title
        font.draw(batch, "SHIP BUILDER", 10, Gdx.graphics.getHeight() - 10);

        // Category
        font.draw(
            batch,
            "Category: " + selectedCategory,
            10,
            Gdx.graphics.getHeight() - 40
        );
        font.draw(
            batch,
            "[1-6] Change Category",
            10,
            Gdx.graphics.getHeight() - 60
        );

        // Parts list
        float y = Gdx.graphics.getHeight() - 100;
        for (int i = 0; i < availableParts.size; i++) {
            ShipPartCatalog.PartCatalogEntry entry = availableParts.get(i);
            Color color = (i == selectedPartIndex) ? Color.YELLOW : Color.WHITE;
            font.setColor(color);
            font.draw(batch, entry.displayName, 10, y);
            font.setColor(entry.cost <= credits ? Color.GREEN : Color.RED);
            font.draw(batch, entry.cost + " cr", 10, y - 15);
            y -= 60;
        }
        font.setColor(Color.WHITE);

        // Instructions
        font.draw(batch, "Credits: " + credits, 10, 80);
        font.draw(batch, "[UP/DOWN] Select part", 10, 60);
        font.draw(batch, "[ENTER/CLICK] Place part", 10, 40);
        font.draw(batch, "[C] Clear ship", 10, 20);

        // Right panel - stats
        float rx = Gdx.graphics.getWidth() - 250;
        font.draw(batch, "SHIP STATS", rx, Gdx.graphics.getHeight() - 10);
        font.draw(
            batch,
            "Mass: " + (int) shipBuilder.getTotalMass() + " kg",
            rx,
            Gdx.graphics.getHeight() - 40
        );
        font.draw(
            batch,
            "Hull: " + (int) shipBuilder.getTotalHull(),
            rx,
            Gdx.graphics.getHeight() - 60
        );
        font.draw(
            batch,
            "Thrust: " + (int) shipBuilder.getTotalThrust() + " N",
            rx,
            Gdx.graphics.getHeight() - 80
        );
        font.draw(
            batch,
            "T/W: " + String.format("%.2f", shipBuilder.getThrustToWeight()),
            rx,
            Gdx.graphics.getHeight() - 100
        );

        // Validation
        String status = shipBuilder.getValidationSummary();
        Color statusColor = shipBuilder.isValid() ? Color.GREEN : Color.RED;
        font.setColor(statusColor);
        font.draw(batch, status, rx, Gdx.graphics.getHeight() - 130);
        font.setColor(Color.WHITE);

        // Controls
        font.draw(batch, "[ESC] Exit", rx, 40);
        font.draw(batch, "[Right Drag] Rotate", rx, 20);

        batch.end();
    }

    private void drawBox(Vector3 center, float w, float h, float d) {
        float hw = w / 2,
            hh = h / 2,
            hd = d / 2;
        // Just draw outline
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
            center.y + hh,
            center.z - hd
        );
        shapeRenderer.line(
            center.x + hw,
            center.y + hh,
            center.z - hd,
            center.x - hw,
            center.y + hh,
            center.z - hd
        );
        shapeRenderer.line(
            center.x - hw,
            center.y + hh,
            center.z - hd,
            center.x - hw,
            center.y - hh,
            center.z - hd
        );
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
    }
}
