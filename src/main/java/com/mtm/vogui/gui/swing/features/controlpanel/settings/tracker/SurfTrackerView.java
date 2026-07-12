/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.settings.tracker;

import com.mtm.vogui.gui.swing.shared.components.textfield.IntegerTextField;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.tracker.TrackerSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * SURF tracker parameters (humble view): owns its own widgets as private fields and exposes
 * intents only. Sub-view of {@link TrackerSettingsView}. Widgets commit live into the settings;
 * {@link #load()} refreshes them back after a load/reset.
 */
public class SurfTrackerView {

    private final AppContext context;

    private final IntegerTextField txtMaxFeaturesPerScale;
    private final IntegerTextField txtExtractRadius;
    private final IntegerTextField txtInitialSampleSize;

    private final JPanel panel;

    public SurfTrackerView(@NotNull AppContext context) {
        this.context = context;
        TrackerSettings trackerSettings = context.settings().tracker();
        TrackerSettings defaultTrackerSettings = trackerSettings.getDefault();

        // maxFeaturesPerScale
        var lblMaxFeaturesPerScale = new JLabel("<html>Max Features Per Scale:</html>");
        this.txtMaxFeaturesPerScale = new IntegerTextField(
                trackerSettings.surf()::maxFeaturesPerScale,
                trackerSettings.surf()::maxFeaturesPerScale,
                defaultTrackerSettings.surf().maxFeaturesPerScale(),
                5,
                JTextField.CENTER
        );

        // extractRadius
        var lblExtractRadius = new JLabel("<html>Extract Radius:</html>");
        this.txtExtractRadius = new IntegerTextField(
                trackerSettings.surf()::extractRadius,
                trackerSettings.surf()::extractRadius,
                defaultTrackerSettings.surf().extractRadius(),
                5,
                JTextField.CENTER
        );

        // initialSampleSize
        var lblInitialSampleSize = new JLabel("<html>Initial Sample Size:</html>");
        this.txtInitialSampleSize = new IntegerTextField(
                trackerSettings.surf()::initialSampleSize,
                trackerSettings.surf()::initialSampleSize,
                defaultTrackerSettings.surf().initialSampleSize(),
                5,
                JTextField.CENTER
        );

        // panel
        this.panel = new JPanel();
        this.panel.add(lblMaxFeaturesPerScale);
        this.panel.add(this.txtMaxFeaturesPerScale);
        this.panel.add(lblExtractRadius);
        this.panel.add(this.txtExtractRadius);
        this.panel.add(lblInitialSampleSize);
        this.panel.add(this.txtInitialSampleSize);

        this.panel.setOpaque(false);

        // layout
        SpringLayout panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblMaxFeaturesPerScale, 5, SpringLayout.NORTH, this.panel);
        panelLayout.putConstraint(SpringLayout.WEST, lblMaxFeaturesPerScale, 0, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtMaxFeaturesPerScale, -1, SpringLayout.NORTH, lblMaxFeaturesPerScale);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtMaxFeaturesPerScale, 3, SpringLayout.EAST, lblMaxFeaturesPerScale);
        panelLayout.putConstraint(SpringLayout.NORTH, lblExtractRadius, 0, SpringLayout.NORTH, lblMaxFeaturesPerScale);
        panelLayout.putConstraint(SpringLayout.WEST, lblExtractRadius, 3, SpringLayout.EAST, this.txtMaxFeaturesPerScale);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtExtractRadius, -1, SpringLayout.NORTH, lblExtractRadius);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtExtractRadius, 3, SpringLayout.EAST, lblExtractRadius);

        // second row
        panelLayout.putConstraint(SpringLayout.NORTH, lblInitialSampleSize, 10, SpringLayout.SOUTH, lblMaxFeaturesPerScale);
        panelLayout.putConstraint(SpringLayout.WEST, lblInitialSampleSize, 0, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtInitialSampleSize, -1, SpringLayout.NORTH, lblInitialSampleSize);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtInitialSampleSize, 3, SpringLayout.EAST, lblInitialSampleSize);

        panelLayout.putConstraint(SpringLayout.SOUTH, this.panel, 0, SpringLayout.SOUTH, this.txtInitialSampleSize);

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
        var surf = this.context.settings().tracker().surf();
        this.txtMaxFeaturesPerScale.updateModel(surf.maxFeaturesPerScale());
        this.txtExtractRadius.updateModel(surf.extractRadius());
        this.txtInitialSampleSize.updateModel(surf.initialSampleSize());
    }
}
