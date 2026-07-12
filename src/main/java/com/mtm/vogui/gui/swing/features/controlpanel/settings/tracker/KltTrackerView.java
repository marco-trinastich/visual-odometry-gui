/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.settings.tracker;

import com.mtm.vogui.gui.swing.shared.components.textfield.FloatTextField;
import com.mtm.vogui.gui.swing.shared.components.textfield.IntegerTextField;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.tracker.TrackerSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * KLT tracker parameters (humble view): owns its own widgets as private fields and exposes
 * intents only. Sub-view of {@link TrackerSettingsView}. Widgets commit live into the settings;
 * {@link #load()} refreshes them back after a load/reset; {@link #setEnabledAll(boolean)} greys
 * the whole section out when the tracker type is {@code Default}; {@link #showPyramidLevels(int)}
 * reflects the core's heal-to-default.
 */
public class KltTrackerView {

    private final AppContext context;

    private final IntegerTextField txtTemplateRadius;
    private final IntegerTextField txtPyramidLevels;
    private final IntegerTextField txtMaxFeatures;
    private final IntegerTextField txtRadius;
    private final FloatTextField txtThreshold;

    private final JPanel panel;

    public KltTrackerView(@NotNull AppContext context) {
        this.context = context;
        TrackerSettings trackerSettings = context.settings().tracker();
        TrackerSettings defaultTrackerSettings = trackerSettings.getDefault();

        // templateRadius
        var lblTemplateRadius = new JLabel("<html>Template Radius:</html>");
        this.txtTemplateRadius = new IntegerTextField(
                trackerSettings.klt()::templateRadius,
                trackerSettings.klt()::templateRadius,
                defaultTrackerSettings.klt().templateRadius(),
                5,
                JTextField.CENTER
        );

        // pyramidLevels
        var lblPyramidLevels = new JLabel("<html>Pyramid Levels:</html>");
        this.txtPyramidLevels = new IntegerTextField(
                trackerSettings.klt()::pyramidLevels,
                trackerSettings.klt()::pyramidLevels,
                defaultTrackerSettings.klt().pyramidLevels(),
                5,
                JTextField.CENTER
        );

        // maxFeatures
        var lblMaxFeatures = new JLabel("<html>Max Features:</html>");
        this.txtMaxFeatures = new IntegerTextField(
                trackerSettings.klt()::maxFeatures,
                trackerSettings.klt()::maxFeatures,
                defaultTrackerSettings.klt().maxFeatures(),
                5,
                JTextField.CENTER
        );

        // radius
        var lblRadius = new JLabel("<html>Radius:</html>");
        this.txtRadius = new IntegerTextField(
                trackerSettings.klt()::radius,
                trackerSettings.klt()::radius,
                defaultTrackerSettings.klt().radius(),
                5,
                JTextField.CENTER
        );

        // threshold
        var lblThreshold = new JLabel("<html>Threshold:</html>");
        this.txtThreshold = new FloatTextField(
                trackerSettings.klt()::threshold,
                trackerSettings.klt()::threshold,
                defaultTrackerSettings.klt().threshold(),
                5,
                JTextField.CENTER
        );

        // panel
        this.panel = new JPanel();
        this.panel.add(lblTemplateRadius);
        this.panel.add(this.txtTemplateRadius);
        this.panel.add(lblPyramidLevels);
        this.panel.add(this.txtPyramidLevels);
        this.panel.add(lblMaxFeatures);
        this.panel.add(this.txtMaxFeatures);
        this.panel.add(lblRadius);
        this.panel.add(this.txtRadius);
        this.panel.add(lblThreshold);
        this.panel.add(this.txtThreshold);

        this.panel.setOpaque(false);

        // layout
        SpringLayout panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblTemplateRadius, 5, SpringLayout.NORTH, this.panel);
        panelLayout.putConstraint(SpringLayout.WEST, lblTemplateRadius, 0, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtTemplateRadius, -1, SpringLayout.NORTH, lblTemplateRadius);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtTemplateRadius, 3, SpringLayout.EAST, lblTemplateRadius);
        panelLayout.putConstraint(SpringLayout.NORTH, lblPyramidLevels, 0, SpringLayout.NORTH, lblTemplateRadius);
        panelLayout.putConstraint(SpringLayout.WEST, lblPyramidLevels, 3, SpringLayout.EAST, this.txtTemplateRadius);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtPyramidLevels, -1, SpringLayout.NORTH, lblPyramidLevels);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtPyramidLevels, 3, SpringLayout.EAST, lblPyramidLevels);

        // second row
        panelLayout.putConstraint(SpringLayout.NORTH, lblMaxFeatures, 10, SpringLayout.SOUTH, lblTemplateRadius);
        panelLayout.putConstraint(SpringLayout.WEST, lblMaxFeatures, 0, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtMaxFeatures, -1, SpringLayout.NORTH, lblMaxFeatures);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtMaxFeatures, 0, SpringLayout.WEST, this.txtTemplateRadius);
        panelLayout.putConstraint(SpringLayout.NORTH, lblRadius, 0, SpringLayout.NORTH, lblMaxFeatures);
        panelLayout.putConstraint(SpringLayout.WEST, lblRadius, 3, SpringLayout.EAST, this.txtMaxFeatures);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtRadius, -1, SpringLayout.NORTH, lblRadius);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtRadius, 3, SpringLayout.EAST, lblRadius);
        panelLayout.putConstraint(SpringLayout.NORTH, lblThreshold, 0, SpringLayout.NORTH, lblRadius);
        panelLayout.putConstraint(SpringLayout.WEST, lblThreshold, 3, SpringLayout.EAST, this.txtRadius);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtThreshold, -1, SpringLayout.NORTH, lblThreshold);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtThreshold, 3, SpringLayout.EAST, lblThreshold);

        panelLayout.putConstraint(SpringLayout.SOUTH, this.panel, 0, SpringLayout.SOUTH, this.txtMaxFeatures);

        this.panel.setLayout(panelLayout);
    }

    /**
     * Section panel, consumed by {@link TrackerSettingsView} composition only.
     */
    public JPanel panel() {
        return this.panel;
    }

    /**
     * Refreshes every widget from the current settings (after a load/reset).
     */
    public void load() {
        var klt = this.context.settings().tracker().klt();
        this.txtTemplateRadius.updateModel(klt.templateRadius());
        this.txtPyramidLevels.updateModel(klt.pyramidLevels());
        this.txtMaxFeatures.updateModel(klt.maxFeatures());
        this.txtRadius.updateModel(klt.radius());
        this.txtThreshold.updateModel(klt.threshold());
    }

    /**
     * Enables/disables the whole section (tracker type {@code Default} greys it out).
     */
    public void setEnabledAll(boolean enabled) {
        this.panel.setEnabled(enabled);
        for (Component component : this.panel.getComponents()) {
            component.setEnabled(enabled);
        }
    }

    /**
     * Reflects the KLT pyramid levels healed by the core (safe from any thread).
     */
    public void showPyramidLevels(int pyramidLevels) {
        SwingUtilities.invokeLater(() ->
                this.txtPyramidLevels.setText(String.valueOf(pyramidLevels)));
    }
}
