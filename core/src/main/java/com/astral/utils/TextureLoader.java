package com.astral.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.Array;

/**
 * Utility class for loading and managing textures.
 * Provides standardized texture loading with common settings.
 */
public class TextureLoader {

    private TextureLoader() {} // Utility class

    /**
     * Load a texture with default high-quality settings
     * (mipmaps, linear filtering, repeat wrapping)
     */
    public static Texture load(String path) {
        return load(path, true, TextureFilter.MipMapLinearLinear, TextureWrap.Repeat);
    }

    /**
     * Load a texture with custom settings
     */
    public static Texture load(String path, boolean genMipMaps, TextureFilter filter, TextureWrap wrap) {
        try {
            FileHandle file = Gdx.files.internal(path);
            if (file.exists()) {
                Texture tex = new Texture(file, genMipMaps);
                tex.setFilter(filter, TextureFilter.Linear);
                tex.setWrap(wrap, wrap);
                return tex;
            } else {
                Gdx.app.error("TextureLoader", "File not found: " + path);
            }
        } catch (Exception e) {
            Gdx.app.error("TextureLoader", "Failed to load " + path + ": " + e.getMessage());
        }
        return null;
    }

    /**
     * Load a texture for UI (no mipmaps, clamp to edge)
     */
    public static Texture loadUI(String path) {
        return load(path, false, TextureFilter.Linear, TextureWrap.ClampToEdge);
    }

    /**
     * Safely dispose all textures in an array
     */
    public static void disposeAll(Array<Texture> textures) {
        if (textures == null) return;
        for (Texture tex : textures) {
            if (tex != null) {
                tex.dispose();
            }
        }
        textures.clear();
    }
}
