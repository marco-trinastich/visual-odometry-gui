/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.camera;

import com.mtm.vogui.core.integration.shared.BufferMonitor;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.core.concurrency.Awaitable;
import com.mtm.vogui.models.core.concurrency.AwaitableBuffer;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.core.exceptions.BufferTimeoutException;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.core.exceptions.CameraStartException;
import com.mtm.vogui.utilities.ImageUtils;
import io.quarkus.logging.Log;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * BufferedCamera
 * <p>
 * Abstract class that implements a generic contract for a buffering-based video device.
 */
public abstract class BufferedCamera {

    protected final AppContext context;
    private final Consumer<BufferedImage> guiRenderer;
    private final Consumer<BufferStatus> bufferRenderer;

    // Buffer
    private final AwaitableBuffer<BufferedImage> buffer;
    private ScheduledExecutorService bufferMonitor;
    private long maxBufferItems;


    // State
    protected final Awaitable<Boolean> stopRequested;
    protected final Awaitable<Boolean> running;

    // Warmup gate: a device reopened while still warm can deliver a burst of black frames
    // before the sensor actually streams (observed: ~290 frames in ~300ms on macOS AVFoundation).
    // Feeding them to the vo engine wedges it into a failed state, so they are kept out of the
    // buffer until real content shows up — with a time budget so a legitimately dark scene
    // (fail-open) is let through anyway.
    private static final double WARMUP_LUMINANCE_THRESHOLD = 5.0;
    private static final long WARMUP_TIMEOUT_MS = 2000;
    private boolean warmupDone;
    private long warmupStartMs;
    private long warmupDiscarded;

    protected BufferedCamera(@NotNull AppContext context,
                             Consumer<BufferedImage> guiRenderer,
                             Consumer<BufferStatus> bufferRenderer) {
        this.context = context;
        this.guiRenderer = guiRenderer;
        this.bufferRenderer = bufferRenderer;
        this.buffer = new AwaitableBuffer<>();
        this.stopRequested = new Awaitable<>(false);
        this.running = new Awaitable<>(false);
    }

    /**
     * Starts the device and synchronously waits for it to boot up.
     * <p>
     * Note: before device startup the buffer should be cleared with {@code clearBuffer}, and immediately afterward
     * the {@code running} state should be set to true.
     * Implementations can {@code startBufferMonitor} to monitor the state of the buffer.
     *
     * @return {@code this} device instance
     */
    public abstract BufferedCamera start() throws CameraStartException;

    /**
     * Stops the device and synchronously waits for it to stop.
     * <p>
     * Note: implementations should wait for the device stop and immediately thereafter the {@code running} state
     * should be reset to stopped with {@code resetState}.
     * In addition, all processes waiting for the buffer must be notified ({@code awakeBufferWaiters}) that a stop has
     * occurred.
     */
    public abstract void stop() throws CameraException;

    /**
     * Gets the next image in the buffer to be consumed
     *
     * @return next image provided by the device
     */
    public BufferedImage nextImage() {
        return this.buffer.poll();
    }

    /**
     * Determines whether there is a next image in the buffer to be consumed or whether this may come in the future.
     *
     * @return {@code true} as long as the capture is in progress or the buffer is not empty
     */
    public boolean hasNext() {
        // Until capture is running or buffer isn't empty, there is a next image to consume
        return this.running.get() || !this.buffer.isEmpty();
    }

    /**
     * Gets the instantaneous FPS rate of the device.
     *
     * @return current fps value
     */
    public abstract double getCurrentFPS();

    /**
     * Gets the average FPS rate of the device.
     *
     * @return average fps value
     */
    public abstract double getAverageFPS();

    /**
     * Gets the current frame size of the device.
     *
     * @return frame size
     */
    public abstract Dimension getFrameSize();

    /**
     * Synchronously waits for the buffer to fill.
     *
     * @return {@code true} if interrupted, {@code false} otherwise (buffer filled or timeout)
     * @throws BufferTimeoutException timeout exception
     */
    public boolean waitBuffer() throws BufferTimeoutException {
        // Suspend thread until buffer is filled, times out or the camera/vo thread are not running
        var processingState = this.context.state().processing();
        return this.buffer.waitUntilFilledOrCondition(() ->
                !this.running.get() || processingState.not(ProcessingState.Running)
        );
    }

    /**
     * Awakens processes waiting for the buffer to fill up.
     */
    public void awakeBufferWaiters() {
        this.buffer.awake();
    }

    /**
     * Empties the device buffer.
     */
    public void clearBuffer() {
        this.buffer.clear();
    }

    /**
     * Returns whether the device is running.
     */
    public boolean isRunning() {
        return this.running.get();
    }

    /**
     * Resets device status to stopped and notifies all buffer waiting threads.
     */
    protected void captureStopped() {
        this.stopRequested.set(false);
        this.running.set(false);
        this.awakeBufferWaiters();

        // Reset buffer monitoring
        if (this.bufferMonitor != null) {
            this.bufferMonitor.shutdown();
            this.bufferMonitor = null;
        }
        if (this.bufferRenderer != null) {
            this.bufferRenderer.accept(null);
        }
    }

    /**
     * Adds {@code capturedImage} to device buffer and displays it if {@code guiRenderer} has been provided.
     *
     * @param capturedImage last image received from the device
     */
    protected void fillBufferAndRender(BufferedImage capturedImage) {
        if (this.bufferMonitor == null) {
            // Start buffer monitor
            this.bufferMonitor = BufferMonitor.from(this.buffer, capturedImage, this::monitorBufferStatus).start();
        }

        if (!this.warmupDone && this.isWarmupFrame(capturedImage)) {
            // Kept out of the buffer but still rendered: the preview stays honest
            if (this.guiRenderer != null) {
                this.guiRenderer.accept(capturedImage);
            }
            return;
        }

        if (this.buffer.size() >= this.maxBufferItems) {
            // Buffer overrun, discard frames
            this.stripBufferBottom(this.maxBufferItems);
        }
        if (this.maxBufferItems > 0) {
            // Fill buffer
            this.buffer.push(capturedImage);
        }

        // Render
        if (this.guiRenderer != null) {
            this.guiRenderer.accept(capturedImage);
        }
    }

    private boolean isWarmupFrame(BufferedImage capturedImage) {
        if (this.warmupStartMs == 0) {
            this.warmupStartMs = System.currentTimeMillis();
        }
        long elapsed = System.currentTimeMillis() - this.warmupStartMs;

        if (elapsed < WARMUP_TIMEOUT_MS &&
                ImageUtils.meanLuminance(capturedImage) <= WARMUP_LUMINANCE_THRESHOLD) {
            this.warmupDiscarded++;
            return true;
        }

        // First real frame (or budget exhausted): gate stays open, luminance is never sampled again
        this.warmupDone = true;
        if (this.warmupDiscarded > 0) {
            Log.infof(Messages.DEVICE_WARMUP_LOG, this.warmupDiscarded, elapsed);
        }
        return false;
    }

    protected void monitorBufferStatus(BufferStatus bufferStatus) {
        this.maxBufferItems = bufferStatus != null ?
                bufferStatus.maxBufferItems() :
                AwaitableBuffer.INFINITE_BUFFER;

        if (this.bufferRenderer != null) {
            this.bufferRenderer.accept(bufferStatus);
        }
    }


    protected void stripBufferBottom(long limit) {
        long excess = this.buffer.size() - limit;
        for (long i = 0; i < excess; i++) {
            this.buffer.poll();
        }
    }
}
