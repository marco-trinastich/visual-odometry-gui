/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.state;

/**
 * Immutable snapshot of the instantaneous framerates published on {@link GuiState#currentFpsProperty()}
 * (roughly once per second, a different cadence from the per-frame {@link Telemetry}). The average rates
 * ride along in {@link Telemetry}'s frozen fps status; these are the live "current" values only.
 *
 * @param inputCurrentFps the input source's current framerate (0 for file sources with no device clock)
 * @param voCurrentFps    the visual-odometry processing current framerate
 */
public record CurrentFps(double inputCurrentFps, double voCurrentFps) {
}
