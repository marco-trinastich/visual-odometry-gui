/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.listeners;


import georegression.struct.point.Point2D_F64;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.event.MouseEvent;

public class DragMoveScrollBarsListener extends MouseInputAdapter {

    private Point2D_F64 start_point = new Point2D_F64(0, 0);
    private Point2D_F64 start_bars_values = new Point2D_F64(0, 0);
    private JScrollBar hbar;
    private JScrollBar vbar;

    public DragMoveScrollBarsListener(JScrollPane scrollpane) {
        this.hbar = scrollpane.getHorizontalScrollBar();
        this.vbar = scrollpane.getVerticalScrollBar();
        scrollpane.addMouseListener(this);
    }

    @Override
    public void mouseReleased(MouseEvent evt) {
    }

    @Override
    public void mousePressed(MouseEvent evt) {
        start_point.setTo(evt.getX(), evt.getY());
        start_bars_values.setX(hbar.getValue());
        start_bars_values.setY(vbar.getValue());
    }

    @Override
    public void mouseMoved(MouseEvent evt) {

    }

    @Override
    public void mouseDragged(MouseEvent evt) {

        int delta_x = -(int) Math.round((evt.getX() - start_point.getX()));
        int delta_y = -(int) Math.round((evt.getY() - start_point.getY()));


        hbar.setValue((int) start_bars_values.getX() + delta_x);
        vbar.setValue((int) start_bars_values.getY() + delta_y);

        start_point.setTo(evt.getX(), evt.getY());
        start_bars_values.setX(hbar.getValue());
        start_bars_values.setY(vbar.getValue());
    }


}