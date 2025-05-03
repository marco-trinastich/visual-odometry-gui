/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.processing.tracking;

import com.mtm.vogui.gui.components.chart.ChartScrollPane;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.models.enums.settings.ChartType;
import com.mtm.vogui.models.settings.Settings;
import com.mtm.vogui.utilities.CommonUtils;
import lombok.Builder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.math.BigDecimal;

@Builder
public class PointFactory {
    int chartId;
    ChartType chartType;

    public TrackedPoint newStartPoint() {
        return TrackedPoint.builder()
                .chartId(chartId)
                .chartType(chartType)
                .startPoint(true)
                .build();
    }

    public TrackedPoint newEndPoint() {
        return TrackedPoint.builder()
                .chartId(chartId)
                .chartType(chartType)
                .endPoint(true)
                .build();
    }

    public TrackedPoint newPoint(int frame, Double time, Double x, Double y, Double z, BigDecimal inliersPercent) {
        return TrackedPoint.builder()
                .chartId(chartId)
                .chartType(chartType)
                .frame(frame)
                .time(time)
                .formattedTime(CommonUtils.getFormattedTime(time))
                .x(x)
                .y(y)
                .z(z)
                .inliersPercent(inliersPercent)
                .build();
    }

    public TrackedPoint newPoint(@NotNull ProcessingStatus status, Double y) {
        return this.newPoint(
                status.fps().totalProcessed(),
                status.fps().totalSeconds(),
                status.translation().getX(),
                y != null ? y : -status.translation().getY(),
                status.translation().getZ(),
                status.tracking().inliersPercent());
    }

    public TrackedPoint newPoint(@NotNull ProcessingStatus status) {
        return this.newPoint(status, null);
    }

    public static void removeLastChart(@NotNull DefaultListModel<TrackedPoint> trackedPoints) {
        int lastChartStartIndex = -1;
        for (int i = trackedPoints.getSize() - 1; i >= 0; i--) {
            var trackedPoint = trackedPoints.get(i);
            if (trackedPoint.startPoint) {
                lastChartStartIndex = i;
                break;
            }
        }
        if (lastChartStartIndex > -1) {
            trackedPoints.removeRange(lastChartStartIndex, trackedPoints.getSize() - 1);
        }
    }

    public static PointFactory from(int chartId, ChartType ChartType) {
        return PointFactory.builder()
                .chartId(chartId)
                .chartType(ChartType)
                .build();
    }

    public static PointFactory from(@NotNull Settings settings, @NotNull ProcessingParameters params) {
        var chartType = params.frozenSettings().core().chart().type();
        var chartXZPanel = settings.state().guiController().chartXZPanel();

        return PointFactory.from(chartXZPanel.getChartsCount(), chartType);
    }
}
