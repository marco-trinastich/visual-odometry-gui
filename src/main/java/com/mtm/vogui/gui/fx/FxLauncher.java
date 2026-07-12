/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx;

import com.mtm.vogui.gui.UiLauncher;
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
        Application.launch(FxApplication.class, args);
    }

    @Override
    public void terminate() {
        Platform.exit();
    }
}
