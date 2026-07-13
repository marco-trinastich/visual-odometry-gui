/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.toolbar;

import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.settings.SettingsType;
import io.quarkus.logging.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.BooleanSupplier;

/**
 * Settings persistence commands behind the shell's Settings menu (JavaFX twin of
 * {@code gui.swing.features.controlpanel.toolbar.SettingsMenuController}): load/save/reset/switch
 * format. Status goes through {@link GuiState#appStatusProperty()} (the status bar); the GUI refresh
 * after a load/reset is delegated to the injected refresher (the settings sections' {@code reload()}).
 */
public class SettingsMenuController {

    private final AppContext context;
    private final GuiState guiState;
    private final BooleanSupplier guiRefresher;

    public SettingsMenuController(AppContext context, GuiState guiState, BooleanSupplier guiRefresher) {
        this.context = context;
        this.guiState = guiState;
        this.guiRefresher = guiRefresher;
    }

    public void load() {
        switch (context.currentFormat()) {
            case JSON -> {
                if (context.loadFromJson() && guiRefresher.getAsBoolean()) {
                    status(AppStatus.JSONSettingsLoaded);
                } else if (!Files.exists(context.jsonPath())) {
                    status(AppStatus.JSONSettingsNotFound);
                } else {
                    status(AppStatus.JSONSettingsLoadError);
                }
            }
            case YAML -> {
                if (context.loadFromYaml() && guiRefresher.getAsBoolean()) {
                    status(AppStatus.YAMLSettingsLoaded);
                } else if (!Files.exists(context.yamlPath())) {
                    status(AppStatus.YAMLSettingsNotFound);
                } else {
                    status(AppStatus.YAMLSettingsLoadError);
                }
            }
        }
    }

    public void save() {
        switch (context.currentFormat()) {
            case JSON -> status(context.saveToJson() ? AppStatus.JSONSettingsSaved : AppStatus.JSONSettingsSaveError);
            case YAML -> status(context.saveToYaml() ? AppStatus.YAMLSettingsSaved : AppStatus.YAMLSettingsSaveError);
        }
    }

    public void resetDefaults() {
        context.loadDefaults();
        status(guiRefresher.getAsBoolean() ? AppStatus.SettingsReset : AppStatus.SettingsResetError);
    }

    public void switchFormat(SettingsType chosenFormat) {
        if (chosenFormat == null || chosenFormat == context.currentFormat()) {
            return;
        }

        // Save in the new format, then drop the old file: the chosen format survives reboots
        // implicitly, as the only settings file that exists.
        boolean switched = chosenFormat == SettingsType.JSON ? context.saveToJson() : context.saveToYaml();
        if (switched) {
            context.state().settingsFormat(chosenFormat);
            try {
                Files.deleteIfExists(chosenFormat == SettingsType.JSON ? context.yamlPath() : context.jsonPath());
            } catch (IOException ex) {
                Log.warnf("Could not remove the previous settings file: %s", ex.toString());
            }
            status(chosenFormat == SettingsType.JSON ? AppStatus.JSONSettingsSaved : AppStatus.YAMLSettingsSaved);
        } else {
            // Save failed: the previous file is left untouched, format unchanged.
            status(chosenFormat == SettingsType.JSON ? AppStatus.JSONSettingsSaveError : AppStatus.YAMLSettingsSaveError);
        }
    }

    private void status(AppStatus appStatus) {
        guiState.appStatusProperty().set(appStatus);
    }
}
