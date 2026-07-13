/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.toolbar;

import com.mtm.vogui.core.Core;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.gui.fx.state.TrajectoryEvent;
import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.core.concurrency.NamedThreadFactory;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.utilities.CoreUtils;
import javafx.application.Platform;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * Vo lifecycle commands behind the toolbar buttons (JavaFX twin of
 * {@code gui.swing.features.controlpanel.toolbar.VoController}): start/pause/reset/stop/clear/timed
 * stop. Owns the vo executor and the toolbar-state choreography; drives widgets only through
 * {@link ToolbarView} intents, marshalling to the FX Application Thread with {@link Platform#runLater}.
 * <p>
 * Differences from the Swing controller: status goes through {@link GuiState#appStatusProperty()}
 * (the status bar) instead of the dashboard info panel; the idle-branch "clear" wipes the trajectory
 * charts + points log directly (via {@link GuiState}), and {@code clearEnabled} tracks the ground
 * track's {@code xzHasPoints}. The Device-only timed button tracks the input source via
 * {@link GuiState#inputSourceProperty()}.
 */
public class VoController {

    private final AppContext context;
    private final Core core;
    private final GuiState guiState;
    private final ToolbarView toolbar;
    private final Runnable onRunStarted;
    private final Runnable onRunCleared;
    private final BooleanSupplier clearEnabled;

    private final ExecutorService voExecutor =
            Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.VO_EXECUTOR_THREAD));
    private Future<?> voTask;

    public VoController(AppContext context, Core core, GuiState guiState, ToolbarView toolbar,
                        Runnable onRunStarted, Runnable onRunCleared, BooleanSupplier clearEnabled) {
        this.context = context;
        this.core = core;
        this.guiState = guiState;
        this.toolbar = toolbar;
        this.onRunStarted = onRunStarted;
        this.onRunCleared = onRunCleared;
        this.clearEnabled = clearEnabled;

        // Initial ready state; the Device-only timed button follows the current input source.
        toolbar.setReady(false, isDeviceSource());
        // React to the property's NEW value, not a settings re-read: the bound inputSource can propagate
        // here before the separate viewModel listener commits the choice to settings, so isDeviceSource()
        // would read the previous source and invert the button (Video enabled / Device disabled).
        guiState.inputSourceProperty().addListener((_, _, source) -> {
            if (isIdle()) {
                toolbar.setTimedEnabled(SourceType.Device == source);
            }
        });
    }

    public void start() {
        if (voTask == null || voTask.isDone()) {
            voTask = startVisualOdometry(false);
        }
    }

    public void pause() {
        if (context.state().processing().not(ProcessingState.Paused)) {
            CoreUtils.setProcessingStateSafe(context, ProcessingState.Paused);
        } else {
            CoreUtils.setProcessingStateSafe(context, ProcessingState.Running);
        }
        toolbar.switchPauseText();
    }

    public void reset() {
        CoreUtils.setResetRequested(context, true);
    }

    public void stop() {
        // The vo task itself restores the toolbar on exit.
        CoreUtils.setProcessingStateSafe(context, ProcessingState.Stopped);
    }

    public void clear() {
        // Telemetry stays available from Start through Stop; only Clear returns focus to Settings.
        onRunCleared.run();
        if (context.state().processing().is(ProcessingState.Running)
                || context.state().processing().is(ProcessingState.Paused)) {
            // The vo task itself restores the toolbar on exit.
            CoreUtils.setProcessingStateSafe(context, ProcessingState.Cleared);
        } else {
            // Idle clear (no run to route through the core): wipe the trajectory charts and the points
            // log directly, then reset status. Runs on the FX thread (button handler).
            guiState.emitTrajectory(new TrajectoryEvent.ClearAll());
            guiState.trackedPoints().clear();
            guiState.appStatusProperty().set(AppStatus.Cleared);
            setReadyToolbar();
        }
    }

    /** Opens the toolbar's timed-stop popover; the actual run starts once the user commits a duration. */
    public void timedStop() {
        toolbar.openTimedPopover(this::startTimed);
    }

    private void startTimed(int totalSeconds) {
        if (totalSeconds <= 0) {
            return;
        }

        startVisualOdometry(true);

        Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.VO_TIMED_STOP_THREAD)).submit(() -> {
            // Suspend until vo is running (or errored).
            Platform.runLater(toolbar::disableTimed);
            context.state().processing().waitUntil(ProcessingState.Running, ProcessingState.Error);
            if (context.state().processing().is(ProcessingState.Error)) {
                return;
            }
            Platform.runLater(() -> toolbar.showTimedCountdownStart(totalSeconds));

            AtomicInteger seconds = new AtomicInteger(0);
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
                    NamedThreadFactory.from(AppConstants.VO_TIMED_STOP_COUNTDOWN_THREAD));
            service.scheduleAtFixedRate(() -> timedCountdown(service, seconds, totalSeconds),
                    1, 1, TimeUnit.SECONDS);
        });
    }

    private @NotNull Future<?> startVisualOdometry(boolean isTimed) {
        // Every Start focuses the Telemetry tab (and enables it the first time).
        onRunStarted.run();

        // Signal setup and lock the whole toolbar until the setup outcome is known.
        CoreUtils.setProcessingStateSafe(context, ProcessingState.Init);
        toolbar.lockAll();

        // Enable the processing controls only once setup succeeds and processing actually starts.
        var toolbarWatcher = Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.VO_TOOLBAR_THREAD));
        toolbarWatcher.submit(() -> {
            context.state().processing().waitUntilNot(ProcessingState.Init);
            Platform.runLater(() -> {
                // No-op if processing already ended (toolbar restored by the vo task itself).
                if (context.state().processing().is(ProcessingState.Running)
                        || context.state().processing().is(ProcessingState.Paused)) {
                    toolbar.setRunning(isTimed);
                }
            });
        });
        toolbarWatcher.shutdown();

        return voExecutor.submit(() -> {
            try {
                core.start();
            } finally {
                // Always restore the toolbar, whatever the processing outcome.
                Platform.runLater(this::setReadyToolbar);
            }
        });
    }

    private void setReadyToolbar() {
        // Clear is enabled only when the trajectory holds points (the ground track's xzHasPoints).
        toolbar.setReady(clearEnabled.getAsBoolean(), isDeviceSource());
    }

    private void timedCountdown(ScheduledExecutorService service, AtomicInteger seconds, int totalSeconds) {
        if (context.state().processing().is(ProcessingState.Paused)) {
            // Suspend the countdown until resume.
            service.shutdown();
            context.state().processing().waitUntilNot(ProcessingState.Paused);
            Executors.newSingleThreadScheduledExecutor(
                            NamedThreadFactory.from(AppConstants.VO_TIMED_STOP_COUNTDOWN_THREAD))
                    .scheduleAtFixedRate(() -> timedCountdown(service, seconds, totalSeconds),
                            0, 1, TimeUnit.SECONDS);
            return;
        } else if (context.state().processing().not(ProcessingState.Running)) {
            service.shutdown();
            return;
        }

        int currSeconds = seconds.incrementAndGet();
        Platform.runLater(() -> toolbar.updateTimedCountdown(totalSeconds - currSeconds));

        if (currSeconds >= totalSeconds) {
            // Countdown ended: stop capture and processing.
            if (context.state().processing().is(ProcessingState.Running) || deviceRunning()) {
                try {
                    context.state().device().stop();
                } catch (CameraException _) {
                    // Any camera exception is handled by the vo thread itself on cleanup.
                }
            }
            service.shutdown();
        }
    }

    private boolean deviceRunning() {
        return context.state().device() != null && context.state().device().isRunning();
    }

    private boolean isDeviceSource() {
        return SourceType.Device == context.settings().input().source();
    }

    private boolean isIdle() {
        return context.state().processing().not(ProcessingState.Running)
                && context.state().processing().not(ProcessingState.Paused)
                && context.state().processing().not(ProcessingState.Init);
    }
}
