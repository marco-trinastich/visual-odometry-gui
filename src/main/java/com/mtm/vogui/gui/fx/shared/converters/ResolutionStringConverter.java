/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.converters;

import com.mtm.vogui.models.enums.settings.resolution.CustomResolution;
import com.mtm.vogui.models.interfaces.Resolution;
import javafx.util.StringConverter;

/**
 * Converter for an <em>editable</em> resolution {@code ComboBox}: presets render through
 * {@link Resolution#value()} ("W x H"), and free text the user types parses back into a
 * {@link CustomResolution}. Unparseable text yields {@code null} (the combo keeps no value) rather
 * than committing a bogus 0x0 resolution. App-blind and reusable wherever a resolution is entered.
 */
public class ResolutionStringConverter extends StringConverter<Resolution> {

    @Override
    public String toString(Resolution resolution) {
        return resolution == null ? "" : resolution.value();
    }

    @Override
    public Resolution fromString(String string) {
        if (string == null || string.isBlank()) {
            return null;
        }
        Resolution parsed = CustomResolution.from(string.trim());
        return (parsed.width() > 0 && parsed.height() > 0) ? parsed : null;
    }
}
