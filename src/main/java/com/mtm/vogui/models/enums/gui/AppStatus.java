/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.gui;

import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.interfaces.WithValue;
import com.mtm.vogui.models.core.exceptions.BufferTimeoutException;
import com.mtm.vogui.models.core.exceptions.UnknownSourceException;
import com.mtm.vogui.models.core.exceptions.VoProcessingException;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public enum AppStatus implements WithValue {
    // Processing
    Ready("Ready."),
    Init("Initializing processing..."),
    Processing("Processing..."),
    Paused("Processing paused."),
    Stopped("Processing stopped."),
    VoReset("VO context reset requested."),
    VoFailed("VO failed."),
    VoException("Visual Odometry processing error/exception. Check parameters."),
    BufferException("Buffer timeout exception. Check device settings."),
    Cleared("Cleared."),
    Completed("Processing completed."),
    Invalid("Invalid state."),

    // Processing checks
    ValidSettings("Settings check passed."),
    ValidCalibration("Calibration successfully opened."),
    ValidVideo("Video successfully opened."),
    ValidDevice("Device opened successfully."),
    ValidTracker("Tracker setup passed."),
    ValidVo("Visual Odometry setup passed."),
    InvalidSettings("Settings error. Could not start processing."),
    InvalidCalibration("Error opening Calibration file. Could not start processing."),
    InvalidVideo("Error opening video file. Could not start processing."),
    InvalidDevice("Error opening device. Could not start processing."),
    InvalidTracker("Error setting up tracker. Could not start processing."),
    InvalidVo("Error setting up Visual Odometry. Could not start processing."),
    UnknownDevice("Error opening device. Unknown device specified."),
    VoUnexpectedError("Unexpected exception occurred during Visual Odometry processing."),
    VoGenericError("Generic error occurred during Visual Odometry processing."),

    // Settings management
    XMLSettingsLoaded("XML settings successfully loaded."),
    XMLSettingsNotFound("Error: XML settings file not found."),
    XMLSettingsLoadError("Error loading XML settings."),
    XMLSettingsSaved("XML settings successfully saved."),
    XMLSettingsSaveError("Error saving XML settings."),
    DATSettingsLoaded("DAT settings successfully loaded."),
    DATSettingsNotFound("Error: DAT settings file not found."),
    DATSettingsLoadError("Error loading DAT settings."),
    DATSettingsSaved("DAT settings successfully saved."),
    DATSettingsSaveError("Error saving DAT settings."),
    SettingsReset("Settings successfully reset to default."),
    SettingsResetError("Error resetting settings to default."),

    Empty("");

    private final String value;

    AppStatus(String value) {
        this.value = value;
    }

    public static AppStatus from(ProcessingState state) {
        return from(state, null);
    }

    public static AppStatus from(@NotNull ProcessingState state, Exception ex) {
        AppStatus message;

        if (ex == null) {
            message = switch (state) {
                case StandBy -> AppStatus.Ready;
                case Running -> AppStatus.Processing;
                case Paused -> AppStatus.Paused;
                case Stopped -> AppStatus.Stopped;
                case Cleared -> AppStatus.Cleared;
                case Completed -> AppStatus.Completed;
                case Error -> AppStatus.VoGenericError;
            };
        } else {
            message = switch (ex) {
                case BufferTimeoutException ignored -> AppStatus.BufferException;
                case VoProcessingException ignored -> AppStatus.VoException;
                case UnknownSourceException ignored -> AppStatus.UnknownDevice;
                default -> AppStatus.VoUnexpectedError;
            };
        }

        return message;
    }
}
