/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.settings.chart;

import com.mtm.vogui.gui.fx.shared.behaviors.Spinners;
import com.mtm.vogui.gui.fx.shared.converters.WithValueStringConverter;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.settings.ChartType;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.DoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Thin controller of the Chart settings section: builds its {@link ChartSettingsViewModel} and binds
 * the FXML controls to it (MVVM rule — no logic beyond binding/delegation). Only the persisted
 * settings (altitude-basis type + the two axis scales) are wired; the trajectory-driving buttons land
 * with the FX trajectory chart (see {@link ChartSettingsViewModel}). Dependent {@code @Unremovable}
 * bean, resolved by the CDI controller factory.
 */
@Dependent
@Unremovable
public class ChartSettingsController {

    @Inject
    AppContext context;

    @FXML
    private ComboBox<ChartType> chartTypeCombo;

    @FXML
    private Spinner<Double> scaleXZSpinner;

    @FXML
    private Spinner<Double> scaleYSpinner;

    private ChartSettingsViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new ChartSettingsViewModel(() -> context.settings().chart());

        chartTypeCombo.setItems(viewModel.chartTypes());
        chartTypeCombo.setConverter(new WithValueStringConverter<ChartType>());
        chartTypeCombo.valueProperty().bindBidirectional(viewModel.typeProperty());

        bindScaleSpinner(scaleXZSpinner, viewModel.scaleXZProperty());
        bindScaleSpinner(scaleYSpinner, viewModel.scaleYProperty());
    }

    /** Re-syncs the section from the domain after a settings load/reset. */
    public void reload() {
        viewModel.load();
    }

    private static void bindScaleSpinner(Spinner<Double> spinner, DoubleProperty property) {
        // Scales are non-zero magnitudes (Swing enforced NumberConstraints.NotZero): a smallest-positive
        // floor keeps zero unreachable without clamping any realistic saved value.
        var factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                Double.MIN_VALUE, Double.MAX_VALUE, property.get(), 0.1);
        spinner.setValueFactory(factory);
        factory.valueProperty().bindBidirectional(property.asObject());
        // Commit typed text on focus-out so the scale applies as soon as the field is left — replacing
        // the Swing "Apply" button. Once the FX trajectory chart exists it re-applies live off the
        // committed value (no dedicated button).
        Spinners.commitOnFocusLost(spinner);
    }
}
