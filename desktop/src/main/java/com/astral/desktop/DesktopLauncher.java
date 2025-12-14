package com.astral.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.utils.Timer;
import com.astral.AstralFrontier;
import org.lwjgl.glfw.GLFW;

/**
 * Desktop launcher for AstralFrontier
 */
public class DesktopLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        // Window settings
        config.setTitle("AstralFrontier");
        config.setWindowedMode(1920, 1080);
        config.useVsync(true);
        config.setForegroundFPS(60);

        // OpenGL settings
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 3);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);

        // Window behavior
        config.setResizable(true);
        //config.setWindowIcon("icons/icon128.png", "icons/icon64.png", "icons/icon32.png", "icons/icon16.png");

        new Lwjgl3Application(new AstralFrontier(), config);

        // Make window always on top after initialization
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                try {
                    long window = ((Lwjgl3Graphics) com.badlogic.gdx.Gdx.graphics).getWindow().getWindowHandle();
                    GLFW.glfwSetWindowAttrib(window, GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }, 0.1f);
    }
}
