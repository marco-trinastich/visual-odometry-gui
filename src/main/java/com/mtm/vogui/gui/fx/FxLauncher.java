/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx;

import com.mtm.vogui.gui.UiLauncher;
import com.mtm.vogui.gui.fx.utils.FxUtils;
import javafx.application.Application;
import javafx.application.Platform;

/**
 * Boots the JavaFX UI. {@code Application.launch} blocks until {@code Platform.exit()} or the
 * last window closes; it must run AFTER the Quarkus container is up, so CDI is available inside
 * {@code FxApplication.start}. Not a bean: built by {@code gui.UiBootstrap} only when active.
 */
public class FxLauncher implements UiLauncher {

    @Override
    public void launchAndWait(String... args) {
        FxUtils.suppressUnnamedModuleWarning();
        // Blocks until the FX runtime shuts down (window X, menu Exit, dock/Cmd+Q -> Platform.exit).
        Application.launch(FxApplication.class, args);
        // FX left non-daemon threads (and quarkus:dev would just drop to its restart prompt), so force
        // the process down; the shutdown hook still runs (ShutdownEvent autosave).
        System.exit(0);
    }

    @Override
    public void terminate() {
        Platform.exit();
    }
}
