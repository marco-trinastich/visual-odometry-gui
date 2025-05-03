/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.components.chart;


import com.mtm.vogui.utilities.GuiUtils;
import georegression.struct.point.Point2D_F64;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

/**
 * Chart Panel (ChartScrollPane Component)
 * </p>
 * Component responsible for chart painting
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ChartPanel extends JPanel {

    private final ChartState state;
    private final ChartSettings settings;

    private boolean moveToOriginFlag = false;
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    private static final Font AXIS_NAME_FONT = new Font("Arial", Font.PLAIN, 14);

    public ChartPanel(ChartState state, ChartSettings settings) {
        super();

        // Appearance
        this.setOpaque(false);

        this.state = state;
        this.settings = settings;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        this.changes.addPropertyChangeListener(l);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (this.settings.centeredOriginX()) {
            this.state.originX(Math.round(this.getWidth() / 2));
        }
        if (this.settings.centeredOriginY()) {
            this.state.originY(Math.round(this.getHeight() / 2));
        }

        int chart_width = this.getWidth();
        int chart_height = this.getHeight();


        Graphics2D g2 = (Graphics2D) g;

        // Chart update
        if (g2 == null) return;
        if (this.state.series() == null || !this.state.hasPoints())
            return;

        int currentChart = 0;
        try {

            if (this.settings.showAxis()) {
                g2.setColor(this.settings.axisColor() != null ? this.settings.axisColor() : Color.black);
                g2.drawLine(0, this.state.originY(), this.getWidth(), this.state().originY());
                g2.drawLine(this.state.originX(), 0, this.state.originX(), this.getHeight());


                if (this.settings.showAxisUnits()) {
                    if (this.settings.axisUnitsColor() != null) g2.setColor(this.settings.axisUnitsColor());
                    g2.setFont(new Font("Arial", Font.BOLD, 11));

                    for (int x = 0; x < this.getWidth(); x++) {

                        int relative_x = x - this.state.originX();

                        if ((relative_x) % (10 * this.settings.chartScale()) == 0)
                            g2.drawLine(x, this.state.originY() - 2, x, this.state.originY() + 2);
                        if ((relative_x) % (50 * this.settings.chartScale()) == 0) {
                            g2.drawLine(x + 1, this.state.originY() - 2, x + 1, this.state.originY() + 2);
                            if (relative_x == 0) {
                                g2.drawString(String.valueOf(Math.round(relative_x / this.settings.chartScale())), x + 3, this.state.originY() + 15);
                            } else if (relative_x > 0 && relative_x < 100) {
                                g2.drawString(String.valueOf(Math.round(relative_x / this.settings.chartScale())), x - 7, this.state.originY() + 15);
                            } else if (relative_x >= 100) {
                                g2.drawString(String.valueOf(Math.round(relative_x / this.settings.chartScale())), x - 10, this.state.originY() + 15);
                            } else if (relative_x < 0 && relative_x > -100) {
                                g2.drawString(String.valueOf(Math.round(relative_x / this.settings.chartScale())), x - 12, this.state.originY() + 15);
                            } else if (relative_x <= -100) {
                                g2.drawString(String.valueOf(Math.round(relative_x / this.settings.chartScale())), x - 15, this.state.originY() + 15);
                            }

                        }
                    }
                    for (int y = 0; y < this.getHeight(); y++) {

                        int relative_y = this.state.originY() - y; //Because Height values in windows are measured from 0 on the TOP
                        //so the Y values must be inverted to be positive on TOP
                        if ((relative_y) % (10 * this.settings.chartScale()) == 0)
                            g2.drawLine(this.state.originX() - 2, y, this.state.originX() + 2, y);
                        if ((relative_y) % (50 * this.settings.chartScale()) == 0) {
                            g2.drawLine(this.state.originX() - 2, y + 1, this.state.originX() + 2, y + 1);
                            if (relative_y > 0) {
                                g2.drawString(String.valueOf(Math.round(relative_y / this.settings.chartScale())), this.state.originX() + 6, y + 4);
                            } else if (relative_y < 0) {
                                g2.drawString(String.valueOf(Math.round(relative_y / this.settings.chartScale())), this.state.originX() + 9, y + 4);
                            }

                        }
                    }
                }
            }

            Color drawColor = this.settings.plotColor() != null ? this.settings.plotColor() : Color.blue;
            g2.setColor(drawColor);
            for (int i = 0; i < this.state.series().size(); i++) {
                ArrayList<Point2D_F64> chart = this.state.series().get(i);

                if (this.settings.multipleColors() && i > 0) {
                    g2.setColor(GuiUtils.generateColor(drawColor, i));
                }

                for (Point2D_F64 point : chart) {
                    if (point == null) {
                        return;
                    }

                    //int posx=200,posz=400;
                    int scaledx = (int) (Math.round(point.getX() * this.settings.chartScale()));
                    int scaledy = (int) (Math.round(point.getY() * this.settings.chartScale()));

                    int dispx = this.state.originX() + scaledx;
                    int dispy = this.state.originY() - scaledy;


                    //CHART ADJUSTMENT IN CASE OF POINTS OUT OF THE CHART SIZE (AUTO-RESIZE)							if(dispx<1 || dispy<1 || dispx>chart_width || dispy>chart_height){
                    if (dispx > chart_width) {
                        int new_width = dispx > (chart_width * 2) ? dispx : (chart_width * 2);
                        setPreferredSize(new Dimension(new_width, chart_height));
                        this.changes.firePropertyChange("repaint", null, null);
                        return;
                    }

                    if (dispy > chart_height) {
                        int new_height = dispy > (chart_height * 2) ? dispy : (chart_height * 2);
                        setPreferredSize(new Dimension(chart_width, new_height));
                        this.changes.firePropertyChange("repaint", null, null);
                        return;
                    }

                    if (dispx < 1) {
                        int new_width = dispx < -(chart_width * 2) ? dispx : (chart_width * 2);
                        if (!this.settings.centeredOriginX()) {
                            //this.state.setOriginX(this.state.getOriginX() + 1-dispx); 	//MOTO FLUIDO X (Origine spostata leggermente, Grafico si ridimensiona in modo aderente ai punti)
                            this.state.originX(this.state.originX() + (int) Math.round(new_width / 2)); //Origine molto spostata, Grafico viene raddoppiato nella dimensione sforata (moto meno fluido)
                        }

                        setPreferredSize(new Dimension((int) (new_width), chart_height));
                        this.changes.firePropertyChange("repaint", null, null);
                        return;
                    }

                    if (dispy < 1) {
                        int new_height = dispy < -(chart_height * 2) ? dispy : (chart_height * 2);

                        if (!this.settings.centeredOriginY()) {
                            //this.state.setOriginY(this.state.getOriginY() + 1-dispy);		//MOTO FLUIDO Y (Origine spostata leggermente, Grafico si ridimensiona in modo aderente ai punti)
                            this.state.originY(this.state.originY() + (int) Math.round(new_height / 2)); //Origine molto spostata, Grafico viene raddoppiato nella dimensione sforata (moto meno fluido)
                        }

                        setPreferredSize(new Dimension(chart_width, (int) (new_height)));
                        this.changes.firePropertyChange("repaint", null, null);
                        return;
                    }


                    //g1.drawLine(dispx, dispy, dispx+5, dispy);	//LINES RENDERING

                    //thick_points=true;
                    if (!this.settings.thickPoints()) {
                        g2.fillOval(dispx - 2, dispy - 2, 4, 4);            //Circles Rendering
                    } else {
                        g2.fill3DRect(dispx - 25, dispy - 25, 50, 50, true);    //3D Thick Rendering
                    }
                }
            }


            // Draw axis names only at axis ends
            if (this.shouldShowAxisNames()) {
                g2.setFont(AXIS_NAME_FONT);
                g2.setColor(GuiUtils.getAxisNamesColor(this.settings));

                String xAxisName = this.settings.xAxisName().value();
                // Left: X axis end
                int left = this.getWidth() - (15 + GuiUtils.getStringDisplaySize(xAxisName));
                // Top: above X axis
                int top = this.state.originY() - 10;
                g2.drawString(xAxisName, left, top);

                String yAxisName = this.settings.yAxisName().value();
                // Left: left of Y axis
                left = this.state.originX() - (15 + GuiUtils.getStringDisplaySize(yAxisName));
                // top: Y axis end
                top = 15;
                g2.drawString(yAxisName, left, top);
            }
        } catch (Exception e) {
        }

        if (moveToOriginFlag) {
            changes.firePropertyChange("moveToOrigin", null, null);
            moveToOriginFlag = false;
        }
    }

    private boolean shouldShowAxisNames() {
        return this.state.hasPoints() &&
                this.settings.showAxis() &&
                !this.settings.showPermanentAxisNames();
    }
}
