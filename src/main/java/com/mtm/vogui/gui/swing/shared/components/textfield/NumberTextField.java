/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.swing.shared.components.textfield;

import com.mtm.vogui.models.enums.core.NumberConstraints;
import com.mtm.vogui.gui.swing.shared.listeners.NumberTextFieldListener;
import com.mtm.vogui.gui.swing.utils.SwingUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("serial")
public abstract class NumberTextField<T extends Number> extends JTextField {
    private final NumberConstraints constraints;
    private final Consumer<T> setter;
    private final Supplier<T> getter;

    public NumberTextField(@NotNull Consumer<T> setter, @NotNull Supplier<T> getter, @NotNull T fallback,
                           int columns, int hAlignment) {
        this(NumberConstraints.All, setter, getter, fallback, columns, hAlignment);
    }

    public NumberTextField(NumberConstraints constraints, @NotNull Consumer<T> setter, @NotNull Supplier<T> getter,
                           @NotNull T fallback, int columns, int hAlignment) {
        super();

        this.constraints = constraints;
        this.setter = setter;
        this.getter = getter;

        this.updateModel(this.getter.get(), fallback);
        this.setColumns(columns);
        this.setHorizontalAlignment(hAlignment);
        this.addFocusListener(new NumberTextFieldListener());
    }

    public void refreshModel() {
        this.updateModel(this.getter.get());
    }

    public void updateModel(String value) {
        this.updateModel(this.tryParseNumber(value));
    }

    public void updateModel(@Nullable T value) {
        this.updateModel(value, this.getter.get());
    }

    public void updateModel(@Nullable T value, @NotNull T fallback) {
        var newValue = fallback;
        if (value != null) {
            newValue = this.getNormalizedNumber(this.constraints, value, fallback);
        }
        SwingUtils.setTextIfNeeded(this, newValue);
        if (!Objects.equals(this.getter.get(), newValue)) {
            this.setter.accept(newValue);
        }
    }

    protected abstract T tryParseNumber(String value);

    protected abstract T getNormalizedNumber(@NotNull NumberConstraints constraints,
                                             @NotNull T value,
                                             @NotNull T fallback);
}
