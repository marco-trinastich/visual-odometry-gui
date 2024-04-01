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
