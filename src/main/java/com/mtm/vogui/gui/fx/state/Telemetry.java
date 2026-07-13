/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.state;

import com.mtm.vogui.models.core.processing.ProcessingStatus;

/**
 * Immutable per-frame telemetry snapshot published on {@link GuiState#telemetryProperty()} by
 * {@code fx.rendering.FxRenderSink} and consumed by the telemetry sections. The vo worker freezes a
 * deep-cloned {@link ProcessingStatus} (with the device input-fps already folded into its fps status)
 * so the read-only sections only format and display — no core access, no cloning. The set-and-forget
 * run inputs the status doesn't carry (calibration file, processed source) are frozen alongside it, so
 * the Processing section can show what this run is actually reading.
 *
 * @param status          deep-cloned processing status (translation, prevTranslation, rotation, tracking, fps)
 * @param calibrationFile the calibration file path in use (may be blank)
 * @param sourcePath      the processed source (video/folder path, or device path name; may be blank)
 */
public record Telemetry(ProcessingStatus status, String calibrationFile, String sourcePath) {
}
