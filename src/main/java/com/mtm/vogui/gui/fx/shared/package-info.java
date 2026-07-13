/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

/**
 * Pure, reusable, app-blind JavaFX helpers shared across the {@code features} packages, mirroring
 * {@code gui.swing.shared}. Members here must not import {@code AppContext}, settings,
 * {@link com.mtm.vogui.gui.fx.state.GuiState} or the {@code RenderSink}: they take injected
 * callbacks/observable values so each feature wires them and the member stays generic.
 * Feature-specific controls colocate with their feature under {@code features}, not here.
 * <p>
 * Organised by kind into sub-packages (as {@code gui.swing.shared} is):
 * {@code components} (reusable controls, e.g. the video viewport) and {@code converters}
 * (JavaFX {@code StringConverter}s for the model's display contracts). More sub-packages land as
 * shared parts are factored out while the {@code features} are migrated.
 */
package com.mtm.vogui.gui.fx.shared;
