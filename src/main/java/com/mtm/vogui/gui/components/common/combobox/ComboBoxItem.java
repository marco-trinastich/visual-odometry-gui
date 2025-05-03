/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.components.common.combobox;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComboBoxItem<T> {
    private T value;
    private Integer index;
}
