/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.interfaces;

import jakarta.enterprise.inject.spi.CDI;

@SuppressWarnings("unchecked")
public interface WithDefault<T> {

    default T getDefault() {
        return (T) CDI.current().select(this.getClass()).get();
    }
}
