package com.mtm.vogui.gui.components.common.button;

import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.enums.gui.IconSize;
import com.mtm.vogui.utilities.ImageUtils;

import java.awt.image.BufferedImage;

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
        return ImageUtils.getResourceImage(imageResource, GuiConstants.HI_RES_SIZE, 1.0f);
    }
}
