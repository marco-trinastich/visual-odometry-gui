/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui;

import com.mtm.vogui.core.rendering.RenderSink;
import com.mtm.vogui.gui.fx.FxLauncher;
import com.mtm.vogui.gui.fx.rendering.FxRenderSink;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.gui.swing.SwingLauncher;
import com.mtm.vogui.gui.swing.rendering.SwingRenderSink;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.config.Config;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Composition root of the UI layer: selects the active toolkit from {@code config.ui} and
 * produces its {@link UiLauncher} and {@link RenderSink}. The toolkit-specific classes are
 * plain objects, not beans: only the selected toolkit is ever instantiated, and launchers
 * resolve their own dependencies at launch time.
 */
@ApplicationScoped
public class UiBootstrap {

    @Produces
    @ApplicationScoped
    UiLauncher uiLauncher(Config config) {
        return switch (config.ui()) {
            case JavaFx -> new FxLauncher();
            case Swing -> new SwingLauncher();
        };
    }

    @Produces
    @ApplicationScoped
    RenderSink renderSink(Config config, AppContext context, GuiState guiState) {
        return switch (config.ui()) {
            case JavaFx -> new FxRenderSink(guiState);
            case Swing -> new SwingRenderSink(context);
        };
    }
}
