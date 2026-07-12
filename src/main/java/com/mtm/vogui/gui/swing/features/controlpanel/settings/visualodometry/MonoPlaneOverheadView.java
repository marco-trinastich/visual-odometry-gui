/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.settings.visualodometry;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.textfield.DoubleTextField;
import com.mtm.vogui.gui.swing.shared.components.textfield.IntegerTextField;
import com.mtm.vogui.models.context.AppContext;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Mono-plane-overhead VO parameters (humble view): owns its own widgets as private fields and
 * exposes intents only. Sub-view of {@link VoSettingsView}, swapped into the VO scroll pane.
 * Widgets commit live into the settings; {@link #load()} refreshes them back after a load/reset.
 */
public class MonoPlaneOverheadView {

    private final AppContext context;

    private final DoubleTextField txtCellSize;
    private final DoubleTextField txtMaxCellsPerPixel;
    private final DoubleTextField txtMapHeightFraction;
    private final DoubleTextField txtInlierGroundTol;
    private final IntegerTextField txtRansacIterations;
    private final IntegerTextField txtThresholdRetire;
    private final IntegerTextField txtAbsoluteMinimumTracks;
    private final DoubleTextField txtRespawnTrackFraction;
    private final DoubleTextField txtRespawnCoverageFraction;

    private final JPanel panel;

    public MonoPlaneOverheadView(@NotNull AppContext context) {
        this.context = context;
        var monoVoSettings = context.settings().visualOdometry().monoPlaneOverhead();
        var defaultMonoVoSettings = monoVoSettings.getDefault();

        // cellSize
        var lblCellSize = new JLabel(GuiConstants.LBL_VO_MONO_OVH_CELL_SIZE);
        this.txtCellSize = new DoubleTextField(
                monoVoSettings::cellSize,
                monoVoSettings::cellSize,
                defaultMonoVoSettings.cellSize(),
                5,
                JTextField.CENTER
        );

        // maxCellsPerPixel
        var lblMaxCellsPerPixel = new JLabel(GuiConstants.LBL_VO_MONO_OVH_MAX_CELL_PER_PIXEL);
        this.txtMaxCellsPerPixel = new DoubleTextField(
                monoVoSettings::maxCellsPerPixel,
                monoVoSettings::maxCellsPerPixel,
                defaultMonoVoSettings.maxCellsPerPixel(),
                5,
                JTextField.CENTER
        );

        // mapHeightFraction
        var lblMapHeightFraction = new JLabel(GuiConstants.LBL_VO_MONO_OVH_MAP_HEIGHT_FRACTION);
        this.txtMapHeightFraction = new DoubleTextField(
                monoVoSettings::mapHeightFraction,
                monoVoSettings::mapHeightFraction,
                defaultMonoVoSettings.mapHeightFraction(),
                5,
                JTextField.CENTER
        );

        // inlierGroundTol
        var lblInlierGroundTol = new JLabel(GuiConstants.LBL_VO_MONO_OVH_INLIER_GROUND_TOL);
        this.txtInlierGroundTol = new DoubleTextField(
                monoVoSettings::inlierGroundTol,
                monoVoSettings::inlierGroundTol,
                defaultMonoVoSettings.inlierGroundTol(),
                5,
                JTextField.CENTER
        );

        // ransacIterations
        var lblRansacIterations = new JLabel(GuiConstants.LBL_VO_MONO_RANSAC_ITERATIONS);
        this.txtRansacIterations = new IntegerTextField(
                monoVoSettings::ransacIterations,
                monoVoSettings::ransacIterations,
                defaultMonoVoSettings.ransacIterations(),
                5,
                JTextField.CENTER
        );

        // thresholdRetire
        var lblThresholdRetire = new JLabel(GuiConstants.LBL_VO_MONO_THRESHOLD_RETIRE);
        this.txtThresholdRetire = new IntegerTextField(
                monoVoSettings::thresholdRetire,
                monoVoSettings::thresholdRetire,
                defaultMonoVoSettings.thresholdRetire(),
                5,
                JTextField.CENTER
        );

        // absoluteMinimumTracks
        var lblAbsoluteMinimumTracks = new JLabel(GuiConstants.LBL_VO_MONO_OVH_ABSOLUTE_MINIMUM_TRACKS);
        this.txtAbsoluteMinimumTracks = new IntegerTextField(
                monoVoSettings::absoluteMinimumTracks,
                monoVoSettings::absoluteMinimumTracks,
                defaultMonoVoSettings.absoluteMinimumTracks(),
                5,
                JTextField.CENTER
        );

        // respawnTrackFraction
        var lblRespawnTrackFraction = new JLabel(GuiConstants.LBL_VO_MONO_OVH_RESPAWN_TRACK_FRACTION);
        this.txtRespawnTrackFraction = new DoubleTextField(
                monoVoSettings::respawnTrackFraction,
                monoVoSettings::respawnTrackFraction,
                defaultMonoVoSettings.respawnTrackFraction(),
                5,
                JTextField.CENTER
        );

        // respawnCoverageFraction
        var lblRespawnCoverageFraction = new JLabel(GuiConstants.LBL_VO_MONO_OVH_RESPAWN_COVERAGE_FRACTION);
        this.txtRespawnCoverageFraction = new DoubleTextField(
                monoVoSettings::respawnCoverageFraction,
                monoVoSettings::respawnCoverageFraction,
                defaultMonoVoSettings.respawnCoverageFraction(),
                5,
                JTextField.CENTER
        );

        // panel
        this.panel = new JPanel();
        this.panel.add(lblCellSize);
        this.panel.add(this.txtCellSize);
        this.panel.add(lblMaxCellsPerPixel);
        this.panel.add(this.txtMaxCellsPerPixel);
        this.panel.add(lblMapHeightFraction);
        this.panel.add(this.txtMapHeightFraction);
        this.panel.add(lblInlierGroundTol);
        this.panel.add(this.txtInlierGroundTol);
        this.panel.add(lblRansacIterations);
        this.panel.add(this.txtRansacIterations);
        this.panel.add(lblThresholdRetire);
        this.panel.add(this.txtThresholdRetire);
        this.panel.add(lblAbsoluteMinimumTracks);
        this.panel.add(this.txtAbsoluteMinimumTracks);
        this.panel.add(lblRespawnTrackFraction);
        this.panel.add(this.txtRespawnTrackFraction);
        this.panel.add(lblRespawnCoverageFraction);
        this.panel.add(this.txtRespawnCoverageFraction);

        // layout
        SpringLayout panelLayout = new SpringLayout();

        // first row (8px top pad, matching MonoPlaneInfinityView: keeps the combo->content gap
        // consistent across VO types and clears the topmost text field's top edge)
        panelLayout.putConstraint(SpringLayout.NORTH, lblCellSize, 8, SpringLayout.NORTH, this.panel);
        panelLayout.putConstraint(SpringLayout.WEST, lblCellSize, 5, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtCellSize, -1, SpringLayout.NORTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtCellSize, 0, SpringLayout.WEST, this.txtThresholdRetire);
        panelLayout.putConstraint(SpringLayout.NORTH, lblMaxCellsPerPixel, 0, SpringLayout.NORTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, lblMaxCellsPerPixel, 3, SpringLayout.EAST, this.txtCellSize);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtMaxCellsPerPixel, -1, SpringLayout.NORTH, lblMaxCellsPerPixel);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtMaxCellsPerPixel, 0, SpringLayout.WEST, this.txtAbsoluteMinimumTracks);
        panelLayout.putConstraint(SpringLayout.NORTH, lblMapHeightFraction, 0, SpringLayout.NORTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, lblMapHeightFraction, 3, SpringLayout.EAST, this.txtMaxCellsPerPixel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtMapHeightFraction, -1, SpringLayout.NORTH, lblMapHeightFraction);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtMapHeightFraction, 0, SpringLayout.WEST, this.txtRespawnTrackFraction);
        panelLayout.putConstraint(SpringLayout.NORTH, lblInlierGroundTol, 0, SpringLayout.NORTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, lblInlierGroundTol, 3, SpringLayout.EAST, this.txtMapHeightFraction);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtInlierGroundTol, -1, SpringLayout.NORTH, lblInlierGroundTol);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtInlierGroundTol, 0, SpringLayout.WEST, this.txtRespawnCoverageFraction);
        panelLayout.putConstraint(SpringLayout.NORTH, lblRansacIterations, 0, SpringLayout.NORTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, lblRansacIterations, 3, SpringLayout.EAST, this.txtInlierGroundTol);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtRansacIterations, -1, SpringLayout.NORTH, lblRansacIterations);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtRansacIterations, 3, SpringLayout.EAST, lblRansacIterations);

        // second row
        panelLayout.putConstraint(SpringLayout.NORTH, lblThresholdRetire, 10, SpringLayout.SOUTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, lblThresholdRetire, 5, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtThresholdRetire, -1, SpringLayout.NORTH, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtThresholdRetire, 3, SpringLayout.EAST, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.NORTH, lblAbsoluteMinimumTracks, 0, SpringLayout.NORTH, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.WEST, lblAbsoluteMinimumTracks, 3, SpringLayout.EAST, this.txtThresholdRetire);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtAbsoluteMinimumTracks, -1, SpringLayout.NORTH, lblAbsoluteMinimumTracks);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtAbsoluteMinimumTracks, 3, SpringLayout.EAST, lblAbsoluteMinimumTracks);
        panelLayout.putConstraint(SpringLayout.NORTH, lblRespawnTrackFraction, 0, SpringLayout.NORTH, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.WEST, lblRespawnTrackFraction, 3, SpringLayout.EAST, this.txtAbsoluteMinimumTracks);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtRespawnTrackFraction, -1, SpringLayout.NORTH, lblRespawnTrackFraction);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtRespawnTrackFraction, 3, SpringLayout.EAST, lblRespawnTrackFraction);
        panelLayout.putConstraint(SpringLayout.NORTH, lblRespawnCoverageFraction, 0, SpringLayout.NORTH, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.WEST, lblRespawnCoverageFraction, 3, SpringLayout.EAST, this.txtRespawnTrackFraction);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtRespawnCoverageFraction, -1, SpringLayout.NORTH, lblRespawnCoverageFraction);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtRespawnCoverageFraction, 3, SpringLayout.EAST, lblRespawnCoverageFraction);

        this.panel.setLayout(panelLayout);
        // Shared VO content height (reserves the AS_NEEDED horizontal-scrollbar band; see the
        // constant): the scrollbar overlays empty space at the bottom instead of clipping a row.
        this.panel.setPreferredSize(new Dimension(1200, GuiConstants.VO_SETTINGS_CONTENT_HEIGHT));
    }

    /**
     * Section panel, consumed by {@link VoSettingsView} composition only.
     */
    public JPanel panel() {
        return this.panel;
    }

    /**
     * Refreshes every widget from the current settings (after a load/reset).
     */
    public void load() {
        var monoVoSettings = this.context.settings().visualOdometry().monoPlaneOverhead();
        this.txtCellSize.updateModel(monoVoSettings.cellSize());
        this.txtMaxCellsPerPixel.updateModel(monoVoSettings.maxCellsPerPixel());
        this.txtMapHeightFraction.updateModel(monoVoSettings.mapHeightFraction());
        this.txtInlierGroundTol.updateModel(monoVoSettings.inlierGroundTol());
        this.txtRansacIterations.updateModel(monoVoSettings.ransacIterations());
        this.txtThresholdRetire.updateModel(monoVoSettings.thresholdRetire());
        this.txtAbsoluteMinimumTracks.updateModel(monoVoSettings.absoluteMinimumTracks());
        this.txtRespawnTrackFraction.updateModel(monoVoSettings.respawnTrackFraction());
        this.txtRespawnCoverageFraction.updateModel(monoVoSettings.respawnCoverageFraction());
    }
}
