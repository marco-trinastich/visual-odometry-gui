/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar;

import com.mtm.vogui.gui.fx.features.sidebar.settings.SettingsView;
import com.mtm.vogui.gui.fx.features.sidebar.telemetry.TelemetryView;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;

/**
 * Left-rail feature facade: the tabbed sidebar that hosts the app's two interaction modes —
 * CONFIGURE ({@link SettingsView}) and MONITOR ({@link TelemetryView}) — as sibling tabs, and owns the
 * tab choreography. This is the FX composite that groups the settings + telemetry sub-features (the
 * shell only mounts one {@link #content()}), keeping the tab logic out of the thin shell controller.
 * <p>
 * Mode state machine driven by {@link GuiState#telemetryAvailableProperty()}: the Telemetry tab is
 * disabled with a hint until the first Start (progressive disclosure of the workflow), auto-focused on
 * Start, and re-disabled with focus back to Settings on Clear. Built on the FX Application Thread.
 */
public class SidebarView {

    private final TabPane tabs;
    private final Tab telemetryTab;
    private final SettingsView settingsView;
    private final TelemetryView telemetryView;

    public SidebarView(GuiState guiState) {
        this.settingsView = new SettingsView();
        this.telemetryView = new TelemetryView(guiState);

        Tab settingsTab = new Tab("Settings", settingsView.content());
        this.telemetryTab = new Tab("Telemetry", telemetryView.content());
        // Disabled with a hint until the first Start (progressive disclosure of the workflow).
        this.telemetryTab.setDisable(true);
        this.telemetryTab.setTooltip(new Tooltip("Available after Start"));

        this.tabs = new TabPane(settingsTab, this.telemetryTab);
        this.tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    }

    /** The composed tabbed rail, for the shell to mount. */
    public Region content() {
        return this.tabs;
    }

    /** Run started: enable the Telemetry tab and focus it (called on every Start). */
    public void showTelemetry() {
        this.telemetryTab.setDisable(false);
        this.tabs.getSelectionModel().select(this.telemetryTab);
    }

    /** Cleared: focus Settings and re-lock the Telemetry tab (called only on Clear). */
    public void resetToSettings() {
        this.tabs.getSelectionModel().selectFirst();
        this.telemetryTab.setDisable(true);
    }

    /** Re-syncs the settings sections after a settings load/reset (delegated by the shell's menu). */
    public void reloadSettings() {
        this.settingsView.reload();
    }

    /** The user-selected tracked point, for the shell's point-selection → chart navigation wiring. */
    public ReadOnlyObjectProperty<TrackedPoint> selectedTrackedPoint() {
        return this.telemetryView.selectedTrackedPoint();
    }
}
