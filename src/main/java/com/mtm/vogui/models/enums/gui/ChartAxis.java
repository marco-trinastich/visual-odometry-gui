/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.gui;

import lombok.Getter;

@Getter
public enum ChartAxis {
    X("X"),
    Y("Y"),
    Z("Z"),
    Frame("frame"),
    Seconds("seconds");

    private final String value;

    ChartAxis(String value) {
        this.value = value;
    }
}
