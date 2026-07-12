/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.settings.visualodometry;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.combobox.DisplayValueComboBox;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.visualodometry.VisualOdometrySettings;
import com.mtm.vogui.models.enums.settings.VisualOdometryType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Visual odometry settings feature (humble view): owns the VO section of the control panel
 * (type selector) and orchestrates the per-type sub-views ({@link MonoPlaneInfinityView},
 * {@link MonoPlaneOverheadView}, {@link VoFallbackView}) swapped inside the scroll pane.
 * The type combo commits live into the settings; {@link #load()} refreshes it and delegates
 * to the sub-views.
 */
public class VoSettingsView {

    private final AppContext context;

    private final DisplayValueComboBox<VisualOdometryType> txtVisualOdometryType;
    private final MonoPlaneInfinityView monoPlaneInfinityView;
    private final MonoPlaneOverheadView monoPlaneOverheadView;
    private final VoFallbackView fallbackView;
    private final JScrollPane voScrollPane;

    private final JPanel panel;

    public VoSettingsView(@NotNull AppContext context) {
        this.context = context;
        VisualOdometrySettings voSettings = context.settings().visualOdometry();

        // Per-type sub-views
        this.monoPlaneInfinityView = new MonoPlaneInfinityView(context);
        this.monoPlaneOverheadView = new MonoPlaneOverheadView(context);
        this.fallbackView = new VoFallbackView();

        // Visual odometry container: borderless + transparent so the per-type params sit directly
        // under the combo (like the tracker section), not in a nested frame. Horizontal scroll only
        // when the content is too wide (the overhead type has many parameters); never vertical.
        this.voScrollPane = new JScrollPane();
        this.voScrollPane.setOpaque(false);
        this.voScrollPane.getViewport().setOpaque(false);
        this.voScrollPane.setBorder(BorderFactory.createEmptyBorder());
        this.voScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        this.voScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // Visual odometry type
        var lblVisualOdometryType = new JLabel("<html><b>VO Type:</b></html>");
        this.txtVisualOdometryType = new DisplayValueComboBox<>(
                VisualOdometryType.values(),
                voSettings::type,
                index -> VisualOdometryType.values()[index],
                selection -> this.switchVoPanel(selection.current().value())
        );
        this.txtVisualOdometryType.setSelectedItem(voSettings.type());

        // Visual odometry settings panel
        this.panel = new JPanel();
        this.panel.add(lblVisualOdometryType);
        this.panel.add(this.txtVisualOdometryType);
        this.panel.add(this.voScrollPane);

        this.panel.setOpaque(false);
        this.panel.setBorder(SwingUtils.getSettingsSectionBorder("Visual Odometry",
                GuiConstants.PANEL_BORDER_ACTIVE_COLOR));

        // layout
        SpringLayout panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblVisualOdometryType, 0, SpringLayout.NORTH, this.panel);
        panelLayout.putConstraint(SpringLayout.WEST, lblVisualOdometryType, 3, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtVisualOdometryType, -3, SpringLayout.NORTH, lblVisualOdometryType);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtVisualOdometryType, 3, SpringLayout.EAST, lblVisualOdometryType);
        panelLayout.putConstraint(SpringLayout.EAST, this.txtVisualOdometryType, -3, SpringLayout.EAST, this.panel);

        // second row: anchored below the label with the same gap as the Tracker section
        // (TrackerSettingsView), so the combo->content spacing matches across sections. The
        // per-type sub-views carry their own 8px top pad, so this clears the Aqua combo bezel.
        panelLayout.putConstraint(SpringLayout.NORTH, this.voScrollPane, 3, SpringLayout.SOUTH, lblVisualOdometryType);
        panelLayout.putConstraint(SpringLayout.WEST, this.voScrollPane, 0, SpringLayout.WEST, lblVisualOdometryType);
        panelLayout.putConstraint(SpringLayout.EAST, this.voScrollPane, 0, SpringLayout.EAST, this.panel);

        // panel height
        panelLayout.putConstraint(SpringLayout.SOUTH, this.panel, 0, SpringLayout.SOUTH, this.voScrollPane);

        this.panel.setLayout(panelLayout);

        // Shows current active vo panel
        this.switchVoPanel(voSettings.type());
    }

    /**
     * Section panel, consumed by the control panel composition only.
     */
    public JPanel panel() {
        return this.panel;
    }

    /**
     * Refreshes the type combo from the current settings (after a load/reset) and delegates
     * to the sub-views. Setting the type also swaps the visible per-type panel through the
     * combo selection callback.
     */
    public void load() {
        this.txtVisualOdometryType.setSelectedItem(this.context.settings().visualOdometry().type());
        this.monoPlaneInfinityView.load();
        this.monoPlaneOverheadView.load();
    }

    private void switchVoPanel(@NotNull VisualOdometryType selectedVoType) {
        switch (selectedVoType) {
            case VisualOdometryType.Default, VisualOdometryType.MonoPlaneInfinity -> {
                this.voScrollPane.setViewportView(this.monoPlaneInfinityView.panel());
                this.monoPlaneInfinityView.setEnabledAll(!VisualOdometryType.Default.is(selectedVoType));
            }
            case VisualOdometryType.MonoPlaneOverhead -> this.voScrollPane.setViewportView(this.monoPlaneOverheadView.panel());
            default -> this.voScrollPane.setViewportView(this.fallbackView.panel());
        }
    }
}
