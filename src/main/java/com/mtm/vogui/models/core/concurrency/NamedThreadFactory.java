/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.concurrency;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private static final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private final boolean single;

    /**
     * Constructor accepting the name of the threads that will be created by this {@link ThreadFactory}
     *
     * @param name Name of threads
     */
    private NamedThreadFactory(String name) {
        this(name, true);
    }

    /**
     * Constructor accepting the prefix of the threads that will be created by this {@link ThreadFactory}
     * and whether is a single threaded factory or multithreaded
     *
     * @param namePrefix Prefix for names of threads
     */
    public NamedThreadFactory(String namePrefix, boolean single) {
        this.namePrefix = namePrefix;
        this.single = single;
    }

    /**
     * Returns a new thread using a name as specified by this factory {@inheritDoc}
     */
    public Thread newThread(@NotNull Runnable runnable) {
        return new Thread(runnable, namePrefix + (single ? "" : " thread-" + threadNumber.getAndIncrement()));
    }

    public static @NotNull NamedThreadFactory from(String name) {
        return new NamedThreadFactory(name);
    }
}
