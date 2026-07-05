/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.input.calibration;

import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.models.settings.core.common.PathSettings;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 * Calibration settings
 * <p/>
 * Options related to calibration.
 */
@Dependent
public class CalibrationSettings extends PathSettings implements WithDefault<CalibrationSettings> {

    @Inject
    public CalibrationSettings() {
        super();
    }

    public CalibrationSettings(CalibrationSettings calibration) {
        super(calibration);
    }
}
