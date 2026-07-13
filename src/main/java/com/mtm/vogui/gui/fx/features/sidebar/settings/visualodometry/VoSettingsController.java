/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.settings.visualodometry;

import com.mtm.vogui.gui.fx.shared.behaviors.Spinners;
import com.mtm.vogui.gui.fx.shared.converters.WithValueStringConverter;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.settings.VisualOdometryType;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;

/**
 * Thin controller of the Visual Odometry settings section: builds its {@link VoSettingsViewModel}
 * and binds the FXML controls to it (MVVM rule — no logic beyond binding/delegation). The VO type
 * combo selects which mono-plane variant group is shown; the not-implemented stereo/depth types
 * reveal the fallback placeholder instead. Dependent {@code @Unremovable} bean, resolved by the CDI
 * controller factory.
 */
@Dependent
@Unremovable
public class VoSettingsController {

    @Inject
    AppContext context;

    @FXML
    private ComboBox<VisualOdometryType> voTypeCombo;

    @FXML
    private GridPane monoInfinityGroup;

    @FXML
    private Spinner<Integer> infThresholdAddSpinner;

    @FXML
    private Spinner<Integer> infThresholdRetireSpinner;

    @FXML
    private Spinner<Double> infInlierPixelTolSpinner;

    @FXML
    private Spinner<Integer> infRansacIterationsSpinner;

    @FXML
    private GridPane monoOverheadGroup;

    @FXML
    private Spinner<Double> ovhCellSizeSpinner;

    @FXML
    private Spinner<Double> ovhMaxCellsPerPixelSpinner;

    @FXML
    private Spinner<Double> ovhMapHeightFractionSpinner;

    @FXML
    private Spinner<Double> ovhInlierGroundTolSpinner;

    @FXML
    private Spinner<Integer> ovhRansacIterationsSpinner;

    @FXML
    private Spinner<Integer> ovhThresholdRetireSpinner;

    @FXML
    private Spinner<Integer> ovhAbsoluteMinimumTracksSpinner;

    @FXML
    private Spinner<Double> ovhRespawnTrackFractionSpinner;

    @FXML
    private Spinner<Double> ovhRespawnCoverageFractionSpinner;

    @FXML
    private Label fallbackLabel;

    private VoSettingsViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new VoSettingsViewModel(() -> context.settings().visualOdometry());

        voTypeCombo.setItems(viewModel.visualOdometryTypes());
        voTypeCombo.setConverter(new WithValueStringConverter<VisualOdometryType>());
        voTypeCombo.valueProperty().bindBidirectional(viewModel.typeProperty());

        // Mono-plane-infinity group: shown for MonoPlaneInfinity + Default, greyed out for Default
        // (Default runs the same algorithm with standard, non-editable parameters).
        monoInfinityGroup.visibleProperty().bind(viewModel.monoInfinityVisible());
        monoInfinityGroup.managedProperty().bind(viewModel.monoInfinityVisible());
        monoInfinityGroup.disableProperty().bind(viewModel.monoInfinityDisabled());
        bindIntSpinner(infThresholdAddSpinner, viewModel.infThresholdAddProperty());
        bindIntSpinner(infThresholdRetireSpinner, viewModel.infThresholdRetireProperty());
        bindDoubleSpinner(infInlierPixelTolSpinner, viewModel.infInlierPixelTolProperty(), 0.1);
        bindIntSpinner(infRansacIterationsSpinner, viewModel.infRansacIterationsProperty());

        // Mono-plane-overhead group: shown for MonoPlaneOverhead.
        monoOverheadGroup.visibleProperty().bind(viewModel.monoOverheadVisible());
        monoOverheadGroup.managedProperty().bind(viewModel.monoOverheadVisible());
        bindDoubleSpinner(ovhCellSizeSpinner, viewModel.ovhCellSizeProperty(), 0.01);
        bindDoubleSpinner(ovhMaxCellsPerPixelSpinner, viewModel.ovhMaxCellsPerPixelProperty(), 1.0);
        bindDoubleSpinner(ovhMapHeightFractionSpinner, viewModel.ovhMapHeightFractionProperty(), 0.05);
        bindDoubleSpinner(ovhInlierGroundTolSpinner, viewModel.ovhInlierGroundTolProperty(), 0.1);
        bindIntSpinner(ovhRansacIterationsSpinner, viewModel.ovhRansacIterationsProperty());
        bindIntSpinner(ovhThresholdRetireSpinner, viewModel.ovhThresholdRetireProperty());
        bindIntSpinner(ovhAbsoluteMinimumTracksSpinner, viewModel.ovhAbsoluteMinimumTracksProperty());
        bindDoubleSpinner(ovhRespawnTrackFractionSpinner, viewModel.ovhRespawnTrackFractionProperty(), 0.05);
        bindDoubleSpinner(ovhRespawnCoverageFractionSpinner, viewModel.ovhRespawnCoverageFractionProperty(), 0.05);

        // Fallback placeholder: shown for the not-implemented stereo/depth types.
        fallbackLabel.visibleProperty().bind(viewModel.fallbackVisible());
        fallbackLabel.managedProperty().bind(viewModel.fallbackVisible());
    }

    /** Re-syncs the section from the domain after a settings load/reset. */
    public void reload() {
        viewModel.load();
    }

    private static void bindIntSpinner(Spinner<Integer> spinner, IntegerProperty property) {
        // Thresholds/iterations/track counts are all >= 0.
        var factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, property.get());
        spinner.setValueFactory(factory);
        factory.valueProperty().bindBidirectional(property.asObject());
        // Commit typed text on focus-out (editable Spinners otherwise only commit on Enter).
        Spinners.commitOnFocusLost(spinner);
    }

    private static void bindDoubleSpinner(Spinner<Double> spinner, DoubleProperty property, double step) {
        var factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.0, Double.MAX_VALUE, property.get(), step);
        spinner.setValueFactory(factory);
        factory.valueProperty().bindBidirectional(property.asObject());
        Spinners.commitOnFocusLost(spinner);
    }
}
