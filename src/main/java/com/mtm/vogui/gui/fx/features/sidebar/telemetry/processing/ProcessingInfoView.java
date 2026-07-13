/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.telemetry.processing;

import java.io.File;

import com.mtm.vogui.gui.fx.features.sidebar.telemetry.ui.TelemetryUi;
import com.mtm.vogui.gui.fx.state.Telemetry;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.utilities.CommonUtils;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/**
 * Processing section (humble view): calibration file, processed source, processed/skipped frames and
 * elapsed time. Pure display — {@link #render(Telemetry)} formats the frozen snapshot, no state. The
 * calibration/source values are paths (potentially long), so the label shows only the file name to fit
 * the narrow rail and a tooltip carries the full path; the tooltip is created once and its text updated
 * (never re-installed).
 */
public class ProcessingInfoView {

    private final Label calibration = TelemetryUi.value();
    private final Label source = TelemetryUi.value();
    private final Label frames = TelemetryUi.value();
    private final Label elapsed = TelemetryUi.value();

    private final Tooltip calibrationTip = new Tooltip();
    private final Tooltip sourceTip = new Tooltip();

    private final Region root;

    public ProcessingInfoView() {
        calibration.setTooltip(calibrationTip);
        source.setTooltip(sourceTip);

        GridPane grid = TelemetryUi.grid();
        TelemetryUi.row(grid, 0, "Calibration", calibration);
        TelemetryUi.row(grid, 1, "Source", source);
        TelemetryUi.row(grid, 2, "Frames", frames);
        TelemetryUi.row(grid, 3, "Elapsed", elapsed);
        this.root = TelemetryUi.section("Processing", grid);
    }

    public Region node() {
        return this.root;
    }

    public void render(Telemetry telemetry) {
        setPath(calibration, calibrationTip, telemetry.calibrationFile());
        setPath(source, sourceTip, telemetry.sourcePath());

        FpsStatus fps = telemetry.status().fps();
        int processed = fps.totalProcessed();
        int skipped = fps.totalFrames() - processed;
        frames.setText(processed + " processed · " + skipped + " skipped");
        elapsed.setText(CommonUtils.getFormattedTime(fps.totalSeconds()));
    }

    /** Label shows just the file name (fits the narrow rail); the always-on tooltip keeps the full path. */
    private static void setPath(Label label, Tooltip tooltip, String path) {
        boolean present = path != null && !path.isBlank();
        label.setText(present ? new File(path).getName() : TelemetryUi.EMPTY);
        tooltip.setText(present ? path : TelemetryUi.EMPTY);
    }
}
