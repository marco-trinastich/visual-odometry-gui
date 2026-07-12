/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.rendering;

import boofcv.gui.feature.VisualizeFeatures;
import com.mtm.vogui.core.rendering.RenderSink;
import com.mtm.vogui.gui.swing.state.GuiState;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.models.enums.settings.ChartType;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.interfaces.Resolution;
import georegression.struct.point.Point2D_F64;
import jakarta.enterprise.inject.spi.CDI;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Swing implementation of the core {@link RenderSink}: the only place where core-produced
 * data meets widgets. Every method marshals to the EDT via {@code SwingUtilities.invokeLater}
 * (dialogs excepted: they block the calling vo worker thread until the user answers).
 * Not a bean: built by {@code gui.UiBootstrap} only when the Swing UI is active; resolves
 * its own dependencies programmatically (same pattern as the launchers).
 */
public class SwingRenderSink implements RenderSink {
    private final static int LONGER_RENDER_INTERVAL = 10;

    private final AppContext context;
    private final GuiState guiState;

    public SwingRenderSink() {
        var cdi = CDI.current();
        this.context = cdi.select(AppContext.class).get();
        this.guiState = cdi.select(GuiState.class).get();
    }

    // Dialogs

    @Override
    public void notifyError(String message) {
        guiState.controlPanelView().showErrorDialog(message);
    }

    @Override
    public boolean confirmOrCancel(String message) {
        return guiState.controlPanelView().showConfirmDialog(message);
    }

    // Synchronous queries

    @Override
    public int chartsCount() {
        return guiState.dashboardView().chartsCount();
    }

    // App status

    @Override
    public void renderAppStatus(AppStatus appStatus) {
        guiState.dashboardView().info().setAppStatus(appStatus);
    }

    // Processing lifecycle

    @Override
    public void renderStartPoint(@NotNull ProcessingParameters params) {
        guiState.dashboardView().startSegment(params);
    }

    @Override
    public void renderEndPoint(ProcessingParameters params) {
        guiState.dashboardView().endSegment(params);
    }

    @Override
    public void renderClearAllPoints() {
        guiState.dashboardView().clearAll();
    }

    @Override
    public void resizeAndRepositionVideoFrames(@NotNull ProcessingParameters params) {
        guiState.videoView().resizeAndReposition(params);
    }

    // Per-frame rendering

    @Override
    public void renderVO(ProcessingStatus status, ProcessingParameters params, boolean voResult) {
        renderInputVideo(status);
        renderTrackedFeatures(status, voResult);
        renderOutputVideo(status);
        renderInfoPanel(status, params, voResult);
        renderCharts(status, params, voResult);
    }

    @Override
    public void renderInputVideo(BufferedImage image) {
        // Input video rendering (if needed)
        if (context.settings().input().inputPreview()) {
            guiState.videoView().showInput(image);
        } else {
            guiState.videoView().hideInput();
        }
    }

    @Override
    public void renderCurrentFps(FpsStatus fpsStatus, ProcessingStatus status,
                                 @NotNull ProcessingParameters params) {
        // Running every second

        // Settings
        var sourceType = context.settings().input().source();
        var chartType = context.settings().chart().type();
        var pointFactory = params.pointFactory();

        // Update input fps
        if (SourceType.Device.is(sourceType)) {
            var deviceFps = context.state().device().getCurrentFPS();
            fpsStatus.inputCurrentFPS(deviceFps);
        }

        // Frozen processing/fps status (so that AWT will later be able to display the correct info)
        FpsStatus frozenFpsStatus = fpsStatus.deepClone();
        ProcessingStatus frozenStatus = status.deepClone();
        frozenStatus.fps(frozenFpsStatus);

        var dashboard = guiState.dashboardView();
        dashboard.info().setCurrentFps(frozenStatus);

        if (context.state().processing().not(ProcessingState.Paused) && ChartType.YSeconds.is(chartType)) {
            // Add (second, Y) altitude point and log it in the tracked points
            dashboard.trajectory().addAltitudePoint(
                    frozenStatus.fps().totalSeconds(), -frozenStatus.translation().getY());
            dashboard.info().addTrackedPoint(pointFactory.newPoint(frozenStatus));
        }
    }

    @Override
    public void renderBufferStatus(BufferStatus bufferStatus) {
        if (bufferStatus == null) {
            guiState.dashboardView().info().hideBuffer();
        } else {
            guiState.dashboardView().info().showBuffer(bufferStatus);
        }
    }

    // Settings healed by the core, reflected into the GUI

    @Override
    public void deviceResolutionChanged(Resolution resolution) {
        guiState.controlPanelView().settings().showDeviceResolution(resolution);
    }

    @Override
    public void devicePathChanged(DevicePath devicePath) {
        guiState.controlPanelView().settings().showDevicePath(devicePath);
    }

    @Override
    public void recentPathUsed(@NotNull RecentPathTarget target, @NotNull PathSettings pathSettings,
                               String usedPath) {
        guiState.controlPanelView().settings().showRecentPath(target, pathSettings, usedPath);
    }

    @Override
    public void kltPyramidLevelsChanged(int pyramidLevels) {
        guiState.controlPanelView().settings().showKltPyramidLevels(pyramidLevels);
    }

    // Private members

    private void renderInputVideo(@NotNull ProcessingStatus status) {
        var sourceType = context.settings().input().source();
        if (SourceType.Video.is(sourceType)) {
            renderInputVideo(status.frame().input().buffered());
        }
    }

    private void renderTrackedFeatures(@NotNull ProcessingStatus status, boolean voResult) {
        if (voResult) {
            // Write on vo (output) buffered image
            Graphics2D g2 = status.frame().vo().buffered().createGraphics();

            // Draw active tracks
            if (context.settings().tracker().showActiveTracks()) {
                for (Point2D_F64 p : status.tracking().trackInliers()) {
                    VisualizeFeatures.drawPoint(g2, (int) p.getX(), (int) p.getY(), Color.blue);
                }
            }

            // Draw new tracks
            if (context.settings().tracker().showNewTracks()) {
                for (Point2D_F64 p : status.tracking().trackNew()) {
                    VisualizeFeatures.drawPoint(g2, (int) p.getX(), (int) p.getY(), Color.green);
                }
            }

            g2.dispose();
        }
    }

    private void renderOutputVideo(@NotNull ProcessingStatus status) {
        // Vo process video rendering (output)
        guiState.videoView().showOutput(status.frame().vo().buffered());
    }

    private void renderInfoPanel(@NotNull ProcessingStatus status, @NotNull ProcessingParameters params,
                                 boolean voResult) {
        renderMainInfo(status, params, voResult);
    }

    private void renderMainInfo(@NotNull ProcessingStatus status, @NotNull ProcessingParameters params,
                                boolean voResult) {
        // Freeze core data + device fps for AWT; the info view owns the presentation.
        var sourceType = params.frozenContext().settings().input().source();
        final var deviceFps = SourceType.Device.is(sourceType) ? context.state().device().getAverageFPS() : 0;
        var frozenStatus = status.deepClone();

        // Longer interval: carry current coords over as previous (for direction/distance deltas)
        if (status.fps().totalProcessed() % LONGER_RENDER_INTERVAL == 0) {
            status.prevTranslation(frozenStatus.translation().copy());
        }

        guiState.dashboardView().info().renderTelemetry(frozenStatus, params, deviceFps, voResult);
    }

    private void renderCharts(@NotNull ProcessingStatus status, @NotNull ProcessingParameters params,
                              boolean voResult) {
        if (!voResult) {
            return;
        }

        var chartType = params.frozenContext().settings().chart().type();
        var chartXZScale = context.settings().chart().scaleXZ();
        var chartYScale = context.settings().chart().scaleY();
        var frozenStatus = status.deepClone();

        // Frame-based altitude point only when the chart is frame-based (second-based is per-second)
        Double altitudeX = null;
        Double altitudeY = null;
        if (ChartType.YFrames.is(chartType)) {
            altitudeX = (double) frozenStatus.fps().totalProcessed();
            altitudeY = -frozenStatus.translation().getY();
        }

        guiState.dashboardView().trajectory().plotFrame(
                frozenStatus.translation().getX(), frozenStatus.translation().getZ(),
                altitudeX, altitudeY, chartXZScale, chartYScale);
    }
}
