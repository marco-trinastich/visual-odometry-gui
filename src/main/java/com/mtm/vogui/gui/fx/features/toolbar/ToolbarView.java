/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.toolbar;

import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * Toolbar feature (humble view, JavaFX twin of {@code gui.swing.features.controlpanel.toolbar.ToolbarView}):
 * owns the vo control buttons injected from the shell FXML and exposes intent methods over them — the
 * buttons never leave this class. The button {@code onAction}s are wired by the shell to
 * {@link VoController}/{@link SettingsMenuController}; here we only drive their enabled state and the
 * text choreography (pause/resume, timed countdown). Icons in the Swing version become button text here.
 * All methods must run on the FX Application Thread.
 */
public class ToolbarView {

    private static final String PAUSE_TEXT = "Pause";
    private static final String RESUME_TEXT = "Resume";
    private static final String TIMED_TEXT = "Timed Stop";

    private final Button startButton;
    private final Button pauseButton;
    private final Button resetButton;
    private final Button stopButton;
    private final Button clearButton;
    private final Button timedStopButton;

    public ToolbarView(Button startButton, Button pauseButton, Button resetButton, Button stopButton,
                       Button clearButton, Button timedStopButton) {
        this.startButton = startButton;
        this.pauseButton = pauseButton;
        this.resetButton = resetButton;
        this.stopButton = stopButton;
        this.clearButton = clearButton;
        this.timedStopButton = timedStopButton;
    }

    // Toolbar state intents (enabled order: start, pause, stop, reset, clear, timed)

    /** Disables every control (setup phase: outcome not known yet). */
    public void lockAll() {
        setToolbarStatus(false, false, false, false, false, false);
    }

    /** Processing started: disable start, enable the processing controls. */
    public void setRunning(boolean isTimed) {
        setToolbarStatus(false, !isTimed, true, true, true, false);
    }

    /** Processing ended (or never started): restore the ready state and default button texts. */
    public void setReady(boolean clearEnabled, boolean timedEnabled) {
        setToolbarStatus(true, false, false, false, clearEnabled, timedEnabled);
        resetPauseText();
        timedStopButton.setText(TIMED_TEXT);
    }

    /** Toggles the pause button between Pause and Resume. */
    public void switchPauseText() {
        pauseButton.setText(RESUME_TEXT.equals(pauseButton.getText()) ? PAUSE_TEXT : RESUME_TEXT);
    }

    public void setTimedEnabled(boolean enabled) {
        timedStopButton.setDisable(!enabled);
    }

    public void disableTimed() {
        timedStopButton.setDisable(true);
    }

    /** Switches the timed button into countdown mode showing the initial seconds. */
    public void showTimedCountdownStart(int totalSeconds) {
        timedStopButton.setText(String.valueOf(totalSeconds));
    }

    public void updateTimedCountdown(int remainingSeconds) {
        timedStopButton.setText(String.valueOf(remainingSeconds));
    }

    // Dialogs (modal, on the FX thread)

    /**
     * Asks the user for the timed-processing timeout.
     *
     * @return the chosen seconds, or {@code null} if canceled or not a positive integer
     */
    public Integer promptTimedSeconds() {
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Timed Processing");
        dialog.setHeaderText(null);
        dialog.setContentText("Processing duration (seconds):");
        Optional<String> choice = dialog.showAndWait();
        if (choice.isEmpty()) {
            return null;
        }
        try {
            int seconds = Integer.parseInt(choice.get().trim());
            return seconds > 0 ? seconds : null;
        } catch (NumberFormatException _) {
            return null;
        }
    }

    private void resetPauseText() {
        pauseButton.setText(PAUSE_TEXT);
    }

    private void setToolbarStatus(boolean startEnabled, boolean pauseEnabled, boolean stopEnabled,
                                  boolean resetEnabled, boolean clearEnabled, boolean timedEnabled) {
        startButton.setDisable(!startEnabled);
        pauseButton.setDisable(!pauseEnabled);
        stopButton.setDisable(!stopEnabled);
        resetButton.setDisable(!resetEnabled);
        clearButton.setDisable(!clearEnabled);
        timedStopButton.setDisable(!timedEnabled);
    }
}
