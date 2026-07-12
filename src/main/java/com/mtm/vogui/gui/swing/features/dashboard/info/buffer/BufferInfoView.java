/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.dashboard.info.buffer;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.label.JBoldLabel;
import com.mtm.vogui.gui.swing.shared.components.label.JHintLabel;
import com.mtm.vogui.models.core.integration.BufferStatus;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Buffer-load section (humble view): a label, a progress bar and the load state. The whole
 * section is shown/hidden as a unit by the facade (which relies on the sections' layout to
 * collapse it), so no in-place re-layout lives here anymore. Intents run on the EDT.
 */
public class BufferInfoView {

    private static final int INFINITE_PROGRESS_VALUE = 10000;

    private final JBoldLabel lblBuffer;
    private final JProgressBar progressBufferLoad;
    private final JHintLabel lblBufferLoad;

    private final JPanel panel;

    public BufferInfoView() {
        this.lblBuffer = new JBoldLabel(GuiConstants.LBL_BUFFER_INFO);
        this.progressBufferLoad = new JProgressBar();
        this.lblBufferLoad = new JHintLabel("", false, false, true);

        this.panel = new JPanel();
        this.panel.setOpaque(false);
        this.panel.add(this.lblBuffer);
        this.panel.add(this.progressBufferLoad);
        this.panel.add(this.lblBufferLoad);

        this.layout();
    }

    public JPanel panel() {
        return this.panel;
    }

    public void setBufferProgressBar(BufferStatus bufferStatus, boolean isInfinite) {
        // Set buffer progress bar min/max values
        if (this.progressBufferLoad.getMinimum() != 0) {
            this.progressBufferLoad.setMinimum(0);
        }

        if (isInfinite) {
            if (this.progressBufferLoad.getMaximum() != INFINITE_PROGRESS_VALUE) {
                // Set 10k max value for infinite progress bar
                // (10k images should be close to java heap space limit for almost any image size)
                this.progressBufferLoad.setMaximum(INFINITE_PROGRESS_VALUE);
            }
        } else if (this.progressBufferLoad.getMaximum() != bufferStatus.maxBufferItems()) {
            this.progressBufferLoad.setMaximum((int) bufferStatus.maxBufferItems());
        }

        // Set current value
        this.progressBufferLoad.setValue((int) bufferStatus.bufferItems());

        // Set progress bar status string
        int progressStatus = bufferStatus.maxBufferItems() == 0 ?
                0 :
                (int) (bufferStatus.bufferItems() * 100) / this.progressBufferLoad.getMaximum();
        this.progressBufferLoad.setString(String.format(GuiConstants.PERCENTAGE_TEXT, progressStatus));
        this.progressBufferLoad.setStringPainted(true);
        this.progressBufferLoad.repaint();
    }

    public void setBufferLabel(@NotNull BufferStatus bufferStatus, boolean isInfinite) {
        String bufferState = "";
        Color bufferColor = GuiConstants.LIGHT_BLACK;
        if (bufferStatus.bufferItems() == 0) {
            bufferState = GuiConstants.LBL_BUFFER_STABLE;
            bufferColor = GuiConstants.LIGHT_GREEN;
        } else if (bufferStatus.bufferItems() >= bufferStatus.maxBufferItems()) {
            bufferState = GuiConstants.LBL_BUFFER_OVER_RUN;
            bufferColor = GuiConstants.LIGHT_RED;
        }
        if (bufferStatus.maxBufferItems() == 0) {
            bufferState = GuiConstants.LBL_BUFFER_UNAVAILABLE;
            bufferColor = GuiConstants.LIGHT_RED;
        }

        String maxValueString = isInfinite ? GuiConstants.LBL_BUFFER_INFINITE : bufferStatus.maxBufferSize();
        String bufferAmount = bufferStatus.maxBufferItems() != 0 ?
                String.format(
                        GuiConstants.LBL_BUFFER_HINT,
                        bufferStatus.bufferSize(),
                        maxValueString) :
                "";

        this.lblBufferLoad.setHint(bufferAmount);
        this.lblBufferLoad.setText(bufferState);
        this.lblBufferLoad.setTextColor(bufferColor);
    }

    private void layout() {
        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.NORTH, lblBuffer, 5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, lblBuffer, 5, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.NORTH, progressBufferLoad, 5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, progressBufferLoad, 3, SpringLayout.EAST, lblBuffer);

        layout.putConstraint(SpringLayout.NORTH, lblBufferLoad, 5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, lblBufferLoad, 3, SpringLayout.EAST, progressBufferLoad);
        layout.putConstraint(SpringLayout.EAST, lblBufferLoad, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.SOUTH, panel, 0, SpringLayout.SOUTH, progressBufferLoad);

        this.panel.setLayout(layout);
    }
}
