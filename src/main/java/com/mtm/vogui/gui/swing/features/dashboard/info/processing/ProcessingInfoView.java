/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.dashboard.info.processing;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.label.JBoldLabel;
import com.mtm.vogui.gui.swing.shared.components.label.JHintLabel;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.utilities.CommonUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Processing-info section (humble view): calibration file, processed source, processed/skipped
 * frames and elapsed time. Owns its labels as private fields; consumers only express intent.
 * Intents run on the EDT (the facade marshals).
 */
public class ProcessingInfoView {

    private final JBoldLabel lblInfo;
    private final JHintLabel lblCalibrationFile;
    private final JHintLabel lblProcessedSource;
    private final JHintLabel lblProcessedFrames;
    private final JHintLabel lblElapsedTime;

    private final JPanel panel;

    public ProcessingInfoView() {
        this.lblInfo = new JBoldLabel(GuiConstants.LBL_INFO);
        this.lblCalibrationFile = new JHintLabel(GuiConstants.LBL_CALIBRATION_FILE, true);
        this.lblProcessedSource = new JHintLabel(true);
        this.lblProcessedFrames = new JHintLabel(GuiConstants.LBL_PROCESSED_FRAME);
        this.lblElapsedTime = new JHintLabel(GuiConstants.LBL_ELAPSED_TIME);

        this.panel = new JPanel();
        this.panel.setOpaque(false);
        this.panel.add(this.lblInfo);
        this.panel.add(this.lblCalibrationFile);
        this.panel.add(this.lblProcessedSource);
        this.panel.add(this.lblProcessedFrames);
        this.panel.add(this.lblElapsedTime);

        this.layout();
    }

    public JPanel panel() {
        return this.panel;
    }

    public void setCalibrationFile(String calibrationFile) {
        this.lblCalibrationFile.setText(calibrationFile);
    }

    public void setProcessedSource(String processedSource, @NotNull SourceType source) {
        String hint = switch (source) {
            case Video -> GuiConstants.LBL_PROCESSED_VIDEO;
            case Device -> GuiConstants.LBL_PROCESSED_DEVICE;
        };
        this.lblProcessedSource.setHint(hint);
        this.lblProcessedSource.setText(processedSource);
    }

    public void setProcessedFrames(int totalProcessedFrames, int totalFrames) {
        this.lblProcessedFrames.setText(String.format(
                GuiConstants.LBL_PROCESSED_FRAME_TEXT,
                totalProcessedFrames,
                totalFrames - totalProcessedFrames
        ));
    }

    public void setElapsedTime(double seconds) {
        this.lblElapsedTime.setText(CommonUtils.getFormattedTime(seconds));
    }

    private void layout() {
        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.NORTH, lblInfo, 5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, lblInfo, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblInfo, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblCalibrationFile, 10, SpringLayout.SOUTH, lblInfo);
        layout.putConstraint(SpringLayout.WEST, lblCalibrationFile, 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblCalibrationFile, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblProcessedSource, 10, SpringLayout.SOUTH, lblCalibrationFile);
        layout.putConstraint(SpringLayout.WEST, lblProcessedSource, 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblProcessedSource, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblProcessedFrames, 10, SpringLayout.SOUTH, lblProcessedSource);
        layout.putConstraint(SpringLayout.WEST, lblProcessedFrames, 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblProcessedFrames, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblElapsedTime, 10, SpringLayout.SOUTH, lblProcessedFrames);
        layout.putConstraint(SpringLayout.WEST, lblElapsedTime, 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblElapsedTime, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.SOUTH, panel, 0, SpringLayout.SOUTH, lblElapsedTime);

        this.panel.setLayout(layout);
    }
}
