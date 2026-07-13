/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.telemetry.tracking;

import com.mtm.vogui.gui.fx.features.sidebar.telemetry.ui.TelemetryUi;
import com.mtm.vogui.gui.fx.state.Telemetry;
import com.mtm.vogui.models.core.processing.tracking.TrackingStatus;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/**
 * Tracking section (humble view): total tracked features, inliers and new tracks, plus the inliers
 * percentage. Pure display from the frozen snapshot.
 */
public class TrackingInfoView {

    private final Label tracks = TelemetryUi.value();
    private final Label inliers = TelemetryUi.value();
    private final Label newTracks = TelemetryUi.value();
    private final Label inliersPercent = TelemetryUi.value();

    private final Region root;

    public TrackingInfoView() {
        GridPane grid = TelemetryUi.grid();
        TelemetryUi.row(grid, 0, "Tracks", tracks);
        TelemetryUi.row(grid, 1, "Inliers", inliers);
        TelemetryUi.row(grid, 2, "New", newTracks);
        TelemetryUi.row(grid, 3, "Inliers %", inliersPercent);
        this.root = TelemetryUi.section("Tracking", grid);
    }

    public Region node() {
        return this.root;
    }

    public void render(Telemetry telemetry) {
        TrackingStatus tracking = telemetry.status().tracking();
        tracks.setText(String.valueOf(tracking.totalTracks()));
        inliers.setText(String.valueOf(tracking.trackInliers().size()));
        newTracks.setText(String.valueOf(tracking.trackNew().size()));
        inliersPercent.setText(tracking.inliersPercent() + " %");
    }
}
