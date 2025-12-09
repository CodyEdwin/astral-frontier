package com.astral.screens;

import com.astral.AstralFrontier;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Main menu screen
 */
public class MenuScreen implements Screen {

    private final AstralFrontier game;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
    private BitmapFont titleFont;
    private BitmapFont menuFont;

    private String[] menuItems = {"New Game", "Continue", "Multiplayer", "Options", "Quit"};
    private int selectedIndex = 0;

    private float starfieldOffset = 0f;

    public MenuScreen(AstralFrontier game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        font = new BitmapFont();
        font.setColor(Color.WHITE);

        titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);
        titleFont.setColor(0.7f, 0.85f, 1f, 1f);

        menuFont = new BitmapFont();
        menuFont.getData().setScale(2f);

        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void render(float delta) {
        handleInput();

        // Animate starfield
        starfieldOffset += delta * 10f;

        // Clear screen
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();

        // Draw animated starfield background
        renderStarfield(width, height);

        batch.begin();

        // Draw title
        String title = "ASTRAL FRONTIER";
        float titleX = width / 2f - 250;
        float titleY = height * 0.8f;
        titleFont.draw(batch, title, titleX, titleY);

        // Draw subtitle
        font.setColor(0.5f, 0.6f, 0.8f, 1f);
        font.draw(batch, "Explore the Galaxy. Forge Your Destiny.", titleX + 20, titleY - 50);

        // Draw menu items
        float menuStartY = height * 0.55f;
        float menuSpacing = 60f;

        for (int i = 0; i < menuItems.length; i++) {
            float y = menuStartY - i * menuSpacing;

            if (i == selectedIndex) {
                // Selected item
                menuFont.setColor(0.3f, 0.8f, 1f, 1f);

                // Draw selection indicator - must end batch first
                batch.end();
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0.2f, 0.4f, 0.6f, 0.3f);
                shapeRenderer.rect(width / 2f - 150, y - 35, 300, 50);
                shapeRenderer.end();
                batch.begin();

                menuFont.draw(batch, "> " + menuItems[i], width / 2f - 100, y);
            } else {
                // Unselected item
                menuFont.setColor(0.6f, 0.6f, 0.7f, 1f);
                menuFont.draw(batch, menuItems[i], width / 2f - 80, y);
            }
        }

        // Draw version info
        font.setColor(0.4f, 0.4f, 0.5f, 1f);
        font.draw(batch, "v0.1.0 Alpha - LibGDX " + com.badlogic.gdx.Version.VERSION, 10, 30);

        batch.end();
    }

    private void renderStarfield(int width, int height) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Point);

        // Simple animated starfield
        for (int i = 0; i < 200; i++) {
            float seed = i * 1234.5678f;
            float x = ((seed % width) + starfieldOffset * (0.5f + (i % 3) * 0.5f)) % width;
            float y = (seed * 0.7f) % height;
            float brightness = 0.3f + (i % 10) * 0.07f;

            shapeRenderer.setColor(brightness, brightness, brightness + 0.1f, 1f);
            shapeRenderer.point(x, y, 0);
        }

        shapeRenderer.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex = (selectedIndex - 1 + menuItems.length) % menuItems.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex = (selectedIndex + 1) % menuItems.length;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            selectMenuItem(selectedIndex);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void selectMenuItem(int index) {
        switch (index) {
            case 0 -> { // New Game
                game.startGame();
            }
            case 1 -> { // Continue
                // TODO: Load saved game
                game.startGame();
            }
            case 2 -> { // Multiplayer
                // TODO: Show multiplayer lobby
            }
            case 3 -> { // Options
                // TODO: Show options menu
            }
            case 4 -> { // Quit
                Gdx.app.exit();
            }
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
        if (titleFont != null) titleFont.dispose();
        if (menuFont != null) menuFont.dispose();
    }
}
