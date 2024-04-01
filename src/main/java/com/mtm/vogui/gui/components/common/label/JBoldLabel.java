package com.mtm.vogui.gui.components.common.label;

import com.mtm.vogui.models.constants.GuiConstants;

import javax.swing.*;

public class JBoldLabel extends JLabel {
    public JBoldLabel(String content) {
        super(content);
        setFont(GuiConstants.BOLD_FONT);
    }
}
