/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.components.common.panel;

import com.mtm.vogui.gui.swing.components.common.button.ImageButton;
import lombok.Builder;
import lombok.Data;

import javax.swing.*;

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
