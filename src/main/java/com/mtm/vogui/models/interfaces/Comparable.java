/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.interfaces;

public interface Comparable {
    default boolean is(Object other) {
        return this.equals(other);
    }
}
