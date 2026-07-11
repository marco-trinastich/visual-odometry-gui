/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.components.common.border;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Map;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.AbstractBorder;

import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.utilities.ImageUtils;

@SuppressWarnings("serial")
public class RoundedCornerBorder extends AbstractBorder {
    protected int arch = 12;
    protected Color baseColor;
    protected Color highlightColor;
    protected float withBorder = 1.2F;
    protected String title;

    public RoundedCornerBorder(Color baseColor, String title) {
        this(baseColor, null, title, null, null);
    }

    public RoundedCornerBorder(Color baseColor, Color highlightColor) {
        this(baseColor, highlightColor, null, null, null);
    }

    public RoundedCornerBorder(Color baseColor, Color highlightColor, String title, Integer arch, Float withBorder) {
        this.baseColor = baseColor;
        this.highlightColor = highlightColor != null ? highlightColor : baseColor;
        this.arch = arch != null ? arch : this.arch;
        this.withBorder = withBorder != null ? withBorder : this.withBorder;
        this.title = title;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color paintColor = c.hasFocus() ? this.highlightColor : this.baseColor;

        Graphics2D g2 = (Graphics2D) g.create();

        // Get high quality hints
        RenderingHints hints = ImageUtils.getHighQualityRenderingHints();
        Map<RenderingHints.Key, Object> desktopHints = ImageUtils.getDesktopHints();
        if (desktopHints != null) {
            hints.putAll(desktopHints);
        }
        g2.setRenderingHints(hints);

        g2.setStroke(new BasicStroke(this.withBorder));

        // Define border area
        int r = this.arch;
        int w = width - 1;
        int h = height - 1;
        Area round = new Area(new RoundRectangle2D.Double(x, y, w, h, r, r));

        // Set color
        Color backgroundColor;
        if (c instanceof JPopupMenu) {
            backgroundColor = c.getBackground();
            g2.setPaint(backgroundColor);
            g2.fill(round);
        } else {
            Container parent = c.getParent();
            if (Objects.nonNull(parent)) {
                backgroundColor = parent.getBackground();
                g2.setPaint(backgroundColor);
                Area corner = new Area(new RoundRectangle2D.Float((float) x, (float) y, (float) width, (float) height,
                        (float) r, (float) r));
                corner.subtract(round);
                g2.fill(corner);
            } else {
                backgroundColor = Color.WHITE;
            }
        }

        // Draw border
        g2.setPaint(paintColor);
        g2.draw(round);

        // Draw title
        if (this.title != null) {
            Font font = GuiConstants.DEFAULT_FONT;
            Rectangle2D fontBounds = font.getStringBounds(this.title, g2.getFontRenderContext());
            g2.setPaint(backgroundColor);
            g2.drawRect(x + 15, y, (int) fontBounds.getWidth(), (int) fontBounds.getHeight());
            g2.setPaint(paintColor);
            g2.drawString(this.title, x + 15, y);
        }

        g2.dispose();
    }

    public Insets getBorderInsets(Component c) {
        return new Insets(4, 8, 4, 8);
    }

    public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(4, 8, 4, 8);
        return insets;
    }
}
