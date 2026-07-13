/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.settings.chart;

import com.mtm.vogui.models.context.settings.chart.ChartSettings;
import com.mtm.vogui.models.enums.settings.ChartType;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Supplier;

/**
 * ViewModel of the Chart settings section (hand-rolled MVVM, twin of the settings-bearing part of the
 * Swing {@code ChartSettingsView}). Exposes the altitude-basis chart type and the two axis scales as
 * JavaFX properties, live-committing each into the domain {@link ChartSettings}.
 * <p>
 * Scope note: the Swing section also carries action buttons (Apply / Origin / Last / 3D points) that
 * drive the trajectory chart through {@code TrajectoryView}. Those are intentionally absent here — the
 * JavaFX trajectory/dashboard is not migrated yet, so there is nothing for them to act on. They land
 * with the FX trajectory chart, alongside re-applying these scales to it.
 * No Lombok (the {@code xxxProperty()} convention is hand-written).
 */
public class ChartSettingsViewModel {

    // Re-resolved on every load(): a settings load swaps the sub-settings object (see AppContext).
    private final Supplier<ChartSettings> settingsSupplier;
    private ChartSettings settings;

    private final ObjectProperty<ChartType> type = new SimpleObjectProperty<>();
    private final DoubleProperty scaleXZ = new SimpleDoubleProperty();
    private final DoubleProperty scaleY = new SimpleDoubleProperty();

    private final ObservableList<ChartType> chartTypes =
            FXCollections.observableArrayList(ChartType.values());

    public ChartSettingsViewModel(Supplier<ChartSettings> settingsSupplier) {
        this.settingsSupplier = settingsSupplier;
        load();

        // Live commit into the domain (fluent accessors).
        type.addListener((_, _, value) -> settings.type(value));
        scaleXZ.addListener((_, _, value) -> settings.scaleXZ(value.doubleValue()));
        scaleY.addListener((_, _, value) -> settings.scaleY(value.doubleValue()));
    }

    /** Re-reads every property from the domain (after a settings load/reset). */
    public void load() {
        settings = settingsSupplier.get();
        type.set(settings.type());
        scaleXZ.set(settings.scaleXZ());
        scaleY.set(settings.scaleY());
    }

    public ObjectProperty<ChartType> typeProperty() {
        return type;
    }

    public DoubleProperty scaleXZProperty() {
        return scaleXZ;
    }

    public DoubleProperty scaleYProperty() {
        return scaleY;
    }

    public ObservableList<ChartType> chartTypes() {
        return chartTypes;
    }
}
