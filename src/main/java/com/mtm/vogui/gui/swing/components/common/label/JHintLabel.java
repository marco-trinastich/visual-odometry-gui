/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.components.common.label;

import com.mtm.vogui.models.constants.GuiConstants;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

@SuppressWarnings("serial")
public class JHintLabel extends JPanel {
    private final JLabel lblHint;
    private final JLabel lblText;
    private final boolean syncTooltip;
    private final boolean boldHint;
    private final boolean boldText;

    public JHintLabel(boolean syncTooltip) {
        this("", syncTooltip);
    }

    public JHintLabel(String hintText) {
        this(hintText, false);
    }

    public JHintLabel(String hintText, boolean syncTooltip) {
        this(hintText, syncTooltip, true, false);
    }

    public JHintLabel(String hintText, boolean syncTooltip, boolean boldHint, boolean boldText) {
        // Configure flow horizontal layout without gaps
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));

        // Create fixed hint label
        this.lblHint = new JLabel(hintText);
        this.lblHint.setFont(boldHint ? GuiConstants.BOLD_FONT : this.lblHint.getFont());

        // Create dynamic text label
        this.lblText = new JLabel();
        this.lblText.setFont(boldText ? GuiConstants.BOLD_FONT : this.lblText.getFont());

        // Configure container
        this.add(this.lblHint);
        this.add(this.lblText);
        this.setOpaque(false);

        // Store settings
        this.syncTooltip = syncTooltip;
        this.boldHint = boldHint;
        this.boldText = boldText;

        refreshTooltip();
    }

    public void setHint(String hint) {
        if (!this.lblHint.getText().equalsIgnoreCase(hint)) {
            this.lblHint.setText(hint);
            refreshTooltip();
        }
    }

    public void setText(String text) {
        if (!this.lblText.getText().equalsIgnoreCase(text)) {
            this.lblText.setText(text);
            refreshTooltip();
        }
    }

    public void setText(Double value) {
        setText(String.valueOf(value));
    }

    public void setText(@NotNull BigDecimal value) {
        setText(value.toString());
    }

    public void setHintColor(Color color) {
        this.lblHint.setForeground(color);
    }

    public void setTextColor(Color color) {
        this.lblText.setForeground(color);
    }

    public void setSpaceBetween(int spaceBetween) {
        this.remove(1);
        JPanel lblTextContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, spaceBetween, 0));
        lblTextContainer.add(this.lblText);
        this.add(lblTextContainer);
    }

    private void refreshTooltip() {
        if (this.syncTooltip) {
            String hintTooltip = this.boldHint ?
                    String.format(GuiConstants.BOLD_TAG, this.lblHint.getText()) : this.lblHint.getText();
            String textTooltip = this.boldText ?
                    String.format(GuiConstants.BOLD_TAG, this.lblText.getText()) : this.lblText.getText();

            this.setToolTipText(String.format(GuiConstants.HTML_TAG, hintTooltip + textTooltip));
        }
    }
}
