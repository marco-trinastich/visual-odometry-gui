/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.dashboard.info.odometry;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.label.JBoldLabel;
import com.mtm.vogui.gui.swing.shared.components.label.JHintLabel;
import com.mtm.vogui.gui.swing.shared.components.panel.DirectionPanel;
import com.mtm.vogui.models.enums.gui.PanelBorder;
import com.mtm.vogui.utilities.CommonUtils;
import com.mtm.vogui.utilities.OdometryMathUtils;
import georegression.struct.point.Vector3D_F64;
import org.ejml.data.DMatrixRMaj;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Visual-odometry telemetry section (humble view): current translation, covered distance, the
 * rotation/altitude direction panels and the rotation matrix. Owns its widgets as private
 * fields and the per-session accumulation state (covered distance, previous heading); the pure
 * trig lives in {@link OdometryMathUtils}. Intents run on the EDT (the facade marshals).
 */
public class OdometryInfoView {

    private final JBoldLabel lblPositions;
    private final JHintLabel lblXPosition;
    private final JHintLabel lblYPosition;
    private final JHintLabel lblZPosition;
    private final JHintLabel lblXZDistance;
    private final JHintLabel lblYDistance;
    private final DirectionPanel pnlRotation;
    private final DirectionPanel pnlAltitude;
    private final JBoldLabel lblRotation;
    private final List<JLabel> lblRotationRow;

    private final JPanel panel;

    // Per-session accumulation (reset when a new processing starts, i.e. prevTranslation == null)
    private double distanceXZ;
    private double distanceY;
    private Double prevPointAngle;

    public OdometryInfoView() {
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

        this.lblRotation = new JBoldLabel(GuiConstants.LBL_ROTATION);
        this.lblRotationRow = IntStream.range(0, 4).mapToObj(_ -> new JLabel()).toList();

        this.panel = new JPanel();
        this.panel.setOpaque(false);
        this.panel.add(this.lblPositions);
        this.panel.add(this.lblXPosition);
        this.panel.add(this.lblYPosition);
        this.panel.add(this.lblZPosition);
        this.panel.add(this.pnlRotation);
        this.panel.add(this.pnlAltitude);
        this.panel.add(this.lblXZDistance);
        this.panel.add(this.lblYDistance);
        this.panel.add(this.lblRotation);
        this.lblRotationRow.forEach(this.panel::add);

        this.layout();
    }

    public JPanel panel() {
        return this.panel;
    }

    public void setPosition(@NotNull Vector3D_F64 position) {
        // X: side translation, Y: vertical translation (inverted per VO output), Z: forward
        this.lblXPosition.setText(CommonUtils.roundBigDecimal(position.getX(), 2));
        this.lblYPosition.setText(CommonUtils.roundBigDecimal(-position.getY(), 2));
        this.lblZPosition.setText(CommonUtils.roundBigDecimal(position.getZ(), 2));
    }

    public void setIncrementalDistance(Vector3D_F64 translation, Vector3D_F64 prevTranslation) {
        if (prevTranslation == null) {
            // New processing: reset counters
            this.distanceXZ = 0;
            this.distanceY = 0;
            prevTranslation = new Vector3D_F64();
        }

        distanceXZ += CommonUtils.getPointsDistance(translation.getX(), prevTranslation.getX(),
                translation.getZ(), prevTranslation.getZ());
        this.lblXZDistance.setText(CommonUtils.roundBigDecimal(distanceXZ, 2));

        distanceY += CommonUtils.getAbsDistance(translation.getY(), prevTranslation.getY());
        this.lblYDistance.setText(CommonUtils.roundBigDecimal(distanceY, 2));
    }

    public void setDirectionPanels(Vector3D_F64 translation, Vector3D_F64 prevTranslation) {
        if (prevTranslation == null) {
            // New processing: avoid fake variations on start
            this.prevPointAngle = null;
            prevTranslation = translation;
        }

        // Rotation: frame-to-frame heading delta as polar direction
        double pointAngle = OdometryMathUtils.relativeAngle(translation, prevTranslation);
        if (this.prevPointAngle == null) {
            // First frame: previous = current angle (no fake rotation on start)
            this.prevPointAngle = pointAngle;
        }
        OdometryMathUtils.RotationVector rotation = OdometryMathUtils.rotationVector(this.prevPointAngle - pointAngle);

        this.pnlRotation.setDirection(rotation.x(), rotation.y());
        this.pnlRotation.setToolTipText(String.format(GuiConstants.ROTATION_PANEL_TOOLTIP,
                CommonUtils.roundBigDecimal(rotation.angleRad() * 180f / Math.PI, 2),
                (rotation.angleRad() <= 0 ? GuiConstants.ROTATION_CCW : GuiConstants.ROTATION_CW),
                CommonUtils.roundBigDecimal(rotation.x(), 2),
                CommonUtils.roundBigDecimal(rotation.y(), 2)
        ));

        this.prevPointAngle = pointAngle;

        // Altitude: signed delta
        double altitudeDelta = OdometryMathUtils.altitudeDelta(translation, prevTranslation);
        this.pnlAltitude.setDirection(0, (int) Math.signum(altitudeDelta));
        this.pnlAltitude.setToolTipText(String.format(GuiConstants.ALTITUDE_PANEL_TOOLTIP,
                CommonUtils.getFormattedExponential(altitudeDelta),
                (altitudeDelta >= 0 ? GuiConstants.ALTITUDE_PANEL_INC : GuiConstants.ALTITUDE_PANEL_DEC)
        ));
    }

    public void setRotationMatrix(@NotNull DMatrixRMaj rotation) {
        this.lblRotationRow.getFirst().setText(String.format(
                GuiConstants.LBL_ROTATION_HEADER,
                rotation.getType().name(),
                rotation.getNumRows(),
                rotation.getNumCols()
        ));

        StringBuilder row = new StringBuilder();

        // Extract up to a 3x3 matrix (header row excluded)
        int maxRows = this.lblRotationRow.size() - 1;
        int rows = Math.min(rotation.getNumRows(), maxRows);
        int cols = rotation.getNumCols();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                row.append(CommonUtils.roundBigDecimal(rotation.get(r, c), 2)).append(" ");
            }
            this.lblRotationRow.get(r + 1).setText(row.toString());
            row.setLength(0);
        }

        // Clear exceeding rows
        if (rows < maxRows) {
            for (int i = rows; i < maxRows; i++) {
                this.lblRotationRow.get(i + 1).setText("");
            }
        }
    }

    private void layout() {
        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.NORTH, lblPositions, 5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, lblPositions, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblPositions, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblXPosition, 10, SpringLayout.SOUTH, lblPositions);
        layout.putConstraint(SpringLayout.WEST, lblXPosition, 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblXPosition, -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, lblYPosition, 10, SpringLayout.SOUTH, lblXPosition);
        layout.putConstraint(SpringLayout.WEST, lblYPosition, 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblYPosition, -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, lblZPosition, 10, SpringLayout.SOUTH, lblYPosition);
        layout.putConstraint(SpringLayout.WEST, lblZPosition, 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblZPosition, -5, SpringLayout.EAST, panel);

        // Anchored to the section top (not below the header) so the -25 float can't clip it now
        // that the header sits at the top of its own section panel.
        layout.putConstraint(SpringLayout.NORTH, pnlRotation, 5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.EAST, pnlRotation, -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, pnlAltitude, 10, SpringLayout.SOUTH, pnlRotation);
        layout.putConstraint(SpringLayout.EAST, pnlAltitude, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblXZDistance, 10, SpringLayout.SOUTH, lblZPosition);
        layout.putConstraint(SpringLayout.WEST, lblXZDistance, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblXZDistance, -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, lblYDistance, 10, SpringLayout.SOUTH, lblXZDistance);
        layout.putConstraint(SpringLayout.WEST, lblYDistance, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblYDistance, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblRotation, 10, SpringLayout.SOUTH, lblYDistance);
        layout.putConstraint(SpringLayout.WEST, lblRotation, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblRotation, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, lblRotationRow.getFirst(), 10, SpringLayout.SOUTH, lblRotation);
        layout.putConstraint(SpringLayout.WEST, lblRotationRow.getFirst(), 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblRotationRow.getFirst(), -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, lblRotationRow.get(1), 10, SpringLayout.SOUTH, lblRotationRow.getFirst());
        layout.putConstraint(SpringLayout.WEST, lblRotationRow.get(1), 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblRotationRow.get(1), -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, lblRotationRow.get(2), 10, SpringLayout.SOUTH, lblRotationRow.get(1));
        layout.putConstraint(SpringLayout.WEST, lblRotationRow.get(2), 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblRotationRow.get(2), -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.NORTH, lblRotationRow.get(3), 10, SpringLayout.SOUTH, lblRotationRow.get(2));
        layout.putConstraint(SpringLayout.WEST, lblRotationRow.get(3), 15, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblRotationRow.get(3), -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.SOUTH, panel, 0, SpringLayout.SOUTH, lblRotationRow.get(3));

        this.panel.setLayout(layout);
    }
}
