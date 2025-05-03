/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.core.concurrency;

import com.mtm.vogui.models.core.exceptions.BufferTimeoutException;
import com.mtm.vogui.utilities.CommonUtils;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

public class AwaitableBuffer<T> {
    private final Queue<T> buffer;
    private final Awaitable<Object> awaitable;
    private final static int BUFFER_WAIT_TIMEOUT = 10;
    public final static long INFINITE_BUFFER = Long.MAX_VALUE;

    public AwaitableBuffer() {
        this.buffer = new LinkedList<>();
        this.awaitable = new Awaitable<>(new Object());
    }

    public void push(T value) {
        this.buffer.offer(value);
        // Notify buffer change
        this.awaitable.set(new Object());
    }

    public T poll() {
        T element = this.buffer.poll();
        // Notify buffer change
        this.awaitable.set(new Object());
        return element;
    }

    public T pollLast() {
        // slight violation of queue contract (it shouldn't be possible to pollLast)
        T element = ((LinkedList<T>) this.buffer).pollLast();
        // Notify buffer change
        this.awaitable.set(new Object());
        return element;
    }

    public T peek() {
        return this.buffer.peek();
    }

    public void clear() {
        this.buffer.clear();
        // Notify buffer change
        this.awaitable.set(new Object());
    }

    public int size() {
        return this.buffer.size();
    }

    public boolean isEmpty() {
        return this.buffer.isEmpty();
    }

    public void awake() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        this.awaitable.set(new Object());
    }

    public boolean waitUntilFilledOrCondition(Supplier<Boolean> condition) throws BufferTimeoutException {
        var startTime = System.currentTimeMillis();

        boolean notInterrupted = true;

        // Suspend thread until buffer is filled, on timeout, or condition is met, whichever comes first
        while (this.isEmpty() && (notInterrupted = !isValid(condition))) {
            var currentTime = System.currentTimeMillis();
            if (CommonUtils.getSecsTimeDiff(startTime, currentTime) >= BUFFER_WAIT_TIMEOUT) {
                throw new BufferTimeoutException();
            }
            this.awaitable.waitUntilChange(BUFFER_WAIT_TIMEOUT);
        }

        return !notInterrupted;
    }

    private boolean isValid(Supplier<Boolean> condition) {
        return condition != null && condition.get();
    }
}
