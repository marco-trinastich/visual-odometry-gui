/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing;

import boofcv.gui.image.ImagePanel;
import com.mtm.vogui.gui.swing.components.chart.ChartScrollPane;
import com.mtm.vogui.gui.swing.components.info.InfoScrollPane;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.Data;

import javax.swing.*;

@Data
@ApplicationScoped
public class GuiController {
    // Input Video
    private ImagePanel inputVideoPanel;
    private JFrame inputVideoFrame;

    // Output Video
    private ImagePanel outputVideoPanel;
    private JFrame outputVideoFrame;

    // Chart
    private ChartScrollPane chartXZPanel;
    private ChartScrollPane chartYPanel;
    private InfoScrollPane infoPanel;
}
