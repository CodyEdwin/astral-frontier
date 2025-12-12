package com.astral.systems;

import com.astral.components.CameraComponent;
import com.astral.components.RenderComponent;
import com.astral.components.TransformComponent;
import com.astral.ecs.Entity;
import com.astral.ecs.GameSystem;
import com.astral.ecs.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.*;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Render system - handles all 3D and 2D rendering
 */
public class RenderSystem extends GameSystem implements Disposable {

    // 3D Rendering
    private ModelBatch modelBatch;
    private Environment environment;
    private DirectionalLight sunLight;

    // 2D Rendering
    private SpriteBatch spriteBatch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    // Skybox / Starfield
    private Mesh starfieldMesh;
    private ShaderProgram starfieldShader;
    private float[] starPositions;
    private float[] starColors;
    private float[] starMagnitudes;
    private static final int STAR_COUNT = 10000;

    // Debug rendering
    private boolean showDebugInfo = false;
    private boolean showPhysicsDebug = false;
    private boolean showWireframe = false;

    // Performance stats
    private int drawCalls = 0;
    private int visibleEntities = 0;

    // Camera reference
    private PerspectiveCamera activeCamera;

    public RenderSystem(World world) {
        super(world);
        setPriority(10); // Low priority - render last
    }

    @Override
    public void initialize() {
        Gdx.app.log("RenderSystem", "Initializing renderer...");

        // Initialize 3D rendering
        modelBatch = new ModelBatch();

        // Setup environment
        environment = new Environment();
        environment.set(
            new ColorAttribute(
                ColorAttribute.AmbientLight,
                0.2f,
                0.2f,
                0.2f,
                1f
            )
        );

        // Sun light
        sunLight = new DirectionalLight();
        sunLight.set(1f, 1f, 0.9f, -1f, -0.8f, -0.2f);
        environment.add(sunLight);

        // Initialize 2D rendering
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        // Generate starfield
        generateStarfield();

        Gdx.app.log("RenderSystem", "Renderer initialized");
    }

    private void generateStarfield() {
        starPositions = new float[STAR_COUNT * 3];
        starColors = new float[STAR_COUNT * 3];
        starMagnitudes = new float[STAR_COUNT];

        for (int i = 0; i < STAR_COUNT; i++) {
            // Random position on unit sphere, scaled out
            double theta = Math.random() * Math.PI * 2;
            double phi = Math.acos(2 * Math.random() - 1);
            float r = 50000f; // Far distance

            starPositions[i * 3] = (float) (r *
                Math.sin(phi) *
                Math.cos(theta));
            starPositions[i * 3 + 1] = (float) (r *
                Math.sin(phi) *
                Math.sin(theta));
            starPositions[i * 3 + 2] = (float) (r * Math.cos(phi));

            // Star color (white to blue to orange)
            float temp = (float) Math.random();
            if (temp < 0.1f) {
                // Blue stars
                starColors[i * 3] = 0.7f;
                starColors[i * 3 + 1] = 0.8f;
                starColors[i * 3 + 2] = 1f;
            } else if (temp < 0.2f) {
                // Orange/red stars
                starColors[i * 3] = 1f;
                starColors[i * 3 + 1] = 0.7f;
                starColors[i * 3 + 2] = 0.5f;
            } else {
                // White stars
                starColors[i * 3] = 1f;
                starColors[i * 3 + 1] = 1f;
                starColors[i * 3 + 2] = 1f;
            }

            // Magnitude (brightness)
            starMagnitudes[i] = (float) (Math.random() * 3 + 1);
        }
    }

    public void render(float deltaTime, float interpolation) {
        // Clear screen
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.02f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // Get active camera from player entity
        updateActiveCamera();

        if (activeCamera == null) return;

        // Render starfield
        renderStarfield();

        // Render 3D entities
        render3D(deltaTime);

        // Render debug info
        if (showDebugInfo) {
            renderDebugInfo();
        }
    }

    private void updateActiveCamera() {
        Array<Entity> cameraEntities = getEntitiesWith(CameraComponent.class);
        for (Entity entity : cameraEntities) {
            CameraComponent cam = entity.get(CameraComponent.class);
            if (cam.camera != null) {
                activeCamera = cam.camera;
                break;
            }
        }
    }

    private void renderStarfield() {
        if (activeCamera == null) return;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

        // Simple point-based starfield rendering
        shapeRenderer.setProjectionMatrix(activeCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Point);

        Vector3 camPos = activeCamera.position;

        for (int i = 0; i < STAR_COUNT; i++) {
            float x = starPositions[i * 3] + camPos.x;
            float y = starPositions[i * 3 + 1] + camPos.y;
            float z = starPositions[i * 3 + 2] + camPos.z;

            float r = starColors[i * 3];
            float g = starColors[i * 3 + 1];
            float b = starColors[i * 3 + 2];
            float brightness = starMagnitudes[i] / 3f;

            shapeRenderer.setColor(
                r * brightness,
                g * brightness,
                b * brightness,
                brightness
            );
            shapeRenderer.point(x, y, z);
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void render3D(float deltaTime) {
        if (activeCamera == null) return;

        drawCalls = 0;
        visibleEntities = 0;

        modelBatch.begin(activeCamera);

        Array<Entity> renderables = getEntitiesWith(
            RenderComponent.class,
            TransformComponent.class
        );
        for (Entity entity : renderables) {
            RenderComponent render = entity.get(RenderComponent.class);
            TransformComponent transform = entity.get(TransformComponent.class);

            if (!render.visible) continue;

            // Frustum culling
            float distance = transform.position.dst(activeCamera.position);
            if (distance > render.renderDistance) continue;

            // Update model transform
            transform.updateMatrix();

            // Render primary model instance
            if (render.modelInstance != null) {
                render.modelInstance.transform.set(transform.worldMatrix);
                modelBatch.render(render.modelInstance, environment);
                drawCalls++;
            }

            // Render additional instances (for multi-part ships)
            if (render.additionalInstances != null) {
                for (ModelInstance instance : render.additionalInstances) {
                    instance.transform.set(transform.worldMatrix);
                    modelBatch.render(instance, environment);
                    drawCalls++;
                }
            }

            visibleEntities++;
        }

        modelBatch.end();
    }

    private void renderDebugInfo() {
        spriteBatch.begin();

        int y = Gdx.graphics.getHeight() - 20;
        font.draw(
            spriteBatch,
            "FPS: " + Gdx.graphics.getFramesPerSecond(),
            10,
            y
        );
        font.draw(spriteBatch, "Draw Calls: " + drawCalls, 10, y - 20);
        font.draw(
            spriteBatch,
            "Visible Entities: " + visibleEntities,
            10,
            y - 40
        );
        font.draw(
            spriteBatch,
            "Total Entities: " + world.getEntityCount(),
            10,
            y - 60
        );

        if (activeCamera != null) {
            font.draw(
                spriteBatch,
                String.format(
                    "Camera: %.0f, %.0f, %.0f",
                    activeCamera.position.x,
                    activeCamera.position.y,
                    activeCamera.position.z
                ),
                10,
                y - 80
            );
        }

        spriteBatch.end();
    }

    public void resize(int width, int height) {
        if (activeCamera != null) {
            activeCamera.viewportWidth = width;
            activeCamera.viewportHeight = height;
            activeCamera.update();
        }
    }

    public void setShowDebugInfo(boolean show) {
        this.showDebugInfo = show;
    }

    public void toggleDebugInfo() {
        this.showDebugInfo = !this.showDebugInfo;
    }

    public PerspectiveCamera getActiveCamera() {
        return activeCamera;
    }

    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public void update(float deltaTime) {
        // Rendering handled by render() called from game loop
    }

    @Override
    public void dispose() {
        if (modelBatch != null) modelBatch.dispose();
        if (spriteBatch != null) spriteBatch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
        if (starfieldShader != null) starfieldShader.dispose();
    }
}
