package com.mtm.vogui.gui.components.control.visualodometry;

import com.mtm.vogui.gui.components.common.textfield.DoubleTextField;
import com.mtm.vogui.gui.components.common.textfield.IntegerTextField;
import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.models.enums.settings.VisualOdometryType;
import com.mtm.vogui.models.settings.core.visualodometry.VisualOdometrySettings;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

@Getter
public class MonoPlaneInfinityPanel extends JPanel {

    private JLabel lblThresholdAdd;
    private IntegerTextField txtThresholdAdd;
    private JLabel lblThresholdRetire;
    private IntegerTextField txtThresholdRetire;
    private JLabel lblInlierPixelTol;
    private DoubleTextField txtInlierPixelTol;
    private JLabel lblRansacIterations;
    private IntegerTextField txtRansacIterations;

    public MonoPlaneInfinityPanel(@NotNull VisualOdometrySettings voSettings) {
        super();

        this.createInnerComponents(voSettings);
        this.initPanel(voSettings);
        this.initLayout();
        this.setPreferredSize(new Dimension(this.getPreferredSize().width, 70));
    }

    private void createInnerComponents(@NotNull VisualOdometrySettings voSettings) {
        var monoVoSettings = voSettings.monoPlaneInfinity();
        var defaultMonoVoSettings = monoVoSettings.getDefault();

        // thresholdAdd
        this.lblThresholdAdd = new JLabel(GuiConstants.LBL_VO_MONO_INF_THRESHOLD_ADD);
        this.txtThresholdAdd = new IntegerTextField(
                monoVoSettings::thresholdAdd,
                monoVoSettings::thresholdAdd,
                defaultMonoVoSettings.thresholdAdd(),
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

        // inlierPixelTol
        this.lblInlierPixelTol = new JLabel(GuiConstants.LBL_VO_MONO_INF_INLIER_PIXEL_TOL);
        this.txtInlierPixelTol = new DoubleTextField(
                monoVoSettings::inlierPixelTol,
                monoVoSettings::inlierPixelTol,
                defaultMonoVoSettings.inlierPixelTol(),
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
    }

    private void initPanel(@NotNull VisualOdometrySettings voSettings) {
        // panel
        this.add(this.lblThresholdAdd);
        this.add(this.txtThresholdAdd);
        this.add(this.lblThresholdRetire);
        this.add(this.txtThresholdRetire);
        this.add(this.lblInlierPixelTol);
        this.add(this.txtInlierPixelTol);
        this.add(this.lblRansacIterations);
        this.add(this.txtRansacIterations);
    }

    private void initLayout() {
        // layout
        SpringLayout panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblThresholdAdd, 8, SpringLayout.NORTH, this);
        panelLayout.putConstraint(SpringLayout.WEST, lblThresholdAdd, 5, SpringLayout.WEST, this);
        panelLayout.putConstraint(SpringLayout.NORTH, txtThresholdAdd, -1, SpringLayout.NORTH, lblThresholdAdd);
        panelLayout.putConstraint(SpringLayout.WEST, txtThresholdAdd, 3, SpringLayout.EAST, lblThresholdAdd);
        panelLayout.putConstraint(SpringLayout.NORTH, lblThresholdRetire, 0, SpringLayout.NORTH, lblThresholdAdd);
        panelLayout.putConstraint(SpringLayout.WEST, lblThresholdRetire, 3, SpringLayout.EAST, txtThresholdAdd);
        panelLayout.putConstraint(SpringLayout.NORTH, txtThresholdRetire, -1, SpringLayout.NORTH, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.WEST, txtThresholdRetire, 0, SpringLayout.WEST, txtRansacIterations);

        // second row
        panelLayout.putConstraint(SpringLayout.NORTH, lblInlierPixelTol, 10, SpringLayout.SOUTH, lblThresholdAdd);
        panelLayout.putConstraint(SpringLayout.WEST, lblInlierPixelTol, 5, SpringLayout.WEST, this);
        panelLayout.putConstraint(SpringLayout.NORTH, txtInlierPixelTol, -1, SpringLayout.NORTH, lblInlierPixelTol);
        panelLayout.putConstraint(SpringLayout.WEST, txtInlierPixelTol, 0, SpringLayout.WEST, txtThresholdAdd);
        panelLayout.putConstraint(SpringLayout.NORTH, lblRansacIterations, 0, SpringLayout.NORTH, lblInlierPixelTol);
        panelLayout.putConstraint(SpringLayout.WEST, lblRansacIterations, 3, SpringLayout.EAST, txtInlierPixelTol);
        panelLayout.putConstraint(SpringLayout.NORTH, txtRansacIterations, -1, SpringLayout.NORTH, lblRansacIterations);
        panelLayout.putConstraint(SpringLayout.WEST, txtRansacIterations, 3, SpringLayout.EAST, lblRansacIterations);

        this.setLayout(panelLayout);
    }
}
