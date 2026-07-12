/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.settings.tracker;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.combobox.DisplayValueComboBox;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import com.mtm.vogui.models.context.AppContext;
import com.mtm.vogui.models.context.settings.tracker.TrackerSettings;
import com.mtm.vogui.models.enums.settings.TrackerType;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Tracker settings feature (humble view): owns the Tracker section of the control panel
 * (type selector + track display options) and orchestrates the per-type sub-views
 * ({@link KltTrackerView}, {@link SurfTrackerView}). The combo/checkboxes commit live into
 * the settings; {@link #load()} refreshes them and delegates to the sub-views;
 * {@link #showKltPyramidLevels(int)} forwards the core's heal-to-default to the KLT view.
 */
public class TrackerSettingsView {

    private final AppContext context;

    private final DisplayValueComboBox<TrackerType> txtTrackerType;
    private final KltTrackerView kltTrackerView;
    private final SurfTrackerView surfTrackerView;
    private final JCheckBox chkTrackerShowActiveTracks;
    private final JCheckBox chkTrackerShowNewTracks;

    private final JPanel panel;

    public TrackerSettingsView(@NotNull AppContext context) {
        this.context = context;
        TrackerSettings trackerSettings = context.settings().tracker();

        // Per-type sub-views
        this.kltTrackerView = new KltTrackerView(context);
        this.surfTrackerView = new SurfTrackerView(context);
        JPanel kltTrackerPanel = this.kltTrackerView.panel();
        JPanel surfTrackerPanel = this.surfTrackerView.panel();

        // Show active Tracks
        this.chkTrackerShowActiveTracks = new JCheckBox();
        this.chkTrackerShowActiveTracks.setSelected(trackerSettings.showActiveTracks());
        SwingUtils.setBoldToggleText(this.chkTrackerShowActiveTracks, "Show Active Tracks");
        this.chkTrackerShowActiveTracks.addActionListener(_ -> this.onShowActiveTracksToggled());

        // Show new tracks
        this.chkTrackerShowNewTracks = new JCheckBox();
        this.chkTrackerShowNewTracks.setSelected(trackerSettings.showNewTracks());
        SwingUtils.setBoldToggleText(this.chkTrackerShowNewTracks, "Show New Tracks");
        this.chkTrackerShowNewTracks.addActionListener(_ -> this.onShowNewTracksToggled());

        // Tracker type
        var lblTrackerType = new JLabel("<html><b>Tracker Type:</b></html>");
        this.txtTrackerType = new DisplayValueComboBox<>(
                TrackerType.values(),
                trackerSettings::type,
                index -> TrackerType.values()[index],
                selection -> this.switchTrackerPanel(selection.current().value())
        );
        this.txtTrackerType.setSelectedItem(trackerSettings.type());

        // Tracker settings panel
        this.panel = new JPanel();
        this.panel.add(lblTrackerType);
        this.panel.add(this.txtTrackerType);
        this.panel.add(kltTrackerPanel);
        this.panel.add(surfTrackerPanel);
        this.panel.add(this.chkTrackerShowActiveTracks);
        this.panel.add(this.chkTrackerShowNewTracks);

        this.panel.setOpaque(false);
        this.panel.setBorder(SwingUtils.getSettingsSectionBorder("Tracker",
                GuiConstants.PANEL_BORDER_ACTIVE_COLOR));

        SpringLayout panelLayout = new SpringLayout();

        // first row
        panelLayout.putConstraint(SpringLayout.NORTH, lblTrackerType, 0, SpringLayout.NORTH, this.panel);
        panelLayout.putConstraint(SpringLayout.WEST, lblTrackerType, 3, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.NORTH, this.txtTrackerType, -3, SpringLayout.NORTH, lblTrackerType);
        panelLayout.putConstraint(SpringLayout.WEST, this.txtTrackerType, 3, SpringLayout.EAST, lblTrackerType);
        panelLayout.putConstraint(SpringLayout.EAST, this.txtTrackerType, -3, SpringLayout.EAST, this.panel);

        // second row (klt tracker panel)
        panelLayout.putConstraint(SpringLayout.NORTH, kltTrackerPanel, 5, SpringLayout.SOUTH, lblTrackerType);
        panelLayout.putConstraint(SpringLayout.WEST, kltTrackerPanel, 3, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.EAST, kltTrackerPanel, 0, SpringLayout.EAST, this.panel);

        // second row (surf tracker panel)
        panelLayout.putConstraint(SpringLayout.NORTH, surfTrackerPanel, 5, SpringLayout.SOUTH, lblTrackerType);
        panelLayout.putConstraint(SpringLayout.WEST, surfTrackerPanel, 3, SpringLayout.WEST, this.panel);
        panelLayout.putConstraint(SpringLayout.EAST, surfTrackerPanel, 0, SpringLayout.EAST, this.panel);

        // third row
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkTrackerShowActiveTracks, 2, SpringLayout.SOUTH, kltTrackerPanel);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkTrackerShowActiveTracks, 0, SpringLayout.WEST, lblTrackerType);
        panelLayout.putConstraint(SpringLayout.NORTH, this.chkTrackerShowNewTracks, 0, SpringLayout.NORTH, this.chkTrackerShowActiveTracks);
        panelLayout.putConstraint(SpringLayout.WEST, this.chkTrackerShowNewTracks, 3, SpringLayout.EAST, this.chkTrackerShowActiveTracks);

        panelLayout.putConstraint(SpringLayout.SOUTH, this.panel, 0, SpringLayout.SOUTH, this.chkTrackerShowActiveTracks);

        this.panel.setLayout(panelLayout);

        this.switchTrackerPanel(trackerSettings.type());
    }

    /**
     * Section panel, consumed by the control panel composition only.
     */
    public JPanel panel() {
        return this.panel;
    }

    /**
     * Refreshes the type combo and track options from the current settings (after a load/reset)
     * and delegates to the sub-views. Setting the type also swaps the visible per-type panel
     * through the combo selection callback.
     */
    public void load() {
        var trackerSettings = this.context.settings().tracker();

        this.txtTrackerType.setSelectedItem(trackerSettings.type());

        this.kltTrackerView.load();
        this.surfTrackerView.load();

        // Tracker options
        this.chkTrackerShowActiveTracks.setSelected(trackerSettings.showActiveTracks());
        this.onShowActiveTracksToggled();
        this.chkTrackerShowNewTracks.setSelected(trackerSettings.showNewTracks());
        this.onShowNewTracksToggled();
    }

    /**
     * Reflects the KLT pyramid levels healed by the core (safe from any thread).
     */
    public void showKltPyramidLevels(int pyramidLevels) {
        this.kltTrackerView.showPyramidLevels(pyramidLevels);
    }

    // Handlers

    private void onShowActiveTracksToggled() {
        this.context.settings().tracker().showActiveTracks(this.chkTrackerShowActiveTracks.isSelected());
        SwingUtils.setBoldToggleText(this.chkTrackerShowActiveTracks, "Show Active Tracks");
    }

    private void onShowNewTracksToggled() {
        this.context.settings().tracker().showNewTracks(this.chkTrackerShowNewTracks.isSelected());
        SwingUtils.setBoldToggleText(this.chkTrackerShowNewTracks, "Show New Tracks");
    }

    private void switchTrackerPanel(@NotNull TrackerType selectedTrackerType) {
        // For KLT-based trackers (KLT, KLT-Modern, Default) shows the KLT panel, disabled when Default
        this.kltTrackerView.panel().setVisible(selectedTrackerType.isKlt());
        this.kltTrackerView.setEnabledAll(!TrackerType.Default.is(selectedTrackerType));

        // For SURF-based trackers shows the SURF panel
        this.surfTrackerView.panel().setVisible(selectedTrackerType.isSurf());
    }
}
