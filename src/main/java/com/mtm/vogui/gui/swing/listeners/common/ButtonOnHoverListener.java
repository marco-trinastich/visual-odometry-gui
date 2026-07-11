/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.listeners.common;

import com.mtm.vogui.models.gui.ButtonImageSet;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.atomic.AtomicReference;

public class ButtonOnHoverListener extends MouseAdapter {
    @Setter
    private AtomicReference<ButtonImageSet> imageSet;
    private boolean isPressed;

    public ButtonOnHoverListener(AtomicReference<ButtonImageSet> imageSet) {
        this.imageSet = imageSet;
    }

    public void mouseEntered(@NotNull MouseEvent evt) {
        if (evt.getSource() instanceof JButton button) {
            if (button.isEnabled()) {
                button.setIcon(this.isPressed ?
                        this.imageSet.get().icons().clickedIcon() :
                        this.imageSet.get().icons().hoverIcon()
                );
            }
        }
    }

    public void mouseExited(@NotNull MouseEvent evt) {
        if (evt.getSource() instanceof JButton button) {
            if (button.isEnabled()) {
                button.setIcon(this.imageSet.get().icons().defaultIcon());
            }
        }
    }

    public void mousePressed(@NotNull MouseEvent evt) {
        if (evt.getSource() instanceof JButton button) {
            if (button.isEnabled()) {
                button.setIcon(this.imageSet.get().icons().clickedIcon());
            }
            this.isPressed = true;
        }
    }

    public void mouseReleased(@NotNull MouseEvent evt) {
        this.isPressed = false;
    }
}
