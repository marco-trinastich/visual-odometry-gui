/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.state;

import com.mtm.vogui.gui.swing.features.controlpanel.ControlPanelView;
import com.mtm.vogui.gui.swing.features.dashboard.DashboardView;
import com.mtm.vogui.gui.swing.features.video.VideoView;
import io.quarkus.arc.Unremovable;
import jakarta.inject.Singleton;
import lombok.Data;

/**
 * Shared state of the Swing GUI (the Swing counterpart of {@code gui.fx.state.GuiState}): the
 * feature window facades. Populated by {@code SwingApplication} at startup, consumed by
 * {@code SwingRenderSink} and the toolbar commands - all through view intents, never raw widgets.
 * <p>
 * {@code @Unremovable}: resolved only programmatically ({@code CDI.current()} in
 * {@code SwingLauncher}/{@code SwingRenderSink}), so Arc sees no injection point and would
 * otherwise drop the bean at build time.
 */
@Data
@Singleton
@Unremovable
public class GuiState {

    // Control-panel window facade (settings + toolbar; owns the frame and app-level dialogs)
    private ControlPanelView controlPanelView;

    // Dashboard window facade (trajectory charts + telemetry output window)
    private DashboardView dashboardView;

    // Video feature view (input preview + vo output frames)
    private VideoView videoView;
}
