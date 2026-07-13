/*
 * Visual Odometry GUI - Copyright (c) 2014-2024 Marco Trinastich
 * Licensed under GNU GPL v3 - see LICENSE file for details
 */

package com.mtm.vogui.gui.fx.shared.converters;

import com.mtm.vogui.models.interfaces.WithValue;
import javafx.util.StringConverter;

/**
 * Renders any {@link WithValue} (the model's display-string contract) in a JavaFX {@code ComboBox} /
 * {@code ChoiceBox} cell via its {@code value()}. App-blind and reusable across every settings combo
 * whose items are enums implementing {@link WithValue}. {@link #fromString} is unused for
 * non-editable combos; editable combos that must parse user text use a dedicated converter instead.
 */
public class WithValueStringConverter<T extends WithValue> extends StringConverter<T> {

    @Override
    public String toString(T value) {
        return value == null ? "" : value.value();
    }

    @Override
    public T fromString(String string) {
        // Non-editable combos never parse text back; a typed value has no unique WithValue instance.
        return null;
    }
}
