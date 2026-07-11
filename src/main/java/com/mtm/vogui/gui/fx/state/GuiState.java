/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.state;

import com.mtm.vogui.models.enums.gui.AppStatus;
import jakarta.inject.Singleton;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Observable state of the JavaFX GUI, written by {@code fx.rendering.FxRenderSink} (fed by the
 * core through {@code RenderSink}) and bound by the views. Views never talk to the core directly.
 * <p>
 * JavaFX property pattern is hand-written on purpose: Lombok (especially the project's fluent
 * accessors) does not produce the {@code xxxProperty()} convention bindings rely on.
 * All writes must happen on the FX Application Thread.
 */
@Singleton
public class GuiState {

    private final ObjectProperty<AppStatus> appStatus = new SimpleObjectProperty<>(AppStatus.Ready);

    public ObjectProperty<AppStatus> appStatusProperty() {
        return appStatus;
    }
}
