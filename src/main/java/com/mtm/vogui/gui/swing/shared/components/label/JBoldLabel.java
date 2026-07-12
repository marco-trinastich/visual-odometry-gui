/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.label;

import com.mtm.vogui.gui.swing.utils.SwingUtils;

import javax.swing.*;

@SuppressWarnings("serial")
public class JBoldLabel extends JLabel {
    public JBoldLabel(String content) {
        super(content);
        setFont(SwingUtils.boldFont());
    }
}
