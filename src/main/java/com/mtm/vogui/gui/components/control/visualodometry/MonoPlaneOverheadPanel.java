package com.mtm.vogui.gui.components.control.visualodometry;

import com.mtm.vogui.gui.components.common.textfield.DoubleTextField;
import com.mtm.vogui.gui.components.common.textfield.IntegerTextField;
import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.settings.core.visualodometry.VisualOdometrySettings;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

@Getter
public class MonoPlaneOverheadPanel extends JPanel {

    private JLabel lblCellSize;
    private DoubleTextField txtCellSize;
    private JLabel lblMaxCellsPerPixel;
    private DoubleTextField txtMaxCellsPerPixel;
    private JLabel lblMapHeightFraction;
    private DoubleTextField txtMapHeightFraction;
    private JLabel lblInlierGroundTol;
    private DoubleTextField txtInlierGroundTol;
    private JLabel lblRansacIterations;
    private IntegerTextField txtRansacIterations;
    private JLabel lblThresholdRetire;
    private IntegerTextField txtThresholdRetire;
    private JLabel lblAbsoluteMinimumTracks;
    private IntegerTextField txtAbsoluteMinimumTracks;
    private JLabel lblRespawnTrackFraction;
    private DoubleTextField txtRespawnTrackFraction;
    private JLabel lblRespawnCoverageFraction;
    private DoubleTextField txtRespawnCoverageFraction;

    public MonoPlaneOverheadPanel(@NotNull VisualOdometrySettings voSettings) {
        super();

        this.createInnerComponents(voSettings);
        this.initPanel();
        this.initLayout();
        this.setPreferredSize(new Dimension(1200, 70));
    }

    private void createInnerComponents(@NotNull VisualOdometrySettings voSettings) {
        var monoVoSettings = voSettings.monoPlaneOverhead();
        var defaultMonoVoSettings = monoVoSettings.getDefault();

        // cellSize
        this.lblCellSize = new JLabel(GuiConstants.LBL_VO_MONO_OVH_CELL_SIZE);
        this.txtCellSize = new DoubleTextField(
                monoVoSettings::cellSize,
                monoVoSettings::cellSize,
                defaultMonoVoSettings.cellSize(),
                5,
                JTextField.CENTER
        );

        // maxCellsPerPixel
        this.lblMaxCellsPerPixel = new JLabel(GuiConstants.LBL_VO_MONO_OVH_MAX_CELL_PER_PIXEL);
        this.txtMaxCellsPerPixel = new DoubleTextField(
                monoVoSettings::maxCellsPerPixel,
                monoVoSettings::maxCellsPerPixel,
                defaultMonoVoSettings.maxCellsPerPixel(),
                5,
                JTextField.CENTER
        );

        // mapHeightFraction
        this.lblMapHeightFraction = new JLabel(GuiConstants.LBL_VO_MONO_OVH_MAP_HEIGHT_FRACTION);
        this.txtMapHeightFraction = new DoubleTextField(
                monoVoSettings::mapHeightFraction,
                monoVoSettings::mapHeightFraction,
                defaultMonoVoSettings.mapHeightFraction(),
                5,
                JTextField.CENTER
        );

        // inlierGroundTol
        this.lblInlierGroundTol = new JLabel(GuiConstants.LBL_VO_MONO_OVH_INLIER_GROUND_TOL);
        this.txtInlierGroundTol = new DoubleTextField(
                monoVoSettings::inlierGroundTol,
                monoVoSettings::inlierGroundTol,
                defaultMonoVoSettings.inlierGroundTol(),
                5,
                JTextField.CENTER
        );

        // ransacIterations
        this.lblRansacIterations = new JLabel(GuiConstants.LBL_VO_MONO_RANSAC_ITERATIONS);
        this.txtRansacIterations = new IntegerTextField(
                monoVoSettings::ransacIterations,
                monoVoSettings::ransacIterations,
                defaultMonoVoSettings.ransacIterations(),
                5,
                JTextField.CENTER
        );

        // thresholdRetire
        this.lblThresholdRetire = new JLabel(GuiConstants.LBL_VO_MONO_THRESHOLD_RETIRE);
        this.txtThresholdRetire = new IntegerTextField(
                monoVoSettings::thresholdRetire,
                monoVoSettings::thresholdRetire,
                defaultMonoVoSettings.thresholdRetire(),
                5,
                JTextField.CENTER
        );

        // absoluteMinimumTracks
        this.lblAbsoluteMinimumTracks = new JLabel(GuiConstants.LBL_VO_MONO_OVH_ABSOLUTE_MINIMUM_TRACKS);
        this.txtAbsoluteMinimumTracks = new IntegerTextField(
                monoVoSettings::absoluteMinimumTracks,
                monoVoSettings::absoluteMinimumTracks,
                defaultMonoVoSettings.absoluteMinimumTracks(),
                5,
                JTextField.CENTER
        );

        // respawnTrackFraction
        this.lblRespawnTrackFraction = new JLabel(GuiConstants.LBL_VO_MONO_OVH_RESPAWN_TRACK_FRACTION);
        this.txtRespawnTrackFraction = new DoubleTextField(
                monoVoSettings::respawnTrackFraction,
                monoVoSettings::respawnTrackFraction,
                defaultMonoVoSettings.respawnTrackFraction(),
                5,
                JTextField.CENTER
        );

        // respawnCoverageFraction
        this.lblRespawnCoverageFraction = new JLabel(GuiConstants.LBL_VO_MONO_OVH_RESPAWN_COVERAGE_FRACTION);
        this.txtRespawnCoverageFraction = new DoubleTextField(
                monoVoSettings::respawnCoverageFraction,
                monoVoSettings::respawnCoverageFraction,
                defaultMonoVoSettings.respawnCoverageFraction(),
                5,
                JTextField.CENTER
        );
    }

    private void initPanel() {
        // panel
        this.add(this.lblCellSize);
        this.add(this.txtCellSize);
        this.add(this.lblMaxCellsPerPixel);
        this.add(this.txtMaxCellsPerPixel);
        this.add(this.lblMapHeightFraction);
        this.add(this.txtMapHeightFraction);
        this.add(this.lblInlierGroundTol);
        this.add(this.txtInlierGroundTol);
        this.add(this.lblRansacIterations);
        this.add(this.txtRansacIterations);
        this.add(this.lblThresholdRetire);
        this.add(this.txtThresholdRetire);
        this.add(this.lblAbsoluteMinimumTracks);
        this.add(this.txtAbsoluteMinimumTracks);
        this.add(this.lblRespawnTrackFraction);
        this.add(this.txtRespawnTrackFraction);
        this.add(lblRespawnCoverageFraction);
        this.add(txtRespawnCoverageFraction);
    }

    private void initLayout() {
        // layout
        SpringLayout panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblCellSize, 0, SpringLayout.NORTH, this);
        panelLayout.putConstraint(SpringLayout.WEST, lblCellSize, 5, SpringLayout.WEST, this);
        panelLayout.putConstraint(SpringLayout.NORTH, txtCellSize, -1, SpringLayout.NORTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, txtCellSize, 0, SpringLayout.WEST, txtThresholdRetire);
        panelLayout.putConstraint(SpringLayout.NORTH, lblMaxCellsPerPixel, 0, SpringLayout.NORTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, lblMaxCellsPerPixel, 3, SpringLayout.EAST, txtCellSize);
        panelLayout.putConstraint(SpringLayout.NORTH, txtMaxCellsPerPixel, -1, SpringLayout.NORTH, lblMaxCellsPerPixel);
        panelLayout.putConstraint(SpringLayout.WEST, txtMaxCellsPerPixel, 0, SpringLayout.WEST, txtAbsoluteMinimumTracks);
        panelLayout.putConstraint(SpringLayout.NORTH, lblMapHeightFraction, 0, SpringLayout.NORTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, lblMapHeightFraction, 3, SpringLayout.EAST, txtMaxCellsPerPixel);
        panelLayout.putConstraint(SpringLayout.NORTH, txtMapHeightFraction, -1, SpringLayout.NORTH, lblMapHeightFraction);
        panelLayout.putConstraint(SpringLayout.WEST, txtMapHeightFraction, 0, SpringLayout.WEST, txtRespawnTrackFraction);
        panelLayout.putConstraint(SpringLayout.NORTH, lblInlierGroundTol, 0, SpringLayout.NORTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, lblInlierGroundTol, 3, SpringLayout.EAST, txtMapHeightFraction);
        panelLayout.putConstraint(SpringLayout.NORTH, txtInlierGroundTol, -1, SpringLayout.NORTH, lblInlierGroundTol);
        panelLayout.putConstraint(SpringLayout.WEST, txtInlierGroundTol, 0, SpringLayout.WEST, txtRespawnCoverageFraction);
        panelLayout.putConstraint(SpringLayout.NORTH, lblRansacIterations, 0, SpringLayout.NORTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, lblRansacIterations, 3, SpringLayout.EAST, txtInlierGroundTol);
        panelLayout.putConstraint(SpringLayout.NORTH, txtRansacIterations, -1, SpringLayout.NORTH, lblRansacIterations);
        panelLayout.putConstraint(SpringLayout.WEST, txtRansacIterations, 3, SpringLayout.EAST, lblRansacIterations);

        // second row
        panelLayout.putConstraint(SpringLayout.NORTH, lblThresholdRetire, 10, SpringLayout.SOUTH, lblCellSize);
        panelLayout.putConstraint(SpringLayout.WEST, lblThresholdRetire, 5, SpringLayout.WEST, this);
        panelLayout.putConstraint(SpringLayout.NORTH, txtThresholdRetire, -1, SpringLayout.NORTH, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.WEST, txtThresholdRetire, 3, SpringLayout.EAST, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.NORTH, lblAbsoluteMinimumTracks, 0, SpringLayout.NORTH, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.WEST, lblAbsoluteMinimumTracks, 3, SpringLayout.EAST, txtThresholdRetire);
        panelLayout.putConstraint(SpringLayout.NORTH, txtAbsoluteMinimumTracks, -1, SpringLayout.NORTH, lblAbsoluteMinimumTracks);
        panelLayout.putConstraint(SpringLayout.WEST, txtAbsoluteMinimumTracks, 3, SpringLayout.EAST, lblAbsoluteMinimumTracks);
        panelLayout.putConstraint(SpringLayout.NORTH, lblRespawnTrackFraction, 0, SpringLayout.NORTH, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.WEST, lblRespawnTrackFraction, 3, SpringLayout.EAST, txtAbsoluteMinimumTracks);
        panelLayout.putConstraint(SpringLayout.NORTH, txtRespawnTrackFraction, -1, SpringLayout.NORTH, lblRespawnTrackFraction);
        panelLayout.putConstraint(SpringLayout.WEST, txtRespawnTrackFraction, 3, SpringLayout.EAST, lblRespawnTrackFraction);
        panelLayout.putConstraint(SpringLayout.NORTH, lblRespawnCoverageFraction, 0, SpringLayout.NORTH, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.WEST, lblRespawnCoverageFraction, 3, SpringLayout.EAST, txtRespawnTrackFraction);
        panelLayout.putConstraint(SpringLayout.NORTH, txtRespawnCoverageFraction, -1, SpringLayout.NORTH, lblRespawnCoverageFraction);
        panelLayout.putConstraint(SpringLayout.WEST, txtRespawnCoverageFraction, 3, SpringLayout.EAST, lblRespawnCoverageFraction);

        this.setLayout(panelLayout);
    }
}
