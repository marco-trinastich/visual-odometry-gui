/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.core;

/**
 * Outcome of a calibration file load attempt
 */
public enum CalibrationLoadResult {
    Ok,
    NotFound,
    LegacyXmlFormat,
    Invalid;

    public boolean isOk() {
        return this == Ok;
    }
}
