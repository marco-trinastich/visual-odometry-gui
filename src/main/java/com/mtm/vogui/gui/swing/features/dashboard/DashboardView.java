/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.dashboard;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.features.dashboard.info.InfoView;
import com.mtm.vogui.gui.swing.features.dashboard.trajectory.TrajectoryView;
import com.mtm.vogui.gui.swing.shared.listeners.MaximizeOnDblClickListener;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.models.enums.core.ProcessingState;
import com.mtm.vogui.models.enums.settings.ChartType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Dashboard feature facade (humble view): owns the output window (trajectory charts +
 * telemetry) and its two sub-views ({@link TrajectoryView}, {@link InfoView}). It builds and
 * lays out the frame, wires the cross-feature point-selection navigation, and exposes the
 * segment lifecycle as marshaled intents; per-frame rendering goes straight to the sub-views
 * via {@link #trajectory()} / {@link #info()}.
 */
public class DashboardView {

    private final AppContext context;

    private final TrajectoryView trajectoryView = new TrajectoryView();
    private final InfoView infoView = new InfoView();

    private final JFrame frame;

    public DashboardView(@NotNull AppContext context) {
        this.context = context;
        this.frame = buildFrame();
        wirePointNavigation();
    }

    /**
     * Makes the dashboard window visible (called once at startup by the composition root).
     */
    public void show() {
        this.frame.setVisible(true);
    }

    // Sub-views (per-frame rendering + settings controls talk to these)

    public TrajectoryView trajectory() {
        return this.trajectoryView;
    }

    public InfoView info() {
        return this.infoView;
    }

    // Synchronous query

    public int chartsCount() {
        return this.trajectoryView.xzChartsCount();
    }

    // Segment lifecycle (any thread: one EDT hop, orchestrating both sub-views)

    /**
     * Opens a trajectory segment: marks the start point and aligns the altitude axis to the
     * chart type.
     */
    public void startSegment(@NotNull ProcessingParameters params) {
        var chartType = params.frozenContext().settings().chart().type();
        SwingUtilities.invokeLater(() -> {
            this.infoView.addStartPoint(params);
            this.trajectoryView.setAltitudeAxis(chartType);
        });
    }

    /**
     * Closes the current segment if it has points on both charts; otherwise drops it.
     */
    public void endSegment(@NotNull ProcessingParameters params) {
        SwingUtilities.invokeLater(() -> {
            if (this.trajectoryView.hasBothOpenSegmentPoints()) {
                this.trajectoryView.closeSegment();
                this.infoView.addEndPoint(params);
            } else {
                this.infoView.dropOpenSegment();
            }
        });
    }

    /**
     * Clears charts and tracked points and hides the info panel. Safe from any thread.
     */
    public void clearAll() {
        SwingUtilities.invokeLater(() -> {
            this.infoView.clear();
            this.trajectoryView.clearAll();
        });
    }

    // Composition

    private JFrame buildFrame() {
        JFrame chartFrame = new JFrame(GuiConstants.DASHBOARD_FRAME_TITLE);
        chartFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JComponent xzComponent = this.trajectoryView.xzComponent();
        JComponent yComponent = this.trajectoryView.yComponent();
        JComponent infoComponent = this.infoView.panel();

        Container contentPane = chartFrame.getContentPane();
        contentPane.add(xzComponent);
        contentPane.add(yComponent);
        contentPane.add(infoComponent);

        // Maximize each panel on double-click
        xzComponent.addMouseListener(new MaximizeOnDblClickListener(xzComponent, chartFrame));
        yComponent.addMouseListener(new MaximizeOnDblClickListener(yComponent, chartFrame));
        infoComponent.addMouseListener(new MaximizeOnDblClickListener(infoComponent, chartFrame));

        // Layout: XZ chart (top ~11/30), Y chart (middle), info panel (bottom ~11/30)
        SpringLayout frameLayout = new SpringLayout();
        Spring hpSpring = new HeightProportionalSpring(chartFrame, 11 / 30f);

        frameLayout.putConstraint(SpringLayout.NORTH, xzComponent, 5, SpringLayout.NORTH, contentPane);
        frameLayout.putConstraint(SpringLayout.WEST, xzComponent, 5, SpringLayout.WEST, contentPane);
        frameLayout.putConstraint(SpringLayout.EAST, xzComponent, -5, SpringLayout.EAST, contentPane);
        frameLayout.putConstraint(SpringLayout.SOUTH, xzComponent, hpSpring, SpringLayout.NORTH, contentPane);

        frameLayout.putConstraint(SpringLayout.NORTH, yComponent, 5, SpringLayout.SOUTH, xzComponent);
        frameLayout.putConstraint(SpringLayout.WEST, yComponent, 5, SpringLayout.WEST, contentPane);
        frameLayout.putConstraint(SpringLayout.EAST, yComponent, -5, SpringLayout.EAST, contentPane);
        frameLayout.putConstraint(SpringLayout.SOUTH, contentPane, hpSpring, SpringLayout.SOUTH, yComponent);

        frameLayout.putConstraint(SpringLayout.NORTH, infoComponent, 5, SpringLayout.SOUTH, yComponent);
        frameLayout.putConstraint(SpringLayout.WEST, infoComponent, 5, SpringLayout.WEST, contentPane);
        frameLayout.putConstraint(SpringLayout.EAST, infoComponent, -5, SpringLayout.EAST, contentPane);
        frameLayout.putConstraint(SpringLayout.SOUTH, infoComponent, -5, SpringLayout.SOUTH, contentPane);

        chartFrame.setLayout(frameLayout);
        chartFrame.setLocation(0, 0);
        chartFrame.setPreferredSize(SwingUtils.getDefaultFrameDimension());
        chartFrame.pack();

        return chartFrame;
    }

    /**
     * Wires tracked-point selection to chart navigation: selecting a point (while idle) scrolls
     * both charts to it. Cross-feature glue kept in the facade (needs both sub-views + core state).
     */
    private void wirePointNavigation() {
        this.infoView.addPointSelectionListener(evt -> {
            // Only when the selection settled (not mid-adjust) and no processing is running
            if (evt.getValueIsAdjusting() ||
                    this.context.state().processing().is(ProcessingState.Running)) {
                return;
            }

            TrackedPoint selected = this.infoView.selectedPoint();
            if (selected == null) {
                return;
            }

            if (selected.x() != null && selected.z() != null) {
                this.trajectoryView.moveXZToPoint(selected.x(), selected.z());
            }
            if (selected.y() != null) {
                var x = ChartType.YFrames.is(selected.chartType()) ? selected.frame() : selected.time();
                this.trajectoryView.moveYToPoint(x, selected.y());
            }
        });
    }

    /**
     * A {@link Spring} fixed to a fraction of a component's live height.
     */
    private static final class HeightProportionalSpring extends Spring {
        private final Component component;
        private final float proportion;

        private HeightProportionalSpring(Component component, float proportion) {
            this.component = component;
            this.proportion = proportion;
        }

        @Override
        public void setValue(int direction) {
        }

        @Override
        public int getValue() {
            return Math.round(component.getHeight() * proportion);
        }

        @Override
        public int getPreferredValue() {
            return Math.round(component.getHeight() * proportion);
        }

        @Override
        public int getMinimumValue() {
            return Math.round(component.getHeight() * proportion);
        }

        @Override
        public int getMaximumValue() {
            return Math.round(component.getHeight() * proportion);
        }
    }
}
