/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.telemetry;

import com.mtm.vogui.gui.fx.features.sidebar.telemetry.framerate.FpsInfoView;
import com.mtm.vogui.gui.fx.features.sidebar.telemetry.odometry.OdometryInfoView;
import com.mtm.vogui.gui.fx.features.sidebar.telemetry.processing.ProcessingInfoView;
import com.mtm.vogui.gui.fx.features.sidebar.telemetry.trackedpoints.TrackedPointsView;
import com.mtm.vogui.gui.fx.features.sidebar.telemetry.tracking.TrackingInfoView;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Telemetry (Run Info) feature facade (humble view, JavaFX counterpart of the Swing
 * {@code gui.swing.features.dashboard.info.InfoView}): the sidebar's "Telemetry" tab. A vertical
 * {@link SplitPane} splits the scrollable readouts (top) from the tracked-points log (bottom), so the
 * log stays visible and the divider is user-resizable. Pure reactive display — it listens to the
 * observable {@code fx.state.GuiState} channels and pushes each snapshot into the (dumb) sections.
 * <p>
 * Channels: per-frame {@link GuiState#telemetryProperty()} feeds processing/odometry/tracking/fps-averages;
 * per-second {@link GuiState#currentFpsProperty()} feeds the fps current column; {@link GuiState#bufferProperty()}
 * feeds the buffer row inside the Framerate section; {@link GuiState#trackedPoints()} backs the log list.
 * All fire on the FX Application Thread. Exposes the uniform contract {@link #content()}.
 */
public class TelemetryView {

    private final SplitPane content;
    private final TrackedPointsView trackedPoints;

    public TelemetryView(GuiState guiState) {
        ProcessingInfoView processing = new ProcessingInfoView();
        OdometryInfoView odometry = new OdometryInfoView();
        TrackingInfoView tracking = new TrackingInfoView();
        FpsInfoView fps = new FpsInfoView();

        VBox sections = new VBox(14,
                processing.node(), odometry.node(), tracking.node(), fps.node());
        sections.getStyleClass().add("telemetry-content");
        ScrollPane readouts = new ScrollPane(sections);
        readouts.setFitToWidth(true);

        this.trackedPoints = new TrackedPointsView(guiState.trackedPoints());

        this.content = new SplitPane(readouts, trackedPoints.node());
        this.content.setOrientation(Orientation.VERTICAL);
        // Readouts are compact now, so give the points log a bigger share (it's the long, scrolled part).
        this.content.setDividerPositions(0.83);

        guiState.telemetryProperty().addListener((_, _, telemetry) -> {
            if (telemetry != null) {
                processing.render(telemetry);
                odometry.render(telemetry);
                tracking.render(telemetry);
                fps.render(telemetry);
            }
        });
        guiState.currentFpsProperty().addListener((_, _, current) -> {
            if (current != null) {
                fps.renderCurrent(current);
            }
        });
        // Buffer status (null hides the buffer row inside the Framerate section).
        guiState.bufferProperty().addListener((_, _, status) -> fps.renderBuffer(status));
    }

    public Region content() {
        return this.content;
    }

    /** The user-selected tracked point, for the shell's point-selection → chart navigation wiring. */
    public ReadOnlyObjectProperty<TrackedPoint> selectedTrackedPoint() {
        return this.trackedPoints.selectedProperty();
    }
}
