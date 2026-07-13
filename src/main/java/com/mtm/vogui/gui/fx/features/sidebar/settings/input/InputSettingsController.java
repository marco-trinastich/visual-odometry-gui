/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.settings.input;

import atlantafx.base.controls.ToggleSwitch;
import com.mtm.vogui.gui.fx.shared.behaviors.Combos;
import com.mtm.vogui.gui.fx.shared.components.RemovableListCells;
import com.mtm.vogui.gui.fx.shared.converters.DevicePathStringConverter;
import com.mtm.vogui.gui.fx.shared.converters.ResolutionStringConverter;
import com.mtm.vogui.gui.fx.shared.converters.WithValueStringConverter;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.enums.settings.DeviceType;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.models.interfaces.Resolution;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.converter.DefaultStringConverter;

import java.io.File;
import java.util.function.Consumer;

/**
 * Thin controller of the Input settings section: builds its {@link InputSettingsViewModel} and binds
 * the FXML controls to it (MVVM rule — no logic beyond binding/delegation). Wires the persisted input
 * controls, the calibration/video recent-path management (browse pickers + drop-down "✕" removal), the
 * discovery-backed device path/resolution combos, and the reflection of core-healed values published
 * on {@link GuiState}. Dependent {@code @Unremovable} bean, resolved by the CDI controller factory.
 */
@Dependent
@Unremovable
public class InputSettingsController {

    @Inject
    AppContext context;

    @Inject
    GuiState guiState;

    @FXML
    private ComboBox<String> calibrationCombo;

    @FXML
    private Button calibrationBrowse;

    @FXML
    private RadioButton videoRadio;

    @FXML
    private ComboBox<String> videoCombo;

    @FXML
    private MenuButton videoBrowse;

    @FXML
    private MenuItem videoBrowseFile;

    @FXML
    private MenuItem videoBrowseFolder;

    @FXML
    private RadioButton deviceRadio;

    @FXML
    private ComboBox<DeviceType> deviceTypeCombo;

    @FXML
    private Label pathLabel;

    @FXML
    private ComboBox<DevicePath> devicePathCombo;

    @FXML
    private Label resolutionLabel;

    @FXML
    private ComboBox<Resolution> deviceResolutionCombo;

    @FXML
    private VBox v4l4jGroup;

    @FXML
    private ToggleSwitch sustainFramerateToggle;

    @FXML
    private ToggleSwitch timeoutImageIOToggle;

    @FXML
    private ToggleSwitch keepFormatToggle;

    @FXML
    private ToggleSwitch fullResolutionPreviewToggle;

    @FXML
    private ToggleSwitch inputPreviewToggle;

    private InputSettingsViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new InputSettingsViewModel(() -> context.settings().input());

        // Calibration + video are editable recent-path combos: history in the drop-down, free text in
        // the editor. A DefaultStringConverter + commit-on-focus-out lets a typed path apply on leave;
        // each drop-down entry carries a "✕" to drop it from the recent-path history.
        bindPathCombo(calibrationCombo, viewModel.calibrationPaths(), viewModel.calibrationPathProperty(),
                viewModel::removeCalibrationPath);
        bindPathCombo(videoCombo, viewModel.videoPaths(), viewModel.videoPathProperty(),
                viewModel::removeVideoPath);

        // Browse pickers: a confirmed choice records the path in the history and selects it.
        calibrationBrowse.setOnAction(_ -> browse(calibrationCombo, "Open Calibration",
                new FileChooser.ExtensionFilter("YAML Camera Calibration (*.yaml, *.yml)", "*.yaml", "*.yml"),
                viewModel::pushCalibrationPath));
        // Video can be a single media file or a folder of images (the core's LoadFileImageSequence
        // path), so the picker offers both — FX has no combined files-and-directories dialog.
        videoBrowseFile.setOnAction(_ -> browse(videoCombo, "Open Video",
                new FileChooser.ExtensionFilter("Supported media (*.avi, *.mp4, *.mjpeg)", "*.avi", "*.mp4", "*.mjpeg"),
                viewModel::pushVideoPath));
        videoBrowseFolder.setOnAction(_ -> browseDirectory(videoCombo, "Open Image Sequence Folder",
                viewModel::pushVideoPath));

        // Source: Video/Device radios kept in sync with the ViewModel's source property both ways.
        ToggleGroup sourceGroup = new ToggleGroup();
        videoRadio.setToggleGroup(sourceGroup);
        deviceRadio.setToggleGroup(sourceGroup);
        syncSourceRadios();
        viewModel.sourceProperty().addListener((_, _, _) -> syncSourceRadios());
        videoRadio.selectedProperty().addListener((_, _, selected) -> {
            if (selected) {
                viewModel.sourceProperty().set(SourceType.Video);
            }
        });
        deviceRadio.selectedProperty().addListener((_, _, selected) -> {
            if (selected) {
                viewModel.sourceProperty().set(SourceType.Device);
            }
        });
        // Mirror the source onto GuiState so the toolbar's Device-only timed button can react.
        guiState.inputSourceProperty().bind(viewModel.sourceProperty());

        // Device driver type.
        deviceTypeCombo.setItems(viewModel.deviceTypes());
        deviceTypeCombo.setConverter(new WithValueStringConverter<DeviceType>());
        deviceTypeCombo.valueProperty().bindBidirectional(viewModel.deviceTypeProperty());

        // Device path + resolution: editable combos populated from discovery; typed text parses back
        // into a DevicePath / CustomResolution, committed on focus-out.
        devicePathCombo.setItems(viewModel.devicePaths());
        devicePathCombo.setConverter(new DevicePathStringConverter());
        devicePathCombo.valueProperty().bindBidirectional(viewModel.devicePathProperty());
        Combos.commitOnFocusLost(devicePathCombo);

        deviceResolutionCombo.setItems(viewModel.deviceResolutions());
        deviceResolutionCombo.setConverter(new ResolutionStringConverter());
        deviceResolutionCombo.valueProperty().bindBidirectional(viewModel.deviceResolutionProperty());
        Combos.commitOnFocusLost(deviceResolutionCombo);

        // Core-healed values (published on GuiState by FxRenderSink at capture time) reflected back.
        guiState.healedDevicePathProperty().addListener((_, _, path) -> viewModel.reflectDevicePath(path));
        guiState.healedDeviceResolutionProperty().addListener(
                (_, _, resolution) -> viewModel.reflectDeviceResolution(resolution));
        guiState.recentPathUsedProperty().addListener((_, _, used) -> {
            if (used != null) {
                switch (used.target()) {
                    case Calibration -> viewModel.reflectRecentCalibration(used.usedPath());
                    case VideoSource -> viewModel.reflectRecentVideo(used.usedPath());
                }
            }
        });

        // Device adjustments (V4L4J).
        sustainFramerateToggle.selectedProperty().bindBidirectional(viewModel.sustainFramerateProperty());
        timeoutImageIOToggle.selectedProperty().bindBidirectional(viewModel.timeoutImageIOProperty());
        keepFormatToggle.selectedProperty().bindBidirectional(viewModel.keepFormatProperty());

        // Preview flags.
        fullResolutionPreviewToggle.selectedProperty().bindBidirectional(viewModel.fullResolutionPreviewProperty());
        inputPreviewToggle.selectedProperty().bindBidirectional(viewModel.inputPreviewProperty());

        // Enablement follows the chosen source: video path with Video, device driver with Device.
        videoCombo.disableProperty().bind(viewModel.videoSourceSelected().not());
        videoBrowse.disableProperty().bind(viewModel.videoSourceSelected().not());
        deviceTypeCombo.disableProperty().bind(viewModel.deviceSourceSelected().not());

        // Device path/resolution are only meaningful for a device source: revealed with Device. They
        // share the source grid (for column alignment), so each row's cells reveal individually — an
        // unmanaged row collapses, keeping the layout tight.
        bindVisible(pathLabel, viewModel.deviceSourceSelected());
        bindVisible(devicePathCombo, viewModel.deviceSourceSelected());
        bindVisible(resolutionLabel, viewModel.deviceSourceSelected());
        bindVisible(deviceResolutionCombo, viewModel.deviceSourceSelected());

        // V4L4J adjustments are driver-specific: revealed (not just enabled) only for that driver.
        v4l4jGroup.visibleProperty().bind(viewModel.v4l4jAdjustmentsVisible());
        v4l4jGroup.managedProperty().bind(viewModel.v4l4jAdjustmentsVisible());
    }

    /** Re-syncs the section from the domain after a settings load/reset. */
    public void reload() {
        viewModel.load();
    }

    /** Binds a node's visible+managed to {@code shown} (an unmanaged grid cell collapses its row). */
    private static void bindVisible(Node node, BooleanBinding shown) {
        node.visibleProperty().bind(shown);
        node.managedProperty().bind(shown);
    }

    private void syncSourceRadios() {
        SourceType source = viewModel.sourceProperty().get();
        videoRadio.setSelected(SourceType.Video == source);
        deviceRadio.setSelected(SourceType.Device == source);
    }

    private static void bindPathCombo(ComboBox<String> combo, ObservableList<String> items,
                                      StringProperty pathProperty, Consumer<String> onRemove) {
        combo.setItems(items);
        combo.setConverter(new DefaultStringConverter());
        combo.setCellFactory(RemovableListCells.withDeleteButton(onRemove));
        combo.valueProperty().bindBidirectional(pathProperty);
        Combos.commitOnFocusLost(combo);
    }

    /**
     * Opens a file picker seeded from the combo's current path, and hands a confirmed absolute path to
     * {@code onChosen} (which records it in the recent-path history and selects it).
     */
    private static void browse(ComboBox<String> combo, String title, FileChooser.ExtensionFilter filter,
                               Consumer<String> onChosen) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(filter);

        String current = combo.getValue();
        if (current != null && !current.isBlank()) {
            File file = new File(current.trim());
            File dir = file.isDirectory() ? file : file.getParentFile();
            if (dir != null && dir.isDirectory()) {
                chooser.setInitialDirectory(dir);
            }
            if (file.isFile()) {
                chooser.setInitialFileName(file.getName());
            }
        }

        Window owner = combo.getScene() != null ? combo.getScene().getWindow() : null;
        File chosen = chooser.showOpenDialog(owner);
        if (chosen != null) {
            onChosen.accept(chosen.getAbsolutePath());
        }
    }

    /**
     * Opens a directory picker (for the video image-sequence-folder source) seeded from the combo's
     * current path, and hands a confirmed absolute path to {@code onChosen}.
     */
    private static void browseDirectory(ComboBox<String> combo, String title, Consumer<String> onChosen) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);

        String current = combo.getValue();
        if (current != null && !current.isBlank()) {
            File file = new File(current.trim());
            File dir = file.isDirectory() ? file : file.getParentFile();
            if (dir != null && dir.isDirectory()) {
                chooser.setInitialDirectory(dir);
            }
        }

        Window owner = combo.getScene() != null ? combo.getScene().getWindow() : null;
        File chosen = chooser.showDialog(owner);
        if (chosen != null) {
            onChosen.accept(chosen.getAbsolutePath());
        }
    }
}
