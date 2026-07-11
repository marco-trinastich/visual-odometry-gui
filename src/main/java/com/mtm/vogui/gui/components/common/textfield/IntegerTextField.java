/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.components.common.textfield;

import com.mtm.vogui.models.enums.core.NumberConstraints;
import com.mtm.vogui.utilities.CommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("serial")
public class IntegerTextField extends NumberTextField<Integer> {

    public IntegerTextField(@NotNull Consumer<Integer> setter, @NotNull Supplier<Integer> getter,
                            Integer fallback, int columns, int hAlignment) {
        this(NumberConstraints.All, setter, getter, fallback, columns, hAlignment);
    }

    public IntegerTextField(NumberConstraints constraints,
                            @NotNull Consumer<Integer> setter,
                            @NotNull Supplier<Integer> getter,
                            Integer fallback, int columns, int hAlignment) {
        super(constraints, setter, getter, fallback, columns, hAlignment);
    }

    @Override
    protected Integer tryParseNumber(String value) {
        return CommonUtils.tryParseInteger(value);
    }

    @Override
    protected Integer getNormalizedNumber(@NotNull NumberConstraints constraints,
                                          @NotNull Integer value,
                                          @NotNull Integer fallback) {
        return CommonUtils.getNormalizedInteger(constraints, value, fallback);
    }
}
