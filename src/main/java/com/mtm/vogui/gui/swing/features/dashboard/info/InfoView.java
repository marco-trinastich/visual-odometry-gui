/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.dashboard.info;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.features.dashboard.DashboardView;
import com.mtm.vogui.gui.swing.features.dashboard.info.buffer.BufferInfoView;
import com.mtm.vogui.gui.swing.features.dashboard.info.fps.FpsInfoView;
import com.mtm.vogui.gui.swing.features.dashboard.info.odometry.OdometryInfoView;
import com.mtm.vogui.gui.swing.features.dashboard.info.processing.ProcessingInfoView;
import com.mtm.vogui.gui.swing.features.dashboard.info.trackedpoints.TrackedPointsView;
import com.mtm.vogui.gui.swing.features.dashboard.info.tracking.TrackingInfoView;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.core.concurrency.AwaitableBuffer;
import com.mtm.vogui.models.core.integration.BufferStatus;
import com.mtm.vogui.models.core.processing.ProcessingParameters;
import com.mtm.vogui.models.core.processing.ProcessingStatus;
import com.mtm.vogui.models.core.processing.fps.FpsStatus;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.models.enums.gui.AppStatus;
import com.mtm.vogui.models.enums.settings.ChartType;
import com.mtm.vogui.models.enums.settings.SourceType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import java.awt.*;

/**
 * Info/telemetry feature facade (humble view): owns the scrollable info pane of the dashboard
 * window and composes the telemetry sections ({@link ProcessingInfoView}, {@link OdometryInfoView},
 * {@link TrackingInfoView}, {@link FpsInfoView}, {@link BufferInfoView}, {@link TrackedPointsView}),
 * plus the status label. Consumers express intent through methods - no widget leaves this class.
 * <p>
 * Threading: high-frequency renders driven by the vo worker thread ({@link #renderTelemetry},
 * {@link #setCurrentFps}, {@link #addTrackedPoint}, {@link #showBuffer}/{@link #hideBuffer},
 * {@link #setAppStatus}) marshal to the EDT here, then call the (thread-unaware) sections; the
 * segment-lifecycle ops are invoked from inside a {@link DashboardView} {@code invokeLater} and
 * mutate directly.
 */
public class InfoView {

    private static final int LONGER_RENDER_INTERVAL = 10;

    private final ProcessingInfoView processingView = new ProcessingInfoView();
    private final OdometryInfoView odometryView = new OdometryInfoView();
    private final TrackingInfoView trackingView = new TrackingInfoView();
    private final FpsInfoView fpsView = new FpsInfoView();
    private final BufferInfoView bufferView = new BufferInfoView();
    private final TrackedPointsView trackedPointsView = new TrackedPointsView();

    private final JLabel lblStatus = new JLabel();
    private final JPanel sectionsPanel = new JPanel();
    private final JPanel container = new JPanel();
    private final JScrollPane scrollPane = new JScrollPane();

    private AppStatus status;

    public InfoView() {
        buildSections();
        buildContainer();

        this.scrollPane.setOpaque(false);
        this.scrollPane.setBorder(SwingUtils.getRoundedTitledBorder(
                GuiConstants.INFO_PANEL_TITLE, GuiConstants.PANEL_BORDER_ACTIVE_COLOR));

        setSectionsVisible(false);
        this.bufferView.panel().setVisible(false);
        setAppStatus(AppStatus.Ready);
        this.scrollPane.setPreferredSize(new Dimension(400, 400));
    }

    // Frame composition / navigation wiring (used by DashboardView only)

    public JComponent panel() {
        return this.scrollPane;
    }

    public void addPointSelectionListener(ListSelectionListener listener) {
        this.trackedPointsView.addSelectionListener(listener);
    }

    public TrackedPoint selectedPoint() {
        return this.trackedPointsView.selectedPoint();
    }

    // App status (any thread)

    public void setAppStatus(AppStatus appStatus) {
        SwingUtilities.invokeLater(() -> {
            if (this.status != appStatus) {
                this.status = appStatus;
                this.lblStatus.setText(String.format(GuiConstants.LBL_STATUS, appStatus.value()));
            }
        });
    }

    // Per-frame telemetry (vo worker thread: one EDT hop)

    /**
     * Renders the whole telemetry block from the frozen status: processing info, position,
     * rotation, tracking, average fps, the new tracked point (when {@code voResult}) and, on
     * the slower interval, the direction panels and covered distance.
     */
    public void renderTelemetry(@NotNull ProcessingStatus frozenStatus, @NotNull ProcessingParameters params,
                                double deviceFps, boolean voResult) {
        var sourceType = params.frozenContext().settings().input().source();
        var chartType = params.frozenContext().settings().chart().type();

        SwingUtilities.invokeLater(() -> {
            if (!this.sectionsPanel.isVisible()) {
                setSectionsVisible(true);
            }

            this.processingView.setCalibrationFile(params.frozenContext().settings().input().calibration().path());

            String sourcePath = SourceType.Video.is(sourceType) ?
                    params.frozenContext().settings().input().video().path() :
                    params.frozenContext().settings().input().device().path().name();
            this.processingView.setProcessedSource(sourcePath, sourceType);

            this.processingView.setProcessedFrames(frozenStatus.fps().totalProcessed(),
                    frozenStatus.fps().totalFrames());
            this.processingView.setElapsedTime(frozenStatus.fps().totalSeconds());

            this.odometryView.setPosition(frozenStatus.translation());
            this.odometryView.setRotationMatrix(frozenStatus.rotation());
            this.trackingView.setTrackingStatus(frozenStatus.tracking());

            if (SourceType.Device.is(sourceType)) {
                frozenStatus.fps().inputAverageFPS(deviceFps);
            }
            this.fpsView.setAverageFps(frozenStatus.fps());

            if (voResult) {
                Double loggedY = ChartType.YSeconds.is(chartType) ? null : -frozenStatus.translation().getY();
                this.trackedPointsView.addPoint(params.pointFactory().newPoint(frozenStatus, loggedY));
            }

            // Slower interval: items rendered less often to stay meaningful
            if (frozenStatus.fps().totalProcessed() % LONGER_RENDER_INTERVAL == 0) {
                this.odometryView.setDirectionPanels(frozenStatus.translation(), frozenStatus.prevTranslation());
                this.odometryView.setIncrementalDistance(frozenStatus.translation(), frozenStatus.prevTranslation());
            }
        });
    }

    public void setCurrentFps(@NotNull ProcessingStatus frozenStatus) {
        FpsStatus fps = frozenStatus.fps();
        SwingUtilities.invokeLater(() -> this.fpsView.setCurrentFps(fps));
    }

    public void addTrackedPoint(TrackedPoint point) {
        SwingUtilities.invokeLater(() -> this.trackedPointsView.addPoint(point));
    }

    // Buffer (vo worker thread: one EDT hop)

    public void showBuffer(@NotNull BufferStatus bufferStatus) {
        boolean isInfinite = bufferStatus.maxBufferItems() == AwaitableBuffer.INFINITE_BUFFER;
        SwingUtilities.invokeLater(() -> {
            if (!this.bufferView.panel().isVisible()) {
                setBufferVisible(true);
            }
            this.bufferView.setBufferProgressBar(bufferStatus, isInfinite);
            this.bufferView.setBufferLabel(bufferStatus, isInfinite);
        });
    }

    public void hideBuffer() {
        if (this.bufferView.panel().isVisible()) {
            SwingUtilities.invokeLater(() -> setBufferVisible(false));
        }
    }

    // Segment lifecycle (EDT only: called inside a DashboardView invokeLater)

    public void addStartPoint(@NotNull ProcessingParameters params) {
        this.trackedPointsView.addPoint(params.pointFactory().newStartPoint());
    }

    public void addEndPoint(@NotNull ProcessingParameters params) {
        this.trackedPointsView.addPoint(params.pointFactory().newEndPoint());
    }

    /**
     * Drops the points of the last (unclosed) segment: from its start marker to the end.
     */
    public void dropOpenSegment() {
        this.trackedPointsView.dropOpenSegment();
    }

    /**
     * Clears the tracked points and hides the info sections.
     */
    public void clear() {
        this.trackedPointsView.clear();
        setSectionsVisible(false);
    }

    // Composition

    private void buildSections() {
        this.sectionsPanel.setOpaque(false);
        this.sectionsPanel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        addSection(this.processingView.panel(), gbc, 0);
        addSection(this.odometryView.panel(), gbc, 5);
        addSection(this.trackingView.panel(), gbc, 5);
        addSection(this.fpsView.panel(), gbc, 5);
        addSection(this.bufferView.panel(), gbc, 5);

        // Tracked points: fills the remaining vertical space
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1;
        this.sectionsPanel.add(this.trackedPointsView.panel(), gbc);
    }

    private void addSection(JComponent section, GridBagConstraints gbc, int topInset) {
        gbc.insets = new Insets(topInset, 0, 0, 0);
        this.sectionsPanel.add(section, gbc);
    }

    private void buildContainer() {
        this.container.setPreferredSize(new Dimension(GuiConstants.INFO_PANEL_WIDTH, GuiConstants.INFO_PANEL_HEIGHT));
        this.container.add(this.lblStatus);
        this.container.add(this.sectionsPanel);

        SpringLayout layout = new SpringLayout();
        layout.putConstraint(SpringLayout.NORTH, this.lblStatus, 5, SpringLayout.NORTH, this.container);
        layout.putConstraint(SpringLayout.WEST, this.lblStatus, 5, SpringLayout.WEST, this.container);
        layout.putConstraint(SpringLayout.EAST, this.lblStatus, -5, SpringLayout.EAST, this.container);

        layout.putConstraint(SpringLayout.NORTH, this.sectionsPanel, 10, SpringLayout.SOUTH, this.lblStatus);
        layout.putConstraint(SpringLayout.WEST, this.sectionsPanel, 5, SpringLayout.WEST, this.container);
        layout.putConstraint(SpringLayout.EAST, this.sectionsPanel, -5, SpringLayout.EAST, this.container);
        layout.putConstraint(SpringLayout.SOUTH, this.sectionsPanel, -5, SpringLayout.SOUTH, this.container);
        this.container.setLayout(layout);

        this.scrollPane.setViewportView(this.container);
    }

    private void setSectionsVisible(boolean visible) {
        this.sectionsPanel.setVisible(visible);
        this.container.setPreferredSize(new Dimension(
                GuiConstants.INFO_PANEL_WIDTH,
                visible ? GuiConstants.INFO_PANEL_HEIGHT : 100));
        this.container.revalidate();
        this.container.repaint();
    }

    private void setBufferVisible(boolean visible) {
        this.bufferView.panel().setVisible(visible);
        // GridBag collapses the (invisible) buffer section: the tracked-points list reclaims the space
        this.sectionsPanel.revalidate();
        this.sectionsPanel.repaint();
    }
}
