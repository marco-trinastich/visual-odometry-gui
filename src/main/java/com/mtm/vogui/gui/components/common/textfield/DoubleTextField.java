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
public class DoubleTextField extends NumberTextField<Double> {

    public DoubleTextField(@NotNull Consumer<Double> setter, @NotNull Supplier<Double> getter,
                           Double fallback, int columns, int hAlignment) {
        this(NumberConstraints.All, setter, getter, fallback, columns, hAlignment);
    }

    public DoubleTextField(NumberConstraints constraints,
                           @NotNull Consumer<Double> setter,
                           @NotNull Supplier<Double> getter,
                           Double fallback, int columns, int hAlignment) {
        super(constraints, setter, getter, fallback, columns, hAlignment);
    }

    @Override
    protected Double tryParseNumber(String value) {
        return CommonUtils.tryParseDouble(value);
    }

    @Override
    protected Double getNormalizedNumber(@NotNull NumberConstraints constraints,
                                         @NotNull Double value,
                                         @NotNull Double fallback) {
        return CommonUtils.getNormalizedDouble(constraints, value, fallback);
    }
}
