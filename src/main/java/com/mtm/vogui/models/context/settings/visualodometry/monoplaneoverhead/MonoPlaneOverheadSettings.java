/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.settings.visualodometry.monoplaneoverhead;

import com.mtm.vogui.models.interfaces.WithDefault;
import lombok.Data;
import org.jetbrains.annotations.NotNull;


/**
 * MonoPlaneOverhead settings
 * </p>
 * Options related to MonoPlaneOverhead vo algorithm.
 */
@Data
public class MonoPlaneOverheadSettings implements WithDefault<MonoPlaneOverheadSettings> {
    private double cellSize;
    private double maxCellsPerPixel;
    private double mapHeightFraction;
    private double inlierGroundTol;
    private int ransacIterations;
    private int thresholdRetire;
    private int absoluteMinimumTracks;
    private double respawnTrackFraction;
    private double respawnCoverageFraction;

    public MonoPlaneOverheadSettings() {
        this.loadDefaults();
    }

    public MonoPlaneOverheadSettings(@NotNull MonoPlaneOverheadSettings voSettings) {
        this.cellSize(voSettings.cellSize);
        this.maxCellsPerPixel(voSettings.maxCellsPerPixel);
        this.mapHeightFraction(voSettings.mapHeightFraction);
        this.inlierGroundTol(voSettings.inlierGroundTol);
        this.ransacIterations(voSettings.ransacIterations);
        this.thresholdRetire(voSettings.thresholdRetire);
        this.absoluteMinimumTracks(voSettings.absoluteMinimumTracks);
        this.respawnTrackFraction(voSettings.respawnTrackFraction);
        this.respawnCoverageFraction(voSettings.respawnCoverageFraction);
    }

    public void loadDefaults() {
        this.cellSize = 0.06;
        this.maxCellsPerPixel = 25;
        this.mapHeightFraction = 0.7;
        this.inlierGroundTol = 1.5;
        this.ransacIterations = 300;
        this.thresholdRetire = 2;
        this.absoluteMinimumTracks = 100;
        this.respawnTrackFraction = 0.5;
        this.respawnCoverageFraction = 0.6;
    }
}
