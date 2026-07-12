/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.gui;

import com.mtm.vogui.utilities.AnsiUtils;

/**
 * UI toolkit the application boots with ({@code config.ui}). Swing is the legacy parity
 * reference during the JavaFX migration. Constant names are the accepted config values;
 * each carries its display name and brand colour for boot/log banners (JavaFX's orange,
 * Java's blue for Swing, which has no colour of its own).
 */
public enum UiToolkit {
    JavaFx("JavaFX", AnsiUtils.ORANGE),
    Swing("Swing", AnsiUtils.BLUE);

    private final String displayName;
    private final int brandColour;

    UiToolkit(String displayName, int brandColour) {
        this.displayName = displayName;
        this.brandColour = brandColour;
    }

    public String displayName() {
        return this.displayName;
    }

    /** ANSI 256-colour palette code for boot/log banners. */
    public int brandColour() {
        return this.brandColour;
    }
}
