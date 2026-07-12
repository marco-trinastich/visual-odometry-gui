/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.panel;

import lombok.Builder;
import lombok.Data;

import javax.swing.*;

import com.mtm.vogui.gui.swing.shared.components.button.ImageButton;

@Data
@Builder
public class ToolbarPanel {
    private ImageButton btnSettings;
    private ImageButton btnStartVO;
    private ImageButton btnPauseVO;
    private ImageButton btnResetVO;
    private ImageButton btnStopVO;
    private ImageButton btnClearVO;
    private JButton btnTimedProcessingVO;
}
