/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.components.info;

import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.models.enums.gui.PanelBorder;
import com.mtm.vogui.gui.swing.GuiController;
import com.mtm.vogui.gui.swing.components.common.panel.DirectionPanel;
import com.mtm.vogui.gui.swing.components.common.label.JBoldLabel;
import com.mtm.vogui.gui.swing.components.common.label.JHintLabel;
import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.gui.swing.listeners.common.RunnableOnListDataChangeListener;
import com.mtm.vogui.gui.swing.listeners.trackedpoints.TrackedPointsCopyAllOnDblClick;
import com.mtm.vogui.gui.swing.listeners.trackedpoints.TrackedPointsCopyOnSelection;
import com.mtm.vogui.gui.swing.listeners.trackedpoints.TrackedPointsMoveChartOnSelection;
import com.mtm.vogui.gui.swing.renderers.TrackedPointsListCellRenderer;
import com.mtm.vogui.utilities.GuiUtils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public class InfoScrollComponents {
    // Container
    private JPanel container;

    // Status label
    private JLabel lblStatus;

    // Info panel
    private JPanel infoPanel;

    // Processing info
    private JBoldLabel lblInfo;
    private JHintLabel lblCalibrationFile;
    private JHintLabel lblProcessedSource;
    private JHintLabel lblProcessedFrames;
    private JHintLabel lblElapsedTime;

    // Visual odometry info
    private JBoldLabel lblPositions;
    private JHintLabel lblXPosition;
    private JHintLabel lblYPosition;
    private JHintLabel lblZPosition;
    private JHintLabel lblXZDistance;
    private JHintLabel lblYDistance;
    private DirectionPanel pnlRotation;
    private DirectionPanel pnlAltitude;
    private JBoldLabel lblRotation;
    private List<JLabel> lblRotationRow;

    // Tracking info
    private JBoldLabel lblTrackingInfo;
    private JHintLabel lblTrackingStatus;
    private JHintLabel lblTrackingInliers;

    // FPS info
    private JBoldLabel lblInputFps;
    private JHintLabel lblInputFpsCurrent;
    private JHintLabel lblInputFpsAverage;
    private JBoldLabel lblVoFps;
    private JHintLabel lblVoFpsCurrent;
    private JHintLabel lblVoFpsAverage;

    // Buffer info
    private JLabel lblBuffer;
    private JProgressBar progressBufferLoad;
    private JHintLabel lblBufferLoad;

    // Tracked points list
    private JLabel lblTrackedPoints;
    private DefaultListModel<TrackedPoint> lstTrackedPointsModel;
    private JList<TrackedPoint> lstTrackedPoints;
    private JScrollPane lstTrackedPointsScroll;

    // Info panel layout manager
    private SpringLayout infoPanelLayout;

    // Dependencies
    private final AppContext context;
    private final GuiController controller;

    public InfoScrollComponents(AppContext context, GuiController controller) {
        this.context = context;
        this.controller = controller;
        this.init();
    }

    // Components visibility
    public void setInfoPanelVisible(boolean visible) {
        if (!visible) {
            this.infoPanel.setVisible(false);
            this.container.setPreferredSize(new Dimension(GuiConstants.INFO_PANEL_WIDTH, 100));
        } else {
            this.infoPanel.setVisible(true);
            this.container.setPreferredSize(
                    new Dimension(GuiConstants.INFO_PANEL_WIDTH, GuiConstants.INFO_PANEL_HEIGHT)
            );
        }
    }

    public void setBufferInfoVisible(boolean visible) {
        if (!visible) {
            if (!this.lblBuffer.isVisible() &&
                    !this.progressBufferLoad.isVisible() &&
                    !this.lblBufferLoad.isVisible())
                return;

            infoPanelLayout.removeLayoutComponent(lblBuffer);
            infoPanelLayout.removeLayoutComponent(progressBufferLoad);
            infoPanelLayout.removeLayoutComponent(lblBufferLoad);
            infoPanelLayout.removeLayoutComponent(lblTrackedPoints);

            this.layoutTrackedPointsWithoutBuffer();

            this.lblBuffer.setVisible(false);
            this.progressBufferLoad.setVisible(false);
            this.lblBufferLoad.setVisible(false);

            this.infoPanel.paintAll(this.infoPanel.getGraphics());
        } else {
            if (this.lblBuffer.isVisible() &&
                    this.progressBufferLoad.isVisible() &&
                    this.lblBufferLoad.isVisible())
                return;

            this.infoPanelLayout.removeLayoutComponent(this.lblTrackedPoints);

            this.layoutBufferInfo();
            this.layoutTrackedPointsWithBuffer();

            this.lblBuffer.setVisible(true);
            this.progressBufferLoad.setVisible(true);
            this.lblBufferLoad.setVisible(true);

            this.infoPanel.paintAll(this.infoPanel.getGraphics());
        }
    }

    // Components creation
    private void init() {
        // Status label creation
        this.createStatusLabel();

        // Info panel creation
        this.createInfoPanel();

        // Container creation
        this.createContainer();

        // Layout container
        this.layoutContainer();
    }

    private void createStatusLabel() {
        this.lblStatus = new JLabel();
    }

    private void createInfoPanel() {
        // Build subcomponents

        // Processing info
        this.lblInfo = new JBoldLabel(GuiConstants.LBL_INFO);
        this.lblCalibrationFile = new JHintLabel(GuiConstants.LBL_CALIBRATION_FILE, true);
        this.lblProcessedSource = new JHintLabel(true);
        this.lblProcessedFrames = new JHintLabel(GuiConstants.LBL_PROCESSED_FRAME);
        this.lblElapsedTime = new JHintLabel(GuiConstants.LBL_ELAPSED_TIME);

        // Visual odometry info
        this.lblPositions = new JBoldLabel(GuiConstants.LBL_POSITIONS);
        this.lblXPosition = new JHintLabel(GuiConstants.LBL_X_POSITION);
        this.lblYPosition = new JHintLabel(GuiConstants.LBL_Y_POSITION);
        this.lblZPosition = new JHintLabel(GuiConstants.LBL_Z_POSITION);
        this.lblXZDistance = new JHintLabel(GuiConstants.LBL_XZ_DISTANCE);
        this.lblYDistance = new JHintLabel(GuiConstants.LBL_Y_DISTANCE);
        this.pnlRotation = new DirectionPanel(
                GuiConstants.DIRECTION_PANEL_WIDTH,
                GuiConstants.DIRECTION_PANEL_HEIGHT
        );
        this.pnlRotation.enableBorder(Color.black, GuiConstants.DIRECTION_PANEL_BORDER, PanelBorder.Circle);
        this.pnlAltitude = new DirectionPanel(
                GuiConstants.DIRECTION_PANEL_WIDTH,
                GuiConstants.DIRECTION_PANEL_HEIGHT
        );
        this.pnlAltitude.enableBorder(Color.black, GuiConstants.DIRECTION_PANEL_BORDER, PanelBorder.Circle);
        // 4-rows rotation data
        this.lblRotation = new JBoldLabel(GuiConstants.LBL_ROTATION);
        this.lblRotationRow = IntStream.range(0, 4).mapToObj(_ -> new JLabel()).toList();

        // Tracking info
        this.lblTrackingInfo = new JBoldLabel(GuiConstants.LBL_TRACKING_INFO);
        this.lblTrackingStatus = new JHintLabel(GuiConstants.LBL_TRACKING_STATUS);
        this.lblTrackingInliers = new JHintLabel(GuiConstants.LBL_TRACKING_INLIERS);

        // FPS info
        this.lblInputFps = new JBoldLabel(GuiConstants.LBL_INPUT_FPS);
        this.lblInputFpsCurrent = new JHintLabel(GuiConstants.LBL_CURRENT_FPS);
        this.lblInputFpsAverage = new JHintLabel(GuiConstants.LBL_AVERAGE_FPS);
        this.lblVoFps = new JBoldLabel(GuiConstants.LBL_OUTPUT_FPS);
        this.lblVoFpsCurrent = new JHintLabel(GuiConstants.LBL_CURRENT_FPS);
        this.lblVoFpsAverage = new JHintLabel(GuiConstants.LBL_AVERAGE_FPS);

        // Buffer info
        this.lblBuffer = new JBoldLabel(GuiConstants.LBL_BUFFER_INFO);
        this.progressBufferLoad = new JProgressBar();
        this.lblBufferLoad = new JHintLabel("", false, false, true);

        // Tracked points list
        this.lblTrackedPoints = new JBoldLabel(GuiConstants.LBL_TRACKED_POINTS);
        this.lstTrackedPointsModel = context.state().trackedPoints();
        this.lstTrackedPointsModel.addListDataListener(new RunnableOnListDataChangeListener(this::scrollListToEnd));
        this.lstTrackedPoints = new JList<>(lstTrackedPointsModel);
        this.lstTrackedPoints.setCellRenderer(new TrackedPointsListCellRenderer());
        this.lstTrackedPoints.addListSelectionListener(new TrackedPointsCopyOnSelection());
        this.lstTrackedPoints.addListSelectionListener(new TrackedPointsMoveChartOnSelection(context, controller));
        this.lstTrackedPoints.addMouseListener(new TrackedPointsCopyAllOnDblClick(lstTrackedPointsModel));
        this.lstTrackedPointsScroll = new JScrollPane(lstTrackedPoints);


        // Compose info panel
        this.infoPanel = this.composeInfoPanel();

        // Layout info panel
        this.layoutInfoPanel();
    }

    private @NotNull JPanel composeInfoPanel() {
        JPanel infoPanel = new JPanel();
        infoPanel.setOpaque(false);

        // Processing info
        infoPanel.add(this.lblInfo);
        infoPanel.add(this.lblCalibrationFile);
        infoPanel.add(this.lblProcessedSource);
        infoPanel.add(this.lblProcessedFrames);
        infoPanel.add(this.lblElapsedTime);

        // Visual odometry info
        infoPanel.add(this.lblPositions);
        infoPanel.add(this.lblXPosition);
        infoPanel.add(this.lblYPosition);
        infoPanel.add(this.lblZPosition);
        infoPanel.add(this.lblXZDistance);
        infoPanel.add(this.lblYDistance);
        infoPanel.add(this.pnlRotation);
        infoPanel.add(this.pnlAltitude);
        infoPanel.add(this.lblRotation);
        infoPanel.add(this.lblRotationRow.get(0));
        infoPanel.add(this.lblRotationRow.get(1));
        infoPanel.add(this.lblRotationRow.get(2));
        infoPanel.add(this.lblRotationRow.get(3));

        // Tracking info
        infoPanel.add(this.lblTrackingInfo);
        infoPanel.add(this.lblTrackingInliers);
        infoPanel.add(this.lblTrackingStatus);

        // Fps info
        infoPanel.add(this.lblInputFps);
        infoPanel.add(this.lblInputFpsCurrent);
        infoPanel.add(this.lblInputFpsAverage);
        infoPanel.add(this.lblVoFps);
        infoPanel.add(this.lblVoFpsCurrent);
        infoPanel.add(this.lblVoFpsAverage);

        // Buffer info
        infoPanel.add(this.lblBuffer);
        infoPanel.add(this.progressBufferLoad);
        infoPanel.add(this.lblBufferLoad);

        // Tracked points info
        infoPanel.add(this.lblTrackedPoints);
        infoPanel.add(this.lstTrackedPointsScroll);

        return infoPanel;
    }

    private void createContainer() {
        this.container = new JPanel();

        this.container.setPreferredSize(new Dimension(GuiConstants.INFO_PANEL_WIDTH, GuiConstants.INFO_PANEL_HEIGHT));
        this.container.add(this.lblStatus);
        this.container.add(this.infoPanel);
    }

    // Components layout
    private void layoutInfoPanel() {
        Spring halfWidth = GuiUtils.getHalfWidthSpring(infoPanel);

        this.infoPanelLayout = new SpringLayout();

        // Processing info

        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblInfo, 5, SpringLayout.NORTH, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblInfo, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblInfo, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblCalibrationFile, 10, SpringLayout.SOUTH, lblInfo);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblCalibrationFile, 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblCalibrationFile, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblProcessedSource, 10,
                SpringLayout.SOUTH, lblCalibrationFile
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblProcessedSource, 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblProcessedSource, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblProcessedFrames, 10,
                SpringLayout.SOUTH, lblProcessedSource
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblProcessedFrames, 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblProcessedFrames, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblElapsedTime, 10,
                SpringLayout.SOUTH, lblProcessedFrames
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblElapsedTime, 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblElapsedTime, -5, SpringLayout.EAST, infoPanel);

        // Visual odometry info

        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblPositions, 10, SpringLayout.SOUTH, lblElapsedTime);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblPositions, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblPositions, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblXPosition, 10, SpringLayout.SOUTH, lblPositions);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblXPosition, 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblXPosition, -5, SpringLayout.EAST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblYPosition, 10, SpringLayout.SOUTH, lblXPosition);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblYPosition, 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblYPosition, -5, SpringLayout.EAST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblZPosition, 10, SpringLayout.SOUTH, lblYPosition);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblZPosition, 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblZPosition, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(SpringLayout.NORTH, pnlRotation, -25, SpringLayout.SOUTH, lblPositions);
        infoPanelLayout.putConstraint(SpringLayout.EAST, pnlRotation, -5, SpringLayout.EAST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.NORTH, pnlAltitude, 10, SpringLayout.SOUTH, pnlRotation);
        infoPanelLayout.putConstraint(SpringLayout.EAST, pnlAltitude, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblXZDistance, 10, SpringLayout.SOUTH, lblZPosition);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblXZDistance, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblXZDistance, -5, SpringLayout.EAST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblYDistance, 10, SpringLayout.SOUTH, lblXZDistance);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblYDistance, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblYDistance, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblRotation, 10, SpringLayout.SOUTH, lblYDistance);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblRotation, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblRotation, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblRotationRow.getFirst(), 10,
                SpringLayout.SOUTH, lblRotation
        );
        infoPanelLayout.putConstraint(
                SpringLayout.WEST, lblRotationRow.getFirst(), 15,
                SpringLayout.WEST, infoPanel
        );
        infoPanelLayout.putConstraint(
                SpringLayout.EAST, lblRotationRow.getFirst(), -5,
                SpringLayout.EAST, infoPanel
        );
        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblRotationRow.get(1), 10,
                SpringLayout.SOUTH, lblRotationRow.getFirst()
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblRotationRow.get(1), 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblRotationRow.get(1), -5, SpringLayout.EAST, infoPanel);
        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblRotationRow.get(2), 10,
                SpringLayout.SOUTH, lblRotationRow.get(1)
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblRotationRow.get(2), 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblRotationRow.get(2), -5, SpringLayout.EAST, infoPanel);
        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblRotationRow.get(3), 10,
                SpringLayout.SOUTH, lblRotationRow.get(2)
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblRotationRow.get(3), 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblRotationRow.get(3), -5, SpringLayout.EAST, infoPanel);

        // Tracking info

        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblTrackingInfo, 10,
                SpringLayout.SOUTH, lblRotationRow.get(3)
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblTrackingInfo, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblTrackingInfo, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblTrackingStatus, 10,
                SpringLayout.SOUTH, lblTrackingInfo
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblTrackingStatus, 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblTrackingStatus, -5, SpringLayout.EAST, infoPanel);
        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblTrackingInliers, 10,
                SpringLayout.SOUTH, lblTrackingStatus
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblTrackingInliers, 15, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblTrackingInliers, -5, SpringLayout.EAST, infoPanel);

        // Fps info

        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblInputFps, 10, SpringLayout.SOUTH, lblTrackingInliers);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblInputFps, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblInputFps, halfWidth, SpringLayout.WEST, infoPanel);

        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblInputFpsCurrent, 10, SpringLayout.SOUTH, lblInputFps);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblInputFpsCurrent, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblInputFpsCurrent, halfWidth, SpringLayout.WEST, infoPanel);

        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblInputFpsAverage, 10,
                SpringLayout.SOUTH, lblInputFpsCurrent
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblInputFpsAverage, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblInputFpsAverage, halfWidth, SpringLayout.WEST, infoPanel);

        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblVoFps, 10, SpringLayout.SOUTH, lblTrackingInliers);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblVoFps, halfWidth, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblInputFps, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblVoFpsCurrent, 10, SpringLayout.SOUTH, lblVoFps);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblVoFpsCurrent, halfWidth, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblVoFpsCurrent, -5, SpringLayout.EAST, infoPanel);

        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblVoFpsAverage, 10,
                SpringLayout.SOUTH, lblVoFpsCurrent
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblVoFpsAverage, halfWidth, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblVoFpsAverage, -5, SpringLayout.EAST, infoPanel);

        // Buffer info
        this.layoutBufferInfo();

        // Tracked points list
        this.layoutTrackedPointsWithBuffer();
        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lstTrackedPointsScroll, 10,
                SpringLayout.SOUTH, lblTrackedPoints
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lstTrackedPointsScroll, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lstTrackedPointsScroll, -5, SpringLayout.EAST, infoPanel);
        infoPanelLayout.putConstraint(
                SpringLayout.SOUTH, lstTrackedPointsScroll, -5,
                SpringLayout.SOUTH, infoPanel
        );

        this.infoPanel.setLayout(this.infoPanelLayout);
    }

    private void layoutTrackedPointsWithoutBuffer() {
        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblTrackedPoints, 15,
                SpringLayout.SOUTH, lblInputFpsAverage
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblTrackedPoints, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblTrackedPoints, -5, SpringLayout.EAST, infoPanel);
    }

    private void layoutTrackedPointsWithBuffer() {
        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblTrackedPoints, 15, SpringLayout.SOUTH, lblBuffer);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblTrackedPoints, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblTrackedPoints, -5, SpringLayout.EAST, infoPanel);
    }

    private void layoutBufferInfo() {
        infoPanelLayout.putConstraint(SpringLayout.NORTH, lblBuffer, 10, SpringLayout.SOUTH, lblInputFpsAverage);
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblBuffer, 5, SpringLayout.WEST, infoPanel);
        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, progressBufferLoad, 10,
                SpringLayout.SOUTH, lblInputFpsAverage
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, progressBufferLoad, 3, SpringLayout.EAST, lblBuffer);
        infoPanelLayout.putConstraint(
                SpringLayout.NORTH, lblBufferLoad, 10,
                SpringLayout.SOUTH, lblInputFpsAverage
        );
        infoPanelLayout.putConstraint(SpringLayout.WEST, lblBufferLoad, 3, SpringLayout.EAST, progressBufferLoad);
        infoPanelLayout.putConstraint(SpringLayout.EAST, lblBufferLoad, -5, SpringLayout.EAST, infoPanel);
    }

    private void layoutContainer() {
        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.NORTH, this.lblStatus, 5, SpringLayout.NORTH, this.container);
        layout.putConstraint(SpringLayout.WEST, this.lblStatus, 5, SpringLayout.WEST, this.container);
        layout.putConstraint(SpringLayout.EAST, this.lblStatus, -5, SpringLayout.EAST, this.container);

        layout.putConstraint(SpringLayout.NORTH, this.infoPanel, 10, SpringLayout.SOUTH, this.lblStatus);
        layout.putConstraint(SpringLayout.WEST, this.infoPanel, 5, SpringLayout.WEST, this.container);
        layout.putConstraint(SpringLayout.EAST, this.infoPanel, -5, SpringLayout.EAST, this.container);
        layout.putConstraint(SpringLayout.SOUTH, this.infoPanel, -5, SpringLayout.SOUTH, this.container);

        this.container.setLayout(layout);
    }

    // List repaint
    private void scrollListToEnd() {
        JScrollBar vBar = this.lstTrackedPointsScroll.getVerticalScrollBar();
        if (vBar != null)
            vBar.setValue(vBar.getMaximum());
    }
}
