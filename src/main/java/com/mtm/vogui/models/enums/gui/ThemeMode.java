/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.gui;

import lombok.Getter;

/**
 * UI theme selection, persisted in settings. {@code AUTO} follows the OS colour scheme; {@code LIGHT}
 * and {@code DARK} pin a fixed theme. Applied by {@code gui.fx.ThemeManager} (kept toolkit-agnostic
 * here so the persisted settings never reach into the GUI layer).
 */
@Getter
public enum ThemeMode {
    AUTO("Auto"),
    LIGHT("Light"),
    DARK("Dark");

    private final String label;

    ThemeMode(String label) {
        this.label = label;
    }
}
