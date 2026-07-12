/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.dashboard.info.fps;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.label.JBoldLabel;
import com.mtm.vogui.gui.swing.shared.components.label.JHintLabel;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.utilities.CommonUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Framerate section (humble view): input source vs visual-odometry framerate laid out in two
 * half-width columns (input left, VO right). Owns its labels as private fields; intents run on
 * the EDT (the facade marshals).
 */
public class FpsInfoView {

    private final JBoldLabel lblInputFps;
    private final JHintLabel lblInputFpsCurrent;
    private final JHintLabel lblInputFpsAverage;
    private final JBoldLabel lblVoFps;
    private final JHintLabel lblVoFpsCurrent;
    private final JHintLabel lblVoFpsAverage;

    private final JPanel panel;

    public FpsInfoView() {
        this.lblInputFps = new JBoldLabel(GuiConstants.LBL_INPUT_FPS);
        this.lblInputFpsCurrent = new JHintLabel(GuiConstants.LBL_CURRENT_FPS);
        this.lblInputFpsAverage = new JHintLabel(GuiConstants.LBL_AVERAGE_FPS);
        this.lblVoFps = new JBoldLabel(GuiConstants.LBL_OUTPUT_FPS);
        this.lblVoFpsCurrent = new JHintLabel(GuiConstants.LBL_CURRENT_FPS);
        this.lblVoFpsAverage = new JHintLabel(GuiConstants.LBL_AVERAGE_FPS);

        this.panel = new JPanel();
        this.panel.setOpaque(false);
        this.panel.add(this.lblInputFps);
        this.panel.add(this.lblInputFpsCurrent);
        this.panel.add(this.lblInputFpsAverage);
        this.panel.add(this.lblVoFps);
        this.panel.add(this.lblVoFpsCurrent);
        this.panel.add(this.lblVoFpsAverage);

        this.layout();
    }

    public JPanel panel() {
        return this.panel;
    }

    public void setAverageFps(@NotNull FpsStatus fpsStatus) {
        this.lblInputFpsAverage.setText(String.format(
                GuiConstants.LBL_FPS_TEXT,
                CommonUtils.roundBigDecimal(fpsStatus.inputAverageFPS(), 2)
        ));
        this.lblVoFpsAverage.setText(String.format(
                GuiConstants.LBL_FPS_TEXT,
                CommonUtils.roundBigDecimal(fpsStatus.averageFPS(), 2)
        ));
    }

    public void setCurrentFps(@NotNull FpsStatus fpsStatus) {
        this.lblInputFpsCurrent.setText(String.format(
                GuiConstants.LBL_FPS_TEXT,
                CommonUtils.roundBigDecimal(fpsStatus.inputCurrentFPS(), 2)
        ));
        this.lblVoFpsCurrent.setText(String.format(
                GuiConstants.LBL_FPS_TEXT,
                CommonUtils.roundBigDecimal(fpsStatus.currentFPS(), 2)
        ));
    }

    private void layout() {
        Spring halfWidth = SwingUtils.getHalfWidthSpring(panel);

        SpringLayout layout = new SpringLayout();

        // Left column: input framerate (header spans full width)
        layout.putConstraint(SpringLayout.NORTH, lblInputFps, 5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, lblInputFps, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblInputFps, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblInputFpsCurrent, 10, SpringLayout.SOUTH, lblInputFps);
        layout.putConstraint(SpringLayout.WEST, lblInputFpsCurrent, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblInputFpsCurrent, halfWidth, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblInputFpsAverage, 10, SpringLayout.SOUTH, lblInputFpsCurrent);
        layout.putConstraint(SpringLayout.WEST, lblInputFpsAverage, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblInputFpsAverage, halfWidth, SpringLayout.WEST, panel);

        // Right column: visual odometry framerate
        layout.putConstraint(SpringLayout.NORTH, lblVoFps, 5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, lblVoFps, halfWidth, SpringLayout.WEST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblVoFpsCurrent, 10, SpringLayout.SOUTH, lblVoFps);
        layout.putConstraint(SpringLayout.WEST, lblVoFpsCurrent, halfWidth, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblVoFpsCurrent, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblVoFpsAverage, 10, SpringLayout.SOUTH, lblVoFpsCurrent);
        layout.putConstraint(SpringLayout.WEST, lblVoFpsAverage, halfWidth, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblVoFpsAverage, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.SOUTH, panel, 0, SpringLayout.SOUTH, lblInputFpsAverage);

        this.panel.setLayout(layout);
    }
}
