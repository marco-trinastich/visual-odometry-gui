/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.listeners.trackedpoints;

import com.mtm.vogui.models.core.processing.tracking.TrackedPoint;
import com.mtm.vogui.gui.swing.renderers.TrackedPointsListCellRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TrackedPointsCopyAllOnDblClick implements MouseListener {
    private final ListModel<TrackedPoint> lstPointsModel;

    public TrackedPointsCopyAllOnDblClick(ListModel<TrackedPoint> lstPointsModel) {
        this.lstPointsModel = lstPointsModel;
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        if (evt.getClickCount() == 2 && lstPointsModel != null && lstPointsModel.getSize() > 0) {
            StringBuilder output = new StringBuilder();
            for (int i = 0; i < lstPointsModel.getSize(); i++) {
                TrackedPoint value = lstPointsModel.getElementAt(i);
                output.append(TrackedPointsListCellRenderer.getRenderedText(value, false)).append("\n");
            }

            StringSelection stringSelection = new StringSelection(output.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                clipboard.setContents(stringSelection, null);
            } catch (Exception _) {
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent evt) {
    }

    @Override
    public void mouseExited(MouseEvent evt) {
    }

    @Override
    public void mousePressed(MouseEvent evt) {
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
    }
}
