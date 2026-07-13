/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.shell;

import com.mtm.vogui.core.Core;
import com.mtm.vogui.gui.fx.shared.behaviors.Editors;
import com.mtm.vogui.gui.fx.features.sidebar.SidebarView;
import com.mtm.vogui.gui.fx.features.toolbar.SettingsMenuController;
import com.mtm.vogui.gui.fx.features.toolbar.ToolbarView;
import com.mtm.vogui.gui.fx.features.toolbar.VoController;
import com.mtm.vogui.gui.fx.features.trajectory.TrajectoryView;
import com.mtm.vogui.gui.fx.features.video.VideoView;
import com.mtm.vogui.gui.fx.ThemeManager;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.gui.ThemeMode;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.settings.ChartType;
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

    @Inject
    ThemeManager themeManager;

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
    private RadioMenuItem themeAuto;

    @FXML
    private RadioMenuItem themeLight;

    @FXML
    private RadioMenuItem themeDark;

    @FXML
    private StackPane sidebarPane;

    @FXML
    private StackPane trajectoryPane;

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

        // Compose the left rail (Settings + Telemetry tabs, with its own tab state machine) and mount
        // it into the left slot. The sidebar owns the tab choreography; the shell just mounts + delegates.
        SidebarView sidebarView = new SidebarView(guiState);
        sidebarPane.getChildren().setAll(sidebarView.content());

        // Compose the trajectory feature (the two charts + their nav controls) and mount it.
        TrajectoryView trajectoryView = new TrajectoryView(guiState);
        trajectoryPane.getChildren().setAll(trajectoryView.content());

        // Point-selection → chart navigation: selecting a tracked point while idle scrolls both charts
        // to it. Cross-feature glue at the composition root (needs both features + core state), mirroring
        // the Swing DashboardView wiring.
        sidebarView.selectedTrackedPoint().addListener((_, _, selected) ->
                navigateToPoint(trajectoryView, selected));

        // Compose the video feature (it owns its own input|output split) and mount it.
        VideoView videoView = new VideoView(guiState);
        videoPane.getChildren().setAll(videoView.content());

        // Toolbar + settings-menu commands (command pattern: button/menu -> controller -> core).
        ToolbarView toolbar = new ToolbarView(startButton, pauseButton, resetButton, stopButton,
                clearButton, timedStopButton);
        voController = new VoController(context, core, guiState, toolbar,
                sidebarView::showTelemetry, sidebarView::resetToSettings, trajectoryView::xzHasPoints);
        settingsMenu = new SettingsMenuController(context, guiState,
                () -> {
                    sidebarView.reloadSettings();
                    // Re-sync the shell-level menu state too (outside the sections' reload).
                    mnuAutosave.setSelected(context.settings().autosave());
                    syncFormatRadios();
                    // A loaded/reset settings file may carry a different theme — re-apply and re-check.
                    themeManager.refresh();
                    syncThemeRadios();
                    return true;
                });

        // Save-format radios: mutually-exclusive, current format checked; selecting the other switches.
        ToggleGroup formatGroup = new ToggleGroup();
        formatJson.setToggleGroup(formatGroup);
        formatYaml.setToggleGroup(formatGroup);
        syncFormatRadios();

        // Theme radios: mutually-exclusive, persisted mode checked; selecting one applies + persists it.
        ToggleGroup themeGroup = new ToggleGroup();
        themeAuto.setToggleGroup(themeGroup);
        themeLight.setToggleGroup(themeGroup);
        themeDark.setToggleGroup(themeGroup);
        syncThemeRadios();

        startButton.setOnAction(_ -> voController.start());
        pauseButton.setOnAction(_ -> voController.pause());
        resetButton.setOnAction(_ -> voController.reset());
        stopButton.setOnAction(_ -> voController.stop());
        timedStopButton.setOnAction(_ -> voController.timedStop());
        clearButton.setOnAction(_ -> voController.clear());
    }

    @FXML
    private void onSettingsMenuShowing() {
        // Opening a menu doesn't fire focus-lost on a focused Spinner/combo editor, so a Save/Exit
        // picked straight from here would serialize stale text — commit the pending edit first.
        Editors.commitFocused(statusLabel.getScene());
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
    private void onSelectThemeAuto() {
        themeManager.setMode(ThemeMode.AUTO);
    }

    @FXML
    private void onSelectThemeLight() {
        themeManager.setMode(ThemeMode.LIGHT);
    }

    @FXML
    private void onSelectThemeDark() {
        themeManager.setMode(ThemeMode.DARK);
    }

    private void syncThemeRadios() {
        ThemeMode mode = themeManager.mode();
        themeAuto.setSelected(ThemeMode.AUTO == mode);
        themeLight.setSelected(ThemeMode.LIGHT == mode);
        themeDark.setSelected(ThemeMode.DARK == mode);
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

    /**
     * Scrolls both charts to the selected tracked point, unless a run is in progress. Markers (segment
     * start/end) resolve to their adjacent real point; a selection that stays a marker navigates nowhere.
     */
    private void navigateToPoint(TrajectoryView trajectory, TrackedPoint selected) {
        if (selected == null || context.state().processing().is(ProcessingState.Running)) {
            return;
        }
        TrackedPoint point = resolveRealPoint(selected);
        if (point == null) {
            return;
        }
        if (point.x() != null && point.z() != null) {
            trajectory.moveXzToPoint(point.x(), point.z());
        }
        if (point.y() != null) {
            Double x = ChartType.YFrames.is(point.chartType()) ? (double) point.frame() : point.time();
            if (x != null) {
                trajectory.moveYToPoint(x, point.y());
            }
        }
    }

    /**
     * Resolves a selected row to a real point, skipping segment markers: a start marker resolves to the
     * next row, an end marker to the previous one; a marker with no real neighbour resolves to none.
     * Mirrors the Swing {@code TrackedPointsView.selectedPoint()} logic (by identity in the log list).
     */
    private TrackedPoint resolveRealPoint(TrackedPoint selected) {
        var points = guiState.trackedPoints();
        int index = -1;
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i) == selected) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            return null;
        }
        TrackedPoint point = selected;
        if (point.startPoint() && index < points.size() - 1) {
            point = points.get(index + 1);
        }
        if (point.endPoint() && index > 0) {
            point = points.get(index - 1);
        }
        return point.startPoint() || point.endPoint() ? null : point;
    }
}
