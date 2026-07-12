/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.chart;

import java.awt.*;
import java.util.ArrayList;

import javax.swing.*;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.listeners.DragMoveListener;
import com.mtm.vogui.gui.swing.utils.SwingUtils;

import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * ChartScrollPane
 * <p/>
 * This panel handles a generic scrollable point chart
 */
@SuppressWarnings("serial")
public class ChartScrollPane extends JScrollPane {

    private final ChartState state;
    @Getter
    private final ChartStyle style;

    private final ChartPanel chartPanel;
    private final ChartViewPort chartViewPort;

    /**
     * Default origin (0,0), auto centering, custom title
     */
    public ChartScrollPane(String title) {
        this(0, 0, true, true, title);
    }

    /**
     * Custom origin, custom centering
     */
    public ChartScrollPane(int originX, int originY, boolean autoCenterX, boolean autoCenterY, String title) {
        super();

        this.state = new ChartState(originX, originY);
        this.style = new ChartStyle(autoCenterX, autoCenterY);

        this.chartPanel = new ChartPanel(state, style);
        this.chartViewPort = new ChartViewPort(this.state, this.style, this);
        this.initContainer(title);
    }

    private void initContainer(String title) {
        // Init chart container
        this.setViewport(this.chartViewPort);
        this.setViewportView(this.chartPanel);
        this.setOpaque(false);
        this.addMouseMotionListener(new DragMoveListener(this.chartViewPort, this.chartPanel));
        //this.addMouseMotionListener(new DragMoveListenerScrollBars(this));

        if (title != null) {
            this.setBorder(SwingUtils.getRoundedTitledBorder(title, GuiConstants.PANEL_BORDER_ACTIVE_COLOR));
        }
    }


    // Points handling

    public void addPoint(double x, double y) {
        this.state.series().getLast().add(new Point2D_F64(x, y));
        this.refit();

        if (this.style.followNewPoints()) {
            this.moveToPoint(x, y);
        }
    }

    public boolean hasPoints() {
        return this.state.hasPoints();
    }

    public boolean hasPointsLastChart() {
        return this.state.getLastPoint() != null;
    }

    public void clearAllPoints() {
        this.state.reset();
        this.repaint();
    }

    public void closeChart() {
        // Add new chart
        this.state.series().add(new ArrayList<>());
        this.repaint();
    }

    public int getChartsCount() {
        return this.state.series().size();
    }


    // Repositioning

    public void moveToOrigin() {
        this.moveToPoint(this.state.getOrigin());
    }

    public void moveToLast() {
        if (!this.state.hasPoints())
            return;

        Point2D_F64 lastPoint = this.state.getLastPoint();
        if (lastPoint != null) {
            this.moveToPoint(lastPoint);
        }
    }

    public void moveToPoint(Point2D_F64 point) {
        // Need to remap real coords to display coords
        this.moveToPoint(this.getDisplayPoint(point));
    }

    public void moveToPoint(double x, double y) {
        // Need to remap real coords to display coords
        this.moveToPoint(this.getDisplayPoint(x, y));
    }

    public void moveToPoint(@NotNull Point2D_I32 point) {
        this.moveToPoint(point.x, point.y);
    }

    public void moveToPoint(int x, int y) {
        // Scroll coords
        int scrollX = x - (this.getWidth() / 2);
        int scrollY = y - (this.getHeight() / 2);

        // Scroll to points
        this.getHorizontalScrollBar().setValue(scrollX);
        this.getVerticalScrollBar().setValue(scrollY);
    }


    // Painting

    public void resetSize() {
        // Reset origin, refit to the data at the current scale, lay out synchronously so the
        // scrollbars pick up the new extent, then scroll to origin - no deferred paint latch.
        this.state.resetOrigin();
        this.refit();
        this.validate();
        this.moveToOrigin();
    }

    /**
     * Recomputes the panel's preferred size from the data (starting from the current viewport
     * extent) and re-installs it in the scroll pane. Replaces the old grow-inside-paint + the
     * "repaint" property-change round-trip.
     */
    private void refit() {
        Dimension extent = this.getViewport().getExtentSize();
        this.chartPanel.recomputeBounds(extent.width, extent.height);
        this.setViewportView(this.chartPanel);
        this.chartPanel.repaint();
    }

    public void repaintViewport() {
        this.chartViewPort.paintComponent(this.chartViewPort.getGraphics());
    }


    // Mapping functions

    private @NotNull Point2D_I32 getDisplayPoint(@NotNull Point2D_F64 point) {
        return this.getDisplayPoint(point.getX(), point.getY());
    }

    private @NotNull Point2D_I32 getDisplayPoint(double x, double y) {
        // On screen coords
        int displayX = (int) Math.round(this.state.originX() + (x * this.style.chartScale()));
        int displayY = (int) Math.round(this.state.originY() - (y * this.style.chartScale()));
        return new Point2D_I32(displayX, displayY);
    }
}
