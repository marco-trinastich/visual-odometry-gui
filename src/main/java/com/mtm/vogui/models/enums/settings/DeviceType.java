/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.settings;

import com.mtm.vogui.models.interfaces.Comparable;
import com.mtm.vogui.models.interfaces.WithValue;
import lombok.Getter;

@Getter
public enum DeviceType implements WithValue, Comparable {
    // Constant names follow the codebase Cv-suffix convention (class prefixes, Jackson
    // persistence); display values use the official project spellings
    BoofCv("BoofCV"),
    OpenCv("OpenCV"),
    V4L4J("V4L4J");

    private final String value;

    DeviceType(String value) {
        this.value = value;
    }
}
