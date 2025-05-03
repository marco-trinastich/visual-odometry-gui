/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

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
