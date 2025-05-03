/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.processing.fps;

import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.core.concurrency.NamedThreadFactory;
import com.mtm.vogui.utilities.CoreUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class FpsCounter implements AutoCloseable {
    @Getter
    private int totalFrames;
    @Getter
    private int totalProcessed;
    private int partialProcessed;
    private long startTime;
    private long pauseTime;

    // Scheduler
    @Setter
    private Consumer<FpsStatus> periodicTask;
    private final ScheduledExecutorService scheduler;
    @Getter
    private Future<?> task;

    private FpsCounter() {
        this(null);
    }

    private FpsCounter(Consumer<FpsStatus> periodicTask) {
        this.periodicTask = periodicTask;
        this.startTime = System.currentTimeMillis();
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                NamedThreadFactory.from(AppConstants.FPS_COUNTER_THREAD));
    }

    public FpsCounter start() {
        // Spawn new thread only if not already running
        if (this.task == null || this.task.isDone()) {
            this.task = this.scheduler.scheduleAtFixedRate(this::scheduledTask, 1, 1, TimeUnit.SECONDS);
        }

        return this;
    }

    public void pause() {
        this.pauseTime = System.currentTimeMillis();
    }

    public void resume() {
        this.startTime += System.currentTimeMillis() - this.pauseTime;
    }

    @Override
    @SneakyThrows
    public void close() {
        CoreUtils.shutdownAndWait(this.scheduler);
    }

    public void addFrame() {
        this.totalFrames++;
    }

    public FpsStatus addProcessedFrame() {
        this.totalProcessed++;
        this.partialProcessed++;

        return this.getStatus();
    }

    public FpsStatus getStatus() {
        double totalSeconds = this.getTotalSeconds();
        return FpsStatus.builder()
                .totalFrames(this.totalFrames)
                .totalProcessed(this.totalProcessed)
                .totalSeconds(totalSeconds)
                .averageFPS(this.getAverageFPS(totalSeconds))
                .currentFPS(this.getCurrentFPS())
                .inputAverageFPS(this.getAverageFPS(totalSeconds))
                .inputCurrentFPS(this.getCurrentFPS())
                .build();
    }

    private double getTotalSeconds() {
        // Return total elapsed seconds from timer creation
        return (System.currentTimeMillis() - this.startTime) * 0.001;
    }

    private double getAverageFPS(double totalSeconds) {
        // Return average processing rate per second
        return this.totalProcessed / totalSeconds;
    }

    private int getCurrentFPS() {
        // Return current processing rate per second
        return this.partialProcessed;
    }

    private void scheduledTask() {
        // Running every second

        // Run custom task if any
        if (this.periodicTask != null) {
            this.periodicTask.accept(this.getStatus());
        }

        // Reset partial processed counter
        this.partialProcessed = 0;
    }

    public static @NotNull FpsCounter build() {
        return new FpsCounter();
    }

    public static @NotNull FpsCounter with(Consumer<FpsStatus> periodicTask) {
        return new FpsCounter(periodicTask);
    }
}
