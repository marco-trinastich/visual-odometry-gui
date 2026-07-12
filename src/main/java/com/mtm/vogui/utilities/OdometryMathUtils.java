/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.utilities;

import georegression.struct.point.Vector3D_F64;
import org.jetbrains.annotations.NotNull;

/**
 * Pure, stateless derivations of the direction/rotation telemetry shown by the info panel.
 * Toolkit-agnostic (depends only on the georegression {@link Vector3D_F64} data type), so both
 * the Swing and the JavaFX info views share it. Per-session accumulation (covered distance,
 * previous angle) is presentation lifecycle and stays in the owning view.
 */
public final class OdometryMathUtils {

    private OdometryMathUtils() {
    }

    /**
     * Angle (radians) of the previous→current XZ displacement measured from the horizontal line
     * crossing the previous point, resolved by quadrant (0 when the two points coincide).
     */
    public static double relativeAngle(@NotNull Vector3D_F64 translation, Vector3D_F64 prevTranslation) {
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

    /**
     * Polar direction of a frame-to-frame rotation: {@code x} is the angle sine (used
     * horizontally), {@code y} the cosine (used vertically). {@code angleRad} echoes the input:
     * positive = clockwise, negative = counter-clockwise.
     */
    public static @NotNull RotationVector rotationVector(double rotationAngleRad) {
        return new RotationVector(Math.sin(rotationAngleRad), Math.cos(rotationAngleRad), rotationAngleRad);
    }

    /**
     * Altitude delta between frames, inverted to match the VO algorithm output: positive = climb,
     * negative = descent.
     */
    public static double altitudeDelta(@NotNull Vector3D_F64 translation, @NotNull Vector3D_F64 prevTranslation) {
        return prevTranslation.getY() - translation.getY();
    }

    /**
     * Polar rotation direction plus the originating angle (radians).
     */
    public record RotationVector(double x, double y, double angleRad) {
    }
}
