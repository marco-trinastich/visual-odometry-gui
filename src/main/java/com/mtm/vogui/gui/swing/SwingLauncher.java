/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing;

import com.mtm.vogui.core.Core;
import com.mtm.vogui.gui.UiLauncher;
import com.mtm.vogui.gui.swing.state.GuiState;
import com.mtm.vogui.models.context.AppContext;
import io.quarkus.runtime.Quarkus;
import jakarta.enterprise.inject.spi.CDI;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Boots the legacy Swing UI. Dependencies are resolved programmatically at launch time
 * (same pattern as {@code FxApplication.start}): the container is up before any launcher runs.
 * Not a bean: built by {@code gui.UiBootstrap} only when the Swing UI is active.
 */
public class SwingLauncher implements UiLauncher {

    @Override
    public void launchAndWait(String... args) {
        var cdi = CDI.current();
        var application = new SwingApplication(
                cdi.select(AppContext.class).get(),
                cdi.select(Core.class).get(),
                cdi.select(GuiState.class).get(),
                ConfigProvider.getConfig().getValue("quarkus.application.version", String.class));

        application.start();
        Quarkus.waitForExit();
    }

    @Override
    public void terminate() {
        // Nothing to tear down: waitForExit() unblocks as soon as Quarkus stops
    }
}
