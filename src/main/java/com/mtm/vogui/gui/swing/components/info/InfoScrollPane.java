/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.components.info;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.models.core.processing.tracking.TrackingStatus;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.settings.SourceType;
import com.mtm.vogui.gui.swing.GuiController;
import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.utilities.CommonUtils;
import com.mtm.vogui.utilities.GuiUtils;
import georegression.struct.point.Vector3D_F64;
import org.ejml.data.DMatrixRMaj;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * InfoScrollPane
 * <p/>
 * This panel contains all Visual Odometry processing details
 */
@SuppressWarnings("serial")
public class InfoScrollPane extends JScrollPane {

    // State fields
    private AppStatus status;
    private double distanceXZ;
    private double distanceY;
    private Double prevPointAngle;

    private final InfoScrollComponents components;

    private final static int INFINITE_PROGRESS_VALUE = 10000;

    public InfoScrollPane(AppContext context, GuiController controller, String title) {
        // Build components
        this.components = new InfoScrollComponents(context, controller);

        this.initContainer(title);
    }

    public void initContainer(String title) {
        // Init info container
        this.setOpaque(false);
        this.setViewportView(this.components.container());
        this.setAppStatus(AppStatus.Empty);

        if (title != null) {
            this.setBorder(GuiUtils.getRoundedTitledBorder(title, GuiConstants.PANEL_BORDER_ACTIVE_COLOR));
        }
    }

    // Components setters
    public void setAppStatus(AppStatus status) {
        if (this.status != status) {
            this.status = status;
            this.components.lblStatus().setText(String.format(GuiConstants.LBL_STATUS, status.value()));
        }
    }

    public void setCalibrationFile(String calibrationFile) {
        this.components.lblCalibrationFile().setText(calibrationFile);
    }

    public void setProcessedSource(String processedSource, @NotNull SourceType source) {
        String hint = "";
        switch (source) {
            case Video -> hint = GuiConstants.LBL_PROCESSED_VIDEO;
            case Device -> hint = GuiConstants.LBL_PROCESSED_DEVICE;
        }
        this.components.lblProcessedSource().setHint(hint);
        this.components.lblProcessedSource().setText(processedSource);
    }

    public void setProcessedFrames(int totalProcessedFrames, int totalFrames) {
        this.components.lblProcessedFrames().setText(String.format(
                GuiConstants.LBL_PROCESSED_FRAME_TEXT,
                totalProcessedFrames,
                totalFrames - totalProcessedFrames
        ));
    }

    public void setElapsedTime(double seconds) {
        // Set elapsed time to formatted time
        this.components.lblElapsedTime().setText(CommonUtils.getFormattedTime(seconds));
    }

    public void setPosition(@NotNull Vector3D_F64 position) {
        // Set x position label (side translation estimation)
        this.components.lblXPosition().setText(CommonUtils.roundBigDecimal(position.getX(), 2));
        // Set y position label (vertical translation estimation)
        this.components.lblYPosition().setText(CommonUtils.roundBigDecimal(-position.getY(), 2));
        // Set z position label (forward translation estimation)
        this.components.lblZPosition().setText(CommonUtils.roundBigDecimal(position.getZ(), 2));
    }

    public void setIncrementalDistance(Vector3D_F64 translation, Vector3D_F64 prevTranslation) {
        if (prevTranslation == null) {
            // If previous position is null, this is a new processing, thus reset counters
            this.distanceXZ = 0;
            this.distanceY = 0;
            prevTranslation = new Vector3D_F64();
        }

        // Increment covered xz and y distances
        distanceXZ += CommonUtils.getPointsDistance(translation.getX(), prevTranslation.getX(),
                translation.getZ(), prevTranslation.getZ());
        this.components.lblXZDistance().setText(CommonUtils.roundBigDecimal(distanceXZ, 2));
        distanceY += CommonUtils.getAbsDistance(translation.getY(), prevTranslation.getY());
        this.components.lblYDistance().setText(CommonUtils.roundBigDecimal(distanceY, 2));
    }

    public void setDirectionPanels(Vector3D_F64 translation, Vector3D_F64 prevTranslation) {
        if (prevTranslation == null) {
            // If previous position is null, this is a new processing, thus reset counters
            this.prevPointAngle = null;
            // Set prev coords to curr coords (to avoid fake variations on start)
            prevTranslation = translation;
        }

        // Rotation panel

        // Calculate angle between 2 points
        double pointAngle = getRelativeAngle(translation, prevTranslation);

        if (this.prevPointAngle == null) {
            // If first frame, sets previous = current angle (to avoid fake rotation on start)
            this.prevPointAngle = pointAngle;
        }

        // Rotation angle as difference between angles
        // - positive -> clockwise rotation
        // - negative -> counter-clockwise rotation
        // - zero -> no rotation
        double rotationAngle = this.prevPointAngle - pointAngle;

        // Polar coordinates
        // x -> angle sine (height of the angle used horizontally)
        double vectorX = Math.sin(rotationAngle);
        // y -> angle cosine (width of the angle used vertically)
        double vectorY = Math.cos(rotationAngle);

        // Set rotation panel direction to scaled up polar coordinates
        this.components.pnlRotation().setDirection(vectorX, vectorY);

        // Set tooltip to rotation angle, sin and cos
        this.components.pnlRotation().setToolTipText(String.format(GuiConstants.ROTATION_PANEL_TOOLTIP,
                CommonUtils.roundBigDecimal(rotationAngle * 180f / Math.PI, 2),
                (rotationAngle <= 0 ? GuiConstants.ROTATION_CCW : GuiConstants.ROTATION_CW),
                CommonUtils.roundBigDecimal(vectorX, 2),
                CommonUtils.roundBigDecimal(vectorY, 2)
        ));

        // Update previous angle
        prevPointAngle = pointAngle;

        // Altitude Panel

        // y -> difference between current and previous altitude (inverted because of VO algorithm output)
        vectorY = prevTranslation.getY() - translation.getY();

        // Set altitude panel direction to vectorY sign (positive for increment, negative for decrement)
        this.components.pnlAltitude().setDirection(0, (int) Math.signum(vectorY));

        // Set tooltip to altitude delta value
        this.components.pnlAltitude().setToolTipText(String.format(GuiConstants.ALTITUDE_PANEL_TOOLTIP,
                CommonUtils.getFormattedExponential(vectorY),
                (vectorY >= 0 ? GuiConstants.ALTITUDE_PANEL_INC : GuiConstants.ALTITUDE_PANEL_DEC)
        ));
    }

    private static double getRelativeAngle(@NotNull Vector3D_F64 translation, Vector3D_F64 prevTranslation) {
        if (translation.equals(prevTranslation)) {
            return 0;
        }

        // Relative coordinates between current and previous point
        double relativeX = (translation.getX() - prevTranslation.getX());
        double relativeZ = (translation.getZ() - prevTranslation.getZ());

        // Angular coefficient of the (prevX,prevZ)-(X,Z) crossing line
        double m = relativeZ / relativeX;

        // Compute angle between current point and horizontal line crossing previous point
        double pointAngle = 0;
        if (relativeX >= 0 && relativeZ >= 0) {
            // First quadrant (+ +) --> 0° <= pointAngle <= 90°
            pointAngle = Math.atan(m);
        } else if (relativeX < 0 && relativeZ >= 0) {
            // Second quadrant (- +) --> 90° < pointAngle <= 180°
            pointAngle = Math.PI + Math.atan(m);
        } else if (relativeX < 0 && relativeZ < 0) {
            // Third quadrant (- -) --> 180° < pointAngle < 270°
            pointAngle = Math.PI + Math.atan(m);
        } else if (relativeX >= 0 && relativeZ < 0) {
            // Fourth quadrant (+ -) --> 270° <= pointAngle < 360°
            pointAngle = 2 * Math.PI + Math.atan(m);
        }
        return pointAngle;
    }

    public void setRotation(@NotNull DMatrixRMaj rotation) {
        this.components.lblRotationRow().getFirst().setText(String.format(
                GuiConstants.LBL_ROTATION_HEADER,
                rotation.getType().name(),
                rotation.getNumRows(),
                rotation.getNumCols()
        ));

        StringBuilder row = new StringBuilder();

        // Extract a 3x3 matrix
        int maxRows = this.components.lblRotationRow().size() - 1;
        int rows = Math.min(rotation.getNumRows(), maxRows);
        int cols = rotation.getNumCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                row.append(CommonUtils.roundBigDecimal(rotation.get(r, c), 2)).append(" ");
            }
            this.components.lblRotationRow().get(r + 1).setText(row.toString());
            row.setLength(0);
        }

        // Clear exceeding rows
        if (rows < maxRows) {
            for (int i = rows; i < maxRows; i++) {
                this.components.lblRotationRow().get(i + 1).setText("");
            }
        }
    }

    public void setTrackingStatus(@NotNull TrackingStatus trackingStatus) {
        this.components.lblTrackingStatus().setText(String.format(
                GuiConstants.LBL_TRACKING_STATUS_TEXT,
                trackingStatus.totalTracks(),
                trackingStatus.trackInliers().size(),
                trackingStatus.trackNew().size()
        ));

        this.components.lblTrackingInliers().setText(String.format(
                GuiConstants.PERCENTAGE_TEXT,
                trackingStatus.inliersPercent()
        ));
    }

    public void setAverageFps(@NotNull FpsStatus fpsStatus) {
        // Set input average fps
        this.components.lblInputFpsAverage().setText(String.format(
                GuiConstants.LBL_FPS_TEXT,
                CommonUtils.roundBigDecimal(fpsStatus.inputAverageFPS(), 2)
        ));

        // Set vo processing average fps
        this.components.lblVoFpsAverage().setText(String.format(
                GuiConstants.LBL_FPS_TEXT,
                CommonUtils.roundBigDecimal(fpsStatus.averageFPS(), 2)
        ));
    }

    public void setCurrentFps(@NotNull FpsStatus fpsStatus) {
        // Set input current fps
        this.components.lblInputFpsCurrent().setText(String.format(
                GuiConstants.LBL_FPS_TEXT,
                CommonUtils.roundBigDecimal(fpsStatus.inputCurrentFPS(), 2)
        ));

        // Set vo processing current fps
        this.components.lblVoFpsCurrent().setText(String.format(
                GuiConstants.LBL_FPS_TEXT,
                CommonUtils.roundBigDecimal(fpsStatus.currentFPS(), 2)
        ));
    }

    public void setBufferProgressBar(BufferStatus bufferStatus, boolean isInfinite) {
        // Set buffer progress bar min/max values
        if (this.components.progressBufferLoad().getMinimum() != 0) {
            this.components.progressBufferLoad().setMinimum(0);
        }

        if (isInfinite) {
            if (this.components.progressBufferLoad().getMaximum() != INFINITE_PROGRESS_VALUE) {
                // Set 10k max value for infinite progress bar
                // (10k images should be close to java heap space limit for almost any image size)
                this.components.progressBufferLoad().setMaximum(INFINITE_PROGRESS_VALUE);
            }
        } else if (this.components.progressBufferLoad().getMaximum() != bufferStatus.maxBufferItems()) {
            this.components.progressBufferLoad().setMaximum((int)  bufferStatus.maxBufferItems());
        }

        // Set current value
        this.components.progressBufferLoad().setValue((int)  bufferStatus.bufferItems());

        // Set progress bar status string
        int progressStatus =  bufferStatus.maxBufferItems() == 0 ?
                0 :
                (int) ( bufferStatus.bufferItems() * 100) / this.components.progressBufferLoad().getMaximum();
        this.components.progressBufferLoad().setString(String.format(GuiConstants.PERCENTAGE_TEXT, progressStatus));
        this.components.progressBufferLoad().setStringPainted(true);
        this.components.progressBufferLoad().repaint();
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

        this.components.lblBufferLoad().setHint(bufferAmount);
        this.components.lblBufferLoad().setText(bufferState);
        this.components.lblBufferLoad().setTextColor(bufferColor);
    }

    // Components visibility
    public void setInfoPanelVisible(boolean visible) {
        this.components.setInfoPanelVisible(visible);
    }

    public boolean isInfoPanelVisible() {
        return this.components.infoPanel().isVisible();
    }

    public void setBufferInfoVisible(boolean visible) {
        this.components.setBufferInfoVisible(visible);
    }

    public boolean isBufferInfoVisible() {
        return this.components.progressBufferLoad().isVisible();
    }

    // List accessors
    public TrackedPoint getSelectedPoint() {
        JList<TrackedPoint> lstTrackedPoints = this.components.lstTrackedPoints();
        DefaultListModel<TrackedPoint> lstTrackedPointsModel = this.components.lstTrackedPointsModel();
        TrackedPoint selected = lstTrackedPoints.getSelectedValue();

        if (selected == null)
            return null;

        if (selected.startPoint() &&
                lstTrackedPoints.getSelectedIndex() < lstTrackedPointsModel.getSize() - 1) {
            // If start point, take next element
            selected = lstTrackedPointsModel.elementAt(lstTrackedPoints.getSelectedIndex() + 1);
        }

        if (selected.endPoint() && lstTrackedPoints.getSelectedIndex() > 0) {
            // If end point, take previous element
            selected = lstTrackedPointsModel.elementAt(lstTrackedPoints.getSelectedIndex() - 1);
        }

        // If selected is still a start/end point return null
        if (selected.startPoint() || selected.endPoint())
            return null;

        return selected;
    }

    public boolean isListEvent(@NotNull ListSelectionEvent evt) {
        return evt.getSource().equals(this.components.lstTrackedPoints());
    }
}
