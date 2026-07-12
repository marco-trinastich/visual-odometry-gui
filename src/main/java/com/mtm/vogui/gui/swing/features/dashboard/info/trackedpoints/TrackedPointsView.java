/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.dashboard.info.trackedpoints;

import com.mtm.vogui.gui.swing.constants.GuiConstants;
import com.mtm.vogui.gui.swing.shared.components.label.JBoldLabel;
import com.mtm.vogui.gui.swing.shared.listeners.RunnableOnListDataChangeListener;
import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;

/**
 * Tracked-points section (humble view): owns the points log list and its backing model, the
 * copy-on-select / copy-all-on-double-click behaviour and auto-scroll-to-end. The list never
 * leaves the view; consumers add/clear points and query the resolved selection through intents.
 * The cross-feature selection→chart navigation listener is registered by the facade.
 */
public class TrackedPointsView {

    private final JBoldLabel lblTrackedPoints;
    private final DefaultListModel<TrackedPoint> model;
    private final JList<TrackedPoint> list;
    private final JScrollPane scroll;

    private final JPanel panel;

    public TrackedPointsView() {
        this.lblTrackedPoints = new JBoldLabel(GuiConstants.LBL_TRACKED_POINTS);

        this.model = new DefaultListModel<>();
        this.model.addListDataListener(new RunnableOnListDataChangeListener(this::scrollListToEnd));

        this.list = new JList<>(this.model);
        this.list.setCellRenderer(new TrackedPointsListCellRenderer());
        this.list.addListSelectionListener(new TrackedPointsCopyOnSelection());
        this.list.addMouseListener(new TrackedPointsCopyAllOnDblClick(this.model));
        this.scroll = new JScrollPane(this.list);

        this.panel = new JPanel();
        this.panel.setOpaque(false);
        this.panel.add(this.lblTrackedPoints);
        this.panel.add(this.scroll);

        this.layout();
    }

    public JPanel panel() {
        return this.panel;
    }

    // Model mutation

    public void addPoint(TrackedPoint point) {
        this.model.addElement(point);
    }

    public void clear() {
        this.model.clear();
    }

    /**
     * Drops the points of the last (unclosed) segment: from its start marker to the end.
     */
    public void dropOpenSegment() {
        int lastSegmentStart = -1;
        for (int i = this.model.getSize() - 1; i >= 0; i--) {
            if (this.model.get(i).startPoint()) {
                lastSegmentStart = i;
                break;
            }
        }
        if (lastSegmentStart > -1) {
            this.model.removeRange(lastSegmentStart, this.model.getSize() - 1);
        }
    }

    // Selection

    public void addSelectionListener(ListSelectionListener listener) {
        this.list.addListSelectionListener(listener);
    }

    /**
     * The user-selected tracked point, skipping segment start/end markers (returns the adjacent
     * real point, or {@code null} when the selection resolves to a marker or is empty).
     */
    public TrackedPoint selectedPoint() {
        TrackedPoint selected = this.list.getSelectedValue();

        if (selected == null)
            return null;

        if (selected.startPoint() &&
                this.list.getSelectedIndex() < this.model.getSize() - 1) {
            // If start point, take next element
            selected = this.model.elementAt(this.list.getSelectedIndex() + 1);
        }

        if (selected.endPoint() && this.list.getSelectedIndex() > 0) {
            // If end point, take previous element
            selected = this.model.elementAt(this.list.getSelectedIndex() - 1);
        }

        // If selected is still a start/end point return null
        if (selected.startPoint() || selected.endPoint())
            return null;

        return selected;
    }

    private void scrollListToEnd() {
        JScrollBar vBar = this.scroll.getVerticalScrollBar();
        if (vBar != null)
            vBar.setValue(vBar.getMaximum());
    }

    private void layout() {
        SpringLayout layout = new SpringLayout();

        layout.putConstraint(SpringLayout.NORTH, lblTrackedPoints, 5, SpringLayout.NORTH, panel);
        layout.putConstraint(SpringLayout.WEST, lblTrackedPoints, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, lblTrackedPoints, -5, SpringLayout.EAST, panel);

        layout.putConstraint(SpringLayout.NORTH, scroll, 10, SpringLayout.SOUTH, lblTrackedPoints);
        layout.putConstraint(SpringLayout.WEST, scroll, 5, SpringLayout.WEST, panel);
        layout.putConstraint(SpringLayout.EAST, scroll, -5, SpringLayout.EAST, panel);
        layout.putConstraint(SpringLayout.SOUTH, scroll, -5, SpringLayout.SOUTH, panel);

        this.panel.setLayout(layout);
    }
}
