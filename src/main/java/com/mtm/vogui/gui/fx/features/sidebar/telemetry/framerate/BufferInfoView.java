/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.telemetry.framerate;

import atlantafx.base.theme.Styles;

import com.mtm.vogui.gui.fx.features.sidebar.telemetry.ui.TelemetryUi;
import com.mtm.vogui.models.core.concurrency.AwaitableBuffer;
import com.mtm.vogui.models.core.integration.BufferStatus;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Buffer-load readout (humble view): a progress bar plus a colour-coded state (stable / buffering /
 * overrun / unavailable) and the load amount. Header-less on purpose — it is embedded under the
 * Framerate section (buffer and throughput are the same pipeline story) and shown/hidden as a unit
 * by that section (buffer status present vs {@code null}). Pure display.
 */
class BufferInfoView {

    // Pseudo-max for an unbounded buffer, so the bar shows a sane fraction instead of a flat line.
    private static final double INFINITE_SCALE = 10_000d;

    private final ProgressBar progress = new ProgressBar(0);
    private final Label state = TelemetryUi.value();
    private final Label amount = TelemetryUi.caption("");

    private final Region root;

    BufferInfoView() {
        progress.setMaxWidth(Double.MAX_VALUE);
        HBox line = new HBox(8, state, amount);
        VBox body = new VBox(4, progress, line);
        HBox.setHgrow(progress, Priority.ALWAYS);
        this.root = body;
    }

    Region node() {
        return this.root;
    }

    void render(BufferStatus buffer) {
        boolean infinite = buffer.maxBufferItems() == AwaitableBuffer.INFINITE_BUFFER;
        double max = infinite ? INFINITE_SCALE : buffer.maxBufferItems();
        progress.setProgress(max <= 0 ? 0 : Math.min(1d, buffer.bufferItems() / max));

        state.getStyleClass().removeAll(Styles.SUCCESS, Styles.DANGER, Styles.WARNING);
        if (buffer.maxBufferItems() == 0) {
            state.setText("Unavailable");
            state.getStyleClass().add(Styles.DANGER);
        } else if (buffer.bufferItems() == 0) {
            state.setText("Stable");
            state.getStyleClass().add(Styles.SUCCESS);
        } else if (buffer.bufferItems() >= buffer.maxBufferItems()) {
            state.setText("Overrun");
            state.getStyleClass().add(Styles.DANGER);
        } else {
            state.setText("Buffering");
            state.getStyleClass().add(Styles.WARNING);
        }

        amount.setText(buffer.maxBufferItems() == 0
                ? ""
                : buffer.bufferSize() + " / " + (infinite ? "∞" : buffer.maxBufferSize()));
    }
}
