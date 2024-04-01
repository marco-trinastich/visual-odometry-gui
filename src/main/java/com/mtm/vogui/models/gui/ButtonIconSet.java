package com.mtm.vogui.models.gui;

import lombok.Builder;
import lombok.Data;

import javax.swing.*;

@Data
@Builder
public class ButtonIconSet {
    private ImageIcon defaultIcon;
    private ImageIcon hoverIcon;
    private ImageIcon clickedIcon;
    private ImageIcon disabledIcon;
}
