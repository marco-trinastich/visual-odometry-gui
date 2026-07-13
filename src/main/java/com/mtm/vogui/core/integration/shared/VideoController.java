/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.shared;

import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.core.concurrency.Awaitable;
import com.mtm.vogui.models.core.integration.VideoCallBack;
import com.mtm.vogui.models.core.processing.fps.FpsCounter;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import io.quarkus.logging.Log;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class VideoController implements VideoCallBack {

    // The per-frame fps stats are computed every frame but only logged at this interval: at capture
    // rate a per-frame log floods the console (shared by the OpenCv and V4L4J camera paths).
    private static final long FPS_LOG_INTERVAL_MS = 5_000;

    @Getter
    private Dimension frameSize;
    private final Consumer<BufferedImage> imageConsumer;

    // FPS counter
    private FpsCounter counter;
    private FpsStatus fpsStatus;
    private long lastFpsLogMs;

    // State
    private boolean stopRequested;
    private final Awaitable<Boolean> running;

    private VideoController(@NotNull Consumer<BufferedImage> imageConsumer) {
        this.imageConsumer = imageConsumer;
        this.stopRequested = false;
        this.running = new Awaitable<>(false);
    }

    @Override
    public void init(int width, int height, ImageType<? extends ImageBase<?>> imageType) {
        // Set frame size
        this.frameSize = new Dimension(width, height);

        // Start fps counter thread
        this.counter = FpsCounter.with(this::updateCurrentFps).start();
        this.fpsStatus = this.counter.getStatus();

        // Set initialized
        this.running.set(true);
    }


    @Override
    public void nextFrame(ImageBase<?> leftImg, Object sourceData, long timeStamp) {
        // Get last device captured image
        BufferedImage capturedImage = (BufferedImage) sourceData;
        // Send received image to consumer
        this.imageConsumer.accept(capturedImage);
        // Update average FPS info
        this.updateAverageFps();
    }

    @Override
    public void stop() {
        this.stopRequested = true;
    }

    @Override
    public void stopped() {
        // It is absolutely necessary to always unlock any pending thread before exiting (running lock)
        this.stopRequested = false;
        this.running.set(false);
    }

    @Override
    public boolean stopRequested() {
        if (this.stopRequested) {
            this.counter.close();
            return true;
        } else {
            return false;
        }
    }

    public boolean isRunning() {
        return this.running.get();
    }

    public void waitUntilStarted() {
        this.running.waitUntil(true);
    }

    public void waitUntilStopped() {
        this.running.waitUntil(false);
    }

    public double getAverageFPS() {
        return this.fpsStatus.averageFPS();
    }

    public double getCurrentFPS() {
        return this.fpsStatus.currentFPS();
    }


    // Private

    private void updateAverageFps() {
        // Update fps info
        this.counter.addFrame();
        this.counter.addProcessedFrame();
        this.fpsStatus.setAverage(this.counter.getStatus());

        // Log to console, throttled: a per-frame log floods the terminal at capture rate.
        long now = System.currentTimeMillis();
        if (now - this.lastFpsLogMs >= FPS_LOG_INTERVAL_MS) {
            this.lastFpsLogMs = now;
            Log.infof(Messages.DEVICE_FPS_LOG,
                    this.fpsStatus.averageFPS(),
                    this.fpsStatus.currentFPS(),
                    this.fpsStatus.totalProcessed(),
                    this.fpsStatus.totalSeconds()
            );
        }
    }

    private void updateCurrentFps(@NotNull FpsStatus fpsStatus) {
        this.fpsStatus.currentFPS(fpsStatus.currentFPS());
        this.fpsStatus.inputCurrentFPS(fpsStatus.inputCurrentFPS());
    }


    public static @NotNull VideoController from(Consumer<BufferedImage> imageConsumer) {
        return new VideoController(imageConsumer);
    }
}
