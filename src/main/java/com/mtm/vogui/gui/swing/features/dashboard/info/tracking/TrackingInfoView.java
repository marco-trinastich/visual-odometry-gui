/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.dashboard.info.tracking;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.label.JBoldLabel;
import com.mtm.vogui.gui.swing.shared.components.label.JHintLabel;
import com.mtm.vogui.models.core.processing.tracking.TrackingStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Tracking-info section (humble view): total tracked features and inliers percentage. Owns its
 * labels as private fields; intents run on the EDT (the facade marshals).
 */
public class TrackingInfoView {

    private final JBoldLabel lblTrackingInfo;
    private final JHintLabel lblTrackingStatus;
    private final JHintLabel lblTrackingInliers;

    private final JPanel panel;

    public TrackingInfoView() {
        this.lblTrackingInfo = new JBoldLabel(GuiConstants.LBL_TRACKING_INFO);
        this.lblTrackingStatus = new JHintLabel(GuiConstants.LBL_TRACKING_STATUS);
        this.lblTrackingInliers = new JHintLabel(GuiConstants.LBL_TRACKING_INLIERS);

        this.panel = new JPanel();
        this.panel.setOpaque(false);
        this.panel.add(this.lblTrackingInfo);
        this.panel.add(this.lblTrackingStatus);
        this.panel.add(this.lblTrackingInliers);

        this.layout();
    }

    public JPanel panel() {
        return this.panel;
    }

    public void setTrackingStatus(@NotNull TrackingStatus trackingStatus) {
        this.lblTrackingStatus.setText(String.format(
                GuiConstants.LBL_TRACKING_STATUS_TEXT,
                trackingStatus.totalTracks(),
                trackingStatus.trackInliers().size(),
                trackingStatus.trackNew().size()
        ));

        this.lblTrackingInliers.setText(String.format(
                GuiConstants.PERCENTAGE_TEXT,
                trackingStatus.inliersPercent()
        ));
    }

    private void layout() {
        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.NORTH, lblTrackingInfo, 5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, lblTrackingInfo, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblTrackingInfo, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblTrackingStatus, 10, SpringLayout.SOUTH, lblTrackingInfo);
        layout.putConstraint(SpringLayout.WEST, lblTrackingStatus, 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblTrackingStatus, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblTrackingInliers, 10, SpringLayout.SOUTH, lblTrackingStatus);
        layout.putConstraint(SpringLayout.WEST, lblTrackingInliers, 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblTrackingInliers, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.SOUTH, panel, 0, SpringLayout.SOUTH, lblTrackingInliers);

        this.panel.setLayout(layout);
    }
}
