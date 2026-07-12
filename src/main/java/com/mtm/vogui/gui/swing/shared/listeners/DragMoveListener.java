/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.listeners;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

public class DragMoveListener extends MouseInputAdapter {
    private final Cursor dc;
    private final Cursor hc = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
    private final Rectangle rect = new Rectangle();
    private final JComponent comp;
    private final JViewport vport;
    private Point startPt = new Point();
    private Point move = new Point();

    public DragMoveListener(JViewport vport, JComponent comp) {
        this.vport = vport;
        this.comp = comp;
        this.dc = comp.getCursor();
        vport.addMouseMotionListener(this);
        vport.addMouseListener(this);
    }

    public void mouseDragged(MouseEvent e) {
        Point pt = e.getPoint();
        move.setLocation(pt.x - startPt.x, pt.y - startPt.y);
        startPt.setLocation(pt);
        Rectangle vr = vport.getViewRect();
        int w = vr.width;
        int h = vr.height;
        Point ptZero = SwingUtilities.convertPoint(vport, 0, 0, comp);
        rect.setRect(ptZero.x - move.x, ptZero.y - move.y, w, h);
        comp.scrollRectToVisible(rect);
    }

    public void mousePressed(MouseEvent e) {
        comp.setCursor(hc);
        startPt.setLocation(e.getPoint());
    }

    public void mouseReleased(MouseEvent e) {
        comp.setCursor(dc);
    }

    public void mouseClicked(MouseEvent e) {
        this.vport.getParent().dispatchEvent(e);
    }
}