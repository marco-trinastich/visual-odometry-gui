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
 * Mono-plane-infinity VO parameters (humble view): owns its own widgets as private fields and
 * exposes intents only. Sub-view of {@link VoSettingsView}, swapped into the VO scroll pane.
 * Widgets commit live into the settings; {@link #load()} refreshes them back after a load/reset;
 * {@link #setEnabledAll(boolean)} greys the whole section out when the VO type is {@code Default}.
 */
public class MonoPlaneInfinityView {

    private final AppContext context;

    private final IntegerTextField txtThresholdAdd;
    private final IntegerTextField txtThresholdRetire;
    private final DoubleTextField txtInlierPixelTol;
    private final IntegerTextField txtRansacIterations;

    private final JPanel panel;

    public MonoPlaneInfinityView(@NotNull AppContext context) {
        this.context = context;
        var monoVoSettings = context.settings().visualOdometry().monoPlaneInfinity();
        var defaultMonoVoSettings = monoVoSettings.getDefault();

        // thresholdAdd
        var lblThresholdAdd = new JLabel(GuiConstants.LBL_VO_MONO_INF_THRESHOLD_ADD);
        this.txtThresholdAdd = new IntegerTextField(
                monoVoSettings::thresholdAdd,
                monoVoSettings::thresholdAdd,
                defaultMonoVoSettings.thresholdAdd(),
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

        // inlierPixelTol
        var lblInlierPixelTol = new JLabel(GuiConstants.LBL_VO_MONO_INF_INLIER_PIXEL_TOL);
        lblInlierPixelTol.setToolTipText(GuiConstants.TIP_VO_MONO_INF_INLIER_PIXEL_TOL);
        this.txtInlierPixelTol = new DoubleTextField(
                monoVoSettings::inlierPixelTol,
                monoVoSettings::inlierPixelTol,
                defaultMonoVoSettings.inlierPixelTol(),
                5,
                JTextField.CENTER
        );
        this.txtInlierPixelTol.setToolTipText(GuiConstants.TIP_VO_MONO_INF_INLIER_PIXEL_TOL);

        // ransacIterations
        var lblRansacIterations = new JLabel(GuiConstants.LBL_VO_MONO_RANSAC_ITERATIONS);
        this.txtRansacIterations = new IntegerTextField(
                monoVoSettings::ransacIterations,
                monoVoSettings::ransacIterations,
                defaultMonoVoSettings.ransacIterations(),
                5,
                JTextField.CENTER
        );

        // panel
        this.panel = new JPanel();
        this.panel.add(lblThresholdAdd);
        this.panel.add(this.txtThresholdAdd);
        this.panel.add(lblThresholdRetire);
        this.panel.add(this.txtThresholdRetire);
        this.panel.add(lblInlierPixelTol);
        this.panel.add(this.txtInlierPixelTol);
        this.panel.add(lblRansacIterations);
        this.panel.add(this.txtRansacIterations);

        // layout
        SpringLayout panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblThresholdAdd, 8, SpringLayout.NORTH, this.panel);
        panelLayout.putConstraint(SpringLayout.WEST, lblThresholdAdd, 5, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtThresholdAdd, -1, SpringLayout.NORTH, lblThresholdAdd);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtThresholdAdd, 3, SpringLayout.EAST, lblThresholdAdd);
        panelLayout.putConstraint(SpringLayout.NORTH, lblThresholdRetire, 0, SpringLayout.NORTH, lblThresholdAdd);
        panelLayout.putConstraint(SpringLayout.WEST, lblThresholdRetire, 3, SpringLayout.EAST, this.txtThresholdAdd);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtThresholdRetire, -1, SpringLayout.NORTH, lblThresholdRetire);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtThresholdRetire, 0, SpringLayout.WEST, this.txtRansacIterations);

        // second row
        panelLayout.putConstraint(SpringLayout.NORTH, lblInlierPixelTol, 10, SpringLayout.SOUTH, lblThresholdAdd);
        panelLayout.putConstraint(SpringLayout.WEST, lblInlierPixelTol, 5, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtInlierPixelTol, -1, SpringLayout.NORTH, lblInlierPixelTol);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtInlierPixelTol, 0, SpringLayout.WEST, this.txtThresholdAdd);
        panelLayout.putConstraint(SpringLayout.NORTH, lblRansacIterations, 0, SpringLayout.NORTH, lblInlierPixelTol);
        panelLayout.putConstraint(SpringLayout.WEST, lblRansacIterations, 3, SpringLayout.EAST, this.txtInlierPixelTol);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtRansacIterations, -1, SpringLayout.NORTH, lblRansacIterations);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtRansacIterations, 3, SpringLayout.EAST, lblRansacIterations);

        this.panel.setLayout(panelLayout);
        // Shared VO content height (see the constant): keeps the VO section a constant height across
        // types even though only MonoPlaneOverhead actually needs the reserved scrollbar band.
        this.panel.setPreferredSize(new Dimension(this.panel.getPreferredSize().width, GuiConstants.VO_SETTINGS_CONTENT_HEIGHT));
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
        var monoVoSettings = this.context.settings().visualOdometry().monoPlaneInfinity();
        this.txtThresholdAdd.updateModel(monoVoSettings.thresholdAdd());
        this.txtThresholdRetire.updateModel(monoVoSettings.thresholdRetire());
        this.txtInlierPixelTol.updateModel(monoVoSettings.inlierPixelTol());
        this.txtRansacIterations.updateModel(monoVoSettings.ransacIterations());
    }

    /**
     * Enables/disables the whole section (VO type {@code Default} greys it out).
     */
    public void setEnabledAll(boolean enabled) {
        this.panel.setEnabled(enabled);
        for (Component component : this.panel.getComponents()) {
            component.setEnabled(enabled);
        }
    }
}
