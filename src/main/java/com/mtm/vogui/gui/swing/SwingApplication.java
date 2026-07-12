/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing;

import java.awt.*;
import java.time.LocalDateTime;

import javax.swing.*;

import com.mtm.vogui.core.Core;
import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.features.controlpanel.ControlPanelView;
import com.mtm.vogui.gui.swing.features.dashboard.DashboardView;
import com.mtm.vogui.gui.swing.features.video.VideoView;
import com.mtm.vogui.gui.swing.shared.components.border.RoundedCornerBorder;
import com.mtm.vogui.gui.swing.state.GuiState;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.context.AppContext;


import com.mtm.vogui.utilities.*;

import boofcv.BoofVersion;

import org.jetbrains.annotations.NotNull;

/**
 * Legacy Swing application root: composition root only. Not a bean: built by
 * {@code gui.swing.SwingLauncher} only when the Swing UI is active. Creates the two window
 * facades ({@link DashboardView}, {@link ControlPanelView}) plus the video view and wires the
 * base look and feel; runtime interactions go through the views, never raw frames.
 */
public class SwingApplication {

    private final AppContext context;
    private final Core core;
    private final GuiState guiState;
    private final String appVersion;

    private boolean isSystemLookAndFeelEnabled;

    public SwingApplication(AppContext context, Core core, GuiState guiState, String appVersion) {
        this.context = context;
        this.core = core;
        this.guiState = guiState;
        this.appVersion = appVersion;
    }

    /**
     * Start application after creating all gui frames
     */
    public void start() {
        // Configure base UI look and feel
        setBaseUI();

        // Video frames (input preview + vo output)
        createVideoView();

        // Dashboard window (trajectory charts + telemetry)
        createDashboardWindow();

        // Control-panel window (settings + toolbar)
        createControlPanelWindow();
    }

    private void createVideoView() {
        // Video feature: input preview + vo output frames
        guiState.videoView(new VideoView());
    }

    private void createDashboardWindow() {
        // Dashboard feature: trajectory charts + telemetry output window (owns its own frame)
        var dashboardView = new DashboardView(this.context);
        guiState.dashboardView(dashboardView);
        dashboardView.show();
    }

    private void createControlPanelWindow() {
        // Control-panel feature: settings + toolbar (owns its own frame + the app-level dialogs)
        var controlPanelView = new ControlPanelView(
                this.context, this.core, this.guiState, this.isSystemLookAndFeelEnabled);
        guiState.controlPanelView(controlPanelView);
        controlPanelView.show();
    }

    private void setBaseUI() {
        // Configure app UI (title, icon, about)
        setAppBaseUI();

        // Configure UI theme
        setThemeUI();
    }


    private void setAppBaseUI() {
        // Set app title
        if (OSUtils.isMac()) {
            System.setProperty(GuiConstants.MACOS_APP_TITLE_PROPERTY, AppConstants.APP_TITLE);
        }

        // Set app icon
        Image appIcon = SwingUtils.getResourceImage(AppConstants.APP_ICON);
        if (appIcon != null) {
            Taskbar taskbar = Taskbar.getTaskbar();
            taskbar.setIconImage(appIcon);
        }

        // Set app about window
        if (Desktop.isDesktopSupported()) {
            JFrame aboutFrame = this.createAboutFrame();
            Desktop.getDesktop().setAboutHandler(_ -> {
                resetAboutFrame(aboutFrame, true);
            });
        }
    }

    private @NotNull JFrame createAboutFrame() {
        JLabel appImage = new JLabel();
        Image appIcon = SwingUtils.getResourceImage(AppConstants.APP_ICON, 150, 1f);
        if (appIcon != null) {
            appImage.setIcon(new ImageIcon(appIcon));
        }

        JLabel appInfo = new JLabel(String.format(AppConstants.APP_TITLE_PATTERN, this.appVersion));

        JLabel appDescription = new JLabel(AppConstants.APP_DESCRIPTION);
        SwingUtils.setFont(appDescription, 10);

        JLabel javaInfo = new JLabel(String.format(AppConstants.JAVA_INFO,
                System.getProperty(AppConstants.JAVA_VERSION)));
        javaInfo.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        SwingUtils.setFont(javaInfo, 10, Font.ITALIC);

        JLabel boofCvInfo = new JLabel(String.format(AppConstants.BOOFCV_INFO,
                BoofVersion.VERSION,
                BoofVersion.BUILD_DATE
        ));
        SwingUtils.setFont(boofCvInfo, 10, Font.ITALIC);

        JLabel licenseInfo = new JLabel(AppConstants.LICENSE_INFO);
        licenseInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        SwingUtils.setFont(licenseInfo, 10, Font.ITALIC);

        JLabel authorInfo = new JLabel(String.format(AppConstants.AUTHOR_INFO, LocalDateTime.now().getYear()));

        JPanel aboutPanel = new JPanel(new GridBagLayout());
        aboutPanel.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = c.gridx = 0;
        aboutPanel.add(appImage, c);
        c.gridy = 1;
        aboutPanel.add(appInfo, c);
        c.gridy = 2;
        aboutPanel.add(appDescription, c);
        c.gridy = 3;
        aboutPanel.add(javaInfo, c);
        c.gridy = 4;
        aboutPanel.add(boofCvInfo, c);
        c.gridy = 5;
        aboutPanel.add(licenseInfo, c);
        c.gridy = 6;
        aboutPanel.add(authorInfo, c);

        JFrame aboutFrame = new JFrame(AppConstants.ABOUT_TITLE);
        aboutFrame.getContentPane().add(aboutPanel);
        resetAboutFrame(aboutFrame, false);
        aboutFrame.pack();

        return aboutFrame;
    }

    private void resetAboutFrame(JFrame aboutFrame, boolean show) {
        SwingUtils.resizeAndCenter(aboutFrame, 350, false);
        if (show) {
            aboutFrame.setVisible(true);
        }
    }

    private void setThemeUI() {
        // Enable native lookAndFeel
        try {
            isSystemLookAndFeelEnabled = true;
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                 UnsupportedLookAndFeelException _) {
            isSystemLookAndFeelEnabled = false;
        }

        // Set title bar auto light/dark
        if (OSUtils.isMac()) {
            System.setProperty(GuiConstants.MACOS_APP_APPEARANCE_PROPERTY, GuiConstants.MACOS_APP_APPEARANCE_VALUE);
        }

        // Enable system anti-aliasing
        SwingUtils.setSystemAntiAliasing();

        // Global theming
        SwingUtils.setUIPropertyEndsWith(GuiConstants.ALL_BACKGROUNDS_PROP, GuiConstants.APP_BACKGROUND_COLOR);
        SwingUtils.setUIProperty(GuiConstants.COMBO_BOX_BACKGROUND_PROP, GuiConstants.COMBO_BOX_BACKGROUND_COLOR);
        SwingUtils.setUIProperty(GuiConstants.LIST_BACKGROUND_PROP, GuiConstants.LIST_BACKGROUND_COLOR);
        SwingUtils.setUIProperty(GuiConstants.LIST_SELECTION_BACKGROUND_PROP,
                GuiConstants.LIST_SELECTION_BACKGROUND_COLOR);
        SwingUtils.setUIProperty(GuiConstants.TEXT_FIELD_BACKGROUND_PROP, GuiConstants.TEXT_FIELD_BACKGROUND_COLOR);
        SwingUtils.setUIProperty(GuiConstants.TEXT_FIELD_BORDER_PROP,
                new RoundedCornerBorder(
                        GuiConstants.TEXT_FIELD_BORDER_BASE_COLOR,
                        GuiConstants.TEXT_FIELD_BORDER_HIGHLIGHT_COLOR
                ));
    }

}
