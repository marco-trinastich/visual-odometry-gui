package com.mtm.vogui.models.core.concurrency;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Supplier;

public class Awaitable<T> {
    private final Object mutex;
    private volatile T value;

    public Awaitable(T value) {
        this.mutex = new Object();
        this.value = value;
    }

    public T get() {
        // Return value without synchronization
        return this.value;
    }

    public void set(T value) {
        synchronized (this.mutex) {
            // Resume any blocked thread
            this.value = value;
            this.mutex.notifyAll();
        }
    }

    public boolean compareAndSet(T value) {
        synchronized (this.mutex) {
            if (!this.value.equals(value)) {
                // Set and resume any blocked thread
                this.value = value;
                this.mutex.notifyAll();
                return false;
            }
            return true;
        }
    }

    public boolean is(T value) {
        return this.value.equals(value);
    }

    public boolean not(T value) {
        return !this.is(value);
    }

    public void waitUntil(T value) {
        // Suspend thread until state changes to desired value
        this.waitUntilCondition(() -> this.is(value), null);
    }

    @SafeVarargs
    public final void waitUntil(T... values) {
        // Suspend thread until state changes to one among desired values
        this.waitUntilCondition(() -> Arrays.stream(values).anyMatch(this::is), null);
    }

    public void waitUntilNot(T value) {
        // Suspend thread until state becomes different from value
        this.waitUntilCondition(() -> !this.is(value), null);
    }

    public void waitUntilChange() {
        this.waitUntilChange(null);
    }

    public void waitUntilChange(Integer timeoutSecs) {
        // Suspend thread until state changes
        var value = this.value;
        this.waitUntilCondition(() -> !this.is(value), timeoutSecs != null ? timeoutSecs * 1000L : null);
    }

    public void waitUntilCondition(@NotNull Supplier<Boolean> condition) {
        this.waitUntilCondition(condition, null);
    }

    @SneakyThrows
    public void waitUntilCondition(@NotNull Supplier<Boolean> condition, Long timeoutMillis) {
        synchronized (this.mutex) {
            while (!condition.get()) {
                // Suspend thread until condition is satisfied
                this.mutex.wait(timeoutMillis != null ? timeoutMillis : 0L);
            }
        }
    }
}
