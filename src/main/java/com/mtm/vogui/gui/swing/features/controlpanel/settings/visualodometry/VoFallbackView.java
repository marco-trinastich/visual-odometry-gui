/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.features.controlpanel.settings.visualodometry;

import javax.swing.*;
import java.awt.*;

/**
 * Placeholder VO parameters section (humble view) shown for VO types without their own panel.
 * Sub-view of {@link VoSettingsView}, swapped into the VO scroll pane.
 */
public class VoFallbackView {

    private final JPanel panel;

    public VoFallbackView() {
        this.panel = new JPanel();
        this.panel.setPreferredSize(new Dimension(this.panel.getPreferredSize().width, 70));
    }

    /**
     * Section panel, consumed by {@link VoSettingsView} composition only.
     */
    public JPanel panel() {
        return this.panel;
    }
}
