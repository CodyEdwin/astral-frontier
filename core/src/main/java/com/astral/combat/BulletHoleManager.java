package com.astral.combat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import java.util.Random;

/**
 * Manages bullet hole decals on terrain and surfaces
 */
public class BulletHoleManager implements Disposable {

    private static final int GRID_SIZE = 8;  // 8x8 grid
    private static final int TOTAL_HOLES = 64;
    private static final int MAX_BULLET_HOLES = 100;  // Max visible at once
    private static final float DECAL_SIZE = 0.8f;  // Size in world units (increased for visibility)
    private static final float FADE_START_TIME = 10f;  // Start fading after 10 seconds
    private static final float FADE_DURATION = 5f;  // Fade out over 5 seconds

    private Texture spriteSheet;
    private TextureRegion[] bulletHoleRegions;
    private DecalBatch decalBatch;
    private Array<BulletHole> bulletHoles;
    private Random random;
    private boolean initialized = false;
    private PerspectiveCamera camera;

    public BulletHoleManager() {
        this.bulletHoles = new Array<>();
        this.random = new Random();
    }

    /**
     * Initialize with camera (must be called on GL thread)
     */
    public void initialize(PerspectiveCamera camera) {
        if (initialized) return;

        this.camera = camera;

        try {
            // Load sprite sheet
            spriteSheet = new Texture(Gdx.files.internal("textures/bulletholes.png"));

            int cellWidth = spriteSheet.getWidth() / GRID_SIZE;
            int cellHeight = spriteSheet.getHeight() / GRID_SIZE;

            // Create texture regions for each bullet hole
            bulletHoleRegions = new TextureRegion[TOTAL_HOLES];
            for (int row = 0; row < GRID_SIZE; row++) {
                for (int col = 0; col < GRID_SIZE; col++) {
                    int index = row * GRID_SIZE + col;
                    bulletHoleRegions[index] = new TextureRegion(
                        spriteSheet,
                        col * cellWidth,
                        row * cellHeight,
                        cellWidth,
                        cellHeight
                    );
                }
            }

            // Create decal batch with camera group strategy
            decalBatch = new DecalBatch(new CameraGroupStrategy(camera));

            initialized = true;
            Gdx.app.log("BulletHoleManager", "Initialized with " + TOTAL_HOLES + " bullet hole variants (" + cellWidth + "x" + cellHeight + " each)");
        } catch (Exception e) {
            Gdx.app.error("BulletHoleManager", "Failed to initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Add a bullet hole at the specified position
     * @param position World position of the hit
     * @param normal Surface normal at hit point (for orientation)
     */
    public void addBulletHole(Vector3 position, Vector3 normal) {
        if (!initialized || bulletHoleRegions == null) {
            Gdx.app.log("BulletHoleManager", "Cannot add bullet hole - not initialized");
            return;
        }

        // Remove oldest if at max capacity
        while (bulletHoles.size >= MAX_BULLET_HOLES) {
            bulletHoles.removeIndex(0);
        }

        // Pick a random bullet hole variant
        int variant = random.nextInt(TOTAL_HOLES);
        TextureRegion region = bulletHoleRegions[variant];

        // Create decal with transparency
        Decal decal = Decal.newDecal(DECAL_SIZE, DECAL_SIZE, region, true);

        // Position slightly above terrain to prevent z-fighting
        decal.setPosition(position.x, position.y + 0.02f, position.z);

        // For ground decals: make them face upward (lying flat on ground)
        // Use lookAt to orient the decal to face the normal direction
        Vector3 lookTarget = new Vector3(position.x, position.y + 1f, position.z);
        decal.lookAt(lookTarget, Vector3.Z);

        // Apply random rotation around the up axis
        float randomRotation = random.nextFloat() * 360f;
        decal.rotateZ(randomRotation);

        // Set blending for transparency
        decal.setBlending(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        BulletHole hole = new BulletHole(decal, 0f);
        bulletHoles.add(hole);

        Gdx.app.log("BulletHoleManager", "Added bullet hole at " + position + ", total: " + bulletHoles.size);
    }

    /**
     * Update bullet holes (fade out old ones)
     */
    public void update(float delta) {
        if (!initialized) return;

        for (int i = bulletHoles.size - 1; i >= 0; i--) {
            BulletHole hole = bulletHoles.get(i);
            hole.age += delta;

            // Calculate fade
            if (hole.age > FADE_START_TIME) {
                float fadeProgress = (hole.age - FADE_START_TIME) / FADE_DURATION;
                if (fadeProgress >= 1f) {
                    bulletHoles.removeIndex(i);
                    continue;
                }
                // Fade out alpha
                float alpha = 1f - fadeProgress;
                hole.decal.setColor(1f, 1f, 1f, alpha);
            }
        }
    }

    /**
     * Render all bullet holes
     */
    public void render() {
        if (!initialized || decalBatch == null || bulletHoles.size == 0) return;

        // Enable blending for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Disable depth write but keep depth test so decals appear on terrain
        Gdx.gl.glDepthMask(false);

        for (BulletHole hole : bulletHoles) {
            decalBatch.add(hole.decal);
        }
        decalBatch.flush();

        // Restore depth write
        Gdx.gl.glDepthMask(true);
    }

    /**
     * Get current bullet hole count
     */
    public int getCount() {
        return bulletHoles.size;
    }

    @Override
    public void dispose() {
        if (decalBatch != null) {
            decalBatch.dispose();
            decalBatch = null;
        }
        if (spriteSheet != null) {
            spriteSheet.dispose();
            spriteSheet = null;
        }
        bulletHoles.clear();
        initialized = false;
    }

    /**
     * Bullet hole data
     */
    private static class BulletHole {
        Decal decal;
        float age;

        BulletHole(Decal decal, float age) {
            this.decal = decal;
            this.age = age;
        }
    }
}
