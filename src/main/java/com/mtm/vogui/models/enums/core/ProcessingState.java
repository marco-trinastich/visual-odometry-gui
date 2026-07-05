/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.core;

import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;

@Getter
public enum ProcessingState implements WithValue, Comparable {
    Init("Init"),
    StandBy("StandBy"),
    Running("Running"),
    Paused("Paused"),
    Stopped("Stopped"),
    Cleared("Cleared"),
    Completed("Completed"),
    Error("Error");

    private final String value;

    ProcessingState(String value) {
        this.value = value;
    }
}
