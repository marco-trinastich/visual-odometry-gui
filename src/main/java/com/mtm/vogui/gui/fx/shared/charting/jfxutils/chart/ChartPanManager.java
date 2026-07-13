/*
 * Copyright 2013 Jason Winnebeck
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mtm.vogui.gui.fx.shared.charting.jfxutils.chart;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import com.mtm.vogui.gui.fx.shared.charting.jfxutils.EventHandlerManager;
import lombok.Getter;
import lombok.Setter;

/**
 * ChartPanManager manages drag gestures on an {@link XYChart} by translating them to panning
 * actions on the chart's axes.
 *
 * @author Jason Winnebeck
 */
public class ChartPanManager {
    @Getter
    private final BooleanProperty panning = new SimpleBooleanProperty(false);

    /**
     * The default mouse filter for the {@link ChartPanManager} filters events unless only primary
     * mouse button (usually left) is depressed.
     */
    public static final EventHandler<MouseEvent> DEFAULT_FILTER = mouseEvent -> {
        if (mouseEvent.getButton() != MouseButton.PRIMARY ||
                mouseEvent.isControlDown() || mouseEvent.getClickCount() >= 2)
            mouseEvent.consume();
    };

    private final EventHandlerManager handlerManager;

    private final ValueAxis<?> xAxis;
    private final ValueAxis<?> yAxis;
    private final XYChartInfo chartInfo;

    private AxisConstraint panMode = AxisConstraint.None;

    /**
     * -- GETTER --
     * Returns the current strategy in use.
     *
     * @see #setAxisConstraintStrategy(AxisConstraintStrategy)
     * -- SETTER --
     * Sets the {@link AxisConstraintStrategy} to use, which determines which axis is allowed for panning. The default
     * implementation is {@link AxisConstraintStrategies#getDefault()}.
     * @see AxisConstraintStrategies
     */
    @Setter
    @Getter
    private AxisConstraintStrategy axisConstraintStrategy = AxisConstraintStrategies.getDefault();

    /**
     * -- GETTER --
     * Returns the mouse filter.
     *
     * @see #setMouseFilter(EventHandler)
     * -- SETTER --
     * Sets the mouse filter for starting the pan action. If the filter consumes the event with
     * {@link Event#consume()}, then the event is ignored. If the filter is null, all events are
     * passed through. The default filter is {@link #DEFAULT_FILTER}.
     */
    @Setter
    @Getter
    private EventHandler<? super MouseEvent> mouseFilter = DEFAULT_FILTER;

    private boolean dragging = false;

    private boolean wasXAnimated;
    private boolean wasYAnimated;

    private double lastX;
    private double lastY;

    public ChartPanManager(XYChart<?, ?> chart) {
        handlerManager = new EventHandlerManager(chart);
        xAxis = (ValueAxis<?>) chart.getXAxis();
        yAxis = (ValueAxis<?>) chart.getYAxis();
        chartInfo = new XYChartInfo(chart, chart);

        handlerManager.addEventHandler(false, MouseEvent.DRAG_DETECTED, mouseEvent -> {
            if (passesFilter(mouseEvent))
                startDrag(mouseEvent);
        });

        handlerManager.addEventHandler(false, MouseEvent.MOUSE_DRAGGED, this::drag);

        handlerManager.addEventHandler(false, MouseEvent.MOUSE_RELEASED, mouseEvent -> release());
    }

    public void start() {
        handlerManager.addAllHandlers();
    }

    public void stop() {
        handlerManager.removeAllHandlers();
        release();
    }

    private boolean passesFilter(MouseEvent event) {
        if (mouseFilter != null) {
            MouseEvent cloned = (MouseEvent) event.clone();
            mouseFilter.handle(cloned);
            return !cloned.isConsumed();
        }

        return true;
    }

    private void startDrag(MouseEvent event) {
        DefaultChartInputContext context = new DefaultChartInputContext(chartInfo, event.getX(), event.getY());
        panMode = axisConstraintStrategy.getConstraint(context);

        if (panMode != AxisConstraint.None) {
            panning.set(true);

            lastX = event.getX();
            lastY = event.getY();

            wasXAnimated = xAxis.getAnimated();
            wasYAnimated = yAxis.getAnimated();

            xAxis.setAnimated(false);
            xAxis.setAutoRanging(false);
            yAxis.setAnimated(false);
            yAxis.setAutoRanging(false);

            dragging = true;
        }
    }

    private void drag(MouseEvent event) {
        if (!dragging)
            return;

        if (panMode == AxisConstraint.Both || panMode == AxisConstraint.Horizontal) {
            double dX = (event.getX() - lastX) / -xAxis.getScale();
            lastX = event.getX();
            xAxis.setAutoRanging(false);
            xAxis.setLowerBound(xAxis.getLowerBound() + dX);
            xAxis.setUpperBound(xAxis.getUpperBound() + dX);
        }

        if (panMode == AxisConstraint.Both || panMode == AxisConstraint.Vertical) {
            double dY = (event.getY() - lastY) / -yAxis.getScale();
            lastY = event.getY();
            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(yAxis.getLowerBound() + dY);
            yAxis.setUpperBound(yAxis.getUpperBound() + dY);
        }
    }

    private void release() {
        if (!dragging)
            return;

        dragging = false;

        xAxis.setAnimated(wasXAnimated);
        yAxis.setAnimated(wasYAnimated);
    }
}
