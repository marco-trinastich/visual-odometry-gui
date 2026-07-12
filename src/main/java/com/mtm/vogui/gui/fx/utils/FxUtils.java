/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.utils;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * FX Application Thread helpers for code running on worker threads.
 */
public final class FxUtils {

    private FxUtils() {
    }

    /**
     * Runs the action on the FX thread and blocks the calling worker thread until it completes.
     * Needed for modal dialogs whose answer the worker must wait for.
     */
    public static void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Runs the supplier on the FX thread, blocking the calling worker thread for the result.
     */
    public static <T> T callOnFxThreadAndWait(Supplier<T> action) {
        AtomicReference<T> result = new AtomicReference<>();
        runOnFxThreadAndWait(() -> result.set(action.get()));
        return result.get();
    }

    /**
     * Coalesced hand-off to the FX thread: high-frequency producers overwrite the pending value
     * and at most one FX job is queued at a time, so only the latest value is ever delivered
     * (same mechanism {@code javafx.concurrent.Task} uses internally; avoids runLater flooding).
     */
    public static <T> Consumer<T> coalescedFxConsumer(Consumer<T> fxThreadAction) {
        AtomicReference<T> pending = new AtomicReference<>();
        return value -> {
            if (pending.getAndSet(value) == null) {
                Platform.runLater(() -> fxThreadAction.accept(pending.getAndSet(null)));
            }
        };
    }
}
