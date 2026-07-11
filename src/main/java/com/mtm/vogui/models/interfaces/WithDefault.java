/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.interfaces;

@SuppressWarnings("unchecked")
public interface WithDefault<T> {

    /**
     * A fresh instance with default values, built through the no-arg constructor
     * every implementation is required to provide.
     */
    default T getDefault() {
        try {
            return (T) this.getClass().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException exc) {
            throw new IllegalStateException("Missing no-arg constructor for " + this.getClass().getName(), exc);
        }
    }
}
