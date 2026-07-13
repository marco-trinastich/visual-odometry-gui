/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.converters;

import com.mtm.vogui.models.enums.settings.DevicePath;
import javafx.util.StringConverter;

/**
 * Converter for an <em>editable</em> device-path {@code ComboBox}: discovered devices render through
 * {@link DevicePath#value()} (their display name), and free text the user types parses back into a
 * {@link DevicePath} (name == id == the typed identifier). App-blind and reusable wherever a device
 * path is entered.
 */
public class DevicePathStringConverter extends StringConverter<DevicePath> {

    @Override
    public String toString(DevicePath devicePath) {
        return devicePath == null ? "" : devicePath.value();
    }

    @Override
    public DevicePath fromString(String string) {
        return (string == null || string.isBlank()) ? null : DevicePath.from(string.trim());
    }
}
