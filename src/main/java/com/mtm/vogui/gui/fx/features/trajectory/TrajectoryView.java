/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.features.trajectory;

import com.mtm.vogui.gui.fx.shared.charting.TrajectoryChart;
import com.mtm.vogui.gui.fx.state.GuiState;
import com.mtm.vogui.gui.fx.state.TrajectoryEvent;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.models.enums.gui.ChartAxis;
import com.mtm.vogui.models.enums.settings.ChartType;
import atlantafx.base.theme.Styles;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;

/**
 * Trajectory feature facade (humble view, JavaFX counterpart of the Swing
 * {@code gui.swing.features.dashboard.trajectory.TrajectoryView}): the dashboard's trajectory pane.
 * Owns the two {@link TrajectoryChart}s — the XZ ground track and the Y altitude — and the compact
 * navigation controls beside each (Origin / Last, plus a 3D-points toggle on the ground track). Mouse
 * pan/zoom is the primary interaction (drag = pan, Ctrl+drag or wheel = zoom, double-click = fit-all);
 * the buttons cover the common "recenter" cases.
 * <p>
 * Pure reactive display: it registers as the consumer of {@link GuiState}'s trajectory event stream
 * and applies each {@link TrajectoryEvent} on the FX Application Thread — the sink stays widget-free.
 * The feature owns the segment close-or-drop decision (it alone holds both series) and keeps the
 * {@link GuiState#segmentCount()} mirror in sync, so the core can tag each run's points. Exposes the
 * uniform facade contract {@link #content()} plus point-navigation intents for the tracked-points log.
 */
public final class TrajectoryView {

    /** A run's scale is 1.0 by default; only a non-default scale switches a chart to fixed zoom. */
    private static final double DEFAULT_SCALE = 1.0;

    private final GuiState guiState;
    private final TrajectoryChart xzChart;
    private final TrajectoryChart yChart;
    private final SplitPane content;

    public TrajectoryView(GuiState guiState) {
        this.guiState = guiState;
        this.xzChart = new TrajectoryChart(ChartAxis.X.value(), ChartAxis.Z.value());
        this.yChart = new TrajectoryChart(ChartAxis.Frame.value(), ChartAxis.Y.value());

        this.content = new SplitPane(
                card("Ground track", xzChart, xzControls()),
                card("Altitude", yChart, yControls()));
        this.content.setOrientation(Orientation.VERTICAL);
        // Give the ground track the larger share (the altitude trace is a single dimension).
        this.content.setDividerPositions(0.8);

        guiState.setTrajectoryHandler(this::onEvent);
    }

    /** The composed charts region, for the shell to mount. */
    public Region content() {
        return this.content;
    }

    // Point-navigation intents (driven by the tracked-points log selection; FX thread)

    public void moveXzToPoint(double x, double z) {
        this.xzChart.moveToPoint(x, z);
    }

    public void moveYToPoint(double x, double y) {
        this.yChart.moveToPoint(x, y);
    }

    /** True when the ground track holds any point — gates the toolbar's idle Clear. */
    public boolean xzHasPoints() {
        return this.xzChart.hasPoints();
    }

    // Trajectory event stream (FX thread)

    private void onEvent(TrajectoryEvent event) {
        switch (event) {
            case TrajectoryEvent.StartSegment start -> {
                yChart.setAxisNames(xAxisName(start.chartType()), ChartAxis.Y.value());
                applyInitialZoom(xzChart, start.xzScale());
                applyInitialZoom(yChart, start.yScale());
            }
            case TrajectoryEvent.Plot plot -> {
                xzChart.addPoint(plot.xzX(), plot.xzZ());
                if (plot.altitudeX() != null && plot.altitudeY() != null) {
                    yChart.addPoint(plot.altitudeX(), plot.altitudeY());
                }
            }
            case TrajectoryEvent.Altitude altitude -> yChart.addPoint(altitude.x(), altitude.y());
            case TrajectoryEvent.EndSegment end -> endSegment(end.endMarker());
            case TrajectoryEvent.ClearAll _ -> clearAll();
        }
    }

    private void endSegment(TrackedPoint endMarker) {
        if (xzChart.hasOpenSegmentPoints() && yChart.hasOpenSegmentPoints()) {
            xzChart.closeSegment();
            yChart.closeSegment();
            guiState.segmentCount().incrementAndGet();
            guiState.trackedPoints().add(endMarker);
        } else {
            // Aborted run (no points on both charts): drop the open segment from the log, leave the
            // series open so the next run continues it — mirrors the Swing dropOpenSegment behaviour.
            dropOpenSegment(guiState.trackedPoints());
        }
    }

    private void clearAll() {
        xzChart.clearAll();
        yChart.clearAll();
        guiState.segmentCount().set(1);
    }

    /** Removes the last (unclosed) segment from the points log: its start marker through the end. */
    private static void dropOpenSegment(ObservableList<TrackedPoint> points) {
        for (int i = points.size() - 1; i >= 0; i--) {
            if (points.get(i).startPoint()) {
                points.remove(i, points.size());
                return;
            }
        }
    }

    private static void applyInitialZoom(TrajectoryChart chart, double scale) {
        // A non-default scale means the user wants a fixed zoom; scale 1.0 keeps auto-range/fit-all.
        if (scale != DEFAULT_SCALE) {
            chart.applyScale(scale);
        }
    }

    private static String xAxisName(ChartType chartType) {
        ChartAxis axis = chartType == null ? null : chartType.xAxis();
        return axis == null ? ChartAxis.Frame.value() : axis.value();
    }

    // Composition

    // Lucide icon path data (24x24 viewport, stroke-only): plain play triangles (no skip bar) pointing
    // to origin / latest, and a three-dot scatter for the markers toggle. Thin round-capped strokes (css).
    private static final String ICON_TO_START = "M16,5 L7,12 L16,19 Z";
    private static final String ICON_TO_END = "M8,5 L17,12 L8,19 Z";
    private static final String ICON_MARKERS =
            "M4,15 a2.4,2.4 0 1,0 4.8,0 a2.4,2.4 0 1,0 -4.8,0"
            + " M9.6,7 a2.4,2.4 0 1,0 4.8,0 a2.4,2.4 0 1,0 -4.8,0"
            + " M15.2,15.5 a2.4,2.4 0 1,0 4.8,0 a2.4,2.4 0 1,0 -4.8,0";
    /** Scale from the 24px Lucide viewport down to the compact size the chart chrome wants. */
    private static final double ICON_SCALE = 0.6;

    private HBox xzControls() {
        ToggleButton markers = new ToggleButton();
        markers.setGraphic(icon(ICON_MARKERS));
        markers.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.SMALL);
        markers.setTooltip(new Tooltip("Markers"));
        markers.selectedProperty().addListener((_, _, on) -> xzChart.setSymbolsVisible(on));
        return controlsBar(
                navButton(ICON_TO_START, "Recenter on trajectory start", xzChart::moveToOrigin),
                navButton(ICON_TO_END, "Recenter on latest point", xzChart::moveToLast),
                markers);
    }

    private HBox yControls() {
        return controlsBar(
                navButton(ICON_TO_START, "Recenter on trajectory start", yChart::moveToOrigin),
                navButton(ICON_TO_END, "Recenter on latest point", yChart::moveToLast));
    }

    /** A compact, flat icon button with a tooltip spelling out its action. */
    private static Button navButton(String iconPath, String tooltip, Runnable action) {
        Button button = new Button();
        button.setGraphic(icon(iconPath));
        button.getStyleClass().addAll(Styles.BUTTON_ICON, Styles.FLAT, Styles.SMALL);
        button.setTooltip(new Tooltip(tooltip));
        button.setOnAction(_ -> action.run());
        return button;
    }

    /**
     * A Lucide-style stroked icon: a {@link SVGPath} styled thin/round via {@code .chart-nav-icon}
     * (theme-aware stroke) and scaled from Lucide's 24px viewport. Wrapped in a {@link Group} so the
     * scale is reflected in the layout bounds — the hosting button then hugs the icon instead of
     * reserving the full 24px box.
     */
    private static Node icon(String iconPath) {
        SVGPath glyph = new SVGPath();
        glyph.setContent(iconPath);
        glyph.getStyleClass().add("chart-nav-icon");
        glyph.setScaleX(ICON_SCALE);
        glyph.setScaleY(ICON_SCALE);
        return new Group(glyph);
    }

    private static HBox controlsBar(Node... controls) {
        HBox bar = new HBox(4, controls);
        bar.getStyleClass().add("chart-controls");
        bar.setAlignment(Pos.CENTER);
        // Keep the bar at its natural size: a StackPane would otherwise stretch it (default maxWidth is
        // unbounded) to full width, defeating the bottom-right float.
        bar.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        return bar;
    }

    /**
     * Composes one chart pane: the chart fills the card, with a muted floating title pinned top-left
     * and the navigation controls floating bottom-right — both overlaid so they don't consume the
     * plot's vertical space. The title is mouse-transparent (decoration only); the controls sit inset
     * from the edges and pick only within their own bounds, so mouse pan/zoom on the plot is unaffected.
     */
    private static Region card(String title, TrajectoryChart chart, HBox controls) {
        Label heading = new Label(title);
        heading.getStyleClass().add("chart-title");
        heading.setMouseTransparent(true);
        StackPane.setAlignment(heading, Pos.TOP_LEFT);

        StackPane.setAlignment(controls, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(controls, new Insets(6));

        StackPane card = new StackPane(chart.node(), heading, controls);
        card.getStyleClass().add("chart-card");
        return card;
    }
}
