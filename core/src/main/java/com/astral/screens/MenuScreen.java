package com.astral.screens;

import com.astral.AstralFrontier;
import com.astral.ui.UIRenderer;
import com.astral.ui.UITheme;
import com.astral.ui.UIButton;
import com.astral.ui.UIPanel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Main menu screen with Starfield-inspired UI
 */
public class MenuScreen implements Screen {

    private final AstralFrontier game;
    private UIRenderer uiRenderer;
    private UIPanel mainPanel;
    private List<UIButton> menuButtons;
    private int selectedIndex = 0;
    
    private float time = 0f;
    private float[] starX, starY, starSpeed, starBrightness;
    private static final int STAR_COUNT = 300;

    public MenuScreen(AstralFrontier game) {
        this.game = game;
    }

    @Override
    public void show() {
        uiRenderer = new UIRenderer();
        menuButtons = new ArrayList<>();
        
        // Initialize starfield
        starX = new float[STAR_COUNT];
        starY = new float[STAR_COUNT];
        starSpeed = new float[STAR_COUNT];
        starBrightness = new float[STAR_COUNT];
        
        for (int i = 0; i < STAR_COUNT; i++) {
            starX[i] = MathUtils.random(Gdx.graphics.getWidth());
            starY[i] = MathUtils.random(Gdx.graphics.getHeight());
            starSpeed[i] = MathUtils.random(5f, 30f);
            starBrightness[i] = MathUtils.random(0.3f, 1f);
        }
        
        createUI();
        Gdx.input.setCursorCatched(false);
    }

    private void createUI() {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        
        // Main menu panel - centered
        float panelWidth = 400;
        float panelHeight = 450;
        float panelX = (width - panelWidth) / 2f;
        float panelY = (height - panelHeight) / 2f - 50;
        
        mainPanel = new UIPanel(panelX, panelY, panelWidth, panelHeight);
        mainPanel.show();
        
        // Menu buttons
        String[] menuItems = {"NEW GAME", "CONTINUE", "MULTIPLAYER", "OPTIONS", "QUIT"};
        float buttonWidth = 320;
        float buttonHeight = 50;
        float buttonSpacing = 15;
        float startY = panelY + panelHeight - 100;
        
        for (int i = 0; i < menuItems.length; i++) {
            float buttonX = panelX + (panelWidth - buttonWidth) / 2f;
            float buttonY = startY - i * (buttonHeight + buttonSpacing);
            
            UIButton button = new UIButton(buttonX, buttonY, buttonWidth, buttonHeight, menuItems[i]);
            button.setVisible(true);
            menuButtons.add(button);
        }
        
        // Set first button as selected
        if (!menuButtons.isEmpty()) {
            menuButtons.get(0).setHovered(true);
        }
    }

    @Override
    public void render(float delta) {
        time += delta;
        uiRenderer.update(delta);
        mainPanel.update(delta);
        
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        boolean mouseDown = Gdx.input.isTouched();
        
        for (UIButton button : menuButtons) {
            button.update(delta, mouseX, mouseY, mouseDown);
        }
        
        handleInput();
        
        // Clear screen with dark space color
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        
        // Render animated starfield
        renderStarfield(delta, width, height);
        
        // Render main panel
        mainPanel.render(uiRenderer);
        
        // Render title
        renderTitle(width, height);
        
        // Render menu buttons
        for (int i = 0; i < menuButtons.size(); i++) {
            UIButton button = menuButtons.get(i);
            button.setHovered(i == selectedIndex);
            button.render(uiRenderer);
        }
        
        // Render version info
        renderVersionInfo(width);
    }

    private void renderStarfield(float delta, int width, int height) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        
        uiRenderer.getShapeRenderer().begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Filled);
        
        for (int i = 0; i < STAR_COUNT; i++) {
            // Move stars
            starX[i] += starSpeed[i] * delta;
            if (starX[i] > width) {
                starX[i] = 0;
                starY[i] = MathUtils.random(height);
            }
            
            // Twinkle effect
            float twinkle = 0.7f + 0.3f * MathUtils.sin(time * 3f + i);
            float brightness = starBrightness[i] * twinkle;
            
            // Draw star with glow
            float size = starSpeed[i] > 20 ? 2f : 1f;
            uiRenderer.getShapeRenderer().setColor(brightness, brightness, brightness + 0.1f, 1f);
            uiRenderer.getShapeRenderer().circle(starX[i], starY[i], size);
        }
        
        uiRenderer.getShapeRenderer().end();
    }

    private void renderTitle(int width, int height) {
        uiRenderer.getSpriteBatch().begin();
        
        // Main title
        uiRenderer.getFont().getData().setScale(3.5f);
        uiRenderer.getFont().setColor(UITheme.ACCENT_CYAN);
        
        String title = "ASTRAL FRONTIER";
        GlyphLayout layout = uiRenderer.getLayout();
        layout.setText(uiRenderer.getFont(), title);
        float titleX = (width - layout.width) / 2f;
        float titleY = height * 0.85f;
        
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), title, titleX, titleY);
        
        // Subtitle
        uiRenderer.getFont().getData().setScale(1.2f);
        uiRenderer.getFont().setColor(UITheme.TEXT_SECONDARY);
        
        String subtitle = "Explore the Galaxy. Forge Your Destiny.";
        layout.setText(uiRenderer.getFont(), subtitle);
        float subtitleX = (width - layout.width) / 2f;
        
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), subtitle, subtitleX, titleY - 50);
        
        uiRenderer.getSpriteBatch().end();
    }

    private void renderVersionInfo(int width) {
        uiRenderer.getSpriteBatch().begin();
        
        uiRenderer.getFont().getData().setScale(0.9f);
        uiRenderer.getFont().setColor(UITheme.TEXT_MUTED);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), 
            "v0.1.0 Alpha - LibGDX " + com.badlogic.gdx.Version.VERSION, 15, 25);
        
        // Controls hint
        String controls = "[W/S] Navigate  [ENTER] Select  [ESC] Quit";
        GlyphLayout layout = uiRenderer.getLayout();
        layout.setText(uiRenderer.getFont(), controls);
        uiRenderer.getFont().draw(uiRenderer.getSpriteBatch(), controls, width - layout.width - 15, 25);
        
        uiRenderer.getSpriteBatch().end();
    }

    private void handleInput() {
        // Keyboard navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            selectedIndex = (selectedIndex - 1 + menuButtons.size()) % menuButtons.size();
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            selectedIndex = (selectedIndex + 1) % menuButtons.size();
        }
        
        // Mouse hover detection
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();
        
        for (int i = 0; i < menuButtons.size(); i++) {
            UIButton button = menuButtons.get(i);
            if (button.contains(mouseX, mouseY)) {
                selectedIndex = i;
            }
        }
        
        // Selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            selectMenuItem(selectedIndex);
        }
        
        // Mouse click
        if (Gdx.input.justTouched()) {
            for (int i = 0; i < menuButtons.size(); i++) {
                UIButton button = menuButtons.get(i);
                if (button.contains(mouseX, mouseY)) {
                    selectMenuItem(i);
                    break;
                }
            }
        }
        
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    private void selectMenuItem(int index) {
        switch (index) {
            case 0 -> game.startGame();           // New Game
            case 1 -> game.startGame();           // Continue (TODO: load save)
            case 2 -> { }                         // Multiplayer (TODO)
            case 3 -> { }                         // Options (TODO)
            case 4 -> Gdx.app.exit();             // Quit
        }
    }

    @Override
    public void resize(int width, int height) {
        createUI(); // Recreate UI on resize
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() { }

    @Override
    public void dispose() {
        if (uiRenderer != null) uiRenderer.dispose();
    }
}
