/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.chart;


import com.mtm.vogui.gui.swing.utils.SwingUtils;
import io.quarkus.logging.Log;
import georegression.struct.point.Point2D_F64;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Chart Panel (ChartScrollPane Component)
 * <p/>
 * Component responsible for chart painting. {@link #paintComponent} is side-effect free: the
 * preferred size needed to fit every point is computed by {@link #recomputeBounds}, driven by
 * data changes ({@code ChartScrollPane.addPoint}/{@code resetSize}), not during painting.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("serial")
public class ChartPanel extends JPanel {

    private static final int BOUNDS_MARGIN = 20;

    private final ChartState state;
    private final ChartStyle style;

    public ChartPanel(ChartState state, ChartStyle style) {
        super();

        // Appearance
        this.setOpaque(false);

        this.state = state;
        this.style = style;
    }

    /**
     * Sizes the panel (and shifts non-centered origins) so every point fits at the current scale,
     * starting from the given base (viewport) extent. Replaces the legacy grow-inside-paint.
     * EDT only.
     */
    public void recomputeBounds(int baseWidth, int baseHeight) {
        int prefW = Math.max(baseWidth, 1);
        int prefH = Math.max(baseHeight, 1);

        double scale = this.style.chartScale();
        boolean any = false;
        double minXs = 0, maxXs = 0, minYs = 0, maxYs = 0;
        for (ArrayList<Point2D_F64> chart : this.state.series()) {
            for (Point2D_F64 point : chart) {
                if (point == null) {
                    continue;
                }
                double xs = point.getX() * scale;
                double ys = point.getY() * scale;
                if (!any) {
                    minXs = maxXs = xs;
                    minYs = maxYs = ys;
                    any = true;
                } else {
                    minXs = Math.min(minXs, xs);
                    maxXs = Math.max(maxXs, xs);
                    minYs = Math.min(minYs, ys);
                    maxYs = Math.max(maxYs, ys);
                }
            }
        }

        if (any) {
            // X axis: centered origins are placed at width/2 by paint, so just size to the extent;
            // non-centered origins shift right to keep the leftmost point visible.
            if (this.style.centeredOriginX()) {
                double ext = Math.max(Math.abs(minXs), Math.abs(maxXs));
                prefW = Math.max(prefW, (int) Math.ceil(2 * (ext + BOUNDS_MARGIN)));
            } else {
                int originX = Math.max(this.state.initX(), (int) Math.ceil(-minXs) + BOUNDS_MARGIN);
                prefW = Math.max(prefW, originX + (int) Math.ceil(maxXs) + BOUNDS_MARGIN);
                this.state.originX(originX);
            }

            // Y axis is inverted on screen (dispy = originY - y*scale)
            if (this.style.centeredOriginY()) {
                double ext = Math.max(Math.abs(minYs), Math.abs(maxYs));
                prefH = Math.max(prefH, (int) Math.ceil(2 * (ext + BOUNDS_MARGIN)));
            } else {
                int originY = Math.max(this.state.initY(), (int) Math.ceil(maxYs) + BOUNDS_MARGIN);
                prefH = Math.max(prefH, originY + (int) Math.ceil(-minYs) + BOUNDS_MARGIN);
                this.state.originY(originY);
            }
        } else {
            if (!this.style.centeredOriginX()) {
                this.state.originX(this.state.initX());
            }
            if (!this.style.centeredOriginY()) {
                this.state.originY(this.state.initY());
            }
        }

        this.setPreferredSize(new Dimension(prefW, prefH));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        this.applyCentering();

        Graphics2D g2 = (Graphics2D) g;

        // Chart update
        if (g2 == null) return;
        if (this.state.series() == null || !this.state.hasPoints())
            return;

        try {

            if (this.style.showAxis()) {
                g2.setColor(this.style.axisColor() != null ? this.style.axisColor() : Color.black);
                g2.drawLine(0, this.state.originY(), this.getWidth(), this.state().originY());
                g2.drawLine(this.state.originX(), 0, this.state.originX(), this.getHeight());


                if (this.style.showAxisUnits()) {
                    if (this.style.axisUnitsColor() != null) g2.setColor(this.style.axisUnitsColor());
                    g2.setFont(new Font("Arial", Font.BOLD, 11));

                    for (int x = 0; x < this.getWidth(); x++) {

                        int relative_x = x - this.state.originX();

                        if ((relative_x) % (10 * this.style.chartScale()) == 0)
                            g2.drawLine(x, this.state.originY() - 2, x, this.state.originY() + 2);
                        if ((relative_x) % (50 * this.style.chartScale()) == 0) {
                            g2.drawLine(x + 1, this.state.originY() - 2, x + 1, this.state.originY() + 2);
                            if (relative_x == 0) {
                                g2.drawString(String.valueOf(Math.round(relative_x / this.style.chartScale())), x + 3, this.state.originY() + 15);
                            } else if (relative_x > 0 && relative_x < 100) {
                                g2.drawString(String.valueOf(Math.round(relative_x / this.style.chartScale())), x - 7, this.state.originY() + 15);
                            } else if (relative_x >= 100) {
                                g2.drawString(String.valueOf(Math.round(relative_x / this.style.chartScale())), x - 10, this.state.originY() + 15);
                            } else if (relative_x < 0 && relative_x > -100) {
                                g2.drawString(String.valueOf(Math.round(relative_x / this.style.chartScale())), x - 12, this.state.originY() + 15);
                            } else if (relative_x <= -100) {
                                g2.drawString(String.valueOf(Math.round(relative_x / this.style.chartScale())), x - 15, this.state.originY() + 15);
                            }

                        }
                    }
                    for (int y = 0; y < this.getHeight(); y++) {

                        int relative_y = this.state.originY() - y; //Because Height values in windows are measured from 0 on the TOP
                        //so the Y values must be inverted to be positive on TOP
                        if ((relative_y) % (10 * this.style.chartScale()) == 0)
                            g2.drawLine(this.state.originX() - 2, y, this.state.originX() + 2, y);
                        if ((relative_y) % (50 * this.style.chartScale()) == 0) {
                            g2.drawLine(this.state.originX() - 2, y + 1, this.state.originX() + 2, y + 1);
                            if (relative_y > 0) {
                                g2.drawString(String.valueOf(Math.round(relative_y / this.style.chartScale())), this.state.originX() + 6, y + 4);
                            } else if (relative_y < 0) {
                                g2.drawString(String.valueOf(Math.round(relative_y / this.style.chartScale())), this.state.originX() + 9, y + 4);
                            }

                        }
                    }
                }
            }

            Color drawColor = this.style.resolvedPlotColor();
            g2.setColor(drawColor);
            for (int i = 0; i < this.state.series().size(); i++) {
                ArrayList<Point2D_F64> chart = this.state.series().get(i);

                if (this.style.multipleColors() && i > 0) {
                    g2.setColor(SwingUtils.generateColor(drawColor, i));
                }

                for (Point2D_F64 point : chart) {
                    if (point == null) {
                        return;
                    }

                    int scaledx = (int) (Math.round(point.getX() * this.style.chartScale()));
                    int scaledy = (int) (Math.round(point.getY() * this.style.chartScale()));

                    int dispx = this.state.originX() + scaledx;
                    int dispy = this.state.originY() - scaledy;

                    if (!this.style.thickPoints()) {
                        g2.fillOval(dispx - 2, dispy - 2, 4, 4);            //Circles Rendering
                    } else {
                        g2.fill3DRect(dispx - 25, dispy - 25, 50, 50, true);    //3D Thick Rendering
                    }
                }
            }
        } catch (Exception exc) {
            Log.debugf("Chart rendering failed: %s", exc.toString());
        }
    }

    private void applyCentering() {
        if (this.style.centeredOriginX()) {
            this.state.originX(Math.round(this.getWidth() / 2));
        }
        if (this.style.centeredOriginY()) {
            this.state.originY(Math.round(this.getHeight() / 2));
        }
    }
}
