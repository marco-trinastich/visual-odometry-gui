/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.toolbar;

import com.mtm.vogui.core.Core;
import com.mtm.vogui.gui.swing.state.GuiState;
import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.core.concurrency.NamedThreadFactory;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.utilities.CoreUtils;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Vo lifecycle commands behind the toolbar buttons: start/pause/reset/stop/clear/timed stop.
 * Owns the vo executor and the toolbar-state choreography; talks to widgets only through
 * {@link ToolbarView} intents. The toolbar is late-bound: the view wires its buttons to this
 * controller at construction, then hands itself back via {@code toolbar(view)}.
 */
public class VoController {

    private final AppContext context;
    private final Core core;
    private final GuiState guiState;

    // Visual Odometry executor
    private final ExecutorService voExecutor =
            Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.VO_EXECUTOR_THREAD));
    private Future<?> voTask;

    @Setter
    private ToolbarView toolbar;

    public VoController(AppContext context, Core core, GuiState guiState) {
        this.context = context;
        this.core = core;
        this.guiState = guiState;
    }

    public void start() {
        if (this.voTask == null || this.voTask.isDone()) {
            this.voTask = this.startVisualOdometry(false);
        }
    }

    public void pause() {
        if (this.context.state().processing().not(ProcessingState.Paused)) {
            // Notify pause to vo thread
            CoreUtils.setProcessingStateSafe(this.context, ProcessingState.Paused);
        } else {
            // Notify resume to vo thread
            CoreUtils.setProcessingStateSafe(this.context, ProcessingState.Running);
        }
        // Switch pause/resume icon
        this.toolbar.switchPauseIcon();
    }

    public void reset() {
        // Notify reset to vo thread
        CoreUtils.setResetRequested(this.context, true);
    }

    public void stop() {
        // Notify stop to vo thread; the vo task itself restores the toolbar on exit
        CoreUtils.setProcessingStateSafe(this.context, ProcessingState.Stopped);
    }

    public void clear() {
        if (this.context.state().processing().is(ProcessingState.Running) ||
                this.context.state().processing().is(ProcessingState.Paused)) {
            // Notify clear to vo thread; the vo task itself restores the toolbar on exit
            CoreUtils.setProcessingStateSafe(this.context, ProcessingState.Cleared);
        } else {
            this.guiState.dashboardView().clearAll();
            this.guiState.dashboardView().info().setAppStatus(AppStatus.Cleared);
            this.setReadyToolbar(false);
        }
    }

    public void timedStop() {
        Integer totalSeconds = this.toolbar.promptTimedSeconds();
        if (totalSeconds == null) {
            return;
        }

        // Start timed vo process
        this.startVisualOdometry(true);

        Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.VO_TIMED_STOP_THREAD)).submit(() -> {
            // Suspend thread until vo process is running (or error)
            this.toolbar.disableTimedAndRepaint();
            this.context.state().processing().waitUntil(
                    ProcessingState.Running,
                    ProcessingState.Error
            );
            if (this.context.state().processing().is(ProcessingState.Error))
                return;
            this.toolbar.showTimedCountdownStart(totalSeconds);

            // Start countdown
            AtomicInteger seconds = new AtomicInteger(0);
            ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
                    NamedThreadFactory.from(AppConstants.VO_TIMED_STOP_COUNTDOWN_THREAD));
            service.scheduleAtFixedRate(() -> timedStop(context, service, seconds, totalSeconds),
                    1, 1, TimeUnit.SECONDS);
        });
    }

    private @NotNull Future<?> startVisualOdometry(boolean isTimed) {
        // Signal setup phase and lock the whole toolbar until the setup outcome is known
        CoreUtils.setProcessingStateSafe(this.context, ProcessingState.Init);
        this.toolbar.lockAll();

        // Enable the processing controls only once setup succeeds and processing actually starts
        var toolbarWatcher = Executors.newSingleThreadExecutor(NamedThreadFactory.from(AppConstants.VO_TOOLBAR_THREAD));
        toolbarWatcher.submit(() -> {
            this.context.state().processing().waitUntilNot(ProcessingState.Init);
            SwingUtilities.invokeLater(() -> {
                // No-op if processing already ended (toolbar restored by the vo task itself)
                if (this.context.state().processing().is(ProcessingState.Running) ||
                        this.context.state().processing().is(ProcessingState.Paused)) {
                    this.toolbar.setRunning(isTimed);
                }
            });
        });
        toolbarWatcher.shutdown();

        // Run vo processing in dedicated thread
        return voExecutor.submit(() -> {
            try {
                this.core.start();
            } finally {
                // Always restore the toolbar, whatever the processing outcome
                SwingUtilities.invokeLater(() -> this.setReadyToolbar(null));
            }
        });
    }

    private void setReadyToolbar(Boolean clearEnabled) {
        clearEnabled = clearEnabled != null ? clearEnabled : this.guiState.dashboardView().trajectory().xzHasPoints();
        boolean timedEnabled = SourceType.Device.is(this.context.settings().input().source());

        this.toolbar.setReady(clearEnabled, timedEnabled);
    }

    private void timedStop(@NotNull AppContext context, ScheduledExecutorService service, AtomicInteger seconds,
                           int totalSeconds) {
        if (context.state().processing().is(ProcessingState.Paused)) {
            // Wait until resume
            service.shutdown();
            context.state().processing().waitUntilNot(ProcessingState.Paused);
            service.scheduleAtFixedRate(() -> timedStop(context, service, seconds, totalSeconds),
                    0, 1, TimeUnit.SECONDS);
        } else if (context.state().processing().not(ProcessingState.Running)) {
            // Stop if vo thread isn't running
            service.shutdown();
            return;
        }

        // Update counter
        int currSeconds = seconds.incrementAndGet();
        this.toolbar.updateTimedCountdown(totalSeconds - currSeconds);

        // If countdown ended
        if (currSeconds == totalSeconds) {
            // Stop capture and processing
            if (context.state().processing().is(ProcessingState.Running) ||
                    context.state().device().isRunning()) {
                try {
                    context.state().device().stop();
                } catch (CameraException _) {
                    // any camera exception will be managed by vo thread itself on cleanup phase
                }
            }
            service.shutdown();
        }
    }
}
