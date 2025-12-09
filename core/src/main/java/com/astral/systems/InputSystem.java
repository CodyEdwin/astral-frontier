package com.astral.systems;

import com.astral.ecs.GameSystem;
import com.astral.ecs.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;

/**
 * Input system - handles keyboard, mouse, and gamepad input
 */
public class InputSystem extends GameSystem implements InputProcessor {

    // Ship controls
    public float throttle = 0f;      // -1 to 1
    public float strafe = 0f;        // -1 to 1
    public float vertical = 0f;      // -1 to 1
    public float pitch = 0f;         // -1 to 1
    public float yaw = 0f;           // -1 to 1
    public float roll = 0f;          // -1 to 1
    public boolean boost = false;
    public boolean brake = false;

    // FPS controls
    public boolean forward = false;
    public boolean backward = false;
    public boolean left = false;
    public boolean right = false;
    public boolean jump = false;
    public boolean sprint = false;
    public boolean crouch = false;
    public boolean interact = false;

    // Combat
    public boolean primaryFire = false;
    public boolean secondaryFire = false;
    public boolean reload = false;

    // UI
    public boolean inventory = false;
    public boolean map = false;
    public boolean menu = false;
    public boolean questLog = false;

    // Mouse
    public final Vector2 mouseDelta = new Vector2();
    public final Vector2 mousePosition = new Vector2();
    public boolean mouseLocked = true;

    // Debug
    public boolean[] debugKeys = new boolean[12]; // F1-F12

    private int centerX, centerY;

    public InputSystem(World world) {
        super(world);
        setPriority(100); // High priority - process input first
    }

    @Override
    public void initialize() {
        Gdx.input.setInputProcessor(this);
        Gdx.input.setCursorCatched(true);
        centerX = Gdx.graphics.getWidth() / 2;
        centerY = Gdx.graphics.getHeight() / 2;
    }

    @Override
    public void update(float deltaTime) {
        // Reset per-frame values
        mouseDelta.setZero();
    }

    public void processInput() {
        // Ensure center is valid (may not be set if called before window ready)
        if (centerX == 0 || centerY == 0) {
            centerX = Gdx.graphics.getWidth() / 2;
            centerY = Gdx.graphics.getHeight() / 2;
        }

        // Mouse delta for look
        if (mouseLocked && centerX > 0 && centerY > 0) {
            int mx = Gdx.input.getX();
            int my = Gdx.input.getY();
            mouseDelta.set(mx - centerX, my - centerY);
            Gdx.input.setCursorPosition(centerX, centerY);
        }

        // Ship controls from keyboard
        throttle = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) throttle += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) throttle -= 1f;

        strafe = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) strafe += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) strafe -= 1f;

        vertical = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.Q)) vertical += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.E)) vertical -= 1f;

        roll = 0f;
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            // Space + A/D for roll
            if (Gdx.input.isKeyPressed(Input.Keys.A)) roll -= 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) roll += 1f;
        }

        boost = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        brake = Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT);

        // Convert mouse delta to pitch/yaw
        float sensitivity = 0.002f; // Much lower for smooth control
        yaw = -mouseDelta.x * sensitivity;
        pitch = -mouseDelta.y * sensitivity;

        // FPS controls
        forward = Gdx.input.isKeyPressed(Input.Keys.W);
        backward = Gdx.input.isKeyPressed(Input.Keys.S);
        left = Gdx.input.isKeyPressed(Input.Keys.A);
        right = Gdx.input.isKeyPressed(Input.Keys.D);
        sprint = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);

        // Combat
        primaryFire = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        secondaryFire = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            // ESC handled by GameScreen to avoid double-toggle
            case Input.Keys.I -> inventory = true;
            case Input.Keys.M -> map = true;
            case Input.Keys.J -> questLog = true;
            case Input.Keys.SPACE -> jump = true;
            case Input.Keys.C -> crouch = true;
            case Input.Keys.F -> interact = true;
            case Input.Keys.R -> reload = true;
            case Input.Keys.F1 -> debugKeys[0] = !debugKeys[0];
            case Input.Keys.F2 -> debugKeys[1] = !debugKeys[1];
            case Input.Keys.F3 -> debugKeys[2] = !debugKeys[2];
            case Input.Keys.F4 -> debugKeys[3] = !debugKeys[3];
        }
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.I -> inventory = false;
            case Input.Keys.M -> map = false;
            case Input.Keys.J -> questLog = false;
            case Input.Keys.SPACE -> jump = false;
            case Input.Keys.C -> crouch = false;
            case Input.Keys.F -> interact = false;
            case Input.Keys.R -> reload = false;
            case Input.Keys.ESCAPE -> menu = false;
        }
        return true;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        mousePosition.set(screenX, screenY);
        return true;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        return true;
    }

    public void toggleMouseLock() {
        mouseLocked = !mouseLocked;
        Gdx.input.setCursorCatched(mouseLocked);
    }

    public void setMouseLocked(boolean locked) {
        mouseLocked = locked;
        Gdx.input.setCursorCatched(locked);
    }

    public void resize(int width, int height) {
        centerX = width / 2;
        centerY = height / 2;
    }
}
