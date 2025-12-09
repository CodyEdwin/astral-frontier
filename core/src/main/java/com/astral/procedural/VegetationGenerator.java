package com.astral.procedural;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import java.util.Random;

/**
 * Generates animated vegetation (cacti, trees, bushes, rocks) for terrain
 */
public class VegetationGenerator {

    private final long seed;
    private final ModelBuilder modelBuilder;

    // Cached models
    private Model cactusModel;
    private Model palmTreeModel;
    private Model deadTreeModel;
    private Model bushModel;
    private Model rockModel;
    private Model grassTuftModel;

    public VegetationGenerator(long seed) {
        this.seed = seed;
        this.modelBuilder = new ModelBuilder();
        createModels();
    }

    private void createModels() {
        cactusModel = createCactus();
        palmTreeModel = createPalmTree();
        deadTreeModel = createDeadTree();
        bushModel = createBush();
        rockModel = createRock();
        grassTuftModel = createGrassTuft();
    }

    private Model createCactus() {
        modelBuilder.begin();
        Material green = new Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.5f, 0.25f, 1f)));
        MeshPartBuilder b = modelBuilder.part("cactus", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, green);
        b.box(0, 2f, 0, 0.4f, 4f, 0.4f);
        b.box(-0.6f, 2.5f, 0, 0.25f, 1.5f, 0.25f);
        b.box(0.6f, 3f, 0, 0.25f, 2f, 0.25f);
        return modelBuilder.end();
    }

    private Model createPalmTree() {
        modelBuilder.begin();
        Material trunk = new Material(ColorAttribute.createDiffuse(new Color(0.45f, 0.3f, 0.15f, 1f)));
        MeshPartBuilder t = modelBuilder.part("trunk", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, trunk);
        t.box(0, 3f, 0, 0.4f, 6f, 0.4f);

        Material fronds = new Material(ColorAttribute.createDiffuse(new Color(0.2f, 0.5f, 0.2f, 1f)));
        MeshPartBuilder f = modelBuilder.part("fronds", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, fronds);
        for (int i = 0; i < 6; i++) {
            float angle = i * 60f * MathUtils.degreesToRadians;
            float x = MathUtils.cos(angle) * 1.5f;
            float z = MathUtils.sin(angle) * 1.5f;
            f.box(x, 5.8f, z, 0.2f, 0.1f, 1.8f);
        }
        return modelBuilder.end();
    }

    private Model createDeadTree() {
        modelBuilder.begin();
        Material wood = new Material(ColorAttribute.createDiffuse(new Color(0.35f, 0.25f, 0.15f, 1f)));
        MeshPartBuilder b = modelBuilder.part("dead", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, wood);
        b.box(0, 2f, 0, 0.35f, 4f, 0.35f);
        b.box(-0.7f, 3f, 0, 0.12f, 0.12f, 1f);
        b.box(0.6f, 3.5f, 0.2f, 0.12f, 0.12f, 0.8f);
        return modelBuilder.end();
    }

    private Model createBush() {
        modelBuilder.begin();
        Material green = new Material(ColorAttribute.createDiffuse(new Color(0.3f, 0.45f, 0.25f, 1f)));
        MeshPartBuilder b = modelBuilder.part("bush", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, green);
        b.box(0, 0.4f, 0, 0.9f, 0.8f, 0.9f);
        b.box(0.25f, 0.6f, 0.15f, 0.5f, 0.5f, 0.5f);
        return modelBuilder.end();
    }

    private Model createRock() {
        modelBuilder.begin();
        Material stone = new Material(ColorAttribute.createDiffuse(new Color(0.55f, 0.5f, 0.45f, 1f)));
        MeshPartBuilder b = modelBuilder.part("rock", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, stone);
        b.box(0, 0.35f, 0, 1.1f, 0.7f, 0.9f);
        b.box(0.15f, 0.7f, 0.1f, 0.5f, 0.35f, 0.4f);
        return modelBuilder.end();
    }

    private Model createGrassTuft() {
        modelBuilder.begin();
        Material grass = new Material(ColorAttribute.createDiffuse(new Color(0.55f, 0.5f, 0.3f, 1f)));
        MeshPartBuilder b = modelBuilder.part("grass", GL20.GL_TRIANGLES,
            VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal, grass);
        b.box(0, 0.2f, 0, 0.04f, 0.4f, 0.04f);
        b.box(0.08f, 0.18f, 0.04f, 0.03f, 0.36f, 0.03f);
        b.box(-0.06f, 0.19f, -0.04f, 0.03f, 0.38f, 0.03f);
        return modelBuilder.end();
    }

    /**
     * Generate vegetation for a chunk
     */
    public Array<VegetationInstance> generateForChunk(int chunkX, int chunkZ, float chunkWorldSize,
                                                       PlanetType planetType, HeightProvider heightProvider) {
        Array<VegetationInstance> instances = new Array<>();
        Random rand = new Random(seed ^ ((long) chunkX << 16) ^ chunkZ);

        float offsetX = chunkX * chunkWorldSize;
        float offsetZ = chunkZ * chunkWorldSize;

        int count = switch (planetType) {
            case DESERT -> rand.nextInt(12) + 4;
            case FOREST -> rand.nextInt(40) + 20;
            case ICE -> rand.nextInt(4) + 1;
            case LAVA -> rand.nextInt(2);
            default -> rand.nextInt(8) + 3;
        };

        for (int i = 0; i < count; i++) {
            float lx = rand.nextFloat() * chunkWorldSize;
            float lz = rand.nextFloat() * chunkWorldSize;
            float wx = offsetX + lx;
            float wz = offsetZ + lz;
            float h = heightProvider.getHeight(wx, wz);

            Model model = chooseModel(planetType, rand);
            if (model == null) continue;

            float scale = 0.6f + rand.nextFloat() * 0.8f;
            float rot = rand.nextFloat() * 360f;
            float animPhase = rand.nextFloat() * MathUtils.PI2;
            float animSpeed = 0.5f + rand.nextFloat() * 1f;
            boolean canAnimate = (model == palmTreeModel || model == bushModel || model == grassTuftModel);

            ModelInstance inst = new ModelInstance(model);
            inst.transform.setToTranslation(wx, h, wz);
            inst.transform.rotate(Vector3.Y, rot);
            inst.transform.scale(scale, scale, scale);

            instances.add(new VegetationInstance(inst, wx, wz, animPhase, animSpeed, canAnimate));
        }
        return instances;
    }

    private Model chooseModel(PlanetType type, Random rand) {
        float r = rand.nextFloat();
        return switch (type) {
            case DESERT -> r < 0.35f ? cactusModel : r < 0.5f ? palmTreeModel :
                           r < 0.65f ? deadTreeModel : r < 0.85f ? rockModel : grassTuftModel;
            case FOREST -> r < 0.4f ? palmTreeModel : r < 0.7f ? bushModel : grassTuftModel;
            case ICE -> r < 0.7f ? rockModel : deadTreeModel;
            default -> r < 0.6f ? rockModel : deadTreeModel;
        };
    }

    public void dispose() {
        if (cactusModel != null) cactusModel.dispose();
        if (palmTreeModel != null) palmTreeModel.dispose();
        if (deadTreeModel != null) deadTreeModel.dispose();
        if (bushModel != null) bushModel.dispose();
        if (rockModel != null) rockModel.dispose();
        if (grassTuftModel != null) grassTuftModel.dispose();
    }

    public interface HeightProvider {
        float getHeight(float x, float z);
    }

    /**
     * Vegetation instance with animation support
     */
    public static class VegetationInstance {
        public final ModelInstance modelInstance;
        public final float worldX, worldZ;
        public final float animPhase;
        public final float animSpeed;
        public final boolean canAnimate;
        private final Vector3 basePosition = new Vector3();

        public VegetationInstance(ModelInstance inst, float wx, float wz,
                                   float phase, float speed, boolean animate) {
            this.modelInstance = inst;
            this.worldX = wx;
            this.worldZ = wz;
            this.animPhase = phase;
            this.animSpeed = speed;
            this.canAnimate = animate;
            inst.transform.getTranslation(basePosition);
        }

        /**
         * Update wind sway animation
         */
        public void update(float time) {
            if (!canAnimate) return;

            float sway = MathUtils.sin(time * animSpeed + animPhase) * 0.02f;
            float swayZ = MathUtils.cos(time * animSpeed * 0.7f + animPhase) * 0.015f;

            // Apply subtle rotation for wind effect
            modelInstance.transform.setToTranslation(basePosition);
            modelInstance.transform.rotate(Vector3.X, sway * MathUtils.radiansToDegrees);
            modelInstance.transform.rotate(Vector3.Z, swayZ * MathUtils.radiansToDegrees);
        }
    }
}
