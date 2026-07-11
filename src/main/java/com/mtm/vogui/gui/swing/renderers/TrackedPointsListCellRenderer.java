/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.renderers;

import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.utilities.CommonUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class TrackedPointsListCellRenderer implements ListCellRenderer<TrackedPoint> {
    private final DefaultListCellRenderer defaultListCellRenderer = new DefaultListCellRenderer();

    @Override
    public Component getListCellRendererComponent(JList<? extends TrackedPoint> list, TrackedPoint trackedPoint,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        DefaultListCellRenderer component = (DefaultListCellRenderer) defaultListCellRenderer
                .getListCellRendererComponent(list, null, index, isSelected, cellHasFocus);
        component.setText(getRenderedText(trackedPoint, true));

        return component;
    }

    public static String getRenderedText(@NotNull TrackedPoint trackedPoint, boolean addDetails) {
        String renderedText;
        if (trackedPoint.startPoint()) {
            renderedText = String.format(GuiConstants.LIST_CHART_START, trackedPoint.chartId());
        } else if (trackedPoint.endPoint()) {
            renderedText = String.format(GuiConstants.LIST_CHART_END, trackedPoint.chartId());
        } else {
            renderedText = String.format(GuiConstants.LIST_CHART_POINT,
                    trackedPoint.frame(),
                    trackedPoint.formattedTime(),
                    trackedPoint.x() != null ?
                            CommonUtils.roundBigDecimal(trackedPoint.x(), 2) :
                            GuiConstants.LIST_CHART_NA,
                    trackedPoint.y() != null ?
                            CommonUtils.roundBigDecimal(trackedPoint.y(), 2) :
                            GuiConstants.LIST_CHART_NA,
                    trackedPoint.z() != null ?
                            CommonUtils.roundBigDecimal(trackedPoint.z(), 2) :
                            GuiConstants.LIST_CHART_NA,
                    trackedPoint.inliersPercent()
            );

            if (addDetails) {
                renderedText +=
                        String.format(GuiConstants.LIST_CHART_POINT_DETAILS, trackedPoint.chartType().id());
            }
        }

        return renderedText;
    }
}
