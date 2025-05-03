/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.core.integration.shared;

import com.mtm.vogui.models.constants.AppConstants;
import com.mtm.vogui.models.constants.Messages;
import com.mtm.vogui.models.core.concurrency.AwaitableBuffer;
import com.mtm.vogui.models.core.concurrency.NamedThreadFactory;
import com.mtm.vogui.models.core.integration.BufferStatus;
import io.quarkus.logging.Log;
import org.jetbrains.annotations.NotNull;
import org.openjdk.jol.info.GraphLayout;

import java.awt.image.BufferedImage;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class BufferMonitor {

    // Memory status
    private long heapInitialSize;
    private long heapMaxSize;
    private long imageSize;
    private long maxBufferItems;
    private Long gcLimitMaxBufferItems;

    // Instances
    private final AwaitableBuffer<?> buffer;
    private final BufferedImage capturedImage;
    private final Consumer<BufferStatus> consumer;

    // Scheduler
    private final ScheduledExecutorService scheduler;
    private Future<?> task;
    private long cycles;

    private final static double HEAP_OVERHEAD = 6;
    private final static int CYCLE_INTERVAL_MS = 100;
    private final static int LOG_INTERVAL_MS = 10000;

    private BufferMonitor(AwaitableBuffer<?> buffer, BufferedImage capturedImage, Consumer<BufferStatus> consumer) {
        this.buffer = buffer;
        this.capturedImage = capturedImage;
        this.consumer = consumer;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(
                NamedThreadFactory.from(AppConstants.BUFFER_MONITOR_THREAD));
    }

    public ScheduledExecutorService start() {
        if (this.consumer != null && (this.task == null || this.task.isDone())) {
            // Initialize memory status
            this.initMemoryStatus();

            // Monitoring cycle
            this.cycles = 0;
            this.task = this.scheduler.scheduleAtFixedRate(() -> this.consumer.accept(this.getBufferStatus()),
                    0, CYCLE_INTERVAL_MS, TimeUnit.MILLISECONDS);
        }

        return this.scheduler;
    }

    public void initMemoryStatus() {
        // Single image size
        this.imageSize = GraphLayout.parseInstance(capturedImage).totalSize();
        // Max java heap size
        this.heapMaxSize = Runtime.getRuntime().maxMemory();

        // Run a GC cycle to ensure we get correct heap size (especially after previous runs)
        System.gc();
        // Current java heap size (used+reserved)
        this.heapInitialSize = Runtime.getRuntime().totalMemory();

        // Compute max buffer items
        this.maxBufferItems = this.computeMaxBufferItems(this.heapMaxSize, this.heapInitialSize, this.imageSize,
                HEAP_OVERHEAD);
    }

    public BufferStatus getBufferStatus() {
        long bufferItems = this.buffer.size();
        long bufferSize = bufferItems * this.imageSize;
        long maxBufferItems = this.getMaxBufferItems();

        // Adjust max buffer size if needed
        long heapCurrentSize = Runtime.getRuntime().totalMemory();
        if (heapCurrentSize == this.heapMaxSize && this.gcLimitMaxBufferItems == null) {
            // Warning: full heap reserved by JVM -> perform gc and limit buffer items to current size
            System.gc();
            maxBufferItems = this.gcLimitMaxBufferItems = bufferItems;
        }
        if (heapCurrentSize < this.heapMaxSize && this.gcLimitMaxBufferItems != null) {
            this.gcLimitMaxBufferItems = null;
        }
        var bufferStatus = BufferStatus.from(
                this.imageSize,
                this.heapInitialSize,
                this.heapMaxSize,
                bufferSize,
                maxBufferItems * this.imageSize,
                bufferItems,
                maxBufferItems
        );

        this.logBufferStatus(bufferStatus);

        return bufferStatus;
    }

    public void logBufferStatus(BufferStatus bufferStatus) {
        if (cycles % (LOG_INTERVAL_MS / CYCLE_INTERVAL_MS) == 0) {
            Log.infof(Messages.BUFFER_MONITOR_LOG,
                    bufferStatus.heapInitialSize(),
                    bufferStatus.heapMaxSize(),
                    bufferStatus.bufferSize(),
                    bufferStatus.imageSize(),
                    bufferStatus.maxBufferSize(),
                    bufferStatus.maxBufferItems());
        }
        cycles++;
    }

    public long getMaxBufferItems() {
        return this.gcLimitMaxBufferItems != null ? this.gcLimitMaxBufferItems : this.maxBufferItems;
    }

    public long computeMaxBufferItems(long heapMaxSize, long heapInitialSize, long imageSize, double overhead) {
        return (long) (this.computeMaxBufferSize(heapMaxSize, heapInitialSize, overhead) / imageSize);
    }

    private double computeMaxBufferSize(long heapMaxSize, long heapInitialSize, double overhead) {
        // Calculate usable heap space in bytes
        return Math.max(heapMaxSize - (heapInitialSize * overhead), 0);
    }

    public static @NotNull BufferMonitor from(AwaitableBuffer<?> buffer, BufferedImage bufferedImage,
                                              Consumer<BufferStatus> consumer) {
        return new BufferMonitor(buffer, bufferedImage, consumer);
    }
}
