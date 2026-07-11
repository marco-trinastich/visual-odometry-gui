/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.components.common.textfield;

import com.mtm.vogui.models.enums.core.NumberConstraints;
import com.mtm.vogui.utilities.CommonUtils;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("serial")
public class FloatTextField extends NumberTextField<Float> {

    public FloatTextField(@NotNull Consumer<Float> setter, @NotNull Supplier<Float> getter,
                          Float fallback, int columns, int hAlignment) {
        this(NumberConstraints.All, setter, getter, fallback, columns, hAlignment);
    }

    public FloatTextField(NumberConstraints constraints,
                          @NotNull Consumer<Float> setter,
                          @NotNull Supplier<Float> getter,
                          Float fallback, int columns, int hAlignment) {
        super(constraints, setter, getter, fallback, columns, hAlignment);
    }

    @Override
    protected Float tryParseNumber(String value) {
        return CommonUtils.tryParseFloat(value);
    }

    @Override
    protected Float getNormalizedNumber(@NotNull NumberConstraints constraints,
                                        @NotNull Float value,
                                        @NotNull Float fallback) {
        return CommonUtils.getNormalizedFloat(constraints, value, fallback);
    }
}
