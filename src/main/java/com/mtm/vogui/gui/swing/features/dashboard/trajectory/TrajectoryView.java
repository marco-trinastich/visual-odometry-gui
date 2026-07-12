/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.dashboard.trajectory;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.features.dashboard.DashboardView;
import com.mtm.vogui.gui.swing.shared.components.chart.ChartScrollPane;
import com.mtm.vogui.models.enums.gui.ChartAxis;
import com.mtm.vogui.models.enums.settings.ChartType;

import javax.swing.*;
import java.awt.*;

/**
 * Trajectory feature (humble view): owns the two trajectory charts (XZ ground track + Y
 * altitude) of the dashboard window. Wraps the reusable {@link ChartScrollPane} widgets as
 * private fields and exposes intent methods - the widgets never leave this class.
 * <p>
 * Threading: high-frequency renders driven by the vo worker thread ({@link #plotFrame} /
 * {@link #addAltitudePoint}) marshal to the EDT themselves; the remaining ops are invoked
 * from the EDT (settings buttons, point-selection navigation, or inside a {@link DashboardView}
 * lifecycle {@code invokeLater}) and mutate directly.
 */
public class TrajectoryView {

    private final ChartScrollPane xzChart;
    private final ChartScrollPane yChart;

    public TrajectoryView() {
        // XZ ground-track chart: default origin (0,0), X/Z axes centered
        this.xzChart = new ChartScrollPane(GuiConstants.CHART_XZ_TITLE);
        this.xzChart.style().backgroundColor(Color.white);
        this.xzChart.style().plotColor(Color.blue);
        this.xzChart.style().axisColor(Color.black);
        this.xzChart.style().axisNames(ChartAxis.X, ChartAxis.Z);
        this.xzChart.style().axisNamesColor(Color.blue);
        this.xzChart.style().axisUnitsColor(Color.blue);
        this.xzChart.style().showLegend(true);
        this.xzChart.setPreferredSize(new Dimension(400, 400));

        // Y altitude chart: custom origin, X fixed (Y axis at 20), Y auto-centered
        this.yChart = new ChartScrollPane(20, 85, false, true, GuiConstants.CHART_Y_TITLE);
        this.yChart.style().backgroundColor(Color.white);
        this.yChart.style().plotColor(Color.blue);
        this.yChart.style().axisColor(Color.black);
        this.yChart.style().axisNames(ChartAxis.Frame, ChartAxis.Y);
        this.yChart.style().axisNamesColor(Color.blue);
        this.yChart.style().axisUnitsColor(Color.blue);
        this.yChart.setPreferredSize(new Dimension(400, 200));
    }

    // Frame composition (used by DashboardView only): the charts as opaque components

    public JComponent xzComponent() {
        return this.xzChart;
    }

    public JComponent yComponent() {
        return this.yChart;
    }

    // Queries

    public int xzChartsCount() {
        return this.xzChart.getChartsCount();
    }

    public boolean xzHasPoints() {
        return this.xzChart.hasPoints();
    }

    /**
     * True when both charts have at least one point in the current (open) segment: the
     * condition to close a valid trajectory segment. EDT only.
     */
    public boolean hasBothOpenSegmentPoints() {
        return this.xzChart.hasPointsLastChart() && this.yChart.hasPointsLastChart();
    }

    // Lifecycle (EDT only: called inside a DashboardView invokeLater)

    /**
     * Sets the Y chart axis names to match the chart type (frame- or second-based).
     */
    public void setAltitudeAxis(ChartType chartType) {
        this.yChart.style().axisNames(chartType.xAxis(), chartType.yAxis());
    }

    /**
     * Closes the current segment on both charts (starts a fresh point list).
     */
    public void closeSegment() {
        this.xzChart.closeChart();
        this.yChart.closeChart();
    }

    /**
     * Clears every point and resets both charts to their origin/size.
     */
    public void clearAll() {
        this.xzChart.clearAllPoints();
        this.yChart.clearAllPoints();
        this.xzChart.resetSize();
        this.yChart.resetSize();
    }

    // Per-frame rendering (vo worker thread: marshals to the EDT)

    /**
     * Adds the estimated ground-track point (and, when {@code altitudeX}/{@code altitudeY} are
     * given, the frame-based altitude point), re-syncing both chart scales first. One EDT hop.
     */
    public void plotFrame(double xzX, double xzZ, Double altitudeX, Double altitudeY,
                          double xzScale, double yScale) {
        SwingUtilities.invokeLater(() -> {
            syncScale(this.xzChart, xzScale);
            syncScale(this.yChart, yScale);

            this.xzChart.addPoint(xzX, xzZ);
            if (altitudeX != null && altitudeY != null) {
                this.yChart.addPoint(altitudeX, altitudeY);
            }
        });
    }

    /**
     * Adds a second-based altitude point to the Y chart. One EDT hop.
     */
    public void addAltitudePoint(double x, double y) {
        SwingUtilities.invokeLater(() -> this.yChart.addPoint(x, y));
    }

    // View controls (EDT: settings buttons / point-selection navigation)

    public void applyXZScale(double scale) {
        this.xzChart.style().chartScale(scale);
        this.xzChart.resetSize();
    }

    public void applyYScale(double scale) {
        this.yChart.style().chartScale(scale);
        this.yChart.resetSize();
    }

    public void showXZ3DPoints(boolean enabled) {
        this.xzChart.style().thickPoints(enabled);
        this.xzChart.repaint();
    }

    public void moveXZToOrigin() {
        this.xzChart.moveToOrigin();
    }

    public void moveXZToLast() {
        this.xzChart.moveToLast();
    }

    public void moveYToOrigin() {
        this.yChart.moveToOrigin();
    }

    public void moveYToLast() {
        this.yChart.moveToLast();
    }

    public void moveXZToPoint(double x, double z) {
        this.xzChart.moveToPoint(x, z);
    }

    public void moveYToPoint(double x, double y) {
        this.yChart.moveToPoint(x, y);
    }

    private static void syncScale(ChartScrollPane chart, double scale) {
        if (chart.style().chartScale() != scale) {
            chart.style().chartScale(scale);
            chart.resetSize();
        }
    }
}
