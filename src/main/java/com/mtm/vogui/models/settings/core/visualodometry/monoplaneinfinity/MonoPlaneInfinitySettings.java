/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.visualodometry.monoplaneinfinity;

import boofcv.factory.sfm.ConfigPlanarTrackPnP;
import com.mtm.vogui.models.interfaces.WithDefault;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * MonoPlaneInfinity settings
 * </p>
 * Options related to MonoPlaneInfinity vo algorithm.
 */
@Data
@Dependent
public class MonoPlaneInfinitySettings implements Serializable, WithDefault<MonoPlaneInfinitySettings> {
    private int thresholdAdd;
    private int thresholdRetire;
    private double inlierPixelTol;
    private int ransacIterations;

    @Inject
    public MonoPlaneInfinitySettings() {
        this.loadDefaults();
    }

    public MonoPlaneInfinitySettings(@NotNull MonoPlaneInfinitySettings monoPlaneInfinitySettings) {
        this.thresholdAdd(monoPlaneInfinitySettings.thresholdAdd);
        this.thresholdRetire(monoPlaneInfinitySettings.thresholdRetire);
        this.inlierPixelTol(monoPlaneInfinitySettings.inlierPixelTol);
        this.ransacIterations(monoPlaneInfinitySettings.ransacIterations);
    }

    public void loadDefaults() {
        // Defaults: 75, 2, 1.5, 200
        this.thresholdAdd = 75;
        this.thresholdRetire = 2;
        this.inlierPixelTol = 1.5;
        this.ransacIterations = 200;
    }

    public ConfigPlanarTrackPnP getConfig(){
        ConfigPlanarTrackPnP config = new ConfigPlanarTrackPnP();
        config.thresholdAdd = this.thresholdAdd;
        config.thresholdRetire = this.thresholdRetire;
        config.thresholdPixelError = this.inlierPixelTol;
        config.ransac.iterations = this.ransacIterations;

        return config;
    }
}
