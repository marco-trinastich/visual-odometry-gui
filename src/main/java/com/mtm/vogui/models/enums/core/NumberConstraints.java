/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.models.enums.core;

import com.mtm.vogui.models.interfaces.Comparable;

public enum NumberConstraints implements Comparable {
    All,
    Positive,
    StrictlyPositive,
    Negative,
    StrictlyNegative,
    NotZero
}
