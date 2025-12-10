package com.astral.screens.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;

/**
 * Renders the pause menu overlay.
 */
public class PauseMenuRenderer implements Disposable {

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    public PauseMenuRenderer() {
    }

    public void initialize() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(3f);
    }

    /**
     * Render the pause menu overlay
     */
    public void render() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Dark overlay
        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.7f);
        shapeRenderer.rect(0, 0, width, height);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Pause text
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, "PAUSED", width / 2f - 80, height / 2f + 50);
        font.getData().setScale(1.5f);
        font.draw(batch, "Press ESC to resume", width / 2f - 120, height / 2f - 20);
        font.getData().setScale(3f);
        batch.end();
    }

    /**
     * Render game hints (controls, etc.)
     */
    public void renderHints() {
        batch.begin();
        font.getData().setScale(1.5f);
        font.setColor(0.5f, 0.8f, 1f, 1f);
        font.draw(batch, "Press L to land on planet", 20, 60);
        font.draw(batch, "WASD: Move | Mouse: Look | Shift: Boost", 20, 30);
        font.getData().setScale(3f);
        batch.end();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
    }
}
