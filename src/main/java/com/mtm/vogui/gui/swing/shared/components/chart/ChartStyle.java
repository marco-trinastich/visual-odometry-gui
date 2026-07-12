/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.chart;

import com.mtm.vogui.models.enums.gui.ChartAxis;
import lombok.Data;

import java.awt.*;

/**
 * Runtime presentation config of a {@link ChartScrollPane} (colors, scale, axis names,
 * point style). Distinct from the persisted {@code models.context.settings.chart.ChartSettings}:
 * this is view-only state, never serialized. Fully toolkit-generic: it knows nothing about the
 * app's chart types (the caller maps a chart type to a pair of {@link ChartAxis}).
 */
@Data
public class ChartStyle {
    // Chart settings
    private double chartScale;
    private boolean centeredOriginX;
    private boolean centeredOriginY;
    private boolean followNewPoints;
    private boolean showLegend;
    private boolean thickPoints;

    // Axis settings
    private boolean showAxis;
    private boolean showAxisUnits;
    private boolean showPermanentAxisNames;
    private ChartAxis xAxisName;
    private ChartAxis yAxisName;

    // Colors
    private Color backgroundColor;
    private Color plotColor;
    private Color axisColor;
    private Color axisUnitsColor;
    private Color axisNamesColor;
    private boolean multipleColors;

    public ChartStyle(boolean autoCenterX, boolean autoCenterY) {
        // Chart
        this.chartScale = 1.0;
        this.centeredOriginX = autoCenterX;
        this.centeredOriginY = autoCenterY;
        this.followNewPoints = true;
        this.showLegend = false;
        this.thickPoints = false;

        // Axis
        this.showAxis = true;
        this.showAxisUnits = true;
        this.showPermanentAxisNames = true;
        this.xAxisName = ChartAxis.X;
        this.yAxisName = ChartAxis.Y;

        // Colors
        this.plotColor = Color.blue;
        this.axisColor = Color.black;
        this.axisUnitsColor = Color.blue;
        this.axisNamesColor = Color.blue;
        this.multipleColors = true;
    }

    public void axisNames(ChartAxis x, ChartAxis y) {
        this.xAxisName = x;
        this.yAxisName = y;
    }

    /**
     * Plot color with fallback to blue.
     */
    public Color resolvedPlotColor() {
        return this.plotColor != null ? this.plotColor : Color.blue;
    }

    /**
     * Axis-names color with fallback to the axis color, then black.
     */
    public Color resolvedAxisNamesColor() {
        if (this.axisNamesColor != null) {
            return this.axisNamesColor;
        }
        return this.axisColor != null ? this.axisColor : Color.black;
    }
}
