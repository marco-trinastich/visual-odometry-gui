/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.rendering;

import com.mtm.vogui.core.rendering.RenderSink;
import com.mtm.vogui.gui.fx.utils.FxUtils;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.interfaces.Resolution;
import jakarta.enterprise.inject.spi.CDI;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

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

    private final GuiState guiState;
    private final Consumer<AppStatus> coalescedAppStatus;

    public FxRenderSink() {
        this.guiState = CDI.current().select(GuiState.class).get();
        this.coalescedAppStatus = FxUtils.coalescedFxConsumer(status -> this.guiState.appStatusProperty().set(status));
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
        // TODO Fase 2: video panels
    }

    // Per-frame rendering

    @Override
    public void renderVO(ProcessingStatus status, ProcessingParameters params, boolean voResult) {
        // TODO Fase 2/3: video, info, charts
    }

    @Override
    public void renderInputVideo(BufferedImage image) {
        // TODO Fase 2: video panels
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
        // TODO Fase 2: settings form (with bindings this becomes automatic)
    }

    @Override
    public void devicePathChanged(DevicePath devicePath) {
        // TODO Fase 2: settings form (with bindings this becomes automatic)
    }

    @Override
    public void recentPathUsed(RecentPathTarget target, PathSettings pathSettings, String usedPath) {
        // TODO Fase 2: settings form (with bindings this becomes automatic)
    }

    @Override
    public void kltPyramidLevelsChanged(int pyramidLevels) {
        // TODO Fase 2: settings form (with bindings this becomes automatic)
    }
}
