/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.settings.image;

import atlantafx.base.controls.ToggleSwitch;
import com.mtm.vogui.gui.fx.shared.behaviors.Combos;
import com.mtm.vogui.gui.fx.shared.behaviors.Spinners;
import com.mtm.vogui.gui.fx.shared.converters.ResolutionStringConverter;
import com.mtm.vogui.gui.fx.shared.converters.WithValueStringConverter;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.settings.ImageTypeDescriptor;
import com.mtm.vogui.models.interfaces.Resolution;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;

/**
 * Thin controller of the Image settings section: builds its {@link ImageSettingsViewModel} and binds
 * the FXML controls to it (MVVM rule — no logic beyond binding/delegation). Dependent {@code @Unremovable}
 * bean resolved by the CDI controller factory, like every FXML controller in the FX UI.
 */
@Dependent
@Unremovable
public class ImageSettingsController {

    @Inject
    AppContext context;

    @FXML
    private ComboBox<ImageTypeDescriptor> imageTypeCombo;

    @FXML
    private ToggleSwitch resizeToggle;

    @FXML
    private ComboBox<Resolution> resolutionCombo;

    @FXML
    private ToggleSwitch internalPreviewToggle;

    @FXML
    private ToggleSwitch frameSkipToggle;

    @FXML
    private Spinner<Integer> frameSkipSpinner;

    private ImageSettingsViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new ImageSettingsViewModel(() -> context.settings().image());

        imageTypeCombo.setItems(viewModel.imageTypes());
        imageTypeCombo.setConverter(new WithValueStringConverter<ImageTypeDescriptor>());
        imageTypeCombo.valueProperty().bindBidirectional(viewModel.descriptorProperty());

        resizeToggle.selectedProperty().bindBidirectional(viewModel.resizeProperty());

        resolutionCombo.setItems(viewModel.resizeResolutions());
        resolutionCombo.setConverter(new ResolutionStringConverter());
        resolutionCombo.valueProperty().bindBidirectional(viewModel.resolutionProperty());
        resolutionCombo.disableProperty().bind(resizeToggle.selectedProperty().not());
        // Commit typed text on focus-out (editable ComboBoxes otherwise only commit on Enter).
        Combos.commitOnFocusLost(resolutionCombo);

        internalPreviewToggle.selectedProperty().bindBidirectional(viewModel.internalImagePreviewProperty());

        frameSkipToggle.selectedProperty().bindBidirectional(viewModel.frameSkipEnabledProperty());

        SpinnerValueFactory.IntegerSpinnerValueFactory frameSkipFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE,
                        viewModel.frameSkipValueProperty().get());
        frameSkipSpinner.setValueFactory(frameSkipFactory);
        frameSkipFactory.valueProperty().bindBidirectional(viewModel.frameSkipValueProperty().asObject());
        frameSkipSpinner.disableProperty().bind(frameSkipToggle.selectedProperty().not());
        // Commit typed text on focus-out (editable Spinners otherwise only commit on Enter).
        Spinners.commitOnFocusLost(frameSkipSpinner);
    }

    /** Re-syncs the section from the domain after a settings load/reset. */
    public void reload() {
        viewModel.load();
    }
}
