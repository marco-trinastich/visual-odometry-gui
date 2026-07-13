/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.rendering;

import boofcv.gui.feature.VisualizeFeatures;
import com.mtm.vogui.core.rendering.RenderSink;
import com.mtm.vogui.gui.fx.utils.FxUtils;
import com.mtm.vogui.gui.fx.state.CurrentFps;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.gui.fx.state.Telemetry;
import com.mtm.vogui.gui.fx.state.TrajectoryEvent;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.models.enums.settings.ChartType;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.interfaces.Resolution;
import georegression.struct.point.Point2D_F64;
import jakarta.enterprise.inject.spi.CDI;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

/**
 * JavaFX implementation of the core {@link RenderSink}: writes into the observable
 * {@link GuiState} (views bind to it) and shows dialogs on the FX thread. Per-frame channels
 * use coalesced hand-off so the vo worker never floods the FX thread.
 * Not a bean: built by {@code gui.UiBootstrap} only when the JavaFX UI is active; resolves
 * its own dependencies programmatically (same pattern as the launchers).
 * <p>
 * The trajectory charts are fed through a dedicated ordered channel: the sink emits
 * {@link TrajectoryEvent}s (plot points + segment lifecycle) that the charts feature applies — so the
 * sink stays widget-free, driving everything through {@link GuiState}.
 */
public class FxRenderSink implements RenderSink {

    // Carry current translation over as previous every Nth frame (heading/covered-distance deltas).
    private static final int LONGER_RENDER_INTERVAL = 10;

    private final AppContext context;
    private final GuiState guiState;
    private final Consumer<AppStatus> coalescedAppStatus;
    private final Consumer<Image> coalescedInputFrame;
    private final Consumer<Image> coalescedOutputFrame;
    private final Consumer<Integer> coalescedKltPyramidLevels;
    // Ordered (non-dropping) hand-off for tracked-point log ops, applied in order on the FX thread.
    private final Consumer<Consumer<ObservableList<TrackedPoint>>> pointOps;
    // Ordered (non-dropping) hand-off for trajectory events (plot points + segment lifecycle), applied
    // in order on the FX thread; the charts feature is the registered consumer (via GuiState).
    private final Consumer<TrajectoryEvent> chartOps;

    public FxRenderSink() {
        var cdi = CDI.current();
        this.context = cdi.select(AppContext.class).get();
        this.guiState = cdi.select(GuiState.class).get();
        this.coalescedAppStatus = FxUtils.coalescedFxConsumer(status -> guiState.appStatusProperty().set(status));
        this.coalescedInputFrame = FxUtils.coalescedFxConsumer(image -> guiState.inputFrameProperty().set(image));
        this.coalescedOutputFrame = FxUtils.coalescedFxConsumer(image -> guiState.outputFrameProperty().set(image));
        this.coalescedKltPyramidLevels =
                FxUtils.coalescedFxConsumer(levels -> guiState.kltPyramidLevelsProperty().set(levels));
        this.pointOps = FxUtils.orderedFxConsumer(op -> op.accept(guiState.trackedPoints()));
        this.chartOps = FxUtils.orderedFxConsumer(guiState::emitTrajectory);
    }

    // Dialogs

    @Override
    public void notifyError(String message) {
        FxUtils.runOnFxThreadAndWait(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
            alert.setHeaderText(null);
            alert.showAndWait();
        });
    }

    @Override
    public boolean confirmOrCancel(String message) {
        return FxUtils.callOnFxThreadAndWait(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK, ButtonType.CANCEL);
            alert.setHeaderText(null);
            return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
        });
    }

    // Synchronous queries

    @Override
    public int chartsCount() {
        // Open-segment count, kept in sync by the charts feature; read cross-thread (no FX hop needed).
        return guiState.segmentCount().get();
    }

    // App status

    @Override
    public void renderAppStatus(AppStatus appStatus) {
        coalescedAppStatus.accept(appStatus);
    }

    // Processing lifecycle

    @Override
    public void renderStartPoint(ProcessingParameters params) {
        // Open the chart segment (align the altitude axis + carry the initial-zoom scales) and log the
        // matching start marker. The start marker is the log's own record; the chart holds the points.
        var settings = params.frozenContext().settings();
        chartOps.accept(new TrajectoryEvent.StartSegment(
                settings.chart().type(),
                settings.chart().autoScaleXZ(),
                settings.chart().scaleXZ(),
                settings.chart().autoScaleY(),
                settings.chart().scaleY()));

        TrackedPoint marker = params.pointFactory().newStartPoint();
        pointOps.accept(points -> points.add(marker));
    }

    @Override
    public void renderEndPoint(ProcessingParameters params) {
        // Close-or-drop is the charts feature's call (it holds both series): on close it appends this end
        // marker to the log, on drop it removes the whole open segment from the log. The marker is built
        // here because the sink owns the run's PointFactory.
        chartOps.accept(new TrajectoryEvent.EndSegment(params.pointFactory().newEndPoint()));
    }

    @Override
    public void renderClearAllPoints() {
        chartOps.accept(new TrajectoryEvent.ClearAll());
        pointOps.accept(ObservableList::clear);
    }

    @Override
    public void resizeAndRepositionVideoFrames(ProcessingParameters params) {
        // No-op in the single-window layout: the viewports letterbox to their SplitPane slots, so
        // there are no free-floating frames to size/position (the Swing sink drove separate JFrames).
    }

    // Per-frame rendering

    @Override
    public void renderVO(ProcessingStatus status, ProcessingParameters params, boolean voResult) {
        renderInputVideo(status);
        renderTrackedFeatures(status, voResult);
        renderOutputVideo(status);
        renderTelemetry(status, params, voResult);
        renderCharts(status, params, voResult);
    }

    @Override
    public void renderInputVideo(BufferedImage image) {
        // Mirror the Swing sink: show the preview only when enabled, otherwise clear the viewport.
        coalescedInputFrame.accept(context.settings().input().inputPreview() ? FxUtils.toFxImage(image) : null);
    }

    @Override
    public void renderCurrentFps(FpsStatus fpsStatus, ProcessingStatus status, ProcessingParameters params) {
        // Instantaneous rates, roughly once per second. Device sources read the live capture clock;
        // file sources use the rate the core already computed on the fps status.
        SourceType sourceType = context.settings().input().source();
        double inputCurrent = SourceType.Device.is(sourceType)
                ? context.state().device().getCurrentFPS()
                : fpsStatus.inputCurrentFPS();
        CurrentFps snapshot = new CurrentFps(inputCurrent, fpsStatus.currentFPS());
        Platform.runLater(() -> guiState.currentFpsProperty().set(snapshot));

        // Time-based chart: plot a per-second altitude sample on the Y chart and log it (mirrors the
        // Swing sink's renderCurrentFps; the frame-based case plots from renderCharts instead).
        if (context.state().processing().not(ProcessingState.Paused)
                && ChartType.YSeconds.is(context.settings().chart().type())) {
            ProcessingStatus frozen = status.deepClone();
            chartOps.accept(new TrajectoryEvent.Altitude(
                    frozen.fps().totalSeconds(), -frozen.translation().getY()));
            TrackedPoint point = params.pointFactory().newPoint(frozen);
            pointOps.accept(points -> points.add(point));
        }
    }

    @Override
    public void renderBufferStatus(BufferStatus bufferStatus) {
        // A null status hides the buffer section; the telemetry view branches on it.
        Platform.runLater(() -> guiState.bufferProperty().set(bufferStatus));
    }

    // Settings healed by the core, reflected into the GUI

    @Override
    public void deviceResolutionChanged(Resolution resolution) {
        // Low-frequency (once per run start): publish on GuiState; the input section reflects it.
        Platform.runLater(() -> guiState.healedDeviceResolutionProperty().set(resolution));
    }

    @Override
    public void devicePathChanged(DevicePath devicePath) {
        Platform.runLater(() -> guiState.healedDevicePathProperty().set(devicePath));
    }

    @Override
    public void recentPathUsed(RecentPathTarget target, PathSettings pathSettings, String usedPath) {
        // The history in `pathSettings` is already updated core-side; the section re-reads it from the
        // domain, so only the (target, usedPath) tag needs to cross to the FX thread.
        Platform.runLater(() ->
                guiState.recentPathUsedProperty().set(new GuiState.RecentPathUsed(target, usedPath)));
    }

    @Override
    public void kltPyramidLevelsChanged(int pyramidLevels) {
        // Publish on GuiState; the tracker settings section reflects it into its field.
        coalescedKltPyramidLevels.accept(pyramidLevels);
    }

    // Private members (mirror the Swing sink: input is re-rendered for file sources, tracked
    // features are drawn onto the vo frame, then the output frame is handed off)

    private void renderInputVideo(@NotNull ProcessingStatus status) {
        // Device sources push their preview through renderInputVideo(BufferedImage) directly; file
        // sources have no preview thread, so the input frame is re-rendered here per processed frame.
        if (SourceType.Video.is(context.settings().input().source())) {
            renderInputVideo(status.frame().input().buffered());
        }
    }

    private void renderTrackedFeatures(@NotNull ProcessingStatus status, boolean voResult) {
        if (!voResult) {
            return;
        }
        // Draw active/new tracks onto the vo output frame (pure AWT on a BufferedImage: data, not widgets)
        Graphics2D g2 = status.frame().vo().buffered().createGraphics();
        if (context.settings().tracker().showActiveTracks()) {
            for (Point2D_F64 p : status.tracking().trackInliers()) {
                VisualizeFeatures.drawPoint(g2, (int) p.getX(), (int) p.getY(), Color.blue);
            }
        }
        if (context.settings().tracker().showNewTracks()) {
            for (Point2D_F64 p : status.tracking().trackNew()) {
                VisualizeFeatures.drawPoint(g2, (int) p.getX(), (int) p.getY(), Color.green);
            }
        }
        g2.dispose();
    }

    private void renderOutputVideo(@NotNull ProcessingStatus status) {
        coalescedOutputFrame.accept(FxUtils.toFxImage(status.frame().vo().buffered()));
    }

    private void renderTelemetry(@NotNull ProcessingStatus status, @NotNull ProcessingParameters params,
                                 boolean voResult) {
        // Freeze the core data (+ the presentation inputs the status doesn't carry) for the FX thread;
        // the telemetry sections only format and display. Mirrors the Swing sink's renderMainInfo.
        var settings = params.frozenContext().settings();
        var sourceType = settings.input().source();
        double deviceFps = SourceType.Device.is(sourceType) ? context.state().device().getAverageFPS() : 0;

        ProcessingStatus frozen = status.deepClone();
        if (SourceType.Device.is(sourceType)) {
            frozen.fps().inputAverageFPS(deviceFps);
        }
        // Carry current translation over as previous every Nth frame (heading/covered-distance deltas).
        if (status.fps().totalProcessed() % LONGER_RENDER_INTERVAL == 0) {
            status.prevTranslation(frozen.translation().copy());
        }

        // Freeze the set-and-forget run inputs the status doesn't carry (shown in the Processing section).
        String calibrationFile = settings.input().calibration().path();
        String sourcePath = SourceType.Video.is(sourceType)
                ? settings.input().video().path()
                : settings.input().device().path().name();

        Telemetry snapshot = new Telemetry(frozen, calibrationFile, sourcePath);
        Platform.runLater(() -> guiState.telemetryProperty().set(snapshot));

        // Log the tracked point for a valid estimate (Y logged unless the chart is time-based).
        if (voResult) {
            Double loggedY = ChartType.YSeconds.is(settings.chart().type())
                    ? null : -frozen.translation().getY();
            TrackedPoint point = params.pointFactory().newPoint(frozen, loggedY);
            pointOps.accept(points -> points.add(point));
        }
    }

    private void renderCharts(@NotNull ProcessingStatus status, @NotNull ProcessingParameters params,
                             boolean voResult) {
        if (!voResult) {
            return;
        }
        // Ground-track point every processed frame; the frame-based altitude point rides along (the
        // second-based one is plotted from renderCurrentFps instead). Mirrors the Swing sink.
        var chartType = params.frozenContext().settings().chart().type();
        ProcessingStatus frozen = status.deepClone();

        Double altitudeX = null;
        Double altitudeY = null;
        if (ChartType.YFrames.is(chartType)) {
            altitudeX = (double) frozen.fps().totalProcessed();
            altitudeY = -frozen.translation().getY();
        }
        chartOps.accept(new TrajectoryEvent.Plot(
                frozen.translation().getX(), frozen.translation().getZ(), altitudeX, altitudeY));
    }
}
