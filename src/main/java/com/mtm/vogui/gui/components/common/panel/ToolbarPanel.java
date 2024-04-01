package com.mtm.vogui.gui.components.common.panel;

import com.mtm.vogui.gui.components.common.button.ImageButton;
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
