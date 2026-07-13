/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.toolbar;

import atlantafx.base.theme.Styles;
import com.mtm.vogui.gui.fx.shared.behaviors.Spinners;
import com.mtm.vogui.gui.fx.utils.FxUtils;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.function.IntConsumer;

/**
 * Toolbar feature (humble view, JavaFX twin of {@code gui.swing.features.controlpanel.toolbar.ToolbarView}):
 * owns the vo control buttons injected from the shell FXML and exposes intent methods over them — the
 * buttons never leave this class. The button {@code onAction}s are wired by the shell to
 * {@link VoController}/{@link SettingsMenuController}; here we only drive their enabled state and the
 * text choreography (pause/resume, timed countdown). Icons in the Swing version become button text here.
 * <p>
 * Timed stop replaces the old modal seconds prompt with an auto-hiding {@link Popup} anchored under the
 * button (progressive disclosure: the control is Device-only and disabled most of the time, so it earns
 * no permanent toolbar space). The popover offers quick presets plus an editable spinner; committing a
 * duration hands it to the caller via the {@link #openTimedPopover(IntConsumer)} callback, after which
 * the button itself morphs into the live countdown exactly as before.
 * <p>
 * All methods must run on the FX Application Thread.
 */
public class ToolbarView {

    private static final String PAUSE_TEXT = "Pause";
    private static final String RESUME_TEXT = "Resume";
    private static final String TIMED_TEXT = "Timed Stop  ▾";
    private static final int[] TIMED_PRESETS = {10, 30, 60};
    private static final int TIMED_DEFAULT = 10;

    private final Button startButton;
    private final Button pauseButton;
    private final Button resetButton;
    private final Button stopButton;
    private final Button clearButton;
    private final Button timedStopButton;

    private Popup timedPopup;
    private Spinner<Integer> secondsSpinner;
    private IntConsumer onTimedStart;

    public ToolbarView(Button startButton, Button pauseButton, Button resetButton, Button stopButton,
                       Button clearButton, Button timedStopButton) {
        this.startButton = startButton;
        this.pauseButton = pauseButton;
        this.resetButton = resetButton;
        this.stopButton = stopButton;
        this.clearButton = clearButton;
        this.timedStopButton = timedStopButton;
        this.timedStopButton.setText(TIMED_TEXT);
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

    // Timed-stop popover (progressive disclosure; auto-hides on click outside)

    /**
     * Opens the timed-stop popover under the toolbar button. When the user commits a duration,
     * {@code onStart} is invoked with the chosen seconds; the popover then closes on its own.
     */
    public void openTimedPopover(IntConsumer onStart) {
        this.onTimedStart = onStart;
        ensureTimedPopup();
        // A click on the (showing) trigger auto-hides the popup before this runs, so re-show is the
        // natural toggle; guard against a stray double-open when it is genuinely already showing.
        if (timedPopup.isShowing()) {
            timedPopup.hide();
            return;
        }
        Point2D anchor = timedStopButton.localToScreen(0, timedStopButton.getHeight());
        timedPopup.show(timedStopButton, anchor.getX(), anchor.getY() + 4);
        secondsSpinner.getEditor().requestFocus();
        secondsSpinner.getEditor().selectAll();
    }

    private void ensureTimedPopup() {
        if (timedPopup != null) {
            return;
        }

        Label title = new Label("Run for");
        title.getStyleClass().add(Styles.TEXT_MUTED);

        secondsSpinner = new Spinner<>(1, 3600, TIMED_DEFAULT);
        secondsSpinner.setEditable(true);
        secondsSpinner.setPrefWidth(96);
        Spinners.commitOnFocusLost(secondsSpinner);
        Label unit = new Label("seconds");
        unit.getStyleClass().add(Styles.TEXT_MUTED);
        HBox spinnerRow = new HBox(6, secondsSpinner, unit);
        spinnerRow.setAlignment(Pos.CENTER_LEFT);

        HBox presets = new HBox(6);
        for (int preset : TIMED_PRESETS) {
            presets.getChildren().add(presetButton(preset));
        }

        Button start = new Button("Start");
        start.getStyleClass().add(Styles.ACCENT);
        start.setDefaultButton(true);
        start.setMaxWidth(Double.MAX_VALUE);
        start.setOnAction(_ -> commitTimed());

        VBox card = new VBox(10, title, presets, spinnerRow, start);
        card.getStyleClass().add("timed-popover");
        // A Popup has its own scene and does not inherit the main scene's app.css, so scope it here.
        FxUtils.applyAppStylesheet(card);

        timedPopup = new Popup();
        timedPopup.setAutoHide(true);
        timedPopup.setAutoFix(true);
        timedPopup.getContent().add(card);
    }

    private Button presetButton(int seconds) {
        Button button = new Button(seconds + "s");
        button.getStyleClass().add(Styles.BUTTON_OUTLINED);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setOnAction(_ -> secondsSpinner.getValueFactory().setValue(seconds));
        HBox.setHgrow(button, Priority.ALWAYS);
        return button;
    }

    private void commitTimed() {
        Spinners.commit(secondsSpinner); // flush any typed-but-uncommitted text before reading
        int seconds = secondsSpinner.getValue();
        timedPopup.hide();
        if (onTimedStart != null) {
            onTimedStart.accept(seconds);
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
