/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.sidebar.telemetry.odometry;

import com.mtm.vogui.gui.fx.features.sidebar.telemetry.ui.TelemetryUi;
import com.mtm.vogui.gui.fx.shared.components.DirectionDial;
import com.mtm.vogui.gui.fx.state.Telemetry;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.utilities.CommonUtils;
import com.mtm.vogui.utilities.OdometryMathUtils;
import georegression.struct.point.Vector3D_F64;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.ejml.data.DMatrixRMaj;

/**
 * Visual-odometry section (humble view): current translation, covered distance, the heading/altitude
 * direction dials and the rotation matrix. Owns the per-run presentation accumulation (covered distance
 * and previous heading), reset when a new run starts ({@code prevTranslation == null}); the trigonometry
 * lives in {@link OdometryMathUtils}. Position and rotation update every frame; the dials and distance
 * update on the slower interval, mirroring the Swing view.
 * <p>
 * Note: distance is accumulated from the (coalesced) snapshots the FX thread receives, so under heavy
 * frame-drop it is a slight under-estimate — an informational metric, matching the Swing behaviour.
 */
public class OdometryInfoView {

    private static final int LONGER_RENDER_INTERVAL = 10;
    private static final double DIAL_DIAMETER = 56;

    private final Label positionX = TelemetryUi.value();
    private final Label positionY = TelemetryUi.value();
    private final Label positionZ = TelemetryUi.value();
    private final Label distanceXZ = TelemetryUi.value();
    private final Label distanceY = TelemetryUi.value();
    private final DirectionDial headingDial = new DirectionDial(DIAL_DIAMETER);
    private final DirectionDial altitudeDial = new DirectionDial(DIAL_DIAMETER);
    private final Tooltip headingTooltip = new Tooltip();
    private final Tooltip altitudeTooltip = new Tooltip();
    private final Label rotationMatrix = new Label(TelemetryUi.EMPTY);

    private final Region root;

    // Per-run accumulation (reset when a new run starts, i.e. prevTranslation == null)
    private double coveredXZ;
    private double coveredY;
    private Double prevHeading;

    public OdometryInfoView() {
        // Position is three short numbers -> one horizontal row keeps it compact. Monospace + a fixed
        // min width per value so the X/Y/Z groups don't jitter horizontally as digits change.
        for (Label axis : new Label[]{positionX, positionY, positionZ}) {
            axis.getStyleClass().add("telemetry-num");
            axis.setMinWidth(48);
        }
        // Tight spacing so the triple clusters left (in line with the other rows), not spread to the edge.
        HBox position = new HBox(8,
                TelemetryUi.axis("X", positionX), TelemetryUi.axis("Y", positionY),
                TelemetryUi.axis("Z", positionZ));

        Tooltip.install(headingDial, headingTooltip);
        Tooltip.install(altitudeDial, altitudeTooltip);
        HBox dials = new HBox(16, dial(headingDial, "Heading"), dial(altitudeDial, "Altitude"));
        dials.setAlignment(Pos.CENTER_LEFT);

        GridPane distance = TelemetryUi.grid();
        TelemetryUi.row(distance, 0, "Path XZ", distanceXZ);
        TelemetryUi.row(distance, 1, "Path Y", distanceY);

        // Rotation matrix is bulky and rarely glanced at live -> collapsible, collapsed by default.
        rotationMatrix.getStyleClass().add("telemetry-matrix");
        VBox rotation = TelemetryUi.disclosure("Rotation matrix", rotationMatrix);

        VBox body = new VBox(10, position, dials, distance, rotation);
        this.root = TelemetryUi.section("Odometry", body);
    }

    public Region node() {
        return this.root;
    }

    public void render(Telemetry telemetry) {
        ProcessingStatus status = telemetry.status();
        setPosition(status.translation());
        setRotationMatrix(status.rotation());

        // Slower interval: dials and covered distance stay meaningful (and less busy).
        if (status.fps().totalProcessed() % LONGER_RENDER_INTERVAL == 0) {
            setDirectionDials(status.translation(), status.prevTranslation());
            setCoveredDistance(status.translation(), status.prevTranslation());
        }
    }

    private void setPosition(Vector3D_F64 position) {
        // X: side, Y: vertical (inverted per VO output), Z: forward
        positionX.setText(CommonUtils.roundBigDecimal(position.getX(), 2).toString());
        positionY.setText(CommonUtils.roundBigDecimal(-position.getY(), 2).toString());
        positionZ.setText(CommonUtils.roundBigDecimal(position.getZ(), 2).toString());
    }

    private void setCoveredDistance(Vector3D_F64 translation, Vector3D_F64 prevTranslation) {
        if (prevTranslation == null) {
            coveredXZ = 0;
            coveredY = 0;
            prevTranslation = new Vector3D_F64();
        }
        coveredXZ += CommonUtils.getPointsDistance(translation.getX(), prevTranslation.getX(),
                translation.getZ(), prevTranslation.getZ());
        coveredY += CommonUtils.getAbsDistance(translation.getY(), prevTranslation.getY());
        distanceXZ.setText(CommonUtils.roundBigDecimal(coveredXZ, 2).toString());
        distanceY.setText(CommonUtils.roundBigDecimal(coveredY, 2).toString());
    }

    private void setDirectionDials(Vector3D_F64 translation, Vector3D_F64 prevTranslation) {
        if (prevTranslation == null) {
            prevHeading = null;
            prevTranslation = translation;
        }

        // Heading: frame-to-frame direction change as a polar vector.
        double heading = OdometryMathUtils.relativeAngle(translation, prevTranslation);
        if (prevHeading == null) {
            prevHeading = heading;
        }
        OdometryMathUtils.RotationVector rotation = OdometryMathUtils.rotationVector(prevHeading - heading);
        headingDial.setDirection(rotation.x(), rotation.y());
        headingTooltip.setText(String.format("Δ heading %s° (%s)%nx %s, y %s",
                CommonUtils.roundBigDecimal(rotation.angleRad() * 180 / Math.PI, 2),
                rotation.angleRad() <= 0 ? "CCW" : "CW",
                CommonUtils.roundBigDecimal(rotation.x(), 2),
                CommonUtils.roundBigDecimal(rotation.y(), 2)));
        prevHeading = heading;

        // Altitude: signed up/down/flat indicator.
        double altitudeDelta = OdometryMathUtils.altitudeDelta(translation, prevTranslation);
        altitudeDial.setDirection(0, Math.signum(altitudeDelta));
        altitudeTooltip.setText(String.format("Δ altitude %s (%s)",
                CommonUtils.getFormattedExponential(altitudeDelta),
                altitudeDelta >= 0 ? "climbing" : "descending"));
    }

    private void setRotationMatrix(DMatrixRMaj rotation) {
        int rows = Math.min(rotation.getNumRows(), 3);
        int cols = rotation.getNumCols();
        StringBuilder text = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                text.append(CommonUtils.roundBigDecimal(rotation.get(r, c), 2)).append("  ");
            }
            if (r < rows - 1) {
                text.append(System.lineSeparator());
            }
        }
        rotationMatrix.setText(text.toString());
    }

    private static VBox dial(DirectionDial dial, String caption) {
        VBox box = new VBox(4, dial, TelemetryUi.caption(caption));
        box.setAlignment(Pos.CENTER);
        return box;
    }
}
