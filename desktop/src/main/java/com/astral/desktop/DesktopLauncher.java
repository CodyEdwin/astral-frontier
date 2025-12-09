package com.astral.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.astral.AstralFrontier;

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
    }
}
