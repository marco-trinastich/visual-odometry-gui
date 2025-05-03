/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.gui;

import lombok.Getter;

@Getter
public enum PanelBorder {
    Rect("Rect"),
    Circle("Circle");

    private final String value;

    PanelBorder(String value) {
        this.value = value;
    }
}
