/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.state;

import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.models.enums.settings.ChartType;

/**
 * A single step in the trajectory event stream that the core-fed {@code fx.rendering.FxRenderSink}
 * emits and the charts feature applies to its {@link com.mtm.vogui.gui.fx.shared.charting.TrajectoryChart
 * TrajectoryChart}s. Pure data (no JavaFX): the frozen numbers a chart needs, nothing more — the sink
 * never touches widgets, the view holds all chart state.
 * <p>
 * Delivered ordered and non-dropping (via {@code FxUtils.orderedFxConsumer}), because every plotted
 * point and every segment boundary matters — unlike per-frame video, which can coalesce to the latest.
 */
public sealed interface TrajectoryEvent {

    /**
     * Opens a run's segment: aligns the Y chart abscissa to the altitude basis and carries the
     * persisted axis scales, applied as the initial zoom (scale {@code 1.0} keeps auto-range/fit-all).
     */
    record StartSegment(ChartType chartType, double xzScale, double yScale) implements TrajectoryEvent {
    }

    /**
     * A processed frame's ground-track point ({@code xzX}/{@code xzZ}) plus, for a frame-based chart,
     * the altitude point ({@code altitudeX}/{@code altitudeY}); the altitude pair is {@code null} for a
     * second-based chart (those points arrive via {@link Altitude} once per second).
     */
    record Plot(double xzX, double xzZ, Double altitudeX, Double altitudeY) implements TrajectoryEvent {
    }

    /** A second-based altitude sample for the Y chart (emitted once per second while running). */
    record Altitude(double x, double y) implements TrajectoryEvent {
    }

    /**
     * Ends the run's segment. The charts feature closes it (and logs {@code endMarker}) when the open
     * segment holds points on both charts; otherwise it drops the open segment from the points log — a
     * decision that needs the live series state, which only the feature holds. {@code endMarker} is
     * pre-built by the sink (it owns the run's {@code PointFactory}) and only appended on close.
     */
    record EndSegment(TrackedPoint endMarker) implements TrajectoryEvent {
    }

    /** Clears every segment on both charts (Clear during/after a run). */
    record ClearAll() implements TrajectoryEvent {
    }
}
