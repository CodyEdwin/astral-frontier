package com.astral.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.astral.AstralFrontier;

/**
 * Android launcher for Astral Frontier
 */
public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();

        // Enable OpenGL ES 3.0 for better graphics
        config.useGL30 = true;

        // Disable sensors we don't need for battery life
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;

        // Enable immersive mode (hide system bars)
        config.useImmersiveMode = true;

        // Set multisampling for smoother edges
        config.numSamples = 2;

        // Initialize the game
        initialize(new AstralFrontier(), config);
    }
}
