/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.settings.core.input.calibration;

import com.mtm.vogui.models.constants.SettingsConstants;
import com.mtm.vogui.models.interfaces.WithDefault;
import com.mtm.vogui.models.settings.core.common.PathSettings;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Calibration settings
 * <p/>
 * Options related to calibration.
 */
@Dependent
public class CalibrationSettings extends PathSettings implements Serializable, WithDefault<CalibrationSettings> {

    @Inject
    public CalibrationSettings() {
        super();
    }

    public CalibrationSettings(CalibrationSettings calibration) {
        super(calibration);
    }

    @Override
    protected String @NotNull [] defaultPaths() {
        // Default calibrations list
        return SettingsConstants.DEFAULT_CALIBRATION_PATHS;
    }
}
