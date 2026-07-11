/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.context.settings.input.calibration;

import com.mtm.vogui.models.context.settings.common.PathSettings;
import com.mtm.vogui.models.interfaces.WithDefault;

/**
 * Calibration settings
 * <p/>
 * Options related to calibration.
 */
public class CalibrationSettings extends PathSettings implements WithDefault<CalibrationSettings> {

    public CalibrationSettings() {
        super();
    }

    public CalibrationSettings(CalibrationSettings calibration) {
        super(calibration);
    }
}
