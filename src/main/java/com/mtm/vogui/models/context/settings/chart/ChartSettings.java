/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.settings.chart;

import com.mtm.vogui.models.enums.settings.ChartType;
import com.mtm.vogui.models.interfaces.WithDefault;
import lombok.Data;
import org.jetbrains.annotations.NotNull;


/**
 * Chart settings
 * <p/>
 * Options related to charts.
 */
@Data
public class ChartSettings implements WithDefault<ChartSettings> {

    private ChartType type;
    // When auto is set, the chart auto-ranges (fit-all) and the paired scale is ignored; otherwise the
    // scale is the fixed initial zoom (1.0 = the reference span). Auto off by default: an explicit
    // choice rather than the old magic where scale 1.0 doubled as the auto sentinel.
    private boolean autoScaleXZ;
    private double scaleXZ;
    private boolean autoScaleY;
    private double scaleY;

    public ChartSettings() {
        this.loadDefaults();
    }

    public ChartSettings(@NotNull ChartSettings chart) {
        this.type = chart.type;
        this.autoScaleXZ = chart.autoScaleXZ;
        this.scaleXZ = chart.scaleXZ;
        this.autoScaleY = chart.autoScaleY;
        this.scaleY = chart.scaleY;
    }

    public void loadDefaults() {
        this.type = ChartType.YFrames;
        this.autoScaleXZ = false;
        this.scaleXZ = 1.0;
        this.autoScaleY = false;
        this.scaleY = 1.0;
    }
}
