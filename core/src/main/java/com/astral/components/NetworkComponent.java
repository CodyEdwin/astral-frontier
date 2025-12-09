package com.astral.components;

import com.astral.ecs.Component;
import java.util.UUID;

/**
 * Network synchronization component
 */
public class NetworkComponent implements Component {

    public int networkId;
    public UUID ownerId;
    public boolean isOwner = false;
    public int lastInputSequence = 0;
    public long lastUpdateTime = 0;
    public NetworkPriority priority = NetworkPriority.MEDIUM;

    // Interpolation state
    public boolean needsInterpolation = false;
    public float interpolationAlpha = 0f;

    public enum NetworkPriority {
        LOW(100),       // Update every 100ms
        MEDIUM(50),     // Update every 50ms
        HIGH(20),       // Update every 20ms
        CRITICAL(0);    // Every tick

        public final int updateInterval;

        NetworkPriority(int interval) {
            this.updateInterval = interval;
        }
    }

    @Override
    public void reset() {
        networkId = 0;
        ownerId = null;
        isOwner = false;
        lastInputSequence = 0;
        lastUpdateTime = 0;
        priority = NetworkPriority.MEDIUM;
        needsInterpolation = false;
        interpolationAlpha = 0f;
    }
}
