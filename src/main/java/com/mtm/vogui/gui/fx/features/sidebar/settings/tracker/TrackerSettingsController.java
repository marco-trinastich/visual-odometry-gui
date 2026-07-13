/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.settings.tracker;

import atlantafx.base.controls.ToggleSwitch;
import com.mtm.vogui.gui.fx.shared.behaviors.Spinners;
import com.mtm.vogui.gui.fx.shared.converters.WithValueStringConverter;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.settings.TrackerType;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.property.IntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.GridPane;

/**
 * Thin controller of the Tracker settings section: builds its {@link TrackerSettingsViewModel} and
 * binds the FXML controls to it (MVVM rule — no logic beyond binding/delegation). Also bridges the
 * one render-state signal this section reacts to: the KLT pyramid levels the core heals at capture
 * time, published on {@link GuiState}, are pushed into the ViewModel here (keeping the ViewModel
 * GuiState-free like the other sections). Dependent {@code @Unremovable} bean, resolved by the CDI
 * controller factory.
 */
@Dependent
@Unremovable
public class TrackerSettingsController {

    @Inject
    AppContext context;

    @Inject
    GuiState guiState;

    @FXML
    private ComboBox<TrackerType> trackerTypeCombo;

    @FXML
    private GridPane kltGroup;

    @FXML
    private Spinner<Integer> templateRadiusSpinner;

    @FXML
    private Spinner<Integer> pyramidLevelsSpinner;

    @FXML
    private Spinner<Integer> maxFeaturesSpinner;

    @FXML
    private Spinner<Integer> radiusSpinner;

    @FXML
    private Spinner<Double> thresholdSpinner;

    @FXML
    private GridPane surfGroup;

    @FXML
    private Spinner<Integer> maxFeaturesPerScaleSpinner;

    @FXML
    private Spinner<Integer> extractRadiusSpinner;

    @FXML
    private Spinner<Integer> initialSampleSizeSpinner;

    @FXML
    private ToggleSwitch showActiveTracksToggle;

    @FXML
    private ToggleSwitch showNewTracksToggle;

    private TrackerSettingsViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new TrackerSettingsViewModel(() -> context.settings().tracker());

        trackerTypeCombo.setItems(viewModel.trackerTypes());
        trackerTypeCombo.setConverter(new WithValueStringConverter<TrackerType>());
        trackerTypeCombo.valueProperty().bindBidirectional(viewModel.typeProperty());

        // KLT group: shown for KLT-based types, greyed out for Default.
        kltGroup.visibleProperty().bind(viewModel.kltVisible());
        kltGroup.managedProperty().bind(viewModel.kltVisible());
        kltGroup.disableProperty().bind(viewModel.kltDisabled());
        bindIntSpinner(templateRadiusSpinner, viewModel.templateRadiusProperty());
        bindIntSpinner(pyramidLevelsSpinner, viewModel.pyramidLevelsProperty());
        bindIntSpinner(maxFeaturesSpinner, viewModel.maxFeaturesProperty());
        bindIntSpinner(radiusSpinner, viewModel.radiusProperty());
        bindThresholdSpinner();

        // SURF group: shown for SURF-based types.
        surfGroup.visibleProperty().bind(viewModel.surfVisible());
        surfGroup.managedProperty().bind(viewModel.surfVisible());
        bindIntSpinner(maxFeaturesPerScaleSpinner, viewModel.maxFeaturesPerScaleProperty());
        bindIntSpinner(extractRadiusSpinner, viewModel.extractRadiusProperty());
        bindIntSpinner(initialSampleSizeSpinner, viewModel.initialSampleSizeProperty());

        showActiveTracksToggle.selectedProperty().bindBidirectional(viewModel.showActiveTracksProperty());
        showNewTracksToggle.selectedProperty().bindBidirectional(viewModel.showNewTracksProperty());

        // Render-state → ViewModel: reflect the KLT pyramid levels the core heals at capture time.
        guiState.kltPyramidLevelsProperty().addListener((_, _, levels) -> {
            if (levels.intValue() > 0) {
                viewModel.pyramidLevelsProperty().set(levels.intValue());
            }
        });
    }

    /** Re-syncs the section from the domain after a settings load/reset. */
    public void reload() {
        viewModel.load();
    }

    private static void bindIntSpinner(Spinner<Integer> spinner, IntegerProperty property) {
        // Counts/radii/levels are all >= 1; the core additionally heals pyramid levels at capture time.
        var factory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, property.get());
        spinner.setValueFactory(factory);
        Spinners.bindBidirectional(factory, property);
        // Commit typed text on focus-out (editable Spinners otherwise only commit on Enter).
        Spinners.commitOnFocusLost(spinner);
    }

    private void bindThresholdSpinner() {
        var factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(
                0.0, Double.MAX_VALUE, viewModel.thresholdProperty().get(), 0.1);
        thresholdSpinner.setValueFactory(factory);
        Spinners.bindBidirectional(factory, viewModel.thresholdProperty());
        Spinners.commitOnFocusLost(thresholdSpinner);
    }
}
