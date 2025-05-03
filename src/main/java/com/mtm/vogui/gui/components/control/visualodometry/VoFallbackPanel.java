/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.components.control.visualodometry;

import javax.swing.*;
import java.awt.*;

public class VoFallbackPanel extends JPanel {

    public VoFallbackPanel() {
        super();
        this.setPreferredSize(new Dimension(this.getPreferredSize().width, 70));
    }
}
