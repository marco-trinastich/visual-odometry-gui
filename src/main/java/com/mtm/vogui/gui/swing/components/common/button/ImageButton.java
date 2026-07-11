/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.components.common.button;

import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.enums.gui.IconSize;
import com.mtm.vogui.gui.swing.listeners.common.ButtonOnHoverListener;
import com.mtm.vogui.models.gui.ButtonImageSet;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("serial")
public abstract class ImageButton extends JButton {
    private final ButtonImageSet baseImageSet;
    private ButtonImageSet alternativeImageSet;
    private final AtomicReference<ButtonImageSet> activeImageSet;
    @Setter
    private Color foregroundColor = Color.WHITE;
    private String foregroundText;

    public final static IconSize DEFAULT_SIZE = IconSize.I_32;

    public ImageButton(String defaultIcon, String hoverIcon, String disabledIcon,
                       String altDefaultIcon, String altHoverIcon, String altDisabledIcon, IconSize size) {
        hoverIcon = hoverIcon != null ? hoverIcon : defaultIcon;
        disabledIcon = disabledIcon != null ? disabledIcon : defaultIcon;
        altDisabledIcon = altDisabledIcon != null ? altDisabledIcon : altDefaultIcon;
        altHoverIcon = altHoverIcon != null ? altHoverIcon : altDefaultIcon;

        // Icon sets
        size = size != null ? size : DEFAULT_SIZE;
        this.baseImageSet = this.buildImageSet(defaultIcon, hoverIcon, disabledIcon, size);
        if (altDefaultIcon != null) {
            this.alternativeImageSet = this.buildImageSet(altDefaultIcon, altHoverIcon, altDisabledIcon, size);
        }
        this.activeImageSet = new AtomicReference<>(this.baseImageSet);

        // On hover listener
        ButtonOnHoverListener onHoverListener = new ButtonOnHoverListener(this.activeImageSet);
        this.addMouseListener(onHoverListener);

        // Set default image button style
        this.setPreferredSize(new Dimension(size.value(), size.value()));
        this.setIcon(this.activeImageSet.get().icons().defaultIcon());
        this.setBorder(BorderFactory.createEmptyBorder());
        this.setContentAreaFilled(false);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        this.setIcon(enabled ?
                this.activeImageSet.get().icons().defaultIcon() :
                this.activeImageSet.get().icons().disabledIcon());
    }

    public void setForegroundText(String foregroundText) {
        this.foregroundText = foregroundText;
        this.paint(this.getGraphics());
    }

    public void removeForegroundText() {
        this.foregroundText = null;
        this.paint(this.getGraphics());
    }

    public void switchIconSet() {
        if (this.alternativeImageSet == null)
            return;

        if (this.activeImageSet.get() == this.baseImageSet) {
            this.setActiveImageSet(this.alternativeImageSet);
        } else {
            this.setActiveImageSet(this.baseImageSet);
        }
        this.setDefaultIcon();
    }

    public void defaultIconSet() {
        this.setActiveImageSet(this.baseImageSet);
        this.setDefaultIcon();
    }

    public void setOpacity(float opacity) {
        this.setOpacity(opacity, opacity, opacity, opacity);
    }

    public void setOpacity(float defaultOpacity, float hoverOpacity, float clickedOpacity, float disabledOpacity) {
        this.baseImageSet.setOpacity(defaultOpacity, hoverOpacity, clickedOpacity, disabledOpacity);
    }

    public void setAlternativeOpacity(float opacity) {
        this.setAlternativeOpacity(opacity, opacity, opacity, opacity);
    }

    public void setAlternativeOpacity(float defaultOpacity, float hoverOpacity, float clickedOpacity,
                                      float disabledOpacity) {
        if (this.alternativeImageSet != null) {
            this.alternativeImageSet.setOpacity(defaultOpacity, hoverOpacity, clickedOpacity, disabledOpacity);
        }
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (this.foregroundText != null) {
            Graphics2D g2 = (Graphics2D) g;

            Font font = GuiConstants.DEFAULT_FONT;
            Rectangle2D bounds = font.getStringBounds(this.foregroundText, g2.getFontRenderContext());

            g2.setPaint(this.foregroundColor);
            g2.setFont(font);
            g2.drawString(this.foregroundText,
                    (this.getWidth() / 2) - (int) (bounds.getWidth() / 2),
                    (this.getHeight() / 2) + 5);
        }
    }

    abstract BufferedImage getImageResource(String imageResource);

    private @NotNull ButtonImageSet buildImageSet(String baseIconRes,
                                                  String hoverIconRes,
                                                  String disabledIconRes,
                                                  @NotNull IconSize size) {
        // Read full-res images
        BufferedImage defaultIconImg = this.getImageResource(baseIconRes);
        BufferedImage hoverIconImg = baseIconRes.equals(hoverIconRes) ?
                defaultIconImg : this.getImageResource(hoverIconRes);
        BufferedImage disabledIconImg = baseIconRes.equals(disabledIconRes) ?
                defaultIconImg : this.getImageResource(disabledIconRes);

        ButtonImageSet imageSet = ButtonImageSet.builder()
                .defaultImage(defaultIconImg)
                .hoverImage(hoverIconImg)
                .clickedImage(hoverIconImg)
                .disabledImage(disabledIconImg)
                .size(size)
                .build();

        // Pre-allocate icons
        imageSet.generateIcons();

        return imageSet;
    }

    private void setActiveImageSet(ButtonImageSet imageSet) {
        this.activeImageSet.set(imageSet);
    }

    private void setDefaultIcon() {
        this.setIcon(this.activeImageSet.get().icons().defaultIcon());
    }
}
