/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel;

import com.mtm.vogui.core.Core;
import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.features.controlpanel.settings.SettingsView;
import com.mtm.vogui.gui.swing.features.controlpanel.toolbar.SettingsMenuController;
import com.mtm.vogui.gui.swing.features.controlpanel.toolbar.ToolbarView;
import com.mtm.vogui.gui.swing.features.controlpanel.toolbar.VoController;
import com.mtm.vogui.gui.swing.shared.components.panel.ToolbarPanel;
import com.mtm.vogui.gui.swing.state.GuiState;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.utilities.OSUtils;
import io.quarkus.logging.Log;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Control-panel window facade (humble view): the input-side counterpart of {@code DashboardView}.
 * Owns the main {@link JFrame} (dialog parent) and composes the {@code toolbar} + {@code settings}
 * sub-features into it. Also the home of the app-level dialogs, parented to this frame - so the
 * render sink and the composition root talk to intents here, never to a raw frame.
 * <p>
 * Threading: {@link #show()} runs at startup on the EDT; the dialog intents block the calling vo
 * worker thread until the user answers (same contract the {@code RenderSink} relies on).
 */
public class ControlPanelView {

    private final AppContext context;

    private final ToolbarView toolbarView;
    private final SettingsView settingsView;

    private final JFrame frame;

    public ControlPanelView(@NotNull AppContext context, Core core, GuiState guiState,
                            boolean systemLookAndFeelEnabled) {
        this.context = context;

        // Toolbar feature: view + vo/settings-menu commands (late-bound: the view wires its buttons
        // to the controllers, the controllers drive it back through intents). Built before the
        // settings views: the input section drives the timed button through the toolbar.
        var voController = new VoController(context, core, guiState);
        var settingsMenuController = new SettingsMenuController(context, guiState,
                this::refreshGuiFromParameters);
        this.toolbarView = new ToolbarView(context, voController, settingsMenuController);
        voController.toolbar(this.toolbarView);
        settingsMenuController.toolbar(this.toolbarView);
        ToolbarPanel toolbarPanel = this.toolbarView.panel();

        // Settings feature facade: owns the five settings sections and composes their panels.
        this.settingsView = new SettingsView(context, this.toolbarView, guiState, systemLookAndFeelEnabled);
        JScrollPane mainScrollPane = new JScrollPane(this.settingsView.panel());
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        this.frame = buildFrame(mainScrollPane, toolbarPanel);
        if (OSUtils.isMac()) {
            this.frame.getRootPane().putClientProperty(GuiConstants.MACOS_TRANSPARENT_TITLE_PROPERTY, true);
        }
        layoutFrame(this.frame, mainScrollPane, toolbarPanel);
    }

    /**
     * Makes the control-panel window visible (called once at startup by the composition root).
     */
    public void show() {
        this.frame.setVisible(true);
    }

    /**
     * Settings feature facade, reached by the render sink to reflect core-healed values.
     */
    public SettingsView settings() {
        return this.settingsView;
    }

    // App-level dialogs (parented to this frame; block the caller until answered)

    public void showErrorDialog(String message) {
        JOptionPane.showConfirmDialog(this.frame, message, "Error",
                JOptionPane.PLAIN_MESSAGE, JOptionPane.ERROR_MESSAGE);
    }

    public boolean showConfirmDialog(String message) {
        int choice = JOptionPane.showConfirmDialog(this.frame, message, "Error",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
        return choice == JOptionPane.OK_OPTION;
    }

    /**
     * Reloads every settings section (and the toolbar autosave menu) from the freshly loaded/reset
     * settings. Wired into the settings menu as the post-load/reset refresh callback.
     */
    public Boolean refreshGuiFromParameters() {
        try {
            this.toolbarView.showAutosave(this.context.settings().autosave());
            this.settingsView.load();
            return true;
        } catch (Exception exc) {
            Log.error("GUI refresh from parameters failed", exc);
            return false;
        }
    }

    // Composition

    private @NotNull JFrame buildFrame(JScrollPane mainScrollPane, @NotNull ToolbarPanel toolbarPanel) {
        JFrame frame = new JFrame(GuiConstants.CONTROL_PANEL_FRAME_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPane = frame.getContentPane();
        contentPane.add(mainScrollPane);
        contentPane.add(toolbarPanel.btnSettings());
        contentPane.add(toolbarPanel.btnStartVO());
        contentPane.add(toolbarPanel.btnPauseVO());
        contentPane.add(toolbarPanel.btnResetVO());
        contentPane.add(toolbarPanel.btnStopVO());
        contentPane.add(toolbarPanel.btnClearVO());
        contentPane.add(toolbarPanel.btnTimedProcessingVO());

        return frame;
    }

    private void layoutFrame(@NotNull JFrame frame, JScrollPane mainScrollPane, @NotNull ToolbarPanel toolbar) {
        SpringLayout panelLayout = new SpringLayout();
        Container contentPane = frame.getContentPane();

        // Main scroll pane
        panelLayout.putConstraint(SpringLayout.NORTH, mainScrollPane, 0, SpringLayout.NORTH, contentPane);
        panelLayout.putConstraint(SpringLayout.WEST, mainScrollPane, 0, SpringLayout.WEST, contentPane);
        panelLayout.putConstraint(SpringLayout.EAST, mainScrollPane, 0, SpringLayout.EAST, contentPane);
        panelLayout.putConstraint(SpringLayout.SOUTH, mainScrollPane, -5, SpringLayout.NORTH, toolbar.btnStartVO());

        // Toolbar buttons
        panelLayout.putConstraint(SpringLayout.NORTH, toolbar.btnStartVO(), -40, SpringLayout.SOUTH, contentPane);
        panelLayout.putConstraint(SpringLayout.WEST, toolbar.btnStartVO(), 10, SpringLayout.WEST, contentPane);
        panelLayout.putConstraint(SpringLayout.NORTH, toolbar.btnPauseVO(), 0, SpringLayout.NORTH, toolbar.btnStartVO());
        panelLayout.putConstraint(SpringLayout.WEST, toolbar.btnPauseVO(), 5, SpringLayout.EAST, toolbar.btnStartVO());
        panelLayout.putConstraint(SpringLayout.NORTH, toolbar.btnStopVO(), 0, SpringLayout.NORTH, toolbar.btnStartVO());
        panelLayout.putConstraint(SpringLayout.WEST, toolbar.btnStopVO(), 5, SpringLayout.EAST, toolbar.btnPauseVO());
        panelLayout.putConstraint(SpringLayout.NORTH, toolbar.btnResetVO(), 0, SpringLayout.NORTH, toolbar.btnStartVO());
        panelLayout.putConstraint(SpringLayout.WEST, toolbar.btnResetVO(), 5, SpringLayout.EAST, toolbar.btnStopVO());
        panelLayout.putConstraint(SpringLayout.NORTH, toolbar.btnClearVO(), 0, SpringLayout.NORTH, toolbar.btnStartVO());
        panelLayout.putConstraint(SpringLayout.WEST, toolbar.btnClearVO(), 5, SpringLayout.EAST, toolbar.btnResetVO());
        panelLayout.putConstraint(SpringLayout.NORTH, toolbar.btnTimedProcessingVO(), 0, SpringLayout.NORTH, toolbar.btnStartVO());
        panelLayout.putConstraint(SpringLayout.WEST, toolbar.btnTimedProcessingVO(), 5, SpringLayout.EAST, toolbar.btnClearVO());

        // Settings button
        panelLayout.putConstraint(SpringLayout.NORTH, toolbar.btnSettings(), 0, SpringLayout.NORTH, toolbar.btnStartVO());
        panelLayout.putConstraint(SpringLayout.EAST, toolbar.btnSettings(), -10, SpringLayout.EAST, contentPane);

        frame.setLayout(panelLayout);

        // Move to the right of the dashboard frame, centered in the horizontal band between it and
        // the video frames (which sit at 2 * default width + 65, leaving 55px of slack to split),
        // Y location at the screen top
        frame.setLocation(SwingUtils.getDefaultFrameDimension().width + (55 / 2), 0);
        frame.setPreferredSize(SwingUtils.getDefaultFrameDimension());
        frame.pack();
    }
}
