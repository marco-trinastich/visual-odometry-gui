/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui;

import com.mtm.vogui.core.rendering.RenderSink;
import com.mtm.vogui.gui.fx.rendering.FxRenderSink;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.gui.swing.rendering.SwingRenderSink;
import com.mtm.vogui.models.context.AppContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Selects the active {@link RenderSink} implementation from the {@code vogui.ui} property
 * ({@code javafx} default, {@code swing} for the legacy UI during the migration).
 * The sink implementations are plain classes, not beans: only the selected one is ever built.
 */
@ApplicationScoped
public class RenderSinkProducer {

    public final static String UI_PROPERTY = "vogui.ui";
    public final static String UI_SWING = "swing";

    @Produces
    @ApplicationScoped
    RenderSink renderSink(@ConfigProperty(name = UI_PROPERTY, defaultValue = "javafx") String ui,
                          AppContext context, GuiState guiState) {
        return UI_SWING.equalsIgnoreCase(ui) ? new SwingRenderSink(context) : new FxRenderSink(guiState);
    }
}
