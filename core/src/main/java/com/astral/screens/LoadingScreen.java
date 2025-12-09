package com.astral.screens;

import com.astral.AstralFrontier;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Loading screen shown during initial asset loading
 */
public class LoadingScreen implements Screen {

    private final AstralFrontier game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private float loadProgress = 0f;
    private float displayProgress = 0f;
    private boolean loadingComplete = false;
    private float minDisplayTime = 1f; // Minimum time to show loading screen
    private float elapsedTime = 0f;

    public LoadingScreen(AstralFrontier game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);

        // Start loading assets
        startAssetLoading();
    }

    private void startAssetLoading() {
        // TODO: Queue assets for loading
        // For now, simulate loading
        loadingComplete = true;
        loadProgress = 1f;
    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;

        // Update loading progress
        // TODO: Check actual asset loading progress
        // loadProgress = assetManager.getProgress();

        // Smooth progress bar animation
        displayProgress += (loadProgress - displayProgress) * delta * 5f;

        // Clear screen
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        int barWidth = 400;
        int barHeight = 20;
        int barX = (width - barWidth) / 2;
        int barY = height / 3;

        // Draw progress bar background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.2f, 0.2f, 0.3f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Draw progress bar fill
        shapeRenderer.setColor(0.3f, 0.6f, 1f, 1f);
        shapeRenderer.rect(barX, barY, barWidth * displayProgress, barHeight);
        shapeRenderer.end();

        // Draw border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.5f, 0.5f, 0.6f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);
        shapeRenderer.end();

        // Draw text
        batch.begin();

        // Title
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(3f);
        titleFont.setColor(0.7f, 0.8f, 1f, 1f);
        String title = "ASTRAL FRONTIER";
        float titleWidth = titleFont.getRegion().getRegionWidth() * title.length() * 0.5f;
        titleFont.draw(batch, title, (width - titleWidth) / 2 - 100, height * 0.7f);

        // Loading text
        font.setColor(Color.WHITE);
        String loadingText = "Loading... " + (int)(displayProgress * 100) + "%";
        font.draw(batch, loadingText, barX, barY - 20);

        // Tips
        font.getData().setScale(1f);
        font.setColor(0.6f, 0.6f, 0.7f, 1f);
        font.draw(batch, "Press ESC at any time to access the menu", barX, barY - 60);

        batch.end();
        titleFont.dispose();

        // Transition to menu when loading complete
        if (loadingComplete && elapsedTime > minDisplayTime && displayProgress > 0.99f) {
            game.showMainMenu();
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
    }
}
