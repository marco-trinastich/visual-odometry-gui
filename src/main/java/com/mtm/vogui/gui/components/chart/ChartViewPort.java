package com.mtm.vogui.gui.components.chart;


import com.mtm.vogui.models.constants.GuiConstants;
import com.mtm.vogui.utilities.GuiUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Chart ViewPort (ChartScrollPane Component)
 * </p>
 * Component painting above and below chart panel (legend, permanent axis names, etc.)
 *
 * @author Marco Trinastich 2015-2024
 */
public class ChartViewPort extends JViewport {
    private final ChartState state;
    private final ChartSettings settings;
    private final JScrollBar horizontalBar;
    private final JScrollBar verticalBar;

    public ChartViewPort(ChartState state, ChartSettings settings, @NotNull JScrollPane scrollPane) {
        // Key element to redraw only needed bits
        this.setOpaque(false);

        this.state = state;
        this.settings = settings;
        this.horizontalBar = scrollPane.getHorizontalScrollBar();
        this.verticalBar = scrollPane.getVerticalScrollBar();
    }

    /**
     * ViewPort paint event --> paints ABOVE contained chart
     *
     * @param g the <code>Graphics</code> context within which to paint
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // Draw legend
        if (this.shouldShowLegend()) {
            Color drawColor = GuiUtils.getPlotColor(this.settings);
            for (int i = 0; i < this.state.series().size(); i++) {
                if (this.state.series().get(i).isEmpty()) {
                    continue;
                }

                // Set current chart color
                g.setColor(this.settings.multipleColors() ? GuiUtils.generateColor(drawColor, i) : drawColor);
                // Draw chart name
                g.drawString(String.format(GuiConstants.CHART_START, i + 1),
                        this.getLegendItemX(),
                        this.getLegendItemY(i)
                );
            }
        }
    }

    /**
     * ViewPort paintComponent event --> paints BELOW contained chart
     *
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background below everything
        if (this.settings.backgroundColor() != null) {
            g.setColor(this.settings.backgroundColor());
            g.fillRect(0, 0, this.getWidth(), this.getHeight());
        }

        // Draw axis names
        if (this.shouldShowPermanentAxisNames()) {
            g.setColor(GuiUtils.getAxisNamesColor(this.settings));

            if (this.isXAxisInViewport()) {
                String xAxisName = this.settings.xAxisName().value();
                // Left: viewport end
                int left = this.getWidth() - (15 + GuiUtils.getStringDisplaySize(xAxisName));
                // Top: above X axis
                int top = (this.state.originY() - this.verticalBar.getValue()) - 10;
                g.drawString(xAxisName, left, top);
            }

            if (this.isYAxisInViewport()) {
                String yAxisName = this.settings.yAxisName().value();
                // Left: left of Y axis
                int left = (this.state.originX() - this.horizontalBar.getValue()) -
                        (15 + GuiUtils.getStringDisplaySize(yAxisName));
                // Top: viewport top
                int top = 15;
                g.drawString(yAxisName, left, top);
            }
        }
    }

    // Positions
    private int getLegendItemX() {
        return this.getWidth() - 70;
    }

    private int getLegendItemY(int chartId) {
        return 20 + (15 * chartId);
    }

    // Conditions
    private boolean shouldShowLegend() {
        return this.state.hasPoints() &&
                this.settings.showLegend();
    }

    private boolean shouldShowPermanentAxisNames() {
        return this.state.hasPoints() &&
                this.settings.showAxis() &&
                this.settings.showPermanentAxisNames();
    }

    private boolean isXAxisInViewport() {
        return this.verticalBar.getValue() >= (this.state.originY() - this.getHeight()) &&
                this.verticalBar.getValue() < this.state.originY();
    }

    private boolean isYAxisInViewport() {
        return this.horizontalBar.getValue() >= (this.state.originX() - this.getWidth()) &&
                this.horizontalBar.getValue() < this.state.originX();
    }
}
