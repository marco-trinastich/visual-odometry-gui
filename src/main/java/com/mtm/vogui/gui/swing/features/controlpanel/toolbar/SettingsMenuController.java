/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.toolbar;

import com.mtm.vogui.gui.swing.state.GuiState;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.settings.SettingsType;
import io.quarkus.logging.Log;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Supplier;

/**
 * Settings persistence commands behind the toolbar settings menu: load/save/reset/switch format.
 * The GUI refresh after a load/reset is delegated to the injected refresher (today the
 * monolithic {@code SwingApplication.refreshGuiFromParameters}, per-section {@code load()}
 * views as the settings feature gets extracted).
 */
public class SettingsMenuController {

    private final AppContext context;
    private final GuiState guiState;
    private final Supplier<Boolean> guiRefresher;

    @Setter
    private ToolbarView toolbar;

    public SettingsMenuController(AppContext context, GuiState guiState, Supplier<Boolean> guiRefresher) {
        this.context = context;
        this.guiState = guiState;
        this.guiRefresher = guiRefresher;
    }

    public void load() {
        switch (this.context.currentFormat()) {
            case JSON:
                // Reload settings from file and refresh the whole GUI from them
                if (this.context.loadFromJson() && this.guiRefresher.get()) {
                    this.guiState.dashboardView().info().setAppStatus(AppStatus.JSONSettingsLoaded);
                } else if (!Files.exists(this.context.jsonPath())) {
                    this.guiState.dashboardView().info().setAppStatus(AppStatus.JSONSettingsNotFound);
                } else {
                    this.guiState.dashboardView().info().setAppStatus(AppStatus.JSONSettingsLoadError);
                }
                break;
            case YAML:
                if (this.context.loadFromYaml() && this.guiRefresher.get()) {
                    this.guiState.dashboardView().info().setAppStatus(AppStatus.YAMLSettingsLoaded);
                } else if (!Files.exists(this.context.yamlPath())) {
                    this.guiState.dashboardView().info().setAppStatus(AppStatus.YAMLSettingsNotFound);
                } else {
                    this.guiState.dashboardView().info().setAppStatus(AppStatus.YAMLSettingsLoadError);
                }
                break;
            default:
                break;
        }
    }

    public void save() {
        switch (this.context.currentFormat()) {
            case JSON:
                this.guiState.dashboardView().info().setAppStatus(this.context.saveToJson()
                        ? AppStatus.JSONSettingsSaved
                        : AppStatus.JSONSettingsSaveError);
                break;
            case YAML:
                this.guiState.dashboardView().info().setAppStatus(this.context.saveToYaml()
                        ? AppStatus.YAMLSettingsSaved
                        : AppStatus.YAMLSettingsSaveError);
                break;
            default:
                break;
        }
    }

    public void resetDefaults() {
        // Resets parameters to defaults and refreshes the whole GUI from them
        this.context.loadDefaults();
        boolean resetSuccess = this.guiRefresher.get();
        this.guiState.dashboardView().info().setAppStatus(resetSuccess
                ? AppStatus.SettingsReset
                : AppStatus.SettingsResetError);
    }

    public void switchFormat() {
        SettingsType currentFormat = this.context.currentFormat();
        SettingsType chosenFormat = this.toolbar.promptFormatSwitch(currentFormat);
        if (chosenFormat == null || chosenFormat == currentFormat) {
            return;
        }

        // Save in the new format, then drop the old file: the chosen format
        // survives reboots implicitly, as the only settings file that exists
        boolean switched = chosenFormat == SettingsType.JSON
                ? this.context.saveToJson()
                : this.context.saveToYaml();
        if (switched) {
            this.context.state().settingsFormat(chosenFormat);
            try {
                Files.deleteIfExists(chosenFormat == SettingsType.JSON
                        ? this.context.yamlPath()
                        : this.context.jsonPath());
            } catch (IOException ex) {
                Log.warnf("Could not remove the previous settings file: %s", ex.toString());
            }
            this.guiState.dashboardView().info().setAppStatus(chosenFormat == SettingsType.JSON
                    ? AppStatus.JSONSettingsSaved
                    : AppStatus.YAMLSettingsSaved);
        } else {
            // Save failed: the previous file is left untouched, format unchanged
            this.guiState.dashboardView().info().setAppStatus(chosenFormat == SettingsType.JSON
                    ? AppStatus.JSONSettingsSaveError
                    : AppStatus.YAMLSettingsSaveError);
        }
    }
}
