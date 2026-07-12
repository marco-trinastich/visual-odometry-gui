/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.button.graphics;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import lombok.Getter;

import java.util.List;

@Getter
public enum IconSize {
    I_32(32),
    I_64(64),
    I_128(128),
    I_256(256);

    private final int value;

    IconSize(int value) {
        this.value = value;
    }

    public List<Integer> resolutions() {
        int resIndex = GuiConstants.HI_RES_VARIANTS.indexOf(this.value);
        return GuiConstants.HI_RES_VARIANTS.subList(resIndex, GuiConstants.HI_RES_VARIANTS.size());
    }
}
