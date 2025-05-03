/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.listeners.common;


import com.mtm.vogui.gui.components.common.textfield.NumberTextField;
import org.jetbrains.annotations.NotNull;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class NumberTextFieldListener implements FocusListener {
    @Override
    public void focusGained(@NotNull FocusEvent e) {
        if (e.getSource() instanceof NumberTextField<?> textField) {
            // Before user input align text/model values (if needed)
            textField.refreshModel();
        }
    }

    @Override
    public void focusLost(@NotNull FocusEvent e) {
        if (e.getSource() instanceof NumberTextField<?> textField) {
            // After user input try to parse, normalize and update model
            textField.updateModel(textField.getText());
        }
    }
}
