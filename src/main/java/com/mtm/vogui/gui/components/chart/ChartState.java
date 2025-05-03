/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.components.chart;

import georegression.struct.point.Point2D_F64;
import georegression.struct.point.Point2D_I32;
import lombok.Data;

import java.util.ArrayList;

@Data
public class ChartState {
    private int originX;
    private int originY;
    private final int initX;
    private final int initY;

    private final ArrayList<ArrayList<Point2D_F64>> series;

    public ChartState(int originX, int originY) {
        this.initX = originX;
        this.initY = originY;
        this.series = new ArrayList<>();

        this.reset();
    }

    public void reset() {
        this.resetOrigin();
        this.resetPoints();
    }

    public void resetOrigin() {
        this.originX = this.initX;
        this.originY = this.initY;
    }

    private void resetPoints() {
        this.series.clear();
        this.series.add(new ArrayList<>());
    }

    public void removeLast() {
        if (!this.series.isEmpty()) {
            this.series.removeLast();
        }
    }

    public Point2D_I32 getOrigin() {
        return new Point2D_I32(this.originX, this.originY);
    }

    public Point2D_F64 getLastPoint() {
        Point2D_F64 lastPoint = null;
        for (int i = this.series.size() - 1; i >= 0; i--) {
            var chart = this.series.get(i);
            if (!chart.isEmpty()) {
                lastPoint = chart.getLast();
                break;
            }
        }
        return lastPoint;
    }

    public boolean hasPoints() {
        return this.series.stream().anyMatch(x -> !x.isEmpty());
    }
}
