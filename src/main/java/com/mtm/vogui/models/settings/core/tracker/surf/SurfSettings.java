/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.tracker.surf;

import com.mtm.vogui.models.interfaces.WithDefault;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * SURF tracker settings
 */
@Data
public class SurfSettings implements Serializable, WithDefault<SurfSettings> {

    private int maxFeaturesPerScale;
    private int extractRadius;
    private int initialSampleSize;

    public SurfSettings() {
        // Also the Jackson deserialization entry point
        this.loadDefaults();
    }

    public SurfSettings(@NotNull SurfSettings surf) {
        this.maxFeaturesPerScale = surf.maxFeaturesPerScale;
        this.extractRadius = surf.extractRadius;
        this.initialSampleSize = surf.initialSampleSize;
    }

    public void loadDefaults() {
        this.maxFeaturesPerScale = 200;
        this.extractRadius = 3;
        this.initialSampleSize = 2;
    }
}
