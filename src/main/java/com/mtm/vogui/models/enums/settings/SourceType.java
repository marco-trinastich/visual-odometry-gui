/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.settings;

import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;

@Getter
public enum SourceType implements WithValue, Comparable {
    Video("Video"),
    Device("Device");

    private final String value;

    SourceType(String value) {
        this.value = value;
    }
}
