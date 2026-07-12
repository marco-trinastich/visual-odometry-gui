/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.button.graphics;

import com.mtm.vogui.utilities.ImageUtils;
import lombok.Builder;

import javax.swing.*;
import java.awt.image.BufferedImage;

@Builder
public class ButtonImageSet {
    private BufferedImage defaultImage;
    private Float defaultAlpha;
    private BufferedImage hoverImage;
    private Float hoverAlpha;
    private BufferedImage clickedImage;
    private Float clickedAlpha;
    private BufferedImage disabledImage;
    private Float disabledAlpha;
    private IconSize size;

    private ButtonIconSet iconSet;

    private final static float DEFAULT_ALPHA = 0.8f;
    private final static float HOVER_ALPHA = 0.9f;
    private final static float CLICKED_ALPHA = 1.0f;
    private final static float DISABLED_ALPHA = 0.8f;

    public ButtonIconSet icons() {
        if (this.iconSet == null) {
            this.generateIcons();
        }

        return this.iconSet;
    }

    public void generateIcons() {
        // Adjust opacity
        BufferedImage defaultIconImg = ImageUtils.modifyBufferedImageAlpha(this.defaultImage,
                this.defaultAlpha != null ? this.defaultAlpha : DEFAULT_ALPHA);
        BufferedImage hoverIconImg = ImageUtils.modifyBufferedImageAlpha(this.hoverImage,
                this.hoverAlpha != null ? this.hoverAlpha : HOVER_ALPHA);
        BufferedImage clickedIconImg = ImageUtils.modifyBufferedImageAlpha(this.clickedImage,
                this.clickedAlpha != null ? this.clickedAlpha : CLICKED_ALPHA);
        BufferedImage disabledIconImg = ImageUtils.modifyBufferedImageAlpha(this.disabledImage,
                this.disabledAlpha != null ? this.disabledAlpha : DISABLED_ALPHA);

        // Build hi-res icons
        ImageIcon defaultIcon =
                new ImageIcon(ImageUtils.getMultiResImageFromBuffered(defaultIconImg, this.size.resolutions()));
        ImageIcon hoverIcon =
                new ImageIcon(ImageUtils.getMultiResImageFromBuffered(hoverIconImg, this.size.resolutions()));
        ImageIcon clickedIcon =
                new ImageIcon(ImageUtils.getMultiResImageFromBuffered(clickedIconImg, this.size.resolutions()));
        ImageIcon disabledIcon =
                new ImageIcon(ImageUtils.getMultiResImageFromBuffered(disabledIconImg, this.size.resolutions()));

        this.iconSet = ButtonIconSet.builder()
                .defaultIcon(defaultIcon)
                .hoverIcon(hoverIcon)
                .clickedIcon(clickedIcon)
                .disabledIcon(disabledIcon)
                .build();
    }

    public void setOpacity(float defaultAlpha, float hoverAlpha, float clickedAlpha, float disabledAlpha) {
        this.defaultAlpha = defaultAlpha;
        this.hoverAlpha = hoverAlpha;
        this.clickedAlpha = clickedAlpha;
        this.disabledAlpha = disabledAlpha;
        this.generateIcons();
    }
}
