/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui;

import com.mtm.vogui.core.rendering.RenderSink;
import com.mtm.vogui.gui.fx.FxLauncher;
import com.mtm.vogui.gui.fx.rendering.FxRenderSink;
import com.mtm.vogui.gui.swing.SwingLauncher;
import com.mtm.vogui.gui.swing.rendering.SwingRenderSink;
import com.mtm.vogui.models.context.config.Config;
import com.mtm.vogui.models.enums.gui.UiToolkit;
import com.mtm.vogui.utilities.AnsiUtils;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Composition root of the UI layer: selects the active toolkit from {@code config.ui} and
 * produces its {@link UiLauncher} and {@link RenderSink}. The toolkit-specific classes are
 * plain objects, not beans: only the selected toolkit is ever instantiated, and launchers
 * and sinks resolve their own dependencies (including their toolkit's {@code GuiState})
 * at creation time.
 */
@ApplicationScoped
public class UiBootstrap {

    @Produces
    @ApplicationScoped
    UiLauncher uiLauncher(Config config) {
        UiToolkit ui = config.ui();
        Log.infof("UI: %s", AnsiUtils.boldColoured(ui.displayName(), ui.brandColour()));
        return switch (ui) {
            case JavaFx -> new FxLauncher();
            case Swing -> new SwingLauncher();
        };
    }

    @Produces
    @ApplicationScoped
    RenderSink renderSink(Config config) {
        return switch (config.ui()) {
            case JavaFx -> new FxRenderSink();
            case Swing -> new SwingRenderSink();
        };
    }
}
