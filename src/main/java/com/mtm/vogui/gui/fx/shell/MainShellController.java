/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shell;

import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.settings.SettingsType;
import io.quarkus.arc.Unremovable;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
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
public class MainShellController {

    @Inject
    AppContext context;

    @Inject
    GuiState guiState;

    @ConfigProperty(name = "quarkus.application.version")
    String appVersion;

    @FXML
    private Label statusLabel;

    @FXML
    private CheckMenuItem mnuAutosave;

    @FXML
    public void initialize() {
        statusLabel.textProperty().bind(guiState.appStatusProperty()
                .map(status -> status == null ? "" : status.value()));

        mnuAutosave.setSelected(context.settings().autosave());
    }

    @FXML
    private void onSaveSettings() {
        boolean saved = context.saveToCurrentFormat();
        boolean isJson = SettingsType.JSON == context.state().settingsFormat();
        guiState.appStatusProperty().set(saved
                ? (isJson ? AppStatus.JSONSettingsSaved : AppStatus.YAMLSettingsSaved)
                : (isJson ? AppStatus.JSONSettingsSaveError : AppStatus.YAMLSettingsSaveError));
    }

    @FXML
    private void onToggleAutosave() {
        context.settings().autosave(mnuAutosave.isSelected());
    }

    @FXML
    private void onExit() {
        Platform.exit();
    }

    @FXML
    private void onAbout() {
        Alert about = new Alert(Alert.AlertType.INFORMATION,
                AppConstants.APP_DESCRIPTION + "\nVersion " + appVersion,
                javafx.scene.control.ButtonType.OK);
        about.setTitle(AppConstants.ABOUT_TITLE);
        about.setHeaderText(AppConstants.APP_TITLE);
        about.showAndWait();
    }
}
