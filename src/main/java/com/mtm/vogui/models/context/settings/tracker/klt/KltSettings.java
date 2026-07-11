/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.settings.tracker.klt;

import com.mtm.vogui.models.interfaces.WithDefault;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * KLT tracker settings
 */
@Data
public class KltSettings implements Serializable, WithDefault<KltSettings> {

    private int templateRadius;
    private int pyramidLevels;
    private int maxFeatures;
    private int radius;
    private float threshold;

    public KltSettings() {
        // Also the Jackson deserialization entry point
        this.loadDefaults();
    }

    public KltSettings(@NotNull KltSettings klt) {
        this.templateRadius = klt.templateRadius;
        this.pyramidLevels = klt.pyramidLevels;
        this.maxFeatures = klt.maxFeatures;
        this.radius = klt.radius;
        this.threshold = klt.threshold;
    }

    public void loadDefaults() {
        this.templateRadius = 3;
        this.pyramidLevels = 4;     // pyramidScaling=[1,2,4,8]
        this.maxFeatures = 600;     // also 200 is a good value
        this.radius = 3;
        this.threshold = 1.00f;
    }
}
