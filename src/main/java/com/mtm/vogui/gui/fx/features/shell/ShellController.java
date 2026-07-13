/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.shell;

import com.mtm.vogui.core.Core;
import com.mtm.vogui.gui.fx.features.settings.SettingsView;
import com.mtm.vogui.gui.fx.features.toolbar.SettingsMenuController;
import com.mtm.vogui.gui.fx.features.toolbar.ToolbarView;
import com.mtm.vogui.gui.fx.features.toolbar.VoController;
import com.mtm.vogui.gui.fx.features.video.VideoView;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.settings.SettingsType;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Thin controller of the main shell: binds views to {@link GuiState} and delegates actions.
 * No logic here beyond binding/delegation (MVVM rule).
 * <p>
 * {@code @Unremovable}: FXML controllers are resolved reflectively by the CDI controller
 * factory, so Arc sees no injection point and would otherwise drop the bean at build time.
 */
@Dependent
@Unremovable
public class ShellController {

    @Inject
    AppContext context;

    @Inject
    GuiState guiState;

    @Inject
    Core core;

    @ConfigProperty(name = "quarkus.application.version")
    String appVersion;

    @FXML
    private Label statusLabel;

    @FXML
    private CheckMenuItem mnuAutosave;

    @FXML
    private RadioMenuItem formatJson;

    @FXML
    private RadioMenuItem formatYaml;

    @FXML
    private StackPane settingsPane;

    @FXML
    private StackPane videoPane;

    @FXML
    private Button startButton;

    @FXML
    private Button pauseButton;

    @FXML
    private Button resetButton;

    @FXML
    private Button stopButton;

    @FXML
    private Button timedStopButton;

    @FXML
    private Button clearButton;

    private VoController voController;
    private SettingsMenuController settingsMenu;

    @FXML
    public void initialize() {
        statusLabel.textProperty().bind(guiState.appStatusProperty()
                .map(status -> status == null ? "" : status.value()));

        mnuAutosave.setSelected(context.settings().autosave());

        // Compose the settings feature and mount it into the left slot.
        SettingsView settingsView = new SettingsView();
        settingsPane.getChildren().setAll(settingsView.content());

        // Compose the video feature (it owns its own input|output split) and mount it.
        VideoView videoView = new VideoView(guiState);
        videoPane.getChildren().setAll(videoView.content());

        // Toolbar + settings-menu commands (command pattern: button/menu -> controller -> core).
        ToolbarView toolbar = new ToolbarView(startButton, pauseButton, resetButton, stopButton,
                clearButton, timedStopButton);
        voController = new VoController(context, core, guiState, toolbar);
        settingsMenu = new SettingsMenuController(context, guiState,
                () -> {
                    settingsView.reload();
                    // Re-sync the shell-level menu state too (outside the sections' reload).
                    mnuAutosave.setSelected(context.settings().autosave());
                    syncFormatRadios();
                    return true;
                });

        // Save-format radios: mutually-exclusive, current format checked; selecting the other switches.
        ToggleGroup formatGroup = new ToggleGroup();
        formatJson.setToggleGroup(formatGroup);
        formatYaml.setToggleGroup(formatGroup);
        syncFormatRadios();

        startButton.setOnAction(_ -> voController.start());
        pauseButton.setOnAction(_ -> voController.pause());
        resetButton.setOnAction(_ -> voController.reset());
        stopButton.setOnAction(_ -> voController.stop());
        timedStopButton.setOnAction(_ -> voController.timedStop());
        clearButton.setOnAction(_ -> voController.clear());
    }

    @FXML
    private void onLoadSettings() {
        settingsMenu.load();
    }

    @FXML
    private void onSaveSettings() {
        settingsMenu.save();
    }

    @FXML
    private void onResetSettings() {
        settingsMenu.resetDefaults();
    }

    @FXML
    private void onSelectJsonFormat() {
        settingsMenu.switchFormat(SettingsType.JSON);
        // Re-sync in case the switch failed (radio must reflect the actual current format).
        syncFormatRadios();
    }

    @FXML
    private void onSelectYamlFormat() {
        settingsMenu.switchFormat(SettingsType.YAML);
        syncFormatRadios();
    }

    private void syncFormatRadios() {
        boolean isJson = SettingsType.JSON == context.currentFormat();
        formatJson.setSelected(isJson);
        formatYaml.setSelected(!isJson);
    }

    @FXML
    private void onToggleAutosave() {
        context.settings().autosave(mnuAutosave.isSelected());
        // Persist immediately: an OFF choice could never survive exit otherwise (autosave-on-exit is
        // skipped exactly when it has just been turned off).
        context.saveToCurrentFormat();
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    @FXML
    private void onAbout() {
        AboutDialog.show(appVersion, statusLabel.getScene().getWindow());
    }
}
