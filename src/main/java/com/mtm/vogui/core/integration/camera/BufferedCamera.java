package com.mtm.vogui.core.integration.camera;

import com.mtm.vogui.core.integration.shared.BufferMonitor;
import com.mtm.vogui.models.core.concurrency.Awaitable;
import com.mtm.vogui.models.core.concurrency.AwaitableBuffer;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.core.exceptions.BufferTimeoutException;
import com.mtm.vogui.models.core.exceptions.CameraException;
import com.mtm.vogui.models.core.exceptions.CameraStartException;
import com.mtm.vogui.models.settings.Settings;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

/**
 * BufferedCamera
 * <p>
 * Abstract class that implements a generic contract for a buffering-based video device.
 *
 * @author Marco Trinastich 2024
 */
public abstract class BufferedCamera {

    protected final Settings settings;
    private final Consumer<BufferedImage> guiRenderer;
    private final Consumer<BufferStatus> bufferRenderer;

    // Buffer
    private final AwaitableBuffer<BufferedImage> buffer;
    private ScheduledExecutorService bufferMonitor;
    private long maxBufferItems;


    // State
    protected final Awaitable<Boolean> stopRequested;
    protected final Awaitable<Boolean> running;

    protected BufferedCamera(@NotNull Settings settings,
                             Consumer<BufferedImage> guiRenderer,
                             Consumer<BufferStatus> bufferRenderer) {
        this.settings = settings;
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
        var processingState = this.settings.state().processing();
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
        for (long i = 0; i <= excess; i++) {
            this.buffer.poll();
        }
    }

    @SuppressWarnings("unused")
    protected void stripBufferTop(long limit) {
        long excess = this.buffer.size() - limit;
        for (long i = 0; i <= excess; i++) {
            this.buffer.pollLast();
        }
    }
}
