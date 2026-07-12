/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.button;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.button.graphics.IconSize;
import com.mtm.vogui.gui.swing.utils.SwingUtils;

import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class BufferedImageButton extends ImageButton {
    public BufferedImageButton(String defaultIcon) {
        this(defaultIcon, null);
    }

    public BufferedImageButton(String defaultIcon, String disabledIcon) {
        this(defaultIcon, disabledIcon, null);
    }

    public BufferedImageButton(String defaultIcon, String disabledIcon, String altDefaultIcon) {
        this(defaultIcon, null, disabledIcon, altDefaultIcon, null, null);
    }

    public BufferedImageButton(String defaultIcon, String hoverIcon, String disabledIcon,
                               String altDefaultIcon, String altHoverIcon, String altDisabledIcon) {
        this(defaultIcon, hoverIcon, disabledIcon, altDefaultIcon, altHoverIcon, altDisabledIcon, null);
    }

    public BufferedImageButton(String defaultIcon, String hoverIcon, String disabledIcon,
                               String altDefaultIcon, String altHoverIcon, String altDisabledIcon, IconSize size) {
        super(defaultIcon, hoverIcon, disabledIcon, altDefaultIcon, altHoverIcon, altDisabledIcon, size);
    }

    @Override
    BufferedImage getImageResource(String imageResource) {
        return SwingUtils.getResourceImage(imageResource, GuiConstants.HI_RES_SIZE, 1.0f);
    }
}
