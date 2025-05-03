/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.listeners.trackedpoints;

import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.settings.ChartType;
import com.mtm.vogui.models.settings.Settings;
import com.mtm.vogui.gui.GuiController;
import com.mtm.vogui.gui.components.info.InfoScrollPane;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class TrackedPointsMoveChartOnSelection implements ListSelectionListener {
    private final Settings settings;
    private final GuiController controller;

    public TrackedPointsMoveChartOnSelection(Settings settings, GuiController controller) {
        this.settings = settings;
        this.controller = controller;
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        InfoScrollPane chartInfoPanel = controller.infoPanel();

        // If the event comes from tracked points list, no changes are made and no vo process is ongoing
        if (chartInfoPanel.isListEvent(evt) &&
                !evt.getValueIsAdjusting() &&
                settings.state().processing().not(ProcessingState.Running)) {

            // Get selected element from tracked points list
            TrackedPoint selected = chartInfoPanel.getSelectedPoint();

            if (selected == null)
                return;

            if (selected.x() != null && selected.z() != null) {
                //Move the XZ chart to the (x, z) point position
                controller.chartXZPanel().moveToPoint(selected.x(), selected.z());
            }

            if (selected.y() != null) {
                // Move the Y chart to the (frame, y) point position
                var x = ChartType.YFrames.is(selected.chartType()) ? selected.frame() : selected.time();
                controller.chartYPanel().moveToPoint(x, selected.y());
            }
        }
    }
}
