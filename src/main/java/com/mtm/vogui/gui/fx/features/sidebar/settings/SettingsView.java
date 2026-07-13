/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.settings;

import com.mtm.vogui.gui.fx.features.sidebar.settings.chart.ChartSettingsController;
import com.mtm.vogui.gui.fx.features.sidebar.settings.image.ImageSettingsController;
import com.mtm.vogui.gui.fx.features.sidebar.settings.input.InputSettingsController;
import com.mtm.vogui.gui.fx.features.sidebar.settings.tracker.TrackerSettingsController;
import com.mtm.vogui.gui.fx.features.sidebar.settings.visualodometry.VoSettingsController;
import com.mtm.vogui.gui.fx.utils.FxUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Settings feature facade (JavaFX twin of {@code gui.swing.features.controlpanel.settings.SettingsView}):
 * loads each settings section from its FXML and stacks them in an {@link Accordion} (one section open at
 * a time, so the panel height stays bounded as sections grow), exposing a single {@link #content()} the
 * shell mounts plus a {@link #reload()} that re-syncs every section after a settings load/reset. Each
 * section is a self-contained FXML {@link TitledPane} + thin controller + ViewModel triplet.
 * <p>
 * Migration status: Input + Image + Tracker + Visual Odometry + Chart sections are live (each adds one
 * loader + one accordion pane + one controller reference here). The Input section is complete bar the
 * toolbar coupling (setTimedEnabled on the Device source), which has no FX target yet; the Chart
 * section's manipulation controls (origin/last/3D points, live scale) belong with the FX trajectory
 * chart, not the config column.
 */
public class SettingsView {

    private static final String INPUT_FXML =
            "/gui/fx/features/sidebar/settings/input/input-settings.fxml";
    private static final String IMAGE_FXML =
            "/gui/fx/features/sidebar/settings/image/image-settings.fxml";
    private static final String TRACKER_FXML =
            "/gui/fx/features/sidebar/settings/tracker/tracker-settings.fxml";
    private static final String VO_FXML =
            "/gui/fx/features/sidebar/settings/visualodometry/vo-settings.fxml";
    private static final String CHART_FXML =
            "/gui/fx/features/sidebar/settings/chart/chart-settings.fxml";

    private final ScrollPane content;
    private final InputSettingsController inputController;
    private final ImageSettingsController imageController;
    private final TrackerSettingsController trackerController;
    private final VoSettingsController voController;
    private final ChartSettingsController chartController;

    public SettingsView() {
        try {
            FXMLLoader inputLoader = FxUtils.newFxmlLoader(INPUT_FXML);
            TitledPane inputSection = inputLoader.load();
            this.inputController = inputLoader.getController();

            FXMLLoader imageLoader = FxUtils.newFxmlLoader(IMAGE_FXML);
            TitledPane imageSection = imageLoader.load();
            this.imageController = imageLoader.getController();

            FXMLLoader trackerLoader = FxUtils.newFxmlLoader(TRACKER_FXML);
            TitledPane trackerSection = trackerLoader.load();
            this.trackerController = trackerLoader.getController();

            FXMLLoader voLoader = FxUtils.newFxmlLoader(VO_FXML);
            TitledPane voSection = voLoader.load();
            this.voController = voLoader.getController();

            FXMLLoader chartLoader = FxUtils.newFxmlLoader(CHART_FXML);
            TitledPane chartSection = chartLoader.load();
            this.chartController = chartLoader.getController();

            Accordion accordion =
                    new Accordion(inputSection, imageSection, trackerSection, voSection, chartSection);
            accordion.setExpandedPane(inputSection);

            // Wrapper carrying the ".settings-content" inset knob (app.css); tuned independently from
            // the telemetry tab (the accordion reads best flush, so its default inset is 0).
            VBox wrapper = new VBox(accordion);
            wrapper.getStyleClass().add("settings-content");

            this.content = new ScrollPane(wrapper);
            this.content.setFitToWidth(true);
        } catch (IOException e) {
            // A missing/broken settings FXML is a fatal wiring error: fail fast rather than boot a
            // half-built settings panel (never a swallowed catch — see the project logging rules).
            throw new IllegalStateException("Failed to load settings FXML", e);
        }
    }

    /** The composed, scrollable settings column, for the shell to mount. */
    public Region content() {
        return content;
    }

    /** Re-syncs every section from the (freshly loaded/reset) settings. */
    public void reload() {
        inputController.reload();
        imageController.reload();
        trackerController.reload();
        voController.reload();
        chartController.reload();
    }
}
