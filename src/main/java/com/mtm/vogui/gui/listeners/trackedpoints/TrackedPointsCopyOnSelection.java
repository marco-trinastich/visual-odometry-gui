/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.listeners.trackedpoints;

import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.gui.renderers.TrackedPointsListCellRenderer;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

public class TrackedPointsCopyOnSelection implements ListSelectionListener {
    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (evt.getValueIsAdjusting())
            return;
        if (evt.getSource() instanceof JList<?> lstPoints) {
            TrackedPoint selectedValue = (TrackedPoint) lstPoints.getSelectedValue();

            if (selectedValue == null)
                return;

            StringSelection stringSelection =
                    new StringSelection(TrackedPointsListCellRenderer.getRenderedText(selectedValue, false));
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                clipboard.setContents(stringSelection, null);
            } catch (Exception _) {
            }
        }
    }
}
