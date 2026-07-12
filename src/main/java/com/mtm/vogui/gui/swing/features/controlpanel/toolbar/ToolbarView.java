/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.toolbar;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.button.BufferedImageButton;
import com.mtm.vogui.gui.swing.shared.components.button.ImageButton;
import com.mtm.vogui.gui.swing.shared.components.panel.ToolbarPanel;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.enums.settings.SettingsType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Toolbar feature (humble view): owns the vo control buttons and the settings popup menu.
 * Buttons are wired to {@link VoController}/{@link SettingsMenuController} at construction;
 * consumers express intent through methods - widgets never leave this class
 * (the {@link ToolbarPanel} struct is exposed for main frame layout only).
 */
public class ToolbarView {

    private final ImageButton btnSettings;
    private final ImageButton btnStartVO;
    private final ImageButton btnPauseVO;
    private final ImageButton btnResetVO;
    private final ImageButton btnStopVO;
    private final ImageButton btnClearVO;
    private final ImageButton btnTimedProcessingVO;
    private final JCheckBoxMenuItem mnuAutosave;

    private final ToolbarPanel panel;

    public ToolbarView(@NotNull AppContext context, @NotNull VoController vo,
                       @NotNull SettingsMenuController settingsMenu) {
        // Settings button
        this.btnSettings = new BufferedImageButton(GuiConstants.BTN_SETTINGS);

        // Settings popup menu
        JMenuItem mnuLoadSettings = new JMenuItem(GuiConstants.MNU_LOAD_SETTINGS_TEXT);
        mnuLoadSettings.addActionListener(_ -> settingsMenu.load());
        JMenuItem mnuSaveSettings = new JMenuItem(GuiConstants.MNU_SAVE_SETTINGS_TEXT);
        mnuSaveSettings.addActionListener(_ -> settingsMenu.save());
        JMenuItem mnuResetSettings = new JMenuItem(GuiConstants.MNU_RESET_SETTINGS_TEXT);
        mnuResetSettings.addActionListener(_ -> settingsMenu.resetDefaults());
        JMenuItem mnuSwitchSettings = new JMenuItem(GuiConstants.MNU_SWITCH_SETTINGS_TEXT);
        mnuSwitchSettings.addActionListener(_ -> settingsMenu.switchFormat());
        this.mnuAutosave = new JCheckBoxMenuItem(GuiConstants.MNU_AUTOSAVE_TEXT);
        this.mnuAutosave.setSelected(context.settings().autosave());
        this.mnuAutosave.addActionListener(_ -> {
            context.settings().autosave(this.mnuAutosave.isSelected());
            // Persisted immediately: an OFF choice could never survive the exit otherwise
            // (autosave-on-exit is skipped exactly when it has just been turned off)
            context.saveToCurrentFormat();
        });

        JPopupMenu popupSettings = new JPopupMenu();
        popupSettings.add(mnuLoadSettings);
        popupSettings.add(mnuSaveSettings);
        popupSettings.add(mnuResetSettings);
        popupSettings.add(mnuSwitchSettings);
        popupSettings.add(this.mnuAutosave);

        // Force menu size calculation
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        popupSettings.show(null, (int) screenSize.getWidth(), (int) screenSize.getHeight());
        popupSettings.setVisible(false);
        popupSettings.setPreferredSize(new Dimension(popupSettings.getWidth(), popupSettings.getHeight()));

        // Popup display listener
        this.btnSettings.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (e.getComponent().contains(e.getPoint())) {
                    popupSettings.show(
                            e.getComponent(),
                            e.getComponent().getWidth() - (int) popupSettings.getPreferredSize().getWidth(),
                            -((int) popupSettings.getPreferredSize().getHeight() + 2)
                    );
                }
            }
        });

        // Start visual odometry button
        this.btnStartVO = new BufferedImageButton(GuiConstants.BTN_START, GuiConstants.BTN_START_DISABLED);
        this.btnStartVO.setToolTipText(GuiConstants.BTN_START_TOOLTIP);
        this.btnStartVO.addActionListener(_ -> vo.start());

        // Pause visual odometry button (enabled on process start)
        this.btnPauseVO = new BufferedImageButton(GuiConstants.BTN_PAUSE, GuiConstants.BTN_PAUSE_DISABLED,
                GuiConstants.BTN_PAUSE_DISABLED);
        this.btnPauseVO.setToolTipText(GuiConstants.BTN_PAUSE_TOOLTIP);
        this.btnPauseVO.setEnabled(false);
        this.btnPauseVO.addActionListener(_ -> vo.pause());

        // Stop visual odometry button (enabled on process start)
        this.btnStopVO = new BufferedImageButton(GuiConstants.BTN_STOP, GuiConstants.BTN_STOP_DISABLED);
        this.btnStopVO.setToolTipText(GuiConstants.BTN_STOP_TOOLTIP);
        this.btnStopVO.setEnabled(false);
        this.btnStopVO.addActionListener(_ -> vo.stop());

        // Reset visual odometry button (enabled on process start)
        this.btnResetVO = new BufferedImageButton(GuiConstants.BTN_RESET);
        this.btnResetVO.setToolTipText(GuiConstants.BTN_RESET_TOOLTIP);
        this.btnResetVO.setEnabled(false);
        this.btnResetVO.addActionListener(_ -> vo.reset());

        // Clear visual odometry button (enabled on process start)
        this.btnClearVO = new BufferedImageButton(GuiConstants.BTN_CLEAR);
        this.btnClearVO.setToolTipText(GuiConstants.BTN_CLEAR_TOOLTIP);
        this.btnClearVO.setEnabled(false);
        this.btnClearVO.addActionListener(_ -> vo.clear());

        // Timed processing button (device only)
        this.btnTimedProcessingVO = new BufferedImageButton(
                GuiConstants.BTN_TIMED_PROCESSING_VO, GuiConstants.BTN_TIMED_PROCESSING_VO,
                GuiConstants.BTN_EMPTY
        );
        this.btnTimedProcessingVO.setAlternativeOpacity(1.0f);
        this.btnTimedProcessingVO.setToolTipText(GuiConstants.BTN_TIMED_PROCESSING_VO_TOOLTIP);
        this.btnTimedProcessingVO.setEnabled(false);
        this.btnTimedProcessingVO.addActionListener(_ -> vo.timedStop());

        this.panel = ToolbarPanel.builder()
                .btnSettings(this.btnSettings)
                .btnStartVO(this.btnStartVO)
                .btnPauseVO(this.btnPauseVO)
                .btnResetVO(this.btnResetVO)
                .btnStopVO(this.btnStopVO)
                .btnClearVO(this.btnClearVO)
                .btnTimedProcessingVO(this.btnTimedProcessingVO)
                .build();
    }

    /**
     * Widget struct consumed by the main frame layout only.
     */
    public ToolbarPanel panel() {
        return this.panel;
    }

    // Toolbar state intents

    /**
     * Disables every control (setup phase: outcome not known yet).
     */
    public void lockAll() {
        this.setToolbarStatus(false, false, false, false, false, false);
    }

    /**
     * Processing started: disable start, enable the processing controls.
     */
    public void setRunning(boolean isTimed) {
        this.setToolbarStatus(false, !isTimed, true, true, true, false);
    }

    /**
     * Processing ended (or never started): restore the ready state and default icons/texts.
     */
    public void setReady(boolean clearEnabled, boolean timedEnabled) {
        this.setToolbarStatus(true, false, false, false, clearEnabled, timedEnabled);

        // Restore buttons icons/texts
        this.btnTimedProcessingVO.removeForegroundText();
        this.btnTimedProcessingVO.defaultIconSet();
        this.btnPauseVO.defaultIconSet();
    }

    public void switchPauseIcon() {
        this.btnPauseVO.switchIconSet();
    }

    public void setTimedEnabled(boolean enabled) {
        this.btnTimedProcessingVO.setEnabled(enabled);
    }

    public void disableTimedAndRepaint() {
        this.btnTimedProcessingVO.setEnabled(false);
        this.btnTimedProcessingVO.repaint();
    }

    /**
     * Switches the timed button to countdown mode showing the initial seconds.
     */
    public void showTimedCountdownStart(int totalSeconds) {
        this.btnTimedProcessingVO.switchIconSet();
        this.btnTimedProcessingVO.setForegroundText(String.valueOf(totalSeconds));
    }

    public void updateTimedCountdown(int remainingSeconds) {
        this.btnTimedProcessingVO.setForegroundText(String.valueOf(remainingSeconds));
    }

    public void showAutosave(boolean autosave) {
        this.mnuAutosave.setSelected(autosave);
    }

    // Dialogs (anchored to the toolbar's window)

    /**
     * Asks the user for the timed processing timeout.
     *
     * @return the chosen seconds, or {@code null} if canceled
     */
    public Integer promptTimedSeconds() {
        String choice = (String) JOptionPane.showInputDialog(
                this.btnTimedProcessingVO,
                GuiConstants.DLG_TIMED_PROCESSING_MESSAGE,
                GuiConstants.DLG_TIMED_PROCESSING_TITLE,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                GuiConstants.DLG_TIMED_PROCESSING_DEFAULT_VALUE
        );
        return choice == null ? null : Integer.parseInt(choice);
    }

    /**
     * Asks the user for the settings save format.
     *
     * @return the chosen format, or {@code null} if canceled
     */
    public SettingsType promptFormatSwitch(SettingsType currentFormat) {
        int choice = JOptionPane.showOptionDialog(this.btnSettings,
                "Do you want to change Save Format? (Actual save format: " + currentFormat + ")",
                "Change Save Format",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null,
                new String[]{"YAML", "JSON"},
                "JSON");
        return switch (choice) {
            case 0 -> SettingsType.YAML;
            case 1 -> SettingsType.JSON;
            default -> null; //If canceled leaves current format
        };
    }

    private void setToolbarStatus(boolean startEnabled, boolean pauseEnabled, boolean stopEnabled,
                                  boolean resetEnabled, boolean clearEnabled, boolean timedEnabled) {
        this.btnStartVO.setEnabled(startEnabled);
        this.btnPauseVO.setEnabled(pauseEnabled);
        this.btnStopVO.setEnabled(stopEnabled);
        this.btnResetVO.setEnabled(resetEnabled);
        this.btnClearVO.setEnabled(clearEnabled);
        this.btnTimedProcessingVO.setEnabled(timedEnabled);
    }
}
