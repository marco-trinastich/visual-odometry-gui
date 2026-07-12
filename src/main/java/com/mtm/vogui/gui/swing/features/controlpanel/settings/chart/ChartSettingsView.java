/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.settings.chart;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.features.dashboard.trajectory.TrajectoryView;
import com.mtm.vogui.gui.swing.shared.components.combobox.DisplayValueComboBox;
import com.mtm.vogui.gui.swing.shared.components.textfield.DoubleTextField;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.chart.ChartSettings;
import com.mtm.vogui.models.enums.core.NumberConstraints;
import com.mtm.vogui.models.enums.settings.ChartType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.Dimension;

/**
 * Chart settings feature (humble view): owns the Chart section of the control panel.
 * Widgets commit live into the persisted settings; the chart action buttons drive the output
 * trajectory charts through {@link TrajectoryView} intents; {@link #load()} refreshes everything
 * back after a load/reset.
 */
public class ChartSettingsView {

    private final AppContext context;
    private final TrajectoryView trajectory;

    private final DisplayValueComboBox<ChartType> txtChartType;
    private final DoubleTextField txtChartXZScale;
    private final JCheckBox chkChartXZ3DPoints;
    private final DoubleTextField txtChartYScale;

    private final JPanel panel;

    public ChartSettingsView(@NotNull AppContext context, @NotNull TrajectoryView trajectory) {
        this.context = context;
        this.trajectory = trajectory;
        ChartSettings chartSettings = context.settings().chart();
        ChartSettings defaultChartSettings = chartSettings.getDefault();

        /* Altitude basis (governs the Y chart abscissa: seconds vs frames) */
        JLabel lblChartType = new JLabel("<html>Altitude per:</html>");

        this.txtChartType = new DisplayValueComboBox<>(
                ChartType.values(),
                chartSettings::type,
                index -> ChartType.values()[index]
        );
        this.txtChartType.setSelectedItem(chartSettings.type());
        // Narrow, fixed size so the combo stays on its label's row: capping the MAX height is what
        // matters - otherwise SpringLayout stretches the combo vertically (the section panel's SOUTH
        // is tied to it and the parent stretches the section to fill the settings column).
        var comboSize = new Dimension(110, this.txtChartType.getPreferredSize().height);
        this.txtChartType.setPreferredSize(comboSize);
        this.txtChartType.setMaximumSize(comboSize);

        /* Chart X/Z */
        var lblChartXZ = new JLabel("<html><b>Chart X/Z</b></html>");

        var lblChartXZScale = new JLabel("<html>Scale: </html>");
        this.txtChartXZScale = new DoubleTextField(
                NumberConstraints.NotZero,
                chartSettings::scaleXZ,
                chartSettings::scaleXZ,
                defaultChartSettings.scaleXZ(),
                5,
                JTextField.CENTER
        );

        // Applying loaded Chart X/Z Scale
        this.trajectory.applyXZScale(chartSettings.scaleXZ());

        var btnChartXZApplyScale = new JButton("Apply");
        btnChartXZApplyScale.addActionListener(_ -> this.applyXZScale());

        var btnChartXZMoveToOrigin = new JButton("Origin");
        btnChartXZMoveToOrigin.addActionListener(_ -> this.trajectory.moveXZToOrigin());

        var btnChartXZMoveToLastPoint = new JButton("Last");
        btnChartXZMoveToLastPoint.addActionListener(_ -> this.trajectory.moveXZToLast());

        this.chkChartXZ3DPoints = new JCheckBox("<html>3D points</html>");
        this.chkChartXZ3DPoints.addActionListener(_ -> this.on3DPointsToggled());

        /* Chart Y */
        var lblChartY = new JLabel("<html><b>Chart Y</b></html>");

        var lblChartYScale = new JLabel("<html>Scale: </html>");
        this.txtChartYScale = new DoubleTextField(
                NumberConstraints.NotZero,
                chartSettings::scaleY,
                chartSettings::scaleY,
                defaultChartSettings.scaleY(),
                5,
                JTextField.CENTER
        );

        // Applying loaded Chart Y Scale
        this.trajectory.applyYScale(chartSettings.scaleY());

        var btnChartYApplyScale = new JButton("Apply");
        btnChartYApplyScale.addActionListener(_ -> this.applyYScale());

        var btnChartYMoveToOrigin = new JButton("Origin");
        btnChartYMoveToOrigin.addActionListener(_ -> this.trajectory.moveYToOrigin());

        var btnChartYMoveToLastPoint = new JButton("Last");
        btnChartYMoveToLastPoint.addActionListener(_ -> this.trajectory.moveYToLast());

        /* Panel creation */
        this.panel = new JPanel();
        this.panel.setOpaque(false);
        this.panel.setBorder(SwingUtils.getSettingsSectionBorder("Chart",
                GuiConstants.PANEL_BORDER_ACTIVE_COLOR));

        this.panel.add(lblChartType);
        this.panel.add(this.txtChartType);
        this.panel.add(lblChartXZ);
        this.panel.add(lblChartXZScale);
        this.panel.add(this.txtChartXZScale);
        this.panel.add(btnChartXZApplyScale);
        this.panel.add(btnChartXZMoveToOrigin);
        this.panel.add(btnChartXZMoveToLastPoint);
        this.panel.add(this.chkChartXZ3DPoints);
        this.panel.add(lblChartY);
        this.panel.add(lblChartYScale);
        this.panel.add(this.txtChartYScale);
        this.panel.add(btnChartYApplyScale);
        this.panel.add(btnChartYMoveToOrigin);
        this.panel.add(btnChartYMoveToLastPoint);

        /* Panel disposition */
        SpringLayout panelLayout = new SpringLayout();

        //On the first row Chart X/Z Label
        panelLayout.putConstraint(SpringLayout.NORTH, lblChartXZ, 0, SpringLayout.NORTH, this.panel);
        panelLayout.putConstraint(SpringLayout.WEST, lblChartXZ, 3, SpringLayout.WEST, this.panel);

        //On the second row Chart XZ Scale Label/TextField, Apply Scale Button, Move to Origin Button,
        //Move to Last Point Button, and 3D Points CheckBox
        panelLayout.putConstraint(SpringLayout.NORTH, lblChartXZScale, 7, SpringLayout.SOUTH, lblChartXZ);
        panelLayout.putConstraint(SpringLayout.WEST, lblChartXZScale, 5, SpringLayout.WEST, lblChartXZ);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtChartXZScale, -3, SpringLayout.NORTH, lblChartXZScale);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtChartXZScale, 3, SpringLayout.EAST, lblChartXZScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartXZApplyScale, -1, SpringLayout.NORTH, this.txtChartXZScale);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartXZApplyScale, 3, SpringLayout.EAST, this.txtChartXZScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartXZMoveToOrigin, 0, SpringLayout.NORTH, btnChartXZApplyScale);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartXZMoveToOrigin, 3, SpringLayout.EAST, btnChartXZApplyScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartXZMoveToLastPoint, 0, SpringLayout.NORTH, btnChartXZMoveToOrigin);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartXZMoveToLastPoint, 3, SpringLayout.EAST, btnChartXZMoveToOrigin);
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkChartXZ3DPoints, 0, SpringLayout.NORTH, btnChartXZMoveToLastPoint);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkChartXZ3DPoints, 3, SpringLayout.EAST, btnChartXZMoveToLastPoint);

        //On the third row Chart Y Label
        panelLayout.putConstraint(SpringLayout.NORTH, lblChartY, 8, SpringLayout.SOUTH, lblChartXZScale);
        panelLayout.putConstraint(SpringLayout.WEST, lblChartY, 0, SpringLayout.WEST, lblChartXZ);

        //On the fourth row Chart Y Scale Label/TextField, Apply Scale Button, Move to Origin Button
        //and Move to Last Point Button (symmetric to Chart X/Z)
        panelLayout.putConstraint(SpringLayout.NORTH, lblChartYScale, 7, SpringLayout.SOUTH, lblChartY);
        panelLayout.putConstraint(SpringLayout.WEST, lblChartYScale, 5, SpringLayout.WEST, lblChartY);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtChartYScale, -3, SpringLayout.NORTH, lblChartYScale);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtChartYScale, 3, SpringLayout.EAST, lblChartYScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartYApplyScale, -1, SpringLayout.NORTH, this.txtChartYScale);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartYApplyScale, 3, SpringLayout.EAST, this.txtChartYScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartYMoveToOrigin, 0, SpringLayout.NORTH, btnChartYApplyScale);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartYMoveToOrigin, 3, SpringLayout.EAST, btnChartYApplyScale);
        panelLayout.putConstraint(SpringLayout.NORTH, btnChartYMoveToLastPoint, 0, SpringLayout.NORTH, btnChartYMoveToOrigin);
        panelLayout.putConstraint(SpringLayout.WEST, btnChartYMoveToLastPoint, 3, SpringLayout.EAST, btnChartYMoveToOrigin);

        //On the fifth row the Altitude-by selector (governs the Y chart abscissa); narrow combo.
        //Anchored below the scale-row buttons (the row's lowest element) for a clean gap.
        panelLayout.putConstraint(SpringLayout.NORTH, lblChartType, 4, SpringLayout.SOUTH, btnChartYApplyScale);
        panelLayout.putConstraint(SpringLayout.WEST, lblChartType, 5, SpringLayout.WEST, lblChartY);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtChartType, -3, SpringLayout.NORTH, lblChartType);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtChartType, 3, SpringLayout.EAST, lblChartType);

        //The height of the panel is constrained to the last row bottom
        panelLayout.putConstraint(SpringLayout.SOUTH, this.panel, 0, SpringLayout.SOUTH, this.txtChartType);

        this.panel.setLayout(panelLayout);
    }

    /**
     * Section panel, consumed by the control panel composition only.
     */
    public JPanel panel() {
        return this.panel;
    }

    /**
     * Refreshes every widget from the current settings (after a load/reset)
     * and re-applies the scales to the chart panels.
     */
    public void load() {
        var chartSettings = this.context.settings().chart();

        this.txtChartType.setSelectedItem(chartSettings.type());

        this.txtChartXZScale.updateModel(chartSettings.scaleXZ());
        this.applyXZScale();

        this.txtChartYScale.updateModel(chartSettings.scaleY());
        this.applyYScale();
    }

    // Action handlers

    private void applyXZScale() {
        this.trajectory.applyXZScale(this.context.settings().chart().scaleXZ());
    }

    private void applyYScale() {
        this.trajectory.applyYScale(this.context.settings().chart().scaleY());
    }

    private void on3DPointsToggled() {
        SwingUtils.setBoldToggleText(this.chkChartXZ3DPoints, "3D Points");
        this.trajectory.showXZ3DPoints(this.chkChartXZ3DPoints.isSelected());
    }
}
