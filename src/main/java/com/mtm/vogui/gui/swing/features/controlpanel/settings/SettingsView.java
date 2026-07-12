/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.settings;

import com.mtm.vogui.gui.swing.features.controlpanel.settings.chart.ChartSettingsView;
import com.mtm.vogui.gui.swing.features.controlpanel.settings.image.ImageSettingsView;
import com.mtm.vogui.gui.swing.features.controlpanel.settings.input.InputSettingsView;
import com.mtm.vogui.gui.swing.features.controlpanel.settings.tracker.TrackerSettingsView;
import com.mtm.vogui.gui.swing.features.controlpanel.settings.visualodometry.VoSettingsView;
import com.mtm.vogui.gui.swing.features.controlpanel.toolbar.ToolbarView;
import com.mtm.vogui.gui.swing.state.GuiState;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.enums.gui.RecentPathTarget;
import com.mtm.vogui.models.enums.settings.DevicePath;
import com.mtm.vogui.models.interfaces.Resolution;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Settings feature facade (humble view): owns the five settings sub-views, composes their
 * section panels into the control panel's main scroll content, and exposes a single surface
 * to the composition root and the render sink.
 * <p>
 * {@link #load()} refreshes every section after a load/reset; the {@code show*} intents forward
 * the values healed by the core at capture time to the section that owns them, so the sink talks
 * to this one facade rather than reaching into individual sub-views.
 */
public class SettingsView {

    private final InputSettingsView inputSettingsView;
    private final ImageSettingsView imageSettingsView;
    private final TrackerSettingsView trackerSettingsView;
    private final VoSettingsView voSettingsView;
    private final ChartSettingsView chartSettingsView;

    private final JPanel panel;

    public SettingsView(@NotNull AppContext context, @NotNull ToolbarView toolbar,
                        @NotNull GuiState guiState, boolean systemLookAndFeelEnabled) {
        // Built before the settings sub-views: the input section drives the timed button.
        this.inputSettingsView = new InputSettingsView(context, toolbar);
        this.imageSettingsView = new ImageSettingsView(context);
        this.trackerSettingsView = new TrackerSettingsView(context);
        this.voSettingsView = new VoSettingsView(context);
        this.chartSettingsView = new ChartSettingsView(context, guiState.dashboardView().trajectory());

        this.panel = buildPanel(systemLookAndFeelEnabled);
    }

    /**
     * The composed settings panel (all sections), wrapped in a scroll pane by the composition root.
     */
    public JPanel panel() {
        return this.panel;
    }

    /**
     * Reloads every settings section from the (freshly loaded/reset) settings.
     */
    public void load() {
        this.inputSettingsView.load();
        this.imageSettingsView.load();
        this.trackerSettingsView.load();
        this.voSettingsView.load();
        this.chartSettingsView.load();
    }

    // Values healed by the core, forwarded to the section that owns them

    public void showDeviceResolution(Resolution resolution) {
        this.inputSettingsView.showDeviceResolution(resolution);
    }

    public void showDevicePath(DevicePath devicePath) {
        this.inputSettingsView.showDevicePath(devicePath);
    }

    public void showRecentPath(@NotNull RecentPathTarget target, @NotNull PathSettings pathSettings,
                               String usedPath) {
        this.inputSettingsView.showRecentPath(target, pathSettings, usedPath);
    }

    public void showKltPyramidLevels(int pyramidLevels) {
        this.trackerSettingsView.showKltPyramidLevels(pyramidLevels);
    }

    // Composition

    private JPanel buildPanel(boolean systemLookAndFeelEnabled) {
        final JPanel inputSettingsPanel = this.inputSettingsView.panel();
        final JPanel internalImageSettingsPanel = this.imageSettingsView.panel();
        final JPanel trackerSettingsPanel = this.trackerSettingsView.panel();
        final JPanel visualOdometrySettingsPanel = this.voSettingsView.panel();
        final JPanel chartSettingsPanel = this.chartSettingsView.panel();

        final JPanel mainPanel = new JPanel();
        mainPanel.add(inputSettingsPanel);
        mainPanel.add(internalImageSettingsPanel);
        mainPanel.add(trackerSettingsPanel);
        mainPanel.add(visualOdometrySettingsPanel);
        mainPanel.add(chartSettingsPanel);

        SpringLayout panelLayout = new SpringLayout();

        // Input Settings Panel
        panelLayout.putConstraint(SpringLayout.NORTH, inputSettingsPanel, 10, SpringLayout.NORTH, mainPanel);
        panelLayout.putConstraint(SpringLayout.WEST, inputSettingsPanel, 5, SpringLayout.WEST, mainPanel);
        panelLayout.putConstraint(SpringLayout.EAST, inputSettingsPanel, -5, SpringLayout.EAST, mainPanel);

        // Internal Image Settings Panel
        panelLayout.putConstraint(SpringLayout.NORTH, internalImageSettingsPanel, 1, SpringLayout.SOUTH, inputSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, internalImageSettingsPanel, 5, SpringLayout.WEST, mainPanel);
        panelLayout.putConstraint(SpringLayout.EAST, internalImageSettingsPanel, -5, SpringLayout.EAST, mainPanel);

        // Tracker Settings Panel
        panelLayout.putConstraint(SpringLayout.NORTH, trackerSettingsPanel, 1, SpringLayout.SOUTH, internalImageSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, trackerSettingsPanel, 5, SpringLayout.WEST, mainPanel);
        panelLayout.putConstraint(SpringLayout.EAST, trackerSettingsPanel, -5, SpringLayout.EAST, mainPanel);

        // Visual Odometry Settings Panel
        panelLayout.putConstraint(SpringLayout.NORTH, visualOdometrySettingsPanel, 1, SpringLayout.SOUTH, trackerSettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, visualOdometrySettingsPanel, 5, SpringLayout.WEST, mainPanel);
        panelLayout.putConstraint(SpringLayout.EAST, visualOdometrySettingsPanel, -5, SpringLayout.EAST, mainPanel);

        // Chart Settings Panel
        panelLayout.putConstraint(SpringLayout.NORTH, chartSettingsPanel, 1, SpringLayout.SOUTH, visualOdometrySettingsPanel);
        panelLayout.putConstraint(SpringLayout.WEST, chartSettingsPanel, 5, SpringLayout.WEST, mainPanel);
        panelLayout.putConstraint(SpringLayout.EAST, chartSettingsPanel, -5, SpringLayout.EAST, mainPanel);
        panelLayout.putConstraint(SpringLayout.SOUTH, chartSettingsPanel, 1, SpringLayout.SOUTH, mainPanel);

        mainPanel.setLayout(panelLayout);

        // Preferred size depends on whether the system look and feel is active
        mainPanel.setPreferredSize(
                systemLookAndFeelEnabled ? new Dimension(480, 865) : new Dimension(480, 785));

        return mainPanel;
    }
}
