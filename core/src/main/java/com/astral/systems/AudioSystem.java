package com.astral.systems;

import com.astral.ecs.GameSystem;
import com.astral.ecs.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Audio system - handles 3D positional audio and music
 */
public class AudioSystem extends GameSystem implements Disposable {

    private static final int MAX_CONCURRENT_SOUNDS = 32;
    private static final float MUSIC_CROSSFADE_DURATION = 2f;

    // Caches
    private final ObjectMap<String, Sound> soundCache = new ObjectMap<>();
    private final ObjectMap<String, Music> musicCache = new ObjectMap<>();
    private final Array<ActiveSound> activeSounds = new Array<>();

    // Music state
    private Music currentMusic;
    private Music nextMusic;
    private float crossfadeProgress;
    private float musicVolume = 0.7f;
    private float sfxVolume = 1.0f;
    private float masterVolume = 1.0f;

    // Listener position (usually camera)
    private final Vector3 listenerPosition = new Vector3();
    private final Vector3 listenerForward = new Vector3(0, 0, -1);
    private final Vector3 listenerUp = new Vector3(0, 1, 0);

    public AudioSystem(World world) {
        super(world);
        setPriority(5);
    }

    @Override
    public void initialize() {
        Gdx.app.log("AudioSystem", "Audio system initialized");
    }

    @Override
    public void update(float deltaTime) {
        // Update 3D sounds
        updateActiveSounds();

        // Handle music crossfade
        updateMusicCrossfade(deltaTime);

        // Clean up finished sounds
        cleanupFinishedSounds();
    }

    private void updateActiveSounds() {
        for (ActiveSound active : activeSounds) {
            if (active.is3D) {
                float distance = active.position.dst(listenerPosition);
                float attenuation = calculateAttenuation(distance);
                float pan = calculatePan(active.position);

                Sound sound = soundCache.get(active.soundId);
                if (sound != null) {
                    sound.setVolume(active.id, active.baseVolume * attenuation * sfxVolume * masterVolume);
                    sound.setPan(active.id, pan, active.baseVolume * attenuation);
                }
            }
        }
    }

    private void updateMusicCrossfade(float deltaTime) {
        if (nextMusic != null) {
            crossfadeProgress += deltaTime / MUSIC_CROSSFADE_DURATION;

            if (crossfadeProgress >= 1f) {
                if (currentMusic != null) currentMusic.stop();
                currentMusic = nextMusic;
                currentMusic.setVolume(musicVolume * masterVolume);
                nextMusic = null;
            } else {
                if (currentMusic != null) {
                    currentMusic.setVolume(musicVolume * masterVolume * (1f - crossfadeProgress));
                }
                nextMusic.setVolume(musicVolume * masterVolume * crossfadeProgress);
            }
        }
    }

    private void cleanupFinishedSounds() {
        // Remove sounds that have finished playing
        for (int i = activeSounds.size - 1; i >= 0; i--) {
            if (activeSounds.get(i).finished) {
                activeSounds.removeIndex(i);
            }
        }
    }

    public long play(String soundId) {
        return play(soundId, 1f, 1f, 0f);
    }

    public long play(String soundId, float volume, float pitch, float pan) {
        Sound sound = getSound(soundId);
        if (sound == null) return -1;

        cullIfNeeded();

        long id = sound.play(volume * sfxVolume * masterVolume, pitch, pan);

        ActiveSound active = new ActiveSound();
        active.id = id;
        active.soundId = soundId;
        active.baseVolume = volume;
        active.is3D = false;
        activeSounds.add(active);

        return id;
    }

    public long play3D(String soundId, Vector3 position) {
        return play3D(soundId, position, 1f, 1f);
    }

    public long play3D(String soundId, Vector3 position, float volume, float pitch) {
        Sound sound = getSound(soundId);
        if (sound == null) return -1;

        cullIfNeeded();

        float distance = position.dst(listenerPosition);
        float attenuation = calculateAttenuation(distance);
        float pan = calculatePan(position);

        long id = sound.play(volume * attenuation * sfxVolume * masterVolume, pitch, pan);

        ActiveSound active = new ActiveSound();
        active.id = id;
        active.soundId = soundId;
        active.position.set(position);
        active.baseVolume = volume;
        active.is3D = true;
        activeSounds.add(active);

        return id;
    }

    private void cullIfNeeded() {
        if (activeSounds.size >= MAX_CONCURRENT_SOUNDS) {
            // Find and remove quietest/furthest sound
            ActiveSound quietest = null;
            float lowestVolume = Float.MAX_VALUE;

            for (ActiveSound s : activeSounds) {
                float vol = s.baseVolume;
                if (s.is3D) {
                    vol *= calculateAttenuation(s.position.dst(listenerPosition));
                }
                if (vol < lowestVolume) {
                    lowestVolume = vol;
                    quietest = s;
                }
            }

            if (quietest != null) {
                Sound sound = soundCache.get(quietest.soundId);
                if (sound != null) sound.stop(quietest.id);
                activeSounds.removeValue(quietest, true);
            }
        }
    }

    public void playMusic(String trackId) {
        playMusic(trackId, true);
    }

    public void playMusic(String trackId, boolean crossfade) {
        Music newTrack = getMusic(trackId);
        if (newTrack == null) return;

        if (crossfade && currentMusic != null) {
            nextMusic = newTrack;
            crossfadeProgress = 0f;
            nextMusic.setVolume(0f);
            nextMusic.setLooping(true);
            nextMusic.play();
        } else {
            if (currentMusic != null) currentMusic.stop();
            currentMusic = newTrack;
            currentMusic.setVolume(musicVolume * masterVolume);
            currentMusic.setLooping(true);
            currentMusic.play();
        }
    }

    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
        if (nextMusic != null) {
            nextMusic.stop();
            nextMusic = null;
        }
    }

    private float calculateAttenuation(float distance) {
        float refDistance = 10f;
        float maxDistance = 500f;
        float rolloff = 1f;

        distance = MathUtils.clamp(distance, refDistance, maxDistance);
        return refDistance / (refDistance + rolloff * (distance - refDistance));
    }

    private float calculatePan(Vector3 soundPos) {
        Vector3 toSound = soundPos.cpy().sub(listenerPosition).nor();
        Vector3 right = listenerForward.cpy().crs(listenerUp).nor();
        return MathUtils.clamp(toSound.dot(right), -1f, 1f);
    }

    public void setListenerPosition(Vector3 position, Vector3 forward, Vector3 up) {
        listenerPosition.set(position);
        listenerForward.set(forward);
        listenerUp.set(up);
    }

    private Sound getSound(String id) {
        Sound sound = soundCache.get(id);
        if (sound == null) {
            // Try different audio formats and paths
            String[] paths = {"audio/" + id, "audio/sfx/" + id};
            String[] extensions = {".mp3", ".ogg", ".wav"};
            for (String basePath : paths) {
                for (String ext : extensions) {
                    try {
                        String path = basePath + ext;
                        if (Gdx.files.internal(path).exists()) {
                            sound = Gdx.audio.newSound(Gdx.files.internal(path));
                            soundCache.put(id, sound);
                            Gdx.app.log("AudioSystem", "Loaded sound: " + path);
                            return sound;
                        }
                    } catch (Exception e) {
                        // Try next format
                    }
                }
            }
            Gdx.app.error("AudioSystem", "Failed to load sound: " + id);
        }
        return sound;
    }

    private Music getMusic(String id) {
        Music music = musicCache.get(id);
        if (music == null) {
            try {
                music = Gdx.audio.newMusic(Gdx.files.internal("audio/music/" + id + ".ogg"));
                musicCache.put(id, music);
            } catch (Exception e) {
                Gdx.app.error("AudioSystem", "Failed to load music: " + id);
            }
        }
        return music;
    }

    // Volume controls
    public void setMasterVolume(float volume) {
        this.masterVolume = MathUtils.clamp(volume, 0f, 1f);
        updateMusicVolume();
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = MathUtils.clamp(volume, 0f, 1f);
        updateMusicVolume();
    }

    public void setSfxVolume(float volume) {
        this.sfxVolume = MathUtils.clamp(volume, 0f, 1f);
    }

    private void updateMusicVolume() {
        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume * masterVolume);
        }
    }

    @Override
    public void dispose() {
        stopMusic();

        for (Sound sound : soundCache.values()) {
            sound.dispose();
        }
        soundCache.clear();

        for (Music music : musicCache.values()) {
            music.dispose();
        }
        musicCache.clear();
    }

    private static class ActiveSound {
        long id;
        String soundId;
        final Vector3 position = new Vector3();
        float baseVolume;
        boolean is3D;
        boolean finished = false;
    }
}
