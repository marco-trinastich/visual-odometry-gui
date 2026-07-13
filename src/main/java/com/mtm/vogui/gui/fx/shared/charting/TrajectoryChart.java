/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.charting;

import com.mtm.vogui.gui.fx.shared.charting.jfxutils.chart.ChartPanManager;
import com.mtm.vogui.gui.fx.shared.charting.jfxutils.chart.JFXChartUtil;
import com.mtm.vogui.gui.fx.shared.charting.jfxutils.chart.StableTicksAxis;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Region;

/**
 * App-blind, reusable trajectory chart: a {@link LineChart} over two {@link StableTicksAxis} with
 * gillius/jfxutils mouse interaction wired in (drag = pan, Ctrl+drag or wheel = zoom, double-click =
 * auto-range/fit-all). The JavaFX successor to the Swing {@code ChartScrollPane} pixel canvas — the
 * old manual scroll/scale machinery is replaced by real axis pan/zoom, so this class exposes only the
 * domain-agnostic intents a trajectory needs.
 * <p>
 * A trajectory is a sequence of <em>segments</em> (one {@link XYChart.Series} each): points append to
 * the open (last) series; {@link #closeSegment()} starts a fresh one. There is always at least one
 * open series, so {@link #seriesCount()} is never zero (mirrors the Swing chart, which seeded one
 * empty series and grew from there — the run's segment id is that count).
 * <p>
 * Interaction modes, tracked by each axis' {@code autoRanging} flag (single source of truth, so the
 * jfxutils double-click handler and this class never disagree):
 * <ul>
 *   <li><b>AUTO</b> (default / after double-click): axes fit all data as the trajectory grows.</li>
 *   <li><b>MANUAL</b> (after {@link #applyScale}/{@link #moveToOrigin}/{@link #moveToLast}/
 *       {@link #moveToPoint} or a mouse pan/zoom): fixed span; {@link #addPoint} keeps the newest
 *       point centred while <em>following</em> (paused by {@link #moveToOrigin}/{@link #moveToPoint},
 *       resumed by {@link #moveToLast}).</li>
 * </ul>
 * All methods must run on the FX Application Thread.
 */
public final class TrajectoryChart {

    /** Visible units on each axis at scale 1.0 when switching to a fixed (manual) zoom. */
    private static final double REFERENCE_SPAN = 100.0;
    /** Fallback span when recentering an axis whose current span is degenerate (never auto-ranged). */
    private static final double DEFAULT_SPAN = 100.0;
    /**
     * Cap on the number of markers drawn when "3D points" is on, regardless of the real point count.
     * A long trajectory (thousands of samples) would otherwise create thousands of symbol nodes and
     * stall the app; we decimate to an evenly-spaced subset so the visual stays readable and cheap.
     */
    private static final int MAX_SYMBOLS = 100;

    private final StableTicksAxis xAxis = new StableTicksAxis();
    private final StableTicksAxis yAxis = new StableTicksAxis();
    private final LineChart<Number, Number> chart = new LineChart<>(xAxis, yAxis);
    private final Region node;

    /** In MANUAL mode, keep the newest point centred as it arrives (Swing "follow new points"). */
    private boolean following = true;
    /** "3D points" toggle state: markers are drawn (decimated to {@value #MAX_SYMBOLS}) only when on. */
    private boolean symbolsVisible = false;
    /** Coalesces bursty per-point marker refreshes into one pass per pulse (see {@link #scheduleSymbolRefresh()}). */
    private boolean symbolRefreshScheduled = false;

    public TrajectoryChart(String xName, String yName) {
        xAxis.setLabel(xName);
        yAxis.setLabel(yName);
        // StableTicksAxis forces 0 into the auto-range by default (fine for a ground track around the
        // origin), but that fights a fixed manual span; the manual setters clear auto-ranging anyway.
        // No chart title: the feature paints a compact floating title over the plot instead, so the
        // chart's own title band doesn't eat vertical space in the narrow dashboard panes.
        chart.setCreateSymbols(false);   // long trajectories: markers only when "3D points" is on
        // A trajectory revisits X values (the path curves back on itself); LineChart's default X_AXIS
        // sorting would reorder samples by X and connect them out of path order, producing spurious
        // vertical spikes. NONE keeps the polyline in the real frame order.
        chart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
        chart.setAnimated(false);        // per-point axis animation would thrash under the vo frame rate
        chart.setLegendVisible(false);
        chart.getStyleClass().add("trajectory-chart");
        // Seed the first (open) segment so seriesCount() is never zero.
        chart.getData().add(new XYChart.Series<>());

        // Mouse interaction: pan (drag) + zoom (Ctrl+drag / wheel) + double-click auto-range reset.
        node = JFXChartUtil.setupZooming(chart);
        new ChartPanManager(chart).start();
        JFXChartUtil.addDoublePrimaryClickAutoRangeHandler(chart);
    }

    /** The mountable chart region (a zoom-enabled wrapper around the {@link LineChart}). */
    public Region node() {
        return this.node;
    }

    // Axis labels (the Y chart's abscissa flips between frame- and second-based)

    public void setAxisNames(String xName, String yName) {
        xAxis.setLabel(xName);
        yAxis.setLabel(yName);
    }

    // Points

    /** Appends a point to the open (last) segment; recentres on it while following in MANUAL mode. */
    public void addPoint(double x, double y) {
        openSeries().getData().add(new XYChart.Data<>(x, y));
        if (following && !xAxis.isAutoRanging()) {
            center(x, y);
        }
        if (symbolsVisible) {
            scheduleSymbolRefresh();   // the new point shifts the decimation stride as the total grows
        }
    }

    /** True when any segment holds at least one point. */
    public boolean hasPoints() {
        return chart.getData().stream().anyMatch(s -> !s.getData().isEmpty());
    }

    /** True when the open (last) segment holds at least one point (ready to be closed). */
    public boolean hasOpenSegmentPoints() {
        return !openSeries().getData().isEmpty();
    }

    /** Number of segments, i.e. the id the next run's points are tagged with (never zero). */
    public int seriesCount() {
        return chart.getData().size();
    }

    // Segment lifecycle

    /** Closes the open segment by starting a fresh empty one (the new open series). */
    public void closeSegment() {
        chart.getData().add(new XYChart.Series<>());
    }

    /** Clears every segment and returns to a single empty open series in AUTO (fit-all) mode. */
    public void clearAll() {
        chart.getData().clear();
        chart.getData().add(new XYChart.Series<>());
        following = true;
        setAutoRanging(true);
    }

    // View controls

    /**
     * "3D points": show markers along the trajectory or a bare line. To keep long trajectories cheap,
     * markers are decimated to an evenly-spaced subset of at most {@value #MAX_SYMBOLS} (see
     * {@link #applySymbolDecimation()}) rather than one per sample.
     */
    public void setSymbolsVisible(boolean visible) {
        this.symbolsVisible = visible;
        chart.setCreateSymbols(visible);
        if (visible) {
            scheduleSymbolRefresh();
        }
    }

    /**
     * Fixed zoom (Swing scale analogue: higher = more zoomed in). Switches to MANUAL mode with a
     * span of {@value #REFERENCE_SPAN}/scale on both axes, resumes following, and recentres on the
     * newest point (or the origin when empty).
     */
    public void applyScale(double scale) {
        double span = REFERENCE_SPAN / scale;
        setAutoRanging(false);
        following = true;
        XYChart.Data<Number, Number> last = lastPoint();
        double cx = last == null ? 0 : last.getXValue().doubleValue();
        double cy = last == null ? 0 : last.getYValue().doubleValue();
        setSpan(xAxis, span);
        setSpan(yAxis, span);
        center(cx, cy);
    }

    /**
     * Centres the view on the trajectory's start at the current zoom and stops following. "Origin" is
     * the first plotted point, not the mathematical (0,0): for the ground track that first point sits
     * at the world origin anyway, while for the altitude trace (frame vs Y) the data lives at frame 0
     * at the real altitude — centring on (0,0) there would leave the line off-screen. Falls back to
     * (0,0) when empty.
     */
    public void moveToOrigin() {
        setAutoRanging(false);
        following = false;
        XYChart.Data<Number, Number> first = firstPoint();
        double cx = first == null ? 0 : first.getXValue().doubleValue();
        double cy = first == null ? 0 : first.getYValue().doubleValue();
        center(cx, cy);
    }

    /** Centres the view on the newest point and resumes following. */
    public void moveToLast() {
        XYChart.Data<Number, Number> last = lastPoint();
        if (last == null) {
            return;
        }
        setAutoRanging(false);
        following = true;
        center(last.getXValue().doubleValue(), last.getYValue().doubleValue());
    }

    /** Centres the view on a specific point at the current zoom and stops following (point nav). */
    public void moveToPoint(double x, double y) {
        setAutoRanging(false);
        following = false;
        center(x, y);
    }

    // Internals

    private XYChart.Series<Number, Number> openSeries() {
        return chart.getData().getLast();
    }

    private XYChart.Data<Number, Number> lastPoint() {
        for (int i = chart.getData().size() - 1; i >= 0; i--) {
            var data = chart.getData().get(i).getData();
            if (!data.isEmpty()) {
                return data.getLast();
            }
        }
        return null;
    }

    private XYChart.Data<Number, Number> firstPoint() {
        for (var series : chart.getData()) {
            var data = series.getData();
            if (!data.isEmpty()) {
                return data.getFirst();
            }
        }
        return null;
    }

    // Marker decimation ("3D points"): cap the drawn symbols at MAX_SYMBOLS regardless of point count

    /**
     * Coalesces marker refreshes: points can stream in at the vo frame rate, but the decimation only
     * needs to run once per pulse. The first request schedules a single {@link #applySymbolDecimation()}
     * via {@link Platform#runLater}; further requests in the same pulse are dropped until it fires.
     */
    private void scheduleSymbolRefresh() {
        if (symbolRefreshScheduled) {
            return;
        }
        symbolRefreshScheduled = true;
        Platform.runLater(() -> {
            symbolRefreshScheduled = false;
            applySymbolDecimation();
        });
    }

    /**
     * Shows only every {@code step}-th sample's symbol (step chosen so at most {@value #MAX_SYMBOLS}
     * remain visible), hiding the rest via {@code visible}/{@code managed=false} so they cost nothing
     * to render or lay out. {@code getNode()} is non-null here because non-animated LineChart creates
     * each symbol synchronously on add; a still-null node is simply skipped and caught on the next pass.
     */
    private void applySymbolDecimation() {
        if (!symbolsVisible) {
            return;
        }
        int total = 0;
        for (var series : chart.getData()) {
            total += series.getData().size();
        }
        int step = Math.max(1, (int) Math.ceil((double) total / MAX_SYMBOLS));
        int index = 0;
        for (var series : chart.getData()) {
            for (var point : series.getData()) {
                Node symbol = point.getNode();
                if (symbol != null) {
                    boolean show = index % step == 0;
                    symbol.setVisible(show);
                    symbol.setManaged(show);
                }
                index++;
            }
        }
    }

    private void setAutoRanging(boolean auto) {
        xAxis.setAutoRanging(auto);
        yAxis.setAutoRanging(auto);
    }

    /** Recentres an axis on {@code center}, preserving its current span (default when degenerate). */
    private void center(double cx, double cy) {
        recenter(xAxis, cx);
        recenter(yAxis, cy);
    }

    private static void recenter(ValueAxis<Number> axis, double center) {
        double span = axis.getUpperBound() - axis.getLowerBound();
        if (!(span > 0)) {
            span = DEFAULT_SPAN;
        }
        axis.setLowerBound(center - span / 2);
        axis.setUpperBound(center + span / 2);
    }

    private static void setSpan(ValueAxis<Number> axis, double span) {
        double center = (axis.getUpperBound() + axis.getLowerBound()) / 2;
        if (!Double.isFinite(center)) {
            center = 0;
        }
        axis.setLowerBound(center - span / 2);
        axis.setUpperBound(center + span / 2);
    }
}
