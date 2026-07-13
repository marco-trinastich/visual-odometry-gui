/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.telemetry.framerate;

import com.mtm.vogui.gui.fx.features.sidebar.telemetry.ui.TelemetryUi;
import com.mtm.vogui.gui.fx.state.CurrentFps;
import com.mtm.vogui.gui.fx.state.Telemetry;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.utilities.CommonUtils;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Framerate section (humble view): a compact Input/VO × Current/Average matrix, plus the buffer-load
 * readout embedded below (buffer and framerate are the same pipeline story — does processing keep up
 * with capture?). Averages come from the per-frame {@link Telemetry} snapshot; the instantaneous
 * current rates arrive on their own (per-second) {@link CurrentFps} channel; the buffer row is shown
 * only while the core reports a buffer status (device/camera mode).
 */
public class FpsInfoView {

    private final Label inputCurrent = TelemetryUi.value();
    private final Label inputAverage = TelemetryUi.value();
    private final Label voCurrent = TelemetryUi.value();
    private final Label voAverage = TelemetryUi.value();

    private final BufferInfoView bufferView = new BufferInfoView();

    private final Region root;

    public FpsInfoView() {
        GridPane grid = TelemetryUi.grid();
        grid.add(TelemetryUi.caption("Input"), 1, 0);
        grid.add(TelemetryUi.caption("VO"), 2, 0);
        grid.add(TelemetryUi.caption("Current"), 0, 1);
        grid.add(inputCurrent, 1, 1);
        grid.add(voCurrent, 2, 1);
        grid.add(TelemetryUi.caption("Average"), 0, 2);
        grid.add(inputAverage, 1, 2);
        grid.add(voAverage, 2, 2);

        // Buffer sits inside the framerate section, header-less; hidden until a status arrives.
        setBufferVisible(false);
        this.root = TelemetryUi.section("Framerate", new VBox(8, grid, bufferView.node()));
    }

    public Region node() {
        return this.root;
    }

    /** Average rates from the per-frame snapshot. */
    public void render(Telemetry telemetry) {
        FpsStatus fps = telemetry.status().fps();
        inputAverage.setText(fps(fps.inputAverageFPS()));
        voAverage.setText(fps(fps.averageFPS()));
    }

    /** Instantaneous rates from the per-second channel. */
    public void renderCurrent(CurrentFps current) {
        inputCurrent.setText(fps(current.inputCurrentFps()));
        voCurrent.setText(fps(current.voCurrentFps()));
    }

    /** Buffer load (device/camera mode); a {@code null} status hides the buffer row. */
    public void renderBuffer(BufferStatus buffer) {
        if (buffer == null) {
            setBufferVisible(false);
            return;
        }
        setBufferVisible(true);
        bufferView.render(buffer);
    }

    private void setBufferVisible(boolean visible) {
        bufferView.node().setVisible(visible);
        bufferView.node().setManaged(visible);
    }

    private static String fps(double value) {
        return CommonUtils.roundBigDecimal(value, 2) + " fps";
    }
}
