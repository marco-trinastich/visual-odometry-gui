/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.listeners;

import javax.swing.*;

import com.mtm.vogui.gui.swing.shared.components.chart.ChartScrollPane;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;

public class MaximizeOnDblClickListener implements MouseListener {
    private final JComponent componentToMaximize;
    private final JFrame container;
    private final SpringLayout maximizeComponentLayout;
    private LayoutManager currentLayout;
    private boolean isMaximized;

    public MaximizeOnDblClickListener(JComponent componentToMaximize, JFrame container) {
        // Component to Maximize
        this.componentToMaximize = componentToMaximize;
        // Container into which the component resides and will be maximized
        this.container = container;
        // Predefined layout for component maximization
        this.maximizeComponentLayout = getMaximizeComponentLayout();
        // Status (initialized to false)
        this.isMaximized = false;
    }

    @Override
    public void mouseClicked(MouseEvent evt) {
        if (this.componentToMaximize == null || this.container == null)
            return;

        // On double-click
        if (evt.getClickCount() == 2) {
            // If component is not maximized
            if (!isMaximized) {
                isMaximized = true;

                // Save old container layout
                currentLayout = container.getContentPane().getLayout();

                // Hide all container components except componentToMaximize
                Arrays.stream(container.getContentPane().getComponents())
                        .filter((Component component) -> !component.equals(componentToMaximize))
                        .forEach((Component component) -> component.setVisible(false));

                // Apply maximizeComponentLayout to the container
                container.getContentPane().setLayout(maximizeComponentLayout);
            } else {
                // If component is already maximized
                isMaximized = false;

                // Show all previously hidden components
                Arrays.stream(container.getContentPane().getComponents())
                        .filter((Component component) -> !component.equals(componentToMaximize))
                        .forEach((Component component) -> component.setVisible(true));

                // Restore old layout
                container.getContentPane().setLayout(currentLayout);
                currentLayout = null;
            }

            // Revalidate container (and repaint)
            container.revalidate();

            // Repaint componentToMaximize
            componentToMaximize.repaint();

            // If we are maximizing a ChartScrollPane, ViewPort needs explicit repainting
            // (to correctly display e.g. axis names, X, Y)
            if (componentToMaximize instanceof ChartScrollPane chartPanel) {
                chartPanel.repaintViewport();
            }
        }
    }

    private SpringLayout getMaximizeComponentLayout() {
        SpringLayout maximizeComponentLayout = new SpringLayout();
        maximizeComponentLayout.putConstraint(
                SpringLayout.NORTH,
                componentToMaximize,
                5,
                SpringLayout.NORTH,
                container.getContentPane()
        );
        maximizeComponentLayout.putConstraint(
                SpringLayout.WEST,
                componentToMaximize,
                5,
                SpringLayout.WEST,
                container.getContentPane()
        );
        maximizeComponentLayout.putConstraint(
                SpringLayout.EAST,
                componentToMaximize,
                -5,
                SpringLayout.EAST,
                container.getContentPane()
        );
        maximizeComponentLayout.putConstraint(
                SpringLayout.SOUTH,
                componentToMaximize,
                -5,
                SpringLayout.SOUTH,
                container.getContentPane()
        );

        return maximizeComponentLayout;
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
