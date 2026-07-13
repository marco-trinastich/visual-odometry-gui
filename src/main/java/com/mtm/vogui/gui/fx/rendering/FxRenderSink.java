/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.rendering;

import boofcv.gui.feature.VisualizeFeatures;
import com.mtm.vogui.core.rendering.RenderSink;
import com.mtm.vogui.gui.fx.utils.FxUtils;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.interfaces.Resolution;
import georegression.struct.point.Point2D_F64;
import jakarta.enterprise.inject.spi.CDI;
import javafx.application.Platform;
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
 * Migration status: dialogs and app status are live; video (Fase 2), charts/info (Fase 3)
 * land with their panels.
 */
public class FxRenderSink implements RenderSink {

    private final AppContext context;
    private final GuiState guiState;
    private final Consumer<AppStatus> coalescedAppStatus;
    private final Consumer<Image> coalescedInputFrame;
    private final Consumer<Image> coalescedOutputFrame;
    private final Consumer<Integer> coalescedKltPyramidLevels;

    public FxRenderSink() {
        var cdi = CDI.current();
        this.context = cdi.select(AppContext.class).get();
        this.guiState = cdi.select(GuiState.class).get();
        this.coalescedAppStatus = FxUtils.coalescedFxConsumer(status -> guiState.appStatusProperty().set(status));
        this.coalescedInputFrame = FxUtils.coalescedFxConsumer(image -> guiState.inputFrameProperty().set(image));
        this.coalescedOutputFrame = FxUtils.coalescedFxConsumer(image -> guiState.outputFrameProperty().set(image));
        this.coalescedKltPyramidLevels =
                FxUtils.coalescedFxConsumer(levels -> guiState.kltPyramidLevelsProperty().set(levels));
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
        // TODO Fase 3: derive from the charts state; until then every run starts from chart id 0
        return 0;
    }

    // App status

    @Override
    public void renderAppStatus(AppStatus appStatus) {
        coalescedAppStatus.accept(appStatus);
    }

    // Processing lifecycle (charts/video land in Fase 2/3)

    @Override
    public void renderStartPoint(ProcessingParameters params) {
        // TODO Fase 3: charts/tracked points
    }

    @Override
    public void renderEndPoint(ProcessingParameters params) {
        // TODO Fase 3: charts/tracked points
    }

    @Override
    public void renderClearAllPoints() {
        // TODO Fase 3: charts/tracked points
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
        // TODO Fase 3: info panel + charts
    }

    @Override
    public void renderInputVideo(BufferedImage image) {
        // Mirror the Swing sink: show the preview only when enabled, otherwise clear the viewport.
        coalescedInputFrame.accept(context.settings().input().inputPreview() ? FxUtils.toFxImage(image) : null);
    }

    @Override
    public void renderCurrentFps(FpsStatus fpsStatus, ProcessingStatus status, ProcessingParameters params) {
        // TODO Fase 3: info panel
    }

    @Override
    public void renderBufferStatus(BufferStatus bufferStatus) {
        // TODO Fase 3: info panel
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
}
