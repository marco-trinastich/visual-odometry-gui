/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.panel;

import com.mtm.vogui.models.enums.gui.PanelBorder;
import com.mtm.vogui.gui.swing.utils.SwingUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

@SuppressWarnings("serial")
public class DirectionPanel extends JPanel {
    private double dirX, dirY;
    private boolean borderEnabled = false;
    private float borderThickness = 0;
    private Color borderColor;
    private PanelBorder borderStyle = PanelBorder.Circle;

    private static final int SCALE = 10000;

    public DirectionPanel() {
        this(null, null);
    }

    public DirectionPanel(Integer width, Integer height) {
        super();
        this.setOpaque(false);
        if (width != null && height != null) {
            this.setPreferredSize(new Dimension(width, height));
        }
        this.dirX = this.getWidth() / 2d;
        this.dirY = this.getHeight() / 2d;
        this.repaint();
    }

    public void setDirection(double x, double y) {
        this.dirX = x;
        this.dirY = y;
        this.repaint();
    }

    public void enableBorder(Color borderColor, float borderThickness, PanelBorder borderStyle) {
        this.borderEnabled = true;
        this.borderColor = borderColor;
        this.borderThickness = borderThickness;
        this.borderStyle = borderStyle;
    }

    public void disableBorder() {
        this.borderEnabled = false;
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Center coordinates
        int cx, cy;
        cx = Math.round(this.getWidth() / 2f);
        cy = Math.round(this.getHeight() / 2f);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw axes
        this.drawAxes(g2, cx, cy);

        // Draw direction line
        this.drawLine(g2, cx, cy);

        // Draw border
        this.drawBorder(g2, cx, cy);
    }

    private void drawAxes(@NotNull Graphics2D g2, int cx, int cy) {
        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(0, cy, this.getWidth(), cy);
        g2.drawLine(cx, 0, cx, this.getHeight());
    }

    private void drawLine(@NotNull Graphics2D g2, int cx, int cy) {
        g2.setColor(Color.green);
        g2.setStroke(new BasicStroke(4));

        int scaleFactor = PanelBorder.Circle.equals(borderStyle) ? cx : SCALE;
        g2.drawLine(cx, cy, cx + (int) (scaleFactor * this.dirX), cy - (int) (scaleFactor * this.dirY));
    }

    private void drawBorder(Graphics2D g2, int cx, int cy) {
        // Draw rect or circle border
        if (this.borderEnabled & this.borderThickness > 0) {
            g2.setColor(this.borderColor);
            g2.setStroke(new BasicStroke(this.borderThickness));
            switch (borderStyle) {
                case Rect -> g2.drawRect(
                        (int) (this.borderThickness / 2),
                        (int) (this.borderThickness / 2),
                        (int) (this.getWidth() - this.borderThickness),
                        (int) (this.getHeight() - this.borderThickness)
                );
                case Circle -> {
                    Shape ring = SwingUtils.getRingShape(
                            cx,
                            cy,
                            cx - this.borderThickness,
                            this.borderThickness
                    );
                    g2.draw(ring);
                }
            }
        }
    }
}
